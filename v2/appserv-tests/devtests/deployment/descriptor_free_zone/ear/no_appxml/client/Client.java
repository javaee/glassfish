package com.sun.s1asdev.deployment.noappxml.client;

import javax.ejb.EJB;
import com.sun.s1asdev.deployment.noappxml.ejb.Sful;
import com.sun.s1asdev.deployment.noappxml.ejb.Sless;

public class Client {

    public static void main (String[] args) {
        Client client = new Client(args);
        client.doTest();
    }  
    
    public Client (String[] args) {}

    @EJB
    private static Sful sful;

    @EJB
    private static Sless sless;

    public void doTest() {
        try {

            System.err.println("invoking stateful");
            sful.hello();

            System.err.println("invoking stateless");
            sless.hello();
        
            pass();
        } catch(Exception e) {
            e.printStackTrace();
            fail();
        }
        
    	return;
    }

    private void pass() {
        System.err.println("PASSED: descriptor_free_zone/ear/no_appxml");
        System.exit(0);
    }
                                                                                             
    private void fail() {
        System.err.println("FAILED: descriptor_free_zone/ear/no_appxml");
        System.exit(-1);
    }
}
