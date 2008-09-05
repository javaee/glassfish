/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.jdbc.devtests.v3.test.reconfig;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import javax.transaction.SystemException;
import org.glassfish.jdbc.devtests.v3.util.HtmlUtil;

/**
 *
 * @author shalini
 */
public class ReconfigTestUtil {
    Map<String, Boolean> resultsMap = new HashMap<String, Boolean>();

    Map<String, Boolean> poolPropertyChangeTest(DataSource ds, PrintWriter out, 
            boolean throwException) {
        //Tests the property change of jdbc connection pool by asadmin set command.
        try {
            if (testPropertyChange(ds, out, throwException)) {
                resultsMap.put("pool-property-change-test", true);
            }else{
                resultsMap.put("pool-property-change-test", false);
            }
        } catch (Exception e) {
            resultsMap.put("pool-property-change-test", false);
        } 
        return resultsMap;
    }

    Map<String, Boolean> resourceAttributeChangeTest(DataSource ds, PrintWriter out, 
            boolean throwException) {
        //Tests the attribute set of jdbc resource by asadmin set command.
        try {
            if (testJDBCResourceChange(ds, out, throwException)) {
                resultsMap.put("resource-change-wrong-table-test", true);
            }else{
                resultsMap.put("resource-change-wrong-table-test", false);
            }
        } catch (Exception e) {
            resultsMap.put("resource-change-wrong-table-test", false);
        } 
        
        try {
            if(testJDBCResourceChangeCorrectTable(ds, out, !throwException)) {
                resultsMap.put("resource-change-correct-table-test", true);
            } else {
                resultsMap.put("resource-change-correct-table-test", false);
            }
        } catch (Exception ex) {
            resultsMap.put("resource-change-correct-table-test", false);
        }
        return resultsMap;
    }
    
    Map<String, Boolean> poolAttributeChangeTest(DataSource ds, PrintWriter out, 
            int maxPoolSize, boolean throwException) {
        //Tests the attribute set by asadmin set command.
        try {
            if (testMaxPoolSize(ds, out, maxPoolSize, throwException)) {
                resultsMap.put("existing-pool-attribute-max-pool-size", true);
            }else{
                resultsMap.put("existing-pool-attribute-max-pool-size", false);
            }
        } catch (Exception e) {
            resultsMap.put("existing-pool-attribute-max-pool-size", false);
        }
        return resultsMap;
    }

    private boolean testJDBCResourceChange(DataSource ds, PrintWriter out, 
            boolean throwException) throws SystemException {
        HtmlUtil.printHR(out);
        out.println("<h4> Reconfig test : tablename : reconfigTestTable (jdbc-reconfig-test-pool-2)</h4>");
        boolean passed = true;

        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        out.println("Getting connection ....");
        try {
            con = ds.getConnection();
            //get data from a table from the database jdbc-dev-test-db (for jdbc-dev-test-pool)
            //exception will be thrown since the pool name has been changed to DerbyPool
            stmt = con.createStatement();
            rs = stmt.executeQuery("select * from reconfigTestTable");
        } catch (Exception ex) {
            out.println("Caught Exception ...");
            HtmlUtil.printException(ex, out);
            if ( throwException ) {
                passed = true;
            } else {
                passed = false;
            }
        } finally {
            try { con.close(); } catch ( Exception e ) {}
        }
        
        HtmlUtil.printHR(out);
        return passed;
    }

    private boolean testJDBCResourceChangeCorrectTable(DataSource ds, PrintWriter out, 
            boolean throwException) throws SystemException {
        HtmlUtil.printHR(out);
        out.println("<h4> Reconfig test : tableName : sampleTable (jdbc-reconfig-test-pool-1) </h4>");
        boolean passed = true;

        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        out.println("Getting connection ....");
        try {
            con = ds.getConnection();
            //get data from a table from the database sun-appserv-samples (for DerbyPool)
            //no exception will be thrown
            stmt = con.createStatement();
            rs = stmt.executeQuery("select * from sampleTable");
        } catch (Exception ex) {
            out.println("Caught Exception ...");
            HtmlUtil.printException(ex, out);
            if ( throwException ) {
                passed = true;
            } else {
                passed = false;
            }
        } finally {
            try { con.close(); } catch ( Exception e ) {}
        }
        
        HtmlUtil.printHR(out);
        return passed;        
    }
    
    public boolean testMaxPoolSize(DataSource ds, PrintWriter out, 
            int maxPoolSize, boolean throwException)  
            throws SystemException {
        HtmlUtil.printHR(out);
        out.println("\n<h4> Reconfig - Attribute max-pool-size test </h4>");

        boolean passed = true;
        Connection[] conns = new Connection[maxPoolSize];
        for( int i = 0; i < maxPoolSize; i++ ) {
            out.println("\nthrowException is : " + throwException );
            try {
                out.println("\nGetting connection : " + i );
                conns[i] = ds.getConnection();
                out.println("Connection Got : " + conns[i]);
            } catch (Exception e) {
                out.println("\nCaught exception (First try)");
                HtmlUtil.printException(e, out);
                e.printStackTrace();
                return false;
            }
        }

        //try getting an extra connection
        out.println("\nTry getting extra connection");
        Connection con = null;
        try {
            con = ds.getConnection();
            out.println("Got Connection : " + con);
        } catch( Exception e) {
            out.print("\nCaught exception" ) ;
            if ( throwException ) {
                passed = true;
            } else {
                passed = false;
            }
        } finally {
            try { con.close(); } catch ( Exception e ) {}
            for (int i = 0 ; i < maxPoolSize;i++ ) {
                try {
                    conns[i].close();
                } catch( Exception e) {
                }
            }
        }
        HtmlUtil.printHR(out);
        return passed;
    }

    private boolean testPropertyChange(DataSource ds, PrintWriter out, 
            boolean throwException) throws SystemException {
        HtmlUtil.printHR(out);
        out.println("\n<h4> Reconfig - Pool property change test </h4>");

        boolean passed = true;
        Connection con = null;
        out.println("\nthrowException is : " + throwException );
        try {
            out.println("\nGetting connection... ");
            con = ds.getConnection();
            out.println("Connection Got : " + con);
        } catch (Exception e) {
            out.println("\nCaught exception !!!");
            if ( throwException ) {
                passed = true;
            } else {
                passed = false;
            }
        } finally {
            try { con.close(); } catch ( Exception e ) {}            
        }
        HtmlUtil.printHR(out);
        return passed;        
    }
}
