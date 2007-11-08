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
 *  SystemLoggerUtilities.java
 */

package com.sun.jbi.jsf.util;

import com.sun.enterprise.tools.admingui.util.AMXUtil;
import com.sun.enterprise.tools.admingui.util.JMXUtil;
import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.config.ModuleLogLevelsConfig;
import java.util.Hashtable;
import java.util.Map;
import java.io.InputStream;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;  
import org.w3c.dom.NodeList;  
import org.w3c.dom.Element;  
import org.w3c.dom.Attr;  
import java.io.IOException;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import javax.xml.parsers.ParserConfigurationException;


/**
 *
 * JBI Contstants related to the JSR-208 Specification
 *
 **/
public final class SystemLoggerUtilities
{

    public static final Hashtable loggerLabels = new Hashtable ();
    static
    {
        loggerLabels.put ("admin",           "Admin");
        loggerLabels.put ("ejb",             "EJB Container");
        loggerLabels.put ("classloader",     "Classloader");
        loggerLabels.put ("configuration",   "Configuration");
        loggerLabels.put ("connector",       "Connector");
        loggerLabels.put ("corba",           "CORBA");
        loggerLabels.put ("deployment",      "Deployment");
        loggerLabels.put ("javamail",        "JavaMail");
        loggerLabels.put ("jaxr",            "JAXR");
        loggerLabels.put ("jaxrpc",          "JAXRPC");
        loggerLabels.put ("jms",             "JMS");
        loggerLabels.put ("jta",             "JTA");
        loggerLabels.put ("jts",             "JTS");
        loggerLabels.put ("mdb",             "MDB Container");
        loggerLabels.put ("naming",          "Naming");
        loggerLabels.put ("root",            "Root");
        loggerLabels.put ("saaj",            "SAAJ");
        loggerLabels.put ("security",        "Security");
        loggerLabels.put ("selfmanagement",  "Self Management");
        loggerLabels.put ("server",          "System");
        loggerLabels.put ("util",            "Util");
        loggerLabels.put ("verifier",        "Verifier");
        loggerLabels.put ("web",             "Web Container");
        loggerLabels.put ("jbi",             "JBI");
        loggerLabels.put ("nodeggent",       "Node Agent");
        loggerLabels.put ("synchronization", "Synchronization");
        loggerLabels.put ("gms",             "Group Management Service");
    }

    public static final Hashtable loggerNames = new Hashtable ();
    static
    {
        loggerNames.put ("javax.enterprise.system.tools.admin",               "Admin");
        loggerNames.put ("javax.enterprise.system.container.ejb",             "EJB");
        loggerNames.put ("javax.enterprise.system.core.classloading",         "Classloader");
        loggerNames.put ("javax.enterprise.system.core.config",               "Configuration");
        loggerNames.put ("javax.enterprise.resource.resourceadapter",         "Connector");
        loggerNames.put ("javax.enterprise.resource.corba",                   "Corba");
        loggerNames.put ("javax.enterprise.system.tools.deployment",          "Deployment");
        loggerNames.put ("javax.enterprise.resource.javamail",                "Javamail");
        loggerNames.put ("javax.enterprise.system.webservices.registry",      "Jaxr");
        loggerNames.put ("javax.enterprise.system.webservices.rpc",           "Jaxrpc");
        loggerNames.put ("javax.enterprise.resource.webservices.jaxws.rpc",   "Jaxws");
        loggerNames.put ("javax.enterprise.resource.jms; javax.resourceadapter.mqjmsra", "Jms");
        loggerNames.put ("javax.enterprise.resource.jta",                     "Jta");
        loggerNames.put ("javax.enterprise.system.core.transaction",          "Jts");
        loggerNames.put ("javax.enterprise.system.container.ejb.mdb",         "MDB");
        loggerNames.put ("javax.enterprise.system.core.naming",               "Naming");
        loggerNames.put ("javax.enterprise",                                  "Root");
        loggerNames.put ("javax.enterprise.system.webservices.saaj",          "Saaj");
        loggerNames.put ("javax.enterprise.system.core.security",             "Security");
        loggerNames.put ("javax.enterprise.system.core.selfmanagement",       "SelfManagement");
        loggerNames.put ("javax.enterprise.system",                           "Server");
        loggerNames.put ("javax.enterprise.system.util",                      "Util");
        loggerNames.put ("javax.enterprise.system.tools.verifier",            "Verifier");
        loggerNames.put ("javax.enterprise.system.container.web; org.apache.catalina; org.apache.coyote; org.apache.jasper;","WEB");
        loggerNames.put ("com.sun.jbi",                                       "JBI");
        loggerNames.put ("javax.ee.enterprise.system.nodeagent",              "NodeAgent");
        loggerNames.put ("javax.ee.enterprise.system.tools.synchronization",  "Synchronization");
        loggerNames.put ("javax.ee.enterprise.system.gms",                    "Gms");
    }

