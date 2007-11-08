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
 * DeploymentRequest.java
 *
 * Created on December 10, 2001, 11:29 PM
 */

package com.sun.enterprise.deployment.backend;
import com.sun.enterprise.loader.EJBClassLoader;
import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.enterprise.deploy.spi.Target;
import com.sun.enterprise.util.io.*;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.enterprise.instance.ApplicationEnvironment;
import com.sun.enterprise.instance.ModuleEnvironment;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.util.diagnostics.ObjectAnalyzer;
import com.sun.enterprise.instance.AppsManager;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.server.Constants;
import com.sun.enterprise.management.deploy.DeploymentCallback;

/**
 *
 * @author  bnevins
 * @version 
 */

public class DeploymentRequest 
{
    private boolean isDone = false;
    
	///////////////////////////////////////////////////////////////////////////
	/////////  Public Methods       ///////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////

	/** Create a deployment request object.  The constructor must be called with the three
	 * arguments required for all deployment requests.  You must call other methods
	 * to set attributes required for <i>some</i> requests.
	 * @parameter iEnv InstanceEnvironment object
	 * @parameter theCommand The command (Deploy Redeploy Undeploy) of the request
	 * @parameter theType The type of deployment (Application, Web Module, EJB Module, Connector Module
	 */
	public DeploymentRequest(
		InstanceEnvironment		iEnv,
		DeployableObjectType	theType, 
		DeploymentCommand		theCommand)
		//File					source,	// directory or archive here - optional for undeploy
		//String					theName)
		throws IASDeploymentException
	{
		// order counts -- don't change it unless you know what you're doing!
		instanceEnv = iEnv;
		setType(theType);
		setCommand(theCommand);
	}
	
	///////////////////////////////////////////////////////////////////////////

	/** Makes sure everything that needs to be setup is OK.  Use it
	 * just before deploying
	 * WBN: 4-26-02 added code to change Deploy to Redeploy if neccessary
	 */
	public void verify() throws IASDeploymentException
	{
		if(isVerified)
			return;
		
		if(isFileSourceRequired() && fileSource == null) {
			String msg = localStrings.getString(
					"enterprise.deployment.backend.file_source_required" );
			throw new IASDeploymentException( msg );
		}
	
		if(isNameRequired() && name == null) {
			String msg = localStrings.getString(
					"enterprise.deployment.backend.name_required" );
			throw new IASDeploymentException( msg );
		}
		
		if(isContextRootRequired() && contextRoot == null &&
                    defaultContextRoot == null) {

			String msg = localStrings.getString(
					"enterprise.deployment.backend.context_root_required" );
			throw new IASDeploymentException( msg );
		}
		
		if(name == null) // this is true iff no name is required to be supplied
			createName();
		
		// Bug 4679970 -- we must have a file-system-legal name.
		// we have a contract with other code that there will be a client jar
		// whose name is <app-name>Client.jar
		// thus AppName: "foo?<*//x" --> "foo?<*//xClient.jar" -- impossible to create!!
		
		if(!FileUtils.isLegalFilename(name))
		{
			// 2 approaches -- (1) don't allow it and (2) change the name
			// We're going with approach (1) for now...
			
			// approach (1)
			String msg = localStrings.getString(
				"enterprise.deployment.backend.illegal_characters_in_component_name",
				FileUtils.getIllegalFilenameCharacters() );
			throw new IASDeploymentException( msg );
			// approach (2)
			//name = FileUtils.makeLegalFilename(name);
		}
	
		setEnv();
		checkForRedeploy();
		isVerified = true;
	}
	
	/** sets the Name of the App or Module.  If you don't call this, and if it's legal, 
	 * one will be provided for you at no cost.
	 * <br>Deploy - not required
	 * <br>Redeploy - required
	 * <br>Undeploy - required
	 * @param theName String name of module or application
	 */
	public void setName(String theName) throws IASDeploymentException
	{
		if(StringUtils.ok(theName))
			name = theName;

		else if(isNameRequired()) {
			String msg = localStrings.getString(
					"enterprise.deployment.backend.null_setname" );
			throw new IASDeploymentException( msg );
		}
	}

	/** Sets the source of deployment files.
	 * <br>Deploy - required
	 * <br>Redeploy - required
	 * <br>Undeploy - not required
	 * @param src String pointing at Archive file or root-dir for user-specified
	 * 'pre-exploded' deployment
	 */
	public void setFileSource(String src)  throws IASDeploymentException
	{
		setFileSource(new File(src));
	}
	
