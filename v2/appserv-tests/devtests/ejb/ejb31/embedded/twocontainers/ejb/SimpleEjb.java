package org.glassfish.tests.ejb.sample;

import java.util.Collection;
import java.util.Date;

import javax.ejb.Stateless;

import javax.persistence.PersistenceContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * @author Jerome Dochez
 */
@Stateless
public class SimpleEjb {

    @PersistenceContext(unitName="test") EntityManager em;

    public String saySomething() {
        return "boo";
    }

    public int testJPA() {
        int result = 0;
        Query q = em.createNamedQuery("SimpleEntity.findAll");
        Collection entities = q.getResultList();
        int s = entities.size();
        for (Object o : entities) {
            SimpleEntity se = (SimpleEntity)o;
            System.out.println("Found entity: " + se.getName());
        }

        if (s < 10) {
            System.out.println("Record # " + (s + 1));
            SimpleEntity e = new SimpleEntity("Entity number " + (s + 1) + " created at " + new Date());
            em.persist(e);
            result = (s + 1);
        } else {
            result = 10;
        }
        return result;

    }
}
