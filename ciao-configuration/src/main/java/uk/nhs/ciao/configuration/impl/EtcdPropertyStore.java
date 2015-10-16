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

import java.io.IOException;
import java.net.URI;
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

import uk.nhs.ciao.exceptions.CIAOConfigurationException;

public class EtcdPropertyStore implements PropertyStore {
	
	private static final String CIAO_PREFIX = "ciao";
	private static final String EXISTENCE_KEY = "configured";

	private String url = null;
	
	private static Logger logger = LoggerFactory.getLogger(EtcdPropertyStore.class);
	
	public EtcdPropertyStore(String url) {
		this.url = url;
	}

	public boolean versionExists(String cip_name, String version, String classifier) throws CIAOConfigurationException {
		StringBuffer path = makeConfigPath(cip_name, version, classifier).append(EXISTENCE_KEY);
		logger.debug("Checking for existence key at path: {}", path);
		boolean exists = false;
		EtcdClient etcd = null;
		try {
			logger.debug("Attempting to access ETCD at URL: {}", this.url);
			etcd = new EtcdClient(URI.create(this.url));
			logger.debug("Looking for ETCD path: {}", path);
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

	public CipProperties setDefaults(String cip_name, String version, String classifier, Properties defaultConfig) throws CIAOConfigurationException {
		// First, check the properties have not already been set
		if (versionExists(cip_name, version, classifier)) {
			throw new CIAOConfigurationException("The ETCD properties have already been initialised for this CIP version");
		}
		
		// Also initialise our active config
		final CipProperties store = new MemoryCipProperties(cip_name, version, defaultConfig);
		
		StringBuffer path = makeConfigPath(cip_name, version, classifier);
		EtcdClient etcd = null;
		try {
			logger.debug("Attempting to access ETCD at URL: {}", this.url);
			etcd = new EtcdClient(URI.create(this.url));
			for (Entry<Object, Object> entry : defaultConfig.entrySet()) {
				String key = entry.getKey().toString();
				String value = entry.getValue().toString();
				EtcdKeysResponse response = etcd.put(path.toString() + key, value).send().get();
				logger.debug("Set value {} in path {}", response.node.value, path.toString() + key);
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
		
		return store;
	}

	public CipProperties loadConfig(String cip_name, String version, String classifier) throws CIAOConfigurationException {
		final MemoryCipProperties store = new MemoryCipProperties(cip_name, version);
		
		EtcdClient etcd = null;
		try {
			logger.debug("Attempting to access ETCD at URL: {}", this.url);
			etcd = new EtcdClient(URI.create(this.url));
			StringBuffer path = makeConfigPath(cip_name, version, classifier);
			
			// Retrieve all entries
			EtcdKeysResponse response = etcd.getDir(path.toString()).recursive().send().get();
			List<EtcdNode> entries = response.node.nodes;
			for (EtcdNode entry : entries) {
				String key = entry.key.substring(path.length()+1);
				store.setConfigValue(key, entry.value);
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
		
		return store;
	}
	
	/**
	 * Builds a path for our etcd config using the CIP name, version, and (if it exists) the classifier.
	 * @param cip_name
	 * @param version
	 * @param classifier
	 * @return String filename
	 */
	private static StringBuffer makeConfigPath(String cip_name, String version, String classifier) {
		StringBuffer path = new StringBuffer();
		path.append(CIAO_PREFIX).append('/').append(cip_name).append('/').append(version);
		if (classifier != null) {
			path.append("/").append(classifier);
		}
		path.append("/");
		logger.debug("ETCD Path: {}", path);
		return path;
	}
}