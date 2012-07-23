/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.crd;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.glassfish.ejb.deployment.io.EjbDeploymentDescriptorFile;
import org.glassfish.web.deployment.io.WebDeploymentDescriptorFile;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.ConnectorResourceDefinitionDescriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbInterceptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.io.ApplicationDeploymentDescriptorFile;
import com.sun.enterprise.deployment.xml.TagNames;

import junit.framework.TestCase;
import junit.framework.Assert;
public class DDTest extends TestCase {

    private static SimpleReporterAdapter stat =  new SimpleReporterAdapter("appserv-tests");
    private File descriptorDir = null;

    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.setupHK2();
        descriptorDir = new File(System.getProperty("workDir"), "descriptor");

    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testApplicationDD() throws Exception{
        
        String tcName = "connectore-resource-definition-application-DD-test";
        InputStream ddIS=null;
        try{
            String ddFileName = "ut-application.xml";
            File ddFile = new File(descriptorDir, ddFileName);
            Assert.assertTrue("The application.xml not found: "+ddFile,ddFile.exists());
            
            ddIS = new FileInputStream(ddFile);
            ApplicationDeploymentDescriptorFile ddReader = new ApplicationDeploymentDescriptorFile();
            Application application = (Application) ddReader.read( ddIS);
            
            Set<ConnectorResourceDefinitionDescriptor> actualCRDDs = application.getConnectorResourceDefinitionDescriptors();

            Map<String,ConnectorResourceDefinitionDescriptor> expectedCRDDs = 
                    new HashMap<String,ConnectorResourceDefinitionDescriptor>();
            ConnectorResourceDefinitionDescriptor desc;

            desc = new ConnectorResourceDefinitionDescriptor();
            desc.setName("java:global/env/ConnectorResource");
            desc.setClassName("javax.resource.cci.ConnectionFactory");
            desc.setDescription("global-scope resource defined in application DD");
            desc.addProperty("transactionSupport", "LocalTransaction");
            desc.addProperty(TagNames.CONNECTOR_RESOURCE_ADAPTER_NAME, "RaApplicationName");
            expectedCRDDs.put(desc.getName(), desc);
            
            desc = new ConnectorResourceDefinitionDescriptor();
            desc.setName("java:app/env/ConnectorResource");
            desc.setClassName("javax.resource.cci.ConnectionFactory");
            desc.setDescription("application-scope resource defined in application DD");
            desc.addProperty("transactionSupport", "LocalTransaction");
            desc.addProperty(TagNames.CONNECTOR_RESOURCE_ADAPTER_NAME, "RaApplicationName");
            expectedCRDDs.put(desc.getName(), desc);

            TestUtil.compareCRDD(expectedCRDDs, actualCRDDs);
            stat.addStatus(tcName, stat.PASS);
            
        }catch(Exception e){
            stat.addStatus(tcName, stat.FAIL);
            throw e;
        }finally{
            if(ddIS != null){
                ddIS.close();
            }
        }
        return;

    }

    public void testSessionEJBDD() throws Exception{
        
        String tcName = "connectore-resource-definition-Session-EJB-DD-test";
        InputStream ddIS=null;
        try{
            String ddFileName = "ut-session-ejb-jar.xml";
            File ddFile = new File(descriptorDir, ddFileName);
            Assert.assertTrue("The ut-session-ejb-jar.xml not found: "+ddFile, ddFile.exists());
            
            ddIS = new FileInputStream(ddFile);
            EjbDeploymentDescriptorFile ddReader = new EjbDeploymentDescriptorFile();
            EjbBundleDescriptor ejbBundle = (EjbBundleDescriptor) ddReader.read( ddIS);
            
            for(EjbDescriptor ejbDescriptor : ejbBundle.getEjbs()){
                ejbDescriptor.getConnectorResourceDefinitionDescriptors();
                if(ejbDescriptor.getName().equals("HelloStatefulEJB")){
                    testStatefulSessionEJBDD(ejbDescriptor);
                }else if(ejbDescriptor.getName().equals("HelloEJB")){
                    testStatelessSessionEJBDD(ejbDescriptor);
                }else{
                    fail("Unknown EJB descriptor: "+ejbDescriptor.getName());
                }
            }
            
            stat.addStatus(tcName, stat.PASS);
            
        }catch(Exception e){
            stat.addStatus(tcName, stat.FAIL);
            throw e;
        }finally{
            if(ddIS != null){
                ddIS.close();
            }
        }

        return;
    }
    
