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



