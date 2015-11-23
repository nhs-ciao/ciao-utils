package uk.nhs.ciao.spine.pds.route;

import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangeException;
import org.apache.camel.ExchangePattern;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;

import uk.nhs.ciao.camel.BaseRouteBuilder;
import uk.nhs.ciao.spine.pds.PDSException;
import uk.nhs.ciao.spine.pds.hl7.HL7ResponseParser;
import uk.nhs.ciao.spine.pds.model.SimpleTrace;

/**
 * Builds routes associated with the PDS service facade
 * <p>
 * The main route URI is {@link #pdsUri} - if using the Java
 * {@link uk.nhs.ciao.spine.pds.PersonalDemographicsService} class it should 
 * be configured with a matching URI, other Camel routes can also
 * be configured to target this URI.
 * <p>
 * The service determines which type of request to perform based on the type
 * of object used for the IN message body: e.g. {@link SimpleTrace} should
 * be used when a simple trace -> patient lookup is required.
 * <p>
 * The output message will containing an object appropriate to the type
 * of request, or the exception will contain a {@link PDSException} if
 * the request failed. For {@link SimpleTrace} requests, the response type
 * is a {@link uk.nhs.ciao.model.Patient} - which may be null if no patient
 * was found and no errors occurred.
 * 
 * @see uk.nhs.ciao.spine.pds.PersonalDemographicsService
 */
public class PDSRoutes extends BaseRouteBuilder {
	private String serviceFacadeUri = "direct:pds";
	private String requestSenderUri = "direct:pdsRequestSender";
	private String pdsUri;
	private String requestWiretapUri = "jms:ciao-pdsRequestAudit";
	private String responseWiretapUri = "jms:ciao-pdsResponseAudit";
	private String simpleTraceUri = "direct:pdsSimpleTrace";
	private String payloadBuilderRef = "payloadBuilder";
		
	/**
	 * The main service facade
	 */
	public void setServiceFacadeUri(final String serviceFacadeUri) {
		this.serviceFacadeUri = serviceFacadeUri;
	}
	
	/**
	 * URI of the internal PDS request sender
	 * <p>
	 * This route acts as an HTTP client of the remote PDS server
	 */
	public void setRequestSenderUri(final String requestSenderUri) {
		this.requestSenderUri = requestSenderUri;
	}
	
	/**
	 * The target URI of the remote PDS server
	 */
	public void setPdsUri(final String pdsUri) {
		this.pdsUri = pdsUri;
	}
	
	/**
	 * URI to send copies of all outgoing PDS requests to
	 */
	public void setRequestWiretapUri(final String requestWiretapUri) {
		this.requestWiretapUri = requestWiretapUri;
	}
	
	/**
	 * URI to send copies of all incoming PDS responses to
	 */
	public void setResponseWiretapUri(final String responseWiretapUri) {
		this.responseWiretapUri = responseWiretapUri;
	}
	
	/**
	 * URI of the internal simple trace route
	 */
	public void setSimpleTraceUri(final String simpleTraceUri) {
		this.simpleTraceUri = simpleTraceUri;
	}
	
	/**
	 * ID/ref for the PDS request payload builder
	 */
	public void setPayloadBuilderRef(final String payloadBuilderRef) {
		this.payloadBuilderRef = payloadBuilderRef;
	}
	
	@Override
	public void configure() throws Exception {
		addDefaultProperties();
		
		addServiceFacadeRoute();
		addRequestSenderRoute();
		addSimpleTraceRoute();
	}
	
	private void addDefaultProperties() throws Exception {
		if (Strings.isNullOrEmpty(pdsUri)) {
			pdsUri = getContext().resolvePropertyPlaceholders("https://{{PDSURL}}");
		}
	}
	
	private void addServiceFacadeRoute() throws Exception {
		from(serviceFacadeUri)
			.choice()
				.when(body().isInstanceOf(SimpleTrace.class))
				.to(simpleTraceUri)
			.endChoice()
			.otherwise()
				.throwException(new PDSException("Unsupported request type"))
			.endChoice()
		.end();
	}

	private void addRequestSenderRoute() throws Exception {
		// Send to Spine
    	from(requestSenderUri).routeId("pds-request-sender")
    		.onException(Exception.class)
				.bean(new PDSExceptionPropagator())
			.end()
    	
    		// First, log the outbound message
    		.wireTap(requestWiretapUri)
    		// Remove any request headers that came in from the client's request
    		.removeHeaders("*")
    		// Set the SOAPAction header and specify the URL for the Spine request
    		.setHeader("SOAPaction", simple("urn:nhs:names:services:pdsquery/QUPA_IN000005UK01"))
    		// Send the message, using the configured security context (to
    		// handle the TLS MA connection
    		.to(ExchangePattern.InOut, pdsUri + "?throwExceptionOnFailure=false")

    		.convertBodyTo(String.class)
    		// Log the message that comes back from Spine
    		.wireTap(responseWiretapUri)
    		
    		.choice()
    			// A response code that doesn't start with 2 can be treated as an error
    			.when(header(Exchange.HTTP_RESPONSE_CODE).not().startsWith("2"))
    				.bean(new HttpErrorHandler()) // throws as PDSException
    			.endChoice()
    		.end();
	}

	private void addSimpleTraceRoute() throws Exception {
		// Generate Spine Request
    	from(simpleTraceUri).routeId("pds-simple-trace")
    		.onException(Exception.class)
				.bean(new PDSExceptionPropagator())
			.end()
    	
    		// Pass the query parameters into a java class to
    		// create the SOAP request content
			.convertBodyTo(SimpleTrace.class)
    		.beanRef(payloadBuilderRef, "buildSimpleTrace(${body})")
    		.to(ExchangePattern.InOut, requestSenderUri)
    	
    		// Convert the response to a Patient
    		.bean(HL7ResponseParser.class, "parseSpineResponse(${body})");
	}
	
	/**
	 * Throws a PDSException constructed from the  HTTP error response body
	 */
	public static class HttpErrorHandler {
		public void throwAsPDSException(@Body final String body) throws PDSException {
			throw new PDSException("HTTP Error\n\n" + body);
		}
	}
	
	/**
	 * Propagates existing PDSExceptions and wraps other exceptions as PDSException
	 */
	public static class PDSExceptionPropagator {
		public void propagateAsPDSException(@ExchangeException final Exception exception) throws PDSException {
			if (exception != null) {
				Throwables.propagateIfInstanceOf(exception, PDSException.class);
				throw new PDSException(exception);
			}
		}
	}
}
