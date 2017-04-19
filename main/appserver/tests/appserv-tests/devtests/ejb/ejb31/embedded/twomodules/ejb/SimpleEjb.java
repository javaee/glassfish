package org.glassfish.tests.ejb.sample;

import java.util.Collection;
import java.util.Date;

import javax.ejb.Stateless;
import javax.ejb.EJB;
import javax.annotation.security.PermitAll;
import javax.annotation.security.DenyAll;

import javax.persistence.PersistenceContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * @author Jerome Dochez
 */
@Stateless
public class SimpleEjb {

    @PersistenceContext(unitName="test") EntityManager em;

    @EJB SingletonBean singleton;

    @PermitAll
    public String saySomething() {
        return "SingletonBean " + singleton.foo();
    }

    public String bar() {
        return "bar";
    }

    @PermitAll
    public String testJPA() {
        String result = null;
        Query q = em.createNamedQuery("SimpleEntity.findAll");
        Collection entities = q.getResultList();
        int s = entities.size();
        for (Object o : entities) {
            SimpleEntity se = (SimpleEntity)o;
            SimpleRelated sr = se.getRelated();
            System.out.println("Found related: " + ((sr == null)? sr : sr.getName()));
        }

        if (s < 10) {
            System.out.println("Record # " + (s + 1));
            SimpleEntity e = new SimpleEntity("Entity number " + (s + 1) + " created at " + new Date());
            SimpleRelated r = new SimpleRelated("Related to " + (s + 1));
            e.setRelated(r);
            r.setEntity(e);
            em.persist(e);
            result = "Entity number " + (s + 1);
        } else {
            result = "10 entities created";
        }
        return result;

    }
}
