package uk.nhs.ciao.spine.sds.ldap;

import java.io.IOException;
import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.SearchResult;

import org.apache.camel.Exchange;
import org.apache.camel.InvalidPayloadException;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultExchange;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

public class CamelLdapConnection implements LdapConnection {
	private ProducerTemplate producerTemplate;
	private final String beanRef;
	private Integer pageSize;
	
	public CamelLdapConnection(final ProducerTemplate producerTemplate, final String beanRef) {
		this.producerTemplate = Preconditions.checkNotNull(producerTemplate);
		this.beanRef = Preconditions.checkNotNull(beanRef);
	}
	
	@Override
	public void setPageSize(final int pageSize) {
		if (pageSize > 0) {
			enableRequestPaging(pageSize);
		} else {
			disableRequestPaging();
		}
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
	public <T> T get(final LdapQuery query, final SearchResultMapper<T> mapper) throws NamingException, IOException {
		final List<SearchResult> searchResults = executeSearch(query);
		return searchResults.isEmpty() ? null : mapper.mapSearchResult((SearchResult)searchResults.get(0));
	}
	
	@Override
	public <T> List<T> list(final LdapQuery query, SearchResultMapper<T> mapper) throws NamingException, IOException {
		final List<SearchResult> searchResults = executeSearch(query);
		
		final List<T> list = Lists.newArrayList();
		
		for (final SearchResult searchResult: searchResults) {
			final T entry = mapper.mapSearchResult(searchResult);
			if (entry != null) {
				list.add(entry);
			}
		}
		
		return list;
	}
	
	private List<SearchResult> executeSearch(final LdapQuery query) throws NamingException, IOException {
		final String uri = getUri(query);
		
		final Exchange exchange = new DefaultExchange(producerTemplate.getCamelContext());
		exchange.getIn().setBody(query.getFilter().toString());
		
		producerTemplate.send(uri, exchange);		
		if (exchange.getException() != null) {
			Throwables.propagateIfInstanceOf(exchange.getException(), NamingException.class);
			Throwables.propagateIfInstanceOf(exchange.getException(), IOException.class);
			throw new IOException(exchange.getException());
		}
		
		try {
			@SuppressWarnings("unchecked")
			final List<SearchResult> searchResults = (List<SearchResult>)exchange.getOut().getMandatoryBody(List.class);
			return searchResults;
		} catch (InvalidPayloadException e) {
			throw new IOException(e);
		}
	}
	
	private String getUri(final LdapQuery query) {
		final StringBuilder url = new StringBuilder()
			.append("ldap:")
			.append(beanRef)
			.append("?");
		
		url.append("base=").append(query.getName());
		
		if (query.getAttributeNames() != null && query.getAttributeNames().length > 0) {
			url.append("&returnedAttributes=");
			Joiner.on(',').appendTo(url, query.getAttributeNames());
		}
		
		if (pageSize != null) {
			url.append("&pageSize=").append(pageSize);
		}
		
		return url.toString();
	}
}
