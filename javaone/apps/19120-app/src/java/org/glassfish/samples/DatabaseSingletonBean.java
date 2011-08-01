/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.samples;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.LocalBean;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author arun
 */
@Singleton
@LocalBean
@Startup
public class DatabaseSingletonBean {

    @PersistenceContext EntityManager em;
    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
    @PostConstruct
    private void init() {
        addMovie("Black Swan");
        addMovie("The Matrix");
        addMovie("Inception");
        addMovie("The Fighter");
        addMovie("Iron Man 2");
        addMovie("Invictus");
        addMovie("Up in the Air");
        addMovie("Million Dollar Baby");
    }

    private void addMovie(String name) {
        Movie m = new Movie(name);
        em.persist(m);
    }
}
