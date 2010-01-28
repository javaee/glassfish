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

package org.glassfish.appclient.client;

import com.sun.enterprise.container.common.spi.util.InjectionException;
import com.sun.enterprise.deployment.node.SaxParserHandlerBundled;
import com.sun.enterprise.universal.glassfish.TokenResolver;
import com.sun.enterprise.util.LocalStringManager;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
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
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import org.glassfish.appclient.client.acc.ACCClassLoader;
import org.glassfish.appclient.client.acc.ACCLogger;
import org.glassfish.appclient.client.acc.AgentArguments;
import org.glassfish.appclient.client.acc.AppClientContainer;
import org.glassfish.appclient.client.acc.AppClientContainer.Builder;
import org.glassfish.appclient.client.acc.AppclientCommandArguments;
import org.glassfish.appclient.client.acc.CommandLaunchInfo;
import org.glassfish.appclient.client.acc.CommandLaunchInfo.ClientLaunchType;
import org.glassfish.appclient.client.acc.TargetServerHelper;
import org.glassfish.appclient.client.acc.UserError;
import org.glassfish.appclient.client.acc.Util;
import org.glassfish.appclient.client.acc.config.AuthRealm;
import org.glassfish.appclient.client.acc.config.ClientContainer;
import org.glassfish.appclient.client.acc.config.ClientCredential;
import org.glassfish.appclient.client.acc.config.LogService;
import org.glassfish.appclient.client.acc.config.MessageSecurityConfig;
import org.glassfish.appclient.client.acc.config.Property;
import org.glassfish.appclient.client.acc.config.TargetServer;
import org.glassfish.appclient.client.jws.boot.LaunchSecurityHelper;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 *
 * @author tjquinn
 */
public class AppClientFacade {

    private static final String SUN_ACC_CONTENT_PROPERTY_NAME = "sun-acc.xml.content";

    private static final Class<?> stringsAnchor = ACCClassLoader.class;
    private static LocalStringManager localStrings = new LocalStringManagerImpl(stringsAnchor);

    private static CommandLaunchInfo launchInfo = null;

    private static AppclientCommandArguments appClientCommandArgs = null;

    private static URI installRootURI = null;

    private static AppClientContainer acc = null;

