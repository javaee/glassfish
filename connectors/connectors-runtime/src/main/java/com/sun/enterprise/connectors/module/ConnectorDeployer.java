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

package com.sun.enterprise.connectors.module;

import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.Application;
import com.sun.logging.LogDomains;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.javaee.core.deployment.JavaEEDeployer;
import org.glassfish.javaee.services.ResourceManager;
import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.glassfish.internal.api.ConnectorClassFinder;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Deployer for a resource-adapter.
 *
 * @author Jagadish Ramu
 */
@Service
public class ConnectorDeployer extends JavaEEDeployer<ConnectorContainer, ConnectorApplication>
        implements PostConstruct {

    @Inject
    private ConnectorRuntime runtime;

    @Inject
    private ClassLoaderHierarchy clh;

    @Inject
    private ResourceManager resourceManager;

    private static Logger _logger = LogDomains.getLogger(ConnectorDeployer.class, LogDomains.RSR_LOGGER);

    private ConnectorClassFinder ccf = null;

    public ConnectorDeployer() {
    }

    /**
     * Returns the meta data assocated with this Deployer
     *
     * @return the meta data for this Deployer
     */
    public MetaData getMetaData() {
        return new MetaData(false, null,
                new Class[] { Application.class });
    }

    /**
     * Loads the meta date associated with the application.
     *
     * @param type type of metadata that this deployer has declared providing.
     */
    public <T> T loadMetaData(Class<T> type, DeploymentContext context) {
        return null;
    }

    /**
     * Loads a previously prepared application in its execution environment and
     * return a ContractProvider instance that will identify this environment in
     * future communications with the application's container runtime.
     *
     * @param container in which the application will reside
     * @param context   of the deployment
     * @return an ApplicationContainer instance identifying the running application
     */
    @Override
    public ConnectorApplication load(ConnectorContainer container, DeploymentContext context) {
        super.load(container, context);
        File sourceDir = context.getSourceDir();
        String sourcePath = sourceDir.getAbsolutePath();
        String moduleName = sourceDir.getName();

        boolean isEmbedded = isEmbedded(context);
        ClassLoader classLoader = null;
        //this check is not needed as system-rars are never deployed, just to be safe.
        if (!ConnectorsUtil.belongsToSystemRA(moduleName)) {
            try {
                //for a connector deployer, classloader will always be ConnectorClassFinder
                ccf = (ConnectorClassFinder) context.getClassLoader();
                classLoader = ccf;
                //for embedded .rar, compute the embedded .rar name
                if(isEmbedded){
                    moduleName = getEmbeddedRarModuleName(getApplicationName(context), moduleName);
                }

                //don't add the class-finder to the chain if its embedded .rar

                if(!(isEmbedded)){
                    classLoader =  clh.getConnectorClassLoader(null);
					clh.getConnectorClassLoader(null).addDelegate(ccf);
                }

                ConnectorDescriptor cd = context.getModuleMetaData(ConnectorDescriptor.class);
                runtime.createActiveResourceAdapter(cd, moduleName, sourcePath, classLoader);
                //runtime.createActiveResourceAdapter(sourcePath, moduleName, ccf);

            } catch (ConnectorRuntimeException cre) {
                _logger.log(Level.WARNING, " unable to load the resource-adapter [ " + moduleName + " ]", cre);
                if(!(isEmbedded) && ccf != null) {
                    clh.getConnectorClassLoader(null).removeDelegate(ccf);
                }

            }
        }
        return new ConnectorApplication(moduleName, getApplicationName(context), resourceManager, classLoader, runtime);
    }

    private String getEmbeddedRarModuleName(String applicationName, String moduleName) {
        String embeddedRarName = moduleName.substring(0,
                moduleName.indexOf(ConnectorConstants.EXPLODED_EMBEDDED_RAR_EXTENSION));

        moduleName = applicationName + ConnectorConstants.EMBEDDEDRAR_NAME_DELIMITER + embeddedRarName;
        return moduleName;
    }

    private boolean isEmbedded(DeploymentContext context) {
        ReadableArchive archive = context.getSource();
        return (archive != null && archive.getParentArchive() != null);
    }

    private String getApplicationName(DeploymentContext context){
        String applicationName = null;
        ReadableArchive parentArchive = context.getSource().getParentArchive();
        if(parentArchive != null){
            applicationName = parentArchive.getName();
        }
        return applicationName;
    }


    /**
     * Unload or stop a previously running application identified with the
     * ContractProvider instance. The container will be stop upon return from this
     * method.
     *
     * @param appContainer instance to be stopped
     * @param context      of the undeployment
     */
    public void unload(ConnectorApplication appContainer, DeploymentContext context) {
        File sourceDir = context.getSourceDir();
        String moduleName = sourceDir.getName();

        try {
            if(isEmbedded(context)){
                String applicationName = getApplicationName(context);
                moduleName = getEmbeddedRarModuleName(applicationName, moduleName);
            }
            runtime.destroyActiveResourceAdapter(moduleName, true);
        } catch (ConnectorRuntimeException e) {
            _logger.log(Level.WARNING, " unable to unload the resource-adapter [ " + moduleName + " ]", e);
        } finally {

            //remove it only if it is not embedded
            if(!isEmbedded(context)){
                //remove the class-finder (class-loader) from connector-class-loader chain
                clh.getConnectorClassLoader(null).removeDelegate(ccf);
            }
        }
    }

    /**
     * Clean any files and artifacts that were created during the execution
     * of the prepare method.
     *
     * @param context deployment context
     */
    public void clean(DeploymentContext context) {
    }


    protected String getModuleType() {
        return ConnectorConstants.CONNECTOR_MODULE;
    }

    /**
     * The component has been injected with any dependency and
     * will be placed into commission by the subsystem.
     */
    public void postConstruct() {
    }

    public void logFine(String message) {
        _logger.log(Level.FINE, message);
    }
}
