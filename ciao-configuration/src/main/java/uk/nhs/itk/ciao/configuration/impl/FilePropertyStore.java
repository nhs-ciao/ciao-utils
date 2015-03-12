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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Map.Entry;

import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.responses.EtcdException;
import mousio.etcd4j.responses.EtcdKeysResponse;
import mousio.etcd4j.responses.EtcdKeysResponse.EtcdNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convenience class to read configuration from a property file
 * @author Adam Hatherly
 */
public class FilePropertyStore {
	
	private static final String CIAO_PREFIX = ".ciao";
	private static final String EXISTENCE_KEY = "configured";
	
	private HashMap<String, String> configValues = null;
	private String filePath = null;
	
	private static Logger logger = LoggerFactory.getLogger(FilePropertyStore.class);
	
	public FilePropertyStore(String path) {
		if (path != null) {
			this.filePath = path;
		} else {
			// Use a default path in the user's home directory under $home/.ciao/
			String home = System.getProperty("user.home").replace('\\', '/');
			StringBuffer defaultPath = new StringBuffer();
			defaultPath.append(home).append('/').append(CIAO_PREFIX);
			this.filePath = defaultPath.toString();
		}
	}
	
	public boolean storeExists(String cip_name, String version) throws Exception {
		boolean exists = false;
		Properties defaultProperties = new Properties();
		StringBuffer configFileName = new StringBuffer();
		configFileName.append(this.filePath).append('/').append(cip_name).append("-").append(version).append(".properties");
		defaultProperties.load(new FileInputStream(new File(configFileName.toString())));
		if (defaultProperties.containsKey(EXISTENCE_KEY)) {
			exists = true;
		}
		return exists;
	}

	public void setDefaults(String cip_name, String version, Properties defaultConfig) throws Exception {
		// First, check the properties have not already been set
		if (storeExists(cip_name, version)) {
			throw new Exception("The properties file has already been created for this CIP version");
		}
		
	}

	public String getConfigValue(String key) throws Exception {
		if (this.configValues == null) {
			throw new Exception("The configuration for this CIP has not been initialised");
		}
		if (!this.configValues.containsKey(key)) {
			logger.info("Key not found: {}", key);
		}
		logger.info("Values: {}", this.configValues);
		return this.configValues.get(key);
	}

	public void loadConfig(String cip_name, String version) throws Exception {
		// TODO Auto-generated method stub
		if (this.configValues == null) {
			this.configValues = new HashMap<String, String>();
			
			
		}
	}
	
}
