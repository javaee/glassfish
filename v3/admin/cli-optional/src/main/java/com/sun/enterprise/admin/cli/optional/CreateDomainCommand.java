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
package com.sun.enterprise.admin.cli.optional;

import com.sun.enterprise.cli.framework.*;
import com.sun.enterprise.util.SystemPropertyConstants;
import java.io.File;

import com.sun.enterprise.admin.servermgmt.pe.PEDomainsManager;
import com.sun.enterprise.admin.servermgmt.DomainsManager;
import com.sun.enterprise.admin.servermgmt.DomainConfig;

import java.util.Collection;
import java.util.Properties;
import java.util.StringTokenizer;

import com.sun.enterprise.util.net.NetUtils;
import com.sun.enterprise.util.io.FileUtils;

import com.sun.appserv.management.client.prefs.LoginInfo;
import com.sun.appserv.management.client.prefs.LoginInfoStore;
import com.sun.appserv.management.client.prefs.LoginInfoStoreFactory;

// imports for config pluggability feature
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.Habitat;
import org.glassfish.api.embedded.LifecycleException;
import org.glassfish.api.embedded.Server;
import org.glassfish.api.admin.config.Container;
import org.glassfish.api.admin.config.DomainInitializer;
import org.glassfish.api.admin.config.DomainContext;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.logging.LogDomains;

public class CreateDomainCommand extends BaseLifeCycleCommand {

    // constant variables for create-domain options
    private static final String DOMAIN_PATH = "path";
    private static final String INSTANCE_PORT = "instanceport";
    private static final String DOCROOT = "docroot";
    private static final String TEMPLATE = "template";
    private static final String DOMAIN_PROPERTIES = "domainproperties";
    private static final String CHECKPORTS_OPTION = "checkports";
    private static final String PORTBASE_OPTION = "portbase";
    private static final int DEFAULT_HTTPSSL_PORT = 8181;
    private static final int DEFAULT_IIOPSSL_PORT = 3820;
    private static final int DEFAULT_IIOPMUTUALAUTH_PORT = 3920;
    private static final int DEFAULT_INSTANCE_PORT = 8080;
    private static final int DEFAULT_JMS_PORT = 7676;
    private static final String DEFAULT_JMS_USER = "admin";
    private static final String DEFAULT_JMS_PASSWORD = "admin";
    private static final int DEFAULT_IIOP_PORT = 3700;
    private static final int DEFAULT_JMX_PORT = 8686;
    private static final int PORT_MAX_VAL = 65535;
    private static final int PORTBASE_ADMINPORT_SUFFIX = 48;
    private static final int PORTBASE_HTTPSSL_SUFFIX = 81;
    private static final int PORTBASE_IIOPSSL_SUFFIX = 38;
    private static final int PORTBASE_IIOPMUTUALAUTH_SUFFIX = 39;
    private static final int PORTBASE_INSTANCE_SUFFIX = 80;
    private static final int PORTBASE_JMS_SUFFIX = 76;
    private static final int PORTBASE_IIOP_SUFFIX = 37;
    private static final int PORTBASE_JMX_SUFFIX = 86;
    public static final String DOMAINDIR_OPTION = "domaindir";
    public static final String PROFILE_OPTION = "profile";
    private static final String SAVELOGIN_OPTION = "savelogin";
    private static final String KEYTOOLOPTIONS = "keytooloptions";
    private String domainName = null;
    private String adminUser = null;
    private String adminPassword = null;
    private String masterPassword = null;
    private boolean checkPorts;

    @Override
    public boolean validateOptions() throws CommandValidationException {
        boolean ret = super.validateOptions();
        checkPorts = getBooleanOption(CHECKPORTS_OPTION);
        return ret;
    }

    public void verifyPortBase() throws CommandValidationException {
        if (usePortBase()) {
            final int portbase = convertPortStr(getOption(PORTBASE_OPTION));
            setOptionsWithPortBase(portbase);
        } else if (getOption(ADMIN_PORT) == null) {
            throw new CommandValidationException(getLocalizedString("RequireEitherOrOption",
                    new Object[]{ADMIN_PORT,
                PORTBASE_OPTION
            }));
        }
    }

