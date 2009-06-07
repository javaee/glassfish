/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 
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

package org.glassfish.appclient.client.acc.agent;

import com.sun.enterprise.deployment.node.SaxParserHandler;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.universal.glassfish.TokenResolver;
import com.sun.enterprise.util.LocalStringManager;
import com.sun.enterprise.util.LocalStringManagerImpl;
import java.io.BufferedInputStream;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.instrument.Instrumentation;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import org.glassfish.appclient.client.AppClientFacadeInfo;
import org.glassfish.appclient.client.acc.ACCClassLoader;
import org.glassfish.appclient.client.acc.ACCLogger;
import org.glassfish.appclient.client.acc.AgentArguments;
import org.glassfish.appclient.client.acc.AppClientCommand;
import org.glassfish.appclient.client.acc.AppClientContainer;
import org.glassfish.appclient.client.acc.AppClientContainer.Builder;
import org.glassfish.appclient.client.acc.AppclientCommandArguments;
import org.glassfish.appclient.client.acc.CommandLaunchInfo;
import org.glassfish.appclient.client.acc.CommandLaunchInfo.ClientLaunchType;
import org.glassfish.appclient.client.acc.TargetServerHelper;
import org.glassfish.appclient.client.acc.UserError;
import org.glassfish.appclient.client.acc.Util;
import org.glassfish.appclient.client.acc.config.ClientContainer;
import org.glassfish.appclient.client.acc.config.ClientCredential;
import org.glassfish.appclient.client.acc.config.LogService;
import org.glassfish.appclient.client.acc.config.TargetServer;
import org.glassfish.appclient.client.acc.config.util.XML;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Agent which prepares the ACC before the VM launches the selected main program.
 * <p>
 * This agent gathers processes agent arguments, supplied either by the
 * appclient script or the end-user (when entering a java command directly),
 * and processes those arguments.  The primary purpose is to:
 * <ol>
 * <li>identify the main class that the Java launcher has decided to start,
 * <li>create and initialize a new app client container instance, asking the
 * ACC to load and inject the indicated main class in the process <b>if and only if</b>
 * the main class is not the AppClientCommand class.
 * </ol>
 * Then the agent is done.  The java launcher and the VM see to it that the main class's
 * main method is invoked.
 *
 * @author tjquinn
 */
public class AppClientContainerAgent {

    private static CommandLaunchInfo launchInfo = null;

    private static AppclientCommandArguments appClientCommandArgs = null;

    private static Logger logger = Logger.getLogger(AppClientContainerAgent.class.getName());

    private static LocalStringManager localStrings = new LocalStringManagerImpl(AppClientContainerAgent.class);

    private static AppClientContainer acc = null;

    private static URI installRootURI = null;