    private void testStatefulSessionEJBDD(EjbDescriptor ejb) throws Exception{
        ConnectorResourceDefinitionDescriptor desc;
        Map<String,ConnectorResourceDefinitionDescriptor> expectedCRDDs = new HashMap<String,ConnectorResourceDefinitionDescriptor>();

        desc = new ConnectorResourceDefinitionDescriptor();
        desc.setDescription("global-scope resource defined in EJB DD");
        desc.setName("java:global/env/StatefulEJB_ConnectorResource");
        desc.setClassName("javax.resource.cci.ConnectionFactory");
        desc.addProperty("transactionSupport", "LocalTransaction");
        desc.addProperty(TagNames.CONNECTOR_RESOURCE_ADAPTER_NAME, "RaApplicationName");
        expectedCRDDs.put(desc.getName(), desc);
        
        desc = new ConnectorResourceDefinitionDescriptor();
        desc.setDescription("module-scope resource defined in EJB DD");
        desc.setName("java:module/env/StatefulEJB_ConnectorResource");
        desc.setClassName("javax.resource.cci.ConnectionFactory");
        desc.addProperty("transactionSupport", "LocalTransaction");
        desc.addProperty(TagNames.CONNECTOR_RESOURCE_ADAPTER_NAME, "RaApplicationName");
        expectedCRDDs.put(desc.getName(), desc);

        desc = new ConnectorResourceDefinitionDescriptor();
        desc.setDescription("component-scope resource defined in EJB DD");
        desc.setName("java:comp/env/StatefulEJB_ConnectorResource");
        desc.setClassName("javax.resource.cci.ConnectionFactory");
        desc.addProperty("transactionSupport", "LocalTransaction");
        desc.addProperty(TagNames.CONNECTOR_RESOURCE_ADAPTER_NAME, "RaApplicationName");
        expectedCRDDs.put(desc.getName(), desc);

        TestUtil.compareCRDD(expectedCRDDs, ejb.getConnectorResourceDefinitionDescriptors());
        
    }
    
    private void testStatelessSessionEJBDD(EjbDescriptor ejb) throws Exception{
        ConnectorResourceDefinitionDescriptor desc;
        Map<String,ConnectorResourceDefinitionDescriptor> expectedCRDDs = new HashMap<String,ConnectorResourceDefinitionDescriptor>();

        desc = new ConnectorResourceDefinitionDescriptor();
        desc.setDescription("global-scope resource defined in EJB DD");
        desc.setName("java:global/env/HelloEJB_ConnectorResource");
        desc.setClassName("javax.resource.cci.ConnectionFactory");
        desc.addProperty("transactionSupport", "LocalTransaction");
        desc.addProperty(TagNames.CONNECTOR_RESOURCE_ADAPTER_NAME, "RaApplicationName");
        expectedCRDDs.put(desc.getName(), desc);
        
        desc = new ConnectorResourceDefinitionDescriptor();
        desc.setDescription("module-scope resource defined in EJB DD");
        desc.setName("java:module/env/HelloEJB_ConnectorResource");
        desc.setClassName("javax.resource.cci.ConnectionFactory");
        desc.addProperty("transactionSupport", "LocalTransaction");
        desc.addProperty(TagNames.CONNECTOR_RESOURCE_ADAPTER_NAME, "RaApplicationName");
        expectedCRDDs.put(desc.getName(), desc);

        desc = new ConnectorResourceDefinitionDescriptor();
        desc.setDescription("component-scope resource defined in EJB DD");
        desc.setName("java:comp/env/HelloEJB_ConnectorResource");
        desc.setClassName("javax.resource.cci.ConnectionFactory");
        desc.addProperty("transactionSupport", "LocalTransaction");
        desc.addProperty(TagNames.CONNECTOR_RESOURCE_ADAPTER_NAME, "RaApplicationName");
        expectedCRDDs.put(desc.getName(), desc);

        TestUtil.compareCRDD(expectedCRDDs, ejb.getConnectorResourceDefinitionDescriptors());
        
    }
    
