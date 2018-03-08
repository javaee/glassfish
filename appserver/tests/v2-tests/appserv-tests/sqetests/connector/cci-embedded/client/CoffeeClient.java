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

package com.sun.s1peqe.connector.cci;

import java.util.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class CoffeeClient {
   private static SimpleReporterAdapter stat=new SimpleReporterAdapter("appserv-tests");

   public static void main(String[] args) {
       String testId = "J2EE Connectors : Embedded Adapter Tests";
       try {

	   if (args.length == 1) {
	       testId = args[0];
	   }

           System.err.println(testId + " : CoffeeClient started in main...");
	   stat.addDescription("J2EE Connectors 1.5: Embedded CCI Adapter tests");
           Context initial = new InitialContext();
           Object objref = initial.lookup("java:comp/env/ejb/SimpleCoffee");

           CoffeeRemoteHome home = 
               (CoffeeRemoteHome)PortableRemoteObject.narrow(objref, 
                                                       CoffeeRemoteHome.class);

           CoffeeRemote coffee = home.create();

           int count = coffee.getCoffeeCount();
           System.err.println("Coffee count = " + count);

           System.err.println("Inserting 3 coffee entries...");
           coffee.insertCoffee("Mocha", 10);
           coffee.insertCoffee("Espresso", 20);
           coffee.insertCoffee("Kona", 30);

           int newCount = coffee.getCoffeeCount();
           System.err.println("Coffee count = " + newCount);
	   if (count==(newCount-3)) {
		stat.addStatus("Connector:cci Connector " + testId + " rar Test status:",stat.PASS);
	   }else{
		stat.addStatus("Connector:cci Connector " + testId + " rar Test status:",stat.FAIL);
	   }



       } catch (Exception ex) {
           System.err.println("Caught an unexpected exception!");
	   stat.addStatus("Connector:CCI Connector " + testId + " rar Test status:",stat.FAIL);
           ex.printStackTrace();
       }finally{
	   //print test summary
	   stat.printSummary(testId);
       }
   } 


} 
