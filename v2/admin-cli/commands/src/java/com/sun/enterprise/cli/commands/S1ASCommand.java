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

import com.sun.enterprise.cli.framework.*;
import com.sun.enterprise.deployment.client.ServerConnectionIdentifier;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.ReflectionException;

import com.sun.appserv.management.util.misc.ExceptionUtil;

import com.sun.enterprise.admin.jmx.remote.DefaultConfiguration;

import com.sun.enterprise.admin.servermgmt.RepositoryConfig;
import com.sun.enterprise.admin.servermgmt.RepositoryManager;
import com.sun.enterprise.admin.servermgmt.RepositoryException;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.OS;
import com.sun.enterprise.util.ASenvPropertyReader;

import com.sun.appserv.management.client.prefs.LoginInfo;
import com.sun.appserv.management.client.prefs.LoginInfoStore;
import com.sun.appserv.management.client.prefs.LoginInfoStoreFactory;


import java.util.Vector;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.net.MalformedURLException;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Properties;
import java.util.Enumeration;
import java.io.File;
import java.util.List;
import java.util.Iterator;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.InputStream;


/**
 *  This is an abstract class extended from Command.
 *  This class contains the method to create the JMX connector.
 *  @version  $Revision: 1.38 $
 */
public abstract class S1ASCommand extends Command
{

    public final static String JMX_PROTOCOL = "jmxmp";    
    public final static String TERSE = "terse";    
    public final static String INTERACTIVE = "interactive";
    public final static String PASSWORDFILE = "passwordfile";
    public final static String ECHO = "echo";    
    public final static String HOST = "host";    
    public final static String PORT = "port";    
    public final static String USER = "user";    
    public final static String PASSWORD = "password";    
    public final static String SECURE = "secure";    
    public final static String MAPPED_PASSWORD = "mappedpassword";
    public final static String OBJECT_NAME = "objectname";    
    public final static String ARGUMENTS = "arguments";    
    public final static String OPERATION = "operation";    
    public final static String PARAMS = "params";    
    public final static String PARAM_TYPES = "paramtypes";    
    public final static String RETURN_TYPE = "returntype";    
    public final static String DISPLAY_TYPE = "displaytype";
    public final static String PROPERTY = "property.";
    public final static String DOMAIN = "domain";
    
    public final static String ATTRIBUTE_LIST_CLASS = AttributeList.class.getName();
    public final static String PROPERTIES_CLASS = Properties.class.getName();
    public final static String BOOLEAN_CLASS = Boolean.class.getName();
    public final static String PRIMITIVE_BOOLEAN_CLASS = boolean.class.getName();
    public final static String STRING_ARRAY = (new String[]{}).getClass().getName();
    public final static String MAP_CLASS = Map.class.getName();
    public final static String LONG_CLASS = Long.class.getName();
    public final static String PRIMITIVE_LONG_CLASS = long.class.getName();
    public final static String INTEGER_CLASS = Integer.class.getName();
    public final static String PRIMITIVE_INTEGER_CLASS = int.class.getName();
    public final static String OBJECT_CLASS = Object.class.getName();
        
    public final static String PARAM_DELIMITER = ",";
    public final static String PARAM_VALUE_DELIMITER = "=";
    public final static String PROPERTY_DELIMITER = ":";
 
    protected final static String ASADMINPREFS = ".asadminprefs";
    private final static String ASADMINENV = "asadminenv.conf";

    /** these constants are for the hack for reading properties **/
    protected final static String SHORTHAND_DELIMETER = ":";
    protected final static char   ESCAPE_CHAR = '\\';
    protected static final String SET_DELIMETER = "=";

    protected final static String ENV_PREFIX = "AS_ADMIN_";
    
    protected static final String DEFAULT_NOT_DEPRECATED_PASSWORDFILE_OPTIONS = 
        "password|adminpassword|userpassword|masterpassword|aliaspassword|mappedpassword";
    
    // this variable ought to be renamed to avoid confusing it with a constant
    protected String NOT_DEPRECATED_PASSWORDFILE_OPTIONS =
        DEFAULT_NOT_DEPRECATED_PASSWORDFILE_OPTIONS;
    
    protected final static String COMMENT_PREFIX = "#";


    public final static String LIST_JMS_RESOURCES = "list-jms-resources";

    //these are global variables so that .asadminpref file does not need to be read
    //each time getUser or getPassword get invoked.
    private String userValue = null;
    private boolean warningDisplayed = false;


    public S1ASCommand()
    {
        //Use asenv.conf/bat to set up necessary system properties
        final ASenvPropertyReader reader = new ASenvPropertyReader(
            System.getProperty(SystemPropertyConstants.CONFIG_ROOT_PROPERTY));
        reader.setSystemProperties();
    }
    

