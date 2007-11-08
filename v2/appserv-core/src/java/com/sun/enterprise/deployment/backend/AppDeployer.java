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
 * AppDeployer.java
 *
 * Created on December 11, 2001, 5:37 PM
 */

package com.sun.enterprise.deployment.backend;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.*;
import java.util.*;
import javax.enterprise.deploy.shared.ModuleType;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.instance.ApplicationEnvironment;
import com.sun.enterprise.instance.AppsManager;
import com.sun.enterprise.instance.BaseManager;
import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.ejb.codegen.IASEJBCTimes;
import com.sun.enterprise.util.io.FileSource;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.zip.ZipItem;
import com.sun.enterprise.util.zip.ZipFileException;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.diagnostics.Reminder;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.deploy.shared.FileArchive;
import com.sun.enterprise.deployment.deploy.shared.FileArchiveFactory;
import com.sun.enterprise.deployment.deploy.shared.AbstractArchive;
import com.sun.enterprise.deployment.Descriptor;
import com.sun.enterprise.deployment.util.ModuleDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.deploy.shared.FileArchive;
import com.sun.enterprise.deployment.archivist.ApplicationArchivist;

// imports for dd generator
import com.sun.enterprise.deployment.interfaces.*;
import com.sun.enterprise.tools.verifier.AppVerifier;

import com.sun.enterprise.security.application.EJBSecurityManager;
import com.sun.enterprise.security.factory.EJBSecurityManagerFactory;
import com.sun.web.security.WebSecurityManagerFactory;

/** 
 * AppDeployer is responsible for Deploying Applications.  Both archives and
 * user-supplied "pre-exploded" directories are supported.
 *
 * WBN February 2, 2002 -- this code is now officially a mess!
 * @author  bnevins
 * @version 
 */
public class AppDeployer extends AppDeployerBase
{
	AppDeployer(DeploymentRequest r)  throws IASDeploymentException
	{
		super(r);
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
                // retrieve the J2EECPhase deployment status
                J2EECPhaseStatus = request.getCurrentDeploymentStatus();

		try
		{
			begin();
		}
		catch(Exception e)
		{
			rollback();
			String msg = localStrings.getString(
					"enterprise.deployment.backend.dorequest_exception" );
                        if (e.getCause() != null )
                            msg+= e.getCause().toString();

			logger.log(Level.WARNING, msg, e);
                        if (e instanceof IASDeploymentException) {
                            throw (IASDeploymentException) e;
                        } else {                        
                            throw new IASDeploymentException( msg, e);
                        }
		}
	}


	///////////////////////////////////////////////////////////////////////////

	public void doRequestFinish() throws IASDeploymentException
	{
		try
		{
			predeploy();
			deploy();
			generatePolicy();
		}
		catch(Exception e)
		{
			String msg = localStrings.getString(
					"enterprise.deployment.backend.dorequest_exception" );
                        if (e.getCause() != null )
                            msg+= e.getCause().toString();

			rollback();
			logger.log(Level.FINE, msg, e);
                        if (e instanceof IASDeploymentException) {
                            throw (IASDeploymentException) e;
                        } else {
                            throw new IASDeploymentException( msg, e);
                        }
		}
		finally
		{
			finish();
		}
	}

	///////////////////////////////////////////////////////////////////////////

	public void cleanup_internal()
	{
		// nothing to do.
	}

	///////////////////////////////////////////////////////////////////////////
	
