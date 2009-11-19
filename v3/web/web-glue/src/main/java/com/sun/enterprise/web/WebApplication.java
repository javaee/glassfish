/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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

package com.sun.enterprise.web;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.ApplicationConfig;
import com.sun.enterprise.config.serverbeans.Module;
import com.sun.enterprise.config.serverbeans.Engine;
import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.web.EnvironmentEntry;
import com.sun.enterprise.deployment.web.ContextParameter;
import com.sun.enterprise.util.Result;
import com.sun.logging.LogDomains;
import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.ApplicationContext;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.deployment.common.ApplicationConfigInfo;
import org.glassfish.deployment.common.DeploymentProperties;
import org.glassfish.web.plugin.common.EnvEntry;
import org.glassfish.web.plugin.common.ContextParam;

public class WebApplication implements ApplicationContainer<WebBundleDescriptor> {

    final Logger logger = LogDomains.getLogger(WebApplication.class, LogDomains.WEB_LOGGER);

    private final WebContainer container;
    private final WebModuleConfig wmInfo;
    Properties props = null;
    private Set<WebModule> webModules = new HashSet<WebModule>();
    private final org.glassfish.web.plugin.common.WebModuleConfig appConfigCustomizations;

    public WebApplication(WebContainer container, WebModuleConfig config, 
            Properties props, final ApplicationConfigInfo appConfigInfo) {
        this.container = container;
        this.wmInfo = config;
        this.props = props;
        this.appConfigCustomizations = extractCustomizations(appConfigInfo);
    }

    public boolean start(ApplicationContext appContext) throws Exception {

        webModules.clear();

        if (appContext!=null) {
            wmInfo.setAppClassLoader(appContext.getClassLoader());
            if (appContext instanceof DeploymentContext) {
                wmInfo.setDeploymentContext((DeploymentContext)appContext);
            }
            applyApplicationConfig(appContext);
        }

        List<Result<WebModule>> results = container.loadWebModule(
            wmInfo, "null", props);
        props = null;
        if (results == null) {
            logger.log(Level.SEVERE, "webApplication.unknownError");
            return false;
        }

        boolean isFailure = false;
        StringBuilder sb = null;
        for (Result<WebModule> result : results) {
            if (result.isFailure()) {
                if (sb == null) {
                    sb = new StringBuilder(result.exception().toString());
                } else {
                    sb.append(result.exception().toString());
                }
                logger.log(Level.WARNING, result.exception().toString(),
                           result.exception());
                isFailure = true;
            } else {
                webModules.add(result.result());
            }
        }

        if (isFailure) {
            webModules.clear();
            throw new Exception(sb.toString());
        }

        logger.info("Loading application " + wmInfo.getDescriptor().getName() +
                    " at " + wmInfo.getDescriptor().getContextRoot());

        return true;
    }

    public boolean stop(ApplicationContext stopContext) {

        props = null;

        if (Boolean.parseBoolean(stopContext.getAppProps().getProperty(DeploymentProperties.KEEP_SESSIONS))) {
            props = new Properties();
        }
        container.unloadWebModule(getDescriptor().getContextRoot(), null,
                                  null, props);

        return true;
    }

    /**
     * Suspends this application on all virtual servers.
     */
    public boolean suspend() {
        return container.suspendWebModule(
            wmInfo.getDescriptor().getContextRoot(), "null", null);
    }

    /**
     * Resumes this application on all virtual servers.
     */
    public boolean resume() throws Exception {
        // WebContainer.loadWebModule(), which is called by start(),
        // already checks if the web module has been suspended, and if so,
        // just resumes it and returns
        return start(null);
    }

    /**
     * Returns the class loader associated with this application
     *
     * @return ClassLoader for this app
     */
    public ClassLoader getClassLoader() {
        return wmInfo.getAppClassLoader();
    }

    WebContainer getContainer() {
        return container;
    }

    /**
     * Gets a set of all the WebModule instances (one per virtual
     * server deployment) of this WebApplication.
     * 
     * <p>For each WebModule in the returned set, the corresponding
     * ServletContext may be obtained by calling WebModule#getServletContext
     */
    public Set<WebModule> getWebModules() {
        return webModules;
    }

    /**
     * Returns the deployment descriptor associated with this application
     *
     * @return deployment descriptor if they exist or null if not
     */
    public WebBundleDescriptor getDescriptor() {
        return wmInfo.getDescriptor();
    }

    /**
     * Extracts the application config information for the web container
     * from the saved config info.  The saved config info is from the
     * in-memory configuration (domain.xml) if this app was already deployed
     * and is being redeployed.
     *
     * @param appConfigInfo
     * @return
     */
    private org.glassfish.web.plugin.common.WebModuleConfig extractCustomizations(
            final ApplicationConfigInfo appConfigInfo) {
        return appConfigInfo.get(trimmedModuleName(wmInfo.getName()), "web");
    }

