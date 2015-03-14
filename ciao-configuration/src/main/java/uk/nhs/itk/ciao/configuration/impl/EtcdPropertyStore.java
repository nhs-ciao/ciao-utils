package uk.nhs.itk.ciao.configuration.impl;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.responses.EtcdException;
import mousio.etcd4j.responses.EtcdKeysResponse;
import mousio.etcd4j.responses.EtcdKeysResponse.EtcdNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	public boolean storeExists(String cip_name, String version) throws Exception {
		StringBuffer path = new StringBuffer();
		path.append(CIAO_PREFIX).append('/').append(cip_name).append('/').append(version).append('/').append(EXISTENCE_KEY);
		boolean exists = false;
		EtcdClient etcd = null;
		try {
			logger.info("Attempting to access ETCD at URL: {}", this.url);
			etcd = new EtcdClient(URI.create(this.url));
			logger.info("Looking for ETCD path: {}", path.toString());
			EtcdKeysResponse val = etcd.get(path.toString()).send().get();
			logger.info("Got existence value: {}", val.node.value);
			if (val.node.value.equals("true")) {
				exists = true;
			}
		} catch (EtcdException e) {
			if (e.errorCode == 100) {
				logger.info("Got an ETCD exception (100) when trying to access ETCD store - the key doesn't exist");
			} else {
				logger.info("Got an ETCD exception when trying to access ETCD store", e);
				throw new Exception(e);
			}
		} finally {
			try {
				etcd.close();
			} catch (Exception e) {
				// Clean up connection
			}
		}
		return exists;
	}

	public void setDefaults(String cip_name, String version, Properties defaultConfig) throws Exception {
		// First, check the properties have not already been set
		if (storeExists(cip_name, version)) {
			throw new Exception("The ETCD properties have already been initialised for this CIP version");
		}
		StringBuffer path = new StringBuffer();
		path.append(CIAO_PREFIX).append('/').append(cip_name).append('/').append(version).append('/');
		EtcdClient etcd = null;
		try {
			logger.info("Attempting to access ETCD at URL: {}", this.url);
			etcd = new EtcdClient(URI.create(this.url));
			if (this.configValues == null) {
				this.configValues = new HashMap<String, String>();
			}
			for (Entry<Object, Object> entry : defaultConfig.entrySet()) {
				String key = entry.getKey().toString();
				String value = entry.getValue().toString();
				EtcdKeysResponse response = etcd.put(path.toString() + key, value).send().get();
				logger.info("Set value {} in path {}", response.node.value, path.toString() + key);
				// Also initialise our active config
				this.configValues.put(key, value);
			}
			// Add the "configured" key for future checking
			EtcdKeysResponse response = etcd.put(path.toString() + EXISTENCE_KEY, "true").send().get();
			logger.info("Set value {} in path {}", response.node.value, path.toString() + EXISTENCE_KEY);
		} catch (EtcdException e) {
			if (e.errorCode == 100) {
				logger.info("Got an ETCD exception (100) when trying to access ETCD store - the key doesn't exist");
			} else {
				logger.info("Got an ETCD exception when trying to access ETCD store", e);
				throw new Exception(e);
			}
		} finally {
			try { etcd.close(); } catch (Exception e) {}
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
			EtcdClient etcd = null;
			try {
				logger.info("Attempting to access ETCD at URL: {}", this.url);
				etcd = new EtcdClient(URI.create(this.url));
				StringBuffer path = new StringBuffer();
				path.append(CIAO_PREFIX).append('/').append(cip_name).append('/').append(version);
				
				// Retrieve all entries
				EtcdKeysResponse response = etcd.getDir(path.toString()).recursive().send().get();
				List<EtcdNode> entries = response.node.nodes;
				for (EtcdNode entry : entries) {
					String key = entry.key.substring(path.length()+2);
					this.configValues.put(key, entry.value);
					logger.info("Adding entry - key: {} , value: {}", key, entry.value);
				}
			} catch (EtcdException e) {
				if (e.errorCode == 100) {
					logger.info("Got an ETCD exception (100) when trying to access ETCD store - the key doesn't exist");
				} else {
					logger.info("Got an ETCD exception when trying to access ETCD store", e);
					throw new Exception(e);
				}
			} finally {
				try { etcd.close(); } catch (Exception e) {}
			}
		}
	}

}
