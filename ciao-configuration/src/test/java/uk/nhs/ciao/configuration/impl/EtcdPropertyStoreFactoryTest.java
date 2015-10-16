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

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.responses.EtcdException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.ciao.configuration.impl.EtcdPropertyStore;

public class EtcdPropertyStoreFactoryTest {

	private static final String CIAO_PREFIX = "ciao";
	public static final String CIPNAME = "ciao-configuration-test";
	//private static final String ETCDURL = "http://104.155.27.125:80";
	public static final String ETCDURL = "http://127.0.0.1:4001";
	public static final String VERSION = "v1";
	public static final String TEST_CLASSIFIER = "BLAH";

	private static Logger logger = LoggerFactory.getLogger(EtcdPropertyStoreFactoryTest.class);

	/**
	 * Removes test data created when executing the unit test
	 * @throws IOException
	 * @throws EtcdException
	 * @throws TimeoutException
	 */
	public static void removeTestData() throws IOException, EtcdException, TimeoutException {
		logger.info("Attempting to initialise ETCD with URL: {}", ETCDURL);
		StringBuffer path = new StringBuffer();
		path.append(CIAO_PREFIX).append('/').append(CIPNAME);//.append('/').append(VERSION);
		EtcdClient etcd = new EtcdClient(URI.create(ETCDURL));
	    // Logs etcd version
	    logger.info(etcd.getVersion());
	    logger.info("Removing previous test data at path: {}", path.toString());
        //Directory and all subcontents delete
	    try {
	    	etcd.deleteDir(path.toString()).recursive().send().get();
	    	logger.info("Removed");
	    } catch (EtcdException e) {
			if (e.errorCode == 100) {
				logger.info("Data did not exist, nothing to remove.");
			} else {
				logger.info("Got an ETCD exception when trying to access ETCD store", e);
				throw e;
			}
		} finally {
			try {
				etcd.close();
			} catch (Exception e) {
				// Clean up connection
			}
		}
	}
	
	private EtcdPropertyStore etcdStore;

	private CipProperties createInitialStore(String classifier) {
		CipProperties initialStore = null;
		
		etcdStore = new EtcdPropertyStore(ETCDURL); 
		Properties defaultConfig = new Properties();
		defaultConfig.setProperty("testProperty1", "testValue1");
		defaultConfig.setProperty("testProperty2", "testValue2");
		try {
			initialStore = etcdStore.setDefaults(CIPNAME, VERSION, classifier, defaultConfig);
		} catch (Exception e1) {
			e1.printStackTrace();
			fail(e1.getMessage());
		}
		
		return initialStore;
	}
	
	@Before
	public void setUp() {
	}

	/**
	 * We should also remove the test data when we have finished a test
	 * @throws Exception
	 */
	@After
	public void tearDown() throws Exception {
		etcdStore = null;
		removeTestData();
	}

	
	@Test
	public void testVersionDoesntExist() {
		EtcdPropertyStore etcdStore = new EtcdPropertyStore(ETCDURL); 
		boolean result = false;
		try {
			result = etcdStore.versionExists(CIPNAME, VERSION, null);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Error accessing ETCD URL");
		}
		assertFalse(result);
	}
	
	@Test
	public void testCreateInitialStore() {
		createInitialStore(null);
		boolean result = false;
		try {
			result = etcdStore.versionExists(CIPNAME, VERSION, null);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Error accessing ETCD URL");
		}
		assertTrue(result);
	}
	
	@Test
	public void testCreateInitialStoreWithClassifier() {
		createInitialStore(TEST_CLASSIFIER);
		boolean result = false;
		try {
			result = etcdStore.versionExists(CIPNAME, VERSION, TEST_CLASSIFIER);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Error accessing ETCD URL");
		}
		assertTrue(result);
	}
	
	@Test
	public void testGetConfigValue() {
		createInitialStore(null);
		try {
			final CipProperties store = etcdStore.loadConfig(CIPNAME, VERSION, null);
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
		createInitialStore(TEST_CLASSIFIER);
		try {
			final CipProperties store = etcdStore.loadConfig(CIPNAME, VERSION, TEST_CLASSIFIER);
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
		createInitialStore(null);
		try {
			final CipProperties store = etcdStore.loadConfig(CIPNAME, VERSION, null);
			logger.info("Attempting to read value for invalid key: missingKey");
			String val = store.getConfigValue("missingKey");
			assertNull(val);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testGetDefaultConfigKeys() {
		// Capture the initially created store instance (i.e. no save->load round-trip in ETCD)
		final CipProperties store = createInitialStore(null);
		try {			
			logger.info("Attempting to read all config keys");
			
			// "configured" key is only returned if the configuration has been loaded from ETCD
			final Set<String> expected = new HashSet<String>(
						Arrays.asList("testProperty1", "testProperty2"));
			final Set<String> actual = store.getConfigKeys();
			assertEquals(expected, actual);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testGetPreviouslySavedConfigKeys() {
		createInitialStore(null);
		try {
			final CipProperties store = etcdStore.loadConfig(CIPNAME, VERSION, null);
			logger.info("Attempting to read all config keys");
			
			// "configured" key is automatically registered in ETCD
			final Set<String> expected = new HashSet<String>(
						Arrays.asList("testProperty1", "testProperty2", "configured"));
			final Set<String> actual = store.getConfigKeys();
			assertEquals(expected, actual);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