	/** Sets the source of deployment files.
	 * <br>Deploy - required
	 * <br>Redeploy - required
	 * <br>Undeploy - not required
	 * @param src File Object pointing at Archive file or root-dir for user-specified
	 * 'pre-exploded' deployment
	 */
	public void setFileSource(File src)  throws IASDeploymentException
	{
		fileSource = null;
		
		if(src == null && !isFileSourceRequired())
			return;	// all OK...
		
		try
		{
			fileSource = new FileSource(src);
		}
		catch(Exception e)
		{
			if(isFileSourceRequired())
				throw new IASDeploymentException("DeploymentRequest.setFileSource()" + e);
		}
	}


         public void setDeploymentPlan(File plan) {
             deploymentPlan = plan;
         }

         public File getDeploymentPlan() {
             return deploymentPlan;
         }

         public void setDeploymentCallback(DeploymentCallback callback) {
             deploymentCallback = callback;
         }

         public DeploymentCallback getDeploymentCallback() {
             return deploymentCallback;
         }

         public void setAbort(boolean isAborted) {
             this.isAborted = isAborted;
         }

         public boolean isAborted() {
             return isAborted;
         }

         public void setExternallyManagedApp(boolean isExtManagedApp) {
             isExternallyManagedApp = isExtManagedApp;
         }
                                                                                
         public boolean isExternallyManagedApp() {
             return isExternallyManagedApp;
         }

         public void setExternallyManagedPath(boolean isExtManagedPath) {
             isExternallyManagedPath = isExtManagedPath;
         }
                                                                                
         public boolean isExternallyManagedPath() {
             return isExternallyManagedPath;
         }


         public void setReload(boolean isReload) {
             this.isReload = isReload;
         }

         public boolean isReload() {
             return isReload;
         }

         public void setTargetName(String name)
         {
             targetName = name;
         }

         public String getTargetName()
         {
             return targetName;
         }

	///////////////////////////////////////////////////////////////////////////

	/** sets the 'shared' attribute
	 * @param shared true for shared Ejb modules, false for unshared Ejb modules
	 */
	public void setShared(boolean newShared) throws IASDeploymentException
	{
		if(!isEjbModule()) {
			String msg = localStrings.getString(
					"enterprise.deployment.backend.cannot_set_shared_flag" );
			throw new IASDeploymentException( msg );
		}
			
		shared = newShared;
	}
	/** sets the 'forced' attribute.  If set to true - no error occurs if the App or Module is
	 * already registered.  If false, an Exception will be thrown if the App or Module is already 
	 * deployed.
	 * @param newForced true for forcing Deployment of already registered Apps/Modules.  
	 * I.e. a Redeployment
	 */
	public void setForced(boolean newForced)
	{
		forced = newForced;
	}
	/**
	* Adds the optional arguments for this request.  Currently - these are CMP-only args.
	* It would have been nice to add these args to the pre-existing
	* optionalAttributes.  But those attributes are all blindly added to Config by
	* classes in the instance package.  It would have been a KLUDGE-fest to separate
	* the different name-value pairs in this class and then repackage.  Which is why
	* we have yet-another Properties object.
	* bnevins 4/3/2003
	*/
	public void addOptionalArguments(Properties props)
	{
		/* We want to be slightly smart here.  If this method is called more than once,
		* we can't just wipe-out the earlier mappings!
		* So we keep our own Properties object and always ADD to it...
		* If this method is never called, optionalArguments will be empty but not null.
		*/
			optionalArguments.putAll(props);
		}

        public void addOptionalArguments(Map map)
        {
            optionalArguments.putAll(map);
        }

	/**
	* Adds one optional argument.
	* @see addOptionalArguments
	*/
	public void addOptionalArgument(String key, String value)
	{
		optionalArguments.put(key, value);
	}

	 ///////////////////////////////////////////////////////////////////////////

	/** 
	 * sets the 'precompile-jsp' attribute.  If set to true - JSP precompilation is
	 * performed, if neccessary.  If the precompile has errors, the deployment will
	 * officially fail.  This feature is by default, false.
	 * @param precompileJSP true for performing JSP compilation at Deployment Time.
	 */
	public void setPrecompileJSP(boolean newPrecompileJSP)
	{
		precompileJSP = newPrecompileJSP;
	}

