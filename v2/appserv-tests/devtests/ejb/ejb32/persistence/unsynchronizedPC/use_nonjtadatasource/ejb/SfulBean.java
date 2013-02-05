package com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.use_nonjtadatasource.ejb;

import java.util.Map;

import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.SynchronizationType;


@Stateful
public class SfulBean {
    @PersistenceContext(unitName="lib/unsynchpc_use_nonjtadatasource-par.jar#em",
            type=PersistenceContextType.EXTENDED, 
            synchronization=SynchronizationType.UNSYNCHRONIZED)
    EntityManager em;
    
    public Person testUsingNonJTADataSource(Map<String, Boolean> resultMap) {
        Person p = em.find(Person.class, 1);
        System.out.println("I am in testUsingNonJTADataSource, and person name is " + p.getName());
        resultMap.put("equalsCurrentName", p.getName().equals("currentName"));
        return p;
    }
    
    public boolean testRollBackDoesNotClearUnsynchPC(Person person) {
         return (em.contains(person) && person.getName() != "newName"); 
    }
}
