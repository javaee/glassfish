package com.sun.s1asdev.ejb.ejb30.hello.session2;

import javax.annotation.PreDestroy;
import javax.interceptor.InvocationContext;

public class MyCallbackHandler {

    @PreDestroy 
    public void myPreDestMethod(InvocationContext invCtx) {
        System.out.println("In MyCallbackHandler::myPreDestMethod ");
        
        try {
            invCtx.proceed();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }


}
