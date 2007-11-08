/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
 * $Id: ApplicationsConfigMBeanTest.java,v 1.3 2005/12/25 03:43:11 tcfujii Exp $
 */

package com.sun.enterprise.admin.mbeans;

import com.sun.enterprise.admin.jmx.remote.DefaultConfiguration;
import com.sun.enterprise.admin.target.TargetType;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.deployment.backend.DeploymentStatus;
import com.sun.enterprise.deployment.util.DeploymentProperties;
//jdk imports
import java.util.Properties;
import java.io.File;

//junit imports
import junit.framework.*;
import junit.textui.TestRunner;

//JMX imports
import javax.management.AttributeList;
import javax.management.Attribute;
import javax.management.ObjectName;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;


public class ApplicationsConfigMBeanTest extends TestCase {
    
    private JMXConnector connector;
    
    private final static String HOST = "hoyas.red.iplanet.com";
    private final static String PORT = "1234";
    private final static String ADMIN_USER = "admin";
    private final static String ADMIN_PASSWORD = "adminadmin";
    private final static String APPLICATION = "/export/stateless-simple.ear";
    
    public ApplicationsConfigMBeanTest(String name) throws Exception {
        super(name);
    }
   
    /**
     * This test is used as a driver to test the deployment of an 
     * application to multiple targets against a running instance of
     * the application server. Uncomment the lines within this method 
     * and set the appropriate class member variables to administer the test.
     *
     * @author Rob Ruyak
     */
    public void testTargetAwareDeployment() {
        final java.io.File appArchive = 
            new java.io.File(APPLICATION);
        printAllDeployedComponents();
        //deployApplication(appArchive,getTargets());
        printAllDeployedComponents();
        //checkDeploymentPassed("stateless-simple", getTargets());
        //undeployApplication(appArchive, getTargets());
    }
   
