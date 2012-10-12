/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.jmsdd;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbInterceptor;
import com.sun.enterprise.deployment.JMSDestinationDefinitionDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.io.AppClientDeploymentDescriptorFile;
import com.sun.enterprise.deployment.io.ApplicationDeploymentDescriptorFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;
import junit.framework.Assert;

import org.glassfish.ejb.deployment.io.EjbDeploymentDescriptorFile;
import org.glassfish.web.deployment.io.WebDeploymentDescriptorFile;

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

            Set<JMSDestinationDefinitionDescriptor> actualJMSDDDs = application.getJMSDestinationDefinitionDescriptors();

            Map<String, JMSDestinationDefinitionDescriptor> expectedJMSDDDs =
                    new HashMap<String, JMSDestinationDefinitionDescriptor>();
            JMSDestinationDefinitionDescriptor desc;

            desc = new JMSDestinationDefinitionDescriptor();
            desc.setDescription("global-scope jms destination defined in UT Application DD");
            desc.setName("java:global/env/UT_Application_DD_JMSDestination");
            desc.setClassName("javax.jms.Queue");
            desc.setResourceAdapterName("jmsra");
            desc.setDestinationName("myPhysicalQueue");
            expectedJMSDDDs.put(desc.getName(), desc);

            desc = new JMSDestinationDefinitionDescriptor();
            desc.setDescription("application-scope jms destination defined in UT Application DD");
            desc.setName("java:app/env/UT_Application_DD_JMSDestination");
            desc.setClassName("javax.jms.Topic");
            desc.setResourceAdapterName("jmsra");
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
            desc.setResourceAdapterName("jmsra");
            desc.setDestinationName("myPhysicalQueue");
            expectedJMSDDDs.put(desc.getName(), desc);

            desc = new JMSDestinationDefinitionDescriptor();
            desc.setDescription("application-scope jms destination defined in UT Appclient DD");
            desc.setName("java:app/env/UT_Appclient_DD_JMSDestination");
            desc.setClassName("javax.jms.Topic");
            desc.setResourceAdapterName("jmsra");
            desc.setDestinationName("myPhysicalTopic");
            expectedJMSDDDs.put(desc.getName(), desc);

            desc = new JMSDestinationDefinitionDescriptor();
            desc.setDescription("module-scope jms destination defined in UT Appclient DD");
            desc.setName("java:module/env/UT_Appclient_DD_JMSDestination");
            desc.setClassName("javax.jms.Topic");
            desc.setResourceAdapterName("jmsra");
            desc.setDestinationName("myPhysicalTopic");
            expectedJMSDDDs.put(desc.getName(), desc);

            desc = new JMSDestinationDefinitionDescriptor();
            desc.setDescription("component-scope jms destination defined in UT Appclient DD");
            desc.setName("java:comp/env/UT_Appclient_DD_JMSDestination");
            desc.setClassName("javax.jms.Queue");
            desc.setResourceAdapterName("jmsra");
            desc.setDestinationName("myPhysicalQueue");
            expectedJMSDDDs.put(desc.getName(), desc);

            TestUtil.compareJMSDDD(expectedJMSDDDs, appclientDesc.getJMSDestinationDefinitionDescriptors());

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
                ejbDescriptor.getJMSDestinationDefinitionDescriptors();
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
        desc.setResourceAdapterName("jmsra");
        desc.setDestinationName("myPhysicalQueue");
        expectedJMSDDDs.put(desc.getName(), desc);

        desc = new JMSDestinationDefinitionDescriptor();
        desc.setDescription("application-scope jms destination defined in UT Session EJB DD");
        desc.setName("java:app/env/UT_HelloStatefulEJB_DD_JMSDestination");
        desc.setClassName("javax.jms.Topic");
        desc.setResourceAdapterName("jmsra");
        desc.setDestinationName("myPhysicalTopic");
        expectedJMSDDDs.put(desc.getName(), desc);

        desc = new JMSDestinationDefinitionDescriptor();
        desc.setDescription("module-scope jms destination defined in UT Session EJB DD");
        desc.setName("java:module/env/UT_HelloStatefulEJB_DD_JMSDestination");
        desc.setClassName("javax.jms.Topic");
        desc.setResourceAdapterName("jmsra");
        desc.setDestinationName("myPhysicalTopic");
        expectedJMSDDDs.put(desc.getName(), desc);

        desc = new JMSDestinationDefinitionDescriptor();
        desc.setDescription("component-scope jms destination defined in UT Session EJB DD");
        desc.setName("java:comp/env/UT_HelloStatefulEJB_DD_JMSDestination");
        desc.setClassName("javax.jms.Queue");
        desc.setResourceAdapterName("jmsra");
        desc.setDestinationName("myPhysicalQueue");
        expectedJMSDDDs.put(desc.getName(), desc);

        TestUtil.compareJMSDDD(expectedJMSDDDs, ejb.getJMSDestinationDefinitionDescriptors());
    }

    private void testStatelessSessionEJBDD(EjbDescriptor ejb) {
        JMSDestinationDefinitionDescriptor desc;
        Map<String, JMSDestinationDefinitionDescriptor> expectedJMSDDDs = new HashMap<String, JMSDestinationDefinitionDescriptor>();

        desc = new JMSDestinationDefinitionDescriptor();
        desc.setDescription("global-scope jms destination defined in UT Session EJB DD");
        desc.setName("java:global/env/UT_HelloEJB_DD_JMSDestination");
        desc.setClassName("javax.jms.Queue");
        desc.setResourceAdapterName("jmsra");
        desc.setDestinationName("myPhysicalQueue");
        expectedJMSDDDs.put(desc.getName(), desc);

        desc = new JMSDestinationDefinitionDescriptor();
        desc.setDescription("application-scope jms destination defined in UT Session EJB DD");
        desc.setName("java:app/env/UT_HelloEJB_DD_JMSDestination");
        desc.setClassName("javax.jms.Topic");
        desc.setResourceAdapterName("jmsra");
        desc.setDestinationName("myPhysicalTopic");
        expectedJMSDDDs.put(desc.getName(), desc);

        desc = new JMSDestinationDefinitionDescriptor();
        desc.setDescription("module-scope jms destination defined in UT Session EJB DD");
        desc.setName("java:module/env/UT_HelloEJB_DD_JMSDestination");
        desc.setClassName("javax.jms.Topic");
        desc.setResourceAdapterName("jmsra");
        desc.setDestinationName("myPhysicalTopic");
        expectedJMSDDDs.put(desc.getName(), desc);

        desc = new JMSDestinationDefinitionDescriptor();
        desc.setDescription("component-scope jms destination defined in UT Session EJB DD");
        desc.setName("java:comp/env/UT_HelloEJB_DD_JMSDestination");
        desc.setClassName("javax.jms.Queue");
        desc.setResourceAdapterName("jmsra");
        desc.setDestinationName("myPhysicalQueue");
        expectedJMSDDDs.put(desc.getName(), desc);

        TestUtil.compareJMSDDD(expectedJMSDDDs, ejb.getJMSDestinationDefinitionDescriptors());
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
                desc.setResourceAdapterName("jmsra");
                desc.setDestinationName("myPhysicalQueue");
                expectedJMSDDDs.put(desc.getName(), desc);

                desc = new JMSDestinationDefinitionDescriptor();
                desc.setDescription("application-scope jms destination defined in UT Entity EJB DD");
                desc.setName("java:app/env/UT_Entity_DD_JMSDestination");
                desc.setClassName("javax.jms.Topic");
                desc.setResourceAdapterName("jmsra");
                desc.setDestinationName("myPhysicalTopic");
                expectedJMSDDDs.put(desc.getName(), desc);

                desc = new JMSDestinationDefinitionDescriptor();
                desc.setDescription("module-scope jms destination defined in UT Entity EJB DD");
                desc.setName("java:module/env/UT_Entity_DD_JMSDestination");
                desc.setClassName("javax.jms.Topic");
                desc.setResourceAdapterName("jmsra");
                desc.setDestinationName("myPhysicalTopic");
                expectedJMSDDDs.put(desc.getName(), desc);

                desc = new JMSDestinationDefinitionDescriptor();
                desc.setDescription("component-scope jms destination defined in UT Entity EJB DD");
                desc.setName("java:comp/env/UT_Entity_DD_JMSDestination");
                desc.setClassName("javax.jms.Queue");
                desc.setResourceAdapterName("jmsra");
                desc.setDestinationName("myPhysicalQueue");
                expectedJMSDDDs.put(desc.getName(), desc);

                TestUtil.compareJMSDDD(expectedJMSDDDs, ejbDescriptor.getJMSDestinationDefinitionDescriptors());
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
                desc.setResourceAdapterName("jmsra");
                desc.setDestinationName("myPhysicalQueue");
                expectedJMSDDDs.put(desc.getName(), desc);

                desc = new JMSDestinationDefinitionDescriptor();
                desc.setDescription("application-scope jms destination defined in UT MDB EJB DD");
                desc.setName("java:app/env/UT_MDB_DD_JMSDestination");
                desc.setClassName("javax.jms.Topic");
                desc.setResourceAdapterName("jmsra");
                desc.setDestinationName("myPhysicalTopic");
                expectedJMSDDDs.put(desc.getName(), desc);

                desc = new JMSDestinationDefinitionDescriptor();
                desc.setDescription("module-scope jms destination defined in UT MDB EJB DD");
                desc.setName("java:module/env/UT_MDB_DD_JMSDestination");
                desc.setClassName("javax.jms.Topic");
                desc.setResourceAdapterName("jmsra");
                desc.setDestinationName("myPhysicalTopic");
                expectedJMSDDDs.put(desc.getName(), desc);

                desc = new JMSDestinationDefinitionDescriptor();
                desc.setDescription("component-scope jms destination defined in UT MDB EJB DD");
                desc.setName("java:comp/env/UT_MDB_DD_JMSDestination");
                desc.setClassName("javax.jms.Queue");
                desc.setResourceAdapterName("jmsra");
                desc.setDestinationName("myPhysicalQueue");
                expectedJMSDDDs.put(desc.getName(), desc);

                TestUtil.compareJMSDDD(expectedJMSDDDs, ejbDescriptor.getJMSDestinationDefinitionDescriptors());
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
                desc.setResourceAdapterName("jmsra");
                desc.setDestinationName("myPhysicalQueue");
                expectedJMSDDDs.put(desc.getName(), desc);

                desc = new JMSDestinationDefinitionDescriptor();
                desc.setDescription("application-scope jms destination defined in UT Interceptor EJB DD");
                desc.setName("java:app/env/UT_Interceptor_DD_JMSDestination");
                desc.setClassName("javax.jms.Topic");
                desc.setResourceAdapterName("jmsra");
                desc.setDestinationName("myPhysicalTopic");
                expectedJMSDDDs.put(desc.getName(), desc);

                desc = new JMSDestinationDefinitionDescriptor();
                desc.setDescription("module-scope jms destination defined in UT Interceptor EJB DD");
                desc.setName("java:module/env/UT_Interceptor_DD_JMSDestination");
                desc.setClassName("javax.jms.Topic");
                desc.setResourceAdapterName("jmsra");
                desc.setDestinationName("myPhysicalTopic");
                expectedJMSDDDs.put(desc.getName(), desc);

                desc = new JMSDestinationDefinitionDescriptor();
                desc.setDescription("component-scope jms destination defined in UT Interceptor EJB DD");
                desc.setName("java:comp/env/UT_Interceptor_DD_JMSDestination");
                desc.setClassName("javax.jms.Queue");
                desc.setResourceAdapterName("jmsra");
                desc.setDestinationName("myPhysicalQueue");
                expectedJMSDDDs.put(desc.getName(), desc);

                TestUtil.compareJMSDDD(expectedJMSDDDs, interceptor.getJMSDestinationDefinitionDescriptors());
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
            desc.setResourceAdapterName("jmsra");
            desc.setDestinationName("myPhysicalQueue");
            expectedJMSDDDs.put(desc.getName(), desc);

            desc = new JMSDestinationDefinitionDescriptor();
            desc.setDescription("application-scope jms destination defined in UT Web DD");
            desc.setName("java:app/env/UT_Web_DD_JMSDestination");
            desc.setClassName("javax.jms.Topic");
            desc.setResourceAdapterName("jmsra");
            desc.setDestinationName("myPhysicalTopic");
            expectedJMSDDDs.put(desc.getName(), desc);

            desc = new JMSDestinationDefinitionDescriptor();
            desc.setDescription("module-scope jms destination defined in UT Web DD");
            desc.setName("java:module/env/UT_Web_DD_JMSDestination");
            desc.setClassName("javax.jms.Topic");
            desc.setResourceAdapterName("jmsra");
            desc.setDestinationName("myPhysicalTopic");
            expectedJMSDDDs.put(desc.getName(), desc);

            desc = new JMSDestinationDefinitionDescriptor();
            desc.setDescription("component-scope jms destination defined in UT Web DD");
            desc.setName("java:comp/env/UT_Web_DD_JMSDestination");
            desc.setClassName("javax.jms.Queue");
            desc.setResourceAdapterName("jmsra");
            desc.setDestinationName("myPhysicalQueue");
            expectedJMSDDDs.put(desc.getName(), desc);

            TestUtil.compareJMSDDD(expectedJMSDDDs, webBundle.getJMSDestinationDefinitionDescriptors());

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
