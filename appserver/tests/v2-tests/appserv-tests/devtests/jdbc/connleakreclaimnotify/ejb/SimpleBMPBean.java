package com.sun.s1asdev.jdbc.statementwrapper.ejb;

import javax.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.sql.*;


public class SimpleBMPBean
        implements SessionBean {

    protected DataSource ds;
    private transient javax.ejb.SessionContext m_ctx = null;

    public void setSessionContext(SessionContext context) {
	m_ctx = context;
    }

    public Integer ejbCreate() throws CreateException {
        return new Integer(1);
    }


/* Get a single connection and do not close it */
    public boolean test1() {
        Connection conn = null;
        boolean passed = true;
        Context context = null;
	long startTime = 0, endTime = 0, timeTaken =0;
        try {
            context = new InitialContext();
            ds = (DataSource) context.lookup("java:comp/env/jdbc/DataSource");
        } catch(NamingException e) {
             throw new EJBException("cant find datasource");
        }
        try { 
            startTime = System.currentTimeMillis();
            conn = ds.getConnection();
	    endTime = System.currentTimeMillis();
        } catch (Exception e) {
            e.printStackTrace();
            passed = false;
        } 
	timeTaken = (endTime-startTime)/1000;
        System.out.println("preparedStmtTest : TimeTaken : " + timeTaken);
	if(timeTaken > 59) {
	    passed = false;
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