    public void testEntityEJBDD() throws Exception{
        
        String tcName = "connectore-resource-definition-Entity-EJB-DD-test";
        InputStream ddIS=null;
        try{
            String ddFileName = "ut-entity-ejb-jar.xml";
            File ddFile = new File(descriptorDir, ddFileName);
            Assert.assertTrue("The ut-entity-ejb-jar.xml not found: "+ddFile, ddFile.exists());
            
            ddIS = new FileInputStream(ddFile);
            EjbDeploymentDescriptorFile ddReader = new EjbDeploymentDescriptorFile();
            EjbBundleDescriptor ejbBundle = (EjbBundleDescriptor) ddReader.read( ddIS);
            assertEquals("Only contains one entity bean", 1, ejbBundle.getEjbs().size());
            for(EjbDescriptor ejbDescriptor : ejbBundle.getEjbs()){
                ConnectorResourceDefinitionDescriptor desc;
                Map<String,ConnectorResourceDefinitionDescriptor> expectedCRDDs = new HashMap<String,ConnectorResourceDefinitionDescriptor>();

                desc = new ConnectorResourceDefinitionDescriptor();
                desc.setDescription("global-scope resource defined in EJB DD");
                desc.setName("java:global/env/Entity_ConnectorResource");
                desc.setClassName("javax.resource.cci.ConnectionFactory");
                desc.addProperty("transactionSupport", "LocalTransaction");
                desc.addProperty(TagNames.CONNECTOR_RESOURCE_ADAPTER_NAME, "RaApplicationName");
                expectedCRDDs.put(desc.getName(), desc);
                
                desc = new ConnectorResourceDefinitionDescriptor();
                desc.setDescription("module-scope resource defined in EJB DD");
                desc.setName("java:module/env/Entity_ConnectorResource");
                desc.setClassName("javax.resource.cci.ConnectionFactory");
                desc.addProperty("transactionSupport", "LocalTransaction");
                desc.addProperty(TagNames.CONNECTOR_RESOURCE_ADAPTER_NAME, "RaApplicationName");
                expectedCRDDs.put(desc.getName(), desc);

                desc = new ConnectorResourceDefinitionDescriptor();
                desc.setDescription("component-scope resource defined in EJB DD");
                desc.setName("java:comp/env/Entity_ConnectorResource");
                desc.setClassName("javax.resource.cci.ConnectionFactory");
                desc.addProperty("transactionSupport", "LocalTransaction");
                desc.addProperty(TagNames.CONNECTOR_RESOURCE_ADAPTER_NAME, "RaApplicationName");
                expectedCRDDs.put(desc.getName(), desc);

                TestUtil.compareCRDD(expectedCRDDs, ejbDescriptor.getConnectorResourceDefinitionDescriptors());
            }

            stat.addStatus(tcName, stat.PASS);
            
        }catch(Exception e){
            stat.addStatus(tcName, stat.FAIL);
            throw e;
        }finally{
            if(ddIS != null){
                ddIS.close();
            }
        }

        return;
    }

