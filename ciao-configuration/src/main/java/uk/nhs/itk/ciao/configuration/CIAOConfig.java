package uk.nhs.itk.ciao.configuration;

import java.util.Properties;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.itk.ciao.configuration.impl.EtcdPropertyStore;
import uk.nhs.itk.ciao.configuration.impl.FilePropertyStore;
import uk.nhs.itk.ciao.configuration.impl.PropertyStore;

/**
 * The first time a CIP is run, it will attempt to connect to the etcd URL provided.
 * If it is able to connect, it will check whether configuration already exists for
 * this CIP. It will do this by looking for the `/ciao/cipname/version` key. If
 * config already exists, it will be used immediately. If no config exists, a
 * default set of configuration values will be added to the etcd repository.
 * If the CIP is unable to connect to the provided URL, an error will be returned.
 * 
 * If no etcd URL is provided, the CIP will look for a local configuration file
 * with the name `cipname-version.properties`. It will look in the path specified
 * or in a .ciao directory in the user's home directory if no path is given.
 * If the file is found, it will be used, and if not, a default
 * configuration file will be created.
 * 
 * All configuration values in CIAO CIPs should be accessed using this class.
 * 
 * @author Adam Hatherly
 */
public class CIAOConfig {
	
	private static String ETCDURLPARAM = "etcdURL";
	private static String CONFIGPATHPARAM = "configPath";
	
	private PropertyStore propertyStore = null;
	private static Logger logger = LoggerFactory.getLogger(CIAOConfig.class);

	/**
	 * Constructor to initialise the configuration for a CIAO CIP.
	 * @param args Command line arguments passed to the CIP, these will be used
	 * to identify the URL for etcd and/or the Path to look for a config file
	 * @param cipName Name of CIP
	 * @param version Version number of CIP
	 * @param defaultConfig Java properties object with default config values for CIP
	 * @throws Exception 
	 */
	public CIAOConfig(String args[], String cipName, String version, Properties defaultConfig) throws Exception {
		// --etcdURL=http://127.0.0.1:4001
		// --configPath=/etc/ciao
		OptionParser parser = new OptionParser();
        parser.accepts( ETCDURLPARAM ).withRequiredArg();
        parser.accepts( CONFIGPATHPARAM ).withRequiredArg();
        OptionSet options = parser.parse( args );
        
        String etcdURL = null;
        String configFilePath = null;
        
        if (options.has( ETCDURLPARAM )) {
        	etcdURL = options.valueOf(ETCDURLPARAM).toString();
        }
        if (options.has( CONFIGPATHPARAM )) {
        	configFilePath = options.valueOf(CONFIGPATHPARAM).toString();
        }
        
        initialise(etcdURL, configFilePath, cipName, version, defaultConfig);
	}
	
	/**
	 * Constructor to initialise the configuration for a CIAO CIP.
	 * @param etcdURL URL for etcd (or null to use a local config file)
	 * @param configFilePath Path to look for config file
	 * @param cipName Name of CIP
	 * @param version Version number of CIP
	 * @param defaultConfig Java properties object with default config values for CIP
	 * @throws Exception 
	 */
	public CIAOConfig(String etcdURL, String configFilePath,
			String cipName, String version, Properties defaultConfig) throws Exception {
		
		initialise(etcdURL, configFilePath, cipName, version, defaultConfig);
	}
	
	/**
	 * Method to retrieve the configuration value for a given key
	 * @param key
	 * @return value of configuration item
	 * @throws Exception if the configuration path was not initialised correctly
	 */
	public String getConfigValue(String key) throws Exception {
		if (this.propertyStore == null) {
			throw new Exception("Configuration not initialised correctly - see error logs for details.");
		} else {
			return this.propertyStore.getConfigValue(key);
		}
	}
	
	private void initialise(String etcdURL, String configFilePath,
			String cipName, String version, Properties defaultConfig) throws Exception {
		
		// See if we have an ETCD URL
		if (etcdURL != null) {
			EtcdPropertyStore etcd = new EtcdPropertyStore(etcdURL);
			try {
				if (etcd.storeExists(cipName, version)) {
					logger.info("Found etcd config at URL: " + etcdURL);
					etcd.loadConfig(cipName, version);
					this.propertyStore = etcd;
				} else {
					logger.info("etcd config not yet initialised for this CIP");
					etcd.setDefaults(cipName, version, defaultConfig);
					logger.info("Initialised default etcd config for this CIP at URL: " + etcdURL);
					this.propertyStore = etcd;
				}
			} catch (Exception e) {
				logger.info("Can't connect to ETCD URL provided");
				throw e;
			}
		} else {
			logger.info("No ETCD URL provided, using local configuration");
			// Fall-back on file-based config
			FilePropertyStore fileStore = new FilePropertyStore(configFilePath);
			if (fileStore.storeExists(cipName, version)) {
				logger.info("Found file-based config at path: {}", fileStore.getPath());
				fileStore.loadConfig(cipName, version);
				this.propertyStore = fileStore;
			} else {
				fileStore.setDefaults(cipName, version, defaultConfig);
				logger.info("Initialised default file-based config for this CIP at path: {}", fileStore.getPath());
				this.propertyStore = fileStore;
			}
		}
	}
}