    private void setOptionsWithPortBase(final int portbase) throws CommandValidationException {
        //set the option name and value in the options list
        verifyPortBasePortIsValid(ADMIN_PORT, portbase + PORTBASE_ADMINPORT_SUFFIX);
        setOption(ADMIN_PORT, String.valueOf(portbase + PORTBASE_ADMINPORT_SUFFIX));

        verifyPortBasePortIsValid(INSTANCE_PORT, portbase + PORTBASE_INSTANCE_SUFFIX);
        setOption(INSTANCE_PORT, String.valueOf(portbase + PORTBASE_INSTANCE_SUFFIX));

        StringBuffer sb = new StringBuffer();
        verifyPortBasePortIsValid(DomainConfig.K_HTTP_SSL_PORT, portbase + PORTBASE_HTTPSSL_SUFFIX);
        sb.append(DomainConfig.K_HTTP_SSL_PORT);
        sb.append("=");
        sb.append(String.valueOf(portbase + PORTBASE_HTTPSSL_SUFFIX));
        sb.append(":");

        verifyPortBasePortIsValid(DomainConfig.K_IIOP_SSL_PORT, portbase + PORTBASE_IIOPSSL_SUFFIX);
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

        verifyPortBasePortIsValid(DomainConfig.K_JMS_PORT, portbase + PORTBASE_JMS_SUFFIX);
        sb.append(DomainConfig.K_JMS_PORT);
        sb.append("=");
        sb.append(String.valueOf(portbase + PORTBASE_JMS_SUFFIX));
        sb.append(":");

        verifyPortBasePortIsValid(DomainConfig.K_ORB_LISTENER_PORT, portbase + PORTBASE_IIOP_SUFFIX);
        sb.append(DomainConfig.K_ORB_LISTENER_PORT);
        sb.append("=");
        sb.append(String.valueOf(portbase + PORTBASE_IIOP_SUFFIX));
        sb.append(":");

        verifyPortBasePortIsValid(DomainConfig.K_JMX_PORT, portbase + PORTBASE_JMX_SUFFIX);
        sb.append(DomainConfig.K_JMX_PORT);
        sb.append("=");
        sb.append(String.valueOf(portbase + PORTBASE_JMX_SUFFIX));
        setOption(DOMAIN_PROPERTIES, sb.toString());
    }

    /* validates adminpassword and masterpassword */
    public void validatePassword(String password, String type) throws CommandValidationException {
        if (!isPasswordValid(password)) {
            throw new CommandValidationException(getLocalizedString("PasswordLimit",
                    new Object[]{type}));
        }
        CLILogger.getInstance().printDebugMessage("domainName = " + domainName);
    }

    /**
     *  this methods returns the admin password and is used to get the admin password
     *  for create-domain. The password can be passed in on the command line 
     *  (--adminpassword), environment variable (AS_ADMIN_ADMINPASSWORD), 
     *  or in the password file (--passwordfile).
     *  first it checks if adminpassword option is specified on command line.
     *  if not, then it'll try to get AS_ADMIN_ADMINPASSWORD and AS_ADMIN_PASWORD from
     *  the password file.  If both are specified, the AS_ADMIN_PASSWORD takes precedence.
     *  if all else fails, then prompt the user for the password if interactive=true.
     *  @return admin password
     *  @throws CommandValidationException if could not get adminpassword option 
     */
    protected String getAdminPassword()
            throws CommandValidationException, CommandException {
        //getPassword(optionName, allowedOnCommandLine, readPrefsFile, readPasswordOptionFromPrefs, readMasterPasswordFile, mgr, config,
        //promptUser, confirm, validate)
        final String deprecatePassword = getPassword(ADMIN_PASSWORD, true, false, true, false,
                null, null, false, false, false, false);
        String tempPassword = null;
        if (deprecatePassword == null) {
            tempPassword = getPassword(PASSWORD, "AdminPasswordPrompt",
                    "AdminPasswordConfirmationPrompt",
                    true, false, false, false, null, null,
                    true, true, true, true);
        } else {
            //AS_ADMIN_ADMINPASWORD takes precedence. 
            return deprecatePassword;
        }
        return tempPassword;
    }

