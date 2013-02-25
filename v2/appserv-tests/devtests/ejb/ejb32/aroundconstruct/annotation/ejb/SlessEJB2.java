package com.acme;

import javax.ejb.Stateless;
import javax.interceptor.*;
import javax.annotation.*;

@Stateless
public class SlessEJB2 extends BaseBean {

    @Resource private SomeManagedBean mb;
    @Resource private SomeManagedBean2 mb2;
    @Resource private SomeManagedBean3 mb3;

    public String sayHello() {
        try {
            verify("SlessEJB2");
            throw new RuntimeException("SlessEJB2 was intercepted");
        } catch (Exception e) {
            // ok
        }
        mb.foo();
        mb2.foo();
        mb3.foo();
        return "Hello";
    }

}
