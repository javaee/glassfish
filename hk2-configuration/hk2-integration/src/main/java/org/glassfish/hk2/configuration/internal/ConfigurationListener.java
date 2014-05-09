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
package org.glassfish.hk2.configuration.internal;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.IndexedFilter;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.configuration.api.ConfiguredBy;
import org.glassfish.hk2.configuration.hub.api.BeanDatabase;
import org.glassfish.hk2.configuration.hub.api.BeanDatabaseUpdateListener;
import org.glassfish.hk2.configuration.hub.api.Change;
import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.configuration.hub.api.Type;

/**
 * @author jwells
 *
 */
@Singleton
public class ConfigurationListener implements BeanDatabaseUpdateListener {
    @Inject
    private Hub hub;
    
    @Inject
    private ServiceLocator locator;
    
    @Inject
    private DynamicConfigurationService configurationService;
    
    @Inject
    private ConfiguredByInjectionResolver injectionResolver;
    
    @PostConstruct
    private void postConstruct() {
        Thread t = new Thread(new Initializer());
        t.start();
    }
    
    private ActiveDescriptor<?> addInstanceDescriptor(DynamicConfiguration config, ActiveDescriptor<?> parent, String name, Object bean) {
        DelegatingNamedActiveDescriptor addMe = new DelegatingNamedActiveDescriptor(parent, name);
        
        ActiveDescriptor<?> systemDescriptor = config.addActiveDescriptor(addMe);
        
        injectionResolver.addBean(systemDescriptor, bean);
        
        return systemDescriptor;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.configuration.hub.api.BeanDatabaseUpdateListener#initialize(org.glassfish.hk2.configuration.hub.api.BeanDatabase)
     */
    @Override
    public void initialize(BeanDatabase database) {
        Set<Type> allTypes = database.getAllTypes();
        
        LinkedList<ActiveDescriptor<?>> added = new LinkedList<ActiveDescriptor<?>>();
        DynamicConfiguration config = configurationService.createDynamicConfiguration();
        
        for (Type type : allTypes) {
            String typeName = type.getName();
            
            List<ActiveDescriptor<?>> typeDescriptors = locator.getDescriptors(new NoNameTypeFilter(locator, typeName));
            
            for (ActiveDescriptor<?> typeDescriptor : typeDescriptors) {
                // These match the type, so now we have to add one per instance
                
                Map<String, Object> typeInstances = type.getInstances();
                for (Map.Entry<String, Object> entry : typeInstances.entrySet()) {
                    added.add(addInstanceDescriptor(config, typeDescriptor, entry.getKey(), entry.getValue()));
                }
            }
        }
        
        // Add all instances
        config.commit();
        
        // Create demand for all the ones we just added
        for (ActiveDescriptor<?> descriptor : added) {
            ServiceHandle<?> handle = locator.getServiceHandle(descriptor);
            handle.getService();  // TODO: how to handle errors?
        }
        
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.configuration.hub.api.BeanDatabaseUpdateListener#databaseHasChanged(org.glassfish.hk2.configuration.hub.api.BeanDatabase, java.util.List)
     */
    @Override
    public void databaseHasChanged(BeanDatabase newDatabase,
            List<Change> changes) {
        LinkedList<ActiveDescriptor<?>> added = new LinkedList<ActiveDescriptor<?>>();
        DynamicConfiguration config = configurationService.createDynamicConfiguration();
        
        for (Change change : changes) {
            if (Change.ChangeCategory.ADD_INSTANCE.equals(change.getChangeCategory())) {
                String addedInstanceKey = change.getInstanceKey();
                Object addedInstanceBean = change.getInstanceValue();
                
                List<ActiveDescriptor<?>> typeDescriptors = locator.getDescriptors(new NoNameTypeFilter(locator, change.getChangeType().getName()));
                
                for (ActiveDescriptor<?> typeDescriptor : typeDescriptors) {
                    // These match the type, so now we have to add one per instance
                    
                        added.add(addInstanceDescriptor(config, typeDescriptor, addedInstanceKey, addedInstanceBean));
                }
            }   
        }
        
        // Add all instances
        config.commit();
        
        // Create demand for all the ones we just added
        for (ActiveDescriptor<?> descriptor : added) {
            ServiceHandle<?> handle = locator.getServiceHandle(descriptor);
            handle.getService();  // TODO: how to handle errors?
        }
            
    }
    
    private class Initializer implements Runnable {

        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            hub.addListener(ConfigurationListener.this);
        }
        
    }
    
    /**
     * Matches only things with scope ConfiguredBy and which have no name
     * 
     * @author jwells
     *
     */
    private static class NoNameTypeFilter implements IndexedFilter {
        private final ServiceLocator locator;
        private final String typeName;
        
        private NoNameTypeFilter(ServiceLocator locator, String typeName) {
            this.locator = locator;
            this.typeName = typeName;
        }

        /* (non-Javadoc)
         * @see org.glassfish.hk2.api.Filter#matches(org.glassfish.hk2.api.Descriptor)
         */
        @Override
        public boolean matches(Descriptor d) {
            if (d.getName() != null) return false;
            
            
            ActiveDescriptor<?> reified;
            try {
                reified = locator.reifyDescriptor(d);
            }
            catch (MultiException me) {
                return false;
            }
            
            Class<?> implClass = reified.getImplementationClass();
            ConfiguredBy configuredBy = implClass.getAnnotation(ConfiguredBy.class);
            if (configuredBy == null) return false;
            
            return configuredBy.type().equals(typeName);
        }

        /* (non-Javadoc)
         * @see org.glassfish.hk2.api.IndexedFilter#getAdvertisedContract()
         */
        @Override
        public String getAdvertisedContract() {
            return ConfiguredBy.class.getName();
        }

        /* (non-Javadoc)
         * @see org.glassfish.hk2.api.IndexedFilter#getName()
         */
        @Override
        public String getName() {
            return null;
        }
        
    }

}
