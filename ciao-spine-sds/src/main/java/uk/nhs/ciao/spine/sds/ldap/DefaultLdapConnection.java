package uk.nhs.ciao.spine.sds.ldap;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.SizeLimitExceededException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;

import com.google.common.collect.Lists;

public class DefaultLdapConnection implements LdapConnection {
	private final Hashtable<Object, Object> environment;
	private Integer pageSize;
	
	public DefaultLdapConnection(final Map<?, ?> environment) {
		this.environment = new Hashtable<Object, Object>(environment);
	}
	
	@Override
	public void enableRequestPaging(final int pageSize) {
		this.pageSize = pageSize;
	}

	@Override
	public void disableRequestPaging() {
		this.pageSize = null;
	}

	@Override
	public <T> T get(final LdapQuery query, SearchResultMapper<T> mapper) throws NamingException {
		final LdapContext context = createLdapContext();
		try {
			final NamingEnumeration<SearchResult> results = executeSearch(context, query);
			try {
				return results.hasMore() ? mapper.mapSearchResult(results.next()) : null;
			} finally {
				closeQuietly(results);
			}
		} finally {
			closeQuietly(context);
		}
	}

	@Override
	public <T> List<T> list(final LdapQuery query, SearchResultMapper<T> mapper) throws NamingException, IOException {
		final LdapContext context = createLdapContext();
		
		try {
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
				
				final NamingEnumeration<SearchResult> results = executeSearch(context, query);
				try {
					while (results.hasMore()) {
						final T item = mapper.mapSearchResult(results.next());
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
		} finally {
			closeQuietly(context);
		}
	}
	
	private NamingEnumeration<SearchResult> executeSearch(final LdapContext context, final LdapQuery query) throws NamingException {
		final SearchControls searchControls = new SearchControls();
		searchControls.setCountLimit(0);
		searchControls.setReturningAttributes(query.getAttributeNames());
		return context.search(query.getName(), query.getFilter().toString(), searchControls);
	}
	
	private LdapContext createLdapContext(final Control... connCtls) throws NamingException {
		return new InitialLdapContext(environment, connCtls);
	}
	
	private void closeQuietly(final LdapContext context) {
		if (context == null) {
			return;
		}
		
		try {
			context.close();
		} catch (NamingException e) {
			e.printStackTrace(); // TODO: LOGGER
		}
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