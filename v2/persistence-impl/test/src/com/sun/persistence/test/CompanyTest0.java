/*
 * CompanyTest0.java
 *
 * Copyright 2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package com.sun.persistence.test;

import com.company.*;

/**
 * Tests some simple insert+find+delete operations on Company entities.
 * @author Martin Zaun
 */
public class CompanyTest0 extends TestBase {

    static public void main(String[] args) {
        out.println("--> CompanyTest0.main()");
	init(args);
	run();
	cleanup();
        out.println("<-- CompanyTest0.main()");
    }

    static public void run() {
        out.println("--> CompanyTest0.run()");

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

	    Department d = new Department();
	    d.setDeptid(1);
	    d.setName("Dept1");
	    em.persist(d);
	    em.remove(d);

	    out.println("tx.commit()...");
	    tx.commit();
	    assert (!tx.isActive());
	}

	// insert+delete, seperate transactions
	{
	    out.println("tx.begin()...");
	    tx.begin();
	    assert (tx.isActive());

	    Department d = new Department();
	    d.setDeptid(2);
	    d.setName("Dept2");
	    em.persist(d);

	    out.println("tx.commit()...");
	    tx.commit();
	    assert (!tx.isActive());

	    out.println("tx.begin()...");
	    tx.begin();
	    assert (tx.isActive());

	    em.remove(d);

	    out.println("tx.commit()...");
	    tx.commit();
	    assert (!tx.isActive());
	}

	// insert+find+delete, seperate transactions
	{
	    out.println("tx.begin()...");
	    tx.begin();
	    assert (tx.isActive());

	    Department d = new Department();
	    d.setDeptid(3);
	    d.setName("Dept3");
	    em.persist(d);

	    out.println("tx.commit()...");
	    tx.commit();
	    assert (!tx.isActive());

	    out.println("tx.begin()...");
	    tx.begin();
	    assert (tx.isActive());

	    Department d1 = em.find(Department.class, new Long(3));
	    em.remove(d1);

	    out.println("tx.commit()...");
	    tx.commit();
	    assert (!tx.isActive());
	}

        out.println("<-- CompanyTest0.run()");
    }
}
