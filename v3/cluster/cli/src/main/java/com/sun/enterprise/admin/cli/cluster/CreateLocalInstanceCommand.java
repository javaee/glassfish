/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.admin.cli.cluster;

import com.sun.enterprise.admin.cli.CLIConstants;
import com.sun.enterprise.admin.cli.CLILogger;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import static com.sun.enterprise.admin.cli.cluster.PortBaseHelper.*;
import java.util.*;
import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.*;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import com.sun.enterprise.admin.cli.remote.RemoteCommand;
import com.sun.enterprise.admin.util.SecureAdminClientManager;
import com.sun.enterprise.config.serverbeans.ServerRef;
import com.sun.enterprise.admin.servermgmt.KeystoreManager;
import com.sun.enterprise.admin.util.CommandModelData.ParamModelData;
import com.sun.enterprise.security.store.PasswordAdapter;
import com.sun.enterprise.util.OS;


/**
 *  This is a local command that calls the primitive remote _register-instance to add the
 *  entries in domain.xml and then the primitive local command _create-instance-filesystem
 *  to create the empty directory structure and das.properties
 *
 */
@Service(name = "create-local-instance")
@Scoped(PerLookup.class)
public final class CreateLocalInstanceCommand extends CreateLocalInstanceFilesystemCommand {
    private final String CONFIG = "config";
    private final String CLUSTER = "cluster";

    @Param(name = CONFIG, optional = true)
    private String configName;

    @Param(name = CLUSTER, optional = true)
    private String clusterName;

    @Param(name="lbenabled", optional=true, defaultValue = ServerRef.LBENABLED_DEFAULT_VALUE)
    private Boolean lbEnabled;

    @Param(name = "systemproperties", optional = true, separator = ':')
    private String systemProperties;     // XXX - should it be a Properties?

    @Param(name = "portbase", optional = true)
    private String portBase;

    @Param(name = "checkports", optional = true, defaultValue = "true")
    private boolean checkPorts = true;

    @Param(name = "bootstrap", optional = true, defaultValue = "true")
    private boolean bootstrap = true;

    @Param(name = "savemasterpassword", optional = true, defaultValue = "false")
    private boolean saveMasterPassword = false;

    private static final String RENDEZVOUS_PROPERTY_NAME = "rendezvousOccurred";
    private String INSTANCE_DOTTED_NAME;
    private String RENDEZVOUS_DOTTED_NAME;
    private boolean _rendezvousOccurred;
    private String _node;
    private PortBaseHelper pbh;
    protected static final String DEFAULT_MASTER_PASSWORD = KeystoreManager.DEFAULT_MASTER_PASSWORD;
    private ParamModelData masterPasswordOption;
    private static final String MASTER_PASSWORD_ALIAS="master-password";

    /**
     */
    @Override
    protected void validate()
            throws CommandException {
        
        if (configName != null && clusterName != null) {
            throw new CommandException(
                    Strings.get("ConfigClusterConflict"));
        }

        setDasDefaultsOnly = true; //Issue 12847 - Call super.validate to setDasDefaults only
        super.validate();          //so _validate-node uses das host from das.properties. No dirs created.
        if (node != null) {
            validateNode(node, getInstallRootPath(), getInstanceHostName(true));
        }

        pbh = new PortBaseHelper(portBase, checkPorts);
        pbh.verifyPortBase();
        setDasDefaultsOnly = false;
        super.validate();  // instanceName is validated and set in super.validate(), directories created
        INSTANCE_DOTTED_NAME = "servers.server." + instanceName;
        RENDEZVOUS_DOTTED_NAME = INSTANCE_DOTTED_NAME + ".property." + RENDEZVOUS_PROPERTY_NAME;

        /*
         * Before contacting the DAS, intialize client authentication so
         * we either send the admin indicator header or we use client cert
         * authentication, depending on the current configuration.
         */
        SecureAdminClientManager.initClientAuthentication(
                passwords.get(CLIConstants.MASTER_PASSWORD) != null ?
                    passwords.get(CLIConstants.MASTER_PASSWORD).toCharArray() : null,
                programOpts.isInteractive(),
                null /* no server_name option on create-local-instance */,
                nodeDir,
                node);

        if (!rendezvousWithDAS()) {
            instanceDir.delete();
            throw new CommandException(
                    Strings.get("Instance.rendezvousFailed", DASHost, "" + DASPort));
        }

        _rendezvousOccurred = rendezvousOccurred();
        if (_rendezvousOccurred) {
            throw new CommandException(
                    Strings.get("Instance.rendezvousAlready", instanceName, DASHost, "" + DASPort));
        }
    }