    public static void premain(String agentArgsText, Instrumentation inst) {
        try {
            long now = System.currentTimeMillis();
            JavaVersion javaVersion = new JavaVersion();
            if (javaVersion.asInt() < 16) {
                throw new UserError(localStrings.getLocalString(
                        AppClientContainerAgent.class,
                        "main.badVersion",
                        "Current Java version {0} is too low; {1} or later required",
                        new Object[] {javaVersion.versionString, "1.6"}));
            }
            AgentArguments agentArgs = AgentArguments.newInstance(agentArgsText);

            List<String> effectiveCommandLineArgs = new ArrayList<String>();

            /*
            * If the agent arguments includes an args file specification, then
            * open it and read the command-line arguments from the file.
            */
//            final String argFilePath = agentArgs.namedValues().getProperty(
//                    AppClientArgumentsFile.AGENT_ARGS_FILE_PROPERTY);
//            if (argFilePath != null) {
//
//                final String argsFileFormat = agentArgs.namedValues().getProperty(
//                                AppClientArgumentsFile.AGENT_ARGS_FORMAT_PROPERTY);
//
//                AppClientArgumentsFile argsFile = AppClientArgumentsFile
//                        .newInstance(argFilePath, argsFileFormat);
//
//                effectiveCommandLineArgs.addAll(
//                        skipJVMArgs(argsFile.getArguments()));
//            }

            /*
             * Add any arguments specified to the agent to the end of the list so
             * settings specified as agent arguments override settings on the
             * command line.
             */
            effectiveCommandLineArgs.addAll(agentArgs.unnamedValues());



            AppclientCommandArguments appClientCommandArgs = AppclientCommandArguments
                    .newInstance(effectiveCommandLineArgs);

            if (appClientCommandArgs.isUsage() || appClientCommandArgs.isHelp()) {
                usage(0);
            }
            /*
             * Process the agent arguments which include most of the appclient script
             * arguments.
             */
            launchInfo = CommandLaunchInfo.newInstance(agentArgsText);

            /*
             * Handle the legacy env. variable APPCPATH.  
             */
            ACCClassLoader loader = initClassLoader(System.getenv("APPCPATH"));
//            ACCModulesManager.initialize(loader);
            Thread.currentThread().setContextClassLoader(loader);


            if (launchInfo.getClientLaunchType() != ClientLaunchType.UNKNOWN) {
                appClientCommandArgs = launchInfo.getAppclientCommandArguments();

                /*
                 * Load the ACC configuration XML file.
                 */
                initInstallRootProperty();
                ClientContainer clientContainer = readConfig(
                        appClientCommandArgs.getConfigFilePath(), loader);

                /*
                 * Decide what target servers to use.  This combines any
                 * specified on the command line with any in the config file's
                 * target-server elements as well as any set in the properties
                 * of the config file.
                 */
                final TargetServer[] targetServers = TargetServerHelper.targetServers(
                        clientContainer,
                        appClientCommandArgs.getTargetServer());

                /*
                 * Get the builder.  Doing so correctly involves merging
                 * the configuration file data with some of the command line and
                 * agent arguments.
                 */
                final AppClientContainer.Builder builder =
                        createBuilder(targetServers,
                            clientContainer.getLogService(),
                            appClientCommandArgs);

                /*
                 * Create the ACC.  Again, precisely how we create it depends on some
                 * of the command line arguments and agent arguments.
                 */
                acc = createContainer(builder,
                        launchInfo);

                /*
                 * Because the JMV might invoke the client's main class, the agent
                 * needs to prepare the container.  (This is done as part of the
                 * AppClientContainer.start() processing in the public API.
                 */
                acc.prepare();
            }
            
            /*
             * Make the new ACC accessible to the facade main classes.
             */
            AppClientFacadeInfo.setACC(acc);
            logger.fine("AppClientContainerAgent finished after " + (System.currentTimeMillis() - now) + " ms");

        } catch (UserError ue) {
            ue.displayAndExit();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    private static void initInstallRootProperty() throws URISyntaxException {
        URI jarURI = AppClientContainerAgent.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        File jarFile = new File(jarURI);
        File dirFile = jarFile.getParentFile().getParentFile();
        installRootURI = dirFile.toURI();

        System.setProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY, dirFile.getAbsolutePath());
    }

    private static List<String> skipJVMArgs(final List<String> options) {
        List<String> result = new ArrayList<String>();
        /*
         * JVM args are those that appear before the first "-jar xxx" or the
         * first "-client xxx" or the first stand-alone value (the main class).
         */
        int slot = 0;
        boolean isClassSelected = false;

        while (slot < options.size()) {
            final String option = options.get(slot);
            if (isClassSelected) {
                result.add(option);
            } else {
                if (option.equals("-jar") ||
                        option.equals("-client")) {
                    /*
                     * This is the class selector.
                     */
                    isClassSelected = true;
                    if (slot >= options.size()) {
                        throw new IllegalArgumentException(option);
                    }
                    slot++; // skip past the value
                } else if (option.charAt(0) != '-') {
                    isClassSelected = true;
                } else if (option.equals("-classpath") ||
                        option.equals("-cp")) {
                    if (slot >= options.size()) {
                        throw new IllegalArgumentException(option);
                    }
                    slot++;
                } else {
                    // Must be a JVM option - ignore it.
                }
            }
        }
        return result;
    }

    private static ACCClassLoader initClassLoader(final String appcPath) throws MalformedURLException {
        ACCClassLoader newLoader = ACCClassLoader.newInstance(Thread.currentThread().getContextClassLoader());
        if (appcPath != null) {
            for (String elt : appcPath.split(File.pathSeparator)) {
                File f = new File(elt);
                newLoader.appendURL(f.toURI().toURL());
            }
        }
        return newLoader;
    }

    private static Builder createBuilder(
            final TargetServer[] targetServers,
            final LogService logService,
            final AppclientCommandArguments appClientCommandArgs) throws IOException {

        Builder builder = AppClientContainer.newBuilder(targetServers);

        /*
         * Augment the builder with settings from the app client options that
         * can affect the builder itself.  (This is distinct from options
         * that affect what client to launch which are handled in creating
         * the ACC itself.
         */
        updateClientCredentials(builder, appClientCommandArgs);
        builder.logger(new ACCLogger(logService));

        return builder;
    }

    private static void updateClientCredentials(
            final Builder builder,
            final AppclientCommandArguments appClientCommandArgs) {

        ClientCredential cc = builder.getClientCredential();
        String user = (cc != null ? cc.getUserName() : null);
        char[] pw = (cc != null && cc.getPassword() != null ?
            cc.getPassword().get() : null);

        /*
         * user on command line?
         */
        String commandLineUser;
        if ((commandLineUser = appClientCommandArgs.getUser()) != null) {
            user = commandLineUser;
        }

        /*
         * password or passwordfile on command line? (theAppClientCommandArgs
         * class takes care of reading the password from the file and/or
         * handling the -password option.
         */
        char[] commandLinePW;
        if ((commandLinePW = appClientCommandArgs.getPassword()) != null) {
            pw = commandLinePW;
        }
        builder.clientCredentials(user, pw);
    }

    private static AppClientContainer createContainer(
            final Builder builder,
            final CommandLaunchInfo launchInfo) throws Exception, UserError {

        /*
         * The launchInfo already knows something about how to conduct the
         * launch.
         */
        ClientLaunchType launchType = launchInfo.getClientLaunchType();
        AppClientContainer container = null;

        switch (launchType) {
            case JAR:
            case DIR:
                /*
                 * The client name in the launch info is a file path for the
                 * directory or JAR to launch.
                 */
                container = createContainerForAppClientArchiveOrDir(
                        builder,
                        launchInfo.getClientName(),
                        launchInfo.getAppclientCommandArguments().getMainclass(),
                        launchInfo.getAppclientCommandArguments().getName());
                break;

            case CLASS:
                container = createContainerForClassName(builder, launchInfo.getClientName());
                break;

            case CLASSFILE:
                container = createContainerForClassFile(builder, launchInfo.getClientName());
                break;
        }

        if (container == null) {
            throw new IllegalArgumentException("cannot choose app client launch type");
        }

        return container;
    }

    private static AppClientContainer createContainerForAppClientArchiveOrDir(
            final Builder builder,
            final String appClientPath,
            final String mainClassName,
            final String clientName) throws Exception, UserError {

        URI uri = Util.getURI(new File(appClientPath));
        return builder.newContainer(uri, null /* callbackHandler */, mainClassName, clientName);
    }

    private static AppClientContainer createContainerForClassName(
            final Builder builder,
            final String className) throws Exception, UserError {

        /*
         * Place "." on the class path so that when we convert the class file
         * path to a fully-qualified class name and try to load it, we'll find
         * it.
         */

        ClassLoader loader = prepareLoaderToFindClassFile(Thread.currentThread().getContextClassLoader());
        Thread.currentThread().setContextClassLoader(loader);
        Class mainClass = Class.forName(className, true, loader);

        AppClientContainer result = builder.newContainer(mainClass);
        return result;
    }

    private static ClassLoader prepareLoaderToFindClassFile(final ClassLoader currentLoader) throws MalformedURLException {
        File currentDirPath = new File(System.getProperty("user.dir"));
        ClassLoader newLoader = new URLClassLoader(new URL[] {currentDirPath.toURI().toURL()}, currentLoader);
        return newLoader;
    }

    private static AppClientContainer createContainerForClassFile(
            final Builder builder,
            final String classFilePath) throws MalformedURLException, ClassNotFoundException, FileNotFoundException, IOException, Exception, UserError {
        
        Util.verifyFilePath(classFilePath);

        /*
         * Strip off the trailing .class from the path and convert separator
         * characters to dots to build a fully-qualified class name.
         */
        String className = classFilePath
                .substring(0, classFilePath.lastIndexOf(".class"))
                .replace(File.separatorChar, '.');

        return createContainerForClassName(builder, className);
    }

    private static ClientContainer readConfig(final String configPath,
            final ClassLoader loader) throws UserError, JAXBException,
                FileNotFoundException, ParserConfigurationException,
                SAXException, URISyntaxException,
                IOException {
        ClientContainer result = null;
        File configFile = checkXMLFile(configPath);
        checkXMLFile(launchInfo.getAppclientCommandArguments().getConfigFilePath());
        /*
         * Although JAXB makes it very simple to parse the XML into Java objects,
         * we have to do several things explicitly to use our local copies of
         * DTDs and XSDs.
         */
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setValidating(true);
        spf.setNamespaceAware(true);
        SAXParser parser = spf.newSAXParser();
        XMLReader reader = parser.getXMLReader();

        /*
         * Get the GF-specific local entity resolver that knows about the
         * local copy of the DTD.
         */
        reader.setEntityResolver(new LocalDTDResolver());

        /*
         * To support installation-directory independence the default sun-acc.xml
         * refers to the wss-config file using ${com.sun.aas.installRoot}...  So
         * preprocess the sun-acc.xml file to replace any tokens with the
         * corresponding values, then submit that result to JAXB.
         */
        InputSource inputSource = replaceTokensForParsing(configFile);

        SAXSource saxSource = new SAXSource(reader, inputSource);
        JAXBContext jc = JAXBContext.newInstance(ClientContainer.class );

        Unmarshaller u = jc.createUnmarshaller();
        result = (ClientContainer) u.unmarshal(saxSource);

        return result;
    }

    private static InputSource replaceTokensForParsing(final File configFile) throws FileNotFoundException, IOException, URISyntaxException {
        FileReader reader = new FileReader(configFile);
        char[] buffer = new char[1024];

        CharArrayWriter writer = new CharArrayWriter();
        int charsRead;
        while ((charsRead = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, charsRead);
        }
        writer.close();
        reader.close();

        Map<String,String> mapping = new HashMap<String,String>();
        Properties props = System.getProperties();
        for (Enumeration e = props.propertyNames(); e.hasMoreElements(); ) {
            String propName = (String) e.nextElement();
            mapping.put(propName, props.getProperty(propName));
        }

        /*
         * Add com.sun.aas.installRoot if it's not already there.
         */
        if ( ! props.containsKey(SystemPropertyConstants.INSTALL_ROOT_PROPERTY) ) {
            URI thisJarURI = AppClientContainerAgent.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            if (thisJarURI.getScheme().equals("jar")) {
                String ssp = thisJarURI.getSchemeSpecificPart();
                thisJarURI = (ssp.startsWith("file:") ? URI.create(ssp) : URI.create("file:" + ssp));
            }
            File thisJarFile = new File(thisJarURI);

            mapping.put(SystemPropertyConstants.INSTALL_ROOT_PROPERTY, thisJarFile.getParentFile().getParentFile().getAbsolutePath());
        }
        TokenResolver resolver = new TokenResolver(mapping);
        String configWithTokensReplaced = resolver.resolve(writer.toString());
        InputSource inputSource = new InputSource(new StringReader(configWithTokensReplaced));
        return inputSource;
    }

    private static File checkXMLFile(String xmlFullName) throws UserError
    {
        try {
            File f = new File(xmlFullName);
            if((f != null) && f.exists() && f.isFile() && f.canRead()){
                return f;
            }else{// If given file does not exists
                xmlMessage(xmlFullName);
                return null;
            }
        } catch (Exception ex) {
            xmlMessage(xmlFullName);
            return null;
        }
    }

    private static void usage(final int exitStatus) {
        System.err.println(getUsage());
        System.exit(exitStatus);
    }

    private static String getUsage() {
        return localStrings.getLocalString(
            AppClientContainerAgent.class,
            "main.usage",
            "appclient [ <classfile> | -client <appjar> ] [-mainclass <appClass-name>|-name <display-name>] [-xml <xml>] [-textauth] [-user <username>] [-password <password>|-passwordfile <password-file>] [-targetserver host[:port][,host[:port]...] [app-args]"
            )
            + System.getProperty("line.separator")
            + localStrings.getLocalString(AppClientContainerAgent.class,
                "main.usage.1",
                "  or  :\n\tappclient [ <valid JVM options and valid ACC options> ] [ <appClass-name> | -jar <appjar> ] [app args]");
    }

    private static void xmlMessage(String xmlFullName) throws UserError {
        UserError ue = new UserError(localStrings.getLocalString(
                AppClientContainerAgent.class,
                "main.cannot_read_clientContainer_xml",
               "Client Container xml: {0} not found or unable to read.\nYou may want to use the -xml option to locate your configuration xml.",
                new String[] {xmlFullName}));
        ue.setUsage(getUsage());
        throw ue;

    }
    
    private static class LocalDTDResolver extends DefaultHandler {

        private final static String ACC_PUBLIC_ID = "-//Sun Microsystems Inc.//DTD Application Server 8.0 Application Client Container//EN";
        private final static URI ACC_WEB_URI = URI.create("sun-application-client-container_1_2.dtd");

        private final static Map<String,URI> publicIDToFilePath = initPublicIDToWeb();

        private static Map<String,URI> initPublicIDToWeb() {
            Map<String,URI> result = new HashMap<String,URI>();
            result.put(ACC_PUBLIC_ID, ACC_WEB_URI);
            return result;
        }

        @Override
        public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException {
            if (publicId == null) {
                if (systemId == null ||
                    systemId.lastIndexOf('/') == systemId.length()) {
                        return null;
                }
                String fileName = SaxParserHandler.getSchemaURLFor(systemId.substring(systemId.lastIndexOf('/') + 1));
                if (fileName == null) {
                    fileName = systemId;
                }
                return new InputSource(fileName);
            }
            if (publicIDToFilePath.containsKey(publicId)) {
                URI dtdURI = installRootURI.resolve("lib/dtds/").resolve(publicIDToFilePath.get(publicId));
                File dtdFile = new File(dtdURI);
                return new InputSource(new BufferedInputStream(new FileInputStream(dtdFile)));
            }
            return null;
        }
    }

    private static class JavaVersion {
        private String versionString = System.getProperty("java.version");
        private int versionAsInt = initVersionAsInt();

        private int initVersionAsInt() {
            int firstDot = versionString.indexOf(".");
            String tensString = versionString.substring(0,firstDot);
            int nextDot = versionString.indexOf(".", firstDot+1);
            if (nextDot<0) {
                nextDot= versionString.length();
            }
            String onesString = versionString.substring(firstDot+1, nextDot);
            int version = -1;
    //        try {
                int tens = new Integer( tensString ).intValue();
                int ones = new Integer( onesString ).intValue();
                version = (tens*10) + ones;
    //        } catch(NumberFormatException nfe) {
    //
    //        }
            return version;
        }

        private int asInt() {
            return versionAsInt;
        }

        @Override
        public String toString() {
            return versionString;
        }
    }
}
