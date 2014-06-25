#--------------------------------------------#
#               R E A D   M E                #
#--------------------------------------------#

##
#	Objet

Déploiement AppsGate sur le RaspBerry



##
#	Installation

- Sur Raspian Wheezy

    1. Copier mongo dans /srv
    
    2. Copier la distribution dans /srv

    3. Pour les bibliothèques RXTX*, créer des liens symboliques ne faisant
         pas mention du numéro de version.
         
     $ ln -s /srv/Appsgate-rpi-distribution/rxtx_natives_RPi/librxtxI2C-2.2pre1.so
     						/srv/Appsgate-rpi-distribution/rxtx_natives_RPi/librxtxI2C.so
     						
     $ ln -s /srv/Appsgate-rpi-distribution/rxtx_natives_RPi/librxtxParallel-2.2pre1.so
     				   /srv/Appsgate-rpi-distribution/rxtx_natives_RPi/librxtxParallel.so
     				   
     $ ln -s /srv/Appsgate-rpi-distribution/rxtx_natives_RPi/librxtxRaw-2.2pre1.so
     						/srv/Appsgate-rpi-distribution/rxtx_natives_RPi/librxtxRaw.so
     						
     $ ln -s /srv/Appsgate-rpi-distribution/rxtx_natives_RPi/librxtxRS485-2.2pre1.so
     					  /srv/Appsgate-rpi-distribution/rxtx_natives_RPi/librxtxRS485.so
     					  
     $ ln -s /srv/Appsgate-rpi-distribution/rxtx_natives_RPi/librxtxSerial-2.2pre1.so
     					 /srv/Appsgate-rpi-distribution/rxtx_natives_RPi/librxtxSerial.so
	  