    /**
     */
    @Override
    protected int executeCommand()
            throws CommandException, CommandValidationException {
        int exitCode = -1;

        if (node == null) {
            _node = nodeDirChild.getName();
            String nodeHost = getInstanceHostName(true);
            createNodeImplicit(_node, getInstallRootPath(), nodeHost);
        } else {
            _node = node;
        }
        
        if (isRegisteredToDAS()) {
            if (!_rendezvousOccurred) {
                setRendezvousOccurred("true");
                _rendezvousOccurred = true;
            }

        } else {
            try {
                registerToDAS();
                _rendezvousOccurred = true;
            } catch (CommandException ce) {
                instanceDir.delete();
                throw ce;
            }
        }
        if (bootstrap) {
            bootstrapSecureAdminFiles();
        }
        try {
            exitCode = super.executeCommand();
            if (exitCode == SUCCESS) {
                saveMasterPassword();
            }
        } catch (CommandException ce) {
            String msg = "Something went wrong in creating the local filesystem for instance " + instanceName;
            if (ce.getLocalizedMessage() != null) {
                msg = msg + ": " + ce.getLocalizedMessage();
            }
            logger.printError(msg);
            setRendezvousOccurred("false");
            _rendezvousOccurred = false;
            
            throw new CommandException(msg, ce);
        }
        return exitCode;
    }

    private int bootstrapSecureAdminFiles() throws CommandException {
        RemoteCommand rc = new RemoteCommand("_bootstrap-secure-admin", this.programOpts, this.env);
        rc.setFileOutputDirectory(instanceDir);
        logger.printDetailMessage("Download root for bootstrapping: " + instanceDir.getAbsolutePath());
        final int result = rc.execute(new String[] {"_bootstrap-secure-admin"});

        /*
         * The domain.xml just bootstrapped will look up-to-date compared to
         * the domain.xml on the DAS when this instance is started (if nothing
         * else happens in the meantime to change the DAS domain.xml timestamp).
         * That would fool the synchronization logic into thinking the instance
         * is up-to-date, whereas the instance will need to be sync-ed.
         *
         * So, adjust the just downloaded domain.xml's timestamp so it will
         * seem obsolete and trigger a sync when the instance is started.
         */
        final URI domainXMLURI = URI.create("config/domain.xml");
        final File domainXMLFile = new File(instanceDir.toURI().resolve(domainXMLURI));
        domainXMLFile.setLastModified(domainXMLFile.lastModified() - 1000);
        return result;
    }

    /**
     * If --savemasterpassword=true, then saves tries to save the master password.
     * If AS_ADMIN_MASTERPASSWORD from --passwordfile exists that is used.
     * If it does not exist, the user is asked to enter the master password.
     * The password is validated against the keystore if it exists. If successful, master-password
     * is saved to the server instance directory <glassfish-install>/nodes/<host name>/<instance>/master-password.
     * If the password entered does not match the keystore, master-password is not
     * saved and a warning is displayed. The command is still successful.
     * @throws CommandException
     */
    private void saveMasterPassword() throws CommandException {
        if (saveMasterPassword) {
            masterPasswordOption = new ParamModelData(CLIConstants.MASTER_PASSWORD,
                        String.class, false, null);
            masterPasswordOption.description = Strings.get("MasterPassword");
            masterPasswordOption.param._password = true;
            String masterPassword = getPassword(masterPasswordOption, DEFAULT_MASTER_PASSWORD, false);
            if (masterPassword != null) {
                File mp = new File(new File(getServerDirs().getServerDir(), "config"), "keystore.jks");
                if (mp.canRead()) {
                    if (verifyMasterPassword(masterPassword)) {
                        createMasterPasswordFile(masterPassword);
                    } else {
                        logger.printMessage(Strings.get("masterPasswordIncorrect"));
                    }
                } else {
                    createMasterPasswordFile(masterPassword);
                }
                
            }
        }
    }

    /**
     * Create the master password keystore. This routine can also modify the master password
     * if the keystore already exists
     * @param masterPassword
     * @throws CommandException
     */
    protected void createMasterPasswordFile(String masterPassword) throws CommandException {
        final File pwdFile = new File(this.getServerDirs().getServerDir(), MASTER_PASSWORD_ALIAS);
        try {
            PasswordAdapter p = new PasswordAdapter(pwdFile.getAbsolutePath(),
                MASTER_PASSWORD_ALIAS.toCharArray());
            p.setPasswordForAlias(MASTER_PASSWORD_ALIAS, masterPassword.getBytes());
            chmod("600", pwdFile);
        } catch (Exception ex) {
            throw new CommandException(Strings.get("masterPasswordFileNotCreated", pwdFile),
                ex);
        }
    }

