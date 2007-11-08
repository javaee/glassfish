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
 package com.sun.enterprise.tools.deployment.main;

import java.io.*;
import java.net.*;
import com.sun.enterprise.tools.deployment.backend.JarInstaller;
import com.sun.enterprise.tools.deployment.backend.DeploymentSessionImpl;
import com.sun.enterprise.tools.deployment.backend.DeploymentSession;
//import com.sun.enterprise.tools.deployment.ui.utils.UIConfig;  //IASRI 4691307
import java.rmi.RemoteException;
import java.util.*;
import java.util.zip.*;
import java.util.jar.*;
import com.sun.ejb.sqlgen.DBInfo;
import com.sun.ejb.sqlgen.SQLGenerator;
import com.sun.enterprise.tools.packager.ComponentPackager;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.NotificationEvent;
import com.sun.enterprise.util.FileUtil;

import com.sun.enterprise.deployment.xml.*;
import com.sun.enterprise.deployment.*;

/** The master object of the J2EE deployment tool.
* @author Danny Coward
*/

public class DeployTool {
    private ApplicationManager applicationManager;
    private StandAloneManager standaloneManager;
    private ServerManager serverManager;
    private ComponentPackager componentPackager;
    private File workingDirectory;
    private File toolHomeDirectory;
    private static LocalStringManagerImpl localStrings =
	new LocalStringManagerImpl(DeployTool.class);
    public static final String HOME_DIR = ".assemblytool";  //IASRI 4691307 // NOI18N

    private static DeployTool deployToolInstance = null;

    /* eventually, this should be an externally configurable item */
    public static final String UI_STARTUP_CLASS = "com.sun.enterprise.tools.deployment.ui.DT"; // NOI18N
    protected UIFactory uiFactory = null;
	
    private String userdir = null;
    /** 
     * Construct a new deploytool indicating whether this tool has a UI or not.
     */
    
    //add an method for specifying userdir.  IASRI 4691307 
    public DeployTool(boolean hasUI, String dir){
        userdir = dir;
        _deploytool(hasUI);
    }
    
    
    public DeployTool(boolean hasUI) {
        _deploytool(hasUI);
    }
    
    private void  _deploytool(boolean hasUI){
        
	deployToolInstance = this;

        //If user specify userdir, will set that to UIConfig so that getConfigDirectory() will return that.
        // IASRI 4691307
        /* cannot do this because server.xml excludes ui directory. Will cause problem to build.
        if (userdir != null){
                UIConfig.setUserDir(userdir);
        }
         **/
	this.uiFactory = createUIFactory(hasUI);
	
        if (this.uiFactory != null){
	    /* ui */
            // IASRI 4691307
            // If the server build.xml include the ui directory, we won't need to test userdir here.
            // just call uiFactory.getConfigDirectory();
            File cfgDir = null;
            if (userdir == null){
                cfgDir = this.uiFactory.getConfigDirectory();
            }else{
                cfgDir = new File(userdir);
                cfgDir.mkdirs();
            }
	    File tmpDir = this.uiFactory.getTempDirectory();
            System.out.println("user directory = " + cfgDir);    // IASRI 4691307 info user. // NOI18N
            System.out.println("temp directory = " + tmpDir);    // IASRI 4691307 info user. // NOI18N
	    this.applicationManager = new ApplicationManager(cfgDir, tmpDir);
	    this.standaloneManager  = new StandAloneManager(cfgDir, tmpDir);
	    this.serverManager      = new ServerManager(cfgDir);
	    this.uiFactory.startUI();

	} else {

	    /* non-ui */
	    File toolDir = this.getToolHomeDirectory();
	    File tempDir = this.getWorkingDirectory();
            System.out.println("user directory = " + toolDir);   // IASRI 4691307 info user. // NOI18N
            System.out.println("temp directory = " + tempDir);   // IASRI 4691307 info user. // NOI18N
	    this.applicationManager = new ApplicationManager(toolDir, tempDir);
	    this.standaloneManager  = new StandAloneManager(toolDir, tempDir);
	    this.serverManager      = new ServerManager(toolDir);

	}

    }   

    public static DeployTool getDeployToolInstance() {
	// The created DeployTool instance is needed by the deploytool ui.
	// This method allows the ui to obtain the instance.
	return deployToolInstance;
    }

    /* load ui classes if UI is requested */
    protected static UIFactory createUIFactory(boolean hasUI) {

	if (hasUI) {

 	    try {

	    	/* locate the class (make sure it's a UIFactory) */
	    	Class uiFactoryClass = Class.forName(UI_STARTUP_CLASS);
	    	if (!UIFactory.class.isAssignableFrom(uiFactoryClass)) {
	    	    throw new ClassCastException("Class does not implement UIFactory"); // NOI18N
	    	}

	    	/* instantiate/initialize ui */
  	    	return (UIFactory)uiFactoryClass.newInstance();

	    } catch (Throwable t) {

	    	System.out.println(localStrings.getLocalString(
		    "enterprise.tools.deployment.main.error_creating_ui",
		    "Unable to create UI: " + t));

	    }

	}

	return null;

    }

