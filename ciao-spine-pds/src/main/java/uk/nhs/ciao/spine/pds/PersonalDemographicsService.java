package uk.nhs.ciao.spine.pds;

import org.apache.camel.ProducerTemplate;

import com.google.common.base.Preconditions;

import uk.nhs.ciao.spine.pds.query.SimpleTraceQuery;

public class PersonalDemographicsService {
	 // TODO: Placeholder for Java style service wrapper for PDS - see the SpineDirectoryService class as a guide
	
	private final ProducerTemplate producerTemplate;
	// TODO: need service URI
	
	public PersonalDemographicsService(final ProducerTemplate producerTemplate) {
		this.producerTemplate = Preconditions.checkNotNull(producerTemplate);
	}
	
	public SimpleTraceQuery startSimpleTrace() {
		return new SimpleTraceQuery(producerTemplate);
	}
}
