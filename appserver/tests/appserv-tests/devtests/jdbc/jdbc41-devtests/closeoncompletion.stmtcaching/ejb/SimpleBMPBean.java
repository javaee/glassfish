/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.jdbc.stmtcaching.ejb;

import javax.ejb.*;
import javax.naming.*;
import java.sql.*;

public class SimpleBMPBean implements EntityBean {

    protected com.sun.appserv.jdbc.DataSource ds;

    public void setEntityContext(EntityContext entityContext) {
        Context context = null;
        try {
            context = new InitialContext();
            ds = (com.sun.appserv.jdbc.DataSource) context.lookup("java:comp/env/DataSource");
        } catch (NamingException e) {
            throw new EJBException("cant find datasource");
        }
    }

    public Integer ejbCreate() throws CreateException {
        return new Integer(1);
    }

    public boolean testCloseOnCompletion() {
	System.out.println("closeOnCompletion JDBC 41 test Start");
        Connection conn = null;
        PreparedStatement stmt = null;
        String tableName = "customer_stmt_wrapper";
	ResultSet rs = null;
	ResultSet rs1 = null;
        boolean passed = true;
	try {
            conn = ds.getConnection();
            stmt = conn.prepareStatement("select * from "+ tableName +" where c_phone= ?");
	    stmt.setString(1, "shal");
	    stmt.closeOnCompletion();
	    if(!stmt.isCloseOnCompletion()) {
		passed = false;
	    }
	    System.out.println(">>> stmt.isCloseOnCompletion()=" + stmt.isCloseOnCompletion());
	    rs = stmt.executeQuery();
	    rs1 = stmt.executeQuery(); //Got a second rs but not closing it.
	    rs.close();
	    //After this the isClosed should not be true as another resultSet is still open.
	    if(stmt.isClosed()){
		passed = false;
	    }
	    System.out.println("Statement closed=" + stmt.isClosed());
	    rs1.close();
	    //Both the resultSets are closed. At this stage isClosed should be true.
	    if(!stmt.isClosed()){
	        passed = false;
	    }
	    System.out.println("Statement closed=" + stmt.isClosed());
	    try {
	        ResultSet rs2 = stmt.executeQuery();
		rs2.close();
	    } catch(SQLException ex) {
		System.out.println("Statement object used after closeoncompletion : exception: expected");
		passed = true;
	    }
        } catch (Exception e) {
            e.printStackTrace();
            passed = false;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e1) {}
            } 
	}
	System.out.println("closeOnCompletion JDBC 41 test End");
	return passed;
    }
    
    public void ejbLoad() {
    }

    public void ejbStore() {
    }

    public void ejbRemove() {
    }

    public void ejbActivate() {
    }

    public void ejbPassivate() {
    }

    public void unsetEntityContext() {
    }

    public void ejbPostCreate() {
    }
}
