package uk.nhs.itk.ciao.configuration;

import java.io.InputStream;

public interface CIAOProperties {
		
	/**
	 * This method retrieves a CIAO configuration entry
	 * @param key The key to retrieve
	 * @return The value of the configuration entry
	 */
	public String getConfigValue(String key);
}
