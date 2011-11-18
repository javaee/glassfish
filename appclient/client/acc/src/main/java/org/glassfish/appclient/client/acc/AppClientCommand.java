/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.glassfish.appclient.client.acc;

import com.sun.enterprise.module.bootstrap.BootException;
import com.sun.enterprise.util.LocalStringManager;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.glassfish.appclient.client.acc.config.ClientContainer;
import org.glassfish.appclient.client.acc.config.Property;
import org.glassfish.appclient.client.acc.config.Security;
import org.glassfish.appclient.client.acc.config.TargetServer;
import org.glassfish.enterprise.iiop.api.IIOPConstants;

/**
 * Implements the appclient command.
 *
 * @author tjquinn
 */
public class AppClientCommand {

    /**
     * Command-line option definitions
     */
    private static final String CLIENT = "-client";
    private static final String NAME = "-name";
    private static final String MAIN_CLASS = "-mainclass";
    private static final String TEXT_AUTH = "-textauth";
    private static final String XML_PATH = "-xml";
    private static final String ACC_CONFIG_XML = "-configxml";
    private static final String DEFAULT_CLIENT_CONTAINER_XML = "sun-acc.xml";
    // duplicated in com.sun.enterprise.jauth.ConfigXMLParser
    private static final String SUNACC_XML_URL = "sun-acc.xml.url";
    private static final String NO_APP_INVOKE = "-noappinvoke";
    //Added for allow user to pass user name and password through command line.
    private static final String USER = "-user";
    private static final String PASSWORD = "-password";
    private static final String PASSWORD_FILE = "-passwordfile";
    private static final String DASH = "-";
    private static final String TARGETSERVER = "-targetserver";

    private static final String lineSep = System.getProperty("line.separator");

    // XXX Replace with reference to v3 equiv of S1ASCtxFactory.IIOP_ENDPOINTS_PROPERTY
    private static final String IIOP_ENDPOINTS_PROPERTY = "com.sun.appserv.iiop.endpoints";

    private static LocalStringManager localStrings = new LocalStringManagerImpl(AppClientCommand.class);;

    private static final Logger _logger = Logger.getLogger(AppClientCommand.class.getName());

    protected final String[] args;

    protected String clientJarOrDirOrClassFile = null;
    protected String displayName = null;
    protected String mainClass = null;
    protected String xmlPath = null;
    protected String accConfigXml = DEFAULT_CLIENT_CONTAINER_XML;
    protected String jwsACCConfigXml = null;
    protected String[] appArgs;
    protected String classFileFromCommandLine = null;
    protected boolean useTextAuth = false;
    protected boolean runClient = true;

    protected String username = null;
    protected char[] password = null;

    protected String servers = null;

    private ClientContainer clientContainerFromConfigFile = null;

    /** loaded Class if user specified a .class file as the -client value */
    private Class clientClass = null;

    /**
     * Main method for the command.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new AppClientCommand(args).run();
        } catch (UserError ue) {
            ue.displayAndExit();
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }



    private AppClientCommand(String[] args) throws BootException, URISyntaxException {
        this.args = args;
    }


    private void run() throws UserError, FileNotFoundException, JAXBException {
        /*
         *Handle any command line arguments intended for the ACC (as opposed to
         *the client itself) and save the returned args intended for the client.
         */
//         String[] appArgs = processCommandLine(args);

         clientContainerFromConfigFile = processConfigFile();

         /*
          * Choose the target servers to use in preparing the config.
          */
         final TargetServer[] targetServers = TargetServerHelper.targetServers(
                 clientContainerFromConfigFile, servers);

         /*
          * Use the appropriate constructor based on how the user has indicated
          * the client to be run.
          */

         /*
          * See if the user specified a .class file as the -client.
          */
         AppClientContainer.Builder builder;
         if (isClientValueClassFile()) {
//             builder = configureForClassFile(targetServers, clientJarOrDirOrClassFile);
         } else {
//             builder = configureFor
         }


    }

    private boolean isClientValueClassFile() {
        return clientJarOrDirOrClassFile.endsWith(".class");
    }

