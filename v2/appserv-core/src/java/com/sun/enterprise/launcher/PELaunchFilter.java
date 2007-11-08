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
package com.sun.enterprise.launcher;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.StringTokenizer;
import org.w3c.dom.*;
import java.io.File;
import javax.xml.parsers.*;
import com.sun.org.apache.commons.launcher.LaunchFilter;
import com.sun.org.apache.commons.launcher.LaunchCommand;
import org.apache.tools.ant.BuildException;

//The RelativePathResolver is used to translate relative paths containing
//embedded system properties (e.g. ${com.sun.aas.instanceRoot}/applications)
//into absolute paths
import com.sun.enterprise.util.RelativePathResolver;

import com.sun.enterprise.util.ASenvPropertyReader;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.OS;


/**
 * Create a filter for Appserver to use commons launcher. This filter is used
 * to parse out the jvm-options from server.xml and use it to launch the vm.
 *
 * @author Ramesh Mandava (ramesh.mandava@sun.com)
 * @author Sheetal Vartak (sheetalv@sun.com)
 */

public class PELaunchFilter implements LaunchFilter {

    public static final String DEBUG_OPTIONS = "com.sun.aas.jdwpOptions";
    public static final String LOCALE = "locale";

    ArrayList jvmArgsList = null;
    HashMap sysProps = null;
    String serverModeOrClientMode = null;


    public static String getDebugProperty(String debug_options, String name) {
        int nameIndex;
        if ( (nameIndex = debug_options.indexOf(name)) != -1 ) {
            // format is "name=value"
            String value = debug_options.substring(nameIndex
                                                   + name.length() + 1);
            int commaIndex;
            if ( (commaIndex = value.indexOf(",")) != -1 ) {
                value = value.substring(0, commaIndex);
            }
            return value;
        }
        return null;
    }


