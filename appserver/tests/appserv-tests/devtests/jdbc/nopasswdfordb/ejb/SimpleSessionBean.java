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

package com.sun.s1asdev.jdbc.nopasswdfordb.ejb;

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
    public void setSessionContext(SessionContext context) {
        ctxt_ = context;
	try {
	    ic_ = new InitialContext();
	} catch( NamingException ne ) {
	    ne.printStackTrace();
	}
    }

    public void ejbCreate() throws CreateException {
    }

    /**
     * Lookup a datasource with resource-ref specifying an
     * empty password in default-resource-principal
     * <resource-ref>
     *   <default-resource-principal>
     *     <name>DBUSER</name>
     *     <password></password>
     *   </default-resource-principal>
     * </resource-ref>
     * 
     * Note that the pool still has a valid password set in it
     */
    public boolean test1() throws Exception {
	DataSource ds = (DataSource)ic_.lookup("java:comp/env/DataSource");
	Connection conn1 = null;
	boolean passed = false;
	//clean the database
	try {
	    conn1 = ds.getConnection();
	} catch( Exception e) {
	    e.printStackTrace();
	    return true;
	} finally {
	    if (conn1 != null) { 
	        try { conn1.close(); } catch( Exception e1 ) {}
	    }
	}
	
	return false;
    }

    /**
     * Lookup a datasource without resource-ref specifying 
     * password in default-resource-principal
     * <resource-ref>
     *   <default-resource-principal>
     *     <name>DBUSER</name>
     *   </default-resource-principal>
     * </resource-ref>
     * 
     * Note that the pool still has a valid password set in it
     */

    public boolean test2() throws Exception {
    	DataSource ds = (DataSource)ic_.lookup("java:comp/env/DataSource1");
	Connection conn1 = null;
	boolean passed = false;
	//clean the database
	try {
	    conn1 = ds.getConnection();
	} catch( Exception e) {
	    e.printStackTrace();
	    return true;
	} finally {
	    if (conn1 != null) { 
	        try { conn1.close(); } catch( Exception e1 ) {}
	    }
	}
	
	return false;
    }

    /**
     * Lookup a datasource with resource-ref specifying 
     * password in default-resource-principal but no password
     * in the pool
     * <resource-ref>
     *   <default-resource-principal>
     *     <name>DBUSER</name>
     *     <password>DBPASSWORD</password>
     *   </default-resource-principal>
     * </resource-ref>
     * 
     * Note that the pool has no password set in it
     */

    public boolean test3() throws Exception {
    	DataSource ds = (DataSource)ic_.lookup("java:comp/env/DataSource2");
	Connection conn1 = null;
	boolean passed = false;
	//clean the database
	try {
	    conn1 = ds.getConnection();
	} catch( Exception e) {
	    e.printStackTrace();
	    return false;
	} finally {
	    if (conn1 != null) { 
	        try { conn1.close(); } catch( Exception e1 ) {}
	    }
	}
	
	return true;
    }

    /**
     * Lookup a datasource with resource-ref specifying an
     * empty password in default-resource-principal
     * <resource-ref>
     *   <default-resource-principal>
     *     <name>DBUSER</name>
     *     <password></password>
     *   </default-resource-principal>
     * </resource-ref>
     * 
     * Note that the pool still has a valid password set in it
     */
    public boolean test4() throws Exception {
	DataSource ds = (DataSource)ic_.lookup("java:comp/env/XADataSource");
	Connection conn1 = null;
	boolean passed = false;
	//clean the database
	try {
	    conn1 = ds.getConnection();
	} catch( Exception e) {
	    e.printStackTrace();
	    return true;
	} finally {
	    if (conn1 != null) { 
	        try { conn1.close(); } catch( Exception e1 ) {}
	    }
	}
	
	return false;
    }

    /**
     * Lookup a datasource without resource-ref specifying 
     * password in default-resource-principal
     * <resource-ref>
     *   <default-resource-principal>
     *     <name>DBUSER</name>
     *   </default-resource-principal>
     * </resource-ref>
     * 
     * Note that the pool still has a valid password set in it
     */

    public boolean test5() throws Exception {
    	DataSource ds = (DataSource)ic_.lookup("java:comp/env/XADataSource1");
	Connection conn1 = null;
	boolean passed = false;
	//clean the database
	try {
	    conn1 = ds.getConnection();
	} catch( Exception e) {
	    e.printStackTrace();
	    return true;
	} finally {
	    if (conn1 != null) { 
	        try { conn1.close(); } catch( Exception e1 ) {}
	    }
	}
	
	return false;
    }

    /**
     * Lookup a datasource with resource-ref specifying 
     * password in default-resource-principal but no password
     * in the pool
     * <resource-ref>
     *   <default-resource-principal>
     *     <name>DBUSER</name>
     *     <password>DBPASSWORD</password>
     *   </default-resource-principal>
     * </resource-ref>
     * 
     * Note that the pool has no password set in it
     */

    public boolean test6() throws Exception {
    	DataSource ds = (DataSource)ic_.lookup("java:comp/env/XADataSource2");
	Connection conn1 = null;
	boolean passed = false;
	//clean the database
	try {
	    conn1 = ds.getConnection();
	} catch( Exception e) {
	    e.printStackTrace();
	    return false;
	} finally {
	    if (conn1 != null) { 
	        try { conn1.close(); } catch( Exception e1 ) {}
	    }
	}
	
	return true;
    }

    public void ejbLoad() {}
    public void ejbStore() {}
    public void ejbRemove() {}
    public void ejbActivate() {}
    public void ejbPassivate() {}
    public void unsetEntityContext() {}
    public void ejbPostCreate() {}
}
