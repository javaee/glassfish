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

/*
 * ProcessLauncherConfig.java
 *
 * Created on November 25, 2003, 10:18 AM
 */

package com.sun.enterprise.tools.launcher;
import java.io.FileWriter;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import com.sun.logging.LogDomains;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.util.RelativePathResolver;
import com.sun.enterprise.util.i18n.StringManager;


/**
 * This class is used to read the processLauncer.xml file, parse it and provide a useful structure
 * for use in the ProcessLauncher class.
 *
 * processLauncher.xml File layout
 *
 *  <processes>
 *      <process name="s1as8-server">
 *          <sysproperty key="com.sun.aas.instanceName" value="${com.sun.aas.instanceName}"  if="com.sun.aas.instanceName" />
 *          <main_class classname="com.sun.enterprise.server.PEMain" />
 *          <classpath dir="${com.sun.aas.installRoot}/lib"
 *              includes=".*jar$"
 *              excludes="appserv-rt.jar, ...." />
 *              prefix="/export/home/basler/test.jar"
 *      </process>
 *  ....
 *  </processes>
 *
 *
 * processes - Docroot element that has the defined process element as its children
 *
 *      process - Main element that can be mapped 1-to-1 to a process definition that is to be
 *                executed through the ProcessLauncher
 *          name - Name of the process definition type.  Must be unique within the processLauncher.xml file.
 *
 *      sysproperty - Defines system properties for the java command to be executed.  A "-D" is pre-pended
 *                    to the property key unless the key starts with "-X".  An equal sign ("=") with the value is
 *                    appended to the key if the value attribute exists.  The property will not be added if the
 *                    "if" system property designated by the associated attribute is not present.  The
 *                    com.sun.enterprise.util.RelativePathResolver class will be used to resolve system property
 *                    tokens in the values of the sysproperty elements.
 *          key - Name of system property
 *          value - Value for the system property.  This value is optional.  An equals sign "=" will not be
 *                  appended to the key if this value doesn't exist.
 *          if - System property that must exist for this system property to be added.  This attribute is optional.
 *
 *      main_class - Element that denotes the main class to execute in the java command.
 *          classname - Fully qualified class name of the class to run
 *
 *      classpath - Element that builds the classpath portion of the java command.
 *          dir - This attribute holds the fully qualified path to the lib directory where the jar exists.
 *                This is optional, but if it is not entered the "includes" & "excludes" attributes are not used.
 *                This is to allow for a known fully qualified classpath to be enter via the "prefix" attritbute.
 *                The com.sun.enterprise.util.RelativePathResolver class will be used to resolve system property tokens
 *                in this attribute.
 *          includes - A comma delimited list of jar names to include in the classpath if they exist in the lib
 *                     directory specified in the "dir" attribute.  The names can contain a regular expression to
 *                     assist name resolution.  For users who are not familiar with regular expressions a short-cut has
 *                     been added to allow a "*" as a wildcard prefix (e.g. "*.jar").  This functionality was added for
 *                     backwards compatibility with the Apache Commons Launcher and for future uses the equivalent regular
 *                     expression ".*jar$" should be used.
 *          excludes - A comma delimited list of jar names to exclude in the classpath if they exist in the lib directory
 *                     specified in the "dir" attribute.  The names can contain a regular expression to assist name resolution.
 *                     For users who are not familiar with regular expressions a short-cut has been added to allow a "*" as a
 *                     wildcard prefix (e.g. "*.jar").  This functionality was added for backwards compatibility with the
 *                     Apache Commons Launcher and for future uses the equivalent regular expression ".*jar$" should be used.
 *          prefix - A list of fully qualified classpath jars.  The com.sun.enterprise.util.RelativePathResolver class will
 *                   be used to resolve system property tokens in this attribute.  This attribute can also be used if a
 *                   classpath is known before execution time.  The java default system property "${path.separator}" should
 *                   be used as the path delimiter to keep the profile platform agnostic.
 */
public class ProcessLauncherConfig {

    private String _process="";
    private String _configFile="";
    private String _classpathExcludes="";
    private String _classpathIncludes="";
    private String _classpathLibDir="";
    private String _classpathPrefix="";
    private String _classpathJ2se14Prefix="";
    private String _classpathJ2se15OrLaterPrefix="";
    private String _mainClass="";
    private StringManager _strMgr=null;
    private Properties _sysProperties=null;
    private static boolean bDebug=false;

    private static final String PROCESS="process";
    private static final String SYSTEM_PROPERTY="sysproperty";
    private static final String MAIN_CLASS="main_class";
    private static final String MAIN_CLASS_CLASSNAME="classname";
    private static final String CLASSPATH="classpath";
    private static final String CLASSPATH_INCLUDES="includes";
    private static final String CLASSPATH_EXCLUDES="excludes";
    private static final String CLASSPATH_PREFIX="prefix";
    private static final String CLASSPATH_J2SE1_4_PREFIX="j2se1_4_prefix";
    private static final String CLASSPATH_J2SE1_5_OR_LATER_PREFIX="j2se1_5_or_later_prefix";
    private static final String CLASSPATH_DIR="dir";


