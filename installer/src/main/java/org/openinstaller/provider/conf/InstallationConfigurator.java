/**
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER. 
* 
* Copyright 2007-2008 Sun Microsystems, Inc. All rights reserved. 
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
import org.openinstaller.util.EnhancedException;
import org.openinstaller.util.ExecuteCommand;
import org.openinstaller.util.ClassUtils;
import javax.management.Notification;
import javax.management.NotificationListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class InstallationConfigurator implements Configurator, NotificationListener {

private final String productName;
private final String altRootDir;
private final String xcsFilePath;
private final String installDir;

private int gWaitCount = 0;
private String productError = null;

private final static String GLASSFISH_PRODUCT_NAME = "glassfish";
private final static String UPDATETOOL_PRODUCT_NAME = "updatetool";

private static final Logger LOGGER;

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


    
     try {
        if (productName.equals(GLASSFISH_PRODUCT_NAME)) {
            LOGGER.log(Level.INFO, "Configuring GlassFish");
            configureGlassfish(
                installDir,
                aSheet.getProperty("Administration.A_ADMIN_PORT"),
                aSheet.getProperty("Administration.A_HTTP_PORT"));
	    }

        if (productName.equals(UPDATETOOL_PRODUCT_NAME)) {
            LOGGER.log(Level.INFO, "Configuring Updatetool");
            LOGGER.log(Level.INFO, "Installation directory: " + installDir);
            configureUpdatetool(
                installDir,
                aSheet.getProperty("Configuration.BOOTSTRAP_UPDATETOOL"),
                aSheet.getProperty("Configuration.ALLOW_UPDATE_CHECK"),
                aSheet.getProperty("Configuration.PROXY_HOST"),
                aSheet.getProperty("Configuration.PROXY_PORT"));
	    }
     }
     catch (Exception e) {
         
     }
  
     return new ResultReport(ResultReport.ResultStatus.SUCCESS, "http://docs.sun.com/doc/820-4836", "http://docs.sun.com/doc/820-4836", null, productError);
         
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
     }
     catch (Exception e) {
         
     }

    return new ResultReport(ResultReport.ResultStatus.SUCCESS, "http://docs.sun.com/doc/820-4836", "http://docs.sun.com/doc/820-4836", null, productError);
}

public void handleNotification (final Notification aNotification,
    final Object aHandback) {
    /* We received a message from the configurator, so reset the count */
    synchronized(this) {
      gWaitCount = 0;
    }
}


