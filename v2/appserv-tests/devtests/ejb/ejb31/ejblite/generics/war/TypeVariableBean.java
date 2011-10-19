package com.sun.s1as.devtests.ejb.generics;

import java.util.*;
import javax.ejb.*;

@Stateless
public class TypeVariableBean<T> {
    public String hello(String s) {
        return "Hello from TypeVariableBean.hello(String)";
    }

    public String hello(T t) {
        return "Hello from TypeVariableBean.hello(T)";
    }

//    public String hello(Object o) {  not allowed, conflict with hello(T)

    //should be able to override toString as final method
    @Override
    public final String toString() {
        return "TypeVariableBean<T>: " + super.toString();
    }

}
