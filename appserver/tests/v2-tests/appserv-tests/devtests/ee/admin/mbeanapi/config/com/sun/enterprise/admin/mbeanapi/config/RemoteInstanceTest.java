/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.mbeanapi.config;

import com.sun.enterprise.admin.mbeanapi.common.*;
import com.sun.appserv.management.util.misc.ExceptionUtil;
import com.sun.appserv.management.config.PropertiesAccess;
import com.sun.appserv.management.config.ServerConfigKeys;


import java.io.*;
import java.util.*;



/**
 * Creates, starts, stops and deletes standalone instance(s) in a
 * particular administrative domain
 * @author <a href=mailto:shreedhar.ganapathy@sun.com>Shreedhar Ganapathy</a>
 *         Date: Aug 26, 2004
 * @version $Revision: 1.1 $
 */
public class RemoteInstanceTest {
    StandaloneInstanceManager sim;
    protected static final String LIST_INSTANCES_COMMAND = "asadmin list-instances";
    protected static final String START_NODE_AGENT_COMMAND = "asadmin start-node-agent";
    protected static final Object CREATE_NODE_AGENT_COMMAND = "asadmin create-node-agent";
    protected static final Object LIST_NODE_AGENTS_COMMAND = "asadmin list-node-agents";
    protected static final String STOP_NODE_AGENT_COMMAND = "asadmin stop-node-agent";
    protected static final String DELETE_NODE_AGENT_COMMAND = "asadmin delete-node-agent";
    protected static final String DELETE_NODE_AGENT_CONFIG_COMMAND = "asadmin delete-node-agent-config";
    SortedMap summary = new TreeMap();

