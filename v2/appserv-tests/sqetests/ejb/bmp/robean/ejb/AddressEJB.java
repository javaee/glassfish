/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package samples.ejb.bmp.robean.ejb;

import javax.ejb.*;
import javax.naming.*;
import java.sql.*;
import javax.sql.DataSource;

public class AddressEJB implements javax.ejb.EntityBean {
    //database fields
    String SSN;
    String firstName;
    String lastName;
    String address1;
    String address2;
    String city;
    String state;
    String zipCode;
    EntityContext ejbContext = null;
    InitialContext ic = null;
    DataSource dataSource = null;

    public String getName() {
        return firstName + " " + lastName;
    }

    public String getAddress() {
        return address1 + "\n" + address2 + "\n" + city + ", " + state + ", " + zipCode;
    }

    public String getSSN() {
        return this.SSN;
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

    public String ejbFindByPrimaryKey(String SSN) throws FinderException {
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
        //Since this is a ReadOnlyBean, there is no need for an ejbStore
    }

    public void ejbLoad() {
        try {
            Connection conn = null;
            String primaryKey = (String)ejbContext.getPrimaryKey();
            conn = dataSource.getConnection();
            Statement statement = conn.createStatement();
            String query = "SELECT * FROM customer1 where SSN = '" + primaryKey + "'";
            ResultSet results = statement.executeQuery(query);
            if (results.next()) {
                this.SSN = results.getString("SSN");
                this.lastName = results.getString("lastName");
                this.firstName = results.getString("firstName");
                this.address1 = results.getString("address1");
                this.address2 = results.getString("address2");
                this.city = results.getString("city");
                this.state = results.getString("state");
                this.zipCode = results.getString("zipCode");
            } else {
                System.out.println("ERROR!! No entry matching the entered Social Security Number!");
            }
            conn.close();
        } catch (SQLException e) {
            System.out.println("SQLException occurred in ejbLoad() method");
        }   
    }

    public void ejbActivate() {}
    public void ejbPassivate() {}
}
