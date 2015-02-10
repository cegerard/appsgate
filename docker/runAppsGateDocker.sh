#! /bin/bash

# Run mongo container
docker run -d --name mongoDB mongo
# Run appsgate container export ports and link with mongoDB
docker run -d -p 8181:8181 -p 8087:8087 --link mongoDB:DB --name appsgate yeastlab/appsgate:master

