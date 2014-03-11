ANDROIDPATH=/Users/thibaud/Development/android-sdk-macosx
ANDROIDVERSION=17.0.0

ANDROIDFELIXPATH=/data/felix

ANDROIDADBDEVICE=084972d8


export PATH=$PATH:$ANDROIDPATH/build-tools/$ANDROIDVERSION:$ANDROIDPATH/tools:$ANDROIDPATH/platform-tools

for jarfile in *.jar; do
	dx --dex --output=classes.dex $jarfile
	aapt add $jarfile classes.dex
#	mv $jarfile ../bundle/$jarfile
done
