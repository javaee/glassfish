package com.sun.enterprise.container.common.spi;

import org.jvnet.hk2.annotations.Contract;

import javax.transaction.TransactionManager;

@Contract
public interface JavaEETransactionManager
    extends TransactionManager {

    public JavaEETransaction begin(int timeout);
    
    public int getStatus();

    public boolean isNullTransaction();

    public void enlistComponentResources();

    public boolean isTimedOut();

    public JavaEETransaction getTransaction();
    
}
