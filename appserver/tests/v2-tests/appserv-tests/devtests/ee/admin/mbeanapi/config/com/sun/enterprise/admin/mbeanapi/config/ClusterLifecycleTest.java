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

import com.sun.enterprise.admin.mbeanapi.common.AMXConnector;
import com.sun.enterprise.admin.mbeanapi.common.ClusterManager;
import com.sun.enterprise.admin.mbeanapi.common.ClusteredInstanceManager;
import com.sun.appserv.management.util.misc.ExceptionUtil;
import com.sun.appserv.management.config.ClusterConfig;
import com.sun.appserv.management.config.ServerConfigKeys;
import com.sun.appserv.management.config.PropertiesAccess;

import java.util.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @author <a href=mailto:shreedhar.ganapathy@sun.com>Shreedhar Ganapathy</a>
 *         Date: Sep 11, 2004
 * @version $Revision: 1.5 $
 */
public class ClusterLifecycleTest {
    private AMXConnector mAmxConnector;
    private ClusterManager cm;
    private ClusteredInstanceManager cim;
    private Hashtable clusters = new Hashtable();

    protected static final String LIST_INSTANCES_COMMAND = "asadmin list-instances";
    protected static final String START_NODE_AGENT_COMMAND = "asadmin start-node-agent";
    protected static final Object CREATE_NODE_AGENT_COMMAND = "asadmin create-node-agent";
    protected static final Object LIST_NODE_AGENTS_COMMAND = "asadmin list-node-agents";
    protected static final String STOP_NODE_AGENT_COMMAND = "asadmin stop-node-agent";
    protected static final String DELETE_NODE_AGENT_COMMAND = "asadmin delete-node-agent";
    protected static final String DELETE_NODE_AGENT_CONFIG_COMMAND = "asadmin delete-node-agent-config";
    private static final String LIST_CLUSTERS_COMMAND = "asadmin list-clusters";
    private SortedMap summary = new TreeMap();

