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
 * $Id: ResourcesMBeanTest.java,v 1.3 2005/12/25 03:43:13 tcfujii Exp $
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
import java.util.Map;

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


public class ResourcesMBeanTest extends TestCase {
    
    private JMXConnector connector;
    
    private final static String HOST = "localhost";
    private final static String PORT = "4848";
    private final static String ADMIN_USER = "admin";
    private final static String ADMIN_PASSWORD = "adminadmin";
    private final static String APPLICATION = "/export/stateless-simple.ear";
    
    public ResourcesMBeanTest(String name) throws Exception {
        super(name);
    }
    
    /**
     *
     */
    public void testCreateResource() {
        System.out.println("..testCreateResource");
        executeCreateResource("null","dummy", new java.util.HashMap());
        executeCreateResource("","dummy1",new java.util.HashMap());
        executeCreateResource(null,"dummy2",new java.util.HashMap());
    }
    
    /**
     *
     */
    public void testDeleteResource() {
        System.out.println("..testDeleteResource");
        executeDeleteResource("null","dummy");
        executeDeleteResource(null,"dummy1");
        executeDeleteResource("","dummy2");
    }
    
    public void executeCreateResource(String target, String resourceName, Map options) {
        try {
             final MBeanServerConnection server = getMBeanServer();
             String methodToInvoke = "createJdbcConnectionPool";
             Attribute attr1 = new Attribute("datasource_classname","com.pointbase.xa.xaDataSource");
             Attribute attr2 = new Attribute("name",resourceName);
             AttributeList list = new AttributeList();
             list.add(attr1);
             list.add(attr2);
             ObjectName objectName = 
                    new ObjectName(
                        "com.sun.appserv:type=resources,category=config");       
             printMBeanTestInfo(objectName);
             ObjectName result = (ObjectName)server.invoke(objectName,  
                methodToInvoke, new Object[]{list,new Properties(),target},
                new String[]{"javax.management.AttributeList", "java.util.Properties","java.lang.String"});
        } catch(Throwable e) {
            fail(e.getMessage());
        }
    }
    
    public void executeDeleteResource(String target, String resourceName) {
        try {
             final MBeanServerConnection server = getMBeanServer();
             String methodToInvoke = "deleteJdbcConnectionPool";
             ObjectName objectName = 
                    new ObjectName(
                        "com.sun.appserv:type=resources,category=config");       
             printMBeanTestInfo(objectName);
             ObjectName result = (ObjectName)server.invoke(objectName,  
                methodToInvoke, new Object[]{resourceName,target},
                new String[]{"java.lang.String","java.lang.String"});
        } catch(Throwable e) {
            System.out.println("Something failed in deleting the resource!");
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
        TestSuite suite = new TestSuite(ResourcesMBeanTest.class);
        return suite;
    }

    public static void main(String args[]) throws Exception {
        final TestRunner runner= new TestRunner();
        final TestResult result = runner.doRun(ResourcesMBeanTest.suite(), false);
        System.exit(result.errorCount() + result.failureCount());
    }
}