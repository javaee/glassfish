package com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.rollback_notclear_unsynchPC.ejb;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.SynchronizationType;
import javax.transaction.UserTransaction;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class SlessBean implements Tester{
    @PersistenceContext(unitName="lib/unsynchpc_rollback_notclear_unsynchPC-par.jar#em",
            synchronization=SynchronizationType.UNSYNCHRONIZED)
    EntityManager em;
    
    @Override
    public boolean doTest() {
        System.out.println("I am in SlessBean.doTest");
        
        UserTransaction utx = null;
        try {
            utx = (UserTransaction)(new javax.naming.InitialContext()).lookup("java:comp/UserTransaction");
            utx.begin();
            
            Person person = new Person("Tom");
            em.persist(person);
            
            //Let unsynchronized PC join the transaction;
            em.joinTransaction();
            
            System.out.println("Does PC contain person before rollback: " + em.contains(person));
            utx.rollback();
            System.out.println("Does PC contain person after rollback: " + em.contains(person));
            
            return !(em.contains(person));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
