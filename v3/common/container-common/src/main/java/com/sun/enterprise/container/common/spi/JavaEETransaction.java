package com.sun.enterprise.container.common.spi;

import org.jvnet.hk2.annotations.Contract;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.Transaction;

@Contract
public interface JavaEETransaction
    extends Transaction {

    public EntityManager getExtendedEntityManager(EntityManagerFactory factory);

    public EntityManager getTxEntityManager(EntityManagerFactory factory);

    public void addTxEntityManagerMapping(EntityManagerFactory factory, EntityManager em);

    public void addExtendedEntityManagerMapping(EntityManagerFactory factory, EntityManager em);

    public void removeExtendedEntityManagerMapping(EntityManagerFactory factory);

    public <T> void setContainerData(T data);

    public <T> T getContainerData();

}
