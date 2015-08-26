package uk.nhs.ciao.spine.sds;

import javax.naming.NamingException;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultProducerTemplate;
import org.apache.camel.impl.SimpleRegistry;
import org.junit.After;

import com.google.common.base.Throwables;

import uk.nhs.ciao.camel.CamelUtils;
import uk.nhs.ciao.spine.sds.ldap.CamelLdapConnection;
import uk.nhs.ciao.spine.sds.ldap.LdapConnection;

/**
 * {@link SpineDirectoryService} tests backed by a {@link CamelLdapConnection}.
 */
public class CamelSpineDirectoryServiceTest extends BaseSpineDirectoryServiceTest {
	private CamelContext camelContext;
	private ProducerTemplate producerTemplate;
	
	@Override
	protected LdapConnection setupConnection(final EmbeddedLDAPServer server) throws Exception {
		final String beanRef = "sdsInitialContext";
		final SimpleRegistry registry = new SimpleRegistry() {
			@Override
			public Object lookupByName(final String name) {
				// Simulating 'prototype' option in spring config
				// Each lookup from the LDAP component requires a new instance!
				if (beanRef.equals(name)) {
					try {
						return server.createInitialLdapContext();
					} catch (NamingException e) {
						Throwables.propagate(e);
					}
				}
				
				return super.lookupByName(name);
			}
		};
		
		camelContext = new DefaultCamelContext(registry);
		producerTemplate = new DefaultProducerTemplate(camelContext);
		
		camelContext.start();
		producerTemplate.start();
		
		return new CamelLdapConnection(producerTemplate, beanRef);
	}
	
	@After
	public void tearDown() {
		CamelUtils.stopQuietly(producerTemplate, camelContext);
	}
}
