package com.sun.s1asdev.ejb.stubs.standaloneclient.client;

import java.io.*;
import java.util.*;

import javax.ejb.EJBHome;
import javax.ejb.Handle;
import javax.ejb.EJBMetaData;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import org.omg.CORBA.ORB;

import com.sun.s1asdev.ejb.stubs.ejbapp.HelloHome;
import com.sun.s1asdev.ejb.stubs.ejbapp.Hello;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private Context ic;
    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("ejb-stubs-standaloneclient");

        // Step 1:  It is important to call initailizeSystemProperties to 
        // avoid problems with switching ORBs between J2SE and AppServer. 
        // These are system properties that needs to be run once or these 
        // properties can be passed through -D flags
//PG->        initializeSystemProperties( );
        
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-stubs-standaloneclientID");
    }  
    
    public Client (String[] args) {
        try {
            setupInitialContext( "achumba" , "3700" );  
//          ic = new InitialContext();


        } catch(Exception e) {
             System.out.println(
                  "standaloneclient.Client(), Exception when setting " + 
                  "up the InitialContext environment");
             e.printStackTrace();
        } 
    }
    
    public void doTest() {

        try {

            System.out.println("Looking up ejb ref ");
            // create EJB using factory from container 
            Object objref = ic.lookup("ejb/ejb_stubs_ejbapp_HelloBean");
            System.out.println("objref = " + objref);
            System.err.println("Looked up home!!");
                
            HelloHome  home = (HelloHome)PortableRemoteObject.narrow
                (objref, HelloHome.class);
                                                                     
            System.err.println("Narrowed home!!");
                
            Hello hr = home.create();
            System.err.println("Got the EJB!!");
                
            // invoke method on the EJB
            System.out.println("invoking ejb");
            hr.sayHello();


            NamingEnumeration _enum = ic.list("");
            System.out.println ("PG-> display all the IntialContext entries");
            while( _enum.hasMore() ) {
                System.out.println ("PG-> " + _enum.next() );
            }

            Handle handle = hr.getHandle();
            System.out.println("successfully got handle - 1");

            EJBMetaData metadata = home.getEJBMetaData();
            System.out.println("successfully got metadata - 1");

            System.out.println("successfully invoked ejb");
            stat.addStatus("standaloneclient main", stat.PASS);

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("standaloneclient main" , stat.FAIL);
        }
        
    	return;
    }

    private void setupInitialContext( String host, String port ) 
    throws Exception 
    {
        Properties env = new Properties();

//        env.put( Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.cosnaming.CNCtxFactory" );
        env.put( Context.INITIAL_CONTEXT_FACTORY, "com.sun.enterprise.naming.SerialInitContextFactory");
        
        env.put( Context.PROVIDER_URL, "iiop://" + host + ":" + port );

        ic = new InitialContext( env );
    }

    // Initialize to use SUN ONE AppServer 7 ORB and UtilDelegate
    // NOTE: All these are OMG standard properties provided to plug in an ORB 
    // to JDK
    private static void initializeSystemProperties( ) {
        System.setProperty( "org.omg.CORBA.ORBClass",
             "com.sun.corba.ee.impl.orb.ORBImpl" );
        System.setProperty( "javax.rmi.CORBA.UtilClass", 
             "com.sun.corba.ee.impl.javax.rmi.CORBA.Util" );

        System.setProperty( "javax.rmi.CORBA.StubClass",
             "com.sun.corba.ee.impl.javax.rmi.CORBA.StubDelegateImpl");  
        System.setProperty( "javax.rmi.CORBA.PortableRemoteClass",
             "com.sun.corba.ee.impl.javax.rmi.PortableRemoteObject");
    }

} //Client{}
