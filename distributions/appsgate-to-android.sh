#!/bin/bash
#this script transform apam distribution into one compatible with android
# Steps before executing this script
# Android SDK Configuration
# 1  Install Android SDK
# 2 With Android SDK Manager, install Android 4.2.2 (17.0.0)
# (3) Create an AVD (android) for Android 4.2.2 or use a real device (developer mode enabled, USB debugging enabled, root access)
# (4) Start the Emulator (emulator avd myEmulator)
# (6) Modify the ANDROIDPATH in this script accordingly to your Android SDK installation
# 7 Modify the ANDROIDADBDEVICE in this script accordingly to your devices (adb devices)
# 8 move this script on the directory ApAM/distributions/basic-distribution
# 9 Execute using sh appsgate-to-android.sh

# script to start felix : /data/felix/felix-android.sh



ANDROIDPATH=/Users/thibaud/Development/android-sdk-macosx
ANDROIDVERSION=17.0.0

ANDROIDFELIXPATH=/data/felix

ANDROIDADBDEVICE=084972d8


export PATH=$PATH:$ANDROIDPATH/build-tools/$ANDROIDVERSION:$ANDROIDPATH/tools:$ANDROIDPATH/platform-tools

echo
echo XXXX
echo "Current PATH=$PATH"
echo XXXX



echo
echo XXXX
echo Creating temporary android-appsgate files and directories
echo XXXX

rm -rf android-appsgate

mkdir android-appsgate
mkdir android-appsgate/bin
mkdir android-appsgate/bundle
mkdir android-appsgate/conf
mkdir android-appsgate/load
mkdir android-appsgate/tmp

