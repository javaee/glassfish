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

package com.sun.s1asdev.jdbc.fetchoutofseq.ejb;

import javax.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.rmi.*;
import java.util.*;
import java.sql.*;

public class SimpleBMPBean
    implements EntityBean
{

    protected DataSource ds;

    public void setEntityContext(EntityContext entityContext) {
	Context context = null;
	try {
	    context    = new InitialContext();
	    ds = (DataSource) context.lookup("java:comp/env/DataSource");
	} catch (NamingException e) {
	    throw new EJBException("cant find datasource");
	}
    }

    public Integer ejbCreate() throws CreateException {
	return new Integer(1);
    }

    public boolean test1() {
        return issueQuery( true );
    }

    public boolean test2() {
        return issueQuery( false );
    }

    /**
     *
     * If a connection's autocommit is set to true and we attempt a select
     * for update sql query, Oracle throws a "ORA-01002: Fetch out of sequence"
     * exception. This test tries to :
     * 1. get a connection using getNonTxConnection API and try a "select for
     * update" query. Since a connection obtained using getNonTxConnection
     * is not managed (transaction-wise), its autocommit is set to true by
     * default. So this fails.
     * 2. gets a connection as above but sets its autocommit to true. The 
     * query will then pass.
     */
    public boolean issueQuery(boolean autoCommit) {
        Connection conn = null;
	PreparedStatement stmt = null;
        boolean passed  = (autoCommit ? false : true);
	try {
	    //conn = ds.getConnection();
	    conn = ((com.sun.appserv.jdbc.DataSource)ds).getNonTxConnection();
	    if ( !autoCommit ) {
	        conn.setAutoCommit( false );
	    }
	    stmt = conn.prepareStatement("SELECT c_id, c_phone FROM O_CUSTOMER FOR UPDATE OF c_id");
	    stmt.executeQuery();
	} catch (Exception e) {
	    if (autoCommit) {
	        passed = true;
	    } else {
	        passed = false;
	    }
	    e.printStackTrace();
	} finally {
	    if (stmt != null ) {
	        try {stmt.close();} catch(Exception e1) {}
	    }
	    if ( conn != null ) {
	        try { conn.close(); } catch( Exception e1) {}    
	    }
        } 
        
	return passed;
    }
   

    public void ejbLoad() {}
    public void ejbStore() {}
    public void ejbRemove() {}
    public void ejbActivate() {}
    public void ejbPassivate() {}
    public void unsetEntityContext() {}
    public void ejbPostCreate() {}
}