    private static final String JAXWS_MODULE_PROPERTY="javax.enterprise.resource.webservices.jaxws";
    private static final String JBI_MODULE_PROPERTY="com.sun.jbi" ;

    private static Logger sLog = JBILogger.getInstance();


    /**
     * This constructor method is passed an XML file.  It uses the JAXP API to
     * obtain a DOM parser, and to parse the file into a DOM Document object,
     * which is used by the remaining methods of the class.
     **/
    private static Document parseDomDocument (InputStream documentStream)
        throws IOException, SAXException, ParserConfigurationException
    {
        // Get a JAXP parser factory object
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        // Tell the factory what kind of parser we want 
        dbf.setValidating(false);

        // Use the factory to get a JAXP parser object
        javax.xml.parsers.DocumentBuilder parser = dbf.newDocumentBuilder();

        Document document = parser.parse(documentStream);
        return document;
    }


    private static String getLoggerList(String module){
        String[] params = {module};
        String[] types = {"java.lang.String"};

        String logList = "";
        String seperator = "";
        List loggers = (List) JMXUtil.invoke(
                                            "com.sun.appserv:name=logmanager,category=runtime,server=server",
                                            "getLognames4LogModule", params, types);
        if (loggers != null)
        {
            for (int cnt = 0; cnt < loggers.size(); cnt++)
            {
                logList += loggers.get(cnt);
                logList += seperator;
                seperator ="; ";
            }
        }

        return logList;
    }    


    public static Map addAdditionalSystemLoggers (Map aLogLevels, 
                                                  String aComponentName,
                                                  String aTarget,
                                                  String additionalFile) 
    {
        InputStream fileInputStream = null;
        Class c = null;
        Document document = null;
        try {
            c = Class.forName("com.sun.jbi.jsf.util.JBILogLevelsPropertySheetAdaptor");
            fileInputStream = c.getResourceAsStream(additionalFile);
        } 
        catch (Exception ex) {
            sLog.fine("JBILogLevelsPropertySheetAdaptor(): error retrieving input stream for the additional logger xml file.");
            return (aLogLevels);
        }

        // If the inputStream is null, then we can assume that the file xml did not
        // exist.  This is ok, we'll just continue with our processing.
        if (fileInputStream != null)
        {
            try {

                aTarget = ClusterUtilities.getInstanceDomainCluster(aTarget);
                String targetConfig = aTarget.toLowerCase() + "-config";

                document = parseDomDocument(fileInputStream);
                NodeList loggersList = document.getElementsByTagName("additional-loggers");
                int loggersListLength = loggersList.getLength();

                for (int i = 0; i < loggersListLength; i++) {

                    Element componentNameElement = (Element)loggersList.item(i);
                    NodeList componentList = componentNameElement.getElementsByTagName("component-name");
                    int componentListLength = componentList.getLength();
                    
                    for (int j = 0; j < componentListLength; j++) {

                        Element componentElement = (Element)componentList.item(j);
                        Attr componentNameAttr = componentElement.getAttributeNode("value");
                        String componentName = componentNameAttr.getValue();

                        if (aComponentName.equals(componentName))
                        {
                            NodeList moduleList = componentElement.getElementsByTagName("module-name");
                            int moduleListLength = moduleList.getLength();

                            for (int k=0; k<moduleListLength; k++)
                            {
                                Element moduleElement = (Element)moduleList.item(k);
                                Attr moduleNameAttr = moduleElement.getAttributeNode("value");
                                String moduleName = moduleNameAttr.getValue();
                                String listName = moduleName.toLowerCase();

                                sLog.fine("JBILogLevelsPropertySheetAdaptor - addAdditionalSystemLoggers: " +
                                          "componentName=" + aComponentName + 
                                          ", targetName=" + aTarget +
                                          ", moduleName=" + moduleName);

                                String loggerList = getLoggerList(listName);
                                ConfigConfig config = AMXUtil.getConfig(targetConfig);
                                ModuleLogLevelsConfig mConfig = config.getLogServiceConfig().getModuleLogLevelsConfig();
                                
                                String logLevelValue = SystemLoggerUtilities.getLogLevelValue(moduleName,targetConfig);
                                Level systemLogLevel = Level.parse(logLevelValue);
                                aLogLevels.put(loggerList,systemLogLevel);
                            }
                        }
                    }
                }
            }
            catch (IOException ex) {
                sLog.fine("JBILogLevelsPropertySheetAdaptor(): Unable to find theAdditional Logger File.");
            }
            catch (ParserConfigurationException ex) {
                sLog.fine("JBILogLevelsPropertySheetAdaptor(): Error parsing the Additional Logger File.");
            }
            catch (SAXException ex) {
                sLog.fine("JBILogLevelsPropertySheetAdaptor(): Error parsing the Additional Logger File.");
            }
            catch (RuntimeException ex) {
                sLog.fine("JBILogLevelsPropertySheetAdaptor(): Error parsing the Additional Logger File.");
            }
        }
        return (aLogLevels);
    }