echo
echo XXXX
echo Creating dex file for felix.jar
echo XXXX
cp Appsgate-distribution/bin/felix.jar android-appsgate/tmp/felix.jar
cd android-appsgate/tmp
dx --dex --output=classes.dex felix.jar
aapt add felix.jar classes.dex
mv felix.jar ../bin/felix.jar
cd ..
cd ..
rm android-appsgate/tmp/*

echo
echo XXXX
echo Creating dex files for the external libs bundles
echo XXXX
cp Appsgate-distribution/bundle/* android-appsgate/tmp/
cd android-appsgate/tmp
rm felix-gogo-*
rm apam-universal-shell-*
rm mail*
rm mongo*.jar
rm org.apache.felix.ipojo-*.jar
rm kxml*
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

cp Appsgate-distribution/load/* android-appsgate/tmp/
cd android-appsgate/tmp
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
echo "cd $ANDROIDFELIXPATH"  > android-appsgate/felix-android.sh
echo "rm -rf felix-cache" >> android-appsgate/felix-android.sh
echo "/system/bin/dalvikvm -Xms128m  -Xmx256m -classpath bin/felix.jar org.apache.felix.main.Main" >> android-appsgate/felix-android.sh


echo
echo XXXX
echo Setting configuration files
echo You may add dalvik packages to \"org.osgi.framework.system.packages.extra\"
echo depending on your own bundle needs
echo XXXX
cp -R Appsgate-distribution/conf android-appsgate
echo updating felix configuration file
# echo "org.osgi.service.http.port=8080" >> android-appsgate/conf/config.properties

# Generic Configuration
echo "felix.fileinstall.tmpdir="$ANDROIDFELIXPATH"/tmp" >> android-appsgate/conf/config.properties
echo "ipojo.proxy=disabled" >> android-appsgate/conf/config.properties
echo "felix.bootdelegation.implicit=false" >> android-appsgate/conf/config.properties
echo "obr.repository.url=http://felix.apache.org/obr/releases.xml" >> android-appsgate/conf/config.properties

# extra package used for apam
echo " org.osgi.framework.system.packages.extra= \\" >> android-appsgate/conf/config.properties
echo " javax.xml.parsers; \\" >> android-appsgate/conf/config.properties
echo " org.xml.sax; \\" >> android-appsgate/conf/config.properties
echo " org.xml.sax.ext; \\" >> android-appsgate/conf/config.properties
echo " org.xml.sax.helpers; \\" >> android-appsgate/conf/config.properties
echo " javax.net.ssl; \\" >> android-appsgate/conf/config.properties
echo " javax.security.auth; \\" >> android-appsgate/conf/config.properties

# extra package used with appsgate
echo " org.w3c.dom; \\" >> android-appsgate/conf/config.properties
echo " org.w3c.dom.ls; \\" >> android-appsgate/conf/config.properties
echo " javax.net; \\" >> android-appsgate/conf/config.properties
echo " javax.security.auth.callback ;\\" >> android-appsgate/conf/config.properties
echo " javax.xml.xpath; \\" >> android-appsgate/conf/config.properties
echo " javax.xml; \\" >> android-appsgate/conf/config.properties
echo " javax.xml.transform; \\" >> android-appsgate/conf/config.properties
echo " javax.xml.transform.dom; \\" >> android-appsgate/conf/config.properties
echo " android.content; \\" >> android-appsgate/conf/config.properties
echo " android.database; \\" >> android-appsgate/conf/config.properties
echo " android.database.sqlite; \\" >> android-appsgate/conf/config.properties
echo " android.net; \\" >> android-appsgate/conf/config.properties
echo " android.os; \\" >> android-appsgate/conf/config.properties
echo " android.util; \\" >> android-appsgate/conf/config.properties
echo " android.widget; \\" >> android-appsgate/conf/config.properties
echo " org.apache.http; \\" >> android-appsgate/conf/config.properties
echo " org.apache.http.auth; \\" >> android-appsgate/conf/config.properties
echo " org.apache.http.client; \\" >> android-appsgate/conf/config.properties
echo " org.apache.http.client.methods; \\" >> android-appsgate/conf/config.properties
echo " org.apache.http.conn; \\" >> android-appsgate/conf/config.properties
echo " org.apache.http.conn.scheme; \\" >> android-appsgate/conf/config.properties
echo " org.apache.http.conn.ssl; \\" >> android-appsgate/conf/config.properties
echo " org.apache.http.cookie; \\" >> android-appsgate/conf/config.properties
echo " org.apache.http.entity; \\" >> android-appsgate/conf/config.properties
echo " org.apache.http.impl.auth; \\" >> android-appsgate/conf/config.properties
echo " org.apache.http.impl.client; \\" >> android-appsgate/conf/config.properties
echo " org.apache.http.impl.conn.tsccm; \\" >> android-appsgate/conf/config.properties
echo " org.apache.http.impl.conn; \\" >> android-appsgate/conf/config.properties
echo " org.apache.http.message; \\" >> android-appsgate/conf/config.properties
echo " org.apache.http.params; \\" >> android-appsgate/conf/config.properties
echo " org.apache.http.protocol; \\" >> android-appsgate/conf/config.properties
echo " org.apache.http.util; \\" >> android-appsgate/conf/config.properties
echo " javax.xml.datatype; \\" >> android-appsgate/conf/config.properties
echo " org.xmlpull.v1; \\" >> android-appsgate/conf/config.properties
echo " javax.xml.namespace" >> android-appsgate/conf/config.properties




echo
echo XXXX
echo Setting up Android terminal
echo These might not work on a real terminal, you should do the following commands as root on the device
echo desactivated
echo XXXX

echo "cd android-appsgate"
echo "adb -s $ANDROIDADBDEVICE shell rm -rf $ANDROIDFELIXPATH"
echo "adb -s $ANDROIDADBDEVICE shell mkdir $ANDROIDFELIXPATH/tmp"
echo "adb -s $ANDROIDADBDEVICE shell chmod 700 /data/felix"
echo "find * -type f -exec adb -s $ANDROIDADBDEVICE push {} $ANDROIDFELIXPATH/{} \;"
echo "cd .."

#cd android-appsgate
#adb -s $ANDROIDADBDEVICE shell rm -rf $ANDROIDFELIXPATH
#adb -s $ANDROIDADBDEVICE shell mkdir $ANDROIDFELIXPATH/tmp
#adb -s $ANDROIDADBDEVICE shell chmod 700 /data/felix
#find * -type f -exec adb -s $ANDROIDADBDEVICE push {} $ANDROIDFELIXPATH/{} \;
#cd ..

#echo
#echo XXXX
#echo Android terminal ready
echo XXXX
