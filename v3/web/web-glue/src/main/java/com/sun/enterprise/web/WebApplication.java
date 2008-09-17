/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.web.EnvironmentEntry;
import com.sun.enterprise.deployment.web.ContextParameter;
import com.sun.enterprise.util.Result;
import com.sun.logging.LogDomains;
import java.util.Collection;
import java.util.HashSet;
import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.StartupContext;
import org.glassfish.deployment.common.DeploymentProperties;
import org.glassfish.web.loader.WebappClassLoader;
import org.glassfish.web.plugin.common.WebAppConfig;
import org.glassfish.web.plugin.common.EnvEntry;
import org.glassfish.web.plugin.common.ContextParam;

import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jvnet.hk2.config.ConfigSupport;

public class WebApplication implements ApplicationContainer<WebBundleDescriptor> {

    private static final String ADMIN_VS = "__asadmin";
    final Logger logger = LogDomains.getLogger(WebApplication.class, LogDomains.WEB_LOGGER);

    private final WebContainer container;
    private final WebModuleConfig wmInfo;
    private final DeploymentContext deploymentContext;
 
    public WebApplication(WebContainer container, WebModuleConfig config,
                          DeploymentContext dc) {
        this.container = container;
        this.wmInfo = config;
        this.deploymentContext = dc;
    }


    public boolean start(StartupContext startupContext) throws Exception {
        wmInfo.setAppClassLoader(startupContext.getClassLoader());
        applyApplicationConfig(startupContext);
        return start();
    }


    private boolean start() throws Exception {
        // TODO : dochez : add action report here...
        List<Result<WebModule>> results = container.loadWebModule(
            wmInfo, "null", deploymentContext);
        if (results == null) {
            logger.log(Level.SEVERE,
                "Unknown error, loadWebModule returned null, file a bug");
            return false;
        }

        boolean isFailure = false;
        StringBuilder sb = null;
        for (Result result : results) {
            if (result.isFailure()) {
                if (sb == null) {
                    sb = new StringBuilder(result.exception().toString());
                } else {
                    sb.append(result.exception().toString());
                }
                logger.log(Level.WARNING, result.exception().toString(),
                           result.exception());
                isFailure = true;
            }
        }
        if (isFailure) {
            throw new Exception(sb.toString());
        }
     
        logger.info("Loading application " + wmInfo.getDescriptor().getName() +
                    " at " + wmInfo.getDescriptor().getContextRoot());

        return true;
    }


