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


package com.sun.enterprise.jbi.serviceengine.install;

import com.sun.enterprise.jbi.serviceengine.ServiceEngineException;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.logging.LogDomains;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.ObjectName;



/**
 * Installs Java EE Service Engine on a CAS installation. In openESB world, an
 * instance could either be a CAS or a non CAS ESB member. Java EE Servijece Engine
 * is installed on a instance only if the instance is CAS. All components
 * installed on CAS are installed on a non CAS ESB member by the CAS.
 * @author Manishaa Umbar
 */
public class InstallerImpl implements Installer {
    
        
    /**
     * addComponent operation of ESB installation MBean adds the component
     * to ESB's registry and repository. By adding a component, component becomes
     * installable.
     */
    private static final String ADD_COMPONENT_OPERATION="addComponent";
    
    /**
     * Install a already added component in ESB environment
     */
    private static final String INSTALL_COMPONENT_OPERATION="installComponent";
    
    /**
     * removeComponent operation of ESB installation MBean removes the component
     * from ESB's registry and repository.
     */
    private static final String REMOVE_COMPONENT_OPERATION="removeComponent";
    
    /**
     * UnInstalls  already added component in ESB environment. By uninstalling a component,
     * component becomes removable from the registry and repository of CAS.
     */
    private static final String UNINSTALL_COMPONENT_OPERATION="uninstallComponent";
    
    /**
     * Starts the component
     */
    private static final String START_COMPONENT_OPERATION="startComponent";
    
    /**
     * Stops the component
     */
    private static final String STOP_COMPONENT_OPERATION="stopComponent";
    
    /**
     * Checks if an engine with provided name exists in openESB .
     * This operation is used to find out if Service Engine is installed or not
     */
    private static final String IS_ENGINE_OPERATION="isEngine";
    
    /**
     *Determines whether the jbi instance is CAS instance
     */
    private static final String IS_CAS_OPERATION = "getIsCentralAdminServer";
    
    private String componentName = null;
    private String SE_BUNDLE = "lib/addons/jbi/appserv-jbise.jar";
    
    private String JBI_INSTANCE_NAME="JBI_INSTANCE_NAME";
    private String JBI_FOLDER = "jbi";
    private String JBI_ENV_PROPERTIES = File.separatorChar +
            JBI_FOLDER + File.separatorChar + "config" + File.separatorChar +
            "jbienv.properties";
    
    
    private MBeanHelper mbeanHelper;
    
    private String jbiInstanceName;
    
    private boolean casInstallation;
    private boolean jbiInstalled = false;
    
    /**
     * Internal handle to the logger instance
     */
    private static Logger logger =
            LogDomains.getLogger(LogDomains.SERVER_LOGGER);
    
    /** Creates a new instance of CASInstallerImpl */
    public InstallerImpl(MBeanHelper mbeanHelper) {
        this.mbeanHelper = mbeanHelper;
        String installationRoot =
                ApplicationServer.getServerContext().getInstallRoot();
        String jbiInstallationDir = installationRoot + File.separator + JBI_FOLDER;
        jbiInstalled = (new File(jbiInstallationDir).exists() &&
                (getJBIInstanceName() != null));
        if(jbiInstalled)
            casInstallation = isCASInstallation();
    }
    
