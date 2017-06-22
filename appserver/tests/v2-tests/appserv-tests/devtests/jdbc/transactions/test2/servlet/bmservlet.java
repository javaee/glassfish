package com.sun.s1asdev.jdbc.transactions.test2.servlet;


import java.util.*;
import java.lang.reflect.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import javax.sql.*;
import com.sun.s1asdev.jdbc.transactions.test2.ejb.*;

 
public class bmservlet extends HttpServlet
{
    public void doGet(HttpServletRequest req, HttpServletResponse res)
                    throws ServletException, IOException
    {
         defaultAction(req, res);
    }
    
    public void doPost(HttpServletRequest req, HttpServletResponse res)
                    throws ServletException, IOException
    {
         defaultAction(req, res);
    }

    public void displayMessage(HttpServletRequest req,
                      HttpServletResponse res,
                      String messageText)
                    throws ServletException, IOException
    {
        res.setContentType("text/html");
        PrintWriter out = res.getWriter();
        out.println(messageText);
    }


    public void defaultAction(HttpServletRequest req, HttpServletResponse res)
                   throws ServletException, IOException
    {
     bmsample1home home = null;
     bmsample1 remote = null;
     res.setContentType("text/plain");
     PrintWriter out = res.getWriter();
     Context ctx;
     Properties p;
	       
     try
      {
   	  Hashtable env = new Hashtable(1);
      env.put("javax.naming.factory.initial", "com.netscape.server.jndi.RootContextFactory");
 	  ctx = new InitialContext(env);
       //TestUtil.init(p); 
      Object objref = ctx.lookup("ejb/bmsamplebean1");
      home = (bmsample1home)PortableRemoteObject.narrow(objref, bmsample1home.class);
      remote = home.create();
      out.println("calling M1 ->DB1");
      out.println(" the result of invoking the ejb method is " + remote.performDBOps());
       out.println("calling M2 ->DB2");
      out.println("...... result of invoking the ejb method is " + remote.performDBOps2());
	   }
       catch (Exception e)
       {
	   System.out.println(" ERROR: " + e);
       }
      try
        {
        javax.sql.DataSource ds,ds2;
        java.sql.Connection conn = null,conn2 = null;
        java.sql.Statement stmt = null,stmt2 = null;
        java.sql.ResultSet rs = null,rs2 = null;
	ctx = new InitialContext();
	ds = (DataSource)ctx.lookup("java:comp/env/jdbc/oraclethird");
	ds2 = (DataSource)ctx.lookup("java:comp/env/jdbc/oracleds2");
        
        conn = ds.getConnection();
	conn2 = ds2.getConnection();
	stmt=conn.createStatement();
	stmt2=conn2.createStatement();
	out.println("..........Verifying table contents ....");
	rs=stmt.executeQuery("select * from status21");
	int count=0;
         while (rs.next())
        {count++;
         out.println("record = "+rs.getString(1));
        }
         rs2=stmt2.executeQuery("select * from status2");
         int count2=0;
        while (rs2.next())
         {count2++;
         out.println("record = "+rs2.getString(1));
         }
         out.println("Total Records in table1 = "+count);
        out.println("Total Records in table2 = "+count2);
	if ((count==1)&&(count2==1))
         out.println("Result:FAIL");
         else
         out.println("Result:PASS");
         stmt.executeUpdate("delete from status21");
         stmt2.executeUpdate("delete from status2");
         conn.commit();
         conn2.commit();  
         rs.close();
         out.println("deleted in 1");
         out.println("deleted in 2");
	 rs2.close();
         stmt.close();
	stmt2.close();

	 conn.close();
	 conn2.close();
        }catch(Exception e){}			      
								      
	  
    }


}