    /**
     *  An abstract method that validates the options 
     *  on the specification in the xml properties file
     *  This method verifies for the correctness of number of 
     *  operands and if all the required options are supplied by the client.
     *  @return boolean returns true if success else returns false
     */
    public boolean validateOptions() throws CommandValidationException
    {
        setLoggerLevel();
        
        // echo the command if echo is turned on
        if (getBooleanOption(ECHO))
        {
            CLILogger.getInstance().printMessage(this.toString());
        }
        //Throw an error when password options are used on commandline
        // or ignore them when set through environment variables
        Map options = getOptions();
        Iterator optionNames = options.keySet().iterator();
        while (optionNames.hasNext())
        {
            final String optionKey = (String) optionNames.next();
            if (optionKey.matches(NOT_DEPRECATED_PASSWORDFILE_OPTIONS))
                validatePasswordOption(optionKey);
        }
        readAsadminEnvFile();
    	return true;
    }

    
    private void validatePasswordOption(String passwordOptionName)
                     throws CommandValidationException
    {
        //As per sun security policy reusable passwords should not be allowed on
        //command line (give an exception) or environment variables (ignore).
        if (getCLOption(passwordOptionName) != null)
            throw new CommandValidationException(getLocalizedString("PasswordsNotAllowedOnCommandLine",
                                                    new Object[]{passwordOptionName}));
        if ((MultiProcessCommand.getLocalEnvironmentValue(passwordOptionName) == null) &&
                (getENVOption(passwordOptionName) != null && getOption(PASSWORDFILE)==null))
        {
            CLILogger.getInstance().printWarning(getLocalizedString("PasswordsNotAllowedInEnvironment",
                                                        new Object[]{passwordOptionName}));
        }
    }

        /**
         * read asadminenv.conf file
         * and set the entries as options.
         * entries from asadminenv.conf file should have higher
         * precedence over the default options specified in the descriptor file
         **/
    
    private void readAsadminEnvFile()
    {
        final String cr = System.getProperty(SystemPropertyConstants.CONFIG_ROOT_PROPERTY);
        final File file = new File(cr, ASADMINENV);
        if (file.exists()) {
            CLILogger.getInstance().printDebugMessage("Reading asadminenv.conf file");
            InputStream is = null;
            try {
                is = new BufferedInputStream(new FileInputStream(file));
                final Properties prop = new Properties();
                prop.load(is);
                for (Enumeration en=prop.propertyNames(); en.hasMoreElements();)
                {
                    final String entry = (String)en.nextElement();
                    if (entry.startsWith(ENV_PREFIX))
                    {
                        final String optionName = entry.substring(ENV_PREFIX.length()).toLowerCase();
                        if (getCLOption(optionName) == null &&
                            getENVOption(optionName) == null)
                        {
                            final String optionValue = prop.getProperty(entry);
                            CLILogger.getInstance().printDebugMessage("asadminenv.conf: set the following options: " + optionName + "=" + optionValue);
                            setOption(optionName, optionValue);
                        }
                    }
                }
            } catch(final Exception e) {
                    //ignore exception, this could either mean that the file is
                    //corrupted or entries are corrupted but it should not impact
                    //the execution of the command.
                    //however print exception as a debug message
e.printStackTrace();
                CLILogger.getInstance().printDebugMessage(e.getLocalizedMessage());
            } finally {
                try {
                    if (is != null)
                        is.close();
                } catch(final Exception ignore){}
            }
        }
    }
    

    
    /**
     *  This method sets the logger level depending on the terse option.
     *  If terse is true, then the logger level is set to INFO.
     *  If terse is false, then the logger level is set to FINE.
     */
    protected void setLoggerLevel()
    {
        final boolean terse = getBooleanOption(TERSE);

        if (terse)
        {
            //set the logger level to INFO
            CLILogger.getInstance().setOutputLevel(java.util.logging.Level.INFO);
        }
        else
        {
            //set the logger level to FINE
            CLILogger.getInstance().setOutputLevel(java.util.logging.Level.FINE);
        }
    }
   

    /**
     *  This method returns the MBeanServerConnection to used to invoke the 
     *  MBean methods
     *  @param protocol - the protocol part of the URL. If null, defaults to jmxmp.
     *  @param host - the host part of the URL. If null, defaults to the local host
     *  name, as determined by InetAddress.getLocalHost().getHostName(). If it is a
     *  numeric IPv6 address, it can optionally be enclosed in square brackets [].
     *  @port - the port part of the URL. 
     *  @user - the user name for authenticating with MBeanServer
     *  @password - the password for authenticating with MBeanServer
     *  @return MBeanServerConnection 
     */
    protected MBeanServerConnection getMBeanServerConnection(String protocol, 
                                        String host, int port, 
                                        String user, String password) 
                                        throws CommandException
    {
        //Create a JMXMP connector client and
        //connect it to the JMXMP connector server
        final JMXServiceURL url = getJMXServiceURL(protocol, host, port);
        try
        {
            final JMXConnector jmxc = JMXConnectorFactory.connect(url);
  
            //Get an MBeanSErverConnection
            CLILogger.getInstance().printDebugMessage("Get an MBeanSErverConnection");
            return jmxc.getMBeanServerConnection();
        }
        catch(IOException ioe)
        {
            throw new CommandException(ioe);
        }
    }

    
    /**
     *  This method creates the JMXServiceURL
     *  @param protocol
     *  @param host
     *  @param port
     */
    private JMXServiceURL getJMXServiceURL(String protocol, String host, int port)
	throws CommandException
    {
        try
        {
            CLILogger.getInstance().printDebugMessage(
                "Create a JMXMP connector client and " +
                "connect it to the JMXMP connector server");

            final JMXServiceURL  url = new JMXServiceURL(protocol, host, port);
            CLILogger.getInstance().printDebugMessage("url = " + url.toString());
            return url;
        }
        catch(MalformedURLException mue)
        {
            throw new CommandException(mue);
        }
    }


