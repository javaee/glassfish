package com.sun.s1asdev.ejb.ejb30.persistence.eem_adapted;

import javax.ejb.EJBLocalObject;

import javax.persistence.EntityManager;

public interface SfulDelegate
    extends EJBLocalObject {

    Person getPerson();

    Person createPerson(String name, String data);

    Person find(String name);

    boolean remove(String name);
    
    EntityManager getEM();

}
