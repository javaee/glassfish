package com.sun.s1asdev.ejb.ejb30.hello.session.client;

import java.io.*;
import java.util.*;
import javax.ejb.EJB;
import com.sun.s1asdev.ejb.ejb30.hello.session.*;

import org.testng.Assert;
import org.testng.annotations.Test;

public class Client {

    public static void main(String[] args) {
        org.testng.TestNG testng = new org.testng.TestNG();
        testng.setTestClasses(
            new Class[] { com.sun.s1asdev.ejb.ejb30.hello.session.client.Client.class } );
        testng.run();
    }

    private static @EJB Sful sful;
    private static @EJB Sless sless;

    @Test
    public void doTest() {
        System.out.println("invoking stateful");
        sful.hello();

        System.out.println("invoking stateless");
        sless.hello();
        Assert.assertTrue(true, "Successfully invokes ejb30 stateful and " +
                          "stateless beans");
    }
}
