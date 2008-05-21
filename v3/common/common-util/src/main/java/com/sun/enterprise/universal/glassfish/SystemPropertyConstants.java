/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.enterprise.universal.glassfish;

public class SystemPropertyConstants {

    public static final String OPEN = "${";
    public static final String CLOSE = "}";
    public static final String UNIX_ASENV_FILENAME = "asenv.conf";
    public static final String WINDOWS_ASENV_FILENAME = "asenv.bat";
    public static final String WEB_SERVICES_LIB_PROPERTY =
            "com.sun.aas.webServicesLib";
    public static final String PERL_ROOT_PROPERTY = "com.sun.aas.perlRoot";
    public static final String IMQ_LIB_PROPERTY = "com.sun.aas.imqLib";
    public static final String IMQ_BIN_PROPERTY = "com.sun.aas.imqBin";
    public static final String CONFIG_ROOT_PROPERTY = "com.sun.aas.configRoot";
    public static final String INSTALL_ROOT_PROPERTY =
            "com.sun.aas.installRoot";
    public static final String JAVA_ROOT_PROPERTY = "com.sun.aas.javaRoot";
    public static final String ICU_LIB_PROPERTY = "com.sun.aas.icuLib";
    public static final String DEFAULT_LOCALE_PROPERTY =
            "com.sun.aas.defaultLocale";
    public static final String DOMAINS_ROOT_PROPERTY =
            "com.sun.aas.domainsRoot";
    public static final String INSTANCE_ROOT_PROPERTY =
            "com.sun.aas.instanceRoot";
    public static final String AGENT_CERT_NICKNAME =
            "com.sun.aas.agentCertNickname";
    public static final String AGENT_ROOT_PROPERTY =
            "com.sun.aas.agentRoot";
    public static final String AGENT_NAME_PROPERTY =
            "com.sun.aas.agentName";
    public static final String WEBCONSOLE_LIB_PROPERTY =
            "com.sun.aas.webconsoleLib";
    public static final String WEBCONSOLE_APP_PROPERTY =
            "com.sun.aas.webconsoleApp";
    public static final String JATO_ROOT_PROPERTY =
            "com.sun.aas.jatoRoot";
    public static final String ANT_ROOT_PROPERTY = "com.sun.aas.antRoot";
    public static final String ANT_LIB_PROPERTY = "com.sun.aas.antLib";
    public static final String JHELP_ROOT_PROPERTY = "com.sun.aas.jhelpRoot";
    public static final String SERVER_NAME = "com.sun.aas.instanceName";
    public static final String CLUSTER_NAME = "com.sun.aas.clusterName";
    public static final String HADB_ROOT_PROPERTY = "com.sun.aas.hadbRoot";
    public static final String NSS_ROOT_PROPERTY = "com.sun.aas.nssRoot";
    public static final String NSS_BIN_PROPERTY = "com.sun.aas.nssBin";
    public static final String NATIVE_LAUNCHER = "com.sun.aas.nativeLauncher";
    public static final String NATIVE_LAUNCHER_LIB_PREFIX = "com.sun.aas.nativeLauncherLibPrefix";
    public static final String KEYSTORE_PROPERTY = "javax.net.ssl.keyStore";
    public static final String KEYSTORE_PASSWORD_PROPERTY = "javax.net.ssl.keyStorePassword";
    public static final String JKS_KEYSTORE =
            System.getProperty("file.separator") + "config" +
            System.getProperty("file.separator") + "keystore.jks";
    public static final String TRUSTSTORE_PROPERTY = "javax.net.ssl.trustStore";
    public static final String TRUSTSTORE_PASSWORD_PROPERTY = "javax.net.ssl.trustStorePassword";
    public static final String JKS_TRUSTSTORE =
            System.getProperty("file.separator") + "config" +
            System.getProperty("file.separator") + "cacerts.jks";
    public static final String ADMIN_REALM = "admin-realm";
    public static final String NSS_DB_PROPERTY = "com.sun.appserv.nss.db";
    public static final String NSS_DB_PASSWORD_PROPERTY = "com.sun.appserv.nss.db.password";
    public static final String CLIENT_TRUSTSTORE_PROPERTY =
            TRUSTSTORE_PROPERTY;
    public static final String CLIENT_TRUSTSTORE_PASSWORD_PROPERTY =
            TRUSTSTORE_PASSWORD_PROPERTY;
    public static final String PID_FILE = ".__com_sun_appserv_pid";
    public static final String REF_TS_FILE = "admsn";
    public static final String KILLSERV_SCRIPT = "killserv";
    public static final String KILL_SERV_UNIX = "killserv";
    public static final String KILL_SERV_WIN = "killserv.bat";
    public static final String DEFAULT_SERVER_INSTANCE_NAME = "server";
    public static final String JDMK_HOME_PROPERTY = "com.sun.aas.jdmkHome";
    public static final String DERBY_ROOT_PROPERTY = "com.sun.aas.derbyRoot";
    public static final String MFWK_HOME_PROPERTY = "com.sun.aas.mfwkHome";
    public static final String DOMAIN_NAME = "domain.name";
    public static final String HOST_NAME_PROPERTY = "com.sun.aas.hostName";
    public static final String CONFIG_NAME_PROPERTY = "com.sun.aas.configName";
    public static final String DOCROOT_PROPERTY = "docroot";
    public static final String ACCESSLOG_PROPERTY = "accesslog";
    public static final String DEFAULT_SERVER_SOCKET_ADDRESS = "0.0.0.0";
    public static final String CLUSTER_AWARE_FEATURE_FACTORY_CLASS = "com.sun.enterprise.ee.server.pluggable.EEPluggableFeatureImpl";
    public static final String TEMPLATE_CONFIG_NAME = "default-config";
    public static final String DEFAULT_ADMIN_USER = "anonymous";
    public static final String DEFAULT_ADMIN_PASSWORD = "";
}
