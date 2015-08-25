package uk.nhs.ciao.spine.sds;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.commons.io.FileUtils;
import org.apache.directory.server.constants.ServerDNConstants;
import org.apache.directory.server.core.CoreSession;
import org.apache.directory.server.core.DefaultDirectoryService;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.apache.directory.server.core.partition.ldif.LdifPartition;
import org.apache.directory.server.core.schema.SchemaPartition;
import org.apache.directory.server.ldap.LdapServer;
import org.apache.directory.server.protocol.shared.transport.TcpTransport;
import org.apache.directory.shared.ldap.entry.DefaultServerEntry;
import org.apache.directory.shared.ldap.entry.ServerEntry;
import org.apache.directory.shared.ldap.ldif.LdifEntry;
import org.apache.directory.shared.ldap.ldif.LdifReader;
import org.apache.directory.shared.ldap.name.DN;
import org.apache.directory.shared.ldap.schema.SchemaManager;
import org.apache.directory.shared.ldap.schema.ldif.extractor.SchemaLdifExtractor;
import org.apache.directory.shared.ldap.schema.ldif.extractor.impl.DefaultSchemaLdifExtractor;
import org.apache.directory.shared.ldap.schema.loader.ldif.LdifSchemaLoader;
import org.apache.directory.shared.ldap.schema.manager.impl.DefaultSchemaManager;
import org.apache.directory.shared.ldap.schema.registries.SchemaLoader;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.ciao.spine.sds.ldap.DefaultLdapConnection;
import uk.nhs.ciao.spine.sds.ldap.LdapConnection;

public class SpineDirectoryServiceTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(SpineDirectoryServiceTest.class);
	
	private static File workingDirectory;
	private static LdapServer ldapServer;
	private static DefaultDirectoryService directoryService;
	
	@BeforeClass
	public static void setupLdapServer() throws Exception {
	    workingDirectory = new File("target", "apacheds-work");
	    if (workingDirectory.exists()) {
	    	FileUtils.deleteDirectory(workingDirectory);
	    }
	    
	    workingDirectory.mkdir();

	    directoryService = new DefaultDirectoryService();	    
	    directoryService.setWorkingDirectory(workingDirectory);

	    final SchemaPartition schemaPartition = directoryService.getSchemaService().getSchemaPartition();
	    
	    final LdifPartition ldifPartition = new LdifPartition();
	    ldifPartition.setWorkingDirectory(new File(workingDirectory, "schema").getPath());

	    final File schemaRepository = new File(workingDirectory, "schema");
	    final SchemaLdifExtractor extractor = new DefaultSchemaLdifExtractor(workingDirectory);
	    extractor.extractOrCopy(true);

	    schemaPartition.setWrappedPartition(ldifPartition);

	    final SchemaLoader loader = new LdifSchemaLoader(schemaRepository);
	    final SchemaManager schemaManager = new DefaultSchemaManager(loader);
	    directoryService.setSchemaManager(schemaManager);
	    
	    schemaManager.loadAllEnabled();

	    schemaPartition.setSchemaManager(schemaManager);

	    final List<Throwable> errors = schemaManager.getErrors();
	    if (!errors.isEmpty()) {
	      throw new Exception("Schema load failed : " + errors);
	    }
	    
	    final JdbmPartition systemPartition = new JdbmPartition();
	    systemPartition.setId("system");
	    systemPartition.setPartitionDir(new File(workingDirectory, "system"));
	    systemPartition.setSuffix(ServerDNConstants.SYSTEM_DN);
	    systemPartition.setSchemaManager(schemaManager);
	    directoryService.setSystemPartition(systemPartition);

	    // main data partition
	    final JdbmPartition servicesPartition = new JdbmPartition();
	    servicesPartition.setId("Services");
	    servicesPartition.setPartitionDir(new File(workingDirectory, "services"));
	    servicesPartition.setSuffix("ou=Services,o=nhs");
	    servicesPartition.setSchemaManager(schemaManager);	    
	    directoryService.addPartition(servicesPartition);
	    
	    directoryService.setShutdownHookEnabled(false);
	    directoryService.getChangeLog().setEnabled(false);

	    ldapServer = new LdapServer();
	    ldapServer.setTransports(new TcpTransport(11389));
	    ldapServer.setDirectoryService(directoryService);

	    directoryService.startup();
	    ldapServer.start();
	    
	    // import the schema
	    importLdif(SpineDirectoryServiceTest.class.getResourceAsStream("minimised-sds-schema.ldif"));
	    setupServicesRootEntry();
	    
	    // import test data
	    importLdif(SpineDirectoryServiceTest.class.getResourceAsStream("test-data.ldif"));
	}
	
	private static void setupServicesRootEntry() throws Exception {
		final DN dn = new DN("ou=Services,o=nhs");
        final ServerEntry entry = directoryService.newEntry( dn );
        entry.add("ou", "Services");
        entry.add("objectClass", "top", "domain", "extensibleObject");
        entry.add("dc", "Services");
        directoryService.getAdminSession().add(entry);
	}
	
	private static void importLdif(final InputStream in) throws Exception {
	    final CoreSession session = directoryService.getAdminSession();
		for (final LdifEntry ldifEntry: new LdifReader(in)) {
			final ServerEntry entry = new DefaultServerEntry(directoryService.getSchemaManager(),
					ldifEntry.getEntry());
			session.add(entry);
	    }
	}
	
	@AfterClass
	public static void tearDownLdapServer() {
		if (ldapServer != null) {
			ldapServer.stop();
		}
		
		if (directoryService != null) {
			try {
				directoryService.shutdown();
			} catch (Exception e) {
				LOGGER.warn("Exception while trying to shutdown directoryService", e);
			}
			
			try {
				FileUtils.deleteDirectory(directoryService.getWorkingDirectory());
			} catch (IOException e) {
				LOGGER.warn("Exception while trying to clean workingDirectory", e);
			}
		}
	}
	
	private SpineDirectoryService sds;
	
	@Before
	public void setup() {
		final Hashtable<Object, Object> env = new Hashtable<Object, Object>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, "ldap://localhost:11389/");
		env.put("com.sun.jndi.ldap.connect.pool", "true");
		
		final LdapConnection connection = new DefaultLdapConnection(env);
		sds = new SpineDirectoryService(connection);
	}
	
	@Test
	public void anonAuth() throws NamingException, IOException {
		Assert.assertEquals(2, sds.findAccreditedSystems().list().size());
	}
}
