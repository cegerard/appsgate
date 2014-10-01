#!/bin/bash
#this script transform apam distribution into one compatible with android
# Steps before executing this script
# Android SDK Configuration
# 1  Install Android SDK
# 2 With Android SDK Manager, install Android 4.4 KitKat (19.0.0)
# (3) Create an AVD (android) for Android 4.4 or use a real device (developer mode enabled, USB debugging enabled, root access)
# (4) Start the Emulator (emulator avd myEmulator)
# (6) Modify the ANDROIDPATH in this script accordingly to your Android SDK installation
# 7 Modify the ANDROIDADBDEVICE in this script accordingly to your devices (adb devices)
# 8 move this script on the directory ApAM/distributions/basic-distribution
# 9 Execute using sh build-android.sh

# script to start felix : /data/ehmi-middleware/felix-android.sh



ANDROIDPATH=/Users/thibaud/Development/android-sdk-macosx
ANDROIDVERSION=19.0.0

ANDROIDFELIXPATH=/data/ehmi-middleware

ANDROIDADBDEVICE=xxxxxx

COMPLETE=complete
TARGET=android-appsgate


export PATH=$PATH:$ANDROIDPATH/build-tools/$ANDROIDVERSION:$ANDROIDPATH/tools:$ANDROIDPATH/platform-tools

echo
echo XXXX
echo "Current PATH=$PATH"
echo XXXX



echo
echo XXXX
echo Creating temporary $TARGET files and directories
echo XXXX

rm -rf $TARGET

mkdir $TARGET
mkdir $TARGET/bin
mkdir $TARGET/bundle
mkdir $TARGET/conf
mkdir $TARGET/load
mkdir $TARGET/tmp

echo
echo XXXX
echo Creating dex file for felix.jar
echo XXXX
cp $COMPLETE/patch.felix.diff $TARGET/tmp/
cp $COMPLETE/bin/felix.jar $TARGET/tmp/felix.jar

