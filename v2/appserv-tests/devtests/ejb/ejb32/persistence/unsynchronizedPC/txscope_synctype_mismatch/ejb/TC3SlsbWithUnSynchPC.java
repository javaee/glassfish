package com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.txscope_synctype_mismatch.ejb;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.SynchronizationType;

@Stateless
public class TC3SlsbWithUnSynchPC implements Finder{
    
    @PersistenceContext(unitName="lib/unsyncpc_txscope_synctype_mismatch-par.jar#em",
            synchronization = SynchronizationType.UNSYNCHRONIZED)
    EntityManager em;

    @Override
    public Person findPerson(String name) {
        Person p = em.find(Person.class, name);
        System.out.print("Find persion " + p);
        return p;
    }
}
