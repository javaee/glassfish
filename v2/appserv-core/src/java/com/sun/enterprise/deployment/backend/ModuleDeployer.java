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
 * ModuleDeployer.java
 *
 * Created on January 3, 2002, 12:45 AM
 */

package com.sun.enterprise.deployment.backend;

import java.io.*;
import java.util.Set;
import java.util.List;
import java.util.logging.*;
import java.util.Properties;
import javax.enterprise.deploy.shared.ModuleType;

import com.sun.ejb.codegen.IASEJBCTimes;
import com.sun.enterprise.instance.ModuleEnvironment;
import com.sun.enterprise.instance.WebModulesManager;
import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.enterprise.instance.BaseManager;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.io.FileSource;
import com.sun.enterprise.util.zip.ZipItem;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.server.Constants;
import com.sun.enterprise.loader.EJBClassPathUtils;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.enterprise.deployment.WebServicesDescriptor;
import com.sun.enterprise.deployment.WebService;
import com.sun.enterprise.deployment.deploy.shared.FileArchive;
import com.sun.enterprise.deployment.deploy.shared.FileArchiveFactory;
import com.sun.enterprise.deployment.deploy.shared.AbstractArchive;
import com.sun.enterprise.deployment.archivist.ApplicationArchivist;
import com.sun.enterprise.deployment.archivist.Archivist;
import com.sun.enterprise.deployment.phasing.DeploymentServiceUtils;

// verifier imports
import com.sun.enterprise.tools.verifier.AppVerifier;

/** ModuleDeployer is an abstract class with common methods required for deploying Web Modules
 * and EJB Modules.
 *
 * @author  bnevins
 * @version
 */

public abstract class ModuleDeployer extends Deployer
{
	abstract protected BaseManager	createConfigManager(InstanceEnvironment ienv, ModuleEnvironment menv) throws IASDeploymentException, ConfigException;
	abstract protected void			preDeploy()			throws IASDeploymentException;
	abstract protected void			deploy()			throws IASDeploymentException, ConfigException;
	
	///////////////////////////////////////////////////////////////////////////
	
