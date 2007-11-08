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

package com.sun.enterprise.cli.commands;

import com.sun.enterprise.cli.framework.CommandValidationException;
import com.sun.enterprise.cli.framework.CommandException;
import com.sun.enterprise.cli.framework.CLILogger;

import com.sun.enterprise.util.ProcessExecutor;
import com.sun.enterprise.util.RelativePathResolver;
import com.sun.enterprise.security.store.PasswordAdapter;
import com.sun.enterprise.admin.servermgmt.pe.PEFileLayout;
import com.sun.enterprise.admin.servermgmt.RepositoryManager;
import com.sun.enterprise.admin.servermgmt.DomainsManager;
import com.sun.enterprise.admin.servermgmt.DomainConfig;
import com.sun.enterprise.admin.servermgmt.DomainException;
import com.sun.enterprise.admin.servermgmt.InstancesManager;
import com.sun.enterprise.admin.common.Status;
import com.sun.enterprise.cli.commands.InplaceDomainUpgradeHandler;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.HttpListener;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.io.FileUtils;
import java.io.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import com.sun.enterprise.util.OS;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigContextFactory;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.lang.reflect.Method;


/**
 *  This is a sample Deploy command
 *  @version  $Revision: 1.27 $
 */
public class StartDomainCommand extends BaseLifeCycleCommand
{

    private static final String SERVER_LOG_FILE_NAME = "server.log";
    private static final String LOGS_DIR             = "logs";
    private static final String VERBOSE              = "verbose";
    private static final String APPLICATION_SERVER_8_0 = "Application Server 8.0";
    private static final String DTD_FILE_8_0 = "sun-domain_1_0";
    private static final String DTD_FILE_8_1 = "sun-domain_1_1";
    private static final long   UPGRADE_TIMEOUT = 1200000;  //20min
    private static final long   UPDATECENTER_TIMEOUT = 500;  //0.5sec
    private String domainName;
    private DomainConfig config;
    private ConfigContext cc;
    private DomainsManager mgr;
    private String adminPwdAlias;

	// backup message: tells user about the backup file.  This is not wanted in many
    // cases -- e.g. domain is already running, bad password.
	// note: as of Sept 26,2005 the backup of domain.xml has been deprecated.
	// Thus, this variable does nothing.  It can all be turned back on by setting
	// the enableBackups boolean
    private boolean doBackupMessage = true;
	private final static boolean enableBackups = false;

    /**
     *  An abstract method that validates the options
     *  on the specification in the xml properties file
     *  This method verifies for the correctness of number of
     *  operands and if all the required options are supplied by the client.
     *  @return boolean returns true if success else returns false
     */
    public boolean validateOptions() throws CommandValidationException
    {
        return super.validateOptions();
    }
    public void startDomain(String DomainName)
        throws CommandException, CommandValidationException
    {
        domainName = DomainName;
        long start  = System.currentTimeMillis();

        try
        {
            doBackupMessage = false;
            init();
            if (!checkIfRunning())
            {
                checkIfStopping();

                try
                {
                    validateAdminPassword();
                }
                catch(Exception e)
                {
                    throw new CommandValidationException(getLocalizedString(
                            "BadUsernameOrPassword"));
                }
                try
                {
                    validateMasterPassword();
                }
                catch(Exception e)
                {
                    throw new CommandValidationException(getLocalizedString(
                            "BadMasterPassword"));
                }
                saveExtraPasswordOptions();
                doBackupMessage = true;
                mgr.startDomain(config);

                if(enableBackups)
                {
                    saveCopyOfConfig(config);
                }

                if (startupNeedsAdminCreds()) {
                    CLILogger.getInstance().printDetailMessage(getLocalizedString("DomainStarted",
                                                                                  new Object[] {domainName}));
                } else {
                    CLILogger.getInstance().printDetailMessage(getLocalizedString("DomainReady",
                                                                                  new Object[] {domainName}));
                }
                final boolean terse = getBooleanOption(TERSE);
                new DomainReporter(config, terse, cc).report();
                long msec = System.currentTimeMillis() - start;

                    /* convert milliseconds duration to xxx.y seconds, where y is rounded off properly.
                     * E.g.
                     27999 -> 28.0
                     27449 --> 27.4
                     27450 -> 27.5
                    */
                double sec = ((double)Math.round( ((double)msec) / 100.0)) / 10.0;
                CLILogger.getInstance().printDebugMessage(getLocalizedString("DomainStartTime",
                                                                             new Object[] { sec }));
            }
        }
        catch(Exception e)
        {
            CLILogger.getInstance().printDetailMessage(e.getLocalizedMessage());

			if(enableBackups)
			{
				if(doBackupMessage)
					checkOnBackup(config);
			}

            throw new CommandException(getLocalizedString("CannotStartDomain",
                       new Object[] {domainName}), e);
        }     
    }


