#! /bin/bash

## ============================================================
##
## Role       : "Chien de garde" logiciel 
##
## Auteur     : PME
##
## Historique :
##   05/04/12 : Creation
##   08/06/12 : Modification JPU pour initialiser le fichier config
##   24/07/12 : Modification pour radvd	
##   02/08/12 :	Changement de repertoire
## ============================================================

# Repertoires
# -----------
# WTC_TOOLS_DIR could be set in an env variable
if [ -z $WTC_TOOLS_DIR ] 
then
	WTC_TOOLS_DIR=/usr/share/wtc-tools	
fi

base=$WTC_TOOLS_DIR/wtc_tunslip6
#base="/home/vendors/wdgTunslip6"
# Fichiers
# --------

journal="$base/journal.log"
config="$base/dongle.conf"
TS6BRADDRFILE="/tmp/ts6braddrfile"

# Dates
# -----

laDate="date +%Y/%m/%d"
lHeure="date +%H:%M:%S"

# Initialisation des variables par defaut
# ---------------------------------------

WDGTUNSLIP="wdgTunslip6.sh"
default_IP="de30::1"
use_param_IP=0
use_param_prefix=0
default_prefixe_lenght=96
default_tun=tun0
default_PANID=5500


# Description de l'utilisation
# ----------------------------

function usage() {
	echo "Utilisation  : $1"
	echo "               Watchdog logiciel"
	echo ""
	echo "  -h         : Affiche le mode d'emploi"
	echo "  -@ IPv6    : Adresse ipv6 de l'interface, example: ./wdgTunslip6.sh -@ de30::1"
	echo "  -p integer : Nombre de bit du préfix de l'IPv6, example: ./wdgTunslip6.sh -@ de30::1 -p 96"
}

# ecrire
# ------

function ecrire() {
	echo $1 >> $2
	fic=`cat $2 | tail -n100`
	echo "$fic" > $2
}

#Vérification si l'adresse reçu est en IPv6 pas fait si il y a plusieur fois de ::
#----------------------------------------------------------------------------------

check_ipv6="^(([0-9A-Fa-f]{1,4}:){7}[0-9A-Fa-f]{1,4}|([0-9A-Fa-f]{1,4}:){1,1}(:[0-9A-Fa-f]{1,4}){1,6}|([0-9A-Fa-f]{1,4}:){1,2}(:[0-9A-Fa-f]{1,4}){1,5}|([0-9A-Fa-f]{1,4}:){1,3}(:[0-9A-Fa-f]{1,4}){1,4}|([0-9A-Fa-f]{1,4}:){1,4}(:[0-9A-Fa-f]{1,4}){1,3}|([0-9A-Fa-f]{1,4}:){1,5}(:[0-9A-Fa-f]{1,4}){1,2}|([0-9A-Fa-f]{1,4}:){1,6}(:[0-9A-Fa-f]{1,4}){1,1})$"

#Récupère le préfixe de l'adresse ip Global et le nombre de bit static
#---------------------------------------------------------------------------
function get_global_ip_and_prefix_len() {

	IPS=`ifconfig | awk 'BEGIN {i=0} $1~/~eth[0-9]*$/{interface=$1}/inet6.*[sS]cope:Global/ {if (i==0) {print $3; i++;}}'`
	Bit_prefix=`basename $IPS`
	IP_Global=`dirname $IPS`
}
#Récupère les informations nécessaire pour initialisé le fichier de config
#-------------------------------------------------------------------------

function init_config() {
	
	if test ! -s $config #check if the file doesn't exist and hasn't a size greater than zero
	then
		echo "-------Init config------------"		
		runTun=`ifconfig |grep "tun"`
		if test  -z "$runTun" #check if the lenght of $runTun is zero
		then
			
			devs=`ls /dev/ttyUSB*`
			for leDev in $devs
			do
			ret=`$base/dongle $leDev`
			validipv6=`echo "$ret"|grep -E "$check_ipv6"`
			echo "=========Observation ipv6" "$validipv6"
			if test -n "$validipv6"
			then
				echo "IPv6 OK"
				if [[ (( $use_param_IP == 0 )) ]]
				then
					get_global_ip_and_prefix_len
					if [[ ( $IP_Global == "" ) ]] || [[ ($Bit_prefix == "" ) ]]
					then
						echo "ERROR,Don't take the suffix: ff:ff00:1\nSet de30::ff:ff00:1/96\n"
						interfaceIP="$default_IP/$default_prefixe_lenght"
					else 
						#voir si je modifie avec le code de PEG avec ipv6SetPart.c
						modifIP=`/home/vendors/modify_IP $IP_Global $Bit_prefix`
							
						if [[ (( $use_param_prefix == 0 )) ]]
						then
							default_prefixe_lenght=96
							echo "Initialise le bit$default_prefixe_lenght"
						fi
						interfaceIP="$modifIP/$default_prefixe_lenght"
					fi
				else
					interfaceIP="$default_IP/$default_prefixe_lenght"
					echo "interface: $interfaceIP"
				fi
				tun=$default_tun
				#PANID=53265
				PANID=$default_PANID
				echo  -e "$validipv6\t$interfaceIP\t$tun\t$PANID" > $config 
			else
				echo "============Mauvais ipv6"
			fi 
  			done 	
		fi
	fi

}

