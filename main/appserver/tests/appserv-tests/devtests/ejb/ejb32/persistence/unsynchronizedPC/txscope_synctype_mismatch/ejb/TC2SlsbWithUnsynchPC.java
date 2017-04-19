package com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.txscope_synctype_mismatch.ejb;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.SynchronizationType;

@Stateless
public class TC2SlsbWithUnsynchPC implements Tester{

    @PersistenceContext(unitName="lib/unsyncpc_txscope_synctype_mismatch-par.jar#em",
            synchronization = SynchronizationType.UNSYNCHRONIZED)
    EntityManager em;
    
    @EJB(beanName = "SlsbWithSynchPC") 
    Finder finder;
    
    @Override
    public boolean doTest() {
        try {
            //FIXME: a workaround to initiate JavaEETransactionImpl.txEntityManagerMap, 
            //so that the current PC will be associated with the current TX.
            em.find(Person.class, "Tom");
            
            System.out.println("I am in TC2SlsbWithUnsynchPC.doTest");
            //expect exception thrown from finder.findPerson
            finder.findPerson("Tom");
            System.out.println("method TC2SlsbWithUnsynchPC.findPerson ends with no exception thrown");
            return false;
        } catch(EJBException ejbEx) {
            System.out.println("method TC2SlsbWithUnsynchPC.findPerson ends with EJBException thrown");
            //expect EJBException
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("method TC2SlsbWithUnsynchPC.findPerson ends with unexpected exception thrown");
            //unexpected Exception
            return false;
        }
    }
    

}
