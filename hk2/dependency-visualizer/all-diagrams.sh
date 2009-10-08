#!/bin/bash
#Generates visualizations for all the major containers in glassfish v3

# First all major containers
./subset.sh admin
./subset.sh admingui
./subset.sh appclient
#./subset.sh common
./subset.sh connector
./subset.sh core
./subset.sh deployment
./subset.sh ejb
#./subset.sh embedded
./subset.sh flashlight
./subset.sh install
#./subset.sh jbi
./subset.sh jdbc
./subset.sh jms
./subset.sh orb
./subset.sh persistence
#./subset.sh packager
./subset.sh security
./subset.sh transaction
#./subset.sh verifier
./subset.sh web
./subset.sh webservices
#./subset.sh extras

