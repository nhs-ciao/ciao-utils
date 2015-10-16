package uk.nhs.ciao.configuration;

import java.util.Properties;

import uk.nhs.ciao.exceptions.CIAOConfigurationException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class CommandLineParser {
	private static String ETCDURLPARAM = "etcdURL";
	private static String CONFIGPATHPARAM = "configPath";
	private static String CLASSIFIERPARAM = "classifier";
	
	
	protected static void initialiseFromCLIArguments(CIAOConfig config,
					String args[], String cipName, String version, Properties defaultConfig) throws CIAOConfigurationException {
		// --etcdURL=http://127.0.0.1:4001
		// --configPath=/etc/ciao
		// --classifier=uat_cluster
		OptionParser parser = new OptionParser();
        parser.accepts( ETCDURLPARAM ).withRequiredArg();
        parser.accepts( CONFIGPATHPARAM ).withRequiredArg();
        parser.accepts( CLASSIFIERPARAM ).withRequiredArg();
        parser.allowsUnrecognizedOptions();
        OptionSet options = parser.parse( args );
        
        String etcdURL = null;
        String configFilePath = null;
        String classifier = null;
        
        if (options.has( ETCDURLPARAM )) {
        	etcdURL = options.valueOf(ETCDURLPARAM).toString();
        }
        if (options.has( CONFIGPATHPARAM )) {
        	configFilePath = options.valueOf(CONFIGPATHPARAM).toString();
        }
        if (options.has( CLASSIFIERPARAM )) {
        	classifier = options.valueOf(CLASSIFIERPARAM).toString();
        }
        
        config.initialise(etcdURL, configFilePath, cipName, version, defaultConfig, classifier);
	}
}
