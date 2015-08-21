package uk.nhs.ciao.spine.sds.ldap;

import com.google.common.base.Preconditions;

public class LdapQuery {
	private final String name;
	private final LdapFilter filter;
	private final String[] attributeNames;
	
	public LdapQuery(final String name, final LdapFilter filter, final String[] attributeNames) {
		this.name = name;
		this.filter = Preconditions.checkNotNull(filter);
		this.attributeNames = Preconditions.checkNotNull(attributeNames);
	}
	
	public String getName() {
		return name;
	}
	
	public LdapFilter getFilter() {
		return filter;
	}
	
	public String[] getAttributeNames() {
		return attributeNames;
	}
}