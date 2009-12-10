/**
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER. 
* 
* Copyright 2007-2009 Sun Microsystems, Inc. All rights reserved. 
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

package org.openinstaller.provider.conf;


import java.io.BufferedOutputStream;
import org.openinstaller.provider.conf.ResultReport;
import org.openinstaller.provider.conf.Configurator;
import org.openinstaller.config.PropertySheet;
import org.openinstaller.util.*;
import org.glassfish.installer.util.*;
import com.sun.pkg.bootstrap.Bootstrap;
import javax.management.Notification;
import javax.management.NotificationListener;
import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedInputStream;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Properties;
import java.util.SortedMap;
import java.util.jar.*;
import java.util.zip.*;
import java.lang.*;

public final class InstallationConfigurator implements Configurator, NotificationListener {

private final String productName;
private final String altRootDir;
private final String xcsFilePath;
private final String installDir;
private String adminPort;

private int gWaitCount = 0;
private String productError = null;

private final static String GLASSFISH_PRODUCT_NAME = "glassfish";
private final static String UPDATETOOL_PRODUCT_NAME = "updatetool";

private static final Logger LOGGER;

/* List of port numbers currently defaulted by asadmin command */
String portArray[][] = {
                        {"jms.port", "7676"},
                        {"domain.jmxPort", "8686"},
                        {"orb.listener.port", "3700"},
                        {"http.ssl.port", "8181"},
                        {"orb.ssl.port", "3820"},
                        {"orb.mutualauth.port", "3920"}
                        };

static {
    LOGGER = Logger.getLogger(ClassUtils.getClassName());
}




public InstallationConfigurator(final String aProductName, final String aAltRootDir,
     final String aXCSFilePath, final String aInstallDir) {

    productName = aProductName;
    altRootDir = aAltRootDir;
    xcsFilePath = aXCSFilePath;
    installDir = aInstallDir;

    
}


public ResultReport configure (final PropertySheet aSheet, final boolean aValidateFlag) throws EnhancedException {

     boolean configSuccessful = true;
    
     try {
        if (productName.equals(GLASSFISH_PRODUCT_NAME)) {
            LOGGER.log(Level.INFO, "Configuring GlassFish");
	    // Store admin port to be used for short cut creation.
	    adminPort = aSheet.getProperty("Administration.ADMIN_PORT");
            configSuccessful = configureGlassfish(
                installDir,
		adminPort,
                aSheet.getProperty("Administration.HTTP_PORT"),
                aSheet.getProperty("Administration.ADMIN_USER"),
                aSheet.getProperty("Administration.ADMIN_PASSWORD"));
		
	String folderName = 
		(String)TemplateProcessor.getInstance().getFromDataModel("PRODUCT_NAME");
    	if (System.getProperty("os.name").indexOf("Windows") !=-1 ) {
        	LOGGER.log(Level.INFO, "Creating shortcuts under Folder :<" + folderName + ">");
		createServerShortCuts(folderName); 
	}
	}

        if (productName.equals(UPDATETOOL_PRODUCT_NAME)) {
            LOGGER.log(Level.INFO, "Configuring Updatetool");
            LOGGER.log(Level.INFO, "Installation directory: " + installDir);
            configSuccessful = configureUpdatetool(
                installDir,
                aSheet.getProperty("Configuration.BOOTSTRAP_UPDATETOOL"),
                aSheet.getProperty("Configuration.ALLOW_UPDATE_CHECK"),
                aSheet.getProperty("Configuration.PROXY_HOST"),
                aSheet.getProperty("Configuration.PROXY_PORT"));
	    }
	String folderName = 
		(String)TemplateProcessor.getInstance().getFromDataModel("PRODUCT_NAME");
    	if (System.getProperty("os.name").indexOf("Windows") !=-1 ) {
        	LOGGER.log(Level.INFO, "Creating shortcuts under Folder :<" + folderName + ">");
		createUpdatetoolShortCuts(folderName);
	}
     }
     catch (Exception e) {
         configSuccessful = false;
     }


     ResultReport.ResultStatus status = ResultReport.ResultStatus.SUCCESS;
     if (!configSuccessful) {
         status = ResultReport.ResultStatus.FAIL;
     }
  
     return new ResultReport(status, "http://docs.sun.com/doc/820-7690 ", "http://docs.sun.com/doc/820-7690", null, productError);
         
}


public PropertySheet getCurrentConfiguration() {

    return new PropertySheet();
}


