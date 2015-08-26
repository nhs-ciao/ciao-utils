package uk.nhs.ciao.spine.sds.ldap;

import static uk.nhs.ciao.spine.sds.ldap.LdapUtils.closeQuietly;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class DefaultLdapConnection implements LdapConnection {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultLdapConnection.class);
	
	private final Hashtable<Object, Object> environment;
	private volatile Integer pageSize;
	
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
	public <T> T get(final LdapQuery query, final SearchResultMapper<T> mapper) throws NamingException {
		final LdapContext context  = createLdapContext();
		
		try {
			return get(context, query, mapper);
		} finally {
			closeQuietly(context);
		}
	}

	@Override
	public <T> List<T> list(final LdapQuery query, SearchResultMapper<T> mapper) throws NamingException, IOException {
		final LdapContext context = createLdapContext();
		
		try {
			return list(context, query, mapper);
		} finally {
			closeQuietly(context);
		}
	}
	
	private <T> T get(final LdapContext context, final LdapQuery query, final SearchResultMapper<T> mapper) throws NamingException {
		final NamingEnumeration<SearchResult> results = executeSearch(context, query);
		try {
			return results.hasMore() ? mapper.mapSearchResult(results.next()) : null;
		} finally {
			closeQuietly(results);
		}
	}
	
	private <T> List<T> list(final LdapContext context, final LdapQuery query,
			final SearchResultMapper<T> mapper) throws NamingException, IOException {
		// store the original pageSize for the duration of the processing
		final Integer pageSize = this.pageSize;
		if (pageSize != null) {
			context.setRequestControls(new Control[] {
				new PagedResultsControl(pageSize, Control.NONCRITICAL)
			});
		}
		
		final List<T> list = Lists.newArrayList();
		
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
				LOGGER.warn("Exceeded LDAP search result size limit - will continue with available results", e);
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
	
	private NamingEnumeration<SearchResult> executeSearch(final LdapContext context, final LdapQuery query) throws NamingException {
		final SearchControls searchControls = new SearchControls();
		searchControls.setCountLimit(0);
		searchControls.setReturningAttributes(query.getAttributeNames());
		return context.search(query.getName(), query.getFilter().toString(), searchControls);
	}
	
	private LdapContext createLdapContext() throws NamingException {
		return new InitialLdapContext(environment, null);
	}
}