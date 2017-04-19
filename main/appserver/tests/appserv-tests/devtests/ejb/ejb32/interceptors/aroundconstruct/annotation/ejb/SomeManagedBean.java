package com.acme;

import javax.annotation.*;

import javax.ejb.EJB;
import javax.annotation.Resource;
import javax.interceptor.Interceptors;
import org.omg.CORBA.ORB;
import javax.persistence.PersistenceContext;
import javax.persistence.EntityManager;

@ManagedBean("somemanagedbean")
@Interceptors(InterceptorA.class)
public class SomeManagedBean extends BaseBean {

    @Resource ORB orb;

    @PostConstruct
    private void init() {
	System.out.println("In SomeManagedBean::init() " + this);
    }
    

    public void foo() {
	System.out.println("In SomeManagedBean::foo() ");
	verifyA("SomeManagedBean");
        if (orb == null) throw new RuntimeException("SomeManagedBean: ORB is null");
    }

    @PreDestroy
    private void destroy() {
	System.out.println("In SomeManagedBean::destroy() ");
    }
}
