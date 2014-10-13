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
package org.glassfish.hk2.configuration.hub.xml.dom.integration.internal;

import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.DynamicConfigurationListener;
import org.glassfish.hk2.api.IndexedFilter;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.configuration.hub.api.WriteableBeanDatabase;
import org.glassfish.hk2.configuration.hub.api.WriteableType;
import org.glassfish.hk2.configuration.hub.xml.dom.integration.XmlDomHubData;
import org.glassfish.hk2.configuration.hub.xml.dom.integration.XmlDomIntegrationCommitMessage;
import org.glassfish.hk2.configuration.hub.xml.dom.integration.XmlDomIntegrationUtilities;
import org.glassfish.hk2.configuration.hub.xml.dom.integration.XmlDomTranslationService;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.reflection.Logger;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigModel;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.UnprocessedChangeEvents;

/**
 * Implementation of the listener needed to add/remove instances of
 * the hk2-config configuration beans into the hub
 * 
 * @author jwells
 */
@Singleton
public class ConfigListener implements DynamicConfigurationListener {
    private final static int MAX_TRIES = 10000;
    private final static IndexedFilter CONFIG_FILTER = BuilderHelper.createContractFilter(ConfigBean.class.getName());
    private final static String TYPE_CONNECTOR = "/";
    private final static String INSTANCE_CONNECTOR = ".";
    private final static ThreadLocal<Boolean> skipper = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
        
    };
    
    @Inject
    private ServiceLocator locator;
    
    @Inject
    private Hub hub;
    
    private final HashMap<ActiveDescriptor<?>, HubKey> descriptors = new HashMap<ActiveDescriptor<?>, HubKey>();
    
    private final LinkedList<String> knownChangedProperties = new LinkedList<String>();
    
    private static String getTagName(ConfigModel childModel, Dom parent, String defaultTagName) {
        if (parent == null) return defaultTagName;
        
        Set<String> parentElementNames = parent.model.getElementNames();
        for (String parentElementName : parentElementNames) {
            ConfigModel.Property property = parent.model.getElement(parentElementName);
            if (property == null) continue;
            if (!(property instanceof ConfigModel.Node)) continue;
            
            ConfigModel.Node parentNode = (ConfigModel.Node) property;
            
            if (parentNode.getModel() == childModel) {
                if (WritebackHubListener.STAR.equals(parentElementName)) {
                    return defaultTagName;
                }
                
                return parentElementName;
            }
        }
        return defaultTagName;
    }
    
    private HubKey getHubKey(ActiveDescriptor<?> descriptor) {
        ServiceHandle<?> handle = locator.getServiceHandle(descriptor);
        ConfigBeanProxy configBeanProxy = (ConfigBeanProxy) handle.getService();
        
        Dom dom = Dom.unwrap(configBeanProxy);
        Dom topDom = dom;
        
        LinkedList<String> xpathTags = new LinkedList<String>();
        LinkedList<String> nameTags = new LinkedList<String>();
        while (dom != null) {
            ConfigModel model = dom.model;
            Dom parent = dom.parent();
            
            String tagName = model.getTagName();
            tagName = getTagName(model, parent, tagName);
            if (tagName != null) {
                xpathTags.addFirst(tagName);
            }
            
            if (model.key != null) {
                if (dom.getKey() == null) {
                    // Do not really know what to do here
                    throw new AssertionError("Bean " + dom.getImplementation() + " has a key field but the key is null");
                }
                nameTags.addFirst(dom.getKey());
            }
            else if (tagName != null) {
                nameTags.addFirst(tagName);
            }
            else {
                nameTags.addFirst(XmlDomIntegrationUtilities.DEFAULT_INSTANCE_NAME);
            }
            
            dom = dom.parent();
        }
        
        StringBuffer typeBuffer = new StringBuffer();
        for (String tag : xpathTags) {
            typeBuffer.append(TYPE_CONNECTOR + tag);
        }
        
        boolean firstTime = true;
        StringBuffer instanceBuffer = new StringBuffer();
        for (String name : nameTags) {
            if (firstTime) {
                firstTime = false;
                instanceBuffer.append(name);
            }
            else {
                instanceBuffer.append(INSTANCE_CONNECTOR + name);
            }
        }
        
        HubKey retVal = new HubKey(handle,
                typeBuffer.toString(),
                instanceBuffer.toString(),
                null,
                locator.getAllServices(XmlDomTranslationService.class));
        topDom.addListener(retVal);
        return retVal;
    }
    
    private void addInstance(ActiveDescriptor<?> descriptor) {
        HubKey hubKey = getHubKey(descriptor);
        Object target = hubKey.getTranslatedService();
        Object metadata = hubKey.getTranslatedMetadata();
        
        // Must add this in prior to telling the database about
        // it to stop infinite recursions
        descriptors.put(descriptor, hubKey);
        
        for (int lcv = 0; lcv < MAX_TRIES; lcv++) {
            WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
            
            WriteableType wt = wbd.findOrAddWriteableType(hubKey.getTranslatedType());
            
            wt.addInstance(hubKey.getTranslatedInstance(), target, metadata);
            
            try {
                wbd.commit(new XmlDomIntegrationCommitMessage() {});
                break;
            }
            catch (IllegalStateException ise) {
                // try again
            }
        }
    }
    
    private void removeInstance(HubKey key) {
        for (int lcv = 0; lcv < MAX_TRIES; lcv++) {
            WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
            
            WriteableType wt = wbd.findOrAddWriteableType(key.getTranslatedType());
            
            wt.removeInstance(key.getTranslatedInstance());
            
            try {
                wbd.commit(new XmlDomIntegrationCommitMessage() {});
                break;
            }
            catch (IllegalStateException ise) {
                // try again
            }
        }
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.DynamicConfigurationListener#configurationChanged()
     */
    @Override
    @PostConstruct
    public void configurationChanged() {
        try {
          internalConfigurationChanged();
        }
        catch (Throwable th) {
            Logger.getLogger().debug(getClass().getName(), "configurationChanged", th);
        }
    }
    
    /* package */ void addKnownChange(String property) {
        synchronized (knownChangedProperties) {
            knownChangedProperties.add(property);
        }
        
        
    }
    
    /* package */ void removeKnownChange(String property) {
        synchronized (knownChangedProperties) {
            Iterator<String> iterator = knownChangedProperties.iterator();
            while (iterator.hasNext()) {
                String removeMe = iterator.next();
                if (removeMe != null && removeMe.equals(property)) {
                    iterator.remove();
                    return;
                }
            }
        }
        
        
    }
    
    /* package */ void skip() {
        skipper.set(true);
    }
    
    /* package */ void unskip() {
        skipper.set(false);
    }
    
    private void internalConfigurationChanged() {
        if (skipper.get()) {
            Logger.getLogger().debug("WRITEBACK: Ignoring configuration because we are responsible");
            return;
        }
        
        List<ActiveDescriptor<?>> currentDescriptors = locator.getDescriptors(CONFIG_FILTER);
        
        synchronized (descriptors) {
            HashSet<ActiveDescriptor<?>> descriptorsCopy = new HashSet<ActiveDescriptor<?>>(descriptors.keySet());
            descriptorsCopy.removeAll(currentDescriptors);
            
            // Everything left over needs to be removed
            for (ActiveDescriptor<?> removeMe : descriptorsCopy) {
                HubKey hubKey = descriptors.remove(removeMe);
                if (hubKey == null) continue;
                
                removeInstance(hubKey);
            }
        
            for (ActiveDescriptor<?> descriptor : currentDescriptors) {
                if (descriptors.containsKey(descriptor)) continue;
                addInstance(descriptor);
            }
        }
    }
    
    private class HubKey implements org.jvnet.hk2.config.ConfigListener {
        private final ServiceHandle<?> iHandle;
        private final String iType;
        private final String iInstance;
        private final Object iMetadata;
        private final List<XmlDomTranslationService> translators;
        
        private String translatedType;
        private String translatedInstance;
        private Object translatedService;
        private Object translatedMetadata;
        
        private HubKey(ServiceHandle<?> handle, String type, String instance, Object metadata, List<XmlDomTranslationService> translators) {
            this.iHandle = handle;
            this.iType = type;
            this.iInstance = instance;
            this.iMetadata = metadata;
            this.translators = translators;
        }
        
        private void translate() {
            if (translators.isEmpty()) {
                translatedType = iType;
                translatedInstance = iInstance;
                translatedService = iHandle.getService();
                translatedMetadata = iMetadata;
                return;
            }
            
            XmlDomHubData original = new XmlDomHubData(iType, iInstance, iHandle.getService(), iMetadata);
            XmlDomHubData userData = original;
            
            for (XmlDomTranslationService translator : translators) {
                XmlDomHubData previousUserData = userData;
                try {
                    userData = translator.translate(userData);
                }
                catch (Throwable th) {
                    // TODO:  We have a lot of different kind of errors to handle
                }
                
                if (userData == null) userData = previousUserData;
                
                if (userData.getType() == null ||
                        userData.getInstanceKey() == null ||
                        userData.getBean() == null) {
                    throw new IllegalArgumentException("The data returned from " + translator + " had nulls as return values");
                }
            }
            
            // OK, finished
            translatedType = userData.getType();
            translatedInstance = userData.getInstanceKey();
            translatedService = userData.getBean();
            translatedMetadata = userData.getMetadata();
        }
        
        private synchronized String getTranslatedType() {
            if (translatedType == null) {
                translate();
            }
            
            return translatedType;
        }
        
        private synchronized String getTranslatedInstance() {
            if (translatedType == null) {
                translate();
            }
            
            return translatedInstance;
        }
        
        private synchronized Object getTranslatedService() {
            if (translatedType == null) {
                translate();
            }
            
            return translatedService;
        }
        
        private synchronized Object getTranslatedMetadata() {
            if (translatedType == null) {
                translate();
            }
            
            return translatedMetadata;
        }
        
        /* (non-Javadoc)
         * @see org.jvnet.hk2.config.ConfigListener#changed(java.beans.PropertyChangeEvent[])
         */
        @Override
        public synchronized UnprocessedChangeEvents changed(PropertyChangeEvent[] events) {
            synchronized (knownChangedProperties) {
                Iterator<String> iterator = knownChangedProperties.iterator();
                while (iterator.hasNext()) {
                    String knownProperty = iterator.next();
                    
                    for (PropertyChangeEvent event : events) {
                        if (event.getPropertyName() != null && event.getPropertyName().equals(knownProperty)) {
                            iterator.remove();
                        }
                        
                        Logger.getLogger().debug("WRITEBACK: ConfigListener ignoring property changes due to detected replay");
                        return null;
                    }
                }
            }
            
            // Must force re-translation
            translate();
            
            for (int lcv = 0; lcv < MAX_TRIES; lcv++) {
                WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
                
                WriteableType wt = wbd.getWriteableType(translatedType);
                if (wt == null) return null;
                
                wt.modifyInstance(translatedInstance, translatedService, events);
                
                try {
                  wbd.commit(new XmlDomIntegrationCommitMessage() {});
                  break;
                }
                catch (IllegalStateException ise) {
                    // keep going
                }
            }
            
            return null;
        }
          
        @Override
        public String toString() {
            return "HubKey(" + iType + "," + iInstance + "," + System.identityHashCode(this) + ")";
        }   
    }
}
