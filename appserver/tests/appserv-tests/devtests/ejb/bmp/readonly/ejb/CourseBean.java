/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2001-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.s1asdev.ejb.bmp.readonly.ejb;

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

    public String ejbCreate(String courseId, String name) throws CreateException {
        try {
            insertCourse(courseId, name);
        } catch (Exception ex) {
            throw new EJBException("ejbCreate: " + ex.getMessage());
        }

        this.courseId = courseId;
        this.name = name;
        return courseId;
    }

    public String ejbFindByPrimaryKey(String primaryKey) throws FinderException {
        boolean result;

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
                (DataSource) ic.lookup("java:comp/env/jdbc/bmp-readonlyDB");
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
            "insert into ReadOnlyCourse values ( ? , ? )";
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
            "from ReadOnlyCourse where courseid = ? ";
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
            "delete from ReadOnlyCourse  " +
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
            "from ReadOnlyCourse where courseid = ? ";
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
            "update ReadOnlyCourse set name =  ? " +
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
}
