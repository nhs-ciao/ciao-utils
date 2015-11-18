package uk.nhs.ciao.spine.pds.query;

import org.apache.camel.ProducerTemplate;

import uk.nhs.ciao.model.Patient;
import uk.nhs.ciao.spine.pds.model.SimpleTrace;
import uk.nhs.interoperability.payloads.DateValue;
import uk.nhs.interoperability.payloads.vocabularies.generated.Sex;

import com.google.common.base.Preconditions;

public class SimpleTraceQuery {
	private final ProducerTemplate producerTemplate;
	private final SimpleTrace simpleTrace;
	
	public SimpleTraceQuery(final ProducerTemplate producerTemplate) {
		this.producerTemplate = Preconditions.checkNotNull(producerTemplate);
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
	
	public Patient getPatient() {
		// TODO: Hook into configured routes
		throw new UnsupportedOperationException();
	}
}
