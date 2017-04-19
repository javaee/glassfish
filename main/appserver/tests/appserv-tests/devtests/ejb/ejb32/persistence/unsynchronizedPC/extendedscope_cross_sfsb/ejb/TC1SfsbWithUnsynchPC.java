package com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.extendedscope_cross_sfsb.ejb;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.SynchronizationType;

@Stateful
public class TC1SfsbWithUnsynchPC implements Tester{
    @PersistenceContext(unitName="lib/unsyncpc_extendedscope_cross_sfsb-par.jar#em",
            synchronization = SynchronizationType.UNSYNCHRONIZED,
            type = PersistenceContextType.EXTENDED)
    EntityManager em;
    
    @EJB private TC1FinderHome finderHome;
    private TC1Finder finderLocalObject;
    
    public boolean doTest() {
        try {
            // Should throw EJBException here
            finderLocalObject = finderHome.createFinder();
            System.out.println("Method TC1FinderHome.createFinder invoked without exception thrown");
            finderLocalObject.findPerson("Tom");
            return false;
        } catch (EJBException ejbException) {
            System.out.println("Method TC1FinderHome.createFinder invoked with expected exception thrown");
            // Expected exception
            return true;
        } catch (Exception e) {
            System.out.println("Unexpected exception thrown");
            e.printStackTrace();
            // Unexpected exception
            return false;
        }
        
    }
}
