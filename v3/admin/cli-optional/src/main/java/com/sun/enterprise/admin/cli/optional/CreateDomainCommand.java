/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.admin.cli.optional;

import java.io.File;
import java.io.Console;
import java.util.*;
import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.*;
import org.glassfish.api.embedded.*;
import org.glassfish.api.embedded.Server;
import org.glassfish.api.admin.config.*;
import com.sun.enterprise.admin.cli.*;
import com.sun.enterprise.admin.servermgmt.DomainConfig;
import com.sun.enterprise.admin.servermgmt.DomainsManager;
import com.sun.enterprise.admin.servermgmt.DomainException;
import com.sun.enterprise.admin.servermgmt.RepositoryManager;
import com.sun.enterprise.admin.servermgmt.pe.PEDomainsManager;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.net.NetUtils;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.module.bootstrap.*;

import com.sun.appserv.management.client.prefs.LoginInfo;
import com.sun.appserv.management.client.prefs.LoginInfoStore;
import com.sun.appserv.management.client.prefs.LoginInfoStoreFactory;
import com.sun.logging.*;

/**
 *  This is a local command that creates a domain.
 */
@Service(name = "create-domain")
@Scoped(PerLookup.class)
public final class CreateDomainCommand extends CLICommand {

    // constants for create-domain options
    private static final String DOMAINDIR = "domaindir";
    private static final String ADMIN_PORT = "adminport";
    private static final String ADMIN_PASSWORD = "AS_ADMIN_PASSWORD";
    private static final String ADMIN_ADMINPASSWORD = "AS_ADMIN_ADMINPASSWORD";
    private static final String MASTER_PASSWORD = "AS_ADMIN_MASTERPASSWORD";
    private static final String DEFAULT_MASTER_PASSWORD =
                                    RepositoryManager.DEFAULT_MASTER_PASSWORD;
    private static final String SAVE_MASTER_PASSWORD = "savemasterpassword";
    private static final String PROFILE_OPTION = "profile";
    private static final String DOMAIN_PATH = "path";
    private static final String INSTANCE_PORT = "instanceport";
    private static final String TEMPLATE = "template";
    private static final String DOMAIN_PROPERTIES = "domainproperties";
    private static final String CHECKPORTS_OPTION = "checkports";
    private static final String PORTBASE_OPTION = "portbase";
    private static final String SAVELOGIN_OPTION = "savelogin";
    private static final String KEYTOOLOPTIONS = "keytooloptions";
    private static final String NOPASSWORD = "nopassword";

    private static final int DEFAULT_HTTPSSL_PORT = 8181;
    private static final int DEFAULT_IIOPSSL_PORT = 3820;
    private static final int DEFAULT_IIOPMUTUALAUTH_PORT = 3920;
    private static final int DEFAULT_INSTANCE_PORT = 8080;
    private static final int DEFAULT_JMS_PORT = 7676;
    private static final String DEFAULT_JMS_USER = "admin";
    private static final String DEFAULT_JMS_PASSWORD = "admin";
    private static final int DEFAULT_IIOP_PORT = 3700;
    private static final int DEFAULT_JMX_PORT = 8686;
    private static final int DEFAULT_OSGI_SHELL_TELNET_PORT = 6666;
    private static final int PORT_MAX_VAL = 65535;
    private static final int PORTBASE_ADMINPORT_SUFFIX = 48;
    private static final int PORTBASE_HTTPSSL_SUFFIX = 81;
    private static final int PORTBASE_IIOPSSL_SUFFIX = 38;
    private static final int PORTBASE_IIOPMUTUALAUTH_SUFFIX = 39;
    private static final int PORTBASE_INSTANCE_SUFFIX = 80;
    private static final int PORTBASE_JMS_SUFFIX = 76;
    private static final int PORTBASE_IIOP_SUFFIX = 37;
    private static final int PORTBASE_JMX_SUFFIX = 86;
    private static final int PORTBASE_OSGI_SHELL_SUFFIX = 66;

    private static final char ESCAPE_CHAR = '\\';
    private static final char EQUAL_SIGN = '=';
    private static final String DELIMITER = ":";

    private String domainName = null;
    private String adminUser = null;
    private String adminPassword = null;
    private String masterPassword = null;

    private boolean checkPorts;

