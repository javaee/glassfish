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

import java.util.*;
import java.io.*;
//Added by Ludo to the Sun ONE Application Server Assembly Tool IASRI4691307
import javax.swing.*;

import com.sun.enterprise.Version;
import com.sun.enterprise.util.FileUtil;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.packager.ComponentPackager;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.tools.deployment.backend.JarInstaller;
import com.sun.enterprise.resource.ConnectorInfo;
import com.sun.enterprise.resource.NameNotFoundException;
import com.sun.enterprise.resource.DuplicateNameException;
import com.sun.enterprise.resource.ConfigurationPropertyException;


/** This is the entry point for the J2EE Deployment Tool. Use -help for usage help.
** @author Danny Coward
*/

public class Main 
{

    /* -------------------------------------------------------------------------
    ** Localization
    */

    private static LocalStringManagerImpl localStrings =
	new LocalStringManagerImpl(Main.class);

    //private static String VERSION = Version.version;    // IASRI 4691307 
    public static String VERSION = "1.0";   // IASRI 4691307 // NOI18N
//  private static String VERSION = 
//	localStrings.getLocalString(
//	"enterprise.tools.deployment.main.version",
//	"1.3");

    private static String NAME = 
	localStrings.getLocalString(
	"enterprise.tools.deployment.main.name",
	"Deployment tool version {0}.", 
	new Object[] { VERSION });

    private static String UI_STARTUP_MESSAGE = 
	localStrings.getLocalString(
	"enterprise.tools.deployment.main.defaultstartupmessage",
	"Starting Assembly tool, version {0}\n(Type 'assemblytool -help' for command line options.)",  // IASRI 4691307
	new Object[] { VERSION });

    private static String LOG_NAME =
	localStrings.getLocalString(
	"enterprise.tools.deployment.main.logname",
	"Deploytool");

    /* -------------------------------------------------------------------------
    */

    /** The deploy option.*/
    public static String DEPLOY_APPLICATION = "-deploy"; // NOI18N
    private static String EJB_WIZARD = "-ejbWizard"; // NOI18N
    private static String PACKAGE_EJBS = "-packageEjbs"; // NOI18N
    private static String LIST_APPLICATIONS = "-listApps"; // NOI18N
    private static String NO_OVER_WRITE = "-noOverWrite"; // NOI18N
    private static String DEPLOY_CONNECTOR = "-deployConnector"; // NOI18N
    private static String UNDEPLOY_CONNECTOR = "-undeployConnector"; // NOI18N
    private static String LIST_CONNECTORS = "-listConnectors"; // NOI18N
    private static String ADD_FACTORY = "-addConnectionFactory"; // NOI18N
    private static String REMOVE_FACTORY = "-removeConnectionFactory"; // NOI18N

    /** The undeploy option.*/
    public static String UNINSTALL = "-uninstall"; // NOI18N
    /** Option to generate SQL.*/
    public static String GENERATE_SQL = "-generateSQL"; // NOI18N
    /** The overwrite coption for SQL generation.*/
    public static String DONT_OVER_WRITE_SQL = "-noOverWrite"; // NOI18N
    /** Option to bring up the UI.*/
    public static String UI = "-ui"; // NOI18N
    /** Help option.*/
    public static String HELP = "-help"; // NOI18N

    /* -------------------------------------------------------------------------
    */

