# ciao-utils

*CIAO related utilities, libraries and scripts*

## Modules

* [ciao-configuration](./ciao-configuration/README.md) - Provides configuration for CIAO CIPs (see the [CIP Architecture page](https://github.com/nhs-ciao/ciao-design/tree/master/CIPArchitecture) for details).
* [ciao-dts](./ciao-dts) - Library to create and parse [DTS/MESH](http://systems.hscic.gov.uk/spine/DTS) control files 
* [ciao-spine-pds](./ciao-spine-pds) - Library for connecting to and querying the [Personal Demographics Service (PDS)](http://systems.hscic.gov.uk/demographics/pds)
* [ciao-spine-sds](./ciao-spine-sds) - Library for connecting to and querying the [Spine Directory Service (SDS)](https://isd.hscic.gov.uk/trud3/user/guest/group/0/pack/5)

## Building

There is currently no top-level build script for the `ciao-utils` repository. Modules are built individually by navigating into the module folder and running maven. For example to build `ciao-dts` run:

```bash
cd ciao-dts
mvn clean install
```
