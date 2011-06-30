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
