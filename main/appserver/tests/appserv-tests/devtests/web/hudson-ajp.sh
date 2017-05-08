#! /bin/bash
#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
#
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 2 only ("GPL") or the Common Development
# and Distribution License("CDDL") (collectively, the "License").  You
# may not use this file except in compliance with the License.  You can
# obtain a copy of the License at
# https://oss.oracle.com/licenses/CDDL+GPL-1.1
# or LICENSE.txt.  See the License for the specific
# language governing permissions and limitations under the License.
#
# When distributing the software, include this License Header Notice in each
# file and include the License file at LICENSE.txt.
#
# GPL Classpath Exception:
# Oracle designates this particular file as subject to the "Classpath"
# exception as provided by Oracle in the GPL Version 2 section of the License
# file that accompanied this code.
#
# Modifications:
# If applicable, add the following below the License Header, with the fields
# enclosed by brackets [] replaced by your own identifying information:
# "Portions Copyright [year] [name of copyright owner]"
#
# Contributor(s):
# If you wish your version of this file to be governed by only the CDDL or
# only the GPL Version 2, indicate your decision by adding "[Contributor]
# elects to include this software in this distribution under the [CDDL or GPL
# Version 2] license."  If you don't indicate a single choice of license, a
# recipient has the option to distribute your version of this file under
# either the CDDL, the GPL Version 2 or to extend the choice of license to
# its licensees as provided above.  However, if you add GPL Version 2 code
# and therefore, elected the GPL Version 2 license, then the option applies
# only if the new code is made subject to such option by the copyright
# holder.
#

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
