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

package com.sun.s1asdev.jdbc.markconnectionasbad.xa.client;

import javax.naming.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import java.util.Set;
import java.util.HashSet;
import java.sql.*;

public class Client {

    SimpleReporterAdapter stat = new SimpleReporterAdapter();
    public static final int MAX_POOL_SIZE = 5;


    public static void main(String[] args)
            throws Exception {

        Client client = new Client();
        client.runTest();
    }

    public void runTest() throws Exception {


	Set<Integer> localdsSet = new HashSet();
	Set<Integer> localdsAfterSet = new HashSet();
	int countLocalds = 0;

        InitialContext ic = new InitialContext();
	com.sun.appserv.jdbc.DataSource ds = (com.sun.appserv.jdbc.DataSource) ic.lookup("jdbc/localdatasource");
        
	stat.addDescription("DMMCF Mark-Connection-As-Bad Appclient ");


	localdsSet = getFromLocalDS(MAX_POOL_SIZE, ds);
	System.out.println("localds = " + localdsSet);
	
	//jdbc-local-pool
        test1(ds);

	localdsAfterSet = getFromLocalDS(MAX_POOL_SIZE, ds);
	System.out.println("localdsAfter = " + localdsAfterSet);

	countLocalds = compareAndGetCount(localdsSet, localdsAfterSet);
	if(MAX_POOL_SIZE-countLocalds == 5) {
            stat.addStatus(" Mark-Connection-As-Bad destroyedCount localds: ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad destroyedCount localds: ", stat.FAIL);
        }

        stat.printSummary();
    }

    public int compareAndGetCount(Set<Integer> beforeSet, Set<Integer> afterSet) {
        //denotes the count of hashcodes that matched in both sets.
        int contains = 0;	    
	if(!beforeSet.containsAll(afterSet)) {
            //if it does not contain all the elements of the after set
	    //find how many are absent from the beforeSet
            for(int afterInt : afterSet) {
                    if(beforeSet.contains(afterInt)) {
	                    contains++;
	                }
            }		    
	}
        return contains;
    }

    public Set<Integer> getFromLocalDS(int count, com.sun.appserv.jdbc.DataSource localds1) {
        int connHashCode = 0;	
        Connection conn = null;
	Set<Integer> hashCodeSet = new HashSet();
        for (int i = 0; i < count; i++) {
	    try {
		conn = localds1.getNonTxConnection();
		connHashCode = (localds1.getConnection(conn)).hashCode();
                hashCodeSet.add(connHashCode);
            } catch (Exception e) {

            } finally {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (Exception e1) {
                    }
                }
            }
	}
	return hashCodeSet;
    }

    /* Read Operation - Driver  - shareable */
    public boolean test1(com.sun.appserv.jdbc.DataSource localds1) {

        boolean passed = true;
        for (int i = 0; i < 5; i++) {
            Connection conn = null;
            try {
                conn = localds1.getConnection();
            } catch (Exception e) {
                passed = false;
            } finally {
                if (conn != null) {
                    try {
                        localds1.markConnectionAsBad(conn);
                        conn.close();
                    } catch (Exception e1) {
                    }
                }
            }
        }

        return passed;
    }

}
