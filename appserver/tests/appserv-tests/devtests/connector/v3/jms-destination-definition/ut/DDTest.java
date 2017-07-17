/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.jmsdd;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.io.AppClientDeploymentDescriptorFile;
import com.sun.enterprise.deployment.io.ApplicationDeploymentDescriptorFile;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.glassfish.deployment.common.JavaEEResourceType;
import org.glassfish.ejb.deployment.io.EjbDeploymentDescriptorFile;
import org.glassfish.web.deployment.io.WebDeploymentDescriptorFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DDTest extends TestCase {

    private static SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests");
    private File descriptorDir = null;

    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.setupHK2();
        descriptorDir = new File(System.getProperty("workDir"), "descriptor");
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testApplicationDD() throws Exception {
        String tcName = "jms-destination-definition-Application-DD-test";
        InputStream ddIS = null;

        try {
            String ddFileName = "ut-application.xml";
            File ddFile = new File(descriptorDir, ddFileName);
            Assert.assertTrue("The application.xml not found: " + ddFile, ddFile.exists());

            ddIS = new FileInputStream(ddFile);
            ApplicationDeploymentDescriptorFile ddReader = new ApplicationDeploymentDescriptorFile();
            Application application = (Application)ddReader.read(ddIS);

            Set<ResourceDescriptor> actualJMSDDDs = application.getResourceDescriptors(JavaEEResourceType.JMSDD);

            Map<String, JMSDestinationDefinitionDescriptor> expectedJMSDDDs =
                    new HashMap<String, JMSDestinationDefinitionDescriptor>();
            JMSDestinationDefinitionDescriptor desc;

            desc = new JMSDestinationDefinitionDescriptor();
            desc.setDescription("global-scope jms destination defined in UT Application DD");
            desc.setName("java:global/env/UT_Application_DD_JMSDestination");
            desc.setClassName("javax.jms.Queue");
            desc.setResourceAdapter("jmsra");
            desc.setDestinationName("myPhysicalQueue");
            expectedJMSDDDs.put(desc.getName(), desc);

            desc = new JMSDestinationDefinitionDescriptor();
            desc.setDescription("application-scope jms destination defined in UT Application DD");
            desc.setName("java:app/env/UT_Application_DD_JMSDestination");
            desc.setClassName("javax.jms.Topic");
            desc.setResourceAdapter("jmsra");
            desc.setDestinationName("myPhysicalTopic");
            expectedJMSDDDs.put(desc.getName(), desc);

            TestUtil.compareJMSDDD(expectedJMSDDDs, actualJMSDDDs);
            stat.addStatus(tcName, stat.PASS);

        } catch(Exception e) {
            stat.addStatus(tcName, stat.FAIL);
            throw e;
        } finally {
            if (ddIS != null) {
                ddIS.close();
            }
        }
    }

    public void testAppclientDD() throws Exception {
        String tcName = "jms-destination-definition-Appclient-DD-test";
        InputStream ddIS = null;

        try {
            String ddFileName = "ut-application-client.xml";
            File ddFile = new File(descriptorDir, ddFileName);
            Assert.assertTrue("The ut-application-client.xml not found: " + ddFile, ddFile.exists());

            ddIS = new FileInputStream(ddFile);
            AppClientDeploymentDescriptorFile ddReader = new AppClientDeploymentDescriptorFile();
            ApplicationClientDescriptor appclientDesc = (ApplicationClientDescriptor)ddReader.read(ddIS);

            JMSDestinationDefinitionDescriptor desc;
            Map<String, JMSDestinationDefinitionDescriptor> expectedJMSDDDs = new HashMap<String, JMSDestinationDefinitionDescriptor>();

            desc = new JMSDestinationDefinitionDescriptor();
            desc.setDescription("global-scope jms destination defined in UT Appclient DD");
            desc.setName("java:global/env/UT_Appclient_DD_JMSDestination");
            desc.setClassName("javax.jms.Queue");
            desc.setResourceAdapter("jmsra");
            desc.setDestinationName("myPhysicalQueue");
            expectedJMSDDDs.put(desc.getName(), desc);

            desc = new JMSDestinationDefinitionDescriptor();
            desc.setDescription("application-scope jms destination defined in UT Appclient DD");
            desc.setName("java:app/env/UT_Appclient_DD_JMSDestination");
            desc.setClassName("javax.jms.Topic");
            desc.setResourceAdapter("jmsra");
            desc.setDestinationName("myPhysicalTopic");
            expectedJMSDDDs.put(desc.getName(), desc);

            desc = new JMSDestinationDefinitionDescriptor();
            desc.setDescription("module-scope jms destination defined in UT Appclient DD");
            desc.setName("java:module/env/UT_Appclient_DD_JMSDestination");
            desc.setClassName("javax.jms.Topic");
            desc.setResourceAdapter("jmsra");
            desc.setDestinationName("myPhysicalTopic");
            expectedJMSDDDs.put(desc.getName(), desc);

            desc = new JMSDestinationDefinitionDescriptor();
            desc.setDescription("component-scope jms destination defined in UT Appclient DD");
            desc.setName("java:comp/env/UT_Appclient_DD_JMSDestination");
            desc.setClassName("javax.jms.Queue");
            desc.setResourceAdapter("jmsra");
            desc.setDestinationName("myPhysicalQueue");
            expectedJMSDDDs.put(desc.getName(), desc);

            TestUtil.compareJMSDDD(expectedJMSDDDs, appclientDesc.getResourceDescriptors(JavaEEResourceType.JMSDD));

            stat.addStatus(tcName, stat.PASS);

        } catch(Exception e) {
            stat.addStatus(tcName, stat.FAIL);
            throw e;
        } finally {
            if (ddIS != null) {
                ddIS.close();
            }
        }
    }

    public void testSessionEJBDD() throws Exception {
        String tcName = "jms-destination-definition-Session-EJB-DD-test";
        InputStream ddIS = null;

        try {
            String ddFileName = "ut-session-ejb-jar.xml";
            File ddFile = new File(descriptorDir, ddFileName);
            Assert.assertTrue("The ut-session-ejb-jar.xml not found: " + ddFile, ddFile.exists());

            ddIS = new FileInputStream(ddFile);
            EjbDeploymentDescriptorFile ddReader = new EjbDeploymentDescriptorFile();
            EjbBundleDescriptor ejbBundle = (EjbBundleDescriptor)ddReader.read(ddIS);

            for (EjbDescriptor ejbDescriptor : ejbBundle.getEjbs()) {
                ejbDescriptor.getResourceDescriptors(JavaEEResourceType.JMSDD);
                if (ejbDescriptor.getName().equals("HelloStatefulEJB")) {
                    testStatefulSessionEJBDD(ejbDescriptor);
                } else if (ejbDescriptor.getName().equals("HelloEJB")) {
                    testStatelessSessionEJBDD(ejbDescriptor);
                } else {
                    fail("Unknown EJB descriptor: " + ejbDescriptor.getName());
                }
            }

            stat.addStatus(tcName, stat.PASS);

        } catch(Exception e) {
            stat.addStatus(tcName, stat.FAIL);
            throw e;
        } finally {
            if (ddIS != null) {
                ddIS.close();
            }
        }
    }

    private void testStatefulSessionEJBDD(EjbDescriptor ejb) {
        JMSDestinationDefinitionDescriptor desc;
        Map<String, JMSDestinationDefinitionDescriptor> expectedJMSDDDs = new HashMap<String, JMSDestinationDefinitionDescriptor>();

        desc = new JMSDestinationDefinitionDescriptor();
        desc.setDescription("global-scope jms destination defined in UT Session EJB DD");
        desc.setName("java:global/env/UT_HelloStatefulEJB_DD_JMSDestination");
        desc.setClassName("javax.jms.Queue");
        desc.setResourceAdapter("jmsra");
        desc.setDestinationName("myPhysicalQueue");
        expectedJMSDDDs.put(desc.getName(), desc);

        desc = new JMSDestinationDefinitionDescriptor();
        desc.setDescription("application-scope jms destination defined in UT Session EJB DD");
        desc.setName("java:app/env/UT_HelloStatefulEJB_DD_JMSDestination");
        desc.setClassName("javax.jms.Topic");
        desc.setResourceAdapter("jmsra");
        desc.setDestinationName("myPhysicalTopic");
        expectedJMSDDDs.put(desc.getName(), desc);

        desc = new JMSDestinationDefinitionDescriptor();
        desc.setDescription("module-scope jms destination defined in UT Session EJB DD");
        desc.setName("java:module/env/UT_HelloStatefulEJB_DD_JMSDestination");
        desc.setClassName("javax.jms.Topic");
        desc.setResourceAdapter("jmsra");
        desc.setDestinationName("myPhysicalTopic");
        expectedJMSDDDs.put(desc.getName(), desc);

        desc = new JMSDestinationDefinitionDescriptor();
        desc.setDescription("component-scope jms destination defined in UT Session EJB DD");
        desc.setName("java:comp/env/UT_HelloStatefulEJB_DD_JMSDestination");
        desc.setClassName("javax.jms.Queue");
        desc.setResourceAdapter("jmsra");
        desc.setDestinationName("myPhysicalQueue");
        expectedJMSDDDs.put(desc.getName(), desc);

        TestUtil.compareJMSDDD(expectedJMSDDDs, ejb.getResourceDescriptors(JavaEEResourceType.JMSDD));
    }

    private void testStatelessSessionEJBDD(EjbDescriptor ejb) {
        JMSDestinationDefinitionDescriptor desc;
        Map<String, JMSDestinationDefinitionDescriptor> expectedJMSDDDs = new HashMap<String, JMSDestinationDefinitionDescriptor>();

        desc = new JMSDestinationDefinitionDescriptor();
        desc.setDescription("global-scope jms destination defined in UT Session EJB DD");
        desc.setName("java:global/env/UT_HelloEJB_DD_JMSDestination");
        desc.setClassName("javax.jms.Queue");
        desc.setResourceAdapter("jmsra");
        desc.setDestinationName("myPhysicalQueue");
        expectedJMSDDDs.put(desc.getName(), desc);

        desc = new JMSDestinationDefinitionDescriptor();
        desc.setDescription("application-scope jms destination defined in UT Session EJB DD");
        desc.setName("java:app/env/UT_HelloEJB_DD_JMSDestination");
        desc.setClassName("javax.jms.Topic");
        desc.setResourceAdapter("jmsra");
        desc.setDestinationName("myPhysicalTopic");
        expectedJMSDDDs.put(desc.getName(), desc);

        desc = new JMSDestinationDefinitionDescriptor();
        desc.setDescription("module-scope jms destination defined in UT Session EJB DD");
        desc.setName("java:module/env/UT_HelloEJB_DD_JMSDestination");
        desc.setClassName("javax.jms.Topic");
        desc.setResourceAdapter("jmsra");
        desc.setDestinationName("myPhysicalTopic");
        expectedJMSDDDs.put(desc.getName(), desc);

        desc = new JMSDestinationDefinitionDescriptor();
        desc.setDescription("component-scope jms destination defined in UT Session EJB DD");
        desc.setName("java:comp/env/UT_HelloEJB_DD_JMSDestination");
        desc.setClassName("javax.jms.Queue");
        desc.setResourceAdapter("jmsra");
        desc.setDestinationName("myPhysicalQueue");
        expectedJMSDDDs.put(desc.getName(), desc);

        TestUtil.compareJMSDDD(expectedJMSDDDs, ejb.getResourceDescriptors(JavaEEResourceType.JMSDD));
    }

    public void testEntityEJBDD() throws Exception {
        String tcName = "jms-destination-definition-Entity-EJB-DD-test";
        InputStream ddIS = null;

        try {
            String ddFileName = "ut-entity-ejb-jar.xml";
            File ddFile = new File(descriptorDir, ddFileName);
            Assert.assertTrue("The ut-entity-ejb-jar.xml not found: " + ddFile, ddFile.exists());

            ddIS = new FileInputStream(ddFile);
            EjbDeploymentDescriptorFile ddReader = new EjbDeploymentDescriptorFile();
            EjbBundleDescriptor ejbBundle = (EjbBundleDescriptor)ddReader.read(ddIS);
            assertEquals("Only contains one entity bean", 1, ejbBundle.getEjbs().size());
            for (EjbDescriptor ejbDescriptor : ejbBundle.getEjbs()) {
                JMSDestinationDefinitionDescriptor desc;
                Map<String, JMSDestinationDefinitionDescriptor> expectedJMSDDDs = new HashMap<String, JMSDestinationDefinitionDescriptor>();

                desc = new JMSDestinationDefinitionDescriptor();
                desc.setDescription("global-scope jms destination defined in UT Entity EJB DD");
                desc.setName("java:global/env/UT_Entity_DD_JMSDestination");
                desc.setClassName("javax.jms.Queue");
                desc.setResourceAdapter("jmsra");
                desc.setDestinationName("myPhysicalQueue");
                expectedJMSDDDs.put(desc.getName(), desc);

                desc = new JMSDestinationDefinitionDescriptor();
                desc.setDescription("application-scope jms destination defined in UT Entity EJB DD");
                desc.setName("java:app/env/UT_Entity_DD_JMSDestination");
                desc.setClassName("javax.jms.Topic");
                desc.setResourceAdapter("jmsra");
                desc.setDestinationName("myPhysicalTopic");
                expectedJMSDDDs.put(desc.getName(), desc);

                desc = new JMSDestinationDefinitionDescriptor();
                desc.setDescription("module-scope jms destination defined in UT Entity EJB DD");
                desc.setName("java:module/env/UT_Entity_DD_JMSDestination");
                desc.setClassName("javax.jms.Topic");
                desc.setResourceAdapter("jmsra");
                desc.setDestinationName("myPhysicalTopic");
                expectedJMSDDDs.put(desc.getName(), desc);

                desc = new JMSDestinationDefinitionDescriptor();
                desc.setDescription("component-scope jms destination defined in UT Entity EJB DD");
                desc.setName("java:comp/env/UT_Entity_DD_JMSDestination");
                desc.setClassName("javax.jms.Queue");
                desc.setResourceAdapter("jmsra");
                desc.setDestinationName("myPhysicalQueue");
                expectedJMSDDDs.put(desc.getName(), desc);

                TestUtil.compareJMSDDD(expectedJMSDDDs, ejbDescriptor.getResourceDescriptors(JavaEEResourceType.JMSDD));
            }

            stat.addStatus(tcName, stat.PASS);

        } catch(Exception e) {
            stat.addStatus(tcName, stat.FAIL);
            throw e;
        } finally {
            if (ddIS != null) {
                ddIS.close();
            }
        }
    }

    public void testMDBEJBDD() throws Exception {
        String tcName = "jms-destination-definition-MDB-EJB-DD-test";
        InputStream ddIS = null;

        try {
            String ddFileName = "ut-mdb-ejb-jar.xml";
            File ddFile = new File(descriptorDir, ddFileName);
            Assert.assertTrue("The ut-mdb-ejb-jar.xml not found: " + ddFile, ddFile.exists());

            ddIS = new FileInputStream(ddFile);
            EjbDeploymentDescriptorFile ddReader = new EjbDeploymentDescriptorFile();
            EjbBundleDescriptor ejbBundle = (EjbBundleDescriptor)ddReader.read(ddIS);
            assertEquals("Only contains one MDB", 1, ejbBundle.getEjbs().size());
            for (EjbDescriptor ejbDescriptor : ejbBundle.getEjbs()) {
                JMSDestinationDefinitionDescriptor desc;
                Map<String, JMSDestinationDefinitionDescriptor> expectedJMSDDDs = new HashMap<String, JMSDestinationDefinitionDescriptor>();

                desc = new JMSDestinationDefinitionDescriptor();
                desc.setDescription("global-scope jms destination defined in UT MDB EJB DD");
                desc.setName("java:global/env/UT_MDB_DD_JMSDestination");
                desc.setClassName("javax.jms.Queue");
                desc.setResourceAdapter("jmsra");
                desc.setDestinationName("myPhysicalQueue");
                expectedJMSDDDs.put(desc.getName(), desc);

                desc = new JMSDestinationDefinitionDescriptor();
                desc.setDescription("application-scope jms destination defined in UT MDB EJB DD");
                desc.setName("java:app/env/UT_MDB_DD_JMSDestination");
                desc.setClassName("javax.jms.Topic");
                desc.setResourceAdapter("jmsra");
                desc.setDestinationName("myPhysicalTopic");
                expectedJMSDDDs.put(desc.getName(), desc);

                desc = new JMSDestinationDefinitionDescriptor();
                desc.setDescription("module-scope jms destination defined in UT MDB EJB DD");
                desc.setName("java:module/env/UT_MDB_DD_JMSDestination");
                desc.setClassName("javax.jms.Topic");
                desc.setResourceAdapter("jmsra");
                desc.setDestinationName("myPhysicalTopic");
                expectedJMSDDDs.put(desc.getName(), desc);

                desc = new JMSDestinationDefinitionDescriptor();
                desc.setDescription("component-scope jms destination defined in UT MDB EJB DD");
                desc.setName("java:comp/env/UT_MDB_DD_JMSDestination");
                desc.setClassName("javax.jms.Queue");
                desc.setResourceAdapter("jmsra");
                desc.setDestinationName("myPhysicalQueue");
                expectedJMSDDDs.put(desc.getName(), desc);

                TestUtil.compareJMSDDD(expectedJMSDDDs, ejbDescriptor.getResourceDescriptors(JavaEEResourceType.JMSDD));
            }

            stat.addStatus(tcName, stat.PASS);

        } catch(Exception e) {
            stat.addStatus(tcName, stat.FAIL);
            throw e;
        } finally {
            if (ddIS != null) {
                ddIS.close();
            }
        }
    }

    public void testInterceptorEJBDD() throws Exception {
        String tcName = "jms-destination-definition-Interceptor-EJB-DD-test";
        InputStream ddIS = null;

        try {
            String ddFileName = "ut-interceptor-ejb-jar.xml";
            File ddFile = new File(descriptorDir, ddFileName);
            Assert.assertTrue("The ut-interceptor-ejb-jar.xml not found: " + ddFile, ddFile.exists());

            ddIS = new FileInputStream(ddFile);
            EjbDeploymentDescriptorFile ddReader = new EjbDeploymentDescriptorFile();
            EjbBundleDescriptor ejbBundle = (EjbBundleDescriptor)ddReader.read(ddIS);
            assertEquals("Only contains one interceptor", 1, ejbBundle.getInterceptors().size());
            for (EjbInterceptor interceptor : ejbBundle.getInterceptors()) {
                JMSDestinationDefinitionDescriptor desc;
                Map<String, JMSDestinationDefinitionDescriptor> expectedJMSDDDs = new HashMap<String, JMSDestinationDefinitionDescriptor>();

                desc = new JMSDestinationDefinitionDescriptor();
                desc.setDescription("global-scope jms destination defined in UT Interceptor EJB DD");
                desc.setName("java:global/env/UT_Interceptor_DD_JMSDestination");
                desc.setClassName("javax.jms.Queue");
                desc.setResourceAdapter("jmsra");
                desc.setDestinationName("myPhysicalQueue");
                expectedJMSDDDs.put(desc.getName(), desc);

                desc = new JMSDestinationDefinitionDescriptor();
                desc.setDescription("application-scope jms destination defined in UT Interceptor EJB DD");
                desc.setName("java:app/env/UT_Interceptor_DD_JMSDestination");
                desc.setClassName("javax.jms.Topic");
                desc.setResourceAdapter("jmsra");
                desc.setDestinationName("myPhysicalTopic");
                expectedJMSDDDs.put(desc.getName(), desc);

                desc = new JMSDestinationDefinitionDescriptor();
                desc.setDescription("module-scope jms destination defined in UT Interceptor EJB DD");
                desc.setName("java:module/env/UT_Interceptor_DD_JMSDestination");
                desc.setClassName("javax.jms.Topic");
                desc.setResourceAdapter("jmsra");
                desc.setDestinationName("myPhysicalTopic");
                expectedJMSDDDs.put(desc.getName(), desc);

                desc = new JMSDestinationDefinitionDescriptor();
                desc.setDescription("component-scope jms destination defined in UT Interceptor EJB DD");
                desc.setName("java:comp/env/UT_Interceptor_DD_JMSDestination");
                desc.setClassName("javax.jms.Queue");
                desc.setResourceAdapter("jmsra");
                desc.setDestinationName("myPhysicalQueue");
                expectedJMSDDDs.put(desc.getName(), desc);

                TestUtil.compareJMSDDD(expectedJMSDDDs, interceptor.getResourceDescriptors(JavaEEResourceType.JMSDD));
            }

            stat.addStatus(tcName, stat.PASS);

        } catch(Exception e) {
            stat.addStatus(tcName, stat.FAIL);
            throw e;
        } finally {
            if (ddIS != null) {
                ddIS.close();
            }
        }
    }

    public void testWebDD() throws Exception {
        String tcName = "jms-destination-definition-Web-DD-test";
        InputStream ddIS = null;

        try {
            String ddFileName = "ut-web.xml";
            File ddFile = new File(descriptorDir, ddFileName);
            Assert.assertTrue("The ut-web.xml not found: " + ddFile, ddFile.exists());

            ddIS = new FileInputStream(ddFile);
            WebDeploymentDescriptorFile ddReader = new WebDeploymentDescriptorFile();
            WebBundleDescriptor webBundle = ddReader.read(ddIS);

            JMSDestinationDefinitionDescriptor desc;
            Map<String, JMSDestinationDefinitionDescriptor> expectedJMSDDDs = new HashMap<String, JMSDestinationDefinitionDescriptor>();

            desc = new JMSDestinationDefinitionDescriptor();
            desc.setDescription("global-scope jms destination defined in UT Web DD");
            desc.setName("java:global/env/UT_Web_DD_JMSDestination");
            desc.setClassName("javax.jms.Queue");
            desc.setResourceAdapter("jmsra");
            desc.setDestinationName("myPhysicalQueue");
            expectedJMSDDDs.put(desc.getName(), desc);

            desc = new JMSDestinationDefinitionDescriptor();
            desc.setDescription("application-scope jms destination defined in UT Web DD");
            desc.setName("java:app/env/UT_Web_DD_JMSDestination");
            desc.setClassName("javax.jms.Topic");
            desc.setResourceAdapter("jmsra");
            desc.setDestinationName("myPhysicalTopic");
            expectedJMSDDDs.put(desc.getName(), desc);

            desc = new JMSDestinationDefinitionDescriptor();
            desc.setDescription("module-scope jms destination defined in UT Web DD");
            desc.setName("java:module/env/UT_Web_DD_JMSDestination");
            desc.setClassName("javax.jms.Topic");
            desc.setResourceAdapter("jmsra");
            desc.setDestinationName("myPhysicalTopic");
            expectedJMSDDDs.put(desc.getName(), desc);

            desc = new JMSDestinationDefinitionDescriptor();
            desc.setDescription("component-scope jms destination defined in UT Web DD");
            desc.setName("java:comp/env/UT_Web_DD_JMSDestination");
            desc.setClassName("javax.jms.Queue");
            desc.setResourceAdapter("jmsra");
            desc.setDestinationName("myPhysicalQueue");
            expectedJMSDDDs.put(desc.getName(), desc);

            TestUtil.compareJMSDDD(expectedJMSDDDs, webBundle.getResourceDescriptors(JavaEEResourceType.JMSDD));

            stat.addStatus(tcName, stat.PASS);

        } catch(Exception e) {
            stat.addStatus(tcName, stat.FAIL);
            throw e;
        } finally {
            if (ddIS != null) {
                ddIS.close();
            }
        }
    }
}
