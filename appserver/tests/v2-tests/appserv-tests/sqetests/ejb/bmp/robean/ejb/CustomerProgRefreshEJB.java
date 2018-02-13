/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2018 Oracle and/or its affiliates. All rights reserved.
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

package samples.ejb.bmp.robean.ejb;

import javax.ejb.*;
import javax.naming.*;
import java.sql.*;
import javax.sql.DataSource;

public class CustomerProgRefreshEJB implements javax.ejb.EntityBean {
    //database fields
    double balance;
    EntityContext ejbContext = null;
    InitialContext ic = null;
    DataSource dataSource = null;

    public double getBalance() {
        return balance;
    }

    public void setEntityContext(EntityContext cntx) {
        ejbContext = cntx;
        try {
            ic = new InitialContext();
            dataSource = (DataSource)ic.lookup("java:comp/env/jdbc/bmp-robean");
        } catch (NamingException e) {
            System.out.println("Naming exception occured while trying to lookup the datasource");
        }
    }

    public void unsetEntityContext() {
        ejbContext = null;
    }

    public PKString1 ejbFindByPrimaryKey(PKString1 SSN) throws FinderException {
        Connection conn = null;
	Statement statement = null;
	ResultSet results = null;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();
            String query = "SELECT * FROM customer1 where SSN = '" + SSN.getPK() + "'";
            results = statement.executeQuery(query);
            if (results.next()) {
                return SSN;
            } else {
                System.out.println("ERROR!! No entry matching the entered Social Security Number!");
                return new PKString1("");
            }
        } catch (SQLException e) {
            System.out.println("SQLException occured in ejbFindbyPrimaryKey method.");
            return new PKString1("");
        } finally {
	    if (results != null) 
		try {
		    results.close();
		} catch (Exception ex) { }
	    if (statement != null) 
		try {
		    statement.close();
		} catch (Exception ex) { }
	    if (conn != null) 
		try {
		    conn.close();
		} catch (Exception ex) { }
	}
    }

    public PKString1 ejbCreate() {
        return null;
    }
    
    public void ejbPostCreate() {       
    }  
    
    public void ejbRemove() {
    }

    public void ejbStore() {
        // No need to implement EJB store since its ROB
    }

    public void ejbLoad() {
        try {
            Connection conn = null;
            PKString1 primaryKey = (PKString1)ejbContext.getPrimaryKey();
            conn = dataSource.getConnection();
            Statement statement = conn.createStatement();
            String query = "SELECT balance FROM customer1 where SSN = '" + primaryKey.getPK() + "'";
            ResultSet results = statement.executeQuery(query);
            if (results.next()) {
                this.balance = results.getDouble("balance");
            } else {
                System.out.println("ERROR!! No entry matching the entered Social Security Number!");
            }
            conn.close();
        } catch (SQLException e) {
            System.out.println("SQLException occurred in ejbLoad() method");
        }   
    }

    public void ejbActivate() {
    }
    public void ejbPassivate() {
    }
}
