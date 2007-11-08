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
package com.sun.enterprise.appclient.jws;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.interfaces.DeploymentImplConstants;
import com.sun.enterprise.deployment.runtime.JavaWebStartAccessDescriptor;
import com.sun.enterprise.deployment.util.ModuleDescriptor;
import com.sun.enterprise.instance.AppclientModulesManager;
import com.sun.enterprise.instance.ApplicationEnvironment;
import com.sun.enterprise.instance.AppsManager;
import com.sun.enterprise.instance.BaseManager;
import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.web.WebContainer;
import com.sun.logging.LogDomains;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.ModuleType;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;

/**
 *Defines strings and algorithms used in constructing names, URLs, etc. related to supporting Java
 *Web Start access to app clients.
 *<p>
 *The logic for building names, URLs, etc. is collected into this one class
 *so it is easier to understand go to make changes.
 *
 * @author tjquinn
 */
public class NamingConventions {
    
    /** fixed prefix for URLs handled by the Java Web Start support web app */
    
    /*
     *The system EAR for Java Web Start specifies the URL for the embedded
     *web app.  That same value must be assigned here.
     */
    public static final String SYSTEM_WEBAPP_URL = "/__JWSappclients";
    
    /**
     *Performs any conversion needed from the HTTP request path information to 
     *the corresponding content key
     *@param the pathInfo from the HTTP request
     *@return the content key for the given request pathInfo
     */
    public static String pathToContentKey(String requestPath) {
        return requestPath;
    }
    
    /**
     *Converts the content key into the corresponding path for URLs.
     *@param content key to be converted
     *@return the path to be used in URLs
     */
    public static String contentKeyToPath(String contentKey) {
        return contentKey;
    }
    
    /**
     *URL paths used to request content have this form:
     *
     *${category}/${subcategory}/${path-to-content}
     *
     *The subcategory has different meanings depending on the category.
     *
     *Categories:
     *
     *appclient - Content related to a particular app client.  The subcategory
     *is the appclient's context-root (for Java Web Start access) assigned either
     *by the user in the runtime deployment descriptor or by default - the module ID
     *
     *application - Content related to the enterprise app in which one or more
     *app clients were bundled.  The subcategory is the enterprise app's module
     *ID. 
     *
     *appserver - Content related to the app server itself.  The subcategory 
     *is used to gather files and content into roughly related groups.  Partly it
     *exists to let the Java Web Start support logic treat all requests pretty
     *uniformly.
     *
     *appserverS - Content related to the app server itself, but signed (jars).
     *
     */
    
    /** Category definitions */
    public static final String APPCLIENT_CATEGORY = "__appclient";
    public static final String APPLICATION_CATEGORY = "__application";
    public static final String APPSERVER_CATEGORY = "__appserver";
    public static final String APPSERVER_SIGNED_CATEGORY = "__appserverS";
    
    /** Subcategories of appserver content. */

    public static final String APPSERVER_LIB_FILES = "aslib";   // typically in ${installRoot}/lib 
    public static final String APPSERVER_MQLIB_FILES = "mqlib"; // MQ files packaged with the app server - typically in ${installRoot}/imq/lib 
    public static final String APPSERVER_JMSRALIB_FILES = "jmsralib"; // JMS files packaged with the app server - in ${installRoot}/lib/install/applications/jmsra
    public static final String APPSERVER_EXTJAR_FILES = "extjar"; // extension jars
    public static final String APPSERVER_DERBY_FILES = "derby"; // derby library jar files
    /**
     *Templates for dynamic documents are packaged in the jar file with this class.
     *These names are used in getResource method invocations to retrieve the templates.
     */
    public static final String APPCLIENT_MAIN_JNLP_TEMPLATE_NAME = "appclientMainDocumentTemplate.jnlp";
    public static final String APPCLIENT_CLIENT_JNLP_TEMPLATE_NAME = "appclientClientDocumentTemplate.jnlp";
    public static final String APPCLIENT_MAIN_HTML_TEMPLATE_NAME = "appclientMainDocumentTemplate.html";
    public static final String APPCLIENT_CLIENT_HTML_TEMPLATE_NAME = "appclientClientDocumentTemplate.html";
    public static final String APPCLIENT_MAIN_JNLP_EXT_TEMPLATE_NAME = "appclientMainExtDocumentTemplate.jnlp";