//    private AppClientContainer.Builder configureForClassFile(
//            final TargetServer[] targetServers,
//            final String classFileName) throws UserError {
//        /*
//         * Try to load the class.
//         */
//        final String className = clientJarOrDirOrClassFile.substring(
//                0, clientJarOrDirOrClassFile.lastIndexOf(".class") + 1);
//        Class clientClass;
//        try {
//            clientClass = getClass().forName(className);
//        } catch (ClassNotFoundException ex) {
//            File classFile = new File(clientJarOrDirOrClassFile);
//            throw new UserError(localStrings.getLocalString(
//                    getClass(),
//                    "appclient.cannotFindClassFile",
//                    "Because -client did not appear, tried to use {0} as a class name but could not find {1}",
//                    new Object[] {className, classFile.getAbsolutePath()}));
//        }
//        AppClientContainer.Builder config = AppClientContainer.newBuilder(
//                targetServers);
//
//        return config;
//    }


    /**
     *Processes any command-line arguments, setting up static variables for use
     *later in the processing.
     *<p>
     *As side-effects, these variables may be assigned values:
     *<ul>
     *<le>clientJarOrDirOrClassFile
     *<le>displayName
     *<le>mainClass
     *<le>xmlPath
     *<le>accConfigXml
     *<le>guiAuth
     *<le>runClient
     *<le>classFileFromCommandLine
     *<le>servers
     *</ul>
     *@param args the command-line arguments passed to the ACC
     *@return arguments to be passed to the actual client (with ACC arguments removed)
     */
    private String[] processCommandLine(String[] args) throws UserError {
        List<String> clientArgs = new ArrayList<String>();

        AtomicInteger i = new AtomicInteger();
        String arg = null;

        // Parse command line arguments.
        if(args.length < 1) {
            usage();
        } else {
            while(i.get() < args.length) {
                arg = args[i.getAndIncrement()];
                if(arg.equals(CLIENT)) {
                    clientJarOrDirOrClassFile = getRequiredCommandOptionValue(args, CLIENT, i,
                            "appclient.clientWithoutValue",
                            "The -client option must be followed by a file spec for the client file");
                } else if (arg.equals(NAME)) {
                    displayName = getRequiredCommandOptionValue(args, NAME, i, 
                            "appclient.nameWithoutValue",
                            "The -name option must be followed by a display name to be used in identifying the app client to be run");
                    ensureAtMostOneOfNameAndMainClass();
                } else if(arg.equals(MAIN_CLASS)) {
                    mainClass = getRequiredCommandOptionValue(args, MAIN_CLASS, i, 
                            "appclient.mainClassWithoutValue",
                            "The -mainclass option must be followed by the fully-qualified name of the class to be run");
                    ensureAtMostOneOfNameAndMainClass();
                } else if(arg.equals(XML_PATH) ) {
                    xmlPath = getRequiredUniqueCommandOptionValue(args, XML_PATH, xmlPath, i, 
                            "appclient.xmlWithoutValue",
                            "The -xml option must be followed by the location of the configuration XML file");
                } else if(arg.equals(ACC_CONFIG_XML) ) {
                    accConfigXml = getRequiredUniqueCommandOptionValue(args, ACC_CONFIG_XML, accConfigXml, i, 
                            "appclient.accConfigXmlWithoutValue",
                            "The -configxml option must be followed by the location of the back-up configuration XML file");
                } else if(arg.equals(TEXT_AUTH)) {
                    // Overrides legacy auth.gui setting.
                    useTextAuth = true;
                    logOption(TEXT_AUTH);
                } else if(arg.equals(NO_APP_INVOKE)) {
                    runClient = false;
                    logOption(NO_APP_INVOKE);
                } else if(arg.equals(USER)) {
                    username = getRequiredCommandOptionValue(args, USER, i, 
                            "appclient.userWithoutValue",
                            "The -user option must be followed by a username");
//                    System.setProperty(LOGIN_NAME, userNameValue);
                } else if(arg.equals(PASSWORD)) {
                    password = getRequiredCommandOptionValue(args, PASSWORD, i, 
                            "appclient.passwordWithoutValue",
                            "The -password option must be followed by a password").toCharArray();
//                    System.setProperty(LOGIN_PASSWORD, passwordValue);
                } else if (arg.equals(PASSWORD_FILE)) {
                    String passwordFileValue = getRequiredCommandOptionValue(args, PASSWORD_FILE, i, 
                            "appclient.passwordFileWithoutValue",
                            "The -passwordfile option must be followed by the location of the password file");
                    try {
                        password = loadPasswordFromFile(passwordFileValue).toCharArray();
//                        System.setProperty(LOGIN_PASSWORD,
//                            loadPasswordFromFile(passwordFileValue));

                    } catch(IOException ex) {
                        throw new UserError(getLocalString(
                                "appclient.errorReadingFromPasswordFile",
                                "Error reading the password from the password file {0}",
                                 new Object[] {passwordFileValue}), ex);
                    }
                } else if (arg.equals(TARGETSERVER)) {
                    servers = getRequiredCommandOptionValue(args, TARGETSERVER, i,
                            "appclient.serverWithoutValue",
                            "The -targetserver option must be followed by host:port[,...]");
                } else {
                    clientArgs.add(arg);
                    logArgument(arg);
                }
            }


//            String uname = System.getProperty(LOGIN_NAME);
//            String upass = System.getProperty(LOGIN_PASSWORD);
//            if( uname != null || upass != null ) {
//                UsernamePasswordStore.set(uname, upass);
//            }



            /*The user may have asked
             *to execute a .class file by omitting the -client argument.  In this
             *case the user either specifies the name only of the class to run
             *using -mainclass or omits -mainclass and specifies the path to
             *the .class file as the first command-line argument that would
             *otherwise be passed to the actual client.  In this second
             *case, the first argument is removed from the list passed to the client.
             */
            if ((mainClass == null) && (clientJarOrDirOrClassFile == null)) {
                /*
                 *Make sure there is at least one argument ready to be passed
                 *to the client before trying
                 *to use the first one as the class file spec.
                 */
                if (clientArgs.size() > 0) {
                    classFileFromCommandLine = clientArgs.get(0);
                    clientArgs.remove(0);
                    logClassFileArgument(classFileFromCommandLine);
                } else {
                    usage();
                }
            }
        }
        logClientArgs(clientArgs);
        return clientArgs.toArray(new String[clientArgs.size()]);
    }

    private String loadPasswordFromFile(String fileName)
            throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(fileName));
            Properties props = new Properties();
            props.load(inputStream);
            return props.getProperty("PASSWORD");
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    private ClientContainer processConfigFile() throws FileNotFoundException, JAXBException {
        String configFilePath = xmlPath != null ? xmlPath : accConfigXml;
        InputStream is = new FileInputStream(configFilePath);
        JAXBContext context = JAXBContext.newInstance(ClientContainer.class);
        Unmarshaller u = context.createUnmarshaller();

        return (ClientContainer) u.unmarshal(is);
    }

    /**
     *Returns the next unused argument as a String value, so long as there is
     *a next argument and it does not begin with a dash which would indicate
     *the next argument.
     *@param position the mutable current position in the argument array
     *@param errorKey the message key for looking up the correct error if the
     *next argument cannot be used as a value
     */
    private String getRequiredCommandOptionValue(
            String [] args, String optionName, AtomicInteger position,
            String errorKey, String defaultErrorMsg) throws UserError {
        String result = null;
        /*
         *Make sure there is at least one more argument and that it does not
         *start with a dash.  Either of those cases means the user omitted
         *the required value.
         */
        if(position.get() < args.length && !args[position.get()].startsWith(DASH)) {
            result = args[position.getAndIncrement()];
        } else {
            throw new UserError(getLocalString(errorKey, defaultErrorMsg));
        }
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine(getLocalString("appclient.optionValueIs", optionName, result));
        }
        return result;
    }

    /**
     *Returns the next unused argument (if present and not prefixed with a dash)
     *as a string value as long as the current value of the argument expected
     *is not already set.
     *@param optionName the name of the option being processed
     *@param currentValue the current value of the argument
     *@param position the mutable current position in the argument array
     *@param errorKey the message key for looking up the correct error if the
     *next argument cannot be used as a value
     *@throws IllegalArgumentException
     */
    private String getRequiredUniqueCommandOptionValue(String [] args,
            String optionName, String currentValue, AtomicInteger position,
            String errorKey, String defaultMsg) throws UserError {
        if (currentValue != null) {
            throw new UserError(getLocalString("appclient.duplicateValue",
                    "Option {0} can be specified at most once and was already set to {1}",
                    optionName, currentValue));
        }
        return getRequiredCommandOptionValue(args, optionName, position, errorKey,
                defaultMsg);
    }
    /**
     *Makes sure that at most one of the -name and -mainclass arguments
     *appeared on the command line.
     *@throws IllegalArgumentException if both appeared
     */
    private void ensureAtMostOneOfNameAndMainClass() throws UserError {
        if (mainClass != null && displayName != null) {
            throw new UserError(getLocalString("appclient.mainclassOrNameNotBoth",
                    "Specify either -mainclass or -name but not both to identify the app client to be run"));
        }
    }

    /**
     *Reports that the specified option name has been processed from the command line.
     *@param optionName the String name of the option
     */
    private void logOption(String optionName) {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine(getLocalString("appclient.valuelessOptionFound",
                    "Option {0} processed",
                    optionName));
        }
    }

    private void logArgument(String arg) {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine(getLocalString("appclient.argumentValueFound",
                    "Argument value {0} processed",
                    arg));
        }
    }

    private void logClassFileArgument(String classFile) {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine(getLocalString("appclient.classFileUsed",
                    "Will use argument {0} as the .class file to be run",
                    classFile));
        }
    }

    private void logClientArgs(List<String> clientArgs) {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine(getLocalString("appclient.clientArgs",
                    "Arguments to be passed to the client: {0}",
                    clientArgs.toString()));
        }
    }

    private String getUsage() {
        return getLocalString(
            "main.usage",
            "appclient [ <classfile> | -client <appjar> | -jar <appjar> ] [-mainclass <appClass-name>|-name <display-name>] [-xml <xml>] [-textauth] [-user <username>] [-password <password>|-passwordfile <password-file>] [app-args]");
    }

    private void usage() {
        System.out.println(getUsage());
        System.exit(1);
    }

    private static String getLocalString(final String key, final String defaultMsg,
            final Object... args) {
        return localStrings.getLocalString(AppClientCommand.class, key, defaultMsg, args);
    }

    private static String getLocalString(final String key, final String defaultMsg) {
        return getLocalString(key, defaultMsg, new Object[0]);
    }


}
