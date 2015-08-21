package uk.nhs.ciao.spine.sds.query;

import java.io.IOException;
import java.util.List;

import javax.naming.NamingException;

import uk.nhs.ciao.spine.sds.ldap.LdapConnection;
import uk.nhs.ciao.spine.sds.ldap.LdapQuery;
import uk.nhs.ciao.spine.sds.ldap.SearchResultMapper;

import com.google.common.base.Preconditions;

public abstract class SDSQuery<T> {
	private final LdapConnection connection;
	private final SearchResultMapper<T> searchResultMapper;
	
	public SDSQuery(final LdapConnection connection, final SearchResultMapper<T> searchResultMapper) {
		this.connection = Preconditions.checkNotNull(connection);
		this.searchResultMapper = Preconditions.checkNotNull(searchResultMapper);
	}
	
	public T get() throws NamingException {
		return connection.get(getLdapQuery(), searchResultMapper);
	}
	
	public List<T> list() throws IOException, NamingException {
		return connection.list(getLdapQuery(), searchResultMapper);
	}
	
	public abstract void reset();
	protected abstract LdapQuery getLdapQuery();
}