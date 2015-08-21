package uk.nhs.ciao.spine.sds;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;

import org.apache.commons.collections.comparators.ComparatorChain;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

public class LDAPExample {
	public static void main(final String[] args) throws Exception {
		new LDAPExample().run();		
	}
	
	private final SpineDirectoryService sds;
	
	public LDAPExample() throws NamingException {
		final Hashtable<Object, Object> env = new Hashtable<Object, Object>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, "ldap://localhost:389/");
		//env.put(Context.SECURITY_PROTOCOL, "ssl");
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, "cn=Directory Manager");
		env.put(Context.SECURITY_CREDENTIALS, "admin");
		
		final InitialLdapContext context = new InitialLdapContext(env, null);
		sds = new SpineDirectoryService(context);
		sds.enableRequestPaging(500);
	}
	
	private void run() throws Exception {
		System.out.println(sds.findAccreditedSystems().withNhsIDCode("YEA").list());
		System.out.println(sds.findMessageHandlingServices().withNhsIDCode("YEA").get());
		
		findMatchesInDirectory();
		
		// by ASID
		System.out.println(findUsingAsid("urn:nhs:names:services:ebsepr", "PRPA_IN030000UK11", "951718497518"));
		
		// by ODSCode
		System.out.println(findUsingODSCode("urn:nhs:names:services:ebs", "PRSC_IN150001UK02", "YEA"));
	}
	
	public void findMatchesInDirectory() throws IOException, NamingException {
		for (final AccreditedSystem as: sds.findAccreditedSystems().list()) {
			for (final String svcIA: as.getNhsAsSvcIAs()) {
				final int index = svcIA.lastIndexOf(':');
				final String service = svcIA.substring(0, index);
				final String action = svcIA.substring(index + 1);
				final SpineEndpointAddress address = findUsingODSCode(service, action, as.getNhsIDCode());
				if (address != null) {
					System.out.println(address);
				}
			}
		}
	}
	
	private SpineEndpointAddress findUsingODSCode(final String service, final String action, final String odsCode) throws NamingException, IOException {
		final String svcIA = service + ":" + action;
		
		final List<AccreditedSystem> accreditedSystems = sds.findAccreditedSystems()
			.withNhsAsSvcIA(svcIA)
			.withNhsIDCode(odsCode)
			.list();
		
		if (accreditedSystems.isEmpty()) {
			return null;
		} else if (accreditedSystems.size() > 1) {
			Collections.sort(accreditedSystems, SORT_AS_BY_DATE);
			
			final List<String> ids = Lists.newArrayList();
			for (final AccreditedSystem accreditedSystem: accreditedSystems) {
				ids.add(accreditedSystem.getUniqueIdentifier());
			}
			
			System.err.println("Found many matching AccreditedSystems: " + ids.size() + " - " + ids);
		}
		
		for (final AccreditedSystem accreditedSystem: accreditedSystems) {
			final MessageHandlingService messageHandlingService = findMessageHandlingService(svcIA, accreditedSystem);
			if (messageHandlingService != null) {
				final SpineEndpointAddress address = new SpineEndpointAddress();
				address.setService(service);
				address.setAction(action);
				address.setAsid(accreditedSystem.getUniqueIdentifier());
				address.setCpaId(messageHandlingService.getNhsMhsCPAId());
				address.setMhsPartyKey(messageHandlingService.getNhsMHSPartyKey());
				address.setOdsCode(odsCode);
				return address;
			}
		}
		
		return null;
	}
	
	private SpineEndpointAddress findUsingAsid(final String service, final String action, final String asid) throws NamingException, IOException {
		final String svcIA = service + ":" + action;
	
		final AccreditedSystem accreditedSystem = sds.findAccreditedSystems()
			.withNhsAsSvcIA(svcIA)
			.withUniqueIdentifier(asid)
			.get();
		
		final MessageHandlingService messageHandlingService = findMessageHandlingService(svcIA, accreditedSystem);
		if (messageHandlingService == null) {
			return null;
		}
		
		final SpineEndpointAddress address = new SpineEndpointAddress();
		address.setService(service);
		address.setAction(action);
		address.setAsid(asid);
		address.setCpaId(messageHandlingService.getNhsMhsCPAId());
		address.setMhsPartyKey(messageHandlingService.getNhsMHSPartyKey());
		address.setOdsCode(accreditedSystem.getNhsIDCode());
		return address;
	}
	
	private MessageHandlingService findMessageHandlingService(final String svcIA, final AccreditedSystem accreditedSystem) throws IOException, NamingException {
		if (accreditedSystem == null) {
			return null;
		}
		
		final List<MessageHandlingService> messageHandlingServices = sds.findMessageHandlingServices()
				.withNhsMhsSvcIA(svcIA)
				.withNhsMHSPartyKey(accreditedSystem.getNhsMHSPartyKey())
				.list();
		
		if (messageHandlingServices.isEmpty()) {
			return null;
		} else if (messageHandlingServices.size() > 1) {
			Collections.sort(messageHandlingServices, SORT_MHS_BY_DATE);
			
			final List<String> ids = Lists.newArrayList();
			for (final MessageHandlingService messageHandlingService: messageHandlingServices) {
				ids.add(messageHandlingService.getUniqueIdentifier());
			}
			
			System.err.println("Found many matching MessageHandlingServices: " + ids.size() + " - " + ids);
		}
	
		return messageHandlingServices.get(0);
	}
	
	private static Comparator<String> SORT_DATE_STRINGS = Ordering.natural().nullsLast();
	
	private static Comparator<AccreditedSystem> SORT_AS_BY_DATE = new Comparator<AccreditedSystem>() {
		@Override
		public int compare(final AccreditedSystem left, final AccreditedSystem right) {
			return SORT_DATE_STRINGS.compare(left.getNhsDateApproved(), right.getNhsDateApproved());
		}
	};
	
	private static Comparator<MessageHandlingService> SORT_MHS_BY_DATE = new Comparator<MessageHandlingService>() {
		@Override
		public int compare(final MessageHandlingService left, final MessageHandlingService right) {
			return SORT_DATE_STRINGS.compare(left.getNhsDateApproved(), right.getNhsDateApproved());
		}
	};
}
