/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