    /**
     *Several generated virtual file names use the context root as part of the
     *name with fixed suffix appended.  
     */
    private static final String MAIN_JNLP_SUFFIX = ".jnlp";
    private static final String MAIN_HTML_SUFFIX = "-jnlp.html";
    private static final String CLIENT_JNLP_SUFFIX = "-client.jnlp";
    private static final String CLIENT_HTML_SUFFIX = "-client-jnlp.html";
    private static final String MAIN_EXT_JNLP_SUFFIX = "-ext.jnlp";
    
    /** the logger to use for Java Web Start-related code */
    public static final String JWS_LOGGER = LogDomains.DPL_LOGGER;
    
    /**
     *The admin GUI (or any other client) can create an instance of NamingConventions
     *for use in retrieving the actual URL path for a stand-alone app client
     *or a nested app client, including using user-specified paths if they 
     *are present.
     *
     *The next instance variables support that use.
     */
    
    /** the instance's apps manager */
    private AppsManager appsManager = null;
    
    /** the instance's app client modules manager */
    private AppclientModulesManager appclientModulesManager = null;
    
    /**
     *Creates a new instance of NamingConventions, locating the required
     *manager objects automatically.
     *@throws ConfigException in case of any problem locating the app server
     *objects that provide the AppsManager and AppclientModulesManager objects.
     */
    public NamingConventions() throws ConfigException {
        ServerContext appServerContext;
        InstanceEnvironment instEnv;
        
        if ((appServerContext = ApplicationServer.getServerContext()) == null) {
            throw new ConfigException("Error getting current app server context; ApplicationServer.getServerContext() returned null");
        }
        if ((instEnv = appServerContext.getInstanceEnvironment()) == null) {
            throw new ConfigException("Error getting current instance environment; appServercontext.getInstanceEnvironment() returned null");
        }
        appsManager = new AppsManager(instEnv, false);
        appclientModulesManager = new AppclientModulesManager(instEnv, false);
    }
    
    /** Creates a new instance of NamingConventions, using previously-created
     *references to the two manager objects.
     *@param appsManager the AppsManager instance to use for looking up applications
     *@param appclientsManager the AppclientModulesManager instance to use for looking up app clients
     */
    public NamingConventions(AppsManager appsManager, AppclientModulesManager appclientModulesManager) {
        this.appsManager = appsManager;
        this.appclientModulesManager = appclientModulesManager;
    }
    
    public static String webAppURI() {
        return SYSTEM_WEBAPP_URL;
    }
    
    public static String appServerCodebasePath() {
        return SYSTEM_WEBAPP_URL + "/" + APPSERVER_CATEGORY;
    }

    public static String fullJarPath(String contentKey) {
        return SYSTEM_WEBAPP_URL + contentKeyToPath(contentKey);
    }
    
    public static String relativeFilePath(URI instanceRootDirURI, File targetFile) {
        URI targetURI = targetFile.toURI();
        URI relativeURI = instanceRootDirURI.relativize(targetURI);
        return relativeURI.toString();
    }

    public static String extJarFilePath(int extDirNumber, File extJarFile) {
        String path = "/" + extDirNumber + "/" + extJarFile.getName();
        return path;
    }

    /**
     *Returns the developer-specified context root from the app client's
     *runtime descriptor.
     *@return the developer-specified context root; null if none was specified
     */
    private static String getExplicitContextRoot(ModuleDescriptor moduleDescr) {
        /*
         *Return the developer-specified context root, if there is one.
         */
        String result = null;

        BundleDescriptor bd = moduleDescr.getDescriptor();
        if (bd instanceof ApplicationClientDescriptor) {
            ApplicationClientDescriptor acd = (ApplicationClientDescriptor) bd;
            JavaWebStartAccessDescriptor jwsAD = acd.getJavaWebStartAccessDescriptor();
            if (jwsAD != null) {
                result = jwsAD.getContextRoot();
            }
        }
        return result;
    }
    
