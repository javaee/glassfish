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

package com.sun.s1asdev.jdbc.statementwrapper.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;

import com.sun.s1asdev.jdbc.statementwrapper.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.statementwrapper.ejb.SimpleBMP;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;

public class SimpleBMPClient {

    public static SimpleReporterAdapter stat = new SimpleReporterAdapter();
    public static final Object lock = new Object();
    public static int NO_OF_THREADS = 5; 
    public static final int JMX_PORT = 8686;
    public static final String HOST_NAME = "localhost";
    public long sumTotal = 0;
    public boolean successExecuting = true;

    public SimpleBMPClient(int option){
	if(option == 1) {
            execute();
	} else if(option == 2) {
	    try {
	        getMonitorablePropertyOfConnectionPool("ql-jdbc-pool", "frequsedsqlqueries");
	    } catch(Exception ex) {
                ex.printStackTrace();
	    }
	}
    }

    private void execute() {

        WorkerThread workers[] = new WorkerThread[NO_OF_THREADS];
        for(int i=0; i< workers.length;i++){
            int j = i+1;
            //System.out.println("Thread T" + j + " to be executed for " + j + " times");
            workers[i] = new WorkerThread(j, "customer_stmt_wrapper"+j);
        }

        for(int i=0; i<workers.length;i++){
            workers[i].start();
        }

        if(successExecuting) {
	    stat.addStatus("SQL Trace monitoring Test1 ", stat.PASS);
	} else {
	    stat.addStatus("SQL Trace monitoring Test1 ", stat.FAIL);
	}
        stat.printSummary();
    }

    public static void main(String[] args)
        throws Exception {

	stat.addDescription("SQL Tracing tests");
        if (args != null && args.length > 0) {
            String param = args[0];

            switch (Integer.parseInt(param)) {
                case 1: {//run the threads
		    new SimpleBMPClient(1);
		    break;
		}
		case 2: { //compare monitoring stats
                    new SimpleBMPClient(2);
		    break;
		}
	    }
	}
    }

    class WorkerThread extends Thread{

	private int timesToExecuteQuery = 0;

        public WorkerThread(int noExecs, String name){
	    super(name);
	    timesToExecuteQuery = noExecs;
        }

        public void run() {
            try{
                InitialContext ic = new InitialContext();
                Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
                SimpleBMPHome simpleBMPHome = (SimpleBMPHome)
                javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

                SimpleBMP simpleBMP = simpleBMPHome.create();
		String tableName = Thread.currentThread().getName();
		String tableValue = "iiop";
		for(int i=0; i<timesToExecuteQuery; i++) {
                    simpleBMP.preparedStatementTest1(tableName, tableValue);
		}
            }catch(Exception e){
                System.out.println("Thread : " + Thread.currentThread().getName() + "did not run ");
                e.printStackTrace();
		SimpleBMPClient.this.successExecuting = false;
            }
        }
    }

    public void getMonitorablePropertyOfConnectionPool(String poolName, String monitoringStat) throws Exception {

	boolean passed = false;
	final String urlStr = "service:jmx:rmi:///jndi/rmi://" + HOST_NAME + ":" + JMX_PORT + "/jmxrmi";    
        final JMXServiceURL url = new JMXServiceURL(urlStr);

	final JMXConnector jmxConn = JMXConnectorFactory.connect(url);
	final MBeanServerConnection connection = jmxConn.getMBeanServerConnection();

        ObjectName objectName =
                new ObjectName("amx:pp=/mon/server-mon[server],type=jdbcra-mon,name=resources/" + poolName);

	javax.management.openmbean.CompositeDataSupport returnValue = 
		(javax.management.openmbean.CompositeDataSupport) 
		connection.getAttribute(objectName, monitoringStat);

	String freqUsedQueries = (String) returnValue.get("current");
	String delimiter = "%%%EOL%%%";
	StringTokenizer st = new StringTokenizer(freqUsedQueries, delimiter);

	List<String> queryList = new LinkedList<String>();
	List<String> expectedQueryList = new LinkedList<String>();
	expectedQueryList.add("select * from customer_stmt_wrapper5 where c_phone= ?");
	expectedQueryList.add("select * from customer_stmt_wrapper4 where c_phone= ?");
	expectedQueryList.add("select * from customer_stmt_wrapper3 where c_phone= ?");

	while(st.hasMoreTokens()) {
	    String query = st.nextToken().trim();
	    if(!query.equals("")) {
		queryList.add(query);
	    }
	}
	/*for(int k=0; k<queryList.size(); k++) {
	    System.out.println(">>>>> query=" + queryList.get(k));
	}*/

	passed = expectedQueryList.equals(queryList);
		            
	if(passed) {
	    stat.addStatus("SQL Trace monitoring Test2 ", stat.PASS);
	} else {
	    stat.addStatus("SQL Trace monitoring Test2 ", stat.FAIL);
	}
        stat.printSummary();
    }

}
