#!/bin/sh

mvn clean install
cd target
zip -d org.apache.felix.ipojo-1.11.1-SNAPSHOT.jar org/osgi/*
dx --debug --verbose --dex --output=classes.dex org.apache.felix.ipojo-1.11.1-SNAPSHOT.jar
aapt add org.apache.felix.ipojo-1.11.1-SNAPSHOT.jar classes.dex

adb push org.apache.felix.ipojo-1.11.1-SNAPSHOT.jar /data/felix/bundle
cd ..