	protected ModuleDeployer(DeploymentRequest r)  throws IASDeploymentException
	{
		super(r);
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	protected boolean needsStubs()
	{
		// override this for any module that needs stubs created
		return false;
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	protected boolean needsJSPs()
	{
		// override this for any module that works with generated JSPs
		return false;
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	protected final File getModuleDir()
	{
		return moduleDir;
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	public void doRequest() throws IASDeploymentException
	{
		doRequestPrepare();
		doRequestFinish();
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	public void doRequestPrepare() throws IASDeploymentException
	{
		try
		{
			begin();
		}
		catch(Exception e)
		{
			if(shouldRollback)
				rollback();
			
			String msg = localStrings.getString(
			"enterprise.deployment.backend.dorequest_exception");
                        if (e.getCause() != null )
                            msg+= e.getCause().toString();

			logger.log(Level.WARNING, msg, e);
			throw new IASDeploymentException(msg, e);
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	public void doRequestFinish() throws IASDeploymentException
	{
		try
		{
                        // retrieve the J2EECPhase deployment status
                        J2EECPhaseStatus = request.getCurrentDeploymentStatus();
			if(request.isDeploy() || request.isReDeploy())
			{
                                // create a DeploymentStatus for predeploy stage
                                // it is a substage of J2EECPhase status
                                DeploymentStatus preDeployStatus =
                                    new DeploymentStatus(J2EECPhaseStatus);
                                request.setCurrentDeploymentStatus(
                                    preDeployStatus);
				beginFinish();
                                preDeploy();

                                // create a DeploymentStatus for runEJBC stage
                                // it is a substage of J2EECPhase status
                                DeploymentStatus runEJBCStatus =
                                    new DeploymentStatus(J2EECPhaseStatus);
                                request.setCurrentDeploymentStatus(
                                    runEJBCStatus);
				deploy();
                                register();

                                // create a DeploymentStatus for postDeploy
                                // stage, it is a substage of J2EECPhase status
                                DeploymentStatus postDeployStatus =
                                    new DeploymentStatus(J2EECPhaseStatus);
                                request.setCurrentDeploymentStatus(
                                    postDeployStatus);
				postDeploy();

				generatePolicy();
                        } else if(request.isUnDeploy())
			{
				beginFinish();
				preundeploy();
				undeploy();
				removePolicy();
			}
			else
			{
				String msg = localStrings.getString(
				"enterprise.deployment.backend.unknown_deployment_command" );
				throw new IASDeploymentException( msg );
			}
		}
		catch(Exception e)
		{
			if(shouldRollback)
				rollback();
			
			String msg = localStrings.getString(
			"enterprise.deployment.backend.dorequest_exception");
                        if (e.getCause() != null )
                            msg+= e.getCause().toString();
			logger.log(Level.FINE, msg, e);
                        if (e instanceof IASDeploymentException) {
                            throw (IASDeploymentException) e;
                        } else {
                            throw new IASDeploymentException(msg, e);
                        }
		}
		finally
		{
			finish();
		}
	}
	
	// this method is overridden from Deployer.  It is final so you know FOR SURE
	// that it isn't overridden by any subclasses.
	protected final void begin() throws IASDeploymentException
	{
		super.begin();
		
		// get environment object refs
		
		InstanceEnvironment instanceEnv = getInstanceEnv();
		moduleEnv						= request.getModuleEnv();
		moduleName						= request.getName();
		
		// check them
		
		if(moduleEnv == null)
		{
			String msg = localStrings.getString("enterprise.deployment.backend.null_moduleenvironment");
			throw new IASDeploymentException(msg);
		}
		
		try
		{
			moduleEnv.verify();
			modulesMgr = createConfigManager(instanceEnv, moduleEnv);
                        isReg = DeploymentServiceUtils.isRegistered(moduleName, request.getType());

			setDeployCommand();
			
			if(request.isReDeploy())
			{
                            //  first let's try to get the application from the
                            // instance manager cache
                            // if it's not there, get it from the request which
                            // is set through deployment context cache
                            moduleDD = getManager().getRegisteredDescriptor(
                                moduleName);
                            if (moduleDD == null) { 
                                moduleDD = request.getDescriptor();
                            } 

                                originalModuleDir = new File(DeploymentServiceUtils.getLocation(moduleName, request.getType()));
				unregister();
                                removePolicy();
			}
		}
		catch(Exception e)
		{
                    if (e instanceof IASDeploymentException) {
                        throw (IASDeploymentException)e; 
                    } else {
			throw new IASDeploymentException(e);
                    }
		}
		
		shouldRollback = true;
		
		// for redeploy -- when we get to doRequestFinish() -- the module will NOT be registered
		// any longer.
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	// bnevins -- added August 2003 to allow for the MBean to unload the module between
	// begin() and now.
	
	private void beginFinish() throws IASDeploymentException
	{
		setDirs();

                if (request.isDeploy()) {
                    // if the directories already exist on disk, wipe them out
                    // We do this because otherwise they'd never get this 
                    // Module deployed without wiping the dirs manually
                    liquidate();
                }

                // send PRE_DEPLOY event so the deployment event
                // listener can do the necessary work
		if(request.isReDeploy()) {
                    DeploymentEventInfo info = new DeploymentEventInfo(
                        moduleDir, stubsDir, moduleDD, getRequest());
                    DeploymentEvent ev = new DeploymentEvent(
                        DeploymentEventType.PRE_DEPLOY, info);
                    DeploymentEventManager.notifyDeploymentEvent(ev);

                    liquidate();
                }

	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private void setDirs() throws IASDeploymentException
	{
		assert modulesMgr != null;
		assert moduleName != null;
		assert moduleEnv != null;
		
		if(request.isDeploy())
			setDirsDeploy();
		else if(request.isReDeploy())
			setDirsReDeploy();
		else if(request.isUnDeploy())
			setDirsUnDeploy();
		else
		{
			String msg = localStrings.getString(
			"enterprise.deployment.backend.deployment_type_error" );
			throw new IASDeploymentException( msg );
		}
		
		request.setDeployedDirectory(moduleDir);
		request.setJSPDirectory(jspDir);
		request.setStubsDirectory(stubsDir);
		request.setGeneratedXMLDirectory(xmlDir);
	}
	
    	/**
	 * @return the module classpath
	 */
	protected List getModuleClasspath(Archivist archivist,
                AbstractArchive archive) throws IASDeploymentException {
	    try {
		String location = request.getDeployedDirectory().getAbsolutePath();
		return EJBClassPathUtils.getModuleClasspath(request.getName(), location, this.getManager());
	    } catch(Exception e) {
		throw new IASDeploymentException(e);
	    }
	}
        
	///////////////////////////////////////////////////////////////////////////
	
	private void setDirsDeploy() throws IASDeploymentException
	{
		if(isReg)
		{
			String msg = localStrings.getString(
			"enterprise.deployment.backend.deploy_error_module_exists" );
			throw new IASDeploymentException( msg );
		}
                xmlDir = new File(moduleEnv.getModuleGeneratedXMLPath());
                jwsDir = new File(moduleEnv.getJavaWebStartPath());
		
		if(needsStubs())
		{
			stubsDir = new File(moduleEnv.getModuleStubPath());
		}
		else
			stubsDir = null;
		
		if(needsJSPs())
		{
			assert (modulesMgr instanceof WebModulesManager);
			jspDir = new File(moduleEnv.getModuleJSPPath());
		}
		if(isArchive())
		{
			File parent = new File(getInstanceEnv().getModuleRepositoryPath());
                        moduleDir = new File(parent, moduleName);
                        moduleDir.mkdirs();
		}
		else if(isDirectory())
		{
			FileSource fileSource = request.getFileSource();
			
			if(fileSource == null || !fileSource.exists())
			{
				String msg = localStrings.getString(
				"enterprise.deployment.backend.file_source_does_not_exist",
				fileSource );
				throw new IASDeploymentException( msg );
			}
			
			moduleDir = fileSource.getFile();
			
			if(!FileUtils.safeIsDirectory(moduleDir))
			{
				String msg = localStrings.getString(
				"enterprise.deployment.backend.deployment_directory_does_not_exist",
				moduleDir.getPath() );
				throw new IASDeploymentException( msg );
			}
		}
		else
		{
			String msg = localStrings.getString(
			"enterprise.deployment.backend.deployment_not_dir_or_archive" );
			throw new IASDeploymentException( msg );
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
	
	private void setDirsUnDeploy() throws IASDeploymentException
	{
		// Use the already registered location
		try
		{
			if(!isReg)
			{
				String msg = localStrings.getString(
				"enterprise.deployment.backend.undeploy_error_module_not_registered" );
				throw new IASDeploymentException( msg );
			}
			
			moduleDir	= new File(DeploymentServiceUtils.getLocation(moduleName, request.getType()));
                        xmlDir = new File(modulesMgr.getGeneratedXMLLocation(moduleName));
                        jwsDir = new File(moduleEnv.getJavaWebStartPath());
			stubsDir = null;
			jspDir   = null;
			
			if(needsStubs())
				stubsDir	= new File(modulesMgr.getStubLocation(moduleName));
			if(needsJSPs())
			{
				assert (modulesMgr instanceof WebModulesManager);
				WebModulesManager mgr = (WebModulesManager)modulesMgr;
				jspDir = new File(mgr.getJSPLocation(moduleName));
			}
		}
		catch(Exception e)
		{
			String msg = localStrings.getString(
			"enterprise.deployment.backend.error_getting_module_directory",
			e );
			throw new IASDeploymentException( msg );
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private void setDirsReDeploy() throws IASDeploymentException
	{
		if(!isReg)
		{
			String msg = localStrings.getString(
			"enterprise.deployment.backend.redeploy_error_module_not_registered" );
			throw new IASDeploymentException( msg );
		}
		
                xmlDir = new File(modulesMgr.getGeneratedXMLLocation(moduleName));
                jwsDir = new File(moduleEnv.getJavaWebStartPath());

		// let's get the easy stuff out of the way first -- stubs & jsps
		stubsDir = null;
		jspDir   = null;
		
		if(needsStubs()) {
			stubsDir	= new File(modulesMgr.getStubLocation(moduleName));
                }
		if(needsJSPs())
		{
			assert (modulesMgr instanceof WebModulesManager);
			WebModulesManager mgr = (WebModulesManager)modulesMgr;
			jspDir = new File(mgr.getJSPLocation(moduleName));
		}
		
		if(isArchive())
		{
			// be sure we have the original deployed directory
			if(!FileUtils.safeIsDirectory(originalModuleDir))
			{
				String msg = localStrings.getString(
				"enterprise.deployment.backend.modulesmanager_error_getting_module_location",
				moduleName );
				throw new IASDeploymentException(msg);
			}
			
			moduleDir		= originalModuleDir;
		}
		else if(isDirectory())
		{
			FileSource fileSource = request.getFileSource();
			
			if(!fileSource.exists())
			{
				String msg = localStrings.getString(
				"enterprise.deployment.backend.file_source_does_not_exist",
				fileSource );
				throw new IASDeploymentException( msg );
			}
			
			assert fileSource.isDirectory();
			moduleDir = fileSource.getFile();
			
		}
		else
		{
			String msg = localStrings.getString(
			"enterprise.deployment.backend.redeployment_not_dir_or_archive" );
			throw new IASDeploymentException( msg );
		}
		
		moduleDir.mkdirs();
	}

    /**
    * Attempt to delete the deployed directory-tree.
    * <p> This method call is <b>guaranteed</b> to never throw any kind of Exception
    */
    public void cleanup_internal()
    {
                try
                {
                        if(request.isUnDeploy())
                        {
                                if(isMaybeCMPDropTables)
                                        dropTables();

                                liquidate();
                        }

                        // nothing to do for Deploy
                }
                catch(Exception e)
                {
                        logger.warning("Exception caught and ignored in cleanup_internal()");
                }
    }

	///////////////////////////////////////////////////////////////////////////
	
	
	protected void preundeploy() throws IASDeploymentException, ConfigException
	{
            try {
                // setup the cmp stuff for undeployment...
                // do this here before unregistering the descriptor
                if(getRequest().isMaybeCMPDropTables())
                {
                    isMaybeCMPDropTables = true;

                    // first let's try to get the application from the
                    // instance manager cache
                    // if it's not there, get it from the request which
                    // is set through deployment context cache
                    moduleDD = getManager().getRegisteredDescriptor(
                        moduleName);
                    if (moduleDD == null) { 
                        moduleDD = request.getDescriptor();
                    } 
                }
            } catch(Throwable t) {
                // yes we are swallowing all possible errors from outside this package!   
                logger.log( Level.WARNING,

                "enterprise.deployment_pre_undeploy_event_error", t);
            }
    }                                                                                 
	///////////////////////////////////////////////////////////////////////////
	
	private void undeploy() throws IASDeploymentException, ConfigException
	{
		try
		{
			unregister();
		}
		catch(ConfigException e)
		{
			String msg = localStrings.getString(
			"enterprise.deployment.backend.config_exception_on_remove",
			moduleName, e );
			throw new IASDeploymentException( msg, e);
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	protected void  postDeploy() throws IASDeploymentException, ConfigException
	{

                // handle undeleted files if there is any
                handleUndeletedFiles();

                // save the object type in optional attributes 
                Properties optionalAttributes = request.getOptionalAttributes();
                if (optionalAttributes == null) {
                    optionalAttributes = new Properties();
                }
                String resourceType = getResourceType(moduleDir);
                if(resourceType != null) {
                    optionalAttributes.setProperty(ServerTags.OBJECT_TYPE,
                        resourceType);
                }

                request.setExternallyManagedApp(
                    isExternallyManagedApp(moduleDir));
    
                handlePostDeployEvent();
          }

    /**
    * Called from postDeploy and postRedeploy to process extra events.
    */
    protected void handlePostDeployEvent() throws IASDeploymentException
    {
                DeploymentEventInfo info = getEventInfo();
                DeploymentEvent ev	 = new DeploymentEvent(
                DeploymentEventType.POST_DEPLOY, info);
                DeploymentEventManager.notifyDeploymentEvent(ev);

     }
	
   /** 
    * Create DeploymentEvent info instance.
    * @return DeploymentEventInfo 
    */
    protected DeploymentEventInfo getEventInfo() throws IASDeploymentException
    {	
		return new DeploymentEventInfo(moduleDir, stubsDir,
			request.getDescriptor(), getRequest());
    }

	///////////////////////////////////////////////////////////////////////////
	
	private void rollback()
	{
            try {
                unregister();
                liquidate(true);
            } catch(Exception e) {
                // Can't do anything about it!!
                logger.log( Level.WARNING,
                    "enterprise.deployment_rollback_error", e);
            }
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	protected void register() throws IASDeploymentException, ConfigException
	{
		modulesMgr.registerDescriptor(moduleName, request.getDescriptor());
	}
		
	///////////////////////////////////////////////////////////////////////////
        protected void liquidate(boolean isRollback)
            throws IASDeploymentException
        {
            if (request.isUnDeploy()) {                 
                if (! (DeploymentServiceUtils.isDirectoryDeployed(moduleName,                    request.getType()) || request.isReload()) ) {
                    cleanAndCheck(moduleDir);
                }
            } else {
                if (isArchive()) {
                    cleanAndCheck(moduleDir);
                }
            }
                                                                                            if (isRollback) {
                DeleteOrKeepFailedStubs(stubsDir);
            } else {
                cleanAndCheck(stubsDir);
            }
                                                                               
            cleanAndCheck(jspDir);
            cleanAndCheck(xmlDir);
            cleanAndCheck(jwsDir);

            liquidateTimeStamp = System.currentTimeMillis();
        }

        protected void liquidate() throws IASDeploymentException {
            liquidate(false);
        } 
	
	///////////////////////////////////////////////////////////////////////////
	
	protected void setShared(boolean what) throws ConfigException
	{
	/* WBN 510-02
	 * This feature wasn't implemented by core server.
	 * I'm commenting-out the call but leaving everything else
	 * in place -- since it will probably be added in a later
	 * version
	 */
		//modulesMgr.setShared(moduleName, what);
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	
	protected void unregister() throws ConfigException
	{
		modulesMgr.unregisterDescriptor(moduleName);
	}

	/**
	 * Runs the verifier on this module if verification is ON in
	 * the deployment request.
	 *
	 * @throws  IASDeploymentException  if an error found in this module
	 *                                  after verification
	 */
    protected void runVerifier() throws IASDeploymentException
    {
        if (request.isVerifying()) {
            try {
                String archive = request.getDeployedDirectory().getCanonicalPath();
                File jspOutDir = (request.getPrecompileJSP())? jspDir:null;
                new AppVerifier().verify(request.getDescriptor(),
                        (new FileArchiveFactory()).openArchive(archive),
                        request.getCompleteClasspath(),
                        jspOutDir);
            } catch (Exception e) {
                String msg = localStrings.getString(
                        "enterprise.deployment.backend.verifier_error");
                throw new IASDeploymentException(msg);
            }
        }
    }
	
	///////////////////////////////////////////////////////////////////////////
	
	private void setDeployCommand() throws IASDeploymentException
	{
		if(request.isUnDeploy())
		{
			if(!isReg)
			{
				String msg = localStrings.getString(
				"enterprise.deployment.backend.undeploy_error_module_not_registered" );
				throw new IASDeploymentException( msg );
			}
		}
		
		else if(request.isDeploy())
		{
			if(isReg)
			{
				//WBN 2/27/02 - By definition -- a REDEPLOY is now a forced DEPLOY if
				// the Module already exists
				if(request.isForced())
				{
					request.setCommand(DeploymentCommand.REDEPLOY);
				}
				else
				{
					String msg = localStrings.getString(
					"enterprise.deployment.backend.deploy_error_module_exists" );
					throw new IASDeploymentException( msg );
				}
			}
			else
			{
				// isReg is false.  This means that it isn't registered as the current type
				// of module.  But we might be clashing with a registered module of a different flavor.
				// E.g. this may be an ejb deployment and there is a web module already deployed
				// with the same name.
				// this will throw an IASDeploymentException if it is registered to another type...
				checkRegisteredAnywhereElse(moduleName);
			}
		}
	}
	
	/**
	 * runs the ejbc compiler for the deployable module
	 */
	protected ZipItem[] runEJBC() throws IASDeploymentException
	{
		ZipItem[] clientStubs = null;
		
		try
		{
			
			// ejbc timing info
			IASEJBCTimes timing	 = new IASEJBCTimes();
			
			EJBCompiler compiler = new EJBCompiler
			(
			moduleName,
			moduleDir,
			stubsDir,
			getManager(),
			request,
			timing
			);
			
			// runs ejbc
			clientStubs = compiler.compile();
			
			// add the ejbc timing info to deployment time
			addEJBCTime(timing);
			
		} catch (Exception e)
		{
			logger.log( Level.WARNING,
			"enterprise.deployment_ejbc_error", e );
			String msg = localStrings.getString(
			"enterprise.deployment.backend.ejbc_error" );
			throw new IASDeploymentException(msg, e);
		}
		
		// returns the client stubs or an empty array if no stubs
		return clientStubs;
	}
	
	/**
	 * @return the BaseManager implementation for this deployer
	 */
	protected  BaseManager getManager()
	{
		return modulesMgr;
	}
    
   ///////////////////////////////////////////////////////////////////////////

    /**
    * Call into CMP to drop tables.  This is called just before files are deleted as part
    * of the cleanup() call.  We do it very late in the process since dropping tables
    * can't be rolled-back.  Thus all of the other steps that can have errors that require a
    * rollback have already been done successfully - or we wouldn't be here.
    * bnevins April 2003
    * <p> This method call is <b>guaranteed</b> to never throw any kind of Exception
    */
    protected void dropTables()
    {
                assert isMaybeCMPDropTables; // programmer error if this is false!
                                                                                
                try
                {
                    DeploymentEventInfo info = new DeploymentEventInfo(moduleDir, stubsDir, moduleDD, request);
                                                                               
                    DeploymentEvent ev       = new DeploymentEvent(DeploymentEventType.PRE_UNDEPLOY, info);                                                 
                    DeploymentEventManager.notifyDeploymentEvent(ev);
                }
                catch(Throwable t)
                {
                        // yes we are swallowing all possible errors from outside this package!   
                        logger.log( Level.WARNING,

                        "enterprise.deployment_pre_undeploy_event_error", t);
                }
    }                                                                                 

    public void removePolicy() throws IASDeploymentException {
        //no op
    }
	
	///////////////////////////////////////////////////////////////////////////
	
	protected		BaseManager			modulesMgr						= null;
	protected		String				moduleName						= null;
	protected		File				moduleDir						= null;
	protected		File				stubsDir						= null;
	protected		File				jspDir							= null;
	protected		File				xmlDir							= null;
	protected		File				jwsDir							= null;
	protected		File				originalModuleDir				= null;
	protected		String				originalContextRoot				= null;
	protected		ModuleEnvironment	moduleEnv						= null;
        protected boolean                                     isMaybeCMPDropTables    = false;
	private			boolean				isReg							= false;
	private			boolean				shouldRollback					= false;
	private			boolean				moduleDirWasRenamed				= false;
        private Application moduleDD = null;
        private DeploymentStatus J2EECPhaseStatus = null;
	private static	StringManager		localStrings					= StringManager.getManager( ModuleDeployer.class );
}
