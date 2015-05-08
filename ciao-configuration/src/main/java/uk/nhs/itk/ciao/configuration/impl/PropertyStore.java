/*
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package uk.nhs.itk.ciao.configuration.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import uk.nhs.itk.ciao.exceptions.CIAOConfigurationException;

/**
 * Implementations of the CIAO property store. These should be accessed
 * through the CIAOConfig class.
 * @see uk.nhs.itk.ciao.configuration.CIAOConfig
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
	public boolean storeExists(String cip_name, String version) throws CIAOConfigurationException;
	
	/**
	 * Load all the configuration keys at the specified path
	 * @param cip_name Name of CIP
	 * @param version Version of CIP
	 */
	public void loadConfig(String cip_name, String version) throws CIAOConfigurationException;
	
	/**
	 * Populate the property store with the provided default values
	 * @param cip_name Name of CIP
	 * @param version Version of CIP
	 * @param defaultConfig Java properties object with default values to set
	 * @throws Exception If unable to set default config values
	 */
	public void setDefaults(String cip_name, String version, Properties defaultConfig) throws CIAOConfigurationException;
	
	/**
	 * Retrieve a configuration value for the provided key
	 * @param key Key to identify config value
	 * @return Value of configuration item
	 * @throws Exception If unable to retrieve config value
	 */
	public String getConfigValue(String key) throws CIAOConfigurationException;
	
	/**
	 * Returns a java properties object containing all configuration values
	 * @return Java properties object
	 * @throws Exception If unable to retrieve config values
	 */
	public Properties getAllProperties() throws CIAOConfigurationException;
	
	/**
	 * Print all the configuration keys and values - useful for debugging purposes
	 * @return All key-value pairs held
	 */
	public String toString();
}
