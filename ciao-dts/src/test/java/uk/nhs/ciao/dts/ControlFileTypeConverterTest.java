package uk.nhs.ciao.dts;

import java.io.InputStream;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.LoggingLevel;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.DefaultProducerTemplate;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.ciao.camel.CamelUtils;

/**
 * Unit tests for {@link ControlFileTypeConverter}
 */
public class ControlFileTypeConverterTest {
private static final Logger LOGGER = LoggerFactory.getLogger(ControlFileTypeConverterTest.class);
	
	private CamelContext context;
	private ProducerTemplate producerTemplate;
	
	@Before
	public void setup() throws Exception {
		context = new DefaultCamelContext();
		context.addRoutes(new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				from("direct:deserialize")
					.streamCaching()
					.log(LoggingLevel.INFO, LOGGER, "${body}")
					.convertBodyTo(ControlFile.class);
				
				from("direct:serialize")
					.streamCaching()
					.log(LoggingLevel.INFO, LOGGER, "${body}")
					.convertBodyTo(String.class);
			}
		});
		
		context.start();
		producerTemplate = new DefaultProducerTemplate(context);
		producerTemplate.start();
	}
	
	@After
	public void teardown() {
		CamelUtils.stopQuietly(producerTemplate, context);
	}
	
	@Test
	public void testControlFileRoundtrip() {
		assertRoundtrip(getClass().getResourceAsStream("example-control-file-pre.xml"));
	}
	
	@Test
	public void testControlFileWithStatusRecordRoundtrip() {
		assertRoundtrip(getClass().getResourceAsStream("example-control-file-post.xml"));
	}
	
	private void assertRoundtrip(final InputStream in) {
		final ControlFile expected = context.getTypeConverter().convertTo(ControlFile.class, in);
		assertRoundtrip(expected);
	}
	
	private void assertRoundtrip(final ControlFile expected) {
		final String xml = serialize(expected);
		final ControlFile actual = deserialize(xml);
		Assert.assertEquals(expected, actual);
	}
	
	/**
	 * Converts the control file to a String by sending into through
	 * the configured camel route
	 */
	private String serialize(final ControlFile body) {
		final Exchange exchange = new DefaultExchange(context);
		exchange.setPattern(ExchangePattern.InOut);
		exchange.getIn().setBody(body);
		
		producerTemplate.send("direct:serialize", exchange);
		return exchange.getOut().getBody(String.class);
	}
	
	/**
	 * Converts the body to an {@link ControlFile} by sending into through
	 * the configured camel route
	 */
	private ControlFile deserialize(final Object body) {
		final Exchange exchange = new DefaultExchange(context);
		exchange.setPattern(ExchangePattern.InOut);
		exchange.getIn().setBody(body);
		
		producerTemplate.send("direct:deserialize", exchange);
		return exchange.getOut().getBody(ControlFile.class);
	}
}