    public RemoteInstanceTest(){
        try {
            sim = new StandaloneInstanceManager(getHost(),
                                                getAMXPort(),
                                                getAdminUser(),
                                                getAdminPassword(),
                                                getUseTLS() );
            summary.put("TOTAL AMX TESTS EXPECTED TO RUN", new Integer(getNumInstances()*4));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public int runTest()  throws TestFailedException{
        int result;
        result = createInstance();
        if(result == 1){
            printSummaryTestResults();
            throw new TestFailedException("Create Instance test failed");
        }

        result = startInstance();
        if(result == 1){
            printSummaryTestResults();
            throw new TestFailedException("Start Instance test failed");
        }

        result = stopInstance();
        if(result ==1){
            printSummaryTestResults();
            throw new TestFailedException("Stop Instance test failed");
        }

        result = deleteInstance();
        if(result ==1){
            printSummaryTestResults();
            throw new TestFailedException("Delete Instance test failed");
        }

        printSummaryTestResults();
        return 0;
    }

    private void printSummaryTestResults() {
        for(Iterator i = summary.entrySet().iterator(); i.hasNext();){
            System.out.println(i.next().toString());
        }
    }

    protected int createInstance(){
        for(int i = 0; i< getNumInstances(); i++){
            try {
               sim.createInstance(getInstanceNames()[i],
                                getNodeAgentName(),
                                getConfigName(),
                                null);//getOptionalParameters(i+1) );
            } catch (Exception e){
                final Throwable t = ExceptionUtil.getRootCause(e);
                if(t.getMessage() != null){
                    if(t.getMessage().indexOf("WARNING")<0){
                        System.out.println(t.getMessage());
                        return 1;
                    }
                }else{
                    e.printStackTrace();
                }
            }
            System.out.println("Instance Creation for "+ getInstanceNames()[i] + " completed.");
        }
        return 0;
    }

    protected int startInstance() {
        for(int i = 0; i< getNumInstances(); i++){
            final String instanceName = getInstanceNames()[i];
            try {
                sim.startInstance(instanceName);
            } catch (Exception e) {
                e.printStackTrace();
                return 1;
            }
            System.out.println("Starting of instance " + instanceName +" completed.");
        }
        return 0;
    }

    protected int stopInstance() {
        for(int i = 0; i< getNumInstances(); i++){
            final String instanceName = getInstanceNames()[i];
            try {
                sim.stopInstance(instanceName);
            } catch (Exception e) {
                e.printStackTrace();
                return 1;
            }
            System.out.println("Stopping of instance " + instanceName +" completed.");
        }
        return 0;
    }

    protected int deleteInstance() {
        for(int i = 0; i< getNumInstances(); i++){
            final String instanceName = getInstanceNames()[i];
            try {
            System.out.println("Deleting instance "+instanceName);
                sim.deleteInstance(instanceName);
            } catch (Exception e) {
                e.printStackTrace();
                return 1;
            }
            System.out.println("Deleting of instance " + instanceName +" completed.");
        }
        return 0;
    }

    protected String getHost() {
        return System.getProperty("HOST", "localhost");
    }

    protected int getAMXPort() {
        return Integer.parseInt(System.getProperty("AMX_PORT","8686"));
    }

    protected int getAdminPort() {
        return Integer.parseInt(System.getProperty("ADMIN_PORT","4848"));
    }

    protected String getAdminUser() {
        return System.getProperty("ADMIN_USER", "admin");
    }

    protected String getAdminPassword() {
        return System.getProperty("ADMIN_PASSWORD", "adminadmin");
    }

    protected boolean getUseTLS() {
        return Boolean.valueOf(System.getProperty("USE_TLS", "false")).booleanValue();
    }

    protected String[] getInstanceNames() {
        final int num = getNumInstances();
        final String[] instances = new String[num];
        for (int i = 0; i < num; i++){
            instances[i] = "instance"+(1+i);
        }
        return instances;
    }

    protected int getNumInstances() {
        return Integer.parseInt(System.getProperty("NUM_INSTANCES", "1"));
    }

    protected String getNodeAgentName() {
        return System.getProperty("NODE_AGENT");
    }

    protected String getConfigName() {
        return System.getProperty("CONFIG_NAME");
    }

    //Properties should be specified as a SystemProperty "PROPERTIES" with its
    // values following the pattern "key=value:key=value......"
    //For each instance number passed in to this method, the port values will be incremented by
    //that number here for each value provided through PROPERTIES system prop
    protected Map getOptionalParameters(final int instanceNum) throws Exception {
        final Map properties = new HashMap();
        final String props = System.getProperty("PROPERTIES");
        if(props == null)
            return null;
        final String[] specs = props.split(":");
        String key, value;
        for(int i=0; i<specs.length;i++){
            key = specs[i].substring(0, specs[i].indexOf("=",0));
            value = specs[i].substring(specs[i].indexOf("=")+1,specs[i].length());
            key = getLegalKey(key);
            if(key!=null){
                properties.put(key, ""+(Integer.parseInt(value)+instanceNum));
            }
            else{
                throw new Exception("PROPERTIES:property key did not match legal key");
            }
        }
        System.out.println(properties.toString());
        return properties;
    }

    private String getLegalKey(String key) {
        String[] legalKeys = {
                ServerConfigKeys.HTTP_LISTENER_1_PORT_KEY,
                ServerConfigKeys.HTTP_LISTENER_2_PORT_KEY,
                ServerConfigKeys.ORB_LISTENER_1_PORT_KEY,
                ServerConfigKeys.SSL_PORT_KEY,
                ServerConfigKeys.SSL_MUTUALAUTH_PORT_KEY,
                ServerConfigKeys.JMX_SYSTEM_CONNECTOR_PORT_KEY,
            };

        for(int i=0; i<legalKeys.length;i++){
            if(legalKeys[i].matches(PropertiesAccess.PROPERTY_PREFIX+key ))
                return legalKeys[i];
        }
        return null;
    }

    public static void main(final String[] args){
        final RemoteInstanceTest test  = new RemoteInstanceTest();
        try {
            test.runTest();
        } catch (TestFailedException e) {
            e.printStackTrace();
        }
    }
}
