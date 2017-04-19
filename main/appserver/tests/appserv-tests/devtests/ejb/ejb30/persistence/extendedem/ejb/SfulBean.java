package com.sun.s1asdev.ejb.ejb30.persistence.extendedem;

import javax.ejb.Stateful;
import javax.persistence.PersistenceContextType;
import javax.persistence.PersistenceContext;
import javax.persistence.EntityNotFoundException;

import javax.persistence.EntityManager;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

@Stateful
public class SfulBean
    implements Sful {

    private String name;

    private @PersistenceContext(
            unitName="lib/ejb-ejb30-persistence-extendedem-par1.jar#em",
            type=PersistenceContextType.EXTENDED) EntityManager extendedEM;

    private @PersistenceContext(
            unitName="lib/ejb-ejb30-persistence-extendedem-par3.jar#em")
            EntityManager txEM;

    private transient Person _p;

    public void createPerson(String name) {

        _p = new Person(name);
        extendedEM.persist(_p);
        System.out.println("Created " + _p);
            this.name = name;
    }

    public Person findPerson() {

        Person p = extendedEM.find(Person.class, name);
        System.out.println("Found " + p);
        return p;
    }

    public boolean removePerson() {

        Person p = txEM.find(Person.class, name);
        boolean removed = false;
        if (p != null) {
            txEM.remove(p);
            removed = true;
        }
        return removed;
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Person nonTxFindPerson() {
        Person p = (Person) txEM.find(Person.class, name);
        System.out.println("Found " + _p);
        return p;
    }


    public boolean refreshAndFindPerson() {
        boolean foundAfterRefresh = true;

        Person pp = extendedEM.find(Person.class, name);
        foundAfterRefresh = (pp == null);
        System.out.println("Error: Found " + pp);
        return foundAfterRefresh;
    }

}