public ResultReport unConfigure (final PropertySheet aSheet, final boolean aValidateFlag) {

     try {
	if (productName.equals(GLASSFISH_PRODUCT_NAME)) {
            LOGGER.log(Level.INFO, "Unconfiguring GlassFish");
            unconfigureGlassfish(installDir);
	}
        
        if (productName.equals(UPDATETOOL_PRODUCT_NAME)) {
            LOGGER.log(Level.INFO, "Unconfiguring Updatetool");
            LOGGER.log(Level.INFO, "Installation directory: " + installDir);
            unconfigureUpdatetool(installDir);
	}
	/* Delete the newly created folder, on windows. No incremental uninstallation, so delete everything.*/ 
	String folderName = 
		(String)TemplateProcessor.getInstance().getFromDataModel("PRODUCT_NAME");
    	if (System.getProperty("os.name").indexOf("Windows") !=-1 ) {
		WindowsShortcutManager wsShortMgr = new WindowsShortcutManager();
		wsShortMgr.deleteFolder(folderName);
	}
     }
     catch (Exception e) {
         
     }

    return new ResultReport(ResultReport.ResultStatus.SUCCESS, "http://docs.sun.com/doc/820-7690", "http://docs.sun.com/doc/820-7690", null, productError);
}

public void handleNotification (final Notification aNotification,
    final Object aHandback) {
    /* We received a message from the configurator, so reset the count */
    synchronized(this) {
      gWaitCount = 0;
    }
}

