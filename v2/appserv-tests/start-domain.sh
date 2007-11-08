#!/bin/sh

# Extract domain name from config.properties and start the domain
for x in `cat $APS_HOME/config.properties`
do
    varval=`echo $x |cut -d'=' -f1`

    if [ $varval = "admin.user" ];
    then
        AS_ADMIN_USER=`echo $x |cut -d'=' -f2`
    fi
   
    if [ $varval = "admin.password" ];
    then
        AS_ADMIN_PASSWORD=`echo $x |cut -d'=' -f2`
    fi

    if [ $varval = "admin.domain" ];
    then
        AS_ADMIN_DOMAIN=`echo $x |cut -d'=' -f2`
    fi
done

${S1AS_HOME}/bin/asadmin start-domain --user ${AS_ADMIN_USER} --password ${AS_ADMIN_PASSWORD} ${AS_ADMIN_DOMAIN}
