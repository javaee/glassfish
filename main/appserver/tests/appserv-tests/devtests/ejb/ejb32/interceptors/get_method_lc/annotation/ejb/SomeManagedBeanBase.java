package com.acme;

import javax.annotation.*;

import javax.ejb.EJB;
import javax.annotation.Resource;
import javax.interceptor.Interceptors;
import org.omg.CORBA.ORB;
import javax.persistence.PersistenceContext;
import javax.persistence.EntityManager;

public class SomeManagedBeanBase extends BaseBean {

    @Resource ORB orb;

    @PostConstruct
    private void init() {
	System.out.println("In SomeManagedBean::init() " + this);
        verifyMethod(null);
    }
    

    @Interceptors(InterceptorA.class)
    public void foo() {
	System.out.println("In SomeManagedBean::foo() ");
	verifyA_AC("SomeManagedBean");
	verifyA_AI("SomeManagedBean");
        ai = false;
        if (orb == null) throw new RuntimeException("SomeManagedBean: ORB is null");
    }

    @PreDestroy
    private void destroy() {
	System.out.println("In SomeManagedBean::destroy() ");
        verifyMethod(null);
    }
}
