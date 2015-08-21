package uk.nhs.ciao.spine.sds;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.naming.NamingException;

import uk.nhs.ciao.spine.sds.model.AccreditedSystem;
import uk.nhs.ciao.spine.sds.model.MessageHandlingService;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

public class SDSSpineEndpointAddressRepository {
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
	
	private final SpineDirectoryService sds;
	
	public SDSSpineEndpointAddressRepository(final SpineDirectoryService sds) {
		this.sds = Preconditions.checkNotNull(sds);
	}
	
	public SpineEndpointAddress findUsingODSCode(final String service, final String action, final String odsCode) throws NamingException, IOException {
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
	
	public SpineEndpointAddress findUsingAsid(final String service, final String action, final String asid) throws NamingException, IOException {
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
}