    protected void chmod(String args, File file) throws IOException {
        if (OS.isUNIX()) {
            if (!file.exists()) throw new IOException(Strings.get("fileNotFound", file.getAbsolutePath()));

            // " +" regular expression for 1 or more spaces
            final String[] argsString = args.split(" +");
            List<String> cmdList = new ArrayList<String>();
            cmdList.add("/bin/chmod");
            for (String arg : argsString)
                cmdList.add(arg);
            cmdList.add(file.getAbsolutePath());
            new ProcessBuilder(cmdList).start();
        }
    }

    private boolean rendezvousWithDAS() {
        try {
            logger.printMessage(Strings.get("Instance.rendezvousAttempt", DASHost, "" + DASPort));
            boolean success = false;
            RemoteCommand rc = new RemoteCommand("uptime", this.programOpts, this.env);
            int exitCode = rc.execute("uptime");
            if (exitCode == 0) {
                logger.printMessage(Strings.get("Instance.rendezvousSuccess", DASHost, "" + DASPort));
                success = true;
            }
            return success;
        } catch (CommandException ex) {
            return false;
        }
    }

    private int registerToDAS() throws CommandException {
        if (portBase != null) {
            setPorts();
        }
        ArrayList<String> argsList = new ArrayList<String>();
        argsList.add(0, "_register-instance");
        if (clusterName != null) {
            argsList.add("--cluster");
            argsList.add(clusterName);
        }
        if (lbEnabled != null) {
            argsList.add("--lbenabled");
            argsList.add(lbEnabled.toString());
        }
        if (configName != null) {
            argsList.add("--config");
            argsList.add(configName);
        }
        if (_node != null) {
            argsList.add("--node");
            argsList.add(_node);
        }
        if (systemProperties != null) {
            argsList.add("--systemproperties");
            argsList.add(systemProperties);
        }
        argsList.add("--properties");
        argsList.add(RENDEZVOUS_PROPERTY_NAME+"=true");
        argsList.add(this.instanceName);

        String[] argsArray = new String[argsList.size()];
        argsArray = argsList.toArray(argsArray);

        RemoteCommand rc = new RemoteCommand("_register-instance", this.programOpts, this.env);
        return rc.execute(argsArray);
    }

    private boolean isRegisteredToDAS() {
        boolean isRegistered = false;
        try {
            RemoteCommand rc = new RemoteCommand("get", this.programOpts, this.env);
            int exitCode = rc.execute("get", INSTANCE_DOTTED_NAME);
            if (exitCode == SUCCESS) {
                isRegistered = true;
            }
        } catch (CommandException ex) {
            logger.printDebugMessage("asadmin get " + INSTANCE_DOTTED_NAME + " failed.");
            logger.printDebugMessage(instanceName +" may not be registered yet to DAS.");
            //logger.printExceptionStackTrace(ex);
        }
        return isRegistered;
    }

    private boolean rendezvousOccurred() {
        boolean rendezvousOccurred = false;
        RemoteCommand rc = null;
        try {
            rc = new RemoteCommand("get", this.programOpts, this.env);
            Map<String, String> map = rc.executeAndReturnAttributes("get", RENDEZVOUS_DOTTED_NAME);
            String output = map.get("children");
            String val = output.substring(output.indexOf("=") + 1);
            rendezvousOccurred = Boolean.parseBoolean(val);
            if (CLILogger.isDebug()) {
                logger.printDebugMessage("rendezvousOccurred = " + val + " for instance " + instanceName);
            }
        } catch (CommandException ce) {
            logger.printDebugMessage("Remote command failed:");
            if (rc != null) {
                logger.printDebugMessage(rc.toString());
            }
            logger.printDebugMessage(RENDEZVOUS_PROPERTY_NAME+" property may not be set yet on instance " + instanceName);
            if (ce.getLocalizedMessage() != null) {
                logger.printDebugMessage(ce.getLocalizedMessage());
            }
            //logger.printExceptionStackTrace(ce);
        }
        return rendezvousOccurred;
    }

    private int setRendezvousOccurred(String rendezVal) throws CommandException {
        String dottedName = RENDEZVOUS_DOTTED_NAME + "=" + rendezVal;
        RemoteCommand rc = new RemoteCommand("set", this.programOpts, this.env);
        if (CLILogger.isDebug()) {
            logger.printDebugMessage("Setting rendezvousOccurred to " + rendezVal + " for instance " + instanceName);
            logger.printDebugMessage(rc.toString());
        }
        return rc.execute("set", dottedName);
    }