void configureGlassfish(String installDir, String adminPort, String httpPort) throws Exception {

    // set executable permissions on asadmin, stopserv, startserv 

    boolean isWindows = false;
    if (System.getProperty("os.name").indexOf("Windows") !=-1 ) {
        isWindows=true;
    }

    if (!isWindows) {

        String CLInames[] = {"asadmin", "stopserv", "startserv"};
        for (int i = 0; i < CLInames.length; i++) {
            Runtime.getRuntime().exec("/bin/chmod a+x " +
                               installDir + "/glassfish/bin/" + CLInames[i]);
		}
	Runtime.getRuntime().exec("/bin/chmod a+x " +
			installDir + "/bin/asadmin");
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
	    wrapperWriter.write ("\"" + installDir + "\\glassfish\\bin\\asadmin\" start-domain domain1\n");
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
	    wrapperWriter.write ("\"" + installDir + "\\glassfish\\bin\\asadmin\" stop-domain domain1\n");
            wrapperWriter.close();
            wrapperWriter = null;
	}
	else {
	    startWrapperFile = new File(installDir + "/glassfish/lib/asadmin-start-domain");
	    wrapperWriter = new FileWriter(startWrapperFile);
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
    }
      
    
    //create temporary password file for asadmin create-domain

        FileWriter writer = null;
        File pwdFile = null;        
        try {            
            pwdFile = File.createTempFile("asadminTmp", null);                        
            pwdFile.deleteOnExit();            
            writer = new FileWriter(pwdFile);            
            writer.write("AS_ADMIN_ADMINPASSWORD=anonymous\n");
            writer.write("AS_ADMIN_PASSWORD=anonymous\n");
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
 
    //construct asadmin command

        try {

            String asadminCommand;
        
            if (isWindows) {
                 asadminCommand = installDir + "\\glassfish\\bin\\asadmin.bat";
            }
            else {
                asadminCommand = installDir + "/glassfish/bin/asadmin";
            }

            String[] asadminCommandArray = { asadminCommand, "create-domain",
                "--savelogin",
		"--no-checkports",
                "--adminport", adminPort,
                "--user", "anonymous",
                "--passwordfile", pwdFile.getAbsolutePath(),
                "--instanceport", httpPort,
                "domain1"};
            
            LOGGER.log(Level.INFO, "Creating GlassFish domain");
            LOGGER.log(Level.INFO, "Admin port:" + adminPort);
            LOGGER.log(Level.INFO, "HTTP port:" + httpPort);
    
            ExecuteCommand asadminExecuteCommand = new ExecuteCommand(asadminCommandArray);
            asadminExecuteCommand.setOutputType(ExecuteCommand.ERRORS | ExecuteCommand.NORMAL);
            asadminExecuteCommand.setCollectOutput(true);
        
            asadminExecuteCommand.execute();
            hackAdminKeyfile(installDir);

            productError = asadminExecuteCommand.getErrors();
       } catch (Exception e) {

            LOGGER.log(Level.INFO, "Exception while creating GlassFish domain: " + e.getMessage()); 
       }
}
    private void hackAdminKeyfile(String install) { //TODO
       // Sorry to create this hack probably for the last time -- should go away soon
       File gd = new File (install, "glassfish");
       File ddd = new File(gd, "domains");
       File dd  = new File(ddd, "domain1");
       File dc  = new File(dd, "config");
       dc.mkdirs();
       File akf = new File(dc, "admin-keyfile");
       BufferedOutputStream bos = null;
       byte[] line0 = "#Default user: anonymous\n".getBytes();
       byte[] line1 = "anonymous;{SSHA}jBnM2TF2BglcyR3RtTfEXg0U18nbjwDKCfK9wg==;asadmin".getBytes();
       try {
           bos = new BufferedOutputStream(new FileOutputStream(akf));
           bos.write(line0);
           bos.write(line1);
       } catch(IOException ioe) {
           LOGGER.log(Level.INFO, "Exception while overwriting user in keyfile" + ioe.getMessage());
       } finally {
           if (bos != null) {
               try {
                   bos.close();
               } catch(IOException ioe) {
                   //can't do anything anyway
               }
           }
       }
    }
void configureUpdatetool(String installDir, String bootstrap, String allowUpdateCheck,
    String proxyHost, String proxyPort) throws Exception {

    
    boolean isWindows = false;
    if (System.getProperty("os.name").indexOf("Windows") !=-1 ) {
        isWindows=true;
    }


    // check whether to bootstrap at all

    if (bootstrap.equalsIgnoreCase("false")) {
        LOGGER.log(Level.INFO, "Skipping updatetool bootstrap");
        return;
    }

    String proxyURL = null;

    if ((proxyHost.length()>0) && (proxyPort.length()>0)) {
        proxyURL = proxyHost + ":" + proxyPort;
    }

    //adjust Windows path for use in properties file

    String installDirForward = installDir;

    if (isWindows) {
        installDirForward = installDir.replace('\\', '/');
    }
    
        
    
    //create temporary property file for bootstrap

        FileWriter writer = null;
        File propertiesFile = null;        
        try {            
            propertiesFile = File.createTempFile("bootstrapTmp", null);                                  propertiesFile.deleteOnExit();            
            writer = new FileWriter(propertiesFile); 
            writer.write("image.path=" + installDirForward + "\n");
            writer.write("install.pkg=true\n");
            writer.write("install.updatetool=true\n");
            writer.write("optin.update.notification=" + allowUpdateCheck + "\n");
            writer.write("optin.usage.reporting=" + allowUpdateCheck + "\n");
            if (proxyURL != null) {
                writer.write("proxy.URL=" + proxyURL + "\n");
            }
            writer.close();
            writer = null;
                 
            
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, "Error while creating properties file: " + ex.getMessage());
            // ensure that we delete the file should any exception occur
            if (propertiesFile != null) {
                try {
                    propertiesFile.delete();
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
 
    //construct the bootstrap command

        try {

            String javaCommand;
            String bootstrapJar;
        
            if (isWindows) {
                 javaCommand = System.getProperty("java.home") + "\\bin\\javaw.exe";
                 bootstrapJar = installDir + "\\updatetool\\lib\\ucbootstrap.jar";
            }
            else {
                javaCommand = System.getProperty("java.home") + "/bin/java";
                bootstrapJar = installDir + "/updatetool/lib/ucbootstrap.jar";
            }

            String[] javaCommandArray = { javaCommand, 
                "-jar" ,
                bootstrapJar,
                propertiesFile.getAbsolutePath()};
            
            LOGGER.log(Level.INFO, "Bootstrapping updatetool packages");
            
            ExecuteCommand javaExecuteCommand = new ExecuteCommand(javaCommandArray);
            javaExecuteCommand.setOutputType(ExecuteCommand.ERRORS | ExecuteCommand.NORMAL);
            javaExecuteCommand.setCollectOutput(true);
        
            javaExecuteCommand.execute();

            productError = javaExecuteCommand.getErrors();
       } catch (Exception e) {

            LOGGER.log(Level.INFO, "Exception while boostrapping updatetool: " + e.getMessage()); 
       }

    //register notifier if appropriate option is set
    
    if (allowUpdateCheck.equalsIgnoreCase("true")) {
        try {

            String configCommand;
        
            if (isWindows) {
                 configCommand = installDir + "\\updatetool\\bin\\updatetoolconfig.bat";
            }
            else {
                configCommand = installDir + "/updatetool/bin/updatetoolconfig";
            }

            String[] configCommandArray = { configCommand, 
                "--register" };
            
            LOGGER.log(Level.INFO, "Registering notifier process");
            
            ExecuteCommand configExecuteCommand = new ExecuteCommand(configCommandArray);
            configExecuteCommand.setOutputType(ExecuteCommand.ERRORS | ExecuteCommand.NORMAL);
            configExecuteCommand.setCollectOutput(true);
        
            configExecuteCommand.execute();

            productError = productError +configExecuteCommand.getErrors();
       } catch (Exception e) {

            LOGGER.log(Level.INFO, "Exception while registering notifier: " + e.getMessage()); 
       }
    }    

}

void unconfigureUpdatetool(String installDir) throws Exception {

    boolean isWindows = false;
    if (System.getProperty("os.name").indexOf("Windows") !=-1 ) {
        isWindows=true;
    }
    try {

            String configCommand;
        
            if (isWindows) {
                 configCommand = installDir + "\\updatetool\\bin\\updatetoolconfig.bat";
            }
            else {
                configCommand = installDir + "/updatetool/bin/updatetoolconfig";
            }

            String[] configCommandArray = { configCommand, 
                "--unregister" };
            
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
        if (isWindows) {
	    domainsDir = new File (installDir + "\\glassfish\\domains");
	    startWrapperFile = new File(installDir + "\\glassfish\\lib\\asadmin-start-domain.bat");
	    stopWrapperFile = new File(installDir + "\\glassfish\\lib\\asadmin-stop-domain.bat");
	}
	else {
            domainsDir = new File (installDir + "/glassfish/domains");
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
            deleteDirectory(domainsDir);
	}

    }
    catch (Exception e) {

        LOGGER.log(Level.INFO, "Exception while removing created files: " + e.getMessage()); 
    }

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
}
