package uk.nhs.ciao.spine.sds;

import uk.nhs.ciao.spine.sds.ldap.DefaultLdapConnection;
import uk.nhs.ciao.spine.sds.ldap.LdapConnection;

/**
 * {@link SpineDirectoryService} tests backed by a {@link DefaultLdapConnection}.
 */
public class DefaultSpineDirectoryServiceTest extends BaseSpineDirectoryServiceTest {
	@Override
	protected LdapConnection setupConnection(final EmbeddedLDAPServer server) throws Exception {
		return new DefaultLdapConnection(server.getLdapEnvironment());
	}
}
