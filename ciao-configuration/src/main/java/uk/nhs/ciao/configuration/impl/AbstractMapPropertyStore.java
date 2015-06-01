package uk.nhs.ciao.configuration.impl;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;

import uk.nhs.ciao.exceptions.CIAOConfigurationException;

abstract class AbstractMapPropertyStore implements PropertyStore {
	private final Logger logger;
	private Map<String, String> configValues;
	
	public AbstractMapPropertyStore(final Logger logger) {
		this.logger = logger;
	}
	
	/**
	 * Clears this store ready for reuse
	 */
	public void clear() {
		this.configValues = null;
	}
	
	/**
	 * Tests if this property store has been loaded with an active configuration
	 * <p>
	 * It is allowable for the store to be loaded with an empty set of properties.
	 */
	public boolean isLoaded() {
		return configValues != null;
	}
	
	public String getConfigValue(final String key) throws CIAOConfigurationException {
		requireLoaded();
		
		if (logger.isDebugEnabled()) {
			if (!this.configValues.containsKey(key)) {
				logger.debug("Key not found: {}", key);
			}
			logger.debug("Values: {}", this.configValues);
		}
		
		return this.configValues.get(key);
	}
	
	public Set<String> getConfigKeys() throws CIAOConfigurationException {
		requireLoaded();
		return Collections.unmodifiableSet(configValues.keySet());
	}
	
	public Properties getAllProperties() throws CIAOConfigurationException {
		requireLoaded();
		final Properties values = new Properties();
	    for (final Entry<String, String> property : configValues.entrySet()) {
	    	values.setProperty(property.getKey(), property.getValue());
	    }
	    return values;
	}
	
	@Override
	public String toString() {
		return configValues.toString();
	}
	
	/**
	 * Checks that the backing values map has been loaded
	 * @throws CIAOConfigurationException If the values map is not valid
	 * @see #isLoaded()
	 */
	protected void requireLoaded() throws CIAOConfigurationException {
		if (this.configValues == null) {
			throw new CIAOConfigurationException("The configuration for this CIP has not been initialised");
		}
	}
	
	protected void setProperty(final String key, final String value) throws CIAOConfigurationException {
		initConfigValues();
		configValues.put(key, value);
	}
	
	protected void addProperties(final Map<? extends String, ? extends String> properties) throws CIAOConfigurationException {
		initConfigValues();
		
		if (properties != null) {
			configValues.putAll(properties);
		}
	}
	
	protected void addProperties(final Properties properties) throws CIAOConfigurationException {
		initConfigValues();
		
		if (properties != null) {
			for (final Enumeration<?> enumeration = properties.propertyNames(); enumeration.hasMoreElements();) {
		      final String key = (String) enumeration.nextElement();
		      final String value = properties.getProperty(key);
		      configValues.put(key, value);
		      
		      logger.debug("Adding entry - key: {} , value: {}", key, value);
		    }
		}
	}
	
	private void initConfigValues() {
		if (configValues == null) {
			this.configValues = new LinkedHashMap<String, String>();
		}
	}
}