    public void testMDBEJBDD() throws Exception{
        
        String tcName = "connectore-resource-definition-MDB-EJB-DD-test";
        InputStream ddIS=null;
        try{
            String ddFileName = "ut-mdb-ejb-jar.xml";
            File ddFile = new File(descriptorDir, ddFileName);
            Assert.assertTrue("The ut-mdb-ejb-jar.xml not found: "+ddFile, ddFile.exists());
            
            ddIS = new FileInputStream(ddFile);
            EjbDeploymentDescriptorFile ddReader = new EjbDeploymentDescriptorFile();
            EjbBundleDescriptor ejbBundle = (EjbBundleDescriptor) ddReader.read( ddIS);
            assertEquals("Only contains one MDB", 1, ejbBundle.getEjbs().size());
            for(EjbDescriptor ejbDescriptor : ejbBundle.getEjbs()){
                ConnectorResourceDefinitionDescriptor desc;
                Map<String,ConnectorResourceDefinitionDescriptor> expectedCRDDs = new HashMap<String,ConnectorResourceDefinitionDescriptor>();

                desc = new ConnectorResourceDefinitionDescriptor();
                desc.setDescription("global-scope resource defined in EJB DD");
                desc.setName("java:global/env/MDB_ConnectorResource");
                desc.setClassName("javax.resource.cci.ConnectionFactory");
                desc.addProperty("transactionSupport", "LocalTransaction");
                desc.addProperty(TagNames.CONNECTOR_RESOURCE_ADAPTER_NAME, "RaApplicationName");
                expectedCRDDs.put(desc.getName(), desc);
                
                desc = new ConnectorResourceDefinitionDescriptor();
                desc.setDescription("module-scope resource defined in EJB DD");
                desc.setName("java:module/env/MDB_ConnectorResource");
                desc.setClassName("javax.resource.cci.ConnectionFactory");
                desc.addProperty("transactionSupport", "LocalTransaction");
                desc.addProperty(TagNames.CONNECTOR_RESOURCE_ADAPTER_NAME, "RaApplicationName");
                expectedCRDDs.put(desc.getName(), desc);

                desc = new ConnectorResourceDefinitionDescriptor();
                desc.setDescription("component-scope resource defined in EJB DD");
                desc.setName("java:comp/env/MDB_ConnectorResource");
                desc.setClassName("javax.resource.cci.ConnectionFactory");
                desc.addProperty("transactionSupport", "LocalTransaction");
                desc.addProperty(TagNames.CONNECTOR_RESOURCE_ADAPTER_NAME, "RaApplicationName");
                expectedCRDDs.put(desc.getName(), desc);

                TestUtil.compareCRDD(expectedCRDDs, ejbDescriptor.getConnectorResourceDefinitionDescriptors());
            }

            stat.addStatus(tcName, stat.PASS);
            
        }catch(Exception e){
            stat.addStatus(tcName, stat.FAIL);
            throw e;
        }finally{
            if(ddIS != null){
                ddIS.close();
            }
        }

        return;
    }

    public void testInterceptorEJBDD() throws Exception{
        
        String tcName = "connectore-resource-definition-Interceptor-EJB-DD-test";
        InputStream ddIS=null;
        try{
            String ddFileName = "ut-interceptor-ejb-jar.xml";
            File ddFile = new File(descriptorDir, ddFileName);
            Assert.assertTrue("The ut-interceptor-ejb-jar.xml not found: "+ddFile, ddFile.exists());
            
            ddIS = new FileInputStream(ddFile);
            EjbDeploymentDescriptorFile ddReader = new EjbDeploymentDescriptorFile();
            EjbBundleDescriptor ejbBundle = (EjbBundleDescriptor) ddReader.read( ddIS);
            assertEquals("Only contains one interceptor", 1, ejbBundle.getInterceptors().size());
            for(EjbInterceptor interceptor : ejbBundle.getInterceptors()){
                ConnectorResourceDefinitionDescriptor desc;
                Map<String,ConnectorResourceDefinitionDescriptor> expectedCRDDs = new HashMap<String,ConnectorResourceDefinitionDescriptor>();

                desc = new ConnectorResourceDefinitionDescriptor();
                desc.setDescription("global-scope resource defined in EJB DD");
                desc.setName("java:global/env/Interceptor_ConnectorResource");
                desc.setClassName("javax.resource.cci.ConnectionFactory");
                desc.addProperty("transactionSupport", "LocalTransaction");
                desc.addProperty(TagNames.CONNECTOR_RESOURCE_ADAPTER_NAME, "RaApplicationName");
                expectedCRDDs.put(desc.getName(), desc);
                
                desc = new ConnectorResourceDefinitionDescriptor();
                desc.setDescription("module-scope resource defined in EJB DD");
                desc.setName("java:module/env/Interceptor_ConnectorResource");
                desc.setClassName("javax.resource.cci.ConnectionFactory");
                desc.addProperty("transactionSupport", "LocalTransaction");
                desc.addProperty(TagNames.CONNECTOR_RESOURCE_ADAPTER_NAME, "RaApplicationName");
                expectedCRDDs.put(desc.getName(), desc);

                desc = new ConnectorResourceDefinitionDescriptor();
                desc.setDescription("component-scope resource defined in EJB DD");
                desc.setName("java:comp/env/Interceptor_ConnectorResource");
                desc.setClassName("javax.resource.cci.ConnectionFactory");
                desc.addProperty("transactionSupport", "LocalTransaction");
                desc.addProperty(TagNames.CONNECTOR_RESOURCE_ADAPTER_NAME, "RaApplicationName");
                expectedCRDDs.put(desc.getName(), desc);

                TestUtil.compareCRDD(expectedCRDDs, interceptor.getConnectorResourceDefinitionDescriptors());
            }

            stat.addStatus(tcName, stat.PASS);
            
        }catch(Exception e){
            stat.addStatus(tcName, stat.FAIL);
            throw e;
        }finally{
            if(ddIS != null){
                ddIS.close();
            }
        }

        return;
    }

