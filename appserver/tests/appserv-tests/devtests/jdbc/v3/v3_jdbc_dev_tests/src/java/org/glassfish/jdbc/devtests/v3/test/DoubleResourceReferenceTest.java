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
