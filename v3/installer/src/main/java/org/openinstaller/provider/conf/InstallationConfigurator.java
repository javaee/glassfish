/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007-2010 Sun Microsystems, Inc. All rights reserved.
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

import org.openinstaller.config.PropertySheet;
import org.openinstaller.util.*;
import org.glassfish.installer.util.*;
import com.sun.pkg.bootstrap.Bootstrap;
import javax.management.Notification;
import javax.management.NotificationListener;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Properties;
import java.util.Map;

/* A big fat chunk of Code, to be completely thrown away after MS3, to be
 * rewritten and refactored.
 */
public final class InstallationConfigurator implements Configurator, NotificationListener {

    private final String productName;
    private final String installDir;
    private String jdkHome;
    private Map<String, String> configData;
    private String productError = null;
    private static final Logger LOGGER;
    //Path to asenv.conf or asenv.bat
    private String glassfishConfigFilePath;
    //Path to asadmin.bat or asadmin
    private String asadminScriptPath;
    //Class wide flag to hold the overall configuration status.
    private boolean configSuccessful;

    /* List of port numbers currently defaulted by asadmin command */
    private String glassfishPortArray[][] = {
        {"jms.port", "7676"},
        {"domain.jmxPort", "8686"},
        {"orb.listener.port", "3700"},
        {"http.ssl.port", "8181"},
        {"orb.ssl.port", "3820"},
        {"orb.mutualauth.port", "3920"}
    };

    //Configuration data copied from OI panels.
    public Map<String, String> getConfigData() {
        return configData;
    }

    //Helper to access config data's key/values
    public String getConfigValue(String configKey) {
        return configData.get(configKey);
    }

    public void setConfigData(Map<String, String> configData) {
        this.configData = configData;
    }

    //Return configuration file path for GlassFish
    public String getGlassfishConfigFilePath() {
        return glassfishConfigFilePath;
    }

    //Assign appropriate configuration file path
    public void setGlassfishConfigFilePath() {
        if (OSUtils.isWindows()) {
            glassfishConfigFilePath = installDir + "\\glassfish\\config\\asenv.bat";
        } else {
            glassfishConfigFilePath = installDir + "/glassfish/config/asenv.conf";
        }
    }

    //Assign appropriate asadmin file path.
    public void setAsadminScriptPath() {
        if (OSUtils.isWindows()) {
            this.asadminScriptPath = installDir + "\\glassfish\\bin\\asadmin.bat";
        } else {
            this.asadminScriptPath = installDir + "/glassfish/bin/asadmin";
        }
    }

    public String getAsadminScriptPath() {
        return this.asadminScriptPath;
    }

    public String[][] getPortArray() {
        return glassfishPortArray;
    }

    public void setPortArray(String[][] portArray) {
        this.glassfishPortArray = portArray;
    }

    static {
        LOGGER = Logger.getLogger(ClassUtils.getClassName());
    }
    //OI
    private int gWaitCount;

    public InstallationConfigurator(final String productName, final String altRootDir,
            final String xcsFilePath, final String installDir) {

        this.productName = productName;
        this.installDir = installDir;
        setPortArray(new String[][]{
                    {"jms.port", "7676"},
                    {"domain.jmxPort", "8686"},
                    {"orb.listener.port", "3700"},
                    {"http.ssl.port", "8181"},
                    {"orb.ssl.port", "3820"},
                    {"orb.mutualauth.port", "3920"}
                });
        setGlassfishConfigFilePath();
        setAsadminScriptPath();
    }

    /*
     * OI hook to call individual product configurations.
     */
    public ResultReport configure(final PropertySheet propSheet, final boolean validateFlag) throws EnhancedException {

        configSuccessful = true;
        /* Storing a reference of Property Sheet to a local Hash, so that other
         * parts of this class can access the configuration data anytime needed.
         */
        setConfigData(propSheet.getAllProps());
        try {
            if (productName.equals("glassfish")) {
                LOGGER.log(Level.INFO, "Configuring GlassFish");
                configureGlassfish();
            }

            if (productName.equals("updatetool")) {
                LOGGER.log(Level.INFO, "Configuring Updatetool");
                configureUpdatetool();
            }
        } catch (Exception e) {
            // Don't do anything as major error detection is handled throughout
            // this class where appropriate and fatal.
        }

        ResultReport.ResultStatus status =
                configSuccessful ? ResultReport.ResultStatus.SUCCESS
                : ResultReport.ResultStatus.FAIL;
        return new ResultReport(status, "http://docs.sun.com/doc/820-7690 ", "http://docs.sun.com/doc/820-7690", null, productError);
    }

