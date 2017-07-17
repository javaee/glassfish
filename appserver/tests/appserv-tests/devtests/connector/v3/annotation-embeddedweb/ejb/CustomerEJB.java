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

package samples.ejb.subclassing.ejb;

import javax.ejb.*;
import javax.naming.*;
import java.sql.*;
import javax.sql.DataSource;

public class CustomerEJB implements javax.ejb.EntityBean
{
  //database fields
  String SSN;
  String firstName;
  String lastName;
  String address1;
  String address2;
  String city;
  String state;
  String zipCode;
  long savingsBalance;
  long checkingBalance;
  EntityContext ejbContext = null;
  InitialContext ic = null;
  DataSource dataSource = null;

  public static final String SAVINGS = "savings";
  public static final String CHECKING = "checking";

  public String getFirstName()
  {
    return firstName;
  }

  public String getLastName()
  {
    return lastName;
  }

  public String getAddress1()
  {
    return address1; 
  }

  public String getAddress2()
  {
    return address2;
  }

  public String getCity()
  {
    return city;
  }

  public String getState()
  {
    return state;
  }

  public String getZipCode()
  {
    return zipCode;
  }

  public String getSSN()
  {
    return this.SSN;
  }

  public long getSavingsBalance()
  {
    return savingsBalance;
  }

  public long getCheckingBalance()
  {
    return checkingBalance;
  }

  public void doCredit(long amount, String accountType)
  {
    if (accountType.equals(SAVINGS) && amount>0)
      this.savingsBalance = this.savingsBalance + amount + 1;
    else if (accountType.equals(CHECKING))
      this.checkingBalance = this.checkingBalance + amount;
  }

  public void doDebit(long amount, String accountType)
  {
    if (accountType.equals(SAVINGS))
      this.savingsBalance = this.savingsBalance - amount;
    else if (accountType.equals(CHECKING))
      this.checkingBalance = this.checkingBalance - amount;
  }

  public void setEntityContext(EntityContext cntx)
  {
    ejbContext = cntx;
    try {
    ic = new InitialContext();
    dataSource = (DataSource)ic.lookup("java:comp/env/jdbc/SimpleBank");
    } catch (NamingException e) {
      System.out.println("Naming exception occured while trying to lookup the datasource");
      e.printStackTrace();
    }

    try{
        javax.naming.InitialContext ic = new javax.naming.InitialContext();
                DataSource ds = (DataSource)ic.lookup("jdbc/__default");

     }catch(Exception e){
        e.printStackTrace();
     }
  }

  public void unsetEntityContext()
  {
    ejbContext = null;
  }

  public void ejbRemove()
  {
    System.out.println("In ejbRemove method");
    try {
    Connection conn = null;
      conn = dataSource.getConnection();
      String primaryKey = (String)ejbContext.getPrimaryKey();
      Statement statement = conn.createStatement();
      String query = "DELETE FROM customer2 where SSN = '" + primaryKey + "'";
      statement.executeUpdate(query);
      conn.close();
      } catch (SQLException e) {
        System.out.println("SQL exception occured in ejbRemove method");
        e.printStackTrace();
      }
    
  }

  public void ejbStore()
  {
    System.out.println("In ejbStore method");
    System.out.println("SavingsBalance = " + this.savingsBalance);
    System.out.println("CheckingBalance = " + this.checkingBalance);
    try {
    Connection conn = null;
      String primaryKey = (String)ejbContext.getPrimaryKey();
      System.out.println("Primarykey = " + primaryKey);
      conn = dataSource.getConnection();
      PreparedStatement statement = null;
      statement = conn.prepareStatement(
        "UPDATE customer2 " +
        "set lastName = ?, firstName = ?, address1 = ?, address2 = ?, " +
        "city = ?, state = ?, zipCode = ?, savingsBalance = ?, checkingBalance = ? " +
        "where SSN = ?"
      );
      statement.setString(1, this.lastName);
      statement.setString(2, this.firstName);
      statement.setString(3, this.address1);
      statement.setString(4, this.address2);
      statement.setString(5, this.city);
      statement.setString(6, this.state);
      statement.setString(7, this.zipCode);
      statement.setLong(8, this.savingsBalance);
      statement.setLong(9, this.checkingBalance);
      statement.setString(10, primaryKey);
      statement.executeUpdate();
      conn.close();
      } catch (SQLException e) {
        System.out.println("SQL exception occured in ejbStore method");
        e.printStackTrace();
      }

  }

  public void ejbLoad()
  {
    try {
    Connection conn = null;
    String primaryKey = (String)ejbContext.getPrimaryKey();
    conn = dataSource.getConnection();
    Statement statement = conn.createStatement();
    String query = "SELECT * FROM customer2 where SSN = '" + primaryKey + "'";
    ResultSet results = statement.executeQuery(query);
    if (results.next())
    {
      this.SSN = results.getString("SSN");
      this.lastName = results.getString("lastName");
      this.firstName = results.getString("firstName");
      this.address1 = results.getString("address1");
      this.address2 = results.getString("address2");
      this.city = results.getString("city");
      this.state = results.getString("state");
      this.zipCode = results.getString("zipCode");
      this.savingsBalance = results.getLong("savingsBalance");
      this.checkingBalance = results.getLong("checkingBalance");
    } else {
      System.out.println("ERROR!! No entry matching the entered Social Security Number!");
    }
    conn.close();
    } catch (SQLException e) {
      System.out.println("SQLException occurred in ejbLoad() method");
      e.printStackTrace();
    }   
  }

  public void ejbActivate() {}
  public void ejbPassivate() {}
}
      
    
    
