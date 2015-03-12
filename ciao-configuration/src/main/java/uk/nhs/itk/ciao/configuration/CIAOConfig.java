package uk.nhs.itk.ciao.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.itk.ciao.configuration.impl.EtcdPropertyStore;
import uk.nhs.itk.ciao.configuration.impl.PropertyStore;

/**
 * The first time a CIP is run, it will attempt to connect to the etcd URL provided.
 * If it is able to connect, it will check whether configuration already exists for
 * this CIP. It will do this by looking for the `/ciao/<cipname>/version` key. If
 * config already exists, it will be used immediately. If no config exists, a
 * default set of configuration values will be added to the etcd repository.
 * If the CIP is unable to connect to the provided URL, an error will be returned.
 * 
 * If no etcd URL is provided, the CIP will look for a local configuration file
 * (as described above). If it is found, it will be used, and if not, a default
 * configuration file will be created in the user's home directory.
 * 
 * This is a combined factory and singleton to handle config for a running CIP. All
 * configuration values should be accessed through this class.
 * 
 * @author Adam Hatherly
 */
public class CIAOConfig {
	
	private static CIAOProperties propertyStore = null;
	private static Logger logger = LoggerFactory.getLogger(CIAOConfig.class);

	/**
	 * 
	 * @param etcdURL URL for etcd (or null to use a local config file)
	 * @param configFilePath Path to look for config file
	 * @param cipName Name of CIP
	 * @param version Version number of CIP
	 * @param defaultConfig InputStream with default config values for CIP
	 * @throws Exception 
	 */
	public static CIAOProperties getProperties(String etcdURL, String configFilePath,
			String cipName, String version, InputStream defaultConfig) throws Exception {
		
		// If we have already initialised the property store, return it
		if (propertyStore != null) {
			return propertyStore;
		} else {
			// See if we have an ETCD URL
			if (etcdURL == null) {
				logger.debug("No ETCD URL provided, using local configuration");
			} else {
				EtcdPropertyStore etcd = new EtcdPropertyStore(etcdURL);
				try {
					if (etcd.storeExists(cipName, version)) {
						logger.debug("Found etcd config at URL: " + etcdURL);
						
					}
				} catch (Exception e) {
					logger.debug("Can't connect to ETCD URL provided");
					throw e;
				}
			}
			
			return propertyStore;
		}
	}
	
	private void loadDefaultConfig() {
		
	}

	public String getConfigValue(String key) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
