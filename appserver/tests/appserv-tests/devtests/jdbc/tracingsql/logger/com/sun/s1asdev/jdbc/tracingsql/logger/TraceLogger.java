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

package com.sun.s1asdev.jdbc.tracingsql.logger;

import org.glassfish.api.jdbc.SQLTraceListener;
import org.glassfish.api.jdbc.SQLTraceRecord;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TraceLogger implements SQLTraceListener {
   
    DataSource ds;	
    public TraceLogger() { 
	try {
        InitialContext ic = new InitialContext();
	ds = (DataSource) ic.lookup("jdbc/tracingsql-res");
	} catch(NamingException ex) {}
    }

    /**
     * Writes the record to a database.
     */
    public void sqlTrace(SQLTraceRecord record) {

        try {
            //System.out.println("### ds=" + ds);

            Object[] params = record.getParams();
            StringBuffer argsBuf = new StringBuffer();
            if (params != null && params.length > 0) {
                for (Object param : params) {
                    if (param != null) {
                        argsBuf.append(param.toString() + ";");
                    }
                }
            }
            System.out.println(
                "SQLTrace called: Details: class=" + record.getClassName() +
                    " method=" + record.getMethodName() + " args=" +
                    argsBuf.toString());
            writeRecord(ds, record.getClassName(), record.getMethodName(),
                argsBuf.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void writeRecord(DataSource ds, String classname, String methodname, String args) {
        Connection conFromDS = null;
	PreparedStatement stmt = null;
        try{
            conFromDS = ds.getConnection();
	    //System.out.println("###con=" + conFromDS);
            stmt = conFromDS.prepareStatement(
	        "insert into sql_trace values (?, ?, ?)" );

            System.out.println("### stmt=" + stmt);
	    stmt.setString(1, classname);
	    stmt.setString(2, methodname);
	    stmt.setString(3, args);

	    int count = stmt.executeUpdate();
	    //System.out.println("### inserted " + count + " rows");

        }catch(SQLException sqe){
	}finally{

            try{
                if(stmt != null){
                    stmt.close();
                }
            }catch(SQLException sqe){}

            try{
                if(conFromDS != null){
                    conFromDS.close();
                }
            }catch(SQLException sqe){}
        }
    }

}