    public void filter(LaunchCommand command) throws BuildException {


	try {
            //Set system properties that correspond directly to asenv.conf/bat. This
            //keeps us from having to pass them all from -D on the command line.
            ASenvPropertyReader reader = new ASenvPropertyReader(
                System.getProperty(SystemPropertyConstants.CONFIG_ROOT_PROPERTY));
            reader.setSystemProperties();

            // Reset serverModeOrClientMode to null for each filter invocation
            serverModeOrClientMode = null;

            boolean stopOperation = false;
            boolean waitForChild = command.getWaitforchild();
            boolean debugOption = false;

            ArrayList argsList = command.getArgs();
            String[] args =(String[])argsList.toArray(new String[argsList.size()]);

            for ( int ar=0; ar < args.length; ar++ ) {
                if ( "stop".equals( args[ar] ) ) {
                // When server need to be stopped keep the process in foreground
                     stopOperation = true;
                     waitForChild = true;
                    debugOption = false;
                    break;
                 }
                else if ( "debug".equals(args[ar]) ) {
                    debugOption = true; // for "asadmin start-domain --debug"
                }
                else if ( "verbose".equals(args[ar]) ) {
                    waitForChild = true; // for "asadmin start-domain --verbose"
                }
            }


            //ArrayList jvmArgsList = command.getJvmargs( );
            jvmArgsList = command.getJvmargs( );
            if ( jvmArgsList == null ) {
                jvmArgsList = new ArrayList();
            }

	    if ( waitForChild ) {
		// this is used in ServerLogManager to send logs to stderr
                jvmArgsList.add("-Dcom.sun.aas.verboseMode=true");
	    }

	    //HashMap sysProps = command.getSysproperties();
	    sysProps = command.getSysproperties();
	    if ( sysProps == null ) {
                // We are relying on system propeties to be present with the
                // required values
                // It is FATAL error if  execution come to this block
                // PENDING : Logging and update the messages
                System.out.println("[FATAL ERROR] System properties with S1AS"+
                   " installation values doesn't present" );
                System.out.println("Please check <installtionDir>/lib/launcher.xml" );
                System.out.println( "Exiting out");
                System.exit(1);
	    }
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    DocumentBuilder db = dbf.newDocumentBuilder();

            // We must set up a custom entity resolver (the same used by the admin
            // backend in the server). This is necessary to parse the DOCTYPE
            // attribute in domain.xml which now contains ${com.sun.aas.installRoot}
            db.setEntityResolver((org.xml.sax.helpers.DefaultHandler)Class.forName
                ("com.sun.enterprise.config.serverbeans.ServerValidationHandler").newInstance());

            String instanceRoot =(String)sysProps.get("com.sun.aas.instanceRoot");
            String serverName = (String)sysProps.get("com.sun.aas.instanceName");
            // If for some reason com.sun.aas.instanceName property is not set,
            //  we are defaulting to "server"
            if (  serverName == null ) {
                serverName="server";
            }

            String domainConfigFilePath = instanceRoot + File.separator +
			"config" + File.separator +
                "domain.xml";

	    Document doc = db.parse(domainConfigFilePath );

	    Element root = doc.getDocumentElement();

            //Set any system properties specified as domain properties first
            //since these have the lowest precedence.
            handleSystemProperties(root);

            //set the default locale specified in domain.xml config file
            if(root.hasAttribute(LOCALE)) {
                String locale = root.getAttribute(LOCALE);
                if(locale != null && !"".equals(locale)) {
                    sysProps.put(SystemPropertyConstants.DEFAULT_LOCALE_PROPERTY, locale);
                }
            } else {
                sysProps.put(SystemPropertyConstants.DEFAULT_LOCALE_PROPERTY,
                        System.getProperty(SystemPropertyConstants.DEFAULT_LOCALE_PROPERTY));
            }

	    //
	    // Get server element for the given server name
	    //

            NodeList servers = root.getElementsByTagName("servers");

	    if (servers.getLength() < 1) {
                System.out.println("[FATAL ERROR] " +
			" domain.xml doesn't have servers element");
		System.exit(1);
	    }

	    Element serversElement = (Element) servers.item(0);

            NodeList server  = serversElement.getElementsByTagName("server");

	    if (server.getLength() < 1) {
                System.out.println("[FATAL ERROR] " +
			" domain.xml doesn't have server element");
		System.exit(1);
	    }

	    Element serverElement = null;

        boolean foundMatchingServer = false;
	    for (int i=0; i<server.getLength(); i++) {
		serverElement = (Element) server.item(i);
		if ((serverElement.getAttribute("name")).equals(serverName)) {
            foundMatchingServer = true;
			break;
		}
	    }

	    if (! foundMatchingServer) {
                System.out.println("[FATAL ERROR] " +
			" domain.xml doesn't have the server element named: " + serverName);
		System.exit(1);
	    }

	    //
	    // Get config element
	    //
	    // config is an attribute of server. So, use getAttribute
            String configRef = serverElement.getAttribute("config-ref");

        //Set the com.sun.aas.configName system property to hold the configName
        sysProps.put(SystemPropertyConstants.CONFIG_NAME_PROPERTY ,
            configRef);
        System.setProperty(SystemPropertyConstants.CONFIG_NAME_PROPERTY,
            configRef);

        NodeList logServices = getNodeList(root, configRef, "log-service");

	    Element logServiceElement = null;
        String logFileName = null;
        String logToConsole = null;

        // Only one log-service element would be there in domain.xml
        // In case of more than one log-service element declaration is
        // there, then the last one would be used
        for ( int ls=0; ls<logServices.getLength(); ls++ ) {
             logServiceElement = (Element)logServices.item(ls);
             logFileName = RelativePathResolver.resolvePath(
                logServiceElement.getAttribute("file"));
             logToConsole = logServiceElement.getAttribute("log-to-console");
        }

        if( (logToConsole != null )
          &&(logToConsole.equals( "true" ) ) )
        {
            waitForChild = true;
            jvmArgsList.add("-Dcom.sun.aas.verboseMode=true");
        } else if ( logFileName != null ) {
             command.setOutput( new File(logFileName) );
        }
        command.setWaitforchild ( waitForChild );

	    //
	    // Modified the code to get java-config for the given server
	    //
            // NodeList javaConfigs = root.getElementsByTagName("java-config");

            NodeList javaConfigs = getNodeList(root, configRef, "java-config");

            //javaconfig attribute value holders declaration
            Element javaConfigElement = null;
            String debug_enabled = null;
            String debug_options = null;
            String rmic_options = null;
            String javac_options = null;
            String java_home = null;
            String classpath_prefix = null;
            String server_classpath = null;
            String classpath_suffix = null;
            String native_library_path_prefix = null;
            String native_library_path_suffix = null;
            String bytecode_preprocessors = null;
            String env_classpath_ignored = "true";

            // It seems that only one java-config element would be there as
            // part of domain.xml. Shall we just use first occurence instead of
            // for loop. PENDING : Error condition check
            for ( int jc=0; jc<javaConfigs.getLength(); jc++ ) {
                javaConfigElement = (Element)javaConfigs.item(jc);

                debug_enabled = javaConfigElement.getAttribute
                    ("debug-enabled");
                debug_options = javaConfigElement.getAttribute
                    ("debug-options");
                rmic_options = javaConfigElement.getAttribute
                    ("rmic-options");
                javac_options = javaConfigElement.getAttribute
                    ("javac-options");
                if (OS.isDarwin()) {
                    java_home = RelativePathResolver.resolvePath(
                        javaConfigElement.getAttribute("java-home"));
                } else {
                    java_home = RelativePathResolver.resolvePath(
                        javaConfigElement.getAttribute("java-home") +
                        File.separator + "jre");
                }
                classpath_prefix = RelativePathResolver.resolvePath(
                    javaConfigElement.getAttribute("classpath-prefix"));
	        server_classpath = RelativePathResolver.resolvePath(
                    javaConfigElement.getAttribute("server-classpath"));
                classpath_suffix = RelativePathResolver.resolvePath(
                    javaConfigElement.getAttribute("classpath-suffix"));
                native_library_path_prefix = javaConfigElement.getAttribute
                    ("native-library-path-prefix");
                native_library_path_suffix = javaConfigElement.getAttribute
                    ("native-library-path-suffix");
                bytecode_preprocessors = javaConfigElement.getAttribute
                    ("bytecode-preprocessors");
                env_classpath_ignored= javaConfigElement.getAttribute
                    ("env-classpath-ignored");
            }
	    if(java_home != null)
                System.setProperty("java.home", java_home );
            String profilerClasspath = null;
            String profilerNativeLibraryPath = null;
            // Now handle profiler element

	    //
	    // Modified the code to get profiler for the given server
	    //
            // NodeList profilers = root.getElementsByTagName("profiler");

	    NodeList profilers = javaConfigElement.getElementsByTagName("profiler");

            int numberOfProfilerElements =  profilers.getLength();
            if ( numberOfProfilerElements > 0 ) {
                //According to DTD we should have only one or zero, but even if
                // we have more than 1, just use first one
                Element profilerElement = (Element)(profilers.item(0));
                String profilerEnabled=profilerElement.getAttribute("enabled");

                if ( (profilerEnabled != null ) &&
                    ( profilerEnabled.equals("true") ) ) {
                    //Now we need to take care of profiler options
                    String profilerName=profilerElement.getAttribute("name");
                    if ( profilerName == null ) {
                        //Send error message but continue
                        System.err.println("ERROR : Profiler is enabled. But Name is null");
                        System.err.println("Profiler settings are ignored");
                    } else {
                        profilerClasspath =profilerElement.getAttribute(
                            "classpath");
                        profilerNativeLibraryPath =
                            profilerElement.getAttribute("native-library-path");

                        handleJvmOptions ( profilerElement );
                        handleProperties ( profilerElement );

                    }


                }


            }

            String requestedClasspath = server_classpath;

	    // Classpath set through launcher.xml launch task
            String originalClasspath = command.getClasspath();

            if ( originalClasspath != null ) {
                requestedClasspath = originalClasspath + File.pathSeparator +
		  requestedClasspath;
            }

            if (( classpath_prefix != null ) &&
                (!classpath_prefix.trim().equals("")) ) {
                requestedClasspath = classpath_prefix + File.pathSeparator +
                    requestedClasspath;
            }
            if (( classpath_suffix != null ) &&
                (!classpath_suffix.trim().equals("")) ) {
                requestedClasspath = requestedClasspath + File.pathSeparator +
                    classpath_suffix ;
            }

            if (( profilerClasspath != null)&&
                (!profilerClasspath.trim().equals("")) ) {
                requestedClasspath = requestedClasspath + File.pathSeparator +
                    profilerClasspath;
            }


            String envClasspath = (String)sysProps.get("user.classpath");
            sysProps.remove("user.classpath");

            if ( env_classpath_ignored != null  &&
                env_classpath_ignored.equals("false") &&
                (!envClasspath.equals("${env.CLASSPATH}") )) {
                requestedClasspath = requestedClasspath + File.pathSeparator +
                    envClasspath;
            }

            command.setClasspath ( requestedClasspath );

            handleJvmOptions ( javaConfigElement );

            // Now set the remaining properties which we got from java-config
            // We should n't set debugger for stop operation ( stop-domain)
            if ( debug_enabled != null && debug_enabled.equals("true") ) {
                debugOption = true;
            }
            if ( debugOption && !stopOperation ) {

                // If debug is enabled, then we need to pass on -Xdebug option
		jvmArgsList.add("-Xdebug");

 	        // It seems that -Xdebug and other debug options shouldn't go
                // as one argument  So we will check if debug_options starts
                //  with that and give it as separate argument
                debug_options=debug_options.trim();
                if ( debug_options.startsWith("-Xdebug") ) {
                    debug_options =debug_options.substring("-Xdebug".length()).trim();
                }

                // Get the JPDA transport and address (port) from the
                // debug_options. If address is not specified in debug_options
                // for transport=dt_socket, we find a free port
                // and add it to -Xrunjdwp.
                //
                // If address is specified in -Xrunjdwp, then the JVM
                // does not print any debug message, so we need to print it for
                // easy viewing by the user.
                // If address is not specified in debug_options,
                // then the JVM will print a message like:
                // Listening for transport dt_socket at address: 33305
                // This is only visible with "asadmin start-domain --verbose"
                //
                // The format of debug_options is:
                // -Xrunjdwp:<name1>[=<value1>],<name2>[=<value2>]

                String transport = getDebugProperty(debug_options, "transport");
                String addr = getDebugProperty(debug_options, "address");

                if ( transport == null || transport.equals("") ) {
                    // XXX I18N this
                    throw new BuildException("Cannot start server in debug mode: no transport specified in debug-options in domain.xml.");
                }

                if ( transport.equals("dt_socket") ) {
                    if ( addr != null && !addr.equals("") ) {
                        // XXX Should we check if the port is free using
                        // com.sun.enterprise.util.net.NetUtils.isPortFree(port)
                    }
                    else {
                        // Get a free port
                        int port =
                            com.sun.enterprise.util.net.NetUtils.getFreePort();
                        if ( port == 0 ) {
                            // XXX I18N this
                            throw new BuildException("Cannot start server in debug mode: unable to obtain a free port for transport dt_socket.");
                        }
                        addr = String.valueOf(port);

                        debug_options = debug_options + ",address=" + addr;
                    }
                }

                jvmArgsList.add(debug_options);

                // Provide the actual JDWP options to the server using a
                // system property. This allow the server to make it available
                // to the debugger (e.g. S1 Studio) using an API.
                String jdwpOptions = debug_options.substring(
                   debug_options.indexOf("-Xrunjdwp:") + "-Xrunjdwp:".length());

                jvmArgsList.add("-D" + DEBUG_OPTIONS + "=" + jdwpOptions);
            }

	    // Need to fix Launcher code to deal with this native_library_path

            String javaLibPath = System.getProperty("java.library.path");

            if ( (native_library_path_prefix != null ) &&
                (! native_library_path_prefix.trim().equals("") ) ) {
                if (javaLibPath != null ) {
                    javaLibPath =native_library_path_prefix +
                        File.pathSeparator +  javaLibPath;
                } else {
                    javaLibPath = native_library_path_prefix;
                }
            }
            if ( (native_library_path_suffix != null ) &&
                (! native_library_path_suffix.trim().equals("") ) ) {
                if (javaLibPath != null ) {
                    javaLibPath =javaLibPath + File.pathSeparator +
                        native_library_path_suffix ;
                } else {
                    javaLibPath = native_library_path_suffix;
                }
            }
            if (( profilerNativeLibraryPath != null ) &&
                ( !profilerNativeLibraryPath.trim().equals("") ) ) {
                if (javaLibPath != null ) {
                    javaLibPath =javaLibPath + File.pathSeparator +
                        profilerNativeLibraryPath ;
                } else {
                    javaLibPath = profilerNativeLibraryPath;
                }
            }

	    /* windows appends the java.library.path sys property
	     with the %Path% value. We need to make sure that the %Path%
	     doesnot contain double quotes for path elements. Otherwise
	     server exits with error.
	    */
	    javaLibPath = normalize(javaLibPath);
            sysProps.put("java.library.path" , javaLibPath);
            System.setProperty("java.library.path", javaLibPath );

            // Put vm mode -server or -client, only when user mention that as
            // part of domain.xml as the first arg in jvmArgsList  to give that
            // as the first argument to VM

            if ( serverModeOrClientMode != null ) {
                jvmArgsList.add( 0,  "-" + serverModeOrClientMode );
            }

            //Set any server properties specified as configuration properties followeed
            //by server instance specific properties since these have the highest precedence.
            handleSystemProperties(getConfigElement(root, configRef));
            //FIXTHIS:WARNING. Currently properties which are present in the cluster element
            //referenced by the server element are not handled. This is not implemented for
            //the following reasons:
            //1)Currently commons-launcher is used to start the DAS only and the DAS can
            //never be a cluster member
            //2)It is not clear how long this code will stay around
            handleSystemProperties(serverElement);
            /* uncommment to enable a dump of the system properties
            Object[] keys = sysProps.keySet().toArray();
            for (int i = 0; i < keys.length; i++) {
                System.out.println("sysProp: " + (String)keys[i] + " " + sysProps.get((String)keys[i]));
            }
            */

	    command.setSysproperties(sysProps);
	} catch(Exception e) {
	    e.printStackTrace();
	    throw new BuildException();
	}
    }

