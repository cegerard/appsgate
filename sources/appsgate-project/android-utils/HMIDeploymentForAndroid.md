
# AppsGate HMI Distribution for Android #
Last Edition: 18/12/2013 - Thibaud Flury

## Notes about AppsGate HMI distribution from Android
This distribution is currently designed to be installed directly in the Android Device file system and executed as a service by root through init.rc startup script.

It is possible to embed the distribution with an APK (Android application package), but not convenient for the moment. As an application it does not keep the middleware state when user interacts with other applications. As a background service, this lead to unresolved class-loading problems (this should be considered as future work).

## Notes for developers
The Dalvik virtual machine on Android is very close to standard java virtual machine. But it is not compatible with virtual machine (bytecode is not the same). Bundle developers must pay attention to :
* Class files and bundles should be converted to DEX (Dalvik EXecutable) files
* Dalvik does not provide the same set of core packages (for instance javax.management, javax.awt or javax.swing are not available), sometimes packages with the same name will have anover behavior. Please refer to Android API reference : http://developer.android.com/reference/packages.html
* When two (or more) bundles exports the same package, this can leads to errors. For example osgi compendium is used to resolve org.osgi dependencies, users-bundles must not embed the needed packages.



## Device requisites to use AppsGate HMI
* Tested with Android 4.2.x (Jelly Bean API 17) and Android 4.3 (Jelly Bean API 18)
* Must be granted with root access on the Device
* Must have developer/debugging access enabled on the Device
* Device must have LAN access (though Wifi, Ethernet , or even reverse tethering with Chrome ADB tools)
* A mongoDB Database must be accessible through the LAN
* Device must be able to allocate 250 MB of RAM, and 50 MB of storage