	/** 
	 * @returns whether or not precompiling JSP is enabled -- the default is false.
	 */
	public boolean getPrecompileJSP()
	{
		return precompileJSP;
	}

        /**
         * sets the 'generate-rmi-stubs' attribute. 
         * @param newGenerateRMIStubs
         */     
        public void setGenerateRMIStubs(boolean newGenerateRMIStubs)
        {
                generateRMIStubs = newGenerateRMIStubs; 
        }
         
        /**
         * @returns the 'generate-rmi-stubs' attribute.
         */     
        public boolean getGenerateRMIStubs()
        {
                return generateRMIStubs;
        }

       /**
         * sets the 'availability-enabled' attribute.       
         * @param newAvailabilityEnabled    
         */     
        public void setAvailabilityEnabled(boolean newAvailabilityEnabled)
        {
                availabilityEnabled = newAvailabilityEnabled;
        }
        
        /**
         * @returns the 'availability-enabled' attribute.      
         */
        public boolean isAvailabilityEnabled()
        {
                return availabilityEnabled;
        }

       /**
         * sets the 'java-web-start-enabled' attribute.
         * @param newJavaWebStartEnabled    
         */     
        public void setJavaWebStartEnabled(boolean newJavaWebStartEnabled)
        {
                javaWebStartEnabled = newJavaWebStartEnabled;
        }    
         
        /**
         * @returns the 'java-web-start-enabled' attribute.
         */  
        public boolean isJavaWebStartEnabled()
        {
                return javaWebStartEnabled;
        }

       /**
         * sets the 'libraries' attribute.
         * @param newLibraries    
         */
        public void setLibraries(String newLibraries)
        {
                libraries = newLibraries;
        }

        /**
         * @returns the 'libraries' attribute.
         */
        public String getLibraries()
        {
                return libraries;
        }

        /**
         * @returns the 'resourceAction' attribute.
         */
        public String getResourceAction()
        {
                return resourceAction;
        }

        /**
         * sets the 'resourceAction' attribute.
         * @param newResourcesAction
         */
        public void setResourceAction (String newResourceAction)
        {
                resourceAction = newResourceAction;
        }

        /**
         * sets the 'targetList' attribute.
         * @param newTargetList
         */
        public void setResourceTargetList (String newTargetList)
        {
                resourceTargetList = newTargetList;
        }

        /**
         * @returns the 'targetList' attribute.
         */
        public String getResourceTargetList()
        {
                return resourceTargetList;
        }

	///////////////////////////////////////////////////////////////////////////

	/** gets the 'forced' attribute.
	 * @return forced attribute 
	 */
	public boolean isForced()
	{
		return forced;
	}
	
	///////////////////////////////////////////////////////////////////////////

	public File getClientJar()
	{
		return clientJar;
	}
	
	///////////////////////////////////////////////////////////////////////////

	public boolean isApplication()
	{
		return DeployableObjectType.APP.equals(type);
	}

	/** Set the Context Root for Web Modules
	 */
	public void setContextRoot(String s)
	{
		contextRoot = s;
	}
	
	///////////////////////////////////////////////////////////////////////////

	public String getContextRoot()
	{
		return contextRoot;
	}
	
        /** Set the default Context Root for Web Modules
         */
        public void setDefaultContextRoot(String s)
        {
                defaultContextRoot = s;
        }

        public String getDefaultContextRoot()
        {
                return defaultContextRoot;
        }


	///////////////////////////////////////////////////////////////////////////

	public boolean isModule()
	{
		return isWebModule() || isEjbModule() || isConnectorModule() || isAppClientModule();
	}
	
	///////////////////////////////////////////////////////////////////////////

	public boolean isSharedModule()
	{
		return isModule() && shared;
	}
	
	///////////////////////////////////////////////////////////////////////////

	public boolean isStandAloneModule()
	{
		return isModule() && !shared;
	}
	
	///////////////////////////////////////////////////////////////////////////

	public boolean isWebModule()
	{
		return DeployableObjectType.WEB.equals(type);
	}
	
	///////////////////////////////////////////////////////////////////////////

	public boolean isEjbModule()
	{
		return DeployableObjectType.EJB.equals(type);
	}
	
	///////////////////////////////////////////////////////////////////////////

