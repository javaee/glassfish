package com.sun.s1as.devtests.ejb.generics;

import java.util.*;
import javax.ejb.*;

@Stateless
public class TestBean extends AbstractBaseEJB<Object> {
    @Override
    public void doSomething(List<Object> t) {
        System.out.println("In doSomething of " + this);
    }

    @Override
    public void doSomething2(List<Object> t) {
        System.out.println("In doSomething2 of " + this);
    }

    @Override
    public List<Object> doSomething3() {
        System.out.println("In doSomething3 of " + this);
        return null;
    }

    @Override
    public void doSomething4(Object t) {
        System.out.println("In doSomething4 of " + this);
    }

    @Override
    public String hello() {
        System.out.println("In hello of " + this);
        return "hello from " + this;
    }

}
