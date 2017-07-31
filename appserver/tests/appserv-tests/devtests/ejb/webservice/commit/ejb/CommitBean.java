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

package com.sun.s1asdev.ejb.webservice.commit;

import javax.ejb.*;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.sql.*;
import java.util.*;
import java.sql.*;

@Stateless
@WebService
@EJB(name="csb", beanInterface=CommitStatefulLocal.class)
public class CommitBean {

    @Resource(mappedName="jdbc/__default")
    private DataSource ds;

    @Resource 
    private SessionContext sessionCtx;

    public int findCustomer(int i) throws FinderException {
	Connection c = null;
     	PreparedStatement ps = null;
        int returnValue = -1;
	try {
	    c = ds.getConnection();
     	    ps = c.prepareStatement(
		"SELECT c_id from O_customer where c_id = ?");
	    ps.setInt(1, i);
	    ResultSet rs = ps.executeQuery();
	    if (!rs.next())
	       throw new FinderException("No cust for " + i);
	    returnValue = rs.getInt(1);
            System.out.println("findCustomer = " + returnValue);
	} catch (SQLException e)  {
            e.printStackTrace();
	    throw new FinderException("SQL exception " + e);
	} finally { 
	    try {
		if (ps != null)
		    ps.close();
		if (c != null)
		    c.close();
	    } catch (Exception e) {}
	}

        return returnValue;
    }

    public void updateCustomer() throws FinderException {

        System.out.println( "In updateCustomer caller" );
        
	Connection c = null;
     	PreparedStatement ps = null;
	try {
	    c = ds.getConnection();
     	    ps = c.prepareStatement(
		"UPDATE O_customer SET c_phone = ? WHERE c_id = 2 AND c_phone = 'foo'");
            ps.setString(1, "webservice");
	    int result = ps.executeUpdate();
            System.out.println("execute update returned " + result);
	} catch (SQLException e)  {
            e.printStackTrace();
	    throw new FinderException("SQL exception " + e);
	} finally { 
	    try {
		if (ps != null)
		    ps.close();
		if (c != null)
		    c.close();
	    } catch (Exception e) {}
	}

        System.out.println("Adding CommitStatefulBean with SessionSynch " +
                           " to transaction");
        

        CommitStatefulLocal csb = (CommitStatefulLocal)
            sessionCtx.lookup("csb");
        csb.foo();

        return;
        
    }

}