	public boolean isConnectorModule()
	{
		return DeployableObjectType.CONN.equals(type);
	}
	
	///////////////////////////////////////////////////////////////////////////

	public boolean isAppClientModule()
	{
		return DeployableObjectType.CAR.equals(type);
	}	
	

	///////////////////////////////////////////////////////////////////////////

	public boolean isDeploy() throws IASDeploymentException
	{
		if(command == null) 
		{
			String msg =localStrings.getString(	"enterprise.deployment.backend.no_command");
			throw new IASDeploymentException( msg );
		}
		// setCommand, called from the ctor already verified command
		// if this assert fires that means a programmer must have set the
		// command back to null -- Programmer Error
		//assert command != null;	
		
		// check for null again -- this might be a release build!
		//if(command == null)
			//return false;
		
		return command.equals(DeploymentCommand.DEPLOY);
	}
	
	///////////////////////////////////////////////////////////////////////////

	public boolean isReDeploy() throws IASDeploymentException
	{
		if(command == null) 
		{
			String msg =localStrings.getString(	"enterprise.deployment.backend.no_command");
			throw new IASDeploymentException( msg );
		}
		
		// setCommand, called from the ctor already verified command
		// if this assert fires that means a programmer must have set the
		// command back to null -- Programmer Error
		//assert command != null;	
		
		// check for null again -- this might be a release build!
		//if(command == null)
			//return false;
		
		return command.equals(DeploymentCommand.REDEPLOY);
	}
	
	///////////////////////////////////////////////////////////////////////////

	public boolean isUnDeploy() throws IASDeploymentException
	{
		if(command == null) 
		{
			String msg =localStrings.getString(	"enterprise.deployment.backend.no_command");
			throw new IASDeploymentException( msg );
		}
		
		// setCommand, called from the ctor already verified command
		// if this assert fires that means a programmer must have set the
		// command back to null -- Programmer Error
		//assert command != null;	
		
		// check for null again -- this might be a release build!
		//if(command == null)
			//return false;
		
		return command.equals(DeploymentCommand.UNDEPLOY);
	}
	
	///////////////////////////////////////////////////////////////////////////

	public boolean isArchive() throws IASDeploymentException
	{
		if(isUnDeploy()) {
			String msg = localStrings.getString(
					"enterprise.deployment.backend.invalid_isarchive_call" );
			throw new IASDeploymentException( msg );
		}

		return getFileSource().isArchive();
	}
	
	///////////////////////////////////////////////////////////////////////////

	public boolean isDirectory() throws IASDeploymentException
	{
		if(isUnDeploy())
			//throw new IASDeploymentException("Internal Error -- invalid to call isDirectory() for an Undeploy");
			return false;
		
		return getFileSource().isDirectory();
	}

	///////////////////////////////////////////////////////////////////////////

	public String getName()
	{
		return name;
	}

	///////////////////////////////////////////////////////////////////////////

	public String toString()
	{
		return ObjectAnalyzer.toString(this);
	}

	///////////////////////////////////////////////////////////////////////////

	public void setDebug(boolean what)
	{
		debug = true;
	}

	///////////////////////////////////////////////////////////////////////////

	public boolean getDebug()
	{
		return debug;
	}

	///////////////////////////////////////////////////////////////////////////

	public void setNoEJBC()
	{
		noEJBC = true;
	}

	///////////////////////////////////////////////////////////////////////////


	public boolean getNoEJBC()
	{
		return noEJBC;
	}

	///////////////////////////////////////////////////////////////////////////

    /**
     * Deployment backend runs verifier during deployment when set to true.
     *
     * @param    what   when true, deployment backend runs verifier
     */
    public void setVerifying(boolean what)
    {
        isVerifying = what;
    }
    /**
     * Returns a boolean indicating whether this application is going 
     * to be verified during deployment.
     *
     * @return    when true, deployment backedn runs verifier
     */
    public boolean isVerifying()
    {
        return isVerifying;
    }
    /**
     * Return the App or Module Directory so that ownership can be modified, if neccessary.
     * @return The full path root directory of the deployed directory
     */
    public File getDeployedDirectory()
    {
		if(!FileUtils.safeIsDirectory(deployedDirectory))
			deployedDirectory = null;
		
        return deployedDirectory;
    }
    /**
     * Return the Stubs Directory so that ownership can be modified, if neccessary.
     * @return The full path stubs directory root.
     */
    public File getStubsDirectory()
    {
		if(!FileUtils.safeIsDirectory(stubsDirectory))
			stubsDirectory = null;
		
        return stubsDirectory;
    }
    /**
     * Return the Stubs Directory so that ownership can be modified, if neccessary.
     * @return The full path stubs directory root.
     */
    public File getJSPDirectory()
    {
		if(!FileUtils.safeIsDirectory(jspDirectory))
			jspDirectory = null;
		
        return jspDirectory;
    }


