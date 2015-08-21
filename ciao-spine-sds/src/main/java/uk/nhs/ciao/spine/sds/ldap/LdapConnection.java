package uk.nhs.ciao.spine.sds.ldap;

import java.io.IOException;
import java.util.List;

import javax.naming.NamingException;

public interface LdapConnection {

	public abstract void enableRequestPaging(int pageSize);

	public abstract void disableRequestPaging();

	public abstract <T> T get(LdapQuery query, SearchResultMapper<T> mapper)
			throws NamingException, IOException;

	public abstract <T> List<T> list(LdapQuery query,
			SearchResultMapper<T> mapper) throws NamingException, IOException;

}