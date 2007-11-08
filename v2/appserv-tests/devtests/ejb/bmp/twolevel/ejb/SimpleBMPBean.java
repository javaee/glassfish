package com.sun.s1asdev.ejb.bmp.twolevel.ejb;

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
    int id;

    public void setEntityContext(EntityContext entityContext) {
	Context context = null;
	try {
	    context    = new InitialContext();
	    ds = (DataSource) context.lookup("java:comp/env/DataSource");
	} catch (NamingException e) {
	    throw new EJBException("cant find datasource");
	}
    System.out.println("[**SimpleBMPBean**] Done with setEntityContext....");
    }

    public Integer ejbCreate(int i) throws CreateException {

	Connection c = null;
     	PreparedStatement ps = null;
	try {
	    c = ds.getConnection();
     	    ps = c.prepareStatement(
		"INSERT INTO O_customer (c_id, c_phone) VALUES (?,?)");
	    ps.setInt(1, i);
	    ps.setString(2, "550-1212");
	    if (ps.executeUpdate() != 1)
		throw new CreateException("Didnt create ejb");
    System.out.println("[**SimpleBMPBean**] Done with ejbCreate....");
	} catch (SQLException e)  {
	    throw new CreateException("SQL exception " + e);
	} finally { 
	    try {
		if (ps != null)
		    ps.close();
		if (c != null)
		    c.close();
	    } catch (Exception e) {}
	}
	id = i;
	return new Integer(i);
    }

    public void foo() {
        System.out.println("[**SimpleBMPBean**] Done with foo....");
    }

    public Integer ejbFindByPrimaryKey(Integer key) throws FinderException {
	Connection c = null;
     	PreparedStatement ps = null;
	try {
	    c = ds.getConnection();
     	    ps = c.prepareStatement(
		"SELECT c_phone from O_customer where c_id = ?");
	    ps.setInt(1, key.intValue());
	    ResultSet rs = ps.executeQuery();
	    if (!rs.next())
	       throw new FinderException("No cust for " + id);
	    String phone = rs.getString(1);
	    return key;
	} catch (SQLException e)  {
	    throw new FinderException("SQL exception " + e);
	} finally { 
	    try {
		if (ps != null)
		    ps.close();
		if (c != null)
		    c.close();
	    } catch (Exception e) {}
	}
    }

    public void ejbLoad() {
	Connection c = null;
     	PreparedStatement ps = null;
	try {
	    c = ds.getConnection();
     	    ps = c.prepareStatement(
		"SELECT c_phone from O_customer where c_id = ?");
	    ps.setInt(1, id);
	    ResultSet rs = ps.executeQuery();
	    if (!rs.next())
	       throw new NoSuchEntityException("No cust for " + id);
	    String phone = rs.getString(1);
	} catch (SQLException e)  {
	    throw new NoSuchEntityException("SQL exception " + e);
	} finally { 
	    try {
		if (ps != null)
		    ps.close();
		if (c != null)
		    c.close();
	    } catch (Exception e) {}
	}
    }

    public void ejbStore() {
	Connection c = null;
     	PreparedStatement ps = null;
	try {
	    c = ds.getConnection();
     	    ps = c.prepareStatement(
		"UPDATE O_customer SET c_phone = ? WHERE c_id = ?");
	    ps.setString(1, "550-1212");
	    ps.setInt(2, id);
	    if (ps.executeUpdate() != 1)
		throw new EJBException("Didnt store ejb");
        System.out.println("[**SimpleBMPBean**] Done with ejbStore....");
	} catch (SQLException e)  {
	    throw new EJBException("SQL exception " + e);
	} finally { 
	    try {
		if (ps != null)
		    ps.close();
		if (c != null)
		    c.close();
	    } catch (Exception e) {}
	}
    }

    public void ejbRemove() throws RemoveException {
	Connection c = null;
     	PreparedStatement ps = null;
	try {
	    c = ds.getConnection();
     	    ps = c.prepareStatement(
		"DELETE FROM O_customer WHERE c_id = ?");
	    ps.setInt(1, id);
	    if (ps.executeUpdate() != 1)
		throw new RemoveException("Didnt remove ejb");
	} catch (SQLException e)  {
	    throw new RemoveException("SQL exception " + e);
	} finally { 
	    try {
		if (ps != null)
		    ps.close();
		if (c != null)
		    c.close();
	    } catch (Exception e) {}
	}
    }

    public void ejbActivate() {}
    public void ejbPassivate() {}
    public void unsetEntityContext() {}
    public void ejbPostCreate(int i) {
    }
}
