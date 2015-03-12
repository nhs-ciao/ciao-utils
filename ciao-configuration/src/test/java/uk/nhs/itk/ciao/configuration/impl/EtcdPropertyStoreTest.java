package uk.nhs.itk.ciao.configuration.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.net.URI;

import mousio.etcd4j.EtcdClient;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EtcdPropertyStoreTest {

	private static final String CIPNAME = "ciao-configuration-test";
	private static final String ETCDURL = "http://104.155.27.125:80";
	private static final String VERSION = "v1";

	private static Logger logger = LoggerFactory.getLogger(EtcdPropertyStoreTest.class);
	
	/**
	 * Before we can test, we need to clear down the etcd entries for
	 * the "ciao-configuration-test" unit test path
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		logger.info("Attempting to initialise ETCD with URL: {}", ETCDURL);
		try(EtcdClient etcd = new EtcdClient(URI.create(ETCDURL))){
		  // Logs etcd version
		  logger.info(etcd.getVersion());
		  
		  // Directory and all subcontents delete
		  etcd.deleteDir(CIPNAME).recursive().send();
		}
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testStoreDoesntExist() {
		EtcdPropertyStore etcdStore = new EtcdPropertyStore(ETCDURL); 
		boolean result = false;
		try {
			result = etcdStore.storeExists(CIPNAME + "/" + VERSION + "/configured");
		} catch (Exception e) {
			e.printStackTrace();
			fail("Error accessing ETCD URL");
		}
		assertFalse(result);
	}

	@Test
	public void testCreateInitialStore() {
		//EtcdPropertyStore etcdStore = new EtcdPropertyStore(ETCDURL); 
		//boolean result = etcdStore.storeExists(CIPNAME + "/" + VERSION);
		//assertFalse(result);
		
	}
}
