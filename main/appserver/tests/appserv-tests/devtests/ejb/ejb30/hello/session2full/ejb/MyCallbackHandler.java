package com.sun.s1asdev.ejb.ejb30.hello.session2full;

import javax.interceptor.InvocationContext;

public class MyCallbackHandler {

    public void myPreDestroyMethod(InvocationContext invCtx) {
        System.out.println("In session2full.MyCallbackHandler::myPreDestroyMethod ");
        try {
            invCtx.proceed();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }


}
