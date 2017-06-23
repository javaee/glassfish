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

package com.sun.s1asdev.jdbc.stmtcaching.ejb;

import javax.ejb.*;
import javax.naming.*;
import java.sql.*;

public class SimpleBMPBean implements EntityBean {

    protected com.sun.appserv.jdbc.DataSource ds;

    private int[] columnIndexes6 = {2, 3};

    public void setEntityContext(EntityContext entityContext) {
        Context context = null;
        try {
            context = new InitialContext();
            ds = (com.sun.appserv.jdbc.DataSource) context.lookup("java:comp/env/DataSource");
        } catch (NamingException e) {
            throw new EJBException("cant find datasource");
        }
    }

    public Integer ejbCreate() throws CreateException {
        return new Integer(1);
    }

    public boolean testHit() {
	System.out.println("Statement caching Hit test Start");
        Connection firstConnection = null;
        Connection lastConnection = null;
        com.sun.appserv.jdbc.DataSource ds = null;
        PreparedStatement stmt = null;
	PreparedStatement firstStatement = null;
	PreparedStatement lastStatement = null;
        String tableName = "customer_stmt_wrapper";
        ds = this.ds;
	int tableIndex = 1;
        boolean passed = false;
            Connection conn = null;
	try {
                conn = ds.getConnection();
	}catch(Exception ex) {
	}
        for (int i = 1; i < 7; i++) {
	    System.out.println("Index = " + i);
            try {

	        if(i == 6) {
		    stmt = conn.prepareStatement("select * from "+ tableName + tableIndex + " where c_phone= ?");
		    lastStatement = stmt;
		    System.out.println("lastStatement : "  + lastStatement);
		} else {   
		    int[] columnIndexes = {i+1, i+2};
                    stmt = conn.prepareStatement("select * from "+ tableName+i +" where c_phone= ?");
		}
		if(i == 1) {
		    firstStatement = stmt;
		    System.out.println("firstStatement : " + firstStatement);
		} 
            } catch (Exception e) {
                e.printStackTrace();
                passed = false;
            } finally {
		if(stmt != null) {
	            try {
			stmt.close();
		    } catch(Exception ex) {}
		}
            }
        }
	passed = (firstStatement == lastStatement);
	try {
	    if(firstStatement != null) {
		firstStatement.close();
	    }
	    if(lastStatement != null) {
		lastStatement.close();
	    }
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (Exception e1) {
                    }
                } 
	} catch(Exception ex) {
	    ex.printStackTrace();
	}
	System.out.println("Statement caching Hit test End");
	return passed;
    }
    
    public boolean testMiss() {
	System.out.println("Statement caching Miss test Start");
        Connection firstConnection = null;
        Connection lastConnection = null;
        com.sun.appserv.jdbc.DataSource ds = null;
        PreparedStatement stmt = null;
	PreparedStatement firstStatement = null;
	PreparedStatement lastStatement = null;
        String tableName = "customer_stmt_wrapper";
        ds = this.ds;
	int tableIndex = 1;

        Connection conn = null;
        try {
            conn = ds.getConnection();
        } catch(Exception ex) {}
        boolean passed = false;
        for (int i = 1; i < 8; i++) {
            try {

		if(i ==7) {
			stmt = conn.prepareStatement("select * from " + tableName + tableIndex + " where c_phone=?");
			lastStatement = stmt;
		        System.out.println("lastStatement : "  + lastStatement);
		} else {
                    stmt = conn.prepareStatement("select * from "+ tableName+i +" where c_phone= ?");
		}
		if(i == 1) {
		    firstStatement = stmt;
		    System.out.println("firstStatement : " + firstStatement);
		}
            } catch (Exception e) {
                e.printStackTrace();
                passed = false;
            } finally {
            }
        }
        passed = firstStatement != lastStatement;
	try {
	    if(firstStatement != null) {
		firstStatement.close();
	    }
	    if(lastStatement != null) {
		lastStatement.close();
	    }
	    if(conn != null) {
	        conn.close();
	    }
	} catch(Exception ex) {
	    ex.printStackTrace();
	}
	System.out.println("Statement caching Miss test End");
        return passed;
    }


    public boolean testHitColumnNames() {
	System.out.println("Statement caching Hit (Column Names) test Start");
        Connection firstConnection = null;
        Connection lastConnection = null;
        com.sun.appserv.jdbc.DataSource ds = null;
        PreparedStatement stmt = null;
	PreparedStatement firstStatement = null;
	PreparedStatement lastStatement = null;
        String tableName = "customer_stmt_wrapper";
        ds = this.ds;
	int tableIndex = 1;
        boolean passed = false;
        Connection conn = null;
	try {
                conn = ds.getConnection();
	}catch(Exception ex) {
	}
        for (int i = 1; i < 7; i++) {
	    System.out.println("Index = " + i);
            try {

	        if(i == 6) {
		    String[] columnNames = {"c_id", "c_phone"};
		    stmt = conn.prepareStatement("select * from "+ tableName + tableIndex + " where c_phone= ?", columnNames);
		    lastStatement = stmt;
		    System.out.println("lastStatement : "  + lastStatement);
		} else if(i ==1) {   
		    String[] columnNames = {"c_id", "c_phone"};
		    stmt = conn.prepareStatement("select * from "+ tableName + tableIndex + " where c_phone= ?", columnNames);
		    firstStatement = stmt;
		    System.out.println("firstStatement : " + firstStatement);
		} else {
		    int j = i + 2;
                    int k = j + 1;
		    String[] columnNames = {"c_id"+j, "c_id"+k};
                    stmt = conn.prepareStatement("select * from "+ tableName+i +" where c_phone= ?", columnNames);
		}
            } catch (Exception e) {
                e.printStackTrace();
                passed = false;
            } finally {
		if(stmt != null) {
	            try {
			stmt.close();
		    } catch(Exception ex) {}
		}
            }
        }
	passed = (firstStatement == lastStatement);
	try {
	    if(firstStatement != null) {
		firstStatement.close();
	    }
	    if(lastStatement != null) {
		lastStatement.close();
	    }
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (Exception e1) {
                    }
                } 
	} catch(Exception ex) {
	    ex.printStackTrace();
	}
	System.out.println("Statement caching Hit (Column names) test End");
	return passed;
    }

    public boolean testHitColumnIndexes() {
	System.out.println("Statement caching Hit (Column indexes) test Start");
        Connection firstConnection = null;
        Connection lastConnection = null;
        com.sun.appserv.jdbc.DataSource ds = null;
        PreparedStatement stmt = null;
	PreparedStatement firstStatement = null;
	PreparedStatement lastStatement = null;
        String tableName = "customer_stmt_wrapper";
        ds = this.ds;
	int tableIndex = 1;
        boolean passed = false;
            Connection conn = null;
	try {
                conn = ds.getConnection();
	}catch(Exception ex) {
	}
        for (int i = 1; i < 7; i++) {
	    System.out.println("Index = " + i);
            try {

	        if(i == 6) {
		    stmt = conn.prepareStatement("select * from "+ tableName + tableIndex + " where c_phone= ?", columnIndexes6);
		    lastStatement = stmt;
		    System.out.println("lastStatement : "  + lastStatement);
		} else {   
			//First stmt executed would have {2,3} same as columnIndexes6
		    int[] columnIndexes = {i+1, i+2};
                    stmt = conn.prepareStatement("select * from "+ tableName+i +" where c_phone= ?", columnIndexes);
		}
		if(i == 1) {
		    firstStatement = stmt;
		    System.out.println("firstStatement : " + firstStatement);
		} 
            } catch (Exception e) {
                e.printStackTrace();
                passed = false;
            } finally {
		if(stmt != null) {
	            try {
			stmt.close();
		    } catch(Exception ex) {}
		}
            }
        }
	passed = (firstStatement == lastStatement);
	try {
	    if(firstStatement != null) {
		firstStatement.close();
	    }
	    if(lastStatement != null) {
		lastStatement.close();
	    }
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (Exception e1) {
                    }
                } 
	} catch(Exception ex) {
	    ex.printStackTrace();
	}
	System.out.println("Statement caching Hit (Column indexes) test End");
	return passed;
    }
    public void ejbLoad() {
    }

    public void ejbStore() {
    }

    public void ejbRemove() {
    }

    public void ejbActivate() {
    }

    public void ejbPassivate() {
    }

    public void unsetEntityContext() {
    }

    public void ejbPostCreate() {
    }
}