## Tools for Deployments
* GNU-bash script interpreter
* Java Development Kit and Java Runtime 1.6+ (Tested with Oracle and OpenJDK)
* Maven 3.x
* Git
* Latest version of Android SdK (https://developer.android.com/sdk/index.html)
* Android 4.2 (API 17) or 4.3 (API 18) Build Tools (Downloaded using Android SdK Manager)

It is useful to configure some global variable for Android SdK directories (the following tutorials assume that the android command-line tools are directly accessible through their names):
* ANDROIDPATH=/path-to/android-sdk
* ANDROIDVERSION=17.0.0
* export PATH=$PATH:$ANDROIDPATH/build-tools/$ANDROIDVERSION:$ANDROIDPATH/tools:$ANDROIDPATH/platform-tools



## How to install AppsGate for Android

### 1. Prepare the Device
* Connect the device with the USB Cable (if possible) OR Connect to the device through the LAN
If connected through the LAN you have to retrieve the IP address connect the device :
> adb connect <DEVICE_IP>

* Check if your device is there using the command
> adb devices -l
If your device is in the list, save the first field (the device number or its address in the LAN) as a global variable:
ANDROIDADBDEVICE=084972d8
OR
ANDROIDADBDEVICE=192.168.1.3:5555	

* Log on the device console, then verify you are root
> adb -s $ANDROIDADBDEVICE shell
root@android:/ #

* Create the distribution directory (you might choose this directory according to you device)

root@android:/ # mkdir /data/ehmi-middleware
(it works well in the /data directory but may lead to problems in other directory such as /sdcard because of th write access for caching bundles)


* You can now exit the adb shell and prepare the distribution
root@android:/ # exit

###2. Prepare the distribution

* Checkout the latest distribution (or a particular release) from github
> git clone https://github.com/cegerard/appsGate-server.git

* Go to the distributions directory
> cd ./appsGate-server/distributions/

* Open the main script to create the distribution
./appsGate-server/distributions/appsgate-to-android.sh

* edit the global variables according to your configuration
(the value given here are just examples)

ANDROIDPATH=/Users/thibaud/Development/android-sdk-macosx
ANDROIDVERSION=17.0.0

ANDROIDFELIXPATH=/data/ehmi-middleware

ANDROIDADBDEVICE=192.168.1.5:5555

* Execute the script
> sh appsgate-to-android.sh

It will create a basic distribution for the HMI Middleware in the directory ./appsGate-server/distributions/android-appsgate

There are several warning message?during the creation of dex files for the third-party libraries, you can ignore them for the moment :
warning: Ignoring InnerClasses attribute for an anonymous inner class [...]
(Other errors should be investigated)


* Customize the distribution according to to your configuration and needs

./appsGate-server/distributions/android-appsgate/conf/config.properties allows you to configure the Felix OSGi distribution.
You may add system properties,
You may add packages to the property org.osgi.framework.system.packages.extra (these package MUST be available according to android package list), 
don't modify preconfigured value unless you know what you are doing. 

./appsGate-server/distributions/android-appsgate/conf/root.OBRMAN.cfg allows you to define OSGi bundle repositories for the root composite. It ius used to resolve dependencies upon ApAM components, downloading and starting bundle.
You may set the predefined value to false, except if you add the corresponding URL for repositories

./appsGate-server/distributions/android-appsgate/conf/root.PropertyHistoryManager.cfg
You must set the DBHost of mongoDB server (reminder, a mongo server should be accessible through the LAN)

###3. Install the distribution on the device

* You must copy the content of android-appsgate directory in the directory you previously created on the device (depending of your configuration you may have to change the properties of this directory)
> find * -type f -exec adb -s $ANDROIDADBDEVICE push {} $ANDROIDFELIXPATH/{} \;


###4. Run the distribution

* Just connect on the device shell as root and run the main script.
root@android:/ # > sh $ANDROIDFELIXPATH/felix-android.sh

Depending on the Device capabilities, it will take 1-5 min to startup. It does not run as background service so if you disconnect from adb shell, the middleware will stops.

## Additional notes

### Porting the distribution to Android

OSGi Framework on android
* Felix versions 4.2+ must be used (previous releases does not work with android)

Bundles modified or added
* iPojo 1.10+ (actually 1.11 is used) needs to remove org.osgi.* packages from the jar file, these dependencies have to be resolved installing the OSGi compendium (this one exports the needed bundles).
* org.json from android API behaves strangely with json array. Made a bundle from straight json maven artifact
* Jetty HTTP server recent versions does not work well with android. The old version of http service from felix is OK.
* Grizzly Websocket does not work on android. To use websocket, made a bundle from Java-WebSocket (from https://github.com/TooTallNate/Java-WebSocket).
* MongoDB API does not run out-of-the-box on Android. Version 2.12.0-SNAPSHOT have been manually patched (see https://jira.mongodb.org/browse/JAVA-295) to remove dependencies on javax.management
* JavaMail API does not work on android. Made a bundle from specific versions of mail.jar, activation.jar and additional.jar (from http://code.google.com/p/javamail-android). Warning sasl seems to be not supported.
* Made a bundle from official Philips HUE SdK (better than using upnp)
* Made a bundle from cybergarage (this one will be fully replaced with felix upnp base driver)
* Made a bundle from Stax (does not work well for the moment)

Specific Configuration of the framework (config.properties)
* felix.fileinstall.tmpdir= -> should be $ANDROIDFELIXPATH/tmp
* ipojo.proxy=disabled -> this one is mandatory for android
* felix.bootdelegation.implicit=false -> this one is mandatory for android
* obr.repository.url= -> depends on your configuration
* org.osgi.framework.system.packages.extra= -> is an explicit list of the used package (coming from Dalvik API package reference)

Scripts
* ./appsGate-server/sources/appsgate-project/android-utils/Scripts/script_android_jar.sh : used to make android compatible bundle from jar files
* ./appsGate-server/sources/appsgate-project/android-utils/Scripts/script_android_ipojo.sh : remove the org.osgi packages from the ipojo jar file
* ./appsGate-server/distributions/appsgate-to-android.sh : main script to create the android distribution from the latest appsgate distrib

### Known bugs
On some devices (as the Set-Top-Box) dalvik sometimes ends unexpectedly with a "Stack Fault" (no stack trace and no coredump). Maybe too much running bundles for the capabilities of the device (behavior not encountered with Nexus 7 device).


### TODO list
* Add the script felix-android.sh on init.rc to start on boot and keep running.
* Patch the UPnP base driver to remove errors with incorrect UPnP XML
* Use Philips SdK for HUE Lamps
* Remove Cybergarage
* Investigate "stack fault" on STB
* Port the Immotronics pem-enocean and ubikit
