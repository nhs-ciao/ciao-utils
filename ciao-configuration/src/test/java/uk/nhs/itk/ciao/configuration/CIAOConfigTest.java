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
package uk.nhs.itk.ciao.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static uk.nhs.itk.ciao.configuration.impl.EtcdPropertyStoreTest.CIPNAME;
import static uk.nhs.itk.ciao.configuration.impl.EtcdPropertyStoreTest.ETCDURL;
import static uk.nhs.itk.ciao.configuration.impl.EtcdPropertyStoreTest.VERSION;

import java.io.File;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.itk.ciao.configuration.impl.EtcdPropertyStoreTest;
import uk.nhs.itk.ciao.configuration.impl.FilePropertyStoreTest;

public class CIAOConfigTest {

	private static Properties defaultConfig;
	private static Logger logger = LoggerFactory.getLogger(CIAOConfigTest.class);
	
	static {
		defaultConfig = new Properties();
		defaultConfig.setProperty("testProperty1", "testValue1");
		defaultConfig.setProperty("testProperty2", "testValue2");
	}
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
		EtcdPropertyStoreTest.removeTestData();
		FilePropertyStoreTest.removeTestData();
	}

	@Test
	public void testETCD() {
		try {
			CIAOConfig config = new CIAOConfig(ETCDURL, null, CIPNAME, VERSION, defaultConfig);
			assertEquals("testValue2", config.getConfigValue("testProperty2"));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testETCDMultipleAccesses() {
		try {
			CIAOConfig config = new CIAOConfig(ETCDURL, null, CIPNAME, VERSION, defaultConfig);
			// Now, lets create a second CIAOConfig to simulate a second client connecting - which should
			// pick up the same values and not set new defaults.
			CIAOConfig config2 = new CIAOConfig(ETCDURL, null, CIPNAME, VERSION, null);
			assertEquals("testValue2", config2.getConfigValue("testProperty2"));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testFileDefaultPath() {
		try {
			CIAOConfig config = new CIAOConfig(null, null, CIPNAME, VERSION, defaultConfig);
			assertEquals("testValue2", config.getConfigValue("testProperty2"));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testFileMultipleAccesses() {
		try {
			CIAOConfig config = new CIAOConfig(null, null, CIPNAME, VERSION, defaultConfig);
			// Now, lets create a second CIAOConfig to simulate a second client connecting - which should
			// pick up the same values and not set new defaults.
			CIAOConfig config2 = new CIAOConfig(null, null, CIPNAME, VERSION, null);
			assertEquals("testValue2", config2.getConfigValue("testProperty2"));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testFileSpecificPath() {
		String home = System.getProperty("user.home").replace('\\', '/');
		String path = home + "/testPath";
		try {
			CIAOConfig config = new CIAOConfig(null, path, CIPNAME, VERSION, defaultConfig);
			assertEquals("testValue2", config.getConfigValue("testProperty2"));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		// Remove it when we have finished
		logger.info("Removing test data from path: {}", path);
		deleteFilesInDir(path);
		new File(path).delete();
	}
	
	public static void deleteFilesInDir(String directory) {
		File dir = new File(directory);
		File files[] = dir.listFiles();
		if (files != null) {
			for(int index = 0; index < files.length; index++) {
				files[index].delete();
			}
		}
	}
	
	@Test
	public void testETCDWithCommandLineParams() {
		String args[] = new String[] { "--etcdURL", ETCDURL };
		
		try {
			CIAOConfig config = new CIAOConfig(args, CIPNAME, VERSION, defaultConfig);
			assertEquals("testValue2", config.getConfigValue("testProperty2"));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testFileSpecificPathWithCommandLineParameters() {
		String home = System.getProperty("user.home").replace('\\', '/');
		String path = home + "/testPath";
		String args[] = new String[] { "--configPath", path };
		try {
			CIAOConfig config = new CIAOConfig(args, CIPNAME, VERSION, defaultConfig);
			assertEquals("testValue2", config.getConfigValue("testProperty2"));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		// Remove it when we have finished
		logger.info("Removing test data from path: {}", path);
		deleteFilesInDir(path);
		new File(path).delete();
	}

}
