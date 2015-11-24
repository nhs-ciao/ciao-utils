# ciao-spine-pds

*Library to provide a query-builder DSL for running queries against PDS.*

## Introduction

The library provides two main entry points:
- [PDSRoutes](src/main/java/uk/nhs/ciao/spine/pds/route/PDSRoutes.java) - [Apache Camel](http://camel.apache.org) routes for executing PDS queries
- [PersonalDemographicsService](src/main/java/uk/nhs/ciao/spine/pds/PersonalDemographicsService.java) - A fluent-builder / facade to execute PDS queries in an object-oriented fashion

The library provides representations of the following queries:
- [SimpleTrace](src/main/java/uk/nhs/ciao/spine/pds/model/SimpleTrace.java)


## Connecting to PDS

`PDSRoutes` connects to PDS over HTTP/HTTPS - the target URI is configured using `PDSRoutes.setPdsUri()`.

The following Spring XML configuration can be used to configure the Camel HTTP/HTTPS components:
```xml
<bean id="spineSSLContextParameters" class="org.apache.camel.util.jsse.SSLContextParameters">
	<!-- Configure a key manager -->
	<property name="keyManagers">
		<bean class="org.apache.camel.util.jsse.KeyManagersParameters">
			<property name="keyStore">
				<bean class="org.apache.camel.util.jsse.KeyStoreParameters">
					<property name="resource" value="${KEY_STORE}" />
					<property name="password" value="${KEY_STORE_PW}" />
				</bean>
			</property>
			<property name="keyPassword" value="${KEY_PASSWORD}" />
		</bean>
	</property>
		
	<!-- Configure a trust manager -->
	<property name="trustManagers">
		<bean class="org.apache.camel.util.jsse.TrustManagersParameters">
			<property name="keyStore">
				<bean class="org.apache.camel.util.jsse.KeyStoreParameters">
					<property name="resource" value="${TRUST_STORE}" />
					<property name="password" value="${TRUST_STORE_PW}" />
				</bean>
			</property>
		</bean>
	</property>
</bean>

<bean id="http" class="org.apache.camel.component.http4.HttpComponent" />
	
<bean id="https" class="org.apache.camel.component.http4.HttpComponent">
   <property name="sslContextParameters" ref="spineSSLContextParameters"/>
</bean>
```

Another important configuration property is `PDSRoutes.setPayloadBuilderRef()`. This property refers to an instance of [HL7PayloadBuilder](src/main/java/uk/nhs/ciao/spine/pds/hl7/HL7PayloadBuilder.class) stored in the Camel registry. It should be configured with details of the approperiate sender/receiver ASIDs and addresses.

## Querying SDS

The `PersonalDemographicsService` class exposes a fluent builder for creating and executing queries. Internally it uses the `PDSRoutes` API, so a running Camel instance is required.

### Example Queries

> Theses examples use a standard HTTP connection - if HTTPS is required, an instance of `SSLContextParameters` needs to be configured via `HttpComponent.setSslContextParameters()`.

The following example can be used to run queries against the [Spine Toolkit Workbench (TKW)](http://systems.hscic.gov.uk/sa/tools).

```java
// Setup registry (could also be handled via Spring XML config)
final SimpleRegistry registry = new SimpleRegistry();		
registry.put("http", new HttpComponent());

final HL7PayloadBuilder payloadBuilder = new HL7PayloadBuilder();
payloadBuilder.setPdsURL("http://127.0.0.1:4001/syncservice-pds/pds");
payloadBuilder.setFromAddress("from-address");
payloadBuilder.setReceiverASID("SIAB-001");
payloadBuilder.setSenderASID("sender-001");
registry.put("payloadBuilder", payloadBuilder);

// Setup Camel routes
final CamelContext context = new DefaultCamelContext(registry);
final ProducerTemplate producerTemplate = new DefaultProducerTemplate(context);

final PDSRoutes routes = new PDSRoutes();
routes.setRequestWiretapUri("log:request-wiretap");
routes.setResponseWiretapUri("log:response-wiretap");
routes.setPdsUri("http://127.0.0.1:4001/syncservice-pds/pds");

context.addRoutes(routes);

context.start();
producerTemplate.start();

// Create the service facade
final PersonalDemographicsService pds = new PersonalDemographicsService(producerTemplate, "direct:pds");

// Run queries
final Patient patient = pds.startSimpleTrace()
		.forSurname("EXAMPLE")
		.forGender(Sex._Male)
		.forDateOfBirth("19871124")
		.getPatient();
```

## Building and Running

To pull down the code, run:

	git clone https://github.com/nhs-ciao/ciao-utils.git
	
You can then compile the module via:

    cd ciao-spine-pds
	mvn clean install

This will compile the `ciao-spine-pds` module which can be found at `ciao-spine-pds\target\ciao-spine-pds-{version}.jar`.

To add the library to a Maven project include:
```xml
<dependency>
	<groupId>uk.nhs.ciao</groupId>
	<artifactId>ciao-spine-pds</artifactId>
	<version>${pds.version}</version>
</dependency>
```
