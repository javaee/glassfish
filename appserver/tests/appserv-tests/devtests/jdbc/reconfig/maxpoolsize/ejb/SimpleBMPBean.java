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

package com.sun.s1asdev.jdbc.reconfig.maxpoolsize.ejb;

import javax.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.rmi.*;
import java.util.*;
import java.sql.*;

public class SimpleBMPBean implements EntityBean {

    protected DataSource ds;
    int id;

    public void setEntityContext(EntityContext entityContext) {
    }

    public Integer ejbCreate() throws CreateException {
	return new Integer(1);
    }

    /** 
     * The basic strategy here is that we try to get 1 more connection
     * than the maxpoolsize. This single extra getConnection should not
     * pass. If this happens, the test has passed.
     */

    public boolean test1( int maxPoolSize, boolean throwException, boolean useXA ) {
        try {
	    InitialContext ic = new InitialContext();
	    if ( useXA ) {
	        ds = (DataSource) ic.lookup("java:comp/env/DataSource_xa");
	    } else {
	        ds = (DataSource) ic.lookup("java:comp/env/DataSource");
	    }
	} catch( Exception e ) {
	    e.printStackTrace();
	    return false;
	}
	boolean passed = true;
	Connection[] conns = new Connection[maxPoolSize];
        for( int i = 0; i < maxPoolSize; i++ ) {
	    System.out.println("throwException is : " + throwException );
	    try {
		System.out.println("########Getting connection : " + i );
	        conns[i] = ds.getConnection();
	    } catch (Exception e) {
		e.printStackTrace();
	        return false;
	    } 

	}    
	//try getting an extra connection
	System.out.println("---Try getting extra connection");
	Connection con = null;
	try {
	    con = ds.getConnection();
	} catch( Exception e) {
	    System.out.print("Caught exception : " ) ;
	    if ( throwException ) {
		System.out.println("Setting passed to true");
		passed = true;
            } else {
	        passed = false;
            }    

        } finally {
            try { con.close(); } catch ( Exception e ) {}
        }
        
	for (int i = 0 ; i < maxPoolSize;i++ ) {
	    try {
	        conns[i].close();
	    } catch( Exception e) {
	        //passed = false;
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