    /**
     *Returns the explicit context root, if non-null, or the default value otherwise.
     *@return the correct context root value to be used
     */
    private static String chooseContextRoot(String explicitValue, String defaultValue) {
        return (explicitValue == null) ? defaultValue : explicitValue;
    }
    
    public static String appclientCodebasePath(AppclientContentOrigin origin) {
        String result = SYSTEM_WEBAPP_URL + "/" + APPCLIENT_CATEGORY + /* "/" + */ origin.getContextRoot();
        return result;
    }

    private static String appclientJarFilename(String regName) {
        return regName + DeploymentImplConstants.ClientJarSuffix;
    }

  
    /**
     *Defines naming conventions specific to top-level app clients.
     */
    public static class TopLevelAppclient {
        
        public static String virtualContextRoot(Application application, ModuleDescriptor moduleDescr) {
            return chooseContextRoot(getExplicitContextRoot(moduleDescr), defaultVirtualContextRoot(application));
            }
        
        public static String actualContextRoot(Application application) {
            String regName = application.getRegistrationName();
            return "/" + APPCLIENT_CATEGORY + "/" + regName + NamingConventions.Main.JNLPPath(regName);
        }
        
        public static String defaultVirtualContextRoot(Application application) {
            return "/" + application.getRegistrationName();
        }
        
        public static String contentKeyPrefix(AppclientContentOrigin origin) {
            return "/" + APPCLIENT_CATEGORY + "/" + origin.getTopLevelRegistrationName();
        }
        
        public static String appclientJarPath(AppclientContentOrigin origin) {
            return "/" + appclientJarFilename(origin.getTopLevelRegistrationName());
        }
    }

    /**
     *Defines naming conventions specific to nested app clients.
     */
    public static class NestedAppclient {
        
        public static String virtualContextRoot(Application parentApplication, ModuleDescriptor moduleDescr) {
            return chooseContextRoot(getExplicitContextRoot(moduleDescr), defaultVirtualContextRoot(parentApplication, moduleDescr));
        }
        
        public static String actualContextRoot(NestedAppclientContentOrigin origin) {
            String regName = origin.getTopLevelRegistrationName();
            String clientName = origin.getName();
            return "/" + APPCLIENT_CATEGORY + "/" + regName + "/" + clientName + NamingConventions.Main.JNLPPath(clientName);
        }
        
        public static String defaultVirtualContextRoot(Application parentApplication, ModuleDescriptor moduleDescr) {
            return "/" + parentApplication.getRegistrationName() +  "/" + trimJarFileType(moduleDescr.getArchiveUri(),"");
        }
 
        public static String contentKeyPrefix(NestedAppclientContentOrigin origin) {
            return "/" + APPCLIENT_CATEGORY + "/" + origin.getTopLevelRegistrationName() + "/" + origin.getName();
        }
        
        /*
         *Nested app clients do not have their own appclient jar files, so there
         *is no implementation of that method for this inner class.
         */
        
        /**
         *Returns the string to use as the app client identifier within the
         *parent application.
         *<p>
         *Nested app clients are uniquely identified within the containing parent
         *only by the URI to the archive.  Convert the URI to the archive into
         *a unique name by removing the trailing ".jar" from the URI.
         *@param URI to the archive in string form
         *@return the unique name for this nested app client within its parent
         */
        public static String archiveURIToName(String archiveURI) {
            return trimJarFileType(archiveURI, "");
        }
    }
    
    public static class TopLevelApplication {
        
        public static String contextRoot(Application application) {
            return "/" + application.getRegistrationName();
        }
        
        public static String contentKeyPrefix(ApplicationContentOrigin origin) {
            return "/" + APPLICATION_CATEGORY + "/" + origin.getTopLevelRegistrationName();
        }
        
        public static String appclientJarPath(ApplicationContentOrigin origin) {
            return "/" + appclientJarFilename(origin.getTopLevelRegistrationName());
        }
    }
    
    public static class Appserver {
        public static String contentKeyPrefix(AppserverContentOrigin origin) {
            return "/" + APPSERVER_CATEGORY + "/" + origin.getSubcategory();
        }
    }
    