    /**
     * Return a generated Application object given a module file
     */
    public Application deployStandaloneModule(File moduleFile)
        throws Exception {

        // derive an app name
        File tmpDir = FileUtil.getTempDirectory();
        String appName = "app." + moduleFile.getName().replace('.', '_'); // NOI18N
	File appFile = new File(tmpDir, appName + ".ear"); // NOI18N
        int i = 0;
        while (appFile.exists()) {
            i++;
	    appFile = new File(tmpDir, appName + "_" + i + ".ear"); // NOI18N
	}
        // update appName based on appFile
        if (i > 0) {
            appName = appName + "_" + i; // NOI18N
        }
        appFile.deleteOnExit();
        Application app = new Application(appName, appFile);
        if (EjbBundleArchivist.isEjbBundle(moduleFile)) {
            app.addEjbJarFile(moduleFile);
        } else if (WebBundleArchivist.isWebBundle(moduleFile)) {
            app.addWebJarFile(moduleFile);
        } else if (ApplicationClientArchivist.isApplicationClientJar(moduleFile)) {
            app.addAppClientJarFile(moduleFile);
        } else {
            throw new IllegalArgumentException("Unsupported module type: " + // NOI18N
                                               moduleFile);
        }
        return app;
    }

    // internal API for CTS harness, should go when they start using public interfaces
    public void deploy(Application application, String serverName, DeploymentSession deploymentSession, File clientCodeFile)
        throws Exception 
    {
        deploy(application.getName(), application.getApplicationArchivist().getApplicationFile(), serverName, deploymentSession, clientCodeFile);   
    }
    
    public void deploy(String applicationName, File appArchiveFile,
	String serverName, DeploymentSession deploymentSession, File clientCodeFile) 
	throws Exception {
            
	System.out.println(localStrings.getLocalString(
		"enterprise.tools.deployment.main.deployapplicationfileonserversaveasclientjar",
		"Deploy the application in {0} on the server {1} saving the client jar as {2}", 
		new Object[] { appArchiveFile, serverName, clientCodeFile }));
	Object[] msg = { applicationName, serverName };
	// byte[] clientCode = null;
	String clientCode = null;
	Log.print(this,localStrings.getLocalString(
		"enterprise.tools.deployment.main.deploytool.deploy_command",
		"Deploy {0} on {1}", msg));
	JarInstaller backend = this.getServerManager().getServerForName(serverName);
	DeploymentSession deploymentSessionToUse = null;
	if (deploymentSession == null) {
	    deploymentSessionToUse = this.getServerManager().createDeploymentSession(serverName);
	} else {
	    deploymentSessionToUse = deploymentSession;
	}
	
	FileInputStream fis = new FileInputStream(appArchiveFile);
	DataInputStream dis = new DataInputStream(fis);
	byte[] jarData = new byte[(int) appArchiveFile.length()];
	dis.readFully(jarData);
	dis.close();
	fis.close();
	clientCode = backend.deployApplication(jarData, applicationName, deploymentSessionToUse);
	Log.print(this, localStrings.getLocalString(
						    "enterprise.tools.deployment.main.clientcodeat",
						    "client code at {0}", new Object[] {clientCode}));
	if (clientCode != null && clientCodeFile != null) {
	    writeClientJarToFile(clientCode, clientCodeFile);
	    deploymentSessionToUse.notification(new NotificationEvent(this, DeploymentSession.CLIENT_CODE_RETURNED, this));
	    deploymentSessionToUse.setStatusMessage(localStrings.getLocalString(
										"enterprise.tools.deployment.main.clientcodefordeployedapplicationsavedtofile",
										"Client code for the deployed application {0} saved to {1}", new Object[] {applicationName, clientCodeFile}));
	}
    }
    
    private void writeClientJarToFile(String clientCode,
			    File clientCodeFile ) throws IOException {
	URL u = new URL(clientCode);
	HttpURLConnection http = (HttpURLConnection) u.openConnection();
	int code = http.getResponseCode();
	if(code != 200) {
	    System.out.println(localStrings.getLocalString(
							   "enterprise.tools.deployment.main.cannotdownloadURL",
							   "Cannot download URL {0}", new Object[] {clientCode}));
	    System.out.println(localStrings.getLocalString(
							   "enterprise.tools.deployment.main.status",
							   "Status: {0}", new Object[] {new Integer(code)}));
	    throw new IOException(localStrings.getLocalString(
							      "enterprise.tools.deployment.main.cannotdownloadURL",
							      "Cannot download URL {0}", new Object[] {clientCode}));
	}
	BufferedInputStream is = new BufferedInputStream(http.getInputStream());
	FileOutputStream fos = new FileOutputStream(clientCodeFile);
	int len = 0;
	int contentLength = http.getContentLength();
	// System.out.println("CONTENT LENGTH:" + contentLength);
	byte[] buf = new byte[contentLength+1];
	while((len = is.read(buf)) != -1)
	    fos.write(buf, 0, len);
    }

