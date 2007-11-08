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
 * WebModuleDeployer.java
 *
 * Created on January 11, 2002, 12:11 AM
 */

package com.sun.enterprise.deployment.backend;

import com.sun.ejb.codegen.IASEJBCTimes;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.WebModule;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.archivist.Archivist;
import com.sun.enterprise.deployment.autodeploy.AutoDeployConstants;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.deploy.shared.AbstractArchive;
import com.sun.enterprise.deployment.interfaces.SecurityRoleMapper;
import com.sun.enterprise.deployment.interfaces.SecurityRoleMapperFactory;
import com.sun.enterprise.deployment.phasing.DeploymentServiceUtils;
import com.sun.enterprise.deployment.RootDeploymentDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.WebService;
import com.sun.enterprise.deployment.WebServicesDescriptor;
import com.sun.enterprise.instance.BaseManager;
import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.enterprise.instance.ModuleEnvironment;
import com.sun.enterprise.instance.WebModulesManager;
import com.sun.enterprise.loader.EJBClassPathUtils;
import com.sun.enterprise.security.SecurityUtil;
import com.sun.enterprise.security.util.IASSecurityException;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.zip.ZipFileException;
import com.sun.enterprise.util.zip.ZipItem;
import com.sun.web.security.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/** 
 * WebModuleDeployer is a class for deploying Web Modules
 * Much of the code is in the super-class.
 *
 * @author  bnevins
 * @version 
 */
public class WebModuleDeployer extends ModuleDeployer 
{
	WebModuleDeployer(DeploymentRequest r) throws IASDeploymentException
	{
		super(r);
	}
	
	///////////////////////////////////////////////////////////////////////////

	protected BaseManager createConfigManager(InstanceEnvironment ienv, ModuleEnvironment menv)
            throws IASDeploymentException, ConfigException
	{
		webModulesMgr = new WebModulesManager(ienv);
		return webModulesMgr;
	}
	
	///////////////////////////////////////////////////////////////////////////

	protected void preDeploy() throws IASDeploymentException
	{
		try
		{
			assert moduleDir != null;
			assert StringUtils.ok(moduleName);
			
			if(request.isArchive())
			{
                            // check if this is a .war file or a single .class file
                            if (request.getFileSource().getFile().getName().endsWith(".class")) {
                                // we just need to copy the file to the WEB-INF/lib/classes
                                File subDir = new File(moduleDir, "WEB-INF");
                                subDir = new File(subDir, "classes");
                                copyAutodeployedClassFile(request.getFileSource().getFile(), subDir);
                            } else {
                                
                                /*
                                 * Bug 4980750 - Specify the temporary directory into which to explode nested jars.
                                 * This allows web services compilation to avoid including the nested jar file in the
                                 * classpath.  Their inclusion there was causing jar files to be opened but never
                                 * closed (as a result of Introspection during web services compilation and, in turn,
                                 * the use of sun.misc.URLClasspath which is where the unbalanced open actually occurs).
                                 * With the nested jar's contents unjarred into this temporary directory, the temp.
                                 * directory can be included in the web services compilation classpath rather than the
                                 * jar file.  The open jar file was causing subsequent redeployments of the same
                                 * application to fail because on Windows the open jar file could not be overwritten.
                                 */
				J2EEModuleExploder.explodeJar(request.getFileSource().getFile(), moduleDir);
                            }
                        }

                    xmlDir.mkdirs();
                    stubsDir.mkdirs();
		}
		catch(Exception e)
		{
			throw new IASDeploymentException(e);
		}
	}
	
	///////////////////////////////////////////////////////////////////////////

