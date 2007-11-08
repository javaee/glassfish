/**
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 *
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 */

package com.sun.s1peqe.ejb.bmp.enroller.ejb;

import java.sql.*;
import javax.sql.*;
import java.util.*;
import javax.ejb.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import com.sun.enterprise.config.*;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.server.*;


public class StudentBean implements EntityBean {

    private String studentId;
    private String name;
    private EntityContext context;
    private DataSource dataSource;

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
ex.printStackTrace();
            throw new EJBException("ejbCreate: " + ex.getMessage());
        }

        this.studentId = studentId;
        this.name = name;
	System.out.println("Created Student with ID " + studentId + " and Name " + name);
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
	try {
	    InitialContext ic = new InitialContext();
	    dataSource = 
	      (DataSource) ic.lookup("java:comp/env/jdbc/bmp-enrollerDB");
         } catch (Exception ex) {            
	     ex.printStackTrace();
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
        } catch (Exception ex) {
            throw new EJBException("ejbLoad: " + ex.getMessage());
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
            "insert into student values ( ? , ? )";
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
            "from student where studentid = ? ";
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
            "delete from student  " +
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
            "from student where studentid = ? ";
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
            "update student set name =  ? " +
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

    public String[] getServerHostPort() {
        String orbHost = null, orbPort = null;
	try {
	    ServerContext serverContext = ApplicationServer.getServerContext();
	    
	    ConfigContext configContext = serverContext.getConfigContext();
         	    
            Server serverBean = ServerBeansFactory.getServerBean(configContext);
        	    
            IiopService iiopServiceBean = ServerBeansFactory.getIiopServiceBean(configContext);

	    IiopListener[] iiopListenerBeans = iiopServiceBean.getIiopListener();
	    Orb orbBean = iiopServiceBean.getOrb();
       
	    orbHost = iiopListenerBeans[0].getAddress();
	    orbPort = iiopListenerBeans[0].getPort();
        } catch (Exception cfe) {
            cfe.printStackTrace();
        } 
	return new String[] {orbHost, orbPort};

    }
}
