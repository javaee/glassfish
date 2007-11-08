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
package com.sun.enterprise.ee.admin.lbadmin.reader.impl;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import javax.enterprise.deploy.shared.ModuleType;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.WebModule;
import com.sun.enterprise.config.serverbeans.J2eeApplication;
import com.sun.enterprise.config.serverbeans.ApplicationHelper;
import com.sun.enterprise.config.serverbeans.PropertyResolver;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.deployment.util.ModuleDescriptor;
import com.sun.enterprise.deployment.io.ApplicationDeploymentDescriptorFile;
import com.sun.enterprise.deployment.deploy.shared.FileArchive;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.ee.admin.lbadmin.reader.api.LocationHelper;
import com.sun.enterprise.admin.servermgmt.pe.PEFileLayout;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.ee.EELogDomains;

/**
 * This class encapsulates path to the sun-web.xml deployment 
 * descriptors. For a given application or stand alone web 
 * module, this class returns the fully qualified path(s)
 * to the deployment descriptors.
 *
 * @author  Nazrul Islam
 * @since   JDK1.4
 */
public class LocationHelperImpl implements LocationHelper {

    /**
     * Constructor!
     *
     * @param  ctx  config context
     */
    public LocationHelperImpl(ConfigContext ctx) {
        _ctx = ctx;
        _dasName = System.getProperty(SystemPropertyConstants.SERVER_NAME);
    }

    /**
     * Returns the list of file paths for sun-web.xml(s).
     *
     * @param moduleName       Name of the module
     * 
     * @return path to the sun-web.xml file for this stand alone web application
     */
    public String getSunWebXmlPathForModule(String moduleName) {

        String path = null;

        try {

            ConfigBean bean = 
                ApplicationHelper.findApplication(_ctx, moduleName);

            if ((bean != null) && (bean instanceof WebModule)) {

                String loc = ((WebModule) bean).getLocation();

                // path to the sun-web.xml file
                String instanceRoot = System.getProperty(
                        SystemPropertyConstants.INSTANCE_ROOT_PROPERTY);
                File generated = new File(instanceRoot,PEFileLayout.GENERATED_DIR);
                File xml = new File(generated,PEFileLayout.XML_DIR);
                File j2eeModules = new File(xml, PEFileLayout.J2EE_MODULES_DIR);
                File moduleRoot = new File(j2eeModules, moduleName);
                String modulePath = moduleRoot.getAbsolutePath();

                // path to the sun-web.xml file
                File f = new File(modulePath, 
                    WEB_INF+File.separator+SUN_WEB_DD);

                path = f.getAbsolutePath();
            }
        } catch (Exception e) {
            _logger.log(Level.FINE, 
                "Unexpected error while getting path to sun-web.xml", e);
        }
        
        _logger.fine("Returning stand alone web-app path: " + path);

        return path;
    }

