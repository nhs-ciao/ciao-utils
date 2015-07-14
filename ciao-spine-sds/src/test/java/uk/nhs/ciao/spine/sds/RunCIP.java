package uk.nhs.ciao.spine.sds;


import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;

import uk.nhs.ciao.camel.CamelApplication;
import uk.nhs.ciao.camel.CamelApplicationRunner;
import uk.nhs.ciao.exceptions.CIAOConfigurationException;

/**
 * 
 * @author Adam Hatherly
 *
 */
public class RunCIP extends CamelApplication {
	public static final String CONFIG_FILE = "ciao-spine-sds.properties";
	private static final Logger logger = LoggerFactory.getLogger(RunCIP.class);
	
	/**
	 * This is the main class for running CIAO as a simple java process (e.g. within docker)
	 * @param args Command line arguments (e.g. ETCD URL)
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		final CamelApplication application = new RunCIP(args);
		CamelApplicationRunner.runApplication(application);
	}
	
	public RunCIP(final String[] args) throws CIAOConfigurationException {
		super(CONFIG_FILE, args);
	}
	
	@Override
	protected StaticApplicationContext createParentApplicationContext()
			throws CIAOConfigurationException {
		final StaticApplicationContext context = super.createParentApplicationContext();
		
		final String sslConfig;
		if (Boolean.parseBoolean(getCIAOConfig().getConfigValue("TLS_ENABLED"))) {
			logger.info("TLS enabled");
			sslConfig = "tls";
		} else {
			logger.info("TLS NOT enabled");
			sslConfig = "vanilla";
		}
		
		final Properties additionalProperties = new Properties();
		additionalProperties.setProperty("sslConfig", sslConfig);
		
		context.getEnvironment().getPropertySources().addFirst(new PropertiesPropertySource(
				"additionalProperties", additionalProperties));
		
		return context;
	}
}
