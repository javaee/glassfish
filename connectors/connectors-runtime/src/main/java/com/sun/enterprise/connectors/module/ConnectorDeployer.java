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
import com.sun.enterprise.connectors.ConnectorRegistry;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.logging.LogDomains;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.api.deployment.UndeployCommandParameters;
import org.glassfish.api.deployment.OpsParams;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.event.Events;
import org.glassfish.javaee.core.deployment.JavaEEDeployer;
import org.glassfish.javaee.services.ResourceManager;
import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.glassfish.internal.api.ConnectorClassFinder;
import org.glassfish.internal.api.DelegatingClassLoader;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

import javax.validation.*;
import javax.validation.bootstrap.GenericBootstrap;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Collection;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.List;
import java.beans.PropertyVetoException;

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

    @Inject
    private Resources resources;

    @Inject
    private Domain domain;

    @Inject
    private Events events;

    private static Logger _logger = LogDomains.getLogger(ConnectorDeployer.class, LogDomains.RSR_LOGGER);


    public ConnectorDeployer() {
    }

    /**
     * Returns the meta data assocated with this Deployer
     *
     * @return the meta data for this Deployer
     */
    public MetaData getMetaData() {
        return new MetaData(false, null,
                new Class[]{Application.class});
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
        ConnectorDescriptor connDesc = 
            context.getModuleMetaData(ConnectorDescriptor.class);
        if (connDesc != null) {
            connDesc.setClassLoader(context.getClassLoader());
        }

        boolean isEmbedded = ConnectorsUtil.isEmbedded(context);
        ConnectorClassFinder ccf = null;
        ClassLoader classLoader = null;
        //this check is not needed as system-rars are never deployed, just to be safe.
        if (!ConnectorsUtil.belongsToSystemRA(moduleName)) {
            try {
                //for a connector deployer, classloader will always be ConnectorClassFinder
                classLoader =  context.getClassLoader();
                //for embedded .rar, compute the embedded .rar name
                if (isEmbedded) {
                    moduleName = ConnectorsUtil.getEmbeddedRarModuleName(
                            ConnectorsUtil.getApplicationName(context), moduleName);
                }

                //don't add the class-finder to the chain if its embedded .rar

                if (!(isEmbedded)) {
                    ccf = (ConnectorClassFinder) context.getClassLoader();
                    clh.getConnectorClassLoader(null).addDelegate(ccf);
                }

                registerBeanValidator(moduleName, context.getSource(), classLoader);

                ConnectorDescriptor cd = context.getModuleMetaData(ConnectorDescriptor.class);
                runtime.createActiveResourceAdapter(cd, moduleName, sourcePath, classLoader);

            } catch (Exception cre) {
                Object params[] = new Object[]{moduleName, cre};
                _logger.log(Level.WARNING, "unable.to.load.ra", params);

                //since resource-adapter creation has failed, remove the class-loader for the RAR
                if (!(isEmbedded) && ccf != null) {
                    clh.getConnectorClassLoader(null).removeDelegate(ccf);
                }
                //since resource-adapter creation has failed, unregister bean validator of the RAR
                unregisterBeanValidator(moduleName);
                return null;
            }
        }
        return new ConnectorApplication(moduleName, ConnectorsUtil.getApplicationName(context), resourceManager,
                classLoader, runtime, events);
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
            if (ConnectorsUtil.isEmbedded(context)) {
                String applicationName = ConnectorsUtil.getApplicationName(context);
                moduleName = ConnectorsUtil.getEmbeddedRarModuleName(applicationName, moduleName);
            }
            runtime.destroyActiveResourceAdapter(moduleName);
        } catch (ConnectorRuntimeException e) {
            Object params[] = new Object[]{moduleName, e};
            _logger.log(Level.WARNING, "unable.to.unload.ra", params);
        } finally {

            //remove it only if it is not embedded
            if (!ConnectorsUtil.isEmbedded(context)) {
                //remove the class-finder (class-loader) from connector-class-loader chain
                DelegatingClassLoader dcl = clh.getConnectorClassLoader(null);
                for(DelegatingClassLoader.ClassFinder cf : dcl.getDelegates()){
                    ConnectorClassFinder ccf = (ConnectorClassFinder)cf;
                    if(ccf.getResourceAdapterName().equals(moduleName)){
                        dcl.removeDelegate(ccf);
                        break;
                    }
                }
            }

            unregisterBeanValidator(moduleName);
        }
    }

    /**
     * Clean any files and artifacts that were created during the execution
     * of the prepare method.
     *
     * @param dc deployment context
     */
    public void clean(DeploymentContext dc) {
        UndeployCommandParameters dcp = dc.getCommandParameters(UndeployCommandParameters.class);
        if (dcp != null && dcp.origin == OpsParams.Origin.undeploy) {
            if (dcp.cascade != null && dcp.cascade) {
                deleteAllResources(dcp.name(), dcp.target);
            }
        }
    }

    /**
     * deletes all resources (pool, resource, admin-object-resource, ra-config, work-security-map) of a resource-adapter)
     *
     * @param moduleName   resource-adapter name
     * @param targetServer target instance name
     */
    private void deleteAllResources(String moduleName, String targetServer) {

        Collection<ConnectorConnectionPool> conPools = ConnectorsUtil.getAllPoolsOfModule(moduleName, resources);
        Collection<String> poolNames = ConnectorsUtil.getAllPoolNames(conPools);
        Collection<Resource> connectorResources = ConnectorsUtil.getAllResources(poolNames, resources);
        AdminObjectResource[] adminObjectResources = ConnectorsUtil.getEnabledAdminObjectResources(moduleName,
                resources, ConfigBeansUtilities.getServerNamed(targetServer));
        Collection<WorkSecurityMap> securityMaps = ConnectorsUtil.getAllWorkSecurityMaps(resources, moduleName);
        ResourceAdapterConfig rac = ConnectorsUtil.getRAConfig(moduleName, resources);


        deleteConnectorResources(connectorResources, targetServer, moduleName);
        deleteConnectionPools(conPools, moduleName);
        deleteAdminObjectResources(adminObjectResources, targetServer, moduleName);
        deleteWorkSecurityMaps(securityMaps, moduleName);
        if (rac != null) {
            deleteRAConfig(rac);
        }

    }

    private void deleteRAConfig(final ResourceAdapterConfig rac) {
        try {
            // delete resource-adapter-config
            if (ConfigSupport.apply(new SingleConfigCode<Resources>() {
                public Object run(Resources param) throws PropertyVetoException, TransactionFailure {
                    return param.getResources().remove(rac);
                }
            }, resources) == null) {
                _logger.log(Level.WARNING, "unable.to.delete.rac", rac.getResourceAdapterName());
            }

        } catch (TransactionFailure tfe) {
            Object params[] = new Object[]{rac.getResourceAdapterName(), tfe};
            _logger.log(Level.WARNING, "unable.to.delete.rac.exception", params);
        }
    }

    private void deleteWorkSecurityMaps(final Collection<WorkSecurityMap> workSecurityMaps, String raName) {
        try {
            // delete work-security-maps
            if (ConfigSupport.apply(new SingleConfigCode<Resources>() {

                public Object run(Resources param) throws PropertyVetoException,
                        TransactionFailure {
                    for (WorkSecurityMap resource : workSecurityMaps) {
                        param.getResources().remove(resource);
                    }
                    return true; // indicating that removal was successful
                }
            }, resources) == null) {
                _logger.log(Level.WARNING, "unable.to.delete.work.security.map", raName);
            }

        } catch (TransactionFailure tfe) {
            Object params[] = new Object[]{raName, tfe};
            _logger.log(Level.WARNING, "unable.to.delete.work.security.map.exception", params);
        }

    }

    private void deleteAdminObjectResources(final AdminObjectResource[] adminObjectResources, String target,
                                            String raName) {
        try {
            final Server targetServer = domain.getServerNamed(target);
            // delete admin-object-resource
            if (ConfigSupport.apply(new SingleConfigCode<Resources>() {
                public Object run(Resources param) throws PropertyVetoException, TransactionFailure {
                    for (AdminObjectResource resource : adminObjectResources) {
                        param.getResources().remove(resource);

                        // delete resource-ref
                        targetServer.deleteResourceRef(resource.getJndiName());
                    }
                    // not found
                    return true;
                }
            }, resources) == null) {
                _logger.log(Level.WARNING, "unable.to.delete.admin.object", raName);
            }
        } catch (TransactionFailure tfe) {
            Object params[] = new Object[]{raName, tfe};
            _logger.log(Level.WARNING, "unable.to.delete.admin.object.exception", params);
        }

    }

    private void deleteConnectorResources(final Collection<Resource> connectorResources, String target, String raName) {
        try {
            final Server targetServer = domain.getServerNamed(target);

            // delete connector-resource
            if (ConfigSupport.apply(new SingleConfigCode<Resources>() {
                public Object run(Resources param) throws PropertyVetoException, TransactionFailure {
                    for (Resource resource : connectorResources) {
                        param.getResources().remove(resource);

                        // delete resource-ref
                        targetServer.deleteResourceRef(((ConnectorResource) resource).getJndiName());
                    }
                    // not found
                    return true;
                }
            }, resources) == null) {
                _logger.log(Level.WARNING, "unable.to.delete.connector.resource", raName);
            }
        } catch (TransactionFailure tfe) {
            Object params[] = new Object[]{raName, tfe};
            _logger.log(Level.WARNING, "unable.to.delete.connector.resource.exception", params);
        }

    }

    private void deleteConnectionPools(final Collection<ConnectorConnectionPool> conPools, String raName) {
        // delete connector connection pool
        try {
            if (ConfigSupport.apply(new SingleConfigCode<Resources>() {
                public Object run(Resources param) throws PropertyVetoException, TransactionFailure {
                    for (ConnectorConnectionPool cp : conPools) {
                        return param.getResources().remove(cp);
                    }
                    // not found
                    return null;
                }
            }, resources) == null) {
                _logger.log(Level.WARNING, "unable.to.delete.connector.connection.pool", raName);
            }
        } catch (TransactionFailure tfe) {
            Object params[] = new Object[]{raName, tfe};
            _logger.log(Level.WARNING, "unable.to.delete.connector.connection.pool.exception", params);
        }
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

    private void registerBeanValidator(String rarName, ReadableArchive archive, ClassLoader classLoader) {

        ClassLoader contextCL = null;
        try {
            contextCL = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(classLoader);
            Validator beanValidator = null;
            ValidatorFactory validatorFactory = null;

            try {
                List<String> mappingsList = getValidationMappingDescriptors(archive);

                if (mappingsList.size() > 0) {
                    GenericBootstrap bootstrap = Validation.byDefaultProvider();
                    Configuration config = bootstrap.configure();

                    InputStream inputStream = null;
                    try {
                        for (String fileName : mappingsList) {
                            inputStream = archive.getEntry(fileName);
                            config.addMapping(inputStream);
                        }
                        validatorFactory = config.buildValidatorFactory();
                        ValidatorContext validatorContext = validatorFactory.usingContext();
                        beanValidator = validatorContext.getValidator();

                    } catch (IOException e) {
                        _logger.log(Level.FINE, "Exception while processing xml files for detecting " +
                                "bean-validation-mapping", e);
                    } finally {
                        try {
                            if (inputStream != null) {
                                inputStream.close();
                            }
                        } catch (Exception e) {
                            // ignore ?
                        }
                    }
                }

            } catch (Exception e) {
                Object params[] = new Object[]{rarName, e};
                _logger.log(Level.WARNING, "error.processing.xml.for.bean.validation.mapping", params);
            }
            if (beanValidator == null) {
                validatorFactory = Validation.byDefaultProvider().configure().buildValidatorFactory();
                beanValidator = validatorFactory.getValidator();
            }

            ConnectorRegistry registry = ConnectorRegistry.getInstance();
            registry.addBeanValidator(rarName, beanValidator);
        } finally {
            Thread.currentThread().setContextClassLoader(contextCL);
        }
    }

    private List<String> getValidationMappingDescriptors(ReadableArchive archive) {
        String validationMappingNSName = "jboss.org/xml/ns/javax/validation/mapping";

        Enumeration entries = archive.entries();
        List<String> mappingList = new ArrayList<String>();

        while (entries.hasMoreElements()) {

            String fileName = (String) entries.nextElement();
            if (fileName.toUpperCase().endsWith(".XML")) {
                BufferedReader reader = null;
                try {
                    InputStream is = archive.getEntry(fileName);
                    reader = new BufferedReader(new InputStreamReader(is));
                    String line;

                    while ((line = reader.readLine()) != null) {

                        if (line.contains(validationMappingNSName)) {
                            mappingList.add(fileName);
                            break;
                        }
                    }
                } catch (IOException e) {
                    _logger.log(Level.FINE, "Exception while processing xml file [ " + fileName + " ] " +
                            "for detecting bean-validation-mapping", e);
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (Exception e) {
							//ignore ?
                        }
                    }
                }
            }
        }
        return mappingList;
    }

    private void unregisterBeanValidator(String rarName){
        ConnectorRegistry registry = ConnectorRegistry.getInstance();
        registry.removeBeanValidator(rarName);
    }
}