    /* windows appends the java.library.path sys property
       with the %Path% value. We need to make sure that the %Path%
       doesnot contain double quotes for path elements. Otherwise
       server exits with error.
    */
    private String normalize(String path) {
	path = path.replaceAll("\"","");
	path = path.replaceAll("\'","");
	return path;
    }

    private void handleJvmOptions ( Element element  ) {
	    NodeList jvmoptions = element.getChildNodes();
	    Element tmpElement = null;
	    Node tmpNode = null;
	    String systemProperty = null;
	    String property = null;
	    String value = null;
	    for (int i = 0; i < jvmoptions.getLength(); i++) {
                Node varNode = jvmoptions.item(i);
                if ( ( varNode.getNodeType()!=  Node.ELEMENT_NODE ) ||
                    (!varNode.getNodeName().equals("jvm-options") ) ) {
                    continue;
                }
	        tmpElement = (Element) varNode;
	        tmpNode = tmpElement.getFirstChild();
                // If we encounter element like <jvm-options/> then ignore that
                if ( tmpNode == null ) {
                    continue;
                }

                // If we encounter <jvm-options> elements with just white space
                // then ignore that
	        if(tmpNode instanceof Text) {
		    if(tmpNode.getNodeValue().equals("")) {
		        continue;
		    }
	        }
	        systemProperty = (tmpNode.getNodeValue()).trim();

                // Allowing multiple options in one jvm-options element
                // We also allow - to appear in any jvm-option content like
                // -Dtest-name=test-value
                StringTokenizer stk = new StringTokenizer(systemProperty,"-");
                String currentToken = null;
                String option = null;

                int tokenCount = 0;
                int numOfTokens = stk.countTokens();

                while  ( stk.hasMoreTokens() ) {
                    if ( currentToken != null ) {
                        currentToken = currentToken + "-" +  stk.nextToken();
                    } else {
                        currentToken = stk.nextToken();
                    }
                    tokenCount++;

                    if (  ! currentToken.endsWith(" " ) ) {
                        // if there are still tokens then try to augment them
                        // other wise the currentToken would be the last option
                        if ( tokenCount < numOfTokens ) {
                            continue;
                        }
                    }

                    option = currentToken.trim();
                    currentToken = null;

                    if ( option.startsWith("D") ) {
                        option = option.substring(1, option.length() );

                        int indexOfEqual= option.indexOf("=");
                        if ( indexOfEqual > 0 ) {
                            property = option.substring(0,indexOfEqual );
                            value = option.substring(indexOfEqual+1 );
		                sysProps.put(property,
                                    RelativePathResolver.resolvePath(value));
                        } else {
                            //To allow options like -Djava.compiler without val
                            property = option;
		            jvmArgsList.add( "-D" + property );
                        }
                    } else {
                        if ( ( option.equals("client")) ||
                            (option.equals("server")) ) {
                            //If user mentions server or client mode for VM
                            // then use that over default which is "server"
                            // As we want to keep this as first arg don't add
                            // to the jvmArgsList yet
                            serverModeOrClientMode = option;
                        } else {
		            jvmArgsList.add( "-" + option );
                        }
                    }
                }

	    }

    }

