# ciao-dts

*Library to create and parse DTS/MESH control files.*

## Introduction

The DTS/MESH client uses a file-based API to send and receive messages.

- Control files are used both when sending/receiving data messages, and when receiving reports. DTS control files are encoded using an XML format and use the `*.ctl` file-name extension.
- For data messages, a corresponding `data file` using the `*.dat` file-name extension is also present. The format of the data file is unspecified and is treated as a binary payload.

**Control File:**
-	[ControlFile](src/main/java/uk/nhs/ciao/dts/ControlFile.java) - provides a bean-like representation of a DTS control file.
-	[ControlFileParser](src/main/java/uk/nhs/ciao/dts/ControlFileParser.java) - parses an XML serialized control file to object form.
-	[ControlFileSerializer](src/main/java/uk/nhs/ciao/dts/ControlFileSerializer.java) - serializes a control file object into XML.
-	[ControlFileTypeConverter](src/main/java/uk/nhs/ciao/dts/ControlFileTypeConverter.java) - Integrates the control file parser and serializer with Camel.

> Camel is an optional dependency: the control file, parser and serializer can all be used without Camel. If Camel is added to the classpath, the type converter is automatically registered via the `META-INF/services` loader.

## Building and Running

To pull down the code, run:

	git clone https://github.com/nhs-ciao/ciao-utils.git
	
You can then compile the module via:

    cd ciao-dts
	mvn clean install

This will compile the `ciao-dts` module which can be found at `ciao-dts\target\ciao-dts-{version}.jar`.

To add the library to a Maven project include:
```xml
<dependency>
	<groupId>uk.nhs.ciao</groupId>
	<artifactId>ciao-dts</artifactId>
	<version>${sds.version}</version>
</dependency>
```
