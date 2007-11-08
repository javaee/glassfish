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
* EjbModuleDeployer.java
*
* Created on January 3, 2002, 3:33 PM
*/

package com.sun.enterprise.deployment.backend;

import java.io.*;
import java.util.List;
import java.util.logging.*;

import com.sun.ejb.codegen.IASEJBCTimes;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.zip.ZipFileException;
import com.sun.enterprise.util.zip.ZipItem;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.instance.BaseManager;
import com.sun.enterprise.instance.EjbModulesManager;
import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.enterprise.instance.ModuleEnvironment;
import com.sun.enterprise.util.diagnostics.Profiler;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.security.SecurityUtil;
import com.sun.enterprise.deployment.interfaces.SecurityRoleMapper;
import com.sun.enterprise.deployment.interfaces.SecurityRoleMapperFactory;
import com.sun.enterprise.deployment.interfaces.SecurityRoleMapperFactoryMgr;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.phasing.DeploymentServiceUtils;
import com.sun.enterprise.security.util.IASSecurityException;
import com.sun.enterprise.security.application.EJBSecurityManager;
import com.sun.enterprise.security.factory.EJBSecurityManagerFactory;
import com.sun.enterprise.loader.EJBClassPathUtils;

// imports for dd generator
import com.sun.enterprise.deployment.archivist.Archivist;
import com.sun.enterprise.deployment.deploy.shared.AbstractArchive;
import com.sun.enterprise.deployment.interfaces.*;


/** 
* EjbModuleDeployer is a class for deploying EJB Modules
* Much of the code is in the super-class.
*
* @author  bnevins
* @version 
*/
public class EjbModuleDeployer extends ModuleDeployer 
{
    EjbModuleDeployer(DeploymentRequest r) throws IASDeploymentException
    {
		super(r);
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    protected boolean needsStubs()
    {
		return true;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    protected BaseManager createConfigManager(InstanceEnvironment ienv, ModuleEnvironment menv) throws IASDeploymentException, ConfigException
    {
		manager = new EjbModulesManager(ienv);
		return manager;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    protected void preDeploy() throws IASDeploymentException
    {
		assert stubsDir   != null;
		assert moduleDir  != null;
		assert moduleName != null;

		try
		{
			if(request.isArchive()) {
                            // check if this is a .jar file or a single .class file
                            if (request.getFileSource().getFile().getName().endsWith(".class")) {
                                copyAutodeployedClassFile(request.getFileSource().getFile(), moduleDir);
                            } else {
        			J2EEModuleExploder.explodeJar(request.getFileSource().getFile(), moduleDir);                                
                            }
                        }

                        xmlDir.mkdirs();
                        stubsDir.mkdirs();
		}
		catch(Exception e)
		{
			throw new IASDeploymentException(e.toString(), e);
		}
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    protected void deploy() throws IASDeploymentException, ConfigException
    {
		loadDescriptors();
		runVerifier();
                // Set the generated XML directory in application desc
                request.getDescriptor().setGeneratedXMLDirectory(xmlDir.getAbsolutePath());
		ZipItem[] clientStubs = runEJBC();
		createClientJar(clientStubs);
    }
	
    ///////////////////////////////////////////////////////////////////////////
    
    protected void register() throws IASDeploymentException, ConfigException
    {
		super.register();
		setShared(request.isSharedModule());
    } 
    
    ///////////////////////////////////////////////////////////////////////////
    protected void generatePolicy() throws IASDeploymentException
	{
		// if this is a part of application then generatePolicy in AppDeployerBa
		// se will be called, so this should be a no-op
		try
		{
			if(request.isApplication())
			{
				return;
			}
			else
			{
				Application app = request.getDescriptor();
				EjbBundleDescriptor ejbBundleDesc =
					(EjbBundleDescriptor)
					app.getStandaloneBundleDescriptor();
				//For standalone ejbs, the RoleMapper should be constructed by now
				String name = EJBSecurityManager.getContextID(ejbBundleDesc);
				SecurityUtil.generatePolicyFile(name);
                                EJBSecurityManagerFactory ejbsmf =
                                    (EJBSecurityManagerFactory)EJBSecurityManagerFactory.getInstance(); 
                                ejbsmf.createSecurityManager(
                                    (EjbDescriptor)ejbBundleDesc.getEjbs().iterator().next());
			}
		} 
		catch (IASSecurityException se)
		{
			String msg =
			localStrings.getString("enterprise.deployment.backend.generate_policy_error" );

			logger.log(Level.WARNING, msg, se);
			throw new IASDeploymentException(msg, se);
		}
    }


	///////////////////////////////////////////////////////////////////////////
	
	public void removePolicy() throws IASDeploymentException
	{
		if (request.isApplication()) 
		{
			return;
		}

		String requestName = request.getName();
                EJBSecurityManagerFactory ejbsmf =
                   (EJBSecurityManagerFactory)EJBSecurityManagerFactory.getInstance(); 
                String[] names =
                    ejbsmf.getAndRemoveContextIdForEjbAppName(requestName);
                String name = null;
		try 
		{
                        if (names != null && names.length > 0 &&
                                names[0] != null) {
                            name = names[0];
                            SecurityUtil.removePolicy(name);
                            ejbsmf.removeSecurityManager(name);
                        }
		} 
		catch(IASSecurityException ex) 
		{
			String msg = localStrings.getString(
				"enterprise.deployment.backend.remove_policy_error",name);
			logger.log(Level.WARNING, msg, ex);
			throw new IASDeploymentException(msg, ex);
		}
	}

    ///////////////////////////////////////////////////////////////////////////
    
    private	EjbModulesManager		manager					= null;
    private static StringManager	localStrings = StringManager.getManager( EjbModuleDeployer.class );
}