    /**
     *Defines naming conventions related to the main JNLP document created for 
     *either top-level or nested app clients.
     */
    public static class Main {

        private static final String name = "main";
        
        public static String JNLPFilename(String regName) {
            return name + MAIN_JNLP_SUFFIX;
        }
        
        public static String JNLPPath(String regName) {
            return "/" + JNLPFilename(regName);
        }

        public static String JNLPExtFilename(String regName) {
            return name + MAIN_EXT_JNLP_SUFFIX;
        }

        public static String JNLPExtPath(String regName) {
            return "/" + JNLPExtFilename(regName);
        }

        public static String HTMLPath(String regName) {
            return "/" + HTMLFilename(regName);
        }

        public static String HTMLFilename(String regName) {
            return name + MAIN_HTML_SUFFIX;
        }
    }

    /**
     *Defines naming conventions related to the client JNLP document created for
     *either top-level or nested app clients.
     */
    public static class Client {

        private static final String name = "client";
        
        public static String JNLPFilename(String regName) {
            return name + CLIENT_JNLP_SUFFIX;
        }

        public static String JNLPPath(String regName) {
            return "/" + JNLPFilename(regName);
        }

        public static String HTMLFilename(String regName) {
            return name + CLIENT_HTML_SUFFIX;
        }

        public static String HTMLPath(String regName) {
            return "/" + HTMLFilename(regName);
        }
    }


    /**
     *Replaces the final appearance of ".jar" in the input string with the
     *replacement string.
     *@param the input string (typically the URI of the app client jar file)
     *@param the string to replace the final ".jar" in the original
     */
    private static String trimJarFileType(String jarURI, String replacement) {
        int startOfType = jarURI.lastIndexOf(".jar");
        if (startOfType == -1) {
            startOfType = jarURI.length();
        }
        String result = jarURI.substring(0, startOfType) + replacement;
        return result;
    }
    
    /**
     *Returns the URL path for use in launching the stand-alone app client with the
     *specified module ID.
     *@param moduleID the module ID of the stand-alone app client 
     *@return String containing the URL path (excluding scheme, host, and port) for launching the app client; null if the
     *app client is not eligible for Java Web Start access or cannot be queried
     */
    public String getLaunchURLPath(String appclientModuleID) {
        String result = null;
        try {
            Application app = appclientModulesManager.getDescriptor(appclientModuleID, Thread.currentThread().getContextClassLoader(), false);
            /*
             *There should be exactly one app client ModuleDescriptor in this wrapper app.
             */
            ModuleDescriptor[] moduleDescrs = getEligibleAppclientModuleDescriptors(app);
            if (moduleDescrs.length == 1) {
                /*
                 *With the app client's module descr, find the path using either
                 *the developer-provided path or the default one.
                 */
                result = TopLevelAppclient.virtualContextRoot(app, moduleDescrs[0]);
            }
        } catch (ConfigException ce) {
            /*
             *Allow the return to be null if we cannot locate the app client.
             */
        }
        return result;
    }
    
    /**
     *Returns the URL path for use in launching the app client with the specified
     *archive URI embedded in the application with the specified module ID.
     *@param appModuleID the module ID of the application containing the app client 
     *@param appclientArchiveURI the URI of the app client within the ear
     *@return String containing the URL path (excluding scheme, host, and port) for launching the app client; null if the
     *app client is not eligible for Java Web Start access, or it or the containing
     *app cannot be found
     */
    public String getLaunchURLPath(String appModuleID, String appclientArchiveURI) {
        String result = null;
        try {
            Application app = appsManager.getDescriptor(appModuleID, Thread.currentThread().getContextClassLoader(), false);
            ModuleDescriptor[] moduleDescrs = getEligibleAppclientModuleDescriptors(app);
            /*
             *Search the eligible module descriptors for one with an archive
             *URI that matches the desired one.
             */
            for (ModuleDescriptor m : moduleDescrs) {
                String archiveURI = m.getArchiveUri();
                if (archiveURI != null && archiveURI.equals(appclientArchiveURI)) {
                    result = NestedAppclient.virtualContextRoot(app, m);
                    break;
                }
            }
        } catch (ConfigException ce) {
            /*
             *Allow the return to be null if we cannot locate the app.
             */
        }
        return result;
    }

