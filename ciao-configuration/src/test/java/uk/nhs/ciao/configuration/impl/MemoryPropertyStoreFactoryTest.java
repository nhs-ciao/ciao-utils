/*
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package uk.nhs.ciao.configuration.impl;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.ciao.exceptions.CIAOConfigurationException;

public class MemoryPropertyStoreFactoryTest {

	private static final String CIPNAME = "ciao-configuration-test";
	private static final String VERSION = "v1";
	
	private static Logger logger = LoggerFactory.getLogger(MemoryPropertyStoreFactoryTest.class);
	
	private MemoryPropertyStore factory;
	
	@Before
	public void setUp() throws CIAOConfigurationException {
		factory = new MemoryPropertyStore(); 

		final Properties defaultConfig = new Properties();
		defaultConfig.setProperty("testProperty1", "testValue1");
		defaultConfig.setProperty("testProperty2", "testValue2");
		
		factory.setDefaults(CIPNAME, VERSION, defaultConfig);
	}
	
	@Test
	public void testVersionDoesntExist() throws CIAOConfigurationException {
		logger.info("Test that version doesn't exist");
		factory = new MemoryPropertyStore(); 
		final boolean exists = factory.versionExists(CIPNAME, VERSION);
		assertFalse(exists);
	}

	@Test
	public void testCreateInitialStore() throws CIAOConfigurationException {
		final boolean exists = factory.versionExists(CIPNAME, VERSION);
		assertTrue(exists);
	}
	
	@Test
	public void testGetConfigValue() throws CIAOConfigurationException {
		final CipProperties store = factory.loadConfig(CIPNAME, VERSION);
		logger.info("Attempting to read value for key: testProperty1");
		String val = store.getConfigValue("testProperty1");
		assertEquals("testValue1", val);
	}
	
	@Test
	public void testGetMissingConfigValue() throws CIAOConfigurationException {
		final CipProperties store = factory.loadConfig(CIPNAME, VERSION);
		logger.info("Attempting to read value for invalid key: missingKey");
		String val = store.getConfigValue("missingKey");
		assertNull(val);
	}
	
	@Test
	public void testGetConfigKeys() throws CIAOConfigurationException {
		final CipProperties store = factory.loadConfig(CIPNAME, VERSION);
		logger.info("Attempting to read all config keys");
		final Set<String> expected = new HashSet<String>(
					Arrays.asList("testProperty1", "testProperty2"));
		final Set<String> actual = store.getConfigKeys();
		assertEquals(expected, actual);
	}
}
