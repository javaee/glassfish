package com.sun.enterprise.config.serverbeans;

import org.glassfish.config.support.Create;
import org.glassfish.config.support.Delete;
import org.glassfish.config.support.TargetAndNameBasedResolver;
import org.glassfish.config.support.TargetBasedResolver;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;

import java.util.List;

/**
 * An application or Resource reference container object.
 *
 * @author Jerome Dochez
 */
@Configured
public interface RefContainer extends ConfigBeanProxy {

    /**
     * List of all the resources that this instance is referencing.
     *
     * @return the list of resources references
     */
    @Element
    List<ResourceRef> getResourceRef();

    /**
     * List of all the applications that this instance is referencing.
     *
     * @return the list of applications references.
     */
    @Element
    @Create(value="create-application-ref", resolver= TargetBasedResolver.class)
    @Delete(value="delete-application-ref", resolver = TargetAndNameBasedResolver.class)            
    List<ApplicationRef> getApplicationRef();
}