    /** The main method for the J2EE deployment tool.*/
    public static void main(String args[]) {
        
        com.sun.enterprise.util.Utility.checkJVMVersion();
//Added by Ludo to the Sun ONE Application Server Assembly Tool IASRI4691307
//This look and feel is more compliant with the NetBeans one. Do not use BOLD for Menus and Labels
   	int uiFontSize=11;
            java.awt.Font nbDialogPlain = new java.awt.Font ("Dialog", java.awt.Font.PLAIN, uiFontSize); // NOI18N
            java.awt.Font nbSerifPlain = new java.awt.Font ("Serif", java.awt.Font.PLAIN, uiFontSize); // NOI18N
            java.awt.Font nbSansSerifPlain = new java.awt.Font ("SansSerif", java.awt.Font.PLAIN, uiFontSize); // NOI18N
            java.awt.Font nbMonospacedPlain = new java.awt.Font ("Monospaced", java.awt.Font.PLAIN, uiFontSize); // NOI18N
            UIManager.getDefaults ().put ("Button.font", nbDialogPlain); // NOI18N
            UIManager.getDefaults ().put ("ToggleButton.font", nbDialogPlain); // NOI18N
            UIManager.getDefaults ().put ("RadioButton.font", nbDialogPlain); // NOI18N
            UIManager.getDefaults ().put ("CheckBox.font", nbDialogPlain); // NOI18N
            UIManager.getDefaults ().put ("ColorChooser.font", nbDialogPlain); // NOI18N
            UIManager.getDefaults ().put ("ComboBox.font", nbDialogPlain); // NOI18N
            UIManager.getDefaults ().put ("Label.font", nbDialogPlain); // NOI18N
            UIManager.getDefaults ().put ("List.font", nbDialogPlain); // NOI18N
            UIManager.getDefaults ().put ("MenuBar.font", nbDialogPlain); // NOI18N
            UIManager.getDefaults ().put ("MenuItem.font", nbDialogPlain); // NOI18N
            UIManager.getDefaults ().put ("RadioButtonMenuItem.font", nbDialogPlain); // NOI18N
            UIManager.getDefaults ().put ("CheckBoxMenuItem.font", nbDialogPlain); // NOI18N
            UIManager.getDefaults ().put ("Menu.font", nbDialogPlain); // NOI18N
            UIManager.getDefaults ().put ("PopupMenu.font", nbDialogPlain); // NOI18N
            UIManager.getDefaults ().put ("OptionPane.font", nbDialogPlain); // NOI18N
            UIManager.getDefaults ().put ("Panel.font", nbDialogPlain); // NOI18N
            UIManager.getDefaults ().put ("ProgressBar.font", nbDialogPlain); // NOI18N
            UIManager.getDefaults ().put ("ScrollPane.font", nbDialogPlain); // NOI18N
            UIManager.getDefaults ().put ("Viewport.font", nbDialogPlain); // NOI18N
            UIManager.getDefaults ().put ("TabbedPane.font", nbDialogPlain); // NOI18N
            UIManager.getDefaults ().put ("Table.font", nbDialogPlain); // NOI18N
            UIManager.getDefaults ().put ("TableHeader.font", nbDialogPlain); // NOI18N
            UIManager.getDefaults ().put ("TextField.font", nbSansSerifPlain); // NOI18N
            UIManager.getDefaults ().put ("PasswordField.font", nbMonospacedPlain); // NOI18N
            UIManager.getDefaults ().put ("TextArea.font", nbMonospacedPlain); // NOI18N
            UIManager.getDefaults ().put ("TextPane.font", nbSerifPlain); // NOI18N
            UIManager.getDefaults ().put ("EditorPane.font", nbSerifPlain); // NOI18N
            UIManager.getDefaults ().put ("TitledBorder.font", nbDialogPlain); // NOI18N
            UIManager.getDefaults ().put ("ToolBar.font", nbDialogPlain); // NOI18N
            UIManager.getDefaults ().put ("ToolTip.font", nbSansSerifPlain); // NOI18N
            UIManager.getDefaults ().put ("Tree.font", nbDialogPlain); // NOI18N
//End Added by Ludo to the Sun ONE Application Server Assembly Tool IASRI4691307        
       
            /*
        for (int i=0; i< args.length; i++){
            System.out.println("args :" + args[i]);
        }
        System.out.println("==========");
        
        Properties props = System.getProperties();
        for(Enumeration en = props.propertyNames(); en.hasMoreElements(); ){
            String key = (String)en.nextElement();
            System.out.println(key + " = " + (String) (props.get(key)));
        }
        System.out.println("======");
             */
            
	/* startup UI */
	if (args.length == 0) {
	    System.out.println(UI_STARTUP_MESSAGE);
	    new DeployTool(true);
	    return;
	} else
	if (args[0].equals(UI)) {
	    new DeployTool(true);
	    return;
	}
            
        //anissa IASRI 4691307
        if (args[0].equals("-userdir")){ // NOI18N
            if (args.length >=2){
                new DeployTool(true, args[1]);
                return;
            }else {
                help();
            }
        }
        help();
        
        // anissa IASRI 4691307  commented out all the unsupported option.
	/* misc */
        /*
	if (args[0].equals(LIST_APPLICATIONS)) { 
            if( args.length >= 2 ) {
                String server = args[1];
                listApplications(server);                
                return;
            } else {
                help();
                return;
            }
        } else 
	if (args[0].equals(HELP)) {
	    help();
	    return;
	} else 
	if (args[0].equals(EJB_WIZARD)) {
	    // XXX no longer supported
	    //new com.sun.enterprise.tools.deployment.ui.NewAppClientWizard(null);
	    return;
	}
        */
        
	/* command line support */
        /*
	DeployTool deployTool = new DeployTool(false);

	if (args[0].equals(DEPLOY_APPLICATION)) {
        	int argIndex = 1;
        	boolean allowRedeploy = true;
        	if  (args.length <= argIndex) {
        	    help();
        	    return;
        	}
        	if  (args[argIndex].equals(NO_OVER_WRITE)) {
        	    allowRedeploy = false;
        	    argIndex++;
        	}
		if (args.length < argIndex+2) {
		    help();
		    return;
		}

		String applicationJar = args[argIndex]; argIndex++;
		String serverName = args[argIndex]; argIndex++;
		
		File clientCodeFile = null;
		if (args.length > argIndex) {
		    clientCodeFile = new File(args[argIndex]);
		}
		// System.out.println("Deploy on:" + serverName);
                String applicationName = null;
                File applicationJarFile = new File(applicationJar);
		try {
                    applicationName = ApplicationArchivist.getApplicationName(new File(applicationJar));
		} catch (Exception ioe) {
		    ioe.printStackTrace();
		    Log.print(LOG_NAME, localStrings.getLocalString(
			"enterprise.tools.deployment.main.warning",
			"{0}", new Object[] {ioe.getMessage()}));
		    System.exit(1);
		}
		try {

                    boolean isInstalled =         
                        deployTool.getServerManager().isInstalled(applicationName, serverName); 
			if (isInstalled) { 
                            if (!allowRedeploy) {
			        Log.print(LOG_NAME, localStrings.getLocalString(
				    "enterprise.tools.deployment.main.logmessagetodeploy",
				    "To deploy the application {0} must be uninstalled before it can be re-deployed", new Object[] {applicationName}));
			        System.exit(1); 
				}
                        else {
                            System.out.println("Note : application " + applicationName + 
                                " is already installed.  Redeploying...");
                        }
		    }
		    deployTool.deploy(applicationName, applicationJarFile, serverName, null, clientCodeFile);
		} catch (Exception t) {
		    Log.print(LOG_NAME, localStrings.getLocalString(
			"enterprise.tools.deployment.main.warning",
			"{0}", new Object[] {t.getMessage()})); 
		    System.exit(1);
		}
		System.exit(0);
	}

	if (args[0].equals(PACKAGE_EJBS)) {
		if (args.length < 5) {
		    help();
		    return;
		}
		String codebase = args[1];
		String classes = args[2];
		String descriptorFile = args[3];
		String jarFile = args[4];
		Vector classNames = new Vector();
		StringTokenizer st = new StringTokenizer(classes, ":");
		while(st.hasMoreTokens()) {
		    classNames.addElement(st.nextToken());
		}
		String optionalEjbBundleFilename = "";
		String optionalRuntimeFiename = "";
		if (args.length > 5) {
		    if (args[5] != null) {
			optionalEjbBundleFilename = args[5];
		    }
		    if (args[6] != null) {
			optionalRuntimeFiename = args[6];
		    }
		}
		try {
		    deployTool.getComponentPackager().packageEjbs(codebase, classNames, 
			descriptorFile, jarFile, optionalEjbBundleFilename, optionalRuntimeFiename);
		} catch (Exception e) {
		    deployTool.getComponentPackager().handlePackagingException(e);
		    System.exit(1);
		}
		System.exit(0);
	}	
	    
	     
	    
	if (args[0].equals(UNINSTALL)) {
		if (args.length < 3) {
		    help();
		    return;
		}
		String applicationName = args[1];
		String serverName = args[2];
		try {
		    if (deployTool.getServerManager().isInstalled(applicationName, serverName)) {
			deployTool.getServerManager().undeployApplication(applicationName, serverName);
			Log.print(LOG_NAME, localStrings.getLocalString(
			    "enterprise.tools.deployment.main.applicationuninstalledfromserver",
			    "The application {0} was uninstalled from {1}", new Object[] {applicationName, serverName}));
		    } else {
			Log.print(LOG_NAME, localStrings.getLocalString(
			    "enterprise.tools.deployment.main.applicationnotdeployed",
			    "The application {0} is not deployed on {1}", new Object[] {applicationName, serverName}));
		    }
		    return;
		} catch (Exception e) {
		    Log.print(LOG_NAME, e.getMessage());
		    System.exit(1);
		}
	}

	if (args[0].equals(GENERATE_SQL)) {
		String applicationFilename = args[1];
		String serverName = args[2];
		boolean overWrite = true;
		if (args.length > 3) {
		    if (args[3].equals(DONT_OVER_WRITE_SQL)) {
			overWrite = false;
		    }
		}
                // Call sql generator for all ejb-jars in the application.  
                // Set VM return codes as follows based on results :
                // 0 : no errors
                // 1 : EJB QL error
                // 2 : other type of error
                // NOTE : This return code behavior is NOT a publicly documented contract.
                // It is an internal contract used for testing purposes and is
                // subject to change.  
		try {
		    deployTool.doGenerateSQL(applicationFilename, serverName, overWrite);
                } catch (com.sun.ejb.ejbql.EjbQLParseException e) {
                    e.printStackTrace();
                    System.exit(1);
		} catch (Throwable t) {
		    t.printStackTrace();
		    System.exit(2);
		}
		return;
	}

	if (args[0].equals(DEPLOY_CONNECTOR)) {

                if (args.length != 3) help();
                int i = 1;
                String rarFilename = args[i++];
                String serverName = args[i++];

                try {
                    JarInstaller backend = deployTool.getServerManager().
                        getServerForName(serverName);
                    File file = new File(rarFilename);
                    if (!file.exists()) {
                        System.out.println(localStrings.getLocalString
                                           ("deploytool.msg001",
                                            "File does not exist: {0}",
                                            new Object[] {file.toString()}));
                        System.exit(1);
                    }
                    if (!ConnectorArchivist.isConnector(file)) {
                        System.out.println(localStrings.getLocalString
                                           ("deploytool.msg002",
                                         "File is not a resource archive: {0}",
                                            new Object[] {file.toString()}));
                        System.exit(1);
                    }
                    byte[] data = new byte[(int) file.length()];
                    DataInputStream ds =
                        new DataInputStream(new BufferedInputStream
                                            (new FileInputStream(file)));
                    ds.readFully(data);
                    String name = file.getName();
                    backend.deployConnector(data, name);
                    System.out.println(localStrings.getLocalString
                                       ("deploytool.msg003",
                                        "Resource adapter {0} is deployed successfully", new Object[] {name}));
                } catch (Exception ex) {
                    handleDeploymentException(ex);
                }
                return;
	}

	// XXX still used by CTS. Should remove at FCS
	if (args[0].equals(ADD_FACTORY)) {

                if (args.length < 4) help();
                int i = 1;
                String name = args[i++];
                String jndiName = args[i++];
                String serverName = args[i++];
                Properties props = new Properties();
                while (i < args.length) {
                    String str = args[i++];
                    int idx = str.indexOf('=');
                    String propName = str.substring(0, idx);
                    String val = str.substring(idx + 1);
                    // strip out delimiters
                    if (val.startsWith("\"") &&
                        val.endsWith("\"")) {
                        val = val.substring(1, val.length() - 1);
                    }
                    if (val.startsWith("'") &&
                        val.endsWith("'")) {
                        val = val.substring(1, val.length() - 1);
                    }
                    props.put(propName, val);
                }

                try {
                    JarInstaller backend = deployTool.getServerManager().
                        getServerForName(serverName);
                    String appName = null;
                    String connectorName = name;
                    int index = name.indexOf(':');
                    if (index != -1) {
                        appName = name.substring(0, index);
                        connectorName = name.substring(index+1);
                    }
                    // XXX - should pass user/password
                    backend.addConnectionFactory(appName, connectorName,
                                                 jndiName, null, null,
                                                 props);
                    System.out.println("Connector factory " + jndiName +
                                       " is added successfully");
                } catch (Exception ex) {
                    handleDeploymentException(ex);
                }
                return;
	}

	// XXX still used by CTS. Should remove at FCS
	if (args[0].equals(REMOVE_FACTORY)) {
                if (args.length != 3) help();
                int i = 1;
                String jndiName = args[i++];
                String serverName = args[i++];
                try {
                    JarInstaller backend = deployTool.getServerManager().
                        getServerForName(serverName);
                    backend.removeConnectionFactory(jndiName);
                    System.out.println("Connector factory " + jndiName +
                                       " is removed successfully");
                } catch (Exception ex) {
                    handleDeploymentException(ex);
                }
                return;
	}

	if (args[0].equals(UNDEPLOY_CONNECTOR)) {
                if (args.length != 3) help();
                int i = 1;
                String name = args[i++];
                String serverName = args[i++];

                try {
                    JarInstaller backend = deployTool.getServerManager().
                        getServerForName(serverName);
                    backend.undeployConnector(name);
                    System.out.println(localStrings.getLocalString(
                        "deploytool.msg004",
                        "Resource adapter {0} is undeployed successfully", new Object[] {name}));
                } catch (NameNotFoundException ex) {
                    String msg = localStrings.getLocalString
                        ("connector.not.found", "",
                         new Object[] {name});
                    System.out.println(msg);
                } catch (Exception ex) {
                    handleDeploymentException(ex);
                }
                return;
	}

	if (args[0].equals(LIST_CONNECTORS)) {

                if (args.length != 2) help();
                int j = 1;
                String serverName = args[j++];

                try {
                    JarInstaller backend = deployTool.getServerManager().getServerForName(serverName);
                    ConnectorInfo v = backend.listConnectors();
                    if (v.connectors.length == 0) {
                        System.out.println
                            (localStrings.getLocalString
                             ("no.connector.installed", ""));
                    } else {
                        System.out.println
                            (localStrings.getLocalString
                             ("installed.connectors", ""));
                        int size = v.connectors.length;
                        for (int i=0; i<size; i++) {
                            String msg = localStrings.getLocalString
                                ("connector.info", "",
                                 new Object[] {
                                    v.connectors[i]
                                 });
                            System.out.println(msg);
                        }
                    } 
                    System.out.println();
                    if (v.connectionFactories.length == 0) {
                        System.out.println
                            (localStrings.getLocalString
                             ("no.factories.installed", ""));
                    } else {
                        System.out.println
                            (localStrings.getLocalString
                             ("installed.factories", ""));
                        int size = v.connectionFactories.length;
                        for (int i=0; i<size; i++) {
                            String msg = localStrings.getLocalString
                                ("factory.info", "",
                                 new Object[] {
                                    v.connectionFactories[i]
                                 });
                            System.out.println(msg);
                        }
                    } 
                    
                } catch (Exception ex) {
                    handleDeploymentException(ex);
                }
                return;
	}

	// otherwise....
	help();
         *
         *
         * IASRI 4691307 ENd of commented code.
         */

    }   
   
