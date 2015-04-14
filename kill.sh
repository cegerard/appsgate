#! /bin/bash
APAM_ID=`ps aux | grep java | grep felix | awk '{print $2}'`
kill -9 $APAM_ID
