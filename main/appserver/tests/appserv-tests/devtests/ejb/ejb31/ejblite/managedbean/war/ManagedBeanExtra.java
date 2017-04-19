package com.acme;

import javax.annotation.*;
import javax.interceptor.Interceptors;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

@ManagedBean
public class ManagedBeanExtra {

    public void hello() { System.out.println("In ManagedBeanExtra::hello()"); }

}