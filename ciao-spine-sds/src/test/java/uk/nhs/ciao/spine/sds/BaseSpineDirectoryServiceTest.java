package uk.nhs.ciao.spine.sds;

import java.io.IOException;
import java.util.Arrays;

import javax.naming.NamingException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;

import uk.nhs.ciao.spine.sds.ldap.LdapConnection;
import uk.nhs.ciao.spine.sds.model.AccreditedSystem;
import uk.nhs.ciao.spine.sds.model.MessageHandlingService;

/**
 * Tests to run against a {@link SpineDirectoryService} backed by an {@link LdapConnection} (provided
 * by a concrete subclass)
 * 
 * @see #setupConnection(EmbeddedLDAPServer)
 */
public abstract class BaseSpineDirectoryServiceTest {
	private static EmbeddedLDAPServer server;
	
	@BeforeClass
	public static void setupLdapServer() throws Exception {
		server = new EmbeddedLDAPServer();
		server.start();
	}
	
	@AfterClass
	public static void tearDownLdapServer() {
		server.stop();
		server = null;
	}
	
	private LdapConnection connection;
	private SpineDirectoryService sds;
	
	@Before
	public void setup() throws Exception {
		connection = setupConnection(server);
		sds = new SpineDirectoryService(connection);
	}
	
	/**
	 * Creates the LdapConnection to use during tests (invoked during test setup)
	 */
	protected abstract LdapConnection setupConnection(final EmbeddedLDAPServer server) throws Exception;
	
	@Test
	public void testFindAllAccreditedSystems() throws NamingException, IOException {
		Assert.assertEquals(4, sds.findAccreditedSystems().list().size());
	}
	
	@Test
	public void testFindAllMessageHandlingServices() throws NamingException, IOException {
		connection.enableRequestPaging(2); // trigger multiple pages
		Assert.assertEquals(4, sds.findMessageHandlingServices().list().size());
	}
	
	@Test
	public void testFindAccreditedSystem() throws NamingException, IOException {
		connection.enableRequestPaging(50);
		
		final AccreditedSystem expected = new AccreditedSystem();
		expected.setUniqueIdentifier("asid-1");
		expected.setNhsAsSvcIAs(Arrays.asList("service-1:action-1"));
		expected.setNhsDateApproved("20150824120000");
		expected.setNhsIDCode("ods-code-1");
		expected.setNhsMHSPartyKey("party-key-1");
		
		final AccreditedSystem actual = sds.findAccreditedSystems()
			.withUniqueIdentifier(expected.getUniqueIdentifier())
			.withNhsAsSvcIA(expected.getNhsAsSvcIAs().iterator().next())
			.withNhsDateApproved(expected.getNhsDateApproved())
			.withNhsIDCode(expected.getNhsIDCode())
			.withNhsMHSPartyKey(expected.getNhsMHSPartyKey())
			.get();
		
		Assert.assertNotNull(actual);
		Assert.assertEquals(expected, actual);
		ReflectionAssert.assertReflectionEquals(expected, actual);
	}
	
	@Test
	public void testFindMessageHandlingService() throws NamingException, IOException {
		connection.disableRequestPaging();
		
		final MessageHandlingService expected = new MessageHandlingService();
		expected.setUniqueIdentifier("mhs-4");
		expected.setNhsDateApproved("20150824120000");
		expected.setNhsIDCode("ods-code-2");
		expected.setNhsMhsCPAId("cpa-1");
		expected.setNhsMHSPartyKey("party-key-1");
		expected.setNhsMhsSvcIAs(Arrays.asList("service-1:action-1"));

		final MessageHandlingService actual = sds.findMessageHandlingServices()
				.withUniqueIdentifier(expected.getUniqueIdentifier())
				.withNhsDateApproved(expected.getNhsDateApproved())
				.withNhsIDCode(expected.getNhsIDCode())
				.withNhsMhsCPAId(expected.getNhsMhsCPAId())
				.withNhsMHSPartyKey(expected.getNhsMHSPartyKey())
				.withNhsMhsSvcIA(expected.getNhsMhsSvcIAs().iterator().next())
				.get();
		
		Assert.assertNotNull(actual);
		Assert.assertEquals(expected, actual);
		ReflectionAssert.assertReflectionEquals(expected, actual);
	}
}
