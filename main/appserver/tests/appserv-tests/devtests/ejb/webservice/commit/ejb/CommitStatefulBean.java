package com.sun.s1asdev.ejb.webservice.commit;

import javax.ejb.*;
import javax.annotation.Resource;

@Stateful
public class CommitStatefulBean 
    implements CommitStatefulLocal, SessionSynchronization {


    @Resource SessionContext sessionCtx;

    public void foo() {
        System.out.println("In CommitStatefulBean::foo");
    }

     public void afterBegin() {
        System.out.println("In CommitStatefulBean::afterBegin()");
    }

    public void beforeCompletion() {
        System.out.println("In CommitStatefulBean::beforeCompletion() " +
                           "marking tx for rollback");
        sessionCtx.setRollbackOnly();
    }

    public void afterCompletion(boolean committed) {
        System.out.println("In CommitStatefulBean::afterCompletion()");
    }


}
