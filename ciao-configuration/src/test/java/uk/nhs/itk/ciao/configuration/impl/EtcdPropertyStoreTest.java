package uk.nhs.itk.ciao.configuration.impl;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URI;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.responses.EtcdException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EtcdPropertyStoreTest {

	public static final String CIPNAME = "ciao-configuration-test";
	//private static final String ETCDURL = "http://104.155.27.125:80";
	public static final String ETCDURL = "http://10.210.162.21:80";
	public static final String VERSION = "v1";

	private static Logger logger = LoggerFactory.getLogger(EtcdPropertyStoreTest.class);

	/**
	 * Removes test data created when executing the unit test
	 * @throws IOException
	 * @throws EtcdException
	 * @throws TimeoutException
	 */
	public static void removeTestData() throws IOException, EtcdException, TimeoutException {
		logger.info("Attempting to initialise ETCD with URL: {}", ETCDURL);
		StringBuffer path = new StringBuffer();
		path.append("ciao/").append(CIPNAME).append('/').append(VERSION);
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
	
	private EtcdPropertyStore createInitialStore() {
		EtcdPropertyStore etcdStore = new EtcdPropertyStore(ETCDURL); 
		Properties defaultConfig = new Properties();
		defaultConfig.setProperty("testProperty1", "testValue1");
		defaultConfig.setProperty("testProperty2", "testValue2");
		try {
			etcdStore.setDefaults(CIPNAME, VERSION, defaultConfig);
		} catch (Exception e1) {
			e1.printStackTrace();
			fail(e1.getMessage());
		}
		return etcdStore;
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
		removeTestData();
	}

	
	@Test
	public void testStoreDoesntExist() {
		EtcdPropertyStore etcdStore = new EtcdPropertyStore(ETCDURL); 
		boolean result = false;
		try {
			result = etcdStore.storeExists(CIPNAME, VERSION);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Error accessing ETCD URL");
		}
		assertFalse(result);
	}
	
	@Test
	public void testCreateInitialStore() {
		EtcdPropertyStore etcdStore = createInitialStore();
		boolean result = false;
		try {
			result = etcdStore.storeExists(CIPNAME, VERSION);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Error accessing ETCD URL");
		}
		assertTrue(result);
	}
	
	@Test
	public void testGetConfigValue() {
		EtcdPropertyStore etcdStore = createInitialStore();
		try {
			etcdStore.loadConfig(CIPNAME, VERSION);
			logger.info("Attempting to read value for key: testProperty1");
			String val = etcdStore.getConfigValue("testProperty1");
			assertEquals("testValue1", val);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}	
	}
	
	@Test
	public void testGetMissingConfigValue() {
		EtcdPropertyStore etcdStore = createInitialStore();
		try {
			etcdStore.loadConfig(CIPNAME, VERSION);
			logger.info("Attempting to read value for invalid key: missingKey");
			String val = etcdStore.getConfigValue("missingKey");
			assertNull(val);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
