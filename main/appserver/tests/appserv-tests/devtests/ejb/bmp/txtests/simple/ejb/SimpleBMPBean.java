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

package com.sun.s1asdev.ejb.bmp.txtests.simple.ejb;

import javax.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.rmi.*;
import java.util.*;
import java.sql.*;

public class SimpleBMPBean
    implements EntityBean
{
    private String _debugName = "[**SimpleBMPBean**]";
    private EntityContext entityContext;
    private DataSource ds;
    private int id;

    public void setEntityContext(EntityContext entityContext) {
        System.out.println(_debugName + "[" + Thread.currentThread() + "]"
                + " Entered setEntityContext....");
        this.entityContext = entityContext;
	    try {
	        Context context = new InitialContext();
	        ds = (DataSource) context.lookup("java:comp/env/DataSource");
	    } catch (NamingException e) {
	        throw new EJBException("cant find datasource");
	    }
        System.out.println(_debugName + "[" + Thread.currentThread() + "]"
                + " Done with setEntityContext....");
    }

    public Integer ejbCreate(int i) throws CreateException {
        System.out.println(_debugName + "[" + Thread.currentThread() + "]"
                + " Entered ejbCreate(" + i + ")....");

	    Connection c = null;
     	PreparedStatement ps = null;
	    try {
	        c = ds.getConnection();
     	        ps = c.prepareStatement(
		    "INSERT INTO O_customer (c_id, c_phone) VALUES (?,?)");
	        ps.setInt(1, i);
	        ps.setString(2, "Phone_" + i);
	        if (ps.executeUpdate() != 1) {
		        throw new CreateException("Didnt create ejb");
            }
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
        System.out.println(_debugName + "[" + Thread.currentThread() + "]"
                + " Done with ejbCreate(" + i + ")....");
	    return new Integer(i);
    }

    public int getID() {
        System.out.println(_debugName + "[" + Thread.currentThread() + "]"
                + " Entered getID()....");
        System.out.println(_debugName + "[" + Thread.currentThread() + "]"
                + " DONE getID()....");
        return id;
    }

    public CustomerInfo getCustomerInfo() {
        return new CustomerInfo(id, "Phone_" + id);
    }

    public Integer ejbFindByPrimaryKey(Integer key) throws FinderException {
        System.out.println(_debugName + "[" + Thread.currentThread() + "]"
                + " Entered ejbFindByPrimaryKey(" + key.intValue() + ")....");

	    Connection c = null;
     	PreparedStatement ps = null;
	    try {
	        c = ds.getConnection();
     	        ps = c.prepareStatement(
		    "SELECT c_phone from O_customer where c_id = ?");
	        ps.setInt(1, key.intValue());
	        ResultSet rs = ps.executeQuery();
	        if (!rs.next()) {
	            throw new FinderException("No cust for " + id);
            }
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
        System.out.println(_debugName + "[" + Thread.currentThread() + "]"
                + " DONE ejbFindByPrimaryKey(" + key.intValue() + ")....");
	    return key;
    }

    public void ejbLoad() {
        System.out.println(_debugName + "[" + Thread.currentThread() + "]"
                + " Entered ejbLoad()....");
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
        System.out.println(_debugName + "[" + Thread.currentThread() + "]"
                + " DONE ejbLoad()....");
    }

    public void ejbStore() {
        System.out.println(_debugName + "[" + Thread.currentThread() + "]"
                + " Entered ejbStore()....");
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
        System.out.println(_debugName + "[" + Thread.currentThread() + "]"
                + " DONE ejbStore()....");
    }

    public void ejbRemove() throws RemoveException {
        System.out.println(_debugName + "[" + Thread.currentThread() + "]"
                + " Entered ejbRemove()....");
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
        System.out.println(_debugName + "[" + Thread.currentThread() + "]"
                + " DONE ejbRemove()....");
    }

    public void ejbActivate() {}
    public void ejbPassivate() {}
    public void unsetEntityContext() {}
    public void ejbPostCreate(int i) {
    }
}
