package org.glassfish.tests.ejb.sample;

import java.util.Collection;
import java.util.Date;

import javax.ejb.Stateless;
import javax.annotation.sql.DataSourceDefinition;

import javax.persistence.PersistenceContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * @author Jerome Dochez
 */
@DataSourceDefinition(
            name="java:app/jdbc/DB1",
            className="org.apache.derby.jdbc.EmbeddedDataSource",
            portNumber=1527,
            serverName="localhost",
            databaseName="sun-appserv-samples",
            user="APP",
            password="APP",
            properties={"connectionAttributes=;create=true"}
)
@Stateless
public class SimpleEjb {

    @PersistenceContext(unitName="test") EntityManager em;

    public String saySomething() {
        return "boo";
    }

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
