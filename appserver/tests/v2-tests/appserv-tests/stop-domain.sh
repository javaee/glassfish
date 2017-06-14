#!/bin/sh

# Extract domain name from config.properties and start the domain

#DOMAIN_NAME=`cat config.properties | grep "admin.domain" | cut -f2 -d"="`
#${S1AS_HOME}/bin/asadmin stop-domain --domain ${DOMAIN_NAME}
${S1AS_HOME}/bin/asadmin stop-domain domain1
