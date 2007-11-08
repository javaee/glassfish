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
 * Deployer.java
 *
 * Created on December 10, 2001, 11:25 PM
 */

package com.sun.enterprise.deployment.backend;

import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.util.ModuleDescriptor;
import java.io.*;
import java.util.List;	
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.*;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import org.xml.sax.SAXParseException;
import com.sun.ejb.codegen.IASEJBCTimes;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.DasConfig;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.deployment.Application;
import javax.enterprise.deploy.shared.ModuleType;
import com.sun.enterprise.deployment.autodeploy.AutoDeployConstants;
import com.sun.enterprise.deployment.archivist.ApplicationArchivist;
import com.sun.enterprise.deployment.archivist.Archivist;
import com.sun.enterprise.deployment.archivist.ArchivistFactory;
import com.sun.enterprise.deployment.deploy.shared.AbstractArchive;
import com.sun.enterprise.deployment.deploy.shared.DeploymentPlanArchive;
import com.sun.enterprise.deployment.deploy.shared.FileArchive;
import com.sun.enterprise.deployment.interfaces.DeploymentImplConstants;
import com.sun.enterprise.deployment.util.DeploymentProperties;
import com.sun.enterprise.deployment.util.ModuleContentLinker;
import com.sun.enterprise.instance.BaseManager;
import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.enterprise.instance.ModuleEnvironment;
import com.sun.enterprise.loader.EJBClassLoader;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.server.Constants;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.zip.ZipItem;
import java.util.Collection;

/**
 *
 * @author  bnevins
 * @version 
 */
public abstract class Deployer 
{

    public static final String WEB_INF_LIB_PREFIX = "WEB-INF/lib/";
    public static final String TEMP_DIRECTORY_SUFFIX = "__temp";
    public static final String FLAG_FILE = "META-INF" + File.separator 
        + "jbi.xml";

    // this collection contains all the undeleted files
    protected Collection<File> undeletedFiles = new ArrayList<File>(); 
   
    // to record the timestamp when we try to liquidate the files
    protected long liquidateTimeStamp;

    private static String CLIENT_JAR_MAKER_CHOICE = System.getProperty(
                            DeploymentImplConstants.CLIENT_JAR_MAKER_CHOICE);

	///////////////////////////////////////////////////////////////////////////
	/////////    Abstract Methods                                       ///////
	///////////////////////////////////////////////////////////////////////////

	/** @deprecated
	 */
	public abstract void doRequest() throws IASDeploymentException;

	public abstract void doRequestPrepare() throws IASDeploymentException;
	public abstract void doRequestFinish() throws IASDeploymentException;

	public abstract void cleanup_internal();
	protected abstract List getModuleClasspath(Archivist archivist,
                AbstractArchive archive) throws IASDeploymentException;

	///////////////////////////////////////////////////////////////////////////
	/////////    Public   Methods                                       ///////
	///////////////////////////////////////////////////////////////////////////
	public final void cleanup()
	{
		cleanup_internal();
	}	

	///////////////////////////////////////////////////////////////////////////
	
	public String toString()
	{
		return summary.toString();
	}

	/**@return the value of the boolean flag keepFailedStubsValue 
	 * wbn 4/03 -- WHY IS THIS PUBLIC???
	 */
	public static final boolean getKeepFailedStubsValue()
	{
		return keepFailedStubsValue;
	}

	///////////////////////////////////////////////////////////////////////////
	/////////    EVERYTHING below here is protected or private   //////////////
	///////////////////////////////////////////////////////////////////////////
	
	Deployer(DeploymentRequest r) throws IASDeploymentException
	{                        
            if(r == null) {
                String msg = localStrings.getString(
                "enterprise.deployment.backend.null_deployment_request_object");
                throw new IASDeploymentException( msg );
            }
            
            request = r;
            instanceEnv = request.getInstanceEnv();
            
            if(instanceEnv == null) {
                String msg = localStrings.getString(
                "enterprise.deployment.backend.null_instanceenvironment_in_deployment_request");
                throw new IASDeploymentException( msg );
            }
            
            // since we want to cache all of our schema validation
            // we need to make sure our grammar pool is initialized
            // DOLGrammarPool.initializeGrammarPool();

	}
	
	
	///////////////////////////////////////////////////////////////////////////
	