    /**
     * Returns a list of path to the sun-web.xml deployment descriptors.
     *
     * @param   applicationName   name of the application
     * @return  list of path to the sun-web.xml deployment descriptors
     */
    public List getSunWebXmlPathForApplication(String applicationName) {

        List list = new ArrayList();

        try {
            ConfigBean bean = 
                ApplicationHelper.findApplication(_ctx, applicationName);

            if (bean != null) {
                if (bean instanceof J2eeApplication) {

                    // application repository root
                    String loc = ((J2eeApplication) bean).getLocation();
                    PropertyResolver pr = new PropertyResolver(_ctx, _dasName);
                    String rLocation = pr.resolve(loc);

                    // opening the application.xml descriptor 
                    FileArchive in = new FileArchive();
                    in.open(rLocation);
                    ApplicationDeploymentDescriptorFile rootDD = 
                        new ApplicationDeploymentDescriptorFile();
                    Application application = 
                        (Application) rootDD.read(null, in);

                    // all web modules in the J2EE application
                    for (Iterator modules=application.getModules();
                        modules.hasNext();) {

                        ModuleDescriptor aModule = 
                            (ModuleDescriptor) modules.next();

                        if (!((aModule.getModuleType())
                                .equals(ModuleType.WAR))) {

                            _logger.fine("Skipping non web module descriptor: "
                                + aModule.getModuleType());

                            continue;
                        }

                        // alt dd
                        if (aModule.getAlternateDescriptor()!=null) {

                            File f = new File(aModule.getAlternateDescriptor());

                            // Get the path of the parent of alt dd file.
                            // add this path to total path, if this is not
                            // null.
                            String parent = f.getParent();

                            if ( parent == null) {
                                parent = "";
                            }


                            // path to the sun-xxx.xml file
                            String instanceRoot = System.getProperty(
                                    SystemPropertyConstants.INSTANCE_ROOT_PROPERTY);
                            File generated = new File(instanceRoot,PEFileLayout.GENERATED_DIR);
                            File xml = new File(generated,PEFileLayout.XML_DIR);
                            File j2eeApps = new File(xml, PEFileLayout.J2EE_APPS_DIR);
                            File generatedAppRoot = new File(j2eeApps, applicationName);
                            String generatedAppPath = generatedAppRoot.getAbsolutePath();

                            File altWebapp = new File (generatedAppPath, 
                                parent+File.separator
                                +SUN_PREFIX+f.getName());

                            if (altWebapp.exists()) {
                                _logger.fine("Adding alt-web-app path " 
                                    + altWebapp.getAbsolutePath());

                                // adding to the list
                                list.add( altWebapp.getAbsolutePath() );
                            } else {
                                _logger.fine("Invalid path: " 
                                    + altWebapp.getAbsolutePath());
                            }

                        } else { // no alt dd

                            String aUri = aModule.getArchiveUri();

                            if (aUri != null) {
                                
                                String instanceRoot = System.getProperty(
                                        SystemPropertyConstants.INSTANCE_ROOT_PROPERTY);
                                File generated = new File(instanceRoot,PEFileLayout.GENERATED_DIR);
                                File xml = new File(generated,PEFileLayout.XML_DIR);
                                File j2eeApps = new File(xml, PEFileLayout.J2EE_APPS_DIR);
                                File module = new File(j2eeApps, applicationName);
                                String modulePath = module.getAbsolutePath();

                                File webapp = new File (modulePath, 
                                    FileUtils.makeFriendlyFileName(aUri)
                                    + File.separator + WEB_INF 
                                    + File.separator + SUN_WEB_DD);

                                if (webapp.exists()) {
                                    _logger.fine("Adding web-app path " 
                                        + webapp.getAbsolutePath());

                                    // adding to the list
                                    list.add( webapp.getAbsolutePath() );
                                } else {
                                    // path to the sun-web.xml file
                                    webapp = new File(rLocation,
                                            FileUtils.makeFriendlyFileName(aUri)
                                            + File.separator + WEB_INF
                                            + File.separator + SUN_WEB_DD);
                                    
                                    if (webapp.exists()) {
                                        _logger.fine("Adding web-app path "
                                                + webapp.getAbsolutePath());
                                        
                                        // adding to the list
                                        list.add( webapp.getAbsolutePath() );
                                    } else {
                                    _logger.fine("Invalid path: " 
                                        + webapp.getAbsolutePath());
                                    }
                                }
                            }
                        }
                    }
                } else {
                    _logger.fine("Not a J2EE: " + applicationName);
                }
            } else {
                _logger.fine("Application not found in server configuration " 
                    + applicationName);
            }
        } catch (Exception e) {
            _logger.log(Level.FINE, 
                "Unexpected error while getting path to sun-web.xml", e);
        }

        return list;
    }
    public List getSunEjbJarXmlPathForApplication(String applicationName) {

        List list = new ArrayList();

        try {
            ConfigBean bean = 
                ApplicationHelper.findApplication(_ctx, applicationName);

            if (bean != null) {
                if (bean instanceof J2eeApplication) {

                    // application repository root
                    String loc = ((J2eeApplication) bean).getLocation();
                    PropertyResolver pr = new PropertyResolver(_ctx, _dasName);
                    String rLocation = pr.resolve(loc);

                    // opening the application.xml descriptor 
                    FileArchive in = new FileArchive();
                    in.open(rLocation);
                    ApplicationDeploymentDescriptorFile rootDD = 
                        new ApplicationDeploymentDescriptorFile();
                    Application application = 
                        (Application) rootDD.read(null, in);

                    // all web modules in the J2EE application
                    for (Iterator modules=application.getModules();
                        modules.hasNext();) {

                        ModuleDescriptor aModule = 
                            (ModuleDescriptor) modules.next();

                        if (!((aModule.getModuleType())
                                .equals(ModuleType.EJB))) {

                            _logger.fine("Skipping non ejb jar module descriptor: "
                                + aModule.getModuleType());

                            continue;
                        }

                        // alt dd
                        if (aModule.getAlternateDescriptor()!=null) {

                            File f = new File(aModule.getAlternateDescriptor());

                            // Get the path of the parent of alt dd file.
                            // add this path to total path, if this is not
                            // null.
                            String parent = f.getParent();

                            if ( parent == null) {
                                parent = "";
                            }


                            // path to the sun-xxx.xml file
                            String instanceRoot = System.getProperty(
                                    SystemPropertyConstants.INSTANCE_ROOT_PROPERTY);
                            File generated = new File(instanceRoot,PEFileLayout.GENERATED_DIR);
                            File xml = new File(generated,PEFileLayout.XML_DIR);
                            File j2eeApps = new File(xml, PEFileLayout.J2EE_APPS_DIR);
                            File generatedAppRoot = new File(j2eeApps, applicationName);
                            String generatedAppPath = generatedAppRoot.getAbsolutePath();

                            File altWebapp = new File (generatedAppPath, 
                                parent+File.separator
                                +SUN_PREFIX+f.getName());

                            if (altWebapp.exists()) {
                                _logger.fine("Adding alt-web-app path " 
                                    + altWebapp.getAbsolutePath());

                                // adding to the list
                                list.add( altWebapp.getAbsolutePath() );
                            } else {
                                _logger.fine("Invalid path: " 
                                    + altWebapp.getAbsolutePath());
                            }

                        } else { // no alt dd

                            String aUri = aModule.getArchiveUri();

                            if (aUri != null) {
                                
                                String instanceRoot = System.getProperty(
                                        SystemPropertyConstants.INSTANCE_ROOT_PROPERTY);
                                File generated = new File(instanceRoot,PEFileLayout.GENERATED_DIR);
                                File xml = new File(generated,PEFileLayout.XML_DIR);
                                File j2eeApps = new File(xml, PEFileLayout.J2EE_APPS_DIR);
                                File module = new File(j2eeApps, applicationName);
                                String modulePath = module.getAbsolutePath();

                                File ejbapp = new File (modulePath, 
                                    FileUtils.makeFriendlyFileName(aUri)
                                    + File.separator + META_INF 
                                    + File.separator + SUN_EJB_JAR_DD);

                                if (ejbapp.exists()) {
                                    _logger.fine("Adding ejb-app path " 
                                        + ejbapp.getAbsolutePath());

                                    // adding to the list
                                    list.add( ejbapp.getAbsolutePath() );
                                } else {
                                    // path to the sun-web.xml file
                                    ejbapp = new File(rLocation,
                                            FileUtils.makeFriendlyFileName(aUri)
                                            + File.separator + META_INF
                                            + File.separator + SUN_EJB_JAR_DD);
                                    
                                    if (ejbapp.exists()) {
                                        _logger.fine("Adding ejb-app path "
                                                + ejbapp.getAbsolutePath());
                                        
                                        // adding to the list
                                        list.add( ejbapp.getAbsolutePath() );
                                    } else {
                                    _logger.fine("Invalid path: " 
                                        + ejbapp.getAbsolutePath());
                                    }
                                }
                            }
                        }
                    }
                } else {
                    _logger.fine("Not a J2EE: " + applicationName);
                }
            } else {
                _logger.fine("Application not found in server configuration " 
                    + applicationName);
            }
        } catch (Exception e) {
            _logger.log(Level.FINE, 
                "Unexpected error while getting path to sun-ejb-jar.xml", e);
        }

        return list;
    }

    // ---- VARIABLE(S) - PRIVATE -----------------------------------------
    private ConfigContext _ctx              = null;
    private String _dasName                 = null;
    private static Logger _logger           = 
        Logger.getLogger(EELogDomains.EE_ADMIN_LOGGER);
    private static final String WEB_INF     = "WEB-INF";
    private static final String META_INF     = "META-INF";
    private static final String SUN_WEB_DD  = "sun-web.xml";
    private static final String SUN_EJB_JAR_DD  = "sun-ejb-jar.xml";
    private static final String SUN_PREFIX  = "sun-";
}
