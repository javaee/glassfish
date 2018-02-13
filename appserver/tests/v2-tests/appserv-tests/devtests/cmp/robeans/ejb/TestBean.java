/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package test;

import javax.ejb.*;
import javax.naming.*;
import javax.sql.DataSource;

import java.sql.*;

import com.sun.jdo.api.persistence.support.PersistenceManagerFactory;

/**
 * This SessionBean is used to test Read-Only CMP beans by inserting
 * initial data into the table and updating column 'NAME' when requested.
 * This allows to use java2db for the actual beans.
 * This bean does not access CMP beans.
 */ 
public class TestBean implements SessionBean {

    private DataSource ds = null;

    // SessionBean methods
 
    public void ejbCreate() throws CreateException {
        System.out.println("TestBean ejbCreate");
        try {
            ds = lookupDataSource();
 
        } catch (NamingException ex) {
            throw new EJBException(ex.getMessage());
        }
    }    
 
    public void ejbActivate() {
        System.out.println("TestBean ejbActivate");
    }    

    public void ejbPassivate() {
            ds = null;
    }

    public void ejbRemove() {

    }
    
    public void setSessionContext(SessionContext sc) {

    }

    /** Look up a DataSource by JNDI name.
     * The JNDI name is expected to be 'jdo/pmf', but it can reference
     * either a PersistenceManagerFactory or a DataSource. In case of
     * a former, the DataSource name will be a ConnectionFactory's name.
     */
    private DataSource lookupDataSource() throws NamingException {
        Context initial = new InitialContext();
        Object objref = initial.lookup("jdo/pmf");

        if (objref instanceof PersistenceManagerFactory) {
            PersistenceManagerFactory pmf = (PersistenceManagerFactory)objref;
            String cfname = pmf.getConnectionFactoryName();

            System.out.println("DATASOURCE NAME: " + cfname);
            objref = initial.lookup(cfname);
        } 
        return (DataSource) objref;
    }    

    /** Insert values via jdbc call */
    public void insertValues (String table_name) {
        String st = "INSERT INTO " + table_name + " VALUES ('" + 
                table_name + "', '" + table_name + "', '" + table_name + "')";
        System.out.println("INSERT STATEMENT: " + st);
        executeStatement(st);

        // Insert another row
        st = "INSERT INTO " + table_name + " VALUES ('" + 
                table_name + "1', '" + table_name + "1', '" + table_name + "1')";
        System.out.println("INSERT STATEMENT: " + st);
        executeStatement(st);
    }

    /** Update values via jdbc call */
    public void updateValues (String table_name) {
        String st = "UPDATE " + table_name + " SET SHORTNAME = 'FOO' WHERE ID = '" + table_name + "'";
        System.out.println("UPDATE STATEMENT: " + st);
        executeStatement(st);
    }

    /** Execute SQL statement.
     * @param st the SQL statement as a String.
     * @throws EJBException to wrap a SQLException.
     */
    private void executeStatement (String st) {
        Connection conn = null;
        try {
            conn = ds.getConnection();
            PreparedStatement ps = conn.prepareStatement(st);
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new EJBException(e.toString());
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
    }

} 
