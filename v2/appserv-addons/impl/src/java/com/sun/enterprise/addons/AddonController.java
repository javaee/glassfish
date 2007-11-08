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

package com.sun.enterprise.addons;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.net.URL;
import java.net.URLClassLoader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import com.sun.appserv.addons.AddonFatalException;
import com.sun.enterprise.addons.util.JarFileFilter;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.appserv.addons.ConfigurationContext;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.config.serverbeans.HttpListener;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.appserv.management.client.prefs.LoginInfoStore;
import com.sun.appserv.management.client.prefs.LoginInfoStoreFactory;
import com.sun.appserv.management.client.prefs.LoginInfo;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigFactory;

/**
 * Abstract Controller class. This gives all the common 
 * functionality for AddonInstallationController and 
 * AddonConfigurationController. It is also the basic
 * factory that provide concrete Controller classes.
 * <p><b>NOT THREAD SAFE: mutable instance variable: logger, installRoot, servicesAreLoaded</b>
 *  
 * @see AddonInstallationController
 * @see AddonConfigurationController
 * @since 9.1
 * @author binod@dev.java.net
 */
public abstract class AddonController {

    private final HashMap apiBasedServices = new HashMap();
    private final HashMap mainClassBasedServices = new HashMap();
    private final HashMap simpleJars = new HashMap();
    private volatile File installRoot = null;
    private boolean servicesAreLoaded = false;
    private static String adminUser = null;
    private static String adminPassword = null;
    private final String DEFAULT_ADMIN_USER = "admin";
    private final String DEFAULT_ADMIN_PASSWORD = "adminadmin";
    private File domainRoot = null;

    private volatile Logger logger = null;
    protected static final StringManager localStrings =
        StringManager.getManager(AddonController.class);

    /*
     * Examine the plugins and load the necessary services.
     * The loaded services and their names will be saved in HashMap
     * that can be used by others.
     */
    protected void loadServices(File jarDir) throws AddonFatalException {

        if (servicesAreLoaded) {
            return;
        } else {
            servicesAreLoaded = true;
        }

        try {
            File[] jars = jarDir.listFiles(getFilenameFilter());
            if (jars == null) return;
            for (File jar : jars) {
                 if (jar != null) {
                    if (jar.getName().startsWith("grizzly")) {
                        continue;
                    } else if (jar.getName().startsWith("freemarker")) {
                        continue;
                    } else if (jar.getName().startsWith("wadl2java")) {
                        continue;
                    }
                 }
                 JarFile jF = new JarFile(jar);
                 String serviceName = findApiBasedService(jF);
                 ClassLoader cl = createClassLoader(jar.toURI().toURL());
                 if (serviceName != null) {
                     apiBasedServices.put(cl.loadClass
                     (serviceName).newInstance(), getName(jar));
                 } else {
                     String mainClass = findMainClassBasedService(jF);
                     if (mainClass != null) {
                         mainClassBasedServices.put(cl.loadClass
                         (mainClass).newInstance(), getName(jar));
                     } else {
                         simpleJars.put(jar, getName(jar));
                     }
                 }
            }
        } catch (AddonFatalException afe) {
            throw afe;
        }catch (Exception e) {
            throw new AddonFatalException(e);
        }
    }

    /**
     * Create a classloader that load the service.
     */
    protected ClassLoader createClassLoader(URL jar) 
    throws AddonFatalException {
        URL[] classpath = null;
        try {
            classpath = AddonClassPath.getClassPath(jar, 
            getInstallRoot().getCanonicalPath(), getLogger());
        } catch (Exception e) {
            throw new AddonFatalException(e);
        }
        return new URLClassLoader(classpath, this.getClass().getClassLoader());
    }

    /**
     * Return the services that are based on the java api for addons.
     */
    protected HashMap getApiBasedServices() {
        return apiBasedServices;
    }

    /**
     * Return the services that are based on the manifest main class.
     */
    protected HashMap getMainClassBasedServices() {
        return mainClassBasedServices;
    }

    /**
     * Return the services that are neither api based nor main class 
     * based. These are simple jar files. 
     */
    protected HashMap getSimpleJars() {
        return simpleJars;
    }

