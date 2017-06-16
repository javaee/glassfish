/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.s1asdev.management.mejb.client;

import java.io.*;
import java.util.*;
import javax.management.j2ee.ManagementHome;
import javax.management.j2ee.Management;
import javax.management.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    
    // consts
    public static String kTestNotRun    = "TEST NOT RUN";
    public static String kTestPassed    = "TEST PASSED";
    public static String kTestFailed    = "TEST FAILED";
    public boolean DEBUG    = false;
    
    String jndiName = "ejb/mgmt/MEJB";
    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");
    
    public static void main (String[] args) {
        stat.addDescription("management-mejb");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("management-mejb");
    }
    
    public Client(String[] args) {
        String debug=args[0];
        if(debug.equalsIgnoreCase("false")) 
        DEBUG=false;               
        if(debug.equalsIgnoreCase("true")) 
        DEBUG=true;         
    }  

    
    
    public String doTest() {
        
        boolean pass = true;
        String res = kTestNotRun;
        
        try {
            
            Context ic = new InitialContext();
            System.out.println("***Created initial context");
            
            java.lang.Object objref = ic.lookup("ejb/mgmt/MEJB");
            System.out.println("***Looked up ManagementHome!!");
            
            ManagementHome  home = (ManagementHome)PortableRemoteObject.narrow(
                    objref, ManagementHome.class);
            System.out.println("***Narrowed ManagementHome!!");
            
            Management mejb = home.create();
            System.out.println("***Got the MEJB!!");
            
            String domain = mejb.getDefaultDomain();
            System.out.println("***MEJB default domain = "+domain);
            int mbeanCount = mejb.getMBeanCount().intValue();
            System.out.println("***MBeanCount = "+mbeanCount);
            
            // Print attributes of all MBeans
            ObjectName query = new ObjectName("*:*");
            Set mbeanNames = mejb.queryNames(query, null);
            if ( mbeanNames.size() != mbeanCount ) {
                System.out.println("***ERROR: mbeans returned by query is "
                        +mbeanNames.size());
                pass = false;
            }
            if(DEBUG){
                System.out.println("Test running in verbose mode");
                printConsoleOutput(mbeanNames,mejb);            
            }
            mejb.remove();
            if ( pass ) {
                res = kTestPassed;
                System.out.println("MEJB Test passed");
                stat.addStatus("MEJB Test", stat.PASS);
                
            } else {
                res = kTestFailed;
                System.out.println("MEJB Test failed");
                stat.addStatus("MEJB Test", stat.FAIL);
                
            }
            
            return res;
            
        } catch(NamingException ne){
            System.out.println("***Exception while initializing context.\n");
            ne.printStackTrace();
            res = kTestFailed;
            return res;
        } catch(Exception re) {
            re.printStackTrace();
            res = kTestFailed;
            return res;
        }
    }    
    
     public void printConsoleOutput(Set mbeanNames, Management managementbean){
        Set mbeanNamesSet=mbeanNames;
        Management mejb=managementbean;
        
        try{
            Iterator it = mbeanNamesSet.iterator();
            while ( it.hasNext() ) {
                
                ObjectName name = (ObjectName)it.next();
                System.out.println("ObjectName = "+name);
                
                // Construct list of attr names
                MBeanInfo info = mejb.getMBeanInfo(name);
                MBeanAttributeInfo[] attrInfo = info.getAttributes();
                String[] attrNames = new String[attrInfo.length];
                for ( int i=0; i<attrInfo.length; i++ ) {
                    attrNames[i] = attrInfo[i].getName();
                }
                // Get attr values from MEJB and print them
                
                AttributeList attrs = mejb.getAttributes(name, attrNames);
                for ( int i=0; i<attrs.size(); i++ ) {
                    
                    if (attrs.get(i) != null) {
                        System.out.println("TYPE = " +
                                attrs.get(i).getClass().getName());
                    }
                    
                    if (attrs.get(i) instanceof Attribute) {
                        Attribute attr = (Attribute)attrs.get(i);
                        System.out.println("Attribute name = "+attr.getName()
                        +"value = "+attr.getValue());
                    } else {
                        Object attr = attrs.get(i);
                        System.out.println("Attribute = " + attr);
                    }
                }
            }
        } catch(Exception exp){
            //exp.printStackTrace();
            System.out.println("***Exception occured while "+
                    "accessing mbean details:  Keep continuing\n");
        }
        //System.out.println("=======================");
    }
    
    
}