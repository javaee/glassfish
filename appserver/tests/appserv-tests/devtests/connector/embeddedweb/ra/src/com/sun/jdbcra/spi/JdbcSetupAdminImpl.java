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

package com.sun.jdbcra.spi;

import javax.naming.*;
import javax.sql.*;
import java.sql.*;
// import javax.sql.DataSource;
public class JdbcSetupAdminImpl implements JdbcSetupAdmin {

    private String tableName;

    private String jndiName;

    private String schemaName;

    private Integer noOfRows;

    public void setTableName(String db) {
        tableName = db;
    }

    public String getTableName(){
        return tableName;
    }

    public void setJndiName(String name){
        jndiName = name;
    }

    public String getJndiName() {
        return jndiName;
    }

    public void setSchemaName(String name){
        schemaName = name;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setNoOfRows(Integer i) {
        System.out.println("Setting no of rows :" + i);
        noOfRows = i;
    }

    public Integer getNoOfRows() {
        return noOfRows;
    }

private void printHierarchy(ClassLoader cl, int cnt){
while(cl != null) {
	for(int i =0; i < cnt; i++) 
		System.out.print(" " );
	System.out.println("PARENT :" + cl);
	cl = cl.getParent();
	cnt += 3;
}
}

private void compareHierarchy(ClassLoader cl1, ClassLoader cl2 , int cnt){
while(cl1 != null || cl2 != null) {
        for(int i =0; i < cnt; i++)
                System.out.print(" " );
        System.out.println("PARENT of ClassLoader 1 :" + cl1);
	System.out.println("PARENT of ClassLoader 2 :" + cl2);
	System.out.println("EQUALS : " + (cl1 == cl2));
        cl1 = cl1.getParent();
	cl2 = cl2.getParent();
        cnt += 3;
}
}


    public boolean checkSetup(){

        if (jndiName== null || jndiName.trim().equals("")) {
	   return false;
	}

        if (tableName== null || tableName.trim().equals("")) {
	   return false;
	}

        Connection con = null;
	Statement s = null;
	ResultSet rs = null;
	boolean b = false;
        try {
	    InitialContext ic = new InitialContext();
	//debug
	Class clz = DataSource.class;
/*
	if(clz.getClassLoader() != null) {
		System.out.println("DataSource's clasxs : " +  clz.getName() +  " classloader " + clz.getClassLoader());
		printHierarchy(clz.getClassLoader().getParent(), 8);
	}
	Class cls = ic.lookup(jndiName).getClass();
	System.out.println("Looked up class's : " + cls.getPackage() + ":" + cls.getName()  + " classloader "  + cls.getClassLoader());
	printHierarchy(cls.getClassLoader().getParent(), 8);

	System.out.println("Classloaders equal ? " +  (clz.getClassLoader() == cls.getClassLoader()));
	System.out.println("&*&*&*&* Comparing Hierachy DataSource vs lookedup");
	if(clz.getClassLoader() != null) {
		compareHierarchy(clz.getClassLoader(), cls.getClassLoader(), 8);
	}

	System.out.println("Before lookup");
*/
	Object o = ic.lookup(jndiName);
//	System.out.println("after lookup lookup");

	    DataSource ds = (DataSource)o ;
/*
	System.out.println("after cast");
	System.out.println("---------- Trying our Stuff !!!");
	try {
		Class o1 = (Class.forName("com.sun.jdbcra.spi.DataSource"));
		ClassLoader cl1 = o1.getClassLoader();
		ClassLoader cl2 = DataSource.class.getClassLoader();
		System.out.println("Cl1 == Cl2" + (cl1 == cl2));
		System.out.println("Classes equal" + (DataSource.class == o1));
	} catch (Exception ex) {
		ex.printStackTrace();
	}
*/
            con = ds.getConnection();
	    String fullTableName = tableName;
	    if (schemaName != null && (!(schemaName.trim().equals("")))) {
	        fullTableName = schemaName.trim() + "." + fullTableName;
	    }
	    String qry = "select * from " + fullTableName; 

	    System.out.println("Executing query :" + qry);

	    s = con.createStatement();
	    rs = s.executeQuery(qry); 

            int i = 0;
	    if (rs.next()) {
	        i++;
	    }

            System.out.println("No of rows found:" + i);
            System.out.println("No of rows expected:" + noOfRows);

	    if (i == noOfRows.intValue()) {
	       b = true;
	    } else {
	       b = false;
	    }
	} catch(Exception e) {
	    e.printStackTrace();
	    b = false;
	} finally {
	    try {
	        if (rs != null) rs.close();
	        if (s != null) s.close();
	        if (con != null) con.close();
            } catch (Exception e) {
	    }
	}
	System.out.println("Returning setup :" +b);
	return b;
    }

}
