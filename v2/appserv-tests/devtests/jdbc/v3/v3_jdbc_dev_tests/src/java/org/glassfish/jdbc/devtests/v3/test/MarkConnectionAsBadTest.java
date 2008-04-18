package org.glassfish.jdbc.devtests.v3.test;

import com.sun.faces.util.HtmlUtils;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import org.glassfish.jdbc.devtests.v3.util.HtmlUtil;

/**
 *
 * @author jagadish
 */
public class MarkConnectionAsBadTest implements SimpleTest {

    Map<String, Boolean> resultsMap = new HashMap<String, Boolean>();

    public Map<String, Boolean> runTest(DataSource ds1, PrintWriter out) {
        try {
            if (markConnectionAsBad_test_1(ds1, out)) {
                resultsMap.put("mark-connection-as-bad-1", true);
            } else {
                resultsMap.put("mark-connection-as-bad-1", false);
            }
        } catch (Exception e) {
            resultsMap.put("mark-connection-as-bad-1", false);
        }

        try {
            if (markConnectionAsBad_test_2(ds1, out)) {
                resultsMap.put("mark-connection-as-bad-2", true);
            } else {
                resultsMap.put("mark-connection-as-bad-2", false);
            }
        } catch (Exception e) {
            resultsMap.put("mark-connection-as-bad-2", false);
        }

        HtmlUtil.printHR(out);
        return resultsMap;

    }

    public boolean markConnectionAsBad_test_1(DataSource ds, PrintWriter out) {
        boolean result = true;
        Connection physicalConnection = null;
        Set physicalConnections = new HashSet();
        com.sun.appserv.jdbc.DataSource ds1 = (com.sun.appserv.jdbc.DataSource) ds;
        Connection[] conn = new Connection[32];
        out.println("<h4> Mark connection as bad - Test1 </h4>");

        for (int i = 0; i < 32; i++) {

            try {
                conn[i] = ds.getConnection();
                physicalConnections.add(ds1.getConnection(conn[i]));
                
            } catch (Exception e) {
                HtmlUtil.printException(e, out);
                result = false;
            }
        }

        for (int i = 0; i < 32; i++) {
            if (conn[i] != null) {
                try {
                    ds1.markConnectionAsBad(conn[i]);
                    conn[i].close();
                } catch (Exception e1) {
                    HtmlUtil.printException(e1, out);
                    return false; //TEMPORARY;
                //result = false;
                }
            }
        }


        for (int i = 0; i < 32; i++) {

            try {
                conn[i] = ds.getConnection();
                if (physicalConnections.contains(ds1.getConnection(conn[i]))) {
                    result = false;
                }

            } catch (Exception e) {
                HtmlUtil.printException(e, out);
                result = false;

            }
        }

        for (int i = 0; i < 32; i++) {
            if (conn[i] != null) {
                try {
                    conn[i].close();
                } catch (Exception e1) {
                    HtmlUtil.printException(e1, out);
                    result = false;
                }
            }
        }
        out.println("<br> Test result : " + result);
        return result;
    }

    public boolean markConnectionAsBad_test_2(DataSource ds, PrintWriter out) {
        boolean result = true;
        Connection physicalConnection = null;
        Set physicalConnections = new HashSet();
        com.sun.appserv.jdbc.DataSource ds1 = (com.sun.appserv.jdbc.DataSource) ds;
        Connection[] conn = new Connection[32];
        out.println("<h4> Mark connection as bad - Test2 </h4>");

        for (int i = 0; i < 32; i++) {

            try {
                conn[i] = ds.getConnection();
                physicalConnections.add(ds1.getConnection(conn[i]));
                
            } catch (Exception e) {
                HtmlUtil.printException(e, out);
                result = false;
            }
        }

        for (int i = 0; i < 32; i++) {
            if (conn[i] != null) {
                try {
                    conn[i].close();
                } catch (Exception e1) {
                    HtmlUtil.printException(e1, out);
                    result = false;
                }
            }
        }


        for (int i = 0; i < 32; i++) {

            try {
                conn[i] = ds.getConnection();
                if (!physicalConnections.contains(ds1.getConnection(conn[i]))) {
                    result = false;
                }

            } catch (Exception e) {
                HtmlUtil.printException(e, out);
                result = false;

            }
        }

        for (int i = 0; i < 32; i++) {
            if (conn[i] != null) {
                try {
                    conn[i].close();
                } catch (Exception e1) {
                    HtmlUtil.printException(e1, out);
                    result = false;
                }
            }
        }
        out.println("<br> Test result : " + result);
        return result;
    }
}
