/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package pe.ejb.ejb30.persistence.toplinksample.client;

import java.io.*;
import java.util.*;
import javax.ejb.*;
import javax.naming.*;
import java.rmi.*;
import pe.ejb.ejb30.persistence.toplinksample.ejb.*;

import org.testng.Assert;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;


public class Client {

    private static @EJB StatelessInterface sless;
    List rows;
    Iterator i;

    public static void main(String[] args) {
        org.testng.TestNG testng = new org.testng.TestNG();
        testng.setTestClasses(
            new Class[] { pe.ejb.ejb30.persistence.toplinksample.client.Client.class } );
        testng.run();
    }

    @Configuration(beforeTestClass = true)
    public void setup() throws Exception {
        System.out.println("Client: invoking stateful setup");
        sless.setUp();
    }

    @Configuration(afterTestClass = true)
    public void cleanup() throws Exception {
        System.out.println("Cleanup: DELETING ROWS...");
        sless.cleanUp();
    }

    @Test
    public void testGetCustomerOrders() throws Exception {
        System.out.println("Client: getting customer orders");
        Collection coll = sless.getCustomerOrders(1);
        if (coll != null) {
            for (Iterator iterator=coll.iterator(); iterator.hasNext();)
                System.out.println((OrderEntity)iterator.next());
        }
        Assert.assertTrue((coll != null), "Got customers orders");
    }

    @Test
    public void testGetCustomerByName() throws Exception {
        System.out.println("Get customer by name and address");
        List rows = sless.getCustomers("Alice", "Santa Clara");
        if (rows != null) {
            for (Iterator iterator = rows.iterator(); iterator.hasNext();)
                System.out.println((CustomerEntity)iterator.next());
        }

        Assert.assertTrue((rows != null), "Got customers");
    }

    @Test
    public void testGetAllCustomers() throws Exception {
        System.out.println("Get all customers");
        List rows = sless.getAllCustomers();
        if (rows != null) {
            for (Iterator iterator = rows.iterator(); iterator.hasNext();)
                System.out.println((CustomerEntity)iterator.next());
        }

        Assert.assertTrue((rows != null), "Got all customers");
    }

    @Test
    public void testGetAllItemsByName() throws Exception {
        System.out.println("Get all items by name");
        List rows = sless.getAllItemsByName();

        if (rows != null) {
            for (Iterator iterator = rows.iterator(); iterator.hasNext();)
                System.out.println((ItemEntity)iterator.next());
        }
        Assert.assertTrue((rows != null), "Got all item by name");
    }

    @Test
    public void testGetAllOrdersByItem() throws Exception {
        System.out.println("Get all orders by item");
        List rows = sless.getAllOrdersByItem();

        if (rows != null) {
            for (Iterator iterator = rows.iterator(); iterator.hasNext();)
                System.out.println((ItemEntity)iterator.next());
        }
        Assert.assertTrue((rows != null), "Got all orders by item");
    }
}
