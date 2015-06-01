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
package uk.nhs.ciao.configuration.impl;

import java.util.Properties;
import java.util.Set;

import uk.nhs.ciao.exceptions.CIAOConfigurationException;

/**
 * Implementations of the Ciao CipProperties should be accessed
 * through the CIAOConfig class.
 * <p>
 * This interface provides methods to access a versioned set of properties for 
 * a CIP. An instance of this class should be obtained from an appropriate
 * {@link PropertyStore}.
 * <p>
 * The property store implementations handles the specifics of how/where
 * properties are stored (e.g. file system, etcd) and can load
 * multiple versions of CipProperties. In contrast, CipProperties provides
 * access to a specific set of versioned properties.
 * 
 * @see uk.nhs.ciao.configuration.CIAOConfig
 * @author Adam Hatherly
 */
public interface CipProperties {
	/**
	 * The name of the CIP these properties are associated with
	 */
	String getCipName();
	
	/**
	 * The version of the CIP properties
	 */
	String getVersion();
	
	/**
	 * Retrieve a configuration value for the provided key
	 * @param key Key to identify config value
	 * @return Value of configuration item
	 * @throws Exception If unable to retrieve config value
	 */
	public String getConfigValue(String key) throws CIAOConfigurationException;
	
	
	/**
	 * Returns the set of configuration keys associated with this store
	 * @return A set of configuration keys
	 * @throws Exception If unable to retrieve config keys
	 */
	public Set<String> getConfigKeys() throws CIAOConfigurationException;
	
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