    /* Mandatory implementation of OI method?!?, not sure why? */
    public PropertySheet getCurrentConfiguration() {

        return new PropertySheet();
    }

    /*
     * OI hook to call individual product configurations.
     */
    public ResultReport unConfigure(final PropertySheet propSheet, final boolean validateFlag) {

        try {
            if (productName.equals("glassfish")) {
                LOGGER.log(Level.INFO, "Unconfiguring GlassFish");
                unconfigureGlassfish();
            }

            if (productName.equals("updatetool")) {
                LOGGER.log(Level.INFO, "Unconfiguring Updatetool");
                LOGGER.log(Level.INFO, "Installation directory: " + installDir);
                unconfigureUpdatetool();
                org.glassfish.installer.util.FileUtils.deleteDirectory(new File(installDir + File.separator + "updatetool"));
                org.glassfish.installer.util.FileUtils.deleteDirectory(new File(installDir + File.separator + "pkg"));
            }
            /* Delete the newly created folder, on windows. No incremental uninstallation, so delete everything.*/
            String folderName =
                    (String) TemplateProcessor.getInstance().getFromDataModel("PRODUCT_NAME");
            if (OSUtils.isWindows()) {
                WindowsShortcutManager wsShortMgr = new WindowsShortcutManager();
                wsShortMgr.deleteFolder(folderName);
            }
        } catch (Exception e) {
            /* Ignore this for now */
        }

        return new ResultReport(ResultReport.ResultStatus.SUCCESS, "http://docs.sun.com/doc/820-7690", "http://docs.sun.com/doc/820-7690", null, productError);
    }

    /* Mandatory implementation of OI method?!?, not sure why? */
    public void handleNotification(final Notification aNotification,
            final Object aHandback) {
        /* We received a message from the configurator, so reset the count */
        synchronized (this) {
            gWaitCount = 0;
        }
    }

