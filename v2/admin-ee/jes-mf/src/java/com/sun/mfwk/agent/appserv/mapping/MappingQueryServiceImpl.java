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
 * Copyright 2005-2006 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
package com.sun.mfwk.agent.appserv.mapping;

import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import java.io.IOException;
import javax.imageio.metadata.IIOMetadataNode;
import java.lang.reflect.Method;
import java.util.logging.Level;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;

import com.sun.mfwk.agent.appserv.util.Utils;
import com.sun.mfwk.agent.appserv.util.Constants;
import com.sun.mfwk.agent.appserv.modeler.ObjectNameHelper;

class MappingQueryServiceImpl implements MappingQueryService {
    
    public MappingQueryServiceImpl(String uri, String dLocation, String server,
            String domain) throws IOException {
        
        env = new HashMap();
        env.put(Constants.SERVER_NAME_PROP, server);
        env.put(Constants.DOMAIN_NAME_PROP, domain);

        this.doc = Utils.getDocument(uri, dLocation);
        if (this.doc == null) {
            throw new IOException("Unable to read mapping xml file");
        }
    }

    public Object getProperty(String key) {
        return env.get(key);
    }
    
    public NodeList getAS_Mbeans()  {
        if (AS_Mbeans == null) {
            AS_Mbeans = doc.getElementsByTagName(AS_MBEAN_DESCRIPTOR);
        }
        if (keys == null) {
            keys = doc.getElementsByTagName(KEY_MATCHERS);
        }
        return AS_Mbeans;
    }

    private String getKeyProperty(String objectName, String type) 
            throws MalformedObjectNameException {

        ObjectName on = new ObjectName(objectName);
        return on.getKeyProperty(type);
    }
    
    public Element getAS_Mbean(String AS_ObjectName) 
            throws AS_ObjectNameNotFoundException {

        Element elem = null;
        String objectType = null;
        String type = null, typeKey = null;
        NodeList nodes = getAS_Mbeans();

        int keyLength = keys.getLength();
        for (int i = 0; i<keyLength; i++) {
            String key = ((Element)keys.item(i)).getAttribute("key") + "=";
            type = Utils.getStringRegion(AS_ObjectName, key, ",");
            if (type != null)  {
                typeKey =  key;
                break;
            }
        }

        if (type == null) {
            throw new AS_ObjectNameNotFoundException(AS_ObjectName, 
                    AS_ObjectName + " does not have any of the valid keys. Check the key-matcher in mapping xml");
        }

        Map map = ObjectNameHelper.getKeysAndProperties(AS_ObjectName);

        int length = nodes.getLength();
        for (int i = 0; i<length; i++) {
            elem = (Element)nodes.item(i);
            String xmlObjectName = elem.getAttribute(OBJECT_NAME);

            String xmlType = Utils.getStringRegion(xmlObjectName, typeKey, ",");
            if (type.equals(xmlType)) {
                if (ObjectNameHelper.keysMatch(map, xmlObjectName)) {
                    return elem;
                }
            }
        }

        throw new AS_ObjectNameNotFoundException(AS_ObjectName);
    }
    
    public NodeList getCMM_Mbeans(String AS_ObjectName) 
            throws AS_ObjectNameNotFoundException {

        return getCMM_Mbeans(AS_ObjectName, null);
    }   
    

    public NodeList getCMM_Mbeans(String AS_ObjectName, String CMM_Type) 
            throws AS_ObjectNameNotFoundException {

        return getCMM_Mbeans(getAS_Mbean(AS_ObjectName),  
                            AS_ObjectName, CMM_Type );
    }
    
    private NodeList getCMM_Mbeans(Element AS_MbeanDescriptor, 
            String AS_ObjectName, String CMM_Type) {

        String filterClass = AS_MbeanDescriptor.getAttribute(FILTER_CLASS_NAME);

        if ("".equals(filterClass)) filterClass = null;

        if (filterClass == null && CMM_Type == null) {
            return 
                AS_MbeanDescriptor.getElementsByTagName(CMM_MBEAN_DESCRIPTOR);
        }
        else {
            String id = null;
            if (filterClass != null) {
                id = callHandler(filterClass,AS_ObjectName,AS_MbeanDescriptor);
            }
            if (id == null && CMM_Type == null) {
                return AS_MbeanDescriptor.getElementsByTagName(
                                                CMM_MBEAN_DESCRIPTOR);
            }

            NodeList list = 
                AS_MbeanDescriptor.getElementsByTagName(CMM_MBEAN_DESCRIPTOR);
            DocumentFragment fragment = doc.createDocumentFragment();

            for (int i = 0; i<list.getLength(); i++) {

                Element elem = (Element)(list.item(i));
                if ((id == null || id.equals(elem.getAttribute(FILTER_ID))) 
                    && (CMM_Type == null 
                        || CMM_Type.equals(elem.getAttribute(TYPE)))) {              
                    fragment.appendChild(list.item(i).cloneNode(true));
                }
            }

            return fragment.getChildNodes();
        }
     }
    
        
    private String callHandler(String className, String AS_ObjectName, 
            Element elem) {

        try {
            Class handlerClass = Class.forName(className);
            MappingHandler handler = (MappingHandler)handlerClass.newInstance();
            return handler.mappingHandler(AS_ObjectName, elem, this);
        } catch (Exception ex) {
            throw new RuntimeException("Error calling user defined handler",ex);
        }
        
    }
    
    public String getCMM_ObjectName(Element CMM_MbeanDescriptor) {
        return CMM_MbeanDescriptor.getAttribute(OBJECT_NAME);
    }
    
