/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

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