/* Returns true if configuration is successful, else false */
boolean configureGlassfish(String installDir, String adminPort, String httpPort, String adminUser, String adminPwd) throws Exception {

    boolean success = true;

    // set executable permissions on asadmin, stopserv, startserv, jspc 

    boolean isWindows = false;
    if (System.getProperty("os.name").indexOf("Windows") !=-1 ) {
        isWindows=true;
    }

    boolean isMac = false;
    boolean isAix = false;
    String osName = System.getProperty("os.name").toLowerCase();
    if (osName.startsWith("mac os x")) {
        isMac=true;
    }
    if (osName.startsWith("aix")) {
        isAix=true;
    }

    if (!isWindows) {

        String CLInames[] = {"asadmin", "stopserv", "startserv", "jspc"};
        for (int i = 0; i < CLInames.length; i++) {
            Runtime.getRuntime().exec("/bin/chmod a+x " +
                               installDir + "/glassfish/bin/" + CLInames[i]);
		}
	Runtime.getRuntime().exec("/bin/chmod a+x " +
			installDir + "/bin/asadmin");
    }

    //unpack jar files in all directories under glassfish/modules
    
    String modulesDir = installDir + File.separator + "glassfish" +
	    File.separator + "modules";
    
    success = unpackJars(modulesDir) 
	    && unpackJars(modulesDir + File.separator + "endorsed")
	    && unpackJars(modulesDir + File.separator + "autostart");
    
    // if jar extraction failed there is no point in continuing...

    if (!success) {
	 return success;
    }		 

    //create domain startup/shutdown wrapper scripts used by program
    //group menu items

    FileWriter wrapperWriter = null;
    File startWrapperFile = null; 
    File stopWrapperFile = null; 
    try {
        if (isWindows) {
            startWrapperFile = new File(installDir + "\\glassfish\\lib\\asadmin-start-domain.bat");
	    wrapperWriter = new FileWriter(startWrapperFile);
	    wrapperWriter.write ("@echo off\n");
	    wrapperWriter.write ("REM DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.\n");
	    wrapperWriter.write ("REM\n");
            wrapperWriter.write ("REM Copyright 2008 Sun Microsystems, Inc. All rights reserved.\n");
            wrapperWriter.write ("REM\n");
	    wrapperWriter.write ("REM Use is subject to License Terms\n");
	    wrapperWriter.write ("REM\n");
	    wrapperWriter.write ("setlocal\n");
	    wrapperWriter.write ("call \"" + installDir + "\\glassfish\\bin\\asadmin\" start-domain domain1\n");
	    wrapperWriter.write ("pause\n");
 	    wrapperWriter.write ("endlocal\n");
            wrapperWriter.close();
            wrapperWriter = null;

	    stopWrapperFile = new File(installDir + "\\glassfish\\lib\\asadmin-stop-domain.bat");
	    wrapperWriter = new FileWriter(stopWrapperFile);
	    wrapperWriter.write ("@echo off\n");
	    wrapperWriter.write ("REM DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.\n");
	    wrapperWriter.write ("REM\n");
            wrapperWriter.write ("REM Copyright 2008 Sun Microsystems, Inc. All rights reserved.\n");
            wrapperWriter.write ("REM\n");
	    wrapperWriter.write ("REM Use is subject to License Terms\n");
	    wrapperWriter.write ("REM\n");
 	    wrapperWriter.write ("setlocal\n");
	    wrapperWriter.write ("call \"" + installDir + "\\glassfish\\bin\\asadmin\" stop-domain domain1\n");
 	    wrapperWriter.write ("pause\n");
 	    wrapperWriter.write ("endlocal\n");
            wrapperWriter.close();
            wrapperWriter = null;
	}
	else {
	    startWrapperFile = new File(installDir + "/glassfish/lib/asadmin-start-domain");
	    wrapperWriter = new FileWriter(startWrapperFile);
            wrapperWriter.write ("#!/bin/sh\n");
	    wrapperWriter.write ("#\n");
	    wrapperWriter.write ("# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.\n");
	    wrapperWriter.write ("#\n");
            wrapperWriter.write ("# Copyright 2008 Sun Microsystems, Inc. All rights reserved.\n");
            wrapperWriter.write ("#\n");
	    wrapperWriter.write ("# Use is subject to License Terms\n");
	    wrapperWriter.write ("#\n");
	    wrapperWriter.write ("\"" + installDir + "/glassfish/bin/asadmin\" start-domain domain1\n");
            wrapperWriter.close();
            wrapperWriter = null;

	    stopWrapperFile = new File(installDir + "/glassfish/lib/asadmin-stop-domain");
	    wrapperWriter = new FileWriter(stopWrapperFile);
            wrapperWriter.write ("#!/bin/sh\n");        
	    wrapperWriter.write ("#\n");
	    wrapperWriter.write ("# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.\n");
	    wrapperWriter.write ("#\n");
            wrapperWriter.write ("# Copyright 2008 Sun Microsystems, Inc. All rights reserved.\n");
            wrapperWriter.write ("#\n");
	    wrapperWriter.write ("# Use is subject to License Terms\n");
	    wrapperWriter.write ("#\n");
	    wrapperWriter.write ("\"" + installDir + "/glassfish/bin/asadmin\" stop-domain domain1\n");
            wrapperWriter.close();
            wrapperWriter = null;

	    Runtime.getRuntime().exec("/bin/chmod a+x " + stopWrapperFile.getAbsolutePath());
	    Runtime.getRuntime().exec("/bin/chmod a+x " + startWrapperFile.getAbsolutePath());
	}
    } catch (Exception ex) {
            LOGGER.log(Level.INFO, "Error while creating wrapper file: " + ex.getMessage());
            success = false;
    }
      
    
    //create temporary password file for asadmin create-domain

        FileWriter writer = null;
        File pwdFile = null;        

        String pwd = adminPwd;
        try {            
            pwdFile = File.createTempFile("asadminTmp", null);                        
            pwdFile.deleteOnExit();            
            writer = new FileWriter(pwdFile);            
            writer.write("AS_ADMIN_ADMINPASSWORD=" + pwd + "\n");
            if (pwd != null && pwd.trim().length() > 0)
            	writer.write("AS_ADMIN_PASSWORD=" + pwd + "\n");
            else	
            	writer.write("AS_ADMIN_PASSWORD=\n");
            writer.write("AS_ADMIN_MASTERPASSWORD=changeit\n");
            writer.close();
            writer = null;
            if (!isWindows)
	        {
	            Runtime.getRuntime().exec("/bin/chmod 600 " + pwdFile.getAbsolutePath());
	        }      
            
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, "Error while creating password file: " + ex.getMessage());
            // ensure that we delete the file should any exception occur
            if (pwdFile != null) {
                try {
                    pwdFile.delete();
                } catch (Exception ex2) {
                    //ignore we are cleaning up on error
                }                
            }
            throw ex; 
        } finally {
            //ensure that we close the file no matter what.
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception ex2) {
                    //ignore we are cleaning up on error
                }                
            }
        }
    
    //get JDK directory from java.home property and use it to define asadmin 
    //execution environment PATH
    
    String javaHome = System.getProperty("java.home");
    LOGGER.log(Level.INFO, "javaHome: " +javaHome);

    String jdkHome;
   	try {
   		jdkHome = ConfigHelper.getStringValue("JDKSelection.directory.SELECTED_JDK");
        }catch(Exception e) {
    	LOGGER.log(Level.INFO, "JDKHome Couldnt be found ");
    	jdkHome = new File(javaHome).getParent();
    	if (isMac || isAix) {
       	 jdkHome = javaHome;
    	}
       }

    LOGGER.log(Level.INFO, "jdkHome: " +jdkHome);

    //write jdkHome value to asenv.bat on Windows, asenv.conf on non-Windows platform...

    File asenvFile = null;
    if (isWindows) 
            asenvFile = new File(installDir +"\\glassfish\\config\\asenv.bat");
    else 
            asenvFile = new File(installDir +"/glassfish/config/asenv.conf");
        try {
	    
            String line;
            StringBuffer sb = new StringBuffer();
	    	

            FileInputStream fis = new FileInputStream(asenvFile);
	    BufferedReader reader=new BufferedReader ( new InputStreamReader(fis));
	    while((line = reader.readLine()) != null) {
		sb.append(line+"\n");
	    }
	/* Add AS_JAVA to end of buffer and file. */ 
            if (isWindows)
	       line = "set AS_JAVA=" + jdkHome;
            else
	       line = "AS_JAVA=" + jdkHome;
	    sb.append(line+"\n");
            reader.close();

            BufferedWriter out=new BufferedWriter ( new FileWriter(asenvFile));
	    out.write(sb.toString());
	    out.close();
	} catch (Exception ex) {

            LOGGER.log(Level.INFO, "Error while updating asenv configuration file: " + ex.getMessage());
	}
	   
    
    //construct asadmin command
    ExecuteCommand asadminExecuteCommand = null;

        try {

            String asadminCommand;
        
            if (isWindows) {
                asadminCommand = installDir + "\\glassfish\\bin\\asadmin.bat";
            }
            else {
                asadminCommand = installDir + "/glassfish/bin/asadmin";
            }

            // determine admin user
            String user = adminUser;

            String[] asadminCommandArray = { asadminCommand,
		"--user", user,
                "--passwordfile", pwdFile.getAbsolutePath(),
	        "create-domain",
                "--savelogin",
		"--checkports=false",
                "--adminport", adminPort,
                "--instanceport", httpPort,
                "--domainproperties="+ getDomainProperties(adminPort, httpPort),
                "domain1"};

	    String[] asadminCommandArrayMac = { "java", "-jar",
		installDir+"/glassfish/modules/admin-cli.jar",
		"--user", user,
                "--passwordfile", pwdFile.getAbsolutePath(),
	        "create-domain",
                "--savelogin",
		"--checkports=false",
                "--adminport", adminPort,
                "--instanceport", httpPort,
                "--domainproperties="+ getDomainProperties(adminPort, httpPort),
                "domain1"};
            
            LOGGER.log(Level.INFO, "Creating GlassFish domain");
            LOGGER.log(Level.INFO, "Admin port:" + adminPort);
            LOGGER.log(Level.INFO, "HTTP port:" + httpPort);
            LOGGER.log(Level.INFO, "User:" + user);

	    String existingPath = System.getenv("PATH");
	    LOGGER.log(Level.INFO, "Existing PATH: " +existingPath);
            String newPath = jdkHome + File.separator + "bin" +
		    File.pathSeparator + existingPath; 
            LOGGER.log(Level.INFO, "New PATH: " +newPath);
            
	    if (isMac || isAix) {
		    asadminExecuteCommand = new ExecuteCommand(asadminCommandArrayMac);
	    }
	    else {
                   asadminExecuteCommand = new ExecuteCommand(asadminCommandArray);
	    }
	    asadminExecuteCommand.putEnvironmentSetting("PATH", newPath);
            asadminExecuteCommand.setOutputType(ExecuteCommand.ERRORS | ExecuteCommand.NORMAL);
            asadminExecuteCommand.setCollectOutput(true);
        
            asadminExecuteCommand.execute();
	    LOGGER.log(Level.INFO, "Asadmin output: " + asadminExecuteCommand.getAllOutput()); 

            productError = asadminExecuteCommand.getErrors();
            if (productError != null && productError.trim().length() > 0) {
		// special case for keytool related asadmin "failure" 
		// installation should still be reported as successful
		if (productError.indexOf("keytool") != -1) {
	            success=true;
		} else {
                    success = false;
		}
            }
       } catch (Exception e) {
            LOGGER.log(Level.INFO, "In exception, asadmin output: " + asadminExecuteCommand.getAllOutput()); 
            LOGGER.log(Level.INFO, "Exception while creating GlassFish domain: " + e.getMessage());
	    if (productError != null && productError.trim().length() > 0) {
		// special case for keytool related asadmin "failure" 
		// installation should still be reported as successful
		if (productError.indexOf("keytool") != -1) {
	            success=true;
		} else {
                    success = false;
		}
            }
            
       }

       return success;
}

