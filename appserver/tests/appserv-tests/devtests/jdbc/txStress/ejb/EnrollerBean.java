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

package com.sun.s1peqe.ejb.bmp.enroller.ejb;

import java.rmi.RemoteException; 
import javax.ejb.*;
import java.sql.*;
import javax.sql.*;
import javax.transaction.*;
import java.util.*;
import javax.naming.*;

public class EnrollerBean implements SessionBean {
 
    private DataSource ds;
    private DataSource ds_container;
    private DataSource ds_app;
    private String dbName = "java:comp/env/jdbc/bmp-enrollerDB";
    private String dbName_container = "java:comp/env/jdbc/bmp-enrollerDB-container";
    private String dbName_app = "java:comp/env/jdbc/bmp-enrollerDB-application";
    private SessionContext context;

    private String status = null;

    /**
     * Return an ArrayList of CourseIds that student is enroller in
     * @param studentId primary key of the student object
     * @exception RemoteException
     */
    public String doTest (String threadId) {
        this.status = threadId;
        try {
            doActualTest(threadId);
        } catch (Exception ex) {
            ex.printStackTrace();
            status = status + "Test Failed";
            //throw new EJBException("doTest: " + ex.getMessage());
        }
        return status;
    }

    private void doActualTest(String id) throws Exception{
        test1(id);
        test2(id);
        test3(id);
        test4(id); 
        test5(id);
        test6(id); 
        //test7(id); 
    }

    // Tests a tx and verifies that work is actually committed.
    // Clears the data for next cycle.
    private void test1(String id) throws Exception {

        UserTransaction t = context.getUserTransaction();
        t.begin();

        Connection con = ds.getConnection();
        for (int i=0; i < 5; i++) {
            insertEntry(con, id + i, i, id + ":" + i);   
        }
        con.close();

        Connection con1 = ds.getConnection();
        for (int i=0; i < 5; i++ ) {
            updateEntry(con1, id + i, i, id + " updated :" + i );
        }
        con1.close();

        t.commit();


        Connection con2 = ds.getConnection();
        for (int i=0; i < 5; i++) {
            String val = selectValue(con2, id + i, i);
            if (val.equals(id + " updated :" + i)) {
                out("test1." + val);
            } else {
                throw new Exception("Failed while getting value");
            }
        }
        con2.close();

        Connection con3 = ds.getConnection();
        for (int i=0; i < 5; i++) {
            deleteEntry(con3, id + i, i);
        }
        con3.close();
        status = status + "Test1 Passed";
        //System.out.println(status);
    }

    // Tests a tx and verifies that work is actually rolled back.
    // Clears the data for next cycle.
    private void test2(String id) throws Exception {
        UserTransaction t = context.getUserTransaction();
        t.begin();

        Connection con = ds.getConnection();
        for (int i=0; i < 5; i++) {
            insertEntry(con, id + i, i, id + ":" + i);   
        }
        con.close();

        Connection con1 = ds.getConnection();
        for (int i=0; i < 5; i++ ) {
            updateEntry(con1, id + i, i, id + " updated :" + i );
        }
        con1.close();

        t.rollback();


        Connection con2 = ds.getConnection();
        for (int i=0; i < 5; i++) {
            String val = selectValue(con2, id + i, i);
            if (val.equals("NOVALUE")) {
                out("test2."+val);
            } else {
                throw new Exception("Got value : " + val);
            }
        }
        con2.close();

        status = status + "Test2 Passed";
        //System.out.println(status);

    }

    // Tests a tx and verifies that work is actually committed.
    // Clears the data for next cycle.
    private void test3(String id) throws Exception {
        UserTransaction t = context.getUserTransaction();

        Connection con = ds.getConnection();
        Connection con1 = ds.getConnection();

        t.begin();
        for (int i=0; i < 5; i++) {
            insertEntry(con, id + i, i, id + ":" + i);   
        }
        con.close();

        for (int i=0; i < 5; i++ ) {
            updateEntry(con1, id + i, i, id + " updated :" + i );
        }
        con1.close();

        t.commit();

        Connection con2 = ds.getConnection();
        for (int i=0; i < 5; i++) {
            String val = selectValue(con2, id + i, i);
            if (val.equals(id + " updated :" + i)) {
                out("test1." + val);
            } else {
                throw new Exception("Failed while getting value");
            }
        }
        con2.close();

        Connection con3 = ds.getConnection();
        for (int i=0; i < 5; i++) {
            deleteEntry(con3, id + i, i);
        }
        con3.close();
        status = status + "Test3 Passed";
        //System.out.println(status);
    }

