/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
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

import java.net.URI;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.configuration.hub.api.WriteableBeanDatabase;
import org.glassfish.hk2.utilities.reflection.ClassReflectionHelper;
import org.glassfish.hk2.utilities.reflection.internal.ClassReflectionHelperImpl;
import org.glassfish.hk2.xml.api.XmlHubCommitMessage;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.api.XmlService;
import org.glassfish.hk2.xml.jaxb.internal.BaseHK2JAXBBean;

/**
 * @author jwells
 *
 */
@Singleton
public class XmlServiceImpl implements XmlService {
    private final static String ID_PREFIX = "XmlServiceUID-";
    
    private final JAUtilities jaUtilities = new JAUtilities();
    
    @Inject
    private DynamicConfigurationService dcs;
    
    @Inject
    private Hub hub;
    
    private final ClassReflectionHelper classReflectionHelper = new ClassReflectionHelperImpl();
    
    private final AtomicLong idGenerator = new AtomicLong();
    
    /**
     * Gets the XmlService wide unique identifier
     * @return A unique identifier for unkeyed multi-children
     */
    public String getUniqueId() {
        return ID_PREFIX + idGenerator.getAndAdd(1L);
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlService#unmarshall(java.net.URI, java.lang.Class, boolean, boolean)
     */
    @Override
    public <T> XmlRootHandle<T> unmarshall(URI uri,
            Class<T> jaxbAnnotatedClassOrInterface) {
        return unmarshall(uri, jaxbAnnotatedClassOrInterface, true, true);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlService#unmarshall(java.net.URI, java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> XmlRootHandle<T> unmarshall(URI uri,
            Class<T> jaxbAnnotatedInterface,
            boolean advertiseInRegistry, boolean advertiseInHub) {
        if (uri == null || jaxbAnnotatedInterface == null) throw new IllegalArgumentException();
        if (!jaxbAnnotatedInterface.isInterface()) {
            throw new IllegalArgumentException("Only an interface can be given to unmarshall: " + jaxbAnnotatedInterface.getName());
        }
        
        try {
            UnparentedNode parent = jaUtilities.convertRootAndLeaves(jaxbAnnotatedInterface);
                
            return unmarshallClass(uri, (Class<T>) parent.getTranslatedClass(), jaxbAnnotatedInterface, advertiseInRegistry, advertiseInHub);
        }
        catch (RuntimeException re) {
            throw re;
        }
        catch (Exception e) {
            throw new MultiException(e);
        }
    }
    
    @SuppressWarnings("unchecked")
    private <T> XmlRootHandle<T> unmarshallClass(URI uri, Class<T> jaxbAnnotatedClass, Class<T> originalClass,
            boolean advertise, boolean advertiseInHub) throws Exception {
        JAXBContext context = JAXBContext.newInstance(jaxbAnnotatedClass);
        
        Listener listener = new Listener();
        Unmarshaller unmarshaller = context.createUnmarshaller();
        unmarshaller.setListener(listener);
        
        T root = (T) unmarshaller.unmarshal(uri.toURL());
        
        DynamicChangeInfo changeControl = new DynamicChangeInfo(jaUtilities, hub, this, ((advertise) ? dcs : null));
        
        for (BaseHK2JAXBBean base : listener.getAllBeans()) {
            String instanceName = Utilities.createInstanceName(base);
            base._setInstanceName(instanceName);
            
            base._setDynamicChangeInfo(changeControl);
        }
        
        DynamicConfiguration config = (advertise) ? dcs.createDynamicConfiguration() : null ;
        WriteableBeanDatabase wdb = (advertiseInHub) ? hub.getWriteableDatabaseCopy() : null ;
        
        for (BaseHK2JAXBBean bean : listener.getAllBeans()) {
            Utilities.advertise(wdb, config, bean);
        }
        
        if (config != null) {
            config.commit();
        }
        if (wdb != null) {
            wdb.commit(new XmlHubCommitMessage() {});
        }
        
        return new XmlRootHandleImpl<T>(this, hub, root, originalClass, uri, advertise, advertiseInHub, changeControl);
    }
    
    

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlService#createEmptyHandle(java.lang.Class)
     */
    @Override
    public <T> XmlRootHandle<T> createEmptyHandle(
            Class<T> jaxbAnnotatedInterface, boolean advertiseInRegistry,
            boolean advertiseInHub) {
        if (!jaxbAnnotatedInterface.isInterface()) {
            throw new IllegalArgumentException("Only an interface can be given to unmarshall: " + jaxbAnnotatedInterface.getName());
        }
        try {
            jaUtilities.convertRootAndLeaves(jaxbAnnotatedInterface);
        
            return new XmlRootHandleImpl<T>(this, hub, null, jaxbAnnotatedInterface, null, advertiseInRegistry, advertiseInHub,
                    new DynamicChangeInfo(jaUtilities, hub, this, ((advertiseInRegistry) ? dcs : null)));
        }
        catch (RuntimeException re) {
            throw re;
        }
        catch (Exception e) {
            throw new MultiException(e);
        }
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlService#createEmptyHandle(java.lang.Class, boolean, boolean)
     */
    @Override
    public <T> XmlRootHandle<T> createEmptyHandle(
            Class<T> jaxbAnnotationInterface) {
        return createEmptyHandle(jaxbAnnotationInterface, true, true);
    }
    
    private class Listener extends Unmarshaller.Listener {
        private final LinkedList<BaseHK2JAXBBean> allBeans = new LinkedList<BaseHK2JAXBBean>();
        
        @Override
        public void afterUnmarshal(Object target, Object parent) {
            if (!(target instanceof BaseHK2JAXBBean)) return;
            
            BaseHK2JAXBBean targetBean = (BaseHK2JAXBBean) target;
            UnparentedNode targetNode = jaUtilities.getNode(target.getClass());
            
            String keyPropertyName = targetNode.getKeyProperty();
            String keyProperty = null;
            if (keyPropertyName != null) {
                keyProperty = (String) targetBean._getProperty(keyPropertyName);
                targetBean._setKeyValue(keyProperty);
            }
            
            targetBean._setParent(parent);
            
            allBeans.add(targetBean);
            
            if (parent == null) return;
            
            BaseHK2JAXBBean parentBean = (BaseHK2JAXBBean) parent;
            UnparentedNode parentNode = jaUtilities.getNode(parent.getClass());
            ParentedNode childNode = parentNode.getChild(targetNode.getOriginalInterface());
            
            if (keyProperty == null) {
                // One of two possibilities:  A multi-child with no key or a single child
                if (childNode.isMultiChild()) {
                    // Give it a unique identifier
                    targetBean._setKeyValue(getUniqueId());
                }
            }
            else {
                parentBean._addChild(childNode.getChildName(), keyProperty, targetBean);
            }
        }
        
        @Override
        public void beforeUnmarshal(Object target, Object parent) {
            if (!(target instanceof BaseHK2JAXBBean)) return;
            
            BaseHK2JAXBBean baseBean = (BaseHK2JAXBBean) target;
            BaseHK2JAXBBean parentBean = (BaseHK2JAXBBean) parent;
            
            UnparentedNode baseNode = jaUtilities.getNode(target.getClass());
            baseBean._setModel(baseNode, classReflectionHelper);
            
            if (parentBean == null) {
                baseBean._setSelfXmlTag(baseNode.getRootName());
            }
            else {
                baseBean._setParentXmlPath(parentBean._getXmlPath());
                
                UnparentedNode parentNode = jaUtilities.getNode(parent.getClass());
                ParentedNode childNode = parentNode.getChild(baseNode.getOriginalInterface());
                
                baseBean._setSelfXmlTag(childNode.getChildName());
            }
        }
        
        private LinkedList<BaseHK2JAXBBean> getAllBeans() {
            return allBeans;
        }
        
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlService#createBean(java.lang.Class)
     */
    @Override
    public <T> T createBean(Class<T> beanInterface) {
        throw new AssertionError("not yet implemented");
    }

   
}
