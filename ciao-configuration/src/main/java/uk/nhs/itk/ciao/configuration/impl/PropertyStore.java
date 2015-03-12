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
	 * @param path Path to check for
	 * @return true if the property store exists and has values in it, false otherwise
	 * @throws IOException If the property store cannot be accessed
	 */
	public boolean storeExists(String path) throws Exception;
	
	/**
	 * Load all the configuration keys at the specified path
	 * @param path
	 */
	public void loadConfig(String path);
	public void setDefaults(String path, Properties defaultConfig);
	public String getConfigValue(String key);
}
