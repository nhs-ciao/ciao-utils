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
package uk.nhs.ciao.configuration;

import java.util.Properties;

import uk.nhs.ciao.exceptions.CIAOConfigurationException;

/**
 * This class reads CIAO configuration values from environment variables, these can be set as follows:
 * 
 * export CIAO_ETCD_URL=http://127.0.0.1:4001
 * export CIAO_CONFIG_PATH=/etc/ciao
 * export CIAO_CONFIG_CLASSIFIER=uat_cluster
 * 
 * @author Adam Hatherly
 */
public class EnvironmentVariableInitialiser {
	
	/**
	 * Key of environment variable for ETCD URL
	 */
	public static final String ETCD_URL_KEY="CIAO_ETCD_URL";
	/**
	 * Key of environment variable for config file path
	 */
	public static final String ETCD_CONFIG_KEY="CIAO_CONFIG_PATH";
	/**
	 * Key of environment variable for configuration classifier (to differentiate multiple sets of config for the same CIP)
	 */
	public static final String ETCD_CLASSIFIER_KEY="CIAO_CONFIG_CLASSIFIER";

	/**
	 * If config initialisation values have not already been set (through command line parameters), this
	 * class looks for appropriate environment variables to use.
	 * @param config CIAO config object being initialised
	 * @param etcdURL URL for etcd (or null to use a local config file)
	 * @param configFilePath Path to look for config file
	 * @param cipName Name of CIP
	 * @param version Version number of CIP
	 * @param defaultConfig Java properties object with default config values for CIP
	 * @param classifier An optional classifier to allow multiple versions of config to exist for different running CIPs
	 * @throws CIAOConfigurationException
	 */
	protected static void initialiseFromEnvironmentVariables(CIAOConfig config, String etcdURL, String configFilePath,
			String cipName, String version, Properties defaultConfig, String classifier) throws CIAOConfigurationException {
		
		if (etcdURL == null) {
			String value = System.getenv(ETCD_URL_KEY);
            if (value != null) {
            	etcdURL = value;
            }
		}
		if (configFilePath == null) {
			String value = System.getenv(ETCD_CONFIG_KEY);
            if (value != null) {
            	configFilePath = value;
            }
		}
		if (classifier == null) {
			String value = System.getenv(ETCD_CLASSIFIER_KEY);
            if (value != null) {
            	classifier = value;
            }
		}
		config.initialiseConfig(etcdURL, configFilePath, cipName, version, defaultConfig, classifier, null);
	}
}