    // Tests a tx and verifies that work is actually rolled back.
    // Clears the data for next cycle.
    private void test4(String id) throws Exception {
        UserTransaction t = context.getUserTransaction();
        Connection con = ds.getConnection();
        t.begin();

        for (int i=0; i < 5; i++) {
            insertEntry(con, id + i, i, id + ":" + i);   
        }

        Connection con1 = ds.getConnection();
        for (int i=0; i < 5; i++ ) {
            updateEntry(con1, id + i, i, id + " updated :" + i );
        }

        con.close();

        t.rollback();

        for (int i=0; i < 5; i++) {
            String val = selectValue(con1, id + i, i);
            if (val.equals("NOVALUE")) {
                out("test2."+val);
            } else {
                throw new Exception("Got value : " + val);
            }
        }
        con1.close();
        status = status + "Test4 Passed";
        //System.out.println(status);
    }

    // Tests a tx and verifies that work is actually committed.
    // Clears the data for next cycle.
    private void test5(String id) throws Exception {
        UserTransaction t = context.getUserTransaction();

        t.begin();

        Connection con = ds.getConnection();
        for (int i=0; i < 5; i++) {
            insertEntry(con, id + i, i, id + ":" + i);   
        }

        Connection con1 = ds.getConnection();
        for (int i=0; i < 5; i++ ) {
            updateEntry(con1, id + i, i, id + " updated :" + i );
        }

        t.commit();

        con.close();

        for (int i=0; i < 5; i++) {
            String val = selectValue(con1, id + i, i);
            if (val.equals(id + " updated :" + i)) {
                out("test1." + val);
            } else {
                throw new Exception("Failed while getting value");
            }
        }
        con1.close();

        Connection con3 = ds.getConnection();
        for (int i=0; i < 5; i++) {
            deleteEntry(con3, id + i, i);
        }
        con3.close();
        status = status + "Test5 Passed";
        //System.out.println(status);
    }

    // Tests a tx and verifies that work is actually committed.
    // Clears the data for next cycle.
    private void test6(String id) throws Exception {

        UserTransaction t = context.getUserTransaction();
        t.begin();

        System.out.println("con.getConnection");

        Connection con = ds.getConnection();
        System.out.println("Got conn");
        for (int i=0; i < 5; i++) {
            insertEntry(con, id + i, i, id + ":" + i);   
        }
        System.out.println("close conn");
        con.close();

        System.out.println("con1.getConnection");

        Connection con1 = ds.getConnection();
        for (int i=0; i < 5; i++ ) {
            updateEntry(con1, id + i, i, id + " updated :" + i );
        }
        con1.close();

        t.commit();

        System.out.println("con2.getConnection");

        Connection con2 = ds.getConnection();
        for (int i=0; i < 5; i++) {
            String val = selectValue(con2, id + i, i);
            if (val.equals(id + " updated :" + i)) {
                out("test1." + val);
            } else {
                throw new Exception("Failed while getting value");
            }
        }
        con2.close();

        System.out.println("con4.getConnection");
        Connection con4 = ds_container.getConnection();
        boolean pass = false;
        try {
            String val = selectValue(con4, ""+1, 1);
        } catch (SQLException e) {
            pass = true;
            e.printStackTrace();
        }
        con4.close();

        System.out.println("con3.getConnection");
        Connection con3 = ds_app.getConnection("scott", "tiger");
        for (int i=0; i < 5; i++) {
            deleteEntry(con3, id + i, i);
        }
        con3.close();

        if (pass) {
            status = status + "Test6 Passed";
        } else {
            status = status + "Test6 Failed";
        }
        //System.out.println(status);
    }

