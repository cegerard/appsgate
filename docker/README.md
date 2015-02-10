#AppsGate Docker v0.1#

##Usage##

Now appsGate is running with docker with this
integration the only thing you need on the targeted
plateform to run AppsGate is Docker.

For the following instruction you need to have root access.
install.sh and runAppsGateDocker.sh must be run as root and the Dockerfile must be
in the same directory.

- So first install Docker on the targeted OS: https://docs.docker.com/installation/
- Run the install.sh script from the targeted plateform this step can take a while
cause it download all necessary images, packages and build the mongoDB docker container
and the Appsgate docker container. Both containers are started at the end.

You can now use AppsGate from your docker container:
From your browser http://localhost:8181/
On remote client browser http://<SERVER_IP_ADDR>:8181/ 

To restart AppsGate you don't need to rebuild all images, just use the
runAppsgateDocker.sh script instead of install.sh.

##Issues##
As Docker is a virtualization system, EnOcean usb dongle auto-mount is not supported
yet.