/* Returns true if configuration is successful, else false */
boolean configureUpdatetool(String installDir, String bootstrap, String allowUpdateCheck,
    String proxyHost, String proxyPort) throws Exception {

    boolean success = true;

    boolean isWindows = false;
    if (System.getProperty("os.name").indexOf("Windows") !=-1 ) {
        isWindows=true;
    }

    // set execute permissions for UC utilities

    if (!isWindows) {

        String CLInames[] = {"pkg", "updatetool"};
        for (int i = 0; i < CLInames.length; i++) {
            Runtime.getRuntime().exec("/bin/chmod a+x " +
                               installDir + "/bin/" + CLInames[i]);
	}
    }

    //create updatetool wrapper scripts used by program
    //group menu items

    FileWriter wrapperWriter = null;
    File startWrapperFile = null; 
    File updateToolLibDir = null;
    try {
        if (isWindows) {
            updateToolLibDir = new File(installDir + "/updatetool/lib");
            updateToolLibDir.mkdirs();
            startWrapperFile = new File(installDir + "\\updatetool\\lib\\updatetool-start.bat");
	    wrapperWriter = new FileWriter(startWrapperFile);
	    wrapperWriter.write ("@echo off\n");
	    wrapperWriter.write ("REM DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.\n");
	    wrapperWriter.write ("REM\n");
            wrapperWriter.write ("REM Copyright 2008 Sun Microsystems, Inc. All rights reserved.\n");
            wrapperWriter.write ("REM\n");
	    wrapperWriter.write ("REM Use is subject to License Terms\n");
	    wrapperWriter.write ("REM\n");
	    wrapperWriter.write ("setlocal\n");
	    wrapperWriter.write ("cd \"" + installDir + "\\updatetool\\bin\"\n");
	    wrapperWriter.write ("call updatetool.exe\n");
 	    wrapperWriter.write ("endlocal\n");
            wrapperWriter.close();
            wrapperWriter = null;
	}
	else {
            updateToolLibDir = new File(installDir + "/updatetool/lib");
            updateToolLibDir.mkdirs();
	    startWrapperFile = new File(installDir + "/updatetool/lib/updatetool-start");
	    wrapperWriter = new FileWriter(startWrapperFile);
	    wrapperWriter.write ("#!/bin/sh\n");
	    wrapperWriter.write ("#\n");
	    wrapperWriter.write ("# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.\n");
	    wrapperWriter.write ("#\n");
            wrapperWriter.write ("# Copyright 2008 Sun Microsystems, Inc. All rights reserved.\n");
            wrapperWriter.write ("#\n");
	    wrapperWriter.write ("# Use is subject to License Terms\n");
	    wrapperWriter.write ("#\n");
	    wrapperWriter.write ("cd \"" + installDir + "/updatetool/bin\"\n");
	    wrapperWriter.write ("./updatetool\n");
            wrapperWriter.close();
            wrapperWriter = null;
	    
	    Runtime.getRuntime().exec("/bin/chmod a+x " + startWrapperFile.getAbsolutePath());
	}
    } catch (Exception ex) {
            LOGGER.log(Level.INFO, "Error while creating wrapper file: " + ex.getMessage());
            success = false;
    }

    // check whether to bootstrap at all

    if (bootstrap.equalsIgnoreCase("false")) {
        LOGGER.log(Level.INFO, "Skipping updatetool bootstrap");
        return success;
    }

    String proxyURL = null;

    if ((proxyHost.length()>0) && (proxyPort.length()>0)) {
        proxyURL = "http://" + proxyHost + ":" + proxyPort;
    }

    //adjust Windows path for use in properties file

    String installDirForward = installDir;

    if (isWindows) {
        installDirForward = installDir.replace('\\', '/');
    }
    
        
    
    //populate bootstrap properties

	Properties props = new Properties();

	props.setProperty("image.path", installDirForward);
	props.setProperty("install.pkg", "true");
	props.setProperty("install.updatetool", "true");
	props.setProperty("optin.update.notification", allowUpdateCheck);
	props.setProperty("optin.usage.reporting", allowUpdateCheck);
	if (proxyURL != null) {
	    props.setProperty("proxy.URL", proxyURL);
	}
            

    if (allowUpdateCheck.equalsIgnoreCase("true")) {
        LOGGER.log(Level.INFO, "Enabling Updatetool");
    }
 
    //invoke bootstrap
    
    Bootstrap.main(props, LOGGER);


    //notifier is now being registered as part of bootstrap, so explicit
    //call to updatetoolconfig is being removed
    
       return success;
}

