package com.acme;

import javax.ejb.*;
import javax.persistence.*;
import javax.annotation.*;

import javax.naming.InitialContext;

import javax.management.j2ee.ManagementHome;

@Stateful
public class SFSB {

    @PersistenceContext
    private EntityManager em;

    @PostConstruct
    public void init() {
        System.out.println("In SingletonBean::init()");
        FooEntity fe = new FooEntity("POST_CONSTRUCT");
        em.persist(fe);
    }
    
    public String hello() throws Exception {
	System.out.println("In SingletonBean::hello()");
        Query q = em.createQuery("SELECT f FROM FooEntity f WHERE f.name=\"POST_CONSTRUCT\"");
        java.util.List result = q.getResultList(); 
        if (result.size() == 0) 
            throw new EJBException("FooEntity POST_CONSTRUCT not found!!!");

	return "Found " + result.size() + " FooEntity named POST_CONSTRUCT";
    }

    @PreDestroy
    public void destroy() {
        System.out.println("In SingletonBean::destroy()");
        try {
            javax.transaction.TransactionSynchronizationRegistry r = (javax.transaction.TransactionSynchronizationRegistry)
                   new InitialContext().lookup("java:comp/TransactionSynchronizationRegistry");
            if (r.getTransactionStatus() != javax.transaction.Status.STATUS_ACTIVE) {
                throw new IllegalStateException("Transaction status is not STATUS_ACTIVE: " + r.getTransactionStatus());
            }
            FooEntity fe = new FooEntity("FOO");
            em.persist(fe);
        } catch(Exception e) {
            throw new EJBException(e);
        }

    }



}
