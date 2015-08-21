package uk.nhs.ciao.spine.sds;

import uk.nhs.ciao.spine.sds.ldap.LdapConnection;
import uk.nhs.ciao.spine.sds.query.AccreditedSystemQuery;
import uk.nhs.ciao.spine.sds.query.MessageHandlingServiceQuery;

import com.google.common.base.Preconditions;

public class SpineDirectoryService {
	private final LdapConnection connection;
	
	public SpineDirectoryService(final LdapConnection connection) {
		this.connection = Preconditions.checkNotNull(connection);
	}
	
	public AccreditedSystemQuery findAccreditedSystems() {
		return new AccreditedSystemQuery(connection);
	}
	
	public MessageHandlingServiceQuery findMessageHandlingServices() {
		return new MessageHandlingServiceQuery(connection);
	}
}
