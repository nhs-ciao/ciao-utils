package uk.nhs.ciao.configuration.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.ciao.configuration.impl.PropertyStore;
import uk.nhs.ciao.exceptions.CIAOConfigurationException;

/**
 * A {@link PropertyStore} which maintains properties in memory-based.
 */
public class MemoryPropertyStore extends AbstractMapPropertyStore implements PropertyStore {
	private static final Logger LOGGER = LoggerFactory.getLogger(MemoryPropertyStore.class);
	
	private final Map<Version, Properties> propertiesByVersion;
	private Version activeVersion;
	
	public MemoryPropertyStore() {
		super(LOGGER);
		
		this.propertiesByVersion = new HashMap<Version, Properties>();
	}
	
	@Override
	public void setProperty(final String key, final String value) throws CIAOConfigurationException {
		requireLoaded();
		propertiesByVersion.get(activeVersion).setProperty(key, value);
		super.setProperty(key, value);
	}
	
	@Override
	public void addProperties(final Map<? extends String, ? extends String> properties) throws CIAOConfigurationException {
		requireLoaded();
		super.addProperties(properties);
	}

	public boolean storeExists(final String cipName, final String cipVersion) throws CIAOConfigurationException {
		return storeExists(new Version(cipName, cipVersion));
	}

	public void setDefaults(final String cipName, final String cipVersion, final Properties defaultConfig) throws CIAOConfigurationException {
		final Version version = new Version(cipName, cipVersion);
		if (storeExists(version)) {
			throw new CIAOConfigurationException("The properties have already been created for CIP version: " );
		}
		
		// Initialise our active config
		clear();
		activeVersion = version;
		addProperties(defaultConfig);
		LOGGER.debug("Default configuration stored for version: {}", version);
		
		// Store it against the version
		this.propertiesByVersion.put(version, getAllProperties());
	}
	
	public void loadConfig(final String cipName, final String cipVersion) throws CIAOConfigurationException {
		final Version version = new Version(cipName, cipVersion);
		if (isLoaded()) {
			return;
		} else if (!storeExists(version)) {
			throw new CIAOConfigurationException("No properties are available for CIP version: " + version);
		}		
		
		final Properties properties = propertiesByVersion.get(version);
		activeVersion = version;
		addProperties(properties);
	}
	
	private boolean storeExists(final Version version) {
		return propertiesByVersion.containsKey(version);
	}
	
	private static class Version {
		public final String cipName;
		public final String cipVersion;
		
		/**
		 * Class is immutable - the hash can be cached
		 */
		private int hash; // lazy-loaded
		
		public Version(final String cipName, final String cipVersion) {
			this.cipName = cipName;
			this.cipVersion = cipVersion;
		}
		
		@Override
		public String toString() {
			return cipName + ":" + cipVersion;
		}

		@Override
		public int hashCode() {
			int result = hash;
			
			// lazy-load
			if (result == 0) {
				final int prime = 31;
				result = 1;
				result = prime * result + ((cipName == null) ? 0 : cipName.hashCode());
				result = prime * result + ((cipVersion == null) ? 0 : cipVersion.hashCode());
				
				// hash is safe to publish without locks
				hash = result;
			}
			
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			} else if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			
			final Version other = (Version) obj;
			return equal(cipName, other.cipName) &&
					equal(cipVersion, other.cipVersion);
		}
	}
	
	/**
	 * Null-safe utility method to check two objects for equality
	 * <p>
	 * Equivalent to Objects.equals(Object, Object) in Java 1.7+
	 */
	private static boolean equal(final Object a, final Object b) {
		return (a == b) || (a != null && a.equals(b));
	}
}
