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
 * MonitoringRegistrationHelperTest.java
 * JUnit based test
 *
 * Created on April 1, 2004, 5:04 PM
 */

package com.sun.enterprise.admin.monitor.registry.spi;

import com.sun.enterprise.admin.monitor.registry.*;
import com.sun.enterprise.admin.monitor.stats.*;
import javax.management.*;
import javax.management.j2ee.statistics.*;
import java.util.*;
import java.util.logging.Logger;
import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.admin.event.AdminEventListenerRegistry;
import com.sun.enterprise.admin.monitor.jndi.JndiMBeanManager;
import com.sun.enterprise.admin.monitor.registry.spi.reconfig.MonitoringConfigurationHandler;
import com.sun.enterprise.admin.monitor.registry.spi.reconfig.DynamicReconfigurator;
import com.sun.enterprise.admin.monitor.registry.spi.reconfig.MonitoringConfigChangeListener;
import com.sun.enterprise.server.stats.JVMStatsImpl;
import junit.framework.*;

/**
 *
 * @author Rob
 */
public class MonitoringRegistrationHelperTest extends TestCase {
    
    public MonitoringRegistrationHelperTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(MonitoringRegistrationHelperTest.class);
        return suite;
    }
    
    /**
     * Test of getInstance method, of class com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper.
     */
    public void testGetInstance() {
        System.out.println("testGetInstance");
        fail("The test case is empty.");
    }
    
    /**
     * Test of registerEJBCacheStats method, of class com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper.
     */
    public void testRegisterEJBCacheStats() {
        System.out.println("testRegisterEJBCacheStats");
        fail("The test case is empty.");
    }
    
    /**
     * Test of unregisterEJBCacheStats method, of class com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper.
     */
    public void testUnregisterEJBCacheStats() {
        System.out.println("testUnregisterEJBCacheStats");
        fail("The test case is empty.");
    }
    
    /**
     * Test of registerEJBPoolStats method, of class com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper.
     */
    public void testRegisterEJBPoolStats() {
        System.out.println("testRegisterEJBPoolStats");
        fail("The test case is empty.");
    }
    
    /**
     * Test of unregisterEJBPoolStats method, of class com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper.
     */
    public void testUnregisterEJBPoolStats() {
        System.out.println("testUnregisterEJBPoolStats");
        fail("The test case is empty.");
    }
    
    /**
     * Test of registerEJBMethodStats method, of class com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper.
     */
    public void testRegisterEJBMethodStats() {
        System.out.println("testRegisterEJBMethodStats");
        fail("The test case is empty.");
    }
    
    /**
     * Test of unregisterEJBMethodStats method, of class com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper.
     */
    public void testUnregisterEJBMethodStats() {
        System.out.println("testUnregisterEJBMethodStats");
        fail("The test case is empty.");
    }
    
    /**
     * Test of registerEntityBeanStats method, of class com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper.
     */
    public void testRegisterEntityBeanStats() {
        System.out.println("testRegisterEntityBeanStats");
        fail("The test case is empty.");
    }
    
    /**
     * Test of unregisterEntityBeanStats method, of class com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper.
     */
    public void testUnregisterEntityBeanStats() {
        System.out.println("testUnregisterEntityBeanStats");
        fail("The test case is empty.");
    }
    
    /**
     * Test of registerStatefulSessionBeanStats method, of class com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper.
     */
    public void testRegisterStatefulSessionBeanStats() {
        System.out.println("testRegisterStatefulSessionBeanStats");
        fail("The test case is empty.");
    }
    
    /**
     * Test of unregisterStatefulSessionBeanStats method, of class com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper.
     */
    public void testUnregisterStatefulSessionBeanStats() {
        System.out.println("testUnregisterStatefulSessionBeanStats");
        fail("The test case is empty.");
    }
    
    /**
     * Test of registerStatelessSessionBeanStats method, of class com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper.
     */
    public void testRegisterStatelessSessionBeanStats() {
        System.out.println("testRegisterStatelessSessionBeanStats");
        fail("The test case is empty.");
    }
    
    /**
     * Test of unregisterStatelessSessionBeanStats method, of class com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper.
     */
    public void testUnregisterStatelessSessionBeanStats() {
        System.out.println("testUnregisterStatelessSessionBeanStats");
        fail("The test case is empty.");
    }
    
    /**
     * Test of registerMessageDrivenBeanStats method, of class com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper.
     */
    public void testRegisterMessageDrivenBeanStats() {
        System.out.println("testRegisterMessageDrivenBeanStats");
        fail("The test case is empty.");
    }
    
    /**
     * Test of unregisterMessageDrivenBeanStats method, of class com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper.
     */
    public void testUnregisterMessageDrivenBeanStats() {
        System.out.println("testUnregisterMessageDrivenBeanStats");
        fail("The test case is empty.");
    }
    
    /**
     * Test of registerJDBCConnectionPoolStats method, of class com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper.
     */
    public void testRegisterJDBCConnectionPoolStats() {
        System.out.println("testRegisterJDBCConnectionPoolStats");
        fail("The test case is empty.");
    }
    
    /**
     * Test of unregisterJDBCConnectionPoolStats method, of class com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper.
     */
    public void testUnregisterJDBCConnectionPoolStats() {
        System.out.println("testUnregisterJDBCConnectionPoolStats");
        fail("The test case is empty.");
    }
    
    /**
     * Test of registerConnectorConnectionPoolStats method, of class com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper.
     */
    public void testRegisterConnectorConnectionPoolStats() {
        System.out.println("testRegisterConnectorConnectionPoolStats");
        fail("The test case is empty.");
    }
    
    /**
     * Test of unregisterConnectorConnectionPoolStats method, of class com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper.
     */
    public void testUnregisterConnectorConnectionPoolStats() {
        System.out.println("testUnregisterConnectorConnectionPoolStats");
        fail("The test case is empty.");
    }
    
    /**
     * Test of registerOrbConnectionManagerStats method, of class com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper.
     */
    public void testRegisterOrbConnectionManagerStats() {
        System.out.println("testRegisterOrbConnectionManagerStats");
        fail("The test case is empty.");
    }
    
    /**
     * Test of unregisterOrbConnectionManagerStats method, of class com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper.
     */
    public void testUnregisterOrbConnectionManagerStats() {
        System.out.println("testUnregisterOrbConnectionManagerStats");
        fail("The test case is empty.");
    }
    
    /**
     * Test of registerThreadPoolStats method, of class com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper.
     */
    public void testRegisterThreadPoolStats() {
        System.out.println("testRegisterThreadPoolStats");
        fail("The test case is empty.");
    }
    
    /**
     * Test of unregisterThreadPoolStats method, of class com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper.
     */
    public void testUnregisterThreadPoolStats() {
        System.out.println("testUnregisterThreadPoolStats");
        fail("The test case is empty.");
    }
    
    /**
     * Test of registerJTAStats method, of class com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper.
     */
    public void testRegisterJTAStats() {
        System.out.println("testRegisterJTAStats");
        fail("The test case is empty.");
    }
    
    /**
     * Test of unregisterJTAStats method, of class com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper.
     */
    public void testUnregisterJTAStats() {
        System.out.println("testUnregisterJTAStats");
        fail("The test case is empty.");
    }
    
    /**
     * Test of registerJVMStats method, of class com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper.
     */
    public void testRegisterJVMStats() {
        System.out.println("testRegisterJVMStats");
        fail("The test case is empty.");
    }
    
    /**
     * Test of unregisterJVMStats method, of class com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper.
     */
    public void testUnregisterJVMStats() {
        System.out.println("testUnregisterJVMStats");
        fail("The test case is empty.");
    }
    
    /**
     * Test of registerHttpListenerStats method, of class com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper.
     */
    public void testRegisterHttpListenerStats() {
        System.out.println("testRegisterHttpListenerStats");
        fail("The test case is empty.");
    }
    
    /**
     * Test of unregisterHttpListenerStats method, of class com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper.
     */
    public void testUnregisterHttpListenerStats() {
        System.out.println("testUnregisterHttpListenerStats");
        fail("The test case is empty.");
    }
    
    /**
     * Test of registerServletStats method, of class com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper.
     */
    public void testRegisterServletStats() {
        System.out.println("testRegisterServletStats");
        fail("The test case is empty.");
    }
    
    /**
     * Test of unregisterServletStats method, of class com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper.
     */
    public void testUnregisterServletStats() {
        System.out.println("testUnregisterServletStats");
        fail("The test case is empty.");
    }
    
    /**
     * Test of registerWebModuleStats method, of class com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper.
     */
    public void testRegisterWebModuleStats() {
        System.out.println("testRegisterWebModuleStats");
        fail("The test case is empty.");
    }
    
    /**
     * Test of unregisterWebModuleStats method, of class com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper.
     */
    public void testUnregisterWebModuleStats() {
        System.out.println("testUnregisterWebModuleStats");
        fail("The test case is empty.");
    }
    
    /**
     * Test of getThreadPoolNodes method, of class com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper.
     */
    public void testGetThreadPoolNodes() {
        System.out.println("testGetThreadPoolNodes");
        fail("The test case is empty.");
    }
    
    /**
     * Test of getOrbNodes method, of class com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper.
     */
    public void testGetOrbNodes() {
        System.out.println("testGetOrbNodes");
        fail("The test case is empty.");
    }
    
    /**
     * Test of getHttpServiceNodes method, of class com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper.
     */
    public void testGetHttpServiceNodes() {
        System.out.println("testGetHttpServiceNodes");
        fail("The test case is empty.");
    }
    
    /**
     * Test of getTransactionServiceNodes method, of class com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper.
     */
    public void testGetTransactionServiceNodes() {
        System.out.println("testGetTransactionServiceNodes");
        fail("The test case is empty.");
    }
    
    /**
     * Test of getConnectionPoolNodes method, of class com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper.
     */
    public void testGetConnectionPoolNodes() {
        System.out.println("testGetConnectionPoolNodes");
        fail("The test case is empty.");
    }
    
    /**
     * Test of getWebContainerNodes method, of class com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper.
     */
    public void testGetWebContainerNodes() {
        System.out.println("testGetWebContainerNodes");
        fail("The test case is empty.");
    }
    
    /**
     * Test of getEjbContainerNodes method, of class com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper.
     */
    public void testGetEjbContainerNodes() {
        System.out.println("testGetEjbContainerNodes");
        fail("The test case is empty.");
    }
    
    /**
     * Test of getEjbMethodNodes method, of class com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper.
     */
    public void testGetEjbMethodNodes() {
        System.out.println("testGetEjbMethodNodes");
        fail("The test case is empty.");
    }
    
    /**
     * Test of registerMonitoringLevelListener method, of class com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper.
     */
    public void testRegisterMonitoringLevelListener() {
        System.out.println("testRegisterMonitoringLevelListener");
        fail("The test case is empty.");
    }
    
    /**
     * Test of unregisterMonitoringLevelListener method, of class com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper.
     */
    public void testUnregisterMonitoringLevelListener() {
        System.out.println("testUnregisterMonitoringLevelListener");
        fail("The test case is empty.");
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    // TODO add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}
    
    
}
