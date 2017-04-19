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
