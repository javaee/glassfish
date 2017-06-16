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
import javax.transaction.UserTransaction;
import org.glassfish.jdbc.devtests.v3.util.HtmlUtil;
import org.glassfish.jdbc.devtests.v3.util.TablesUtil;

/**
 *
 * @author jagadish
 */
public class SimpleXATest implements SimpleTest  {

    Map<String, Boolean> resultsMap = new HashMap<String, Boolean>();

    public Map<String, Boolean> runTest(DataSource ds, PrintWriter out) {
    try {
            if (testXA(ds, out)) {
                resultsMap.put("simple-xa-test", true);
            }else{
                resultsMap.put("simple-xa-test", false);
            }
        } catch (Exception e) {
            resultsMap.put("simple-xa-test", false);
        }

    return resultsMap;

    }

    private boolean testXA(DataSource ds, PrintWriter out) {
        UserTransaction uTx = null;
        DataSource callFlowDS = null;
        String tableName1 = "xa_ds_table1";
        String tableName2 = "xa_ds_table2";

        Connection con1 = null;
        Connection con2 = null;

        Statement stmt1 = null;
        Statement stmt2 = null;


        try{
        InitialContext ic = new InitialContext();
        callFlowDS = (DataSource)ic.lookup("jdbc/jdbc-simple-xa-test-resource-2");


        String columnName = "xa_ds_col";

        TablesUtil.createTables(ds,out, tableName1, columnName);
        TablesUtil.createTables(callFlowDS,out, tableName2, columnName);

        //con1 = ds.getConnection();
        //con2 = callFlowDS.getConnection();


        uTx = (UserTransaction)ic.lookup("java:comp/UserTransaction");

        out.println("got UserTransaction") ;


        uTx.begin();


        TablesUtil.insertEntry(ds, out, tableName1, "entry1");
        TablesUtil.insertEntry(callFlowDS, out, tableName2, "entry1");
        out.println("inserted entries <br>") ;

  /*      stmt1 = con1.createStatement();
        stmt2 = con2.createStatement();


        stmt1.executeQuery("select * from sys.systables");
        stmt2.executeQuery("select * from sys.systables");
        out.println("got both the resultsets<br>") ; */

        uTx.commit();
        out.println("able to commit") ;

        }catch(Exception e){
            HtmlUtil.printException(e, out);
            try{
                uTx.rollback();
            }catch(Exception e1){
                HtmlUtil.printException(e1, out);
            }
            return false;
        }finally{

            if(stmt1 != null){
                try{
                    stmt1.close();
                }catch(Exception e){}
            }


            if(stmt2 != null){
                try{
                    stmt2.close();
                }catch(Exception e){}
            }

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


            if(ds != null)
                TablesUtil.deleteTables(ds, out, tableName1);
            if(callFlowDS != null)
                TablesUtil.deleteTables(callFlowDS, out, tableName2);
        }
        return true;
    }

}