    private ValidOption masterPasswordOption = new ValidOption(MASTER_PASSWORD,
            "PASSWORD", ValidOption.REQUIRED, strings.get("MasterPassword"));
    private ValidOption adminPasswordOption = new ValidOption(ADMIN_PASSWORD,
            "PASSWORD", ValidOption.REQUIRED, strings.get("AdminPassword"));
    private ValidOption adminPortOption;
    private ValidOption instancePortOption;

    private static final LocalStringsImpl strings =
            new LocalStringsImpl(CreateDomainCommand.class);

    /**
     * The prepare method must ensure that the commandOpts,
     * operandType, operandMin, and operandMax fields are set.
     */
    @Override
    protected void prepare()
            throws CommandException, CommandValidationException {
        Set<ValidOption> opts = new LinkedHashSet<ValidOption>();
        adminPortOption =
            addOption(opts, ADMIN_PORT, '\0', "STRING", false, null);
        addOption(opts, PORTBASE_OPTION, '\0', "STRING", false, null);
        addOption(opts, PROFILE_OPTION, '\0', "STRING", false, null);
        addOption(opts, TEMPLATE, '\0', "STRING", false, null);
        addOption(opts, DOMAINDIR, '\0', "STRING", false, null);
        instancePortOption =
            addOption(opts, INSTANCE_PORT, '\0', "STRING", false, null);
        addOption(opts, SAVE_MASTER_PASSWORD, '\0', "BOOLEAN", false, "false");
        addOption(opts, DOMAIN_PROPERTIES, '\0', "STRING", false, null);
        addOption(opts, KEYTOOLOPTIONS, '\0', "STRING", false, null);
        addOption(opts, SAVELOGIN_OPTION, '\0', "BOOLEAN", false, "false");
        addOption(opts, CHECKPORTS_OPTION, '\0', "BOOLEAN", false, "true");
        addOption(opts, ADMIN_PASSWORD, '\0', "PASSWORD", false, null);
        addOption(opts, NOPASSWORD, '\0', "BOOLEAN", false, "false");
        addOption(opts, "help", '?', "BOOLEAN", false, "false");
        commandOpts = Collections.unmodifiableSet(opts);
        operandName = "domain_name";
        operandType = "STRING";
        operandMin = 1;
        operandMax = 1;

        processProgramOptions();
    }

    /**
     * Add --adminport and --instanceport options with
     * proper default values.  (Can't set default values above
     * because it conflicts with --portbase option processing.)
     */
    protected Set<ValidOption> usageOptions() {
        Set<ValidOption> opts = new LinkedHashSet<ValidOption>();
        addOption(opts, ADMIN_PORT, '\0', "STRING", false,
            Integer.toString(CLIConstants.DEFAULT_ADMIN_PORT));
        addOption(opts, INSTANCE_PORT, '\0', "STRING", false,
            Integer.toString(DEFAULT_INSTANCE_PORT));
        opts.addAll(commandOpts);
        opts.remove(adminPortOption);
        opts.remove(instancePortOption);
        return opts;
    }

    /**
     * The validate method validates that the type and quantity of
     * parameters and operands matches the requirements for this
     * command.  The validate method supplies missing options from
     * the environment.  It also supplies passwords from the password
     * file or prompts for them if interactive.
     */
    @Override
    protected void validate()
            throws CommandException, CommandValidationException  {
        /*
         * If --user wasn't specified as a program option,
         * we treat it as a required option and prompt for it
         * if possible, unless --nopassword was specified in
         * which case we default the user name.
         */
        if (programOpts.getUser() == null && !getBooleanOption(NOPASSWORD)) {
            // prompt for it (if interactive)
            Console cons = System.console();
            if (cons != null && programOpts.isInteractive()) {
                cons.printf("%s", strings.get("AdminUserRequiredPrompt",
                    SystemPropertyConstants.DEFAULT_ADMIN_USER));
                String val = cons.readLine();
                if (ok(val))
                    programOpts.setUser(val);
            } else {
                //logger.printMessage(strings.get("AdminUserRequired"));
                throw new CommandValidationException(
                    strings.get("AdminUserRequired"));
            }
        }

        /*
         * Validate the other options.  Since no other options are required,
         * the next prompted-for value will be the admin password, unless
         * the domain name wasn't specified.  In that case, the domain name
         * prompt will come between the prompt for the user name and the
         * password, which is a bit ugly but we'll ignore that for now.
         */
        super.validate();

        checkPorts = getBooleanOption(CHECKPORTS_OPTION);
    }

