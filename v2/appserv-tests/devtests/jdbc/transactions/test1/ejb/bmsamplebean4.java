package com.sun.s1asdev.jdbc.transactions.test1.ejb;

import java.util.*;
import java.io.*;
import java.rmi.*;
import javax.ejb.*;
import javax.transaction.UserTransaction;
import javax.naming.*;
import javax.sql.*;
import java.sql.*;

public class bmsamplebean4 implements SessionBean 
{
	EJBContext ejbcontext;
        private transient javax.ejb.SessionContext m_ctx = null;
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

    public bmsamplebean4()
    {
    }

    public int performDBOps()
    {
        java.sql.Connection conn = null,conn2=null;
        Statement stmt = null,stmt2=null;
        ResultSet rs = null,rs2=null;
   try {
	    System.out.println("in bean2 ....");
            InitialContext ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/oracleds2");
      	    System.out.println("ds lookup succeeded");
            conn = ds.getConnection();
            System.out.println("Connection succeeded"+conn);
            stmt = conn.createStatement();
            String query1 = "select * from status1";
	    stmt.executeUpdate("insert into status1 values('bean4',3)");
            rs = stmt.executeQuery(query1);
            while(rs.next())
            {
                System.out.println("Last Name: " + rs.getString("NAME"));
                System.out.println("First Name: " + rs.getInt("num"));
            }
            rs.close();
            stmt.close();
            conn.close();
        } 
        catch (SQLException e){
            e.printStackTrace();
            return 1;
        }
        catch (Exception e)
        {
            e.printStackTrace(); 
            return 1;
        }

        return 0;
    }


}



