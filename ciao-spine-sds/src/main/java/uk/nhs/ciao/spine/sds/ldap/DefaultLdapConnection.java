package uk.nhs.ciao.spine.sds.ldap;

import java.io.IOException;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.SizeLimitExceededException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class DefaultLdapConnection implements LdapConnection {
	private final LdapContext context; // TODO: This is NOT thread safe
	private Integer pageSize;
	
	public DefaultLdapConnection(final LdapContext context) {
		this.context = Preconditions.checkNotNull(context);
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
		context.setRequestControls(null);
		
		final NamingEnumeration<SearchResult> results = executeSearch(query);
		try {
			return results.hasMore() ? mapper.mapSearchResult(results.next()) : null;
		} finally {
			closeQuietly(results);
		}
	}

	@Override
	public <T> List<T> list(final LdapQuery query, SearchResultMapper<T> mapper) throws NamingException, IOException {
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
	}
	
	private NamingEnumeration<SearchResult> executeSearch(final LdapQuery query) throws NamingException {
		final SearchControls searchControls = new SearchControls();
		searchControls.setCountLimit(0);
		searchControls.setReturningAttributes(query.getAttributeNames());
		return context.search(query.getName(), query.getFilter().toString(), searchControls);
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