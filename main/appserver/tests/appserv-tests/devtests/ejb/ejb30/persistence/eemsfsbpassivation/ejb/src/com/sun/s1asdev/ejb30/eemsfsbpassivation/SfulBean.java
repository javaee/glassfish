package com.sun.s1asdev.ejb30.eemsfsbpassivation;

import java.util.Map;
import java.util.HashMap;
import javax.naming.InitialContext;
import javax.ejb.Stateful;
import javax.ejb.EJB;
import javax.persistence.PersistenceContextType;
import javax.persistence.PersistenceContext;
import javax.persistence.EntityNotFoundException;
import javax.persistence.EntityManager;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.EJBException;

@Stateful
public class SfulBean implements Sful {
    private String name;

    private @PersistenceContext(unitName = "lib/ejb-ejb30-persistence-eemsfsbpassivation-par1.jar#em", type = PersistenceContextType.EXTENDED)
    EntityManager extendedEM;

    private Map<String, Boolean> testResultsMap = new HashMap<String, Boolean>();

    public void setName(String name) {
        this.name = name;
        try {
            String lookupName = "java:comp/env/ejb/SfulBean";
            InitialContext initCtx = new InitialContext();

            Person person = new Person(name);
            person.data = "data: " + name;
            extendedEM.persist(person);
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }

    public Map<String, Boolean> doTests(String prefix) {
        Person p = extendedEM.find(Person.class, name);
        testResultsMap.put("find" + prefix + "Activate", (p != null));

        return testResultsMap;
    }

    private void sleepForSeconds(int time) {
        try {
            while (time-- > 0) {
                // System.out.println("Sleeping... " + time + " seconds to
                // go...");
                Thread.currentThread().sleep(1000);
            }
        } catch (InterruptedException inEx) {
        }
    }
}
