package com.acme;

import javax.ejb.*;
import javax.interceptor.*;
import javax.annotation.*;

@Stateless
public class SlessEJB3 extends BaseBean {

    public SlessEJB3() {}

    public String sayHello() {
        verifyAC_AC("SlessEJB");
        verifyB_PC("SlessEJB");
        return "SlessEJB3.hello";
    }

    @PostConstruct
    private void init() {
        System.out.println("**SlessEJB PostConstruct");
        verifyMethod(null);
    }

}
