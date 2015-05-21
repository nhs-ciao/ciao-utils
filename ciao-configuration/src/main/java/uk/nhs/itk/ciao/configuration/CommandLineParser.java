package uk.nhs.itk.ciao.configuration;

import java.util.Properties;

import uk.nhs.itk.ciao.exceptions.CIAOConfigurationException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class CommandLineParser {
	private static String ETCDURLPARAM = "etcdURL";
	private static String CONFIGPATHPARAM = "configPath";
	
	protected static void initialiseFromCLIArguments(CIAOConfig config,
					String args[], String cipName, String version, Properties defaultConfig) throws CIAOConfigurationException {
		// --etcdURL=http://127.0.0.1:4001
		// --configPath=/etc/ciao
		OptionParser parser = new OptionParser();
        parser.accepts( ETCDURLPARAM ).withRequiredArg();
        parser.accepts( CONFIGPATHPARAM ).withRequiredArg();
        parser.allowsUnrecognizedOptions();
        OptionSet options = parser.parse( args );
        
        String etcdURL = null;
        String configFilePath = null;
        
        if (options.has( ETCDURLPARAM )) {
        	etcdURL = options.valueOf(ETCDURLPARAM).toString();
        }
        if (options.has( CONFIGPATHPARAM )) {
        	configFilePath = options.valueOf(CONFIGPATHPARAM).toString();
        }
        
        config.initialise(etcdURL, configFilePath, cipName, version, defaultConfig);
	}
}