    /* Configure product glassfish */
    public void configureGlassfish() {

        // set executable permissions on most used scripts

        if (!OSUtils.isWindows()) {
            org.glassfish.installer.util.FileUtils.setExecutable(installDir + "/glassfish/bin/asadmin");
            org.glassfish.installer.util.FileUtils.setExecutable(installDir + "/glassfish/bin/stopserv");
            org.glassfish.installer.util.FileUtils.setExecutable(installDir + "/glassfish/bin/startserv");
            org.glassfish.installer.util.FileUtils.setExecutable(installDir + "/glassfish/bin/jspc");
            org.glassfish.installer.util.FileUtils.setExecutable(installDir + "/bin/asadmin");
        }

        // Update asenv
        updateConfigFile();

        // Unpack all of *pack*ed files.
        unpackJars();

        // Find out what we are going to do? Create the domain or instance?
        String configMode = null;
        try {
            configMode = (String) ConfigHelper.getValue("NodeServerOptions.configoptions.CONFIGURATION_MODE");
        } catch (EnhancedException ex) {
            //Default to CREATE_DOMAIN.
            configMode = "CREATE_DOMAIN";
        }
        if (configMode.equalsIgnoreCase("CREATE_INSTANCE")) {
            createInstance();
        }

        if (configMode.equalsIgnoreCase("CREATE_DOMAIN")) {
            //create domain startup/shutdown wrapper scripts
            if (OSUtils.isWindows()) {
                setupWindowsDomainScripts();
            } else {
                setupUnixDomainScripts();
            }
            createDomain();

            // Create a OS service if user chooses to do do.
            if (configData.get("CREATE_SERVICE").equalsIgnoreCase("true")) {
                createDomainService();
            }
        }

        // Setup start->menu shortcuts for windows.
        if (OSUtils.isWindows()) {
            try {
                createServerShortCuts();
            } catch (EnhancedException ex) {
                //Ignore for now.
                //Logger.getLogger(InstallationConfigurator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /* Create domain by running asadmin create-domain command.
    On Mac OS direcly invokes the equivalent java command line.
     */
    private void createDomain() {

        //create temporary password file for asadmin create-domain
        String pwdFilePath = createPasswordFile();

        //construct asadmin command
        ExecuteCommand asadminExecuteCommand = assembleCreateDomainCommand(pwdFilePath);

        LOGGER.log(Level.INFO, "Creating GlassFish domain");
        LOGGER.log(Level.INFO, "with the following command line");

        //DEBUG

        LOGGER.log(Level.INFO,
                ExecuteCommand.expandCommand(asadminExecuteCommand.getCommand()));

        String existingPath = System.getenv("PATH");
        LOGGER.log(Level.INFO, "Existing PATH: " + existingPath);
        String newPath = jdkHome + File.separator + "bin"
                + File.pathSeparator + existingPath;
        LOGGER.log(Level.INFO, "New PATH: " + newPath);

        try {
            asadminExecuteCommand.putEnvironmentSetting("PATH", newPath);
            asadminExecuteCommand.setOutputType(ExecuteCommand.ERRORS | ExecuteCommand.NORMAL);
            asadminExecuteCommand.setCollectOutput(true);
            asadminExecuteCommand.execute();
            LOGGER.log(Level.INFO, "Asadmin output: " + asadminExecuteCommand.getAllOutput());
            // Look for the string failed till asadmin bugs related to stderr are resolved.
            // Ugly/Buggy, but works for now.
            if (asadminExecuteCommand.getAllOutput().indexOf("failed") != -1) {
                configSuccessful = false;
            }

        } catch (Exception e) {
            LOGGER.log(Level.INFO, "In exception, asadmin output: " + asadminExecuteCommand.getAllOutput());
            LOGGER.log(Level.INFO, "Exception while creating GlassFish domain: " + e.getMessage());
            configSuccessful = false;
        }
    }
    /* Returns the command and required arguments for creating the glassfish domain. */

    private ExecuteCommand assembleCreateDomainCommand(String pwdFilePath) {

        String[] asadminCommandArray = {getAsadminScriptPath(),
            "--user", configData.get("ADMIN_USER"),
            "--passwordfile", pwdFilePath,
            "create-domain",
            "--savelogin",
            "--checkports=false",
            "--adminport", configData.get("ADMIN_PORT"),
            "--instanceport", configData.get("HTTP_PORT"),
            "--domainproperties="
            + getDomainProperties(configData.get("ADMIN_PORT"),
            configData.get("HTTP_PORT")),
            "domain1"};

        String[] asadminCommandArrayMac = {"java", "-jar",
            installDir + "/glassfish/modules/admin-cli.jar",
            "--user", configData.get("ADMIN_USER"),
            "--passwordfile", pwdFilePath,
            "create-domain",
            "--savelogin",
            "--checkports=false",
            "--adminport", configData.get("ADMIN_PORT"),
            "--instanceport", configData.get("HTTP_PORT"),
            "--domainproperties="
            + getDomainProperties(configData.get("ADMIN_PORT"),
            configData.get("HTTP_PORT")),
            "domain1"};
        try {
            return OSUtils.isMac() || OSUtils.isAix() ? new ExecuteCommand(asadminCommandArrayMac) : new ExecuteCommand(asadminCommandArray);
        } catch (InvalidArgumentException ex) {
            configSuccessful = false;
        }
        return null;
    }

    /* Run configuration steps for update tool component. */
    public void configureUpdatetool() throws Exception {

        // set execute permissions for UC utilities
        if (!OSUtils.isWindows()) {
            org.glassfish.installer.util.FileUtils.setExecutable(installDir + "/bin/pkg");
            org.glassfish.installer.util.FileUtils.setExecutable(installDir + "/bin/updatetool");
        }

        setupUpdateToolScripts();



        // check whether to bootstrap at all
        if (!ConfigHelper.getBooleanValue("updatetool.Configuration.BOOTSTRAP_UPDATETOOL")) {
            LOGGER.log(Level.INFO, "Skipping updatetool bootstrap");
        } else {
            boolean allowUpdateCheck = ConfigHelper.getBooleanValue("updatetool.Configuration.ALLOW_UPDATE_CHECK");
            String proxyHost = configData.get("PROXY_HOST");
            String proxyPort = configData.get("PROXY_PORT");
            //populate bootstrap properties
            Properties props = new Properties();
            if (OSUtils.isWindows()) {
                props.setProperty("image.path", installDir.replace('\\', '/'));
            } else {
                props.setProperty("image.path", installDir);
            }
            props.setProperty("install.pkg", "true");
            props.setProperty("install.updatetool", "true");
            props.setProperty("optin.update.notification",
                    allowUpdateCheck ? "true" : "false");

            props.setProperty("optin.usage.reporting",
                    allowUpdateCheck ? "true" : "false");
            if ((proxyHost.length() > 0) && (proxyPort.length() > 0)) {
                props.setProperty("proxy.URL",
                        "http://" + proxyHost + ":" + proxyPort);
            }
            LOGGER.log(Level.INFO, "Bootstrapping Updatetool");
            //invoke bootstrap
            Bootstrap.main(props, LOGGER);

        }
        // Create the required windows start->menu shortcuts for updatetool.
        if (OSUtils.isWindows()) {
            createUpdatetoolShortCuts();
        }
    }

    /* Undo updatetool configuration and post-installation setups.*/
    public void unconfigureUpdatetool() throws Exception {
        /* Try to shutdown the notifer. Don't do this on Mac, the notifier command
        does not work on Mac, refer to Issue #7348. */
        if (!OSUtils.isMac() && !OSUtils.isAix()) {
            try {
                String shutdownCommand;
                if (OSUtils.isWindows()) {
                    shutdownCommand = installDir + "\\updatetool\\bin\\updatetool.exe";
                } else {
                    shutdownCommand = installDir + "/updatetool/bin/updatetool";
                }
                String[] shutdownCommandArray = {shutdownCommand, "--notifier", "--shutdown"};
                LOGGER.log(Level.INFO, "Shutting down notifier process");
                ExecuteCommand shutdownExecuteCommand = new ExecuteCommand(shutdownCommandArray);
                shutdownExecuteCommand.setOutputType(ExecuteCommand.ERRORS | ExecuteCommand.NORMAL);
                shutdownExecuteCommand.setCollectOutput(true);
                shutdownExecuteCommand.execute();
            } catch (Exception e) {
                LOGGER.log(Level.INFO, "Exception while unregistering notifier: " + e.getMessage());
                // Its okay to ignore this for now.
            }
        } /* End, conditional code for Mac and Aix. */

        /* Now unregister notifer. */
        try {
            String configCommand;
            if (OSUtils.isWindows()) {
                configCommand = installDir + "\\updatetool\\bin\\updatetoolconfig.bat";
            } else {
                configCommand = installDir + "/updatetool/bin/updatetoolconfig";
            }
            String[] configCommandArray = {configCommand, "--unregister"};
            LOGGER.log(Level.INFO, "Unregistering notifier process");
            ExecuteCommand configExecuteCommand = new ExecuteCommand(configCommandArray);
            configExecuteCommand.setOutputType(ExecuteCommand.ERRORS | ExecuteCommand.NORMAL);
            configExecuteCommand.setCollectOutput(true);
            configExecuteCommand.execute();
        } catch (Exception e) {
            LOGGER.log(Level.INFO, "Exception while unregistering notifier: " + e.getMessage());
            // Its okay to ignore this for now.

        }
    }

    /* Undo GlassFish configuration and post-installation setups.*/
    public void unconfigureGlassfish() {
        // Try to stop domain.
        stopDomain();
        try {
            // Cleanup list includes both windows and non-windows files.
            // FileUtils does check for the file before deleting.
            String dirList[] = {
                installDir + File.separator + "glassfish" + File.separator + "domains",
                installDir + File.separator + "glassfish" + File.separator + "modules",
                installDir + File.separator + "glassfish" + File.separator + "nodes",
                installDir + File.separator + "glassfish" + File.separator + "lib"
            };
            for (int i = 0; i < dirList.length; i++) {
                org.glassfish.installer.util.FileUtils.deleteDirectory(new File(dirList[i]));
            }

        } catch (Exception e) {
            // Do nothing for now.
            //LOGGER.log(Level.INFO, "Exception while removing created files: " + e.getMessage());
        }
    }


    /* Try to stop domain, so that uninstall can cleanup files effectively.
    Currently only tries to stop the default domain.
     */
    private void stopDomain() {
        ExecuteCommand asadminExecuteCommand = null;
        try {

            String[] asadminCommandArray = {getAsadminScriptPath(), "stop-domain", "domain1"};
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
    Refer to Issue tracker issue #6173.
     */
    private String getDomainProperties(String adminPort, String httpPort) {

        String domainProperties = "";

        /* Check admin and http port given by user against
        the list of default ports used by asadmin. */
        for (int i = 0; i < glassfishPortArray.length; i++) {
            if (glassfishPortArray[i][1].equals(adminPort)
                    || glassfishPortArray[i][1].equals(httpPort)) {
                /* Convert string to a number, then add 1
                then convert it back to a string. Update the
                portArray with new port #. */
                Integer newPortNumber = Integer.parseInt(glassfishPortArray[i][1]) + 1;
                glassfishPortArray[i][1] = Integer.toString(newPortNumber);
            }

            // Store the modified array elements into the commandline
            domainProperties =
                    domainProperties + glassfishPortArray[i][0] + "=" + glassfishPortArray[i][1];

            /* Don't add a ":" to the last element :-), though asadmin ignores it,
            Safe not to put junk in commandline.
             */
            if (i < 5) {
                domainProperties = domainProperties + ":";
            }
        }
        return domainProperties;
    }


    /* Creates shortcuts for windows. The ones created from OI will be removed due to
    mangled names. These shortcuts are in addition to the ones created by default.
    Since the descriptor for defining the short cut entry is not OS specific, we still
    need to carry on the xml entries to create items on Gnome.
     */
    private void createUpdatetoolShortCuts() {
        String folderName =
                (String) TemplateProcessor.getInstance().getFromDataModel("PRODUCT_NAME");
        LOGGER.log(Level.INFO, "Creating shortcuts under Folder :<" + folderName + ">");
        WindowsShortcutManager wsShortMgr = new WindowsShortcutManager();
        wsShortMgr.createFolder(folderName);
        String modifiedInstallDir = installDir.replace("\\", "\\\\");
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
    private void createServerShortCuts() throws EnhancedException {
        String folderName =
                (String) TemplateProcessor.getInstance().getFromDataModel("PRODUCT_NAME");
        LOGGER.log(Level.INFO, "Creating shortcuts under Folder :<" + folderName + ">");

        WindowsShortcutManager wsShortMgr = new WindowsShortcutManager();
        wsShortMgr.createFolder(folderName);
        String modifiedInstallDir = installDir.replace("\\", "\\\\");
        String modifiedAsAdminPath = getAsadminScriptPath().replace("\\", "\\\\");


        String configMode = (String) ConfigHelper.getValue("NodeServerOptions.configoptions.CONFIGURATION_MODE");
        if (configMode.equalsIgnoreCase("CREATE_DOMAIN")) {
            // Create short cut for starting server.
            wsShortMgr.createShortCut(
                    folderName,
                    "Start Application Server",
                    modifiedInstallDir + "\\\\" + modifiedAsAdminPath,
                    "Start server",
                    "start-domain domain1",
                    modifiedInstallDir + "\\\\glassfish\\\\icons\\\\startAppserv.ico",
                    modifiedInstallDir + "\\\\glassfish",
                    "2");


            // Create short cut for Stop application server.
            wsShortMgr.createShortCut(
                    folderName,
                    "Stop Application Server",
                    modifiedInstallDir + "\\\\" + modifiedAsAdminPath,
                    "Stop server",
                    "stop-domain domain1",
                    modifiedInstallDir + "\\\\glassfish\\\\icons\\\\stopAppserv.ico",
                    modifiedInstallDir + "\\\\glassfish",
                    "2");

            // Create short cut for Admin Console.
            wsShortMgr.createShortCut(
                    folderName,
                    "Administration Console",
                    "http://localhost:" + configData.get("ADMIN_PORT"));
        }

        // Create short cut for uninstall.exe.
        wsShortMgr.createShortCut(
                folderName,
                "Uninstall",
                modifiedInstallDir + "\\\\uninstall.exe",
                "Uninstall",
                "-j \"" + jdkHome.replace("\\", "\\\\") + "\"",
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
        String aboutFiles[] = {"about_sdk.html", "about_sdk_web.html", "about.html"};
        // The default
        String aboutFile = "about.html";
        // Traverse through the list to find out which file exist first
        for (int i = 0; i < aboutFiles.length; i++) {
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
                modifiedInstallDir + aboutFilesLocation.replace("\\", "\\\\") + aboutFile.replace("\\", "\\\\"));
    }

    /* Creates the service for the installer created default domain(domain1).
     * Wrapper for "asadmin create-service" command.
     * Currently support is available only for Windows and Solaris platforms.
     */
    private void createDomainService() {
        ExecuteCommand asadminExecuteCommand = null;
        String serviceName = configData.get("SERVICE_NAME");
        String serviceProperties = configData.get("SERVICE_PROPS");

        try {

            if (OSUtils.isWindows()) {
                asadminExecuteCommand = new ExecuteCommand(
                        new String[]{
                            getAsadminScriptPath(),
                            "create-service",
                            "--name", serviceName,
                            "domain1"});
            } else {
                // Check to see if the service Properties are customized.
                // if not just create the service without --serviceProperties
                if (serviceProperties != null && serviceProperties.trim().length() > 0) {
                    asadminExecuteCommand = new ExecuteCommand(
                            new String[]{
                                getAsadminScriptPath(),
                                "create-service",
                                "--name", serviceName,
                                "--serviceproperties", serviceProperties,
                                "domain1"});
                } else {
                    asadminExecuteCommand = new ExecuteCommand(
                            new String[]{
                                getAsadminScriptPath(),
                                "create-service",
                                "--name", serviceName,
                                "domain1"});
                }
            }

            LOGGER.log(Level.INFO, "Creating GlassFish domain Service");
            LOGGER.log(Level.INFO, "Service name:" + serviceName);
            asadminExecuteCommand.setOutputType(ExecuteCommand.ERRORS | ExecuteCommand.NORMAL);
            asadminExecuteCommand.setCollectOutput(true);
            asadminExecuteCommand.execute();

            LOGGER.log(Level.INFO, "asadmin create-service output: " + asadminExecuteCommand.getAllOutput());

            //Ignore this warning till delete-service is in-place
            /*
            if (asadminExecuteCommand.getAllOutput().indexOf("failed")!=-1)
            configSuccessful = false;
             */
        } catch (Exception e) {
            LOGGER.log(Level.INFO, "Possible errors/warnings, asadmin output: " + asadminExecuteCommand.getAllOutput());
            LOGGER.log(Level.INFO, "Related Exception message: " + e.getMessage());
            //Ignore this error, till admin infra. provides a delete-service command.
            //configSuccessful = false;
        }

    }

    /* Creates the user specified instance.
     * Wrapper for "asadmin create-local-instance" command.
     */
    private void createInstance() {
        ExecuteCommand asadminExecuteCommand = assembleCreateInstanceCommand();
        LOGGER.log(Level.INFO, "Creating GlassFish Instance");
        try {
            LOGGER.log(Level.INFO, "Instance name:" + ConfigHelper.getStringValue("Node.NodeConfiguration.INSTANCE_NAME"));
        } catch (EnhancedException ex) {
            Logger.getLogger(InstallationConfigurator.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            asadminExecuteCommand.setOutputType(ExecuteCommand.ERRORS | ExecuteCommand.NORMAL);
            asadminExecuteCommand.setCollectOutput(true);
            asadminExecuteCommand.execute();
            LOGGER.log(Level.INFO, "asadmin create-local-instance output: " + asadminExecuteCommand.getAllOutput());
            // Look for the string failed till asadmin bugs related to stderr are resolved.
            // Ugly/Buggy, but works for now.
            if (asadminExecuteCommand.getAllOutput().indexOf("failed") != -1) {
                configSuccessful = false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.INFO, "Possible errors/warnings, asadmin output: " + asadminExecuteCommand.getAllOutput());
            LOGGER.log(Level.INFO, "Related Exception message: " + e.getMessage());
            configSuccessful = false;
        }
    }

   
    /* Generates command line and arguments required for creating a local instance */
    private ExecuteCommand assembleCreateInstanceCommand() {
        try {
            ExecuteCommand asadminExecuteCommand = null;
            // Command line to create a clustered instance.
            if (ConfigHelper.getBooleanValue("Node.NodeConfiguration.PART_OF_CLUSTER")) {
                asadminExecuteCommand = new ExecuteCommand(new String[]{getAsadminScriptPath(), "--host", ConfigHelper.getStringValue("Node.NodeConfiguration.SERVER_HOST_NAME"), "--port", ConfigHelper.getStringValue("Node.NodeConfiguration.SERVER_ADMIN_PORT"), "create-local-instance", "--cluster", ConfigHelper.getStringValue("Node.NodeConfiguration.CLUSTER_NAME"), ConfigHelper.getStringValue("Node.NodeConfiguration.INSTANCE_NAME")});
            } else {
                asadminExecuteCommand = new ExecuteCommand(new String[]{getAsadminScriptPath(), "--host", ConfigHelper.getStringValue("Node.NodeConfiguration.SERVER_HOST_NAME"), "--port", ConfigHelper.getStringValue("Node.NodeConfiguration.SERVER_ADMIN_PORT"), "create-local-instance", ConfigHelper.getStringValue("Node.NodeConfiguration.INSTANCE_NAME")});
            }
            return asadminExecuteCommand;
        } catch (EnhancedException ex) {
            Logger.getLogger(InstallationConfigurator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    //get JDK directory from java.home property and use it to define asadmin
    //execution environment PATH    
    private void updateConfigFile() {
        try {
            jdkHome = ConfigHelper.getStringValue("JDKSelection.directory.SELECTED_JDK");
        } catch (Exception e) {
            LOGGER.log(Level.INFO, "JDKHome Couldnt be found ");
            jdkHome = new File(System.getProperty("java.home")).getParent();
            if (OSUtils.isMac() || OSUtils.isAix()) {
                jdkHome = System.getProperty("java.home");
            }
        }
        LOGGER.log(Level.INFO, "jdkHome: " + jdkHome);
        //write jdkHome value to asenv.bat on Windows, asenv.conf on non-Windows platform...
        try {
            FileIOUtils configFile = new FileIOUtils();
            configFile.openFile(getGlassfishConfigFilePath());
            /* Add AS_JAVA to end of buffer and file. */
            if (OSUtils.isWindows()) {
                configFile.appendLine("set AS_JAVA=" + jdkHome);
            } else {
                configFile.appendLine("AS_JAVA=" + jdkHome);
            }
            configFile.saveFile();
            configFile.closeFile();
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, "Error while updating asenv configuration file: " + ex.getMessage());
        }
    }

    /* Unpack compressed jars. */
    private void unpackJars() {
        //unpack jar files in all directories under glassfish/modules
        String dirList[] = {
            installDir + File.separator + "glassfish" + File.separator + "modules",
            installDir + File.separator + "glassfish" + File.separator + "modules" + File.separator + "endorsed",
            installDir + File.separator + "glassfish" + File.separator + "modules" + File.separator + "autostart"};

        // if the jar extraction fails, then there is something really wrong.
        for (int i = 0; i < dirList.length; i++) {
            Unpack modulesUnpack = new Unpack(new File(dirList[i]), new File(dirList[i]));
            if (!modulesUnpack.unpackJars()) {
                configSuccessful = false;
            }
        }
    }

    /* Create wrappers for asadmin start/stop on Windows.
     * This also should include copyright, that is currently taken from OSUtils.
     */
    private void setupWindowsDomainScripts() {
        try {
            FileIOUtils startFile = new FileIOUtils();
            startFile.openFile(installDir + "\\glassfish\\lib\\asadmin-start-domain.bat");
            startFile.appendLine(OSUtils.windowsCopyRightNoticeText);
            startFile.appendLine("setlocal");
            startFile.appendLine("call \"" + installDir + "\\glassfish\\bin\\asadmin\" start-domain domain1\n");
            startFile.appendLine("pause");
            startFile.appendLine("endlocal");
            startFile.saveFile();
            startFile.closeFile();

            FileIOUtils stopFile = new FileIOUtils();
            stopFile.openFile(installDir + "\\glassfish\\lib\\asadmin-stop-domain.bat");
            stopFile.appendLine(OSUtils.windowsCopyRightNoticeText);
            stopFile.appendLine("setlocal");
            stopFile.appendLine("call \"" + installDir + "\\glassfish\\bin\\asadmin\" stop-domain domain1\n");
            stopFile.appendLine("pause");
            stopFile.appendLine("endlocal");
            stopFile.saveFile();
            stopFile.closeFile();
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, "Error while creating wrapper file: " + ex.getMessage());
            // OK to ignore this for now.
        }

    }
    /* Create wrappers for asadmin start/stop on Solaris.
     * This also should include copyright, that is currently taken from OSUtils.
     */

    private void setupUnixDomainScripts() {
        try {
            FileIOUtils startFile = new FileIOUtils();
            startFile.openFile(installDir + "/glassfish/lib/asadmin-start-domain");
            startFile.appendLine(OSUtils.unixCopyRightNoticeText);
            startFile.appendLine("\"" + installDir + "/glassfish/bin/asadmin\" start-domain domain1");
            startFile.saveFile();
            startFile.closeFile();

            FileIOUtils stopFile = new FileIOUtils();
            stopFile.openFile(installDir + "/glassfish/lib/asadmin-stop-domain");
            stopFile.appendLine(OSUtils.unixCopyRightNoticeText);
            stopFile.appendLine("\"" + installDir + "/glassfish/bin/asadmin\" stop-domain domain1");
            stopFile.saveFile();
            stopFile.closeFile();

            org.glassfish.installer.util.FileUtils.setExecutable(new File(installDir + "/glassfish/lib/asadmin-start-domain").getAbsolutePath());
            org.glassfish.installer.util.FileUtils.setExecutable(new File(installDir + "/glassfish/lib/asadmin-stop-domain").getAbsolutePath());
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, "Error while creating wrapper file: " + ex.getMessage());
            // OK to ignore this for now.

        }
    }

    /* Create password file required for asadmin commands. */
    private String createPasswordFile() {
        File pwdFile = null;
        String pwd = configData.get("ADMIN_PASSWORD");
        try {

            pwdFile = File.createTempFile("asadminTmp", null);
            FileIOUtils fUtil = new FileIOUtils();
            fUtil.openFile(pwdFile.getAbsolutePath());
            pwdFile.deleteOnExit();
            if (pwd != null && pwd.trim().length() > 0) {
                fUtil.appendLine("AS_ADMIN_PASSWORD=" + pwd);
            } else {
                fUtil.appendLine("AS_ADMIN_PASSWORD=");
            }
            fUtil.appendLine("AS_ADMIN_MASTERPASSWORD=changeit");
            fUtil.saveFile();
            fUtil.closeFile();
            if (!OSUtils.isWindows()) {
                org.glassfish.installer.util.FileUtils.setExecutable(pwdFile.getAbsolutePath());
            }
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, "Error while creating password file: " + ex.getMessage());
            // ensure that we delete the file should any exception occur
            org.glassfish.installer.util.FileUtils.deleteFile(pwdFile.getAbsolutePath());
            configSuccessful = false;
        }
        return pwdFile.getAbsolutePath();
    }

    /*create updatetool wrapper script used by shortcut items */
    private void setupUpdateToolScripts() {

        org.glassfish.installer.util.FileUtils.createDirectory(installDir
                + File.separator
                + "updatetool"
                + File.separator
                + "lib");
        try {
            if (OSUtils.isWindows()) {
                FileIOUtils updateToolScript = new FileIOUtils();
                updateToolScript.openFile(installDir + "\\updatetool\\lib\\updatetool-start.bat");
                updateToolScript.appendLine(OSUtils.windowsCopyRightNoticeText);
                updateToolScript.appendLine("setlocal");
                updateToolScript.appendLine("cd \"" + installDir + "\\updatetool\\bin\"");
                updateToolScript.appendLine("call updatetool.exe");
                updateToolScript.appendLine("endlocal");
                updateToolScript.saveFile();
                updateToolScript.closeFile();
            } else {
                FileIOUtils updateToolScript = new FileIOUtils();
                updateToolScript.openFile(installDir + "/updatetool/lib/updatetool-start");
                updateToolScript.appendLine(OSUtils.unixCopyRightNoticeText);
                updateToolScript.appendLine("cd \"" + installDir + "/updatetool/bin\"");
                updateToolScript.appendLine("./updatetool");
                updateToolScript.saveFile();
                updateToolScript.closeFile();
                org.glassfish.installer.util.FileUtils.setExecutable(installDir + "/updatetool/lib/updatetool-start");
            }

        } catch (Exception ex) {
            LOGGER.log(Level.INFO, "Error while creating wrapper file: " + ex.getMessage());
            // OK to ignore this for now.
        }
    }
}
