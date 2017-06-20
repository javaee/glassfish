package com.sun.s1asdev.jdbc.transactions.test1.ejb;

import java.util.*;
import java.io.*;
import java.rmi.*;
import javax.ejb.*;
import javax.transaction.UserTransaction;
import javax.naming.*;
import javax.sql.*;
import java.sql.*;
import javax.rmi.PortableRemoteObject;

public class bmsamplebean3 implements SessionBean 
{
        private transient javax.ejb.SessionContext m_ctx = null;
	EJBContext ejbcontext;
    public void setSessionContext(javax.ejb.SessionContext ctx)
    {
        m_ctx = ctx;
       // m_ctx.setRollbackOnly();
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

    public bmsamplebean3()
    {
    }

    public int performDBOps()
    {
        java.sql.Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        bmsample4home home = null;
        bmsample4 remote = null;
        int resultFromBean4=1;
        try {
	    System.out.println("in bean1....");
            InitialContext ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/oraclethird");
      	    System.out.println("ds lookup succeeded");
            conn = ds.getConnection();
            System.out.println("Connection succeeded"+conn);
            stmt = conn.createStatement();
	    //stmt.executeQuery("delete from status1");
            String query1 = "select * from status1";
	    stmt.executeUpdate("insert into status1 values('bean3',3)");
            rs = stmt.executeQuery(query1);
            while(rs.next())
            {
                System.out.println("Last Name: " + rs.getString("NAME"));
                System.out.println("First Name: " + rs.getInt("num"));
            }
          
           Object objref = ctx.lookup("ejb/bmsamplebean4");
           home = (bmsample4home)PortableRemoteObject.narrow(objref, bmsample4home.class); 
           remote = home.create();
           resultFromBean4 = remote.performDBOps();
          rs.close(); 
           stmt.close();
           conn.close();
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

        return resultFromBean4;
    }



}



