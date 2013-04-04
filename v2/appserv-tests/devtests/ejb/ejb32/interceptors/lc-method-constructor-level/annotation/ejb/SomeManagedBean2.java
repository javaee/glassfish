package com.acme;

import javax.annotation.*;

import javax.ejb.EJB;
import javax.annotation.Resource;
import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.Interceptors;
import org.omg.CORBA.ORB;
import javax.persistence.PersistenceContext;
import javax.persistence.EntityManager;

@Interceptors(InterceptorC.class)
@ManagedBean("somemanagedbean2")
public class SomeManagedBean2 extends BaseBean {

    @Interceptors(InterceptorA.class)
    public SomeManagedBean2() {}

    @ExcludeClassInterceptors
    @Interceptors(InterceptorA.class)
    @PostConstruct
    private void init2() {
	System.out.println("In SomeManagedBean2::init2() " + this);
        verifyMethod("init2");
    }
    

    @Interceptors(InterceptorA.class)
    public void foo2() {
	System.out.println("In SomeManagedBean2::foo2() ");
	verifyAC_AC("SomeManagedBean2");
	verifyA_PC("SomeManagedBean2");
    }

    @PreDestroy
    private void destroy() {
	System.out.println("In SomeManagedBean2::destroy() ");
        verifyMethod("destroy");
    }
}
