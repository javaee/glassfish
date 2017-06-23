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

package com.sun.s1asdev.jdbc.onlygetconnectionservlet.servlet;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.transaction.*;
import javax.sql.*;
import java.sql.*;
import java.io.*;
import javax.naming.InitialContext;

/**
 * Collection of getConnection tests using a servlet
 * 
 * @author aditya.gore@sun.com
 */ 

public class OnlyGetConnectionServlet extends HttpServlet {

    private DataSource ds;
    private PrintWriter out;
    private UserTransaction utx;

    public void doGet( HttpServletRequest req, HttpServletResponse resp ) 
            throws IOException, ServletException
    {
System.out.println(" @@@@ in doGet");    
        out = resp.getWriter();
        writeHeader();

        try {
            InitialContext ctx = new InitialContext();
            ds = (DataSource) ctx.lookup("java:comp/env/jdbc/onlygetconnectionservlet");
            utx = (UserTransaction) ctx.lookup("java:comp/UserTransaction");
        } catch(Exception e) {
            e.printStackTrace( out );
            return;
        }

//        out.println("-----Test1----");
//        test1();
//        out.println("--------------");
//        out.println("-----Test1----");
//        test2();
//        out.println("--------------");
        test2();

        writeFooter();
    }

    private void test1() {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            con = ds.getConnection();
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT * FROM ONLYGETCONNECTION");
            out.println("test1 :: PASSED"); 
        } catch(Exception e) {
            e.printStackTrace( out ); 
            return;
        } finally {
            if ( rs != null ) { try { rs.close(); }catch( Exception e) {} }
            if ( stmt != null ) { try { stmt.close(); }catch( Exception e) {} }
            if ( con != null ) { try { con.close(); }catch( Exception e) {} }
        }
    }

    private void test2() {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            utx.begin();
            con = ds.getConnection();
            try {
                Thread.sleep( 5000 );    
            } catch(Exception e) {
            }
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT * FROM ONLYGETCONNECTION");
            utx.commit();
            out.println("test2 :: PASSED"); 
        } catch(Exception e) {
            e.printStackTrace( out ); 
            return;
        } finally {
            if ( rs != null ) { try { rs.close(); }catch( Exception e) {} }
            if ( stmt != null ) { try { stmt.close(); }catch( Exception e) {} }
            if ( con != null ) { try { con.close(); }catch( Exception e) {} }
        }
    }

    private void writeHeader() {
        out.println( "<html>" );
        out.println( "<head><title>onlygetconnectionservlet results</title></head>");
        out.println( "<body>");
    }

    private void writeFooter() {
        out.println( "</body>");
        out.println( "</html>");
    }
}