    private String trimmedModuleName(String moduleName) {
        final int hash = moduleName.indexOf('#');
        if (hash == -1) {
            return moduleName;
        }
        return moduleName.substring(hash + 1);
    }
    /**
     * Applies application config customization (stored temporarily in the
     * start-up context's start-up parameters) to the web app's descriptor.
     * @param appContext
     */
    private void applyApplicationConfig(ApplicationContext appContext) {

        WebBundleDescriptor descriptor = wmInfo.getDescriptor();

        try {
            if (appConfigCustomizations != null) {

                EnvEntryCustomizer envEntryCustomizer =
                        new EnvEntryCustomizer(
                            descriptor.getEnvironmentEntrySet(),
                            appConfigCustomizations.getEnvEntry());
                ContextParamCustomizer contextParamCustomizer =
                        new ContextParamCustomizer(
                            descriptor.getContextParametersSet(),
                            appConfigCustomizations.getContextParam());

                envEntryCustomizer.applyCustomizations();
                contextParamCustomizer.applyCustomizations();
            }
        } catch (ClassCastException ex) {
            /*
             * If the user specified an env-entry value that does not
             * work with the env-entry type it can cause a class cast
             * exception.  Log the warning but continue working.
             */
            logger.log(Level.WARNING, "", ex);
        }
    }

    /*
     * Convenience class for applying customizations to descriptor items.
     * <p>
     * Much of the logic is the same for the different types of customizations -
     * and this class abstracts all the common behavior.  This may seem like
     * overkill, factoring this logic out like this, but the applyCustomizations
     * logic is not something we want to have two copies of.
     */
    private abstract class Customizer<T,U> {

        protected Set<T> descriptorItems;
        protected List<U> customizations;

        private String descriptorItemName;

        private Customizer(Set<T> descriptorItems, List<U> customizations, String descriptorItemName) {
            this.descriptorItems = descriptorItems;
            this.customizations = customizations;
            this.descriptorItemName = descriptorItemName;
        }

        /**
         * Indicates whether the customization says to ignore any corresponding
         * descriptor entry.
         * @param customization the customization
         * @return true if the user wants to ignore any corresponding descriptor entry; false otherwise
         */
        protected abstract boolean isIgnoreDescriptorItem(U customization);

        /**
         * Creates a new descriptor item using the information from the
         * customization.
         * @param customization the customization the gives the value(s) for the new descriptor
         * @return the new descriptor item
         */
        protected abstract T newDescriptorItem(U customization);

        /**
         * Assigns the values from the customization to the existing descriptor
         * item.
         * @param descriptorItem descriptor item to change
         * @param customization customization containing the new values to be set in the descriptor item
         */
        protected abstract void setDescriptorItemValue(T descriptorItem, U customization);

        /**
         * Returns the name from the descriptor item
         * @param descriptorItem
         * @return name from the descriptor item
         */
        protected abstract String getName(T descriptorItem);

        /**
         * Returns the value from the descriptor item
         * @param descriptorItem
         * @return value from the descriptor item
         */
        protected abstract String getValue(T descriptorItem);

        /**
         * Returns the name from the customization
         * @param customization
         * @return name from the customization
         */
        protected abstract String getCustomizationName(U customization);

        /**
         * Represents the customization as a String for logging.
         * @param customization
         * @return
         */
        protected abstract String toString(U customization);


        /**
         * Removes the descriptor item from the descriptor's collection
         * of this type of item.
         *
         * @param descriptorItem the item to remove
         */
        protected void removeDescriptorItem(T descriptorItem) {
            descriptorItems.remove(descriptorItem);
        }

        /**
         * Adds a new descriptor item to the descriptor's collection of
         * items, basing the new one on the customization the user created.
         *
         * @param customization
         * @return the newly-created item
         */
        protected T addDescriptorItem(U customization) {
            T newItem = newDescriptorItem(customization);
            descriptorItems.add(newItem);
            return newItem;
        }

