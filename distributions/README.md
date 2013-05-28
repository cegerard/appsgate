AppsGate distribution readme
----------------------------

The Appsgate distribution is a based on a standalone felix distribution with the OSGi/iPOJO/ApAM
layers.

Only one bundle needs an external data based to work correctly and this one is OS dependent.
the data base is mongo db:
http://www.mongodb.org/downloads

The mongo data base is not needed to make AppsGate-distribution work for now and if you don't want
to install it just ignore the connexion error messages from histman bundler or remove
histman-0.0.2-SNAPSHOT.jar and mongo-java-driver-2.10.1.jar from bundle directory.

The AppsGate-test-distribution is provide in order to test some bundle in the little distribution
with only some needed bundles.

