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

import org.glassfish.hk2.configuration.hub.api.BeanDatabase;
import org.glassfish.hk2.configuration.hub.api.BeanDatabaseUpdateListener;
import org.glassfish.hk2.configuration.hub.api.Change;
import org.glassfish.hk2.configuration.hub.api.Instance;
import org.glassfish.hk2.configuration.hub.xml.dom.integration.XmlDomIntegrationCommitMessage;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * Listens for updates
 * @author jwells
 */
public class WritebackHubListener implements BeanDatabaseUpdateListener {
    private final static String SET_PREFIX = "set";
    
    private final ReplayProtector replayProtector;
    
    public WritebackHubListener(ReplayProtector replayProtector) {
        this.replayProtector = replayProtector;
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
    
    private void doModification(Change change, Map<String, Object> beanLikeMap, HK2ConfigBeanMetaData metadata, ConfigBeanProxy originalConfigBean) {
        List<PropertyChangeEvent> modified = new LinkedList<PropertyChangeEvent>();
        
        for (PropertyChangeEvent propChange : change.getModifiedProperties()) {
            String propName = propChange.getPropertyName();
            Class<?> setterType;
            
            Object newValue = propChange.getNewValue();
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
            
            modified.add(propChange);
        }
        
        if (!modified.isEmpty()) {
            replayProtector.addListOfChangesHappening(modified);
        }
        
    }
    
    private void modifyInstance(final Change change, final Map<String, Object> beanLikeMap, final HK2ConfigBeanMetaData metadata) {
        ConfigBeanProxy originalConfigBean = metadata.getConfigBean();
        
        try {
            ConfigSupport.apply(new SingleConfigCode<ConfigBeanProxy>() {

                @Override
                public Object run(ConfigBeanProxy param) throws PropertyVetoException,
                        TransactionFailure {
                    doModification(change, beanLikeMap, metadata, param);
                    
                    return null;
                }
                
            }, originalConfigBean);
        }
        catch (TransactionFailure e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
            
            Change.ChangeCategory category = change.getChangeCategory();
            
            if (category.equals(Change.ChangeCategory.MODIFY_INSTANCE)) {
                modifyInstance(change, beanLikeMap, metadata);
            }
            
            if (category.equals(Change.ChangeCategory.ADD_INSTANCE)) {
                throw new AssertionError("not yet implemented");
            }
            if (category.equals(Change.ChangeCategory.REMOVE_INSTANCE)) {
                throw new AssertionError("not yet implemented");
            }
            
            // Others we just ignore 
        }

    }

}
