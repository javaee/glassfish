package com.acme;

import javax.ejb.*;
import javax.interceptor.*;
import javax.annotation.*;

@Stateless
@Interceptors({InterceptorA.class,InterceptorB.class})
public class SlessEJB extends BaseBean implements Sless {

    @EJB SlessEJB2 s2;

    public String sayHello() {
        verify("SlessEJB");
        return s2.sayHello();
    }

    @PostConstruct
    private void init() {
        System.out.println("**SlessEJB PostConstruct");
    }

}
