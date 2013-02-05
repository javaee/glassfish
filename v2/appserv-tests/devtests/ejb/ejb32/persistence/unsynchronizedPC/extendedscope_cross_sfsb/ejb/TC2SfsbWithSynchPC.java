package com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.extendedscope_cross_sfsb.ejb;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.SynchronizationType;

@Stateful
public class TC2SfsbWithSynchPC implements Tester{
    @PersistenceContext(unitName="lib/unsyncpc_extendedscope_cross_sfsb-par.jar#em",
            synchronization = SynchronizationType.SYNCHRONIZED,
            type = PersistenceContextType.EXTENDED)
    EntityManager em;
    
    @EJB private TC2FinderHome finderHome;
    private TC2Finder finderLocalObject;
    
    public boolean doTest() {
        try {
            // Should throw EJBException here
            finderLocalObject = finderHome.createFinder();
            finderLocalObject.findPerson("Tom");
            System.out.println("Method TC2FinderHome.createFinder invoked without exception thrown");
            return false;
        } catch (EJBException ejbException) {
            System.out.println("Method TC2FinderHome.createFinder invoked without expected exception thrown");
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
