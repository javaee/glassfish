
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
public class LazyConnectionAssociationTest implements SimpleTest {

    Map<String, Boolean> resultsMap = new HashMap<String, Boolean>();

    public Map<String, Boolean> runTest(DataSource ds1, PrintWriter out) {
        try {
            if (testLazyAssoc_1(ds1, out)) {
                resultsMap.put("lazy-connection-association", true);
            }else{
                resultsMap.put("lazy-connection-association", false);
            }
        } catch (Exception e) {
            resultsMap.put("lazy-connection-association", false);
        }
        return resultsMap;
    }

    /**
     * acquire specified number of connections and <b>do not</b> close it, so that further requests on this test
     * should still pass as lazy-assoc is <b>ON</b>
     * @param i
     * @param ds
     */
    private void acquireConnections(int count, DataSource ds, PrintWriter out) 
            throws Exception{
        
            for(int i=0; i<count ; i++){
                ds.getConnection();
            }
    }

    private boolean testLazyAssoc_1(DataSource ds1, PrintWriter out)
            throws SystemException {

        boolean pass = false;
        HtmlUtil.printHR(out);
        out.println("<h4> Lazy connection association test </h4>");
        try{
            acquireConnections(32, ds1, out);
            pass = true;
        }catch(Exception e){
            HtmlUtil.printException(e, out);
        }
        HtmlUtil.printHR(out);

        return pass;
    }
}