    public void verifyPortBase() throws CommandValidationException {
        if (usePortBase()) {
            final int portbase = convertPortStr(getOption(PORTBASE_OPTION));
            setOptionsWithPortBase(portbase);
        } else if (getOption(ADMIN_PORT) == null) {
            options.put(ADMIN_PORT,
                Integer.toString(CLIConstants.DEFAULT_ADMIN_PORT));
        }
    }

    private void setOptionsWithPortBase(final int portbase)
            throws CommandValidationException {
        // set the option name and value in the options list
        verifyPortBasePortIsValid(ADMIN_PORT,
            portbase + PORTBASE_ADMINPORT_SUFFIX);
        options.put(ADMIN_PORT,
            String.valueOf(portbase + PORTBASE_ADMINPORT_SUFFIX));

        verifyPortBasePortIsValid(INSTANCE_PORT,
            portbase + PORTBASE_INSTANCE_SUFFIX);
        options.put(INSTANCE_PORT,
            String.valueOf(portbase + PORTBASE_INSTANCE_SUFFIX));

        StringBuffer sb = new StringBuffer();
        verifyPortBasePortIsValid(DomainConfig.K_HTTP_SSL_PORT,
            portbase + PORTBASE_HTTPSSL_SUFFIX);
        sb.append(DomainConfig.K_HTTP_SSL_PORT);
        sb.append("=");
        sb.append(String.valueOf(portbase + PORTBASE_HTTPSSL_SUFFIX));
        sb.append(":");

        verifyPortBasePortIsValid(DomainConfig.K_IIOP_SSL_PORT,
            portbase + PORTBASE_IIOPSSL_SUFFIX);
        sb.append(DomainConfig.K_IIOP_SSL_PORT);
        sb.append("=");
        sb.append(String.valueOf(portbase + PORTBASE_IIOPSSL_SUFFIX));
        sb.append(":");

        verifyPortBasePortIsValid(DomainConfig.K_IIOP_MUTUALAUTH_PORT,
                portbase + PORTBASE_IIOPMUTUALAUTH_SUFFIX);
        sb.append(DomainConfig.K_IIOP_MUTUALAUTH_PORT);
        sb.append("=");
        sb.append(String.valueOf(portbase + PORTBASE_IIOPMUTUALAUTH_SUFFIX));
        sb.append(":");

        verifyPortBasePortIsValid(DomainConfig.K_JMS_PORT,
            portbase + PORTBASE_JMS_SUFFIX);
        sb.append(DomainConfig.K_JMS_PORT);
        sb.append("=");
        sb.append(String.valueOf(portbase + PORTBASE_JMS_SUFFIX));
        sb.append(":");

        verifyPortBasePortIsValid(DomainConfig.K_ORB_LISTENER_PORT,
            portbase + PORTBASE_IIOP_SUFFIX);
        sb.append(DomainConfig.K_ORB_LISTENER_PORT);
        sb.append("=");
        sb.append(String.valueOf(portbase + PORTBASE_IIOP_SUFFIX));
        sb.append(":");

        verifyPortBasePortIsValid(DomainConfig.K_JMX_PORT,
            portbase + PORTBASE_JMX_SUFFIX);
        sb.append(DomainConfig.K_JMX_PORT);
        sb.append("=");
        sb.append(String.valueOf(portbase + PORTBASE_JMX_SUFFIX));
        sb.append(":");

        verifyPortBasePortIsValid(DomainConfig.K_OSGI_SHELL_TELNET_PORT,
            portbase + PORTBASE_OSGI_SHELL_SUFFIX);
        sb.append(DomainConfig.K_OSGI_SHELL_TELNET_PORT);
        sb.append("=");
        sb.append(String.valueOf(portbase + PORTBASE_OSGI_SHELL_SUFFIX));

        options.put(DOMAIN_PROPERTIES, sb.toString());
    }

