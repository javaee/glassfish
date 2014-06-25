package com.acme;

import javax.ejb.*;
import javax.persistence.*;
import javax.annotation.*;

import javax.naming.InitialContext;

import javax.management.j2ee.ManagementHome;

@Stateful
@LocalBean
public class SFSB extends SuperSFSB implements Hello {

    @PersistenceContext
    private EntityManager em;

    @PostConstruct
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void init() {
        System.out.println("In SFSB::init()");
        FooEntity fe = new FooEntity("BAR");
        em.persist(fe);
        System.out.println("Done SFSB::init()");
    }
    
    public String test(String value, int count) throws EJBException {
	System.out.println("In SFSB::test()");
        Query q = em.createQuery("SELECT f FROM FooEntity f WHERE f.name=:name");
        q.setParameter("name", value);
        java.util.List result = q.getResultList(); 
        if (result.size() != count) 
            throw new EJBException("ERROR: Found " + result.size() + " FooEntity named " + value + ", not expected " + count);

	return "Found " + result.size() + " FooEntity named " + value;
    }

    @Remove
    public void testRemove() {
        System.out.println("In SFSB::testRemove()");
    }

    @PreDestroy
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void destroy() {
        System.out.println("In SFSB::destroy()");
        try {
            javax.transaction.TransactionSynchronizationRegistry r = (javax.transaction.TransactionSynchronizationRegistry)
                   new InitialContext().lookup("java:comp/TransactionSynchronizationRegistry");
            if (r.getTransactionStatus() != javax.transaction.Status.STATUS_ACTIVE) {
                throw new IllegalStateException("Transaction status is not STATUS_ACTIVE: " + r.getTransactionStatus());
            }
            FooEntity fe = new FooEntity("FOO");
            em.persist(fe);
            System.out.println("Done SFSB::destroy()");
        } catch(Exception e) {
            throw new EJBException(e);
        }

    }



}
