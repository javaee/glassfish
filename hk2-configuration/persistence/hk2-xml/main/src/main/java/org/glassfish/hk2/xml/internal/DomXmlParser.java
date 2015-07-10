/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.xml.internal;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.xml.bind.Unmarshaller.Listener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.glassfish.hk2.utilities.reflection.ClassReflectionHelper;
import org.glassfish.hk2.xml.jaxb.internal.BaseHK2JAXBBean;
import org.glassfish.hk2.xml.spi.XmlServiceParser;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * @author jwells
 *
 */
@Singleton
public class DomXmlParser implements XmlServiceParser {
    @Inject
    private Provider<XmlServiceImpl> xmlService;
    
    private final DocumentBuilder documentBuilder;
    
    private DomXmlParser() throws ParserConfigurationException {
        documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.spi.XmlServiceParser#parseRoot(java.lang.Class, java.net.URI, javax.xml.bind.Unmarshaller.Listener)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T parseRoot(Class<T> clazz, URI location, Listener listener)
            throws Exception {
        ClassReflectionHelper classReflectionHelper = xmlService.get().getClassReflectionHelper();
        
        BaseHK2JAXBBean hk2Root = Utilities.createBean(clazz);
        hk2Root._setClassReflectionHelper(classReflectionHelper);
        
        InputStream urlStream = location.toURL().openStream();
        Document document;
        try {
            document = documentBuilder.parse(urlStream);
        }
        finally {
            urlStream.close();
        }
         
        Element docElement = document.getDocumentElement();
        
        handleElement(hk2Root, null,
                docElement, classReflectionHelper, listener);
        
        return (T) hk2Root;
    }
    
    private void handleNode(Node childNode,
            Model model,
            BaseHK2JAXBBean target,
            Listener listener,
            ClassReflectionHelper classReflectionHelper,
            Map<String, List<BaseHK2JAXBBean>> listChildren,
            Map<String, List<BaseHK2JAXBBean>> arrayChildren) {
        if (childNode instanceof Element) {
        
            Element childElement = (Element) childNode;
            String tagName = childElement.getTagName();
            
            if (model.getNonChildProperties().keySet().contains(tagName)) {
                Class<?> childType = model.getNonChildType(tagName);
                
                NodeList childNodeChildren = childElement.getChildNodes();
                
                String valueString = null;
                for (int lcv1 = 0; lcv1 < childNodeChildren.getLength(); lcv1++) {
                    Node childNodeChild = childNodeChildren.item(lcv1);
                    if (childNodeChild instanceof Text) {
                        Text childText = (Text) childNodeChild;
                        
                        valueString = childText.getTextContent().trim();
                        
                        break;
                    }
                }
                
                Object convertedValue = Utilities.getDefaultValue(valueString, childType);
                target._setProperty(tagName, convertedValue);
            }
            else if (model.getKeyedChildren().contains(tagName) ||
                     model.getUnKeyedChildren().contains(tagName)) {
                ParentedModel informedChild = model.getChild(tagName);
                Model grandChild = informedChild.getChildModel();
                
                BaseHK2JAXBBean hk2Root = Utilities.createBean(grandChild.getProxyAsClass());
                hk2Root._setClassReflectionHelper(classReflectionHelper);
                
                handleElement(hk2Root, target, childElement, classReflectionHelper, listener);
                
                if (informedChild.getChildType().equals(ChildType.DIRECT)) {
                    target._setProperty(tagName, hk2Root);
                }
                else if (informedChild.getChildType().equals(ChildType.LIST)) {
                    List<BaseHK2JAXBBean> cList = listChildren.get(tagName);
                    if (cList == null) {
                        cList = new ArrayList<BaseHK2JAXBBean>();
                        listChildren.put(tagName, cList);
                    }
                    cList.add(hk2Root);
                }
                else if (informedChild.getChildType().equals(ChildType.ARRAY)) {
                    List<BaseHK2JAXBBean> cList = arrayChildren.get(tagName);
                    if (cList == null) {
                        cList = new LinkedList<BaseHK2JAXBBean>();
                        arrayChildren.put(tagName, cList);
                    }
                    cList.add(hk2Root);
                }
            }
            else {
                // Probably just ignore it
            }
        }
        else if (childNode instanceof Attr) {
            Attr childAttr = (Attr) childNode;
            String tagName = childAttr.getName();
            
            if (model.getNonChildProperties().keySet().contains(tagName)) {
                Class<?> childType = model.getNonChildType(tagName);
                String sValue = childAttr.getValue();
                
                Object convertedValue = Utilities.getDefaultValue(sValue, childType);
                target._setProperty(tagName, convertedValue);
            }
            
        }
    }
    
    private <T> void handleElement(BaseHK2JAXBBean target, BaseHK2JAXBBean parent,
            Element element, ClassReflectionHelper classReflectionHelper, Listener listener) {
        listener.beforeUnmarshal(target, parent);
        
        Map<String, List<BaseHK2JAXBBean>> listChildren = new HashMap<String, List<BaseHK2JAXBBean>>();
        Map<String, List<BaseHK2JAXBBean>> arrayChildren = new HashMap<String, List<BaseHK2JAXBBean>>();
        
        Model model = target._getModel();
        
        NamedNodeMap attributeMap = element.getAttributes();
        for (int lcv = 0; lcv < attributeMap.getLength(); lcv++) {
            Node childNode = attributeMap.item(lcv);
            
            handleNode(childNode, model, target, listener, classReflectionHelper, listChildren, arrayChildren);
        }
        
        NodeList beanChildren = element.getChildNodes();
        int length = beanChildren.getLength();
        
        for (int lcv = 0; lcv < length; lcv++) {
            Node childNode = beanChildren.item(lcv);
            
            handleNode(childNode, model, target, listener, classReflectionHelper, listChildren, arrayChildren);
        }
        
        for (Map.Entry<String, List<BaseHK2JAXBBean>> entry : listChildren.entrySet()) {
            // Kind of cheating with the erasure, but hey, it works!
            target._setProperty(entry.getKey(), entry.getValue());
        }
        
        for (Map.Entry<String, List<BaseHK2JAXBBean>> entry : arrayChildren.entrySet()) {
            String childTag = entry.getKey();
            ParentedModel pn = model.getChild(childTag);
            Class<?> childType = pn.getChildModel().getOriginalInterfaceAsClass();
            
            List<BaseHK2JAXBBean> individuals = entry.getValue();
            
            Object actualArray = Array.newInstance(childType, individuals.size());
            
            int index = 0;
            for (BaseHK2JAXBBean individual : individuals) {
                Array.set(actualArray, index++, individual);
            }
            
            target._setProperty(childTag, actualArray);
            
        }
        
        listener.afterUnmarshal(target, parent);
    }

}
