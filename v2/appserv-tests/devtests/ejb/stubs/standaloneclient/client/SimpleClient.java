package com.sun.s1asdev.ejb.stubs.standaloneclient;

import javax.ejb.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import java.util.Properties;
import java.io.FileInputStream;
import com.sun.s1asdev.ejb.stubs.ejbapp.HelloHome;
import com.sun.s1asdev.ejb.stubs.ejbapp.Hello;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class SimpleClient {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("ejb-stubs-standaloneclient");
        SimpleClient client = new SimpleClient(args);
        try {
            String jndiName = args[0];
            for(int i = 0; i < 3; i++) {
                System.out.println("Round " + i + ":");
                client.doTest();
            }
            stat.addStatus("simpleclient main", stat.PASS);
        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("simpleclient main", stat.FAIL);
        }

        stat.printSummary("ejb-stubs-standaloneclient");
    }  
    
    private String jndiName;
    private String propsFileName;

    public SimpleClient (String[] args) {

        jndiName = args[0];

        if( (args.length > 1) && (args[1].length() > 0) ) {
            propsFileName = args[1];
            System.out.println("Using props file " + propsFileName + 
                               " for InitialContext");
        } else {
            System.out.println("Using no-arg InitialContext");
        }

    }

    private InitialContext getContext() throws Exception {
        InitialContext ic;

        if( propsFileName == null ) {
            ic = new InitialContext();
        } else {
            Properties props = new Properties();
            FileInputStream fis = new FileInputStream(propsFileName);
            props.load(fis);
	    System.out.println("Using props = " + props);
            ic = new InitialContext(props);
	    System.out.println("ic = " + ic);
        }

        return ic;
    }

    private void doTest() throws Exception {

        Context ic = getContext();
        
        System.out.println("Looking up global jndi name = " + jndiName);
        // create EJB using factory from container 
        Object objref = ic.lookup(jndiName);
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
        System.out.println("successfully invoked ejb");
        
        EJBMetaData md = home.getEJBMetaData();            
        System.out.println("Got EJB meta data = " + md);
        
        EJBMetaData md2 = home.getEJBMetaData();            
        System.out.println("Got EJB meta data again = " + md2);
        
        HomeHandle homeHandle = home.getHomeHandle();
        System.out.println("Got Home Handle");
        HelloHome home2 = (HelloHome) homeHandle.getEJBHome();
        System.out.println("Converted Home Handle back to HelloHome");
        EJBMetaData md3 = home2.getEJBMetaData();            
        System.out.println("Got EJB meta data a 3rd time = " + md3);
        
        Handle helloHandle = hr.getHandle();
        System.out.println("Got hello Handle");
        Hello hello2 = (Hello) helloHandle.getEJBObject();
        System.out.println("Converted Hello Handle back to Hello");
        hello2.sayHello();
        System.out.println("successfully invoked ejb again");
        
    	return;
    }

}

