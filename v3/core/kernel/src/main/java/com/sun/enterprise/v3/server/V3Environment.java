/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.v3.server;

import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.module.bootstrap.StartupContext;

/**
 * Fake for V2 InstanceEnvironment.
 *
 * @author Jerome Dochez
 */
public class V3Environment  {

    /** folder where all generated code like compiled jsps, stubs is stored */

    public static final String kGeneratedDirName		= "generated";

    /** default folder where deployed j2ee-apps are stored */
    public static final String kRepositoryDirName		= "applications";

    public static final String kApplicationDirName		= "j2ee-apps";

    /** folder where deployed modules are stored */

    public static final String kModuleDirName		= "j2ee-modules";
    
    public static final String kConfigXMLFileName		= "domain.xml";

    public static final String kLoggingPropertiesFileNAme = "logging.properties";
    
    /** folder where the configuration of this instance is stored */
    public static final String kConfigDirName		= "config";    
    
    /** init file name */
    public static final String kInitFileName                = "init.conf";   
    
    /** folder where the compiled JSP pages reside */
    public static final String kCompileJspDirName		= "jsp";

    // TODO: this should be File
    final private String root;

    final private StartupContext startupContext;
    
    /** Creates a new instance of V3Environment */
    public V3Environment(String root, StartupContext startupContext) {
        this.root = root;
        this.startupContext = startupContext;
    }
    
    public String getDomainRoot() {
        return root;
    }

    public StartupContext getStartupContext() {
        return startupContext;
    }
    
    public String getConfigDirPath() {
        String[] folderNames = new String[] {root, kConfigDirName};
        return StringUtils.makeFilePath(folderNames, false);
    }

    public String getApplicationRepositoryPath() {
        String[] onlyFolderNames = new String[] {root, kRepositoryDirName};
        return StringUtils.makeFilePath(onlyFolderNames, false);
    }

    public String getApplicationStubPath() {
        String[] onlyFolderNames = new String[] {root, kGeneratedDirName};
        return StringUtils.makeFilePath(onlyFolderNames, false);

    }
    
    public String  getInitFilePath() {
        String[] fileNames = new String[] {root,
            kConfigDirName, kInitFileName};
        return StringUtils.makeFilePath(fileNames, false);
    }    
    
    public String getLibPath() {
        String[] fileNames = new String[] {root,
            "lib"};
        return StringUtils.makeFilePath(fileNames, false);
        
    }

    public String getApplicationGeneratedXMLPath() {
        return null;
    }
    
    /**
        Returns the path for compiled JSP Pages from an J2EE application
        that is deployed on this instance. By default all such compiled JSPs
        should lie in the same folder.
    */
    public String getApplicationCompileJspPath() {
        String[] onlyFolderNames = new String[] {root,
            kGeneratedDirName, kCompileJspDirName, kApplicationDirName};
        return StringUtils.makeFilePath(onlyFolderNames, false);
    }

    /**
        Returns the path for compiled JSP Pages from an Web application
        that is deployed standalone on this instance. By default all such compiled JSPs
        should lie in the same folder.
    */
    public String getWebModuleCompileJspPath() {        
        String[] onlyFolderNames = new String[] {root,
            kGeneratedDirName, kCompileJspDirName, kModuleDirName};
        return StringUtils.makeFilePath(onlyFolderNames, false);
    }
   
    /**
        Returns the absolute path for location where all the deployed
        standalone modules are stored for this Server Instance.
    */
    public String getModuleRepositoryPath() {        
        return null;
    }
    
    public String getJavaWebStartPath() {
        return null;
    }
    
    public String getApplicationBackupRepositoryPath() {
        return null;
    }    
    
    public String getInstanceClassPath() {
        return null;
    }

    public String getModuleStubPath() {
        return null;
    }
}