    public ClusterLifecycleTest(){
        try {
            mAmxConnector = new AMXConnector(getHost(),
                                            getAMXPort(),
                                            getAdminUser(),
                                            getAdminPassword(),
                                            getUseTLS());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        cm = new ClusterManager(mAmxConnector);
        cim = new ClusteredInstanceManager(mAmxConnector );
        summary.put("TOTAL AMX TESTS EXPECTED TO RUN", new Integer(getNumClusters()*4 + getNumInstances()*4));
    }

    public int runTest()  throws TestFailedException{
        int result;
       /* final boolean newNodeAgent = Boolean.valueOf(System.getProperty("NEW_NODE_AGENT","true")).booleanValue();

        if(newNodeAgent){
            result = createNodeAgent();
            if(result == 1){
                throw new TestFailedException("Create Node Agent Operation Failed");
            }
            result = verifyNodeAgentCreation();
            if(result == 1){
                throw new TestFailedException("Verification of Create Node Agent Operation Failed");
            }
        }*/

        result = createCluster();
        if(result == 1){
            printSummaryTestResults();
            throw new TestFailedException("Create Cluster test failed");
        }

        result = verifyClusterCreation();
        if(result == 1){
            printSummaryTestResults();
            throw new TestFailedException("Verification of Create Cluster failed");
        }

        result = createInstance();
        if(result == 1){
            printSummaryTestResults();
            throw new TestFailedException("Create Instance test failed");
        }

        result = verifyInstanceCreation();
        if(result == 1){
            printSummaryTestResults();
            throw new TestFailedException("Create Instance Verification failed");
        }

        result = startNodeAgent();
        if(result == 1) {
            printSummaryTestResults();
            throw new TestFailedException("Start NodeAgent Operation failed");
        }

        result = startCluster();
        if(result==1) {
            printSummaryTestResults();
            throw new TestFailedException("Start Cluster Operation failed");
        }
        result = verifyStartCluster();
        if(result==1) {
            printSummaryTestResults();
            throw new TestFailedException("Verification of Start Cluster Operation failed");
        }

        result = stopCluster();
        if(result==1) {
            printSummaryTestResults();
            throw new TestFailedException("Stop Cluster Operation failed");
        }

        result = verifyStopCluster();
        if(result==1) {
            printSummaryTestResults();
            throw new TestFailedException("Verification of Stop Cluster Operation failed");
        }

        result = startInstance();
        if(result == 1){
            printSummaryTestResults();
            throw new TestFailedException("Start Instance test failed");
        }

        result = verifyStartInstance();
        if(result ==1){
            printSummaryTestResults();
            throw new TestFailedException("Start Instance Verification failed");
        }

        result = stopInstance();
        if(result ==1){
            printSummaryTestResults();
            throw new TestFailedException("Stop Instance test failed");
        }

        result = verifyStopInstance();
        if(result ==1){
            printSummaryTestResults();
            throw new TestFailedException("Stop Instance Verification failed");
        }

        result = deleteInstance();
        if(result ==1){
            printSummaryTestResults();
            throw new TestFailedException("Delete Instance test failed");
        }

        result = verifyDeleteInstance();
        if(result ==1){
            printSummaryTestResults();
            throw new TestFailedException("Delete Instance Verification failed");
        }

        result = deleteCluster();
        if(result ==1){
            printSummaryTestResults();
            throw new TestFailedException("Delete Cluster test failed");
        }

        result = verifyDeleteCluster();
        if(result ==1){
            printSummaryTestResults();
            throw new TestFailedException("Delete Cluster Verification failed");
        }


        /*if(newNodeAgent){
            result = stopNodeAgent();
            if(result == 1){
                throw new TestFailedException("Stop Node Agent Operation Failed");
            }
            result = deleteNodeAgent();
            if(result == 1){
                throw new TestFailedException("Delete Node Agent Operation Failed");
            }
        } */
        printSummaryTestResults();
        return 0;
    }

    private void printSummaryTestResults() {
        for(Iterator i = summary.entrySet().iterator(); i.hasNext();){
            System.out.println(i.next().toString());
        }
    }

    protected int createNodeAgent() {
        final String nodeAgentName = getNodeAgentName();
        final String command = CREATE_NODE_AGENT_COMMAND +
                        " --host " + getHost() +
                        " --port " + getAdminPort() +
                        " --user " + getAdminUser() +
                        " --password " + getAdminPassword()+
                        " " + nodeAgentName;
        System.out.println("running command: " +command);
        final String sb = runCommand(command);
        if(sb.indexOf("successfully")>=0){
            System.out.println("Node Agent create operation for "+nodeAgentName+" completed.");
            return 0;
        }
        return 1;
    }

    protected int verifyNodeAgentCreation() {
        final String nodeAgentName = getNodeAgentName();
        final String command = LIST_NODE_AGENTS_COMMAND +
                               " --host "+ getHost() +
                               " --port "+ getAdminPort() +
                               " --user "+ getAdminUser() +
                               " --password "+ getAdminPassword() +
                               " " + nodeAgentName;
        System.out.println("running command: " +command);
        final String sb = runCommand(command);
        if(sb.indexOf(nodeAgentName)>=0){
            System.out.println("Verification for Node Agent Create Operation for "+nodeAgentName+"  completed.");
            return 0;
        }
        return 1;
    }

    private int createCluster() {
        ClusterConfig clusterConfig ;
        String clusterName;
        for(int i = 0; i< getNumClusters(); i++){
            clusterName = getClusterNames()[i];
            try {
               clusterConfig = cm.createCluster(clusterName,
                                getConfigName(),
                                null );
                clusters.put(clusterName, clusterConfig);
            } catch (Exception e){
                final Throwable t = ExceptionUtil.getRootCause(e);
                System.out.println(t.getMessage());
                return 1;
            }
            System.out.println("Cluster Creation for "+ getClusterNames()[i] + " completed.");
        }
        return 0;
    }

    private int verifyClusterCreation(){
        int retval=1;
        final int numClusters = getNumClusters();
        for(int i = 0; i< numClusters; i++){
            final String clusterName = getClusterNames()[i];
            final String sb = getListClustersCommandOutput(clusterName);
            if(sb.indexOf(clusterName)>=0){
                System.out.println("Cluster Creation Verification for "+getClusterNames()[i] + " successful.");
                summary.put("CLUSTER CREATION TEST", i+1 +" out of "+numClusters+" PASS");
                summary.put("TOTAL AMX TESTS ACUALLY RUN", new Integer(i+1));
                retval=0;
            }
            else
                retval = 1;
        }
        return retval;
    }

    protected int createInstance(){
        String clusterName;
        for(int i=0; i<getNumClusters(); i++){
            clusterName = getClusterNames()[i];
            for(int j = 0; j< getNumInstances(); j++){
                try {
                   cim.createInstance(getInstanceNames()[j],
                                    getNodeAgentName(),
                                    clusterName,
                                    getOptionalParameters(j+1)
                                    );
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
                System.out.println("Instance Creation for "+ getInstanceNames()[j] + " completed.");
            }
        }
        return 0;
    }

    protected int verifyInstanceCreation() {
        int retval=1;
        final int numInst = getNumInstances();
        for(int i = 0; i< numInst; i++){
            final String instanceName = getInstanceNames()[i];
            final String sb = getListInstancesCommandOutput(instanceName);
            if(sb.indexOf(instanceName)>=0){
                System.out.println("Instance Creation Verification for "+getInstanceNames()[i] + " successful.");
                summary.put("INSTANCE CREATION TEST", i+1 +" out of "+numInst+" PASS");
                summary.put("TOTAL AMX TESTS ACUALLY RUN", new Integer(i+1));
                retval=0;
            }
            else
                retval = 1;
        }
        return retval;
    }

    protected String getListInstancesCommandOutput(final String instanceName) {
        //this assumes asadmin is somewhere in the path
        String command = LIST_INSTANCES_COMMAND+
                                " --host "+ getHost()+
                                " --port "+ getAdminPort()+
                                " --user "+ getAdminUser()+
                                " --password " + getAdminPassword();
        if(instanceName != null)
            command += " " + instanceName;
        System.out.println("running command: " +command);
        return runCommand(command);
    }

    private String getListClustersCommandOutput(final String clusterName) {
        //this assumes asadmin is somewhere in the path
        String command = LIST_CLUSTERS_COMMAND+
                                " --host "+ getHost()+
                                " --port "+ getAdminPort()+
                                " --user "+ getAdminUser()+
                                " --password " + getAdminPassword();
        if(clusterName != null)
            command += " " + clusterName;
        System.out.println("running command: " +command);
        return runCommand(command);
    }

    protected String runCommand(final String command) {
        String sb = null;
        try {
            final Process p = Runtime.getRuntime().exec(command);
            sb = convertToString(p.getInputStream());
            System.out.println("Received Process Output String: |" +sb+"|");
            if(sb.length() == 0 ){
                sb = convertToString(p.getErrorStream());
                System.out.println("Received Process Error String: |" +sb+"|");
            }
        } catch (Exception e) {
            //System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return sb;
    }


    protected String convertToString(final InputStream in) {
        final BufferedReader br = new BufferedReader
                                (new InputStreamReader(in));
        String line;
        final StringBuffer sb = new StringBuffer();
        try {
            while ((line = br.readLine()) != null)
            {
                sb.append(line);
                sb.append(" ");
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    protected int startNodeAgent() {
        final String command = START_NODE_AGENT_COMMAND +
                " --user "+ getAdminUser()+
                " --password " + getAdminPassword()+
                " --startinstances=false"+
                " " +getNodeAgentName() ;
        System.out.println("running command: " +command);
        final String result = runCommand(command);
        if(result.indexOf("successfully")>=0 ||
                result.indexOf("running")>=0)
        {
            return 0;
        }
        return 1;
    }

    private int startCluster() {
       for(int i = 0; i< getNumClusters(); i++){
            final String clusterName = getClusterNames()[i];
            try {
                cm.startCluster(clusterName);
            } catch (Exception e) {
                e.printStackTrace();
                return 1;
            }
            System.out.println("Starting of cluster " + clusterName +" completed.");
        }
        return 0;
    }

    private int verifyStartCluster() {
        int retval=1;
        final int numClusters = getNumClusters();
        for(int i = 0; i< numClusters; i++){
            final String clusterName = getClusterNames()[i];
            final String sb = getListClustersCommandOutput(clusterName);
            if(sb.indexOf(clusterName+" running")>=0){
                System.out.println("Cluster Start Verification for "+getClusterNames()[i] + " successful.");
                summary.put("CLUSTER START TEST", i+1 +" out of "+numClusters+" PASS");
                final Integer total = new Integer(((Integer)summary.get("TOTAL AMX TESTS ACUALLY RUN")).intValue()+ 1);
                summary.put("TOTAL AMX TESTS ACUALLY RUN", total);
                retval = 0;
            }
            else
                retval=1;
        }
        return retval;
    }

    private int stopCluster() {
        for(int i = 0; i< getNumClusters(); i++){
             final String clusterName = getClusterNames()[i];
             try {
                 cm.stopCluster(clusterName);
             } catch (Exception e) {
                 e.printStackTrace();
                 return 1;
             }
             System.out.println("Stopping of cluster " + clusterName +" completed.");
         }
         return 0;
    }

    private int verifyStopCluster() {
        int retval = 1;
        final int numClusters = getNumClusters();
        for(int i = 0; i< numClusters; i++){
            final String clusterName = getClusterNames()[i];
            final String sb = getListClustersCommandOutput(clusterName);
            if(sb.indexOf(clusterName+" not running")>=0){
                System.out.println("Cluster Stop Verification for "+getClusterNames()[i] + " successful.");
                summary.put("CLUSTER STOP TEST", i+1 +" out of "+numClusters+" PASS");
                final Integer total = new Integer(((Integer)summary.get("TOTAL AMX TESTS ACUALLY RUN")).intValue()+ 1);
                summary.put("TOTAL AMX TESTS ACUALLY RUN", total);
                retval = 0;
            }
            else
                retval =1;
        }
        return retval;
    }

    protected int startInstance() {
        for(int i = 0; i< getNumInstances(); i++){
            final String instanceName = getInstanceNames()[i];
            try {
                cim.startInstance(instanceName);
            } catch (Exception e) {
                e.printStackTrace();
                return 1;
            }
            System.out.println("Starting of instance " + instanceName +" completed.");
        }
        return 0;
    }

    protected int verifyStartInstance() {
        int retval=1;
        final int numInst = getNumInstances();
        for(int i = 0; i< numInst; i++){
            final String instanceName = getInstanceNames()[i];
            final String sb = getListInstancesCommandOutput(instanceName);
            if(sb.indexOf(instanceName+" running")>=0){
                System.out.println("Instance Start Verification for "+getInstanceNames()[i] + " successful.");
                summary.put("INSTANCE START TEST", i+1 +" out of "+numInst+" PASS");
                final Integer total = new Integer(((Integer)summary.get("TOTAL AMX TESTS ACUALLY RUN")).intValue()+ 1);
                summary.put("TOTAL AMX TESTS ACUALLY RUN", total);
                retval = 0;
            }
            else
                retval=1;
        }
        return retval;
    }

    protected int stopInstance() {
        for(int i = 0; i< getNumInstances(); i++){
            final String instanceName = getInstanceNames()[i];
            try {
                cim.stopInstance(instanceName);
            } catch (Exception e) {
                e.printStackTrace();
                return 1;
            }
            System.out.println("Stopping of instance " + instanceName +" completed.");
        }
        return 0;
    }

    protected int verifyStopInstance() {
        int retval = 1;
        final int numInst = getNumInstances();
        for(int i = 0; i< numInst; i++){
            final String instanceName = getInstanceNames()[i];
            final String sb = getListInstancesCommandOutput(instanceName);
            if(sb.indexOf(instanceName+" not running")>=0){
                System.out.println("Instance Stop Verification for "+getInstanceNames()[i] + " successful.");
                summary.put("INSTANCE STOP TEST", i+1 +" out of "+numInst+" PASS");
                final Integer total = new Integer(((Integer)summary.get("TOTAL AMX TESTS ACUALLY RUN")).intValue()+ 1);
                summary.put("TOTAL AMX TESTS ACUALLY RUN", total);
                retval = 0;
            }
            else
                retval =1;
        }
        return retval;
    }

    protected int deleteInstance() {
        for(int i=0; i<getNumClusters(); i++){
            for(int j = 0; j< getNumInstances(); j++){
                final String instanceName = getInstanceNames()[j];
                try {
                    cim.deleteInstance(instanceName);
                } catch (Exception e) {
                    e.printStackTrace();
                    return 1;
                }
                System.out.println("Deleting of instance " + instanceName +" completed.");
            }
        }
        return 0;
    }

    protected int verifyDeleteInstance() {
        int retval = 1;
        final int numInst = getNumInstances();
        for(int i = 0; i< numInst; i++){
            final String instanceName = getInstanceNames()[i];
            final String sb = getListInstancesCommandOutput(null);
            if(sb.indexOf(instanceName)<0){
                System.out.println("Instance Delete Verification for "+getInstanceNames()[i] + " successful.");
                summary.put("INSTANCE DELETE TEST", i+1 +" out of "+numInst+" PASS");
                final Integer total = new Integer(((Integer)summary.get("TOTAL AMX TESTS ACUALLY RUN")).intValue()+ 1);
                summary.put("TOTAL AMX TESTS ACUALLY RUN", total);
                retval = 0;
            }
            else
                retval =1;
        }
        return retval;
    }

    private int deleteCluster() {
        for(int i = 0; i< getNumClusters(); i++){
            final String clusterName = getClusterNames()[i];
            try {
                cm.deleteCluster(clusterName);
            } catch (Exception e) {
                e.printStackTrace();
                return 1;
            }
            System.out.println("Deleting of cluster " + clusterName +" completed.");
        }
        return 0;
    }

    private int verifyDeleteCluster() {
        int retval = 1;
        final int numClusters = getNumClusters();
        for(int i = 0; i< numClusters; i++){
            final String clusterName = getClusterNames()[i];
            final String sb = getListClustersCommandOutput(null);
            if(sb.indexOf(clusterName)<0){
                System.out.println("Cluster Delete Verification for "+getClusterNames()[i] + " successful.");
                summary.put("CLUSTER DELETE TEST", i+1 +" out of "+numClusters+" PASS");
                final Integer total = new Integer(((Integer)summary.get("TOTAL AMX TESTS ACUALLY RUN")).intValue()+ 1);
                summary.put("TOTAL AMX TESTS ACUALLY RUN", total);
                retval = 0;
            }
            else
                retval =1;
        }
        return retval;
    }

    protected int stopNodeAgent() {
        final String command = STOP_NODE_AGENT_COMMAND +
                " " +getNodeAgentName() ;
        System.out.println("running command: " +command);
        final String result = runCommand(command);
        if(result.indexOf("successfully")>=0){
            return 0;
        }
        return 1;
    }

    private int deleteNodeAgent() {
        String command = DELETE_NODE_AGENT_COMMAND +
                " " +getNodeAgentName() ;
        System.out.println("running command: " +command);
        String result = runCommand(command);
        if(result.indexOf("succesfully")>=0){
            command = DELETE_NODE_AGENT_CONFIG_COMMAND +
                    " --host "+ getHost()+
                    " --port "+ getAdminPort()+
                    " --user "+ getAdminUser()+
                    " --password " + getAdminPassword()+
                    " " +getNodeAgentName();
            System.out.println("running command: " +command);
            result = runCommand(command);
            if(result.indexOf("successfully")>=0){
                return 0;
            }
        }
        return 1;

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

    private String[] getClusterNames() {
        final int num = getNumClusters();
        final String[] clusters = new String[num];
        for (int i = 0; i < num; i++){
            clusters[i] = "cluster"+(1+i);
        }
        return clusters;
    }

    protected int getNumInstances() {
        return Integer.parseInt(System.getProperty("NUM_INSTANCES", "1"));
    }

    private int getNumClusters() {
        return Integer.parseInt(System.getProperty("NUM_CLUSTERS", "1"));
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
        final ClusterLifecycleTest test  = new ClusterLifecycleTest();
        try {
            test.runTest();
        } catch (TestFailedException e) {
            e.printStackTrace();
        }
    }

}
