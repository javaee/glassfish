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

    public void doSomething4(Integer t) {
        System.out.println("In doSomething4(Integer) of " + this);
    }

    @Override
    public void doSomething4(Object t) {
        System.out.println("In doSomething4(Object T) of " + this);
    }

    public void doSomething4(String t) {
        System.out.println("In doSomething4(String) of " + this);
    }

    @Override
    public String hello() {
        System.out.println("In hello of " + this);
        return "hello from " + this;
    }

    //superclass has param List<T>, and subclass has param List
    @Override
    public void doSomething5(List t) {
        System.out.println("In doSomething5 of " + this);
    }

    @Override
    public void doSomething6(List<List<Object>> t) {
        System.out.println("In doSomething6 of " + this);
    }
}
