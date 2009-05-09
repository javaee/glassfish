package org.glassfish.enterprise.iiop.impl;

import org.glassfish.enterprise.iiop.api.HandleDelegateFacade;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import javax.ejb.spi.HandleDelegate;

import java.util.Properties;


@Service
public class HandleDelegateFacadeImpl implements HandleDelegateFacade {

    public HandleDelegate getHandleDelegate() {
        return IIOPHandleDelegate.getHandleDelegate();
    }
}