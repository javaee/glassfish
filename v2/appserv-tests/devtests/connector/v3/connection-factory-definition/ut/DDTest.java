/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.cfd;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.io.ApplicationDeploymentDescriptorFile;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.glassfish.deployment.common.Descriptor;
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
        
        String tcName = "connection-factory-definition-application-DD-test";
        InputStream ddIS=null;
        try{
            String ddFileName = "ut-application.xml";
            File ddFile = new File(descriptorDir, ddFileName);
            Assert.assertTrue("The application.xml not found: "+ddFile,ddFile.exists());
            
            ddIS = new FileInputStream(ddFile);
            ApplicationDeploymentDescriptorFile ddReader = new ApplicationDeploymentDescriptorFile();
            Application application = (Application) ddReader.read( ddIS);
            
            Set<ResourceDescriptor> actualCFDDs = application.getResourceDescriptors(JavaEEResourceType.CFD);

            Map<String,ConnectionFactoryDefinitionDescriptor> expectedCFDDs = 
                    new HashMap<String,ConnectionFactoryDefinitionDescriptor>();
            ConnectionFactoryDefinitionDescriptor desc;

            desc = new ConnectionFactoryDefinitionDescriptor();
            desc.setDescription("global-scope resource defined in application DD");
            desc.setName("java:global/env/ConnectionFactory");
            desc.setInterfaceName("javax.resource.cci.ConnectionFactory");
            desc.setResourceAdapter("RaApplicationName");
            desc.setTransactionSupport("LocalTransaction");
            desc.setMaxPoolSize(16);
            desc.setMinPoolSize(4);
            desc.addProperty("testName", "foo");
            expectedCFDDs.put(desc.getName(), desc);
            
            desc = new ConnectionFactoryDefinitionDescriptor();
            desc.setName("java:app/env/ConnectionFactory");
            desc.setInterfaceName("javax.resource.cci.ConnectionFactory");
            desc.setResourceAdapter("RaApplicationName");
            desc.setDescription("application-scope resource defined in application DD");
            desc.setTransactionSupport("NoTransaction");
            desc.addProperty("testName", "foo");
            expectedCFDDs.put(desc.getName(), desc);

            TestUtil.compareCFDD(expectedCFDDs, actualCFDDs);
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
        
        String tcName = "connection-factory-definition-Session-EJB-DD-test";
        InputStream ddIS=null;
        try{
            String ddFileName = "ut-session-ejb-jar.xml";
            File ddFile = new File(descriptorDir, ddFileName);
            Assert.assertTrue("The ut-session-ejb-jar.xml not found: "+ddFile, ddFile.exists());
            
            ddIS = new FileInputStream(ddFile);
            EjbDeploymentDescriptorFile ddReader = new EjbDeploymentDescriptorFile();
            EjbBundleDescriptor ejbBundle = (EjbBundleDescriptor) ddReader.read( ddIS);
            
            for(EjbDescriptor ejbDescriptor : ejbBundle.getEjbs()){
                ejbDescriptor.getResourceDescriptors(JavaEEResourceType.CFD);
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
        ConnectionFactoryDefinitionDescriptor desc;
        Map<String,ConnectionFactoryDefinitionDescriptor> expectedCFDDs = new HashMap<String,ConnectionFactoryDefinitionDescriptor>();

        desc = new ConnectionFactoryDefinitionDescriptor();
        desc.setDescription("global-scope resource defined in EJB DD");
        desc.setName("java:global/env/StatefulEJB_ConnectionFactory");
        desc.setInterfaceName("javax.resource.cci.ConnectionFactory");
        desc.setResourceAdapter("RaApplicationName");
        desc.setTransactionSupport("LocalTransaction");
        desc.setMaxPoolSize(16);
        desc.setMinPoolSize(4);
        desc.addProperty("testName", "foo");
        expectedCFDDs.put(desc.getName(), desc);
        
        desc = new ConnectionFactoryDefinitionDescriptor();
        desc.setDescription("module-scope resource defined in EJB DD");
        desc.setName("java:module/env/StatefulEJB_ConnectionFactory");
        desc.setInterfaceName("javax.resource.cci.ConnectionFactory");
        desc.setResourceAdapter("RaApplicationName");
        desc.setTransactionSupport("XATransaction");
        desc.setMaxPoolSize(16);
        desc.setMinPoolSize(4);
        desc.addProperty("testName", "foo");
        expectedCFDDs.put(desc.getName(), desc);

        desc = new ConnectionFactoryDefinitionDescriptor();
        desc.setDescription("component-scope resource defined in EJB DD");
        desc.setName("java:comp/env/StatefulEJB_ConnectionFactory");
        desc.setInterfaceName("javax.resource.cci.ConnectionFactory");
        desc.setResourceAdapter("RaApplicationName");
        desc.addProperty("testName", "foo");
        expectedCFDDs.put(desc.getName(), desc);

        TestUtil.compareCFDD(expectedCFDDs, ejb.getResourceDescriptors(JavaEEResourceType.CFD));
        
    }
    
    private void testStatelessSessionEJBDD(EjbDescriptor ejb) throws Exception{
        ConnectionFactoryDefinitionDescriptor desc;
        Map<String,ConnectionFactoryDefinitionDescriptor> expectedCFDDs = new HashMap<String,ConnectionFactoryDefinitionDescriptor>();

        desc = new ConnectionFactoryDefinitionDescriptor();
        desc.setDescription("global-scope resource defined in EJB DD");
        desc.setName("java:global/env/HelloEJB_ConnectionFactory");
        desc.setInterfaceName("javax.resource.cci.ConnectionFactory");
        desc.setResourceAdapter("RaApplicationName");
        desc.setTransactionSupport("LocalTransaction");
        desc.setMaxPoolSize(16);
        desc.setMinPoolSize(4);
        desc.addProperty("testName", "foo");
        expectedCFDDs.put(desc.getName(), desc);
        
        desc = new ConnectionFactoryDefinitionDescriptor();
        desc.setDescription("module-scope resource defined in EJB DD");
        desc.setName("java:module/env/HelloEJB_ConnectionFactory");
        desc.setInterfaceName("javax.resource.cci.ConnectionFactory");
        desc.setResourceAdapter("RaApplicationName");
        desc.setTransactionSupport("XATransaction");
        desc.setMaxPoolSize(16);
        desc.setMinPoolSize(4);
        desc.addProperty("testName", "foo");
        expectedCFDDs.put(desc.getName(), desc);

        desc = new ConnectionFactoryDefinitionDescriptor();
        desc.setDescription("component-scope resource defined in EJB DD");
        desc.setName("java:comp/env/HelloEJB_ConnectionFactory");
        desc.setInterfaceName("javax.resource.cci.ConnectionFactory");
        desc.setResourceAdapter("RaApplicationName");
        desc.addProperty("testName", "foo");
        expectedCFDDs.put(desc.getName(), desc);

        TestUtil.compareCFDD(expectedCFDDs, ejb.getResourceDescriptors(JavaEEResourceType.CFD));
        
    }
    
    public void testEntityEJBDD() throws Exception{
        
        String tcName = "connection-factory-definition-Entity-EJB-DD-test";
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
                ConnectionFactoryDefinitionDescriptor desc;
                Map<String,ConnectionFactoryDefinitionDescriptor> expectedCFDDs = new HashMap<String,ConnectionFactoryDefinitionDescriptor>();

                desc = new ConnectionFactoryDefinitionDescriptor();
                desc.setDescription("global-scope resource defined in EJB DD");
                desc.setName("java:global/env/Entity_ConnectionFactory");
                desc.setInterfaceName("javax.resource.cci.ConnectionFactory");
                desc.setResourceAdapter("RaApplicationName");
                desc.setTransactionSupport("LocalTransaction");
                desc.setMaxPoolSize(16);
                desc.setMinPoolSize(4);
                desc.addProperty("testName", "foo");
                expectedCFDDs.put(desc.getName(), desc);
                
                desc = new ConnectionFactoryDefinitionDescriptor();
                desc.setDescription("module-scope resource defined in EJB DD");
                desc.setName("java:module/env/Entity_ConnectionFactory");
                desc.setInterfaceName("javax.resource.cci.ConnectionFactory");
                desc.setResourceAdapter("RaApplicationName");
                desc.setTransactionSupport("XATransaction");
                desc.setMaxPoolSize(16);
                desc.setMinPoolSize(4);
                desc.addProperty("testName", "foo");
                expectedCFDDs.put(desc.getName(), desc);

                desc = new ConnectionFactoryDefinitionDescriptor();
                desc.setDescription("component-scope resource defined in EJB DD");
                desc.setName("java:comp/env/Entity_ConnectionFactory");
                desc.setInterfaceName("javax.resource.cci.ConnectionFactory");
                desc.setResourceAdapter("RaApplicationName");
                desc.addProperty("testName", "foo");
                expectedCFDDs.put(desc.getName(), desc);

                TestUtil.compareCFDD(expectedCFDDs, ejbDescriptor.getResourceDescriptors(JavaEEResourceType.CFD));
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
        
        String tcName = "connection-factory-definition-MDB-EJB-DD-test";
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
                ConnectionFactoryDefinitionDescriptor desc;
                Map<String,ConnectionFactoryDefinitionDescriptor> expectedCFDDs = new HashMap<String,ConnectionFactoryDefinitionDescriptor>();

                desc = new ConnectionFactoryDefinitionDescriptor();
                desc.setDescription("global-scope resource defined in EJB DD");
                desc.setName("java:global/env/MDB_ConnectionFactory");
                desc.setInterfaceName("javax.resource.cci.ConnectionFactory");
                desc.setResourceAdapter("RaApplicationName");
                desc.setTransactionSupport("LocalTransaction");
                desc.setMaxPoolSize(16);
                desc.setMinPoolSize(4);
                desc.addProperty("testName", "foo");
                expectedCFDDs.put(desc.getName(), desc);
                
                desc = new ConnectionFactoryDefinitionDescriptor();
                desc.setDescription("module-scope resource defined in EJB DD");
                desc.setName("java:module/env/MDB_ConnectionFactory");
                desc.setInterfaceName("javax.resource.cci.ConnectionFactory");
                desc.setResourceAdapter("RaApplicationName");
                desc.setTransactionSupport("XATransaction");
                desc.setMaxPoolSize(16);
                desc.setMinPoolSize(4);
                desc.addProperty("testName", "foo");
                expectedCFDDs.put(desc.getName(), desc);

                desc = new ConnectionFactoryDefinitionDescriptor();
                desc.setDescription("component-scope resource defined in EJB DD");
                desc.setName("java:comp/env/MDB_ConnectionFactory");
                desc.setInterfaceName("javax.resource.cci.ConnectionFactory");
                desc.setResourceAdapter("RaApplicationName");
                desc.addProperty("testName", "foo");
                expectedCFDDs.put(desc.getName(), desc);

                TestUtil.compareCFDD(expectedCFDDs, ejbDescriptor.getResourceDescriptors(JavaEEResourceType.CFD));
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
        
        String tcName = "connection-factory-definition-Interceptor-EJB-DD-test";
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
                ConnectionFactoryDefinitionDescriptor desc;
                Map<String,ConnectionFactoryDefinitionDescriptor> expectedCFDDs = new HashMap<String,ConnectionFactoryDefinitionDescriptor>();

                desc = new ConnectionFactoryDefinitionDescriptor();
                desc.setDescription("global-scope resource defined in EJB DD");
                desc.setName("java:global/env/Interceptor_ConnectionFactory");
                desc.setInterfaceName("javax.resource.cci.ConnectionFactory");
                desc.setResourceAdapter("RaApplicationName");
                desc.setTransactionSupport("LocalTransaction");
                desc.setMaxPoolSize(16);
                desc.setMinPoolSize(4);
                desc.addProperty("testName", "foo");
                expectedCFDDs.put(desc.getName(), desc);
                
                desc = new ConnectionFactoryDefinitionDescriptor();
                desc.setDescription("module-scope resource defined in EJB DD");
                desc.setName("java:module/env/Interceptor_ConnectionFactory");
                desc.setInterfaceName("javax.resource.cci.ConnectionFactory");
                desc.setResourceAdapter("RaApplicationName");
                desc.setTransactionSupport("XATransaction");
                desc.setMaxPoolSize(16);
                desc.setMinPoolSize(4);
                desc.addProperty("testName", "foo");
                expectedCFDDs.put(desc.getName(), desc);

                desc = new ConnectionFactoryDefinitionDescriptor();
                desc.setDescription("component-scope resource defined in EJB DD");
                desc.setName("java:comp/env/Interceptor_ConnectionFactory");
                desc.setInterfaceName("javax.resource.cci.ConnectionFactory");
                desc.setResourceAdapter("RaApplicationName");
                desc.addProperty("testName", "foo");
                expectedCFDDs.put(desc.getName(), desc);

                TestUtil.compareCFDD(expectedCFDDs, interceptor.getResourceDescriptors(JavaEEResourceType.CFD));
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
        
        String tcName = "connection-factory-definition-Web-DD-test";
        InputStream ddIS=null;
        try{
            String ddFileName = "ut-web.xml";
            File ddFile = new File(descriptorDir, ddFileName);
            Assert.assertTrue("The ut-web.xml not found: "+ddFile, ddFile.exists());
            
            ddIS = new FileInputStream(ddFile);
            WebDeploymentDescriptorFile ddReader = new WebDeploymentDescriptorFile();
            WebBundleDescriptor webBundle =  ddReader.read( ddIS);
            
            ConnectionFactoryDefinitionDescriptor desc;
            Map<String,ConnectionFactoryDefinitionDescriptor> expectedCFDDs = new HashMap<String,ConnectionFactoryDefinitionDescriptor>();

            desc = new ConnectionFactoryDefinitionDescriptor();
            desc.setDescription("global-scope resource defined in Web DD");
            desc.setName("java:global/env/ConnectionFactory");
            desc.setInterfaceName("javax.resource.cci.ConnectionFactory");
            desc.setResourceAdapter("RaApplicationName");
            desc.setTransactionSupport("LocalTransaction");
            desc.setMaxPoolSize(16);
            desc.setMinPoolSize(4);
            desc.addProperty("testName", "foo");
            expectedCFDDs.put(desc.getName(), desc);
            
            desc = new ConnectionFactoryDefinitionDescriptor();
            desc.setDescription("application-scope resource defined in Web DD");
            desc.setName("java:app/env/ConnectionFactory");
            desc.setInterfaceName("javax.resource.cci.ConnectionFactory");
            desc.setResourceAdapter("RaApplicationName");
            desc.setTransactionSupport("XATransaction");
            desc.setMaxPoolSize(16);
            desc.setMinPoolSize(4);
            desc.addProperty("testName", "foo");
            expectedCFDDs.put(desc.getName(), desc);

            desc = new ConnectionFactoryDefinitionDescriptor();
            desc.setDescription("module-scope resource defined in Web DD");
            desc.setName("java:module/env/ConnectionFactory");
            desc.setInterfaceName("javax.resource.cci.ConnectionFactory");
            desc.setResourceAdapter("RaApplicationName");
            desc.addProperty("testName", "foo");
            expectedCFDDs.put(desc.getName(), desc);

            TestUtil.compareCFDD(expectedCFDDs, webBundle.getResourceDescriptors(JavaEEResourceType.CFD));

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
