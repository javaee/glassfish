/*
 * $Id: ApplicationBean.java,v 1.1 2005/09/20 21:11:24 edburns Exp $
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


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * <p>Configuration bean for <code>&lt;application&gt; element.</p>
 */

// IMPLEMENTATION NOTE:  It is necessary to collect the class names of the
// sub-elements representing pluggable components, so we can chain them together
// if the implementation classes have appropriate constructors.

public class ApplicationBean {


    // -------------------------------------------------------------- Properties


    private LocaleConfigBean localeConfig;
    public LocaleConfigBean getLocaleConfig() { return localeConfig; }
    public void setLocaleConfig(LocaleConfigBean localeConfig)
    { this.localeConfig = localeConfig; }


    private String messageBundle;
    public String getMessageBundle() { return messageBundle; }
    public void setMessageBundle(String messageBundle)
    { this.messageBundle = messageBundle; }

    private String defaultRenderKitId;
    public String getDefaultRenderKitId() { return defaultRenderKitId; }
    public void setDefaultRenderKitId(String defaultRenderKitId)
    { this.defaultRenderKitId = defaultRenderKitId; }

    // -------------------------------------------- ActionListenerHolder Methods


    private List<String> actionListeners = new ArrayList<String>();


    public void addActionListener(String actionListener) {
        if (!actionListeners.contains(actionListener)) {
            actionListeners.add(actionListener);
        }
    }


    public String[] getActionListeners() {
        String results[] = new String[actionListeners.size()];
        return ((String[]) actionListeners.toArray(results));
    }


    public void removeActionListener(String actionListener) {
        actionListeners.remove(actionListener);
    }


    // ----------------------------------------- NavigationHandlerHolder Methods


    private List<String> navigationHandlers = new ArrayList<String>();


    public void addNavigationHandler(String navigationHandler) {
        if (!navigationHandlers.contains(navigationHandler)) {
            navigationHandlers.add(navigationHandler);
        }
    }


    public String[] getNavigationHandlers() {
        String results[] = new String[navigationHandlers.size()];
        return ((String[]) navigationHandlers.toArray(results));
    }


    public void removeNavigationHandler(String navigationHandler) {
        navigationHandlers.remove(navigationHandler);
    }


    // ------------------------------------------ PropertyResolverHolder Methods


    private List<String> propertyResolvers = new ArrayList<String>();


    public void addPropertyResolver(String propertyResolver) {
        if (!propertyResolvers.contains(propertyResolver)) {
            propertyResolvers.add(propertyResolver);
        }
    }


    public String[] getPropertyResolvers() {
        String results[] = new String[propertyResolvers.size()];
        return ((String[]) propertyResolvers.toArray(results));
    }


    public void removePropertyResolver(String propertyResolver) {
        propertyResolvers.remove(propertyResolver);
    }

    // ---------------------------------------------- StateManagerHolder Methods

    private Map<String,ResourceBundleBean> resourceBundles = new TreeMap<String, ResourceBundleBean>();


    public void addResourceBundle(ResourceBundleBean descriptor) {
        resourceBundles.put(descriptor.getVar(), descriptor);
    }

    public ResourceBundleBean getResourceBundle(String name) {
        return (resourceBundles.get(name));
    }


    public ResourceBundleBean[] getResourceBundles() {
        ResourceBundleBean results[] =
            new ResourceBundleBean[resourceBundles.size()];
        return ((ResourceBundleBean[]) resourceBundles.values().toArray(results));
    }
    
    public void clearResourceBundles() {
        resourceBundles.clear();
    }
    
    public void removeResourceBundle(ResourceBundleBean descriptor) {
        resourceBundles.remove(descriptor.getVar());
    }

    // ---------------------------------------------- StateManagerHolder Methods


    private List<String> stateManagers = new ArrayList<String>();


    public void addStateManager(String stateManager) {
        if (!stateManagers.contains(stateManager)) {
            stateManagers.add(stateManager);
        }
    }


    public String[] getStateManagers() {
        String results[] = new String[stateManagers.size()];
        return ((String[]) stateManagers.toArray(results));
    }


    public void removeStateManager(String stateManager) {
        stateManagers.remove(stateManager);
    }


    // ------------------------------------------ VariableResolverHolder Methods


    private List<String> variableResolvers = new ArrayList<String>();


    public void addVariableResolver(String variableResolver) {
        if (!variableResolvers.contains(variableResolver)) {
            variableResolvers.add(variableResolver);
        }
    }


    public String[] getVariableResolvers() {
        String results[] = new String[variableResolvers.size()];
        return ((String[]) variableResolvers.toArray(results));
    }


    public void removeVariableResolver(String variableResolver) {
        variableResolvers.remove(variableResolver);
    }
    
    // ------------------------------------------ ELResolver Holder Methods


    private List<String> elResolvers = new ArrayList<String>();


    public void addELResolver(String elResolver) {
        if (!elResolvers.contains(elResolver)) {
            elResolvers.add(elResolver);
        }
    }


    public String[] getELResolvers() {
        String results[] = new String[elResolvers.size()];
        return ((String[]) elResolvers.toArray(results));
    }


    public void removeELResolver(String elResolver) {
        elResolvers.remove(elResolver);
    }


    // ------------------------------------------ ViewHandlerHolder Methods


    private List<String> viewHandlers = new ArrayList<String>();


    public void addViewHandler(String viewHandler) {
        if (!viewHandlers.contains(viewHandler)) {
            viewHandlers.add(viewHandler);
        }
    }


    public String[] getViewHandlers() {
        String results[] = new String[viewHandlers.size()];
        return ((String[]) viewHandlers.toArray(results));
    }


    public void removeViewHandler(String viewHandler) {
        viewHandlers.remove(viewHandler);
    }


    // ----------------------------------------------------------------- Methods


}
