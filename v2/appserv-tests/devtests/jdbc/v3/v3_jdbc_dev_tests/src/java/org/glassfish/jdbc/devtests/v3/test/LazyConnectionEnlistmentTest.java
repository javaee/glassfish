/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.jdbc.devtests.v3.test;

import java.io.PrintWriter;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;
import org.glassfish.jdbc.devtests.v3.util.HtmlUtil;

/**
 *
 * @author jagadish
 */
public class LazyConnectionEnlistmentTest implements SimpleTest {

    Map<String, Boolean> resultsMap = new HashMap<String, Boolean>();
    UserTransaction uTx;
    InitialContext ic;

    public Map<String, Boolean> runTest(DataSource ds1, PrintWriter out) {
        try {
            if (testLazyEnlist_1(ds1, out)) {
                resultsMap.put("lazy-connection-enlistment", true);
            }else{
                resultsMap.put("lazy-connection-enlistment", false);
            }
        } catch (Exception e) {
            resultsMap.put("lazy-connection-enlistment", false);
        }
        return resultsMap;
    }

    private boolean testLazyEnlist_1(DataSource ds1, PrintWriter out) {
        Connection con1 = null;
        Connection con2 = null;
        boolean result = false;
        try{
            ic = new InitialContext();
            uTx = (UserTransaction)ic.lookup("java:comp/UserTransaction");

            out.println("got UserTransaction") ;
            uTx.begin();
            con1 = ds1.getConnection();

            DataSource ds2 = (DataSource)ic.lookup("jdbc/jdbc-lazy-enlist-resource-2");
            con2 = ds2.getConnection();

            uTx.commit();
            out.println("able to commit") ;
            result = true;
        }catch(Exception e){
            HtmlUtil.printException(e, out);
        }finally{
            if(con1 != null){
                try{
                    con1.close();
                }catch(Exception e){}
            }

            if(con2 != null){
                try{
                    con2.close();
                }catch(Exception e){}
            }
        }
        return result;
    }
}
