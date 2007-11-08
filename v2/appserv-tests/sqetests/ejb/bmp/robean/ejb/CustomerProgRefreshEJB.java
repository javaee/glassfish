/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
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