    public Map getAttributeMappings(String AS_ObjectName, String CMM_ObjectName)
            throws AS_ObjectNameNotFoundException, 
            CMM_ObjectNameNotFoundException {

        NodeList CMM_Mbeans = getCMM_Mbeans(AS_ObjectName);
        Map map = new HashMap();
        int length = CMM_Mbeans.getLength();
        for (int i = 0; i<length; i++) {
            getAttributeMappings((Element)(CMM_Mbeans.item(i)), map);
        }

        return map;
    }
    
    public Map getAttributeMappings(Element CMM_MbeanDescriptor) {
            return getAttributeMappings(CMM_MbeanDescriptor, new HashMap());
    }

    public NodeList getDefaultAttributeMappings(Element CMM_MbeanDescriptor) {
        return CMM_MbeanDescriptor.getElementsByTagName(
                                    DEF_ATTRIBUTE_DESCRIPTOR);
    }

    public boolean isAttributeHandlerDefined(Element CMM_MbeanDescriptor, 
            String asAttr, String cmmAttr) {
        
        String handler = 
            getAttributeHandlerClass(CMM_MbeanDescriptor, asAttr, cmmAttr);

        return (handler == null) ? false : true;
    }

    public Map getHandlerAttributeMappings(Element CMM_MbeanDescriptor, 
            String asAttr, String cmmAttr) {

        Element attr = findAttribute(CMM_MbeanDescriptor, asAttr, cmmAttr);

        Map map = new HashMap();
        if (attr != null) {
            NodeList properties=attr.getElementsByTagName(PROPERTY_DESCRIPTOR);
            if (properties != null) {
                int length = properties.getLength();
                for (int i = 0; i<length; i++) {
                    Element property = (Element)properties.item(i);

                    map.put(property.getAttribute(PROPERTY_NAME), 
                        property.getAttribute(PROPERTY_VALUE));
                }
            }
        }

        return map;
    }

    public String getAttributeHandlerClass(Element CMM_MbeanDescriptor, 
            String asAttr, String cmmAttr) {

        String handlerClass = null;

        Element attr = findAttribute(CMM_MbeanDescriptor, asAttr, cmmAttr);
        if (attr != null) {
            NodeList attrHandler=attr.getElementsByTagName(ATTRIBUTE_HANDLER);
            if (attrHandler != null) {
                Element e = (Element)attrHandler.item(0);
                if (e != null) {
                    handlerClass = e.getAttribute(HANDLER_CLASS);
                }
            }
        }

        return handlerClass;
    }

    /**
     * Returns the attribute element that matches the give 
     * AS attribute or the CMM attribute.
     *
     * @param  CMM_MBeanDescriptor  descriptor for CMM mbean
     * @param  asAttr  application server attribute or null if not defined
     * @param  cmmAttr  cmm attribute
     *
     * @return  matching attribute element or null
     */
    private Element findAttribute(Element CMM_MbeanDescriptor, 
            String asAttr, String cmmAttr) {

        NodeList attributes = 
            CMM_MbeanDescriptor.getElementsByTagName(ATTRIBUTE_DESCRIPTOR);

        Element target = null;
        int length = attributes.getLength();
        for (int i = 0; i<length; i++) {
            Element attribute = (Element)attributes.item(i);

            String asName = attribute.getAttribute(AS_ATTRIBUTE_NAME);
            String cmmName = attribute.getAttribute(CMM_ATTRIBUTE_NAME);

            if ( ((asName != null) && (asName.equals(asAttr))) 
                    || ((cmmName != null) && (cmmName.equals(cmmAttr))) ) {
                
                target = attribute;
                break;
            }
        }
        return target;
    }
    
    private Map getAttributeMappings(Element CMM_MbeanDescriptor, Map map) {

        NodeList attributes = 
            CMM_MbeanDescriptor.getElementsByTagName(ATTRIBUTE_DESCRIPTOR);

        int length = attributes.getLength();
        for (int i = 0; i<length; i++) {
            Element attribute = (Element)attributes.item(i);
            map.put(attribute.getAttribute(AS_ATTRIBUTE_NAME), 
                attribute.getAttribute(CMM_ATTRIBUTE_NAME));
        }

        return map;
    }
    
    
    private Map env            = null;
    private Document doc       = null;
    private NodeList AS_Mbeans = null; 
    private NodeList keys      = null;

    private static final String AS_MBEAN_DESCRIPTOR      = "as-mbean";
    private static final String CMM_MBEAN_DESCRIPTOR     = "cmm-mbean";
    private static final String AS_ATTRIBUTE_NAME        = "as-name";
    private static final String CMM_ATTRIBUTE_NAME       = "cmm-name";
    private static final String ATTRIBUTE_DESCRIPTOR     = "attribute";
    private static final String DEF_ATTRIBUTE_DESCRIPTOR = "default-attribute";
    private static final String PROPERTY_DESCRIPTOR      = "property";
    private static final String NAME                     = "name";
    private static final String OBJECT_NAME              = "objectName";
    private static final String TYPE_STR                 = "type";
    private static final String J2EE_TYPE_STR            = "j2eeType";
    private static final String FILTER_CLASS_NAME        = "filter-class";
    private static final String FILTER_ID                = "filter-id";
    private static final String FILTER_METHOD            = "mappingHandler";
    private static final String TYPE                     = "type";
    private static final String KEY_MATCHERS             = "key-matcher";
    private static final String PROPERTY_NAME            = "name";
    private static final String PROPERTY_VALUE           = "value";
    private static final String ATTRIBUTE_HANDLER        = "attribute-handler";
    private static final String HANDLER_CLASS            = "handler-class";

    public static final Class[] HANDLER_ARGS = 
        new Class[] {String.class, Element.class, MappingQueryService.class};    
}