    /**
     * Return the Directory where the modified xml files will be located.
     * @return The full path generated xml directory root.
     */
    public File getGeneratedXMLDirectory()
    {
                if(!FileUtils.safeIsDirectory(generatedXMLDirectory))
                        generatedXMLDirectory = null;

        return generatedXMLDirectory;
    }

	///////////////////////////////////////////////////////////////////////////
	/////////  Package Methods      ///////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////

	/* package */ void setClientJar(File f) //throws IASDeploymentException
	{
		/*
		File p = f.getParentFile();
		
		if(!FileUtils.safeIsDirectory(f))
			throw new IASDeploymentException("Illegal parameter to setClientJarFile().  " +
				"The file (" + FileUtils.safeGetCanonicalPath(f) + ") has no existing parent directory.");
		*/
		clientJar = f;
	}
	
	///////////////////////////////////////////////////////////////////////////

	public FileSource getFileSource()
	{
		return fileSource;
	}
	
	///////////////////////////////////////////////////////////////////////////

	public DeployableObjectType getType()
	{
		return type;
	}
	
	///////////////////////////////////////////////////////////////////////////

	/* package */ DeploymentCommand getCommand()
	{
		return command;
	}
	
	///////////////////////////////////////////////////////////////////////////
/**
	String getInstanceName()
	{
		return instanceEnv.getName();
	}
*/	
	///////////////////////////////////////////////////////////////////////////

	public InstanceEnvironment getInstanceEnv()
	{
		return instanceEnv;
	}
	
	///////////////////////////////////////////////////////////////////////////

	/* package */ ApplicationEnvironment getAppEnv() throws IASDeploymentException
	{
		if(!isApplication()) {
			String msg = localStrings.getString(
				"enterprise.deployment.backend.illegal_getapplicationenv_call");
			throw new IASDeploymentException( msg );
		}
		
		return appEnv;
	}
	
	///////////////////////////////////////////////////////////////////////////

	/* package */ ModuleEnvironment getModuleEnv() throws IASDeploymentException
	{
		if(!isModule()) {
			String msg = localStrings.getString(
					"enterprise.deployment.backend.illegal_getmoduleenv_call");
			throw new IASDeploymentException( msg );
		}

		return moduleEnv;
	}

	///////////////////////////////////////////////////////////////////////////

	/* package */ boolean isEar()
	{
		// check if it's archive deployment and 
                // if it's an application 
		return isApplication() && fileSource.isArchive();
	}

	///////////////////////////////////////////////////////////////////////////

	/* package */ boolean isWar()
	{
		// check if it's archive deployment and 
                // if it's a web module
		return isWebModule() && fileSource.isArchive();
	}
	///////////////////////////////////////////////////////////////////////////

	/* package */ boolean isEjbJar()
	{
		// check if it's archive deployment and 
                // if it's a ejb module
		return isEjbModule() && fileSource.isArchive();
	}

    /**
     * Sets the parent class loader to be used for this request.
     *
     * @param    cl    parant class loader
     */
    void setParentClassLoader(ClassLoader cl) 
    {
        this.parentClassLoader = cl;
    }

    /**
     * Returns the parent class loader for this request.
     * 
     * @return    the parent class loader
     */
    ClassLoader getParentClassLoader() 
    {
        return this.parentClassLoader;
    }
        
    /**
     * Sets the parent class paths for this request.
     */
    void setParentClasspath(List classpaths)
    {
        this.parentClasspath = classpaths;
    }

    /**
     * Returns the parent class paths for this request.
     *
     * @return    the parent class paths
     */
    List getParentClasspath() 
    {
        return this.parentClasspath;
    }
        
    /**
     * Sets the optional attributes for this request.
     */
    public void setOptionalAttributes(Properties optionalAttributes)
    {
        this.optionalAttributes = optionalAttributes;
    }

