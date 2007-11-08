package com.sun.s1asdev.deployment.ejb30.ear.xmloverride.client;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import com.sun.s1asdev.deployment.ejb30.ear.xmloverride.*;

public class Client {
    public static void main (String[] args) {
        System.out.println("deployment-ejb30-ear-xmloverride");
        Client client = new Client();
        client.doTest();
    }  
    
    private static @EJB Sless sless;
    private static @EJB Sful sful;

    public void doTest() {

        try {

            System.out.println("invoking stateless");
            try {
                System.out.println(sless.hello());
                System.exit(-1);
            } catch(Exception ex) {
                System.out.println("Expected failure from sless.hello()");
            }

            sless.goodMorning();

            try {
                sless.goodBye();
                System.exit(-1);
            } catch(EJBException ex) {
                System.out.println("Expected failure from sless.goodBye()");
            }

            System.out.println("invoking stateful");
            System.out.println(sful.hello());
            System.out.println(sful.goodNight("everybody"));
            System.out.println(sful.goodNight("everybody", "see you tomorrow"));
            try {
                sful.bye();
                System.exit(-1);
            } catch(Exception ex) {
                System.out.println("Expected failure from sful.bye()");
            }


            System.out.println("test complete");

        } catch(Throwable e) {
            e.printStackTrace();
            System.exit(-1);
        }

    	return;
    }
}
