#! /bin/bash

# Download docker images for the first time
docker pull mongo
docker pull dockerfile/java:oracle-java7

# Run mongo container
docker run -d --name mongoDB mongo

# Build appsgate images from docker java image
docker build -t yeastlab/appsgate:master .

# Run appsgate container export ports and link with mongoDB
docker run -d -p 8181:8181 -p 8087:8087 --link mongoDB:DB --name appsgate yeastlab/appsgate:master
