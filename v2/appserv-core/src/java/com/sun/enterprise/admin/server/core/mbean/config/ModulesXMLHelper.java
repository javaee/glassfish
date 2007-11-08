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

package com.sun.enterprise.admin.server.core.mbean.config;

//Logging imports
import java.util.logging.Logger;
import com.sun.enterprise.admin.server.core.AdminService;

//Config imports
import com.sun.enterprise.config.serverbeans.ServerTags;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

//Keep the following registry at one place - used for deployment
import com.sun.enterprise.deployment.xml.DTDRegistry;
//i18n import
import com.sun.enterprise.admin.common.exception.ControlException;
import com.sun.enterprise.util.i18n.StringManager;

public class ModulesXMLHelper
{
    public static final int MODULE_TYPE_EJB  = 0x01;
    public static final int MODULE_TYPE_WEB  = 0x02;
    public static final int MODULE_TYPE_JAVA = 0x04;
    public static final int MODULE_TYPE_ALL  = 0xff;
    
    public static final int EJB_TYPE_SESSION    = 0x01;
    public static final int EJB_TYPE_ENTITY     = 0x02;
    public static final int EJB_TYPE_MSGDRIVEN  = 0x04;
    public static final int EJB_TYPE_ALL        = 0xff;
    
    //application tags
    static final String APPLICATION_TAG = "application";
    static final String MODULE_TAG      = "module";
    static final String EJB_MODULE_TAG  = "ejb";
    static final String WEB_MODULE_TAG  = "web";
    static final String WEB_URI_TAG     = "web-uri";
    static final String CONTEXT_ROOT_TAG= "context-root";
    static final String JAVA_MODULE_TAG = "java";
    
    //EJB - tags
    static final String ENTERPRISE_BEANS_TAG = "enterprise-beans";
    static final String SESSION_TAG          = "session";
    static final String ENTITY_TAG           = "entity";
    static final String MESSAGE_DRIVEN_TAG   = "message-driven";
    static final String EJB_NAME_TAG         = "ejb-name";
    
    //WEB - tags
    static final String SERVLET_TAG          = "servlet";
    static final String SERVLET_NAME_TAG     = "servlet-name";
    
    static final Logger sLogger = AdminService.sLogger;

    Document document;
    
    public ModulesXMLHelper(String fileName) throws Exception
    {
        document = createDocument(fileName);
    }
    
    public static String[] getModulesFromApplicationLocation(String appLocation, int moduleType) throws Exception
    {
        ModulesXMLHelper myObj = new ModulesXMLHelper(appLocation + "/META-INF/application.xml");
        return myObj.getModules(moduleType);
    }
    
    public static boolean isModuleExists(String appLocation, String moduleName, int moduleType) throws Exception
    {
        String[] strs = getModulesFromApplicationLocation(appLocation, moduleType);
        if(strs!=null)
            for(int i=0; i<strs.length; i++)
                if(moduleName.equals(strs[i]))
                    return true;
        return false;
    }
    
    public static String[] getEnterpriseBeansForEjbModule(String location, String ejbModuleName, int ejbTypes) throws Exception
    {
        if(ejbModuleName!=null)
        { //not standalone - add super directory
            if(ejbModuleName.endsWith(".jar"))
                ejbModuleName = ejbModuleName.substring(0, ejbModuleName.length()-4);
            location = location + "/" + ejbModuleName + "_jar";
        }
        ModulesXMLHelper myObj = new ModulesXMLHelper(location + "/META-INF/ejb-jar.xml");
        return myObj.getEnterpriseBeans(ejbTypes);
    }
    
    public static String[] getServletsForWebModule(String location, String webModuleName) throws Exception
    {
        if(webModuleName!=null)
        { //not standalone - add super directory to appLocation
            if(webModuleName.endsWith(".war"))
                webModuleName = webModuleName.substring(0, webModuleName.length()-4);
            location = location + "/" + webModuleName + "_war";
        }
        ModulesXMLHelper myObj = new ModulesXMLHelper(location + "/WEB-INF/web.xml");
        return myObj.getServlets();
    }
    
    public String[] getModules(int moduleType) throws Exception
    {
        if (document != null)
        {
            ArrayList arr = findChildNodesByName(document.getDocumentElement(), MODULE_TAG);
            String [] strs = new String[arr.size()];
            int noNullCount = 0;
            for (int i=0; i<arr.size(); i++)
            {
                String str = getModuleNameFromNode((Node)arr.get(i), moduleType);
                if(str!=null)
                    strs[noNullCount++] = str;
            }
            String [] res = new String[noNullCount];
            for (int i=0; i<noNullCount; i++)
            {
                res[i] = strs[i];
            }
            return res;
        }
        return new String[0];
        
    }
    
    public String[] getEnterpriseBeans(int ejbType) throws Exception
    {
        if (document != null)
        {
            Node beansListNode = findChildNodeByName(document.getDocumentElement(), ENTERPRISE_BEANS_TAG);
            if(beansListNode!=null)
            {
                ArrayList resList = new ArrayList();
                if((ejbType&EJB_TYPE_SESSION)!=0)
                    addToListEjbNames(resList, beansListNode, SESSION_TAG);
                if((ejbType&EJB_TYPE_SESSION)!=0)
                    addToListEjbNames(resList, beansListNode, ENTITY_TAG);
                if((ejbType&EJB_TYPE_SESSION)!=0)
                    addToListEjbNames(resList, beansListNode, MESSAGE_DRIVEN_TAG);
                String [] res = new String[resList.size()];
                for (int i=0; i<res.length; i++)
                {
                    res[i] = (String)(resList.get(i));
                }
                return res;
                //                return ((String[]) resList.toArray());
            }
        }
        return new String[0];
    }
    
