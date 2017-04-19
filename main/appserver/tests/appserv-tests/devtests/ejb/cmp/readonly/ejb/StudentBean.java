/**
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 *
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 */

package com.sun.s1asdev.ejb.cmp.readonly.ejb;

import java.sql.*;
import javax.sql.*;
import java.util.*;
import javax.ejb.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import com.sun.appserv.ejb.ReadOnlyBeanNotifier;
import com.sun.appserv.ejb.ReadOnlyBeanHelper;

public abstract class StudentBean implements EntityBean {

    public abstract String getStudentId();
    public abstract void setStudentId(String studentId);

    public abstract String getName();
    public abstract void setName(String name);

    private EntityContext context;

   /**
     * Returns the Name of a student.
     */
    public String getNameTx() {
        return getName();
    }

    /**
     * Sets the Name of a student.
     */
    public void setName(String name, boolean notify) {
        // Only called for read-write version of Student.
        setName(name);
        if( notify ) {
            try {
                System.out.println("Notifying read-only bean of update to " +
                                   "read-mostly Student " + name);
                ReadOnlyBeanNotifier studentNotifier = 
                    ReadOnlyBeanHelper.getReadOnlyBeanNotifier
                    ("java:comp/env/ejb/ReadOnlyStudent");
                
                // Update read-only version
                studentNotifier.refresh(getStudentId());
            } catch(Exception e) {
                throw new EJBException(e);
            }
        }
    }

    public String ejbCreate(String studentId, String name) throws CreateException {
        setStudentId(studentId);
        setName(name);
        return studentId;
    }

    public void ejbPostCreate(String studentId, String name) throws CreateException {

    }

    public void ejbHomeTestLocalCreate(String pk) {

        StudentLocalHome localHome = (StudentLocalHome)
            context.getEJBLocalHome();

        boolean createSucceeded = false;
        try {
            localHome.create(pk, "mike");
            createSucceeded = true;
        } catch(EJBException ejbex) {
            //
        } catch(CreateException ce) {
            throw new EJBException("unexpected exception");
        }

        if( createSucceeded ) {
            throw new EJBException("cmp read-only bean create should " +
                                   " have thrown an exception");
        } else {
            System.out.println("Successfully caught exception while trying " +
                               " to do a create on a read-only cmp bean");
        }

    }


    public void ejbHomeTestFind(String pk) {

        StudentHome studentHome = (StudentHome)
            context.getEJBHome();

        try {
            Student student = studentHome.findByPrimaryKey(pk);

            long before = System.nanoTime();
            studentHome.findByRemoteStudent(student);
            long after = System.nanoTime();

            studentHome.findByRemoteStudent(student);
            long after2 = System.nanoTime();

            System.out.println("1st query of " + pk + " took " +
                               (after - before) + " nano secs");

            System.out.println("2nd query of " + pk + " took " +
                               (after2 - after) + " nano secs");
            
        } catch(Exception e) {
            e.printStackTrace();
            throw new EJBException(e);
        }
        
    }

    public void ejbHomeTestLocalFind(String pk) {

        StudentLocalHome localHome = (StudentLocalHome)
            context.getEJBLocalHome();

        try {
            StudentLocal student = localHome.findByPrimaryKey(pk);

            long before = System.nanoTime();
            localHome.findByLocalStudent(student);
            long after = System.nanoTime();

            localHome.findByLocalStudent(student);
            long after2 = System.nanoTime();

            System.out.println("1st query of " + pk + " took " +
                               (after - before) + " nano secs");

            System.out.println("2nd query of " + pk + " took " +
                               (after2 - after) + " nano secs");
            
        } catch(Exception e) {
            e.printStackTrace();
            throw new EJBException(e);
        }
        
    }

    public void ejbHomeTestLocalRemove(String pk) {
        
        
        StudentLocalHome localHome = (StudentLocalHome)
            context.getEJBLocalHome();

        boolean removeHomeSucceeded = false;
        try {
            localHome.remove(pk);
            removeHomeSucceeded = true;
        } catch(EJBException e) {
            //
        } catch(RemoveException re) {
            throw new EJBException("unexpected exception");
        }

        if( removeHomeSucceeded ) {
            throw new EJBException("cmp read-only bean Home remove should " +
                                   " have thrown an exception");
        } else {
            System.out.println("Successfully caught exception while trying " +
                               " to do a remove on a read-only cmp Home");
        }
        
        boolean removeSucceeded = false;
        try {
            StudentLocal student = localHome.findByPrimaryKey(pk);
            student.remove();
            removeSucceeded = true;
        } catch(EJBException e) {
            //
        } catch(FinderException fe) {
            throw new EJBException("unexpected exception");
        } catch(RemoveException re) {
            throw new EJBException("unexpected exception");
        }

        if( removeSucceeded ) {
            throw new EJBException("cmp read-only bean remove should " +
                                   " have thrown an exception");
        } else {
            System.out.println("Successfully caught exception while trying " +
                               " to do a remove on a read-only cmp ");
        }

        
    }
    

    public void ejbRemove() {
        System.out.println("StudentBean.ejbRemove called");
    }

    public void setEntityContext(EntityContext context) {
        this.context = context;       
    }

    public void unsetEntityContext() {     
        this.context = null;
    }

    public void ejbActivate() {     
    }

    public void ejbPassivate() {      
    }

    public void ejbLoad() {        
    }

    public void ejbStore() {        
    }
  

    
}