    /**
     *	<p> Sets the Component Id using the given id tag string.</p>
     *
     *	@param	aLoggerName  The logger name.
     *	@param	aTargetConfig  The target configuration name
     */
    public static String getLogLevelValue(String aLoggerName, 
                                          String aTargetConfig)
    {
        ConfigConfig config = AMXUtil.getConfig(aTargetConfig);
        ModuleLogLevelsConfig mConfig = config.getLogServiceConfig().getModuleLogLevelsConfig();
        String levelValue = "INFO";

        if (aLoggerName.equalsIgnoreCase("Admin")) {
            levelValue = mConfig.getAdmin();
        }
        else if (aLoggerName.equalsIgnoreCase("Classloader")) {
            levelValue = mConfig.getClassloader();
        }
        else if (aLoggerName.equalsIgnoreCase("Configuration")) {
            levelValue = mConfig.getConfiguration();
        }
        else if (aLoggerName.equalsIgnoreCase("Connector")) {
            levelValue = mConfig.getConnector();
        }
        else if (aLoggerName.equalsIgnoreCase("Corba")) {
            levelValue = mConfig.getCORBA();
        }
        else if (aLoggerName.equalsIgnoreCase("Deployment")) {
            levelValue = mConfig.getDeployment();
        }
        else if (aLoggerName.equalsIgnoreCase("Javamail")) {
            levelValue = mConfig.getJavamail();
        }
        else if (aLoggerName.equalsIgnoreCase("Jaxr")) {
            levelValue = mConfig.getJAXR();
        }
        else if (aLoggerName.equalsIgnoreCase("Jaxrpc")) {
            levelValue = mConfig.getJAXRPC();
        }
        else if (aLoggerName.equalsIgnoreCase("Jms")) {
            levelValue = mConfig.getJMS();
        }
        else if (aLoggerName.equalsIgnoreCase("Jta")) {
            levelValue = mConfig.getJTA();
        }
        else if (aLoggerName.equalsIgnoreCase("Jts")) {
            levelValue = mConfig.getJTS();
        }
        else if (aLoggerName.equalsIgnoreCase("MDB")) {
            levelValue = mConfig.getMDBContainer();
        }
        else if (aLoggerName.equalsIgnoreCase("Naming")) {
            levelValue = mConfig.getNaming();
        }
        else if (aLoggerName.equalsIgnoreCase("EJB")) {
            levelValue = mConfig.getEJBContainer();
        }
        else if (aLoggerName.equalsIgnoreCase("Root")) {
            levelValue = mConfig.getRoot();
        }
        else if (aLoggerName.equalsIgnoreCase("Saaj")) {
            levelValue = mConfig.getSAAJ();
        }
        else if (aLoggerName.equalsIgnoreCase("Security")) {
            levelValue = mConfig.getSecurity();
        }
        else if (aLoggerName.equalsIgnoreCase("Server")) {
            levelValue = mConfig.getServer();
        }
        else if (aLoggerName.equalsIgnoreCase("Util")) {
            levelValue = mConfig.getUtil();
        }
        else if (aLoggerName.equalsIgnoreCase("Verifier")) {
            levelValue = mConfig.getVerifier();
        }
        else if (aLoggerName.equalsIgnoreCase("WEB")) {
            levelValue = mConfig.getWebContainer();
        }
        else if (aLoggerName.equalsIgnoreCase("Jbi")) {
            if (mConfig.existsProperty(JBI_MODULE_PROPERTY))
                levelValue = mConfig.getPropertyValue(JBI_MODULE_PROPERTY);
        }
        else if (aLoggerName.equalsIgnoreCase("Jaxws")) {
            if (mConfig.existsProperty(JAXWS_MODULE_PROPERTY))
                levelValue = mConfig.getPropertyValue(JAXWS_MODULE_PROPERTY);
        }
        else if (aLoggerName.equalsIgnoreCase("NodeAgent")) {
            levelValue = mConfig.getNodeAgent();
        }
        else if (aLoggerName.equalsIgnoreCase("Synchronization")) {
            levelValue = mConfig.getSynchronization();
        }
        else if (aLoggerName.equalsIgnoreCase("Gms")) {
            levelValue = mConfig.getGroupManagementService();
        }
        return levelValue;
    }