    private static boolean isJWS = false;


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            if (acc == null) {
                /*
                 * The facade JAR has been run directly, not via the appclient
                 * script and not via Java Web Start.  So we have no agent
                 * arguments and no instrumentation for registering transformations.
                 */
                prepareACC(null, null);
            }
            acc.launch(args);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        } catch (UserError ue) {
            ue.displayAndExit();
        }
    }

    public static AppClientContainer acc() {
        return acc;
    }

    public static void launch(String[] args) throws NoSuchMethodException,
            ClassNotFoundException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException,
            IOException, SAXParseException, InjectionException, UserError {
        acc.launch(args);
    }

    public static void prepareACC(String agentArgsText, Instrumentation inst) throws UserError, MalformedURLException, URISyntaxException, JAXBException, FileNotFoundException, ParserConfigurationException, SAXException, IOException, Exception {
        JavaVersion javaVersion = new JavaVersion();
        if (javaVersion.asInt() < 16) {
            throw new UserError(localStrings.getLocalString(
                    stringsAnchor,
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
        ACCClassLoader loader = initClassLoader(
                (inst == null));
        Thread.currentThread().setContextClassLoader(loader);

        isJWS = Boolean.getBoolean("appclient.is.jws");

        /*
         * The installRoot property will be set by the ServerEnvironment
         * initialization using the ACC start-up context.  That happens during
         * the ACCModulesManager warm-up.
         */

        if (launchInfo.getClientLaunchType() == ClientLaunchType.UNKNOWN) {
            throw new IllegalArgumentException();
        }
        appClientCommandArgs = launchInfo.getAppclientCommandArguments();

        /*
         * Load the ACC configuration XML file.
         */
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
                    clientContainer,
                    appClientCommandArgs);

        /*
         * Create the ACC.  Again, precisely how we create it depends on some
         * of the command line arguments and agent arguments.
         */
        final AppClientContainer newACC = createContainer(builder,
                launchInfo);

        /*
         * Because the JMV might invoke the client's main class, the agent
         * needs to prepare the container.  (This is done as part of the
         * AppClientContainer.start() processing in the public API.
         */
        newACC.prepare(inst);

        acc = newACC;
    }

    private static AppClientContainer prepareACC() {
        // XXX Implement this to support java -jar launching.
        return null;
    }

    private static void usage(final int exitStatus) {
        System.err.println(getUsage());
        System.exit(exitStatus);
    }

    private static String getUsage() {
        return localStrings.getLocalString(
            stringsAnchor,
            "main.usage",
            "appclient [ <classfile> | -client <appjar> ] [-mainclass <appClass-name>|-name <display-name>] [-xml <xml>] [-textauth] [-user <username>] [-password <password>|-passwordfile <password-file>] [-targetserver host[:port][,host[:port]...] [app-args]"
            )
            + System.getProperty("line.separator")
            + localStrings.getLocalString(stringsAnchor,
                "main.usage.1",
                "  or  :\n\tappclient [ <valid JVM options and valid ACC options> ] [ <appClass-name> | -jar <appjar> ] [app args]");
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

    private static ACCClassLoader initClassLoader(
            final boolean loaderShouldTransform) throws MalformedURLException {
        ACCClassLoader loader = ACCClassLoader.instance();
        if (loader == null) {
            loader = ACCClassLoader.newInstance(
                Thread.currentThread().getContextClassLoader(),
                loaderShouldTransform);
        }
        return loader;
    }

    private static Builder createBuilder(
            final TargetServer[] targetServers,
            final ClientContainer clientContainer,
            final AppclientCommandArguments appClientCommandArgs) throws IOException {

        Builder builder = AppClientContainer.newBuilder(targetServers);

        /*
         * Augment the builder with settings from the app client options that
         * can affect the builder itself.  (This is distinct from options
         * that affect what client to launch which are handled in creating
         * the ACC itself.
         */
        updateClientCredentials(builder, appClientCommandArgs);
        final List<MessageSecurityConfig> msc = clientContainer.getMessageSecurityConfig();
        if (msc != null) {
            builder.getMessageSecurityConfig().addAll(clientContainer.getMessageSecurityConfig());
        }
        builder.logger(new ACCLogger(clientContainer.getLogService()));
        final AuthRealm ar = clientContainer.getAuthRealm();
        if (ar != null) {
            builder.authRealm(ar.getClassname());
        }
        final List<Property> p = clientContainer.getProperty();
        if (p != null) {
            builder.containerProperties(p);
        }

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

            case URL:
                container = createContainerForJWSLaunch(
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
        return builder.newContainer(uri, null /* callbackHandler */, mainClassName, clientName,
                launchInfo.getAppclientCommandArguments().isTextauth());
    }

    private static AppClientContainer createContainerForJWSLaunch(
            final Builder builder,
            final String appClientPath,
            final String mainClassName,
            final String clientName) throws Exception, UserError {

        URI uri = URI.create(appClientPath);
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
        Reader configReader;
        /*
         * During a Java Web Start launch, the config is passed as a property
         * value.
         */
        String configInProperty = System.getProperty(SUN_ACC_CONTENT_PROPERTY_NAME);
        if (configInProperty != null) {
            /*
             * Awkwardly, the sun-acc.xml content refers to a config file.
             * We work around this for Java Web Start launch by capturing the
             * content of that config file into a property setting in the
             * generated JNLP document.  We need to write that content into
             * a temporary file here on the client and then replace a
             * placeholder in the sun-acc.xml content with the path to that
             * temp file.
             */
            final File securityConfigTempFile = Util.writeTextToTempFile(
                    configInProperty, "wss-client-config", ".xml", false);
            final Properties p = new Properties();
            p.setProperty("security.config.path", securityConfigTempFile.getAbsolutePath());
            configInProperty = Util.replaceTokens(configInProperty, p);
            configReader = new StringReader(configInProperty);
        } else {
            /*
             * This is not a Java Web Start launch, so read the configuration
             * from a disk file.
             */
            File configFile = checkXMLFile(configPath);
            checkXMLFile(launchInfo.getAppclientCommandArguments().getConfigFilePath());
            configReader = new FileReader(configFile);
        }
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
         * Get the local entity resolver that knows about the
         * bundled .dtd and .xsd files.
         */
        reader.setEntityResolver(new SaxParserHandlerBundled());

        /*
         * To support installation-directory independence the default sun-acc.xml
         * refers to the wss-config file using ${com.sun.aas.installRoot}...  So
         * preprocess the sun-acc.xml file to replace any tokens with the
         * corresponding values, then submit that result to JAXB.
         */
        InputSource inputSource = replaceTokensForParsing(configReader);

        SAXSource saxSource = new SAXSource(reader, inputSource);
        JAXBContext jc = JAXBContext.newInstance(ClientContainer.class );

        Unmarshaller u = jc.createUnmarshaller();
        result = (ClientContainer) u.unmarshal(saxSource);

        return result;
    }

    private static InputSource replaceTokensForParsing(final Reader reader) throws FileNotFoundException, IOException, URISyntaxException {
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

    private static void xmlMessage(String xmlFullName) throws UserError {
        UserError ue = new UserError(localStrings.getLocalString(
                stringsAnchor,
                "main.cannot_read_clientContainer_xml",
               "Client Container xml: {0} not found or unable to read.\nYou may want to use the -xml option to locate your configuration xml.",
                new String[] {xmlFullName}));
        ue.setUsage(getUsage());
        throw ue;

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
