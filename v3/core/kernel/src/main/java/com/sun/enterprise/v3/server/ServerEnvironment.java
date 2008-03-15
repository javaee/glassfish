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
import com.sun.enterprise.universal.glassfish.ASenvPropertyReader;
import com.sun.enterprise.universal.glassfish.SystemPropertyConstants;
import java.io.*;
import java.util.*;

/**
 * Fake for V2 InstanceEnvironment.
 *
 * @author Jerome Dochez
 */
public class ServerEnvironment {

    /** folder where all generated code like compiled jsps, stubs is stored */
    public static final String kGeneratedDirName = "generated";
    /** default folder where deployed j2ee-apps are stored */
    public static final String kRepositoryDirName = "applications";
    public static final String kApplicationDirName = "j2ee-apps";
    /** folder where deployed modules are stored */
    public static final String kModuleDirName = "j2ee-modules";
    public static final String kConfigXMLFileName = "domain.xml";
    public static final String kLoggingPropertiesFileNAme = "logging.properties";
    /** folder where the configuration of this instance is stored */
    public static final String kConfigDirName = "config";
    /** init file name */
    public static final String kInitFileName = "init.conf";
    /** folder where the compiled JSP pages reside */
    public static final String kCompileJspDirName = "jsp";

    // TODO: this should be File
    final private String root;
    final private StartupContext startupContext;
    private final boolean verbose;
    private final boolean debug;
    private static final ASenvPropertyReader asenv = new ASenvPropertyReader();
    private final String domainName; 
    private final String instanceName;

    /** Creates a new instance of ServerEnvironment */
    public ServerEnvironment(String root, StartupContext startupContext) {
        this.root = root;
        this.startupContext = startupContext;
        asenv.getProps().put(SystemPropertyConstants.INSTANCE_ROOT_PROPERTY, root);
        Map<String, String> args = startupContext.getArguments();

        verbose = Boolean.parseBoolean(args.get("-verbose"));
        debug = Boolean.parseBoolean(args.get("-debug"));

        // ugly code because domainName & instanceName are final...
        String s = startupContext.getArguments().get("-domainname");

        if (!ok(s)) {
            try {
                // ugly but we must protect since we're working with a String -- 
                // not a file!
                s = new File(root).getName(); 
            }
            catch(Exception e) { 
                s = ""; 
            }
        }
        domainName = s;

        s = startupContext.getArguments().get("-instancename");

        if (!ok(s)) {
            instanceName = "server";
        }
        else {
            instanceName = s;
        }
    }

    public String getInstanceName() {
        return instanceName;
    }
    
    public String getDomainName() {
        return domainName;
    }
    
    public String getDomainRoot() {
        return root;
    }

    public StartupContext getStartupContext() {
        return startupContext;
    }

    public String getConfigDirPath() {
        String[] folderNames = new String[]{root, kConfigDirName};
        return StringUtils.makeFilePath(folderNames, false);
    }

    public String getApplicationRepositoryPath() {
        String[] onlyFolderNames = new String[]{root, kRepositoryDirName};
        return StringUtils.makeFilePath(onlyFolderNames, false);
    }

    public String getApplicationStubPath() {
        String[] onlyFolderNames = new String[]{root, kGeneratedDirName};
        return StringUtils.makeFilePath(onlyFolderNames, false);

    }

    public String getInitFilePath() {
        String[] fileNames = new String[]{root,
            kConfigDirName, kInitFileName
        };
        return StringUtils.makeFilePath(fileNames, false);
    }

    public String getLibPath() {
        String[] fileNames = new String[]{root,
            "lib"
        };
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
        String[] onlyFolderNames = new String[]{root,
            kGeneratedDirName, kCompileJspDirName
        };
        return StringUtils.makeFilePath(onlyFolderNames, false);
    }

    /**
    Returns the path for compiled JSP Pages from an Web application
    that is deployed standalone on this instance. By default all such compiled JSPs
    should lie in the same folder.
     */
    public String getWebModuleCompileJspPath() {
        return getApplicationCompileJspPath();
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


    public Map<String, String> getProps() {
        return Collections.unmodifiableMap(asenv.getProps());
    }

    private boolean ok(String s) {
        return s != null && s.length() > 0;
    }
}

