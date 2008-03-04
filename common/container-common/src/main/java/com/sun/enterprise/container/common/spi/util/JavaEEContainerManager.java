package com.sun.enterprise.container.common.spi.util;

import com.sun.enterprise.container.common.spi.JavaEEContainer;

import org.jvnet.hk2.annotations.Contract;

@Contract
public interface JavaEEContainerManager {

    public void registerContainer(String componentId, JavaEEContainer container);

    public void unregisterContainer(String componentId);

    public JavaEEContainer getContainerFor(String componentId);

}
