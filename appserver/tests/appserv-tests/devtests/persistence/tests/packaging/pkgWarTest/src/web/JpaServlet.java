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

/*
 * JpaServlet.java
 */

package web;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.*;

import javax.persistence.*;
import javax.transaction.*;
import javax.annotation.*;

import entity.*;

import util.*;

public class JpaServlet extends ServletUtil {

    @PersistenceUnit(name = "myemf", unitName="pu1")
    private EntityManagerFactory emf;
    private EntityManager em;
    private @Resource UserTransaction utx;
    

    private final int customerId = 1;
    private final String customerName = "Joe Smith";
    private final int order1Id = 100;
    private final int order2Id = 200;
    private final String order1Addr = "123 Main St. Anytown, US";
    private final String order2Addr = "567 1st St. Random City, USA";


    public boolean processParams(HttpServletRequest request) {
      try {	
	if (request.getParameter("case") != null) {
	   tc = request.getParameter("case");
	} 
	return true;
     } catch(Exception ex) {
	System.err.println("Exception when processing the request params");
	ex.printStackTrace();
	return false;
     }

    }	

    public EntityManager getEntityManager()
    {
	return  emf.createEntityManager();
    }

    public boolean testInsert() {
        boolean result = false;

	try {
        utx.begin();
        //out.println("createEM  EntityManager=" + em);
	em = getEntityManager();
        // Create new customer
        Customer customer0 = new Customer();
        customer0.setId(customerId);
        customer0.setName(customerName);

        // Persist the customer
        em.persist(customer0);

        // Create 2 orders
        Order order1 = new Order();
        order1.setId(order1Id);
        order1.setAddress(order1Addr);

        Order order2 = new Order();
        order2.setId(order2Id);
        order2.setAddress(order2Addr);

        // Associate orders with the customer. The association
        // must be set on both sides of the relationship: on the
        // customer side for the orders to be persisted when
        // transaction commits, and on the order side because it
        // is the owning side.
        customer0.getOrders().add(order1);
        order1.setCustomer(customer0);

        customer0.getOrders().add(order2);
        order2.setCustomer(customer0);
	utx.commit();
        result = true;
     }catch(Exception ex){
        ex.printStackTrace();
      } finally {
	if (em != null) {
	   em.close();
	}
      }	
      return result;
    }


    public boolean testDelete() {
      boolean result = false;
      try {  
        utx.begin();
	em = getEntityManager();
        Customer c = findCustomer(customerName);
        Customer c0 = em.merge(c);
        em.remove(c0); 
        utx.commit();
        result = true;
      } catch(Exception ex){
	ex.printStackTrace();
      } finally {
        if (em != null) {
           em.close();
        }
      }
      return result;
    }
   
     public boolean verifyInsert() {
        boolean result = false;

	try {
        em = getEntityManager();
	System.out.println("Customer name in verifyInsert:"+customerName);
        Customer c = findCustomer(customerName);

        Collection<Order> orders = c.getOrders();
        if (orders == null || orders.size() != 2) {
            throw new RuntimeException("Unexpected number of orders: "
                    + ((orders == null)? "null" : "" + orders.size()));
        }
        result = true;
        } finally {
	if (em != null) {
           em.close();
	   }
        }
        return result;
    }


    public boolean verifyDelete() {
      boolean result = false;
      try {
      em = getEntityManager();
      Query q = em.createQuery("select c from Customer c");
      List results = q.getResultList();
      if (results == null || results.size() != 0) {
	throw new RuntimeException("Unexpected number of customers after delete");
      }

      q = em.createQuery("select o from Order o");
      results = q.getResultList();

      if (results == null || results.size() != 0) {
	throw new RuntimeException("Unexpected number of orders after delete");
      }
      result = true;
     } finally {
        if (em != null) {
           em.close();
           }
     }
      return result;
    }

    public Customer findCustomer(String name) {
      Query q = em.createQuery("select c from Customer c where c.name = :name");
      q.setParameter("name", name);
      Customer c = (Customer)q.getSingleResult();
      return c;
    }
    
}
