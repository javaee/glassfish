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

package samples.ejb.subclassing.ejb;

import javax.ejb.*;
import javax.naming.*;
import java.sql.*;
import javax.sql.DataSource;

public class CustomerSavingsEJB extends CustomerEJB
{

  public String ejbFindByPrimaryKey(String SSN) throws FinderException
  {
    System.out.println("In ejbFindbyPrimaryKey method");
    try {
    Connection conn = null;
    conn = dataSource.getConnection();
    System.out.println("Got connection. Conn = " + conn);
    Statement statement = conn.createStatement();
    String query = "SELECT * FROM customer2 where SSN = '" + SSN + "'";
    ResultSet results = statement.executeQuery(query);
    conn.close();
    if (results.next())
    {
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

  public String ejbCreate(String SSN, String lastName, String firstName, String address1, String address2, String city, String state, String zipCode)
  {
    System.out.println("In ejbCreate method");
    System.out.println("Params = " + SSN + ":" + lastName + ":" + firstName + ":" + address1 + ":" + address2 + ":" + city + ":" + state + ":" + zipCode);
    this.SSN = SSN;
    this.lastName = lastName;
    this.firstName = firstName;
    this.address1 = address1;
    this.address2 = address2;
    this.city = city;
    this.state = state;
    this.zipCode = zipCode;

    try {
    Connection conn = null;
      conn = dataSource.getConnection();
      PreparedStatement statement = null;
      statement = conn.prepareStatement(
        "INSERT INTO customer2 " +
        "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
      );
      statement.setString(1, SSN);
      statement.setString(2, lastName);
      statement.setString(3, firstName);
      statement.setString(4, address1);
      statement.setString(5, address2);
      statement.setString(6, city);
      statement.setString(7, state);
      statement.setString(8, zipCode);
      statement.setLong(9, 0);
      statement.setLong(10, 0);
      statement.executeUpdate();
      conn.close();
      } catch (SQLException e) {
        System.out.println("SQL exception occured in ejbCreate method");
        e.printStackTrace();
        return SSN;
      }
    return SSN;
  }

  public void ejbPostCreate(String SSN, String lastName, String firstName, String address1, String address2, String city, String state, String zipCode)
  {
    return;
  }

}
      
    
    
