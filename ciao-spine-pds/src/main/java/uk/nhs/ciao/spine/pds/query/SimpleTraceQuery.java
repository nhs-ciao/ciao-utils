package uk.nhs.ciao.spine.pds.query;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultExchange;

import uk.nhs.ciao.model.Patient;
import uk.nhs.ciao.spine.pds.PDSException;
import uk.nhs.ciao.spine.pds.model.SimpleTrace;
import uk.nhs.interoperability.payloads.DateValue;
import uk.nhs.interoperability.payloads.vocabularies.generated.Sex;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

/**
 * Fluent builder for running a PDS {@link SimpleTrace}.
 * 
 * @see #getPatient()
 */
public class SimpleTraceQuery {
	private final ProducerTemplate producerTemplate;
	private final String serviceUri;
	private final SimpleTrace simpleTrace;
	
	public SimpleTraceQuery(final ProducerTemplate producerTemplate, final String serviceUri) {
		this.producerTemplate = Preconditions.checkNotNull(producerTemplate);
		this.serviceUri = Preconditions.checkNotNull(serviceUri);
		this.simpleTrace = new SimpleTrace();
	}
	
	public SimpleTraceQuery forSurname(final String surname) {
		simpleTrace.setSurname(surname);
		return this;
	}
	
	public SimpleTraceQuery forDateOfBirth(final DateValue dateOfBirth) {
		simpleTrace.setDateOfBirth(dateOfBirth);
		return this;
	}
	
	public SimpleTraceQuery forDateOfBirth(final String dateOfBirth) {
		simpleTrace.setDateOfBirth(dateOfBirth);
		return this;
	}
	
	public SimpleTraceQuery forGender(final String gender) {
		simpleTrace.setGender(gender);
		return this;
	}
	
	public SimpleTraceQuery forGender(final Sex gender) {
		simpleTrace.setGender(gender);
		return this;
	}
	
	/**
	 * Runs the configured simple trace and returns the associated patient details
	 * 
	 * @return The associated patient, or null if no such patient was found
	 * @throws PDSException If an error occurred while querying PDS
	 */
	public Patient getPatient() throws PDSException {
		final Exchange exchange = new DefaultExchange(producerTemplate.getCamelContext());
		exchange.setPattern(ExchangePattern.InOut);
		
		final Message request = exchange.getIn();
		request.setBody(simpleTrace);
		
		producerTemplate.send(serviceUri, exchange);

		final Message response = exchange.hasOut() ? exchange.getOut() : exchange.getIn();
		if (exchange.getException() != null) {
			Throwables.propagateIfInstanceOf(exchange.getException(), PDSException.class);
			throw new PDSException(exchange.getException());
		}
		
		return response.getBody(Patient.class);
	}
}