    public boolean isJBIInstalled() {
        return jbiInstalled;
    }
    
    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }
    
    
    /**
     * Installs the service engine and starts it
     * @param zipFilePath packaged 208 compliant service engine bundle
     *
     */
    public String install(String zipFilePath) throws ServiceEngineException {
        //Use InstallationServiceMBean to install new component
        String result = null;
        if(zipFilePath == null) {
            zipFilePath = getServiceEngineBundle();
            if(casInstallation) {
                log(Level.FINE, "Java EE Service Engine Bundle :" , zipFilePath);
                try {
                    
                    ObjectName objName = mbeanHelper.getObjectName(
                            MBeanHelper.ESB_INSTALLATION_SERVICE);
                    
                    log(Level.FINEST, "installation_service_log_name" , objName.toString());
                    
                    result = (String)mbeanHelper.invokeMBeanOperation(objName,
                            ADD_COMPONENT_OPERATION, new Object[]{zipFilePath},
                            new String[] {"java.lang.String"});
                            
                    log(Level.FINEST, " Status of addComponent ", result );
                            
                    result = (String)mbeanHelper.invokeMBeanOperation(objName,
                                    INSTALL_COMPONENT_OPERATION,
                                    new Object[]{componentName,
                                            java.util.Arrays.asList(new String[]{jbiInstanceName}),
                                                    new Properties()},
                                    new String[] {"java.lang.String", "java.util.List","java.util.Properties"});
                                    
                    log(Level.FINEST, " Status of installComponent ", result );
                } catch(Exception e) {
                    log(Level.SEVERE,
                            "Error occurred during installation of Java EE Service Engine",
                            e.getMessage());
                }
            }
        }
        return result;
        
    }
    
    /**
     * Starts the component with provided name
     */
    public void start() throws ServiceEngineException {
        if(casInstallation) {
            try {
                ObjectName objName = mbeanHelper.getObjectName(
                        MBeanHelper.ESB_LIFECYCLE_SERVICE);
                
                log(Level.FINEST, "lifecycle_service_obj_name" , objName.toString());
                
                String result = (String)mbeanHelper.invokeMBeanOperation(objName,
                        START_COMPONENT_OPERATION, new Object[]{componentName},
                        new String[] {"java.lang.String"});
                        log(Level.FINEST, "Start Component Status", result);
            } catch(Exception e) {
                log(Level.SEVERE,
                        "Error occurred during startup of Java EE Service Engine",
                        e.getMessage());
            }
        }
    }
    
    
    /**
     * Checks if the compoenent specified by componentName is installed or
     * not
     */
    public boolean isComponentInstalled() {
        if(casInstallation) {
            try {
                ObjectName objName = mbeanHelper.getObjectName(MBeanHelper.ESB_INSTALLATION_SERVICE);
                String result = (String)mbeanHelper.invokeMBeanOperation(objName,
                                "getComponentInfo",
                                new Object[]{componentName},
                                new String[] {"java.lang.String"});
                if(result == null || result.trim().length() == 0) {
                    return false;
                }
                /*
                String domainDir = ApplicationServer.getServerContext().getInstanceEnvironment().getInstancesRoot();
                String fs = File.separator;
                String javaeeSEDir = domainDir + fs + "jbi" + fs + "engines" + fs + "JavaEEServiceEngine";
                if(!(new File(javaeeSEDir).exists())) {
                    return false;
                }
                */
            } catch(Exception e) {
                log(Level.WARNING, "Exception occurred while getting component by name", e.getMessage());
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Stops the component with provided name
     */
    public void stop() throws ServiceEngineException {
        if(casInstallation) {
            try {
                
                ObjectName objName = mbeanHelper.getObjectName(
                        MBeanHelper.ESB_LIFECYCLE_SERVICE);
                
                log(Level.FINEST, "lifecycle_service_obj_name" , objName.toString());
                
                String result = (String)mbeanHelper.invokeMBeanOperation(objName,
                        STOP_COMPONENT_OPERATION, new Object[]{componentName},
                        new String[] {"java.lang.String"});
                        log(Level.FINEST, "Start Component Status", result);
            } catch(Exception e) {
                log(Level.SEVERE,
                        "Error occurred during stopping of Java EE Service Engine",
                        e.getMessage());
            }
        }
        
    }
    
    /**
     * Uninstalls the component with provided name
     */
    public void uninstall() throws ServiceEngineException {
        if(casInstallation) {
            try {
                
                ObjectName objName = mbeanHelper.getObjectName(
                        MBeanHelper.ESB_INSTALLATION_SERVICE);
                
                log(Level.FINEST, "installation_service_log_name" , objName.toString());
                
                String result = (String)mbeanHelper.invokeMBeanOperation(objName,
                        UNINSTALL_COMPONENT_OPERATION, new Object[]{componentName},
                        new String[] {"java.lang.String"});
                        
                        log(Level.FINEST, " Status of uninstallComponent ", result );
                        
                        result = (String)mbeanHelper.invokeMBeanOperation(objName,
                                REMOVE_COMPONENT_OPERATION,
                                new Object[]{componentName},
                                new String[] {"java.lang.String"});
                                
                                log(Level.FINEST, " Status of removeComponent ", result );
            } catch(Exception e) {
                log(Level.SEVERE,
                        "Error occurred during uninstallation of Java EE Service Engine",
                        e.getMessage());
            }
        }
    }
    
    private boolean isCASInstallation() {
        try {
            
            ObjectName objName = mbeanHelper.getObjectName(jbiInstanceName,
                    MBeanHelper.FRAMEWORK);
            log(Level.FINEST, "Framework MBean Object" , objName.toString());
            
            Boolean result = (Boolean)mbeanHelper.invokeMBeanOperation(objName,
                    IS_CAS_OPERATION, null,new String[] {});
                    
            return result.booleanValue();
        } catch(Exception e) {
            log(Level.SEVERE,
                    "Error occurred during checking if the instance is CAS or not",
                    e.getMessage());
        }
        //Should never reach here
        return false;
    }
    
    private String getJBIInstanceName() {
        if(jbiInstanceName == null) {
            try{
                String installRoot = ApplicationServer.getServerContext().getInstallRoot();
                String jbiEnvProperties = installRoot + JBI_ENV_PROPERTIES;
                
                Properties jbiEnv = new Properties();
                logger.fine("JBI Env properties :" + jbiEnvProperties);
                jbiEnv.load(new FileInputStream(jbiEnvProperties));
                jbiInstanceName = jbiEnv.getProperty(JBI_INSTANCE_NAME);
                
                logger.fine("JBI Instance name " + jbiInstanceName);
            } catch (IOException ioe) {
                logger.log(Level.SEVERE, "IOException during reading of jbi env properties " + ioe.getMessage());
            }
        }
        return jbiInstanceName;
    }
    
    private String getServiceEngineBundle() {
        
        String seBundle = System.getProperty("com.sun.aas.installRoot") + 
                    File.separator + SE_BUNDLE;
        return seBundle;
    }
    
    
    private void log(Level level, String property, String logString) {
        logger.log(level,property,logString);
    }
    
}
