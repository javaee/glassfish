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
 * @(#) DeploymentUtils.java
 *
 */
package com.sun.enterprise.deployment.backend;

import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.ClusterHelper;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.archivist.ArchivistFactory;
import com.sun.enterprise.deployment.archivist.AppClientArchivist;
import com.sun.enterprise.deployment.archivist.ApplicationArchivist;
import com.sun.enterprise.deployment.archivist.Archivist;
import com.sun.enterprise.deployment.archivist.ConnectorArchivist;
import com.sun.enterprise.deployment.archivist.EjbArchivist;
import com.sun.enterprise.deployment.archivist.WebArchivist;
import com.sun.enterprise.deployment.deploy.shared.FileArchive;
import com.sun.enterprise.deployment.RootDeploymentDescriptor;
import com.sun.enterprise.deployment.phasing.DeploymentServiceUtils;
import com.sun.enterprise.instance.AppclientModulesManager;
import com.sun.enterprise.instance.AppsManager;
import com.sun.enterprise.instance.BaseManager;
import com.sun.enterprise.instance.ConnectorModulesManager;
import com.sun.enterprise.instance.EjbModulesManager;
import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.enterprise.instance.WebModulesManager;
import com.sun.enterprise.loader.ClassLoaderUtils;
import com.sun.enterprise.loader.EJBClassLoader;
import com.sun.enterprise.util.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.*;

/** 
 * Utility methods for deployment backend. 
 *
 * @author Nazrul Islam
 * @since  JDK 1.4
 */
public final class DeploymentUtils {

    private DeploymentUtils()   { /* disallow instantiation */ }

    private static final Logger _logger = DeploymentLogger.get();

    /**
     * Sets the parent class loader for the given deployment request.
     *
     * @param    bootStrap    bootStrap (or parent) class loader 
     * @param    baseMgr      provides access to server configuration 
     * @param    req          deployment request
     *
     * @throws   ConfigException  if an error while reading the server.xml
     * @throws   IOException      if an i/o error
     */
    static void setParentClassLoader(ClassLoader bootStrap, BaseManager baseMgr,
            DeploymentRequest req) throws ConfigException, IOException {

        List allClassPaths = new ArrayList();
        
        // system class loader 
        List systemClasspath = baseMgr.getSystemCPathPrefixNSuffix();
        if (systemClasspath.size() > 0) {
            allClassPaths.addAll(systemClasspath);
        }

        // common class loader
        List commonClassPath = getCommonClasspath(baseMgr);
        if (commonClassPath.size() > 0) {
            allClassPaths.addAll(commonClassPath);
        }

        // shared class loader
        // Some explanation on the special handling below:
        // Per the platform specification, connector classes are to be availble
        // to all applications, i.e. a connector deployed to target foo should
        // be available to apps deployed on foo (and not target bar). In that
        // case, we will need to figure out all connector module deployed to 
        // the target on which the application is deployed.  Resolving the
        // classpath accordlingly. 
        String targetString = req.getResourceTargetList();
        List<String> targets = null;
        if (targetString != null) {
            // get the accurate list of targets from client
            targets = DeploymentServiceUtils.getTargetNamesFromTargetString(
                targetString); 
        } else {
            // get all the targets of this domain
            targets = new ArrayList<String>(); 
            ConfigContext configContext = AdminService.getAdminService(
                ).getAdminContext().getAdminConfigContext();

            Server[] servers = ServerHelper.getServersInDomain(configContext); 
            for (Server server: servers) {
                targets.add(server.getName());
            } 
            Cluster[] clusters = ClusterHelper.getClustersInDomain(
                configContext); 
            for (Cluster cluster: clusters) {
                targets.add(cluster.getName());
            } 
        }

        for (String target: targets) {
            List sharedClassPath = 
                baseMgr.getSharedClasspath(true, target);
            if (sharedClassPath.size() > 0) {
                allClassPaths.addAll(sharedClassPath);
            }
        }

        ClassLoader parentClassLoader = 
            getClassLoader(allClassPaths, bootStrap, null);

        // sets the parent class loader
        req.setParentClassLoader(parentClassLoader);

        // sets the class path for the class loader
        req.setParentClasspath(allClassPaths);
    }

    /**
     * Returns the class loader to be used for deployment. It contains all the 
     * urls for the given application.
     *
     * @param    classPaths   name of the application
     * @param    parent       parent class loader
     * @param    other        additional directory to be added to the class path
     *
     * @throws   exception  IOException  if an error while creating the 
     *                                   class loader
     */
    static EJBClassLoader getClassLoader(List paths, ClassLoader parent,
            File other) throws IOException {

        EJBClassLoader ejbCl  = null;

        if (parent != null) {
            ejbCl  = new EJBClassLoader(parent);
        } else {
            ejbCl  = new EJBClassLoader();
        }

        final int LIST_SZ = paths.size();
        for (int i=0; i<LIST_SZ; i++) {
            String path = (String) paths.get(i);
            ejbCl.appendURL(new File(path));
        }

        if (other != null) {
            ejbCl.appendURL(other);
        }

        return ejbCl;
    }

    /**
     * Returns the common class loader paths, if any, or an empty list.
     *
     * @param    mgr    manager obj that provides access to application config
     * @return   the common class path urls
     */
    static List getCommonClasspath(BaseManager mgr) throws IOException {

        InstanceEnvironment env  = mgr.getInstanceEnvironment();
        String dir               = env.getLibClassesPath();
        String jarDir            = env.getLibPath();

        return ClassLoaderUtils.getUrlList(new File[] {new File(dir)}, 
                                           new File[] {new File(jarDir)});
    }

	protected static String getSystemPropertyIgnoreCase(final String key)
	{
		Properties	p	= System.getProperties();
		Set			set = p.entrySet();

		for(Iterator it = set.iterator(); it.hasNext(); )
		{
			Map.Entry	me		= (Map.Entry)it.next();
			String		propKey = (String)me.getKey();
			
			if(key.compareToIgnoreCase(propKey) == 0)
				return (String)me.getValue();
		}
		
		return null;
	}

    /**
     * Returns the deployment descriptor object for the application.
     * Turn off the annotation processing during undeployment.  Note that 
     * this method is called *only* during the undeployment phase right now.
     *
     * @param    appDir       exploded application dir location
     *
     * @return   the deployment descriptor object for the application living in
	 * the given appdir
     *
     * @throws   IASDeploymentException if any of these other Exceptions are caught:
     * ConfigException  if unable to load the deployment descriptor
	 * AppConfigException  if an error if the while reading 
     *                               the deployment descriptor
     * IOException  if an i/o error
     */
    static Application getAppDescriptor(String appDir) throws IASDeploymentException
    {
        return getAppDescriptor(appDir, false);
	}

    /**
     * Returns the deployment descriptor object for the application.
     *
     * @param    appDir       exploded application dir location
     * @param    annotationProcessing whether to process annotation
     *
     * @return   the deployment descriptor object for the application living in
	 * the given appdir
     *
     * @throws   IASDeploymentException if any of these other Exceptions are caught:
     * ConfigException  if unable to load the deployment descriptor
	 * AppConfigException  if an error if the while reading 
     *                               the deployment descriptor
     * IOException  if an i/o error
     */
    static Application getAppDescriptor(String appDir, boolean annotationProcessing) 
            throws IASDeploymentException
    {
		try {
            FileArchive archive = new FileArchive();
            archive.open(appDir);

            ApplicationArchivist archivist = new ApplicationArchivist();
            archivist.setAnnotationProcessingRequested(annotationProcessing);
            archivist.setXMLValidation(false);

            return (Application) archivist.open(archive);
		} catch(Throwable t) {
			throw new IASDeploymentException(t);
		}
	}

    /**
     * Turn off the annotation processing during undeployment.  Note that 
     * this method is called *only* during the undeployment phase right now.
     */
    static Application getModuleDescriptor(String appDir) throws IASDeploymentException
    {
        return getModuleDescriptor(appDir, false);
    }

    static Application getModuleDescriptor(String appDir, boolean annotationProcessing) 
            throws IASDeploymentException
    {
        try {
            FileArchive archive = new FileArchive();
            archive.open(appDir);

            Archivist archivist =
                ArchivistFactory.getArchivistForArchive(archive);

            archivist.setAnnotationProcessingRequested(annotationProcessing);
            archivist.setXMLValidation(false);

            return (Application) ApplicationArchivist.openArchive(archivist, archive, true);
        } catch(Throwable t) {
            throw new IASDeploymentException(t);
        }
    }

    /**
     * This method returns a fully populated top level descriptor object.
     * It does so by first checking the cache in the instance manager.
     * If the descriptor object does not exist in cache, it then proceeds
     * to read off of the disk.  Note that reading from the disk is an
     * expensive operation.  So please use this method with caution.  In
     * addition, this method does not cache the read Descriptor object
     * in the instance manager so that query for Descriptors from cli and 
     * gui would not (and should not) meddle with the cache management for
     * descriptors.
     *
     *@param appId The module ID of the application
     *@param manager The instance manager for the module
     *@return The top level Descriptor object
     */
    public static RootDeploymentDescriptor getDescriptor(
            String appId, BaseManager manager) throws IASDeploymentException {
        Application application = manager.getRegisteredDescriptor(appId);
        if (application != null) {
            if (application.isVirtual()) {
                return application.getStandaloneBundleDescriptor();
            } else {
                return application;
            }
        }

        //Now, load from disk
        FileArchive in = new FileArchive();
        try {
            String appDir = manager.getLocation(appId);
            // if is system predeployed app, load from original app dir
            // else load from generated/xml dir
            // print a warning if generated/xml dir is not there
            // and load from original dir (upgrade scenario)
            if (manager.isSystemAdmin(appId)) {
                in.open(appDir);
            } else {
                String xmlDir = manager.getGeneratedXMLLocation(appId);
                if (FileUtils.safeIsDirectory(xmlDir)) {
                    in.open(xmlDir);
                } else {
                    // log a warning message in the server log
                    _logger.log(Level.WARNING, 
                        "enterprise.deployment.backend.no_generated_xmldir",
                        new Object[]{appId, xmlDir, appDir});
                    in.open(appDir);
                }
            }

            Archivist archivist = null;
            if (manager instanceof AppsManager) {
                archivist = new ApplicationArchivist();
            } else if (manager instanceof EjbModulesManager) {
                archivist = new EjbArchivist();
            } else if (manager instanceof WebModulesManager) {
                archivist = new WebArchivist();
            } else if (manager instanceof AppclientModulesManager) {
                archivist = new AppClientArchivist();
            } else if (manager instanceof ConnectorModulesManager) {
                archivist = new ConnectorArchivist();
            }

            archivist.setAnnotationProcessingRequested(false);
            archivist.setXMLValidation(false);
            Application desc = ApplicationArchivist.openArchive(
                                        appId, archivist, in, true);

            //note: we are not reading back the persistence information here
            //we could, if ever the tools need it.
            if (!desc.isVirtual()) {
                archivist.setHandleRuntimeInfo(false);
                ((ApplicationArchivist) 
                    archivist).readModulesDescriptors(desc, in);
                // now process runtime DDs
                archivist.setHandleRuntimeInfo(true);
                archivist.readRuntimeDeploymentDescriptor(in, desc);
            } else {
                return desc.getBundleDescriptors().iterator().next();
            }
            return desc;
        } catch (Exception ex) {
            _logger.log(Level.SEVERE, 
                        "enterprise.deployment.backend.get_descriptor_failed",
                        new Object[]{appId});
            IASDeploymentException de = new IASDeploymentException(ex.getMessage());
            de.initCause(ex);
            throw de;
        } finally {
            try {
                in.close();
            } catch (Exception ex) {}
        }
    }

    /**
     * This method returns the relative file path of an embedded module to 
     * the application root.
     * For example, if the module is expanded/located at 
     * $domain_dir/applications/j2ee-apps/foo/fooEJB_jar,
     * this method will return fooEJB_jar
     *
     *@param appRootPath The path of the application root which
     *                   contains the module 
     *                   e.g. $domain_dir/applications/j2ee-apps/foo
     *@param moduleUri The module uri
     *                 e.g. fooEJB.jar
     *@return The relative file path of the module to the application root
     */
    public static String getRelativeEmbeddedModulePath(String appRootPath,
        String moduleUri) {
        moduleUri = FileUtils.makeLegalNoBlankFileName(moduleUri);
        if (FileUtils.safeIsDirectory(new File(appRootPath, moduleUri))) {
            return moduleUri;
        } else {
            return FileUtils.makeFriendlyFilename(moduleUri);
        }
    }

    /**
     * This method returns the file path of an embedded module. 
     * For example, if the module is expanded/located at 
     * $domain_dir/applications/j2ee-apps/foo/fooEJB_jar,
     * this method will return 
     * $domain_dir/applications/j2ee-apps/foo/fooEJB_jar
     *
     *@param appRootPath The path of the application root which
     *                   contains the module 
     *                   e.g. $domain_dir/applications/j2ee-apps/foo
     *@param moduleUri The module uri
     *                 e.g. fooEJB.jar
     *@return The file path of the module
     */
    public static String getEmbeddedModulePath(String appRootPath,
        String moduleUri) {
        return appRootPath + File.separator + getRelativeEmbeddedModulePath(appRootPath, moduleUri) ;
    }
}
