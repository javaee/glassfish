/*
 * TestBase.java
 *
 * Copyright 2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package com.sun.persistence.test;

import java.io.PrintWriter;

import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceContextType;

import com.sun.persistence.runtime.em.impl.EntityManagerFactoryImpl;

/**
 * A base class for persistence tests.
 * @author Martin Zaun
 */
public class TestBase {

    static protected final PrintWriter out = new PrintWriter(System.out, true);

    static protected EntityManagerFactory emf;
    static protected EntityManager em;
    static protected EntityTransaction tx;

    static public void init(String[] args) {
        if (args.length != 4) {
            out.println(" missing arguments: url, driver, user, password");
            return;
        }

        final String url = args[0];
        final String driver = args[1];
        final String user = args[2];
        final String password = args[3];
        out.println("arguments:");
        out.println("    url = " + url);
        out.println("    driver = " + driver);
        out.println("    user = " + user);
        out.println("    password = " + password);
        out.println("    java.class.path = "
		    + System.getProperty("java.class.path"));
        
        emf = new EntityManagerFactoryImpl(url, user, password, driver);
        out.println("emf = " + emf);
        em = emf.createEntityManager(PersistenceContextType.EXTENDED);
        out.println("em = " + em);
        tx = em.getTransaction();
        out.println("tx = " + tx);
        out.println("tx.isActive() = " + tx.isActive());
        assert (!tx.isActive());
        assert (tx.equals(em.getTransaction()));
    }

    static public void cleanup() {
        em.close();
        emf.close();
    }

    static public void run() {
        out.println("--> TestBase.run()");
        out.println("<-- TestBase.run()");
    }

    static public void main(String[] args) {
        out.println("--> TestBase.main()");
	init(args);
	run();
	cleanup();
        out.println("<-- TestBase.main()");
    }
}
