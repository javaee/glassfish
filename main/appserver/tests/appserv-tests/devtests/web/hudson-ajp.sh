#! /bin/bash

. hudson-base.sh

update() {
	sed -i .bak -e "s|$1=.*|$1=$2|" ${APS_HOME}/config.properties
}

configure() {
   rm -rf $S1AS_HOME/domains/domain1

   cd $APS_HOME

	svn revert ${APS_HOME}/config.properties
   echo "AS_ADMIN_PASSWORD=" > temppwd
   $S1AS_HOME/bin/asadmin --user admin --passwordfile $APS_HOME/config/adminpassword.txt create-domain --adminport ${WEBTIER_ADMIN_PORT} --domainproperties jms.port=${WEBTIER_JMS_PORT}:domain.jmxPort=${JMX_PORT}:orb.listener.port=${WEBTIER_ORB_PORT}:http.ssl.port=${WEBTIER_SSL_PORT}:orb.ssl.port=${WEBTIER_ORB_SSL_PORT}:orb.mutualauth.port=${WEBTIER_ORB_SSL_MUTUALAUTH_PORT} --instanceport ${WEBTIER_PORT} domain1

	update admin.port ${WEBTIER_ADMIN_PORT}
	update http.port ${APACHE_PORT}
	update https.port ${WEBTIER_SSL_PORT}
	update http.alternate.port ${WEBTIER_ALTERNATE_PORT}
	update orb.port ${WEBTIER_ORB_PORT}
	echo "ajp.port=${WEBTIER_PORT}" >> ${APS_HOME}/config.properties

	cd ${BASE_APACHE_HOME}/conf

	../bin/apachectl -k stop ;

	sed -i .bak`date "+%m%d%H%M"` \
		-e '/^#*ProxyPass.*/d' \
		-e '/^#*ProxyPreserveHost .*/d' \
		-e "s/^Listen.*/Listen ${APACHE_PORT}/" \
		-e "s|^ServerRoot.*|ServerRoot \"${BASE_APACHE_HOME}\"|" \
		-e "s|^DocumentRoot.*|DocumentRoot \"${BASE_APACHE_HOME}/htdocs\"|" \
		-e "s|^<Directory.*|<Directory \"${BASE_APACHE_HOME}/htdocs\">|" \
		httpd.conf

	cat >> httpd.conf <<EOF
ProxyPass / ajp://localhost:${WEBTIER_PORT}/ retry=0 ttl=30
ProxyPassReverse / ajp://localhost:${WEBTIER_PORT}/
ProxyPreserveHost on
EOF

	$S1AS_HOME/bin/asadmin stop-domain

	 sleep 1 && ../bin/apachectl -k start && echo "apache up" || echo "failed to start apache"
	cd -

	$S1AS_HOME/bin/asadmin start-domain

	$S1AS_HOME/bin/asadmin --port ${WEBTIER_ADMIN_PORT} set configs.config.server-config.network-config.network-listeners.network-listener.http-listener-1.jk-enabled=true

	$S1AS_HOME/bin/asadmin stop-domain
}

configure
run