        /**
         * Applies the set of customizations to the descriptor's set of
         * items.
         */
        void applyCustomizations () {
            boolean isFiner = logger.isLoggable(Level.FINER);

          nextCustomization:
            for (U customization : customizations) {
                /*
                 * For each customization try to find a descriptor item with
                 * the same name.  If there is one, either ignore the descriptor
                 * item (if that is what the customization specifies) or override
                 * the descriptor items'a value with the value from the
                 * customization.
                 */
                for (Iterator<T> it = descriptorItems.iterator(); it.hasNext();) {
                    T descriptorItem = it.next();
                    String dItemName = getName(descriptorItem);
                    String customizationItemName = getCustomizationName(customization);
                    if (dItemName.equals(customizationItemName)) {
                        /*
                         * We found a descriptor item that matches this
                         * customization's name.
                         */
                        if (isIgnoreDescriptorItem(customization)) {
                            /*
                             * The user wants to ignore this descriptor item
                             * so remove it from the descriptor's collection
                             * of items.
                             */
                            it.remove();
                            if (isFiner) {
                                logger.finer("App config customization specified to ignore descriptor's " +
                                        descriptorItemName +
                                        getName(descriptorItem) +
                                        " so it will not be present for the application");
                            }
                        } else {
                            /*
                             * The user wants to override the setting of this
                             * descriptor item using the customized settings.
                             */
                            String oldValue = getValue(descriptorItem); // for logging purposes only
                            try {
                                setDescriptorItemValue(descriptorItem, customization);
                                if (isFiner) {
                                logger.finer("Overriding descriptor " +
                                        descriptorItemName + " " +
                                        getName(descriptorItem) + "=" +
                                        oldValue +
                                        " with " + toString(customization));
                                }
                            } catch (Exception e) {
                                logger.warning(toString(customization) + " " + e.getLocalizedMessage());
                            }
                        }
                        /*
                         * We have matched this customization with a descriptor
                         * item, so we can skip to the next customization.
                         */
                        break nextCustomization;
                    }
                }
                /*
                 * The customization matched no existing descriptor item, so
                 * add a new descriptor item.
                 */
                try {
                    T newItem = addDescriptorItem(customization);
                    if (isFiner) {
                        logger.finer("Creating new " + descriptorItemName +
                                getName(newItem) + "=" +
                                getValue(newItem));
                    }
                } catch (Exception e) {
                    logger.warning(toString(customization) + " " + e.getLocalizedMessage());
                }
            }
        }
    }

    /**
     * Concrete implementation of the context-parameter customizer.
     */
    private class ContextParamCustomizer extends Customizer<ContextParameter,ContextParam> {

        private ContextParamCustomizer(Set<ContextParameter> descriptorItems, List<ContextParam> customizations) {
            super(descriptorItems, customizations, "context-param"); // NOI18N
        }
        
        @Override
        protected boolean isIgnoreDescriptorItem(ContextParam customization) {
            return Boolean.parseBoolean(customization.getIgnoreDescriptorItem());
        }

        @Override
        protected void setDescriptorItemValue(ContextParameter descriptorItem, ContextParam customization) {
            descriptorItem.setValue(customization.getParamValue());
        }

        @Override
        protected ContextParameter newDescriptorItem(ContextParam customization) {
            ContextParameter newItem = 
                    new EnvironmentProperty(
                        customization.getParamName(), 
                        customization.getParamValue(), 
                        "" /* description */);
            return newItem;
        }

        @Override
        protected String getName(ContextParameter descriptorItem) {
            return descriptorItem.getName();
        }

        @Override
        protected String getCustomizationName(ContextParam customization) {
            return customization.getParamName();
        }

        @Override
        protected String getValue(ContextParameter descriptorItem) {
            return descriptorItem.getValue();
        }

        @Override
        protected String toString(ContextParam customization) {
            return "Context-param: name=" + customization.getParamName() + ", value=" + customization.getParamValue();
        }
            
    }

    /**
     * Concrete implementation for the EnvEntry customizer.
     */
    private class EnvEntryCustomizer extends Customizer<EnvironmentEntry,EnvEntry> {
        
        private EnvEntryCustomizer(Set<EnvironmentEntry> descriptorItems, List<EnvEntry> customizations) {
            super(descriptorItems, customizations, "env-entry"); // NOI18N
        }

        @Override
        protected boolean isIgnoreDescriptorItem(EnvEntry customization) {
            return Boolean.parseBoolean(customization.getIgnoreDescriptorItem());
        }

        @Override
        protected void setDescriptorItemValue(EnvironmentEntry descriptorItem, EnvEntry customization) {
            customization.validateValue();
            descriptorItem.setValue(customization.getEnvEntryValue());
            descriptorItem.setType(customization.getEnvEntryType());
        }

        @Override
        protected EnvironmentEntry newDescriptorItem(EnvEntry customization) {
            customization.validateValue();
            EnvironmentEntry newItem = 
                    new EnvironmentProperty(
                        customization.getEnvEntryName(), 
                        customization.getEnvEntryValue(), 
                        customization.getDescription(), 
                        customization.getEnvEntryType());
            /*
             * Invoke setValue which records that the value has been set.
             * Otherwise naming does not bind the name.
             */
            newItem.setValue(customization.getEnvEntryValue());
            return newItem;
        }

        @Override
        protected String getName(EnvironmentEntry descriptorItem) {
            return descriptorItem.getName();
        }

        @Override
        protected String getCustomizationName(EnvEntry customization) {
            return customization.getEnvEntryName();
        }

        @Override
        protected String getValue(EnvironmentEntry descriptorItem) {
            return descriptorItem.getValue();
        }

        @Override
        protected String toString(EnvEntry customization) {
            return "EnvEntry: name=" + customization.getEnvEntryName() +
                    ", type=" + customization.getEnvEntryType() +
                    ", value=" + customization.getEnvEntryValue() +
                    ", desc=" + customization.getDescription();
        }
    }
}
