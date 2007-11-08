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
 * InitConfTransfer.java
 *
 * Created on June 8, 2004, 2:32 PM
 */

package com.sun.enterprise.tools.upgrade.miscconfig;

import java.io.*;
import com.sun.enterprise.tools.upgrade.common.*;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.tools.upgrade.logging.*;
import java.util.logging.*;
import java.util.*;
import java.lang.reflect.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.w3c.dom.Document;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.OutputKeys;

/**
 *
 * @author  hans
 */
public class InitConfTransfer {
    private CommonInfoModel commonInfo;
    private StringManager stringManager = StringManager.getManager("com.sun.enterprise.tools.upgrade.miscconfig");
    private Logger logger = CommonInfoModel.getDefaultLogger();
    
    /** Creates a new instance of InitConfTransfer */
    public InitConfTransfer(CommonInfoModel cim) {
        commonInfo = cim;
    }
    
    public void transform() {
        logger.log(Level.INFO, stringManager.getString("upgrade.configTransfers.initconf.startMessage"));
        
        String fileName = commonInfo.getSourceInitConfFileName();
        BufferedReader reader = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        //factory.setValidating(true);
        factory.setNamespaceAware(true);
        try {
            DocumentBuilder builderDomainXml = factory.newDocumentBuilder();
            builderDomainXml.setEntityResolver((org.xml.sax.helpers.DefaultHandler)Class.forName
            ("com.sun.enterprise.config.serverbeans.ServerValidationHandler").newInstance());
            Document resultDoc = builderDomainXml.parse( new File(commonInfo.getTargetConfigXMLFile()) );
            reader = new BufferedReader(new FileReader(fileName));
            while (reader.ready()) {
                String line = reader.readLine();
                String key = null;
                String value = null;
                StringTokenizer st = new StringTokenizer(line, " ", false);
                if ( st.hasMoreTokens() ) {
                    key = st.nextToken();
                } else {
                    continue;
                }
                if ( st.hasMoreTokens() ) {
                    value = st.nextToken();
                } else {
                    continue;
                }
                try {
                    Method m = getClass().getMethod("transform" + key, new Class [] { Document.class, String.class });
                    m.invoke(this, new Object [] { resultDoc, value } );
                } catch (NoSuchMethodException nsm) {
                    logger.log(Level.WARNING, stringManager.getString("upgrade.configTransfers.initconf.unsupportedElement") + key);
                } catch (Exception e) {
                    logger.log(Level.WARNING, stringManager.getString("upgrade.configTransfers.initconf.exception") + e.getLocalizedMessage());
                }
            }
            // write out the resultDoc to destination file.
            // Use a Transformer for output
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            if (resultDoc.getDoctype() != null){
                String systemValue = resultDoc.getDoctype().getSystemId();
                transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, systemValue);
                String pubValue = resultDoc.getDoctype().getPublicId();
                transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, pubValue);
            }
            DOMSource source = new DOMSource(resultDoc);
            StreamResult result = new StreamResult(new FileOutputStream(commonInfo.getTargetConfigXMLFile()));
            transformer.transform(source, result);
        } catch (IOException ioe) {
            logger.log(Level.WARNING, stringManager.getString("upgrade.configTransfers.initconf.iofailure") + ioe.getLocalizedMessage());
        } catch (Exception e) {
            logger.log(Level.WARNING, stringManager.getString("upgrade.configTransfers.initconf.exception") + e.getLocalizedMessage());
        }
        if (reader != null) {
            try {
                reader.close();
            } catch (Exception e) {
                logger.log(Level.WARNING, stringManager.getString("upgrade.configTransfers.initconf.iofailure") + e.getLocalizedMessage());
            }
        }
    }
    
    public void transformServerName(Document domainXML, String value) {
        
    }
    
    public void transformServerID(Document domainXML, String value) {
        
    }
    
    public void transformExtraPath(Document domainXML, String value) {
        
    }
    
    public void transformInit(Document domainXML, String value) {
        //NSAPI no longer supported
    }
    
    public void transformNetsiteRoot(Document domainXML, String value) {
        
    }
    
    public void transformDNS(Document domainXML, String value) {
        logger.finest("DNS = " + value);
        Element docEle = domainXML.getDocumentElement();
        NodeList nodeList = docEle.getElementsByTagName("config");
        for (int i=0; i < nodeList.getLength(); i++) {
            Element configElement = (Element)nodeList.item(i);
            String attrValue = configElement.getAttribute("name");
            if (attrValue.equals(commonInfo.getCurrentSourceInstance() + "-config")) {
                NodeList httpServiceNodes = configElement.getElementsByTagName("http-service");
                //there is only one http-service element
                Element httpServiceElement = (Element)httpServiceNodes.item(0);
                NodeList httpList = httpServiceElement.getElementsByTagName("http-protocol");
                // There should be only one http-protocol element.
                Element element = (Element)httpList.item(0);
                //If the element exists in the target, do the transformation
                if (element != null) {
                    if (value.equalsIgnoreCase("off")) {
                        element.setAttribute("dns-lookup-enabled", "false");
                    } else {
                        element.setAttribute("dns-lookup-enabled", "true");
                    }
                }
            }
        }
    }
    
    public void transformAsyncDNS(Document domainXML, String value) {
        //TO DO - don't know what this translates to?
    }
    
    public void transformConnQueueSize(Document domainXML, String value) {
        //http-service/connection-pool:queue-size-bytes
        logger.finest("ConnQueueSize = " + value);
        Element docEle = domainXML.getDocumentElement();
        NodeList nodeList = docEle.getElementsByTagName("config");
        for (int i=0; i < nodeList.getLength(); i++) {
            Element configElement = (Element)nodeList.item(i);
            String attrValue = configElement.getAttribute("name");
            if (attrValue.equals(commonInfo.getCurrentSourceInstance() + "-config")) {
                NodeList httpServiceNodes = configElement.getElementsByTagName("http-service");
                //there is only one http-service element
                Element httpServiceElement = (Element)httpServiceNodes.item(0);
                NodeList subList = httpServiceElement.getElementsByTagName("connection-pool");
                // There can be only one connection-pool element.
                Element element = (Element)subList.item(0);
                //If the element exists in the target, do the transformation
                if (element != null) {
                    //Fix for CR 6461070
                    element.setAttribute("queue-size-in-bytes", value);
                    //element.setAttribute("queue-size-bytes", value);
                    //End - 6461070
                }
            }
        }
    }
    
    public void transformHeaderBufferSize(Document domainXML, String value) {
        //http-service/request-processing:header-buffer-size-bytes
        logger.finest("HeaderBufferSize = " + value);
        Element docEle = domainXML.getDocumentElement();
        NodeList nodeList = docEle.getElementsByTagName("config");
        for (int i=0; i < nodeList.getLength(); i++) {
            Element configElement = (Element)nodeList.item(i);
            String attrValue = configElement.getAttribute("name");
            if (attrValue.equals(commonInfo.getCurrentSourceInstance() + "-config")) {
                NodeList httpServiceNodes = configElement.getElementsByTagName("http-service");
                //there is only one http-service element
                Element httpServiceElement = (Element)httpServiceNodes.item(0);
                NodeList subList = httpServiceElement.getElementsByTagName("request-processing");
                // There can be only one request-processing element.
                Element element = (Element)subList.item(0);
                //If the element exists in the target, do the transformation
                if (element != null) {
                    element.setAttribute("header-buffer-size-bytes", value);
                }
            }
        }
    }
    
    public void transformIOTimeout(Document domainXML, String value) {
        //http-service/request-processing:request-timeout-in-seconds
        logger.finest("IOTimeout = " + value);
        Element docEle = domainXML.getDocumentElement();
        NodeList nodeList = docEle.getElementsByTagName("config");
        for (int i=0; i < nodeList.getLength(); i++) {
            Element configElement = (Element)nodeList.item(i);
            String attrValue = configElement.getAttribute("name");
            if (attrValue.equals(commonInfo.getCurrentSourceInstance() + "-config")) {
                NodeList httpServiceNodes = configElement.getElementsByTagName("http-service");
                //there is only one http-service element.
                Element httpServiceElement = (Element)httpServiceNodes.item(0);
                NodeList subList = httpServiceElement.getElementsByTagName("request-processing");
                // There can be only one request-processing element.
                Element element = (Element)subList.item(0);
                //If the element exists in the target, do the transformation
                if (element != null) {
                    element.setAttribute("request-timeout-in-seconds", value);
                }
            }
        }
    }
    
    public void transformKeepAliveThreads(Document domainXML, String value) {
        //http-service/keep-alive:keep-alive-thread-count
        logger.finest("KeepAliveThreads = " + value);
        Element docEle = domainXML.getDocumentElement();
        NodeList nodeList = docEle.getElementsByTagName("config");
        for (int i=0; i < nodeList.getLength(); i++) {
            Element configElement = (Element)nodeList.item(i);
            String attrValue = configElement.getAttribute("name");
            if (attrValue.equals(commonInfo.getCurrentSourceInstance() + "-config")) {
                NodeList httpServiceNodes = configElement.getElementsByTagName("http-service");
                //there is only one http-service element
                Element httpServiceElement = (Element)httpServiceNodes.item(0);
                NodeList subList = httpServiceElement.getElementsByTagName("keep-alive");
                // There can be only one request-processing element.
                Element element = (Element)subList.item(0);
                //If the element exists in the target, do the transformation
                if (element != null) {
                    element.setAttribute("keep-alive-thread-count", value);
                }
            }
        }
    }
    
    public void transformKeepAliveTimeout(Document domainXML, String value) {
        //http-service/keep-alive:timeout-in-seconds
        logger.finest("KeepAliveTimeout = " + value);
        Element docEle = domainXML.getDocumentElement();
        NodeList nodeList = docEle.getElementsByTagName("config");
        for (int i=0; i < nodeList.getLength(); i++) {
            Element configElement = (Element)nodeList.item(i);
            String attrValue = configElement.getAttribute("name");
            if (attrValue.equals(commonInfo.getCurrentSourceInstance() + "-config")) {
                NodeList httpServiceNodes = configElement.getElementsByTagName("http-service");
                //there is only one http-service element
                Element httpServiceElement = (Element)httpServiceNodes.item(0);
                NodeList subList = httpServiceElement.getElementsByTagName("keep-alive");
                // There can be only one request-processing element.
                Element element = (Element)subList.item(0);
                //If the element exists in the target, do the transformation
                if (element != null) {
                    element.setAttribute("timeout-in-seconds", value);
                }
            }
        }
    }
    
    public void transformKernelThreads(Document domainXML, String value) {
        //unsupported
    }
    
    public void transformListenQ(Document domainXML, String value) {
        //http-service/connection-pool:max-pending-count
        logger.finest("ListenQ = " + value);
        Element docEle = domainXML.getDocumentElement();
        NodeList nodeList = docEle.getElementsByTagName("config");
        for (int i=0; i < nodeList.getLength(); i++) {
            Element configElement = (Element)nodeList.item(i);
            String attrValue = configElement.getAttribute("name");
            if (attrValue.equals(commonInfo.getCurrentSourceInstance() + "-config")) {
                NodeList httpServiceNodes = configElement.getElementsByTagName("http-service");
                //there is only one http-service element
                Element httpServiceElement = (Element)httpServiceNodes.item(0);
                NodeList subList = httpServiceElement.getElementsByTagName("connection-pool");
                // There can be only one connection-pool element.
                Element element = (Element)subList.item(0);
                //If the element exists in the target, do the transformation
                if (element != null) {
                    element.setAttribute("max-pending-count", value);
                }
            }
        }
    }
    
    public void transformRcvBufSize(Document domainXML, String value) {
        //http-service/connection-pool:receive-buffer-size-bytes
        logger.finest("RcvBufSize = " + value);
        Element docEle = domainXML.getDocumentElement();
        NodeList nodeList = docEle.getElementsByTagName("config");
        for (int i=0; i < nodeList.getLength(); i++) {
            Element configElement = (Element)nodeList.item(i);
            String attrValue = configElement.getAttribute("name");
            if (attrValue.equals(commonInfo.getCurrentSourceInstance() + "-config")) {
                NodeList httpServiceNodes = configElement.getElementsByTagName("http-service");
                //there is only one http-service element
                Element httpServiceElement = (Element)httpServiceNodes.item(0);
                NodeList subList = httpServiceElement.getElementsByTagName("connection-pool");
                // There can be only one connection-pool element.
                Element element = (Element)subList.item(0);
                //If the element exists in the target, do the transformation
                if (element != null) {
                    element.setAttribute("receive-buffer-size-bytes", value);
                }
            }
        }
    }
    
    public void transformMaxKeepAliveConnections(Document domainXML, String value) {
        //http-service/keep-alive:timeout-in-seconds
        logger.finest("MaxKeepAliveConnections = " + value);
        Element docEle = domainXML.getDocumentElement();
        NodeList nodeList = docEle.getElementsByTagName("config");
        for (int i=0; i < nodeList.getLength(); i++) {
            Element configElement = (Element)nodeList.item(i);
            String attrValue = configElement.getAttribute("name");
            if (attrValue.equals(commonInfo.getCurrentSourceInstance() + "-config")) {
                NodeList httpServiceNodes = configElement.getElementsByTagName("http-service");
                //there is only one http-service element
                Element httpServiceElement = (Element)httpServiceNodes.item(0);
                NodeList subList = httpServiceElement.getElementsByTagName("keep-alive");
                // There can be only one request-processing element.
                Element element = (Element)subList.item(0);
                //If the element exists in the target, do the transformation
                if (element != null) {
                    element.setAttribute("max-keep-alive-connections", value);
                }
            }
        }
    }
    
    public void transformRqThrottle(Document domainXML, String value) {
        //http-service/request-processing:thread-count
        logger.finest("RqThrottle = " + value);
        Element docEle = domainXML.getDocumentElement();
        NodeList nodeList = docEle.getElementsByTagName("config");
        for (int i=0; i < nodeList.getLength(); i++) {
            Element configElement = (Element)nodeList.item(i);
            String attrValue = configElement.getAttribute("name");
            if (attrValue.equals(commonInfo.getCurrentSourceInstance() + "-config")) {
                NodeList httpServiceNodes = configElement.getElementsByTagName("http-service");
                //there is only one http-service element
                Element httpServiceElement = (Element)httpServiceNodes.item(0);
                NodeList subList = httpServiceElement.getElementsByTagName("request-processing");
                // There can be only one request-processing element.
                Element element = (Element)subList.item(0);
                //If the element exists in the target, do the transformation
                if (element != null) {
                    element.setAttribute("thread-count", value);
                }
            }
        }
    }
    
    public void transformRqThrottleMin(Document domainXML, String value) {
        //http-service/request-processing:initial-thread-count
        logger.finest("RqThrottleMin = " + value);
        Element docEle = domainXML.getDocumentElement();
        NodeList nodeList = docEle.getElementsByTagName("config");
        for (int i=0; i < nodeList.getLength(); i++) {
            Element configElement = (Element)nodeList.item(i);
            String attrValue = configElement.getAttribute("name");
            if (attrValue.equals(commonInfo.getCurrentSourceInstance() + "-config")) {
                NodeList httpServiceNodes = configElement.getElementsByTagName("http-service");
                //there is only one http-service element
                Element httpServiceElement = (Element)httpServiceNodes.item(0);
                NodeList subList = httpServiceElement.getElementsByTagName("request-processing");
                // There can be only one request-processing element.
                Element element = (Element)subList.item(0);
                //If the element exists in the target, do the transformation
                if (element != null) {
                    element.setAttribute("initial-thread-count", value);
                }
            }
        }
    }
    
    public void transformSndBufSize(Document domainXML, String value) {
        //http-service/connection-pool:send-buffer-size-bytes
        logger.finest("SndBufSize = " + value);
        Element docEle = domainXML.getDocumentElement();
        NodeList nodeList = docEle.getElementsByTagName("config");
        for (int i=0; i < nodeList.getLength(); i++) {
            Element configElement = (Element)nodeList.item(i);
            String attrValue = configElement.getAttribute("name");
            if (attrValue.equals(commonInfo.getCurrentSourceInstance() + "-config")) {
                NodeList httpServiceNodes = configElement.getElementsByTagName("http-service");
                //there is only one http-service element
                Element httpServiceElement = (Element)httpServiceNodes.item(0);
                NodeList subList = httpServiceElement.getElementsByTagName("connection-pool");
                // There can be only one connection-pool element.
                Element element = (Element)subList.item(0);
                //If the element exists in the target, do the transformation
                if (element != null) {
                    element.setAttribute("send-buffer-size-bytes", value);
                }
            }
        }
    }
    
    public void transformStackSize(Document domainXML, String value) {
        //http-service
        //property "stack-size"
        //specified as a name value pair in the property element under http-service.
        String name = "stack-size";
        logger.finest("StackSize = " + value);
        Element docEle = domainXML.getDocumentElement();
        NodeList nodeList = docEle.getElementsByTagName("config");
        for (int i=0; i < nodeList.getLength(); i++) {
            Element configElement = (Element)nodeList.item(i);
            String attrValue = configElement.getAttribute("name");
            if (attrValue.equals(commonInfo.getCurrentSourceInstance() + "-config")) {
                NodeList httpServiceNodes = configElement.getElementsByTagName("http-service");
                //there is only one http-service element
                Element httpServiceElement = (Element)httpServiceNodes.item(0);
                NodeList resultProperties = httpServiceElement.getElementsByTagName("property");
                Element resultProperty = null;
                if(resultProperties != null){
                    for(int index=0; index < resultProperties.getLength(); index++){
                        if(((Element)resultProperties.item(index)).getAttribute("name").equals(name)){
                            resultProperty = (Element)resultProperties.item(index);
                            resultProperty.getAttributeNode("value").setValue(value);
                            //this.handleSpecialCases(element, resultProperty, parentSource, parentResult);
                            break;
                        }
                    }
                }
                if(resultProperty == null){
                    resultProperty = httpServiceElement.getOwnerDocument().createElement("property");
                    resultProperty.setAttribute("name", name);
                    resultProperty.setAttribute("value", value);
                    httpServiceElement.appendChild(resultProperty);
                }
            }
        }
    }
    
    public void transformStrictHttpHeaders(Document domainXML, String value) {
        //not exposed in domain.xml
    }
    
    public void transformTerminateTimeout(Document domainXML, String value) {
        //not supported
    }
    
    public void transformUser(Document domainXML, String value) {
        //not supported
    }
    
    public void transformTempDir(Document domainXML, String value) {
        //not supported
    }
    
    public void transformThreadIncrement(Document domainXML, String value) {
        //http-service/request-processing:thread-increment
        logger.finest("ThreadIncrement = " + value);
        Element docEle = domainXML.getDocumentElement();
        NodeList nodeList = docEle.getElementsByTagName("config");
        for (int i=0; i < nodeList.getLength(); i++) {
            Element configElement = (Element)nodeList.item(i);
            String attrValue = configElement.getAttribute("name");
            if (attrValue.equals(commonInfo.getCurrentSourceInstance() + "-config")) {
                NodeList httpServiceNodes = configElement.getElementsByTagName("http-service");
                //there is only one http-service element
                Element httpServiceElement = (Element)httpServiceNodes.item(0);
                NodeList subList = httpServiceElement.getElementsByTagName("request-processing");
                // There can be only one request-processing element.
                Element element = (Element)subList.item(0);
                //If the element exists in the target, do the transformation
                if (element != null) {
                    element.setAttribute("thread-increment", value);
                }
            }
        }
    }
    
    public void transformNativePoolStackSize(Document domainXML, String value) {
    }
    
    public void transformNativePoolMaxThreads(Document domainXML, String value) {
    }
    
    public void transformNativePoolMinThreads(Document domainXML, String value) {
    }
    
    public void transformNativePoolQueueSize(Document domainXML, String value) {
    }
    
    public void transformErrorLogDateFormat(Document domainXML, String value) {
    }
    
    public void transformLogFlushInterval(Document domainXML, String value) {
    }
    
    public void transformPidLog(Document domainXML, String value) {
    }
    
    public void transformSecurity(Document domainXML, String value) {
        //http-service/http-protocol:ssl-enabled
        logger.finest("Security = " + value);
        Element docEle = domainXML.getDocumentElement();
        NodeList nodeList = docEle.getElementsByTagName("config");
        for (int i=0; i < nodeList.getLength(); i++) {
            Element configElement = (Element)nodeList.item(i);
            String attrValue = configElement.getAttribute("name");
            if (attrValue.equals(commonInfo.getCurrentSourceInstance() + "-config")) {
                NodeList httpServiceNodes = configElement.getElementsByTagName("http-service");
                //there is only one http-service element
                Element httpServiceElement = (Element)httpServiceNodes.item(0);
                NodeList httpList = httpServiceElement.getElementsByTagName("http-protocol");
                // There should be only one http-protocol element.
                Element element = (Element)httpList.item(0);
                //If the element exists in the target, do the transformation
                if (element != null) {
                    if (value.equalsIgnoreCase("off")) {
                        element.setAttribute("ssl-enabled", "false");
                    } else {
                        element.setAttribute("ssl-enabled", "true");
                    }
                }
            }
        }
    }
    
    public void transformSSLCacheEntries(Document domainXML, String value) {
        //http-service
        //property "ssl-cache-entries"
        //specified as a name value pair in the property element under http-service.
        String name = "ssl-cache-entries";
        logger.finest("SSLCacheEntries = " + value);
        Element docEle = domainXML.getDocumentElement();
        NodeList nodeList = docEle.getElementsByTagName("config");
        for (int i=0; i < nodeList.getLength(); i++) {
            Element configElement = (Element)nodeList.item(i);
            String attrValue = configElement.getAttribute("name");
            if (attrValue.equals(commonInfo.getCurrentSourceInstance() + "-config")) {
                NodeList httpServiceNodes = configElement.getElementsByTagName("http-service");
                //there is only one http-service element
                Element httpServiceElement = (Element)httpServiceNodes.item(0);
                NodeList resultProperties = httpServiceElement.getElementsByTagName("property");
                Element resultProperty = null;
                if(resultProperties != null){
                    for(int index=0; index < resultProperties.getLength(); index++){
                        if(((Element)resultProperties.item(index)).getAttribute("name").equals(name)){
                            resultProperty = (Element)resultProperties.item(index);
                            resultProperty.getAttributeNode("value").setValue(value);
                            //this.handleSpecialCases(element, resultProperty, parentSource, parentResult);
                            break;
                        }
                    }
                }
                if(resultProperty == null){
                    resultProperty = httpServiceElement.getOwnerDocument().createElement("property");
                    resultProperty.setAttribute("name", name);
                    resultProperty.setAttribute("value", value);
                    httpServiceElement.appendChild(resultProperty);
                }
            }
        }
    }
    
    public void transformSSLClientAuthDataLimit(Document domainXML, String value) {
        //http-service
        //property "ssl-client-auth-data-limit"
        //specified as a name value pair in the property element under http-service.
        String name = "ssl-client-auth-data-limit";
        logger.finest("SSLClientAuthDataLimit = " + value);
        Element docEle = domainXML.getDocumentElement();
        NodeList nodeList = docEle.getElementsByTagName("config");
        for (int i=0; i < nodeList.getLength(); i++) {
            Element configElement = (Element)nodeList.item(i);
            String attrValue = configElement.getAttribute("name");
            if (attrValue.equals(commonInfo.getCurrentSourceInstance() + "-config")) {
                NodeList httpServiceNodes = configElement.getElementsByTagName("http-service");
                //there is only one http-service element
                Element httpServiceElement = (Element)httpServiceNodes.item(0);
                NodeList resultProperties = httpServiceElement.getElementsByTagName("property");
                Element resultProperty = null;
                if(resultProperties != null){
                    for(int index=0; index < resultProperties.getLength(); index++){
                        if(((Element)resultProperties.item(index)).getAttribute("name").equals(name)){
                            resultProperty = (Element)resultProperties.item(index);
                            resultProperty.getAttributeNode("value").setValue(value);
                            //this.handleSpecialCases(element, resultProperty, parentSource, parentResult);
                            break;
                        }
                    }
                }
                if(resultProperty == null){
                    resultProperty = httpServiceElement.getOwnerDocument().createElement("property");
                    resultProperty.setAttribute("name", name);
                    resultProperty.setAttribute("value", value);
                    httpServiceElement.appendChild(resultProperty);
                }
            }
        }
    }
    
    public void transformSSLClientAuthTimeout(Document domainXML, String value) {
        //http-service
        //property "ssl-client-auth-timeout"
        //specified as a name value pair in the property element under http-service.
        String name = "ssl-client-auth-timeout";
        logger.finest("SSLClientAuthTimeout = " + value);
        Element docEle = domainXML.getDocumentElement();
        NodeList nodeList = docEle.getElementsByTagName("config");
        for (int i=0; i < nodeList.getLength(); i++) {
            Element configElement = (Element)nodeList.item(i);
            String attrValue = configElement.getAttribute("name");
            if (attrValue.equals(commonInfo.getCurrentSourceInstance() + "-config")) {
                NodeList httpServiceNodes = configElement.getElementsByTagName("http-service");
                //there is only one http-service element
                Element httpServiceElement = (Element)httpServiceNodes.item(0);
                NodeList resultProperties = httpServiceElement.getElementsByTagName("property");
                Element resultProperty = null;
                if(resultProperties != null){
                    for(int index=0; index < resultProperties.getLength(); index++){
                        if(((Element)resultProperties.item(index)).getAttribute("name").equals(name)){
                            resultProperty = (Element)resultProperties.item(index);
                            resultProperty.getAttributeNode("value").setValue(value);
                            //this.handleSpecialCases(element, resultProperty, parentSource, parentResult);
                            break;
                        }
                    }
                }
                if(resultProperty == null){
                    resultProperty = httpServiceElement.getOwnerDocument().createElement("property");
                    resultProperty.setAttribute("name", name);
                    resultProperty.setAttribute("value", value);
                    httpServiceElement.appendChild(resultProperty);
                }
            }
        }
    }
    
    public void transformSSLSessionTimeout(Document domainXML, String value) {
        //http-service
        //property "ssl-session-timeout"
        //specified as a name value pair in the property element under http-service.
        String name = "ssl-session-timeout";
        logger.finest("SSLSessionTimeout = " + value);
        Element docEle = domainXML.getDocumentElement();
        NodeList nodeList = docEle.getElementsByTagName("config");
        for (int i=0; i < nodeList.getLength(); i++) {
            Element configElement = (Element)nodeList.item(i);
            String attrValue = configElement.getAttribute("name");
            if (attrValue.equals(commonInfo.getCurrentSourceInstance() + "-config")) {
                NodeList httpServiceNodes = configElement.getElementsByTagName("http-service");
                //there is only one http-service element
                Element httpServiceElement = (Element)httpServiceNodes.item(0);
                NodeList resultProperties = httpServiceElement.getElementsByTagName("property");
                Element resultProperty = null;
                if(resultProperties != null){
                    for(int index=0; index < resultProperties.getLength(); index++){
                        if(((Element)resultProperties.item(index)).getAttribute("name").equals(name)){
                            resultProperty = (Element)resultProperties.item(index);
                            resultProperty.getAttributeNode("value").setValue(value);
                            //this.handleSpecialCases(element, resultProperty, parentSource, parentResult);
                            break;
                        }
                    }
                }
                if(resultProperty == null){
                    resultProperty = httpServiceElement.getOwnerDocument().createElement("property");
                    resultProperty.setAttribute("name", name);
                    resultProperty.setAttribute("value", value);
                    httpServiceElement.appendChild(resultProperty);
                }
            }
        }
    }
    
    public void transformSSL3SessionTimeout(Document domainXML, String value) {
        //http-service
        //property "ssl3-session-timeout"
        //specified as a name value pair in the property element under http-service.
        String name = "ssl3-session-timeout";
        logger.finest("SSL3SessionTimeout = " + value);
        Element docEle = domainXML.getDocumentElement();
        NodeList nodeList = docEle.getElementsByTagName("config");
        for (int i=0; i < nodeList.getLength(); i++) {
            Element configElement = (Element)nodeList.item(i);
            String attrValue = configElement.getAttribute("name");
            if (attrValue.equals(commonInfo.getCurrentSourceInstance() + "-config")) {
                NodeList httpServiceNodes = configElement.getElementsByTagName("http-service");
                //there is only one http-service element
                Element httpServiceElement = (Element)httpServiceNodes.item(0);
                NodeList resultProperties = httpServiceElement.getElementsByTagName("property");
                Element resultProperty = null;
                if(resultProperties != null){
                    for(int index=0; index < resultProperties.getLength(); index++){
                        if(((Element)resultProperties.item(index)).getAttribute("name").equals(name)){
                            resultProperty = (Element)resultProperties.item(index);
                            resultProperty.getAttributeNode("value").setValue(value);
                            //this.handleSpecialCases(element, resultProperty, parentSource, parentResult);
                            break;
                        }
                    }
                }
                if(resultProperty == null){
                    resultProperty = httpServiceElement.getOwnerDocument().createElement("property");
                    resultProperty.setAttribute("name", name);
                    resultProperty.setAttribute("value", value);
                    httpServiceElement.appendChild(resultProperty);
                }
            }
        }
    }
    
    public void transformHTTPVersion(Document domainXML, String value) {
        //http-service/http-protocol:http-version
        logger.finest("HTTPVersion = " + value);
        Element docEle = domainXML.getDocumentElement();
        NodeList nodeList = docEle.getElementsByTagName("config");
        for (int i=0; i < nodeList.getLength(); i++) {
            Element configElement = (Element)nodeList.item(i);
            String attrValue = configElement.getAttribute("name");
            if (attrValue.equals(commonInfo.getCurrentSourceInstance() + "-config")) {
                NodeList httpServiceNodes = configElement.getElementsByTagName("http-service");
                //there is only one http-service element
                Element httpServiceElement = (Element)httpServiceNodes.item(0);
                NodeList httpList = httpServiceElement.getElementsByTagName("http-protocol");
                // There should be only one http-protocol element.
                Element element = (Element)httpList.item(0);
                //If the element exists in the target, do the transformation
                if (element != null) {
                    element.setAttribute("http-version", value);
                }
            }
        }
    }
    
 /*   
    public static void main(String [] args) {
        try{
            com.sun.enterprise.tools.upgrade.logging.LogService.initialize("upgradetest.log");
        
        }catch(Exception e){
            e.printStackTrace();
        }        
        CommonInfoModel cim = new CommonInfoModel();
        cim.setSourceInstallDir("C:\\Sun\\AppServer7");
        cim.setTargetInstallDir("C:\\Sun\\AppServer81ee");
        cim.setCurrentDomain("domain1");
        cim.setCurrentSourceInstance("server1");
        cim.setTargetDomainRoot("C:\\Sun\\AppServer81ee\\domains");
        java.util.Hashtable ht = new java.util.Hashtable();
        ht.put("domain1", "C:\\Sun\\AppServer7\\domains\\domain1");
        cim.setDomainMapping(ht);
        cim.enlistDomainsFromSource();
        cim.setAdminPassword("adminadmin");
        new InitConfTransfer(cim).transform();
    }
  */
 
}
