#!/bin/sh  +x

AS_ADMIN_USER=`sed '/^admin.user\=*/!d; s///;q' ee-config.properties`
AS_ADMIN_PORT=`sed '/^admin.port\=*/!d; s///;q' ee-config.properties`
AS_ADMIN_PASSWORD=`sed '/^admin.password\=*/!d; s///;q' ee-config.properties`
AS_ADMIN_DOMAIN=`sed '/^admin.domain\=*/!d; s///;q' ee-config.properties`
AS_ADMIN_NODEAGENT=`sed '/^nodeagent.name\=*/!d; s///;q' ee-config.properties`
AS_ADMIN_SERVER=`sed '/^appserver.instance.name\=*/!d; s///;q' ee-config.properties`