    protected ProcessLauncherConfig() {
    }

    /**
     * Overloaded construnctor to intialize the process artifacts into a struncture that can be read
     * by the ProcessLauncher in one shot
     */
    protected ProcessLauncherConfig(String configFile, String process) throws ConfigException {
        initializeConfig(configFile, process);
    }

    /**
     * This method read the processLauncher.xml file and calls a method that loads the named process configuration into
     * the a struncture that can be used by the processlauncher
     */
    protected void initializeConfig(String configFile, String process) throws ConfigException {
        // set internal variables
        _configFile=configFile;
        _process=process;
        _sysProperties=new Properties();
        _strMgr=StringManager.getManager(ProcessLauncherConfig.class);

        try {
            // read in config
            boolean bFoundProcess=false;
            String key=null;
            Document doc=readDOM(configFile);
            Element element=null;
            NodeList nl=doc.getElementsByTagName(PROCESS);

            for(int ii=0;ii < nl.getLength(); ii++) {
                // find correct process
                element=(Element)nl.item(ii);
                key=element.getAttribute("name");
                if(key.equals(process)) {
                    // found correct process, extract information
                    bFoundProcess=true;
                    loadProcess(element);
                }
            }

            if(!bFoundProcess) {
                // error flaguser
                throw new ConfigException(_strMgr.getString("launcher.process_launcher_config_not_found", 
                    new String[]{process, configFile}));
            }
        } catch (ConfigException ce) {
            throw ce;
        } catch (Exception e) {
            throw new ConfigException(_strMgr.getString("launcher.process_launcher_config_exception", _configFile), e);
        }
    }

    /**
     * This methods digests the name process into a struncture that can be used by the ProcessLauncer
     */
    protected void loadProcess(Element process) {
        // read in process
        String key=null, value=null, ifx=null;
        Element element=null;

        // load system properties specificaly for this process
        NodeList nl=process.getChildNodes();
        for(int ii=0;ii < nl.getLength(); ii++) {

            if(nl.item(ii) instanceof Element) {
                element=(Element)nl.item(ii);

                if(element.getTagName().equals(SYSTEM_PROPERTY)) {
                    // system_properties and add to config
                    key=element.getAttribute("key");
                    value=element.getAttribute("value");
                    ifx=element.getAttribute("if");

                    // get attribte always returns "" if it doesn't exist
                    if(!key.equals("")) {
                        // check to see if there is a condition on the property
                        if(ifx.equals("") || System.getProperty(ifx) != null) {
                            // add system properties
                            _sysProperties.setProperty(key, RelativePathResolver.resolvePath(value));
                        }
                    }

                } else if(element.getTagName().equals(MAIN_CLASS)) {
                    // main class add to config
                    _mainClass=element.getAttribute(MAIN_CLASS_CLASSNAME);

                } else if(element.getTagName().equals(CLASSPATH)) {
                    // classpath add to config
                    _classpathLibDir=element.getAttribute(CLASSPATH_DIR);
                    _classpathIncludes=element.getAttribute(CLASSPATH_INCLUDES);
                    _classpathExcludes=element.getAttribute(CLASSPATH_EXCLUDES);
                    _classpathPrefix=element.getAttribute(CLASSPATH_PREFIX);
                    _classpathJ2se14Prefix=element.getAttribute(CLASSPATH_J2SE1_4_PREFIX);
                    _classpathJ2se15OrLaterPrefix=element.getAttribute(CLASSPATH_J2SE1_5_OR_LATER_PREFIX);
                }
            }
        }
    }


    protected String getConfigFile() {
        return _configFile;
    }
    protected String getClasspathLibDir() {
        return _classpathLibDir;
    }
    protected String getClasspathIncludes() {
        return _classpathIncludes;
    }
    protected String getClasspathExcludes() {
        return _classpathExcludes;
    }
    protected String getClasspathPrefix() {
        return _classpathPrefix;
    }
    protected String getClasspathJ2se14Prefix() {
        return _classpathJ2se14Prefix;
    }
    protected String getClasspathJ2se15OrLaterPrefix() {
        return _classpathJ2se15OrLaterPrefix;
    }

    
    
    protected String getMainClass() {
        return _mainClass;
    }
    protected Properties getSystemProperties() {
        return _sysProperties;
    }

    /**
    * readDOM - This method reads in XML into a DOM
    *
    * @param file - A qualified file where to read the XML
    * @return Document - The read in DOM
    * @exception - Any thrown exception that may occur during the read process
    */
    protected Document readDOM(String file) throws SAXException, ParserConfigurationException, IOException {
        DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
        DocumentBuilder db=dbf.newDocumentBuilder();
        return db.parse(new File(file));
    }
}
