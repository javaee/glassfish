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
 * @(#) Constants.java
 *
 * Copyright 2000-2001 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of iPlanet/Sun Microsystems, Inc. ("Confidential Information").
 * You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license
 * agreement you entered into with iPlanet/Sun Microsystems.
 */
package com.sun.enterprise.server;

import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.admin.util.IAdminConstants;

/**
 * Static constants for this package.
 */
public final class Constants implements IAdminConstants
{
    private static final String prefix = "com.sun.aas.";
    
    public static final String Package = "com.sun.enterprise.server";
    
    /** Variable that contains the path to the iAS config installation. */
    public static final String IAS_ROOT = SystemPropertyConstants.INSTANCE_ROOT_PROPERTY;

    /** Name of the configuration dir name; server.xml resides here */
    public static final String CONFIG_DIR_NAME = "config";

    /** Variable that contains the path to the entire iAS installation. */
    public static final String INSTALL_ROOT = SystemPropertyConstants.INSTALL_ROOT_PROPERTY; 

    /** Variable that contains the path to the Config Root. */
    public static final String INSTALL_CFG_ROOT = SystemPropertyConstants.CONFIG_ROOT_PROPERTY;
    
    public static final String INSTALL_IMQ_BIN = SystemPropertyConstants.IMQ_BIN_PROPERTY;
    public static final String INSTALL_IMQ_LIB = SystemPropertyConstants.IMQ_LIB_PROPERTY;

    // ---- DIFFERENT DEPLOYMENT CONSTANTS --------------------------------
    
    /** 
     * Variable that contains the path to the user-specified java compiler
     * that ejbc will try to use.  The variable is case-insensitive
     */
    public static final String USER_SPECIFIED_COMPILER =
                                    prefix + "deployment.java.compiler";

    /**
     * Variable that enable compilation through file for javac and fastjavac.
     */
    public static final String ENABLE_JAVAC_FILE = 
                                prefix + "deployment.javac.file.enable";
    
    /** 
     * Variable that contains the options for the user-specified java compiler
     * that ejbc will try to use.  The variable is case-insensitive
     */
    public static final String USER_SPECIFIED_COMPILER_OPTIONS = 
                                    USER_SPECIFIED_COMPILER + ".options";
     
    /** 
     * This is a backdoor designed for QA and support staff.
     * If the magical environmental variable, "KeepFailedStubs",
     * is set to "true", then it results in the generated stubs files being 
     * retained and placed into the expected directory with "_failed" appended 
     * to the name.  The directory will be deleted and replaced the next time 
     * there is a failed deployment.  It will never be automatically cleaned up.
     * Note:  KeepFailedStubs and true are both case insensitive.
     */    
    public final static String KEEP_FAILED_STUBS = 
                                    prefix + "deployment.KeepFailedStubs";

    /**
     * This is a backdoor to turn off annotation processing for deployment.
     */
    public static final String PROCESS_ANNOTATION = prefix + "deployment.ProcessAnnotation";

    /** Variable that contains the xsl directory for verifier tool */
    public static final String VERIFIER_XSL = "com.sun.aas.verifier.xsl";

    /** entry in the MANIFEST file of archive giving the resourceType*/
    public static final String APPLICATION_TYPE = "Application-Type";

    // --- SYSTEM APP DEPLOYMENT RELATED CONSTANTS --- //
    public static final String LIB = "lib";
    public static final String LIB_INSTALL = "install";
    public static final String LIB_INSTALL_APPLICATIONS = "applications";
    // target types
    public static final String TARGET_TYPE_ADMIN = "admin";
    public static final String TARGET_TYPE_INSTANCE = "instance";
    
    
    public static final String ALLOW_SYSAPP_DEPLOYMENT = prefix+ "deployment.AllowSysAppDeployment";


    // ---- TIMEOUTS FOR DIFFERENT COMPILERS USED BY EJBC ------------------

    /** option to specify fast javac time out */
    public static final String FASTJAVAC_TIMEOUT_MS = 
                                prefix + "deployment.Fastjavac.TimeoutMS";

    /** option to specify javac time out */
    public static final String JAVAC_TIMEOUT_MS = 
                                    prefix + "deployment.Javac.TimeoutMS";

    /** option to specify rmic time out */
    public static final String RMIC_TIMEOUT_MS = 
                                    prefix + "deployment.Rmic.TimeoutMS";

    /** option to specify user specified compiler time out */
    public static final String  USER_SPECIFIED_COMPILER_TIMEOUT_MS = 
                                    USER_SPECIFIED_COMPILER + ".TimeoutMS";

    /** default fast javac time out */
    public static final int DEFAULT_FASTJAVAC_TIMEOUT_MS = 4000;

    /** default javac time out */
    public static final int DEFAULT_JAVAC_TIMEOUT_MS     = 30000;

    /** default rmic time out */
    public static final int DEFAULT_RMIC_TIMEOUT_MS      = 40000;

    /** default user specified time out */
    public static final int DEFAULT_USER_SPECIFIED_COMPILER_TIMEOUT_MS = 
                                                    DEFAULT_JAVAC_TIMEOUT_MS;

    /**
     * The separator character between an application name and the web
     * module name within the application.
     */
    public static final String NAME_SEPARATOR = ":";

	/** cmp-only deployment arguments and the 3 possible "tri-state" values.
	 * bnevins
	 */
	public static final String	CMP_UNIQUE_TABLE_NAMES		= "CmpInfo.uniqueTableNames";
	public static final String	CMP_DB_VENDOR_NAME			= "CmpInfo.dbVendorName";
	public static final String	CMP_DROP_TABLES				= "CmpInfo.DROP_TABLES";
	public static final String	CMP_CREATE_TABLES			= "CmpInfo.CREATE_TABLES";
	public static final String	CMP_DROP_AND_CREATE_TABLES	= "CmpInfo.DROP_AND_CREATE_TABLES";
	public static final String	TRUE						= "true";
	public static final String	FALSE						= "false";
	public static final String	UNDEFINED					= "undefined";

        // constants for loading/unloading application
        public static final String LOAD_UNLOAD_ACTION = "loadUnloadAction"; 

        // possible values for load/unload action
        // load all parts of regular application
        public static final int LOAD_UNSET = 0;
        public static final int LOAD_ALL = 1;
        // load rars of the embedded rar application
        public static final int LOAD_RAR = 2;
        // load rest of the embedded rar application
        public static final int LOAD_REST = 3;
        // unload all parts of regular application
        public static final int UNLOAD_ALL = 4;
        // unload rars of the embedded rar application
        public static final int UNLOAD_RAR = 5;
        // unload rest of the embedded rar application
        public static final int UNLOAD_REST = 6;
}
