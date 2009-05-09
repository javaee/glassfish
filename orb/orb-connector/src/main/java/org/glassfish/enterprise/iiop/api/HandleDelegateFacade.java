package org.glassfish.enterprise.iiop.api;

import org.jvnet.hk2.annotations.Contract;

import javax.ejb.spi.HandleDelegate;

@Contract
public interface HandleDelegateFacade {

    public HandleDelegate getHandleDelegate();

}
