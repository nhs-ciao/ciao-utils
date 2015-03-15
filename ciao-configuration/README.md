# ciao-configuration

Provides configuration for CIAO CIPs (see the [CIP Architecture page](https://github.com/nhs-ciao/ciao-design/tree/master/CIPArchitecture) for details).

## Usage

The best way of managing the default configuration for your CIP is to use a java properties
file within your java project. This will be used to set the default configuration
when the CIP is first run (but will not over-write any changes made subsequently).

To make use of this ciao-configuration project in your CIP, simply add this in your POM:

```
	<dependency>
		<groupId>uk.nhs.interoperability</groupId>
		<artifactId>ciao-configuration</artifactId>
		<version>0.0.1-SNAPSHOT</version>
	</dependency>
```

Note: If you are using a snapshot build rather than a release build you will also need this:

```
	<repositories>
	<!-- This repository lets us use snapshot builds (only release
		    builds are propagated to maven central) -->
		<repository>
			<snapshots>
				<enabled>true</enabled>
			</snapshots>
			<id>oss</id>
			<name>OSS Sonatype</name>
			<url>https://oss.sonatype.org/content/groups/public/</url>
		</repository>
	</repositories>
```

### CIP Name and Version

To ensure the name and version is kept in-line with your maven pom, you can add these
lines in the properties file:

```
cip.version=${project.version}
cip.name=${project.name}
```

Then, in your POM, add the following lines:

```xml
    <build>
      <resources>
        <resource>
          <directory>src/main/resources</directory>
          <filtering>true</filtering>
        </resource>
      </resources>   
    </build>
```

This will allow maven to substitute the name and version from the POM into your
properties file each time you run a build.

### Load default CIP config values

You can add any additional CIP configuration into your properties file, for example:

```
PDSURL=http://127.0.0.1:4001/syncservice-pds/pds
```

You will need to pass this default configuration into the CIAOConfig object so it
can set defaults if needed. To do this, read the properties file from the classpath
as you normally would - e.g.:

```java
    private static Properties loadDefaultConfig() {
		InputStream in = null;
		Properties defaultProperties = new Properties();
        try {
        	in = PropertyReader.class.getClassLoader().getResourceAsStream(CONFIG_FILE);
            if (in != null) {
            	defaultProperties.load(in);
            	in.close();
            }
        } catch (Exception ex) {
       		logger.error("Default config not found: " + CONFIG_FILE, ex);
       		return null;
        } finally {
            try {
                if (in != null) {
                	in.close();
                }
            } catch (IOException ex) {
            }
        }
        return defaultProperties;
    }
```

Alternatively, if you don't have much config and would rather just set the values
directly from your code, you can do this:

```
    Properties defaultConfig = new Properties();
    defaultConfig.setProperty("testProperty1", "testValue1");
    defaultConfig.setProperty("testProperty2", "testValue2");
```


### Initialise CIP config

You can now use this ciao-configuration library to initialise the configuration - this
will deal with parsing command line parameters, connecting to etcd (if appropriate),
creating default config, etc.

To initialise the config:

```java
    Properties defaultConfig = loadDefaultConfig();
    String version = defaultConfig.get("cip.version").toString();
    String cipName = defaultConfig.get("cip.name").toString();
    CIAOConfig cipConfig = new CIAOConfig(args, cipName, version, defaultConfig);
```

### Using CIP config

You will want to re-use the CIAOConfig object wherever you need to access config
in your CIP. If you re-initialise the object, it will have to re-read the config values
each time, so by re-using the object you can avoid costly calls to etcd during CIP
execution.

One way to do this would be to implement a singleton object to hold the CIAOConfig
object in memory. Another approach is to add it into your Camel registry - for example:

```java
    JndiContext jndi = new JndiContext();
    jndi.bind("cipConfig", cipConfig);
    CamelContext context = new DefaultCamelContext(jndi);
```

Now, whenever you need to access config in a processor, you can then do it like this:

```java
    CIAOConfig ciaoConfig = exchange.getContext().getRegistry().lookupByNameAndType("cipConfig", CIAOConfig.class);
    String pdsURL = ciaoConfig.getConfigValue("PDSURL");
```
