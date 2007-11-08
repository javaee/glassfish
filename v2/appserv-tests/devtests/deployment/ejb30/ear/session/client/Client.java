package com.sun.s1asdev.ejb.ejb30.hello.session.client;

import java.io.*;
import java.util.*;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import com.sun.s1asdev.ejb.ejb30.hello.session.*;

public class Client {
    private static boolean afterInj = false;

    public static void main (String[] args) {

        System.out.println("ejb-ejb30-hello-session");
        Client client = new Client(args);
        client.doTest();
    }  
    
    public Client (String[] args) {
    }
    
    private static @EJB Sful sful;
    private static @EJB Sless sless;

    public void doTest() {

        try {

            System.out.println("invoking stateful");
            sful.hello();

            System.out.println("invoking stateless");
            sless.hello();

            if (!afterInj) {
                System.exit(-1);
            } else {
                System.out.println("after injection check");
            }

            System.out.println("test complete");

        } catch(Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        
    	return;
    }

    @PostConstruct
    private static void afterInjection() {
        afterInj = true;
    }

}

