/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.aod;

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
import com.sun.enterprise.deployment.AdministeredObjectDefinitionDescriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbInterceptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.io.ApplicationDeploymentDescriptorFile;

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
        
        String tcName = "administered-object-definition-application-DD-test";
        InputStream ddIS=null;
        try{
            String ddFileName = "ut-application.xml";
            File ddFile = new File(descriptorDir, ddFileName);
            Assert.assertTrue("The application.xml not found: "+ddFile,ddFile.exists());
            
            ddIS = new FileInputStream(ddFile);
            ApplicationDeploymentDescriptorFile ddReader = new ApplicationDeploymentDescriptorFile();
            Application application = (Application) ddReader.read( ddIS);
            
            Set<AdministeredObjectDefinitionDescriptor> actualAODDs = application.getAdministeredObjectDefinitionDescriptors();

            Map<String,AdministeredObjectDefinitionDescriptor> expectedAODDs = 
                    new HashMap<String,AdministeredObjectDefinitionDescriptor>();
            AdministeredObjectDefinitionDescriptor desc;

            desc = new AdministeredObjectDefinitionDescriptor();
            desc.setName("java:global/env/AdminObject");
            desc.setClassName("connector.MyAdminObject");
            desc.setDescription("global-scope resource defined in application DD");
            desc.setResourceAdapterName("aod-ra");
            desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
            expectedAODDs.put(desc.getName(), desc);
            
            desc = new AdministeredObjectDefinitionDescriptor();
            desc.setName("java:app/env/AdminObject");
            desc.setClassName("connector.MyAdminObject");
            desc.setDescription("application-scope resource defined in application DD");
            desc.setResourceAdapterName("aod-ra");
            desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
            expectedAODDs.put(desc.getName(), desc);

            TestUtil.compareAODD(expectedAODDs, actualAODDs);
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
        
        String tcName = "administered-object-definition-Session-EJB-DD-test";
        InputStream ddIS=null;
        try{
            String ddFileName = "ut-session-ejb-jar.xml";
            File ddFile = new File(descriptorDir, ddFileName);
            Assert.assertTrue("The ut-session-ejb-jar.xml not found: "+ddFile, ddFile.exists());
            
            ddIS = new FileInputStream(ddFile);
            EjbDeploymentDescriptorFile ddReader = new EjbDeploymentDescriptorFile();
            EjbBundleDescriptor ejbBundle = (EjbBundleDescriptor) ddReader.read( ddIS);
            
            for(EjbDescriptor ejbDescriptor : ejbBundle.getEjbs()){
                ejbDescriptor.getAdministeredObjectDefinitionDescriptors();
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
        AdministeredObjectDefinitionDescriptor desc;
        Map<String,AdministeredObjectDefinitionDescriptor> expectedAODDs = new HashMap<String,AdministeredObjectDefinitionDescriptor>();

        desc = new AdministeredObjectDefinitionDescriptor();
        desc.setDescription("global-scope resource defined in EJB DD");
        desc.setName("java:global/env/StatefulEJB_AdminObject");
        desc.setClassName("connector.MyAdminObject");
        desc.setResourceAdapterName("aod-ra");
        desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
        expectedAODDs.put(desc.getName(), desc);
        
        desc = new AdministeredObjectDefinitionDescriptor();
        desc.setDescription("module-scope resource defined in EJB DD");
        desc.setName("java:module/env/StatefulEJB_AdminObject");
        desc.setClassName("connector.MyAdminObject");
        desc.setResourceAdapterName("aod-ra");
        desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
        expectedAODDs.put(desc.getName(), desc);

        desc = new AdministeredObjectDefinitionDescriptor();
        desc.setDescription("component-scope resource defined in EJB DD");
        desc.setName("java:comp/env/StatefulEJB_AdminObject");
        desc.setClassName("connector.MyAdminObject");
        desc.setResourceAdapterName("aod-ra");
        desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
        expectedAODDs.put(desc.getName(), desc);

        TestUtil.compareAODD(expectedAODDs, ejb.getAdministeredObjectDefinitionDescriptors());
        
    }
    
    private void testStatelessSessionEJBDD(EjbDescriptor ejb) throws Exception{
        AdministeredObjectDefinitionDescriptor desc;
        Map<String,AdministeredObjectDefinitionDescriptor> expectedAODDs = new HashMap<String,AdministeredObjectDefinitionDescriptor>();

        desc = new AdministeredObjectDefinitionDescriptor();
        desc.setDescription("global-scope resource defined in EJB DD");
        desc.setName("java:global/env/HelloEJB_AdminObject");
        desc.setClassName("connector.MyAdminObject");
        desc.setResourceAdapterName("aod-ra");
        desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
        expectedAODDs.put(desc.getName(), desc);
        
        desc = new AdministeredObjectDefinitionDescriptor();
        desc.setDescription("module-scope resource defined in EJB DD");
        desc.setName("java:module/env/HelloEJB_AdminObject");
        desc.setClassName("connector.MyAdminObject");
        desc.setResourceAdapterName("aod-ra");
        desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
        expectedAODDs.put(desc.getName(), desc);

        desc = new AdministeredObjectDefinitionDescriptor();
        desc.setDescription("component-scope resource defined in EJB DD");
        desc.setName("java:comp/env/HelloEJB_AdminObject");
        desc.setClassName("connector.MyAdminObject");
        desc.setResourceAdapterName("aod-ra");
        desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
        expectedAODDs.put(desc.getName(), desc);

        TestUtil.compareAODD(expectedAODDs, ejb.getAdministeredObjectDefinitionDescriptors());
        
    }
    
    public void testEntityEJBDD() throws Exception{
        
        String tcName = "administered-object-definition-Entity-EJB-DD-test";
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
                AdministeredObjectDefinitionDescriptor desc;
                Map<String,AdministeredObjectDefinitionDescriptor> expectedAODDs = new HashMap<String,AdministeredObjectDefinitionDescriptor>();

                desc = new AdministeredObjectDefinitionDescriptor();
                desc.setDescription("global-scope resource defined in EJB DD");
                desc.setName("java:global/env/Entity_AdminObject");
                desc.setClassName("connector.MyAdminObject");
                desc.setResourceAdapterName("aod-ra");
                desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
                expectedAODDs.put(desc.getName(), desc);
                
                desc = new AdministeredObjectDefinitionDescriptor();
                desc.setDescription("module-scope resource defined in EJB DD");
                desc.setName("java:module/env/Entity_AdminObject");
                desc.setClassName("connector.MyAdminObject");
                desc.setResourceAdapterName("aod-ra");
                desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
                expectedAODDs.put(desc.getName(), desc);

                desc = new AdministeredObjectDefinitionDescriptor();
                desc.setDescription("component-scope resource defined in EJB DD");
                desc.setName("java:comp/env/Entity_AdminObject");
                desc.setClassName("connector.MyAdminObject");
                desc.setResourceAdapterName("aod-ra");
                desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
                expectedAODDs.put(desc.getName(), desc);

                TestUtil.compareAODD(expectedAODDs, ejbDescriptor.getAdministeredObjectDefinitionDescriptors());
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
        
        String tcName = "administered-object-definition-MDB-EJB-DD-test";
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
                AdministeredObjectDefinitionDescriptor desc;
                Map<String,AdministeredObjectDefinitionDescriptor> expectedAODDs = new HashMap<String,AdministeredObjectDefinitionDescriptor>();

                desc = new AdministeredObjectDefinitionDescriptor();
                desc.setDescription("global-scope resource defined in EJB DD");
                desc.setName("java:global/env/MDB_AdminObject");
                desc.setClassName("connector.MyAdminObject");
                desc.setResourceAdapterName("aod-ra");
                desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
                expectedAODDs.put(desc.getName(), desc);
                
                desc = new AdministeredObjectDefinitionDescriptor();
                desc.setDescription("module-scope resource defined in EJB DD");
                desc.setName("java:module/env/MDB_AdminObject");
                desc.setClassName("connector.MyAdminObject");
                desc.setResourceAdapterName("aod-ra");
                desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
                expectedAODDs.put(desc.getName(), desc);

                desc = new AdministeredObjectDefinitionDescriptor();
                desc.setDescription("component-scope resource defined in EJB DD");
                desc.setName("java:comp/env/MDB_AdminObject");
                desc.setClassName("connector.MyAdminObject");
                desc.setResourceAdapterName("aod-ra");
                desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
                expectedAODDs.put(desc.getName(), desc);

                TestUtil.compareAODD(expectedAODDs, ejbDescriptor.getAdministeredObjectDefinitionDescriptors());
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
        
        String tcName = "administered-object-definition-Interceptor-EJB-DD-test";
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
                AdministeredObjectDefinitionDescriptor desc;
                Map<String,AdministeredObjectDefinitionDescriptor> expectedAODDs = new HashMap<String,AdministeredObjectDefinitionDescriptor>();

                desc = new AdministeredObjectDefinitionDescriptor();
                desc.setDescription("global-scope resource defined in EJB DD");
                desc.setName("java:global/env/Interceptor_AdminObject");
                desc.setClassName("connector.MyAdminObject");
                desc.setResourceAdapterName("aod-ra");
                desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
                expectedAODDs.put(desc.getName(), desc);
                
                desc = new AdministeredObjectDefinitionDescriptor();
                desc.setDescription("module-scope resource defined in EJB DD");
                desc.setName("java:module/env/Interceptor_AdminObject");
                desc.setClassName("connector.MyAdminObject");
                desc.setResourceAdapterName("aod-ra");
                desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
                expectedAODDs.put(desc.getName(), desc);

                desc = new AdministeredObjectDefinitionDescriptor();
                desc.setDescription("component-scope resource defined in EJB DD");
                desc.setName("java:comp/env/Interceptor_AdminObject");
                desc.setClassName("connector.MyAdminObject");
                desc.setResourceAdapterName("aod-ra");
                desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
                expectedAODDs.put(desc.getName(), desc);

                TestUtil.compareAODD(expectedAODDs, interceptor.getAdministeredObjectDefinitionDescriptors());
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
        
        String tcName = "administered-object-definition-Web-DD-test";
        InputStream ddIS=null;
        try{
            String ddFileName = "ut-web.xml";
            File ddFile = new File(descriptorDir, ddFileName);
            Assert.assertTrue("The ut-web.xml not found: "+ddFile, ddFile.exists());
            
            ddIS = new FileInputStream(ddFile);
            WebDeploymentDescriptorFile ddReader = new WebDeploymentDescriptorFile();
            WebBundleDescriptor webBundle =  ddReader.read( ddIS);
            
            AdministeredObjectDefinitionDescriptor desc;
            Map<String,AdministeredObjectDefinitionDescriptor> expectedAODDs = new HashMap<String,AdministeredObjectDefinitionDescriptor>();

            desc = new AdministeredObjectDefinitionDescriptor();
            desc.setDescription("global-scope resource defined in Web DD");
            desc.setName("java:global/env/AdminObject");
            desc.setClassName("connector.MyAdminObject");
            desc.setResourceAdapterName("aod-ra");
            desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
            expectedAODDs.put(desc.getName(), desc);
            
            desc = new AdministeredObjectDefinitionDescriptor();
            desc.setDescription("application-scope resource defined in Web DD");
            desc.setName("java:app/env/AdminObject");
            desc.setClassName("connector.MyAdminObject");
            desc.setResourceAdapterName("aod-ra");
            desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
            expectedAODDs.put(desc.getName(), desc);

            desc = new AdministeredObjectDefinitionDescriptor();
            desc.setDescription("module-scope resource defined in Web DD");
            desc.setName("java:module/env/AdminObject");
            desc.setClassName("connector.MyAdminObject");
            desc.setResourceAdapterName("aod-ra");
            desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
            expectedAODDs.put(desc.getName(), desc);

            TestUtil.compareAODD(expectedAODDs, webBundle.getAdministeredObjectDefinitionDescriptors());

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
