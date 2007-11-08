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

package com.sun.enterprise.admin.mbeans;

import com.sun.enterprise.admin.common.exception.ServerInstanceException;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.ConfigBeansFactory;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.J2eeApplication;
import com.sun.enterprise.config.serverbeans.WebModule;
import com.sun.enterprise.config.serverbeans.EjbModule;
import com.sun.enterprise.config.serverbeans.ConnectorModule;
import com.sun.enterprise.config.serverbeans.AppclientModule;
import com.sun.enterprise.config.serverbeans.LifecycleModule;
import com.sun.enterprise.config.serverbeans.Mbean;
import javax.enterprise.deploy.shared.ModuleType;
import com.sun.enterprise.deployment.backend.DeploymentUtils;
import com.sun.enterprise.deployment.util.XModuleType;
import com.sun.enterprise.instance.AppsManager;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.config.serverbeans.ServerXPathHelper;
import com.sun.enterprise.deployment.io.DescriptorConstants;
import com.sun.enterprise.admin.server.core.AdminService;
import java.util.Iterator;
import java.io.File;
import java.io.FileReader;
import java.io.StringWriter;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import com.sun.enterprise.instance.InstanceFactory;
import com.sun.enterprise.util.io.FileUtils;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;
import com.sun.enterprise.util.RelativePathResolver;

/**
 * Provides API for getting attribute values like
 * moduleType, location, etc. for j2ee stand-alone modules
 * and the sub-modules within.
 *
 * @author Sreenivas Munnangi
 */

public class J2EEModule {

    // logger and local strings initialization
    private static final StringManager localStrings = 
	StringManager.getManager(J2EEModule.class);
    private static final Logger sLogger = 
	Logger.getLogger(LogDomains.ADMIN_LOGGER);

    // local variables
    private String standaloneModuleName = null;
    private String subModuleName = null;
    private ModuleType moduleType = null;
    private String ddLocation = null;

    /**
     * null constructor
     */
    public J2EEModule () {
    }

    /**
     * constructor for stand-alone module
     */
    public J2EEModule (String standaloneModuleName) 
	throws ServerInstanceException {

	if ((standaloneModuleName == null) ||
            (standaloneModuleName.length() < 1)) {
		sLogger.log(Level.WARNING, getLogMsg("invalid standAloneModuleName"));
		throw new ServerInstanceException ( localStrings.getString(
			"admin.mbeans.J2EEModule.invalidStandaloneModuleName"));
	}
	this.standaloneModuleName = standaloneModuleName;
    }

    /**
     * constructor for sub-module
     */
    public J2EEModule (String standaloneModuleName, String subModuleName) 
	throws ServerInstanceException {

	this(standaloneModuleName);

	if ((subModuleName == null) ||
            (subModuleName.length() < 1)) {
		sLogger.log(Level.WARNING, 
			getLogMsg("invalid sub module for the given stand-alone module"));
		throw new ServerInstanceException (localStrings.getString(
			"admin.mbeans.J2EEModule.invalidSubModuleName"));
	}
	this.subModuleName = subModuleName;
    }

    /**
     * Returns module type
     *
     * If both standaloneModuleName and subModuleName are valid strings
     * then the assumption is that standaloneModuleName represents a valid
     * j2ee application and subModuleName represents a module within 
     * the application.
     *
     * If subModuleName is null then the assumption is that the 
     * standaloneModuleName is a valid j2ee stand-alone module.
     */
     public ModuleType getModuleType() throws ServerInstanceException {

	if (moduleType != null) return moduleType;

	if (subModuleName == null) {
		moduleType = getModuleType(standaloneModuleName);
	} else {
		moduleType = getModuleType(standaloneModuleName, subModuleName);
	}

	return moduleType;
     }

    /**
     * Returns the deployment descriptors location for this module, i.e.
     * where the deployment descriptors are stored.
     */
    public String getDeploymentDescriptorsLocation() 
	throws ServerInstanceException {

	if (ddLocation != null) {
		ddLocation = RelativePathResolver.resolvePath(ddLocation);
		return ddLocation;
	}

	if (subModuleName == null) {
		ddLocation = getDDLocation(standaloneModuleName);
	} else {
		ddLocation = getDDLocation(standaloneModuleName, subModuleName);
	}

	if (ddLocation != null) {
		ddLocation = RelativePathResolver.resolvePath(ddLocation);
	}

	return ddLocation;
    }