    private void handleSystemProperties ( Element element  ) {
        handleProperties(element, "system-property");
    }

    private void handleProperties ( Element element  ) {
        handleProperties(element, "property");
    }

    private void handleProperties ( Element element, String elementName  ) {
        NodeList properties = element.getChildNodes();
        Element propertyElement = null;
        Node tmpNode = null;
        String systemProperty = null;
        String property = null;
        String value = null;
        for (int i = 0; i < properties.getLength(); i++) {
           Node varNode = properties.item(i);
           if ( ( varNode.getNodeType()!=  Node.ELEMENT_NODE ) ||
                (!varNode.getNodeName().equals(elementName) ) ) {
                continue;
            }
            propertyElement = (Element)varNode;

            String propertyName = propertyElement.getAttribute("name");
            String propertyValue = propertyElement.getAttribute("value");

            sysProps.put(propertyName, propertyValue);
        }
    }

    private Element getConfigElement(Element root, String configRef)
    {
        Element configElement = null;
        NodeList configs = root.getElementsByTagName("configs");
        //will never enter this condition as dtd requires configs.
        if (configs.getLength() < 1) {
            System.out.println("[FATAL ERROR] " +
            " domain.xml doesn't have configs element");
            System.exit(1);
        }

        Element configsElement = (Element) configs.item(0);
        NodeList config = configsElement.getElementsByTagName("config");
        if (config.getLength() < 1) {
            System.out.println("[FATAL ERROR] " +
            " domain.xml doesn't have config element");
            System.exit(1);
        }

        for (int j=0; j < config.getLength(); j++) {
            configElement = (Element) config.item(j);
            if ((configElement.getAttribute("name")).equals(configRef)) {
                return configElement;
            }
        }

        //There better be a configuration matching the server's configuration ref
        //or domain.xml is corrupt.
        System.out.println("[FATAL ERROR] " +
            " domain.xml doesn't have config element matching " + configRef);
        System.exit(1);
        return null;
    }

    private NodeList getNodeList(Element root, String configRef, String nlName) {
        Element configElement = getConfigElement(root, configRef);
        return configElement.getElementsByTagName(nlName);
    }

}