    /**
     *  An abstract method that executes the command
     *  @throws CommandException
     */
    public void runCommand()
            throws CommandException, CommandValidationException {
        validateOptions();

        //domain validation upfront (i.e. before we prompt)
        try {
            domainName = (String) operands.firstElement();
            DomainsManager manager = new PEDomainsManager();
            DomainConfig config = getDomainConfig(domainName);
            manager.validateDomain(config, false);
            verifyPortBase();
        } catch (Exception e) {
            CLILogger.getInstance().printDetailMessage(e.getLocalizedMessage());
            throw new CommandException(getLocalizedString("CouldNotCreateDomain",
                    new Object[]{domainName}), e);
        }


        adminUser = getAdminUser();
        if (adminUser == null || adminUser.length() == 0) {
            adminUser = SystemPropertyConstants.DEFAULT_ADMIN_USER;
            adminPassword = SystemPropertyConstants.DEFAULT_ADMIN_PASSWORD;
            masterPassword = DEFAULT_MASTER_PASSWORD;
        } else {
            if (adminUser.equals(SystemPropertyConstants.DEFAULT_ADMIN_USER) &&
                    (adminPassword == null || adminPassword.length() == 0)) {
                adminPassword = SystemPropertyConstants.DEFAULT_ADMIN_PASSWORD;
                masterPassword = DEFAULT_MASTER_PASSWORD;
            } else {                            
                adminPassword = getAdminPassword();
                validatePassword(adminPassword, ADMIN_PASSWORD);
                masterPassword = getMasterPassword();
                validatePassword(masterPassword, MASTER_PASSWORD);
            }
        }


        try {
            //verify admin port is valid if specified on command line
            if (getOption(ADMIN_PORT) != null) {
                verifyPortIsValid(getOption(ADMIN_PORT));
            }
            //instance option is entered then verify instance port is valid
            if (getOption(INSTANCE_PORT) != null) {
                verifyPortIsValid(getOption(INSTANCE_PORT));
            }

            //get domain properties from options or from option
            Properties domainProperties = getDomainProperties(getOption(DOMAIN_PROPERTIES));
            //we give priority to the --adminport option
            domainProperties.remove(DomainConfig.K_ADMIN_PORT);
            //saving the login information happens inside this method
            createTheDomain(getDomainsRoot(), domainProperties);
        } catch (Exception e) {
            CLILogger.getInstance().printDetailMessage(e.getLocalizedMessage());
            throw new CommandException(getLocalizedString("CouldNotCreateDomain",
                    new Object[]{domainName}), e);
        }

    }

    /**
    Verify that the port is valid.
    Port must be greater than 0 and less than 65535.
    This method will also check if the port is in use.
    If checkPorts is false it does not throw an Exception if it is in use.
    @param portNum - the port number to verify
    @throws CommandException if Port is not valid
    @throws CommandValidationException is port number is not a numeric value.
     */
    private void verifyPortIsValid(String portNum)
            throws CommandException, CommandValidationException {

        final int portToVerify = convertPortStr(portNum);
        NetUtils.PortAvailability avail = NetUtils.checkPort(portToVerify);
        System.out.println(" *******************" + getClass().getName() + " *******************");
        switch(avail) {
            case illegalNumber:
                throw new CommandException(getLocalizedString("InvalidPortRange",
                        new Object[]{portNum}));

            case inUse:
                if(checkPorts)
                    throw new CommandException(getLocalizedString("PortInUseError",
                        new Object[]{(String) operands.firstElement(), portNum}));
                else
                    CLILogger.getInstance().printWarning(getLocalizedString("PortInUseWarning",
                        new Object[]{portNum}));
                break;
            case noPermission:
                throw new CommandException(getLocalizedString("NoPermissionForPortMsg",
                        new Object[]{portNum, (String) operands.firstElement()}));

            case unknown:
                throw new CommandException(getLocalizedString("UnknownPortMsg",
                        new Object[]{portNum}));

            case OK:
                CLILogger.getInstance().printDebugMessage("Port =" + portToVerify);
                break;
        }
    }

    /**
     * Converts the port string to port int
     * @param port - the port number
     * @return 
     * @throws CommandValidationExeption if port string is not numeric
     */
    private int convertPortStr(final String port)
            throws CommandValidationException {
        try {
            return Integer.parseInt(port);
        } catch (Exception e) {
            throw new CommandValidationException(
                    getLocalizedString("InvalidPortNumber",
                    new Object[]{port}));
        }
    }

    /**
    Verify that the portbase port is valid
    Port must be greater than 0 and less than 65535.
    This method will also check if the port is in used.
    @param portNum - the port number to verify
    @throws CommandException if Port is not valid
    @throws CommandValidationException is port number is not a numeric value.
     */
    private void verifyPortBasePortIsValid(String portName, int portNum)
            throws CommandValidationException {
        if (portNum <= 0 || portNum > PORT_MAX_VAL) {
            throw new CommandValidationException(getLocalizedString("InvalidPortBaseRange",
                    new Object[]{portNum, portName}));
        }
        if (checkPorts && !NetUtils.isPortFree(portNum)) {
            throw new CommandValidationException(getLocalizedString("PortBasePortInUse",
                    new Object[]{portNum, portName}));
        }
        CLILogger.getInstance().printDebugMessage("Port =" + portNum);
    }

