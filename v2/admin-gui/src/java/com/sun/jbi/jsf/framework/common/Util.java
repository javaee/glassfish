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
 * Util.java
 *   Collection of helper methods
 */

package com.sun.jbi.jsf.framework.common;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.xml.sax.InputSource;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Attr; 
import org.w3c.dom.NodeList;


/**
 *
 * @author ylee
 */
public class Util {
    
    /** Creates a new instance of Util */
    public Util() {
    }
 
    
    public static String replaceInvalidChars(String value) {
        return value.replaceAll("\\.","_");
    }
    
    public static String fixupName(String name, String prefix) {
        if ( name.startsWith(prefix) ) {
            return name.substring(prefix.length()+1);
        } else {
            return name;
        }
    }
    
    public static String mapType(String type) {
        String rtype = type;
        if ( GenericConstants.BC_TYPE.equals(type) ) {
            rtype = GenericConstants.BC_TYPE_MBEAN_NAME;
        } else if ( GenericConstants.SE_TYPE.equals(type) ) {
            rtype = GenericConstants.SE_TYPE_MBEAN_NAME;
        } else if ( GenericConstants.SU_TYPE.equals(type) ) {
            rtype = GenericConstants.SU_TYPE_MBEAN_NAME;
        }
        return rtype;
    }
    
    
    public static String convertType(Object type) {
        String ctype = "";
        if ( type instanceof Boolean ) {
            ctype = type.toString();
        } else {
            ctype = type + "";
        }
        return ctype;
    }
    
    /**
     * @param containerValue
     * @param componentValue
     * @return result of mapping
     * map component values -
     *  if ( container==# ) then return the componentcontainerValue value
     */
    public static String mapComponentValue(String containerValue, String componentValue) {
        if ( GenericConstants.HASH_SEPARATOR.equals(containerValue) ) {
            return componentValue;
        }
        return containerValue;
    }
    
    public static String mapInstalledType(String type) {
        String rtype = type;
        if ( GenericConstants.SE_TYPE.equals(type) ) {
            rtype = GenericConstants.ENGINE_INSTALLED_TYPE;
        } else if ( GenericConstants.BC_TYPE.equals(type) )  {
            rtype = GenericConstants.BINDING_INSTALLED_TYPE;
        }
        return rtype;
    }
        
    
    public static String trimRight(String str, String token) {
        String result="";
        if (str!=null ) {
            result = str.substring(0,str.lastIndexOf(token));
        }
        return result;
    }
    
    /**
     * trim off left side of string including token
     * @param str   string to be trimmed
     * @param token token used to match from the start of string
     * @return trimmed string minus the token part
     */  
    public static String trimLeft(String str,String token) {
        String result=str;
        if ( str!=null ) {
            int index = str.indexOf(token);
            //result = index>=0 ? str.substring(index+token.length()) : str;
            if ( index>=0 ) {
                result = str.substring(index+token.length());
            }
        }
        return result;
    } 
    
    public static String trimLeft2(String str,String token) {
        String result="";
        if ( str!=null ) {
            int index = str.indexOf(token);
            result = index>=0 ? str.substring(token.length()) : str;
        }
        return result;
    } 
        
    
    public static String getNamespace(String endpoint,String token) {
        String result="";
        if ( endpoint!=null ) {
            result = endpoint.substring(0,endpoint.indexOf(token));
        }
        return result;
    }
    

    public static Map getNameSpaces(Node root) {
        Map<String,String> map = new HashMap<String,String>();
        NamedNodeMap attributes = root.getAttributes();
        if(attributes.getLength() == 0) {
            return map;
        }
        for (int index = 0; index < attributes.getLength(); index++) {
            Attr attrib = (Attr)attributes.item(index);
            String attibuteName = attrib.getNodeName();
            int nameSpaceDelimiter = attibuteName.indexOf(":");
            if(nameSpaceDelimiter != -1) {
                map.put(attibuteName.substring(nameSpaceDelimiter+1, attibuteName.length()),
                                attrib.getValue());
            }
        } 
        return map;
    }    
    
    
    public static String resolveNameSpace(Map nameSpace,String qName) {
        int nameSpaceDelimiter = qName.indexOf(":");
        if(nameSpaceDelimiter == -1) {
            return qName; // qname does not have name space
        }
        String ns = qName.substring(0, nameSpaceDelimiter);
        return nameSpace.get(ns)+"^"+qName.substring(nameSpaceDelimiter+1,
                        qName.length());
    }
    
    
    /**
     * @param file   -  name of zip file
     * @param entryName -   name of xml file (jbi.xml)
     * @return the document root element of the xml file
     * @throws Exception
     */
    public static Element getXMLDocumentRoot(ZipFile file,String entryName) throws Exception{
        ZipEntry ze = file.getEntry(entryName);
        InputSource is = new InputSource(file.getInputStream(ze));
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder() ;
        Document xmlDoc = db.parse(is);
        Element root = xmlDoc.getDocumentElement();
        return root;
    }    
    
    /**
     * @param jbiFilePath   -   full path name of the xml file location
     * @return the document root element of xml file
     * @throws Exception
     */
    public static Element getXMLDocumentRoot(String jbiFilePath) throws Exception{
        FileInputStream fis = new FileInputStream(jbiFilePath);
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder() ;
        Document xmlDoc = db.parse(fis);
        Element root = xmlDoc.getDocumentElement();
        return root;
    }    
    
    
    public static String getNodeTextValue(Node node) {
        NodeList nl = node.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node lNode = nl.item(i);
            if(lNode.getNodeType() == Node.TEXT_NODE) {
                return lNode.getNodeValue();
            }
            
        }
        return "";
    }    
    
}
