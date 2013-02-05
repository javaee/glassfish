package com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.extendedscope_cross_sfsb.ejb;

import javax.ejb.Init;
import javax.ejb.LocalHome;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.SynchronizationType;

import com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.extendedscope_cross_sfsb.ejb.Person;


@Stateful
@LocalHome(TC1FinderHome.class)
public class TC1SfsbWithSynchPC {
    @PersistenceContext(unitName="lib/unsyncpc_extendedscope_cross_sfsb-par.jar#em",
            synchronization = SynchronizationType.SYNCHRONIZED,
            type = PersistenceContextType.EXTENDED)
    EntityManager em;
    
    //@Override
    public Person findPerson(String name) {
        Person p = em.find(Person.class, name);
        System.out.print("Find persion " + p);
        return p;
    }
    
    @Init
    public void createFinder() {
        
    }
    
}