    /** 
     *  Get domain properties from options
     *  @param propertyValues from options
     *  @return domain properties 
     *  @throws CommandException if cannot get properties
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
            final int index = propertyString.indexOf(Character.toString(EQUAL_SIGN));
            if (index == -1) {
                throw new CommandValidationException(getLocalizedString("InvalidPropertySyntax"));
            }
            final String propertyName = propertyString.substring(0, index);
            final String propertyValue = propertyString.substring(index + 1);
            propertyList.put(propertyName, propertyValue);
        }
        CLILogger.getInstance().printDebugMessage("domain properties = " + propertyList);
        return propertyList;
    }

    /** 
     *  create the domain
     *  @param domainPath - domain path to insert in domainConfig
     *  @param domainProperties - properties to insert in domainConfig
     *  @throws CommandException if domain cannot be created
     */
    private void createTheDomain(final String domainPath,
            Properties domainProperties)
            throws Exception {
        final Integer adminPort = Integer.valueOf(getOption(ADMIN_PORT));
        CLILogger.getInstance().printDetailMessage(getLocalizedString("UsingPort",
                new Object[]{"Admin", Integer.toString(adminPort)}));

        //
        // fix for bug# 4930684
        // domain name is validated before the ports
        //
        String domainFilePath = (domainPath + File.separator + domainName);        
        
        if (FileUtils.safeGetCanonicalFile(new File(domainFilePath)).exists()) {
            throw new CommandValidationException(
                getLocalizedString("DomainExists", new Object[]{domainName}));
        }

        final Integer instancePort = getPort(domainProperties,
                DomainConfig.K_INSTANCE_PORT,
                getOption(INSTANCE_PORT),
                Integer.toString(DEFAULT_INSTANCE_PORT),
                "HTTP Instance");

        final Integer jmsPort = getPort(domainProperties,
                DomainConfig.K_JMS_PORT, null,
                Integer.toString(DEFAULT_JMS_PORT), "JMS");

        /*  Bug# 4859518
        The authentication of jms broker will fail if the jms user is not in the 
        user database referred by jms broker. Setting the jms user and password to 
        "admin" which is always present in the user db. Once the mechanism to update
        the user db with given jms user/password is in place we should set the
        user specified values for jms user and password.
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

        //FIXTHIS: Currently the jmx admin port is used only for SE/EE where the DAS as
        //in addition to the http admin listener a jsr160 connector listener. This jmx connector
        //will be exposed to access the mbean API of the DAS. If we choose to expose the API
        //in PE, then this jsr160 listener will be added to the PE domain.xml and this will be 
        //needed. If we choose not to, then this should be moved into SE/EE specific code
        //(namely DomainsManager.createDomain).
        final Integer jmxPort = getPort(domainProperties,
                DomainConfig.K_JMX_PORT, null,
                Integer.toString(DEFAULT_JMX_PORT),
                "JMX_ADMIN");

        Boolean saveMasterPassword = getSaveMasterPassword(masterPassword);

        //System.out.println("adminPassword=" + adminPassword + " masterPassword=" + masterPassword + 
        //     " saveMasterPassword=" + saveMasterPassword);

        checkPortPrivilege(new Integer[]{adminPort, instancePort, jmsPort, orbPort, httpSSLPort,
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
                iiopMutualAuthPort, jmxPort,
                domainProperties);
        if (getOption(TEMPLATE) != null) {
            domainConfig.put(DomainConfig.K_TEMPLATE_NAME, getOption(TEMPLATE));
        }

        domainConfig.put(DomainConfig.K_VALIDATE_PORTS,
                Boolean.valueOf(checkPorts));
        /* comment out for V3 until profiles decision is taken */
        // setUsageProfile(domainConfig);
        domainConfig.put(DomainConfig.KEYTOOLOPTIONS, getOption(KEYTOOLOPTIONS));
        DomainsManager manager = new PEDomainsManager();

        manager.createDomain(domainConfig);
        //modifyInitialDomainXml();
        CLILogger.getInstance().printMessage(getLocalizedString("DomainCreated",
                new Object[]{domainName}));
        //checkAsadminPrefsFile();
        if (getBooleanOption(SAVELOGIN_OPTION)) {
            saveLogin(adminPort, adminUser, adminPassword, domainName);
        }
    }

