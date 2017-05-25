/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
