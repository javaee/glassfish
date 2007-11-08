/*
 * AcmeTest0.java
 *
 * Copyright 2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package com.sun.persistence.test;

import com.acme.*;

/**
 * Tests some simple insert+find+delete operations on Acme entities.
 * @author Martin Zaun
 */
public class AcmeTest0 extends TestBase {

    static public void main(String[] args) {
        out.println("--> AcmeTest0.main()");
	init(args);
	run();
	cleanup();
        out.println("<-- AcmeTest0.main()");
    }

    static public void run() {
        out.println("--> AcmeTest0.run()");

	// rollback an empty tranaction
        {
	    out.println("tx.begin()...");
	    tx.begin();
	    assert (tx.isActive());

	    out.println("tx.rollback()...");
	    tx.rollback();
	    assert (!tx.isActive());
	}

	// commit an empty tranaction
        {
	    out.println("tx.begin()...");
	    tx.begin();
	    assert (tx.isActive());

	    out.println("tx.commit()...");
	    tx.commit();
	    assert (!tx.isActive());
	}

	// insert+delete, same transaction
	{
	    out.println("tx.begin()...");
	    tx.begin();
	    assert (tx.isActive());

	    Address a = new Address();
	    a.setId(new Long(1));
	    a.setStreet("Addr1");
	    em.persist(a);
	    em.remove(a);

	    out.println("tx.commit()...");
	    tx.commit();
	    assert (!tx.isActive());
	}

	// insert+delete, seperate transactions
	{
	    out.println("tx.begin()...");
	    tx.begin();
	    assert (tx.isActive());

	    Address a = new Address();
	    a.setId(new Long(2));
	    a.setStreet("Addr2");
	    em.persist(a);

	    out.println("tx.commit()...");
	    tx.commit();
	    assert (!tx.isActive());

	    out.println("tx.begin()...");
	    tx.begin();
	    assert (tx.isActive());

	    em.remove(a);

	    out.println("tx.commit()...");
	    tx.commit();
	    assert (!tx.isActive());
	}

	// insert+find+delete, seperate transactions
	{
	    out.println("tx.begin()...");
	    tx.begin();
	    assert (tx.isActive());

	    Address a = new Address();
	    a.setId(new Long(3));
	    a.setStreet("Addr3");
	    em.persist(a);

	    out.println("tx.commit()...");
	    tx.commit();
	    assert (!tx.isActive());

	    out.println("tx.begin()...");
	    tx.begin();
	    assert (tx.isActive());

	    Address a1 = em.find(Address.class, new Long(3));
	    em.remove(a1);

	    out.println("tx.commit()...");
	    tx.commit();
	    assert (!tx.isActive());
	}

        out.println("<-- AcmeTest0.run()");
    }
}
