package uk.nhs.itk.ciao.configuration.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilePropertyStoreTest {

	private static final String CIPNAME = "ciao-configuration-test";
	private static final String VERSION = "v1";
	
	private static Logger logger = LoggerFactory.getLogger(FilePropertyStoreTest.class);
	
	/**
	 * Removes test data created when executing the unit test
	 */
	public static void removeTestData() {
		String home = System.getProperty("user.home").replace('\\', '/');
		StringBuffer filePath = new StringBuffer();
		filePath.append(home).append("/.ciao/").append(CIPNAME).append('-').append(VERSION).append(".properties");
		File f = new File(filePath.toString());
		logger.info("Removing test data from path: {}", filePath);
		if (f.exists()) {
			try {
				Files.delete(f.toPath());
				logger.info("Removed");
			} catch (IOException e) {
				logger.error("Unable to delete configuration file at path: {}", filePath, e);
			}
		}
	}
	
	@Before
	public void setUp() throws Exception {
		removeTestData();
	}

	/**
	 * We should also remove the test data when we have finished each test
	 */
	@After
	public void tearDown() throws Exception {
		removeTestData();
	}
	
	@Test
	public void testStoreDoesntExist() {
		logger.info("Test that store doesn't exist");
		FilePropertyStore fileStore = new FilePropertyStore(null); 
		boolean result = false;
		try {
			result = fileStore.storeExists(CIPNAME, VERSION);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Error accessing config file");
		}
		assertFalse(result);
	}
	
	private FilePropertyStore createInitialStore() {
		FilePropertyStore fileStore = new FilePropertyStore(null); 
		Properties defaultConfig = new Properties();
		defaultConfig.setProperty("testProperty1", "testValue1");
		defaultConfig.setProperty("testProperty2", "testValue2");
		try {
			fileStore.setDefaults(CIPNAME, VERSION, defaultConfig);
		} catch (Exception e1) {
			e1.printStackTrace();
			fail(e1.getMessage());
		}
		return fileStore;
	}
	
	@Test
	public void testCreateInitialStore() {
		FilePropertyStore fileStore = createInitialStore(); 
		boolean result = false;
		try {
			result = fileStore.storeExists(CIPNAME, VERSION);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Error accessing config file");
		}
		assertTrue(result);
	}
	
	@Test
	public void testGetConfigValue() {
		FilePropertyStore fileStore = createInitialStore();
		try {
			fileStore.loadConfig(CIPNAME, VERSION);
			logger.info("Attempting to read value for key: testProperty1");
			String val = fileStore.getConfigValue("testProperty1");
			assertEquals("testValue1", val);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}	
	}
	
	@Test
	public void testGetMissingConfigValue() {
		FilePropertyStore fileStore = createInitialStore();
		try {
			fileStore.loadConfig(CIPNAME, VERSION);
			logger.info("Attempting to read value for invalid key: missingKey");
			String val = fileStore.getConfigValue("missingKey");
			assertNull(val);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
