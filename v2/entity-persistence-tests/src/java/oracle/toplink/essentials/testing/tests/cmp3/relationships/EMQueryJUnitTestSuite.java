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


package oracle.toplink.essentials.testing.tests.cmp3.relationships;


import java.util.Collection;

import javax.persistence.EntityManager;

import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;
import javax.persistence.EntityNotFoundException;

import oracle.toplink.essentials.testing.models.cmp3.relationships.*;
import oracle.toplink.essentials.exceptions.DatabaseException;

import javax.persistence.Query;

public class EMQueryJUnitTestSuite extends JUnitTestCase {
    protected Integer nonExistingCustomerId = new Integer(999999);
        
    public EMQueryJUnitTestSuite() {
    }
    
    public EMQueryJUnitTestSuite(String name) {
        super(name);
    }
    
    public void setUp () {
        super.setUp();
        clearCache();
        new RelationshipsTableManager().replaceTables(JUnitTestCase.getServerSession());        
    }

    
    /*
     * bug 4628215: changed ObjectNotFoundException to EntityNotFoundException, and modified getReference to 
     * throw this exception instead of returning null.
     */
    public void testgetReference() throws Exception {
        Customer customer=null;
        Exception exception= null;
        try{
            customer = (Customer)createEntityManager().getReference(Customer.class,nonExistingCustomerId );
        }catch(Exception e){
            exception=e;
        }
        assertTrue("Test problem: Customer was found with an id of "+nonExistingCustomerId,customer==null );
        assertTrue("getReference() did not throw an instance of EntityNotFoundException", exception instanceof EntityNotFoundException);
    }

    /*
     * createNativeQuery(string) feature test
     *   tests that delete/insert and selects can be executed.
     */
    public void testcreateNativeQuery() throws Exception {
        EntityManager em = createEntityManager();
        try{
            em.getTransaction().begin();
            Query query1 = em.createNativeQuery("Select * FROM CMP3_CUSTOMER");
            Query query2 = em.createNativeQuery("INSERT INTO CMP3_CUSTOMER (CUST_ID, NAME, CITY, CUST_VERSION) VALUES (1111, NULL, NULL, 1)");
            Query query3 = em.createNativeQuery("DELETE FROM CMP3_CUSTOMER WHERE (CUST_ID=1111)");

            Collection c1 = query1.getResultList();
            if ((c1!=null) &&(c1.size()>0))
            assertTrue("getResultList returned null ",c1!=null );
            query2.executeUpdate();
            Collection c2 = query1.getResultList();
            assertTrue("getResultList returned null ",c2!=null );
            query3.executeUpdate();
            Collection c3 = query1.getResultList();
            assertTrue("getResultList returned null ",c3!=null );

            assertTrue("Native Select query gave unexpected result after Native Insert query ", c2.size()==(c1.size()+1) );
            assertTrue("Native Select query gave unexpected result after Native Delete query ", c3.size()==c1.size() );
        }finally{
            try{
                em.getTransaction().rollback();
                em.close();
            }catch(Exception ee){}
        }
    }

    /*
     * createNativeQuery(string) feature test
     *   tests that Query with Select SQL can be executed using getResultList() after it
     *   has run using executeUpdate()
     */
    public void testcreateNativeQueryWithSelectSQL() throws Exception {
        EntityManager em = createEntityManager();
        try{
            em.getTransaction().begin();
            Query query1 = em.createNativeQuery("Select * FROM CMP3_CUSTOMER");
            Query query2 = em.createNativeQuery("INSERT INTO CMP3_CUSTOMER (CUST_ID, NAME, CITY, CUST_VERSION) VALUES (1111, NULL, NULL, 1)");
            Query query3 = em.createNativeQuery("DELETE FROM CMP3_CUSTOMER WHERE (CUST_ID=1111)");

            Collection c1 = query1.getResultList();
            assertTrue("getResultList returned null ",c1!=null );

            // this may fail with some drivers
            int result = 0;
            try {
                result = query1.executeUpdate();
            } catch (RuntimeException ex) {
                em.getTransaction().rollback();
                em.getTransaction().begin();
            }
        

            query2.executeUpdate();
            Collection c2 = query1.getResultList();
            assertTrue("getResultList returned null ",c2!=null );
            query3.executeUpdate();
            Collection c3 = query1.getResultList();
            assertTrue("getResultList returned null ",c3!=null );

            assertTrue("Native Select query run with executeUpdate modified "+result+" rows ", result==0 );
            assertTrue("Native Select query gave unexpected result after Native Insert query ", c2.size()==(c1.size()+1) );
            assertTrue("Native Select query gave unexpected result after Native Delete query ", c3.size()==c1.size() );
        }finally{
            try{
                em.getTransaction().rollback();
                em.close();
            }catch(Exception ee){}
        }
    }

