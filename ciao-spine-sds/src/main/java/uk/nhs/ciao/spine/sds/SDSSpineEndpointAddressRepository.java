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
	private final AccreditedSystemSelectionStrategy accreditedSystemSelectionStrategy;
	private final MessageHandlingServiceSelectionStrategy messageHandlingServiceSelectionStrategy;
	
	public SDSSpineEndpointAddressRepository(final SpineDirectoryService sds) {
		this.sds = Preconditions.checkNotNull(sds);
		
		this.accreditedSystemSelectionStrategy = new AccreditedSystemSelectionStrategy();
		this.accreditedSystemSelectionStrategy.setSortByDateApproved();
		
		this.messageHandlingServiceSelectionStrategy = new MessageHandlingServiceSelectionStrategy();
		this.messageHandlingServiceSelectionStrategy.setSortByDateApproved();
	}
	
	public SDSSpineEndpointAddressRepository(final SpineDirectoryService sds, final AccreditedSystemSelectionStrategy accreditedSystemSelectionStrategy,
			final MessageHandlingServiceSelectionStrategy messageHandlingServiceSelectionStrategy) {
		this.sds = Preconditions.checkNotNull(sds);
		this.accreditedSystemSelectionStrategy = Preconditions.checkNotNull(accreditedSystemSelectionStrategy);
		this.messageHandlingServiceSelectionStrategy = Preconditions.checkNotNull(messageHandlingServiceSelectionStrategy);
	}
	
	public SpineEndpointAddress findByODSCode(final String service, final String action, final String odsCode) throws NamingException, IOException {
		final String svcIA = service + ":" + action;
		
		final List<AccreditedSystem> accreditedSystems = sds.findAccreditedSystems()
			.withNhsAsSvcIA(svcIA)
			.withNhsIDCode(odsCode)
			.list();
		
		accreditedSystemSelectionStrategy.select(accreditedSystems);
		
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
	
	public SpineEndpointAddress findByAsid(final String service, final String action, final String asid) throws NamingException, IOException {
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
		return messageHandlingServiceSelectionStrategy.select(messageHandlingServices);
	}
	
	public static class MessageHandlingServiceSelectionStrategy {
		private Comparator<MessageHandlingService> comparator;
		private boolean throwException;
		
		public void setComparator(final Comparator<MessageHandlingService> comparator) {
			this.comparator = comparator;
		}
		
		public void setSortByDateApproved() {
			this.comparator = SORT_MHS_BY_DATE;
		}
		
		public void setThrowException(final boolean throwException) {
			this.throwException = throwException;
		}
		
		public MessageHandlingService select(final List<MessageHandlingService> messageHandlingServices) throws IOException {
			if (messageHandlingServices.isEmpty()) {
				return null;
			} else if (messageHandlingServices.size() == 1) {
				return messageHandlingServices.get(0);
			}
			
			if (throwException) {
				throw new IOException("Found multiple matching MessageHandlingServices: " + getIds(messageHandlingServices));
			}
			
			if (comparator != null) {
				Collections.sort(messageHandlingServices, comparator);
			}
			
			final List<String> ids = getIds(messageHandlingServices);
			System.err.println("Found multiple matching MessageHandlingServices: " + ids.size() + " - " + ids);
			
			return messageHandlingServices.get(0);
		}
		
		private List<String> getIds(final List<MessageHandlingService> messageHandlingServices) {
			final List<String> ids = Lists.newArrayList();
			for (final MessageHandlingService messageHandlingService: messageHandlingServices) {
				ids.add(messageHandlingService.getUniqueIdentifier());
			}
			return ids;
		}
	}
	
	public static class AccreditedSystemSelectionStrategy {
		private Comparator<AccreditedSystem> comparator;
		private boolean throwException;
		private boolean onlyReturnFirst;
		
		public void setComparator(final Comparator<AccreditedSystem> comparator) {
			this.comparator = comparator;
		}
		
		public void setSortByDateApproved() {
			this.comparator = SORT_AS_BY_DATE;
		}
		
		public void setThrowException(final boolean throwException) {
			this.throwException = throwException;
		}
		
		public void setOnlyReturnFirst(final boolean onlyReturnFirst) {
			this.onlyReturnFirst = onlyReturnFirst;
		}
		
		public void select(final List<AccreditedSystem> accreditedSystems) throws IOException {
			if (accreditedSystems.size() <= 1) {
				return;
			}
			
			if (throwException) {
				throw new IOException("Found multiple matching AccreditedSystems: " + getIds(accreditedSystems));
			}
			
			if (comparator != null) {
				Collections.sort(accreditedSystems, comparator);
			}
			
			final List<String> ids = getIds(accreditedSystems);
			System.err.println("Found multiple matching AccreditedSystems: " + ids.size() + " - " + ids);
			
			if (onlyReturnFirst) {
				final AccreditedSystem accreditedSystem = accreditedSystems.get(0);
				accreditedSystems.clear();
				accreditedSystems.add(accreditedSystem);
			}
		}
		
		private List<String> getIds(final List<AccreditedSystem> accreditedSystems) {
			final List<String> ids = Lists.newArrayList();
			for (final AccreditedSystem accreditedSystem: accreditedSystems) {
				ids.add(accreditedSystem.getUniqueIdentifier());
			}
			return ids;
		}
	}
}
