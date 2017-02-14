/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014-2017 Oracle and/or its affiliates. All rights reserved.
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
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.stream.XMLStreamReader;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.DescriptorVisibility;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.IterableProvider;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.Self;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.Visibility;
import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.configuration.hub.api.WriteableBeanDatabase;
import org.glassfish.hk2.utilities.reflection.ClassReflectionHelper;
import org.glassfish.hk2.utilities.reflection.Logger;
import org.glassfish.hk2.utilities.reflection.internal.ClassReflectionHelperImpl;
import org.glassfish.hk2.xml.api.XmlHubCommitMessage;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.api.XmlService;
import org.glassfish.hk2.xml.jaxb.internal.BaseHK2JAXBBean;
import org.glassfish.hk2.xml.spi.PreGenerationRequirement;
import org.glassfish.hk2.xml.spi.XmlServiceParser;

/**
 * @author jwells
 *
 */
@Singleton
@Visibility(DescriptorVisibility.LOCAL)
public class XmlServiceImpl implements XmlService {
    public final static boolean DEBUG_PARSING = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
        @Override
        public Boolean run() {
            return Boolean.parseBoolean(
                System.getProperty("org.jvnet.hk2.xmlservice.parser.stream", "false"));
        }
            
    });
    
    @Inject
    private ServiceLocator serviceLocator;
    
    @Inject
    private DynamicConfigurationService dynamicConfigurationService;
    
    @Inject
    private Hub hub;
    
    @Inject
    private IterableProvider<XmlServiceParser> parser;
    
    private final ClassReflectionHelper classReflectionHelper = new ClassReflectionHelperImpl();
    
    private final JAUtilities jaUtilities = new JAUtilities(classReflectionHelper);
    
    @Inject @Self
    private ActiveDescriptor<?> selfDescriptor;
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlService#unmarshall(java.net.URI, java.lang.Class, boolean, boolean)
     */
    @Override
    public <T> XmlRootHandle<T> unmarshal(URI uri,
            Class<T> jaxbAnnotatedClassOrInterface) {
        return unmarshal(uri, jaxbAnnotatedClassOrInterface, true, true);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlService#unmarshall(java.net.URI, java.lang.Class)
     */
    @Override
    public <T> XmlRootHandle<T> unmarshal(URI uri,
            Class<T> jaxbAnnotatedInterface,
            boolean advertiseInRegistry, boolean advertiseInHub) {
        if (uri == null || jaxbAnnotatedInterface == null) throw new IllegalArgumentException();
        if (!jaxbAnnotatedInterface.isInterface()) {
            throw new IllegalArgumentException("Only an interface can be given to unmarshall: " + jaxbAnnotatedInterface.getName());
        }
        
        XmlServiceParser localParser = parser.named(selfDescriptor.getName()).get();
        if (localParser == null) {
            throw new IllegalStateException("There is no XmlServiceParser implementation");
        }
        
        try {
            boolean generateAll = PreGenerationRequirement.MUST_PREGENERATE.equals(localParser.getPreGenerationRequirement());
            jaUtilities.convertRootAndLeaves(jaxbAnnotatedInterface, generateAll);
            
            ModelImpl model = jaUtilities.getModel(jaxbAnnotatedInterface);
                
            return unmarshallClass(uri, model, localParser, null, advertiseInRegistry, advertiseInHub);
        }
        catch (RuntimeException re) {
            throw re;
        }
        catch (Throwable e) {
            throw new MultiException(e);
        }
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlService#unmarshall(javax.xml.stream.XMLStreamReader, java.lang.Class, boolean, boolean)
     */
    @Override
    public <T> XmlRootHandle<T> unmarshal(XMLStreamReader reader,
            Class<T> jaxbAnnotatedInterface, boolean advertiseInRegistry,
            boolean advertiseInHub) {
        if (reader == null || jaxbAnnotatedInterface == null) throw new IllegalArgumentException();
        if (!jaxbAnnotatedInterface.isInterface()) {
            throw new IllegalArgumentException("Only an interface can be given to unmarshall: " + jaxbAnnotatedInterface.getName());
        }
        
        try {
            jaUtilities.convertRootAndLeaves(jaxbAnnotatedInterface, false);
            
            ModelImpl model = jaUtilities.getModel(jaxbAnnotatedInterface);
                
            return unmarshallClass(null, model, null, reader, advertiseInRegistry, advertiseInHub);
        }
        catch (RuntimeException re) {
            throw re;
        }
        catch (Throwable e) {
            throw new MultiException(e);
        }
    }
    
    private <T> XmlRootHandle<T> unmarshallClass(URI uri, ModelImpl model,
            XmlServiceParser localParser, XMLStreamReader reader,
            boolean advertise, boolean advertiseInHub) throws Exception {
        long elapsedUpToJAXB = 0;
        if (JAUtilities.DEBUG_GENERATION_TIMING) {
            elapsedUpToJAXB = System.currentTimeMillis();
        }
        
        Hk2JAXBUnmarshallerListener listener = new Hk2JAXBUnmarshallerListener(jaUtilities, classReflectionHelper);
        
        long jaxbUnmarshallElapsedTime = 0L;
        if (JAUtilities.DEBUG_GENERATION_TIMING) {
            jaxbUnmarshallElapsedTime = System.currentTimeMillis();
            elapsedUpToJAXB = jaxbUnmarshallElapsedTime - elapsedUpToJAXB;
            Logger.getLogger().debug("Time up to parsing " + uri + " is " + elapsedUpToJAXB + " milliseconds");
        }
        
        T root;
        if (localParser != null) {
            root = localParser.parseRoot(model, uri, listener);
        }
        else {
            root = XmlStreamImpl.parseRoot(this, model, reader, listener);
        }
        
        long elapsedJAXBToAdvertisement = 0;
        if (JAUtilities.DEBUG_GENERATION_TIMING) {
            elapsedJAXBToAdvertisement = System.currentTimeMillis();
            jaxbUnmarshallElapsedTime = elapsedJAXBToAdvertisement - jaxbUnmarshallElapsedTime;
            Logger.getLogger().debug("Time parsing " + uri + " is " + jaxbUnmarshallElapsedTime + " milliseconds " +
              ", now with " + jaUtilities.getNumGenerated() + " proxies generated and " +
                    jaUtilities.getNumPreGenerated() + " pre generated proxies loaded");
        }
        
        DynamicChangeInfo<T> changeControl = new DynamicChangeInfo<T>(jaUtilities,
                hub,
                advertiseInHub,
                this,
                dynamicConfigurationService,
                advertise,
                serviceLocator);
        
        XmlRootHandleImpl<T> retVal = new XmlRootHandleImpl<T>(this, hub, root, model, uri, advertise, advertiseInHub, changeControl);
        
        changeControl.setRoot(retVal);
        
        for (BaseHK2JAXBBean base : listener.getAllBeans()) {
            String instanceName = Utilities.createInstanceName(base);
            base._setInstanceName(instanceName);
            
            base._setDynamicChangeInfo(retVal, changeControl);
            
            if (DEBUG_PARSING) {
                Logger.getLogger().debug("XmlServiceDebug found bean " + base);
            }
        }
        if (DEBUG_PARSING) {
            Logger.getLogger().debug("XmlServiceDebug after parsing all beans in " + uri);
        }
        
        long elapsedPreAdvertisement = 0L;
        if (JAUtilities.DEBUG_GENERATION_TIMING) {
            elapsedPreAdvertisement = System.currentTimeMillis();
            elapsedJAXBToAdvertisement = elapsedPreAdvertisement - elapsedJAXBToAdvertisement;
            Logger.getLogger().debug("Time from parsing to PreAdvertisement " + uri + " is " + elapsedJAXBToAdvertisement + " milliseconds");
        }
        
        DynamicConfiguration config = (advertise) ? dynamicConfigurationService.createDynamicConfiguration() : null ;
        WriteableBeanDatabase wdb = (advertiseInHub) ? hub.getWriteableDatabaseCopy() : null ;
        
        boolean attachedTransaction = false;
        if (config != null && wdb != null) {
            attachedTransaction = true;
            
            wdb.setCommitMessage(new XmlHubCommitMessage() {});
            
            config.registerTwoPhaseResources(wdb.getTwoPhaseResource());
        }
        
        LinkedList<BaseHK2JAXBBean> allBeans = listener.getAllBeans();
        List<ActiveDescriptor<?>> addedDescriptors = new ArrayList<ActiveDescriptor<?>>(allBeans.size());
        for (BaseHK2JAXBBean bean : allBeans) {
            ActiveDescriptor<?> added = Utilities.advertise(wdb, config, bean);
            if (added != null) {
                addedDescriptors.add(added);
            }
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
        
        if (wdb != null && !attachedTransaction) {
            wdb.commit(new XmlHubCommitMessage() {});
        }
        
        // The following is put here so that InstanceLifecycleListeners will get invoked at this time
        // rather than later so that defaulting can be done now.  It doesn't hurt because these are
        // all constant descriptors
        for (ActiveDescriptor<?> ad : addedDescriptors) {
            serviceLocator.getServiceHandle(ad).getService();
        }
        
        if (JAUtilities.DEBUG_GENERATION_TIMING) {
            elapsedHubAdvertisement = System.currentTimeMillis() - elapsedHubAdvertisement;
            Logger.getLogger().debug("Time to advertise " + uri + " in Hub is " + elapsedHubAdvertisement + " milliseconds");
        }
        
        return retVal;
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
            jaUtilities.convertRootAndLeaves(jaxbAnnotatedInterface, true);
            
            ModelImpl model = jaUtilities.getModel(jaxbAnnotatedInterface);
            
            DynamicChangeInfo<T> change = new DynamicChangeInfo<T>(jaUtilities,
                    hub,
                    advertiseInHub,
                    this,
                    dynamicConfigurationService,
                    advertiseInRegistry,
                    serviceLocator);
        
            XmlRootHandleImpl<T> retVal = new XmlRootHandleImpl<T>(this,
                    hub, null, model, null, advertiseInRegistry,
                    advertiseInHub, change);
            
            change.setRoot(retVal);
            
            return retVal;
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
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlService#createBean(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T createBean(Class<T> beanInterface) {
        if (!beanInterface.isInterface()) {
            throw new IllegalArgumentException("Only an interface can be given to unmarshall: " + beanInterface.getName());
        }
        
        jaUtilities.convertRootAndLeaves(beanInterface, true);
        
        ModelImpl model = jaUtilities.getModel(beanInterface);
        
        T retVal = (T) Utilities.createBean(model.getProxyAsClass());
        
        BaseHK2JAXBBean base = (BaseHK2JAXBBean) retVal;
        
        base._setClassReflectionHelper(classReflectionHelper);
        base._setActive();
        
        
        return retVal;
    }

    public ClassReflectionHelper getClassReflectionHelper() {
        return classReflectionHelper;
    }
    
    /* package */ DynamicConfigurationService getDynamicConfigurationService() {
        return dynamicConfigurationService;
    }
    
    public JAUtilities getJAUtilities() {
        return jaUtilities;
    }

    public ServiceLocator getServiceLocator() {
        return serviceLocator;
    }
    
    public XmlServiceParser getParser() {
        return parser.named(selfDescriptor.getName()).get();
    }
}