    /**
     */
    @Override
    protected int executeCommand()
            throws CommandException, CommandValidationException {

        // domain validation upfront (i.e. before we prompt)
        try {
            domainName = operands.get(0);
            DomainsManager manager = new PEDomainsManager();
            DomainConfig config =
                new DomainConfig(domainName, getDomainsRoot());
            manager.validateDomain(config, false);
            verifyPortBase();
        } catch (DomainException e) {
            logger.printDetailMessage(e.getLocalizedMessage());
            throw new CommandException(
                strings.get("CouldNotCreateDomain", domainName), e);
        }

        /*
         * The admin user is specified with the --user program option.
         * If not specified (because the user hit Enter at the prompt),
         * we use the default, which allows unauthenticated login.
         */
        adminUser = programOpts.getUser();
        if (!ok(adminUser)) {
            adminUser = SystemPropertyConstants.DEFAULT_ADMIN_USER;
            adminPassword = SystemPropertyConstants.DEFAULT_ADMIN_PASSWORD;
            masterPassword = DEFAULT_MASTER_PASSWORD;
        } else if (getBooleanOption(NOPASSWORD)) {
            adminPassword = SystemPropertyConstants.DEFAULT_ADMIN_PASSWORD;
            masterPassword = DEFAULT_MASTER_PASSWORD;
        } else {
            /*
             * If the admin password was supplied in the password
             * file, and no master password is suppied, we use the
             * default master password without prompting.
             *
             * The admin password can be supplied using the deprecated
             * AS_ADMIN_ADMINPASSWORD option in the password file.
             */
            boolean haveAdminPwd = false;
            adminPassword = passwords.get(ADMIN_ADMINPASSWORD);
            if (adminPassword != null) {
                haveAdminPwd = true;
                logger.printWarning(strings.get("DeprecatedAdminPassword"));
            } else {
                haveAdminPwd = passwords.get(ADMIN_PASSWORD) != null;
                adminPassword = getAdminPassword();
            }
            validatePassword(adminPassword, adminPasswordOption);
            if (haveAdminPwd)
                masterPassword = passwords.get(MASTER_PASSWORD);
            else
                masterPassword = getMasterPassword();
            if (masterPassword == null)
                masterPassword = DEFAULT_MASTER_PASSWORD;
            validatePassword(masterPassword, masterPasswordOption);
        }


        try {
            // verify admin port is valid if specified on command line
            if (getOption(ADMIN_PORT) != null) {
                verifyPortIsValid(getOption(ADMIN_PORT));
            }
            // instance option is entered then verify instance port is valid
            if (getOption(INSTANCE_PORT) != null) {
                verifyPortIsValid(getOption(INSTANCE_PORT));
            }

            // get domain properties from options or from option
            Properties domainProperties =
                getDomainProperties(getOption(DOMAIN_PROPERTIES));
            // we give priority to the --adminport option
            domainProperties.remove(DomainConfig.K_ADMIN_PORT);
            // saving the login information happens inside this method
            createTheDomain(getDomainsRoot(), domainProperties);
        } catch (CommandException ce) {
            logger.printMessage(ce.getLocalizedMessage());
            throw new CommandException(
                strings.get("CouldNotCreateDomain", domainName), ce);
        } catch (Exception e) {
            logger.printDetailMessage(e.getLocalizedMessage());
            throw new CommandException(
                strings.get("CouldNotCreateDomain", domainName), e);
        }
        return 0;
    }

    /**
     * Verify that the port is valid.
     * Port must be greater than 0 and less than 65535.
     * This method will also check if the port is in use.
     * If checkPorts is false it does not throw an Exception if it is in use.
     *
     * @param portNum - the port number to verify
     * @throws CommandException if Port is not valid
     * @throws CommandValidationException is port number is not a numeric value.
     */
    private void verifyPortIsValid(String portNum)
            throws CommandException, CommandValidationException {

        final int portToVerify = convertPortStr(portNum);
        NetUtils.PortAvailability avail = NetUtils.checkPort(portToVerify);
	String domainName = operands.get(0);

        switch (avail) {
        case illegalNumber:
            throw new CommandException(
                strings.get("InvalidPortRange", portNum));

        case inUse:
            if (checkPorts)
                throw new CommandException(
                    strings.get("PortInUseError", domainName, portNum));
            else
                logger.printWarning(strings.get("PortInUseWarning", portNum));
            break;

        case noPermission:
            if (checkPorts)
                throw new CommandException(
                    strings.get("NoPermissionForPortError", 
                    portNum, domainName));
            else
                logger.printWarning(strings.get("NoPermissionForPortWarning", 
                    portNum, domainName));
            break;

        case unknown:
            throw new CommandException(strings.get("UnknownPortMsg", portNum));

        case OK:
            logger.printDebugMessage("Port =" + portToVerify);
            break;
        }
    }

