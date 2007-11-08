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
 * $Id: ClustersConfigMBeanTest.java,v 1.1.1.1 2006/08/08 19:48:40 dpatil Exp $
 */

package com.sun.enterprise.ee.admin.mbeans;

//junit imports
import junit.framework.*;
import junit.textui.TestRunner;

import java.util.List;
import java.util.Iterator;

import javax.management.ObjectName;
import javax.management.MBeanException;
import com.sun.logging.LogDomains;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.admin.servermgmt.InstanceException;

/**
 * admin-ee/mbeans.jar admin-ee/admin.jar admin/mbeans.jar admin/servermgmt.jar
 * admin-ee/servermgmt.jar
 * admin-core/admin.jar admin-core/util.jar admin-core/config-api.jar
 * appserv-commons.jar schema2beans.jar appserv-core.jar
 * jmxri.jar jakarta-commons/commons-modeler.jar 
 * jakarta-commons/commons-logging.jar
 * imqspi.jar jms-api.jar deployment-api.jar
 */
public class ClustersConfigMBeanTest extends TestCase
{
    public void testListClusters() throws Exception
    {
        final ObjectName[] clusters = clustersMBean.listClusters("testdomain");
        assertEquals(
        new ObjectName("testdomain:type=cluster,category=config,name=cluster1"),
        clusters[0]);
    }

    public void testListClusters1()
    {
        try
        {
            final ObjectName[] clusters = clustersMBean.listClusters(null);
            assertTrue(false);
        }
        catch (InstanceException ie)
        {
            //ok
        }
    }

    public void testListClusters2() throws Exception
    {
        try
        {
            final ObjectName[] clusters = clustersMBean.listClusters("X");
            assertTrue(false);
        }
        catch (InstanceException ie)
        {
            //ok
        }
    }

    public void testGetStatus() throws Exception
    {
        final String status = clustersMBean.getStatusAsString("cluster1");
        assertEquals("not running", status);
    }

    public void testListClustersWithStatus() throws Exception
    {
        final String[] ca = clustersMBean.listClustersAsString(
                                        "testdomain", true);
        assertEquals("cluster1 not running", ca[0]);
    }

    public void testStartAndStopCluster()
        throws InstanceException, MBeanException
    {
        clustersMBean.startCluster("cluster1");
        String status = clustersMBean.getStatusAsString("cluster1");
        assertEquals("running", status);
        try
        {
            clustersMBean.startCluster("cluster1");
            assertTrue(false);
        }
        catch (InstanceException ie)
        {
            //ok
        }
        clustersMBean.stopCluster("cluster1");
        status = clustersMBean.getStatusAsString("cluster1");
        assertEquals("not running", status);
        try
        {
            clustersMBean.stopCluster("cluster1");
            assertTrue(false);
        }
        catch (InstanceException ie)
        {
            //ok
        }
    }

    public void testInconsistentServer() throws InstanceException
    {
        MockClusterAndServerRegistry.addServer(new MockInconsistentServer("is1"));
        MockClusterAndServerRegistry.addServer(new MockInconsistentServer("is2"));
        MockClusterAndServerRegistry.associate("cluster1", "is1");
        MockClusterAndServerRegistry.associate("cluster1", "is2");

        try
        {
            clustersMBean.startCluster("cluster1");
            assertTrue(false);
        }
        catch (InstanceException ie)
        {
            //ok
        }
        try
        {
            clustersMBean.stopCluster("cluster1");
            assertTrue(false);
        }
        catch (InstanceException ie)
        {
            //ok
        }

        MockClusterAndServerRegistry.dissociate("cluster1", "is1");
        MockClusterAndServerRegistry.dissociate("cluster1", "is2");
        MockClusterAndServerRegistry.deleteServer("is1");
        MockClusterAndServerRegistry.deleteServer("is2");
    }

    public void testFailedServer() throws InstanceException
    {
        MockClusterAndServerRegistry.addCluster("cluster2");
        MockClusterAndServerRegistry.addServer(new MockFailedServer("failedserver"));
        MockClusterAndServerRegistry.addServer(new MockFailedServer("failedserver1"));
        MockClusterAndServerRegistry.addServer(new MockFailedServer("failedserver2"));
        MockClusterAndServerRegistry.associate("cluster2", "failedserver");
        MockClusterAndServerRegistry.associate("cluster2", "failedserver2");

        try
        {
            clustersMBean.startCluster("cluster2");
            assertTrue(false);
        }
        catch (InstanceException ie)
        {
            //ok
        }

        try
        {
            clustersMBean.stopCluster("cluster2");
            assertTrue(false);
        }
        catch (InstanceException ie)
        {
            //ok
        }

        MockClusterAndServerRegistry.dissociate("cluster2", "failedserver");
        MockClusterAndServerRegistry.dissociate("cluster2", "failedserver2");
        MockClusterAndServerRegistry.deleteServer("failedserver");
        MockClusterAndServerRegistry.deleteServer("failedserver1");
        MockClusterAndServerRegistry.deleteServer("failedserver2");
        MockClusterAndServerRegistry.deleteCluster("cluster2");
    }

    /** Creates a new instance of ClustersConfigMBeanTest */
    public ClustersConfigMBeanTest(String name)
    {
        super(name);
    }

    ClustersConfigMBean clustersMBean;

    protected void setUp()
    {
        LogDomains.getLogger(EELogDomains.EE_ADMIN_LOGGER);
        MockClusterAndServerRegistry.setDomain("testdomain");
        MockClusterAndServerRegistry.addCluster("cluster1");
        MockClusterAndServerRegistry.addServer("server1");
        MockClusterAndServerRegistry.addServer("server2");
        MockClusterAndServerRegistry.associate("cluster1", "server1");
        MockClusterAndServerRegistry.associate("cluster1", "server2");
        clustersMBean = new MockClustersConfigMBean();
    }

    protected void tearDown()
    {
        clustersMBean = null;
        MockClusterAndServerRegistry.dissociate("cluster1", "server1");
        MockClusterAndServerRegistry.deleteServer("server1");
        MockClusterAndServerRegistry.dissociate("cluster1", "server2");
        MockClusterAndServerRegistry.deleteServer("server2");
        MockClusterAndServerRegistry.deleteCluster("cluster1");
    }

    public static junit.framework.Test suite()
    {
        TestSuite suite = new TestSuite(ClustersConfigMBeanTest.class);
        return suite;
    }

    public static void main(String args[]) throws Exception
    {
        final TestRunner runner= new TestRunner();
        final TestResult result = runner.doRun(ClustersConfigMBeanTest.suite(), false);
        System.exit(result.errorCount() + result.failureCount());
    }
}
