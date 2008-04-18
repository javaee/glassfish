package org.glassfish.jdbc.devtests.v3.test;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.transaction.SystemException;
import org.glassfish.jdbc.devtests.v3.util.HtmlUtil;

/**
 *
 * @author jagadish
 */
public class ConnectionSharingTest implements SimpleTest{

    
    Map<String, Boolean> resultsMap = new HashMap<String, Boolean>();

    public Map<String, Boolean> runTest(DataSource ds1, PrintWriter out) {
        HtmlUtil.printHR(out);
        try {
            if (testConnectionSharing(ds1, out)) {
                resultsMap.put("connection-sharing", true);
            }else{
                resultsMap.put("connection-sharing", false);
            }
        } catch (Exception e) {
            resultsMap.put("connection-sharing", false);
        }
        HtmlUtil.printHR(out);
        return resultsMap;
    }

    
     private boolean testConnectionSharing(DataSource ds1, PrintWriter out) throws SystemException {
        boolean result = false;
        Connection con = null;
        Connection con1 = null;
        Statement stmt = null;
        ResultSet rs = null;

        out.println("<h4> connection-sharing test </h4>");
        javax.transaction.UserTransaction ut = null;
        try {
            out.println("<br>Starting test ...");
            InitialContext ic = new InitialContext();
            ut = (javax.transaction.UserTransaction) ic.lookup("java:comp/UserTransaction");
            out.println("<br>Able to lookup UserTransaction");
            ut.begin();
            out.println("<br> Started UserTransaction");

            out.println("<br>Trying to get connection ...");

            out.println("<br>ds value : " + ds1);
            con = ds1.getConnection();
            com.sun.appserv.jdbc.DataSource myDS = ((com.sun.appserv.jdbc.DataSource) ds1);
            Connection con_ = myDS.getConnection(con);
            out.println("<br>Got connection - con : " + con_);

            con1 = ds1.getConnection();
            Connection con1_ = myDS.getConnection(con1);
            out.println("<br> Got connection - con1 : " + con1_);
            if (con1_ == con_) {
                result = true;
            }

            ut.commit();


        } catch (Throwable e) {
            HtmlUtil.printException(e, out);
            out.println("Rolling back transaction<br>");
            ut.rollback();
            result = false;
        } finally {

            try {
                if (con1 != null) {
                    con1.close();
                }
            } catch (Exception e) {
                HtmlUtil.printException(e, out);
            }

            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception e) {
                HtmlUtil.printException(e, out);
            }
            
            out.println("<br> Test result : " + result);
            return result;
        }
    }
}
