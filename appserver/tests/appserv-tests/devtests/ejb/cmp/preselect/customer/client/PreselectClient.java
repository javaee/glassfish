/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2001-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1peqe.ejb.cmp.preselect.client;

import java.util.Iterator;
import java.util.ArrayList;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import com.sun.s1peqe.ejb.cmp.preselect.ejb.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;


public class PreselectClient {
    
    private static SimpleReporterAdapter status =
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) {
        try {
	    System.out.println("Starting preselect....");
	    System.out.println("Customer Bob has added 3 items to his shopping cart :");
	    System.out.println(" The items are : 1> Dark Chocolate $40, 2> Milk Chocolate $30, 3> White Chocolate $20");
            status.addDescription("Testing cmp preselect app.");
            Context initial = new InitialContext();
            Object objref = initial.lookup("java:comp/env/ejb/SimpleCustomer");

            CustomerHome home = 
	      (CustomerHome)PortableRemoteObject.narrow(objref, 
                                                        CustomerHome.class);
            CustomerRemote myCustomer = home.create("1","Bob");

	    System.out.println("Let us select all items that Bob bought whose price is below $100");
	    int items = myCustomer.getItemsForLess();

	    if (items == 0) {
	        System.out.println("No items available below $100 for this customer. ejbStore() was called before ejbSelect() and it modified the price of each item to $200");
		status.addStatus("cmp preselect:getSomeInfo", status.PASS);
	    } else {
	        System.out.println("Found " + items + " items with price less than 100. ejbStore was not called before ejbSelect. Hence the price of the items was not modified to $200");
	        status.addStatus("cmp preselect:getSomeInfo", status.FAIL);
	    }
 
            status.printSummary("preselectAppID");
            System.exit(0);
        } catch (Throwable ex) {
	    status.addStatus("cmp preselect:getSomeInfo", status.FAIL);
            System.err.println("Caught an exception in main: " + ex.toString());
            ex.printStackTrace();
        }
    }

}
