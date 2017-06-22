package client;

import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.naming.InitialContext;
import javax.transaction.UserTransaction;
import javax.transaction.Status;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import javax.sql.DataSource;

import javax.xml.ws.*;
import endpoint.ejb.*;

public class Client extends HttpServlet {

       @WebServiceRef(name="sun-web.serviceref/HelloEJBService")
       HelloEJBService service;
       @Resource(mappedName="jdbc/__default") private DataSource ds;

       public void doGet(HttpServletRequest req, HttpServletResponse resp) 
		throws javax.servlet.ServletException {
           doPost(req, resp);
       }

       public void doPost(HttpServletRequest req, HttpServletResponse resp)
              throws javax.servlet.ServletException {
            UserTransaction ut = null;
            // Create Table with name CUSTOMER_cm. This name will be used in the EJB
	    String tableName = "CUSTOMER_cm";
	    String nameEntry = "Vikas";
	    String emailEntry= "vikas@sun.com";
            try {
	        Connection con = ds.getConnection();
                // autocommit is made true so that the table is created and dropped immediately
                con.setAutoCommit(true);
	        createTable(con, tableName);
                ut = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
                ut.begin();

                System.out.println(" Service is :" + service);
		Hello port = service.getHelloEJBPort();
                
		String ret = port.sayHello("Appserver Tester !");
                System.out.println("Return value from webservice:"+ret);

                if(ut.getStatus() != Status.STATUS_ACTIVE) {
                    ret += "FAILED";
                } else {
                    System.out.println("**** committing transaction");
                    ut.commit();
                    if(!isDataUpdated(con, tableName, nameEntry, emailEntry)) {
                        ret += "FAILED";
                    }
                }

                PrintWriter out = resp.getWriter();
                resp.setContentType("text/html");
                out.println("<html>");
                out.println("<head>");
                out.println("<title>TestServlet</title>");
                out.println("</head>");
                out.println("<body>");
                out.println("<p>");
                out.println("So the RESULT OF EJB webservice IS :");
                out.println("</p>");
                out.println("[" + ret + "]");
                out.println("</body>");
                out.println("</html>");
		dropTable(con, tableName);
            } catch(Exception e) {
                e.printStackTrace();
	    }
       }

       // use this table in the EJB webservice
       private void createTable(Connection con, String tableName) throws Exception {
	    System.out.println("**** auto commit = " + con.getAutoCommit());
            PreparedStatement pStmt =
            con.prepareStatement("CREATE TABLE "+tableName+" (NAME VARCHAR(30) NOT NULL PRIMARY KEY, EMAIL VARCHAR(30))");
            pStmt.executeUpdate();
       }

       private void dropTable(Connection con, String tableName) throws Exception {
            PreparedStatement pStmt = con.prepareStatement("DROP TABLE "+tableName);
            pStmt.executeUpdate();
       }

       // Check whether the EJB webservice has updated the data in the table.
       private boolean isDataUpdated(Connection con, String tableName, String name, String email) throws Exception {
	    PreparedStatement pStmt = con.prepareStatement("SELECT NAME, EMAIL FROM "+tableName);
	    ResultSet rs = pStmt.executeQuery();
	    try {
	      while(rs.next()) {
		String db_Name  = rs.getString(1);
		String db_Email = rs.getString(2);
		System.out.println("NAME="+db_Name+", EMAIL="+db_Email);
		if(db_Name.equals(name) && db_Email.equals(email))
		    return true;
	      }
	    } finally {
	      rs.close();
	    }
	    return false;
       }
}
