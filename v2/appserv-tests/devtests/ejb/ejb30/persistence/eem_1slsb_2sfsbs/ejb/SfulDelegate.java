package com.sun.s1asdev.ejb.ejb30.persistence.eem_1slsb_2sfsbs;

import javax.ejb.Local;

import javax.persistence.EntityManager;

@Local
public interface SfulDelegate {

    Person create(String name, String data);

    Person find(String name);

    boolean remove(String name);
    
    EntityManager getEM();

    SfulPeer getSfulPeer();

}
