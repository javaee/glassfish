package com.sun.s1asdev.ejb.ejb30.persistence.eem_1slsb_2sfsbs;

import javax.ejb.Stateless;
import javax.ejb.EJB;
import javax.persistence.PersistenceContextType;
import javax.persistence.PersistenceContext;
import javax.persistence.EntityNotFoundException;

import javax.persistence.EntityManager;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

@Stateless
public class SlessBean
    implements Sless {

    @EJB
    private SfulPeer sfulPeer;

    public SfulPeer getSfulPeer() {
        return sfulPeer;
    }

}
