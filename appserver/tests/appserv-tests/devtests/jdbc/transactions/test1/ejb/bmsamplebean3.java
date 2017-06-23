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