    private void saveAsBytes(byte[] data, File file) throws IOException {
	if (data == null) {
	    throw new IOException(localStrings.getLocalString(
							      "enterprise.tools.deployment.main.nulldataforclientcodefile",
							      "null data for client code file"));
	}
	FileOutputStream fileStream = new FileOutputStream(file);
	fileStream.write(data, 0, data.length);
	fileStream.close();
    }
    
    /** Parse the given runtime information file and update the given application
    * with its data.
    */
    public void setRuntimeDeploymentInfo(Application application, File runtimeDeploymentInfo) throws Exception {
	//ApplicationArchivist archivist = new ApplicationArchivist();
	FileInputStream fis = new FileInputStream(runtimeDeploymentInfo);
	RuntimeDescriptorNode node = (RuntimeDescriptorNode) RuntimeDescriptorNode.readRuntimeDescriptorNodes(fis).elementAt(0);
	node.updateRuntimeInformation(application);
	this.getApplicationManager().saveApplication(application);
	
	Object[] msg = {application.getName()};
	Log.print(this, localStrings.getLocalString( "enterprise.tools.deployment.main.deploytool.setruntime_command", "Done setting runtime deployment information on {0} to: {1}", msg));
    }
  
    /** Open the given application from the supplied filename and update any of the CMP entity beans therein
    * with default generated SQL statements for the persistent methods from the datdbases running
    * on the given server. Use overwrite to desructively overwrite existing SQL statements.
    */
    public void doGenerateSQL(String applicationFilename, String serverName, boolean overWrite) throws Exception 
    {
	DBInfo dbInfo = this.getServerManager().getDBInfo(serverName);
	Application application = ApplicationArchivist.openAT(                  //bug# 4774785; 4691307
						new File(applicationFilename));

	Iterator itr = application.getEjbBundleDescriptors().iterator(); 
	while ( itr.hasNext() ) {
	    EjbBundleDescriptor ebd = (EjbBundleDescriptor)itr.next();

            SQLGenerator.generateSQL(ebd, ebd.getCMPResourceReference(), 
                                     overWrite, dbInfo);
	}

	application.getApplicationArchivist().save(application.getApplicationArchivist().getApplicationFile(), true);
	System.out.println(localStrings.getLocalString(
						       "enterprise.tools.deployment.main.donegeneratingSQL",
						       "Done generating SQL"));
    }


    public ComponentPackager getComponentPackager() {
	if (this.componentPackager == null) {
	    this.componentPackager = new ComponentPackager();
	}
	return this.componentPackager;
    }
    
    /** Gets the object responsible for managing applications. */
    public ApplicationManager getApplicationManager() {
	return applicationManager;
    }
        
    /** Gets the object responsible for managing stand-alone objects. */
    public StandAloneManager getStandAloneManager() {
	return standaloneManager;
    }

    /** Gets the object responsible for managing servers. */
    public ServerManager getServerManager() {
	return serverManager;
    }
    
    /**
    * Return the working directory of the tool.
    */
    
    public File getWorkingDirectory() {
	// this give /var/tmp ! which does not seem to be writable
	//String temp = System.getProperty("java.io.tmpdir");
	//if (temp == null) {
	//    temp = "/tmp";
	//}
	//return new File(temp, "jpedeploytool");

	String home = System.getProperty("user.home");
	if (home == null) {
	    home = ""; // NOI18N
	}
	File tmp = new File(home, "tmp"); // NOI18N
	tmp.mkdirs();
	return tmp;
    }

    /** Gets the user home directory.
    */
    public File getToolHomeDirectory() {
        
	
        
        // IASRI 4691307  for supporting -userdir option
        //test to see if userdir was set, if so use that instead of home directory.
        
        //String home = System.getProperty("user.home");
	//if (home == null) {
	    //home = "";
	//}
        //return new File(home, HOME_DIR);
        
        File homedir;
        if (userdir != null){
            homedir =  new File(userdir);
        }else{
            String home = System.getProperty("user.home");
            if (home == null) {
                home = ""; // NOI18N
            }
            homedir= new File(home, HOME_DIR);
        }
        homedir.mkdirs();
        return homedir;
	// end of IASRI 4691307
    }
    
    /** Formatted version of me as a String.*/
    public String toString() {
	return "Deploy Tool"; // NOI18N
    }
    
    // testing only

    static public void main(String[] args) {
        try {
            DeployTool tool = new DeployTool(false);
            Application app = tool.deployStandaloneModule
                (new File("/home/tcng/Test/ejb.jar")); // NOI18N
            System.err.println(app.toString());
            app = tool.deployStandaloneModule
                (new File("/home/tcng/Test/app.jar")); // NOI18N
            System.err.println(app.toString());
            app = tool.deployStandaloneModule
                (new File("/home/tcng/Test/web.war")); // NOI18N
            System.err.println(app.toString());
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
