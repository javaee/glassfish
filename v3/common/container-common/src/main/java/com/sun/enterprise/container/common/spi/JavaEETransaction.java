package com.sun.enterprise.container.common.spi;

import org.jvnet.hk2.annotations.Contract;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

@Contract
public interface JavaEETransaction {

    public EntityManager getExtendedEntityManager(EntityManagerFactory factory);

    public EntityManager getTxEntityManager(EntityManagerFactory factory);

    public void addTxEntityManagerMapping(EntityManagerFactory factory, EntityManager em);

}
