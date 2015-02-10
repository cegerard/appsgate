#! /bin/bash

#Generate default configuration file for mongoDB
rm /data/appsgate/distributions/complete/conf/defaultMongoDBConfig.cfg
touch /data/appsgate/distributions/complete/conf/defaultMongoDBConfig.cfg
echo "#Default property file to configure the Mongo DB" >> /data/appsgate/distributions/complete/conf/defaultMongoDBConfig.cfg
echo "DBHost="$DB_PORT_27017_TCP_ADDR >> /data/appsgate/distributions/complete/conf/defaultMongoDBConfig.cfg
echo "DBPort="$DB_PORT_27017_TCP_PORT >> /data/appsgate/distributions/complete/conf/defaultMongoDBConfig.cfg
echo "DBName=defaultDB" >> /data/appsgate/distributions/complete/conf/defaultMongoDBConfig.cfg
echo "DBTimeout=3000" >> /data/appsgate/distributions/complete/conf/defaultMongoDBConfig.cfg
echo "dropCollectionsOnStart=false" >> /data/appsgate/distributions/complete/conf/defaultMongoDBConfig.cfg

#Launch appsgate http client
cd /data/appsgate/sources/appsgate-client/
http-server -p 8181 &
cd /data/appsgate/distributions/complete/
./apam &