void unconfigureUpdatetool(String installDir) throws Exception {
    boolean isWindows = false;
    if (System.getProperty("os.name").indexOf("Windows") !=-1 ) {
        isWindows=true;
    }
/* Try to shutdown the notifer. Don't do this on Mac, the notifier command
does not work on Mac, refer to Issue #7348. */
    String osName = System.getProperty("os.name").toLowerCase();
    if (!osName.startsWith("mac os x") && !osName.startsWith("aix")){
    	try {
            String shutdownCommand;
            if (isWindows)
                 shutdownCommand = installDir + "\\updatetool\\bin\\updatetool.exe";
            else
                shutdownCommand = installDir + "/updatetool/bin/updatetool";
            String[] shutdownCommandArray = { shutdownCommand, "--notifier","--shutdown"};
            LOGGER.log(Level.INFO, "Shutting down notifier process");
            ExecuteCommand shutdownExecuteCommand = new ExecuteCommand(shutdownCommandArray);
            shutdownExecuteCommand.setOutputType(ExecuteCommand.ERRORS | ExecuteCommand.NORMAL);
            shutdownExecuteCommand.setCollectOutput(true);
            shutdownExecuteCommand.execute();
       } catch (Exception e) {
            LOGGER.log(Level.INFO, "Exception while unregistering notifier: " + e.getMessage());
       }
} /* End, conditional code for Mac and Aix. */

    /* Now unregister notifer. */
    try {
            String configCommand;
            if (isWindows) {
                 configCommand = installDir + "\\updatetool\\bin\\updatetoolconfig.bat";
            }
            else {
                configCommand = installDir + "/updatetool/bin/updatetoolconfig";
            }
            String[] configCommandArray = { configCommand, "--unregister" };
            LOGGER.log(Level.INFO, "Unregistering notifier process");
            ExecuteCommand configExecuteCommand = new ExecuteCommand(configCommandArray);
            configExecuteCommand.setOutputType(ExecuteCommand.ERRORS | ExecuteCommand.NORMAL);
            configExecuteCommand.setCollectOutput(true);
            configExecuteCommand.execute();
            productError = productError +configExecuteCommand.getErrors();
       } catch (Exception e) {
            LOGGER.log(Level.INFO, "Exception while unregistering notifier: " + e.getMessage()); 
       }
}

