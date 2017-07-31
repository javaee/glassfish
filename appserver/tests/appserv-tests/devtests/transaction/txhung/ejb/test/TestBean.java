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

package com.sun.s1peqe.transaction.txhung.ejb.test;

import javax.ejb.*;
import javax.naming.*;
import javax.ejb.SessionContext;

import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Connection;
import javax.sql.DataSource;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Enumeration;
import java.rmi.RemoteException;
import java.util.*;


public class TestBean implements SessionBean, SessionSynchronization {
    private String user = null;
    private String dbURL1XA = null;
    private String dbURL1NonXA = null;
    private String resource = null;
    private String password = null;
    private SessionContext ctx = null;



    // SessionBean methods
 
    public void ejbCreate() throws CreateException {
	System.out.println("TestBean ejbCreate");

    }    
 
    public void ejbActivate() {
        System.out.println("TestBean ejbActivate");
    }    

    public void ejbPassivate() {
    }

    public void ejbRemove() {

    }
    
    public void setSessionContext(SessionContext sc) {
        System.out.println("setSessionContext in BeanB");
        try {
            ctx = sc;
            Context ic = new InitialContext();
            user = (String) ic.lookup("java:comp/env/user");
            password = (String) ic.lookup("java:comp/env/password");
            dbURL1XA = (String) ic.lookup("java:comp/env/dbURL1-XA");
	    dbURL1NonXA = (String) ic.lookup("java:comp/env/dbURL1-NonXA");
        } catch (Exception ex) {
            System.out.println("Exception in setSessionContext: " +ex.getMessage());
            ex.printStackTrace();
        }


    }


    public boolean testA1(boolean xa) throws CreateException {
	if(xa)
	resource = dbURL1XA;
        else
	resource = dbURL1NonXA;

        System.out.println("Executing the business method testA1");
	return true;

    }


    public void beforeCompletion() {
        System.out.println("in beforeCompletion");
    	Connection con1 = null;
        System.out.println("insert in BeanB");
        try {
            con1 = getConnection(resource);
            Statement stmt1 = con1.createStatement();
	    String acc = "100";
            float bal = 5000;
            stmt1.executeUpdate("INSERT INTO txAccount VALUES ('" + acc + "', " + bal + ")");
            System.out.println("Account added Successfully in "+resource+"...");
            System.out.println("Rolling back the transaction");
	    ctx.setRollbackOnly();
            //return true;
        } catch (Exception ex) {
            System.out.println("Exception in insert: " + ex.toString());
            ex.printStackTrace();
	    //return false;
        } finally {
            try {
                con1.close();
            } catch (java.sql.SQLException ex) {
            }
        }
    }

    public void afterBegin() {
        System.out.println("in afterBegin");
    }

    public void afterCompletion(boolean committed) {
        System.out.println("in afterCompletion");
    }

    private Connection getConnection(String dbURL) {
        Connection con = null;
        System.out.println("getConnection in BeanB");
        try{
            Context context = new InitialContext();
            DataSource ds = (DataSource) context.lookup(dbURL);
            con = ds.getConnection(user, password);
            System.out.println("Got DB Connection Successfully...");
        } catch (Exception ex) {
            System.out.println("Exception in getConnection: " + ex.toString());
            ex.printStackTrace();
        }
        return con;
    }



} 
