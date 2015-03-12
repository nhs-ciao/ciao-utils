package uk.nhs.itk.ciao.configuration.impl;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.promises.EtcdResponsePromise;
import mousio.etcd4j.responses.EtcdException;
import mousio.etcd4j.responses.EtcdKeysResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.itk.ciao.configuration.CIAOProperties;

public class EtcdPropertyStore implements PropertyStore, CIAOProperties {

	private HashMap<String, String> configValues = new HashMap<String, String>();
	private String url = null;
	
	private static Logger logger = LoggerFactory.getLogger(EtcdPropertyStore.class);
	
	public EtcdPropertyStore(String url) {
		this.url = url;
	}
	
	public boolean storeExists(String path) throws IOException, TimeoutException {
		boolean exists = false;
		try {
			logger.info("Attempting to access ETCD at URL: {}", this.url);
			EtcdClient etcd = new EtcdClient(URI.create(this.url));
			logger.info("Looking for ETCD path: {}", path);
			EtcdResponsePromise<EtcdKeysResponse> promise = etcd.get(path).send();
			EtcdKeysResponse val;
			val = promise.get();
			val.toString();
			etcd.close();
		} catch (EtcdException e) {
			if (e.errorCode == 100) {
				logger.info("Got an ETCD exception (100) when trying to access ETCD store - the key doesn't exist");
			} else {
				logger.info("Got an ETCD exception when trying to access ETCD store", e);
				e.printStackTrace();
			}
		}
		return exists;
	}

	public void setDefaults(String path, Properties defaultConfig) {
		// TODO Auto-generated method stub
		
	}

	public String getConfigValue(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	public void loadConfig(String path) {
		// TODO Auto-generated method stub
		
	}

}
