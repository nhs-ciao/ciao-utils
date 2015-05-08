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
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.responses.EtcdException;
import mousio.etcd4j.responses.EtcdKeysResponse;
import mousio.etcd4j.responses.EtcdKeysResponse.EtcdNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.itk.ciao.exceptions.CIAOConfigurationException;

public class EtcdPropertyStore implements PropertyStore {
	
	private static final String CIAO_PREFIX = "ciao";
	private static final String EXISTENCE_KEY = "configured";

	private HashMap<String, String> configValues = null;
	private String url = null;
	
	private static Logger logger = LoggerFactory.getLogger(EtcdPropertyStore.class);
	
	public EtcdPropertyStore(String url) {
		this.url = url;
	}
	
	@Override
	public String toString() {
		return configValues.toString();
	}
	
	public boolean storeExists(String cip_name, String version) throws CIAOConfigurationException {
		StringBuffer path = new StringBuffer();
		path.append(CIAO_PREFIX).append('/').append(cip_name).append('/').append(version).append('/').append(EXISTENCE_KEY);
		boolean exists = false;
		EtcdClient etcd = null;
		try {
			logger.debug("Attempting to access ETCD at URL: {}", this.url);
			etcd = new EtcdClient(URI.create(this.url));
			logger.debug("Looking for ETCD path: {}", path.toString());
			EtcdKeysResponse val = etcd.get(path.toString()).send().get();
			logger.debug("Got existence value: {}", val.node.value);
			if (val.node.value.equals("true")) {
				exists = true;
			}
		} catch (EtcdException e) {
			if (e.errorCode == 100) {
				logger.debug("Got an ETCD exception (100) when trying to access ETCD store - the key doesn't exist");
			} else {
				logger.error("Got an ETCD exception when trying to access ETCD store", e);
				throw new CIAOConfigurationException(e);
			}
		} catch (IOException e) {
			logger.error("Got an IO exception when trying to access ETCD store", e);
			throw new CIAOConfigurationException(e);
		} catch (TimeoutException e) {
			logger.error("Timeout when trying to access ETCD store", e);
			throw new CIAOConfigurationException(e);
		} finally {
			try {
				etcd.close();
			} catch (Exception e) {
				// Clean up connection
			}
		}
		return exists;
	}

	public void setDefaults(String cip_name, String version, Properties defaultConfig) throws CIAOConfigurationException {
		// First, check the properties have not already been set
		if (storeExists(cip_name, version)) {
			throw new CIAOConfigurationException("The ETCD properties have already been initialised for this CIP version");
		}
		StringBuffer path = new StringBuffer();
		path.append(CIAO_PREFIX).append('/').append(cip_name).append('/').append(version).append('/');
		EtcdClient etcd = null;
		try {
			logger.debug("Attempting to access ETCD at URL: {}", this.url);
			etcd = new EtcdClient(URI.create(this.url));
			if (this.configValues == null) {
				this.configValues = new HashMap<String, String>();
			}
			for (Entry<Object, Object> entry : defaultConfig.entrySet()) {
				String key = entry.getKey().toString();
				String value = entry.getValue().toString();
				EtcdKeysResponse response = etcd.put(path.toString() + key, value).send().get();
				logger.debug("Set value {} in path {}", response.node.value, path.toString() + key);
				// Also initialise our active config
				this.configValues.put(key, value);
			}
			// Add the "configured" key for future checking
			EtcdKeysResponse response = etcd.put(path.toString() + EXISTENCE_KEY, "true").send().get();
			logger.debug("Set value {} in path {}", response.node.value, path.toString() + EXISTENCE_KEY);
		} catch (EtcdException e) {
			if (e.errorCode == 100) {
				logger.debug("Got an ETCD exception (100) when trying to access ETCD store - the key doesn't exist");
			} else {
				logger.error("Got an ETCD exception when trying to access ETCD store", e);
				throw new CIAOConfigurationException(e);
			}
		} catch (IOException e) {
			logger.error("Got an IO exception when trying to access ETCD store", e);
			throw new CIAOConfigurationException(e);
		} catch (TimeoutException e) {
			logger.error("Timeout when trying to access ETCD store", e);
			throw new CIAOConfigurationException(e);
		} finally {
			try { etcd.close(); } catch (Exception e) {}
		}
	}

	public String getConfigValue(String key) throws CIAOConfigurationException {
		if (this.configValues == null) {
			throw new CIAOConfigurationException("The configuration for this CIP has not been initialised");
		}
		if (!this.configValues.containsKey(key)) {
			logger.debug("Key not found: {}", key);
		}
		return this.configValues.get(key);
	}

	public void loadConfig(String cip_name, String version) throws CIAOConfigurationException {
		// TODO Auto-generated method stub
		if (this.configValues == null) {
			this.configValues = new HashMap<String, String>();
			EtcdClient etcd = null;
			try {
				logger.debug("Attempting to access ETCD at URL: {}", this.url);
				etcd = new EtcdClient(URI.create(this.url));
				StringBuffer path = new StringBuffer();
				path.append(CIAO_PREFIX).append('/').append(cip_name).append('/').append(version);
				
				// Retrieve all entries
				EtcdKeysResponse response = etcd.getDir(path.toString()).recursive().send().get();
				List<EtcdNode> entries = response.node.nodes;
				for (EtcdNode entry : entries) {
					String key = entry.key.substring(path.length()+2);
					this.configValues.put(key, entry.value);
					logger.debug("Adding entry - key: {} , value: {}", key, entry.value);
				}
			} catch (EtcdException e) {
				if (e.errorCode == 100) {
					logger.debug("Got an ETCD exception (100) when trying to access ETCD store - the key doesn't exist");
				} else {
					logger.error("Got an ETCD exception when trying to access ETCD store", e);
					throw new CIAOConfigurationException(e);
				}
			} catch (IOException e) {
				logger.error("Got an IO exception when trying to access ETCD store", e);
				throw new CIAOConfigurationException(e);
			} catch (TimeoutException e) {
				logger.error("Timeout when trying to access ETCD store", e);
				throw new CIAOConfigurationException(e);
			} finally {
				try { etcd.close(); } catch (Exception e) {}
			}
		}
	}
	
	public Properties getAllProperties() throws CIAOConfigurationException {
		Properties values = new Properties();
	    for (String key : configValues.keySet()) {
	    	values.setProperty(key, configValues.get(key));
	    }
	    return values;
	}

}
