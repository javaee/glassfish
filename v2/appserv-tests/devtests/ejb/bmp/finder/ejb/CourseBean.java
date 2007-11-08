/**
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 *
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 */

package com.sun.s1asdev.ejb.bmp.finder.ejb;

import java.sql.*;
import javax.sql.*;
import java.util.*;
import javax.ejb.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

public class CourseBean implements EntityBean {

    private String courseId;
    private String name;
    private ArrayList studentIds;
    private EntityContext context;
    private EnrollerHome enrollerHome;
    private DataSource dataSource;

    /**
     * Returns an arraylist of StudentIds taking the course.
     * @exception RemoteException
     */
    public ArrayList getStudentIds() {
        return studentIds;
    }

    /**
     * Returns the name of the course.
     * @exception RemoteException
     *
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the course.
     * @exception RemoteException
     *
     */
    public void setName(String name) {
        this.name = name;
    }

    public String ejbCreate(String courseId, String name)
        throws CreateException
    {
        try {
            insertCourse(courseId, name);
        } catch (Exception ex) {
            throw new EJBException("ejbCreate: " + ex.getMessage());
        }

        this.courseId = courseId;
        this.name = name;
        return courseId;
    }

    public void ejbRemove() {
        try {
            deleteCourse(courseId);
        } catch (Exception ex) {
            throw new EJBException("ejbRemove: " + ex.getMessage());
        }
    }

    public void setEntityContext(EntityContext context) {
        this.context = context;
        studentIds = new ArrayList();
        try {
            InitialContext ic = new InitialContext();
            dataSource =
                (DataSource) ic.lookup("java:comp/env/jdbc/bmp-finderDB");
            Context initial = new InitialContext();
            Object objref = initial.lookup("java:comp/env/ejb/Enroller");
            enrollerHome = 
                (EnrollerHome) PortableRemoteObject.narrow(objref,
                                                           EnrollerHome.class);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new EJBException("Unable to connect to database. " +
                                   ex.getMessage());
        }
    }

    public void unsetEntityContext() {
        this.context = null;
    }

    public void ejbActivate() {
        courseId = (String)context.getPrimaryKey();
    }

    public void ejbPassivate() {
        courseId = null;
    }

    public void ejbLoad() {
        try {
            loadCourse();
            loadStudentIds();
        } catch (Exception ex) {
            throw new EJBException("ejbLoad: " + ex.getMessage());
        }
    }

    private void loadStudentIds() {
        studentIds.clear();

        try {
            Enroller enroller = enrollerHome.create();
            ArrayList a = enroller.getStudentIds(courseId);
            studentIds.addAll(a);
        } catch (Exception ex) {
            throw new EJBException("loadStudentIds: " + ex.getMessage());
        }
    }

    public void ejbStore() {
        try {
            storeCourse();
        } catch (Exception ex) {
            throw new EJBException("ejbStore: " + ex.getMessage());
        }
    }

    public void ejbPostCreate(String courseId, String name) { }

    /*********************** Database Routines *************************/

    private void insertCourse (String courseId, String name)
        throws SQLException {
        Connection con =  dataSource.getConnection();
        String insertStatement =
            "insert into FinderCourse values ( ? , ? )";
        PreparedStatement prepStmt =
            con.prepareStatement(insertStatement);

        prepStmt.setString(1, courseId);
        prepStmt.setString(2, name);

        prepStmt.executeUpdate();
        prepStmt.close();
        con.close();
    }

    private boolean selectByPrimaryKey(String primaryKey)
        throws SQLException {

        Connection con =  dataSource.getConnection();
        String selectStatement =
            "select courseid " +
            "from FinderCourse where courseid = ? ";
        PreparedStatement prepStmt =
            con.prepareStatement(selectStatement);
        prepStmt.setString(1, primaryKey);

        ResultSet rs = prepStmt.executeQuery();
        boolean result = rs.next();
        prepStmt.close();
        con.close();
        return result;
    }

    private void deleteCourse(String courseId) throws SQLException {
        Connection con =  dataSource.getConnection();
        String deleteStatement =
            "delete from FinderCourse  " +
            "where courseid = ?";
        PreparedStatement prepStmt =
            con.prepareStatement(deleteStatement);

        prepStmt.setString(1, courseId);
        prepStmt.executeUpdate();
        prepStmt.close();
        con.close();
    }

    private void loadCourse() throws SQLException {
        Connection con =  dataSource.getConnection();
        String selectStatement =
            "select name " +
            "from FinderCourse where courseid = ? ";
        PreparedStatement prepStmt =
            con.prepareStatement(selectStatement);

        prepStmt.setString(1, courseId);

        ResultSet rs = prepStmt.executeQuery();

        if (rs.next()) {
            name = rs.getString(1);
            prepStmt.close();
        } else {
            prepStmt.close();
            throw new NoSuchEntityException("Row for courseId " + courseId +
                                            " not found in database.");
        }
        con.close();
    }

    private void storeCourse() throws SQLException {
        Connection con =  dataSource.getConnection();
        String updateStatement =
            "update FinderCourse set name =  ? " +
            "where courseid = ?";
        PreparedStatement prepStmt =
            con.prepareStatement(updateStatement);

        prepStmt.setString(1, name);
        prepStmt.setString(2, courseId);
        int rowCount = prepStmt.executeUpdate();
        prepStmt.close();

        if (rowCount == 0) {
            throw new EJBException("Storing row for courseId " +
                                   courseId + " failed.");
        }
        con.close();
    }

    private ArrayList selectAllCourses() 
        throws SQLException {

        String selectStatement =
            "select courseid " +
            "from FinderEnrollment ";

        Connection con =  dataSource.getConnection();
        Statement stmt = 
            con.createStatement();

        ResultSet rs = stmt.executeQuery(selectStatement);
        ArrayList a = new ArrayList();

        while (rs.next()) {
            String id = rs.getString(1);
            a.add(id);
        }

        stmt.close();
        con.close();
        return a;
    }

    public ArrayList ejbFindAllCourses()
        throws FinderException
    {
        try {
            return selectAllCourses();
        } catch (Exception sqlEx) {
            return new ArrayList();
        }
    }

    public String ejbFindByPrimaryKey(String primaryKey)
        throws FinderException
    {
        boolean result;

        try {
            // In BMP, can't get timer service or call timer service methods
            TimerService ts = context.getTimerService(); 
            throw new EJBException("should have gotten timer service method " +
                                   " illegal state exception ");
        } catch(IllegalStateException ise) {
            System.out.println("caught expected illegal state exception " +
                               " when trying to access timer service method");
        }

        try {
            result = selectByPrimaryKey(primaryKey);
        } catch (Exception ex) {
            throw new EJBException("ejbFindByPrimaryKey: " +
                                   ex.getMessage());
        }

        if (result) {
            return primaryKey;
        } else {
            throw new ObjectNotFoundException
            ("Row for id " + primaryKey + " not found.");
        }
    }
}