    /**
     * Each controller use specific naming scheme for the 
     * finding the plugins. 
     */
    abstract protected FilenameFilter getFilenameFilter();

    /**
     * Retrive the name of the addon from the file name.
     */
    abstract protected String getName(File jar) throws AddonFatalException;
    

    /**
     * Return the directory where the plugin jar files  
     * will be kept.
     */
    abstract protected File getServiceJarLocation(); 

    /**
     * Return the service interface name.
     */
    abstract protected String getServiceInterface(); 

    /**
     * Find the api based service from the jar file.
     */
    protected String findApiBasedService(JarFile jF) 
    throws AddonFatalException {
        try {
            ZipEntry zE = jF.getEntry(getServiceInterface());
            if (zE != null) {
                InputStream in = jF.getInputStream(zE);
                BufferedReader br = new BufferedReader(
                                    new InputStreamReader(in));
                return br.readLine();
            }
        } catch (Exception e) {
            getLogger().log(Level.FINE, e.getMessage(), e);
            throw new AddonFatalException(e);
        }
        return null;
    }

    /**
     * Find the main class based service from the jarFile.
     */
    protected String findMainClassBasedService(JarFile jF) 
    throws AddonFatalException {
        try {
            Manifest mf = jF.getManifest();
            if(mf != null) {
                Attributes attrs = mf.getMainAttributes();
                if(attrs != null) {
                    String main = attrs.getValue(Attributes.Name.MAIN_CLASS);
                    if(main != null) {
                        return main;
                    }
                }
            }
        } catch (Exception e) {
            throw new AddonFatalException(e);
        }
        return null;
    }


    /**
     * Set the installation root directory.
     */
    public void setInstallRoot(File installRoot) {
        this.installRoot = installRoot;
    }

    /**
     * Return the installation root directory.
     */
    protected File getInstallRoot() {
        return this.installRoot;
    }


    /**
     * Create a controller class for Addon Installation.
     */
    public static AddonInstallationController 
        getAddonInstallationController() {
        return new AddonInstallationController();
    }
    
    /**
     * Create a controller class for Addon Configuration.
     */
    public static AddonConfigurationController 
        getAddonConfigurationController() {
        return new AddonConfigurationController();
    }


    /**
     * Return the logger instance used for logging.
     */
    protected synchronized Logger getLogger() {
        if (logger == null) {
            return Logger.getAnonymousLogger();
        }
        return logger;
    }

    /**
     * Set an appropriate logger.
     */
    public void setLogger(Logger logger) {
        this.logger = logger; 
    }

    public void setAdminCredentials(ConfigurationContext cc) {
        if ((adminPassword == null) || (adminPassword.length() < 1)) {
            populateAdminCredentials();
        }
        cc.setAdminUser(adminUser);
        cc.setAdminPassword(adminPassword);
    }

    private void populateAdminCredentials() {
         try {
           String domainXMLLocation = 
                (getDomainRoot().getCanonicalPath()) + File.separator + 
                "config" + File.separator + "domain.xml"; 
           File domainXML=new File(domainXMLLocation);
           ConfigContext configContext=ConfigFactory.createConfigContext(
                                domainXML.getAbsolutePath()); 
           HttpListener as=ServerHelper.getHttpListener(configContext, 
                                "server", ServerHelper.ADMIN_HTTP_LISTNER_ID);
           String host="localhost";
           int port=Integer.parseInt(as.getPort());
           final LoginInfoStore store = LoginInfoStoreFactory.getStore(null);
           if (store.exists(host, port)) {
               LoginInfo login = store.read(host, port);
               adminPassword = login.getPassword();
               adminUser = login.getUser();
               if (adminUser == null) {
                    adminUser = DEFAULT_ADMIN_USER;
                    adminPassword = DEFAULT_ADMIN_PASSWORD;
               }
           }
       } catch (Exception e) {
           adminUser = DEFAULT_ADMIN_USER;
           adminPassword = DEFAULT_ADMIN_PASSWORD;
           getLogger().log(Level.WARNING, e.getMessage(), e);
       } 
    }

    void setDomainRoot(File domainRoot) {
        this.domainRoot = domainRoot;
    }

    File getDomainRoot() {
        return this.domainRoot;
    }

}
