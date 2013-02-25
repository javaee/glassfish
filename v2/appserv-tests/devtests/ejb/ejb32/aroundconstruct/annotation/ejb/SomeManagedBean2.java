package com.acme;

import javax.annotation.*;

import javax.ejb.EJB;
import javax.annotation.Resource;
import javax.interceptor.Interceptors;
import org.omg.CORBA.ORB;
import javax.persistence.PersistenceContext;
import javax.persistence.EntityManager;

@ManagedBean("someothermanagedbean")
public class SomeManagedBean2 extends BaseBean {

    @Resource ORB orb;

    @PostConstruct
    private void init() {
	System.out.println("In SomeManagedBean2::init() " + this);
    }
    

    public void foo() {
	System.out.println("In SomeManagedBean2::foo() ");
        if (orb == null) throw new RuntimeException("SomeManagedBean2: ORB is null");
    }

    @PreDestroy
    private void destroy() {
	System.out.println("In SomeManagedBean2::destroy() ");
    }
}