    /**
     * Converts the port string to port int
     *
     * @param port the port number
     * @return the port number as an int
     * @throws CommandValidationException if port string is not numeric
     */
    private int convertPortStr(final String port)
            throws CommandValidationException {
        try {
            return Integer.parseInt(port);
        } catch (Exception e) {
            throw new CommandValidationException(
                    strings.get("InvalidPortNumber", port));
        }
    }

    /**
     * Verify that the portbase port is valid
     * Port must be greater than 0 and less than 65535.
     * This method will also check if the port is in used.
     *
     * @param portNum the port number to verify
     * @throws CommandException if Port is not valid
     * @throws CommandValidationException is port number is not a numeric value.
     */
    private void verifyPortBasePortIsValid(String portName, int portNum)
            throws CommandValidationException {
        if (portNum <= 0 || portNum > PORT_MAX_VAL) {
            throw new CommandValidationException(
                strings.get("InvalidPortBaseRange", portNum, portName));
        }
        if (checkPorts && !NetUtils.isPortFree(portNum)) {
            throw new CommandValidationException(
                strings.get("PortBasePortInUse", portNum, portName));
        }
        logger.printDebugMessage("Port =" + portNum);
    }

    /**
     * Get domain properties from options
     *
     * @param propertyValues from options
     * @return domain properties
     * @throws CommandException if cannot get properties
     **/
    private Properties getDomainProperties(final String propertyValues)
            throws CommandException, CommandValidationException {
        Properties propertyList = new Properties();

        if (propertyValues == null) {
            return propertyList;
        }
        StringTokenizer st = new StringTokenizer(propertyValues, DELIMITER);
        while (st.hasMoreTokens()) {
            String propertyString = st.nextToken();
            while (st.hasMoreTokens() &&
                    propertyString.endsWith(Character.toString(ESCAPE_CHAR))) {
                propertyString = propertyString.concat(st.nextToken());
            }
            int index = propertyString.indexOf(Character.toString(EQUAL_SIGN));
            if (index == -1) {
                throw new CommandValidationException(
                    strings.get("InvalidPropertySyntax"));
            }
            final String propertyName = propertyString.substring(0, index);
            final String propertyValue = propertyString.substring(index + 1);
            propertyList.put(propertyName, propertyValue);
        }
        logger.printDebugMessage("domain properties = " + propertyList);
        return propertyList;
    }

