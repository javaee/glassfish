package org.glassfish.jdbc.devtests.v3.test;

import java.io.PrintWriter;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.glassfish.jdbc.devtests.v3.util.HtmlUtil;

/**
 *
 * @author jagadish
 */
public class LeakTest implements SimpleTest{

    
    Map<String, Boolean> resultsMap = new HashMap<String, Boolean>();

    public Map<String, Boolean> runTest(DataSource ds1, PrintWriter out) {
        try {
            if (checkForNoLeak(ds1, out)) {
                resultsMap.put("no-leak-test", true);
            }else{
                resultsMap.put("no-leak-test", false);
            }
        } catch (Exception e) {
            resultsMap.put("no-leak-test", false);
        }
        HtmlUtil.printHR(out);
        return resultsMap;

    }
    private boolean checkForNoLeak(DataSource ds1, PrintWriter out) {
        int count = 32;
        boolean result = false;
        Connection[] connections = new Connection[count];

        out.println("<h4> No leak test </h4>");
        try {
            for (int i = 0; i < count; i++) {
                connections[i] = ds1.getConnection();
            }
            out.println("<br>able to retrieve all 32 connections");
            result = true;
        } catch (Exception e) {
            HtmlUtil.printException(e, out);
            result = false;
        } finally {

            try {
                for (Connection con : connections) {
                    try {
                        if (con != null) {
                            con.close();
                        }
                    } catch (Exception e) {

                    }
                }
            } catch (Exception e) {
                HtmlUtil.printException(e, out);
            }
            out.println("<br> Test result : " + result);
            return result;
        }
    }
}
