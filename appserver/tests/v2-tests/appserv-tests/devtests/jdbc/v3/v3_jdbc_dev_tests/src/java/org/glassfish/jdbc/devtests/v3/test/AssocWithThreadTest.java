/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

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
public class AssocWithThreadTest implements SimpleTest{

    
        Map<String, Boolean> resultsMap = new HashMap<String, Boolean>();

    public Map<String, Boolean> runTest(DataSource ds1, PrintWriter out) {
        try {
            if (testAssocWithThread(ds1, out)) {
                resultsMap.put("assoc-with-thread", true);
            }else{
                resultsMap.put("assoc-with-thread", false);
            }
        } catch (Exception e) {
            resultsMap.put("assoc-with-thread", false);
        }
        return resultsMap;
    }
    
    private boolean testAssocWithThread(DataSource ds1, PrintWriter out) throws SystemException {

        
        HtmlUtil.printHR(out);
        out.println("<h4> Assoc-with-thread test </h4>");

        String result1 = test1(ds1, out);
        String result2 = test1(ds1, out);

        out.println("physical connection 1 : " + result1);
        out.println("physical connection 2 : " + result2);

        HtmlUtil.printHR(out);

        // check whether the connection acquired during two different method invocation, by a thread is same.
        if(result1.equalsIgnoreCase(result2)){
            return true;
        }else{
            return false;
        }
        
    }

    
     private String test1(DataSource ds1, PrintWriter out) throws SystemException {
        String result = null;
        Connection con = null;

        try {
            out.println("<br>");
            out.println("<h4> Starting test </h4>");
            InitialContext ic = new InitialContext();
            out.println("<br>");
            //pool is non-transactional so that sharing won't happen
            con = ds1.getConnection();
            com.sun.appserv.jdbc.DataSource myDS = ((com.sun.appserv.jdbc.DataSource) ds1);
            Connection con_ = myDS.getConnection(con);
            out.println("Thread [ " + Thread.currentThread().getName() +" ] Got connection - con : " + con_);
            result = con_.toString();
            out.println("<br>");

            out.println("<br>");
        } catch (Throwable e) {
            HtmlUtil.printException(e, out);
            result = null;
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception e) {
                HtmlUtil.printException(e, out);
            }
            return result;
        }
    }
}