    /**
     *Returns an array of ModuleDescriptors corresponding to the app clients
     *in this application that are eligible for Java Web Start access.  
     *@param app Application containing (potentially) the nested app clients
     *@return array of ModuleDescriptor objects for eligible app clients
     */
    public static ModuleDescriptor[] getEligibleAppclientModuleDescriptors(Application app) {
        return getEligibleAppclientModuleDescriptors(app, null);
    }
    
    
    /**
     *Returns an array of ModuleDescriptors corresponding to the app clients
     *in this application that are eligible for Java Web Start access. Provides
     *logging if the expected nested ModuleDescriptor(s) are not found.
     *@param Application containing (potentially) the nested app clients
     *@param logger the Logger to use for writing warnings
     *@return array of ModuleDescriptor objects for eligible app clients
     */
    public static ModuleDescriptor[] getEligibleAppclientModuleDescriptors(Application app, Logger logger) {
        Vector<ModuleDescriptor> mds = new Vector<ModuleDescriptor>();

        /*
         *Iterate through the app's app client modules.  For each, get the bundle
         *descriptor and make sure it's an app client descriptor.  If so, get
         *that descriptor's Java Web Start access descriptor.  If that is null, or
         *if it's non-null and the JWS access descriptor says the app client is 
         *eligible for JWS access, add the module descriptor to the collection
         *to be returned.
         */
        for (ApplicationClientDescriptor appclientDescriptor : app.getBundleDescriptors(ApplicationClientDescriptor.class)) {

            JavaWebStartAccessDescriptor jwsAD = appclientDescriptor.getJavaWebStartAccessDescriptor();
            if (jwsAD == null || jwsAD.isEligible()) {
                mds.add(appclientDescriptor.getModuleDescriptor());
            }
        }
        return mds.toArray(new ModuleDescriptor[mds.size()]);
    }
    
    /**
     *Encapsulates logic related to detecing and handling signed copies of the
     *generated app client jar.
     */
    public static class SignedJar {

        private static String SIGNED_JAR_FILENAME_SUFFIX = "-signed";

        /**
         *Returns a File for the signed version of the generated app client jar.
         *@param origin the UserContentOrigin (the app or app client) that owns the jar
         *@param instEnv the environment for this instance
         *@param generatedAppclientFile the generated app client jar file
         *@return File for where the signed generated app client jar should be
         */
        public static File signedGeneratedAppclientJarFile(
                UserContentOrigin origin,
                InstanceEnvironment instEnv,
                File generatedAppclientFile) {
            String regName = origin.getTopLevelRegistrationName();
            ApplicationEnvironment appEnv = instEnv.getApplicationEnvironment(regName);
            File result = new File(appEnv.getJavaWebStartPath(), signedJarPath(generatedAppclientFile.getName()));
            return result;
        }

        /**
         *Returns the path for the signed jar based on the path for the unsigned jar.
         *@param unsignedJarPath path (partial) for the unsigned jar
         *@return the path for the corresponding signed jar
         */
        public static String signedJarPath(String unsignedJarPath) {
//            int lastDot = unsignedJarPath.lastIndexOf('.');
//            if (lastDot == -1) {
//                throw new IllegalArgumentException(unsignedJarPath);
//            }
//            String signedJarPath = unsignedJarPath.substring(0, lastDot) + SIGNED_JAR_FILENAME_SUFFIX + unsignedJarPath.substring(lastDot);
//            return signedJarPath;
            return unsignedJarPath;
        }

        /**
         *Return the java-web-start directory for the given application.
         *@param instEnv InstanceEnvironment for the current instance
         *@param regName the module name of the app for which the j-w-s directory is needed
         *@return the directory file for the app's java-web-start subdirectory
         */
        private static File getAppJWSDir(InstanceEnvironment instEnv, String regName) {
            ApplicationEnvironment appEnv = instEnv.getApplicationEnvironment(regName);
            String appJWSDirPath = appEnv.getJavaWebStartPath();
            File appJWSDir = new File(appJWSDirPath);
            return appJWSDir;
        }
    }
}
