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

import com.google.common.collect.Lists;

import uk.nhs.ciao.spine.sds.ldap.DefaultLdapConnection;
import uk.nhs.ciao.spine.sds.ldap.LdapConnection;
import uk.nhs.ciao.spine.sds.model.AccreditedSystem;
import uk.nhs.ciao.spine.sds.model.MessageHandlingService;

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
		server = null;
	}
	
	private LdapConnection connection;
	private SpineDirectoryService sds;
	
	@Before
	public void setup() {
		connection = new DefaultLdapConnection(server.getLdapEnvironment());
		sds = new SpineDirectoryService(connection);
	}
	
	@Test
	public void testFindAllAccreditedSystems() throws NamingException, IOException {
		Assert.assertEquals(4, sds.findAccreditedSystems().list().size());
	}
	
	@Test
	public void testFindAllMessageHandlingServices() throws NamingException, IOException {
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
