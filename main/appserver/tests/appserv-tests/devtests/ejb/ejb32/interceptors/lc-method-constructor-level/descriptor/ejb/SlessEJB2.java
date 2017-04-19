package com.acme;

import javax.ejb.Stateless;
import javax.interceptor.*;
import javax.annotation.*;

@Stateless
public class SlessEJB2 extends BaseBean {

    public String sayHello() {
        try {
            verify("SlessEJB2");
            throw new RuntimeException("SlessEJB2 was intercepted");
        } catch (Exception e) {
            // ok
        }
        return "Hello";
    }

}
