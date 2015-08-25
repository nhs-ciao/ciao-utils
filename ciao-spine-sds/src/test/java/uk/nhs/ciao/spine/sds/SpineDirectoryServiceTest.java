package uk.nhs.ciao.spine.sds;

import java.io.IOException;

import javax.naming.NamingException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.nhs.ciao.spine.sds.ldap.DefaultLdapConnection;
import uk.nhs.ciao.spine.sds.ldap.LdapConnection;

public class SpineDirectoryServiceTest {
	private static EmbeddedLDAPServer server;
	
	@BeforeClass
	public static void setupLdapServer() throws Exception {
		server = new EmbeddedLDAPServer();
		server.start();
	}
	
	@AfterClass
	public static void tearDownLdapServer() {
		server.stop();
	}
	
	private SpineDirectoryService sds;
	
	@Before
	public void setup() {
		final LdapConnection connection = new DefaultLdapConnection(server.getLdapEnvironment());
		sds = new SpineDirectoryService(connection);
	}
	
	@Test
	public void testFindAccreditedSystems() throws NamingException, IOException {
		Assert.assertEquals(2, sds.findAccreditedSystems().list().size());
	}
}
