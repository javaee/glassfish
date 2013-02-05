package com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.txscope_synctype_mismatch.ejb;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.SynchronizationType;

@Stateless
public class TC4SlsbWithSynchPC implements Tester{

    @PersistenceContext(unitName="lib/unsyncpc_txscope_synctype_mismatch-par.jar#em",
            synchronization = SynchronizationType.SYNCHRONIZED)
    EntityManager em;
    
    @EJB(beanName = "TC4SfsbWithUnsynchPC")
    Finder finder;
    
    @Override
    public boolean doTest() {
        try {
            System.out.println("I am in TC4SlsbWithSynchPC.doTest");
            //expect NO exception thrown from finder.findPerson
            finder.findPerson("Tom");
            System.out.println("method TC4SlsbWithSynchPC.findPerson ends with no exception thrown");
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("method TC4SlsbWithSynchPC.findPerson ends with unexpected exception thrown");
            //unexpected Exception
            return false;
        }
    }
}
