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

package com.sun.s1asdev.jdbc.reconfig.poolauth.ejb;

import javax.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.rmi.*;
import java.util.*;
import java.sql.*;

public class SimpleSessionBean implements SessionBean
{

    private SessionContext ctxt_;
    private InitialContext ic_;
    private DataSource ds;
    public void setSessionContext(SessionContext context) {
        ctxt_ = context;
	try {
	    ic_ = new InitialContext();
        ds = (DataSource)ic_.lookup("java:comp/env/DataSource");
	} catch( NamingException ne ) {
	    ne.printStackTrace();
	}
    }

    public void ejbCreate() throws CreateException {
    }

    public boolean test1() throws Exception {
	//DataSource ds = (DataSource)ic_.lookup("java:comp/env/DataSource");
	Connection conn1 = null;
	Statement stmt1 = null;
	boolean passed = false;
	//Get a connection with user,password = dbuser,dbpassword
	//and access a table that only this principal can
	//access
	try {
	    conn1 = ds.getConnection();
	    stmt1 = conn1.createStatement();
	    stmt1.executeQuery("SELECT * FROM DBUSERTABLE");
	} catch( Exception e) {
	    e.printStackTrace();
	    return false;
	} finally {
	    if (stmt1 != null ) { 
	        try { stmt1.close(); } catch(Exception e) {}
	    }
	    if (conn1 != null) { 
	        try { conn1.close(); } catch( Exception e1 ) {}
	    }
	}

	//Now try querying a table owned by another principal and
	//expect an exception
	try {
	    conn1 = ds.getConnection();
	    stmt1 = conn1.createStatement();
	    stmt1.executeQuery("SELECT * FROM PBPUBLICTABLE");
	} catch( Exception e) {
	    e.printStackTrace();
	    return true;
	} finally {
	    if (stmt1 != null ) { 
	        try { stmt1.close(); } catch(Exception e) {}
	    }
	    if (conn1 != null) { 
	        try { conn1.close(); } catch( Exception e1 ) {}
	    }
	}

	return false;
    }

    public boolean test2() throws Exception {
	//DataSource ds = (DataSource)ic_.lookup("java:comp/env/DataSource");
	Connection conn1 = null;
	Statement stmt1 = null;
	boolean passed = false;
	//Get a connection with user,password = pbpublic,pbpublic
	//and access a table that only this principal can
	//access
	try {
	    conn1 = ds.getConnection();
	    stmt1 = conn1.createStatement();
	    stmt1.executeQuery("SELECT * FROM PBPUBLICTABLE");
	} catch( Exception e) {
	    e.printStackTrace();
	    return false;
	} finally {
	    if (stmt1 != null ) { 
	        try { stmt1.close(); } catch(Exception e) {}
	    }
	    if (conn1 != null) { 
	        try { conn1.close(); } catch( Exception e1 ) {}
	    }
	}

	//Now try querying a table owned by another principal and
	//expect an exception
	try {
	    conn1 = ds.getConnection();
	    stmt1 = conn1.createStatement();
	    stmt1.executeQuery("SELECT * FROM DBUSERTABLE");
	} catch( Exception e) {
	    e.printStackTrace();
	    return true;
	} finally {
	    if (stmt1 != null ) { 
	        try { stmt1.close(); } catch(Exception e) {}
	    }
	    if (conn1 != null) { 
	        try { conn1.close(); } catch( Exception e1 ) {}
	    }
	}

	return false;
    }

    public void ejbLoad() {}
    public void ejbStore() {}
    public void ejbRemove() {}

    public void ejbActivate() {}
    public void ejbPassivate() {}
    public void unsetEntityContext() {}
    public void ejbPostCreate() {}
}
