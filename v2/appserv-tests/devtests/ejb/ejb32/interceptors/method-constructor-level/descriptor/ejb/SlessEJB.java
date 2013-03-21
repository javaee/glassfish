package com.acme;

import javax.ejb.*;
import javax.interceptor.*;
import javax.annotation.*;

@Stateless
public class SlessEJB extends BaseBean {

    @EJB SlessEJB2 s2;
    @EJB SlessEJB3 s3;

    public SlessEJB() {}

    public String sayHello() {
        verifyB_AC("SlessEJB");
        verifyA_PC("SlessEJB");
        return (s2.sayHello() + s3.sayHello());
    }

    @PostConstruct
    private void init() {
        System.out.println("**SlessEJB PostConstruct");
        verifyMethod("init");
    }

}
