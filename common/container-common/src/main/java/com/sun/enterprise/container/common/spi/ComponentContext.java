package com.sun.enterprise.container.common.spi;

import org.jvnet.hk2.annotations.Contract;

@Contract
public interface ComponentContext {

    public JavaEEContainer getContainer();

    public Object getComponent();

    public Object getTransaction();

}
