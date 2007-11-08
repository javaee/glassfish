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
package com.sun.enterprise.admin.wsmgmt.repository.impl;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.RelativePathResolver;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.EjbModule;
import com.sun.enterprise.config.serverbeans.WebModule;
import com.sun.enterprise.config.serverbeans.ApplicationHelper;
import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.util.ModuleDescriptor;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.admin.servermgmt.pe.PEFileLayout;
import javax.enterprise.deploy.shared.ModuleType;
import com.sun.enterprise.deployment.io.ApplicationDeploymentDescriptorFile;
import com.sun.enterprise.admin.wsmgmt.repository.spi.WebServiceInfoProvider;
import com.sun.enterprise.admin.wsmgmt.repository.spi.RepositoryProvider;
import com.sun.enterprise.admin.wsmgmt.repository.impl.cache.CacheMgr;
import com.sun.enterprise.admin.wsmgmt.repository.impl.cache.J2eeApplication;
import com.sun.enterprise.admin.wsmgmt.repository.spi.RepositoryException;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;

/**
 * This is the mechanism to provide web service crawling. A RepositoryProvider
 * implementation is a class that extends the RepositoryProvider abstract class.
 * <br>
 * A RepositoryProvider implemented is identified by its fully qualified class
 * name. The default RepositoryProvider is 
 * com.sun.enterprise.admin.repository.spi.impl.ApplicationServerProvider
 */
public class AppServRepositoryProvider implements RepositoryProvider {

    /**
     *  Constructor.
     */
    public AppServRepositoryProvider() {
        if (instanceRoot == null) {
            instanceRoot = System.getProperty(
                SystemPropertyConstants.INSTANCE_ROOT_PROPERTY); 
        }
    }

    /**
     * Returns the unique identifier for this RepositoryProvider object.
     *
     * @return fully qualified class name of this RepositoryProvider
     */
    public String getProviderID() {
        return PROVIDER_ID;
    }

    /**
     * Returns the map of module descriptor locations that contains web service
     * implementation and assciated properties.
     *
     * @return the map of module information
     */
    public Map getWebServiceModules() {
        Map map = new HashMap();

        // admin config context
        ConfigContext configCtx = AdminService.getAdminService().
                        getAdminContext().getAdminConfigContext();

        CacheMgr mgr = CacheMgr.getInstance();

        // j2ee application
        Map apps = mgr.getJ2eeApplications();
        Collection aValues = apps.values();
        for (Iterator iter=aValues.iterator(); iter.hasNext();) {
            J2eeApplication app = (J2eeApplication) iter.next();

            // ejb bundles
            List ejbBundles = app.getEjbBundles();
            if (ejbBundles != null) {
                for (Iterator itr=ejbBundles.iterator(); itr.hasNext();) {
                    String ejb = (String) itr.next();
                    try {
                        Map m = getEjbBundleInfo(configCtx, app.getName(), ejb);
                        map.put(m.get(WebServiceInfoProvider.
                            SUN_EJB_JAR_XML_LOCATION_PROP_NAME), m);
                    } catch (RepositoryException re) { }
                }
            }

            // web bundles 
            List webBundles = app.getWebBundles();
            if (webBundles != null) {
                for (Iterator itr=webBundles.iterator(); itr.hasNext();) {
                    String web = (String) itr.next();
                    try {
                        Map m = getWebBundleInfo(configCtx, app.getName(), web);
                        map.put(m.get(WebServiceInfoProvider.
                            SUN_WEB_XML_LOCATION_PROP_NAME), m);
                    } catch (RepositoryException re) { }
                }
            }
        }

        // stand alone ejb module
        Map ejbs = mgr.getEjbModules();
        Collection eValues = ejbs.values();
        for (Iterator iter=eValues.iterator(); iter.hasNext();) {
            String ejbMod = (String) iter.next();
            try {
                Map m = getEjbModuleInfo(configCtx, ejbMod);
                map.put(m.get(WebServiceInfoProvider.
                    SUN_EJB_JAR_XML_LOCATION_PROP_NAME), m);
            } catch (RepositoryException re) { }
        }

        // stand alone web module
        Map webs = mgr.getWebModules();
        Collection wValues = webs.values();
        for (Iterator iter=wValues.iterator(); iter.hasNext();) {
            String webMod = (String) iter.next();

            try {
                Map m = getWebModuleInfo(configCtx, webMod);
                map.put(m.get(WebServiceInfoProvider.
                    SUN_WEB_XML_LOCATION_PROP_NAME), m);
            } catch (RepositoryException re) { }
        }

        return map;
    }

