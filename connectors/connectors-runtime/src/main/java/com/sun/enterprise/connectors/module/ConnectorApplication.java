package com.sun.enterprise.connectors.module;

import com.sun.appserv.connectors.spi.ConnectorConstants;
import com.sun.appserv.connectors.spi.ConnectorRuntime;
import com.sun.enterprise.config.serverbeans.ConnectorConnectionPool;
import com.sun.enterprise.config.serverbeans.ConnectorResource;
import com.sun.logging.LogDomains;
import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.javaee.services.ResourceAdaptersBinder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectorApplication implements ApplicationContainer {
    private static Logger _logger = LogDomains.getLogger(LogDomains.RSR_LOGGER);
    private String moduleName = "";
    private Map<ConnectorResource, ConnectorConnectionPool> raResourcePoolMap;
    ResourceAdaptersBinder resourceBinder;
    ConnectorRuntime runtime;

    public ConnectorApplication(String moduleName, Map<ConnectorResource, ConnectorConnectionPool> raResourcePoolMap,
                                ResourceAdaptersBinder resourceBinder, ConnectorRuntime runtime) {
        this.moduleName = moduleName;
        this.resourceBinder = resourceBinder;
        this.raResourcePoolMap = raResourcePoolMap;
        this.runtime = runtime;
    }

    /**
     * Returns the deployment descriptor associated with this application
     *
     * @return deployment descriptor if they exist or null if not
     */
    public Object getDescriptor() {
        //TODO V3 implement ?
        return null;
    }

    /**
     * Starts an application container.
     * ContractProvider starting should not throw an exception but rather should
     * use their prefered Logger instance to log any issue they encounter while
     * starting. Returning false from a start mean that the container failed
     * to start
     *
     * @return true if the container startup was successful.
     */
    public boolean start() {
        boolean started = false;

        deployResources(resourceBinder, raResourcePoolMap);

        started = true; // TODO V3 temporary
        logFine("Resource Adapter [ " + moduleName + " ] started");
        return started;
    }

    private void deployResources(ResourceAdaptersBinder binder,
                                 Map<ConnectorResource, ConnectorConnectionPool> raResourcePoolMap) {

        for (Map.Entry<ConnectorResource, ConnectorConnectionPool> entry : raResourcePoolMap.entrySet()) {
            ConnectorResource resource = entry.getKey();
            ConnectorConnectionPool pool = entry.getValue();
            try {
                binder.bindResource(resource, pool, resource.getJndiName(), ConnectorConstants.RES_TYPE_CR);
            } catch (Exception e) {
                //TODO V3 log
                e.printStackTrace();
            }
        }
    }

    /**
     * Stop the application container
     *
     * @return true if stopping was successful.
     */
    public boolean stop() {
        boolean stopped = false;
        undeployResources(raResourcePoolMap, moduleName);
        //TODO V3 temporary
        stopped = true;
        logFine("Resource Adapter [ " + moduleName + " ] stopped");
        return stopped;
    }

    private void undeployResources(Map<ConnectorResource, ConnectorConnectionPool> raResourcePoolMap, String moduleName) {
        List<String> resources = new ArrayList<String>();
        List<String> pools = new ArrayList<String>();

        for (Map.Entry<ConnectorResource, ConnectorConnectionPool> entry : raResourcePoolMap.entrySet()) {
            ConnectorResource resource = entry.getKey();
            ConnectorConnectionPool pool = entry.getValue();
            resources.add(resource.getJndiName());
            pools.add(pool.getName());

            runtime.destroyResourcesAndPools(pools, resources);
        }
    }

    /**
     * Returns the class loader associated with this application
     *
     * @return ClassLoader for this app
     */
    public ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader(); //TODO V3 is this right behavior ?
    }

    public void logFine(String message) {
        _logger.log(Level.FINE, message);
    }
}