    private void setUsageProfile(final DomainConfig dc) {
        String pp = getCLOption(PROFILE_OPTION);
        String source = getLocalizedString("ProfileUserSource");
        if (pp == null) {
            pp = getOption(PROFILE_OPTION);
            source = getLocalizedString("ProfileConfigFileSource");
        }
        if (pp == null) {
            /* Implementation note: This is the ONLY place where this value
             * is hardcoded. It is almost an assertion that the value is 
             * available. All Application Server installations will
             * make sure that there is a proper value for this variable
             * set in the configuration file. */
            pp = "developer";
            source = getLocalizedString("ProfileGlobalDefaultSource");
        }
        dc.put(DomainConfig.K_PROFILE, pp);
        final String[] args = new String[]{pp, source};
        String msg = getLocalizedString("UsingProfile", args);
        final String templateName = getOption(TEMPLATE);
        if (templateName != null) {
            msg = getLocalizedString("UsingTemplate", new String[]{templateName});
        }
        CLILogger.getInstance().printMessage(msg);
    }

    /** Saves the login information to the login store. Usually this is the file
     * ".asadminpass" in user's home directory.*/
    private void saveLogin(final int port, final String user, final String password, final String dn) {
        final CLILogger logger = CLILogger.getInstance();
        try {
            //by definition, the host name will default to "localhost" and entry is overwritten
            final LoginInfoStore store = LoginInfoStoreFactory.getStore(null);
            final LoginInfo login = new LoginInfo("localhost", port, user, password);
            if (store.exists(login.getHost(), login.getPort())) {
                //just let the user know that the user has chosen to overwrite the login information. This is non-interactive, on purpose
                final Object[] params = new Object[]{login.getHost(), "" + login.getPort()};
                final String msg = getLocalizedString("OverwriteLoginMsgCreateDomain", params);
                logger.printMessage(msg);
            }
            store.store(login, true);
            final Object[] params = new String[]{user, dn, store.getName()};
            final String msg = getLocalizedString("LoginInfoStoredCreateDomain", params);
            logger.printMessage(msg);
        } catch (final Exception e) {
            final Object[] params = new String[]{user, dn};
            final String msg = getLocalizedString("LoginInfoNotStoredCreateDomain", params);
            logger.printWarning(msg);
            if (logger.isDebug()) {
                logger.printExceptionStackTrace(e);
            }
        }
    }

