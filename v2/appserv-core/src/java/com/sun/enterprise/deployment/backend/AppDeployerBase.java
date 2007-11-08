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
 * AppDeployerBase.java
 *
 * Created on April 26, 2002, 5:02 PM
 * 
 * @author  bnevins
 * <BR> <I>$Source: /cvs/glassfish/appserv-core/src/java/com/sun/enterprise/deployment/backend/AppDeployerBase.java,v $
 *
 */

package com.sun.enterprise.deployment.backend;

import java.io.*;
import java.util.Properties;
import java.util.Vector;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.enterprise.deploy.shared.ModuleType;
import java.util.logging.Level;

import com.sun.enterprise.instance.ApplicationEnvironment;
import com.sun.enterprise.instance.AppsManager;
import com.sun.enterprise.instance.BaseManager;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.security.SecurityUtil;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.archivist.Archivist;
import com.sun.enterprise.deployment.deploy.shared.AbstractArchive;
import com.sun.enterprise.deployment.util.ModuleDescriptor;
import com.sun.enterprise.deployment.phasing.DeploymentServiceUtils;
import com.sun.enterprise.server.Constants;
import com.sun.enterprise.security.util.IASSecurityException;
import com.sun.enterprise.security.application.EJBSecurityManager;
import com.sun.enterprise.security.factory.EJBSecurityManagerFactory;
import com.sun.enterprise.loader.EJBClassPathUtils;
import com.sun.web.security.WebSecurityManager;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.enterprise.deployment.WebServicesDescriptor;
import com.sun.enterprise.deployment.WebService;
import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyConfigurationFactory;
import javax.security.jacc.PolicyContextException;
import com.sun.web.security.WebSecurityManagerFactory;
/** Abstract base class for AppDeployer, AppRedeployer, AppUndeployer.
 */

abstract class AppDeployerBase extends Deployer
{
	AppDeployerBase(DeploymentRequest r)  throws IASDeploymentException
	{
		super(r);
	}
	///////////////////////////////////////////////////////////////////////////
	//////    Abstract Methods 
	///////////////////////////////////////////////////////////////////////////
	abstract protected File setAppDir()		throws IASDeploymentException;

	///////////////////////////////////////////////////////////////////////////
	//////    Overridable Protected Methods 
	///////////////////////////////////////////////////////////////////////////
	
	/** Before even attempting the deployment operation -- check and verify and set lots of 
	 *  useful variables and references.
	 */
	protected void begin() throws IASDeploymentException
	{
		super.begin();
		
		try
		{
			appEnv = request.getAppEnv();

			if(appEnv == null) {
				String msg = localStrings.getString(
					"enterprise.deployment.backend.null_applicationenvironment_object");
				throw new IASDeploymentException( msg );
            }

			appMgr = new AppsManager(getInstanceEnv());
			appName = request.getName();
			
			if(!StringUtils.ok(appName)) {
				String msg = localStrings.getString(
						"enterprise.deployment.backend.null_appname" );

				throw new IASDeploymentException( msg );
			}
			
			isReg = DeploymentServiceUtils.isRegistered(
                            getAppName(), request.getType());
			verify();
		}
		catch(IASDeploymentException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			throw new IASDeploymentException(e);
		}
		
	}
	
	///////////////////////////////////////////////////////////////////////////

