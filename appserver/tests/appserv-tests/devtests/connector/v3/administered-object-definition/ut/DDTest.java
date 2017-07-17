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

package com.sun.s1asdev.aod;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.enterprise.deployment.*;
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
            
            Set<ResourceDescriptor> actualAODDs = application.getResourceDescriptors(JavaEEResourceType.AODD);

            Map<String,AdministeredObjectDefinitionDescriptor> expectedAODDs = 
                    new HashMap<String,AdministeredObjectDefinitionDescriptor>();
            AdministeredObjectDefinitionDescriptor desc;

            desc = new AdministeredObjectDefinitionDescriptor();
            desc.setName("java:global/env/AdminObject");
            desc.setInterfaceName("javax.jms.Destination");
            desc.setClassName("connector.MyAdminObject");
            desc.setDescription("global-scope resource defined in application DD");
            desc.setResourceAdapter("aod-ra");
            desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
            expectedAODDs.put(desc.getName(), desc);
            
            desc = new AdministeredObjectDefinitionDescriptor();
            desc.setName("java:app/env/AdminObject");
            desc.setInterfaceName("javax.jms.Destination");
            desc.setClassName("connector.MyAdminObject");
            desc.setDescription("application-scope resource defined in application DD");
            desc.setResourceAdapter("aod-ra");
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
                ejbDescriptor.getResourceDescriptors(JavaEEResourceType.AODD);
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
        desc.setInterfaceName("javax.jms.Destination");
        desc.setClassName("connector.MyAdminObject");
        desc.setResourceAdapter("aod-ra");
        desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
        expectedAODDs.put(desc.getName(), desc);
        
        desc = new AdministeredObjectDefinitionDescriptor();
        desc.setDescription("module-scope resource defined in EJB DD");
        desc.setName("java:module/env/StatefulEJB_AdminObject");
        desc.setInterfaceName("javax.jms.Destination");
        desc.setClassName("connector.MyAdminObject");
        desc.setResourceAdapter("aod-ra");
        desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
        expectedAODDs.put(desc.getName(), desc);

        desc = new AdministeredObjectDefinitionDescriptor();
        desc.setDescription("component-scope resource defined in EJB DD");
        desc.setName("java:comp/env/StatefulEJB_AdminObject");
        desc.setInterfaceName("javax.jms.Destination");
        desc.setClassName("connector.MyAdminObject");
        desc.setResourceAdapter("aod-ra");
        desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
        expectedAODDs.put(desc.getName(), desc);

        TestUtil.compareAODD(expectedAODDs, ejb.getResourceDescriptors(JavaEEResourceType.AODD));
        
    }
    
    private void testStatelessSessionEJBDD(EjbDescriptor ejb) throws Exception{
        AdministeredObjectDefinitionDescriptor desc;
        Map<String,AdministeredObjectDefinitionDescriptor> expectedAODDs = new HashMap<String,AdministeredObjectDefinitionDescriptor>();

        desc = new AdministeredObjectDefinitionDescriptor();
        desc.setDescription("global-scope resource defined in EJB DD");
        desc.setName("java:global/env/HelloEJB_AdminObject");
        desc.setInterfaceName("javax.jms.Destination");
        desc.setClassName("connector.MyAdminObject");
        desc.setResourceAdapter("aod-ra");
        desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
        expectedAODDs.put(desc.getName(), desc);
        
        desc = new AdministeredObjectDefinitionDescriptor();
        desc.setDescription("module-scope resource defined in EJB DD");
        desc.setName("java:module/env/HelloEJB_AdminObject");
        desc.setInterfaceName("javax.jms.Destination");
        desc.setClassName("connector.MyAdminObject");
        desc.setResourceAdapter("aod-ra");
        desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
        expectedAODDs.put(desc.getName(), desc);

        desc = new AdministeredObjectDefinitionDescriptor();
        desc.setDescription("component-scope resource defined in EJB DD");
        desc.setName("java:comp/env/HelloEJB_AdminObject");
        desc.setInterfaceName("javax.jms.Destination");
        desc.setClassName("connector.MyAdminObject");
        desc.setResourceAdapter("aod-ra");
        desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
        expectedAODDs.put(desc.getName(), desc);

        TestUtil.compareAODD(expectedAODDs, ejb.getResourceDescriptors(JavaEEResourceType.AODD));
        
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
                desc.setInterfaceName("javax.jms.Destination");
                desc.setClassName("connector.MyAdminObject");
                desc.setResourceAdapter("aod-ra");
                desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
                expectedAODDs.put(desc.getName(), desc);
                
                desc = new AdministeredObjectDefinitionDescriptor();
                desc.setDescription("module-scope resource defined in EJB DD");
                desc.setName("java:module/env/Entity_AdminObject");
                desc.setInterfaceName("javax.jms.Destination");
                desc.setClassName("connector.MyAdminObject");
                desc.setResourceAdapter("aod-ra");
                desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
                expectedAODDs.put(desc.getName(), desc);

                desc = new AdministeredObjectDefinitionDescriptor();
                desc.setDescription("component-scope resource defined in EJB DD");
                desc.setName("java:comp/env/Entity_AdminObject");
                desc.setInterfaceName("javax.jms.Destination");
                desc.setClassName("connector.MyAdminObject");
                desc.setResourceAdapter("aod-ra");
                desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
                expectedAODDs.put(desc.getName(), desc);

                TestUtil.compareAODD(expectedAODDs, ejbDescriptor.getResourceDescriptors(JavaEEResourceType.AODD));
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
                desc.setInterfaceName("javax.jms.Destination");
                desc.setClassName("connector.MyAdminObject");
                desc.setResourceAdapter("aod-ra");
                desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
                expectedAODDs.put(desc.getName(), desc);
                
                desc = new AdministeredObjectDefinitionDescriptor();
                desc.setDescription("module-scope resource defined in EJB DD");
                desc.setName("java:module/env/MDB_AdminObject");
                desc.setInterfaceName("javax.jms.Destination");
                desc.setClassName("connector.MyAdminObject");
                desc.setResourceAdapter("aod-ra");
                desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
                expectedAODDs.put(desc.getName(), desc);

                desc = new AdministeredObjectDefinitionDescriptor();
                desc.setDescription("component-scope resource defined in EJB DD");
                desc.setName("java:comp/env/MDB_AdminObject");
                desc.setInterfaceName("javax.jms.Destination");
                desc.setClassName("connector.MyAdminObject");
                desc.setResourceAdapter("aod-ra");
                desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
                expectedAODDs.put(desc.getName(), desc);

                TestUtil.compareAODD(expectedAODDs, ejbDescriptor.getResourceDescriptors(JavaEEResourceType.AODD));
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
                desc.setInterfaceName("javax.jms.Destination");
                desc.setClassName("connector.MyAdminObject");
                desc.setResourceAdapter("aod-ra");
                desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
                expectedAODDs.put(desc.getName(), desc);
                
                desc = new AdministeredObjectDefinitionDescriptor();
                desc.setDescription("module-scope resource defined in EJB DD");
                desc.setName("java:module/env/Interceptor_AdminObject");
                desc.setInterfaceName("javax.jms.Destination");
                desc.setClassName("connector.MyAdminObject");
                desc.setResourceAdapter("aod-ra");
                desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
                expectedAODDs.put(desc.getName(), desc);

                desc = new AdministeredObjectDefinitionDescriptor();
                desc.setDescription("component-scope resource defined in EJB DD");
                desc.setName("java:comp/env/Interceptor_AdminObject");
                desc.setInterfaceName("javax.jms.Destination");
                desc.setClassName("connector.MyAdminObject");
                desc.setResourceAdapter("aod-ra");
                desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
                expectedAODDs.put(desc.getName(), desc);

                TestUtil.compareAODD(expectedAODDs, interceptor.getResourceDescriptors(JavaEEResourceType.AODD));
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
            desc.setInterfaceName(null);
            desc.setClassName("connector.MyAdminObject");
            desc.setResourceAdapter("aod-ra");
            desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
            expectedAODDs.put(desc.getName(), desc);
            
            desc = new AdministeredObjectDefinitionDescriptor();
            desc.setDescription("application-scope resource defined in Web DD");
            desc.setName("java:app/env/AdminObject");
            desc.setInterfaceName("javax.jms.Destination");
            desc.setClassName("connector.MyAdminObject");
            desc.setResourceAdapter("aod-ra");
            desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
            expectedAODDs.put(desc.getName(), desc);

            desc = new AdministeredObjectDefinitionDescriptor();
            desc.setDescription("module-scope resource defined in Web DD");
            desc.setName("java:module/env/AdminObject");
            desc.setInterfaceName("javax.jms.Destination");
            desc.setClassName("connector.MyAdminObject");
            desc.setResourceAdapter("aod-ra");
            desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
            expectedAODDs.put(desc.getName(), desc);

            TestUtil.compareAODD(expectedAODDs, webBundle.getResourceDescriptors(JavaEEResourceType.AODD));

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
