/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package samples.ejb.bmp.robean.ejb;

import javax.ejb.*;
import javax.naming.*;
import java.sql.*;
import javax.sql.DataSource;

public class CustomerTransactionalEJB implements javax.ejb.EntityBean {
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

    public String ejbFindByPrimaryKey(String SSN) throws FinderException{
        try {
            Connection conn = null;
            conn = dataSource.getConnection();
            Statement statement = conn.createStatement();
            String query = "SELECT * FROM customer1 where SSN = '" + SSN + "'";
            ResultSet results = statement.executeQuery(query);
            conn.close();
            if (results.next()) {
                return SSN;
            } else {
                System.out.println("ERROR!! No entry matching the entered Social Security Number!");
                return "";
            }
        } catch (SQLException e) {
            System.out.println("SQLException occured in ejbFindbyPrimaryKey method.");
            return "";
        }
    }

    public String ejbCreate() {
        return null;
    }
    
    public void ejbPostCreate() {       
    }  

    public void ejbRemove() {
    }

    public void ejbStore() {
    }

    public void ejbLoad() {
        try {
            Connection conn = null;
            String primaryKey = (String)ejbContext.getPrimaryKey();
            conn = dataSource.getConnection();
            Statement statement = conn.createStatement();
            String query = "SELECT balance FROM customer1 where SSN = '" + primaryKey + "'";
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