void unconfigureGlassfish(String installDir) throws Exception {

    boolean isWindows = false;
    if (System.getProperty("os.name").indexOf("Windows") !=-1 ) {
        isWindows=true;
    }	
    try {
	File domainsDir = null;
	File startWrapperFile = null;
	File stopWrapperFile = null;
	File modulesDir = null;
        if (isWindows) {
	    domainsDir = new File (installDir + "\\glassfish\\domains");
	    modulesDir = new File (installDir + "\\glassfish\\modules");
	    
	    startWrapperFile = new File(installDir + "\\glassfish\\lib\\asadmin-start-domain.bat");
	    stopWrapperFile = new File(installDir + "\\glassfish\\lib\\asadmin-stop-domain.bat");
	}
	else {
            domainsDir = new File (installDir + "/glassfish/domains");
	    modulesDir = new File (installDir + "/glassfish/modules");
	    startWrapperFile = new File(installDir + "/glassfish/lib/asadmin-start-domain");
	    stopWrapperFile = new File(installDir + "/glassfish/lib/asadmin-stop-domain");
        }

	if (startWrapperFile.exists()) {
            startWrapperFile.delete();
        }
	if (stopWrapperFile.exists()) {
            stopWrapperFile.delete();
	}
        if (domainsDir.exists()) {
    	    stopDomain(installDir);
            deleteDirectory(domainsDir);
	}
	// delete modules dir content explicitly since it will contain
	// uncompressed jar files and UC content unknown to OI installer
	if (modulesDir.exists()) {
            deleteDirectory(modulesDir);
	}



    }
    catch (Exception e) {

        LOGGER.log(Level.INFO, "Exception while removing created files: " + e.getMessage()); 
    }

}


