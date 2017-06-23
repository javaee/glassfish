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

import javax.sql.PooledConnection;
import java.sql.Connection;
import javax.sql.DataSource;
import org.apache.derby.jdbc.EmbeddedDataSource;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Enables Authentication for Embedded Derby Mode.
 * Enables Authentication of certain users for use in the JDBC devtests.
 *
 * @author shalini
 */
public class SetDerbyAuthentication {
    
    private static String DATABASES_HOME = "/tmp/jdbc_devtests/databases";
    private static String RECONFIG_DB_NAME = DATABASES_HOME + "/reconfig-db";
    private static String SAMPLE_DB_NAME = DATABASES_HOME + "/sample-db";
    private static String SUN_APPSERV_SAMPLES_DB = DATABASES_HOME + "/sun-appserv-samples";
    
    public static void main(String[] args) {
        setCommonDBAuthentication();
        setReconfigDBAuthentication();
        createReconfigTables();
    }

    private static void setCommonDBAuthentication() {
        Statement stmt = null;
        Connection con = null;
        ResultSet rs = null;
        try {
	    DataSource ds = initializeDataSource();
	    con = (Connection) ds.getConnection();
	    stmt = con.createStatement();
	    //Execute CallableStatement to enable authentication
	    stmt.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(" + 
			    			"'derby.connection.requireAuthentication', 'true')");
   	    rs = stmt.executeQuery( 
	            "VALUES SYSCS_UTIL.SYSCS_GET_DATABASE_PROPERTY(" + 
		    "'derby.connection.requireAuthentication')"); 
	    rs.next(); 
	    System.out.println("Authentication set to " + rs.getString(1)); 		

	    //Setting Usernames and passwords
	    stmt.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(" + 
			    			"'derby.user.dbuser', 'dbpassword')");
	    stmt.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(" + 
			    			"'derby.user.DBUSER', 'DBPASSWORD')");
	    stmt.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(" + 
			    			"'derby.user.APP', 'APP')");
	    stmt.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(" + 
			    			"'derby.user.DERBYUSER', 'DERBYPASSWORD')");
        } catch(Exception ex) {
            ex.printStackTrace();
	} finally {
            try {
                if(rs != null) {
                    rs.close();
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }            
        }       
    }
    
    private static void setReconfigDBAuthentication() {
        Statement stmt = null;
        Connection con = null;
        ResultSet rs = null;              
        try {
	    DataSource ds = initializeReconfigDataSource();
	    con = (Connection) ds.getConnection();
	    stmt = con.createStatement();
	    //Execute CallableStatement to enable authentication
	    stmt.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(" + 
			    			"'derby.connection.requireAuthentication', 'true')");
   	    rs = stmt.executeQuery( 
	            "VALUES SYSCS_UTIL.SYSCS_GET_DATABASE_PROPERTY(" + 
		    "'derby.connection.requireAuthentication')"); 
	    rs.next(); 
	    System.out.println("Authentication set to " + rs.getString(1)); 		

	    //Setting Usernames and passwords
	    stmt.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(" + 
			    			"'derby.user.ruser', 'rpassword')");
	    stmt.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(" + 
			    			"'derby.user.RUSER', 'RPASSWORD')");
        } catch(Exception ex) {
            ex.printStackTrace();
	} finally {
            try {
                if(rs != null) {
                    rs.close();
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }            
            
        }
    }
    
    private static void setSampleDBAuthentication() {
        Statement stmt = null;
        Connection con = null;
        ResultSet rs = null;              
        try {
	    DataSource ds = initializeSampleDataSource();
	    con = (Connection) ds.getConnection();
	    stmt = con.createStatement();
	    //Execute CallableStatement to enable authentication
	    stmt.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(" + 
			    			"'derby.connection.requireAuthentication', 'true')");
   	    rs = stmt.executeQuery( 
	            "VALUES SYSCS_UTIL.SYSCS_GET_DATABASE_PROPERTY(" + 
		    "'derby.connection.requireAuthentication')"); 
	    rs.next(); 
	    System.out.println("Authentication set to " + rs.getString(1)); 		

	    //Setting Usernames and passwords
	    stmt.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(" + 
			    			"'derby.user.APP', 'APP')");
	    stmt.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(" + 
			    			"'derby.user.app', 'app')");
        } catch(Exception ex) {
            ex.printStackTrace();
	} finally {
            try {
                if(rs != null) {
                    rs.close();
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }            
            
        }
    }

    private static void createReconfigTables() {
        Statement stmt = null;
        Statement stmt1= null;
        String reconfigTable = "reconfigTestTable";
        String commonTable = "sampleTable";
        String columnName = "ID";
        Connection con = null;
        Connection con1 = null;
        try {
	    DataSource ds = initializeReconfigDataSource();
            DataSource ds1 = initializeSampleDataSource();
	    con = (Connection) ds.getConnection();
            con1 = (Connection) ds1.getConnection();
	    stmt = con.createStatement();
            stmt1 = con1.createStatement();
            String query = "create table " + reconfigTable + "(" + columnName + " char(50))";
            stmt.executeUpdate(query);
            query = "create table " + commonTable + "(" + columnName + " char(50))";
            stmt1.executeUpdate(query);
            System.out.println("Created tables : reconfigTestTable and sampleTable");
        } catch(Exception ex) {
            ex.printStackTrace();
	} finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if(stmt1 != null) {
                    stmt1.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (con != null) {
                    con.close();
                }
                if(con1 != null) {
                    con1.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }            
        }       
        
    }
    
    /**
     * Sets the EmbeddedDataSource properties corresponding to the pool configuration
     * created for running the JDBC reconfig devtests.
     */
    private static org.apache.derby.jdbc.EmbeddedDataSource initializeReconfigDataSource() {
        EmbeddedDataSource ds = new EmbeddedDataSource();	    
	ds.setUser("ruser");
	ds.setPassword("rpassword");
	ds.setDatabaseName(RECONFIG_DB_NAME);
	ds.setConnectionAttributes("create=true");
	return ds;
    }

    /**
     * Sets the EmbeddedDataSource properties corresponding to the pool configuration
     * created for running the JDBC reconfig devtests.
     */
    private static org.apache.derby.jdbc.EmbeddedDataSource initializeSampleDataSource() {
        EmbeddedDataSource ds = new EmbeddedDataSource();	    
	ds.setUser("APP");
	ds.setPassword("APP");
	ds.setDatabaseName(SAMPLE_DB_NAME);
	ds.setConnectionAttributes("create=true");
	return ds;
    }
    
    /**
     * Sets the EmbeddedDataSource properties corresponding to the pool configuration
     * created for running the JDBC devtests.
     */
    private static org.apache.derby.jdbc.EmbeddedDataSource initializeDataSource() {
        EmbeddedDataSource ds = new EmbeddedDataSource();	    
	ds.setUser("APP");
	ds.setPassword("APP");
	ds.setDatabaseName(SUN_APPSERV_SAMPLES_DB);
	ds.setConnectionAttributes("create=true");
	return ds;
    }
}
