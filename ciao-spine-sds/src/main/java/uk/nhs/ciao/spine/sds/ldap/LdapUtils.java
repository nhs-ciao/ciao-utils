package uk.nhs.ciao.spine.sds.ldap;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility functions related to LDAP
 */
public final class LdapUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(LdapUtils.class);
	
	private LdapUtils() {
		// Suppress default constructor
	}
	
	/**
	 * Closes the specified context logging the exception (if thrown)
	 * 
	 * @param context The context to close
	 */
	public static void closeQuietly(final DirContext context) {
		if (context == null) {
			return;
		}
		
		try {
			context.close();
		} catch (final NamingException e) {
			LOGGER.debug("Unable to close context", e);
		}
	}
	
	/**
	 * Closes the specified enumeration logging the exception (if thrown)
	 * 
	 * @param context The enumeration to close
	 */
	public static void closeQuietly(final NamingEnumeration<?> enumeration) {
		if (enumeration == null) {
			return;
		}
		
		try {
			enumeration.close();
		} catch (final NamingException e) {
			LOGGER.debug("Unable to close enumeration", e);
		}
	}
}