    /**
     * Returns moduleType for the given stand-alone module
     */
    ModuleType getModuleType(String standaloneModuleName)
	throws ServerInstanceException {

	sLogger.log(Level.FINE, 
		getLogMsg("getModuleType for standaloneModuleName" + 
		standaloneModuleName));

        // iterate through each of module types

        ModuleType moduleType = null;

        try {

            // Application
            Applications appsConfigBean =
                    (Applications) ConfigBeansFactory.getConfigBeanByXPath(
		    AdminService.getAdminService().getAdminContext().getAdminConfigContext(),
		    ServerXPathHelper.XPATH_APPLICATIONS);

            // J2EEApplication
            J2eeApplication[] j2eeApps = appsConfigBean.getJ2eeApplication();
            if (j2eeApps != null) {
                for(int i=0; i<j2eeApps.length; i++) {
                    if ((j2eeApps[i].getName()).equals(standaloneModuleName)) {
			ddLocation = j2eeApps[i].getLocation();
			return ModuleType.EAR;
                    }
                }
            }        

            // EJBModule
            EjbModule[] eModules = appsConfigBean.getEjbModule();
            if (eModules != null) {
                for(int i=0; i<eModules.length; i++) {
                    if ((eModules[i].getName()).equals(standaloneModuleName)) {
			ddLocation = eModules[i].getLocation();
			return ModuleType.EJB;
                    }
                }
            }

            // WebModule
            WebModule[] wModules = appsConfigBean.getWebModule();
            if (wModules != null) {
                for(int i=0; i<wModules.length; i++) {
                    if ((wModules[i].getName()).equals(standaloneModuleName)) {
			ddLocation = wModules[i].getLocation();
			return ModuleType.WAR;
                    }
                }
            }

            // ResourceAdapterModule
            ConnectorModule[] connectorConfigBeans = appsConfigBean.getConnectorModule();
            if (connectorConfigBeans != null) {
                for(int i = 0; i < connectorConfigBeans.length; i++) {
                    if ((connectorConfigBeans[i].getName()).equals(standaloneModuleName)) {
			ddLocation = connectorConfigBeans[i].getLocation();
			return ModuleType.RAR;
                    }
                }
            }

            // AppClient Module
            AppclientModule[] acModules = appsConfigBean.getAppclientModule();
            if (acModules != null) {
                for(int i = 0; i < acModules.length; i++) {
                    if ((acModules[i].getName()).equals(standaloneModuleName)) {
			ddLocation = acModules[i].getLocation();
			return ModuleType.CAR;
                    }
                }
            }

            // Lifecycle Module
	    LifecycleModule[] lcModules = appsConfigBean.getLifecycleModule();
            if (lcModules != null) {
                for(int i = 0; i < lcModules.length; i++) {
                    if ((lcModules[i].getName()).equals(standaloneModuleName)) {
			return XModuleType.LCM;
                    }
                }
            }

            // Custom MBean Module
	    Mbean[] mbModules = appsConfigBean.getMbean();
            if (mbModules != null) {
                for(int i = 0; i < mbModules.length; i++) {
                    if ((mbModules[i].getName()).equals(standaloneModuleName)) {
			return XModuleType.CMB;
                    }
                }
            }

        } catch (Exception e) {
                throw new ServerInstanceException(e.getLocalizedMessage());
        }
        return moduleType;
    }

    /**
     * Returns moduleType for the given combination of 
     * stand-alone module and sub-module
     */
    ModuleType getModuleType(String standaloneModuleName, String subModuleName)
	throws ServerInstanceException {

	sLogger.log(Level.FINE, getLogMsg("getModuleType " +
		"standaloneModuleName = " + standaloneModuleName + " " +
		"subModuleName = " + subModuleName));

        ModuleType moduleType = null;

        try {
            // Get application descriptor

            AppsManager am = InstanceFactory.createAppsManager(
		    ApplicationServer.getServerContext().getInstanceName());

            Application appD = (Application)
                DeploymentUtils.getDescriptor(standaloneModuleName, am);

            // Get the bundle descriptor for the given module
            // and determine its' type

            BundleDescriptor bd = null;
            java.util.Set bds = appD.getBundleDescriptors();
            for(Iterator it=bds.iterator(); it.hasNext(); ) {
                bd = (BundleDescriptor)it.next();
                if ((bd.getModuleDescriptor().getArchiveUri()).equals(subModuleName) ||
                     bd.getModuleID().equals(subModuleName) ||
		     bd.getName().equals(subModuleName)) {
                        moduleType = bd.getModuleType();
			// set dd location
			ddLocation = am.getLocation(standaloneModuleName) +
				File.separator +
				FileUtils.makeFriendlyFilename(
					bd.getModuleDescriptor().getArchiveUri());
			break;
                }
            }

        } catch (Exception e) {
            throw new ServerInstanceException(e.getLocalizedMessage());
        }

        return moduleType;

    }


    /**
     * Returns deployment descriptors location for the 
     * given stand-alone module
     *
     * Now the ddLocation is set as part of determininig moduleType
     * In future if rewuired this method can be used to provide enhanced
     * functionality.
     */
    String getDDLocation(String standaloneModuleName)
	throws ServerInstanceException {
	sLogger.log(Level.FINE, getLogMsg(
		"getDDLocation for standaloneModuleName " + standaloneModuleName));
	return ddLocation;
    }

    /**
     * Returns deployment descriptors location for the 
     * given combination of stand-alone module and sub-module
     *
     * Now the ddLocation is set as part of determininig moduleType
     * In future if rewuired this method can be used to provide enhanced
     * functionality.
     */
    String getDDLocation(String standaloneModuleName, String subModuleName)
	throws ServerInstanceException {
	sLogger.log(Level.FINE, getLogMsg(
		"getDDLocation for standaloneModuleName " + 
		standaloneModuleName + " " + 
		"subModuleName = " + subModuleName));
	return ddLocation;
    }



    /**
     * Method to read deployment descriptor xml 
     * and return it as String
     */
    public String getStringForDDxml(String fileName) 
        	throws ServerInstanceException {

	    try {
            return FileUtils.getFileContents(fileName);
        } catch (FileNotFoundException fnfe) {
            sLogger.log(Level.WARNING, getLogMsg(
			"getStringForDDxml FileNotFoundException " + fileName));
	    	throw new ServerInstanceException(fnfe.getLocalizedMessage());
        } catch (IOException ioe) {
            sLogger.log(Level.WARNING, getLogMsg(
			"getStringForDDxml IOException " + fileName));
	    	throw new ServerInstanceException(ioe.getLocalizedMessage());
        }
    }
    
    /**
     * private method for decorating log messages with class name
     * and other appropriate info
     */
     private String getLogMsg(String str) {
	return (this.getClass().getName() + ":" +
		str);
     }

}
