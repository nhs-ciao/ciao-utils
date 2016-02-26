package uk.nhs.ciao.configuration.impl;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.ciao.exceptions.CIAOConfigurationException;

/**
 * A CipProperties implementation where properties are maintained in an in-memory map
 */
public class MemoryCipProperties implements CipProperties {
	private static final Logger LOGGER = LoggerFactory.getLogger(MemoryCipProperties.class);
	
	private final String cipName;
	private final String version;
	private final Map<String, String> configValues;
	
	/**
	 * Initialise an empty in-memory store
	 * @param cipName CIP Name
	 * @param version CIP version
	 */
	public MemoryCipProperties(final String cipName, final String version) {
		this.cipName = cipName;
		this.version = version;
		this.configValues = new LinkedHashMap<String, String>();
	}
	
	/**
	 * Initialise an in-memory store as a copy of another property store
	 * @param copy
	 * @throws CIAOConfigurationException
	 */
	public MemoryCipProperties(final CipProperties copy) throws CIAOConfigurationException {
		this(copy.getCipName(), copy.getVersion(), copy.getAllProperties());
	}
	
	/**
	 * Initialise an in-memory store with given property values
	 * @param cipName CIP Name
	 * @param version CIP Version
	 * @param configValues Config values to initialise
	 */
	public MemoryCipProperties(final String cipName, final String version, final Properties configValues) {
		this(cipName, version);
		
		addConfigValues(configValues);
	}
	
	/**
	 * Initialise an in-memory store with given property values
	 * @param cipName CIP Name
	 * @param version CIP Version
	 * @param configValues Config values to initialise
	 */
	public MemoryCipProperties(final String cipName, final String version,
			final Map<? extends String, ? extends String> configValues) {
		this(cipName, version);
		
		addConfigValues(configValues);
	}
	
	public String getCipName() {
		return cipName;
	}
	
	public String getVersion() {
		return version;
	}
	
	public String getConfigValue(final String key) {
		if (LOGGER.isDebugEnabled()) {
			if (!this.configValues.containsKey(key)) {
				LOGGER.debug("Key not found: {}", key);
			}
			LOGGER.debug("Values: {}", this.configValues);
		}
		
		return this.configValues.get(key);
	}
	
	public boolean containsValue(String key) {
		return this.configValues.containsKey(key);
	}
	
	public Set<String> getConfigKeys() {
		return Collections.unmodifiableSet(configValues.keySet());
	}
	
	public Properties getAllProperties() {
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
	 * Store a key/value in the store
	 * @param key Key
	 * @param value Value
	 */
	public void setConfigValue(final String key, final String value) {
		configValues.put(key, value);
	}
	
	/**
	 * Add a set of config into the store
	 * @param configValues config values to set
	 */
	public final void addConfigValues(final Properties configValues) {
		if (configValues != null) {
			for (final Enumeration<?> enumeration = configValues.propertyNames(); enumeration.hasMoreElements();) {
		      final String key = (String) enumeration.nextElement();
		      final String value = configValues.getProperty(key);
		      this.configValues.put(key, value);
		      
		      LOGGER.debug("Adding entry - key: {} , value: {}", key, value);
		    }
		}
	}
	
	/**
	 * Add a set of config into the store
	 * @param configValues config values to set
	 */
	public final void addConfigValues(final Map<? extends String, ? extends String> configValues) {
		if (configValues != null) {
			this.configValues.putAll(configValues);
			
			LOGGER.debug("Adding entries: {}", configValues);
		}
	}
	
	public final void removeKey(final String key) {
		configValues.remove(key);
	}

	public void addConfigValue(String key, String value) {
		configValues.put(key, value);
	}
}
