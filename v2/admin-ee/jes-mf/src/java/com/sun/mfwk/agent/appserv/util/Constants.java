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

/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */
package com.sun.mfwk.agent.appserv.util;

/**
 * Constants used in JES MF plugin. 
 */
public final class Constants {

    /** Connection credential property name */
    public static final String CONNECTION_CREDENTIALS = "ConnectionCredentials";

    /** Key used for admin user */
    public static final String USER_KEY      = "user";

    /** Key used for admin password */
    public static final String PASSWORD_KEY  = "password";

    /** Key used for name*/
    public static final String NAME_KEY = "name";

    /** Key used for server*/
    public static final String J2EE_SERVER_KEY = "J2EEServer";

    /** Key used for application server certificate */
    public static final String CERT_KEY      = "certificate";

    /** Key used for service url */
    public static final String URI_KEY       = "uri";

    /** Key used for application server name */
    public static final String SERVER_KEY    = "com.sun.aas.instanceName";

    /** Key used for application server host name */
    public static final String HOST_KEY      = "host";

    /** Key used for JMX connector port */
    public static final String PORT_KEY      = "port";

    /** Key used for domain name */
    public static final String DOMAIN_NAME_KEY = "domain";

    /** Key used for server name */
    public static final String SERVER_NAME_KEY = "server";

    /** Key used determine if detected server instance is DAS */
    public static final String IS_DAS_KEY = "isDAS";

    /** Product name constant */
    public static final String PRODUCT_NAME  = "ApplicationServer";

    /** Discovery service object name */
    public static final String DIS_OBJ_NAME  = 
                            "com.sun.mfwk:type=mfDiscoveryService";

    /** Default template file location */
    public static final String DEF_FILE_LOC  = "/opt/SUNWmfwk/lib/";

    /** Default module name */
    public static final String DEF_MODULE_NAME  = "com.sun.cmm.as";

    /** File name containing mapping between AS and CMM mbeans */
    public static final String DEF_MAPPING = "mbean-mapping-descriptors.xml";

    /** File name containing mapping between CMM relations */
    public static final String DEF_RELATION = "cmm-relation-descriptors.xml";
    
    /** File name containing mapping between AS and CMM mbeans for DAS*/
    public static final String DAS_MAPPING="das-mbean-mapping-descriptors.xml";

    /** File name containing mapping between CMM relations for DAS*/
    public static final String DAS_RELATION="das-cmm-relation-descriptors.xml";
    
    /** Pattern used to query all application server monitoring mbeans*/
    public static final String MONITOR_PATTERN = 
                            "com.sun.appserv:*,category=monitor";

    public static final String CLUSTER_PATTERN = 
                            "com.sun.appserv:*,type=cluster,category=config";

    public static final String INSTANCE_REF_PATTERN = 
                            "com.sun.appserv:*,type=server-ref";
    
    /** Bundle file used by JES MF Plugin */
    public static final String BUNDLE_FILE="com.sun.mfwk.agent.appserv.Bundle"; 
    
    /** Pattern used for J2EE runtime mbean */
    public static final String J2EE_RUNTIME_PATTERN = 
                    "com.sun.appserv:*,j2eeType=J2EEServer,category=runtime"; 

    /** Pattern used for JVM runtime mbean */
    public static final String JVM_RUNTIME_PATTERN =
                    "com.sun.appserv:*,j2eeType=JVM,category=runtime";

    /** Domain Administration Server name */
    public static final String ADMIN_SERVER_NAME = "server";

    /** Key used to describe server name in descriptor xml */
    public static final String SERVER_NAME_PROP = "server.name";

    /** Key used to describe domain name in descriptor xml */
    public static final String DOMAIN_NAME_PROP = "domain.name";
    
    /** Pattern used to query all connection manager monitoring mbeans*/
    public static final String CONNECTION_MANAGER_PATTERN = 
        "com.sun.appserv:*,category=monitor,type=connection-manager";
    
    /** Pattern used to query all connector connection pool monitoring mbeans*/
    public static final String CONNECTOR_CONNECTION_POOL_PATTERN = 
        "com.sun.appserv:*,category=monitor,type=connector-connection-pool";
    
    /** Pattern used to query all jdbc connection pool monitoring mbeans*/
    public static final String JDBC_CONNECTION_POOL_PATTERN = 
        "com.sun.appserv:*,category=monitor,type=jdbc-connection-pool";
    
    /** Pattern used to query all connection queue monitoring mbeans*/
    public static final String CONNECTION_QUEUE_PATTERN = 
        "com.sun.appserv:*,category=monitor,type=connection-queue";
}
