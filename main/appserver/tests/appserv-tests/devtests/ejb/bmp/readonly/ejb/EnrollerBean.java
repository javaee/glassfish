
/**
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package com.sun.s1asdev.ejb.bmp.readonly.ejb;

import java.rmi.RemoteException; 
import javax.ejb.*;
import java.sql.*;
import javax.sql.*;
import java.util.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import java.io.*;

public class EnrollerBean implements SessionBean {
 
    private Connection con;
    private String dbName = "java:comp/env/jdbc/bmp-readonlyDB";
    private DataSource ds;
    private SessionContext context;

    /**
     * Enrolls a Student in a course
     * @param studentId primary key of the student object
     * @param courseId primary key of the course object
     * @exception RemoteException
     */

    public void enroll(String studentId, String courseId) {
        try {
            insertEntry(studentId, courseId);
        } catch (Exception ex) {
            throw new EJBException("enroll: " + ex.getMessage());
        }
    }

    /**
     * Un-Enrolls a Student in a course
     * @param studentId primary key of the student object
     * @param courseId primary key of the course object
     * @exception RemoteException
     */
    public void unEnroll(String studentId, String courseId) {
        try {
            deleteEntry(studentId, courseId);
        } catch (Exception ex) {
            throw new EJBException("unEnroll: " + ex.getMessage());
        }
    }

    /**
     * Deletes a Student 
     * @param studentId primary key of the student object
     * @exception RemoteException
     */
    public void deleteStudent(String studentId) {
        try {
            deleteStudentEntries(studentId);
        } catch (Exception ex) {
            throw new EJBException("deleteStudent: " + ex.getMessage());
        }
    }

    /**
     * Deletes a Course 
     * @param courseId primary key of the course object
     * @exception RemoteException
     */
    public void deleteCourse(String courseId) {
        try {
            deleteCourseEntries(courseId);
        } catch (Exception ex) {
            throw new EJBException("deleteCourse: " + ex.getMessage());
        }
    }

    /**
     * Returns an Arraylist of StudentsIds enrolled in a course
     * @param courseId primary key of the course object
     * @exception RemoteException
     */
    public ArrayList getStudentIds(String courseId) {
        try {
            return selectStudent(courseId);
        } catch (Exception ex) {
            throw new EJBException("getStudentIds: " + ex.getMessage());
        }
    }

    /**
     * Return an ArrayList of CourseIds that student is enroller in
     * @param studentId primary key of the student object
     * @exception RemoteException
     */
    public ArrayList getCourseIds(String studentId) {
        try {
            return selectCourse(studentId);
        } catch (Exception ex) {
            throw new EJBException("getCourseIds: " + ex.getMessage());
        }
    }

    public void ejbCreate() {
        try {
           InitialContext ic = new InitialContext();
           ds = (DataSource) ic.lookup(dbName);
        } catch (Exception ex) {
            throw new EJBException("Unable to connect to database. " +
                                   ex.getMessage());
        }
    }

    public void ejbRemove() {
    }

    public void ejbActivate() {
        
    }

    public void ejbPassivate() {
       
    }

    public void setSessionContext(SessionContext context) {
        this.context = context;
    }

    public EnrollerBean() {}

    /*********************** Database Routines *************************/   

    private void insertEntry(String studentId, String courseId)
        throws SQLException {
        Connection con = ds.getConnection();
        String insertStatement =
            "insert into ReadOnlyEnrollment values ( ? , ? )";
        PreparedStatement prepStmt = 
            con.prepareStatement(insertStatement);

        prepStmt.setString(1, studentId);
        prepStmt.setString(2, courseId);

        prepStmt.executeUpdate();
        prepStmt.close();
        con.close();
    }

    private void deleteEntry(String studentId, String courseId) 
        throws SQLException {

        Connection con = ds.getConnection();
        String deleteStatement =
            "delete from ReadOnlyEnrollment " +
            "where studentid = ? and courseid = ?";
        PreparedStatement prepStmt =
            con.prepareStatement(deleteStatement);

        prepStmt.setString(1, studentId);
        prepStmt.setString(2, courseId);
        prepStmt.executeUpdate();
        prepStmt.close();
        con.close();
    }

    private void deleteStudentEntries(String studentId)
        throws SQLException {

        Connection con = ds.getConnection();
        String deleteStatement =
            "delete from ReadOnlyEnrollment " +
            "where studentid = ?";
        PreparedStatement prepStmt =
            con.prepareStatement(deleteStatement);

        prepStmt.setString(1, studentId);
        prepStmt.executeUpdate();
        prepStmt.close();
        con.close();
    }

    private void deleteCourseEntries(String courseId)
        throws SQLException {

        Connection con = ds.getConnection();
        String deleteStatement =
            "delete from ReadOnlyEnrollment " +
            "where courseid = ?";
        PreparedStatement prepStmt =
            con.prepareStatement(deleteStatement);

        prepStmt.setString(1, courseId);
        prepStmt.executeUpdate();
        prepStmt.close();
        con.close();
    }

    private ArrayList selectStudent(String courseId) 
        throws SQLException {

        Connection con = ds.getConnection();
        String selectStatement =
            "select studentid " +
            "from ReadOnlyEnrollment where courseid = ? ";
        PreparedStatement prepStmt = 
            con.prepareStatement(selectStatement);

        prepStmt.setString(1, courseId);
        ResultSet rs = prepStmt.executeQuery();
        ArrayList a = new ArrayList();

        while (rs.next()) {
            String id = rs.getString(1);
            a.add(id);
        }

        prepStmt.close();
        con.close();
        return a;
    }

    private ArrayList selectCourse(String studentId) 
        throws SQLException {

        Connection con = ds.getConnection();
        String selectStatement =
            "select courseid " +
            "from ReadOnlyEnrollment where studentid = ? ";
        PreparedStatement prepStmt = 
            con.prepareStatement(selectStatement);

        prepStmt.setString(1, studentId);
        ResultSet rs = prepStmt.executeQuery();
        ArrayList a = new ArrayList();

        while (rs.next()) {
            String id = rs.getString(1);
            a.add(id);
        }

        prepStmt.close();
        con.close();
        return a;
    }

    public boolean canGetReadOnlyBeanNotifier(boolean testNewNotifier) {
        boolean status;
        if (testNewNotifier) {
            com.sun.appserv.ejb.ReadOnlyBeanNotifier
             notifier = com.sun.appserv.ejb.ReadOnlyBeanHelper.
                  getReadOnlyBeanNotifier("java:comp/env/ejb/Student");

            status = (notifier != null);
        } else {
            status = false; // should not be called
        }
        return status;
    }


    public boolean canGetReadOnlyBeanLocalNotifier(boolean testNewNotifier) {
        boolean status;
        if (testNewNotifier) {
            com.sun.appserv.ejb.ReadOnlyBeanLocalNotifier
             notifier = com.sun.appserv.ejb.ReadOnlyBeanHelper.
                  getReadOnlyBeanLocalNotifier("java:comp/env/ejb/StudentLocal");

            status = (notifier != null);
        } else {
            status = false; // should not be called
        }
        return status;
    }
    
    public boolean testReadOnlyBeanLocalCreate(String studentId, String name) {
        boolean status = false;

        try {
            StudentLocalHome studentLocalHome = (StudentLocalHome)
                new InitialContext().lookup("java:comp/env/ejb/StudentLocal");

            studentLocalHome.create(studentId, name);
            System.out.println("Error.  Should have gotten CreateException in"
                               + " testReadOnlyBeanLocalCreate");            
        } catch(CreateException ce) {
            System.out.println("Successfully got CreateException when " +
                               "attempting to create a read-only bean");
            status = true;
        } catch(Exception e) {
            System.out.println("Got unexpected exception in " + 
                               "testReadOnlyBeanLocalCreate");
            e.printStackTrace();
        }
        return status;
    }


    public boolean testReadOnlyBeanStudentRefresh(String studentId,
            boolean testNewNotifier)
    {
        boolean status = false;
        try {
            if (testNewNotifier) {
                com.sun.appserv.ejb.ReadOnlyBeanNotifier
                notifier = com.sun.appserv.ejb.ReadOnlyBeanHelper.
                    getReadOnlyBeanNotifier("java:comp/env/ejb/Student");
    
                notifier.refresh(studentId);
                status = true;
            } else {
                status = false; // should not be called
            }
        } catch (Exception ex) {
            System.err.println("******* testReadOnlyBeanStudentRefresh ****");
            ex.printStackTrace(); 
            System.err.println("******* testReadOnlyBeanStudentRefresh ****");
        }

        return status;
    }

    public boolean testReadOnlyBeanLocalStudentRefresh(String studentId,
            boolean testNewNotifier)
    {
        boolean status = false;
        try {
            if (testNewNotifier) {

                com.sun.appserv.ejb.ReadOnlyBeanLocalNotifier
                notifier = com.sun.appserv.ejb.ReadOnlyBeanHelper.
                    getReadOnlyBeanLocalNotifier("java:comp/env/ejb/StudentLocal");
    
                notifier.refresh(studentId);
                status = true;
            } else {
                status = false; // should not be called
            }
        } catch (Exception ex) {
            System.err.println("******* testReadOnlyBeanLocalStudentRefresh ****");
            ex.printStackTrace(); 
            System.err.println("******* testReadOnlyBeanLocalStudentRefresh ****");
        }

        return status;
    }
}