    private int createNodeImplicit(String name, String installdir, String nodeHost) throws CommandException {
        ArrayList<String> argsList = new ArrayList<String>();
        argsList.add(0, "_create-node-implicit");
        if (name != null) {
            argsList.add("--name");
            argsList.add(name);
        }
        if (nodeDir != null) {
            argsList.add("--nodedir");
            argsList.add(nodeDir);
        }
        if (installdir != null) {
            argsList.add("--installdir");
            argsList.add(installdir);
        }
        argsList.add(nodeHost);

        String[] argsArray = new String[argsList.size()];
        argsArray = argsList.toArray(argsArray);

        RemoteCommand rc = new RemoteCommand("_create-node-implicit", this.programOpts, this.env);
        return rc.execute(argsArray);
    }

    private int validateNode(String name, String installdir, String nodeHost) throws CommandException {
        ArrayList<String> argsList = new ArrayList<String>();
        argsList.add(0, "_validate-node");

        if (nodeDir != null) {
            argsList.add("--nodedir");
            argsList.add(nodeDir);
        }
        if (nodeHost != null) {
            argsList.add("--nodehost");
            argsList.add(nodeHost);
        }
        if (installdir != null) {
            argsList.add("--installdir");
            argsList.add(installdir);
        }

        argsList.add(name);

        String[] argsArray = new String[argsList.size()];
        argsArray = argsList.toArray(argsArray);

        RemoteCommand rc = new RemoteCommand("_validate-node", this.programOpts, this.env);
        return rc.execute(argsArray);
    }

    private void setPorts() {
        if (portBase != null) {
            StringBuffer sb;
            if (systemProperties == null) {
                sb = new StringBuffer();
            } else {
                sb = new StringBuffer(systemProperties);
            }
            if (sb.indexOf(ADMIN) == -1)
                sb.append(":" + ADMIN + "=" + pbh.getAdminPort());
            if (sb.indexOf(HTTP) == -1)
                sb.append(":" + HTTP + "=" + pbh.getInstancePort());
            if (sb.indexOf(HTTPS) == -1)
                sb.append(":" + HTTPS + "=" + pbh.getHttpsPort());
            if (sb.indexOf(IIOP) == -1)
                sb.append(":" + IIOP + "=" + pbh.getIiopPort());
            if (sb.indexOf(IIOPM) == -1)
                sb.append(":" + IIOPM + "=" + pbh.getIiopmPort());
            if (sb.indexOf(IIOPS) == -1)
                sb.append(":" + IIOPS + "=" + pbh.getIiopsPort());
            if (sb.indexOf(JMS) == -1)
                sb.append(":" + JMS + "=" + pbh.getJmsPort());
            if (sb.indexOf(JMX) == -1)
                sb.append(":" + JMX + "=" + pbh.getJmxPort());
            if (sb.charAt(0) == ':') {
                 systemProperties = sb.substring(1);
            } else {
                systemProperties = sb.toString();
            }
        }
    }

    private String getInstanceHostName(boolean isCanonical) throws CommandException {
        String instanceHostName = null;
        InetAddress localHost = null;
        try {
            localHost = InetAddress.getLocalHost();
        } catch (UnknownHostException ex) {
            throw new CommandException(Strings.get("cantGetHostName", ex));
        }
        if (localHost != null) {
            if (isCanonical) {
                instanceHostName = localHost.getCanonicalHostName();
            } else {
                instanceHostName = localHost.getHostName();
            }
        }
        return instanceHostName;
    }

    @Override
    public String getUsage() {
        String str = super.getUsage();
        String newStr = str;
        StringBuffer sb = new StringBuffer(str);
        String config = "--"+CONFIG+" <"+CONFIG+">";
        String cluster = "--"+CLUSTER+" <"+CLUSTER+">";
        String oldConfigCluster = "["+config+"] ["+cluster+"]";
        String newConfigCluster = "["+config+" | "+cluster+"]";
        int start = sb.indexOf(oldConfigCluster);
        if (start != -1) {
            int end = start + oldConfigCluster.length();
            StringBuffer newsb = sb.replace(start, end, newConfigCluster);
            newStr = newsb.toString();
        }
        return newStr;
    }

    @Override
    protected boolean mkdirs(File f) {
        if (setDasDefaultsOnly) {
            return true;
        } else {
            return f.mkdirs();
        }
    }

    @Override
    protected boolean isDirectory(File f) {
        if (setDasDefaultsOnly) {
            return true;
        } else {
            return f.isDirectory();
        }
    }

    @Override
    protected boolean setServerDirs() {
        if (setDasDefaultsOnly) {
            return false;
        } else {
            return true;
        }
    }
}
