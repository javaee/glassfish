package com.sun.s1asdev.jdbc.tracingsql.logger;

import org.glassfish.api.jdbc.SQLTraceListener;
import org.glassfish.api.jdbc.SQLTraceRecord;
import javax.sql.*;
import java.sql.*;
import javax.naming.*;
import java.rmi.*;
import java.util.*;

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
	    if(params != null && params.length > 0) {
		for(Object param : params) {
                    argsBuf.append(param.toString() + ";");
		}
	    }
	    //System.out.println(">>>>> class=" + record.getClassName() + " method=" + record.getMethodName() + " args=" + argsBuf.toString());
	    writeRecord(ds, record.getClassName(), record.getMethodName(), argsBuf.toString());
	} catch(Exception ex) {
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
