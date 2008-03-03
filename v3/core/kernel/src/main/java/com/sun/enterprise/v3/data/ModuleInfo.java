package com.sun.enterprise.v3.data;

import com.sun.enterprise.v3.data.ContainerInfo;
import org.glassfish.api.deployment.ApplicationContainer;

/**
 * Information about a module in a container. There is a one to one mapping
 * from module to containers. Containers running a module are accessible
 * through the ApplicationContainer interface.
 *
 * @author Jerome Dochez
 */
public class ModuleInfo<T> {

    final private ContainerInfo ctrInfo;
    private ApplicationContainer appCtr;

    public ModuleInfo(ContainerInfo container, ApplicationContainer appCtr) {
        this.ctrInfo = container;
        this.appCtr = appCtr;
    }

    /**
     * Returns the container associated with this application
     *
     * @return the container for this application
     */
    public ContainerInfo getContainerInfo() {
        return ctrInfo;
    }

    /**
     * Set the contaier associated with this application
     * @param the container for this application
     */
    public void setApplicationContainer(ApplicationContainer appCtr) {
        this.appCtr = appCtr;
    }

    /**
     * Returns the contaier associated with this application
     * @return the container for this application
     */
    public ApplicationContainer getApplicationContainer() {
        return appCtr;
    }    
}