    /**
     *  get port from from the properties option or default or free port
     *  @param properties - properties from command line
     *  @param key - key for the type of port
     *  @param portStr -
     *  @param defaultPort - default port to use 
     *  @name  - of port
     *  @throws CommandValidationException if error in retrieve port
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
            port = NetUtils.getFreePort();
            //don't understand why this is a printMessage not an Exception??
            //maybe there will always be a port ??
            if (portNotSpecified) {
                if (defaultPortUsed) {
                    CLILogger.getInstance().printDetailMessage(
                            getLocalizedString("DefaultPortInUse",
                            new Object[]{name, defaultPort, Integer.toString(port)}));
                } else {
                    CLILogger.getInstance().printDetailMessage(getLocalizedString("PortNotSpecified",
                            new Object[]{name,
                        Integer.valueOf(port)
                    }));
                }
            } else if (invalidPortSpecified) {
                CLILogger.getInstance().printDetailMessage(getLocalizedString("InvalidPortRangeMsg",
                        new Object[]{name,
                    Integer.valueOf(port)
                }));
            } else {
                CLILogger.getInstance().printDetailMessage(getLocalizedString("PortInUseError",
                        new Object[]{name,
                    Integer.valueOf(port)
                }));
            }
        } else if (defaultPortUsed) {
            CLILogger.getInstance().printDetailMessage(
                    getLocalizedString("UsingDefaultPort",
                    new Object[]{name, Integer.toString(port)}));
        } else {
            CLILogger.getInstance().printDetailMessage(
                    getLocalizedString("UsingPort",
                    new Object[]{name, Integer.toString(port)}));
        }


        if (properties != null) {
            properties.remove(key);
        }
        return Integer.valueOf(port);
    }

    /**
     * check if portbase option is specified.
     * portbase is mutually exclusive to adminport and domainproperties options
     * if portbase options is specfied and also adminport or domainproperties
     * is specified as well, then throw an exception
     */
    private boolean usePortBase() throws CommandValidationException {
        if (getOption(PORTBASE_OPTION) != null) {
            if (getCLOption(ADMIN_PORT) != null) {
                throw new CommandValidationException(getLocalizedString("MutuallyExclusiveOption",
                        new Object[]{ADMIN_PORT, PORTBASE_OPTION}));
            } else if (getOption(INSTANCE_PORT) != null) {
                throw new CommandValidationException(getLocalizedString("MutuallyExclusiveOption",
                        new Object[]{INSTANCE_PORT, PORTBASE_OPTION}));
            } else if (getOption(DOMAIN_PROPERTIES) != null) {
                throw new CommandValidationException(getLocalizedString("MutuallyExclusiveOption",
                        new Object[]{DOMAIN_PROPERTIES, PORTBASE_OPTION}));
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * check if any of the port values are below 1024.
     * if below 1024, then display a warning message.
     */
    private void checkPortPrivilege(final Integer[] ports) {
        for (Integer port : ports) {
            final int p = port.intValue();
            if (p < 1024) {
                CLILogger.getInstance().printWarning(getLocalizedString("PortPrivilege"));
                //display this message only once.
                //so break once this message is displayed.
                break;
            }
        }
    }

    /**
     *  This method is only called in CreateDomainCommand since create-domain command
     *  is a special case where user can type the enter/return key to accept the
     *  default value of master password  when prompted.
     **/
    private String getMasterPassword() throws CommandValidationException, CommandException {
        //get masterpassword from passwordfile option
        String mpassword = getPassword(MASTER_PASSWORD, "", "", false, false, false, false,
                null, null, false, false, false, false);

        if (mpassword != null) {
            return mpassword;
        }

        //If adminpassword is not prompted and masterpassword is not specified
        //in the passwordfile, then use the default masterpassword.
        //Get both ADMIN_PASSWORD and PASSWORD since both are accepted in the
        //passwordfile.  This is because ADMIN_PASSWORD is deprecated.
        if ((getOption(ADMIN_PASSWORD) != null || getOption(PASSWORD) != null)) {
            return DEFAULT_MASTER_PASSWORD;
        }

        //If the admin password was not provided in the passwordfile
        //(i.e. the password was prompted), then
        //prompt for the master password.
        mpassword = promptMasterPassword();
        if (isPasswordValid(mpassword)) {
            return mpassword;
        } else {
            throw new CommandValidationException(getLocalizedString("PasswordLimit",
                    new Object[]{MASTER_PASSWORD}));
        }
    }

    /**
     * If interactive is true, then prompt the user for the masterpassword.
     * If masterpassword is an empty string (by typing return/enter key)
     * then set masterpassword to the default value.
     * @return masterpassword
     * @throws CommandValidationException is interactive is false and the option
     * value is null or if the two values entered do not match.
     */
    private String promptMasterPassword() throws CommandValidationException {
        String optionValue = getInteractiveOption(MASTER_PASSWORD,
                getLocalizedString(
                "MasterPasswordWithDefaultPrompt"));
        if (optionValue.length() == 0) {
            optionValue = DEFAULT_MASTER_PASSWORD;
        }

        String optionValueAgain = getInteractiveOption(MASTER_PASSWORD,
                getLocalizedString(
                "MasterPasswordConfirmationWithDefaultPrompt"));
        if (optionValueAgain.length() == 0) {
            optionValueAgain = DEFAULT_MASTER_PASSWORD;
        }
        if (!optionValue.equals(optionValueAgain)) {
            throw new CommandValidationException(getLocalizedString("OptionsDoNotMatch",
                    new Object[]{MASTER_PASSWORD}));
        }
        return optionValue;
    }

    private void modifyInitialDomainXml() throws LifecycleException {
        // for each module implementing the @Contract DomainInitializer, extract
        // the initial domain.xml and insert it into the existing domain.xml
        Server server = new Server.Builder("dummylaunch").build();
        server.start();
        Habitat habitat = server.getHabitat();
        // Will always need DAS's name & config. No harm using the name 'server'
        // to fetch <server-config>
        com.sun.enterprise.config.serverbeans.Server serverConfig =
           habitat.getComponent(com.sun.enterprise.config.serverbeans.Server.class,
           "server");
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
        for (DomainInitializer inhabitant : habitat.getAllByContract(
            DomainInitializer.class)) {
            Container newContainerConfig = inhabitant.getInitialConfig(ctx);
            config.getContainers().add(newContainerConfig);
        }
        server.stop();
    }
}


