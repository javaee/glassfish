package com.sun.s1asdev.ejb.ejb30.hello.session3;

import javax.ejb.Stateless;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.EntityManager;
import javax.naming.InitialContext;

@Stateless public class HelloEJB implements Hello {

    @PersistenceUnit 
        private EntityManagerFactory emf1;

    @PersistenceUnit(name="myemf", unitName="foo") 
        private EntityManagerFactory emf2;

    @PersistenceContext
        private EntityManager em1;

    @PersistenceContext(name="myem", unitName="foo") 
        private EntityManager em2;

    public void hello() {

        if( (emf1 != null) && (emf2 != null) && (em1 != null) &&
            (em2 != null) ) {

            try {
                InitialContext ic = new InitialContext();

                EntityManagerFactory lookupemf1 = (EntityManagerFactory)
                    ic.lookup("java:comp/env/com.sun.s1asdev.ejb.ejb30.hello.session3.HelloEJB/emf1");

                EntityManagerFactory lookupemf2 = (EntityManagerFactory)
                    ic.lookup("java:comp/env/myemf");

                EntityManager lookupem1 = (EntityManager)
                    ic.lookup("java:comp/env/com.sun.s1asdev.ejb.ejb30.hello.session3.HelloEJB/em1");

                EntityManager lookupem2 = (EntityManager)
                    ic.lookup("java:comp/env/myem");
                
            } catch(Exception e) {
                throw new javax.ejb.EJBException(e);
            }
            

            System.out.println("HelloEJB successful injection of EMF/EM references!");
        } else {
            throw new javax.ejb.EJBException("One or more EMF/EM references" +
                                             " was not injected in HelloEJB");
        }

        System.out.println("In HelloEJB::hello()");
    }

}
