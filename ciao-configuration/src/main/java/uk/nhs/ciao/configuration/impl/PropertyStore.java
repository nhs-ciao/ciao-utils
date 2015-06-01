package uk.nhs.ciao.configuration.impl;

import java.io.IOException;
import java.util.Properties;

import uk.nhs.ciao.exceptions.CIAOConfigurationException;

/**
 * Implementations of the Ciao PropertyStore should be accessed
 * through the CIAOConfig class.
 * <p>
 * This interface provides methods to check and load versioned CIP
 * properties.
 * <p>
 * The property store implementations handles the specifics of how/where
 * properties are stored (e.g. file system, etcd) and can load
 * multiple versions of CipProperties. In contrast, CipProperties provides
 * access to a specific set of versioned properties.
 * 
 * @see uk.nhs.ciao.configuration.CIAOConfig
 * @author Adam Hatherly
 */
public interface PropertyStore {
	/**
	 * Checks whether the specified versioned properties exists in the store.
	 * @param cip_name Name of CIP
	 * @param version Version of CIP
	 * @return true if the version exists and has values in it, false otherwise
	 * @throws IOException If the property store cannot be accessed
	 */
	public boolean versionExists(String cip_name, String version) throws CIAOConfigurationException;
	
	/**
	 * Load all the configuration keys at the specified path
	 * @param cip_name Name of CIP
	 * @param version Version of CIP
	 * @return A loaded CipProperties instance matching the specified name and version
	 */
	public CipProperties loadConfig(String cip_name, String version) throws CIAOConfigurationException;
	
	/**
	 * Populate the property store with the provided default values
	 * @param cip_name Name of CIP
	 * @param version Version of CIP
	 * @param defaultConfig Java properties object with default values to set
	 * @throws Exception If unable to set default config values
	 * @return A CipProperties instance matching the specified name and version and configured with the specified defaults
	 */
	public CipProperties setDefaults(String cip_name, String version, Properties defaultConfig) throws CIAOConfigurationException;
}
