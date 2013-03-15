package com.acme;

import javax.ejb.*;
import javax.annotation.*;
import javax.interceptor.*;


public class BaseBean {

    boolean ac = false;
    boolean pc = false;

    boolean ac1 = false;
    boolean pc1 = false;

    void verify(String name) {
        if (!ac) throw new RuntimeException("[" + name + "] InterceptorA.AroundConstruct was not called");
        if (!ac1) throw new RuntimeException("[" + name + "] InterceptorB.AroundConstruct was not called");

        if (!pc) throw new RuntimeException("[" + name + "] InterceptorA.PostConstruct was not called");
        if (!pc1) throw new RuntimeException("[" + name + "] InterceptorB.PostConstruct was not called");
    }
}