	protected void predeploy() throws IASDeploymentException
	{

                // create a DeploymentStatus for preDeploy stage
                // it is a substage of J2EECPhase status
                DeploymentStatus preDeployStatus =
                    new DeploymentStatus(J2EECPhaseStatus);
                request.setCurrentDeploymentStatus(preDeployStatus);
		super.predeploy();
			// if the directories already exist on disk -- wipe them out
			// We do this because otherwise they'd never get this App deployed
			// without wiping the dirs manually
		
		if(request.isDeploy())
			liquidate();
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	protected void deploy() throws IASDeploymentException
	{
		try
		{

            Application app;
			if(isArchive()) {						      
                app = explodeArchive();
			} else { //directory deploy
                //create the generated/xml directory for directory deploy
                getXMLDir().mkdirs();
                getJWSDir().mkdirs();
                getStubsDir().mkdirs();

                // construct the standard application.xml if omitted
                ApplicationArchivist appArchivist = new ApplicationArchivist();
		        String dir = request.getDeployedDirectory().getAbsolutePath();
                FileArchive appArchive = new FileArchive();
                appArchive.open(dir);
                if (!appArchivist.hasStandardDeploymentDescriptor(appArchive)) {
                    Application appDesc = 
                        Application.createApplication(appArchive,true,true);
                    request.setDescriptor(appDesc);
                }

                // load deployment descriptors
                app = loadDescriptors();
            }
			request.setDescriptor(app);
                        
                        // Set the generated XML directory in application desc
                        request.getDescriptor().setGeneratedXMLDirectory(getXMLDir().getAbsolutePath());

                        // create a DeploymentStatus for runEJBC stage
                        // it is a substage of J2EECPhase status
                        DeploymentStatus runEJBCStatus =
                            new DeploymentStatus(J2EECPhaseStatus);
                        request.setCurrentDeploymentStatus(runEJBCStatus);
			runJSPC();
			runVerifier();
			ZipItem[] clientStubs = runEJBC(); 
			
                        checkAppclientsMainClasses();
                        
			createClientJar(clientStubs);

                        // create a DeploymentStatus for postDeploy stage
                        // it is a substage of J2EECPhase status
                        DeploymentStatus postDeployStatus =
                            new DeploymentStatus(J2EECPhaseStatus);
                        request.setCurrentDeploymentStatus(postDeployStatus);
			postDeploy();

			addToSummary(getSuccessMessage()  + getAppName() + "\n*********************\n");
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
	
	protected void rollback()
	{
            try {
                getManager().unregisterDescriptor(getAppName());
                liquidate(true);
            } catch(IASDeploymentException ide) {
                // Can't do anything about it!!
                logger.log( Level.WARNING,
                    "enterprise.deployment_rollback_error", ide);
            }
	}       

	///////////////////////////////////////////////////////////////////////////
	
	private Application explodeArchive() throws Exception
	{
            
            // first explode the ear file
            Application appDesc = J2EEModuleExploder.explodeEar(request.getFileSource().getFile(), getAppDir());           
            appDesc.setRegistrationName(request.getName());
            
            request.setDescriptor(appDesc);
            getXMLDir().mkdirs();
            getJWSDir().mkdirs();
            getStubsDir().mkdirs();
                        
            // now I can call the normal descriptor loading...
            loadDescriptors();            

            return appDesc;
	}

	///////////////////////////////////////////////////////////////////////////
	
	private ZipItem[] runEJBC() throws IASDeploymentException
	{
        ZipItem[] clientStubs = null;

        try {
            // ejbc timing info
            IASEJBCTimes timing	 = new IASEJBCTimes();
            EJBCompiler compiler = new EJBCompiler
            (
                getAppName(),	
                getAppDir(),
                getStubsDir(),
                getManager(),
                request,
                timing
            );
            
            // runs ejbc
            clientStubs = compiler.compile();

            /*
             *Clean up the temporary jar expansion directory.
             *
             *Commenting this out to avoid degrading performance.
             */
//            cleanTempUnjarDirectories(adjustedClassPathList);
            
            // add the ejbc timing info to deployment time
            addEJBCTime(timing);

        } 
        catch(IASDeploymentException e) {
            throw e;
        }
        catch (Exception e) {
            logger.log( Level.WARNING,
            "enterprise.deployment_ejbc_error", e );
            String msg = localStrings.getString(
            "enterprise.deployment.backend.ejbc_error" );
            throw new IASDeploymentException(msg, e);
        }

        // returns the client stubs or an empty array if no stubs
        return clientStubs;
	}

	///////////////////////////////////////////////////////////////////////////
	
	private void runJSPC() throws IASDeploymentException
	{
	    if(!request.getPrecompileJSP())
		return;
	    
	    Application app = request.getDescriptor();
	    Set webBundleDescriptors = app.getWebBundleDescriptors();
            if (webBundleDescriptors == null) {
                return;
            }

            Iterator itr = webBundleDescriptors.iterator();
	    while (itr.hasNext())
	    {
		WebBundleDescriptor wbd = (WebBundleDescriptor) itr.next();
		String warDirName = FileUtils.makeFriendlyFilename(
                                wbd.getModuleDescriptor().getArchiveUri());
		File outDir = new File(getJSPDir(), warDirName);
		File inDir	= new File(getAppDir(),	warDirName);
		//String msg = "***** Call JSPCompiler(" + inDir.getPath()+ ", " + outDir.getPath() + ")";
		//logger.log(Level.SEVERE, msg);
		long time = System.currentTimeMillis();
		JSPCompiler.compile(inDir, outDir, wbd,
                                    request.getCompleteClasspath());
		addJSPCTime(System.currentTimeMillis() - time);
	    }
	}

	///////////////////////////////////////////////////////////////////////////
	
	protected void postDeploy() throws ConfigException, IASDeploymentException
	{
                // handle undeleted files if there is any
                handleUndeletedFiles();

		Application applicationDD = request.getDescriptor();
                       getManager().registerDescriptor(applicationDD.getRegistrationName(), 
                                                       applicationDD);
		DeploymentEventInfo info = new DeploymentEventInfo(
			getAppDir(), getStubsDir(),
			applicationDD, getRequest());
		DeploymentEvent ev = new DeploymentEvent(
			DeploymentEventType.POST_DEPLOY, info);
		DeploymentEventManager.notifyDeploymentEvent(ev);

		// need to populate web security PolicyConfig
		WebSecurityManagerFactory wsmfactory =
			WebSecurityManagerFactory.getInstance();
		for (Iterator iter = applicationDD.getWebBundleDescriptors().iterator(); iter.hasNext();)
		{
			wsmfactory.newWebSecurityManager((WebBundleDescriptor)iter.next());
		}
		// need to populate ejb security PolicyConfig
                EJBSecurityManagerFactory ejbmfactory =
                    (EJBSecurityManagerFactory)EJBSecurityManagerFactory.getInstance();
                for (Object ejbBundleDescObj : applicationDD.getEjbBundleDescriptors()) {
                    for (Object ejbDescObj : ((EjbBundleDescriptor)ejbBundleDescObj).getEjbs()) {
                        ejbmfactory.createSecurityManager((EjbDescriptor)ejbDescObj);
                    }
		}

		if(!isDirectory()) {
                    postDeployArchive();
                }

                getManager().registerDescriptor(request.getName(), request.getDescriptor());

                // save the object type in optional attributes 
                Properties optionalAttributes = request.getOptionalAttributes();
                if (optionalAttributes == null) {
                    optionalAttributes = new Properties();
                }
                String resourceType = getResourceType(getAppDir());
                if(resourceType != null) {
                    optionalAttributes.setProperty(ServerTags.OBJECT_TYPE, 
                        resourceType);
                }

                request.setExternallyManagedApp(
                    isExternallyManagedApp(getAppDir()));
	}

	///////////////////////////////////////////////////////////////////////////
	
	private void postDeployArchive() throws ConfigException, IASDeploymentException
	{
	}

	///////////////////////////////////////////////////////////////////////////

        protected File setAppDir() throws IASDeploymentException {
            // brand-new deployment, so no versioning required.
            ApplicationEnvironment aenv = getAppEnv();
            File appDirectory;
            
            if(isArchive()) {
                File parent = new File(getInstanceEnv().getApplicationRepositoryPath());
                appDirectory = new File(parent, getAppName());
                appDirectory.mkdirs();
            }
            else if(isDirectory()) {
                FileSource fileSource = request.getFileSource();
                
                if(!fileSource.exists()) {
                    String msg = localStrings.getString(
                    "enterprise.deployment.backend.file_source_does_not_exist",
                    fileSource.toString() );
                    throw new IASDeploymentException( msg );
                }
                
                assert fileSource.isDirectory();
                appDirectory = fileSource.getFile();
            }
            else {
                String msg = localStrings.getString(
                "enterprise.deployment.backend.deployment_not_dir_or_archive");
                throw new IASDeploymentException( msg );
            }
            
            return appDirectory;
        }
	
	///////////////////////////////////////////////////////////////////////////

	protected String whatAreYou()
	{
		return "Deployment";
	}
	
	///////////////////////////////////////////////////////////////////////////

	protected final String getSuccessMessage()
	{
		return stars + "**** " + whatAreYou() + " successful for " + getAppName() + " ****" + stars;
	}
	
	///////////////////////////////////////////////////////////////////////////

	protected final String getFailureMessage()
	{
		return stars + "**** " + whatAreYou() + " failed for " + getAppName() + " ****" + stars;
	}

    ///////////////////////////////////////////////////////////////////////////
    /**
     * Runs the verifier on this module if verification is ON in
     * the deployment request.
     *
     * @throws  IASDeploymentException  if an error found in this module
     *                                  after verification
     */
    private void runVerifier() throws IASDeploymentException
    {
        if (request.isVerifying()) {
            try {
                String archive = request.getDeployedDirectory().getCanonicalPath();
                File jspDir = (request.getPrecompileJSP())? getJSPDir():null;
                new AppVerifier().verify(request.getDescriptor(),
                        (new FileArchiveFactory()).openArchive(archive),
                        request.getCompleteClasspath(),
                        jspDir);
            } catch (Exception e) {
                String msg = localStrings.getString(
                        "enterprise.deployment.backend.verifier_error");
                throw new IASDeploymentException(msg);
            }
        }
    }
	private final static String	stars = "\n*********************\n";	
        private DeploymentStatus J2EECPhaseStatus = null;
        private static StringManager localStrings =
            StringManager.getManager( AppDeployer.class );
        
}
