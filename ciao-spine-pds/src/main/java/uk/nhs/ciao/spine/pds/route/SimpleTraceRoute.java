package uk.nhs.ciao.spine.pds.route;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;

import uk.nhs.ciao.camel.BaseRouteBuilder;

/**
 * Camel route to execute a Simple Trace query over PDS
 */
public class SimpleTraceRoute extends BaseRouteBuilder {
	// TODO: Extract configurable properties and rewire
	
	@Override
	public void configure() throws Exception {
		// Generate Spine Request
    	from("direct:generateSpineRequest").routeId("pds-simple-trace")
    		// Pass the query parameters into a java class to
    		// create the SOAP request content
    		.beanRef("payloadBuilder",
	    				"buildSimpleTrace(${header.family},"
			    				+ "${header.gender},"
			    				+ "${header.birthdate},"
			    				+ "{{ASID}},"
			    				+ "{{PDSASID}},"
			    				+ "{{PDSURL}},"
			    				+ "{{SOAPFromAddress}})")
    		.to(ExchangePattern.InOut, "direct:spineSender");
    	
    	// TODO: Extract the spine sender to a different route
    	
    	// Send to Spine
    	from("direct:spineSender").routeId("pds-spine-sender")
    		// First, log the outbound message
    		.wireTap("jms:ciao-spineRequestAudit")
    		// Remove any request headers that came in from the client's request
    		.removeHeaders("*")
    		// Set the SOAPAction header and specfy the URL for the Spine request
    		.setHeader("SOAPaction", simple("urn:nhs:names:services:pdsquery/QUPA_IN000005UK01"))
    		.setHeader(Exchange.HTTP_URI, simple("{{PDSURL}}"))
    		// Send the message, using the configured security context (to
    		// handle the TLS MA connection
    		.to("http4://dummyurl"
    						+ "?sslContextParametersRef=spineSSLContextParameters")
    		// Log the message that comes back from Spine
    		.wireTap("jms:ciao-spineResponseAudit")
    		.to("direct:responseProcessor"); // TODO: Run as an IN_OUT style route
	}
}
