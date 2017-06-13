package com.sun.s1asdev.ejb.stress.passivateactivate.client;

import java.util.ArrayList;

import java.io.Serializable;
import javax.naming.*;
import javax.jms.*;
import javax.ejb.*;
import javax.rmi.PortableRemoteObject;
import java.rmi.NoSuchObjectException;
import java.util.Properties;

import com.sun.s1asdev.admin.ee.synchronization.api.client.SynchronizationHome;
import com.sun.s1asdev.admin.ee.synchronization.api.client.Synchronization;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    // consts
    public static String kTestNotRun    = "TEST NOT RUN";
    public static String kTestPassed    = "TEST PASSED";
    public static String kTestFailed    = "TEST FAILED";
    
    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");


    private Synchronization sfsb = null;

    public static void main (String[] args) {

       stat.addDescription("synchronization");
       if ( args.length != 7) {
            stat.addStatus("client initSFSB-- wrong number of arguments", stat.FAIL);
            System.out.println("Usage: SyncClientTest provider-url ctxFactory jndi-name <instanceName:string> <get|put> <source:string> <destination dir/file:string>");
             return;
        } 

        if ( !args[4].equals("get") && !args[4].equals("put") ) {
            stat.addStatus( "client initSFSB-- command can be either put or get. Please retry", stat.FAIL);
            System.out.println("Usage: Command can be either put or get. Please retry");
            return;
        }

        Client client = new Client(args);
        System.out.println("[apiClient] doTest()...");
        client.doTest(args);
        System.out.println("[apiClient] DONE doTest()...");
        stat.printSummary("synchronization");
    }  
    
    public Client (String[] args) {
    }
    
    public void doTest(String[] args) {

        initSFSB(args);
        testSynch(args);
    }

    private void initSFSB(String[] args) {

        System.out.println("[apiClient] Inside init....");
        try {
            Context ic = null;
            Object objref = null;
            if ((args[0] == null) || (args[1] == null)) { 
                ic = new InitialContext();
                System.out.println("[apiClient] Lookingup Bean apiClient ");
                objref = ic.lookup("java:comp/env/ejb/apiClient");
            } else {
                Properties env = new Properties();
                env.put("java.naming.provider.url", args[0]);
                env.put("java.naming.factory.initial", args[1]);
                ic = new InitialContext(env);
                objref = ic.lookup(args[2]);
            }

            SynchronizationHome home = (SynchronizationHome)
                PortableRemoteObject.narrow (objref, SynchronizationHome.class);

            sfsb = (Synchronization) home.create();

            System.out.println("[passivateactivate] Initalization done");
            stat.addStatus("init apiClient", stat.PASS);
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("[apiClient] Exception in init....");
            e.printStackTrace();
            stat.addStatus("client initSFSB", stat.FAIL);
        }
    }

    public void testSynch(String[] args) {
        try {
                boolean r;
                if ( args[4].equals("get")) {
                     r = sfsb.getFile(args[3], args[5], args[6]);

                } else {
                     r = sfsb.putFile(args[3], args[5], args[6]);

                }
                if ( r)
                    System.out.println( args[4] + " of " + args[5] + 
                        " to " + args[6] + " passed.");
                else
                    System.out.println( args[4] + " of " + args[5] + 
                        " to " + args[6] + " failed.");
    
                stat.addStatus("ejbclient accessSFSB", stat.PASS);
        } catch (Exception ex) {
            stat.addStatus("ejbclient accessSFSB", stat.FAIL);

        }
    }

} //Client{}
