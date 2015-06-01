package uk.nhs.ciao.configuration.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.ciao.configuration.impl.CipProperties;
import uk.nhs.ciao.exceptions.CIAOConfigurationException;

/**
 * A {@link PropertyStore} which maintains properties in memory.
 */
public class MemoryPropertyStore implements PropertyStore {
	private static final Logger LOGGER = LoggerFactory.getLogger(MemoryPropertyStore.class);
	
	private final Map<Version, CipProperties> storesByVersion;
	
	public MemoryPropertyStore() {
		this.storesByVersion = new HashMap<Version, CipProperties>();
	}

	public boolean versionExists(final String cipName, final String cipVersion) throws CIAOConfigurationException {
		return storeExists(new Version(cipName, cipVersion));
	}

	public CipProperties setDefaults(final String cipName, final String cipVersion, final Properties defaultConfig) throws CIAOConfigurationException {
		final Version version = new Version(cipName, cipVersion);
		if (storeExists(version)) {
			throw new CIAOConfigurationException("The properties have already been created for CIP version: " );
		}
		
		// Initialise our active config
		final CipProperties store = new MemoryCipProperties(cipName, cipVersion, defaultConfig);
		LOGGER.debug("Default configuration stored for version: {}", version);
		
		// Store it against the version
		this.storesByVersion.put(version, store);
		return store;
	}
	
	public CipProperties loadConfig(final String cipName, final String cipVersion) throws CIAOConfigurationException {
		final Version version = new Version(cipName, cipVersion);
		if (!storeExists(version)) {
			throw new CIAOConfigurationException("No properties are available for CIP version: " + version);
		}

		return storesByVersion.get(version);
	}
	
	private boolean storeExists(final Version version) {
		return storesByVersion.containsKey(version);
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
