package samples.ejb.installed_libraries_embedded.ejb;

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
      
    
    
