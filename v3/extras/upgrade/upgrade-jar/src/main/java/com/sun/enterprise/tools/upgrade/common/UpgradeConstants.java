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
 * UpgradeConstants.java
 *
 * Created on March 10, 2004, 3:24 PM
 */

package com.sun.enterprise.tools.upgrade.common;

/**
 *
 * @author  prakash
 */

public class UpgradeConstants {
    
    // class should not be instantiable
    private UpgradeConstants() {}

    public static final String VERSION_91 = "9.1";	
    public static final String VERSION_91_01 = "9.1_01";	//as91_ur1
    public static final String VERSION_91_02 = "9.1_02";	//as91_ur2
    public static final String VERSION_91_1 = "9.1.1";		// sailfin  (not to be upgraded to v3)
    public static final String VERSION_3_0 = "3.0";		// GF_v3
    public static final String VERSION_2_1 = "2.1";         // as91_ur2 ( Sun GlassFish Enterprise Server v2.1)
    
    public static final String DEVELOPER_PROFILE = "developer";
    public static final String CLUSTER_PROFILE = "cluster";
    public static final String ENTERPRISE_PROFILE = "enterprise";
    public static final String ALL_PROFILE = "All";

    //asupgrade related constants     
    public static final String ASUPGRADE = "asupgrade";
    public static final String ASUPGRADE_BAT = "asupgrade.bat";   
    public static final String ASUPGRADE_LOG = "upgrade.log";
    public static final String BACKUP_DIR = "backup";
	
    //application server related constants    
    public static final String AS_DOMAIN_ROOT = "com.sun.aas.domainRoot";
    public static final String AS_BIN_DIRECTORY = "bin";
    public static final String AS_CONFIG_DIRECTORY = "config";
    public static final String AS_ADMIN_ENV_CONF_FILE = "asadminenv.conf";
    public static final String AS_PROPERTY_ADMIN_PROFILE = "AS_ADMIN_PROFILE=";
    public static final String ASADMIN_COMMAND = "asadmin";
    public static final String ASADMIN_BAT = "asadmin.bat";
    public static final String DOMAIN_XML_FILE = "domain.xml";
    public static final String DELIMITER = ";"; //-seperator for version edititon
    public static final String LOGGING_PROPERTIES = "logging.properties";
    public static final String SERVER_LOG_PROPERTY="com.sun.enterprise.server.logging.GFFileHandler.file";
    public static final String SERVER_LOG_DEFAULT="logs/server.log";
		
    //operating system related constants    
    public static final String OS_NAME_IDENTIFIER = "os.name";
    public static final String OS_NAME_WINDOWS = "Windows";

}
