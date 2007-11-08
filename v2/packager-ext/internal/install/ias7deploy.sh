#!/bin/sh

#
# Copyright 2006 Sun Microsystems, Inc. All rights reserved.
# Use is subject to license terms.
#


# please set the following variables
IAS_ROOT=${IAS_ROOT}
JAVA_HOME=${JAVA_HOME}
if [ `env | grep -c JAVA_HOME` -eq 0 ]; then
    JAVA_HOME=/share/builds/components/jdk/1.4.0beta3/SunOS
fi

# these variables are used internally
RI_HOME=${IAS_ROOT}/ri
SE_JARS=${IAS_ROOT}/iws/bin/https/jar

# do no change

${JAVA_HOME}/bin/java -Djavax.net.ssl.trustStore=${RI_HOME}/lib/security/cacerts.jks -Djava.security.auth.policy=${RI_HOME}/lib/security/jaas.policy -Dorg.xml.sax.parser=org.xml.sax.helpers.XMLReaderAdapter -Dorg.xml.sax.driver=org.apache.crimson.parser.XMLReaderImpl -Dcom.sun.enterprise.home=${RI_HOME} -Djms.home=${RI_HOME} -Dcom.sun.jms.service.jdbc.dbpath=${RI_HOME}/repository/nislam/db/ -Djms.properties=${RI_HOME}/config/jms_service.properties -Djava.security.policy==${RI_HOME}/lib/security/server.policy -Djava.security.auth.login.config=${RI_HOME}/lib/security/clientlogin.config -classpath ${SE_JARS}/cloudscape.jar:${SE_JARS}/cloudutil.jar:${SE_JARS}/RmiJdbc.jar:${SE_JARS}/cloudclient.jar:${SE_JARS}/jhall.jar:/share/builds/components/jdk/1.4.0beta3/SunOS/lib/tools.jar:${SE_JARS}/j2ee.jar:${SE_JARS}/crimson.jar:${SE_JARS}/jaas.jar:${SE_JARS}/jakarta-regexp-1.2.jar:${SE_JARS}/javax.jar:${SE_JARS}/jaxp.jar:${SE_JARS}/jcert.jar:${SE_JARS}/jmxri.jar:${SE_JARS}/jsse.jar:${SE_JARS}/xalan.jar:${SE_HOME}/bin/https/props:${SE_JARS}/persistence-rt.jar:${SE_JARS}/persistence-ui.jar:${SE_JARS}/dbschema.jar:${SE_JARS}/xerces.jar:${SE_JARS}/schema2beans.jar:${SE_JARS}/ias_ffj.jar com.sun.enterprise.deployment.ui.DeployUnitTestTool "$@"
