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

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.Rank;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.configuration.hub.api.WriteableBeanDatabase;
import org.glassfish.hk2.utilities.reflection.ClassReflectionHelper;
import org.glassfish.hk2.utilities.reflection.Logger;
import org.glassfish.hk2.utilities.reflection.internal.ClassReflectionHelperImpl;
import org.glassfish.hk2.xml.api.XmlHubCommitMessage;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.api.XmlService;
import org.glassfish.hk2.xml.jaxb.internal.BaseHK2JAXBBean;
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
    
    @SuppressWarnings("unchecked")
    private <T> XmlRootHandle<T> unmarshallClass(URI uri, UnparentedNode node,
            boolean advertise, boolean advertiseInHub) throws Exception {
        long elapsedUpToJAXB = 0;
        if (JAUtilities.DEBUG_GENERATION_TIMING) {
            elapsedUpToJAXB = System.currentTimeMillis();
        }
        
        Hk2JAXBUnmarshallerListener listener = new Hk2JAXBUnmarshallerListener(jaUtilities, classReflectionHelper);
        
        BaseHK2JAXBBean hk2Root = Utilities.createBean(node.getTranslatedClass());
        hk2Root._setModel(node, classReflectionHelper);
        
        long jaxbUnmarshallElapsedTime = 0L;
        if (JAUtilities.DEBUG_GENERATION_TIMING) {
            jaxbUnmarshallElapsedTime = System.currentTimeMillis();
            elapsedUpToJAXB = jaxbUnmarshallElapsedTime - elapsedUpToJAXB;
            Logger.getLogger().debug("Time in up to JAXB parsing " + uri + " is " + elapsedUpToJAXB + " milliseconds");
        }
         
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
        
        long elapsedJAXBToAdvertisement = 0;
        if (JAUtilities.DEBUG_GENERATION_TIMING) {
            elapsedJAXBToAdvertisement = System.currentTimeMillis();
            jaxbUnmarshallElapsedTime = elapsedJAXBToAdvertisement - jaxbUnmarshallElapsedTime;
            Logger.getLogger().debug("Time in JAXB parsing " + uri + " is " + jaxbUnmarshallElapsedTime + " milliseconds");
        }
        
        DynamicChangeInfo changeControl = new DynamicChangeInfo(jaUtilities,
                ((advertiseInHub) ? hub : null),
                null,
                ((advertise) ? dynamicConfigurationService : null),
                serviceLocator);
        
        for (BaseHK2JAXBBean base : listener.getAllBeans()) {
            String instanceName = Utilities.createInstanceName(base);
            base._setInstanceName(instanceName);
            
            base._setDynamicChangeInfo(changeControl);
        }
        
        long elapsedPreAdvertisement = 0L;
        if (JAUtilities.DEBUG_GENERATION_TIMING) {
            elapsedPreAdvertisement = System.currentTimeMillis();
            elapsedJAXBToAdvertisement = elapsedPreAdvertisement - elapsedJAXBToAdvertisement;
            Logger.getLogger().debug("Time from JAXB to PreAdvertisement " + uri + " is " + elapsedJAXBToAdvertisement + " milliseconds");
        }
        
        DynamicConfiguration config = (advertise) ? dynamicConfigurationService.createDynamicConfiguration() : null ;
        WriteableBeanDatabase wdb = (advertiseInHub) ? hub.getWriteableDatabaseCopy() : null ;
        
        for (BaseHK2JAXBBean bean : listener.getAllBeans()) {
            Utilities.advertise(wdb, config, bean);
        }
        
        long elapsedHK2Advertisement = 0L;
        if (JAUtilities.DEBUG_GENERATION_TIMING) {
            elapsedHK2Advertisement = System.currentTimeMillis();
            elapsedPreAdvertisement = elapsedHK2Advertisement - elapsedPreAdvertisement;
            Logger.getLogger().debug("Time from JAXB to PreAdvertisement " + uri + " is " + elapsedPreAdvertisement + " milliseconds");
        }
        
        if (config != null) {
            config.commit();
        }
        
        long elapsedHubAdvertisement = 0L;
        if (JAUtilities.DEBUG_GENERATION_TIMING) {
            elapsedHubAdvertisement = System.currentTimeMillis();
            elapsedHK2Advertisement = elapsedHubAdvertisement - elapsedHK2Advertisement;
            Logger.getLogger().debug("Time to advertise " + uri + " in HK2 is " + elapsedHK2Advertisement + " milliseconds");
        }
        
        if (wdb != null) {
            wdb.commit(new XmlHubCommitMessage() {});
        }
        
        if (JAUtilities.DEBUG_GENERATION_TIMING) {
            elapsedHubAdvertisement = System.currentTimeMillis() - elapsedHubAdvertisement;
            Logger.getLogger().debug("Time to advertise " + uri + " in Hub is " + elapsedHubAdvertisement + " milliseconds");
        }
        
        return new XmlRootHandleImpl<T>(null, hub, (T) hk2Root, node, uri, advertise, advertiseInHub, changeControl);
        
    }
    
    private static void handleNode(Node childNode, UnparentedNode model, BaseHK2JAXBBean target) {
        if (childNode instanceof Element) {
        
            Element childElement = (Element) childNode;
            String tagName = childElement.getTagName();
            
            if (model.getNonChildProperties().contains(tagName)) {
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
            else if (model.getKeyedChildren().contains(tagName)) {
                // TODO: Keyed child
                throw new AssertionError("keyed children not yet implemented");
            }
            else if (model.getUnKeyedChildren().contains(tagName)) {
                // TODO: Non-keyed child
                throw new AssertionError("un-keyed children not yet implemented");
            }
            else {
                // Probably just ignore it
            }
        }
        else if (childNode instanceof Attr) {
            Attr childAttr = (Attr) childNode;
            String tagName = childAttr.getName();
            
            if (model.getNonChildProperties().contains(tagName)) {
                Class<?> childType = model.getNonChildType(tagName);
                String sValue = childAttr.getValue();
                
                Object convertedValue = Utilities.getDefaultValue(sValue, childType);
                target._setProperty(tagName, convertedValue);
            }
            
        }
    }
    
    private <T> void handleElement(BaseHK2JAXBBean target, BaseHK2JAXBBean parent,
            Element element, Hk2JAXBUnmarshallerListener listener) {
        listener.beforeUnmarshal(target, parent);
        
        UnparentedNode model = target._getModel();
        
        NamedNodeMap attributeMap = element.getAttributes();
        for (int lcv = 0; lcv < attributeMap.getLength(); lcv++) {
            Node childNode = attributeMap.item(lcv);
            
            handleNode(childNode, model, target);
        }
        
        NodeList beanChildren = element.getChildNodes();
        int length = beanChildren.getLength();
        
        for (int lcv = 0; lcv < length; lcv++) {
            Node childNode = beanChildren.item(lcv);
            
            handleNode(childNode, model, target);
        }
        
        listener.afterUnmarshal(target, parent);
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
