package com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.disallowed_methods.ejb;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.SynchronizationType;
import javax.persistence.TransactionRequiredException;

@Stateless
public class SlessBean implements Tester{
    @PersistenceContext(unitName="lib/unsynchpc_disallowed_methods-par.jar#em",
            synchronization=SynchronizationType.UNSYNCHRONIZED)
    EntityManager em;
    
    @Override
    public Map<String, Boolean> doTest() {
        Map<String, Boolean> resultMap = new HashMap<String, Boolean>();
        
        /*
         * SPEC: The application's use of queries with pessimistic locks,
         * bulk update or delete queries, etc. result in the provider 
         * throwing the TransactionRequiredException. 
         */
        //Query with pessimistic locks
        Query query = em.createQuery("SELECT OBJECT(p) FROM Person p WHERE p.name='Tom'");
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        try {
            query.getSingleResult();
            System.out.println("testPessimisticLockQuery with on expception thrown");
            resultMap.put("TestPessimisticLockQuery", false);
        } catch (TransactionRequiredException tre) {
            resultMap.put("TestPessimisticLockQuery", true);
        } catch (Exception e) {
            System.out.println("testPessimisticLockQuery with unexpected expception thrown");
            e.printStackTrace();
            resultMap.put("TestPessimisticLockQuery", false);
        }
        
        //Bulk update
        Query updateQuery = em.createQuery("UPDATE Person p SET p.name='Jim' WHERE p.id > 100");
        try {
            updateQuery.executeUpdate();
            System.out.println("testBulkUpdate with on expception thrown");
            resultMap.put("TestBulkUpdate", false);
        } catch (TransactionRequiredException tre) {
            resultMap.put("TestBulkUpdate", true);
        } catch (Exception e) {
            System.out.println("testBulkUpdate with unexpected expception thrown");
            e.printStackTrace();
            resultMap.put("TestBulkUpdate", false);
        }
        
        //Bulk delete
        Query deleteQuery = em.createQuery("DELETE FROM Person p WHERE p.name = 'Jim'");
        try {
            deleteQuery.executeUpdate();
            System.out.println("testBulkDelete with no expception thrown");
            resultMap.put("TestBulkDelete", false);
        } catch (TransactionRequiredException tre) {
            resultMap.put("TestBulkDelete", true);
        } catch (Exception e) {
            System.out.println("testBulkDelete with unexpected expception thrown");
            e.printStackTrace();
            resultMap.put("TestBulkDelete", false);
        }
        
        
        /*
         * SPEC: The application is permitted to invoke the persist, merge, remove, 
         * and refresh entity lifecycle operations on an entity manager of type 
         * SynchronizationType.UNSYNCHRONIZED independent of whether the 
         * persistence context is joined to the current transaction.
         */
        Person person2 = new Person("Jack");
        try {
            em.persist(person2);
            resultMap.put("TestPersist", true);
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("TestPersist", false);
        }
        
        try {
            em.merge(new Person("Lily"));
            resultMap.put("TestMerge", true);
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("TestMerge", false);
        }
        
        Person person3 = em.find(Person.class, 2);
        person3.setName("Lucy2");
        try {
            em.refresh(person3);
            resultMap.put("TestRefresh", true);
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("TestRefresh", false);
        }
        
        try {
            em.remove(person2);
            resultMap.put("TestRemove", true);
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("TestRemove", false);
        }
        
        return resultMap;
    }
}
