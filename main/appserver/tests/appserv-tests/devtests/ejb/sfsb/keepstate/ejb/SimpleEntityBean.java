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

package com.sun.s1asdev.ejb.sfsb.keepstate.ejb;

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
                "INSERT INTO SimpleEntity (keyid, name) VALUES (?,?)");
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
                "SELECT keyid from SimpleEntity where keyid = ?");
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
                "SELECT name from SimpleEntity where keyid = ?");
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
		"UPDATE SimpleEntity SET name = ? WHERE keyid = ?");
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
		"DELETE FROM SimpleEntity WHERE keyid = ?");
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