	protected final InstanceEnvironment getInstanceEnv()
	{
		return instanceEnv;
	}
	
	///////////////////////////////////////////////////////////////////////////

	protected final DeploymentRequest getRequest()
	{
		return request;
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	protected final boolean isDirectory() throws IASDeploymentException
	{
		return request.isDirectory();
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	protected final boolean isArchive() throws IASDeploymentException
	{
		return request.isArchive();
	}

	///////////////////////////////////////////////////////////////////////////
	
	protected void addToSummary(String s)
	{
		summary.append(s);
		summary.append("\n");
	}

	///////////////////////////////////////////////////////////////////////////
	
	protected void begin() throws IASDeploymentException
	{
		// start profiling...
		timeDeployStarted = System.currentTimeMillis();
		
		try
		{
			instanceEnv.verify();
		}
		catch(Exception e)
		{
			throw new IASDeploymentException(e);
		}
	} 
	
        /**
         * @return the validation level
         */
        public static String getValidationLevel() throws IASDeploymentException {
            
            init();
            return validationLevel;
        }
        
	/**
	 * @return a fully initialized and validated deployment descriptors for this 
	 * deployment request.
	 */
	protected Application loadDescriptors() throws IASDeploymentException {


	    // we will need this later	    
	    BaseManager manager = getManager();
	    if (manager==null) {
		throw new IASDeploymentException(
			localStrings.getString(
			"enterprise.deployment.backend.no_manager_registered",
			request.getType() ));		
	    }
	    
	    // look if we should validate the deployment descriptors
	    init();
	    
	    
	    boolean validateXml = true;
	    
	    try {
		Archivist archivist = ArchivistFactory.getArchivistForType(request.getType().getModuleType());
                archivist.setAnnotationProcessingRequested(true);

		String appDir = request.getDeployedDirectory().getAbsolutePath();
		FileArchive in = new FileArchive();
		in.open(appDir);

		if (request.isVerifying()) {
		    archivist.setRuntimeXMLValidation(true);
		    archivist.setRuntimeXMLValidationLevel("full");
		} 
		if (validationLevel.equals("none")) {
		    archivist.setXMLValidation(false);
		} else {
		    archivist.setXMLValidation(true);
		    archivist.setXMLValidationLevel(validationLevel);
		}		

                //Note in copying of deployment plan to the portable archive,
                //we should make sure the manifest in the deployment plan jar
                //file does not overwrite the one in the original archive
                //NOTE. We are not checking the size of the deploymentPlan file
                //here, since it looks like on windows the size of this tmp
                //file is always zero when we attempt to query its size.  
                //Instead, we are making sure the 0 length deployment plan is
                //not uploaded in DeploymentFacility implementation.
                if (request.getDeploymentPlan() != null) {
                    DeploymentPlanArchive dpa = new DeploymentPlanArchive();
                    dpa.open(request.getDeploymentPlan().getAbsolutePath());

                    if (request.isApplication()) {
                        ApplicationArchivist aa = (ApplicationArchivist)archivist;
                        aa.copyInto(request.getDescriptor(), dpa, in, false);
                    } else { 
                        archivist.copyInto(dpa, in, false);
                    }
                }
		
		// now let's create a class loader for this module
		
		// parent class loader (from admin server)
        ClassLoader parent =
            (Boolean.getBoolean(com.sun.enterprise.server.PELaunch.USE_NEW_CLASSLOADER_PROPERTY))
                    ? com.sun.enterprise.server.PELaunch.getAppServerChain()
                    : ClassLoader.getSystemClassLoader();

		// sets the parent class loader and class paths in deployment req		
            	DeploymentUtils.setParentClassLoader(parent, getManager(), request);
		// parent class paths for this deployment request
		List allClassPaths = request.getParentClasspath();

		// parent class loader for this deployment request
		ClassLoader sharedClassLoader = request.getParentClassLoader();

		List moduleClasspath = getModuleClasspath(archivist, in);
	        request.setModuleClasspath(moduleClasspath);

		allClassPaths.addAll(moduleClasspath);

		final ClassLoader ejbClassLoader = DeploymentUtils.getClassLoader(
                                        moduleClasspath, sharedClassLoader, null);

		// sets the ejb class loader & class paths - used during jspc
		request.setEjbClassLoader(ejbClassLoader);
		request.setCompleteClasspath(allClassPaths);

                // set classloader used for annotation processing
                archivist.setClassLoader(ejbClassLoader);
      
                // set the context classloader to ejbClassLoader for DD 
                // processing 
                ClassLoader origContextClassLoader = 
                    Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(ejbClassLoader);

                Application application = request.getDescriptor();
                if (application!=null && ModuleType.EAR.equals(archivist.getModuleType())) {
                    archivist.readPersistenceDeploymentDescriptors(in, application);
                    // Now process standard DDs, do this before runtime DDs
                    archivist.setHandleRuntimeInfo(false);
                    boolean modulesReadSuccess = ((ApplicationArchivist) archivist).readModulesDescriptors(application, in);
                    if (modulesReadSuccess) {
                        // now process runtime DDs
                        archivist.setHandleRuntimeInfo(true);
                        archivist.readRuntimeDeploymentDescriptor(in, application);
                    } else {
                        // it failed reading sub modules, I null our application which will trigger
                        // deployment failure handling
                        application=null;
                    }
                } else {
                    application = ApplicationArchivist.openArchive(archivist, in, true);
                }
                if (application==null) {
                    throw new IASDeploymentException(localStrings.getString(
                            "enterprise.deployment.backend.error_loading_dds", 
                            new Object[] { request.getName(), " " }));
                }
		application.setRegistrationName(request.getName());
		
		application.setClassLoader(ejbClassLoader);
                archivist.setDescriptor(application);
  
                // let's check for optional dependencies
                if (!archivist.performOptionalPkgDependenciesCheck(in)) {
                    throw new IASDeploymentException(localStrings.getString("enterprise.deployment.backend.archive_opt_dep_not_satisfied", new Object[] {in.getArchiveUri()}));
                }
                                
		archivist.validate(ejbClassLoader);
		
		if (!application.getWebServiceDescriptors().isEmpty()) {
		    ModuleContentLinker visitor = new ModuleContentLinker(in);
		    application.visit((com.sun.enterprise.deployment.util.ApplicationVisitor) visitor);
		}      
		request.setDescriptor(application);	
                
                // restore the original context class loader
                Thread.currentThread().setContextClassLoader(
                    origContextClassLoader);

		return application;
	    } catch(SAXParseException spe) {
		IASDeploymentException e =  new IASDeploymentException(
			localStrings.getString(
			"enterprise.deployment.backend.saxerror_loading_dds",
			request.getName(),String.valueOf(spe.getLineNumber()), 
                            String.valueOf(spe.getColumnNumber()), spe.getLocalizedMessage() ));
		e.initCause(spe);
		throw e; 
            } catch(IASDeploymentException e) {
                throw e;
            } catch (Throwable t) {
		IASDeploymentException e =  new IASDeploymentException(
			localStrings.getString(
			"enterprise.deployment.backend.error_loading_dds",
			request.getName(), t.getMessage() ));
		e.initCause(t);
		throw e;
	    }
	}	
        
        protected void copyAutodeployedClassFile(File srcFile, File destDir)
            throws IOException {

            File autodeployDir = srcFile.getParentFile();
            String packageNameDirs="";
            while (!autodeployDir.getName().equals(AutoDeployConstants.DEFAULT_AUTODEPLOY_DIR)) {
                packageNameDirs=autodeployDir.getName()+ File.separator + packageNameDirs;
                autodeployDir = autodeployDir.getParentFile();
            }
            destDir = new File(destDir, packageNameDirs);
            File destFile = new File(destDir, srcFile.getName());
            FileUtils.copy(request.getFileSource().getFile(), destFile);          
        }
	
        protected final void createClientJar(ZipItem[] clientStubs) throws IASDeploymentException {
            
            Properties props = request.getOptionalArguments();
            String clientJarRequested = props.getProperty(DeploymentProperties.CLIENTJARREQUESTED);

            // destination file for the client jar file
            File appDirectory = request.getGeneratedXMLDirectory();

            // for upgrade scenario, we fall back to original directory
            if (appDirectory == null || 
                !FileUtils.safeIsDirectory(appDirectory)) {
                appDirectory = request.getDeployedDirectory();
            }
            File clientJar = new File(appDirectory, request.getName() 
                + DeploymentImplConstants.ClientJarSuffix);
            
            // now we look if the client jar file is being requested by the client
            // tool
            if (clientJarRequested!=null && 
                    Boolean.valueOf(clientJarRequested).booleanValue()) {
                
                // the client jar file is requested upon deployment,
                // we need to build synchronously                
                ClientJarMakerThread.createClientJar(
                    request, clientJar, clientStubs, CLIENT_JAR_MAKER_CHOICE);
            
                // sets the newly created client jar in the deployment request
                request.setClientJar(clientJar);
            } else {
                
                // the client jar file is not requester, we build it asynchronously.
                Thread clientJarThread = new ClientJarMakerThread(
                    request, clientJar, clientStubs, CLIENT_JAR_MAKER_CHOICE);
                clientJarThread.start();
                
                // done !
            }
        }       
        
	
	/**
	 * Perform class level initialization. I do not want to put it
	 * in the static initializer since the config beans I am accessing
	 * may have not been initialized properly when the class loader 
	 * invokes the static initializer.
	 */
	private static void init() throws IASDeploymentException {
	    
	    // already initialized
	    if (validationLevel!=null) {
		return;
	    }
	    
	    try {
		ConfigContext ctx = ApplicationServer.getServerContext().getConfigContext();
		DasConfig dc = ServerBeansFactory.getDasConfigBean(ctx);
		validationLevel = dc.getDeployXmlValidation();
	    } catch(ConfigException ce) {
		IASDeploymentException e =  new IASDeploymentException(
			localStrings.getString(
			"enterprise.deployment.backend.cannot_get_validationlevel",
			ce.getMessage() ));
		e.initCause(ce);
		throw e;
	    }		
		
	}	    
	    
	/**
	 * @author bnevins
	 * 9-23-03 for S1AS 8.0PE
	 * The namespace has been flattened for 8.0
	 * This method is called to see if a name is registered for a different
	 * type of J2EE Deployable Object.  Deployment Backend itself will check the
	 * Manager for the same type of object to see if it is a re-deploy.  If that
	 * check is negative, it will call here to check all the other types.  If a different
	 * type is registered with the same name, Deployment will fail the deployment
	 * instead of wiping out the other app/module with the same name.
	 * @throws IASDeploymentException if it is registered elsewhere already.
	 */
	
	void checkRegisteredAnywhereElse(String id) throws IASDeploymentException
	{
		String error = BaseManager.isRegisteredAnywhere(instanceEnv, id);
		
		if(error != null)
		{
			String msg = localStrings.getString("enterprise.deployment.backend.nameAlreadyExists", new Object[] { id, error});
			throw new IASDeploymentException(msg);
		}
	}
		    

	///////////////////////////////////////////////////////////////////////////
	
	protected void addEJBCTime(IASEJBCTimes ejbcT)
	{
		ejbcTime += ejbcT.getTotalTime();
		ejbcTiming = ejbcT;
	}
	///////////////////////////////////////////////////////////////////////////
	
	protected void addJSPCTime(long time)
	{
		jspcTime += time;
	}

	///////////////////////////////////////////////////////////////////////////
	protected final void finish() throws IASDeploymentException
	{
		long	total		= System.currentTimeMillis() - timeDeployStarted;
		
		if(total <= 0)
			total = 1;
		
		int		percentEJBC	= (int)(((double)ejbcTime) / ((double)total) * 100.0);
		int		percentJSPC	= (int)(((double)jspcTime) / ((double)total) * 100.0);
		
		deleteLockFile();
		StringBuffer timing	= new StringBuffer("Total Deployment Time: ");
		timing.append(total);
		timing.append(" msec, ");
		if(jspcTime > 0)
		{
			timing.append("Total JSP Compile Time: ");
			timing.append(jspcTime);
			timing.append(" msec (").append(percentJSPC).append("%),  ");
		}
		timing.append("Total EJB Compiler Module Time: ");
		timing.append(ejbcTime);
		timing.append(" msec, Portion spent EJB Compiling: ");
		timing.append(percentEJBC);
		timing.append("%");
		
		
		
		if(ejbcTiming != null)
			timing.append("\nBreakdown of EJBC Module Time: " + ejbcTiming);
		
		logger.fine(timing.toString());
		logger.finer(toString());

            // helps garbage collection
            releaseClassLoader();
	}

	///////////////////////////////////////////////////////////////////////////

    public final void releaseClassLoader() 
    {
        try 
        {
            // releases the parent class loader
            ClassLoader parentCL = request.getParentClassLoader();
            if ( (parentCL != null) && (parentCL instanceof EJBClassLoader) )
            {
                ((EJBClassLoader) parentCL).done();
            }
            
            // releases the ejb class loader
            ClassLoader ejbCL = request.getEjbClassLoader();
            if ( (ejbCL != null) && (ejbCL instanceof EJBClassLoader) )
            {
                ((EJBClassLoader) ejbCL).done();
            }
            
        } 
        catch (Exception e) 
        {
            // ignore
        }

    }

	///////////////////////////////////////////////////////////////////////////

	/** This is a backdoor designed for QA and support staff.
	 * If the magical environmental variable, "KeepFailedStubs",
	 * is set to "true", then this method returns true.
	 * It results in the generated stubs files being retained and
	 * place into the expected directory with "_failed" appended to the name.  The
	 * Directory will be deleted and replaced the next time there
	 * is a failed deployment.  It will never be automatically cleaned up.
	 * Note:  KeepFailedStubs and true are both case insensitive.
	 * <p>This method will do the following, if the flag is set:
	 * <br>delete already existing _fail directory, if any
	 * <br>rename the given stubsdir to xxx_failed
	 * <p>If the flag is not set, it will wipeout the stubsDir recursively.
	 * @param stubsDir The directory with the generated ejb files that would normally be wiped out.
	 * @returns true if the backdoor is open.
	 */	
	protected void DeleteOrKeepFailedStubs(File stubsDir)
	{
		//4704059 - backdoor for analyzing stubs
		if(!FileUtils.safeIsDirectory(stubsDir))
			return;	// not an error
		
		if(keepFailedStubsValue)
		{
			File failedStubsDir = new File(stubsDir.getPath() + FAILED_SUFFIX);				

			// blow-away any already existing failed dir
			FileUtils.whack(failedStubsDir);

			stubsDir.renameTo(failedStubsDir);
			logger.info(Constants.KEEP_FAILED_STUBS + " is set.  Backdoor is open.  Saving failed generated ejb files in: " + failedStubsDir.getPath());
		}
		else
		{
			FileUtils.whack(stubsDir);
		}
	}

        /**
         *Cleans out a directory, logging a warning          
         *(such as deployment, undeployment, etc.) if the directory
         *remains.
         *@param dir the directory File to be cleaned out
         *@throws IASDeploymentException if the directory is not cleaned out successfully
         */
        protected void cleanAndCheck(File dir) {
            if(FileUtils.safeIsDirectory(dir)) {
                FileUtils.whack(dir, undeletedFiles);
            }
        }

        // let's check whether the previously undeleted files have been 
        // replaced in this deployment. 
        // if not, let's print a warning to the user
        protected void handleUndeletedFiles() {
            // all files have been deleted and nothing needs to be done
            if (undeletedFiles.size() == 0) {
                return;
            }

            // log a fine message for all undeleted files for debug purpose
            String allUndeletedFilesMsg = localStrings.getString("enterprise.deployment.backend.all_undeleted_files", FileUtils.formatFileCollection(undeletedFiles));
            logger.fine(allUndeletedFilesMsg);

            for (Iterator<File> files = undeletedFiles.iterator(); files.hasNext();) {
                File file = files.next();
                if (file.lastModified() > liquidateTimeStamp ) {
                    files.remove(); 
                }
            }

            if (undeletedFiles.size() > 0) {
                // Try to delete the left-over files using gc (on Windows).
                // In any case, mark files that still remain for delete-on-exit.
                if ( ! FileUtils.deleteLeftoverFiles(undeletedFiles) ) {
                    String untouchedUndeletedFilesMsg = localStrings.getString("enterprise.deployment.backend.untouched_undeleted_files", FileUtils.formatFileCollection(undeletedFiles));
                    DeploymentStatus handleUndeletedFilesStatus = 
                        new DeploymentStatus(request.getCurrentDeploymentStatus());
                    handleUndeletedFilesStatus.setStageStatus(
                        DeploymentStatus.WARNING);
                    handleUndeletedFilesStatus.setStageStatusMessage(
                        untouchedUndeletedFilesMsg);
                    // also logged in the server.log for autodeploy
                    logger.warning(untouchedUndeletedFilesMsg);
                }
            }
        }

    ///////////////////////////////////////////////////////////////////////////

    protected String getResourceType(File appDir)
    {
        FileInputStream fis = null;
        try{
            File manifestFile = new File(appDir, JarFile.MANIFEST_NAME);
            fis = new FileInputStream(manifestFile);
            Manifest manifest = new Manifest(fis);
            Attributes attrs = manifest.getMainAttributes();
            return attrs.getValue(Constants.APPLICATION_TYPE);
        }catch(Throwable t){
            // by default resource-type will be assigned "user". Need just return null;
            return null;
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException ioe) {
            }
        }
    }

    // for 9.1, we only have jbi JavaEE service units 
    // as externally managed
    protected boolean isExternallyManagedApp(File appDir) {
        File file = new File(appDir, FLAG_FILE);
        return file.exists();
    }

	///////////////////////////////////////////////////////////////////////////

	private final void deleteLockFile()
	{	
		if(lockFile != null)
			lockFile.delete();
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	/** 
	 * @return the BaseManager implementation for this deployer
	 */
	protected abstract BaseManager getManager();

    /**
     *Makes sure each app client specifies Main-Class in its manifest.
     *Logs warnings for any client that fails to do so.
     */
    protected boolean checkAppclientsMainClasses() {
        /*
         *Use the ModuleDescriptors from getModules to obtain the URIs for 
         *use in the warning if needed.
         */
        boolean result = true;
        StringBuilder sb = new StringBuilder();
        for (Iterator it = request.getDescriptor().getModules(); it != null && it.hasNext(); ) {
            ModuleDescriptor md = (ModuleDescriptor) it.next();
            if (md.getModuleType() == ModuleType.CAR) {
                String archiveURI = md.getArchiveUri();
                ApplicationClientDescriptor acd = (ApplicationClientDescriptor) md.getDescriptor();
                String mainClassName = acd.getMainClassName();
                if (mainClassName == null || mainClassName.length() == 0) {
                    result = false;
                    logger.log(Level.WARNING, localStrings.getString("enterprise.deployment.backend.no_main_class"), archiveURI);
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(archiveURI);
                }
            }
        }
        if ( ! result) {
            /*
             *At least one app client failed to identify its main class.  Add a
             *deployment sub-status warning of the problem.
             */
            DeploymentStatus mainClassesNotOKStatus = new DeploymentStatus(request.getCurrentDeploymentStatus());
            mainClassesNotOKStatus.setStageDescription(localStrings.getString("enterprise.deployment.backend.appclient_mainclass_checking"));
            mainClassesNotOKStatus.setStageStatus(DeploymentStatus.WARNING);
            mainClassesNotOKStatus.setStageStatusMessage(localStrings.getString("enterprise.deployment.backend.appclient_mainclass_checking_failed", sb.toString()));
        }
        return result;
    }

	protected				DeploymentRequest		request;
	protected				Logger					logger				= DeploymentLogger.get();
	protected				StringBuffer			summary				= new StringBuffer();
	private					InstanceEnvironment		instanceEnv;
	private					ModuleEnvironment		moduleEnv;
	private					long					timeDeployStarted;
	private					long					ejbcTime			= 0;
	private					long					jspcTime			= 0;
	private					File					lockFile			= null;
	private static			StringManager			localStrings		= StringManager.getManager( Deployer.class );
	private static final	boolean					keepFailedStubsValue;
	private final static	String					FAILED_SUFFIX		= "_failed";
	private static 	String						validationLevel=null;
	private					IASEJBCTimes			ejbcTiming			= null;
	static
	{
		String s = DeploymentUtils.getSystemPropertyIgnoreCase(Constants.KEEP_FAILED_STUBS);
		
		if(s != null && s.compareToIgnoreCase("true") == 0)
			keepFailedStubsValue = true;
		else
			keepFailedStubsValue = false;

		
		//Reminder.message("KeepFailedStubs is: " + keepFailedStubsValue);
	}
    /* HARRY JACC Changes  
       TBD: A different exception in line with ias7 should be propagated ahead 
       instead of classnotfound.
    */
    protected void generatePolicy() throws IASDeploymentException

	{
	// Each subtype of deployer, overrides this
    }

    public abstract void removePolicy() throws IASDeploymentException;
}
