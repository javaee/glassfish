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
    final private ApplicationContainer appCtr;
    final private T descriptor;
    
    public ModuleInfo(ContainerInfo container, ApplicationContainer appCtr, T descriptor) {
        this.ctrInfo = container;
        this.appCtr = appCtr;
        this.descriptor = descriptor;
    }

   /**
    * Returns the descriptor associated with this application if any
    * @return the associated descriptor
    */
    public T getDescriptor() {
        return descriptor;
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
     * Returns the contaier associated with this application
     * @return the container for this application
     */
    public ApplicationContainer getApplicationContainer() {
        return appCtr;
    }    
}
