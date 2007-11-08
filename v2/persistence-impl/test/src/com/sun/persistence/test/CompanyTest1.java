/*
 * CompanyTest1.java
 *
 * Copyright 2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package com.sun.persistence.test;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;

import javax.persistence.Query;

import com.company.*;

/**
 * A simple Company entities test.
 * @author Markus Fuchs
 */
public class CompanyTest1 extends TestBase {

    public static void main(String[] args) {
        out.println("--> CompanyTest1.main()");
	init(args);
	run();
	cleanup();
        out.println("<-- CompanyTest1.main()");
    }

    public static void run() {
        out.println("--> CompanyTest1.run()");
        Company c = null;
        Department d = null;
        try {
            tx.begin();
            c = newCompany(1);
            d = newDepartment(1);
//            em.flush();
            tx.commit();
            tx.begin();
            d.setCompany(c);
            tx.commit();

        } catch (Throwable ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            ex.printStackTrace();
            System.exit(2);
        }
        out.println("<-- CompanyTest1.run()");
    }

    private static Company newTransientCompany(long companyId) {

        Company c = new Company();
        c.setCompanyid(companyId);
        c.setName("Company - " + companyId);
        c.setFounded(new java.util.Date());
        return c;
    }

    private static Company newCompany(long companyId) {

        Company c = newTransientCompany(companyId);
        System.out.println("****** insert : companyId = " + companyId);
        em.persist(c);
        return c;
    }

    private static Department newTransientDepartment(long deptId) {

        Department d = new Department();
        d.setDeptid(deptId);
        d.setName("Dept - " + deptId);
        return d;
    }

    private static Department newDepartment(long deptId) {

        Department d = newTransientDepartment(deptId);
        System.out.println("****** insert : deptId = " + deptId);
        em.persist(d);
        return d;
    }

    private static Employee newTransientEmployee(int empId) {

        Employee e = new Employee();
        System.out.println("****** insert : empId = " + empId);
        e.setEmpid(empId);
        e.setFirstname("emp - " + empId);
        return e;
    }

    private static Employee newEmployee(int empId) {

        Employee e = newTransientEmployee(empId);
        System.out.println("****** insert : empId = " + empId);
        em.persist(e);
        return e;
    }

    private static Insurance newTransientInsurance(int insId) {

        Insurance i = new Insurance();
        i.setInsid(insId);
        i.setCarrier("ins - " + insId);
        return i;
    }

    private static Insurance newInsurance(int insId) {

        Insurance i = newTransientInsurance(insId);
        System.out.println("****** insert : insId = " + insId);
        em.persist(i);
        return i;
    }

    private static Project newTransientProject(int projId) {

        Project p = new Project();
        p.setProjid(projId);
        p.setName("proj - " + projId);
        return p;
    }

    private static Project newProject(int projId) {

        Project p = newTransientProject(projId);
        System.out.println("****** insert : projId = " + projId);
        em.persist(p);
        return p;
    }

    private static void setRelationship1ToN(
            long deptId, long companyId) {

        Company c = newTransientCompany(companyId);
//        c.setFounded(new java.util.Date(0));
        Department d = newTransientDepartment(deptId);
        d.setCompany(c);
        em.persist(c);
        em.persist(d);
    }

    private static void setRelationship1To1(
            int insId, int empId) {

        Employee e = newTransientEmployee(empId);
//        e.setBirthdate(new java.util.Date(0));
        Insurance i = newTransientInsurance(insId);
        i.setEmployee(e);
//        e.setInsurance(i);
        em.persist(e);
        em.persist(i);
    }

    private static Company retrieveCompany(long companyId) {

        Query q = em.createQuery("SELECT Object(c) from Company c where c.companyid = ?1");
        q.setParameter(1, new Long(companyId));
        java.util.Collection rs = q.getResultList();
        java.util.Iterator it = rs.iterator();
        Company c = (Company)it.next();
        System.out.println("Retrieved company: " + c.getName());
        return c;
    }

    private static Company findCompany(long companyId) {

        return em.find(Company.class, new Long(companyId));
    }

    private static Department findDepartment(long deptId) {

        return em.find(Department.class, new Long(deptId));
    }

    private static void updateCompany(long companyId) {

        Company updateCompany = newCompany(companyId);
        em.flush();

        //            Company d = new Company();
        //            Company.Oid oid = new Company.Oid();
        //            oid.companyId = companyId;
        //            em.getObjectById(oid, false);
        System.out.println("****** update : companyId = " + companyId);
        updateCompany.setName("Company" + companyId + "- Updated Name");
    }

    private static void updateDepartment(long deptId) {

        Department updateDept = newDepartment(deptId);
        em.flush();

        //            Department d = new Department();
        //            Department.Oid oid = new Department.Oid();
        //            oid.deptid = deptId;
        //            em.getObjectById(oid, false);
        System.out.println("****** update : deptId = " + deptId);
        updateDept.setName("Dept" + deptId + "- Updated Name");
    }

    private static void deleteDepartment(long deptId) {

        Department deleteDept = newDepartment(deptId);
        em.flush();

        System.out.println("****** delete : deptId = " + deptId);
        em.remove(deleteDept);
    }

    private static void printCollection(Collection c) {
        for (Iterator iter = c.iterator(); iter.hasNext(); ) {
            printObject(iter.next());
        }
    }

    private static void printObject(Object o) {
        if (o instanceof Employee) {
            Employee emp = (Employee)o;
            System.out.println("Employee(" + emp.getEmpid() + ", "  +
                               emp.getFirstname() + ", " +
                               emp.getLastname() + ")");
        }
        else if (o instanceof Department) {
            Department dept = (Department)o;
            System.out.println("Department(" + dept.getDeptid() + ", "  + dept.getName() + ")");
        }
        else if (o instanceof Insurance) {
            Insurance ins = (Insurance)o;
            System.out.println("Insurance(" + ins.getInsid() + ", "  + ins.getCarrier() + ")");
        }
        else if (o instanceof Project) {
            Project p = (Project)o;
            System.out.println("Project(" + p.getProjid() + ", "  + p.getName() + ")");
        }
        else if (o != null) {
            System.out.println(o.getClass().getName() + "(" + o +")");
        }
        else {
            System.out.println("null");
        }
    }
}
