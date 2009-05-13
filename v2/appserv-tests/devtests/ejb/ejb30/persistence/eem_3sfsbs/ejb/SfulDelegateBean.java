package com.sun.s1asdev.ejb.ejb30.persistence.eem_3sfsbs;

import javax.ejb.Stateful;
import javax.ejb.EJB;
import javax.persistence.PersistenceContextType;
import javax.persistence.PersistenceContext;
import javax.persistence.EntityNotFoundException;

import javax.persistence.EntityManager;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

@Stateful
public class SfulDelegateBean
    implements SfulDelegate {

    @PersistenceContext(
        unitName="lib/ejb-ejb30-persistence-eem_3sfsbs-par1.jar#em",
        type=PersistenceContextType.EXTENDED)
    private EntityManager extendedEM;

    @EJB
    private SfulPeer sfulPeer;

    public EntityManager getEM() {
        return extendedEM;
    }
    
    public Person create(String name, String data) {

        Person p = new Person(name, data);
        
        extendedEM.persist(p);
        return p;
    }

    public Person find(String name) {

        Person p = extendedEM.find(Person.class, name);
        System.out.println("Found " + p);
        return p;
    }

    public boolean remove(String name) {

        Person p = extendedEM.find(Person.class, name);
        boolean removed = false;
        if (p != null) {
            extendedEM.remove(p);
            removed = true;
        }
        return removed;
    }

    public SfulPeer getSfulPeer() {
        return sfulPeer;
    }

}
