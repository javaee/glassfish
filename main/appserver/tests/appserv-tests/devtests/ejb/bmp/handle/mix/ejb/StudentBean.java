/**
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 *
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 */

package com.sun.s1asdev.ejb.bmp.handle.mix.ejb;

import java.sql.*;
import javax.sql.*;
import java.util.*;
import javax.ejb.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

public class StudentBean implements EntityBean {

    private String studentId;
    private String name;
    private ArrayList courseIds;
    private EntityContext context;
    private EnrollerHome enrollerHome;
    private DataSource dataSource;

    /**
     * Returns the CourseIds that a student is enrolled in.
     * @param studentId primary key of the student object
     * @param courseId primary key of the course object
     * @exception RemoteException
     */
    public ArrayList getCourseIds() {
        return courseIds;
    }

    /**
     * Returns the Name of a student.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the Name of a student.
     */
    public void setName(String name) {
        this.name = name;
    }

    public String ejbCreate(String studentId, String name) throws CreateException {
        try {
            insertStudent(studentId, name);
        } catch (Exception ex) {
            throw new EJBException("ejbCreate: " + ex.getMessage());
        }

        this.studentId = studentId;
        this.name = name;
        return studentId;
    }

    public String ejbFindByPrimaryKey(String primaryKey) throws FinderException {
        boolean result;

        try {
            result = selectByPrimaryKey(primaryKey);
        } catch (Exception ex) {
            throw new EJBException("ejbFindByPrimaryKey: " + ex.getMessage());
        }

        if (result) {
            return primaryKey;
        } else {
            throw new ObjectNotFoundException
            ("Row for id " + primaryKey + " not found.");
        }
    }

    public void ejbRemove() {
        try {
            deleteStudent(studentId);
        } catch (Exception ex) {
            throw new EJBException("ejbRemove: " + ex.getMessage());
        }
    }

    public void setEntityContext(EntityContext context) {
        this.context = context;
        courseIds = new ArrayList();
        try {
            InitialContext ic = new InitialContext();
            dataSource = 
                (DataSource) ic.lookup("java:comp/env/jdbc/bmp-handle-mixDB");
            Context initial = new InitialContext();
            Object objref = initial.lookup("java:comp/env/ejb/Enroller");
            enrollerHome =
                (EnrollerHome) PortableRemoteObject.narrow(objref,
                                                           EnrollerHome.class);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new EJBException("setEntityContext: " + ex.getMessage());
        }
    }

    public void unsetEntityContext() {
        this.context = null;
    }

    public void ejbActivate() {
        studentId = (String)context.getPrimaryKey();
    }

    public void ejbPassivate() {
        studentId = null;
    }

    public void ejbLoad() {
        try {
            loadStudent();
            loadCourseIds();
        } catch (Exception ex) {
            throw new EJBException("ejbLoad: " + ex.getMessage());
        }
    }

    private void loadCourseIds() {
        courseIds.clear();
        try {
            Enroller enroller = enrollerHome.create();
            ArrayList a = enroller.getCourseIds(studentId);
            courseIds.addAll(a);
        } catch (Exception ex) {
            throw new EJBException("Exception in loadCourseIds: " +
                                   ex.getMessage());
        }
    }

    public void ejbStore() {
        try {
            storeStudent();
        } catch (Exception ex) {
            throw new EJBException("ejbStore: " + ex.getMessage());
        }
    }

    public void ejbPostCreate(String studentId, String name) { }

    /*********************** Database Routines *************************/

    private void insertStudent (String studentId, String name)
        throws SQLException {

        Connection con = dataSource.getConnection();
        String insertStatement =
            "insert into HandleStudent values ( ? , ? )";
        PreparedStatement prepStmt =
            con.prepareStatement(insertStatement);

        prepStmt.setString(1, studentId);
        prepStmt.setString(2, name);

        prepStmt.executeUpdate();
        prepStmt.close();
        con.close();
    }

    private boolean selectByPrimaryKey(String primaryKey)
        throws SQLException {
        Connection con = dataSource.getConnection();
        String selectStatement =
            "select studentid " +
            "from HandleStudent where studentid = ? ";
        PreparedStatement prepStmt =
            con.prepareStatement(selectStatement);
        prepStmt.setString(1, primaryKey);

        ResultSet rs = prepStmt.executeQuery();
        boolean result = rs.next();
        prepStmt.close();
        con.close();
        return result;
    }

    private void deleteStudent(String studentId) throws SQLException {
        Connection con = dataSource.getConnection();
        String deleteStatement =
            "delete from HandleStudent  " +
            "where studentid = ?";
        PreparedStatement prepStmt =
            con.prepareStatement(deleteStatement);

        prepStmt.setString(1, studentId);
        prepStmt.executeUpdate();
        prepStmt.close();
        con.close();
    }

    private void loadStudent() throws SQLException {
        Connection con = dataSource.getConnection();
        String selectStatement =
            "select name " +
            "from HandleStudent where studentid = ? ";
        PreparedStatement prepStmt =
            con.prepareStatement(selectStatement);

        prepStmt.setString(1, studentId);

        ResultSet rs = prepStmt.executeQuery();

        if (rs.next()) {
            name = rs.getString(1);
            prepStmt.close();
        }
        else {
            prepStmt.close();
            throw new NoSuchEntityException("Row for studentId " + studentId +
                                            " not found in database.");
        }
        con.close();
    }

    private void storeStudent() throws SQLException {
        Connection con = dataSource.getConnection();
        String updateStatement =
            "update HandleStudent set name =  ? " +
            "where studentid = ?";
        PreparedStatement prepStmt =
            con.prepareStatement(updateStatement);

        prepStmt.setString(1, name);
        prepStmt.setString(2, studentId);
        int rowCount = prepStmt.executeUpdate();
        prepStmt.close();

        if (rowCount == 0) {
            throw new EJBException("Storing row for studentId " +
                                   studentId + " failed.");
        }
        con.close();
    }
}