    // Tests a tx and verifies that work is actually rolled back.
    // Clears the data for next cycle.
    private void test7(String id) throws Exception {
        UserTransaction t = context.getUserTransaction();
        t.begin();

        Connection con = ds.getConnection();
        for (int i=0; i < 5; i++) {
            insertEntry(con, id + i, i, id + ":" + i);   
        }
        con.close();

        boolean pass = false;
        Connection con4 = null; 
        System.out.println("con4.getConnection");
        try {
            con4 = ds_container.getConnection();
            String val = selectValue(con4,""+1, 1);
        } catch (Exception e) {
            pass = true;
            e.printStackTrace();
            //con4.close();
        }
        System.out.println("Rolling back");

        t.rollback();


        Connection con2 = ds.getConnection();
        for (int i=0; i < 5; i++) {
            String val = selectValue(con2, id + i, i);
            if (val.equals("NOVALUE")) {
                out("test2."+val);
            } else {
                throw new Exception("Got value : " + val);
            }
        }
        con2.close();

        if (pass) {
            status = status + "Test7 Passed";
        } else {
            status = status + "Test7 Failed";
        }
        //System.out.println(status);

    }

    public int verifyTest() {
       int result = -1;
        try {
            Connection con = ds.getConnection();
            String selectStatement =
                "select count(*) " +
                "from testTx ";
            PreparedStatement prepStmt = 
                con.prepareStatement(selectStatement);

            ResultSet rs = prepStmt.executeQuery();

            if (rs.next()) {
                result = rs.getInt(1);
            }
            prepStmt.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public void ejbCreate() {
    }

    public void ejbRemove() {
    }

    public void ejbActivate() {
    }

    public void ejbPassivate() {
    }

    public void setSessionContext(SessionContext context) {
        try {
            this.context = context;
            InitialContext ic = new InitialContext();
            ds = (DataSource) ic.lookup(dbName);
            ds_container = (DataSource) ic.lookup(dbName_container);
            ds_app = (DataSource) ic.lookup(dbName_app);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public EnrollerBean() {}

    /*********************** Database Routines *************************/

    private void insertEntry(Connection con, String key, int iterkey, String value)
        throws SQLException {

        String insertStatement =
            "insert into testTx values ( ? , ? , ?)";
        PreparedStatement prepStmt = 
            con.prepareStatement(insertStatement);

        prepStmt.setString(1, key);
        prepStmt.setInt(2, iterkey);
        prepStmt.setString(3, value);

        prepStmt.executeUpdate();
        prepStmt.close();
    }

    private void deleteEntry(Connection con, String key, int iterkey) 
        throws SQLException {

        String deleteStatement =
            "delete from testTx " +
            "where key = ? and iterkey = ?";
        PreparedStatement prepStmt =
            con.prepareStatement(deleteStatement);

        prepStmt.setString(1, key);
        prepStmt.setInt(2, iterkey);
        prepStmt.executeUpdate();
        prepStmt.close();
    }

    private void updateEntry(Connection con, String key, int iterkey, String value) 
        throws SQLException {

        String deleteStatement =
            "update testTx " +
            "set value = ? " +
            " where key = ? and iterkey = ?";
        PreparedStatement prepStmt =
            con.prepareStatement(deleteStatement);

        prepStmt.setString(1, value);
        prepStmt.setString(2, key);
        prepStmt.setInt(3, iterkey);
        prepStmt.executeUpdate();
        prepStmt.close();
    }

    private String selectValue(Connection con, String key, int iterkey) 
        throws SQLException {

        String selectStatement =
            "select value " +
            "from testTx where key = ? and iterkey = ?";
        PreparedStatement prepStmt = 
            con.prepareStatement(selectStatement);

        prepStmt.setString(1, key);
        prepStmt.setInt(2, iterkey);
        ResultSet rs = prepStmt.executeQuery();
        String result = "NOVALUE";

        if (rs.next()) {
            result = rs.getString(1);
        }

        if (rs.next()) {
            result = "ERROR";
        }

        prepStmt.close();
        return result;
    }


    private void out(String s) {
        //System.out.println(""+s);
    }
}
