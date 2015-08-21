package uk.nhs.ciao.spine.sds;

import java.io.IOException;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.SizeLimitExceededException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class SpineDirectoryService {
	private final LdapContext context; // TODO: This is NOT thread safe
	private Integer pageSize;
	
	private SDSQueryRunner queryRunner = new SDSQueryRunner();
	
	public SpineDirectoryService(final LdapContext context) {
		this.context = Preconditions.checkNotNull(context);
	}
	
	public void enableRequestPaging(final int pageSize) {
		this.pageSize = pageSize;
	}
	
	public void disableRequestPaging() {
		this.pageSize = null;
	}
	
	public AccreditedSystemQuery findAccreditedSystems() {
		return new AccreditedSystemQuery(queryRunner);
	}
	
	public MessageHandlingServiceQuery findMessageHandlingServices() {
		return new MessageHandlingServiceQuery(queryRunner);
	}
	
	public class SDSQueryRunner {
		public <T> T get(final SDSQuery<T> query) throws NamingException {
			context.setRequestControls(null);
			
			final NamingEnumeration<SearchResult> results = executeSearch(query);
			try {
				return results.hasMore() ? query.mapResult(results.next()) : null;
			} finally {
				closeQuietly(results);
			}
		}
		
		public <T> List<T> list(final SDSQuery<T> query) throws NamingException, IOException {
			final List<T> list = Lists.newArrayList();
			
			if (pageSize == null) {
				context.setRequestControls(null);
			} else {
				context.setRequestControls(new Control[] {
						new PagedResultsControl(pageSize, Control.NONCRITICAL)
				});
			}
			
			byte[] cookie;
			do {
				cookie = null;
				
				final NamingEnumeration<SearchResult> results = executeSearch(query);
				try {
					while (results.hasMore()) {
						final T item = query.mapResult(results.next());
						if (item != null) {
							list.add(item);
						}
					}
				} catch (final SizeLimitExceededException e) {
					e.printStackTrace(); // TODO: LOGGER
				} finally {
					closeQuietly(results);
				}
				
				if (pageSize != null) {
					for (Control control: context.getResponseControls()) {
						if (control instanceof PagedResultsResponseControl) {
							final PagedResultsResponseControl pagedResultsResponseControl = (PagedResultsResponseControl) control;
							cookie = pagedResultsResponseControl.getCookie();
							
							context.setRequestControls(new Control[] {
									new PagedResultsControl(pageSize, cookie, Control.NONCRITICAL)
							});
						}
					}
				}
			} while (cookie != null);
			
			return list;
		}
		
		private NamingEnumeration<SearchResult> executeSearch(final SDSQuery<?> query) throws NamingException {
			final SearchControls searchControls = new SearchControls();
			searchControls.setCountLimit(0);
			searchControls.setReturningAttributes(query.getAttributeNames());
			return context.search(query.getName(), query.getFilter(), searchControls);
		}
		
		private void closeQuietly(final NamingEnumeration<?> enumeration) {
			if (enumeration == null) {
				return;
			}
			
			try {
				enumeration.close();
			} catch (NamingException e) {
				e.printStackTrace(); // TODO: LOGGER
			}
		}
	}
	
	public static class AccreditedSystemQuery extends SDSQuery<AccreditedSystem> {
		private String uniqueIdentifier;
		private String nhsAsSvcIA;
		private String nhsMHSPartyKey;
		private String nhsIDCode;
		private String nhsDateApproved;
		
		public AccreditedSystemQuery(final SDSQueryRunner queryRunner) {
			super(queryRunner);
		}
		
		public void reset() {
			uniqueIdentifier = null;
			nhsAsSvcIA = null;
			nhsMHSPartyKey = null;
			nhsIDCode = null;
			nhsDateApproved = null;
		}
		
		public AccreditedSystemQuery withUniqueIdentifier(final String uniqueIdentifier) {
			this.uniqueIdentifier = uniqueIdentifier;
			return this;
		}
		
		public AccreditedSystemQuery withNhsAsSvcIA(final String nhsAsSvcIA) {
			this.nhsAsSvcIA = nhsAsSvcIA;
			return this;
		}
		
		public AccreditedSystemQuery withNhsMHSPartyKey(final String nhsMHSPartyKey) {
			this.nhsMHSPartyKey = nhsMHSPartyKey;
			return this;
		}
		
		public AccreditedSystemQuery withNhsIDCode(final String nhsIDCode) {
			this.nhsIDCode = nhsIDCode;
			return this;
		}
		
		public AccreditedSystemQuery withNhsDateApproved(final String nhsDateApproved) {
			this.nhsDateApproved = nhsDateApproved;
			return this;
		}
		
		@Override
		protected String getFilter() {
			return new FilterBuilder()
				.add("objectclass", "nhsAs")
				.add("uniqueIdentifier", uniqueIdentifier)
				.add("nhsAsSvcIA", nhsAsSvcIA)
				.add("nhsMHSPartyKey", nhsMHSPartyKey)
				.add("nhsIDCode", nhsIDCode)
				.add("nhsDateApproved", nhsDateApproved)
				.build();
		}
		
		@Override
		protected String[] getAttributeNames() {
			return new String[]{"uniqueIdentifier", "nhsAsSvcIA", "nhsMHSPartyKey", "nhsIDCode", "nhsDateApproved"};
		}
		
		@Override
		protected AccreditedSystem mapResult(final SearchResult result) throws NamingException {
			final AccreditedSystem accreditedSystem = new AccreditedSystem();
			
			final Attributes attributes = result.getAttributes();
			accreditedSystem.setUniqueIdentifier(value(attributes, "uniqueIdentifier"));
			accreditedSystem.setNhsAsSvcIAs(values(attributes, "nhsAsSvcIA"));
			accreditedSystem.setNhsMHSPartyKey(value(attributes, "nhsMHSPartyKey"));
			accreditedSystem.setNhsIDCode(value(attributes, "nhsIDCode"));
			accreditedSystem.setNhsDateApproved(value(attributes, "nhsDateApproved"));
			
			return accreditedSystem;
		}
	}
	
	public static class MessageHandlingServiceQuery extends SDSQuery<MessageHandlingService> {
		private String uniqueIdentifier;
		private String nhsMhsSvcIA;
		private String nhsMhsCPAId;
		private String nhsIDCode;
		private String nhsMHSPartyKey;
		private String nhsDateApproved;
		
		public MessageHandlingServiceQuery(final SDSQueryRunner queryRunner) {
			super(queryRunner);
		}
		
		public void reset() {
			this.uniqueIdentifier = null;
			this.nhsMhsSvcIA = null;
			this.nhsMhsCPAId = null;
			this.nhsIDCode = null;
			this.nhsMHSPartyKey = null;
			this.nhsDateApproved = null;
		}
		
		public MessageHandlingServiceQuery withUniqueIdentifier(final String uniqueIdentifier) {
			this.uniqueIdentifier = uniqueIdentifier;
			return this;
		}
		
		public MessageHandlingServiceQuery withNhsMhsSvcIA(final String nhsMhsSvcIA) {
			this.nhsMhsSvcIA = nhsMhsSvcIA;
			return this;
		}

		public MessageHandlingServiceQuery withNhsMhsCPAId(final String nhsMhsCPAId) {
			this.nhsMhsCPAId = nhsMhsCPAId;
			return this;
		}

		public MessageHandlingServiceQuery withNhsIDCode(final String nhsIDCode) {
			this.nhsIDCode = nhsIDCode;
			return this;
		}

		public MessageHandlingServiceQuery withNhsMHSPartyKey(final String nhsMHSPartyKey) {
			this.nhsMHSPartyKey = nhsMHSPartyKey;
			return this;
		}
		
		public MessageHandlingServiceQuery withNhsDateApproved(final String nhsDateApproved) {
			this.nhsDateApproved = nhsDateApproved;
			return this;
		}

		@Override
		protected String getFilter() {
			return new FilterBuilder()
				.add("objectclass", "nhsMhs")
				.add("uniqueIdentifier", uniqueIdentifier)
				.add("nhsMhsSvcIA", nhsMhsSvcIA)
				.add("nhsMhsCPAId", nhsMhsCPAId)
				.add("nhsIDCode", nhsIDCode)
				.add("nhsMHSPartyKey", nhsMHSPartyKey)
				.add("nhsDateApproved", nhsDateApproved)
				.build();
		}
		
		@Override
		protected String[] getAttributeNames() {
			return new String[]{"uniqueIdentifier", "nhsMhsSvcIA", "nhsMhsCPAId", "nhsIDCode", "nhsMHSPartyKey", "nhsDateApproved"};
		}
		
		@Override
		protected MessageHandlingService mapResult(final SearchResult result) throws NamingException {
			final MessageHandlingService messageHandlingService = new MessageHandlingService();
			
			final Attributes attributes = result.getAttributes();
			messageHandlingService.setUniqueIdentifier(value(attributes, "uniqueIdentifier"));
			messageHandlingService.setNhsMhsSvcIAs(values(attributes, "nhsMhsSvcIA"));
			messageHandlingService.setNhsMHSPartyKey(value(attributes, "nhsMHSPartyKey"));
			messageHandlingService.setNhsIDCode(value(attributes, "nhsIDCode"));
			messageHandlingService.setNhsMhsCPAId(value(attributes, "nhsMhsCPAId"));
			messageHandlingService.setNhsDateApproved(value(attributes, "nhsDateApproved"));
			
			return messageHandlingService;
		}
	}
	
	public static abstract class SDSQuery<T> {
		private final SDSQueryRunner queryRunner;
		
		public SDSQuery(final SDSQueryRunner queryRunner) {
			this.queryRunner = Preconditions.checkNotNull(queryRunner);
		}
		
		public T get() throws NamingException {
			return queryRunner.get(this);
		}
		
		public List<T> list() throws IOException, NamingException {
			return queryRunner.list(this);
		}
		
		protected String getName() {
			return "ou=Services, o=nhs";
		}
		
		protected abstract String getFilter();
		protected abstract String[] getAttributeNames();
		protected abstract T mapResult(SearchResult result) throws NamingException;
		
		protected String value(final Attributes attributes, final String attrID) throws NamingException {
			String value = null;
			
			final Attribute attribute = attributes.get(attrID);
			if (attribute != null) {
				final Object object = attribute.get();
				value = object == null ? null : object.toString();
			}
			
			return value;
		}
		
		protected List<String> values(final Attributes attributes, final String attrID) throws NamingException {
			final List<String> values = Lists.newArrayList();
			
			final Attribute attribute = attributes.get(attrID);
			if (attribute != null) {
				for (int index = 0; index < attribute.size(); index++) {
					final Object object = attribute.get(index);
					final String value = object == null ? null : object.toString();
					values.add(value);
				}
			}
			
			return values;
		}
		
		protected class FilterBuilder {
			private final StringBuilder builder = new StringBuilder();
			private int clauses;
			
			public FilterBuilder add(final String name, final String value) {
				if (value != null) {
					clauses++;
					builder.append("(").append(name)
						.append("=").append(value).append(")");
				}
				
				return this;
			}
			
			public String build() {
				if (clauses > 1) {
					builder.insert(0, "(&");
					builder.append(")");
				}
				
				return builder.toString();
			}
		}
	}
}
