package com.sun.s1asdev.ejb30.eempassivation;

import javax.ejb.Local;

import javax.persistence.EntityManager;

@Local
public interface SfulDelegate {

    Person create(String name, String data);

    Person find(String name);

    boolean remove(String name);

    EntityManager getEM();

    int getPassivationCount();
    int getActivationCount();

}