    /**
     * Create the domain.
     *
     * @param domainPath domain path to insert in domainConfig
     * @param domainProperties properties to insert in domainConfig
     * @throws CommandException if domain cannot be created
     */
    private void createTheDomain(final String domainPath,
            Properties domainProperties)
            throws DomainException, CommandValidationException {
        final Integer adminPort = Integer.valueOf(getOption(ADMIN_PORT));
        logger.printDetailMessage(
            strings.get("UsingPort", "Admin", Integer.toString(adminPort)));

        //
        // fix for bug# 4930684
        // domain name is validated before the ports
        //
        String domainFilePath = (domainPath + File.separator + domainName);
        if (FileUtils.safeGetCanonicalFile(new File(domainFilePath)).exists()) {
            throw new CommandValidationException(
                strings.get("DomainExists", domainName));
        }

        final Integer instancePort = getPort(domainProperties,
                DomainConfig.K_INSTANCE_PORT,
                getOption(INSTANCE_PORT),
                Integer.toString(DEFAULT_INSTANCE_PORT),
                "HTTP Instance");

        final Integer jmsPort = getPort(domainProperties,
                DomainConfig.K_JMS_PORT, null,
                Integer.toString(DEFAULT_JMS_PORT), "JMS");

        /*
         * Bug# 4859518
         * The authentication of jms broker will fail if the jms user is not in
         * the user database referred by jms broker. Setting the jms user and
         * password to "admin" which is always present in the user db. Once
         * the mechanism to update the user db with given jms user/password is
         * in place we should set the user specified values for jms user and
         * password.

        final String jmsUser = adminUser;
        final String jmsPassword = adminPassword;
         */
        final String jmsUser = DEFAULT_JMS_USER;
        final String jmsPassword = DEFAULT_JMS_PASSWORD;

        final Integer orbPort = getPort(domainProperties,
                DomainConfig.K_ORB_LISTENER_PORT,
                null, Integer.toString(DEFAULT_IIOP_PORT),
                "IIOP");

        final Integer httpSSLPort = getPort(domainProperties,
                DomainConfig.K_HTTP_SSL_PORT, null,
                Integer.toString(DEFAULT_HTTPSSL_PORT),
                "HTTP_SSL");

        final Integer iiopSSLPort = getPort(domainProperties,
                DomainConfig.K_IIOP_SSL_PORT, null,
                Integer.toString(DEFAULT_IIOPSSL_PORT),
                "IIOP_SSL");

        final Integer iiopMutualAuthPort = getPort(domainProperties,
                DomainConfig.K_IIOP_MUTUALAUTH_PORT, null,
                Integer.toString(DEFAULT_IIOPMUTUALAUTH_PORT),
                "IIOP_MUTUALAUTH");

        final Integer jmxPort = getPort(domainProperties,
                DomainConfig.K_JMX_PORT, null,
                Integer.toString(DEFAULT_JMX_PORT),
                "JMX_ADMIN");

        final Integer osgiShellTelnetPort = getPort(domainProperties,
                DomainConfig.K_OSGI_SHELL_TELNET_PORT, null,
                Integer.toString(DEFAULT_OSGI_SHELL_TELNET_PORT),
                "OSGI_SHELL");

        boolean saveMasterPassword = getSaveMasterPassword(masterPassword);

        checkPortPrivilege(new Integer[]{
            adminPort, instancePort, jmsPort, orbPort, httpSSLPort,
            jmsPort, orbPort, httpSSLPort, iiopSSLPort,
            iiopMutualAuthPort, jmxPort
        });

        DomainConfig domainConfig = new DomainConfig(domainName,
                adminPort, domainPath, adminUser,
                adminPassword,
                masterPassword,
                saveMasterPassword, instancePort, jmsUser,
                jmsPassword, jmsPort, orbPort,
                httpSSLPort, iiopSSLPort,
                iiopMutualAuthPort, jmxPort, osgiShellTelnetPort,
                domainProperties);
        if (getOption(TEMPLATE) != null) {
            domainConfig.put(DomainConfig.K_TEMPLATE_NAME, getOption(TEMPLATE));
        }

        domainConfig.put(DomainConfig.K_VALIDATE_PORTS,
                Boolean.valueOf(checkPorts));

        /* comment out for V3 until profiles decision is taken */
        // setUsageProfile(domainConfig);

        domainConfig.put(DomainConfig.KEYTOOLOPTIONS,
            getOption(KEYTOOLOPTIONS));
        DomainsManager manager = new PEDomainsManager();

        manager.createDomain(domainConfig);
        try {
            modifyInitialDomainXml(domainConfig);
        } catch (Exception e) {
            logger.printWarning(
                            strings.get("CustomizationFailed",e.getMessage()));
        }
        logger.printMessage(strings.get("DomainCreated", domainName));
        logger.printMessage(
            strings.get("DomainPort", domainName, Integer.toString(adminPort)));
        if (adminPassword.equals(
                SystemPropertyConstants.DEFAULT_ADMIN_PASSWORD))
            logger.printMessage(strings.get("DomainAllowsUnauth", domainName,
                                                                    adminUser));
        else
            logger.printMessage(
                strings.get("DomainAdminUser", domainName, adminUser));
        //checkAsadminPrefsFile();
        if (getBooleanOption(SAVELOGIN_OPTION)) {
            saveLogin(adminPort, adminUser, adminPassword, domainName);
        }
    }

    private void setUsageProfile(final DomainConfig dc) {
        String pp = getOption(PROFILE_OPTION);
        String source = strings.get("ProfileUserSource");
        if (pp == null) {
            /*
             * Implementation note: This is the ONLY place where this value
             * is hardcoded. It is almost an assertion that the value is
             * available. All Application Server installations will
             * make sure that there is a proper value for this variable
             * set in the configuration file.
             */
            pp = "developer";
            source = strings.get("ProfileGlobalDefaultSource");
        }
        dc.put(DomainConfig.K_PROFILE, pp);
        String msg = strings.get("UsingProfile", pp, source);
        final String templateName = getOption(TEMPLATE);
        if (templateName != null) {
            msg = strings.get("UsingTemplate", templateName);
        }
        logger.printMessage(msg);
    }