    /**
     *  This method returns the MBeanServerConnection to used to invoke the 
     *  MBean methods via HTPP connector.
     *  @param host - the host part of the URL. If null, defaults to the local
     *  host name, as determined by InetAddress.getLocalHost().getHostName().
     *  If it is a numeric IPv6 address, it can optionally be enclosed in square
     *  brackets [].
     *  @port - the port part of the URL. 
     *  @user - the user name for authenticating with MBeanServer
     *  @password - the password for authenticating with MBeanServer
     *  @return MBeanServerConnection 
     */
    protected MBeanServerConnection getMBeanServerConnection(String host, int port, 
                                        String user, String password) 
	throws CommandException, CommandValidationException
    {
        try 
        {
            String jmxProtocol = null;
            
            if (getBooleanOption(SECURE)){
                jmxProtocol = DefaultConfiguration.S1_HTTPS_PROTOCOL;
            } else {
                jmxProtocol = DefaultConfiguration.S1_HTTP_PROTOCOL;
            }

            final JMXServiceURL url = new JMXServiceURL(jmxProtocol, host, port);
            final JMXConnector conn = JMXConnectorFactory.connect(url, 
                initEnvironment(user, password));
            return conn.getMBeanServerConnection();
        }
        catch (Exception e)
        {
            throw new CommandException(e);
        }
    }

    /**
     *  This method returns the ServerConnectionIdentifier to used to invoke the 
     *  MBean methods via HTTP(S) connector.
     *  @param host - the host part of the URL. If null, defaults to the local
     *  host name, as determined by InetAddress.getLocalHost().getHostName().
     *  If it is a numeric IPv6 address, it can optionally be enclosed in square
     *  brackets [].
     *  @port - the port part of the URL. 
     *  @user - the user name for authenticating with MBeanServer
     *  @password - the password for authenticating with MBeanServer
     *  @return ServerConnectionIdentifier
     */
    protected ServerConnectionIdentifier createServerConnectionIdentifier(
            String host, int port, String user, String password) 
	throws CommandException, CommandValidationException
    {
        ServerConnectionIdentifier conn = 
            new ServerConnectionIdentifier();//getHost(), getPort(), getUser(), getPassword());
        conn.setHostName(host);
        conn.setHostPort(port);
        conn.setUserName(user);
        conn.setPassword(password);

        if (getBooleanOption(SECURE)) {
            conn.setSecure(true);
        } else {
            conn.setSecure(false);
        }

        return conn;
    }


