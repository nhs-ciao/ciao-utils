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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.ciao.exceptions.CIAOConfigurationException;

/**
 * Convenience class to read configuration from a property file
 * @author Adam Hatherly
 */
public class FilePropertyStore implements PropertyStore {
	
	private static final String CIAO_PREFIX = ".ciao";
	
	private static final Logger logger = LoggerFactory.getLogger(FilePropertyStore.class);
	
	private String filePath = null;
	
	public String getPath() {
		return filePath;
	}
	
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
	
	public boolean versionExists(String cip_name, String version) throws CIAOConfigurationException {
		boolean exists = false;
		StringBuffer configFileName = new StringBuffer();
		configFileName.append(this.filePath).append('/').append(cip_name).append("-").append(version).append(".properties");
		logger.debug("Checking if config file exists at path: {}", configFileName);
		File f = new File(configFileName.toString());
		if (f.exists()) {
			exists = true;
			logger.debug("Yes, file exists");
		} else {
			logger.debug("No, file doesn't exist");
		}
		return exists;
	}

	public CipProperties setDefaults(String cip_name, String version, Properties defaultConfig) throws CIAOConfigurationException {
		// First, check the properties have not already been set
		if (versionExists(cip_name, version)) {
			throw new CIAOConfigurationException("The properties file has already been created for this CIP version");
		}
		StringBuffer configFileName = new StringBuffer();
		configFileName.append(this.filePath).append('/').append(cip_name).append("-").append(version).append(".properties");
		File f = new File(configFileName.toString());
		// Create parent directories if required
		File parent = f.getParentFile();
		if(!parent.exists() && !parent.mkdirs()){
		    throw new IllegalStateException("Couldn't create dir: " + parent);
		}
		
		// Write the default config to the appropriate path
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(f);
			defaultConfig.store(fos, "This is a default CIP configuration, please edit the below configuration to suit your installation.");
			fos.flush();
			fos.close();
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new CIAOConfigurationException(e);
		}
		
		// Also initialise our active config
		final CipProperties store = new MemoryCipProperties(cip_name, version, defaultConfig);
		logger.debug("Default configuration stored in path: {}", configFileName);
		return store;
	}

	public CipProperties loadConfig(String cip_name, String version) throws CIAOConfigurationException {
		StringBuffer configFileName = new StringBuffer();
		configFileName.append(this.filePath).append('/').append(cip_name).append("-").append(version).append(".properties");
		File f = new File(configFileName.toString());
		Properties props = new Properties();
		FileInputStream fis;
		try {
			fis = new FileInputStream(f);
			props.load(fis);
			fis.close();
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new CIAOConfigurationException(e);
		}
		
		return new MemoryCipProperties(cip_name, version, props);
	}
}