    public void testWebDD() throws Exception{
        
        String tcName = "connectore-resource-definition-Web-DD-test";
        InputStream ddIS=null;
        try{
            String ddFileName = "ut-web.xml";
            File ddFile = new File(descriptorDir, ddFileName);
            Assert.assertTrue("The ut-web.xml not found: "+ddFile, ddFile.exists());
            
            ddIS = new FileInputStream(ddFile);
            WebDeploymentDescriptorFile ddReader = new WebDeploymentDescriptorFile();
            WebBundleDescriptor webBundle =  ddReader.read( ddIS);
            
            ConnectorResourceDefinitionDescriptor desc;
            Map<String,ConnectorResourceDefinitionDescriptor> expectedCRDDs = new HashMap<String,ConnectorResourceDefinitionDescriptor>();

            desc = new ConnectorResourceDefinitionDescriptor();
            desc.setDescription("global-scope resource defined in Web DD");
            desc.setName("java:global/env/ConnectorResource");
            desc.setClassName("javax.resource.cci.ConnectionFactory");
            desc.addProperty("transactionSupport", "LocalTransaction");
            desc.addProperty(TagNames.CONNECTOR_RESOURCE_ADAPTER_NAME, "RaApplicationName");
            expectedCRDDs.put(desc.getName(), desc);
            
            desc = new ConnectorResourceDefinitionDescriptor();
            desc.setDescription("application-scope resource defined in Web DD");
            desc.setName("java:app/env/ConnectorResource");
            desc.setClassName("javax.resource.cci.ConnectionFactory");
            desc.addProperty("transactionSupport", "LocalTransaction");
            desc.addProperty(TagNames.CONNECTOR_RESOURCE_ADAPTER_NAME, "RaApplicationName");
            expectedCRDDs.put(desc.getName(), desc);

            desc = new ConnectorResourceDefinitionDescriptor();
            desc.setDescription("module-scope resource defined in Web DD");
            desc.setName("java:module/env/ConnectorResource");
            desc.setClassName("javax.resource.cci.ConnectionFactory");
            desc.addProperty("transactionSupport", "LocalTransaction");
            desc.addProperty(TagNames.CONNECTOR_RESOURCE_ADAPTER_NAME, "RaApplicationName");
            expectedCRDDs.put(desc.getName(), desc);

            TestUtil.compareCRDD(expectedCRDDs, webBundle.getConnectorResourceDefinitionDescriptors());

            stat.addStatus(tcName, stat.PASS);
            
        }catch(Exception e){
            stat.addStatus(tcName, stat.FAIL);
            throw e;
        }finally{
            if(ddIS != null){
                ddIS.close();
            }
        }

        return;
    }


}
