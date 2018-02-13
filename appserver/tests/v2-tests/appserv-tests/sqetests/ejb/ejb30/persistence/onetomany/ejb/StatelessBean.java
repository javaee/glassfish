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

package pe.ejb.ejb30.persistence.toplinksample.ejb;

import javax.ejb.*;
import javax.persistence.*;
import javax.annotation.*;
import javax.sql.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import pe.ejb.ejb30.persistence.toplinksample.ejb.*;
import java.util.*;

@Stateless
@Remote({StatelessInterface.class}) 
public class StatelessBean implements StatelessInterface {

    @Resource SessionContext sc;
    @PersistenceContext EntityManager em;

    CustomerEntity c1 = new CustomerEntity(1, "Alice", "Santa Clara");
    CustomerEntity c2 = new CustomerEntity(2, "Betty", "Sunnyvale");
    OrderEntity o1 = new OrderEntity(100, 1);
    OrderEntity o2 = new OrderEntity(101, 2);
    ItemEntity i1 = new ItemEntity(100, "Camcorder");
    ItemEntity i2 = new ItemEntity(101, "PlayStation");
    private String message = "hello";

    public void setUp() {
        System.out.println("StatelessBean:setUp:persisting...");
        em.flush();
        em.persist(c1);
        em.persist(c2);
        em.persist(o1);
        em.persist(o2);
        em.persist(i1);



        System.out.println("Stateless:setUp:Entities persisted");
    }

    public List getCustomers(String name, String city) {
        System.out.println("StatelessBean: getting customers...");
        List result = em.createNamedQuery("findAllCustomersWithLike")
            .setParameter("name", name)
            .setParameter("city", city)
            .getResultList();
        return result;
    }

    public List getAllCustomers() {
        System.out.println("StatelessBean: getting ALL customers...");
        List result = em.createNamedQuery("findAllCustomers").getResultList();
        return result;
    }

    public Collection getCustomerOrders(int custId) {
        System.out.println("StatelessBean:getCustomerOrders");
        CustomerEntity customer = em.find(CustomerEntity.class, new Integer(custId));
        if(customer == null){
            System.out.println("StatelessBean:getCustomerOrders:Customer not found!");
            return null;
        } else {
            OrderEntity order1 = em.find(OrderEntity.class, new Integer(100));
            OrderEntity order2 = em.find(OrderEntity.class, new Integer(101));
            ItemEntity item1 = em.find(ItemEntity.class, new Integer(101));
            order1.setItem(item1);
            //ArrayList al = new ArrayList();
            //al.add(o1);
            //al.add(o2);
            //c1.setOrders(al);
            customer.addOrder(order1);
            customer.addOrder(order2);
            //em.persist(customer);
            System.out.println("StatelessBean:getCustomerOrders:"+
                    "Customer found, returning orders");
            Collection<OrderEntity> coe = customer.getOrders();
            if(coe ==null) {
                System.out.println("StatelessBean:getCustomerOrders: "+
                    "NULL collection returned");
            } else {
                System.out.println("StatelessBean:getCustomerOrders: "+
                    "collection class returned:"+coe.getClass().getName());
            }

            return coe;
        }
    }

    public List getAllItemsByName() {
        System.out.println("StatelessBean: getting ALL customers...");
        List result = em.createNamedQuery("findAllItemsByName")
            .setParameter("1", "Camcorder")
            .getResultList();
        return result;
    }

    public List getAllOrdersByItem() {
        System.out.println("StatelessBean: getting ALL customers...");
        List result = em.createNamedQuery("findAllOrdersByItem")
            .setParameter("id", "1")
            .getResultList();
        return result;
    }

    public void cleanUp() {
        System.out.println("StatelessBean:cleanUp:");
        em.merge(c1);
        if(em.contains(c1)) em.remove(c1);
        em.merge(c2);
        if(em.contains(c2)) em.remove(c2);
        //em.merge(o1); // o1 will be removed as a result of cascade, after removing customer1
        //if(em.contains(o1)) em.remove(o1);
        //em.merge(o2); // o2 will be removed as a result of cascade, after removing customer1
        //if(em.contains(o2)) em.remove(o2);
        em.merge(i1);
        if(em.contains(i1)) em.remove(i1);
        em.merge(i2); // why isn't this throwing an exception?
        if(em.contains(i2)) em.remove(i2); // why isn't this throwing an exception?
        System.out.println("Stateless:cleanUp:Entities removed");
    }

    public void setMessage(String message) {
        System.out.println("Stateless:setMessage:" + message);
        this.message = message;
    }

    public String getMessage() { return message; }
}
