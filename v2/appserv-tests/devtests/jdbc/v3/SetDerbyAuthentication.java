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
    public static void main(String[] args) {
	Statement stmt = null;
        try {
	    DataSource ds = initializeDataSource();
	    Connection con = (Connection) ds.getConnection();
	    stmt = con.createStatement();
	    //Execute CallableStatement to enable authentication
	    stmt.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(" + 
			    			"'derby.connection.requireAuthentication', 'true')");
   	    ResultSet rs = stmt.executeQuery( 
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
	    stmt.close();
        } catch(Exception ex) {
            ex.printStackTrace();
	}
    }

    /**
     * Sets the EmbeddedDataSource properties corresponding to the pool configuration
     * created for running the JDBC devtests.
     */
    private static org.apache.derby.jdbc.EmbeddedDataSource initializeDataSource() {
        EmbeddedDataSource ds = new EmbeddedDataSource();	    
	ds.setUser("APP");
	ds.setPassword("APP");
	ds.setDatabaseName("/space/connectors/v3/glassfish/sun-appserv-samples");
	ds.setConnectionAttributes("create=true");
	return ds;
    }
}	
