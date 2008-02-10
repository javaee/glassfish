package com.sun.enterprise.connectors.module;

import com.sun.appserv.connectors.spi.ConnectorRuntime;
import com.sun.appserv.connectors.spi.ConnectorRuntimeException;
import com.sun.enterprise.config.serverbeans.ConnectorConnectionPool;
import com.sun.enterprise.config.serverbeans.ConnectorResource;
import com.sun.enterprise.connectors.util.ConnectorsUtil;
import com.sun.enterprise.deployment.RootDeploymentDescriptor;
import com.sun.logging.LogDomains;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.javaee.core.deployment.JavaEEDeployer;
import org.glassfish.javaee.services.ResourceAdaptersBinder;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

//TODO V3 shouldn't the Deployer be Deployer<ConnectorContainer, ConnectorApplication> ??

//TODO V3 why should it be Deployer<CC,CA> ??
@Service
public class ConnectorDeployer extends JavaEEDeployer<ConnectorContainer, ConnectorApplication> implements PostConstruct {

    @Inject
    private ConnectorRuntime runtime;

    @Inject
    private ConnectorResource[] connectorResources;

    @Inject
    private ConnectorConnectionPool[] connectorConnectionPools;

    @Inject
    private ResourceAdaptersBinder binder;

    private long startTime;

    private static Logger _logger = LogDomains.getLogger(LogDomains.RSR_LOGGER);

    public ConnectorDeployer() {
        startTime = System.currentTimeMillis();
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
    public ConnectorApplication load(ConnectorContainer container, DeploymentContext context) {
        File sourceDir = context.getSourceDir();
        String sourcePath = sourceDir.getAbsolutePath();
        String moduleName = sourceDir.getName();
        //TODO V3 this check is not needed ?
        if (!ConnectorsUtil.belongsToSystemRA(moduleName)) {
            try {
                runtime.createActiveResourceAdapter(sourcePath, moduleName);
            } catch (ConnectorRuntimeException cre) {
                //TODO V3 log exception
                _logger.log(Level.WARNING, " unable to load the resource-adapter [ " + moduleName + " ]", cre);
            }
        }
        Map<ConnectorResource, ConnectorConnectionPool> raResourcePoolMap =
                new HashMap<ConnectorResource, ConnectorConnectionPool>();
        Map<String, ConnectorConnectionPool> raPools = getConnectorPoolsForRA(connectorConnectionPools, moduleName);
        for (ConnectorResource resource : connectorResources) {
            if (raPools.containsKey(resource.getPoolName())) {
                raResourcePoolMap.put(resource, raPools.get(resource.getPoolName()));
            }
        }
        return new ConnectorApplication(moduleName, raResourcePoolMap, binder, runtime);
    }

    private Map<String, ConnectorConnectionPool> getConnectorPoolsForRA(ConnectorConnectionPool[] ccp, String raName) {
        Map<String, ConnectorConnectionPool> raPools = new HashMap<String, ConnectorConnectionPool>();
        for (ConnectorConnectionPool pool : ccp) {
            if (pool.getResourceAdapterName().equalsIgnoreCase(raName)) {
                raPools.put(pool.getName(), pool);
            }
        }
        return raPools;
    }

    /**
     * Unload or stop a previously running application identified with the
     * ContractProvider instance. The container will be stop upon return from this
     * method.
     *
     * @param appContainer instance to be stopped
     * @param context      of the undeployment
     */
    //TODO V3 unload's job is to remove the app's content from class-loader or class-loader specific to this app
    public void unload(ConnectorApplication appContainer, DeploymentContext context) {
        File sourceDir = context.getSourceDir();
        String moduleName = sourceDir.getName();

        try {
            runtime.destroyActiveResourceAdapter(moduleName, true);
        } catch (ConnectorRuntimeException e) {
            //TODO V3 log exception
            _logger.log(Level.WARNING, " unable to unload the resource-adapter [ " + moduleName + " ]", e);
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

    protected RootDeploymentDescriptor getDefaultBundleDescriptor() {
        //TODO V3
        return null;
    }

    protected String getModuleType() {
        return "connectors";
    }

    /**
     * The component has been injected with any dependency and
     * will be placed into commission by the subsystem.
     */
    public void postConstruct() {
        logFine("Time taken to initialize connector deployer : " + (System.currentTimeMillis() - startTime));
    }

    public void logFine(String message) {
        _logger.log(Level.FINE, message);
    }
}