    /*
     * createNativeQuery(string) feature test
     *   tests that delete/insert and selects defined through annotations can be executed.
     */
    public void testNativeNamedQuery() throws Exception {
        EntityManager em = createEntityManager();
        try{
            em.getTransaction().begin();
            Query query1 = em.createNamedQuery("findAllSQLCustomers");
            Query query2 = em.createNamedQuery("insertCustomer1111SQL");
            Query query3 = em.createNamedQuery("deleteCustomer1111SQL");

            Collection c1 = query1.getResultList();
            assertTrue("getResultList returned null ",c1!=null );
            query2.executeUpdate();
            Collection c2 = query1.getResultList();
            assertTrue("getResultList returned null ",c2!=null );
            query3.executeUpdate();
            Collection c3 = query1.getResultList();
            assertTrue("getResultList returned null ",c3!=null );

            assertTrue("Named Native Select query gave unexpected result after Named Native Insert query ", c2.size()==(c1.size()+1) );
            assertTrue("Named Native Select query gave unexpected result after Named Native Delete query ", c3.size()==c1.size() );
        }finally{
            try{
                em.getTransaction().rollback();
                em.close();
            }catch(Exception ee){}
        }
    }
    
    /*
     * bug 4775066: base EJBQueryImpl to check for null arguments to avoid throwing a NPE.
     * result doesn't matter, only that it doesn't throw or cause an NPE or other exception.
     */
    public void testSetParameterUsingNull() throws Exception {
        try {
            java.util.List l = createEntityManager().createQuery(
                "Select Distinct Object(c) from Customer c where c.name = :cName")
                .setParameter("cName", null)
                .getResultList();
        } catch (DatabaseException e ) {
            // Above query generates following sql
            // SELECT DISTINCT CUST_ID, CITY, NAME, CUST_VERSION FROM CMP3_CUSTOMER WHERE (NAME = NULL)
            // which will not work on most of the dbs
            // Ignore any resulting DatabaseException

        }
    }

    /*
     * bug 5683148/2380: Reducing unnecessary joins on an equality check between the a statement
     *   and itself
     */
    public void testExcludingUnneccesaryJoin() throws Exception {
        EntityManager em = createEntityManager();
        try{
            em.getTransaction().begin();
                
            Order o = new Order();
            Order o2 = new Order();
            em.persist(o);
            em.persist(o2);
                
            java.util.List results = em.createQuery(
                    "Select Distinct Object(a) From OrderBean a where a.item Is Null OR a.item <> a.item")
                    .getResultList();
            assertTrue("Incorrect results returned when testing equal does not produce an unnecessary join ", results.size()==2 );
        }finally{
            try{
                em.getTransaction().rollback();
                em.close();
            }catch(Exception ee){}
        }
    }
    
    /*
     * gf bug 1395: changed jpql to not use distinct on joins unless specified by the user
     */
    public void testRemoveUnneccesaryDistinctFromJoin() throws Exception {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            Customer c = new Customer();
            Order o = new Order();
            o.setShippingAddress("somerandomaddress");
            o.setCustomer(c);
            Order o2 = new Order();
            o2.setShippingAddress("somerandomaddress");
            o2.setCustomer(c);
            
            c.getOrders().add(o);
            c.getOrders().add(o2);
            
            em.persist(c);
            em.flush();
            Collection results1 = em.createQuery(
                "Select Object(a) From Customer a JOIN a.orders o where o.shippingAddress = 'somerandomaddress'")
                .getResultList();
            
            Collection results2=em.createQuery(
                "Select Distinct a From Customer a JOIN a.orders o where o.shippingAddress = 'somerandomaddress'")
                .getResultList();
            assertTrue("Unexpected results returned from query without distinct clause", results1.size()==results2.size()+1);
        }finally{
            em.getTransaction().rollback();
        }
    }

}
