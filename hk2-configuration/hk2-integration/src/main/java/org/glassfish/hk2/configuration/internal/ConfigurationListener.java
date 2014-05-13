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

import java.beans.PropertyChangeEvent;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.configuration.hub.api.BeanDatabase;
import org.glassfish.hk2.configuration.hub.api.BeanDatabaseUpdateListener;
import org.glassfish.hk2.configuration.hub.api.Change;
import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.configuration.hub.api.Type;
import org.glassfish.hk2.utilities.reflection.ReflectionHelper;

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
    
    @Inject
    private ConfiguredByContext context;
    
    @PostConstruct
    private void postConstruct() {
        hub.addListener(this);
    }
    
    private ActiveDescriptor<?> addInstanceDescriptor(DynamicConfiguration config, ActiveDescriptor<?> parent, String name, Object bean) {
        DelegatingNamedActiveDescriptor addMe = new DelegatingNamedActiveDescriptor(parent, name);
        
        ActiveDescriptor<?> systemDescriptor = config.addActiveDescriptor(addMe);
        
        injectionResolver.addBean(systemDescriptor, bean);
        
        return systemDescriptor;
    }
    
    private void modifyInstanceDescriptor(ActiveDescriptor<?> parent,
            String name,
            Object bean,
            List<PropertyChangeEvent> changes
            ) {
        injectionResolver.addBean(parent, bean);
        
        Object target = context.findOnly(parent);
        if (target == null) {
            // race lost
            return;
        }
        
        HashMap<String, PropertyChangeEvent> changedProperties = new HashMap<String, PropertyChangeEvent>();
        for (PropertyChangeEvent pce : changes) {
            changedProperties.put(pce.getPropertyName(), pce);
        }
        
        HashMap<Method, Object[]> dynamicMethods = new HashMap<Method, Object[]>();
        HashSet<Method> notDynamicMethods = new HashSet<Method>();
        
        for (Injectee injectee : parent.getInjectees()) {
            AnnotatedElement ae = injectee.getParent();
            if (ae == null) continue;
            
            if (ae instanceof Field) {
                String propName = BeanUtilities.getParameterNameFromField((Field) ae, true);
                if (propName == null) continue;
                
                PropertyChangeEvent pce = changedProperties.get(propName);
                if (pce != null) {
                    try {
                        ReflectionHelper.setField((Field) ae, target, pce.getNewValue());
                    }
                    catch (Throwable th) {
                        // TODO:  How to handle exceptions
                    }
                }
                
                continue;
            }
            
            if (ae instanceof Method) {
                Method method = (Method) ae;
                
                if (notDynamicMethods.contains(method)) continue;
                
                if (!BeanUtilities.hasDynamicParameter(method)) {
                    notDynamicMethods.add(method);
                    continue;
                }
                
                Object params[] = dynamicMethods.get(method);
                if (params == null) {
                    params = new Object[method.getParameterTypes().length];
                    dynamicMethods.put(method, params);
                }
                
                String propName = BeanUtilities.getParameterNameFromMethod(method, injectee.getPosition());
                if (propName == null) {
                    ActiveDescriptor<?> paramDescriptor = locator.getInjecteeDescriptor(injectee);
                    if (paramDescriptor == null) {
                        params[injectee.getPosition()] = null;
                    }
                    else {
                        params[injectee.getPosition()] = locator.getServiceHandle(paramDescriptor).getService();
                    }
                }
                else {
                    PropertyChangeEvent pce = changedProperties.get(propName);
                    if (pce != null) {
                        params[injectee.getPosition()] = pce.getNewValue();
                    }
                    else {
                        params[injectee.getPosition()] = BeanUtilities.getBeanPropertyValue(propName, bean);
                    }
                }
                
            }
            
        }
        
        for(Map.Entry<Method, Object[]> entries : dynamicMethods.entrySet()) {
            try {
                ReflectionHelper.invoke(target, entries.getKey(), entries.getValue(), true);
            }
            catch (Throwable e) {
                // How to handle errors?
            }
            
        }
        
        
        
        return;
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
            
            List<ActiveDescriptor<?>> typeDescriptors = locator.getDescriptors(new NoNameTypeFilter(locator, typeName, null));
            
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
                
                List<ActiveDescriptor<?>> typeDescriptors = locator.getDescriptors(new NoNameTypeFilter(locator, change.getChangeType().getName(), null));
                
                for (ActiveDescriptor<?> typeDescriptor : typeDescriptors) {
                    // These match the type, so now we have to add one per instance
                    added.add(addInstanceDescriptor(config, typeDescriptor, addedInstanceKey, addedInstanceBean));
                }
            }
            else if (Change.ChangeCategory.MODIFY_INSTANCE.equals(change.getChangeCategory())) {
                String addedInstanceKey = change.getInstanceKey();
                Object addedInstanceBean = change.getInstanceValue();
                
                List<ActiveDescriptor<?>> typeDescriptors = locator.getDescriptors(
                        new NoNameTypeFilter(locator, change.getChangeType().getName(), addedInstanceKey));
                
                for (ActiveDescriptor<?> typeDescriptor : typeDescriptors) {
                    // There should only be one, but the list is safer
                    modifyInstanceDescriptor(typeDescriptor, addedInstanceKey, addedInstanceBean, change.getModifiedProperties());
                }
                
            }
        }
        
        // Add all instances
        if (!added.isEmpty()) {
            config.commit();
        
            // Create demand for all the ones we just added
            for (ActiveDescriptor<?> descriptor : added) {
                ServiceHandle<?> handle = locator.getServiceHandle(descriptor);
                handle.getService();  // TODO: how to handle errors?
            }
        }
            
    }
}