    /**
     *  An abstract method that Executes the command
     *  @throws CommandException
     */
    public void runCommand() throws CommandException, CommandValidationException
    {
        CLILogger.getInstance().printDebugMessage("Launching UpdateCenter Thread");
        final Thread t = new Thread(new UpdateCenter(), "UpdateCenterThread");
        t.start();

        validateOptions();

        String domainName = null;
        try {
            domainName = getDomainName();
        } catch(Exception e)
        {
            CLILogger.getInstance().printDetailMessage(e.getLocalizedMessage());
            domainName = domainName==null?getLocalizedString("Undefined"):domainName;
            throw new CommandException(getLocalizedString("CannotStartDomain",
                       new Object[] {domainName}), e);
        }
        startDomain(domainName);
        if (t.isAlive()) {
            try {
                    //hopefully it'll never reach here
                    //if the updatecenter thread is still alive
                    //then give 0.5 sec more to end the thread before timeout

                    //update center timeout is configurable
                final String ucTo = System.getProperty("UPDATECENTER_TIMEOUT");
                long waitForUc = UPDATECENTER_TIMEOUT;
                if (ucTo != null) {
                    try {
                        waitForUc  = Long.parseLong(ucTo);
                    }
                    catch (NumberFormatException nfe) {
                        waitForUc = UPDATECENTER_TIMEOUT;
                    }
                    if (waitForUc < 1) waitForUc = UPDATECENTER_TIMEOUT;
                }
                t.join(waitForUc);
            }
            catch (InterruptedException ie) {
                    //ignore any exception coming from updatecenter
                    //since we want to want to interfere with domain startup
            }
        }
    }


