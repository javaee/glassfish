package com.sun.s1asdev.ejb.bmp;

import javax.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.rmi.*;
import java.util.*;
import java.sql.*;

public class SimpleEntityBean
    implements EntityBean
{

    private transient DataSource ds;

    private EntityContext entityCtx;
    private String key;
    private String name;

    public void setEntityContext(EntityContext entityContext) {
        this.entityCtx = entityContext;
        initDataSource();
        System.out.println("[**SimpleEntityBean**] setEntityContext called");
    }

    private void initDataSource() {
        try {
            Context context = null;
            context    = new InitialContext();
            ds = (DataSource) context.lookup("java:comp/env/jdbc/DataSource");
        } catch (NamingException e) {
            throw new EJBException("cant find datasource");
        }
    }

    public String ejbCreate(String key, String name)
        throws CreateException
    {

        Connection c = null;
        PreparedStatement ps = null;
        try {
	        c = ds.getConnection();
     	    ps = c.prepareStatement(
                "INSERT INTO SimpleEntity (key, name) VALUES (?,?)");
            ps.setString(1, key);
            ps.setString(2, name);
            if (ps.executeUpdate() != 1) {
                throw new CreateException("Didnt create ejb");
            }
            this.key = key;
            this.name = name;
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
        return key;
    }

    public void ejbPostCreate(String key, String name) {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String ejbFindByPrimaryKey(String key)
        throws FinderException
    {
        Connection c = null;
     	PreparedStatement ps = null;
	    try {
	        c = ds.getConnection();
     	    ps = c.prepareStatement(
                "SELECT key from SimpleEntity where key = ?");
            ps.setString(1, key);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                throw new FinderException("No cust for " + key);
            }
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
                "SELECT name from SimpleEntity where key = ?");
            ps.setString(1, key);
	    ResultSet rs = ps.executeQuery();
	    if (!rs.next())
	       throw new NoSuchEntityException("No cust for " + key);
	    this.name = rs.getString(1);
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
		"UPDATE SimpleEntity SET name = ? WHERE key = ?");
	    ps.setString(1, name);
	    ps.setString(2, key);
	    if (ps.executeUpdate() != 1)
		throw new EJBException("Didnt store ejb");
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
		"DELETE FROM SimpleEntity WHERE key = ?");
	    ps.setString(1, name);
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

    public void ejbActivate() {
        this.key = (String) entityCtx.getPrimaryKey();
        initDataSource();
    }

    public void ejbPassivate() {}
    public void unsetEntityContext() {}
    public void ejbPostCreate(String key) {
    }
}
