/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2006-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.server;

import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.universal.glassfish.ASenvPropertyReader;
import com.sun.enterprise.util.SystemPropertyConstants;

import java.io.*;
import java.util.*;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PostConstruct;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.embedded.Server;

/**
 * Defines various global configuration for the running GlassFish instance.
 *
 * <p>
 * This primarily replaces all the system variables in V2.
 *
 * @author Jerome Dochez
 */
@Service
public class ServerEnvironmentImpl implements ServerEnvironment, PostConstruct {
    @Inject
    StartupContext startupContext;

    @Inject(optional=true)
    Server server=null;

    /** folder where all generated code like compiled jsps, stubs is stored */
    public static final String kGeneratedDirName = "generated";
    public static final String kRepositoryDirName = "applications";
    public static final String kEJBStubDirName = "ejb";
    public static final String kGeneratedXMLDirName = "xml";

    public static final String kConfigXMLFileName = "domain.xml";
    public static final String kLoggingPropertiesFileName = "logging.properties";
    /** folder where the configuration of this instance is stored */
    public static final String kConfigDirName = "config";
    /** init file name */
    public static final String kInitFileName = "init.conf";

    public static final String DEFAULT_ADMIN_CONSOLE_CONTEXT_ROOT = "/admin";
    public static final String DEFAULT_ADMIN_CONSOLE_APP_NAME     = "__admingui"; //same as folder
    
    private /*almost final*/ File root;
    private /*almost final*/ boolean verbose;
    private /*almost final*/ boolean debug;
    private ASenvPropertyReader asenv;
    private /*almost final*/ String domainName;
    private /*almost final*/ String instanceName;

    private final static String INSTANCE_ROOT_PROP_NAME = "com.sun.aas.instanceRoot";

    /**
     * Compute all the values per default.
     */
    public ServerEnvironmentImpl() {
    }

    public ServerEnvironmentImpl(File root) {
        // the getParentFile() that we do later fails to work correctly if
        // root is for example "new File(".")
        this.root = root.getAbsoluteFile();
        asenv = new ASenvPropertyReader();
    }

    /**
     * This is where the real initialization happens.
     */
    public void postConstruct() {

        // todo : dochez : this will need to be reworked...
        if (server==null) {
            asenv = new ASenvPropertyReader(startupContext.getRootDirectory().getParentFile());
        } else {
            asenv = new ASenvPropertyReader(startupContext.getRootDirectory());
        }

        // default
        if(this.root==null) {
            String envVar = System.getProperty(INSTANCE_ROOT_PROP_NAME);
            if (envVar!=null) {
                root = new File(envVar);
            } else {
                root = startupContext.getRootDirectory();
            }
        }

        asenv.getProps().put(SystemPropertyConstants.INSTANCE_ROOT_PROPERTY, root.getAbsolutePath());
        for (Map.Entry<String, String> entry : asenv.getProps().entrySet()) {

            File location = new File(entry.getValue());
            if (!location.isAbsolute()) {
                location = new File(asenv.getProps().get(SystemPropertyConstants.INSTANCE_ROOT_PROPERTY), entry.getValue());
            }
            System.setProperty(entry.getKey(), location.getAbsolutePath());
        }
        
        Properties args = startupContext.getArguments();

        verbose = Boolean.parseBoolean(args.getProperty("-verbose"));
        debug = Boolean.parseBoolean(args.getProperty("-debug"));

        // ugly code because domainName & instanceName are final...
        String s = args.getProperty("-domainname");

        if (!ok(s)) {
            s = root.getName();
        }
        domainName = s;

        s = args.getProperty("-instancename");

        if (!ok(s)) {
            instanceName = "server";
        }
        else {
            instanceName = s;
        }
        // bnevins IT 10209
        asenv.getProps().put(SystemPropertyConstants.SERVER_NAME, instanceName);
        System.setProperty(SystemPropertyConstants.SERVER_NAME, instanceName);
    }

    public String getInstanceName() {
        return instanceName;
    }
    
    public String getDomainName() {
        return domainName;
    }
    
    public File getDomainRoot() {
        return root;
    }


    public StartupContext getStartupContext() {
        return startupContext;
    }

    /**
     * Gets the directory to store configuration.
     * Normally {@code ROOT/config}
     */
    public File getConfigDirPath() {
        return new File(root,kConfigDirName);
    }

    /**
     * Gets the directory to store deployed applications
     * Normally {@code ROOT/applications}
     */
    public File getApplicationRepositoryPath() {
        return new File(root,kRepositoryDirName);
    }

    /**
     * Gets the directory to store generated stuff.
     * Normally {@code ROOT/generated}
     */
    public File getApplicationStubPath() {
        return new File(root,kGeneratedDirName);
    }

    /**
     * Gets the <tt>init.conf</tt> file.
     */
    public File getInitFilePath() {
        return new File(getConfigDirPath(),kInitFileName);
    }

    /**
     * Gets the directory for hosting user-provided jar files.
     * Normally {@code ROOT/lib}
     */
    public File getLibPath() {
        return new File(root,"lib");

    }

    public File getApplicationEJBStubPath() {
        return new File(getApplicationStubPath(), kEJBStubDirName);
    }

    public File getApplicationGeneratedXMLPath() {
        return new File(getApplicationStubPath(),kGeneratedXMLDirName);
    }

    /**
     * Returns the path for compiled JSP Pages from an J2EE application
     * that is deployed on this instance. By default all such compiled JSPs
     * should lie in the same folder.
     */
    public File getApplicationCompileJspPath() {
        return new File(getApplicationStubPath(),kCompileJspDirName);
    }

    /**
     * Returns the path for compiled JSP Pages from an Web application
     * that is deployed standalone on this instance. By default all such compiled JSPs
     * should lie in the same folder.
     */
    public File getWebModuleCompileJspPath() {
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
    
    /** Returns the folder where the admin console application's folder (in the
     *  name of admin console application) should be found. Thus by default,
     *  it should be: [install-dir]/lib/install/applications. No attempt is made
     *  to check if this location is readable or writable.
     *  @return java.io.File representing parent folder for admin console application
     *   Never returns a null
     */
    public File getDefaultAdminConsoleFolderOnDisk() {
        File install = new File(asenv.getProps().get(SystemPropertyConstants.INSTALL_ROOT_PROPERTY));
        File agp = new File(new File(new File(install, "lib"), "install"), "applications");
        return (agp);
    }
    
    public File getMasterPasswordFile() {
        return new File (getDomainRoot(), "master-password");
    }

    public File getJKS() {
        return new File (getConfigDirPath(), "keystore.jks");
    }

    private Status status=Status.starting;
    public Status getStatus() {
        return status;
    }
    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean isEmbedded() {
        return server!=null;
    }
}

