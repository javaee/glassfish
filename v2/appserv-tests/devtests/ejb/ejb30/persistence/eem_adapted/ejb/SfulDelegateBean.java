package com.sun.s1asdev.ejb.ejb30.persistence.eem_adapted;

import javax.ejb.Stateful;
import javax.ejb.Init;
import javax.ejb.LocalHome;

import javax.persistence.PersistenceContextType;
import javax.persistence.PersistenceContext;
import javax.persistence.EntityNotFoundException;

import javax.persistence.EntityManager;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

@Stateful
@LocalHome(AdaptedLocalHome.class)
public class SfulDelegateBean {
    //implements SfulDelegate {

    @PersistenceContext(
        unitName="lib/ejb-ejb30-persistence-eem_adapted-par1.jar#em",
        type=PersistenceContextType.EXTENDED)
    private EntityManager extendedEM;

    Person _person;

    public EntityManager getEM() {
        return extendedEM;
    }
    
    @Init
    public void createDelegate(String name, String data) {
    }

    public Person getPerson() {
        return _person;
    }

    public Person createPerson(String name, String data) {

        Person person = new Person(name, data);
        
        extendedEM.persist(person);
        return person;
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
}
