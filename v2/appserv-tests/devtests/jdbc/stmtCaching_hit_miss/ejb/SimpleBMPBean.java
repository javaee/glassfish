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
