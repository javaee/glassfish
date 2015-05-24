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
import java.net.URI;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.Rank;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.utilities.reflection.ClassReflectionHelper;
import org.glassfish.hk2.utilities.reflection.internal.ClassReflectionHelperImpl;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.api.XmlService;
import org.glassfish.hk2.xml.jaxb.internal.BaseHK2JAXBBean;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * @author jwells
 *
 */
@Named("DomXmlService")
@Rank(-1000)
public class DomXmlServiceImpl implements XmlService {
    private final JAUtilities jaUtilities = new JAUtilities();
    
    @Inject
    private ServiceLocator serviceLocator;
    
    @Inject
    private DynamicConfigurationService dynamicConfigurationService;
    
    @Inject
    private Hub hub;
    
    private final ClassReflectionHelper classReflectionHelper = new ClassReflectionHelperImpl();
    
    private final DocumentBuilder documentBuilder;
    
    private DomXmlServiceImpl() throws ParserConfigurationException {
        documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlService#unmarshall(java.net.URI, java.lang.Class)
     */
    @Override
    public <T> XmlRootHandle<T> unmarshall(URI uri,
            Class<T> jaxbAnnotatedInterface) {
        return unmarshall(uri, jaxbAnnotatedInterface, true, true);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlService#unmarshall(java.net.URI, java.lang.Class, boolean, boolean)
     */
    @Override
    public <T> XmlRootHandle<T> unmarshall(URI uri,
            Class<T> jaxbAnnotatedInterface, boolean advertiseInRegistry,
            boolean advertiseInHub) {
        if (uri == null || jaxbAnnotatedInterface == null) throw new IllegalArgumentException();
        if (!jaxbAnnotatedInterface.isInterface()) {
            throw new IllegalArgumentException("Only an interface can be given to unmarshall: " + jaxbAnnotatedInterface.getName());
        }
        
        try {
            UnparentedNode parent = jaUtilities.convertRootAndLeaves(jaxbAnnotatedInterface);
                
            return unmarshallClass(uri, parent, advertiseInRegistry, advertiseInHub);
        }
        catch (RuntimeException re) {
            throw re;
        }
        catch (Throwable e) {
            throw new MultiException(e);
        }
    }
    
    private <T> XmlRootHandle<T> unmarshallClass(URI uri, UnparentedNode node,
            boolean advertise, boolean advertiseInHub) throws Exception {
        
        Hk2JAXBUnmarshallerListener listener = new Hk2JAXBUnmarshallerListener(jaUtilities, classReflectionHelper);
        
        BaseHK2JAXBBean hk2Root = Utilities.createBean(node.getTranslatedClass());
        hk2Root._setModel(node, classReflectionHelper);
         
        InputStream urlStream = uri.toURL().openStream();
        Document document;
        try {
            document = documentBuilder.parse(urlStream);
        }
        finally {
            urlStream.close();
        }
            
        Element docElement = document.getDocumentElement();
        handleElement(hk2Root, null, docElement, listener);
        
        DynamicChangeInfo changeControl = new DynamicChangeInfo(jaUtilities,
                ((advertiseInHub) ? hub : null),
                null,
                ((advertise) ? dynamicConfigurationService : null),
                serviceLocator);
        
        throw new AssertionError("unmarshallClass not yet implemented in DomXmlServiceImpl"); 
        
    }
    
    private <T> void handleElement(BaseHK2JAXBBean target, BaseHK2JAXBBean parent,
            Element element, Hk2JAXBUnmarshallerListener listener) {
        listener.beforeUnmarshal(target, parent);
        
        NodeList beanChildren = element.getChildNodes();
        int length = beanChildren.getLength();
        
        UnparentedNode model = target._getModel();
        
        for (int lcv = 0; lcv < length; lcv++) {
            Node childNode = beanChildren.item(lcv);
            if (childNode instanceof Element) {
                Element childElement = (Element) childNode;
                String tagName = childElement.getTagName();
                
                if (model.getNonChildProperties().contains(tagName)) {
                    NodeList childNodeChildren = childElement.getChildNodes();
                    
                    for (int lcv1 = 0; lcv1 < childNodeChildren.getLength(); lcv1++) {
                        Node childNodeChild = childNodeChildren.item(lcv1);
                        if (childNodeChild instanceof Text) {
                            Text childText = (Text) childNodeChild;
                            
                            String valueString = childText.getTextContent().trim();
                            
                        }
                    }
                }
                else if (model.getKeyedChildren().contains(tagName)) {
                    // TODO: Keyed child
                }
                else if (model.getUnKeyedChildren().contains(tagName)) {
                    // TODO: Non-keyed child
                }
                else {
                    // Probably just ignore it
                }
                
                
                
            }
        }
        
        
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlService#createEmptyHandle(java.lang.Class, boolean, boolean)
     */
    @Override
    public <T> XmlRootHandle<T> createEmptyHandle(
            Class<T> jaxbAnnotationInterface, boolean advertiseInRegistry,
            boolean advertiseInHub) {
        throw new AssertionError("createEmptyHandle not yet implemented in DomXmlServiceImpl");
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlService#createEmptyHandle(java.lang.Class)
     */
    @Override
    public <T> XmlRootHandle<T> createEmptyHandle(
            Class<T> jaxbAnnotationInterface) {
        return createEmptyHandle(jaxbAnnotationInterface, true, true);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlService#createBean(java.lang.Class)
     */
    @Override
    public <T> T createBean(Class<T> beanInterface) {
        throw new AssertionError("createBean not yet implemented in DomXmlServiceImpl");
    }

}
