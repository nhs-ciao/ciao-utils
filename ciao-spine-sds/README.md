# ciao-spine-sds

*Library to provide model representations of SDS LDAP types and a query-builder DSL for finding matching objects in SDS.*

## Introduction

The Spine Directory Service (SDS) in an LDAP directory which defines various classes / types.

The core abstraction / entry point to the library is:
- [SpineDirectoryService](src/main/java/uk/nhs/ciao/spine/sds/SpineDirectoryService.java)

The library provides Java-bean like representations of the following LDAP classes:
- [AccreditedSystem](src/main/java/uk/nhs/ciao/spine/sds/model/AccreditedSystem.java)
- [MessageHandlingService](src/main/java/uk/nhs/ciao/spine/sds/model/MessageHandlingService.java)
- [MHSContractProperties](src/main/java/uk/nhs/ciao/spine/sds/model/MHSContractProperties.java)


## Connecting to SDS

`SpineDirectoryService` is backed by an implementation of [LdapConnection](src/main/java/uk/nhs/ciao/spine/sds/ldap/LdapConnection.java):
- [DefaultLdapConnection](src/main/java/uk/nhs/ciao/spine/sds/ldap/LdapConnection.java) - a standalone implementation using only the core Java LDAP classes
- [CamelLdapConnection](src/main/java/uk/nhs/ciao/spine/sds/ldap/CamelLdapConnection.java) - an implementation backed by the [Apache Camel LDAP](http://camel.apache.org/ldap.html) component

LdapConnection provides an abstraction for connecting to LDAP (possibly using connection pooling), executing search queries, and obtaining the search results (with or without paging). Clients can use an LdapConnection directly to query the server, however the SpineDirectoryService and query-builder provide a more convenient way of accessing the supported LDAP types.

## Querying SDS

The `SpineDirectoryService` class exposes a fluent builder for creating and executing queries against the supported LDAP types. The resulting objects are returned as structure model objects (similar to a database ORM tool).

The underlying LDAP connection is only used during querying, the resulting model objects are completely disconnected from the original connection. No state or session clean-up is required by the client and the objects are free for standard garbage collection.

### Example Queries
```java

// Configure the SDS LDAP connection
LdapConnection connection = ...
connection.enableRequestPaging(50);

// Initialise the SpineDirectoryService instance
SpineDirectoryService sds = new SpineDirectoryService(connection);

// Query for accredited systems
AccreditedSystem accreditedSystem = sds.findAccreditedSystems()
	.withUniqueIdentifier("an-id")
	.get()

accreditedSystem = sds.findAccreditedSystems()
	.withNhsAsSvcIA("some-svc-ia")
	.withNhsMHSPartyKey("a-party-key")
	.get()

// Query for message handling services
List<MessageHandlingService> messageHandlingServices = sds.findMessageHandlingServices()
	.withNhsIDCode("some-nhs-id")
	.withNhsMHSPartyKey("another-party-key")
	.list()

```

## Building and Running

To pull down the code, run:

	git clone https://github.com/nhs-ciao/ciao-utils.git
	
You can then compile the module via:

    cd ciao-spine-sds
	mvn clean install

This will compile the `ciao-spine-sds` module which can be found at `ciao-spine-sds\target\ciao-spine-sds-{version}.jar`.

To add the library to a Maven project include:
```xml
<dependency>
	<groupId>uk.nhs.ciao</groupId>
	<artifactId>ciao-spine-sds</artifactId>
	<version>${sds.version}</version>
</dependency>
```
