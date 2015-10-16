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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.ciao.configuration.impl.FilePropertyStore;

public class FilePropertyStoreFactoryTest {

	private static final String CIPNAME = "ciao-configuration-test";
	private static final String VERSION = "v1";
	public static final String TEST_CLASSIFIER = "BLAH";
	
	private static Logger logger = LoggerFactory.getLogger(FilePropertyStoreFactoryTest.class);
	
	/**
	 * Removes test data created when executing the unit test
	 */
	public static void removeTestData(String classifier) {
		String home = System.getProperty("user.home").replace('\\', '/');
		StringBuffer filePath = new StringBuffer();
		filePath.append(home).append("/.ciao/").append(CIPNAME).append('-').append(VERSION);
		if (classifier != null) {
			filePath.append('-').append(classifier);
		}
		filePath.append(".properties");
		File f = new File(filePath.toString());
		logger.info("Removing test data from path: {}", filePath);
		if (f.exists()) {
			if (f.delete()) {
				logger.info("Removed");
			} else {
				logger.error("Unable to delete configuration file at path: {}", filePath);
			}
		}
	}
	
	@Before
	public void setUp() throws Exception {
		removeTestData(null);
		removeTestData(TEST_CLASSIFIER);
	}

	/**
	 * We should also remove the test data when we have finished each test
	 */
	@After
	public void tearDown() throws Exception {
		removeTestData(null);
		removeTestData(TEST_CLASSIFIER);
	}
	
	@Test
	public void testVersionDoesntExist() {
		logger.info("Test that version doesn't exist");
		FilePropertyStore fileStore = new FilePropertyStore(null); 
		boolean result = false;
		try {
			result = fileStore.versionExists(CIPNAME, VERSION, null);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Error accessing config file");
		}
		assertFalse(result);
	}
	
	private FilePropertyStore createInitialStore(String classifier) {
		FilePropertyStore fileStore = new FilePropertyStore(null); 
		Properties defaultConfig = new Properties();
		defaultConfig.setProperty("testProperty1", "testValue1");
		defaultConfig.setProperty("testProperty2", "testValue2");
		try {
			fileStore.setDefaults(CIPNAME, VERSION, classifier, defaultConfig);
		} catch (Exception e1) {
			e1.printStackTrace();
			fail(e1.getMessage());
		}
		return fileStore;
	}
	
	@Test
	public void testCreateInitialStore() {
		FilePropertyStore fileStore = createInitialStore(null); 
		boolean result = false;
		try {
			result = fileStore.versionExists(CIPNAME, VERSION, null);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Error accessing config file");
		}
		assertTrue(result);
	}
	
	@Test
	public void testCreateInitialStoreWithClassifier() {
		FilePropertyStore fileStore = createInitialStore(TEST_CLASSIFIER); 
		boolean result = false;
		try {
			result = fileStore.versionExists(CIPNAME, VERSION, TEST_CLASSIFIER);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Error accessing config file");
		}
		assertTrue(result);
	}
	
	@Test
	public void testGetConfigValue() {
		FilePropertyStore fileStore = createInitialStore(null);
		try {
			final CipProperties store = fileStore.loadConfig(CIPNAME, VERSION, null);
			logger.info("Attempting to read value for key: testProperty1");
			String val = store.getConfigValue("testProperty1");
			assertEquals("testValue1", val);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}	
	}
	
	@Test
	public void testGetConfigValueWithClassifier() {
		FilePropertyStore fileStore = createInitialStore(TEST_CLASSIFIER);
		try {
			final CipProperties store = fileStore.loadConfig(CIPNAME, VERSION, TEST_CLASSIFIER);
			logger.info("Attempting to read value for key: testProperty1");
			String val = store.getConfigValue("testProperty1");
			assertEquals("testValue1", val);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}	
	}
	
	@Test
	public void testGetMissingConfigValue() {
		FilePropertyStore fileStore = createInitialStore(null);
		try {
			final CipProperties store = fileStore.loadConfig(CIPNAME, VERSION, null);
			logger.info("Attempting to read value for invalid key: missingKey");
			String val = store.getConfigValue("missingKey");
			assertNull(val);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testGetConfigKeys() {
		final FilePropertyStore fileStore = createInitialStore(null);
		try {
			final CipProperties store = fileStore.loadConfig(CIPNAME, VERSION, null);
			logger.info("Attempting to read all config keys");
			final Set<String> expected = new HashSet<String>(
						Arrays.asList("testProperty1", "testProperty2"));
			final Set<String> actual = store.getConfigKeys();
			assertEquals(expected, actual);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