    /**
     *
     */
    public void checkDeploymentPassed(String appName, String[] targets) {
        final MBeanServerConnection server = getMBeanServer();
        try{
            ObjectName clusters = 
                new ObjectName("com.sun.appserv:type=clusters,category=config");
            for(int i = 0; i < targets.length; i++) {
                ObjectName name = (ObjectName)server.invoke(clusters, 
                    "getClusterByName", new Object[]{targets[i]}, 
                    new String[]{"java.lang.String"});
                if(name != null) {
                    System.out.println("What is the objectname: " + name);
                    String clusterName = 
                        (String)server.getAttribute(name, "name");
                    ObjectName cluster = new ObjectName(
                        "com.sun.appserv:type=cluster,name=" + 
                        clusterName + ",category=config");
                    ObjectName[] deployedObjs = (ObjectName[])server.invoke(
                        cluster,"getApplicationRef", 
                        new Object[]{}, new String[]{}); 
                    for(int z = 0; z < deployedObjs.length; z++) {
                        String objName = 
                           (String)server.getAttribute(deployedObjs[z], "name");
                        if(appName.equals(objName)) {
                            System.out.println("Application deployed " + 
                                "successfully on cluster: " + clusterName);
                        }
                    }
                } else {
                    System.out.println("No clusters found from target list!");
                }
            }//end first for loop
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    /**
     *
     */
    public void deployApplication(java.io.File appArchive, String [] targets) {
        final MBeanServerConnection server = getMBeanServer();
        java.util.Properties myProps = getDeploymentProperties();
        try {
             String methodToInvoke = "deploy";
             ObjectName objectName = 
                    new ObjectName(
                        "com.sun.appserv:type=applications,category=config");       
             printMBeanTestInfo(objectName);
             Object deployResult = server.invoke(objectName,  
                methodToInvoke, new Object[]{myProps, targets},
                new String[]{"java.util.Properties", "[Ljava.lang.String;"});
        } catch(Throwable e) {
            System.out.println("Something Failed!");
            e.printStackTrace();
        }
    }
    
    /**
     *
     */
    public void undeployApplication(java.io.File appArchive, String [] targets) {
        final MBeanServerConnection server = getMBeanServer();
        java.util.Properties myProps = getDeploymentProperties();
        try {
             String methodToInvoke = "undeploy";
             ObjectName objectName = 
                    new ObjectName(
                        "com.sun.appserv:type=applications,category=config"); 
             Object undeployResult = server.invoke(objectName,  
                methodToInvoke, new Object[]{myProps, targets},
                new String[]{"java.util.Properties", "[Ljava.lang.String;"});
        } catch(Throwable e) {
            System.out.println("Something failed when undeploying!");
            e.printStackTrace();
        }
    }
    
    /**
     *
     */
    public void printAllDeployedComponents() {
       final MBeanServerConnection server = getMBeanServer();
       try {
             String methodToInvoke = "getAllDeployedComponents";
             ObjectName objectName = 
                    new ObjectName(
                        "com.sun.appserv:type=applications,category=config"); 
             ObjectName[] result = (ObjectName[])server.invoke(objectName,  
                methodToInvoke, new Object[]{},
                new String[]{});
             for(int i = 0; i < result.length; i++) {
                System.out.println("Deployed: " + result[i]);
             }
        } catch(Throwable e) {
            System.out.println("Something failed when getting all deployed components!");
            e.printStackTrace();
        }
    }
    
    /**
     *
     */
    public String[] getTargets() {
       final MBeanServerConnection server = getMBeanServer();
       String[] targets = null;
       System.out.println("Testing getTargets!");
       try {
             String methodToInvoke = "getTargets";
             ObjectName objectName = 
                    new ObjectName(
                        "com.sun.appserv:type=applications,category=config"); 
             targets = (String[])server.invoke(objectName,  
                methodToInvoke, new Object[]{},
                new String[]{});
             System.out.println("\n");
             for(int i = 0; i < targets.length; i++) {
                System.out.println("Target: " + targets[i]);
             }
            
        } catch(Throwable e) {
            System.out.println("Something failed when getting all targets!");
            e.printStackTrace();
        }
       return targets;
    }
    
    /**
     *
     */
    public void getServers() {
       final MBeanServerConnection server = getMBeanServer();
       try {
             String methodToInvoke = "getServer";
             ObjectName objectName = 
                    new ObjectName(
                        "com.sun.appserv:type=servers,category=config"); 
             ObjectName[] result = (ObjectName[])server.invoke(objectName,  
                methodToInvoke, new Object[]{},
                new String[]{});
             System.out.println("\n");
             for(int i = 0; i < result.length; i++) {
                System.out.println("Server: " + result[i]);
             }
        } catch(Throwable e) {
            System.out.println("Something failed when getting all targets!");
            e.printStackTrace();
        }
    }
    
    /**
     *
     */
    public void getClusters() {
       final MBeanServerConnection server = getMBeanServer();
       try {
             String methodToInvoke = "getCluster";
             ObjectName objectName = 
                    new ObjectName(
                        "com.sun.appserv:type=clusters,category=config"); 
             ObjectName[] result = (ObjectName[])server.invoke(objectName,  
                methodToInvoke, new Object[]{},
                new String[]{});
             System.out.println("\n");
             for(int i = 0; i < result.length; i++) {
                System.out.println("Cluster: " + result[i]);
             }
        } catch(Throwable e) {
            System.out.println("Something failed when getting all targets!");
            e.printStackTrace();
        }
    }
    
    /**
     *
     */
    public java.util.Properties getDeploymentProperties() {
        java.util.Properties props = new java.util.Properties();
        props.setProperty(DeploymentProperties.ARCHIVE_NAME, APPLICATION);
        props.setProperty(DeploymentProperties.VERIFY, "false");
        props.setProperty(DeploymentProperties.PRECOMPILE_JSP, "false");
        props.setProperty(DeploymentProperties.ENABLE, "true");
        props.setProperty(DeploymentProperties.FORCE, "true");
        return props;
    }
    
    /**
     *
     */
    private void printMBeanTestInfo(ObjectName objectName) {
        System.out.println("Testing mbean invocation on <" + objectName + ">");
    }
    
    /**
     *
     */
    private ObjectName getSampleObjectName() {
        ObjectName name = null;
        try {
            name = 
                new ObjectName("com.sun.appserv:type=jdbc-connection-pool," + 
                    "name=__TimerPool,category=config");
        } catch(Exception e) {
            e.printStackTrace();
        }
        return name;
    }
    
    /**
     *
     */
    private MBeanServerConnection getMBeanServer() {
        MBeanServerConnection conn = null;
        try {
            conn = connector.getMBeanServerConnection();
        } catch(Exception e) {
            System.out.println("Could not retrieve connection! " 
                + e.getMessage());
            e.printStackTrace();
        }
        return conn;
    }
    
    /**
     *
     *
     */
    private void initConnector() {
        try {
            connector = JMXConnectorFactory.connect(
                new JMXServiceURL("service:jmx:s1ashttp://" + HOST + 
                    ":" + PORT), initConnectorEnvironment());
        } catch (Exception ex) {
            System.out.println("Remote Connect Failed!!!");
            System.out.println(ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
    
    /**
     *
     */
    private java.util.Map initConnectorEnvironment() {
        final java.util.Map env = new java.util.HashMap();
        final String PKGS = "com.sun.enterprise.admin.jmx.remote.protocol";
        env.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, PKGS);
        env.put(DefaultConfiguration.ADMIN_USER_ENV_PROPERTY_NAME, ADMIN_USER);
        env.put(DefaultConfiguration.ADMIN_PASSWORD_ENV_PROPERTY_NAME, ADMIN_PASSWORD);
        env.put(DefaultConfiguration.HTTP_AUTH_PROPERTY_NAME,
                DefaultConfiguration.DEFAULT_HTTP_AUTH_SCHEME);
        return (env);
    }
    
    protected void setUp() {
        initConnector();
    }

    protected void tearDown() {
    }

    public static junit.framework.Test suite() {
        TestSuite suite = new TestSuite(ApplicationsConfigMBeanTest.class);
        return suite;
    }

    public static void main(String args[]) throws Exception {
        final TestRunner runner= new TestRunner();
        final TestResult result = runner.doRun(ApplicationsConfigMBeanTest.suite(), false);
        System.exit(result.errorCount() + result.failureCount());
    }
}