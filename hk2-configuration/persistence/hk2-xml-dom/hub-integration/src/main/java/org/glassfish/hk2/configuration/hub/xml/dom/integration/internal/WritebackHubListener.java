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
import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.glassfish.hk2.configuration.hub.api.BeanDatabase;
import org.glassfish.hk2.configuration.hub.api.BeanDatabaseUpdateListener;
import org.glassfish.hk2.configuration.hub.api.Change;
import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.configuration.hub.api.Instance;
import org.glassfish.hk2.configuration.hub.api.Type;
import org.glassfish.hk2.configuration.hub.xml.dom.integration.XmlDomIntegrationCommitMessage;
import org.glassfish.hk2.utilities.reflection.Logger;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigModel;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * Listens for updates
 * @author jwells
 */
@Singleton
public class WritebackHubListener implements BeanDatabaseUpdateListener {
    private final static String SET_PREFIX = "set";
    private final static String GET_PREFIX = "get";
    private final static String STAR = "*";
    
    @Inject
    private ReplayProtector replayProtector;
    
    @Inject
    private Hub hub;
    
    private WritebackHubListener() {
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.configuration.hub.api.BeanDatabaseUpdateListener#initialize(org.glassfish.hk2.configuration.hub.api.BeanDatabase)
     */
    @Override
    public void initialize(BeanDatabase database) {
        // This guy only does something on known modifications, so do nothing
    }
    
    private static Method getMethod(Class<?> clazz, String methodName, Class<?>[] setterType) {
        try {
            return clazz.getMethod(methodName, setterType);
        }
        catch (NoSuchMethodException e) {
        }
        
        return null;
    }
    
    private static Method findSetterMethod(String propName, Class<?> clazz, Class<?> setterType) {
        Class<?> setterTypes[] = new Class<?>[1];
        setterTypes[0] = setterType;
        
        char chars[] = propName.toCharArray();
        
        Method foundMethod = null;
        for (int lcv = 0; lcv < chars.length; lcv++) {
            boolean finished = false;
            String trySetterName;
            if (Character.isUpperCase(chars[lcv])) {
                // We are finished here, try it one extra time
                finished = true;
                
                trySetterName = SET_PREFIX + new String(chars);
            }
            else {
                chars[lcv] = Character.toUpperCase(chars[lcv]);
                
                trySetterName = SET_PREFIX + new String(chars);
            }
            
            foundMethod = getMethod(clazz, trySetterName, setterTypes);
            if (foundMethod != null) break;
            if (finished) break;
        }
        
        return foundMethod;
    }
    
    private static Method findChildGetterMethod(Dom parentDom, String xmlName, Class<?> childType) {
        ConfigModel model = parentDom.model;
        
        for (Method m : parentDom.getImplementationClass().getMethods()) {
            String methodName = m.getName();
            if (!methodName.startsWith(GET_PREFIX)) continue;
            
            Class<?> params[] = m.getParameterTypes();
            if (params.length != 0) continue;
            
            Class<?> retType = m.getReturnType();
            
            if (retType == null || !List.class.equals(retType)) continue;
            
            ConfigModel.Property prop = model.toProperty(m);
            if (prop == null) continue;
            
            String methodXmlName = prop.xmlName();
            if (STAR.equals(prop.xmlName())) {
                methodXmlName = childType.getSimpleName();
                methodXmlName = ConfigModel.camelCaseToXML(methodXmlName);
            }
            
            return m;
        }
        
        return null;
    }
    
    private void doModification(Change change, ConfigBeanProxy originalConfigBean) {
        List<PropertyChangeEvent> modified = new LinkedList<PropertyChangeEvent>();
        
        for (PropertyChangeEvent propChange : change.getModifiedProperties()) {
            String propName = propChange.getPropertyName();
            Class<?> setterType;
            
            Object newValue = propChange.getNewValue();
            Logger.getLogger().debug("WRITEBACK: Modifying property " + propName +
                    " to value " + newValue +
                    " from value " + propChange.getOldValue());
            
            if (newValue != null) {
                setterType = newValue.getClass();
            }
            else {
                newValue = propChange.getOldValue();
                if (newValue != null) {
                    setterType = newValue.getClass();
                }
                else {
                    // Going from null to null, who cares?, and should not be possible
                    continue;
                }
            }
            
            if (List.class.isAssignableFrom(setterType)) {
                // This is a child, do not handle here
                Logger.getLogger().debug("WRITEBACK: Not modifying property " + propName +
                        " because it is of type List and is therefor a child");
                
                continue;
            }
            
            Method setter = findSetterMethod(propName, originalConfigBean.getClass(), setterType);
            if (setter == null) {
                // This is not a settable attribute (or we cannot find the setter), skip it
                Logger.getLogger().debug("WRITEBACK: Not modifying property " + propName +
                        " because we could not find a setter for it, it may be write-only");
                
                continue;
            }
            
            try {
                setter.invoke(originalConfigBean, newValue);
            }
            catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            catch (InvocationTargetException e) {
                Logger.getLogger().debug(getClass().getName(), setter.getName(), e.getTargetException());
            }
            
            Logger.getLogger().debug("WRITEBACK: property " + propName +
                    " successfully modified");
            modified.add(propChange);
        }
        
        if (!modified.isEmpty()) {
            replayProtector.addListOfChangesHappening(modified);
        }
        
    }
    
    private void doBeanInitialization(Map<String, Object> initialValues, ConfigBeanProxy originalConfigBean) {
        
        Dom dom = Dom.unwrap(originalConfigBean);
        if (dom == null) {
            return;
        }
        
        Set<String> attributeNames = dom.model.getAttributeNames();
        
        for (Map.Entry<String, Object> entry : initialValues.entrySet()) {
            String propName = entry.getKey();
            Object newValue = entry.getValue();
            
            if (attributeNames.contains(propName)) {
                String strValue;
                if (newValue == null) {
                    strValue = null;
                }
                else {
                    strValue = newValue.toString();
                }
                
                dom.attribute(propName, strValue);
                
                continue;
            }
            
            if (newValue == null) continue;
            Class<?> setterType = newValue.getClass();
            
            if (List.class.isAssignableFrom(setterType)) {
                // This is a child, do not handle here
                continue;
            }
            
            Method setter = findSetterMethod(propName, originalConfigBean.getClass(), setterType);
            if (setter == null) {
                // This is not a settable attribute (or we cannot find the setter), skip it
                continue;
            }
            
            try {
                setter.invoke(originalConfigBean, newValue);
            }
            catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            catch (InvocationTargetException e) {
                // TODO:  What to do here?
            }
        }
    }
    
    private void modifyInstance(final Change change, final Map<String, Object> beanLikeMap, final HK2ConfigBeanMetaData metadata) {
        ConfigBeanProxy originalConfigBean = metadata.getConfigBean();
        
        try {
            ConfigSupport.apply(new SingleConfigCode<ConfigBeanProxy>() {

                @Override
                public Object run(ConfigBeanProxy param) throws PropertyVetoException,
                        TransactionFailure {
                    doModification(change, param);
                    
                    return null;
                }
                
            }, originalConfigBean);
        }
        catch (TransactionFailure e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private static String getElementName(String typeName) {
        int index = typeName.lastIndexOf('/');
        if (index < 0) return typeName;
        
        return typeName.substring(index + 1);
    }
    
    private static String getInstanceKey(String instanceName) {
        int index = instanceName.lastIndexOf('.');
        if (index < 0) return instanceName;
        
        return instanceName.substring(index + 1);
    }
    
    private Instance findParent(String childTypeName, String childInstanceName) {
        int index = childTypeName.lastIndexOf('/');
        if (index < 0) return null;
        
        String parentTypeName = childTypeName.substring(0, index);
        
        index = childInstanceName.lastIndexOf('.');
        if (index < 0) return null;
        
        String parentInstanceName = childInstanceName.substring(0, index);
        
        Instance retVal = hub.getCurrentDatabase().getInstance(parentTypeName, parentInstanceName);
        return retVal;
    }
    
    private void addChild(final Change change, final Map<String, Object> newGuy) {
        Instance instance = change.getInstanceValue();
        String instanceName = change.getInstanceKey();
        Type instanceType = change.getChangeType();
        String typeName = instanceType.getName();
        
        Logger.getLogger().debug("WRITEBACK: adding child with type " + typeName + " and instance " + instanceName);
        
        Instance parent = findParent(typeName, instanceName);
        if (parent == null) {
            Logger.getLogger().debug("WRITEBACK: could not find parent of type " + typeName + " and instance " + instanceName);
            return;
        }
        
        Object rawParentMetadata = parent.getMetadata();
        if (rawParentMetadata == null || !(rawParentMetadata instanceof HK2ConfigBeanMetaData)) {
            Logger.getLogger().debug("WRITEBACK: No metadata for type " + typeName + " and instance " + instanceName);
            return;
        }
        HK2ConfigBeanMetaData parentMetadata = (HK2ConfigBeanMetaData) rawParentMetadata;
        
        ConfigBeanProxy parentConfigBean = parentMetadata.getConfigBean();
        if (parentConfigBean == null) {
            Logger.getLogger().debug("WRITEBACK: No configuration bean for type " + typeName + " and instance " + instanceName);
            return;
        }
        
        Dom parentDom = Dom.unwrap(parentConfigBean);
        if (parentDom == null) {
            Logger.getLogger().debug("WRITEBACK: No parent Dom for type " + typeName + " and instance " + instanceName);
            return;
        }
        
        final String childElementName = getElementName(typeName);
        Dom childDom = parentDom.element(childElementName);
        if (childDom == null) {
            Logger.getLogger().debug("WRITEBACK: No parent Dom for type " + typeName + " and instance " + instanceName +
                    " with element name " + childElementName);
            return;
        }
        
        final Class<? extends ConfigBeanProxy> childClass = (Class<? extends ConfigBeanProxy>) childDom.getImplementationClass();
        
        Object child = null;
        try {
             child = ConfigSupport.apply(new SingleConfigCode<ConfigBeanProxy>() {

                @Override
                public Object run(ConfigBeanProxy writeableBean) throws PropertyVetoException,
                        TransactionFailure {
                    Dom parentDom = Dom.unwrap(writeableBean);
                    
                    Method parentListMethod = findChildGetterMethod(parentDom, childElementName, childClass);
                    if (parentListMethod == null) {
                        Logger.getLogger().debug("WRITEBACK: Could not find parent list method for " + childElementName + " of class " + childClass.getName());
                        return null;
                    }
                    
                    ConfigBeanProxy child = writeableBean.createChild(childClass);
                    
                    doBeanInitialization(newGuy, child);
                    
                    List<ConfigBeanProxy> addToMe = null;
                    try {
                        Object result = parentListMethod.invoke(writeableBean);
                        if (result instanceof List) {
                            addToMe = (List<ConfigBeanProxy>) result;
                        }
                    }
                    catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                    catch (InvocationTargetException e) {
                        Logger.getLogger().debug(getClass().getName(), "run", e.getTargetException());
                        return null;
                    }
                    
                    addToMe.add(child);
                    
                    return child;
                }
                
            }, parentConfigBean);
        }
        catch (TransactionFailure e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
        
        if (child != null) {
            Logger.getLogger().debug("WRITEBACK: added child of type " + typeName + " and instance " + instanceName);
            instance.setMetadata(new HK2ConfigBeanMetaData((ConfigBeanProxy) child));
        }
        else {
            Logger.getLogger().debug("WRITEBACK: failed to add child of type " + typeName + " and instance " + instanceName);
        }
    }
    
    private void removeChild(final Change change) {
        String instanceName = change.getInstanceKey();
        Type instanceType = change.getChangeType();
        String typeName = instanceType.getName();
        
        Logger.getLogger().debug("WRITEBACK: removing type " + typeName + " of instance " + instanceName);
        
        Instance parent = findParent(typeName, instanceName);
        if (parent == null) {
            Logger.getLogger().debug("WRITEBACK: during removal could not find parent of type " + typeName + " of instance " + instanceName);
            return;
        }
        
        Object rawParentMetadata = parent.getMetadata();
        if (rawParentMetadata == null || !(rawParentMetadata instanceof HK2ConfigBeanMetaData)) {
            Logger.getLogger().debug("WRITEBACK: during removal could not find metadata of type " + typeName + " of instance " + instanceName);
            return;
        }
        HK2ConfigBeanMetaData parentMetadata = (HK2ConfigBeanMetaData) rawParentMetadata;
        
        ConfigBeanProxy parentConfigBean = parentMetadata.getConfigBean();
        if (parentConfigBean == null) {
            Logger.getLogger().debug("WRITEBACK: during removal could not find parent config bean of type " + typeName + " of instance " + instanceName);
            return;
        }
        
        Dom parentDom = Dom.unwrap(parentConfigBean);
        if (parentDom == null) {
            Logger.getLogger().debug("WRITEBACK: during removal could not find parent Dom of type " + typeName + " of instance " + instanceName);
            return;
        }
        
        final String childElementName = getElementName(typeName);
        Dom childDom = parentDom.element(childElementName);
        if (childDom == null) {
            Logger.getLogger().debug("WRITEBACK: during removal could not find instance Dom of type " + typeName + " of instance " + instanceName);
            return;
        }
        
        final Class<? extends ConfigBeanProxy> childClass = (Class<? extends ConfigBeanProxy>) childDom.getImplementationClass();
        
        final String instanceKey = getInstanceKey(instanceName);
        
        try {
             ConfigSupport.apply(new SingleConfigCode<ConfigBeanProxy>() {

                @Override
                public Object run(ConfigBeanProxy writeableBean) throws PropertyVetoException,
                        TransactionFailure {
                    Dom parentDom = Dom.unwrap(writeableBean);
                    
                    Method parentListMethod = findChildGetterMethod(parentDom, childElementName, childClass);
                    if (parentListMethod == null) {
                        Logger.getLogger().debug("WRITEBACK: during removal could not find getter for element " + childElementName +
                                " of class " + childClass);
                        return null;
                    }
                    
                    List<ConfigBeanProxy> addToMe = null;
                    try {
                        Object result = parentListMethod.invoke(writeableBean);
                        if (result instanceof List) {
                            addToMe = (List<ConfigBeanProxy>) result;
                        }
                    }
                    catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                    catch (InvocationTargetException e) {
                        Logger.getLogger().debug(getClass().getName(), "run", e.getTargetException());
                        
                        return null;
                    }
                    
                    Dom removeMeDom = null;
                    ConfigBeanProxy removeMe = null;
                    for (ConfigBeanProxy candidate : addToMe) {
                        Dom candidateDom = Dom.unwrap(candidate);
                        if (candidateDom == null) continue;
                        
                        String candidateKey = candidateDom.getKey();
                        if (candidateKey == null) continue;
                        
                        if (instanceKey.equals(candidateKey)) {
                            removeMe = candidate;
                            removeMeDom = candidateDom;
                            break;
                        }
                        
                    }
                    
                    if (removeMe != null) {
                        Logger.getLogger().debug("WRITEBACK: during removal removing service of type " + removeMeDom.getImplementation());
                        
                        parentDom.removeChild(removeMeDom);
                        
                        addToMe.remove(removeMe);
                    }
                    else {
                        Logger.getLogger().debug("WRITEBACK: during removal NOT removing unfound type for " + childElementName +
                                " of class " + childClass);
                    }
                    
                    return null;
                }
                
            }, parentConfigBean);
        }
        catch (TransactionFailure e) {
            Logger.getLogger().debug(getClass().getName(), "removeChild", e);
        }
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.configuration.hub.api.BeanDatabaseUpdateListener#databaseHasChanged(org.glassfish.hk2.configuration.hub.api.BeanDatabase, java.lang.Object, java.util.List)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void databaseHasChanged(BeanDatabase newDatabase,
            Object commitMessage, List<Change> changes) {
        if ((commitMessage != null) &&
                (commitMessage instanceof XmlDomIntegrationCommitMessage)) {
            // This is a change coming from the bean into the map, not
            // the other way around, so stop right now
            return;
        }
        
        for (Change change : changes) {
            Instance instance = change.getInstanceValue();
            Object rawBean = instance.getBean();
            if (!(rawBean instanceof Map)) {
                // Not translated, forget it
                continue;
            }
            Map<String, Object> beanLikeMap = (Map<String,Object>) rawBean;
            
            Change.ChangeCategory category = change.getChangeCategory();
            
            if (category.equals(Change.ChangeCategory.MODIFY_INSTANCE)) {
                Object rawMetaData = instance.getMetadata();
                if (rawMetaData == null ||
                        !(rawMetaData instanceof HK2ConfigBeanMetaData)) {
                    // Not our metadata, forget it
                    continue;
                }
                HK2ConfigBeanMetaData metadata = (HK2ConfigBeanMetaData) rawMetaData;
                if (metadata.getConfigBean() == null) {
                    // We did put it in, but it was not a ConfigBeanProxy (can't happen... probably)
                    continue;
                }
                
                modifyInstance(change, beanLikeMap, metadata);
            }
            else if (category.equals(Change.ChangeCategory.ADD_INSTANCE)) {
                addChild(change, beanLikeMap);
            }
            else if (category.equals(Change.ChangeCategory.REMOVE_INSTANCE)) {
                removeChild(change);
            }
            
            // Others we just ignore 
        }

    }

}