    /**
     * Saves the login information to the login store.  Usually this is the file
     * ".asadminpass" in user's home directory.
     */
    private void saveLogin(final int port, final String user,
            final String password, final String dn) {
        try {
            // by definition, the host name will default to "localhost"
            // and entry is overwritten
            final LoginInfoStore store = LoginInfoStoreFactory.getStore(null);
            final LoginInfo login =
                new LoginInfo("localhost", port, user, password);
            if (store.exists(login.getHost(), login.getPort())) {
                // just let the user know that the user has chosen to overwrite
                // the login information. This is non-interactive, on purpose
                logger.printMessage(strings.get("OverwriteLoginMsgCreateDomain",
                                        login.getHost(), "" + login.getPort()));
            }
            store.store(login, true);
            logger.printMessage(strings.get("LoginInfoStoredCreateDomain",
                                    user, dn, store.getName()));
        } catch (final Exception e) {
            logger.printWarning(
                strings.get("LoginInfoNotStoredCreateDomain", user, dn));
            if (logger.isDebug()) {
                logger.printExceptionStackTrace(e);
            }
        }
    }

    /**
     * Get port from the properties option or default or free port.
     *
     * @param properties properties from command line
     * @param key key for the type of port
     * @param portStr the port as a string, or null to get from properties
     * @param defaultPort default port to use
     * @param name name of port
     * @throws CommandValidationException if error in retrieve port
     */
    private Integer getPort(Properties properties,
            String key,
            String portStr,
            String defaultPort,
            String name)
            throws CommandValidationException {
        int port = 0;
        boolean portNotSpecified = false;
        boolean invalidPortSpecified = false;
        boolean defaultPortUsed = false;
        if ((portStr != null) && !portStr.equals("")) {
            port = convertPortStr(portStr);
            if ((port <= 0) || (port > PORT_MAX_VAL)) {
                invalidPortSpecified = true;
            }
        } else if (properties != null) {
            String property = properties.getProperty(key);
            if ((property != null) && !property.equals("")) {
                port = convertPortStr(property);
            } else {
                portNotSpecified = true;
            }
        } else {
            portNotSpecified = true;
        }
        if (portNotSpecified) {
            port = convertPortStr(defaultPort);
            defaultPortUsed = true;
        }
        if (checkPorts && !NetUtils.isPortFree(port)) {
            int newport = NetUtils.getFreePort();
            if (portNotSpecified) {
                if (defaultPortUsed) {
                    logger.printDetailMessage(strings.get("DefaultPortInUse",
                            name, defaultPort, Integer.toString(newport)));
                } else {
                    logger.printDetailMessage(strings.get("PortNotSpecified",
                            name, Integer.toString(newport)));
                }
            } else if (invalidPortSpecified) {
                logger.printDetailMessage(strings.get("InvalidPortRangeMsg",
                        name, Integer.toString(newport)));
            } else {
                logger.printDetailMessage(strings.get("PortInUse",
                    name, Integer.toString(port), Integer.toString(newport)));
            }
            port = newport;
        } else if (defaultPortUsed) {
            logger.printDetailMessage(strings.get("UsingDefaultPort",
                    name, Integer.toString(port)));
        } else {
            logger.printDetailMessage( strings.get("UsingPort",
                    name, Integer.toString(port)));
        }

        if (properties != null) {
            properties.remove(key);
        }
        return Integer.valueOf(port);
    }

