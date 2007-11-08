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


package oracle.toplink.essentials.testing.tests.cmp3.xml.merge.relationships;

import javax.persistence.EntityManager;

import junit.framework.*;
import junit.extensions.TestSetup;
import oracle.toplink.essentials.internal.ejb.cmp3.EJBQueryImpl;
import oracle.toplink.essentials.sessions.DatabaseSession;
import oracle.toplink.essentials.testing.models.cmp3.xml.merge.relationships.Customer;
import oracle.toplink.essentials.testing.models.cmp3.xml.merge.relationships.Item;
import oracle.toplink.essentials.testing.models.cmp3.xml.merge.relationships.Order;
import oracle.toplink.essentials.testing.models.cmp3.xml.merge.relationships.PartsList;
import oracle.toplink.essentials.testing.models.cmp3.xml.merge.relationships.RelationshipsTableManager;
import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;

/**
 * JUnit test case(s) for the TopLink EntityMappingsXMLProcessor.
 */
public class EntityMappingsMergeRelationshipsJUnitTestCase extends JUnitTestCase {
    private static Integer customerId;
    private static Integer itemId;
    private static Integer orderId;
    
    public EntityMappingsMergeRelationshipsJUnitTestCase() {
        super();
    }
    
    public EntityMappingsMergeRelationshipsJUnitTestCase(String name) {
        super(name);
    }
    
    public void setUp() {try{super.setUp();}catch(Exception x){}}
    
    public static Test suite() {
        TestSuite suite = new TestSuite("Relationships Model");
        suite.addTest(new EntityMappingsMergeRelationshipsJUnitTestCase("testCreateCustomer"));
        suite.addTest(new EntityMappingsMergeRelationshipsJUnitTestCase("testCreateItem"));
        suite.addTest(new EntityMappingsMergeRelationshipsJUnitTestCase("testCreateOrder"));

        suite.addTest(new EntityMappingsMergeRelationshipsJUnitTestCase("testReadCustomer"));
        suite.addTest(new EntityMappingsMergeRelationshipsJUnitTestCase("testReadItem"));
        suite.addTest(new EntityMappingsMergeRelationshipsJUnitTestCase("testReadOrder"));

        suite.addTest(new EntityMappingsMergeRelationshipsJUnitTestCase("testNamedQueryOnCustomer"));
        suite.addTest(new EntityMappingsMergeRelationshipsJUnitTestCase("testNamedQueryOnItem"));
        suite.addTest(new EntityMappingsMergeRelationshipsJUnitTestCase("testNamedQueryOnOrder"));

        suite.addTest(new EntityMappingsMergeRelationshipsJUnitTestCase("testUpdateCustomer"));
        suite.addTest(new EntityMappingsMergeRelationshipsJUnitTestCase("testUpdateItem"));
        suite.addTest(new EntityMappingsMergeRelationshipsJUnitTestCase("testUpdateOrder"));

        suite.addTest(new EntityMappingsMergeRelationshipsJUnitTestCase("testDeleteOrder"));
        suite.addTest(new EntityMappingsMergeRelationshipsJUnitTestCase("testDeleteCustomer"));
        suite.addTest(new EntityMappingsMergeRelationshipsJUnitTestCase("testDeleteItem"));
        
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
        em.close();        
    }
    
    public void testCreateItem() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {

            PartsList pl = new PartsList();
            em.persist(pl);
            PartsList pl2 = new PartsList();
            em.persist(pl2);
            
            java.util.ArrayList partsLists = new java.util.ArrayList();
            partsLists.add(pl);
            partsLists.add(pl2);

            Item item = new Item();
            item.setName("PartA");
            item.setDescription("This is part of a widget.");
            item.setImage(new byte[1024]);
            item.setPartsLists(partsLists);
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
        em.close();
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
        	em.refresh(em.find(Customer.class, customerId)); //refresh Customer
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
        Customer customer = (Customer)createEntityManager().createNamedQuery("findAllXMLMergeCustomers").getSingleResult();
        assertTrue("Error executing named query 'findAllXMLMergeCustomers'", customer != null);
    }

    public void testNamedQueryOnOrder() {
        EJBQueryImpl query = (EJBQueryImpl) createEntityManager().createNamedQuery("findAllXMLMergeOrdersByItem");
        query.setParameter("id", itemId);
        Order order = (Order) query.getSingleResult();
        assertTrue("Error executing named query 'findAllXMLMergeOrdersByItem'", order != null);
    }

    public void testNamedQueryOnItem() {
        EJBQueryImpl query = (EJBQueryImpl) createEntityManager().createNamedQuery("findAllXMLMergeItemsByName");
        query.setParameter("1", "PartA");
        Item item = (Item) query.getSingleResult();
        assertTrue("Error executing named query 'findAllXMLMergeItemsByName'", item != null);
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
        em.clear();
        Customer newCustomer = (Customer) em.find(Customer.class, customerId);
        assertTrue("Error updating Customer", newCustomer.getCity().equals("Dallas"));
    }

    public void testUpdateItem() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            PartsList pl = new PartsList();
            em.persist(pl);
            java.util.ArrayList partsLists = new java.util.ArrayList();
            partsLists.add(pl);
            
            Item item = (Item) em.find(Item.class, itemId);
            item.setDescription("A Widget");
            item.setImage(new byte[1280]);
            item.setPartsLists(partsLists);
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
        em.clear();
        Item newItem = (Item) em.find(Item.class, itemId);
        assertTrue("Error updating Item description", newItem.getDescription().equals("A Widget"));
        assertTrue("Error updating Item image", newItem.getImage().length==1280);
        assertTrue("Error updating Item parts lists", newItem.getPartsLists().size() != 1);
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
        em.clear();
        Customer newCustomer = (Customer) em.find(Customer.class, customerId);
        assertTrue("Error updating Customer", ((Order) newCustomer.getOrders().iterator().next()).getQuantity() == 100);
    }

    public static void main(String[] args) {
        junit.swingui.TestRunner.main(args);
    }
}
