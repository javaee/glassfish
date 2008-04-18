package org.glassfish.jdbc.devtests.v3.test;

import java.io.PrintWriter;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.glassfish.jdbc.devtests.v3.util.HtmlUtil;

/**
 *
 * @author shalini
 */
public class ContainerAuthTest implements SimpleTest {

    Map<String, Boolean> resultsMap = new HashMap<String, Boolean>();
    
    public Map<String, Boolean> runTest(DataSource ds, PrintWriter out) {
        try {
            if (testContAuthUserPass(ds, out)) {
                resultsMap.put("jdbc-cont-auth-test1", true);
            }else{
                resultsMap.put("jdbc-cont-auth-test1", false);
            }
        } catch (Exception e) {
            resultsMap.put("jdbc-cont-auth-test1", false);
        }
        try {
            if (testContAuthNoUserPass(ds, out)) {
                resultsMap.put("jdbc-cont-auth-test2", true);
            }else{
                resultsMap.put("jdbc-cont-auth-test2", false);
            }
        } catch (Exception e) {
            resultsMap.put("jdbc-cont-auth-test2", false);
        }

        HtmlUtil.printHR(out);
        return resultsMap;                
    }

    /**
     * Tests Container Authentication when no username/password are specified.
     * @param ds
     * @param out
     * @return boolean result
     */
    private boolean testContAuthNoUserPass(DataSource ds, PrintWriter out) {
        Connection con = null;
        boolean passed = true;
        out.println("<h4>Container Auth Test - No username/password</h4>");
        try {
            out.print("<br> Getting a connection ...");
            con = ds.getConnection();
        } catch(Exception ex) {
            HtmlUtil.printException(ex, out);
            passed = false;
        } finally {
            if(con != null) {
                try {
                    con.close();
                } catch(Exception ex) {
                    HtmlUtil.printException(ex, out);
                }
            }
            out.println("<br> Test result : " + passed);
            return passed;
        }
    }

    /**
     * Tests Container Authentication when username/password are specified
     * while getting a connection.
     * @param ds
     * @param out
     * @return boolean result
     */
    private boolean testContAuthUserPass(DataSource ds, PrintWriter out) {
        Connection con = null;
        boolean passed = true;
        out.println("<h4> Container Authentication Test - username/password specified </h4>");
        try {
            out.println("<br> Getting a connection with username/password");
            con = ds.getConnection("DBUSER", "DBPASSWORD");
        } catch(Exception ex) {
            HtmlUtil.printException(ex, out);
            passed = false;
        } finally {
            if(con != null) {
                try {
                    con.close();
                } catch(Exception ex) {
                    HtmlUtil.printException(ex, out);
                }
            }
            out.println("<br> Test result : " + passed);
            return passed;
        }
    }
}
