/*
 * $Id: FacesConfigBean.java,v 1.1 2005/09/20 21:11:25 edburns Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at
 * https://javaserverfaces.dev.java.net/CDDL.html or
 * legal/CDDLv1.0.txt. 
 * See the License for the specific language governing
 * permission and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at legal/CDDLv1.0.txt.    
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * [Name of File] [ver.__] [Date]
 * 
 * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.faces.config.beans;
import com.sun.faces.util.ToolsUtil;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 * <p>Base configuration bean for JavaServer Faces Configuration Files.</p>
 */

public class FacesConfigBean {


    private static final Logger logger = ToolsUtil.getLogger(ToolsUtil.FACES_LOGGER +
            ToolsUtil.BEANS_LOGGER);


    // -------------------------------------------------------------- Properties


    private ApplicationBean application;
    public ApplicationBean getApplication() { return application; }
    public void setApplication(ApplicationBean application)
    { this.application = application; }


    private FactoryBean factory;
    public FactoryBean getFactory() { return factory; }
    public void setFactory(FactoryBean factory)
    { this.factory = factory; }


    private LifecycleBean lifecycle;
    public LifecycleBean getLifecycle() { return lifecycle; }
    public void setLifecycle(LifecycleBean lifecycle)
    { this.lifecycle = lifecycle; }


    // ------------------------------------------------- ComponentHolder Methods


    private Map<String,ComponentBean> components = new TreeMap<String, ComponentBean>();


    public void addComponent(ComponentBean descriptor) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "addComponent(" + descriptor.getComponentType() + ")");
        }
        components.put(descriptor.getComponentType(), descriptor);
    }


    public ComponentBean getComponent(String componentType) {
        return (components.get(componentType));
    }


    public ComponentBean[] getComponents() {
        ComponentBean results[] = new ComponentBean[components.size()];
        return (components.values().toArray(results));
    }


    public void removeComponent(ComponentBean descriptor) {
        components.remove(descriptor.getComponentType());
    }


    // ------------------------------------------------- ConverterHolder Methods


    private Map<String,ConverterBean> convertersByClass = new TreeMap<String, ConverterBean>();
    private Map<String,ConverterBean> convertersById = new TreeMap<String, ConverterBean>();


    public void addConverter(ConverterBean descriptor) {
        if (descriptor.getConverterId() != null) {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "addConverterById(" +
                          descriptor.getConverterId() + ")");
            }
            convertersById.put(descriptor.getConverterId(), descriptor);
        } else {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "addConverterByClass(" +
                          descriptor.getConverterForClass() + ")");
            }
            convertersByClass.put(descriptor.getConverterForClass(),
                                  descriptor);
        }
    }


    public ConverterBean getConverterByClass(String converterForClass) {
        return (convertersByClass.get(converterForClass));
    }


    public ConverterBean getConverterById(String converterId) {
        return (convertersById.get(converterId));
    }


    public ConverterBean[] getConvertersByClass() {
        ConverterBean results[] = new ConverterBean[convertersByClass.size()];
        return (convertersByClass.values().toArray(results));
    }


    public ConverterBean[] getConvertersById() {
        ConverterBean results[] = new ConverterBean[convertersById.size()];
        return (convertersById.values().toArray(results));
    }


    public void removeConverter(ConverterBean descriptor) {
        if (descriptor.getConverterId() != null) {
            convertersById.remove(descriptor.getConverterId());
        } else {
            convertersByClass.remove(descriptor.getConverterForClass());
        }
    }


    // ----------------------------------------------- ManagedBeanHolder Methods


    private Map<String,ManagedBeanBean> managedBeans = new TreeMap<String, ManagedBeanBean>();


    public void addManagedBean(ManagedBeanBean descriptor) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "addManagedBean(" +
                      descriptor.getManagedBeanName() + ")");
        }
        managedBeans.put(descriptor.getManagedBeanName(), descriptor);
    }

    public ManagedBeanBean getManagedBean(String name) {
        return (managedBeans.get(name));
    }


    public ManagedBeanBean[] getManagedBeans() {
        ManagedBeanBean results[] =
            new ManagedBeanBean[managedBeans.size()];
        return (managedBeans.values().toArray(results));
    }


    public void removeManagedBean(ManagedBeanBean descriptor) {
        managedBeans.remove(descriptor.getManagedBeanName());
    }


    // -------------------------------------------- NavigationRuleHolder Methods


    private Map<String,NavigationRuleBean> navigationRules = new TreeMap<String, NavigationRuleBean>();


    public void addNavigationRule(NavigationRuleBean descriptor) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "addNavigationRule(" + descriptor.getFromViewId() + ")");
        }
        navigationRules.put(descriptor.getFromViewId(), descriptor);
    }


    public NavigationRuleBean getNavigationRule(String fromViewId) {
        return (navigationRules.get(fromViewId));
    }


    public NavigationRuleBean[] getNavigationRules() {
        NavigationRuleBean results[] =
            new NavigationRuleBean[navigationRules.size()];
        return
            (navigationRules.values().toArray(results));
    }


    public void removeNavigationRule(NavigationRuleBean descriptor) {
        navigationRules.remove(descriptor.getFromViewId());
    }


    // -------------------------------------------- ReferencedBeanHolder Methods


    private Map<String,ReferencedBeanBean> referencedBeans = new TreeMap<String, ReferencedBeanBean>();


    public void addReferencedBean(ReferencedBeanBean descriptor) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "addReferencedBean(" +
                      descriptor.getReferencedBeanName() + ")");
        }
        referencedBeans.put(descriptor.getReferencedBeanName(), descriptor);
    }

    public ReferencedBeanBean getReferencedBean(String name) {
        return (referencedBeans.get(name));
    }


    public ReferencedBeanBean[] getReferencedBeans() {
        ReferencedBeanBean results[] =
            new ReferencedBeanBean[referencedBeans.size()];
        return (referencedBeans.values().toArray(results));
    }


    public void removeReferencedBean(ReferencedBeanBean descriptor) {
        referencedBeans.remove(descriptor.getReferencedBeanName());
    }


    // ------------------------------------------------- RenderKitHolder Methods


    private Map<String,RenderKitBean> renderKits = new TreeMap<String, RenderKitBean>();


    public void addRenderKit(RenderKitBean descriptor) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "addRenderKit(" + descriptor.getRenderKitId() + ")");
        }
        renderKits.put(descriptor.getRenderKitId(), descriptor);
    }

    public RenderKitBean getRenderKit(String id) {
        return (renderKits.get(id));
    }


    public RenderKitBean[] getRenderKits() {
        RenderKitBean results[] =
            new RenderKitBean[renderKits.size()];
        return (renderKits.values().toArray(results));
    }


    public void removeRenderKit(RenderKitBean descriptor) {
        renderKits.remove(descriptor.getRenderKitId());
    }


    // ------------------------------------------------- ValidatorHolder Methods


    private Map<String,ValidatorBean> validators = new TreeMap<String, ValidatorBean>();


    public void addValidator(ValidatorBean descriptor) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "addValidator(" + descriptor.getValidatorId() + ")");
        }
        validators.put(descriptor.getValidatorId(), descriptor);
    }


    public ValidatorBean getValidator(String id) {
        return (validators.get(id));
    }


    public ValidatorBean[] getValidators() {
        ValidatorBean results[] = new ValidatorBean[validators.size()];
        return (validators.values().toArray(results));
    }


    public void removeValidator(ValidatorBean descriptor) {
        validators.remove(descriptor.getValidatorId());
    }


    // ----------------------------------------------------------------- Methods


}
