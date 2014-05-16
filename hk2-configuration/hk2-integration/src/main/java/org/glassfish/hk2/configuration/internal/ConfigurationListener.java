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
import java.beans.PropertyChangeListener;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
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
import org.glassfish.hk2.configuration.api.ConfiguredBy;
import org.glassfish.hk2.configuration.api.PostDynamicChange;
import org.glassfish.hk2.configuration.api.PreDynamicChange;
import org.glassfish.hk2.configuration.hub.api.BeanDatabase;
import org.glassfish.hk2.configuration.hub.api.BeanDatabaseUpdateListener;
import org.glassfish.hk2.configuration.hub.api.Change;
import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.configuration.hub.api.Type;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.reflection.ClassReflectionHelper;
import org.glassfish.hk2.utilities.reflection.MethodWrapper;
import org.glassfish.hk2.utilities.reflection.ReflectionHelper;
import org.glassfish.hk2.utilities.reflection.internal.ClassReflectionHelperImpl;

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
    
    private final Map<String, ModificationInformation> typeInformation = 
            new HashMap<String, ModificationInformation>();
    
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
    
    private boolean invokePreMethod(Object target,
            List<PropertyChangeEvent> changes,
            String typeName) {
        ModificationInformation modInfo = typeInformation.get(typeName);
        if (modInfo == null) {
            return true;
        }
        
        Method preMethod = modInfo.getPreDynamicChangeMethod(target.getClass());
        if (preMethod == null) {
            return true;
        }
        
        Class<?> params[] = preMethod.getParameterTypes();
        
        Object result = null;
        if (params.length <= 0) {
            try {
                result = ReflectionHelper.invoke(target, preMethod, new Object[0], true);
            }
            catch (Throwable th) {
                return true;
            }
        }
        else if (params.length == 1) {
            if (!List.class.isAssignableFrom(params[0])) return true;
            
            Object pValues[] = new Object[1];
            pValues[0] = Collections.unmodifiableList(changes);
            
            try {
                result = ReflectionHelper.invoke(target, preMethod, pValues, true);
            }
            catch (Throwable th) {
                return true;
            }
        }
        else {
            return true;
        }
        
        if (result == null || !(result instanceof Boolean)) {
            return true;
        }
        Boolean b = (Boolean) result;
        
        return b;
        
    }
    
    private void invokePostMethod(Object target,
            List<PropertyChangeEvent> changes,
            String typeName) {
        ModificationInformation modInfo = typeInformation.get(typeName);
        if (modInfo == null) return;
        
        Method postMethod = modInfo.getPostDynamicChangeMethod(target.getClass());
        if (postMethod == null) return;
        
        Class<?> params[] = postMethod.getParameterTypes();
        
        if (params.length <= 0) {
            try {
                ReflectionHelper.invoke(target, postMethod, new Object[0], true);
            }
            catch (Throwable th) {
                return;
            }
        }
        else if (params.length == 1) {
            if (!List.class.isAssignableFrom(params[0])) return;
            
            Object pValues[] = new Object[1];
            pValues[0] = Collections.unmodifiableList(changes);
            
            try {
                ReflectionHelper.invoke(target, postMethod, pValues, true);
            }
            catch (Throwable th) {
                return;
            }
        }
    }
    
    private void modifyInstanceDescriptor(ActiveDescriptor<?> parent,
            String name,
            Object bean,
            String typeName,
            List<PropertyChangeEvent> changes
            ) {
        injectionResolver.addBean(parent, bean);
        
        Object target = context.findOnly(parent);
        if (target == null) {
            // race lost
            return;
        }
        
        boolean moveForward = invokePreMethod(target, changes, typeName);
        if (!moveForward) {
            // User told us to NOT move forward
            return;
        }
        
        HashMap<String, PropertyChangeEvent> changedProperties = new HashMap<String, PropertyChangeEvent>();
        for (PropertyChangeEvent pce : changes) {
            changedProperties.put(pce.getPropertyName(), pce);
            
            if (target instanceof PropertyChangeListener) {
                PropertyChangeListener listener = (PropertyChangeListener) target;
                
                try {
                    listener.propertyChange(pce);
                }
                catch (Throwable th) {
                    // TODO:  What to do about errors?
                }
            }
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
        
        invokePostMethod(target, changes, typeName);
        
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
            
            typeInformation.put(typeName, new ModificationInformation());
            
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
            if (!isEager(descriptor)) continue;
            
            ServiceHandle<?> handle = locator.getServiceHandle(descriptor);
            handle.getService();  // TODO: how to handle errors?
        }
        
    }
    
    private static boolean isEager(ActiveDescriptor<?> descriptor) {
        Class<?> implClass = descriptor.getImplementationClass();
        if (implClass == null) return false;
        
        ConfiguredBy configuredBy = implClass.getAnnotation(ConfiguredBy.class);
        if (configuredBy == null) return false;
        
        return ConfiguredBy.CreationPolicy.EAGER.equals(configuredBy.creationPolicy());
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.configuration.hub.api.BeanDatabaseUpdateListener#databaseHasChanged(org.glassfish.hk2.configuration.hub.api.BeanDatabase, java.util.List)
     */
    @Override
    public void databaseHasChanged(BeanDatabase newDatabase,
            List<Change> changes) {
        LinkedList<ActiveDescriptor<?>> added = new LinkedList<ActiveDescriptor<?>>();
        LinkedList<ActiveDescriptor<?>> removed = new LinkedList<ActiveDescriptor<?>>();
        DynamicConfiguration config = configurationService.createDynamicConfiguration();
        
        for (Change change : changes) {
            if (Change.ChangeCategory.ADD_INSTANCE.equals(change.getChangeCategory())) {
                String typeName = change.getChangeType().getName();
                if (!typeInformation.containsKey(typeName)) {
                    typeInformation.put(typeName, new ModificationInformation());
                }
                
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
                    modifyInstanceDescriptor(typeDescriptor,
                            addedInstanceKey,
                            addedInstanceBean,
                            change.getChangeType().getName(),
                            change.getModifiedProperties());
                }
                
            }
            else if (Change.ChangeCategory.REMOVE_TYPE.equals(change.getChangeCategory())) {
                String typeName = change.getChangeType().getName();
                ModificationInformation cache = typeInformation.remove(typeName);
                if (cache != null) {
                    cache.dispose();
                }
            }
            else if (Change.ChangeCategory.REMOVE_INSTANCE.equals(change.getChangeCategory())) {
                String addedInstanceKey = change.getInstanceKey();
                
                List<ActiveDescriptor<?>> removeDescriptors = locator.getDescriptors(new NoNameTypeFilter(locator, change.getChangeType().getName(), addedInstanceKey));
                
                for (ActiveDescriptor<?> removeDescriptor : removeDescriptors) {
                    config.addUnbindFilter(BuilderHelper.createSpecificDescriptorFilter(removeDescriptor));
                    
                    injectionResolver.removeBean(removeDescriptor);
                    
                    removed.add(removeDescriptor);
                }
                
            }
        }
        
        // Add all instances
        if (!added.isEmpty() || !removed.isEmpty()) {
            config.commit();
        
            // Create demand for all the ones we just added
            for (ActiveDescriptor<?> descriptor : added) {
                if (!isEager(descriptor)) continue;
                
                ServiceHandle<?> handle = locator.getServiceHandle(descriptor);
                handle.getService();  // TODO: how to handle errors?
            }
            
            // Destroy the ones we just removed
            for (ActiveDescriptor<?> descriptor : removed) {
                ServiceHandle<?> handle = locator.getServiceHandle(descriptor);
                handle.destroy();
            }
        }
            
    }
    
    private static class ModificationInformation {
        private final ClassReflectionHelper helper = new ClassReflectionHelperImpl();
        private final HashMap<Class<?>, Method> preMethods =
                new HashMap<Class<?>, Method>();
        private final HashMap<Class<?>, Method> postMethods =
                new HashMap<Class<?>, Method>();
        
        private Method getPreDynamicChangeMethod(Class<?> rawClass) {
            if (preMethods.containsKey(rawClass)) {
                return preMethods.get(rawClass);
            }
            
            Method preModificationMethod = getSpecialMethod(rawClass, PreDynamicChange.class);
            preMethods.put(rawClass, preModificationMethod);
            
            return preModificationMethod;
        }
        
        private Method getPostDynamicChangeMethod(Class<?> rawClass) {
            if (postMethods.containsKey(rawClass)) {
                return postMethods.get(rawClass);
            }
            
            Method postModificationMethod = getSpecialMethod(rawClass, PostDynamicChange.class);
            postMethods.put(rawClass, postModificationMethod);
            
            return postModificationMethod;
        }
        
        private Method getSpecialMethod(Class<?> rawClass, Class<? extends Annotation> anno) {
            Set<MethodWrapper> wrappers = helper.getAllMethods(rawClass);
            for (MethodWrapper wrapper : wrappers) {
                Method candidate = wrapper.getMethod();
                
                if (candidate.getAnnotation(anno) != null) {
                    return candidate;
                }
            }
            
            return null;
        }
        
        private void dispose() {
            helper.dispose();
            
            preMethods.clear();
            postMethods.clear();
        }
        
    }
}