    /**
     * Returns the optional attributes for this request.
     *
     * @return    Properties with the optional attributes 
     */
    public Properties getOptionalAttributes() 
    {
        return this.optionalAttributes;
    }
        

    /**
     * Sets the ejb class loader to be used for this request.
     *
     * @param    cl    ejb class loader
     */
    void setEjbClassLoader(ClassLoader cl) 
    {
        this.ejbClassLoader = cl;
    }

    /**
     * Returns the ejb class loader for this request.
     * 
     * @return    the ejb class loader
     */
    ClassLoader getEjbClassLoader() 
    {
        return this.ejbClassLoader;
    }
        
    /**
     * Sets the complete class paths for this request.
     */
    void setCompleteClasspath(List classpaths)
    {
        this.completeClasspath = classpaths;
    }

    /**
     * Returns the complete class paths for this request.
     *
     * @return    the ejb class paths
     */
    List getCompleteClasspath() 
    {
        return this.completeClasspath;
    }
    
    /**
     * Sets the module classpath (no env classpath) for this 
     * request
     * @param the module classpath
     */
    void setModuleClasspath(List classpath) {
	moduleClasspath = classpath;
    }
    
    /**
     * @return the module classpath for this request
     */
    List getModuleClasspath() {
	
	return moduleClasspath;
    }

	/**
     * Returns the optional arguments for this request.
     * It is guaranteed to be not-null, but might be empty.
     * @return    the optional arguments
     */
	public Properties getOptionalArguments()
	{
		return optionalArguments;
	}

	/**
     * Set the App or Module Directory so that ownership can be modified, if neccessary.
     */
    public void setDeployedDirectory(File d)
    {
        deployedDirectory = d;
    }
    /**
     * Set the Stubs Directory so that ownership can be modified, if neccessary.
     */
    public void setStubsDirectory(File d)
    {
        stubsDirectory = d;
    }
    /**
     * Set the Stubs Directory so that ownership can be modified, if neccessary.
     */
    public void setJSPDirectory(File d)
    {
        jspDirectory = d;
    }

    /**
     * Set the Directory where the modified xml files will be located 
     */
    public void setGeneratedXMLDirectory(File d)
    {
        generatedXMLDirectory = d;
    }


	/** return false, if the variable is false.  Any other case -- return true.
	 */
	boolean isMaybeCMPDropTables()
	{
		String s = getOptionalArguments().getProperty(Constants.CMP_DROP_TABLES);
		
		if(s != null && s.equalsIgnoreCase(Constants.FALSE))
			return false;
		
		return true;
	}
	
	
		public String getHttpHostName() 
		{
            return httpHostName;
        }
        
        public void setHttpHostName(String hostName) 
		{
            httpHostName = hostName;
        }
        
        public int getHttpPort() 
		{
            return httpPort;
        }
        
        public void setHttpPort(int port) 
		{
            httpPort = port;
        }
        
        public String getHttpsHostName() 
		{
            return httpsHostName;
        }
        
        public void setHttpsHostName(String hostName) 
		{
            httpsHostName = hostName;
        }
        
        public int getHttpsPort() 
		{
            return httpsPort;
        }
        
        public void setHttpsPort(int port) 
		{
            httpsPort = port;
        }

	///////////////////////////////////////////////////////////////////////////
	/////////  Private Methods       ///////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
	
	private boolean isFileSourceRequired() throws IASDeploymentException
	{
		if(isUnDeploy())
			return false;
		else
			return true;
	}
	
	///////////////////////////////////////////////////////////////////////////

	private boolean isNameRequired() throws IASDeploymentException
	{
		if(isDeploy())
			return false;
		else
			return true;
	}
	
	///////////////////////////////////////////////////////////////////////////

	private boolean isContextRootRequired() throws IASDeploymentException
	{
		// it is required for WebModules that are Deployed or Redeployed
		return isWebModule() && ( isDeploy() || isReDeploy());
	}
	
	///////////////////////////////////////////////////////////////////////////

	private void setType(DeployableObjectType theType) throws IASDeploymentException
	{
		type = theType;

		if(isConnectorModule())
			shared = true;
		else
			shared = false;
	}
	
	///////////////////////////////////////////////////////////////////////////

	/* package */ void setCommand(DeploymentCommand theCommand)  throws IASDeploymentException
	{
		command = theCommand;
		
		if(command == null) {
			String msg = localStrings.getString(
					"enterprise.deployment.backend.null_command_type" );
			throw new IASDeploymentException( msg );
		}
	}
	
