for jarfile in *.jar; do
	dx --dex --output=classes.dex $jarfile
	aapt add $jarfile classes.dex
#	mv $jarfile ../bundle/$jarfile
done
