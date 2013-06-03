README
------

Author : Didier Donsez

* build.xml
launch the generation

* project.*.properties
buildfile properties use to generate the classes associated to one device 

* device2build.xsl
XLST stylesheet that produces a build file that launches the transformations (device2class.xsl and scdp2class.xsl)

* device2class.xsl
XLST stylesheet that produces the UPnPDevice class

* scdp2modelitf.xsl
XLST stylesheet that produces the interface of a generated device model

* scdp2modelimpl.xsl
XLST stylesheet that produces the empty implementation of a device model

* scdp2skel.xsl
XLST stylesheet that produces the UPnPService class and UPnPStateVariable/UPnPAction inner-classes

* scdp2proxyimpl.xsl
XLST stylesheet that produces a proxy to a device according to a device model

* GenerationUtility.java
This class provides methods invoked by XLST tranformations
to convert strings in java-convention variables names, class names, packages, ...

* UPnPGenerationUtility.java
This class provides methods invoked by XLST tranformations
to extract service type, id and version

* UPnPDataTypeUtil.java
utility class for obtaining java class types for UPnP data type