	///////////////////////////////////////////////////////////////////////////

	private void setEnv() throws IASDeploymentException
	{
		if(instanceEnv == null) {
			String msg = localStrings.getString(
					"enterprise.deployment.backend.null_instanceenvironment" );
			throw new IASDeploymentException( msg );
		}
		
		if(isApplication())
			appEnv = new ApplicationEnvironment(instanceEnv, name);
		else if(isWebModule())
			moduleEnv = new ModuleEnvironment(instanceEnv, name, DeployableObjectType.WEB);
		else if(isEjbModule())
			moduleEnv = new ModuleEnvironment(instanceEnv, name, DeployableObjectType.EJB);
		else if(isConnectorModule())
			moduleEnv = new ModuleEnvironment(instanceEnv, name, DeployableObjectType.CONN);
                else if (isAppClientModule()) 
                        moduleEnv = new ModuleEnvironment(instanceEnv, name ,DeployableObjectType.CAR);                    
		else {
			String msg = localStrings.getString(
					"enterprise.deployment.backend.unknown_deployable_object",
					getClass().getName() );
			throw new IASDeploymentException( msg );
		}
	}

	///////////////////////////////////////////////////////////////////////////

	private void createName()
	{
		String aname = fileSource.getSource().getName();
		
		// special-case.  If it's xxx.ear, the name is xxx, not xxx_ear
		if(isEar())
			name = FileUtils.makeFriendlyFilenameNoExtension(aname);
		else
			name = FileUtils.makeFriendlyFilename(aname);
	}
	
	/** We need to figure-out if it is REALLY an App-Redeploy very early
	 * because DeployerFactory needs to create the right kind of Deployer object
	 */
	private void checkForRedeploy() throws IASDeploymentException
	{
		if(isUnDeploy())
			return;	// don't need to do anything!
		
		assert (appEnv != null && moduleEnv == null)|| (appEnv == null && moduleEnv != null);
		assert !isVerified;
		assert StringUtils.ok(name);
		assert instanceEnv != null;
		
		if(moduleEnv != null)
		{
			// too difficult, and not neccessary,  to figure this out here
			return;
		}
		
		try
		{
			AppsManager	appMgr = new AppsManager(instanceEnv);
			boolean		isReg = appMgr.isRegistered(name);
			
			if(forced && isReg && isDeploy())
				command = DeploymentCommand.REDEPLOY;
		}
		catch(Exception e)
		{
			throw new IASDeploymentException(e);
		}
	}

    /**
     * Sets the target to as specified
     * @param target JSR88 target
     */
    public void setTarget(Target target) {
        this.target = target;
    }   
    
    /**
     * Returns the target 
     * @return target JSR88 Target object
     */
    public Target getTarget() {
        return  target;
    }   
    
    /**
     * Sets the current deployment status to as specified
     * @param status deployment status
     */
    public void setCurrentDeploymentStatus(DeploymentStatus status) {
        currentDeploymentStatus = status;
    }

    /**
     * Returns the current deployment status
     * @return currentDeploymentStatus current deployment status
     */
    public DeploymentStatus getCurrentDeploymentStatus() {
        return currentDeploymentStatus;
    }

    /**
     * Sets startOnUndeploy. An application is set to enabled after
     * deployment or association when this is true
     * @param startOnDeploy
     */
    public void setStartOnDeploy(boolean startOnDeploy) {
        this.startOnDeploy = startOnDeploy;
    }   
    
    /**
     * returns true if isStartOnDeploy is true false otherwise
     * @return true/false
     */
    public boolean isStartOnDeploy() {
        return this.startOnDeploy;
    }   
    
    /**
     * Sets the actioncode to as specified
     * @param actionCode
     */
    public void setActionCode(int actionCode){
        this.actionCode = actionCode;
    }   
    
    /**
     * returns the actionCode 
     * @return int action code
     */
    public int getActionCode() {
        return actionCode;
    }   

    /**
     * Sets cascade. Dependent resources associated with a connector resource are
     * destroyed if this flag is true
     * @param startOnDeploy
     */
    public void setCascade(boolean cascade) {
        this.cascade = cascade;
    }   
    
    /**
     * returns true if cascade is true false otherwise
     * @return true/false
     */
    public boolean getCascade() {
        return this.cascade;
    }       
    
