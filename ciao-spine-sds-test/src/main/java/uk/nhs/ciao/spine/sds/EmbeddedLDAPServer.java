package uk.nhs.ciao.spine.sds;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;

import org.apache.commons.io.FileUtils;
import org.apache.directory.server.constants.ServerDNConstants;
import org.apache.directory.server.core.CoreSession;
import org.apache.directory.server.core.DefaultDirectoryService;
import org.apache.directory.server.core.DirectoryService;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * An embedded LDAP server to run tests against
 * <p>
 * This class configures and starts an embedded ApacheDS instance.
 */
public class EmbeddedLDAPServer {
	private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedLDAPServer.class);
	
	private static final String DEFAULT_SCHEMA_RESOURCE = "minimised-sds-schema.ldif";
	private static final String DEFAULT_DATA_RESOURCE = "test-data.ldif";
	private static final int DEFAULT_PORT = 11389;
	
	private final File workingDirectory;
	private String schemaResource = DEFAULT_SCHEMA_RESOURCE;
	private String dataResource = DEFAULT_DATA_RESOURCE;
	private int port = DEFAULT_PORT;
	
	private LdapServer ldapServer;
	private DirectoryService directoryService;
	
	/**
	 * Constructs a new LDAP server using a default working directory
	 * of ./target/apacheds-work
	 */
	public EmbeddedLDAPServer() {
		this(new File("target", "apacheds-work"));
	}
	
	/**
	 * Constructs a new LDAP server using the specified working directory
	 */
	public EmbeddedLDAPServer(final File workingDirectory) {
		this.workingDirectory = Preconditions.checkNotNull(workingDirectory);
	}
	
	/**
	 * The path used to load the schema LDIF resource (relative to this class)
	 */
	public EmbeddedLDAPServer setSchemaResource(final String schemaResource) {
		this.schemaResource = Preconditions.checkNotNull(schemaResource);
		return this;
	}
	
	/**
	 * The path used to load the data LDIF resource (relative to this class)
	 */
	public EmbeddedLDAPServer setDataResource(final String dataResource) {
		this.dataResource = Preconditions.checkNotNull(dataResource);
		return this;
	}
	
	/**
	 * The LDAP server port
	 */
	public int getPort() {
		return port;
	}
	
	/**
	 * Sets the LDAP server port
	 */
	public EmbeddedLDAPServer setPort(final int port) {
		this.port = port;
		return this;
	}
	
	/**
	 * Configures and starts the LDAP server
	 * <p>
	 * Any existing state in the working directory is cleared
	 * 
	 * @throws Exception If the server could not be fully started - stop() should
	 * 		still be called in the test tear down to fully clear all state
	 */
	public void start() throws Exception {
		 if (workingDirectory.exists()) {
		    	cleanWorkingDirectory();
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

		    // Create a (initially blank) partition for the test data
		    final JdbmPartition servicesPartition = new JdbmPartition();
		    servicesPartition.setId("Services");
		    servicesPartition.setPartitionDir(new File(workingDirectory, "services"));
		    servicesPartition.setSuffix("ou=Services,o=nhs");
		    servicesPartition.setSchemaManager(schemaManager);	    
		    directoryService.addPartition(servicesPartition);
		    
		    directoryService.setShutdownHookEnabled(false);
		    directoryService.getChangeLog().setEnabled(false);

		    ldapServer = new LdapServer();
		    ldapServer.setTransports(new TcpTransport(port));
		    ldapServer.setDirectoryService(directoryService);

		    directoryService.startup();
		    ldapServer.start();
		    
		    // import the schema
		    importLdif(getClass().getResourceAsStream(schemaResource));
		    
		    // import test data
		    setupServicesRootEntry();
		    importLdif(getClass().getResourceAsStream(dataResource));
	}
	
	/**
	 * Stops the server and clears any remaining state in the working directory
	 * <p>
	 * This should be called as part of the test teardown (even if start() failed)
	 */
	public void stop() {
		if (ldapServer != null) {
			ldapServer.stop();
		}
		
		if (directoryService != null) {
			try {
				directoryService.shutdown();
			} catch (Exception e) {
				LOGGER.warn("Exception while trying to shutdown directoryService", e);
			}
		}
		
		try {
			cleanWorkingDirectory();
		} catch (IOException e) {
			LOGGER.warn("Exception while trying to clean workingDirectory", e);
		}
		
		ldapServer = null;
		directoryService = null;
	}
	
	/**
	 * Cleans the working directory
	 * */
	public void cleanWorkingDirectory() throws IOException {
		FileUtils.deleteDirectory(workingDirectory);
	}
	
	/**
	 * Return an LDAP environment suitable for creating an InitialDirContext
	 */
	public Map<Object, Object> getLdapEnvironment() {
		final Map<Object, Object> env = new HashMap<Object, Object>();
		
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, "ldap://localhost:" + port + "/");
		env.put("com.sun.jndi.ldap.connect.pool", "true");

		return env;
	}
	
	/**
	 * Creates a new InitialLdapContext configured for use with this server
	 * 
	 * @param connCtls The initial connection controls to use
	 * @return A configured context
	 * @throws NamingException If the context could not be created
	 * @see #getLdapEnvironment()
	 */
	public InitialLdapContext createInitialLdapContext(final Control... connCtls) throws NamingException {
		final Hashtable<Object, Object> env = new Hashtable<Object, Object>(getLdapEnvironment());
		return new InitialLdapContext(env, connCtls);
	}
	
	/**
	 * Imports entries into the directory from the resource (specified in LDIF format)
	 */
	public void importLdif(final InputStream in) throws Exception {
	    final CoreSession session = directoryService.getAdminSession();
		for (final LdifEntry ldifEntry: new LdifReader(in)) {
			final ServerEntry entry = new DefaultServerEntry(directoryService.getSchemaManager(),
					ldifEntry.getEntry());
			session.add(entry);
	    }
	}
	
	/**
	 * Adds a root entry to the database for the ou=Services,o=nhs tree
	 * <p>
	 * The root must exist before any test data can be added
	 */
	private void setupServicesRootEntry() throws Exception {
		final DN dn = new DN("ou=Services,o=nhs");
        final ServerEntry entry = directoryService.newEntry( dn );
        entry.add("ou", "Services");
        entry.add("objectClass", "top", "domain", "extensibleObject");
        entry.add("dc", "Services");
        directoryService.getAdminSession().add(entry);
	}
}
