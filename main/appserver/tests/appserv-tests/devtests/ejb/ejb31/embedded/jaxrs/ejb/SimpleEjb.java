package org.glassfish.tests.ejb.sample;

import javax.ejb.*;
import javax.persistence.*;
import javax.ws.rs.*;

import java.util.Collection;
import java.util.Date;

/**
 */
@Stateless
@Local(Simple.class)
@LocalBean
@Path("/test")
public class SimpleEjb {

    @PersistenceContext(unitName="test") EntityManager em;

    @GET
    @Path("/simple")
    @Produces("text/plain")
    public String saySomething() {
        return "boo";
    }

    @GET
    @Path("/jpa")
    @Produces("text/plain")
    public String testJPA() {
        String result = null;
        Query q = em.createNamedQuery("SimpleEntity.findAll");
        Collection entities = q.getResultList();
        int s = entities.size();
        for (Object o : entities) {
            SimpleEntity se = (SimpleEntity)o;
            System.out.println("Found entity: " + ((se == null)? se : se.getName()));
        }

        if (s < 10) {
            System.out.println("Record # " + (s + 1));
            SimpleEntity e = new SimpleEntity("Entity number " + (s + 1) + " created at " + new Date());
            em.persist(e);
            result = "Entity number " + (s + 1);
        } else {
            result = "10 entities created";
        }
        return result;

    }
}