    /**
     * Check if portbase option is specified.
     * Portbase is mutually exclusive to adminport and domainproperties options.
     * If portbase options is specfied and also adminport or domainproperties
     * is specified as well, then throw an exception.
     */
    private boolean usePortBase() throws CommandValidationException {
        if (getOption(PORTBASE_OPTION) != null) {
            if (getOption(ADMIN_PORT) != null) {
                throw new CommandValidationException(
                    strings.get("MutuallyExclusiveOption",
                        ADMIN_PORT, PORTBASE_OPTION));
            } else if (getOption(INSTANCE_PORT) != null) {
                throw new CommandValidationException(
                    strings.get("MutuallyExclusiveOption",
                        INSTANCE_PORT, PORTBASE_OPTION));
            } else if (getOption(DOMAIN_PROPERTIES) != null) {
                throw new CommandValidationException(
                    strings.get("MutuallyExclusiveOption",
                        DOMAIN_PROPERTIES, PORTBASE_OPTION));
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if any of the port values are below 1024.
     * If below 1024, then display a warning message.
     */
    private void checkPortPrivilege(final Integer[] ports) {
        for (Integer port : ports) {
            final int p = port.intValue();
            if (p < 1024) {
                logger.printWarning(strings.get("PortPrivilege"));
                // display this message only once.
                // so break once this message is displayed.
                break;
            }
        }
    }

    /* validates adminpassword and masterpassword */
    public void validatePassword(String password, ValidOption pwdOpt)
            throws CommandValidationException {
        // XXX - hack alert!  the description is stored in the default value
        String description = pwdOpt.getDefaultValue();
        if (!ok(description))
            description = pwdOpt.getName();

        if (password == null)
            throw new CommandValidationException(
                                strings.get("PasswordMissing", description));
    }

    /**
     * Get the admin password, either from the password file or by
     * prompting (if allowed).
     *
     * @return admin password
     * @throws CommandValidationException if could not get the admin password
     */
    protected String getAdminPassword() throws CommandValidationException {
        return getPassword(adminPasswordOption, "", true);
    }

    /**
     * Get the master password, prompting if necessary, and
     * accepting the default ("changeit").
     */
    private String getMasterPassword()
            throws CommandValidationException, CommandException {

        return getPassword(masterPasswordOption,
            DEFAULT_MASTER_PASSWORD, true);
    }

    /*
     */
    private void modifyInitialDomainXml(DomainConfig domainConfig)
                                throws LifecycleException {
        // for each module implementing the @Contract DomainInitializer, extract
        // the initial domain.xml and insert it into the existing domain.xml

        Server.Builder builder = new Server.Builder("dummylaunch");
        EmbeddedFileSystem.Builder efsb = new EmbeddedFileSystem.Builder();
        efsb.installRoot(new File(domainConfig.getInstallRoot()));
        File domainDir = new File(domainConfig.getDomainRoot(),
                                        domainConfig.getDomainName());
        File configDir = new File(domainDir, "config");
        efsb.configurationFile(new File(configDir, "domain.xml"), false);
        builder.embeddedFileSystem(efsb.build());

        Properties properties = new Properties();
        properties.setProperty(StartupContext.STARTUP_MODULESTARTUP_NAME,
                                        "DomainCreation");
        Server server = builder.build(properties);

        server.start();
        Habitat habitat = server.getHabitat();
        // Will always need DAS's name & config. No harm using the name 'server'
        // to fetch <server-config>
        com.sun.enterprise.config.serverbeans.Server serverConfig =
            habitat.getComponent(
                com.sun.enterprise.config.serverbeans.Server.class, "server");
        Config config = habitat.getComponent(
            Config.class, serverConfig.getConfigRef());

        // Create a context object for this domain creation to enable the new
        // modules to make decisions
        DomainContext ctx = new DomainContext();
        ctx.setDomainType("dev"); //TODO : Whenever clustering/HA is supported
        // this setting needs to be fixed. Domain type can be dev/ha/cluster and
        // this type needs to be extracted possibly using an api from installer
        ctx.setLogger(LogDomains.getLogger(
            DomainInitializer.class, LogDomains.SERVER_LOGGER));

        // now for every such Inhabitant, fetch the actual initial config and
        // insert it into the module that initial config was targeted for.
        Collection<DomainInitializer> inits =
                habitat.getAllByContract(DomainInitializer.class);
        if (inits.isEmpty()) {
            logger.printMessage(
                "No domain initializers found, bypassing customization step");
        }
        for (DomainInitializer inhabitant : habitat.getAllByContract(
            DomainInitializer.class)) {
            logger.printMessage(
                "Invoke DomainInitializer " + inhabitant.getClass());
            Container newContainerConfig = inhabitant.getInitialConfig(ctx);
            config.getContainers().add(newContainerConfig);
        }
        server.stop();
    }

    /**
     * Should we save the master password or not?
     * Normally this is based on the --savemasterpassword command
     * line option, but if we're using the default master password
     * we always save it.  This means, in the default case, start-domain
     * will always find the master password and will never need to
     * prompt for it.
     */
    protected boolean getSaveMasterPassword(String masterPassword) {
        boolean saveMasterPassword = getBooleanOption(SAVE_MASTER_PASSWORD);
        if (masterPassword != null &&
                masterPassword.equals(DEFAULT_MASTER_PASSWORD)) {
            saveMasterPassword = true;
        }
        return saveMasterPassword;
    }

    protected String getDomainsRoot() throws CommandException {
        String domainDir = getOption(DOMAINDIR);
        if (domainDir == null) {
            domainDir = getSystemProperty(
                            SystemPropertyConstants.DOMAINS_ROOT_PROPERTY);
        }
        if (domainDir == null) {
            throw new CommandException(
                            strings.get("InvalidDomainPath", domainDir));
        }
        return domainDir;
    }
}
