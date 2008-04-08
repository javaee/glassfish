package com.sun.enterprise.container.common.spi;

import org.jvnet.hk2.annotations.Contract;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

@Contract
public interface JavaEEContainer {

    public ClassLoader getContainerClassLoader();

    public String getComponentId();

    public <D> D getDescriptor();

    public EntityManager lookupExtendedEntityManager(EntityManagerFactory emf);
    
}
