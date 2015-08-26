package uk.nhs.ciao.spine.sds;

import java.util.Hashtable;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchResult;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for {@link EmbeddedLDAPServer}
 */
public class EmbeddedLDAPServerTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedLDAPServerTest.class);
	private static EmbeddedLDAPServer ldapServer;
	
	@BeforeClass
	public static void setupLdapServer() throws Exception {
		ldapServer = new EmbeddedLDAPServer();
		ldapServer.start();
	}
	
	@AfterClass
	public static void tearDownLdapServer() {
		ldapServer.stop();
		ldapServer = null;
	}
	
	private InitialDirContext context;
	
	@Before
	public void setup() throws NamingException {
		final Hashtable<Object, Object> env = new Hashtable<Object, Object>(ldapServer.getLdapEnvironment());
		context = new InitialDirContext(env);
	}
	
	@After
	public void tearDown() throws NamingException {
		context.close();
	}
	
	/**
	 * Tests that the embedded ldap server can start properly and has loaded the schema and test data
	 */
	@Test
	public void testConnection() throws NamingException {
		final NamingEnumeration<SearchResult> results = context.search("ou=Services,o=nhs", "(objectclass=nhsAs)", null);
		int count = 0;
		try {
			while (results.hasMore()) {
				results.next();
				count++;
			}
		} finally {
			closeQuietly(results);
		}
		
		Assert.assertEquals(2, count);
	}

	private void closeQuietly(final NamingEnumeration<SearchResult> results) {
		if (results == null) {
			return;
		}
		
		try {
			results.close();
		} catch (Exception e) {
			LOGGER.warn("Unable to close seach results", e);
		}
	}
}
