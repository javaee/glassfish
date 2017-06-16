/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.jdbc.devtests.v3.test;


import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import org.glassfish.jdbc.devtests.v3.util.HtmlUtil;

/**
 *
 * @author jagadish
 */
public class DoubleResourceReferenceTest implements SimpleTest {

    Map<String, Boolean> resultsMap = new HashMap<String, Boolean>();

    public Map<String, Boolean> runTest(DataSource ds, PrintWriter out) {
        try {
            if (testDoubleResourceReference(ds, out)) {
                resultsMap.put("Double-resource-reference-test", true);
            }else{
                resultsMap.put("Double-resource-reference-test", false);
            }
        } catch (Exception e) {
            resultsMap.put("Double-resource-reference-test", false);
        }


        HtmlUtil.printHR(out);
        return resultsMap;
    }

    /**
     * test whether two resources referring to same pool does not cause pool over-write.
     * @param ds1
     * @param out
     * @return
     */
    private boolean testDoubleResourceReference(DataSource ds1, PrintWriter out) {

        boolean result = false;
        Connection con1 = null;
        Connection con2 = null;
        Statement stmt1 = null;
        Statement stmt2 = null;
        try{
        //Initialize pool via first resource
        con1 = ds1.getConnection();

        InitialContext ic = new InitialContext();
        DataSource ds2 = (DataSource) ic.lookup("jdbc/double-resource-reference-resource-2");

        //Initialize (or reuse) pool via first resource
        con2 = ds2.getConnection();

        //If this passes, pool is reused by both resources, no over-write happens
        stmt1 = con1.createStatement();

        stmt2 = con2.createStatement();

        result = true;
        }catch(Exception e){
            HtmlUtil.printException(e, out);

        }finally{
            if(stmt1 != null){
                try{
                    stmt1.close();
                }catch(Exception e){}
            }
            if(con1 != null){
                try{
                    con1.close();
                }catch(Exception e){}
            }
            if(stmt2 != null){
                try{
                    stmt2.close();
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
