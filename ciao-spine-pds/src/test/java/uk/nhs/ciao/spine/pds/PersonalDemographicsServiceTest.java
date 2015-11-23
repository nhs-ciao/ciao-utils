package uk.nhs.ciao.spine.pds;

import java.io.IOException;
import java.io.InputStream;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultProducerTemplate;
import org.apache.camel.impl.SimpleRegistry;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;

import uk.nhs.ciao.camel.CamelUtils;
import uk.nhs.ciao.model.Patient;
import uk.nhs.ciao.spine.pds.hl7.HL7PayloadBuilder;
import uk.nhs.ciao.spine.pds.route.PDSRoutes;
import uk.nhs.interoperability.payloads.util.Emptiables;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Closeables;

/**
 * Tests for {@link PersonalDemographicsService} backed by Camel {@link PDSRoutes}
 */
public class PersonalDemographicsServiceTest {
	private CamelContext context;
	private ProducerTemplate producerTemplate;
	private PersonalDemographicsService pds;
	private MockEndpoint pdsServer;
	private ObjectMapper objectMapper;
	
	@Before
	public void setup() throws Exception {
		final SimpleRegistry registry = new SimpleRegistry();
				
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
		routes.setPdsUri("mock:pds-server");
		
		context.addRoutes(routes);
		
		context.start();
		producerTemplate.start();
		
		pds = new PersonalDemographicsService(producerTemplate, "direct:pds");
		pdsServer = MockEndpoint.resolve(context, "mock:pds-server?throwExceptionOnFailure=false");
		objectMapper = JsonMixins.createObjectMapper();
	}
	
	@After
	public void tearDown() {
		try {
			CamelUtils.stopQuietly(context, producerTemplate);
		} finally {
			if (context != null) {
				MockEndpoint.resetMocks(context);
			}
		}
	}
	
	@Test
	public void testSingleResultQuery() throws Exception {
		// Expectations
		final Patient expected = loadPatientJson("PURVES.json");
		
		pdsServer.expectedMessageCount(1);
		pdsServer.whenExchangeReceived(1, new Processor() {
			@Override
			public void process(final Exchange exchange) {
				exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
				exchange.getOut().setBody(getClass().getResourceAsStream("PURVES.txt"));
			}
		});
		
		// Run the query
		final Patient actual = pds.startSimpleTrace().forSurname("PURVES").getPatient();

		// Assertions
		pdsServer.assertIsSatisfied();
		Assert.assertFalse(Emptiables.isNullOrEmpty(actual));		
		assertPatientEquals(expected, actual);
	}
	
	private Patient loadPatientJson(final String name) throws IOException {
		final InputStream in = getClass().getResourceAsStream(name);
		try {
			return objectMapper.readValue(in, Patient.class);
		} finally {
			Closeables.closeQuietly(in);
		}
	}
	
	private void assertPatientEquals(final Patient expected, final Patient actual) {
		// Standard equals checks may not work on gender (or other enums of this kind)
		// They need to be compared using sameAs()
		if (expected != null && expected.getGender() != null) {
			Assert.assertTrue("gender", expected.getGender().sameAs(actual.getGender()));
			
			// Normalize to same instance for the reflection equals
			actual.setGender(expected.getGender());
		}
		
		ReflectionAssert.assertReflectionEquals(expected, actual);
	}
}