    public String[] getServlets() throws Exception
    {
        if (document != null)
        {
            ArrayList arr = findChildNodesByName(document.getDocumentElement(), SERVLET_TAG);
            String [] res = new String[arr.size()];
            for (int i=0; i<arr.size(); i++)
            {
                Node nameNode = findChildNodeByName((Node)arr.get(i), SERVLET_NAME_TAG);
                res[i] = getTextForNode(nameNode);
            }
            return res;
        }
        return new String[0];
    }
    
    private void addToListEjbNames(ArrayList listToAdd, Node listNode, String tag)
    {
        ArrayList arr = findChildNodesByName(listNode, tag);
        for (int i=0; i<arr.size(); i++)
        {
            Node nameNode = findChildNodeByName((Node)arr.get(i), EJB_NAME_TAG);
            listToAdd.add(getTextForNode(nameNode));
        }
    }
    
    private Document createDocument(String fileName) throws Exception
    {
        DocumentBuilderFactory factory =
        DocumentBuilderFactory.newInstance();
        Document document;
        try
        {
            DocumentBuilder builder = factory.newDocumentBuilder();
            EntityResolver resolver = new AdminEntityResolver();
            builder.setEntityResolver(resolver);
            document = builder.parse(new File(fileName));
        }
        catch (SAXException sxe)
        {
            // Error generated during parsing)
            Exception  x = sxe;
            if (sxe.getException() != null)
                x = sxe.getException();
            sLogger.throwing(getClass().getName(), "createDocument", x);
            throw new ControlException(x.getLocalizedMessage());
            
        }
        catch (ParserConfigurationException pce)
        {
            // Parser with specified options can't be built
            sLogger.throwing(getClass().getName(), "createDocument", pce);
            throw new ControlException(pce.getLocalizedMessage());
        }
        catch (IOException ioe)
        {
            // I/O error
            sLogger.throwing(getClass().getName(), "createDocument", ioe);
            throw new ControlException(ioe.getLocalizedMessage());
        }
        return document;
    }
    
    private ArrayList findChildNodesByName(Node node, String name)
    {
        ArrayList resNodes = new ArrayList();
        for (node = node.getFirstChild(); node != null; node = node.getNextSibling())
        {
            if(node.getNodeName().equalsIgnoreCase(name))
                resNodes.add(node);
        }
        return resNodes;
    }
    
    private Node findChildNodeByName(Node node, String name)
    {
        for (node = node.getFirstChild(); node != null; node = node.getNextSibling())
        {
            if(node.getNodeName().equalsIgnoreCase(name))
                return node;
        }
        return null;
    }
    
    private String getModuleNameFromNode(Node node, int moduleType)
    {
        ArrayList arr;
        if((moduleType&MODULE_TYPE_EJB)!=0 &&
        (arr=findChildNodesByName(node, EJB_MODULE_TAG)).size()>0)
            return  getTextForNode((Node)arr.get(0));
        if((moduleType&MODULE_TYPE_JAVA)!=0 &&
        (arr=findChildNodesByName(node, JAVA_MODULE_TAG)).size()>0)
            return  getTextForNode((Node)arr.get(0));
        if((moduleType&MODULE_TYPE_WEB)!=0 &&
        (arr=findChildNodesByName(node, WEB_MODULE_TAG)).size()>0)
        {
            node = (Node)arr.get(0);
            if((arr=findChildNodesByName(node, WEB_URI_TAG))!=null && 
                    arr.size()>0)
            {
                String desc =  getTextForNode((Node)arr.get(0));
                /*
                // desc = web-uri till now
                String contextRoot = "";
                // now get the context-root - this is helpful 
                arr = findChildNodesByName(node, CONTEXT_ROOT_TAG);
                if (arr != null && arr.size() > 0)
                {
                    contextRoot = getTextForNode((Node) arr.get(0));
                }
                if (contextRoot != null)
                {
                    desc = contextRoot + ":" + desc;
                }
                */
                return desc;
            }
        }
        return null;
    }
    
    private String getTextForNode(Node node)
    {
        for (node = node.getFirstChild(); node != null; node = node.getNextSibling())
        {
            if(node.getNodeType()==Node.TEXT_NODE)
                return node.getNodeValue();
        }
        return null;
    }
}

class AdminEntityResolver implements EntityResolver
{
    static final Logger sLogger = AdminService.sLogger;
	private static StringManager localStrings = StringManager.getManager
      (com.sun.enterprise.admin.server.core.mbean.config.ModulesXMLHelper.class);

    public InputSource resolveEntity(String publicId, String systemId) throws 
            SAXException, IOException
    {
        InputSource is = null;
        String completeDTDPath ="";
        if (completeDTDPath != null)
        {
            /* DTDRegistry returns the paths with a leading '/' which we
            * should get rid of here */
            is = new InputSource 
                    (ClassLoader.getSystemResourceAsStream(completeDTDPath.substring(1)));
            sLogger.finest("publicId = " + publicId);
            sLogger.finest("dtd path = " + completeDTDPath);
        }
        else
        {
            String msg = localStrings.getString
                ("admin.server.core.mbean.config.invalid_public_id", publicId);
            SAXException se = new SAXException(msg);
            sLogger.throwing(getClass().getName(), "resolveEntity", se);
            throw se;
        }
        return is;
    }
}
