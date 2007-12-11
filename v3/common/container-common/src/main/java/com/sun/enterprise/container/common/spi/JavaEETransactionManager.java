package com.sun.enterprise.container.common.spi;

import org.jvnet.hk2.annotations.Contract;

@Contract
public interface JavaEETransactionManager {

    public JavaEETransaction getTransaction();
    
}
