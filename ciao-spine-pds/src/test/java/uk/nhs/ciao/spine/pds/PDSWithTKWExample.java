package uk.nhs.ciao.spine.pds;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.http4.HttpComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultProducerTemplate;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.util.jsse.SSLContextParameters;

import uk.nhs.ciao.model.Patient;
import uk.nhs.ciao.spine.pds.hl7.HL7PayloadBuilder;
import uk.nhs.ciao.spine.pds.route.PDSRoutes;
import uk.nhs.interoperability.payloads.util.Emptiables;

/**
 * Example class running against the TKW
 */
public class PDSWithTKWExample {
	public static void main(final String[] args) throws Exception {
		final SimpleRegistry registry = new SimpleRegistry();
		registry.put("spineSSLContextParameters", new SSLContextParameters());
		
		registry.put("http", new HttpComponent());
		
		final HttpComponent httpsComponent = new HttpComponent();
		httpsComponent.setSslContextParameters((SSLContextParameters)registry.get("spineSSLContextParameters"));
		registry.put("https", httpsComponent);
		
		final HL7PayloadBuilder payloadBuilder = new HL7PayloadBuilder();
		payloadBuilder.setPdsURL("http://127.0.0.1:4001/syncservice-pds/pds");
		payloadBuilder.setFromAddress("from-address");
		payloadBuilder.setReceiverASID("SIAB-001");
		payloadBuilder.setSenderASID("sender-001");
		registry.put("payloadBuilder", payloadBuilder);
		
		final CamelContext context = new DefaultCamelContext(registry);
		final ProducerTemplate producerTemplate = new DefaultProducerTemplate(context);

		final PDSRoutes routes = new PDSRoutes();
		routes.setRequestWiretapUri("log:request-wiretap");
		routes.setResponseWiretapUri("log:response-wiretap");
		routes.setPdsUri("http://127.0.0.1:4001/syncservice-pds/pds");
		
		context.addRoutes(routes);
		
		context.start();
		producerTemplate.start();
		
		final PersonalDemographicsService pds = new PersonalDemographicsService(producerTemplate, "direct:pds");
		final Patient patient = pds.startSimpleTrace().forSurname("PURVES").getPatient();
		printPatient(patient);
	}
	
	private static void printPatient(final Patient patient) {
		if (Emptiables.isNullOrEmpty(patient)) {
			System.out.println("No match found");
		} else {
			System.out.println("Found patient");
			System.out.println("NHS Number: " + patient.getNhsNumber());
			System.out.println("Name: " + patient.getName());
			System.out.println("Address: " + patient.getAddress());
			System.out.println("DateOfBirth: " + patient.getDateOfBirth());
			System.out.println("DateOfDeath: " + patient.getDateOfDeath());
			System.out.println("Gender: " + patient.getGender());
			System.out.println("Telecom: " + patient.getTelecom());
			System.out.println("PracticeCode: " + patient.getPracticeCode());
		}
	}
}
