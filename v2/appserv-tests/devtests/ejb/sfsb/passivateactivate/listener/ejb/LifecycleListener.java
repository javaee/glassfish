package com.sun.s1asdev.ejb.ejb30.sfsb.lifecycle.ejb;

import javax.ejb.PostActivate;

public class LifecycleListener {

    public LifecycleListener() {
    }

    @PostActivate
    public void postActivate(Object target) {
        ((SFSBEJB) target).postActivate();
    }

}