    /**
     * Returns alt-dd or null for the bundle. 
     * 
     * @param  bundleName name of the bundle
     * @param  appXML  path to the application.xml
     * @param  ejbBundle true if an ejb bundle
     */
    private String getAltDD(String bundleName, String appXML, 
            boolean ejbBundle) {

        String altDD = null;
        FileInputStream fis = null;
        try {
            // abort if application.xml file does not exist
            File f = new File(appXML);
            if (!f.exists() || (bundleName==null)) {
                return null;
            }

            ApplicationDeploymentDescriptorFile addf = 
                new ApplicationDeploymentDescriptorFile();
            fis = new FileInputStream(f);
            Application app = (Application) addf.read(fis);
            for (Iterator itr=app.getModules();itr.hasNext();) {
                ModuleDescriptor md = (ModuleDescriptor) itr.next();
                String uri = md.getArchiveUri();
                if (bundleName.equals(uri)) {
                    altDD = md.getAlternateDescriptor();
                    break;
                }
            }
        } catch (Exception e) {
            _logger.log(Level.FINE, "Error while reading alt-dd", e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e) { }
            }
        }

        return altDD;
    }

    /**
     * Returns path information for an ejb bundle. 
     * This is an embedded module within an application.
     * 
     * @param  ctx  config context
     * @param  name name of the application
     * @param  bundleName  ejb bundle name
     *
     * @return  path information for an ejb bundle
     */
    private Map getEjbBundleInfo(ConfigContext ctx, String name, 
            String bundleName) throws RepositoryException {
            
        // validate mandatory arguments
        if ( (bundleName == null) || (ctx == null) || (name == null) ) {
            throw new IllegalArgumentException();
        }

        Map map = new HashMap();

        try {
            // module type is EJB
            map.put(WebServiceInfoProvider.MOD_TYPE_PROP_NAME,
                            WebServiceInfoProvider.MOD_TYPE_EJB);

            // application id
            map.put(WebServiceInfoProvider.APP_ID_PROP_NAME, name);

            // bundle name
            map.put(WebServiceInfoProvider.BUNDLE_NAME_PROP_NAME, bundleName);

            // APP_ROOT_LOCATION is not used
            // config bean
            //com.sun.enterprise.config.serverbeans.J2eeApplication app = 
            //    (com.sun.enterprise.config.serverbeans.J2eeApplication)
            //    ApplicationHelper.findApplication(ctx, name);
            //
            // applications/j2ee-applications/appName
            //String appRoot =
            //    RelativePathResolver.resolvePath(app.getLocation());
            //map.put(WebServiceInfoProvider.APP_ROOT_LOCATION_PROP_NAME, 
            //        appRoot);

            // generated/xml/j2ee-applications/<app-name>
            String xmlDir = instanceRoot
                + File.separator + PEFileLayout.GENERATED_DIR
                + File.separator + PEFileLayout.XML_DIR
                + File.separator + PEFileLayout.J2EE_APPS_DIR
                + File.separator + name;

            // generated/xml/j2ee-applications/<app-name>/bundleName
            String bundleRoot = xmlDir + File.separator 
                       + FileUtils.makeFriendlyFileName(bundleName); 
            map.put(WebServiceInfoProvider.BUNDLE_ROOT_LOCATION_PROP_NAME, 
                    bundleRoot);

            String ejbXML    = null;
            String sunEjbXML = null;
            
            // generated/xml/j2ee-applications/appName/META-INF/application.xml
            String appXML = xmlDir +File.separator + META_INF
                          + File.separator + APPLICATION_XML;
            map.put(WebServiceInfoProvider.APPLICATION_XML_PROP_NAME, 
                    getFormattedFileContents(appXML));
            String altDD = getAltDD(bundleName, appXML, true);

            // no alt DD
            if (altDD == null) {
                ejbXML = bundleRoot + File.separator + META_INF
                       + File.separator + EJB_JAR_XML;

                sunEjbXML = bundleRoot + File.separator + META_INF
                          + File.separator + SUN_EJB_JAR_XML;
            } else {
                ejbXML = xmlDir + File.separator + altDD;
                sunEjbXML = xmlDir + File.separator + SUN_DASH
                          + new File(ejbXML).getName();
            }

            
            // generated/xml/j2ee-applications/appName/bundleName/META-INF/ejb-jar.xml
            map.put(WebServiceInfoProvider.EJB_JAR_XML_PROP_NAME, 
                    getFormattedFileContents(ejbXML));
            
            // generated/xml/j2ee-applications/appName/bundleName/META-INF/sun-ejb-jar.xml
            map.put(WebServiceInfoProvider.SUN_EJB_JAR_XML_LOCATION_PROP_NAME, 
                    sunEjbXML);
            map.put(WebServiceInfoProvider.SUN_EJB_JAR_XML_PROP_NAME, 
                    getFormattedFileContents(sunEjbXML));

            // generated/xml/j2ee-applications/appName/bundleName/META-INF/webservices.xml
            String wsXML =  bundleRoot + File.separator 
                         + META_INF + File.separator + WEBSERVICES_XML;
            map.put(WebServiceInfoProvider.WS_XML_LOCATION_PROP_NAME, wsXML);
            map.put(WebServiceInfoProvider.WS_XML_PROP_NAME,
            getFormattedFileContents(wsXML));

        } catch (Exception e) {
            throw new RepositoryException(e);
        }

        return map;
    }

    /**
     * Returns the path information for a web bundle. 
     * This is an embedded module within an application.
     *
     * @param  ctx  config context
     * @param  name  name of the application
     * @param  bundleName  name of the web bundle
     *
     * @return  path information for a web bundle
     */
    private Map getWebBundleInfo(ConfigContext ctx, String name, 
            String bundleName) throws RepositoryException {

        // validate mandatory arguments
        if ( (bundleName == null) || (ctx == null) || (name == null) ) {
            throw new IllegalArgumentException();
        }

        Map map = new HashMap();

        try {
            // module type is WEB
            map.put(WebServiceInfoProvider.MOD_TYPE_PROP_NAME,
                            WebServiceInfoProvider.MOD_TYPE_WEB);

            // application id
            map.put(WebServiceInfoProvider.APP_ID_PROP_NAME, name);

            // bundle name 
            map.put(WebServiceInfoProvider.BUNDLE_NAME_PROP_NAME, bundleName);

            // APP_ROOT_LOCATION is not used
            // config bean
            //com.sun.enterprise.config.serverbeans.J2eeApplication app = 
            //    (com.sun.enterprise.config.serverbeans.J2eeApplication)
            //    ApplicationHelper.findApplication(ctx, name);
            //
            // applications/j2ee-applications/appName
            //String appRoot =
            //    RelativePathResolver.resolvePath(app.getLocation());
            //map.put(WebServiceInfoProvider.APP_ROOT_LOCATION_PROP_NAME, 
            //        appRoot);

            // generated/xml/j2ee-applications/<app-name>
            String xmlDir = instanceRoot
                + File.separator + PEFileLayout.GENERATED_DIR
                + File.separator + PEFileLayout.XML_DIR
                + File.separator + PEFileLayout.J2EE_APPS_DIR
                + File.separator + name;

            // generated/xml/j2ee-applications/<app-name>/bundleName
            String bundleRoot = xmlDir + File.separator 
                       + FileUtils.makeFriendlyFileName(bundleName); 
            map.put(WebServiceInfoProvider.BUNDLE_ROOT_LOCATION_PROP_NAME, 
                    bundleRoot);

            String webXML    = null;
            String sunWebXML = null;
            
            // generated/xml/j2ee-applications/appName/META-INF/application.xml
            String appXML = xmlDir +File.separator + META_INF
                          + File.separator + APPLICATION_XML;
            map.put(WebServiceInfoProvider.APPLICATION_XML_PROP_NAME, 
                    getFormattedFileContents(appXML));
            String altDD = getAltDD(bundleName, appXML, false);

            // no alt DD
            if (altDD == null) {
                webXML = bundleRoot + File.separator + WEB_INF
                       + File.separator + WEB_XML;

                sunWebXML = bundleRoot + File.separator + WEB_INF
                          + File.separator + SUN_WEB_XML;
            } else {
                webXML = xmlDir + File.separator + altDD;
                sunWebXML = xmlDir + File.separator + SUN_DASH
                          + new File(webXML).getName();
            }

            // generated/xml/j2ee-applications/appName/bundleName/WEB-INF/web.xml
            map.put(WebServiceInfoProvider.WEB_XML_PROP_NAME, 
                    getFormattedFileContents(webXML));

            // generated/xml/j2ee-applications/appName/bundleName/WEB-INF/sun-web.xml
            map.put(WebServiceInfoProvider.SUN_WEB_XML_LOCATION_PROP_NAME, 
                    sunWebXML);
            map.put(WebServiceInfoProvider.SUN_WEB_XML_PROP_NAME, 
                    getFormattedFileContents(sunWebXML));

            // generated/xml/j2ee-applications/appName/bundleName/WEB-INF/webservices.xml
            String wsXML = bundleRoot +  File.separator + WEB_INF
                         + File.separator + WEBSERVICES_XML;
            map.put(WebServiceInfoProvider.WS_XML_LOCATION_PROP_NAME, wsXML);
            map.put(WebServiceInfoProvider.WS_XML_PROP_NAME,
            getFormattedFileContents(wsXML));

        } catch (Exception e) {
            throw new RepositoryException(e);
        }

        return map;
    }

    /**
     * Returns path information for a stand alone ejb module. 
     *
     * @param  ctx  config context
     * @param  name  name of the ejb module
     *
     * @return  path information for an ejb module
     */
    private Map getEjbModuleInfo(ConfigContext ctx, String name) 
            throws RepositoryException {
            
        // validate mandatory arguments
        if ( (ctx == null) || (name == null) ) {
            throw new IllegalArgumentException();
        }

        Map map = new HashMap();

        try {
            // module type is EJB
            map.put(WebServiceInfoProvider.MOD_TYPE_PROP_NAME,
                            WebServiceInfoProvider.MOD_TYPE_EJB);

            // ejb module id
            map.put(WebServiceInfoProvider.APP_ID_PROP_NAME, name);

            // bundle name is empty
            map.put(WebServiceInfoProvider.BUNDLE_NAME_PROP_NAME, null);

            // APP_ROOT_LOCATION is not used
            // ejb module config bean
            //EjbModule ejbMod = (EjbModule)
            //    ApplicationHelper.findApplication(ctx, name);
            //
            // applications/j2ee-modules/moduleName
            //String ejbModuleRoot = 
            //    RelativePathResolver.resolvePath(ejbMod.getLocation());
            //map.put(WebServiceInfoProvider.APP_ROOT_LOCATION_PROP_NAME, 
            //        ejbModuleRoot);

            // generated/xml/j2ee-modules/moduleName
            String xmlDir = instanceRoot
                          + File.separator + PEFileLayout.GENERATED_DIR
                          + File.separator + PEFileLayout.XML_DIR 
                          + File.separator + PEFileLayout.J2EE_MODULES_DIR  
                          + File.separator + name;
            map.put(WebServiceInfoProvider.BUNDLE_ROOT_LOCATION_PROP_NAME, 
                    xmlDir);

            // generated/xml/j2ee-modules/moduleName/META-INF/ejb-jar.xml
            String ejbXML = xmlDir + File.separator + META_INF
                          + File.separator + EJB_JAR_XML;
            map.put(WebServiceInfoProvider.
                EJB_JAR_XML_PROP_NAME, getFormattedFileContents(ejbXML));

            // generated/xml/j2ee-modules/moduleName/META-INF/sun-ejb-jar.xml
            String sunEjbXML = xmlDir + File.separator + META_INF 
                              + File.separator + SUN_EJB_JAR_XML;
            map.put(WebServiceInfoProvider.SUN_EJB_JAR_XML_LOCATION_PROP_NAME, 
                    sunEjbXML);
            map.put(WebServiceInfoProvider.
                SUN_EJB_JAR_XML_PROP_NAME, getFormattedFileContents(sunEjbXML));

            // generated/xml/j2ee-modules/moduleName/META-INF/webservices.xml
            String wsXML = xmlDir + File.separator +  META_INF 
                         + File.separator + WEBSERVICES_XML;
            map.put(WebServiceInfoProvider.WS_XML_LOCATION_PROP_NAME, wsXML);
            map.put(WebServiceInfoProvider.WS_XML_PROP_NAME,
            getFormattedFileContents(wsXML));

        } catch (Exception e) {
            throw new RepositoryException(e);
        }

        return map;
    }

    /**
     * Returns path information for a stand alone web module. 
     *
     * @param  ctx  config context
     * @param  name  name of the web module
     *
     * @return  path information for an web module
     */
    private Map getWebModuleInfo(ConfigContext ctx, String name) 
            throws RepositoryException {

        // validate mandatory arguments
        if ( (ctx == null) || (name == null) ) {
            throw new IllegalArgumentException();
        }

        Map map = new HashMap();

        try {
            // module type is WEB
            map.put(WebServiceInfoProvider.MOD_TYPE_PROP_NAME,
                            WebServiceInfoProvider.MOD_TYPE_WEB);

            // application id
            map.put(WebServiceInfoProvider.APP_ID_PROP_NAME, name);

            // bundle name
            map.put(WebServiceInfoProvider.BUNDLE_NAME_PROP_NAME, null);

            // APP_ROOT_LOCATION is not used
            // config bean
            //WebModule webapp = (WebModule)
            //    ApplicationHelper.findApplication(ctx, name);
            //
            // applications/j2ee-modules/moduleName
            //String webModuleRoot = 
            //    RelativePathResolver.resolvePath(webapp.getLocation());
            //map.put(WebServiceInfoProvider.APP_ROOT_LOCATION_PROP_NAME, 
            //        webModuleRoot);

            // generated/xml/j2ee-modules/moduleName
            String xmlDir = instanceRoot
                          + File.separator + PEFileLayout.GENERATED_DIR
                          + File.separator + PEFileLayout.XML_DIR
                          + File.separator + PEFileLayout.J2EE_MODULES_DIR
                          + File.separator + name;
            map.put(WebServiceInfoProvider.BUNDLE_ROOT_LOCATION_PROP_NAME, 
                    xmlDir);

            // generated/xml/j2ee-modules/moduleName/WEB-INF/web.xml
            String webXML = xmlDir + File.separator + WEB_INF
                          + File.separator + WEB_XML;
            map.put(WebServiceInfoProvider.WEB_XML_PROP_NAME,
            getFormattedFileContents(webXML));

            // generated/xml/j2ee-modules/moduleName/WEB-INF/sun-web.xml
            String sunWebXML = xmlDir + File.separator + WEB_INF
                             + File.separator + SUN_WEB_XML;
            map.put(WebServiceInfoProvider.SUN_WEB_XML_LOCATION_PROP_NAME, 
                    sunWebXML);
            map.put(WebServiceInfoProvider.
                SUN_WEB_XML_PROP_NAME, getFormattedFileContents(sunWebXML));

            // generated/xml/j2ee-modules/moduleName/WEB-INF/webservices.xml
            String wsXML = xmlDir +  File.separator + WEB_INF
                         + File.separator + WEBSERVICES_XML;
            map.put(WebServiceInfoProvider.WS_XML_LOCATION_PROP_NAME, wsXML);
            map.put(WebServiceInfoProvider.WS_XML_PROP_NAME,
            getFormattedFileContents(wsXML));

        } catch (Exception e) {
            throw new RepositoryException(e);
        }

        return map;
    }

    private String getFormattedFileContents(String filePath) {

		String str = null;
        try {
            str = FileUtils.getFileContents(filePath);
        } catch (Exception e) {
            _logger.log(Level.FINE, "Error reading dd file contents", e);
        }

        return str;
    }

    // ---- VARIABLES - PRIVATE -----------------------------------------
    private static final String WEB_INF         = "WEB-INF";
    private static final String META_INF        = "META-INF";
    private static final String WEBSERVICES_XML = "webservices.xml";
    private static final String WEB_XML         = "web.xml";
    private static final String SUN_WEB_XML     = "sun-web.xml";
    private static final String EJB_JAR_XML     = "ejb-jar.xml";
    private static final String SUN_EJB_JAR_XML = "sun-ejb-jar.xml";
    private static final String APPLICATION_XML = "application.xml";
    private static final String SUN_DASH        = "sun-";
    private static String instanceRoot          = null;
    private static Logger _logger = Logger.getLogger(LogDomains.ADMIN_LOGGER);

    /** provider id for the default RepositoryProvider */
    public static final String PROVIDER_ID = 
    "com.sun.enterprise.admin.wsmgmt.repository.impl.AppServRepositoryProvider";
}
