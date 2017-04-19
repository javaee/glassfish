/**
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package com.sun.s1asdev.ejb.bmp.handle.mix.ejb;

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
    private String dbName = "java:comp/env/jdbc/bmp-handle-mixDB";
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
            makeConnection();
        } catch (Exception ex) {
            throw new EJBException("Unable to connect to database. " +
                                   ex.getMessage());
        }
    }

    public void ejbRemove() {
        try {
            con.close();
        } catch (SQLException ex) {
            throw new EJBException("ejbRemove: " + ex.getMessage());
        }
    }

    public void ejbActivate() {
        try {
            makeConnection();
        } catch (Exception ex) {
            throw new EJBException("ejbActivate Exception: " + ex.getMessage());
        }
    }

    public void ejbPassivate() {
        try {
            con.close();
        } catch (SQLException ex) {
            throw new EJBException("ejbPassivate Exception: " + ex.getMessage());
        } finally {
	    con = null;
	}
    }

    public void setSessionContext(SessionContext context) {
        this.context = context;
    }

    public EnrollerBean() {}

    /*********************** Database Routines *************************/

    private void makeConnection() throws NamingException, SQLException {
        InitialContext ic = new InitialContext();
        DataSource ds = (DataSource) ic.lookup(dbName);
        con =  ds.getConnection();
    }

    private void insertEntry(String studentId, String courseId)
        throws SQLException {

        String insertStatement =
            "insert into HandleEnrollment values ( ? , ? )";
        PreparedStatement prepStmt = 
            con.prepareStatement(insertStatement);

        prepStmt.setString(1, studentId);
        prepStmt.setString(2, courseId);

        prepStmt.executeUpdate();
        prepStmt.close();
    }

    private void deleteEntry(String studentId, String courseId) 
        throws SQLException {

        String deleteStatement =
            "delete from HandleEnrollment " +
            "where studentid = ? and courseid = ?";
        PreparedStatement prepStmt =
            con.prepareStatement(deleteStatement);

        prepStmt.setString(1, studentId);
        prepStmt.setString(2, courseId);
        prepStmt.executeUpdate();
        prepStmt.close();
    }

    private void deleteStudentEntries(String studentId)
        throws SQLException {

        String deleteStatement =
            "delete from HandleEnrollment " +
            "where studentid = ?";
        PreparedStatement prepStmt =
            con.prepareStatement(deleteStatement);

        prepStmt.setString(1, studentId);
        prepStmt.executeUpdate();
        prepStmt.close();
    }

    private void deleteCourseEntries(String courseId)
        throws SQLException {

        String deleteStatement =
            "delete from HandleEnrollment " +
            "where courseid = ?";
        PreparedStatement prepStmt =
            con.prepareStatement(deleteStatement);

        prepStmt.setString(1, courseId);
        prepStmt.executeUpdate();
        prepStmt.close();
    }

    private ArrayList selectStudent(String courseId) 
        throws SQLException {

        String selectStatement =
            "select studentid " +
            "from HandleEnrollment where courseid = ? ";
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
        return a;
    }

    private ArrayList selectCourse(String studentId) 
        throws SQLException {

        String selectStatement =
            "select courseid " +
            "from HandleEnrollment where studentid = ? ";
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
        return a;
    }

    public String testEnrollerHomeHandle() {
        try {
            EnrollerHome enrollerHome = (EnrollerHome) context.getEJBHome();
            HomeHandle homeHandle = enrollerHome.getHomeHandle();
            StringBuffer sbuf = new StringBuffer("BEGIN: testEnrollerHomeHandle");
            HomeHandle retHomeHandle = doHomeHandleTest(homeHandle, sbuf, "testEnrollerHomeHandle");
            if (isSame(homeHandle, retHomeHandle)) {
                sbuf.append("\tVerified Home Handles to be identical");
            } else {
                throw new RuntimeException("Error: Deserialized home handle not same...");
            }
            return sbuf.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public String testEnrollerHandle() {
        try {
            Enroller enroller = (Enroller) context.getEJBObject();
            Handle handle = enroller.getHandle();
            StringBuffer sbuf = new StringBuffer("BEGIN: testEnrollerHandle");
            Handle retHandle = doHandleTest(handle, sbuf, "testEnrollerHandle");
            if (isSame(handle, retHandle)) {
                sbuf.append("\tVerified Handles to be identical");
            } else {
                throw new RuntimeException("Error: Deserialized handle not same...");
            }
            return sbuf.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public String testStudentHomeHandle() {
        try {
            InitialContext initial = new InitialContext();
            Object objref = initial.lookup("java:comp/env/ejb/Student");
            StudentHome studentHome =
                (StudentHome) PortableRemoteObject.narrow(objref, StudentHome.class);
            
            HomeHandle homeHandle = studentHome.getHomeHandle();
            StringBuffer sbuf = new StringBuffer("BEGIN: testStudentHomeHandle");
            HomeHandle retHomeHandle = doHomeHandleTest(homeHandle, sbuf, "testStudentHomeHandle");
            if (isSame(homeHandle, retHomeHandle)) {
                sbuf.append("\tVerified Home Handles to be identical");
            } else {
                throw new RuntimeException("Error: Deserialized home handle not same...");
            }
            return sbuf.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public String testStudentHandle(String studentID) {
        try {
            InitialContext initial = new InitialContext();
            Object objref = initial.lookup("java:comp/env/ejb/Student");
            StudentHome studentHome =
                (StudentHome) PortableRemoteObject.narrow(objref, StudentHome.class);
            
            Student student = studentHome.findByPrimaryKey(studentID);
            Handle handle = student.getHandle();
            StringBuffer sbuf = new StringBuffer("BEGIN: testStudentHandle");
            Handle retHandle = doHandleTest(handle, sbuf, "testStudentHandle");
            if (isSame(handle, retHandle)) {
                sbuf.append("\tVerified Handles to be identical");
            } else {
                throw new RuntimeException("Error: Deserialized handle not same...");
            }
            return sbuf.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private HomeHandle doHomeHandleTest(HomeHandle handle, StringBuffer sbuf, String msg)
        throws Exception
    {
            
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(handle);
        oos.flush();
        sbuf.append("\tWrote Homehandle for: " + msg + "\n");


        byte[] data = bos.toByteArray();
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bis);
        HomeHandle newHandle = (HomeHandle) ois.readObject();
        sbuf.append("\tRead  Homehandle for: " + msg + "\n");

        return newHandle;
    }

    private Handle doHandleTest(Handle handle, StringBuffer sbuf, String msg)
        throws Exception
    {
            
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(handle);
        oos.flush();
        sbuf.append("\tWrote handle for: " + msg + "\n");


        byte[] data = bos.toByteArray();
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bis);
        Handle newHandle = (Handle) ois.readObject();
        sbuf.append("\tRead  handle for: " + msg + "\n");

        return newHandle;
    }

    private boolean isSame(HomeHandle h1, HomeHandle h2) 
        throws RemoteException
    {
        EJBMetaData meta1 = h1.getEJBHome().getEJBMetaData();
        EJBMetaData meta2 = h2.getEJBHome().getEJBMetaData();

        boolean status = true;
        status = status && (meta1.getHomeInterfaceClass() == meta2.getHomeInterfaceClass());
        status = status && (meta1.getRemoteInterfaceClass() == meta2.getRemoteInterfaceClass());
        status = status && (meta1.isSession() == meta2.isSession());
        status = status && (meta1.isStatelessSession() == meta2.isStatelessSession());
        return status;
    }

    private boolean isSame(Handle h1, Handle h2)
        throws RemoteException
    {
        EJBMetaData meta1 = h1.getEJBObject().getEJBHome().getEJBMetaData();
        EJBMetaData meta2 = h2.getEJBObject().getEJBHome().getEJBMetaData();

        boolean status = true;
        status = status && (meta1.getHomeInterfaceClass() == meta2.getHomeInterfaceClass());
        status = status && (meta1.getRemoteInterfaceClass() == meta2.getRemoteInterfaceClass());
        status = status && (meta1.isSession() == meta2.isSession());
        status = status && (meta1.isStatelessSession() == meta2.isStatelessSession());
        return status;
    }

}
