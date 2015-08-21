package uk.nhs.ciao.spine.sds;

import java.io.IOException;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultProducerTemplate;
import org.apache.camel.impl.SimpleRegistry;

import uk.nhs.ciao.spine.sds.SDSSpineEndpointAddressRepository.AccreditedSystemSelectionStrategy;
import uk.nhs.ciao.spine.sds.SDSSpineEndpointAddressRepository.MessageHandlingServiceSelectionStrategy;
import uk.nhs.ciao.spine.sds.ldap.CamelLdapConnection;
import uk.nhs.ciao.spine.sds.ldap.DefaultLdapConnection;
import uk.nhs.ciao.spine.sds.ldap.LdapConnection;
import uk.nhs.ciao.spine.sds.model.AccreditedSystem;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

public class SDSExample {
	public static void main(final String[] args) throws Exception {
		long startTime = System.currentTimeMillis();
		final boolean useCamel = false;
		
		CamelContext camelContext = null;
		ProducerTemplate producerTemplate = null;
		
		try {
			final Hashtable<Object, Object> env = new Hashtable<Object, Object>();
			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			env.put(Context.PROVIDER_URL, "ldap://localhost:389/");
			//env.put(Context.SECURITY_PROTOCOL, "ssl");
			env.put(Context.SECURITY_AUTHENTICATION, "simple");
			env.put(Context.SECURITY_PRINCIPAL, "cn=Directory Manager");
			env.put(Context.SECURITY_CREDENTIALS, "admin");
			
			// Enable connection pooling - see http://docs.oracle.com/javase/jndi/tutorial/ldap/connect/pool.html
			env.put("com.sun.jndi.ldap.connect.pool", "true");
			
			final LdapConnection connection;
			if (useCamel) {
				final String beanRef = "sdsInitialContext";
				final SimpleRegistry registry = new SimpleRegistry() {
					@Override
					public Object lookupByName(final String name) {
						// Simulating 'prototype' option in spring config
						// Each lookup from the LDAP component requires a new instance!
						if (beanRef.equals(name)) {
							try {
								return new InitialLdapContext(env, null);
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
				
				connection = new CamelLdapConnection(producerTemplate, beanRef);
			} else {
				connection = new DefaultLdapConnection(env);
			}
			
			connection.enableRequestPaging(500);
			
			final SpineDirectoryService sds = new SpineDirectoryService(connection);
			new SDSExample(sds).run();
		} finally {
			if (camelContext != null) {
				camelContext.stop();
			}
			
			if (producerTemplate != null) {
				producerTemplate.stop();
			}
		}
		
		System.out.println("Time: " + (System.currentTimeMillis() - startTime));
	}
	
	private final SpineDirectoryService sds;
	private final SDSSpineEndpointAddressRepository repository;
	
	public SDSExample(final SpineDirectoryService sds) throws NamingException {
		this.sds = Preconditions.checkNotNull(sds);
		
		final AccreditedSystemSelectionStrategy accreditedSystemSelectionStrategy = new AccreditedSystemSelectionStrategy();
		accreditedSystemSelectionStrategy.setSortByDateApproved();
		
		final MessageHandlingServiceSelectionStrategy messageHandlingServiceSelectionStrategy = new MessageHandlingServiceSelectionStrategy();
		messageHandlingServiceSelectionStrategy.setSortByDateApproved();
		
		this.repository = new SDSSpineEndpointAddressRepository(sds, accreditedSystemSelectionStrategy, messageHandlingServiceSelectionStrategy);
	}
	
	private void run() throws Exception {
		System.out.println(sds.findAccreditedSystems().withNhsIDCode("YEA").list());
		System.out.println(sds.findMessageHandlingServices().withNhsIDCode("YEA").get());
		
		findMatchesInDirectory();
		
		// by ASID
		System.out.println(repository.findByAsid("urn:nhs:names:services:ebsepr", "PRPA_IN030000UK11", "951718497518"));
		
		// by ODSCode
		System.out.println(repository.findByODSCode("urn:nhs:names:services:ebs", "PRSC_IN150001UK02", "YEA"));
	}
	
	public void findMatchesInDirectory() throws IOException, NamingException {
		for (final AccreditedSystem as: sds.findAccreditedSystems().list()) {
			for (final String svcIA: as.getNhsAsSvcIAs()) {
				final int index = svcIA.lastIndexOf(':');
				final String service = svcIA.substring(0, index);
				final String action = svcIA.substring(index + 1);
				final SpineEndpointAddress address = repository.findByODSCode(service, action, as.getNhsIDCode());
				if (address != null) {
					System.out.println(address);
				}
			}
		}
	}
}
