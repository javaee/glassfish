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

package com.sun.s1asdev.ejb.jms.jmsejb.client;

import java.io.*;
import java.util.*;
import javax.ejb.EJBHome;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import org.omg.CORBA.ORB;
import java.sql.*;
import javax.sql.*;
import javax.jms.*;
import com.sun.s1asdev.ejb.jms.jmsejb.HelloHome;
import com.sun.s1asdev.ejb.jms.jmsejb.Hello;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("ejb-mdb-jmsejb");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-mdb-jmsejbID");
    }  
    
    public Client (String[] args) {
    }
    
    public void doTest() {

        String ejbNames[] = { "ejbs/hellobmt", "ejbs/hellocmt" };
        String ejbName = null;
        try {
            for(int i = 0; i < ejbNames.length; i++ ) {
                ejbName = ejbNames[i];
                
                Context ic = new InitialContext();
                
                System.out.println("Looking up ejb ref " + ejbName);
                // create EJB using factory from container 
                // java.lang.Object objref = ic.lookup("helloApp/Hello");
                java.lang.Object objref = ic.lookup("java:comp/env/"+ejbName);
                System.out.println("---ejb stub---" + objref.getClass().getClassLoader());
                System.out.println("---ejb classname---" + objref.getClass().getName());
                System.out.println("---HelloHome---" + HelloHome.class.getClassLoader());
                System.err.println("Looked up home!!");
                
                HelloHome  home = (HelloHome)PortableRemoteObject.narrow(
                                                                         objref, HelloHome.class);
                System.err.println("Narrowed home!!");
                
                
                Hello hr = home.create(helloStr);
                System.err.println("Got the EJB!!");
                
                // invoke method on the EJB
                System.out.println("Asking ejb to send a message");
                String msgText = "Look ma, I received a JMS message!!!";
                
                //
                String result = hr.sendMessage1(msgText);
                System.out.println("Result from sendMessage = " + result);
                System.out.println("Asking ejb to receive a message");
                hr.receiveMessage1();
                
                //
                result = hr.sendMessage2(msgText);
                System.out.println("Result from sendMessage = " + result);
                System.out.println("Asking ejb to receive a message");
                hr.receiveMessage2();
                
                //
                result = hr.sendMessage3(msgText);
                System.out.println("Result from sendMessage = " + result);
                System.out.println("Asking ejb to receive a message");
                hr.receiveMessage3();
                
                /*  comment out 
                hr.sendMessage4Part1(msgText);
                hr.sendMessage4Part2(msgText);

                hr.receiveMessage4Part1();
                hr.receiveMessage4Part2();
                */
                
                hr.sendAndReceiveMessage();

                hr.sendAndReceiveRollback();

                // Must be called in order
                hr.sendMessageRollback(msgText + "rollback");
                hr.receiveMessageRollback();

                stat.addStatus("jmsejb " + ejbName, 
                               stat.PASS);
            }
        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("jmsejb " + ejbName, stat.FAIL);
        }
        
    	return;
    }

    final static String helloStr = "Hello World!";
}

