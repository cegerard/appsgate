# Faire fonctionner la Set Top Box
(mise à jour 20/05/2015)

# Configuration de la Set Top Box

## S'assurer que tous les fichiers sont bien présents dans le répertoire /data/Pace, avec suffisamment de droits:
	drwxrwxr-x root     root              2015-03-16 16:56 ARD
	-rwxrwxrwx root     root          170 2015-05-19 14:14 ARD.sh
	-rwxrwxrwx root     root        17748 2015-05-19 16:57 ARDintego
	-rw-rw-rw- root     root           24 2015-05-19 14:14 ARDintego.conf
	-rwxrwxrwx root     root       248352 2015-05-19 14:14 ARDisoview
	-rw-rw-rw- root     root       818405 2015-02-03 14:55 Appsgate.apk
	drwxrwxrwx root     log               2013-03-25 13:09 UI
	-rw-rw-rw- root     root         4273 2015-05-19 14:14 __readme.txt
	-rw-rw-rw- system   system        724 2014-05-19 10:33 appsgate.ktbl
	-rw-rw---- system   system        984 2015-05-20 15:38 appsgate.sh
	-rw-rw-rw- root     root          480 2015-05-25 17:32 backup_ard.sh
	-rw-r--r-- system   system    4831264 2014-05-19 22:59 boot-NAND_20140519.img
	-rwxrwxrwx system   system        119 2015-04-07 15:20 channels.conf
	-rwxrwxrwx root     root         9090 2015-05-20 16:03 conf_appsgate.json
	drwxr-xr-x system   100               2013-03-26 18:42 curl
	-rw------- root     root      2314149 2013-03-26 18:42 curl.tgz
	-rw-rw-rw- system   system        441 2014-05-19 10:33 demo_setup.sh
	-rw-rw-rw- root     root           38 2015-05-19 16:55 err.err
	-rwxrwxrwx root     root        23463 2015-05-20 16:09 events_appsgate.json
	-rw-rw-rw- system   system      19923 2014-05-19 10:33 init.rc
	-rwxrwxrwx root     root      1686457 2015-05-19 14:14 isoview_1.dat
	-rwxrwxrwx root     root      1686457 2015-05-19 14:14 isoview_2.dat
	lrwxrwxrwx root     root              2015-04-02 17:54 media -> /storage/usb_stg10/
	-rwxrwxrwx root     root          180 2015-05-20 15:16 network.sh
	-rw-rw-rw- system   system        434 2013-03-25 13:05 stbrestd.ini
	-rw-rw-rw- system   system        381 2014-05-19 10:33 stbrestd.ini_example


## Vérifier le fichier /data/Pace/channels.conf
	F#/data/Pace/media/hobbit2.mp4####
	F#/data/Pace/media/avengers_short.mp4####
	F#/data/Pace/media/startrek_short.mp4####

## Verifier que les media du dessus sont bien accessibles en lecture, que la partition est bien montée, etc.

## Vérifier le fichier /data/Pace/appsgate.sh qui devrait ressembler à ça :

	#!/system/bin/sh
	export HOME=/data

	# Fix mac address
	ip link set eth0 down
	ip link set eth0 addr 78:96:20:DA:53:16
	ip link set eth0 up

	(sleep 30 ;/data/Pace/network.sh)
	rdate -s time-a.nist.gov 

	mount -t auto -o remount,rw /system

	echo "***** registring appsgate scancode/keycode *****"
	ir-keytable -w /data/Pace/appsgate.ktbl

	stfbset -M 0x00000000
	setprop pace.appsgate.ui.url file:///data/Pace/UI/index_stub2.html
	stbrestd -f /data/Pace/stbrestd.ini | tee /data/outstbrestd &

	#ARD
	(sleep 30; /data/Pace/ARD.sh)&


## Pour info, le fichier /data/Pace/network.sh (qui ne semble pas exécuté, pb de syntaxe dans appsgate.sh ?)
	#!/system/bin/sh

	MYIP=`getprop dhcp.eth0.ipaddress`
	echo $MYIP
	ifconfig eth0 $MYIP
	echo 127.0.0.1 localhost > /etc/hosts                           
	echo $MYIP mystb >> /etc/hosts


## Contenu du fichier /data/Pace/ARD.sh
	#!/system/bin/sh
	cd /data/Pace
	ip addr del 62.0.0.0/8 dev eth0
	daemonize /data/Pace/ARDisoview
	source /data/Pace/ARDintego.conf
	daemonize /data/Pace/ARDintego $INTEGO_IP

## Contenu du fichier /etc/hosts
	127.0.0.1 localhost
	192.168.1.210 mystb

## Contenu du fichier /etc/resolv.conf
	nameserver 8.8.8.8
	nameserver 192.168.1.1



# Configuration du routeur et de l'infrastructure réseau

## Configurer le routeur pour qu'il affecte  l'adresse IP 192.168.1.210 à la Set-Top-Box
Dans le paramétrage du LAN ou du DHCP, forcer l'adresse IP en fonction de l'adresse MAC
cf appsgate.sh : 78:96:20:DA:53:16

## Brancher la caméra et vérifier qu'elle possède bien l'adresse fixe 192.168.1.208
(MAC: AC:CC:8E:03:A3:3E)

## Brancher le "routeur" SmartIntego/Simon Voss et vérifier qu'elle possède bien l'adresse Fixe 192.168.1.209
(MAC: 00:40:9D:77:9E:E2)

## Brancher la board ARD avec la tête de lecture NFC et vérifier qu'elle possède bien l'adresse Fixe 192.168.1.157



# Démarrage de la démo (l'ordre est important)
1. Brancher et démarrer les équipement ARD
2. Brancher et démarrer la Box, attendre environ 2 minutes que tout soit correctement lancé
3. Lancer le Home Launcher Pace (il ne doit pas y avoir de gros ralentissement)
4. Lancer l'application ARD et vérifier la caméra, vérifier les évènements "au fil de l'eau" (badges reconnus/autorisés par l'application)
5. Lancer l'application Live TV de Pace(la première, la deuxième est un raccourci vers rien)
6. Lancer la plateforme appsgate et attendre qu'elle détecte correctement TV et ARD (environ 2 minutes)


# Pistes pour le dépannage
* Vérifier que la box a bien pour adresse 192.168.1.210
* Vérifier l'heure (elle doit être exacte +/- le décalage horaire)
* Vérifier que la STB accède bien à Internet
* Vérifier avec un top les processus, stbrestd consomme environ 50%, ARDIntego et ARDisoview environ 0%
* Vérifier que la TV se controle par télécommande Spok & par End-User programming
* Vérifier ARD dans la télécommande Spok & End-User Programming






