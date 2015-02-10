# Dockerfile for building java oracle 7 AppsGate images

FROM dockerfile/java:oracle-java7
MAINTAINER Cedric Gerard <cedric.gerard@yeastlab.fr>

# Update apt packages, install and setup npm
RUN apt-get update && apt-get install --yes nodejs npm
RUN ln -s /usr/bin/nodejs /usr/bin/node

# Install node http server
RUN npm install http-server -g

# Install maven 3.0.5
RUN wget http://mirrors.ircam.fr/pub/apache/maven/maven-3/3.0.5/binaries/apache-maven-3.0.5-bin.tar.gz
RUN mkdir /usr/local/apache-maven
RUN tar -xvzf apache-maven-3.0.5-bin.tar.gz
RUN mv apache-maven-3.0.5 /usr/local/apache-maven
#RUN echo "export M2_HOME=/usr/local/apache-maven/apache-maven-3.0.5" >> .bashrc
#RUN echo "export M2=$M2_HOME/bin"  >> .bashrc
#RUN echo 'export MAVEN_OPTS="-Xms256m -Xmx512m"'  >> .bashrc
ENV M2_HOME /usr/local/apache-maven/apache-maven-3.0.5
ENV M2 $M2_HOME/bin
ENV MAVEN_OPTS -Xms256m -Xmx512m
ENV PATH $M2:$PATH

# Clone appsgate repo and compile the master branch
RUN git clone https://github.com/appsgate2015/appsgate.git
WORKDIR appsgate
RUN mvn install -Pcomplete -DskipTests
RUN chmod +x /distribution/complete/apam

# Copy the run script into the container 
Add ./scripts/run.sh /data/appsgate/run.sh

# Define the entrypoint at run time
ENTRYPOINT ["~/appsgate/run.sh"]