cd $TARGET/tmp
patch -p0 felix.jar -i patch.felix.diff
dx --dex --output=classes.dex felix.jar
aapt add felix.jar classes.dex
mv felix.jar ../bin/felix.jar
cd ..
cd ..
rm $TARGET/tmp/*

echo
echo XXXX
echo Patching iPojo
echo XXXX
cp $COMPLETE/bundle/org.apache.felix.ipojo* $TARGET/tmp/
cd $TARGET/tmp
for jarfile in *.jar; do
    zip -d $jarfile org/osgi/*
    dx --debug --verbose --dex --output=classes.dex $jarfile
    aapt add $jarfile classes.dex
    mv $jarfile ../bundle/$jarfile
done
cd ..
cd ..
rm $TARGET/tmp/*



echo
echo XXXX
echo Creating dex files for the external libs bundles
echo XXXX
cp $COMPLETE/bundle/* $TARGET/tmp/
cd $TARGET/tmp
rm felix-gogo-*
rm apam-universal-shell-*
rm mail*
rm kxml*
rm json*
rm rxtx*.jar
rm obrman*
rm mqtt*
rm hawt*

find *.android -type f -exec mv {} {}.jar \; 


for jarfile in *.jar; do
	dx --dex --output=classes.dex $jarfile
	aapt add $jarfile classes.dex
	mv $jarfile ../bundle/$jarfile
done
cd ..
rm tmp/*
cd ..

echo
echo XXXX
echo Creating dex files for the loaded AppsGate bundles
echo XXXX

cp $COMPLETE/load/* $TARGET/tmp/
cd  $TARGET/tmp
for jarfile in *.jar; do
	dx --dex --output=classes.dex $jarfile
	aapt add $jarfile classes.dex
	mv $jarfile ../load/$jarfile
done
cd ..
rm tmp/*
cd ..


echo
echo XXXX
echo Creating script felix-android.sh
echo XXXX
echo "cd $ANDROIDFELIXPATH"  >   $TARGET/felix-android.sh
echo "rm -rf felix-cache" >>   $TARGET/felix-android.sh
echo "/system/bin/dalvikvm -Xms128m  -Xmx256m -classpath bin/felix.jar org.apache.felix.main.Main" >>  $TARGET/felix-android.sh


echo
echo XXXX
echo Setting configuration files
echo You may add dalvik packages to \"org.osgi.framework.system.packages.extra\"
echo depending on your own bundle needs
echo XXXX
cp -R $COMPLETE/conf   $TARGET
echo Making a new felix configuration file
rm $TARGET/conf/config.properties

echo "# Specific OSGi configuration File for Android" >   $TARGET/conf/config.properties

# Generic OSGI configuration stuff
echo "org.osgi.framework.storage.clean=onFirstInit" >>   $TARGET/conf/config.properties
echo "felix.auto.deploy.action=install,start" >>   $TARGET/conf/config.properties
echo "felix.log.level=1" >>   $TARGET/conf/config.properties
echo "felix.bootdelegation.implicit=false" >>   $TARGET/conf/config.properties
echo "felix.fileinstall.tmpdir="$ANDROIDFELIXPATH"/tmp" >>   $TARGET/conf/config.properties
echo "ipojo.proxy=disabled" >>   $TARGET/conf/config.properties

echo "org.osgi.service.http.port=8080" >>   $TARGET/conf/config.properties

# Configuration specficis for appsgate
echo "org.lig.appsgate.websocket.port=8087" >>   $TARGET/conf/config.properties
echo "org.lig.appsgate.weather.locations=Grenoble" >>   $TARGET/conf/config.properties
echo "fr.immotronic.enocean.databaseFilename=conf/enocean.db.json" >>   $TARGET/conf/config.properties
echo "fr.immotronic.enocean.tcm=USB" >>   $TARGET/conf/config.properties
echo "fr.immotronic.enocean.esp=ESP3" >>   $TARGET/conf/config.properties
echo "fr.imag.adele.apam.managers.configuration=MongoDBConfiguration:file:./conf/defaultMongoDBConfig.cfg" >>   $TARGET/conf/config.properties
echo "felix.upnpbase.importer.research.renewal=240" >>   $TARGET/conf/config.properties
echo "google.configuration.file=conf/google.cfg" >>   $TARGET/conf/config.properties



# Generic Configuration
#echo "obr.repository.url=http://felix.apache.org/obr/releases.xml" >>   $TARGET/conf/config.properties

# extra package used for apam
echo " org.osgi.framework.system.packages.extra= \\" >>   $TARGET/conf/config.properties
echo " org.json; \\" >>   $TARGET/conf/config.properties
echo " javax.xml.parsers; \\" >>   $TARGET/conf/config.properties
echo " org.xml.sax; \\" >>   $TARGET/conf/config.properties
echo " org.xml.sax.ext; \\" >>   $TARGET/conf/config.properties
echo " org.xml.sax.helpers; \\" >>   $TARGET/conf/config.properties
echo " javax.net.ssl; \\" >>   $TARGET/conf/config.properties
echo " javax.security.auth; \\" >>   $TARGET/conf/config.properties

# extra package used with appsgate
echo " org.w3c.dom; \\" >>   $TARGET/conf/config.properties
echo " org.w3c.dom.ls; \\" >>   $TARGET/conf/config.properties
echo " javax.net; \\" >>   $TARGET/conf/config.properties
echo " javax.security.auth.callback ;\\" >>   $TARGET/conf/config.properties
echo " javax.xml.xpath; \\" >>   $TARGET/conf/config.properties
echo " javax.xml; \\" >>   $TARGET/conf/config.properties
echo " javax.xml.transform; \\" >>   $TARGET/conf/config.properties
echo " javax.xml.transform.dom; \\" >>   $TARGET/conf/config.properties
echo " android.content; \\" >>   $TARGET/conf/config.properties
echo " android.database; \\" >>   $TARGET/conf/config.properties
echo " android.database.sqlite; \\" >>   $TARGET/conf/config.properties
echo " android.net; \\" >>   $TARGET/conf/config.properties
echo " android.os; \\" >>   $TARGET/conf/config.properties
echo " android.util; \\" >>   $TARGET/conf/config.properties
echo " android.widget; \\" >>   $TARGET/conf/config.properties
echo " org.apache.http; \\" >>   $TARGET/conf/config.properties
echo " org.apache.http.auth; \\" >>   $TARGET/conf/config.properties
echo " org.apache.http.client; \\" >>   $TARGET/conf/config.properties
echo " org.apache.http.client.methods; \\" >>   $TARGET/conf/config.properties
echo " org.apache.http.conn; \\" >>   $TARGET/conf/config.properties
echo " org.apache.http.conn.scheme; \\" >>   $TARGET/conf/config.properties
echo " org.apache.http.conn.ssl; \\" >>   $TARGET/conf/config.properties
echo " org.apache.http.cookie; \\" >>   $TARGET/conf/config.properties
echo " org.apache.http.entity; \\" >>   $TARGET/conf/config.properties
echo " org.apache.http.impl.auth; \\" >>   $TARGET/conf/config.properties


echo " org.apache.http.impl.client; \\" >>   $TARGET/conf/config.properties
echo " org.apache.http.impl.conn.tsccm; \\" >>   $TARGET/conf/config.properties
echo " org.apache.http.impl.conn; \\" >>   $TARGET/conf/config.properties
echo " org.apache.http.message; \\" >>   $TARGET/conf/config.properties
echo " org.apache.http.params; \\" >>   $TARGET/conf/config.properties
echo " org.apache.http.protocol; \\" >>   $TARGET/conf/config.properties
echo " org.apache.http.util; \\" >>   $TARGET/conf/config.properties
echo " javax.xml.datatype; \\" >>   $TARGET/conf/config.properties
echo " org.xmlpull.v1; \\" >>   $TARGET/conf/config.properties
echo " javax.xml.namespace" >>   $TARGET/conf/config.properties




echo
echo XXXX
echo Setting up Android terminal
echo These might not work on a real terminal, you should do the following commands as root on the device
echo desactivated
echo XXXX

echo "cd   $TARGET"
echo "adb -s $ANDROIDADBDEVICE shell rm -rf $ANDROIDFELIXPATH"
echo "adb -s $ANDROIDADBDEVICE shell mkdir -p $ANDROIDFELIXPATH/tmp"
echo "adb -s $ANDROIDADBDEVICE shell chmod -R 700 /data/ehmi-middleware"
echo "find * -type f -exec adb -s $ANDROIDADBDEVICE push {} $ANDROIDFELIXPATH/{} \;"
echo "cd .."

#cd   $TARGET
#adb -s $ANDROIDADBDEVICE shell rm -rf $ANDROIDFELIXPATH
#adb -s $ANDROIDADBDEVICE shell mkdir $ANDROIDFELIXPATH/tmp
#adb -s $ANDROIDADBDEVICE shell chmod 700 /data/ehmi-middleware
#find * -type f -exec adb -s $ANDROIDADBDEVICE push {} $ANDROIDFELIXPATH/{} \;
#cd ..

#echo
#echo XXXX
#echo Android terminal ready
echo XXXX
