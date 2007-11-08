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
// Copyright (c) 1998, 2007, Oracle. All rights reserved.  


package oracle.toplink.essentials.testing.tests.cmp3.xml.relationships;

import javax.persistence.EntityManager;

import junit.framework.*;
import junit.extensions.TestSetup;
import oracle.toplink.essentials.internal.ejb.cmp3.EJBQueryImpl;
import oracle.toplink.essentials.sessions.DatabaseSession;
import oracle.toplink.essentials.testing.models.cmp3.xml.advanced.AdvancedTableCreator;
import oracle.toplink.essentials.testing.models.cmp3.xml.relationships.Customer;
import oracle.toplink.essentials.testing.models.cmp3.xml.relationships.Item;
import oracle.toplink.essentials.testing.models.cmp3.xml.relationships.Order;
import oracle.toplink.essentials.testing.models.cmp3.xml.relationships.RelationshipsTableManager;
import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;

/**
 * JUnit test case(s) for the TopLink EntityMappingsXMLProcessor.
 */
public class EntityMappingsRelationshipsJUnitTestCase extends JUnitTestCase {
    private static Integer customerId;
    private static Integer itemId;
    private static Integer orderId;
    
    public EntityMappingsRelationshipsJUnitTestCase() {
        super();
    }
    
    public EntityMappingsRelationshipsJUnitTestCase(String name) {
        super(name);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite("Relationships Model");
        suite.addTest(new EntityMappingsRelationshipsJUnitTestCase("testCreateCustomer"));
        suite.addTest(new EntityMappingsRelationshipsJUnitTestCase("testCreateItem"));
        suite.addTest(new EntityMappingsRelationshipsJUnitTestCase("testCreateOrder"));

        suite.addTest(new EntityMappingsRelationshipsJUnitTestCase("testReadCustomer"));
        suite.addTest(new EntityMappingsRelationshipsJUnitTestCase("testReadItem"));
        suite.addTest(new EntityMappingsRelationshipsJUnitTestCase("testReadOrder"));

        suite.addTest(new EntityMappingsRelationshipsJUnitTestCase("testNamedQueryOnCustomer"));
        suite.addTest(new EntityMappingsRelationshipsJUnitTestCase("testNamedQueryOnItem"));
        suite.addTest(new EntityMappingsRelationshipsJUnitTestCase("testNamedQueryOnOrder"));

        suite.addTest(new EntityMappingsRelationshipsJUnitTestCase("testUpdateCustomer"));
        suite.addTest(new EntityMappingsRelationshipsJUnitTestCase("testUpdateItem"));
        suite.addTest(new EntityMappingsRelationshipsJUnitTestCase("testUpdateOrder"));

        suite.addTest(new EntityMappingsRelationshipsJUnitTestCase("testDeleteOrder"));
        suite.addTest(new EntityMappingsRelationshipsJUnitTestCase("testDeleteCustomer"));
        suite.addTest(new EntityMappingsRelationshipsJUnitTestCase("testDeleteItem"));
        
        return new TestSetup(suite) {
            
            protected void setUp(){               
                DatabaseSession session = JUnitTestCase.getServerSession();   
                new RelationshipsTableManager().replaceTables(session);
            }
        
            protected void tearDown() {
                clearCache();
            }
        };
    }
    
    public void testCreateCustomer() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            Customer customer = new Customer();
            customer.setName("Joe Black");
            customer.setCity("Austin");
            em.persist(customer);
            customerId = customer.getCustomerId();
            em.getTransaction().commit();    
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
        
    }
    public void testCreateItem() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            Item item = new Item();
            item.setName("Widget");
            item.setDescription("This is a Widget");
            item.setImage(new byte[1024]);
            em.persist(item);
            itemId = item.getItemId();
            em.getTransaction().commit();    
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
        
    }

    public void testCreateOrder() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            Order order = new Order();
            order.setShippingAddress("50 O'Connor St.");
            Customer customer = (Customer) em.find(Customer.class, customerId);
            order.setCustomer(customer);
            order.setQuantity(1);
            Item item = (Item) em.find(Item.class, itemId);
            order.setItem(item);
            em.persist(order);
            orderId = order.getOrderId();
            em.getTransaction().commit();    
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
    }

    public void testDeleteCustomer() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            em.remove(em.find(Customer.class, customerId));
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
        assertTrue("Error deleting Customer", em.find(Customer.class, customerId) == null);
    }

    public void testDeleteItem() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            em.remove(em.find(Item.class, itemId));
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
        assertTrue("Error deleting Item", em.find(Item.class, itemId) == null);
    }

    public void testDeleteOrder() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            em.remove(em.find(Order.class, orderId));
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
        assertTrue("Error deleting Order", em.find(Order.class, orderId) == null);
    }

    public void testNamedQueryOnCustomer() {
        Customer customer = (Customer)createEntityManager().createNamedQuery("findAllXMLCustomers").getSingleResult();
        assertTrue("Error executing named query 'findAllXMLCustomers'", customer != null);
    }

    public void testNamedQueryOnOrder() {
        EJBQueryImpl query = (EJBQueryImpl) createEntityManager().createNamedQuery("findAllXMLOrdersByItem");
        query.setParameter("id", itemId);
        Order order = (Order) query.getSingleResult();
        assertTrue("Error executing named query 'findAllXMLOrdersByItem'", order != null);
    }

    public void testNamedQueryOnItem() {
        EJBQueryImpl query = (EJBQueryImpl) createEntityManager().createNamedQuery("findAllXMLItemsByName");
        query.setParameter("1", "Widget");
        Item item = (Item) query.getSingleResult();
        assertTrue("Error executing named query 'findAllXMLItemsByName'", item != null);
    }

    public void testReadCustomer() {
        Customer customer = (Customer) createEntityManager().find(Customer.class, customerId);
        assertTrue("Error reading Customer", customer.getCustomerId() == customerId);
    }
    
    public void testReadItem() {
        Item item = (Item) createEntityManager().find(Item.class, itemId);
        assertTrue("Error reading Item", item.getItemId() == itemId);
    }

    public void testReadOrder() {
        Order order = (Order) createEntityManager().find(Order.class, orderId);
        assertTrue("Error reading Order", order.getOrderId() == orderId);
    }

    public void testUpdateCustomer() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            Customer customer = (Customer) em.find(Customer.class, customerId);
            customer.setCity("Dallas");
            em.merge(customer);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
        clearCache();
        Customer newCustomer = (Customer) em.find(Customer.class, customerId);
        assertTrue("Error updating Customer", newCustomer.getCity().equals("Dallas"));
    }

    public void testUpdateItem() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            Item item = (Item) em.find(Item.class, itemId);
            item.setDescription("A Widget");
            item.setImage(new byte[1280]);
            em.merge(item);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
        clearCache();
        Item newItem = (Item) em.find(Item.class, itemId);
        assertTrue("Error updating Item description", newItem.getDescription().equals("A Widget"));
        assertTrue("Error updating Item image", newItem.getImage().length==1280);
    }

    public void testUpdateOrder() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            Customer customer = (Customer) em.find(Customer.class, customerId);
            Order order = (Order) customer.getOrders().iterator().next();
            order.setQuantity(100);
            em.merge(customer);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
        clearCache();
        Customer newCustomer = (Customer) em.find(Customer.class, customerId);
        assertTrue("Error updating Customer", ((Order) newCustomer.getOrders().iterator().next()).getQuantity() == 100);
    }

    public static void main(String[] args) {
        junit.swingui.TestRunner.main(args);
    }
}