#Decodage des parametres
# -----------------------
paramIP=0
paramPref=0
while (( $# > 0 ))
do
	case $1 in
		(-h) 	usage $WDGTUNSLIP; exit 0 ;;
		(-@)	shift 1
			paramIP=$1
			;;
		(-p)	shift 1
			paramPref=$1
			;;
		(*)	ecrire "`$laDate` `$lHeure` [ER] Parametre '$1' inconnu" $journal
			echo ""
			echo "$WDGTUNSLIP : Parametre '$1' inconnu"
			echo ""
			usage $WDGTUNSLIP
			echo ""
			exit -1 ;;
  	esac
	
	shift 1
done




#Validation des paramètres
# -----------------------
validipv6=`echo "$paramIP"|grep -E "$check_ipv6"`
echo "=========Observation paramIP " "$paramIP"
if test -n "$validipv6" #test if the lenght of result $validipv6 is nonzero
then
	default_IP=$validipv6
	use_param_IP=1
	echo "IPv6: $default_IP"
else 
	echo "NOT IP valid"
fi

echo "=========Observation param2 " "$paramPref"
if [ $paramPref -ne 0 -o $paramPref -gt 0 ]
then 
	default_prefixe_lenght=$paramPref
	use_param_prefix=1
else 
	echo "NOT good prefix lenght "
fi

echo "etat param_IP: $use_param_IP"
echo "etat param_prefix: $use_param_prefix"
#===================
# Execution du role
#===================

cd $base
while (( 1 ))
do
	
	sleep 5
	init_config
	if [[ (( -e $config )) ]]
	then
		
		localAddrs=`cat $config | grep tun | awk '{print $1;}'`
		for lAddr in $localAddrs
		do
			tunCfg=`cat $config | grep $lAddr | awk '{print $3;}'`
			isTunExist=`ifconfig | grep -c $tunCfg`
			if [[ (( $isTunExist == 0 )) ]]
			then
				ecrire "`$laDate` `$lHeure` [KO] Le tunnel $tunCfg du dongle '$lAddr' n'existe pas" $journal	
				devs=`ls /dev/ttyUSB*`
				for leDev in $devs
				do
					# Beaglebone
					# L'UART interne doit etre configuree au moins une fois
					#if [[ (( $leDev == "/dev/ttyO1" )) ]]
					#then
 					#	echo 0 > /sys/kernel/debug/omap_mux/uart1_txd 
					#	echo 20 > /sys/kernel/debug/omap_mux/uart1_rxd 
					#	sleep 2
					#fi
					nbProc=`ps -aef | grep "tunslip6" | grep -c "$leDev"`				
					if [[ (( $nbProc == 0 )) ]]
					then
						ret=`$base/dongle $leDev`
						if [[ (( $ret == $lAddr )) ]]
						then
							ipAddr=`cat $config | grep $lAddr | awk '{print $2;}'`
							panID=`cat $config | grep $lAddr | awk '{print $4;}'`
							# ./tunslip6 $ipAddr -s $leDev -t $tunCfg -P $panID &
							./tunslip6 $ipAddr -s $leDev -t $tunCfg -P $panID -@ $TS6BRADDRFILE &
							ecrire "`$laDate` `$lHeure` [OK] Le tunnel $tunCfg du dongle '$lAddr' a ete relance" $journal
						fi
					fi
				done
#			else
#				ecrire "`$laDate` `$lHeure` [OK] Le tunnel $tunCfg du dongle '$lAddr' existe" $journal
			fi
		done
	else
		ecrire "`$laDate` `$lHeure` [ER] Le fichier de configuration 'dongle.conf' n'existe pas" $journal
	fi		

	ecrire "" $journal
	
done

exit 0

