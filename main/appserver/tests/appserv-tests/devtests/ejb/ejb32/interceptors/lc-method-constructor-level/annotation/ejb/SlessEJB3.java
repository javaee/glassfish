package com.acme;

import javax.ejb.*;
import javax.interceptor.*;
import javax.annotation.*;

@Stateless
@Interceptors(InterceptorA.class)
public class SlessEJB3 extends BaseBean {

    @Interceptors(InterceptorC.class)
    public SlessEJB3() {}

    @Interceptors(InterceptorB.class)
    public String sayHello() {
        verifyAC_AC("SlessEJB");
        verifyB_PC("SlessEJB");
        return "SlessEJB3.hello";
    }

    @ExcludeClassInterceptors
    @Interceptors(InterceptorB.class)
    @PostConstruct
    private void init() {
        System.out.println("**SlessEJB PostConstruct");
        verifyMethod(null);
    }

}