/* Try to stop domain, so that uninstall can cleanup files effectively.
Currently only tries to stop the default domain.
*/ 
public void stopDomain(String installDir) {
        ExecuteCommand asadminExecuteCommand = null;
        try {
            String asadminCommand;
    	    if (System.getProperty("os.name").indexOf("Windows") !=-1 ) 
                asadminCommand = installDir + "\\glassfish\\bin\\asadmin.bat";
            else 
                asadminCommand = installDir + "/glassfish/bin/asadmin";

            String[] asadminCommandArray = { asadminCommand, "stop-domain","domain1"};
            LOGGER.log(Level.INFO, "Stopping default domain domain1");

            asadminExecuteCommand = new ExecuteCommand(asadminCommandArray);
            asadminExecuteCommand.setOutputType(ExecuteCommand.ERRORS | ExecuteCommand.NORMAL);
            asadminExecuteCommand.setCollectOutput(true);
            asadminExecuteCommand.execute();
            LOGGER.log(Level.INFO, "Asadmin output: " + asadminExecuteCommand.getAllOutput()); 
       } catch (Exception e) {
            LOGGER.log(Level.INFO, "In exception, asadmin output: " + asadminExecuteCommand.getAllOutput()); 
            LOGGER.log(Level.INFO, "Exception while creating GlassFish domain: " + e.getMessage());
       }
}


/* Validates to make sure that the asadmin command line does not
include duplicate port values. Currently HTTP and Admin ports are
input by user and seven other ports(refer to this.PortArray[][])
have been hard-coded with constant values. This method makes sure
that the user-entered values are not duplicated in the assumptions
that asadmin makes. If so, then the assumptions(ports) will be 
incremented by one. Returns the whole of formulated domainproperties
to be used in asadmin create-domain command line.
Refer to Issue traker issue #6173.
*/
public String getDomainProperties(String adminPort, String httpPort) {
	
        String domainProperties = "";

	/* Check admin and http port given by user against
	the list of default ports used by asadmin. */
        for (int i=0;i<portArray.length;i++) {
        	if (portArray[i][1].equals(adminPort) || 
			portArray[i][1].equals(httpPort)) {
                	/* Convert string to a number, then add 1
	                then convert it back to a string. Update the
			portArray with new port #. */
        	 Integer newPortNumber = Integer.parseInt(portArray[i][1]) + 1;
               	 portArray[i][1] = Integer.toString(newPortNumber);
        	}

        // Store the modified array elements into the commandline
        domainProperties = 
		domainProperties + portArray[i][0] + "=" + portArray[i][1];

        /* Don't add a ":" to the last element :-), though asadmin ignores it,
	Safe not to put junk in commandline.
	*/
        if (i < 5)
                domainProperties = domainProperties + ":";
        }
        return domainProperties;
}


static public void deleteDirectory(File objName) throws Exception {
	File filesList[] = objName.listFiles();
	
	String osName = System.getProperty("os.name");
	boolean isWindows = false;
        if (osName.indexOf("Windows") == -1) {
            isWindows = false;
        }
        else {
            isWindows = true;
        }    
        
        boolean notSymlink = true;
        
	if (filesList != null)	{
		for (int i=0;i<filesList.length;i++) {
		if (filesList[i].isDirectory()) {
                    
                   if (isWindows)  {
                       notSymlink = filesList[i].getAbsolutePath().equalsIgnoreCase(filesList[i].getCanonicalPath());
                   }
                   else { 
                       notSymlink = filesList[i].getAbsolutePath().equals(filesList[i].getCanonicalPath());
                   }
                   if (notSymlink) {
		        deleteDirectory(filesList[i]);
                   }
                   else {
		        filesList[i].delete();
                   }

		}
		else {
		    filesList[i].delete();
		}
		}
	}
	objName.delete();
}

public boolean unpackJars(String unpackDir) {

            File packFile;
            Pack200.Unpacker unpacker = Pack200.newUnpacker();
            SortedMap<String,String> unpackerProp = unpacker.properties();
            // check if unpackDir exists, if it doesn't return true
            
            File targetDir = new File(unpackDir);
	        
	    if (!targetDir.exists()) {
	        return true;
	    }
	    
	    try {

	        final String[] fileList = targetDir.list();

	        for (int i = 0 ; i < fileList.length ; i ++) {
		    final String fileName = targetDir + File.separator + fileList[i];

		    if (fileName.endsWith(".pack.gz")) {

			LOGGER.log(Level.INFO, "Uncompressing " + fileName);
		        String unpackedFileName = fileName.substring(0, fileName.length()-8) + ".jar";
                        FileOutputStream fos = new FileOutputStream(unpackedFileName);
                        JarOutputStream jos = new JarOutputStream(
                                                    new BufferedOutputStream(fos));
                        FileInputStream fis = new FileInputStream(fileName);
                        InputStream is = new BufferedInputStream(new GZIPInputStream(fis)); 
                        unpacker.unpack(is, jos);
                        fis.close();
		        jos.close();
		        

	                packFile = new File(fileName);
	                packFile.delete();
                       
	                LOGGER.log(Level.INFO, "Uncompressed " + fileName);
                                               
		    }
		}

            }
            catch (Exception e) {
               LOGGER.log(Level.INFO, "Error uncompressing file:"
	           + e.getMessage());            
               
               return false;
            }

            return true;

}