    /** Prints out the deploytool help message.*/
    public static void help() {

	System.out.println(localStrings.getLocalString
                           ("enterprise.tools.deployment.main.deploymenttoolversion", "The deployment tool version is {0}", new Object[] {VERSION})); // NOI18N

	System.out.println(localStrings.getLocalString
                           ("enterprise.tools.deployment.main.options",
                            "Options:"));
        System.out.print("    "); // NOI18N
        
        /* IASRI 4691307 remove help msg for unsupported options.
         *
	System.out.println(localStrings.getLocalString
                           ("enterprise.tools.deployment.main.runstoolwithUI",
                            "{0}    Runs the tool with a UI (default mode)", new Object[] {UI}));
        System.out.print("    ");
	System.out.println(localStrings.getLocalString
                           ("enterprise.tools.deployment.main.displayhelpmessage", "{0}    Display this help message", new Object[] {HELP}));
        System.out.print("    ");
	System.out.println(localStrings.getLocalString
                           ("enterprise.tools.deployment.main.lastparameteroptional", "{0} <ear file> <server name> [<client jar>]", new Object[] {DEPLOY_APPLICATION}));
        System.out.print("    ");
	System.out.println(localStrings.getLocalString
                           ("enterprise.tools.deployment.main.uninstallapplicationfromserver", "{0} <application name> <server name>", new Object[] {UNINSTALL}));
        System.out.print("    ");
	System.out.println(localStrings.getLocalString
			   ("enterprise.tools.deployment.main.listApps",
			    "{0} <server name>", new Object[] {LIST_APPLICATIONS}));
	System.out.print("    ");
	System.out.println(localStrings.getLocalString
			   ("enterprise.tools.deployment.main.deployConnector",
			    "{0} <rar filename> <server name>",
			    new Object[] {DEPLOY_CONNECTOR}));
	System.out.print("    ");
	System.out.println(localStrings.getLocalString
			   ("enterprise.tools.deployment.main.undeployConnector",
			    "{0} <rar filename> <server name>",
			    new Object[] {UNDEPLOY_CONNECTOR}));
	System.out.print("    ");
	System.out.println(localStrings.getLocalString
			   ("enterprise.tools.deployment.main.listConnectors",
			    "{0} <server name>",
			    new Object[] {LIST_CONNECTORS}));
        System.out.print("    ");
	System.out.println(localStrings.getLocalString
                           ("enterprise.tools.deployment.main.generateSQL", 
			    "{0} <ear file> <server name> [-noOverWrite]", 
			    new Object[] {GENERATE_SQL}));
         * End of changes IASRI 4691307
        */
        
        // Add -userdir option IASRI 4691307
        System.out.println("-userdir <userdir>"); // NOI18N
        
	System.exit(1);
    }