	protected void deploy() throws IASDeploymentException
	{
            // module dir where this module is exploded
            String mLocation;
            try { 
                mLocation = moduleDir.getCanonicalPath();
            } catch(java.io.IOException e) {
                throw new IASDeploymentException(e);
            }
            
            // loads the deployment descriptors
	    Application app = loadDescriptors();
            // Set the generated XML directory in application desc
            request.getDescriptor().setGeneratedXMLDirectory(xmlDir.getAbsolutePath());
	    
            WebBundleDescriptor bundleDesc = (WebBundleDescriptor) app.getStandaloneBundleDescriptor();

            // The priority order of how the context root should be used: 
            // 1. Context root specified by user (via --contextRoot option 
            // or via gui), i.e. request.getContextRoot()
            // 2. Context root specified in sun-web.xml i.e.
            // bundleDesc.getContextRoot()
            // 3. The default context root (archive name etc) i.e.
            // req.getDefaultContextRoot()

            if (request.getContextRoot() == null || 
                request.getContextRoot().trim().equals("")) { 
                if (bundleDesc.getContextRoot() != null &&
                    !bundleDesc.getContextRoot().trim().equals("")) {
                    request.setContextRoot(bundleDesc.getContextRoot());
                } else {
                    request.setContextRoot(request.getDefaultContextRoot());
                }
            }

            ZipItem[] clientStubs = runEJBC();

		try 
		{
			// 4919768 -- please remove this line after October, 2003
			// bnevins 9-9-2003
			// For some reason, a year ago the check was added to see if there are any
			// jsp files by calling: bundleDesc.getJspDescriptors()).isEmpty().
			// But it is an unneccessary restriction.  Many stock web.xml files do not have
			// the magic <jsp-file> tag -- but the web module contains jsp files.  In this case
			// the isEmpty() will return true and JSP precompilation will not occur until Tomcat
			// gets to it later at runtime.  The jsp compiler
			// in such a case will find them and generate the servlet java files(s).
			// So I removed the check.  And I tested to make sure everything will work with
			// (a) normal web modules where the jsp files are tagged in the xml
			// (b) web modules that don't have the tags in web.xml but the module DOES have jsp files
			// (c) web modules that have no jsp files
			
			//old code: if(request.getPrecompileJSP() && !(bundleDesc.getJspDescriptors()).isEmpty()) 
			if(request.getPrecompileJSP())
			{
				long time = System.currentTimeMillis();
				JSPCompiler.compile(
                                        moduleDir,
                                        jspDir,
                                        bundleDesc,
                                        request.getCompleteClasspath());
				addJSPCTime(System.currentTimeMillis() - time);
			}
            // runs verifier if verifier option is ON 
			runVerifier();
		} 
		catch(IASDeploymentException de) 
		{            
			throw de;
		} 
		catch(Exception e)
		{            
			throw new IASDeploymentException(e);
		}
	}
	
	protected boolean needsStubs()
	{
		// override this for webservices stubs/ties 
		return true;
        }

        ///////////////////////////////////////////////////////////////////////////
	
	protected boolean needsJSPs()
	{
		// override this for any module that works with generated JSPs
		return true;
	}		
	

	///////////////////////////////////////////////////////////////////////////

   protected void generatePolicy() throws IASDeploymentException
    {
        // if this is a part of an application then AppDeployerBase will be
        // called.
        // The rolemapper is not created for standalone web components

        // Load the WebArchivist, so that the rolemapper is instantiated.
        try{
            if(webModulesMgr == null){
                webModulesMgr =
                    (WebModulesManager)createConfigManager(getInstanceEnv(),
                                                           moduleEnv);
            }
	    Application app = request.getDescriptor();
            WebBundleDescriptor wbd = (WebBundleDescriptor) app.getStandaloneBundleDescriptor();

            // other things like creating the WebSecurityManager
            WebSecurityManagerFactory wsmf = null;
            wsmf = WebSecurityManagerFactory.getInstance();
            // this should create all permissions
            wsmf.newWebSecurityManager(wbd);
            // for an application the securityRoleMapper should already be created.
            // I am just creating the web permissions and handing it to the security
            // component.
            if(request.isApplication()){
                return;
            }
            String name = WebSecurityManager.getContextID(wbd) ;
            SecurityUtil.generatePolicyFile(name);
        }catch(IASDeploymentException iasde){ // what exception to propagate ahead
            // there will be problems in accessing the application
            String msg =
                localStrings.getString("enterprise.deployment.backend.generate_policy_error" );
            logger.log(Level.WARNING, msg, iasde);
            throw iasde;
        }catch(ConfigException ce){
            // there will be problems in accessing the application
            String msg =
                localStrings.getString("enterprise.deployment.backend.generate_policy_error" );
            logger.log(Level.WARNING, msg, ce);
            throw new IASDeploymentException(ce.toString());
        } catch(IASSecurityException iassec){
            // log this
            String msg =
                localStrings.getString("enterprise.deployment.backend.generate_policy_error" );
            logger.log(Level.WARNING, msg, iassec);
            throw new IASDeploymentException(msg, iassec);
        }

    }

    ///////////////////////////////////////////////////////////////////////////
    public void removePolicy() throws IASDeploymentException
    {
        if (request.isApplication()) {
            return;
        }
        WebSecurityManagerFactory wsmf = WebSecurityManagerFactory.getInstance();
        String requestName = request.getName();
        String[] name 
         = wsmf.getAndRemoveContextIdForWebAppName(requestName);
        try {
            if(name != null){
                if(name[0] != null)
                    SecurityUtil.removePolicy(name[0]);
                    wsmf.removeWebSecurityManager(name[0]);
            }
        } catch(IASSecurityException ex) {
            String msg = localStrings.getString(
            "enterprise.deployment.backend.remove_policy_error",name);
            logger.log(Level.WARNING, msg, ex);
            throw new IASDeploymentException(msg, ex);
        }
    }

    ///////////////////////////////////////////////////////////////////////////


	private WebModulesManager	webModulesMgr = null;
	private static StringManager localStrings =
		StringManager.getManager( WebModuleDeployer.class );
}

