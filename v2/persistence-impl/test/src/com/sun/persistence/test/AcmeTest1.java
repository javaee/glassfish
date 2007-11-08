/*
 * EntityManagerTest.java
 *
 * Copyright 2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package com.sun.persistence.test;

import java.io.PrintWriter;

import javax.persistence.Query;

import com.acme.*;

/**
 * A simple Acme entities test.
 * @author Markus Fuchs
 */
public class AcmeTest1 extends TestBase {

    public static void main(String[] args) {
        out.println("--> AcmeTest1.main()");
	init(args);
	run();
	cleanup();
        out.println("<-- AcmeTest1.main()");
    }

    public static void run() {
        out.println("--> AcmeTest1.run()");

        out.println("tx.begin()...");
        tx.begin();
        assert (tx.isActive());

        boolean error = false;

        try {
            insertValue(1);
            Address e = (Address) findValue(1);
            //e.setStreet("Street==" + 1); // updateValue(1);
            //Address e = printValue(1);
            //e.setStreet("Street==" + 1); // updateValue(1);
            //e = (Address) findValue(1);
            //deleteValue(1, e);
        } catch (Exception ex) {
            System.out.println("Exception: ");
            ex.printStackTrace();
            error = true;
        }

        if (!error) {
            out.println("tx.commit()...");
            tx.commit();
            assert (!tx.isActive());

            out.println("tx.begin()...");
            tx.begin();
            assert (tx.isActive());

            try {
                //printValue(1);
                Address e = (Address) findValue(1);
                e.setStreet("Street==" + 1); // updateValue(1);
            } catch (Exception ex) {
                System.out.println("Exception: ");
                ex.printStackTrace();
                error = true;
            }

            if (!error) {
                out.println("tx.commit()...");
                tx.commit();
               assert (!tx.isActive());

               out.println("tx.begin()...");
               tx.begin();
               assert (tx.isActive());

               try {
                   Address e = (Address) findValue(1);
                   deleteValue(1, e);
               } catch (Exception ex) {
                   System.out.println("Exception: ");
                   ex.printStackTrace();
                   error = true;
               }

               if (!error) {
                   out.println("tx.commit()...");
                   tx.commit();
               } else {
                   out.println("tx.rollback()...");
                   tx.rollback();
               }
            } else {
                out.println("tx.rollback()...");
                tx.rollback();
            }
        } else {
            out.println("tx.rollback()...");
            tx.rollback();
        }
        assert (!tx.isActive());

        out.println("<-- AcmeTest1.run()");
    }

    private static void insertValue(long id) {

        Address e = new Address();
        System.out.println("****** insert : id = " + id);
        e.setStreet("Street+++" + id);
        e.setId(id);
        em.persist(e);
    }

    private static void deleteValue(long id, Object e) {

        System.out.println("****** delete : id = " + id);
        if (e == null)
            e = em.find(Address.class, new Long(id));
        em.remove(e);
    }

    private static Address findValue(long id) {

        System.out.println("****** find : id = " + id);
        Address e = (Address) em.find(Address.class, new Long(id));
        //System.out.println("Street: " + e.getStreet());
        return e;
    }

    private static Address printValue(long id) {
        System.out.println("****** query : id = " + id);
        Query q = em.createQuery("SELECT Object(a) from Address a where a.id = ?1");
        q.setParameter(1, new Long(id));
        java.util.Collection c = q.getResultList();
        java.util.Iterator it = c.iterator();
        Address e = (Address)it.next();
        System.out.println("Street: " + e.getStreet());

        return e;
    }
/*
    private static void insertCompany(int companyId) {

        Company insertCompany = new Company();
        System.out.println("****** insert : companyId = " + companyId);
        insertCompany.setCompanyid(companyId);
        insertCompany.setName("Company - " + companyId);
//        insertCompany.setFounded(new java.util.Date());
        em.persist(insertCompany);
    }

    private static void insertRelationship(
            int deptId, int companyId) {

        Company c = new Company();
        System.out.println("****** insert : companyId = " + companyId);
        c.setCompanyid(companyId);
//        c.setName("Company - " + companyId);
//        c.setFounded(new java.util.Date(0));
        Department d = new Department();
        System.out.println("****** insert : deptId = " + deptId);
        d.setDeptid(deptId);
//        d.setName("Dept - " + deptId);
        d.setCompany(c);
        em.persist(c);
        em.persist(d);
    }
*/

/*
    private static void retrieveDepartment(int deptId) {

            Department.Oid doid = new Department.Oid();
            System.out.println("****** retrieve : deptId = " + deptId);
            Department d = (Department) em.find(doid, true);
            System.out.println("****** retrieved : deptId = " + d.getName());
    }
*/

/*
    private static void updateDepartment(int deptId) {

        Department updateDept = new Department();
        System.out.println("****** update : deptId = " + deptId);
        updateDept.setDeptid(deptId);
        updateDept.setName("Dept - " + deptId);
        em.persist(updateDept);
        em.flush();

        //            Department d = new Department();
        //            Department.Oid oid = new Department.Oid();
        //            oid.deptid = 2;
        //            em.getObjectById(oid, false);
        updateDept.setName("Dept" + deptId + "- Updated Name");
    }

    private static void deleteDepartment(int deptId) {

        Department deleteDept = new Department();
        System.out.println("****** delete : deptId = " + deptId);
        deleteDept.setDeptid(deptId);
        deleteDept.setName("Dept - " + deptId);
        em.persist(deleteDept);
        em.flush();

        em.remove(deleteDept);
    }
*/
}
