package uk.nhs.itk.ciao.configuration.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

/**
 * Implementations of the CIAO property store. These should be accessed
 * through the CIAOProperties class.
 * @author Adam Hatherly
 */
public interface PropertyStore {
	
	/**
	 * Checks whether the property store exists.
	 * @param cip_name Name of CIP
	 * @param version Version of CIP
	 * @return true if the property store exists and has values in it, false otherwise
	 * @throws IOException If the property store cannot be accessed
	 */
	public boolean storeExists(String cip_name, String version) throws Exception;
	
	/**
	 * Load all the configuration keys at the specified path
	 * @param cip_name Name of CIP
	 * @param version Version of CIP
	 */
	public void loadConfig(String cip_name, String version) throws Exception;
	
	public void setDefaults(String cip_name, String version, Properties defaultConfig) throws Exception;
	
	public String getConfigValue(String key) throws Exception;
}