/* Creates shortcuts for windows. The ones created from OI will be removed due to
manged names. These shortcuts are in addition to the ones created by default. 
Since the descriptor for defining the short cut entry is not OS specific, we still
need to carry on the xml entries to create items on Gnome.
*/
public void createUpdatetoolShortCuts(String folderName) {
		WindowsShortcutManager wsShortMgr = new WindowsShortcutManager();
		wsShortMgr.createFolder(folderName);
		String modifiedInstallDir = installDir.replace("\\","\\\\");
		// Create short cut for starting update tool.
		wsShortMgr.createShortCut(
			folderName,
			"Start Update Tool",
			modifiedInstallDir + "\\\\bin\\\\updatetool.exe",
			"Start updatetool",
			"",
			modifiedInstallDir + "\\\\updatetool\\\\vendor-packages\\\\updatetool\\\\images\\\\application-update-tool.ico",
			modifiedInstallDir + "\\\\bin",
			"2");
}
	
/* Creates shortcuts for windows. The ones created from OI will be removed due to
manged names. These shortcuts are in addition to the ones created by default. 
Since the descriptor for defining the short cut entry is not OS specific, we still
need to carry on the xml entries to create items on Gnome.
*/
public void createServerShortCuts(String folderName) {
		WindowsShortcutManager wsShortMgr = new WindowsShortcutManager();
		wsShortMgr.createFolder(folderName);
		String modifiedInstallDir = installDir.replace("\\","\\\\");
		// Create short cut for starting server.
		wsShortMgr.createShortCut(
			folderName,
			"Start Application Server",
			modifiedInstallDir + "\\\\glassfish\\\\bin\\\\asadmin.bat",
			"Start server",
			"start-domain domain1",
			modifiedInstallDir + "\\\\glassfish\\\\icons\\\\startAppserv.ico",
			modifiedInstallDir + "\\\\glassfish",
			"2");


		// Create short cut for Stop application server.
		wsShortMgr.createShortCut(
			folderName,
			"Stop Application Server",
			modifiedInstallDir + "\\\\glassfish\\\\bin\\\\asadmin.bat",
			"Stop server",
			"stop-domain domain1",
			modifiedInstallDir + "\\\\glassfish\\\\icons\\\\stopAppserv.ico",
			modifiedInstallDir + "\\\\glassfish",
			"2");

		// Create short cut for uninstall.exe.
		wsShortMgr.createShortCut(
			folderName,
			"Uninstall",
			modifiedInstallDir + "\\\\uninstall.exe",
			"Uninstall",
			"",
			modifiedInstallDir + "\\\\glassfish\\\\icons\\\\uninstall.ico",
			modifiedInstallDir,
			"2");

		// Create short cut for Quick Start guide.
		wsShortMgr.createShortCut(
			folderName,
			"Quick Start Guide",
			modifiedInstallDir + "\\\\glassfish\\\\docs\\\\quickstart.html");

		// Look for correct page deployed in the installdir before linking it.
		// this code is only w2k specific. 
		String aboutFilesLocation = "\\glassfish\\docs\\";
		String aboutFiles[] = { "about_sdk.html", "about_sdk_web.html","about.html"};
		// The default
		String aboutFile = "about.html";
		// Traverse through the list to find out which file exist first
		for (int i=0;i<aboutFiles.length;i++) {
			File f = new File(modifiedInstallDir + aboutFilesLocation + aboutFiles[i]);
			if (f.exists()) {
				// then break
				aboutFile = aboutFiles[i];
				break;
			}
			f = null;
		}

		// Create short cut for About Page.
		wsShortMgr.createShortCut(
			folderName,
			"About GlassFish Server",
			modifiedInstallDir + aboutFilesLocation.replace("\\","\\\\") + aboutFile.replace("\\","\\\\"));

		// Create short cut for Admin Console.
		wsShortMgr.createShortCut(
			folderName,
			"Administration Console",
			"http://localhost:" + adminPort);
}

}