    public boolean stop() {

        container.unloadWebModule(getDescriptor().getContextRoot(), null,
                                  null, deploymentContext);

        if (getClassLoader() instanceof WebappClassLoader) {
            try {
                ((WebappClassLoader) getClassLoader()).stop();
            } catch (Exception e) {
                logger.log(Level.WARNING,
                           "Unable to stop classloader for " + this, e);
            }
        }

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
        return start();
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
     * Returns the deployment descriptor associated with this application
     *
     * @return deployment descriptor if they exist or null if not
     */
    public WebBundleDescriptor getDescriptor() {
        return wmInfo.getDescriptor();
    }

    /**
     * Applies application config customization (stored temporarily in the
     * start-up context's start-up parameters) to the web app's descriptor.
     * @param startupContext
     */
    private void applyApplicationConfig(StartupContext startupContext) {
        Properties startupParams = startupContext.getStartupParameters();
        /*
         * Fetch the WebAppConfig object, if any was stored in the startup parameters
         * so we could retrieve it here.
         */
        Object config = startupParams.get(
            DeploymentProperties.APP_CONFIG + ".web");
        if (config != null) {
            if ( ! (config instanceof WebAppConfig)) {
                logger.warning("Expected WebAppConfig instance in startup context but found " + config.getClass().getName() + "; ignoring and continuing");
            }
            
            WebAppConfig c = (WebAppConfig) config;

            WebBundleDescriptor descriptor = wmInfo.getDescriptor();

            applyEnvEntryCustomizations(descriptor, c.getEnvEntry());
            applyContextParamCustomizations(descriptor, c.getContextParam());
        }
    }

    /**
     * Applies customizations of context-parameters to the context-parameters
     * specified in the descriptor.
     * <p>
     * Any customization to a context-param name
     * that is also present in the descriptor overrides the value from the descriptor.
     * Any customization that specifies a context-param name that does not appear
     * in the descriptor is added to the runtime data structure.  Any
     * context-param that is present in the descriptor but not in the
     * customizations is removed from the run-time data structure.
     *
     * @param descriptor web app descriptor possibly containing context-param settings
     * @param contextParams customizations of context-params
     */
    private void applyContextParamCustomizations(WebBundleDescriptor descriptor, Collection<ContextParam> contextParams ) {
        boolean isFiner = logger.isLoggable(Level.FINER);

        Set<ContextParameter> uncustomizedContextParameters =
                new HashSet<ContextParameter>(descriptor.getContextParametersSet());
        
        Set<ContextParam> unappliedCustomizationContextParams =
                new HashSet<ContextParam>(contextParams);

        for (ContextParam cParam: contextParams) {
            for (ContextParameter contextParam :
                descriptor.getContextParametersSet()) {
                if (contextParam.getName().equals(cParam.getParamName())) {
                    contextParam.setValue(cParam.getParamValue());
                    if (isFiner) {
                        logger.finer("Overriding descriptor context param " + contextParam.getName() + "=" +
                                contextParam.getValue() + " with customized value " + cParam.getParamValue());
                    }
                    uncustomizedContextParameters.remove(contextParam);
                    unappliedCustomizationContextParams.remove(cParam);
                    break;
                }
            }
        }

        for (ContextParameter cp : uncustomizedContextParameters) {
            if (isFiner) {
                logger.finer("App config customization is present and does not include contextParameter " +
                        cp.getName() + " so this contextParam is being removed from the app's configuration");
            }
            descriptor.removeContextParameter(cp);
        }

        for (ContextParam addedCP : unappliedCustomizationContextParams) {
            if (isFiner) {
                logger.finer("App config customization included context parameter " +
                        addedCP.getParamName() + " not present in the descriptor so it has been added to the app's configuration");
            }
            ContextParameter newCP = new EnvironmentProperty(
                    addedCP.getParamName(),
                    addedCP.getParamValue(),
                    addedCP.getDescription());
            descriptor.addContextParameter(newCP);
        }
    }

    /**
    /**
     * Applies customizations of env-entries to the env-entries
     * specified in the descriptor.
     * <p>
     * Any customization to an env-entry
     * that is also present in the descriptor overrides the value from the descriptor.
     * Any customization that specifies an env-entry name that does not appear
     * in the descriptor is added to the runtime data structure.  Any
     * env-entry that is present in the descriptor but not in the
     * customizations is removed from the run-time data structure.
     * @param descriptor deployment descriptor for the web app
     * @param envEntries customized env entry settings
     */
    private void applyEnvEntryCustomizations(WebBundleDescriptor descriptor, Collection<EnvEntry> envEntries) {
        Set<EnvironmentEntry> uncustomizedDescriptorEnvironmentEntries =
                new HashSet<EnvironmentEntry>(descriptor.getEnvironmentEntrySet());

        Set<EnvEntry> unappliedCustomizationEnvEntries =
                new HashSet<EnvEntry>(envEntries);

        for (EnvEntry env : envEntries) {
            try {
                /*
                 * Make sure the env-entry value can be parsed given the
                 * data type.
                 */
                env.validateValue();

                for (EnvironmentEntry envEntry :
                  descriptor.getEnvironmentEntrySet()) {
                    if (envEntry.getName().equals(env.getEnvEntryName())) {
                        envEntry.setValue(env.getEnvEntryValue());
                        uncustomizedDescriptorEnvironmentEntries.remove(envEntry);
                        unappliedCustomizationEnvEntries.remove(env);
                        break;
                    }
                 }
            } catch (Exception ex) {
                logger.warning("Validation failed for customized application config <env-entry> named " +
                        env.getEnvEntryName() + ": " + ex.getLocalizedMessage());
            }
        }

        /*
         * If there are descriptor env entries that did not appear in the
         * customized set then the user removed them during customization so
         * we should remove them from the in-memory descriptor.
         */
        for (EnvironmentEntry e : uncustomizedDescriptorEnvironmentEntries) {
            descriptor.getEnvironmentEntrySet().remove(e);
        }

        /*
         * If there are any customized env entries that did not appear in
         * the original descriptor, then the user has added them during
         * customization so we add them to the in-memory descriptor.
         */
        for (EnvEntry e : unappliedCustomizationEnvEntries) {
            EnvironmentEntry newE = new EnvironmentProperty(
                    e.getEnvEntryName(),
                    e.getEnvEntryValue(),
                    e.getDescription(),
                    e.getEnvEntryType());
            descriptor.addEnvironmentEntry(newE);
        }
    }
}
