package uk.nhs.ciao.spine.sds.ldap;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.junit.Test;
import org.mockito.Mockito;

/**
 * Unit tests for {@link LdapUtils}
 */
public class LdapUtilsTest {
	@Test
	public void testQuietlyCloseNamingEnumeration() throws NamingException {
		final NamingEnumeration<?> enumeration = Mockito.mock(NamingEnumeration.class);
		LdapUtils.closeQuietly(enumeration);
		Mockito.verify(enumeration).close();
	}
	
	@Test
	public void testQuietlyCloseNullNamingEnumeration() {
		final NamingEnumeration<?> enumeration = null;
		LdapUtils.closeQuietly(enumeration);
	}
	
	@Test
	public void testQuietlyCloseNamingEnumerationTrapsException() throws NamingException {
		final NamingEnumeration<?> enumeration = Mockito.mock(NamingEnumeration.class);
		Mockito.doThrow(NamingException.class).when(enumeration).close();
		LdapUtils.closeQuietly(enumeration);
		Mockito.verify(enumeration).close();
	}
	
	@Test
	public void testQuietlyCloseDirContext() throws NamingException {
		final DirContext context = Mockito.mock(DirContext.class);
		LdapUtils.closeQuietly(context);
		Mockito.verify(context).close();
	}
	
	@Test
	public void testQuietlyCloseNullDirContext() {
		final DirContext context = null;
		LdapUtils.closeQuietly(context);
	}
	
	@Test
	public void testQuietlyCloseDirContextTrapsException() throws NamingException {
		final DirContext context = Mockito.mock(DirContext.class);
		Mockito.doThrow(NamingException.class).when(context).close();
		LdapUtils.closeQuietly(context);
		Mockito.verify(context).close();
	}
}