    /**
     *  This method initialize the environment for creating the JMXConnector.
     *  @return Map - HashMap of environemtn
     */
    private Map initEnvironment(String user, String password) 
        throws CommandValidationException, CommandException
    {
        final Map env = new HashMap();
        final String PKGS = "com.sun.enterprise.admin.jmx.remote.protocol";

        env.put(JMXConnectorFactory.PROTOCOL_PROVIDER_CLASS_LOADER,
                this.getClass().getClassLoader());
        env.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, PKGS);
        env.put(DefaultConfiguration.ADMIN_USER_ENV_PROPERTY_NAME, user);
        env.put(DefaultConfiguration.ADMIN_PASSWORD_ENV_PROPERTY_NAME, password);
        env.put(DefaultConfiguration.HTTP_AUTH_PROPERTY_NAME, 
		DefaultConfiguration.DEFAULT_HTTP_AUTH_SCHEME);
        return ( env );
    }

  
    /*
     * Returns the host option value
     * @return host returns host option value
     */
    protected String getHost()
    {
        return getOption(HOST);
    }


    /*
     * Returns the port option value
     * @return port returns port option value
     */
    protected int getPort() throws CommandValidationException
    {

        final String port = getOption(PORT);                
        return validatePort(port);
    }


    /*
     * Returns the user option value
     * @return user returns user option value
     * @throws CommandValidationException if the following is true:
     *  1.  --user option not on command line
     *  2.  user option not specified in environment
     *  3.  user option not specified in ASADMINPREFS file
     *  4.  user option not specified in .asadminpass file
     *  5. --interactive is false, and no prompting is possible
     */
    protected String getUser() throws CommandValidationException
    {
        if (getOption(USER) == null && userValue == null)
        {
            // read from .asadminpass
            userValue = getUserFromASADMINPASS();
            if (userValue != null)
                return userValue;
            
            // read from .asadminprefs
            userValue= getValuesFromASADMINPREFS(USER);
            if (userValue != null)
            {
                CLILogger.getInstance().printDebugMessage(
                                "user value read from " + ASADMINPREFS);
                return userValue;
            }
            else if (getBooleanOption(INTERACTIVE))
            { 
                //prompt for user
                try {
                    InputsAndOutputs.getInstance().getUserOutput().print(
                                        getLocalizedString("AdminUserPrompt"));
                    return InputsAndOutputs.getInstance().getUserInput().getLine();
                }
                catch (IOException ioe)
                {
                    throw new CommandValidationException(
                                getLocalizedString("CannotReadOption", 
                                                        new Object[]{"user"}));
                }
            }
        }
        else if (getOption(USER) !=  null)
            return getOption(USER);

        else if (userValue !=  null)
            return userValue;

        //if all else fails, then throw an exception
        throw new CommandValidationException(getLocalizedString("OptionIsRequired",
                                                                new Object[] {USER}));
    }


    /**
     *   get user or password from ASADMINPREFS file
     *   @params nameOfValue
     *   @return user or password value from .asadminprefs file 
     *   if file not found then return null
     */
    protected String getUserFromASADMINPASS() 
    {
        String userValue = null;
        int port;
        String host = getHost();
        if (host == null) return userValue;
        try
        {
            //will be null for start-domain/node-agent/appserv commands
            port = getPort();
        }
        catch(CommandValidationException cve)
        {
            // exception only for start-domain/node-agent/appserv commands
            // ignore the exception since we are trying to get user, not port
            // and dont read the .asadminpass
            return userValue;
        }

        // get the user value from .asadminpass
        try
        {
            final LoginInfoStore store = LoginInfoStoreFactory.getStore(null);
            if (store.exists(host, port)) 
            {
                LoginInfo login = store.read(host, port);
                userValue = login.getUser();
                if (userValue != null)
                {
                    CLILogger.getInstance().printDebugMessage(
                                    "user value read from " + store.getName());
                }
            }
        }
        catch(final Exception e) {
            final Object[] params = new String[] {host, "" + port};
            final String msg = getLocalizedString("LoginInfoCouldNotBeRead", params);
            CLILogger.getInstance().printWarning(msg);
            CLILogger.getInstance().printExceptionStackTrace(e);
        }
        return userValue;
    }
        
    
    /**
     *   get user or password from ASADMINPREFS file
     *   @params nameOfValue
     *   @return user or password value from .asadminprefs file 
     *   if file not found then return null
     */
    protected String getValuesFromASADMINPREFS(String nameOfValue) 
    {
        String returnVal = null;
        File file = null;
        try
        {
            file = checkForFileExistence(System.getProperty("user.home"), ASADMINPREFS);
        }
        catch (Exception e)
        {
            CLILogger.getInstance().printDebugMessage(e.getLocalizedMessage());            
                //if file does not exist or cannot be read, return null
            return returnVal;
        }
        InputStream is = null;
        String optionValue = null;
        try {
            is = new BufferedInputStream(new FileInputStream(file));
            final Properties prop = new Properties();
            prop.load(is);
            for (Enumeration en=prop.propertyNames(); en.hasMoreElements();)
            {
                final String entry = (String)en.nextElement();
                if (entry.startsWith(ENV_PREFIX))
                {
                    final String optionName = entry.substring(ENV_PREFIX.length()).toLowerCase();
                    if (optionName.equals(nameOfValue))
                    {
                        optionValue = prop.getProperty(entry);
                        break;
                    }
                }
            }
        }
        catch(final Exception e) {
            //ignore exception, this could either mean that the file is
            //corrupted or entries are corrupted but it should not impact
            //the execution of the command.
            //however print exception as a debug message
            CLILogger.getInstance().printDebugMessage(e.getLocalizedMessage());
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch(final Exception ignore){}
        }
        return optionValue;
    }


    /**
     * Returns the password option value. This is used by all asadmin commands that accept the --password
     * option.
     * @return password returns password option value
     */
    protected String getPassword() throws CommandValidationException, CommandException
    {
        //getPassword(optionName, allowedOnCommandLine, readPrefsFile, readPasswordOptionFromPrefs, readMasterPasswordFile, mgr, config,
        //promptUser, confirm, validate)
        return getPassword(PASSWORD, "AdminPasswordPrompt", "AdminPasswordConfirmationPrompt", 
                            true, true, false, false, null, null, true, false, false, true);       
    }
    
     
    protected boolean isPasswordValid(String passwd) {
        if (passwd.length() < 8) {
            return false;
        } else {
            return true;
        }
    }

    protected String getPassword(String optionName,               
        boolean allowedOnCommandLine,
        boolean readPrefsFile,
        boolean readPasswordOptionFromPrefs, 
        boolean readMasterPasswordFile, RepositoryManager mgr, RepositoryConfig config,
        boolean promptUser, boolean confirm, boolean validate, boolean displayWarning) 
            throws CommandValidationException, CommandException
    {
        return getPassword(optionName, "InteractiveOptionPrompt", "InteractiveOptionConfirmationPrompt",
            allowedOnCommandLine, readPrefsFile, readPasswordOptionFromPrefs, 
            readMasterPasswordFile, mgr, config, promptUser, confirm, validate, displayWarning);
    }
    
    /**
     *  this methods returns a password
     *  first it checks if the password option is specified on command line.
     *  if not, then it'll try to get the password from the password file and 
     *  optionally from the ./asadminprefs file.
     *  if all else fails, then prompt the user for the password if interactive=true.
     *  @param readPrefsFile indicates whether the preferences file should be searched 
     *  for the option.
     *  @param readPasswordOptionFromPrefs indicates whether the AS_ADMIN_PASSWORD option 
     *  should be used from the prefs file before prompting.
     *  @param readMasterPasswordFile indicates whether the password should be read from the 
     *  master password file (valid only for the master password)
     *  @param mgr is needed if readMasterPasswordFile is true and is used to locate the
     *  master password file.
     *  @param config is needed if readMasterPasswordFile is true and is used to locate
     *  the master password file.
     *  @param promptUser indicates whether the user should be prompted for the password.
     *  @param confirm indicate whether the password should be confirmed (i.e. the user is 
     *  prompted for it 2x and must enter the same value.
     *  @param validate indicates whether the password should be validated 
     *  @return admin password
     *  @throws CommandValidationException if could not get adminpassword option 
     */
    protected String getPassword(String optionName,       
        String promptMsg,
        String confirmationPromptMsg,
        boolean allowedOnCommandLine,
        boolean readPrefsFile,
        boolean readPasswordOptionFromPrefs, 
        boolean readMasterPasswordFile, RepositoryManager mgr, RepositoryConfig config,
        boolean promptUser, boolean confirm, boolean validate, boolean displayWarning) 
            throws CommandValidationException, CommandException
    {
        String passwordVal;
        //try to read the password from local environment 
        // which is set using export in multimode
        passwordVal = (String) MultiProcessCommand.getLocalEnvironmentValue(optionName);
        if (passwordVal != null)
            return passwordVal;
        //try to read the password from the password file
        if (getOption(PASSWORDFILE) != null) {
            loadPasswordFileOptions(optionName);
        }
        // read the password which was just read from the passwordfile,
        // making sure its not read from the environment which is not supported.
        passwordVal = getOtherOption(optionName);

        if (passwordVal == null) 
        {
            //Next try the preferences file .asadminpass
            if (readPrefsFile) 
            {
                // Read only when we are dealing with the --port not the 
                // --adminport (in the case of create-domain).
                if (getOption(PORT) != null && getHost() != null) 
                {
                    String host = getHost();
                    int port = getPort();
                    try
                    {
                        final LoginInfoStore store = LoginInfoStoreFactory.getStore(null);
                        if (store.exists(host, port)) 
                        {
                            LoginInfo login = store.read(host, port);
                            passwordVal = login.getPassword();
                            if (passwordVal != null)
                            {
                                CLILogger.getInstance().printDebugMessage(
                                        "password value read from " + store.getName());
                            }
                        }
                    }
                    catch(final Exception e) 
                    {
                        final Object[] params = new String[] {host, "" + port};
                        final String msg = getLocalizedString("LoginInfoCouldNotBeRead", params);
                        CLILogger.getInstance().printWarning(msg);
                        CLILogger.getInstance().printExceptionStackTrace(e);
                    }
                }
                // now read from the .asadminprefs file
                if (passwordVal == null)
                {
                    passwordVal = getValuesFromASADMINPREFS(optionName);
                    if (passwordVal != null)
                    {
                        CLILogger.getInstance().printDebugMessage(
                                "password value read from " + ASADMINPREFS);
                    }
                }
            }
            if (passwordVal == null) 
            {     
                //Next try the password element of the preferences file
                if (readPrefsFile && readPasswordOptionFromPrefs) 
                {
                    passwordVal = getValuesFromASADMINPREFS(PASSWORD);
                }
                if (passwordVal == null) 
                {                 
                    //Next try the master password file
                    if (readMasterPasswordFile && mgr != null && config != null) 
                    {
                        try 
                        {
                            passwordVal = mgr.readMasterPasswordFile(config);
                        } catch (RepositoryException ex) 
                        {
                            throw new CommandException(ex);
                        }
                    }
                    if (passwordVal == null) 
                    {                    
                        //Finally prompt the user if all else fails. A confimation indicates that
                        //the user must enter the password 2x to confirm its value.
                        if (promptUser) 
                        {
                            if (confirm) 
                            {
                                passwordVal = getInteractiveOptionWithConfirmation(optionName,
                                                                                   promptMsg,
                                                                                   confirmationPromptMsg,
                                                                                   validate);
                            } else 
                            {
                                passwordVal = getInteractiveOption(optionName, 
                                    getLocalizedString(promptMsg, new Object[] {optionName})); 
                            }
                        }	
                    }
                }
            }
        }
        //Validate the password 
        if (validate && passwordVal != null && !isPasswordValid(passwordVal)) 
        {
            throw new CommandValidationException(getLocalizedString("PasswordLimit",
                                                                    new Object[]{optionName}));
        }
        return passwordVal;
    }
    
    /**
     * If interactive is true, then prompt the user for the option value. The
     * user is then prompted to enter the option (i.e. a password) again and the
     * two values must match.
     * @return option value
     * @param validatePassword -- true if the option should be validated as a 
     * password. This gives us the ability to fail before prompting twice
     * @param optionName - name of option.
     * @throws CommandValidationException is interactive is false and the option
     * value is null or if the two values entered do not match.
     */
    private String getInteractiveOptionWithConfirmation(final String optionName, final String promptMsg,
        final String confirmationPromptMsg, final boolean validatePassword) 
        throws CommandValidationException
    {
        final String prompt = getLocalizedString(promptMsg, new Object[] {optionName});
        final String confirmationPrompt = getLocalizedString(confirmationPromptMsg,
                                                             new Object[] {optionName});
        
        String optionValue = getInteractiveOption(optionName, prompt);            
        if (validatePassword && !isPasswordValid(optionValue)) {
            if (promptMsg.equals("AdminPasswordPrompt")) {
                final String adminPassword = getLocalizedString("AdminPassword");
                throw new CommandValidationException(getLocalizedString("PasswordLimit",
                                                                        new Object[]{adminPassword}));
            }
            throw new CommandValidationException(getLocalizedString("PasswordLimit",
                new Object[]{optionName}));
        }
        String optionValueAgain = getInteractiveOption(optionName, confirmationPrompt);
        if (!optionValue.equals(optionValueAgain)) {
            throw new CommandValidationException(getLocalizedString("OptionsDoNotMatch",
                new Object[] {optionName}));
        }
        return optionValue;
    }
        
    /**
     * If interactive is true, then prompt the user for the option value.
     * @param optionName - name of option.
     * @param prompt -- the string that will be used to prompt the user
     * @throws CommandException if option value cannot be read in interactive mode.
     * @throws CommandValidationException is interactive is false and the option
     * value is null.
     * @return option value
     */
    protected String getInteractiveOption(String optionName, String prompt) 
        throws CommandValidationException
    {
        //String optionValue = getOption(optionName);
        String optionValue;
        //if option value is null and interactive option is true
        //then prompt the user for the password.
        //if (optionValue == null && getBooleanOption(INTERACTIVE) )
        if (getBooleanOption(INTERACTIVE))
        {
            try 
            {
                InputsAndOutputs.getInstance().getUserOutput().print(prompt);
                InputsAndOutputs.getInstance().getUserOutput().flush();
                optionValue = new CliUtil().getPassword();
            }
            catch (java.lang.NoClassDefFoundError e)
            {
                optionValue = readInput();
            }
            catch (java.lang.UnsatisfiedLinkError e)
            {
                optionValue = readInput();
            }
            catch (Exception e)
            {
                throw new CommandValidationException(e);
            }
        }
        //else if ((optionValue == null) && !getBooleanOption(INTERACTIVE))
        else
            throw new CommandValidationException(getLocalizedString("OptionIsRequired",
                                                                  new Object[] {optionName}));
        return optionValue;
    }

    
    /**
     *   Get input from user.
     */
    protected String readInput()
    {
        try {
            return InputsAndOutputs.getInstance().getUserInput().getLine();
        }
        catch (IOException ioe)
        {
            return null;
        }
    }
    
    

    
    /**
     *   Load the passwords from the password file. 
     *   This method should be called before checkInteractiveOption();
     */
    private void loadPasswordFileOptions(String optionName) throws CommandException
    {
        final String passwordFileName = getOption(PASSWORDFILE);
        File file = checkForFileExistence(null, passwordFileName);
        boolean displayWarning = false;
        InputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(file));
            final Properties prop = new Properties();
            prop.load(is);
            for (Enumeration en=prop.propertyNames(); en.hasMoreElements();)
            {
                final String entry = (String)en.nextElement();
                if (entry.startsWith(ENV_PREFIX))
                {
                    final String optionFromFile = entry.substring(ENV_PREFIX.length()).toLowerCase();
                    displayWarning = !optionFromFile.matches(NOT_DEPRECATED_PASSWORDFILE_OPTIONS) ||
                                     displayWarning;
                    if (optionFromFile.equalsIgnoreCase(optionName))
                    {
                        final String optionValue = prop.getProperty(entry);
                        setOption(optionName, optionValue);
                    }
                }
            }
        }
        catch(final Exception e) {
            throw new CommandException(e);
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch(final Exception ignore){}
        }
        if (displayWarning && !warningDisplayed)
        {
            CLILogger.getInstance().printWarning(getLocalizedString(
                                                     "DeprecatedOptionsFromPasswordfile"));
            warningDisplayed = true;
        }
    }


    /**
     *  Check for the existence of the file in the file system
     *  @param parent - the parent directory containing the file
     *  @param fileName - the name of the file to check for existence
     *  @return File handler
     */
    protected File checkForFileExistence(String parent, String fileName) 
        throws CommandException
    {
        if (fileName == null) return null;
        File file = null;
        if (parent == null)
            file = new File(fileName);
        else
            file = new File(parent, fileName);
        if ( file.canRead() == false )
        {
            throw new CommandException(getLocalizedString("FileDoesNotExist",
                                                          new Object[] {fileName}));
        }
        return file;
    }


    /**
     * Returns the Object name string from the properties
     * @return Object name else returns null
     */
    protected String getObjectName() throws CommandException
    {
        final String objectName = (String)((Vector)getProperty(OBJECT_NAME)).get(0);
        final String objectNameStr = replacePattern(objectName);
        CLILogger.getInstance().printDebugMessage("Object Name = [" + 
						  objectNameStr + "]");
        return objectNameStr;
    }

    
    /*
     * Returns the Params from the properties
     * @return params returns params
     */
    protected Object[] getParamsInfo()throws CommandException, CommandValidationException
    {
        final Vector params = (Vector) getProperty(PARAMS);
        final String[] typesInfo = getTypesInfo();
        Object[] paramsInfo = params==null?null:new Object[params.size()];

        if(typesInfo != null && paramsInfo != null && paramsInfo.length != typesInfo.length)
        {
            throw new CommandException(getLocalizedString("BadDescriptorXML"));
        }
        for (int i = 0; params != null && i < params.size(); i++)
        {
            String paramStr = (String) params.get(i);
			
            CLILogger.getInstance().printDebugMessage("Types Info ["+i+"] = " + typesInfo[i]);
            //CLILogger.getInstance().printDebugMessage("ATTRIBUTE_LIST_CLASS = " + 
            //					      this.ATTRIBUTE_LIST_CLASS);
            if (typesInfo[i].equals(ATTRIBUTE_LIST_CLASS))
            {
                paramsInfo[i] = getAttributeList(paramStr);
            }
            else if (typesInfo[i].equals(PROPERTIES_CLASS))
            {
                paramsInfo[i] = createPropertiesParam((String)params.get(i));
            }
            else if (typesInfo[i].equals(BOOLEAN_CLASS) ||
                     typesInfo[i].equals(PRIMITIVE_BOOLEAN_CLASS))
            {
                if (createBooleanVal((String)params.get(i)) != null)
                    paramsInfo[i] = createBooleanVal((String)params.get(i));
            }
            else if (typesInfo[i].equals(LONG_CLASS) ||
                     typesInfo[i].equals(PRIMITIVE_LONG_CLASS))
            {
                if (createLongVal((String)params.get(i)) != null)
                    paramsInfo[i] = createLongVal((String)params.get(i));
            }
            else if (typesInfo[i].equals(INTEGER_CLASS) ||
                     typesInfo[i].equals(PRIMITIVE_INTEGER_CLASS))
            {
                if (createIntegerVal((String)params.get(i)) != null)
                    paramsInfo[i] = createIntegerVal((String)params.get(i));
            }
            else if (typesInfo[i].equals(STRING_ARRAY))
            {
                CLILogger.getInstance().printDebugMessage("Creating String Array");
                paramsInfo[i] = createStringArrayParam((String)params.get(i));
            }
            else if (typesInfo[i].equals(MAP_CLASS))
            {
                CLILogger.getInstance().printDebugMessage("Creating Map Class");
                paramsInfo[i] = createMapParam(paramStr);
            }
            else if (typesInfo[i].equals(OBJECT_CLASS))
            {
                CLILogger.getInstance().printDebugMessage("Creating Object Class");
                paramsInfo[i] = (Object) replacePattern(paramStr);
                CLILogger.getInstance().printDebugMessage("param value = " + paramsInfo[i]);
            }
            else
            {
                CLILogger.getInstance().printDebugMessage((String) params.get(i));
                paramsInfo[i] = replacePattern((String)params.get(i));
            
                CLILogger.getInstance().printDebugMessage("ParamsInfo = " + paramsInfo[i]);
            }
        }
        return paramsInfo;
    }

    
    /*
     * Returns the Param types from the properties
     * @return paramTypes returns param types
     */
    protected String[] getTypesInfo()
    {
        Vector typesList = (Vector) getProperty(PARAM_TYPES);
        String types[] = typesList==null?null:new String[typesList.size()];
        CLILogger.getInstance().printDebugMessage("Types = ");
        for (int i = 0; typesList != null && i < typesList.size(); i++)
        {
            types[i] = (String) typesList.get(i);
            CLILogger.getInstance().printDebugMessage(types[i]+",");
        }
        CLILogger.getInstance().printDebugMessage("");
        return types;
    }

    
    /*
     * Returns the Operation name from the properties
     * @return operationName returns operation name
     */
    protected String getOperationName() throws CommandException
    {
        String operationName = (String) ((Vector) getProperty(OPERATION)).get(0);
        String operationNameStr = replacePattern(operationName);
        CLILogger.getInstance().printDebugMessage("OperationName = " + operationNameStr);
        return operationNameStr;
    }

    
    /*
     * Returns the Return type from the properties
     * @return returnType returns return type
     */
    protected String getReturnType()
    {
        String returnType = (String) ((Vector) getProperty(RETURN_TYPE)).get(0);
        CLILogger.getInstance().printDebugMessage("ReturnType = " + returnType);
        return returnType;
    }

    
    /*
     * Formulate and Returns the attributes list from the given string
     * @return AttributeList returns attributes list
     */
    private AttributeList getAttributeList(String paramStr) 
        throws CommandException
    {
        AttributeList attrList = new AttributeList();
        StringTokenizer paramsTokenizer = new StringTokenizer(paramStr, PARAM_DELIMITER);
        int size = paramsTokenizer.countTokens();
        for (int i = 0; i < size; i++)
        {
            final String nameValue = paramsTokenizer.nextToken();
            final String nameValueStr = replacePattern(nameValue);
            if (nameValueStr == null) continue;
            final int index = getDelimeterIndex(nameValueStr, PARAM_VALUE_DELIMITER, 0);
            final String attrName = nameValueStr.substring(0, index);
            final String attrValue = nameValueStr.substring(index+1);
            CLILogger.getInstance().printDebugMessage("**Attr Name = " + attrName + 
                                                      ", Value = " +  attrValue);
            attrList.add( new Attribute(attrName, attrValue) );
        }
        return attrList;
    }


    /**
     * Formulate and Returns Properties from the given string
     * @return Properties
     */
    protected Properties createPropertiesParam(String propertyVal)
        throws CommandException, CommandValidationException
    {
        final String propertyStr = replacePattern(propertyVal);
        if (propertyStr == null) return null;
        Properties properties = new Properties();
        final CLITokenizer propertyTok = new CLITokenizer(propertyStr, PROPERTY_DELIMITER);
        while (propertyTok.hasMoreTokens()) {
            final String nameAndvalue = propertyTok.nextToken();
            final CLITokenizer nameTok = new CLITokenizer(nameAndvalue, PARAM_VALUE_DELIMITER);
            if (nameTok.countTokens() == 2)
            {
                properties.setProperty(nameTok.nextTokenWithoutEscapeAndQuoteChars(),
                                       nameTok.nextTokenWithoutEscapeAndQuoteChars());
            }
            else
            {
                throw new CommandValidationException(getLocalizedString("InvalidPropertySyntax"));
            }
        }
        return properties;
    }


    /**
     * Formulate and Returns Map class from the given string
     * @return Map
     */
    private Map createMapParam(final String paramVal)
        throws CommandException, CommandValidationException
    {
        if (paramVal == null) return null;
        Map mapParam = new HashMap();
        try {
            StringTokenizer paramsTokenizer = new StringTokenizer(paramVal,
                                                                 PARAM_DELIMITER);
            int size = paramsTokenizer.countTokens();
            for (int ii = 0; ii < size; ii++)
            {
                final String nameValue = paramsTokenizer.nextToken();
                final String nameValueStr = replacePattern(nameValue);

                if (nameValueStr == null) continue;
                
                if (nameValueStr.startsWith(PROPERTY)) {
                        //if it starts with "property." then it is a
                        //a property value.
                        //need to add 1 to the length to accomodate the "="
                    String propertyStr = nameValueStr.substring(PROPERTY.length()+1);
                        //make sure that there is a property value
                    if (propertyStr.length()>0) {
                        final CLITokenizer propertyTok = new CLITokenizer(propertyStr, PROPERTY_DELIMITER);
                        while (propertyTok.hasMoreTokens()) {
                            final String nameAndvalue = propertyTok.nextToken();
                            final CLITokenizer nameTok = new CLITokenizer(nameAndvalue, PARAM_VALUE_DELIMITER);
                            if (nameTok.countTokens() == 2)
                            {
                                    // need to prepend "property" and save to map
                                    // this is how AMX takes property values
                                mapParam.put(PROPERTY+nameTok.nextTokenWithoutEscapeAndQuoteChars(),
                                         nameTok.nextTokenWithoutEscapeAndQuoteChars());
                            }
                            else
                            {
                                throw new CommandValidationException(getLocalizedString("InvalidPropertySyntax"));
                            }
                        }
                        
                    }
                    
                } else {
                    
                    final int index = getDelimeterIndex(nameValueStr,
                                                        PARAM_VALUE_DELIMITER, 0);
                    if (index > 0) {
                        final String mapKey = nameValueStr.substring(0, index);
                        final String mapValue = nameValueStr.substring(index+1);
                            //do not add to map if value is empty
                        if (mapValue.length() > 0) 
                        {
                            CLILogger.getInstance().printDebugMessage("**Map Key = " +
                                                                      mapKey +
                                                                      ", Value = " +
                                                                      mapValue);
                            mapParam.put( mapKey, mapValue);
                        }
                        
                    }
                }
                
            }
        }
        catch (Exception e) {
            throw new CommandException(e);
        }
        return mapParam;
    }


    /**
     * Formulate and Returns Boolean from the given string
     * @param booleanVal
     * @return Boolean
     */
    private Boolean createBooleanVal(String booleanVal)
        throws CommandException
    {
        String booleanStr = replacePattern(booleanVal);
        return Boolean.valueOf(booleanStr);
    }


    /**
     * Formulate and Returns Long from the given string
     * @param longVal
     * @return Long
     */
    private Long createLongVal(String longVal)
        throws CommandException
    {
        String longStr = replacePattern(longVal);
        return Long.valueOf(longStr);
    }

    
    /**
     * Formulate and Returns Integer from the given string
     * @param integerVal
     * @return Integer
     */
    private Integer createIntegerVal(String integerVal)
        throws CommandException
    {
        String integerStr = replacePattern(integerVal);
        return Integer.valueOf(integerStr);
    }

    
    /**
     * Formulate and Returns StringArray from the given string
     * @return Properties
     */
    protected String[] createStringArrayParam(String str)
	throws CommandException
    {
        final String strArrayVal = replacePattern(str);
        CLILogger.getInstance().printDebugMessage("strArrayVal value = \""+ strArrayVal + "\"");
        if ((strArrayVal == null) || (strArrayVal.equals("")) || (strArrayVal.equals("null")))
            return null;

        final CLITokenizer cliTokenizer = new CLITokenizer(strArrayVal, PROPERTY_DELIMITER);
        String[] strArray = new String[cliTokenizer.countTokens()];
        int ii=0;
        while (cliTokenizer.hasMoreTokens()) 
        {
            strArray[ii++] = cliTokenizer.nextTokenWithoutEscapeAndQuoteChars();
        }
        return strArray;
    }

    
    public void displayExceptionMessage(Exception e) throws CommandException
    {
        //get the root cause of the exception
        Throwable rootException = ExceptionUtil.getRootCause(e);

        if (rootException.getLocalizedMessage() != null)
            CLILogger.getInstance().printDetailMessage(rootException.getLocalizedMessage());
        throw new CommandException(getLocalizedString("CommandUnSuccessful",
                                                          new Object[] {name} ), e);
    }
    


    /**
     *  handles return value from mbean
     *  @param returnval
     */
    protected void handleReturnValue(Object returnval)
    {
        if (returnval == null)
            return;
        Class cl = returnval.getClass();
        if (cl.isArray())
        {
            if (cl == new ObjectName[0].getClass())
            {
                final ObjectName[] objs = (ObjectName[])returnval;
                if (objs.length == 0)
                {
                    CLILogger.getInstance().printDetailMessage(
                                                getLocalizedString("NoElementsToList"));
                    return;
                }

                final String displayType = (String)((Vector)getProperty(DISPLAY_TYPE)).get(0);
                for (int ii=0; ii<objs.length; ii++)
                {
                    ObjectName objectName = (ObjectName)objs[ii];
                    CLILogger.getInstance().printDebugMessage("ObjectName = " + objectName);
                    CLILogger.getInstance().printMessage(objectName.getKeyProperty(displayType));
                }
            }
            else
            {
                final Object[] objs = (Object[])returnval;
                if (objs.length == 0)
                {
                    CLILogger.getInstance().printDetailMessage(
                                                getLocalizedString("NoElementsToList"));
                    return;
                }

                for (int ii=0; ii<objs.length; ii++)
                {
                    CLILogger.getInstance().printMessage(
                        objs[ii].toString());
                }
            }
        }
        else if (cl.getName().equals(List.class.getName()) || returnval instanceof List)
        {
            final List returnList = (List)returnval;
            if (returnList.size() > 0)
            {
                for (int i = 0; i<returnList.size(); i++)
                {
                    CLILogger.getInstance().printMessage(
                                    returnList.get(i).toString());
                }
            }
            else
                CLILogger.getInstance().printDebugMessage(
                                        "Return list is empty");
        }
        else if(cl.getName().equals("com.sun.enterprise.admin.servermgmt.RuntimeStatus"))
        {
            CLILogger.getInstance().printMessage(
                returnval.toString());
        }
    }
    
    /*
     * check if port number if valid
     */
    protected int validatePort(final String port) throws CommandValidationException
    {
        int portNum = -1;
        try {
            portNum = Integer.parseInt(port);
        }
        catch (NumberFormatException nfe) {
            throw new CommandValidationException(getLocalizedString("InvalidPortNumber",
                                                 new Object[] {port}));
        }
        if (portNum < 0 || portNum > 65535)
            throw new CommandValidationException(getLocalizedString("InvalidPortRangeMsg"));
        return portNum;
    }

}
