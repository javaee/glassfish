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