    public static void listApplications(String serverName) {
        try {
	        DeployTool deployTool = new DeployTool(false);
            JarInstaller backend = deployTool.getServerManager().getServerForName(serverName);
            Vector deployedApps = backend.getApplicationNames();
            if( deployedApps.size() > 0 ) {
                System.out.println(localStrings.getLocalString
                        ("enterprise.tools.deployment.main.followingapps", 
                                "The following apps are deployed on {0}:",
                                new Object[] {serverName}));
                for(int appIndex = 0; appIndex < deployedApps.size(); appIndex++) {
                    System.out.println("\t" + (String) deployedApps.elementAt(appIndex)); // NOI18N
                }
			}
            else {
                System.out.println(localStrings.getLocalString
                        ("enterprise.tools.deployment.main.nodeployedapps", // NOI18N
                                "There are no deployed applications on {0}", // NOI18N
                                new Object[] {serverName}));
            }
        }
        catch(Exception e) {
            // @@@
            Log.print(LOG_NAME, e.getMessage());
        }
    }

    static private void handleDeploymentException(Exception ex) {
        if (ex instanceof DuplicateNameException) {
            String msg = localStrings.getLocalString
                ("duplicatename.exception", "",
                 new Object[] {ex.getMessage()});
            System.err.println(msg);
        } else if (ex instanceof NameNotFoundException) {
            String msg = localStrings.getLocalString
                ("namenotfound.exception", "",
                 new Object[] {ex.getMessage()});
            System.err.println(msg);
        } else if (ex instanceof ConfigurationPropertyException) {
            String msg = localStrings.getLocalString
                ("configproperty.exception", "",
                 new Object[] {ex.getMessage()});
            System.err.println(msg);
        } else {
            String msg = localStrings.getLocalString
                ("generic.exception", "",
                 new Object[] {ex.toString()});
            System.err.println(msg);
        }
    }

}

