package uk.nhs.ciao.spine.sds.ldap;

import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;

import com.google.common.collect.Lists;

public abstract class SearchResultMapper<T> {
	public abstract T mapSearchResult(final SearchResult searchResult) throws NamingException;
	
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
}