	/** Before even attempting the deployment operation -- check and verify and set lots of 
	 *  useful variables and references.
	 */
	protected void predeploy() throws IASDeploymentException
	{
		try
		{
			appDir = setAppDir();
			request.setDeployedDirectory(appDir);
			setGeneratedDirs();
		}
		catch(IASDeploymentException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			throw new IASDeploymentException(e);
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	protected final void verify() throws IASDeploymentException
	{
		if(!request.isApplication()) {
			String msg = localStrings.getString(
			"enterprise.deployment.backend.attempt_to_deploy_non_application");
			throw new IASDeploymentException( msg );
		}
			
		if(request.isUnDeploy())
		{
			if(!isReg) {
				String msg = localStrings.getString(
				"enterprise.deployment.backend.undeploy_error_application_not_registered");
				throw new IASDeploymentException( msg );
			}
		}

		else if(request.isDeploy())
		{
			if(isReg)
			{
				String msg = localStrings.getString(
				"enterprise.deployment.backend.deploy_error_application_exists");
				throw new IASDeploymentException( msg );
			}

			// isReg is false.  This means that it isn't registered as an App.
			// But we might be clashing with a registered module of a different flavor.
			// E.g. there may be a web module already deployed with the same name.
			// this will throw an IASDeploymentException if it is registered to another type...

			checkRegisteredAnywhereElse(appName);
		}
		else if(request.isReDeploy())
		{
			if(!isReg)
			{
				String msg = localStrings.getString(
				"enterprise.deployment.backend.redeploy_error_application_does_not_exist");
				throw new IASDeploymentException( msg );
			}
		}
	}

        protected void liquidate(boolean isRollback) 
            throws IASDeploymentException
        {
            if (request.isUnDeploy()) {
                if (! (DeploymentServiceUtils.isDirectoryDeployed(getAppName(),
                    request.getType()) || request.isReload()) ) {
                    cleanAndCheck(getAppDir());
                }
            } else {
                if (isArchive()) {
                    cleanAndCheck(getAppDir());
                }
            }

            if (isRollback) {
                DeleteOrKeepFailedStubs(getStubsDir());
            } else {
                cleanAndCheck(getStubsDir());
            }

            cleanAndCheck(getJSPDir());
            cleanAndCheck(getXMLDir());
            cleanAndCheck(getJWSDir());

            liquidateTimeStamp = System.currentTimeMillis();
        }
                                                                                
        protected void liquidate () throws IASDeploymentException {
            liquidate(false);
        }

	///////////////////////////////////////////////////////////////////////////
	//////    Private Methods 
	///////////////////////////////////////////////////////////////////////////
	private final void setGeneratedDirs() throws IASDeploymentException
	{
		try
		{
			stubsDir	= new File(getAppEnv().getAppStubPath());
			jspDir		= new File(getAppEnv().getAppJSPPath());
			xmlDir		= new File(getAppEnv().getAppGeneratedXMLPath());
			jwsDir		= new File(getAppEnv().getJavaWebStartPath());
			request.setJSPDirectory(jspDir);
			request.setStubsDirectory(stubsDir);
			request.setGeneratedXMLDirectory(xmlDir);
		}
		catch(Exception e)
		{
			String msg = localStrings.getString(
				"enterprise.deployment.backend.error_getting_generated_dirs",
				e );
			throw new IASDeploymentException( msg );
		}
	}	
	///////////////////////////////////////////////////////////////////////////
	//////    Access Methods -- note that the compiler should inline them   ///
	///////////////////////////////////////////////////////////////////////////
	protected final ApplicationEnvironment getAppEnv()
	{
		return appEnv;
	}
	
	///////////////////////////////////////////////////////////////////////////

	protected BaseManager getManager()
	{
		return appMgr;
	}	
	
	///////////////////////////////////////////////////////////////////////////

	protected final String getAppName()
	{
		return appName;
	}
	
	///////////////////////////////////////////////////////////////////////////

	protected final File getStubsDir()
	{
		return stubsDir;
	}
	
	///////////////////////////////////////////////////////////////////////////

	protected final File getJSPDir()
	{
		return jspDir;
	}

       ///////////////////////////////////////////////////////////////////////////
         
        protected final File getXMLDir()
        {       
                return xmlDir;
        }   

       ///////////////////////////////////////////////////////////////////////////

        protected final File getJWSDir()
        {
                return jwsDir;
        }

		
	///////////////////////////////////////////////////////////////////////////

	protected final File getAppDir()
	{
		return appDir;
	}
	
	
	protected final File getModuleDir() {
	    return appDir;
	}
    
    	/**
	 * @return the module classpath
	 */
	protected List getModuleClasspath(Archivist archivist,
                AbstractArchive archive) throws IASDeploymentException
	{
	    try {
                Application application = request.getDescriptor();
                if (application==null) {
                    application = (Application)archivist.readStandardDeploymentDescriptor(archive);
                    application.setRegistrationName(request.getName());
                }
	    	return EJBClassPathUtils.getAppClassPath(
                	application, request.getDeployedDirectory().getAbsolutePath(), getManager());
	    } catch(Exception e) {
		throw new IASDeploymentException(e);
	    }
	}

	/**
	 * @return a fully initialized and validated deployment descriptors for this 
	 * deployment request.
	 */
	protected Application loadDescriptors() throws IASDeploymentException {
            Application app = super.loadDescriptors();
            (new com.sun.enterprise.webservice.WsUtil()).genWSInfo(app, request);
            return app;
        }
        
	///////////////////////////////////////////////////////////////////////////

       protected void generatePolicy() throws IASDeploymentException {
	   try{
               // generate policy for all web modules with
               // moduleName + contextRoot and then generate for ejbs
               // with Appname
                Application applicationDD = request.getDescriptor();
                                                       
                // link with the ejb name       
                String linkName = null;
                boolean lastInService = false;
                for (Iterator iter = applicationDD.getWebBundleDescriptors().iterator();
                        iter.hasNext();){
                    String name 
                        = WebSecurityManager.getContextID((WebBundleDescriptor)iter.next());
                    lastInService = SecurityUtil.linkPolicyFile(name, linkName, lastInService);
                    linkName = name;
		}                                   
                for (Iterator iter = applicationDD.getEjbBundleDescriptors().iterator(); iter.hasNext();) {
                    String name =
                        EJBSecurityManager.getContextID((EjbBundleDescriptor)iter.next());
                    lastInService = SecurityUtil.linkPolicyFile(name, linkName, lastInService);
                    linkName = name;
                }
                // generate policies
               for (Iterator iter = applicationDD.getWebBundleDescriptors().iterator();
                        iter.hasNext();){
                    String name 
                        = WebSecurityManager.getContextID((WebBundleDescriptor)iter.next());
                    SecurityUtil.generatePolicyFile(name);
		}                               
                for (Iterator iter = applicationDD.getEjbBundleDescriptors().iterator(); iter.hasNext();) {
                    String name =
                        EJBSecurityManager.getContextID((EjbBundleDescriptor)iter.next());
                    SecurityUtil.generatePolicyFile(name);
                }
         } catch(IASSecurityException se){
	     // log this
	     String msg =
		 localStrings.getString("enterprise.deployment.backend.generate_policy_error", request.getName());
	     throw new IASDeploymentException(msg, se);
	 }
       }

	public void removePolicy() throws IASDeploymentException
	{
            String name = request.getName();
            
            try {
                WebSecurityManagerFactory wsmf =
                        WebSecurityManagerFactory.getInstance();
                String[] webcontexts
                        = wsmf.getAndRemoveContextIdForWebAppName(name);
		if(webcontexts !=null){
                	for(int i=0; i<webcontexts.length; i++){
			   if(webcontexts[i] != null){
                    		SecurityUtil.removePolicy(webcontexts[i]);
                                wsmf.removeWebSecurityManager(webcontexts[i]);
			   }
                	}
		}

                // removing ejb policy
                EJBSecurityManagerFactory ejbsmf =
                    (EJBSecurityManagerFactory)EJBSecurityManagerFactory.getInstance();
                String[] ejbContextIds
                    = ejbsmf.getAndRemoveContextIdForEjbAppName(name);
                if (ejbContextIds != null) {
                    for (String ejbContextId : ejbContextIds) {
                        if (ejbContextId != null) {
                            SecurityUtil.removePolicy(ejbContextId);
                            ejbsmf.removeSecurityManager(ejbContextId);
                        }
                    }
                }

                //remove any remaining policy
                //This is to address the bug where the CONTEXT_ID in 
                //WebSecurityManagerFactory is not properly populated.
                //We force the sub-modules to be removed in this case.
                //This should not impact undeploy performance on DAS.
                //This needs to be fixed better later.
                String policyRootDir = System.getProperty(
                    "com.sun.enterprise.jaccprovider.property.repository");
                if (policyRootDir != null) {
                    List<String> contextIds = new ArrayList<String>();
                    File policyDir = new File(
                        policyRootDir + File.separator + name);
                    if (policyDir.exists()) {
                        File[] policies = policyDir.listFiles();
                        for (int i = 0; i < policies.length; i++) {
                            if (policies[i].isDirectory()) {
                                contextIds.add(name + '/' + policies[i].getName());
                            }
                        }
                    } else {
                        //we tried.  give up now.
                    }

                    if (contextIds.size() > 0) {
                        for (String cId : contextIds) {
                            SecurityUtil.removePolicy(cId);
                        }
                    }
                }

            } catch(IASSecurityException ex) {
                String msg = localStrings.getString(
                "enterprise.deployment.backend.remove_policy_error", name);
                logger.log(Level.WARNING, msg, ex);
                throw new IASDeploymentException(msg, ex);
            }
	}


	private		ApplicationEnvironment	appEnv;
	private		AppsManager				appMgr;
	private		String					appName;
	private		boolean					isReg;
	private		File					stubsDir;
	private		File					jspDir;
	private		File					xmlDir;
	private		File					jwsDir;
	private		File					appDir;
    private static StringManager localStrings =
        StringManager.getManager( AppDeployerBase.class );
}



