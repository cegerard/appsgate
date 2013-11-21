Appsgate Distribution
=====================

Pre-requirements
----------------
-Have compiled and installed all the appsgate modules (mvn clean install -f appsGate-server/sources/appsgate-project/pom.xml)

How create the distribution
---------------------------

-compile and install the distribution module (mvn clean install -f appsGate-server/sources/appsgate-project/distribution/pom.xml)

How to run
----------

-Go to the directory (cd appsGate-server/sources/appsgate-project/distribution/target/chameleon-distribution)
-Launch chameleon (./chameleon.sh --interactive)


Tips
====

How to include a jar that is not available in maven repository?
----------------------------------------------------------------

-Its enough to copy it to appsGate-server/sources/appsgate-project/distribution/src/main/resources/, thus it will be automatically copied to the distribution as any other bundle that was brought by the dependency declaration
-All bundles installed in the platform are copied into appsGate-server/sources/appsgate-project/distribution/target/chameleon-distribution/runtime/ 

Troubleshooting
===============

-If the distribution compilation fails, that may come from the fact that the some module probabily was removed from the appsgate compilation, this module is shown in the error
-To be absolutely sure that your repository integrity, remove your maven repository then compile appsgate project and then generate the distribution, this will ensure that all the dependencies are downloaded and generated correctly
