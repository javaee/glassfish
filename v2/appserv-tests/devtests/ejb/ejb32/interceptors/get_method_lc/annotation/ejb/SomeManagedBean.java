package com.acme;

import javax.annotation.*;

import javax.ejb.EJB;
import javax.annotation.Resource;
import javax.interceptor.Interceptors;
import org.omg.CORBA.ORB;
import javax.persistence.PersistenceContext;
import javax.persistence.EntityManager;

@ManagedBean("somemanagedbean")
public class SomeManagedBean extends SomeManagedBeanBase {

    @Interceptors(InterceptorA.class)
    public SomeManagedBean() {}

}
