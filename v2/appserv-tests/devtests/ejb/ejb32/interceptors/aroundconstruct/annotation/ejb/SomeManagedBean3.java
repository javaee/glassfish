package com.acme;

import javax.annotation.*;

import javax.ejb.EJB;
import javax.annotation.Resource;
import javax.interceptor.Interceptors;
import org.omg.CORBA.ORB;
import javax.persistence.PersistenceContext;
import javax.persistence.EntityManager;

@ManagedBean("onemoremanagedbean")
public class SomeManagedBean3 extends BaseBean {

    @Resource ORB orb;

    public void foo() {
	System.out.println("In SomeManagedBean3::foo() ");
        if (orb == null) throw new RuntimeException("SomeManagedBean3: ORB is null");
    }

}