    /**
     * Sets the descriptor associated with this request
     */
     public void setDescriptor(Application app) {
	 this.application = app;
     }
     
     /**
      * @return the application descriptor associated with this request
      */
     public Application getDescriptor() {
	 return application;
     }

    /**
     * Sets the description associated with this request
     */
     public void setDescription(String description) {
         this.description = description;
     }

     /**
      * @return the description associated with this request
      */
     public String getDescription() {
         return description;
     }

	 /**
      * Should the app or module be re-registered on fatal errors?
	  * If false is returned, this means that it could NOT be rollbacked
	  * to a previous working version.
      */
     public boolean getReRegisterOnFailure() 
	 {
		return reregisterOnFailure;
     }

	 void setReRegisterOnFailure(boolean b) 
	 {
		reregisterOnFailure = b;
     }
     
     /**
      *Releases any resources held by the deployment request, notably the
      *EJBClassLoader.
      */
     public void done() {
         if ( ! isDone) {
             isDone = true;
             if (ejbClassLoader != null && ejbClassLoader instanceof EJBClassLoader) {
                 ((EJBClassLoader) ejbClassLoader).done();
             }
         }
     }
     
     /**
      *Sets whether this request is part of a larger redeployment sequence.
      *@param inProgress boolean setting for the value
      */
     public void setIsRedeployInProgress(boolean inProgress) {
         isRedeployInProgress = inProgress;
     }
     
     /**
      *Reports whether this request is part of a larger redeployment sequence.
      */
     public boolean isRedeployInProgress() {
         return isRedeployInProgress;
     }
	///////////////////////////////////////////////////////////////////////////

	private	FileSource				fileSource			= null;
        private File deploymentPlan = null;
        private DeploymentCallback deploymentCallback = null;
        private boolean isAborted = false;
        private boolean isExternallyManagedApp = false;
        private boolean isExternallyManagedPath = false;
        private boolean isReload = false;
        private String targetName = null;
	private	DeployableObjectType	type				= null;
	private	DeploymentCommand		command				= null;
	private	String					name				= null;
	private	String					contextRoot			= null;
        private String                                  defaultContextRoot
                = null;
	private	InstanceEnvironment		instanceEnv			= null;
	private	ApplicationEnvironment	appEnv				= null;
	private	ModuleEnvironment		moduleEnv			= null;
	private	File					clientJar			= null;
	private File					deployedDirectory	= null;
	private File					jspDirectory		= null;
	private File					stubsDirectory		= null;
	private File					generatedXMLDirectory		= null;
	private	boolean					shared				= false;
	private	boolean					forced				= true;
	private boolean					isVerified			= false;
    private boolean                 isVerifying         = false;
	private boolean					debug				= false;
	private boolean					noEJBC				= false;
	private boolean					precompileJSP		= false;
	private boolean	generateRMIStubs = false;
	private boolean	availabilityEnabled = false;
	private boolean	javaWebStartEnabled = true;
	private	String libraries = null;
	private String resourceAction = null;
	private String resourceTargetList = null;
	private boolean	directoryDeployed = false;
	private List                    parentClasspath     = null;
    private ClassLoader             parentClassLoader   = null;
	private List                    completeClasspath		= null;
	private List			moduleClasspath	= null;
    private ClassLoader             ejbClassLoader		= null;
	private Properties				optionalAttributes	= null;
	private	Properties				optionalArguments	= new Properties();
	private String					httpHostName		= null;
	private int						httpPort;
	private String					httpsHostName		= null;
	private int						httpsPort;
	private Application				application=null;
	private static StringManager	localStrings =
		StringManager.getManager( DeploymentRequest.class );
   
    /** target name on which this deployment request is performed */
    private Target target                               = null;
    
    /** application state after Deploy/Associate */
    private boolean startOnDeploy                       = true;
    
    /** action code of this request */
    private int actionCode                              = 0;
    
    /** cascading the undeploy **/
    private boolean cascade                             = false;

    /** current deployment status **/
    private DeploymentStatus currentDeploymentStatus = null;
	
    private String description = null;

	/** failed directory redeploy should NOT reregister from the MBean */
	
	private boolean reregisterOnFailure					= true;
    
    /** records whether this request and command are part of a larger redeployment sequence */
    private boolean isRedeployInProgress = false;
}
