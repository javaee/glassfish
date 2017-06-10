/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * glassfish/bootstrap/legal/CDDLv1.0.txt or
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
package ejb;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import java.util.Collection;
import java.util.List;

import entity.*;

@Stateless(name="ejb/Test")
public class TestBean implements Test {

    @PersistenceContext(unitName="pu1")
    private EntityManager em;

    public String testInsert() {

        // Create new customer
        Customer customer0 = new Customer();
        customer0.setId(1);
        customer0.setName("Joe Smith");

        // Persist the customer
        em.persist(customer0);

        // Create 2 orders
        Order order1 = new Order();
        order1.setId(100);
        order1.setAddress("123 Main St. Anytown, USA");

        Order order2 = new Order();
        order2.setId(200);
        order2.setAddress("567 1st St. Random City, USA");

        // Associate orders with the customer. The association 
        // must be set on both sides of the relationship: on the 
        // customer side for the orders to be persisted when 
        // transaction commits, and on the order side because it 
        // is the owning side.
        customer0.getOrders().add(order1);
        order1.setCustomer(customer0);

        customer0.getOrders().add(order2);
        order2.setCustomer(customer0);

        return "OK";
    }
        
    public String verifyInsert() {

        Customer c = findCustomer("Joe Smith");

        Collection<Order> orders = c.getOrders();
        if (orders == null || orders.size() != 2) {
            throw new RuntimeException("Unexpected number of orders: " 
                    + ((orders == null)? "null" : "" + orders.size()));
        }

        return "OK";
    }

     public String testDelete(String name) {

        Customer c = findCustomer(name);

        // Merge the customer to the new persistence context
        Customer c0 = em.merge(c);

        // Delete all records.
        em.remove(c0);

        return "OK";
    }


    public String verifyDelete() {

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

        return "OK";
    }

    public Customer findCustomer(String name) {
	Query q = em.createQuery("select c from Customer c where c.name = :name");
        q.setParameter("name", name);
        Customer c = (Customer)q.getSingleResult();
	return c;
    }

}
