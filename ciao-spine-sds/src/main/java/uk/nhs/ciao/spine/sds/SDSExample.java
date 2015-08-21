package uk.nhs.ciao.spine.sds;

import java.io.IOException;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;

import uk.nhs.ciao.spine.sds.ldap.DefaultLdapConnection;
import uk.nhs.ciao.spine.sds.model.AccreditedSystem;

import com.google.common.base.Preconditions;

public class SDSExample {
	public static void main(final String[] args) throws Exception {
		final Hashtable<Object, Object> env = new Hashtable<Object, Object>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, "ldap://localhost:389/");
		//env.put(Context.SECURITY_PROTOCOL, "ssl");
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, "cn=Directory Manager");
		env.put(Context.SECURITY_CREDENTIALS, "admin");
		
		final InitialLdapContext context = new InitialLdapContext(env, null);
		final DefaultLdapConnection connection = new DefaultLdapConnection(context);
		connection.enableRequestPaging(500);
		
		final SpineDirectoryService sds = new SpineDirectoryService(connection);
		new SDSExample(sds).run();
	}
	
	private final SpineDirectoryService sds;
	private final SDSSpineEndpointAddressRepository repository;
	
	public SDSExample(final SpineDirectoryService sds) throws NamingException {
		this.sds = Preconditions.checkNotNull(sds);
		this.repository = new SDSSpineEndpointAddressRepository(sds);
	}
	
	private void run() throws Exception {
		System.out.println(sds.findAccreditedSystems().withNhsIDCode("YEA").list());
		System.out.println(sds.findMessageHandlingServices().withNhsIDCode("YEA").get());
		
		findMatchesInDirectory();
		
		// by ASID
		System.out.println(repository.findUsingAsid("urn:nhs:names:services:ebsepr", "PRPA_IN030000UK11", "951718497518"));
		
		// by ODSCode
		System.out.println(repository.findUsingODSCode("urn:nhs:names:services:ebs", "PRSC_IN150001UK02", "YEA"));
	}
	
	public void findMatchesInDirectory() throws IOException, NamingException {
		for (final AccreditedSystem as: sds.findAccreditedSystems().list()) {
			for (final String svcIA: as.getNhsAsSvcIAs()) {
				final int index = svcIA.lastIndexOf(':');
				final String service = svcIA.substring(0, index);
				final String action = svcIA.substring(index + 1);
				final SpineEndpointAddress address = repository.findUsingODSCode(service, action, as.getNhsIDCode());
				if (address != null) {
					System.out.println(address);
				}
			}
		}
	}
}
