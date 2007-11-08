package com.sun.s1asdev.ejb.ejb30.hello.session2;

import javax.annotation.PreDestroy;
import javax.interceptor.InvocationContext;
import javax.ejb.EJBException;

public class MyCallbackHandler {

    @PreDestroy 
    public void myPreDestMethod(InvocationContext invCtx) {
        System.out.println("In MyCallbackHandler::myPreDestMethod ");

        try {
            invCtx.getParameters();
            throw new EJBException("expected IllegalStateException");
        } catch(IllegalStateException ise) {
            System.out.println("Successfully caught IllegalStateException " +
                               "when calling invCtx.getParameters from " +
                               "callback handler");
        }

        try {
            invCtx.setParameters(new Object[] {});
            throw new EJBException("expected IllegalStateException");
        } catch(IllegalStateException ise) {
            System.out.println("Successfully caught IllegalStateException " +
                               "when calling invCtx.setParameters from " +
                               "callback handler");
        }

        
        try {
            invCtx.proceed();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }


}