        /**
         *  This api checks and executes the upgrade commands.
         *  This api invokes checkIfVersion80().  If version is 8.0, then it will
         *  try to invoke asupgrade command.
         *  @domainName -- name of domain, this parameter is required to
         *  figure out the version of domain.
         *  @throws CommandException if error invoking asupgrade
         **/
    private void checkAndExecuteUpgrade(String domainName) throws CommandException
    {
        final String domainDir = getDomainsRoot();
        CLILogger.getInstance().printDebugMessage("domainDir = " + domainDir);
        final String installDir = System.getProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY);
        CLILogger.getInstance().printDebugMessage("installDir = " + installDir);
        CLILogger.getInstance().printDebugMessage("domainName = " + domainName);
        final InplaceDomainUpgradeHandler ipuh =
                new InplaceDomainUpgradeHandler(config);
        if (ipuh.needsUpgrade()) {
              String domainDirOption = getOption(DOMAINDIR);
              String domainsDirProperty = System.getProperty(SystemPropertyConstants.DOMAINS_ROOT_PROPERTY);

              if (domainDirOption != null && domainDirOption.indexOf(domainsDirProperty) == -1){
                    throw new CommandException(getLocalizedString("UpgradeUnsupported", new Object[]{domainsDirProperty}));
                }
            try {
                final String sPasswordFile = createPasswordFileText();
                final String[] upgradeCmd;
                /*Need to prefix the upgrade command with CMD /c in case of Windows*/
                if(OS.isWindows())
                        upgradeCmd = new String[]    {"CMD",
                                                        "/c",
                                                      installDir + File.separator +
                                                      "bin" + File.separator +
                                                      "asupgrade", "-c", "-s",
                                                      domainDir+File.separator+domainName,
                                                      "-t", installDir +File.separator+ "domains",
                                                      "-noprompt" };
                else
                            upgradeCmd = new String[] {installDir + File.separator +
                                                      "bin" + File.separator +
                                                      "asupgrade", "-c", "-s",
                                                      domainDir+File.separator+domainName,
                                                      "-t", installDir +File.separator+ "domains",
                                                      "-noprompt" };

                ProcessExecutor pe = new ProcessExecutor(upgradeCmd, UPGRADE_TIMEOUT);
                /*
                 * ProcessExecutor's constructor replaces all the '/'s with '\' in case the OS is Windows.
                 * We don't want that for CMD /c. Hence need to replace it again
                 */
                if(OS.isWindows())
                        upgradeCmd[1] ="/c";

                System.out.println(getLocalizedString("StartingUpgrade"));
                pe.execute();  // timeout in 600sec or 10min
                Process process = pe.getSubProcess();
                int exitValue = process.waitFor();
                if (exitValue != 0) {
                    System.out.println(getLocalizedString("UpgradeFailed"));
                    throw new CommandException(getLocalizedString("UpgradeFailed"));
                }else {
                    System.out.println(getLocalizedString("UpgradeSuccessful"));
                    ipuh.touchUpgradedToFile();
                }
            }
            catch (Exception e) {
                    //e.printStackTrace();
                throw new CommandException(getLocalizedString("UpgradeFailed"), e);
            }
        } else {
            try {
                ipuh.touchUpgradedToFile();
            } catch(final IOException ioe) {
                throw new CommandException(ioe);
            }
        }
    }

    /**
     *  This api check if the domain version is 8.0 or 8.1.
     *  @domainName - domain name to figure out the version
     *  @domainDir - domain directory
     *  @installDir - install directory
     *  @throws CommandExcpetion if error finding version
     **/
    private boolean checkIfVersion80(String domainName, String domainDir,
                                     String installDir) throws CommandException
    {
        final PEFileLayout layout = new PEFileLayout(config);
        final File domainFile = layout.getDomainConfigFile();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setNamespaceAware(true);

        try {

            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver((org.xml.sax.helpers.DefaultHandler)Class.forName
                                      ("com.sun.enterprise.config.serverbeans.ServerValidationHandler").newInstance());
            Document adminServerDoc = builder.parse(domainFile);
            String publicID = adminServerDoc.getDoctype().getPublicId();
            String systemID = adminServerDoc.getDoctype().getSystemId();
            CLILogger.getInstance().printDebugMessage("publicID = " + publicID);
            CLILogger.getInstance().printDebugMessage("systemID = " + systemID);
                //if domain.xml systemID=sun-domain_1_0 then the version is 8.0
                //if domain.xml systemID=sun-domain_1_1 then the version is 8.1
            if (publicID.indexOf(APPLICATION_SERVER_8_0) != -1 &&
                (systemID.indexOf(DTD_FILE_8_0) != -1 ||
                 systemID.indexOf(DTD_FILE_8_1) != -1 )) {
                return true;
            } else {
                return false;
            }
        }
        catch (Exception e)
        {
                //do nothing for now
                //e.printStackTrace();
        }
        return false;
    }

        /**
         * create a temporary passwordfile.txt that contains
         * admin password and master password that is passed
         * to asupgrade command.
         */
    private String createPasswordFileText() throws Exception
    {
        //create a temp passwordfile
        final File fPasswordFile = File.createTempFile("passwordfile", null);
        fPasswordFile.deleteOnExit();
        PrintWriter out = new PrintWriter(new BufferedWriter(
                                          new FileWriter(fPasswordFile.toString())));
        out.println("AS_ADMIN_PASSWORD=" + "adminadmin");
        // hardcoding password because that's the best I can do here (Kedar)
        out.println("AS_ADMIN_MASTERPASSWORD=" + (String)config.get(DomainConfig.K_MASTER_PASSWORD));
        out.close();
        return fPasswordFile.toString();
    }



    /**
     *  Returns the log file location for a domain
     *  @throws CommandException
     */
    private String getDomainLogFile(String domainName) throws CommandException
    {
        String logFile = getDomainsRoot() + File.separator + domainName +
                         File.separator + LOGS_DIR + File.separator + SERVER_LOG_FILE_NAME;
        return logFile;
    }

    private boolean isNotRunning(DomainsManager mgr, DomainConfig cfg) throws Exception
    {
        // note that checking for kInstanceRunningCode is not desired here

        InstancesManager im = mgr.getInstancesManager(cfg);
        int state = im.getInstanceStatus();

        return state == Status.kInstanceNotRunningCode;
    }

    private boolean isStopping(DomainsManager mgr, DomainConfig cfg) throws Exception
    {
        InstancesManager im = mgr.getInstancesManager(cfg);
        int state = im.getInstanceStatus();

        return state == Status.kInstanceStoppingCode;
    }

    private void checkOnBackup(DomainConfig config)
    {
        // the try/catch is extra protection because we're being
        // called from inside a catch block.

        if(config == null)
            return; // nothing we can do here...

        try
        {
            File domainsRoot    = new File(config.getRepositoryRoot());
            File domainRoot     = new File(domainsRoot, config.getDomainName());
            File configDir      = new File(domainRoot, "config");
            File domainxml      = new File(configDir, "domain.xml");
            File backup         = new File(configDir, "domain_bu.xml");

            if(backup.exists())
                pr("NoStartAdvice", backup.getAbsolutePath(), domainxml.getAbsolutePath());
        }
        catch(Exception e)
        {
        }
    }

    private void init() throws CommandException, ConfigException, DomainException
    {
        mgr = getFeatureFactory().getDomainsManager();
        config = getDomainConfig(domainName);
        config.setRefreshConfingContext(false);
        this.validateDomain();
        CLILogger.getInstance().printDetailMessage(getLocalizedString("StartingDomain",
                                         new Object[] {domainName}));
        if (!getBooleanOption(VERBOSE))
        {
            CLILogger.getInstance().printDetailMessage(getLocalizedString("LogRedirectedTo",
                new Object[] {getDomainLogFile(domainName)}));
        }
        this.checkAndExecuteUpgrade(domainName);
        this.configureAddons();
        /* Implementation note:
         It is important to note that the following call is made after all
         the possible changes to domain.xml (from invocation of asadmin start-domain to 
         invocation of launcher) are done. The following call to create the
         config context "caches" the config context and hence the config context
         is not recreated. It is important to preserve that call. 
        */
        final String xmlPath = new PEFileLayout(config).
            getDomainConfigFile().getAbsolutePath();
        this.cc = ConfigFactory.
            createConfigContext(xmlPath);

        
            //get port from ConfigFactory
        final HttpListener admin = ServerHelper.getHttpListener(cc,
                                   SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME,
                                   ServerHelper.ADMIN_HTTP_LISTNER_ID);
        if (admin != null) {
            final String port = admin.getPort();
                //set port and host options so that password can be retrieved from .asadminpass
            setOption("port", port);
            setOption("host", "localhost");            
        }
    }
    

    private void configureAddons() {
        try {
            AddonControl ac = new AddonControl();
            String domainInstanceRoot = getDomainsRoot() + File.separator + getDomainName();
            ac.configureDAS(new File(domainInstanceRoot));
        }catch(Throwable t) {
            CLILogger.getInstance().printDetailMessage(t.getLocalizedMessage());
        }

    }

        /**
         * If already running, then return true.
         */
    private boolean checkIfRunning() throws Exception
    {
        // note that isNotRunning declares 'throws Exception' !!!

        if(!isNotRunning(mgr, config))
        {
            // bnevins Oct 2004
            // This has officially been defined as an error.  So we have to throw an Exception.

            // bnevins Sept 2005
            // we don't want to print a message saying that they should try
            // using the backup config for this situation.
            CLILogger.getInstance().printDetailMessage(getLocalizedString("CannotStartDomainAlreadyRunning",
                   new Object[] {domainName}));
            return true;
        }
        return false;
    }

    private void checkIfStopping() throws Exception
    {
        if(isStopping(mgr, config))
        {
            throw new CommandException(getLocalizedString("CannotStartStoppingDomain",
                   new Object[] {domainName}));
        }
    }

    private void validateDomain() throws DomainException
    {
        // if the next line throws -- print backup availability message
        mgr.validateDomain(config, true);
    }
    private void validateAdminPassword() throws CommandValidationException, DomainException, CommandException
    {
        //In PE, the admin user and password are options that are not needed and as
        //such are ignored.

        if (startupNeedsAdminCreds()) {
            config.put(DomainConfig.K_USER, getUser());
            config.put(DomainConfig.K_PASSWORD, getPassword());
            //Validate the admin user and password that was provided.
            if (getOption(S1ASCommand.PASSWORDFILE) != null )
                adminPwdAlias = RelativePathResolver.getAlias( (String)config.get(DomainConfig.K_PASSWORD));
            if (adminPwdAlias==null) {
                mgr.validateAdminUserAndPassword(config);
            }
        }
    }

    private void validateMasterPassword() throws Exception
    {
        final String masterPassword = getMasterPassword(new RepositoryManager(), config);
        config.put(DomainConfig.K_MASTER_PASSWORD, masterPassword);

        mgr.validateMasterPassword(config);
        if (adminPwdAlias!=null) {
            String domainsRoot =  (String)config.get(DomainConfig.K_DOMAINS_ROOT);
            String keyStoreFile= domainsRoot+ File.separator + domainName +  File.separator + "config" + File.separator +
                      PasswordAdapter.PASSWORD_ALIAS_KEYSTORE;
            PasswordAdapter p =
                new PasswordAdapter(keyStoreFile, masterPassword.toCharArray());
            String clearPwd = p.getPasswordForAlias(adminPwdAlias);
            config.put(DomainConfig.K_PASSWORD, clearPwd);
            mgr.validateAdminUserAndPassword(config);
        }
    }
    private void saveExtraPasswordOptions() throws CommandValidationException, CommandException, DomainException
    {
            String[] extraPasswordOptions = mgr.getExtraPasswordOptions(config);
            if (extraPasswordOptions != null) {
                config.put(DomainConfig.K_EXTRA_PASSWORDS, getExtraPasswords(extraPasswordOptions));
            }
    }

    private void saveCopyOfConfig(DomainConfig config)
    {
        File domainsRoot    = new File(config.getRepositoryRoot());
        File domainRoot     = new File(domainsRoot, config.getDomainName());
        File configDir      = new File(domainRoot, "config");
        File domainxml      = new File(configDir, "domain.xml");
        File backup         = new File(configDir, "domain_bu.xml");

        try
        {
            FileUtils.copy(domainxml.getAbsolutePath(), backup.getAbsolutePath());
            pr("ConfigBackedupOK", backup.getAbsolutePath());
        }
        catch(Exception e)
        {
            pr("ConfigBackedupNot", domainxml.getAbsolutePath(), backup.getAbsolutePath());
        }
    }

    private void pr(String id, Object o)
    {
        String s = getLocalizedString(id, new Object[]{ o } );
        CLILogger.getInstance().printDetailMessage(s);
    }

    private void pr(String id, Object o1, Object o2)
    {
        String s = getLocalizedString(id, new Object[]{ o1, o2 } );
        CLILogger.getInstance().printDetailMessage(s);
    }
    private boolean startupNeedsAdminCreds() {
        try {
            return ( ServerHelper.isClusterAdminSupported(this.cc) );
        } catch (final ConfigException ce) {
            throw new RuntimeException (ce); //Unrecoverable
        }
    }


    /**
     *  inner class that will ping update center
     */
    private class UpdateCenter implements Runnable
    {
        private final String UPDATE_MANAGER_CLASS = "com.sun.enterprise.update.UpdateManager";
        private final String INSTALL_DIR = System.getProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY);
        private final String UPDATECENTER_JAR = INSTALL_DIR+File.separator+"updatecenter"+
                                                File.separator+"lib"+File.separator+"updatecenter.jar";
        
        public void run()
        {
            final ClassLoader cl;
            try {
                CLILogger.getInstance().printDebugMessage("Adding UpdateCenter Jar File");
                final File updateCenterJar = new File(UPDATECENTER_JAR);
                if (updateCenterJar.exists()) {
                    final ClassLoader currentCL = Thread.currentThread().getContextClassLoader();
                    cl = new URLClassLoader(new URL[] {updateCenterJar.toURL()}, currentCL);
                    Thread.currentThread().setContextClassLoader(cl);
                } else {
                        //updatecenter.jar does not exist
                        //so no need to call update center
                    CLILogger.getInstance().printDebugMessage("File does not exist " +
                                                              UPDATECENTER_JAR );
                    return;
                }
            }
            catch (java.net.MalformedURLException me) {
                    //exit thread and do not call updatecenter
                CLILogger.getInstance().printDebugMessage("***** Unable to add URL *****");
                //me.printStackTrace();
                return;
            }


            try {
                    //execute the following reflective:
                    //UpdateManager um = UpdateManager.getInstance();
                    //Scheduler scheduler = um.getScheduler();
                    //scheduler.runUpdateCheck();
                CLILogger.getInstance().printDebugMessage("Calling UpdateCenter Reflectively");
                System.setProperty("com.sun.updatecenter.home", INSTALL_DIR+File.separator+"updatecenter");
                CLILogger.getInstance().printDebugMessage("com.sun.updatecenter.home=" +
                                                          System.getProperty("com.sun.updatecenter.home"));
                final Class updateManagerClass = Class.forName(UPDATE_MANAGER_CLASS, true, cl);
                final Method updateManagerMethod = updateManagerClass.getMethod("getInstance",
                                                                                new Class[]{});
                final Object updateManagerObj = updateManagerMethod.invoke(null, new Object[]{});
                final Class updateManager = updateManagerObj.getClass();
                final Method schedulerMethod = updateManager.getMethod("getScheduler", new Class[]{});
                final Object schedulerObj = schedulerMethod.invoke(updateManagerObj, new Object[]{});
                final Class schedulerClass = schedulerObj.getClass();
                final Method runUpdateMethod = schedulerClass.getMethod("runUpdateCheck",
                                                                        new Class[] {});
                runUpdateMethod.invoke(schedulerObj, new Object[]{});
            }
            catch (Exception e) {
                //e.printStackTrace();
                CLILogger.getInstance().printDebugMessage("***** Unable to execute updatecenter *****");
                //do not want to report any failures
                return;
            }
        }
    }
}


