package com.sun.jdbcra.spi;

import javax.naming.*;
import javax.sql.*;
import java.sql.*;

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
	    DataSource ds = (DataSource) ic.lookup(jndiName);
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
    
    public int getVersion(){
	    return ResourceAdapter.VERSION;
    }

}
