package com.sun.s1asdev.ejb.ejb32.mdb.ra;

import javax.resource.spi.ActivationSpec;
import javax.resource.spi.InvalidPropertyException;
import javax.resource.spi.ResourceAdapter;

/**
 * @author David Blevins
 */
public class CommandActivationSpec implements ActivationSpec {

    private ResourceAdapter resourceAdapter;

    public void validate() throws InvalidPropertyException {
    }

    public ResourceAdapter getResourceAdapter() {
        return resourceAdapter;
    }

    public void setResourceAdapter(ResourceAdapter resourceAdapter) {
        this.resourceAdapter = resourceAdapter;
    }
}
