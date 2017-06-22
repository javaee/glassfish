package com.sun.s1asdev.jdbc.transactions.test2.ejb;


import java.util.*;
import java.io.*;
import java.rmi.*;
import javax.ejb.*;
import javax.transaction.UserTransaction;
import javax.naming.*;
import javax.sql.*;
import java.sql.*;

public class bmsamplebean1 implements SessionBean 
{
	EJBContext ejbcontext;
transient	javax.sql.DataSource ds,ds2;
transient	java.sql.Connection conn = null,conn2=null;
transient    Statement stmt = null,stmt2=null;
transient    ResultSet rs = null,rs2=null;
	

    public void setSessionContext(javax.ejb.SessionContext ctx)
    {
        m_ctx = ctx;
    }

    public void ejbCreate()
    {
    }

    public void ejbRemove() 
    {
    }

    public void ejbActivate() 
    {
    }

    public void ejbPassivate() 
    {
    }

    public bmsamplebean1()
    {
    }

    public int performDBOps()
    {
        try {

	    System.out.println("in method1");
            InitialContext ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/oraclethird");
      	    System.out.println("ds lookup succeeded");
            UserTransaction tx =(UserTransaction)m_ctx.getUserTransaction();
            System.out.println("utx succeeded"+tx); 
            conn = ds.getConnection();
            System.out.println("Connection succeeded"+conn);
	    tx.begin();
            System.out.println("txn status :" +tx.getStatus()); 
            //conn = ds.getConnection();
            //System.out.println("Connection succeeded"+conn);
            stmt = conn.createStatement();
	    //stmt.executeQuery("delete from status21");
	    stmt.executeUpdate("insert into status21 values('method1',3)");
            String query1 = "select * from status21";
            rs = stmt.executeQuery(query1);
            while(rs.next())
            {
                System.out.println("Last Name: " + rs.getString("NAME"));
                System.out.println("First Name: " + rs.getInt("num"));
            }
        } 
        catch (SQLException e)
        {
            System.out.println("SQLException is : " + e);  
           return 1;
        }
        catch (Exception e)
        {
            System.out.println("Exception is : " + e);    
           return 1;
        }

        return 0;
    }


	 public int performDBOps2()
	     {
	   try{
		  InitialContext ctx = new InitialContext();
		  System.out.println("in method2");
		  ds2 = (DataSource) ctx.lookup("java:comp/env/jdbc/oracleds2");
		  System.out.println("second ds lookup");
		  UserTransaction tx =(UserTransaction)m_ctx.getUserTransaction();
		  System.out.println("got second tx"+tx);
		  System.out.println("txn status :" +tx.getStatus());

	          conn2 = ds2.getConnection();
	          System.out.println("Connection succeeded" +conn2);
		  stmt2 = conn2.createStatement();
              //    stmt2.executeQuery("delete from status2");
		  stmt2.executeUpdate("insert into status2 values('method2')");
		  String query1 = "select * from status2";
		  rs2 = stmt2.executeQuery(query1);
		  System.out.println("after rs2");
		   while(rs2.next())
		   {
		      System.out.println("Last Name: " + rs2.getString("NAME"));
		   }
                  System.out.println("txn status :" +tx.getStatus());
	//        Thread.sleep(100000);			
		  System.out.println("txn status :" +tx.getStatus());
	  	  rs.close(); 
	          rs2.close();
		  stmt.close();
	          conn.close();
		  stmt2.close();
		  conn2.close();
		  tx.commit();
		}
		catch (SQLException e)
		 {
                 try
                 {
                 System.out.println("exception " + e.getMessage());
                    e.printStackTrace();
                    System.out.println("error code" +e.getErrorCode());
                    System.out.println("ql state " +e.getSQLState());

               /*   
			e=e.getNextException();
                 System.out.println("exception " + e.getMessage());
                    e.printStackTrace();
                    System.out.println("error code" +e.getErrorCode());
                    System.out.println("ql state " +e.getSQLState());
	        */	    
                    return 1;

		  }catch(Exception ex)
                  {
                  System.out.println("exception " + ex.getMessage());
                  ex.printStackTrace();
                  return 1;
                  }
                 }
		catch (Exception ex)
		 {
		 System.out.println("Exception is : " + ex.getMessage());
                 ex.printStackTrace();
                 return 1;
        }
        return 0;
    }

    private transient javax.ejb.SessionContext m_ctx = null;
}



