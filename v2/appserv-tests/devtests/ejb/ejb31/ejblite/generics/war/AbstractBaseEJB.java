package com.sun.s1as.devtests.ejb.generics;

import javax.ejb.*;
import java.util.*;

/**
 * All business methods in this class are overridden by subclass to verify
 * these business methods are correctly processed.
 */
public abstract class AbstractBaseEJB<T> {
    //abstract method, use parameterized param type with T
    public abstract void doSomething(List<T> t);

    //regular business method, no use of generics param
    public String hello() {
        System.out.println("In AbstractBaseEJB.hello.");
        return "Hello from AbstractBaseEJB.";
    }

    //use parameterized param type with T
    public void doSomething2(List<T> t) {
        System.out.println("In AbstractBaseEJB.doSomething2.");
    }

    //use parameterized return type with T
    public List<T> doSomething3() {
        System.out.println("In AbstractBaseEJB.doSomething3.");
        return null;
    }

    //use TypeVariable generics T as param
    abstract public void doSomething4(T t);

    //superclass has param List<T>, and subclass has param List
    abstract public void doSomething5(List<T> t);

    abstract public void doSomething6(List<List<T>> t);
} 