    /**
     *	<p> Sets the Component Id using the given id tag string.</p>
     *
     *	@param	aLoggerName  The logger name.
     *	@param	aTargetConfig  The target configuration name
     */
    public static void setLogLevelValue(String aLoggerName, 
                                        String aTarget, 
                                        String aLevel)
    {
        String loggerTagName = (String)loggerNames.get(aLoggerName);

        aTarget = ClusterUtilities.getInstanceDomainCluster(aTarget);
        String targetConfig = aTarget.toLowerCase() + "-config";

        ConfigConfig config = AMXUtil.getConfig(targetConfig);
        ModuleLogLevelsConfig mConfig = config.getLogServiceConfig().getModuleLogLevelsConfig();

        if (loggerTagName.equalsIgnoreCase("Admin")) {
            mConfig.setAdmin(aLevel);
        }
        else if (loggerTagName.equalsIgnoreCase("Classloader")) {
            mConfig.setClassloader(aLevel);
        }
        else if (loggerTagName.equalsIgnoreCase("Configuration")) {
            mConfig.setConfiguration(aLevel);
        }
        else if (loggerTagName.equalsIgnoreCase("Connector")) {
            mConfig.setConnector(aLevel);
        }
        else if (loggerTagName.equalsIgnoreCase("Corba")) {
            mConfig.setCORBA(aLevel);
        }
        else if (loggerTagName.equalsIgnoreCase("Deployment")) {
            mConfig.setDeployment(aLevel);
        }
        else if (loggerTagName.equalsIgnoreCase("Javamail")) {
            mConfig.setJavamail(aLevel);
        }
        else if (loggerTagName.equalsIgnoreCase("Jaxr")) {
            mConfig.setJAXR(aLevel);
        }
        else if (loggerTagName.equalsIgnoreCase("Jaxrpc")) {
            mConfig.setJAXRPC(aLevel);
        }
        else if (loggerTagName.equalsIgnoreCase("Jms")) {
            mConfig.setJMS(aLevel);
        }
        else if (loggerTagName.equalsIgnoreCase("Jta")) {
            mConfig.setJTA(aLevel);
        }
        else if (loggerTagName.equalsIgnoreCase("Jts")) {
            mConfig.setJTS(aLevel);
        }
        else if (loggerTagName.equalsIgnoreCase("MDB")) {
            mConfig.setMDBContainer(aLevel);
        }
        else if (loggerTagName.equalsIgnoreCase("Naming")) {
            mConfig.setNaming(aLevel);
        }
        else if (loggerTagName.equalsIgnoreCase("EJB")) {
            mConfig.setEJBContainer(aLevel);
        }
        else if (loggerTagName.equalsIgnoreCase("Root")) {
            mConfig.setRoot(aLevel);
        }
        else if (loggerTagName.equalsIgnoreCase("Saaj")) {
            mConfig.setSAAJ(aLevel);
        }
        else if (loggerTagName.equalsIgnoreCase("Security")) {
            mConfig.setSecurity(aLevel);
        }
        else if (loggerTagName.equalsIgnoreCase("Server")) {
            mConfig.setServer(aLevel);
        }
        else if (loggerTagName.equalsIgnoreCase("Util")) {
            mConfig.setUtil(aLevel);
        }
        else if (loggerTagName.equalsIgnoreCase("Verifier")) {
            mConfig.setVerifier(aLevel);
        }
        else if (loggerTagName.equalsIgnoreCase("WEB")) {
            mConfig.setWebContainer(aLevel);
        }
        else if (loggerTagName.equalsIgnoreCase("Jbi")) {
            if (mConfig.existsProperty(JBI_MODULE_PROPERTY))
                mConfig.setPropertyValue(JBI_MODULE_PROPERTY, aLevel);
            else
                mConfig.createProperty(JBI_MODULE_PROPERTY, aLevel);
        }
        else if (loggerTagName.equalsIgnoreCase("Jaxws")) {
            if (mConfig.existsProperty(JAXWS_MODULE_PROPERTY))
                mConfig.setPropertyValue(JAXWS_MODULE_PROPERTY, aLevel);
            else
                mConfig.createProperty(JAXWS_MODULE_PROPERTY, aLevel);
        }
        else if (loggerTagName.equalsIgnoreCase("NodeAgent")) {
            mConfig.setNodeAgent(aLevel);
        }
        else if (loggerTagName.equalsIgnoreCase("Synchronization")) {
            mConfig.setSynchronization(aLevel);
        }
        else if (loggerTagName.equalsIgnoreCase("Gms")) {
            mConfig.setGroupManagementService(aLevel);
        }
    }

}
