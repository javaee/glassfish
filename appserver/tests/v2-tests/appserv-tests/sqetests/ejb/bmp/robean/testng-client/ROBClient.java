/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2001-2018 Oracle and/or its affiliates. All rights reserved.
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

package samples.ejb.bmp.robean.client;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import java.util.*;
import samples.ejb.bmp.robean.ejb.*; 
import com.sun.ejb.ReadOnlyBeanNotifier;
import com.sun.ejb.containers.ReadOnlyBeanHelper;

import static org.testng.Assert.*;
import org.testng.Assert;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

public class ROBClient {

    Customer customer = null;
    CustomerRefresh customerRefresh = null;
    CustomerProgRefresh customerProgRefresh = null;

    String SSN="123456789";

    public static void main(String[] args) {
        org.testng.TestNG testng = new org.testng.TestNG();
        testng.setTestClasses(
            new Class[] { samples.ejb.bmp.robean.client.ROBClient.class } );
        testng.run();
    }

    @Configuration(beforeTestClass = true)
    public void initialize() throws Exception {
        InitialContext ic = new InitialContext();

        AddressHome addressHome = (AddressHome)PortableRemoteObject.narrow(
            ic.lookup("java:comp/env/ejb/address"), AddressHome.class);

        System.out.println("Narrowed AddressHome !!");

        CustomerHome customerHome = (CustomerHome)PortableRemoteObject.narrow(
            ic.lookup("java:comp/env/ejb/customer"), CustomerHome.class);

        System.out.println("Narrowed CustomerHome !!");

        CustomerTransactionalHome customerTransactionalHome =
            (CustomerTransactionalHome) PortableRemoteObject.narrow(
                ic.lookup("java:comp/env/ejb/customerTransactional"),
                CustomerTransactionalHome.class);

        System.out.println("Narrowed CustomerTransactionalHome !!");

        CustomerRefreshHome customerRefreshHome =
            (CustomerRefreshHome)PortableRemoteObject.narrow(
                ic.lookup("java:comp/env/ejb/customerRefresh"),
                CustomerRefreshHome.class);
        System.out.println("Narrowed CustomerRefreshHome !!");


        CustomerProgRefreshHome customerProgRefreshHome =
            (CustomerProgRefreshHome)PortableRemoteObject.narrow(
                ic.lookup("java:comp/env/ejb/customerProgRefresh"),
                CustomerProgRefreshHome.class);
        System.out.println("Narrowed CustomerProgRefreshHome !!");

        customer = customerHome.findByPrimaryKey(new PKString(SSN));
        customerRefresh = customerRefreshHome.findByPrimaryKey(SSN);
        customerProgRefresh =
            customerProgRefreshHome.findByPrimaryKey(new PKString1(SSN));
    }

    @Test
    public void test1() throws Exception {
        System.out.println("current balance before credit of 100 - " +
                           customer.getBalance());
        System.out.println("current prog balance before credit of 100 - " +
                           customerProgRefresh.getBalance());

        customer.doCredit(100);

        System.out.println("current balance after credit of 100 - "
                           + customer.getBalance());
        System.out.println("current prog balance after credit of 100 - " +
                           customerProgRefresh.getBalance());

        Assert.assertTrue(
            (customer.getBalance() != customerProgRefresh.getBalance()),
            "Test ReadOnlyBean is not updated");
    }

    @Test
    public void test2() throws Exception {
        ReadOnlyBeanNotifier notifier =
            ReadOnlyBeanHelper.getReadOnlyBeanNotifier(
                "java:comp/env/ejb/customerProgRefresh");
        notifier.refresh(new PKString1(SSN));

        System.out.println("current balance before credit of 150 - " +
                           customer.getBalance());
        System.out.println("current prog balance before credit of 150 - " +
                           customerProgRefresh.getBalance());

        customer.doCredit(150);

        System.out.println("current balance after credit of 150 - "
                           + customer.getBalance());
        System.out.println("current prog balance after credit of 150 - " +
                           customerProgRefresh.getBalance());

        Assert.assertTrue(
            (customer.getBalance() == customerProgRefresh.getBalance()),
            "Test ReadOnlyBean is updated programatically");
    }

    @Test
    public void test3() throws Exception {

        System.out.println("current balance - " + customer.getBalance());
        System.out.println("current refresh balance - " +
                           customerRefresh.getBalance());

        customer.doDebit(25);

        System.out.println("current balance - " + customer.getBalance());
        System.out.println("current refresh balance - " +
                           customerRefresh.getBalance());


        //CustomerRefresh Bean is refresh every 15 seconds
        Thread.sleep(20000);

        System.out.println("current prog balance - " +
                           customerRefresh.getBalance());
        Assert.assertTrue(
            (customer.getBalance() == customerRefresh.getBalance()),
            "Test ReadOnlyBean is updated periodically");
    }
}
