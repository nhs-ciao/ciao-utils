package uk.nhs.ciao.spine.pds;

import org.apache.camel.ProducerTemplate;

import com.google.common.base.Preconditions;

import uk.nhs.ciao.spine.pds.query.SimpleTraceQuery;

/**
 * Facade to perform queries on the Spine Personal Demographics Service (PDS)
 * <p>
 * This service should be backed by a set of configured Camel {@link uk.nhs.ciao.spine.pds.route.PDSRoutes}
 */
public class PersonalDemographicsService {
	private final ProducerTemplate producerTemplate;
	private final String serviceUri;
	
	/**
	 * Creates a new service backed by the specified Camel template and URI
	 * <p>
	 * The service URI should match {@link uk.nhs.ciao.spine.pds.route.PDSRoutes#setServiceFacadeUri(String)}
	 *
	 * @param producerTemplate The camel producer template for sending requests
	 * @param serviceUri The target URI of the Camel PDS service facade
	 */
	public PersonalDemographicsService(final ProducerTemplate producerTemplate, final String serviceUri) {
		this.producerTemplate = Preconditions.checkNotNull(producerTemplate);
		this.serviceUri = Preconditions.checkNotNull(serviceUri);
	}
	
	/**
	 * Starts a new simple trace query
	 */
	public SimpleTraceQuery startSimpleTrace() {
		return new SimpleTraceQuery(producerTemplate, serviceUri);
	}
	
	// Builders from additional query types can be added here
}
