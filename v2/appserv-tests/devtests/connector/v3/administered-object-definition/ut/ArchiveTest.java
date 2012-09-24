/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.aod;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.glassfish.ejb.deployment.archivist.EjbArchivist;
import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.web.deployment.archivist.WebArchivist;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.AdministeredObjectDefinitionDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.archivist.ApplicationArchivist;
import com.sun.enterprise.loader.ASURLClassLoader;

import junit.framework.TestCase;
public class ArchiveTest extends TestCase {
    String archiveDir = null;
    private static SimpleReporterAdapter stat =  new SimpleReporterAdapter("appserv-tests");

    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.setupHK2();
        archiveDir = System.getProperty("ArchiveDir");
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testApplicationArchive() throws Exception{
        String tcName = "administered-object-definition-application-archive-test";

        try{
            doTestApplicationArchive();
            stat.addStatus(tcName, stat.PASS);
        }catch(Exception e){
            stat.addStatus(tcName, stat.FAIL);
            throw e;
        }
    }
    private void doTestApplicationArchive() throws Exception{
        String appArchiveName = "administered-object-definitionApp-UT";
        File archive = new File(archiveDir, appArchiveName);
        assertTrue("Do not fing the archive "+archive.getAbsolutePath(), archive.exists());

        ApplicationArchivist reader = (ApplicationArchivist) TestUtil.getByType(ApplicationArchivist.class);
        reader.setAnnotationProcessingRequested(true);
        ASURLClassLoader classLoader = new ASURLClassLoader(this.getClass().getClassLoader());
        classLoader.addURL(archive.toURL());
        reader.setClassLoader(classLoader);
        
        Application applicationDesc = reader.open(archive);
//        System.out.println("--------Administered object in application.xml----------");
//        for( AdministeredObjectDefinitionDescriptor aodd: applicationDesc.getAdministeredObjectDefinitionDescriptors()){
//            System.out.println(aodd.getDescription());
//            System.out.println(aodd.getName());
//            System.out.println("");
//        }
        
        Map<String,AdministeredObjectDefinitionDescriptor> expectedAODDs = 
                new HashMap<String,AdministeredObjectDefinitionDescriptor>();
        AdministeredObjectDefinitionDescriptor desc;

        desc = new AdministeredObjectDefinitionDescriptor();
        desc.setDescription("global-scope resource defined in application DD");
        desc.setName("java:global/env/AdminObject");
        desc.setClassName("connector.MyAdminObject");
        desc.setResourceAdapterName("aod-ra");
        desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
        expectedAODDs.put(desc.getName(), desc);

        desc = new AdministeredObjectDefinitionDescriptor();
        desc.setDescription("application-scope resource defined in application DD");
        desc.setName("java:app/env/AdminObject");
        desc.setClassName("connector.MyAdminObject");
        desc.setResourceAdapterName("aod-ra");
        desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
        expectedAODDs.put(desc.getName(), desc);

        TestUtil.compareAODD(expectedAODDs, applicationDesc.getAdministeredObjectDefinitionDescriptors());

    }

    public void testWebArchive() throws Exception{
        String tcName = "administered-object-definition-web-archive-test";

        try{
            doTestWebArchive();
            stat.addStatus(tcName, stat.PASS);
        }catch(Exception e){
            stat.addStatus(tcName, stat.FAIL);
            throw e;
        }
    }

    private void doTestWebArchive() throws Exception{
        String appArchiveName = "administered-object-definition-web";
        File archive = new File(archiveDir, appArchiveName);
        assertTrue("Do not fing the archive "+archive.getAbsolutePath(), archive.exists());

        ASURLClassLoader classLoader = new ASURLClassLoader(this.getClass().getClassLoader());
        classLoader.addURL(archive.toURL());

        WebArchivist reader = (WebArchivist) TestUtil.getByType(WebArchivist.class);
        reader.setAnnotationProcessingRequested(true);
        reader.setClassLoader(classLoader);
        assertTrue("Archivist should handle annotations.", reader.isAnnotationProcessingRequested());
        
        WebBundleDescriptor webDesc = reader.open(archive);
//        for(AdministeredObjectDefinitionDescriptor aodd : webDesc.getAdministeredObjectDefinitionDescriptors()){
//            System.out.println("Description = "+aodd.getDescription());
//            System.out.println("Name = "+aodd.getName());
//            System.out.println("ClassName = "+aodd.getClassName());
//            System.out.println();
//        }

        Map<String,AdministeredObjectDefinitionDescriptor> expectedAODDs = 
                new HashMap<String,AdministeredObjectDefinitionDescriptor>();
        AdministeredObjectDefinitionDescriptor desc;

        desc = new AdministeredObjectDefinitionDescriptor();
        desc.setDescription("global-scope resource to be modified by DD");
        desc.setName("java:global/env/Servlet_ModByDD_AdminObject");
        desc.setClassName("connector.MyAdminObject");
        desc.setResourceAdapterName("aod-ra");
        desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
        expectedAODDs.put(desc.getName(), desc);

        desc = new AdministeredObjectDefinitionDescriptor();
        desc.setDescription("global-scope resource defined by @AdministeredObjectDefinition");
        desc.setName("java:global/env/Servlet_AdminObject");
        desc.setClassName("connector.MyAdminObject");
        desc.setResourceAdapterName("aod-ra");
        desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
        expectedAODDs.put(desc.getName(), desc);
        
        desc = new AdministeredObjectDefinitionDescriptor();
        desc.setDescription("application-scope resource defined by @AdministeredObjectDefinition");
        desc.setName("java:app/env/Servlet_AdminObject");
        desc.setClassName("connector.MyAdminObject");
        desc.setResourceAdapterName("aod-ra");
        desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
        expectedAODDs.put(desc.getName(), desc);
        
        desc = new AdministeredObjectDefinitionDescriptor();
        desc.setDescription("module-scope resource defined by @AdministeredObjectDefinition");
        desc.setName("java:module/env/Servlet_AdminObject");
        desc.setClassName("connector.MyAdminObject");
        desc.setResourceAdapterName("aod-ra");
        desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
        expectedAODDs.put(desc.getName(), desc);
        
        desc = new AdministeredObjectDefinitionDescriptor();
        desc.setDescription("component-scope resource defined by @AdministeredObjectDefinition");
        desc.setName("java:comp/env/Servlet_AdminObject");
        desc.setClassName("connector.MyAdminObject");
        desc.setResourceAdapterName("aod-ra");
        desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
        expectedAODDs.put(desc.getName(), desc);
        
        desc = new AdministeredObjectDefinitionDescriptor();
        desc.setDescription("global-scope resource defined in Web DD");
        desc.setName("java:global/env/Web_DD_AdminObject");
        desc.setClassName("connector.MyAdminObject");
        desc.setResourceAdapterName("aod-ra");
        desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
        expectedAODDs.put(desc.getName(), desc);
        
        desc = new AdministeredObjectDefinitionDescriptor();
        desc.setDescription("application-scope resource defined in Web DD");
        desc.setName("java:app/env/Web_DD_AdminObject");
        desc.setClassName("connector.MyAdminObject");
        desc.setResourceAdapterName("aod-ra");
        desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
        expectedAODDs.put(desc.getName(), desc);
        
        desc = new AdministeredObjectDefinitionDescriptor();
        desc.setDescription("module-scope resource defined in Web DD");
        desc.setName("java:module/env/Web_DD_AdminObject");
        desc.setClassName("connector.MyAdminObject");
        desc.setResourceAdapterName("aod-ra");
        desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
        expectedAODDs.put(desc.getName(), desc);

        TestUtil.compareAODD(expectedAODDs, webDesc.getAdministeredObjectDefinitionDescriptors());
    }

    public void testEJBArchive() throws Exception{
        String tcName = "administered-object-definition-EJB-archive-test";

        try{
            doTestEJBArchive();
            stat.addStatus(tcName, stat.PASS);
        }catch(Exception e){
            stat.addStatus(tcName, stat.FAIL);
            throw e;
        }
    }
    private void doTestEJBArchive() throws Exception{
        String appArchiveName = "administered-object-definition-ejb";
        File archive = new File(archiveDir, appArchiveName);
        assertTrue("Do not fing the archive "+archive.getAbsolutePath(), archive.exists());

        ASURLClassLoader classLoader = new ASURLClassLoader(this.getClass().getClassLoader());
        classLoader.addURL(archive.toURL());
               
        EjbArchivist reader = (EjbArchivist) TestUtil.getByType(EjbArchivist.class);
        reader.setClassLoader(classLoader);
        reader.setAnnotationProcessingRequested(true);
        assertTrue("Archivist should handle annotations.", reader.isAnnotationProcessingRequested());

        EjbBundleDescriptorImpl ejbBundleDesc = reader.open(archive);
        Set<AdministeredObjectDefinitionDescriptor> acturalAODDs = new HashSet<AdministeredObjectDefinitionDescriptor>(); 
        for( EjbDescriptor ejbDesc: ejbBundleDesc.getEjbs()){
            acturalAODDs.addAll(ejbDesc.getAdministeredObjectDefinitionDescriptors());
//            for( AdministeredObjectDefinitionDescriptor aodd: ejbDesc.getAdministeredObjectDefinitionDescriptors()){
//                System.out.println(aodd.getDescription());
//                System.out.println(aodd.getName());
//                System.out.println("------------------");
//            }
        }
        
        Map<String,AdministeredObjectDefinitionDescriptor> expectedAODDs = 
                new HashMap<String,AdministeredObjectDefinitionDescriptor>();
        AdministeredObjectDefinitionDescriptor desc;

        desc = new AdministeredObjectDefinitionDescriptor();
        desc.setDescription("global-scope resource to be modified by DD");
        desc.setName("java:global/env/HelloStatefulEJB_ModByDD_AdminObject");
        desc.setClassName("connector.MyAdminObject");
        desc.setResourceAdapterName("aod-ra");
        desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
        expectedAODDs.put(desc.getName(), desc);

        desc.setDescription("global-scope resource to be modified by DD");
        desc.setName("java:global/env/HelloEJB_ModByDD_AdminObject");
        desc.setClassName("connector.MyAdminObject");
        desc.setResourceAdapterName("aod-ra");
        desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
        expectedAODDs.put(desc.getName(), desc);

        // administered-object in DD for stateful EJB
        {
            desc = new AdministeredObjectDefinitionDescriptor();
            desc.setDescription("global-scope resource defined in EJB DD");
            desc.setName("java:global/env/HelloStatefulEJB_DD_AdminObject");
            desc.setClassName("connector.MyAdminObject");
            desc.setResourceAdapterName("aod-ra");
            desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
            expectedAODDs.put(desc.getName(), desc);

            desc = new AdministeredObjectDefinitionDescriptor();
            desc.setDescription("application-scope resource defined in EJB DD");
            desc.setName("java:app/env/HelloStatefulEJB_DD_AdminObject");
            desc.setClassName("connector.MyAdminObject");
            desc.setResourceAdapterName("aod-ra");
            desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
            expectedAODDs.put(desc.getName(), desc);

            desc = new AdministeredObjectDefinitionDescriptor();
            desc.setDescription("module-scope resource defined in EJB DD");
            desc.setName("java:module/env/HelloStatefulEJB_DD_AdminObject");
            desc.setClassName("connector.MyAdminObject");
            desc.setResourceAdapterName("aod-ra");
            desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
            expectedAODDs.put(desc.getName(), desc);

            desc = new AdministeredObjectDefinitionDescriptor();
            desc.setDescription("component-scope resource defined in EJB DD");
            desc.setName("java:comp/env/HelloStatefulEJB_DD_AdminObject");
            desc.setClassName("connector.MyAdminObject");
            desc.setResourceAdapterName("aod-ra");
            desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
            expectedAODDs.put(desc.getName(), desc);
        }
        // administered-object in DD for stateless EJB
        {
            desc = new AdministeredObjectDefinitionDescriptor();
            desc.setDescription("global-scope resource defined in EJB DD");
            desc.setName("java:global/env/HelloEJB_DD_AdminObject");
            desc.setClassName("connector.MyAdminObject");
            desc.setResourceAdapterName("aod-ra");
            desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
            expectedAODDs.put(desc.getName(), desc);

            desc = new AdministeredObjectDefinitionDescriptor();
            desc.setDescription("application-scope resource defined in EJB DD");
            desc.setName("java:app/env/HelloEJB_DD_AdminObject");
            desc.setClassName("connector.MyAdminObject");
            desc.setResourceAdapterName("aod-ra");
            desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
            expectedAODDs.put(desc.getName(), desc);

            desc = new AdministeredObjectDefinitionDescriptor();
            desc.setDescription("module-scope resource defined in EJB DD");
            desc.setName("java:module/env/HelloEJB_DD_AdminObject");
            desc.setClassName("connector.MyAdminObject");
            desc.setResourceAdapterName("aod-ra");
            desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
            expectedAODDs.put(desc.getName(), desc);

            desc = new AdministeredObjectDefinitionDescriptor();
            desc.setDescription("component-scope resource defined in EJB DD");
            desc.setName("java:comp/env/HelloEJB_DD_AdminObject");
            desc.setClassName("connector.MyAdminObject");
            desc.setResourceAdapterName("aod-ra");
            desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
            expectedAODDs.put(desc.getName(), desc);
        }
        
        // administered-object in annotation for stateful EJB
        {
            desc = new AdministeredObjectDefinitionDescriptor();
            desc.setDescription("global-scope resource defined by @AdministeredObjectDefinition");
            desc.setName("java:global/env/HelloStatefulEJB_Annotation_AdminObject");
            desc.setClassName("connector.MyAdminObject");
            desc.setResourceAdapterName("aod-ra");
            desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
            expectedAODDs.put(desc.getName(), desc);
            
            desc = new AdministeredObjectDefinitionDescriptor();
            desc.setDescription("application-scope resource defined by @AdministeredObjectDefinition");
            desc.setName("java:app/env/HelloStatefulEJB_Annotation_AdminObject");
            desc.setClassName("connector.MyAdminObject");
            desc.setResourceAdapterName("aod-ra");
            desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
            expectedAODDs.put(desc.getName(), desc);

            desc = new AdministeredObjectDefinitionDescriptor();
            desc.setDescription("module-scope resource defined by @AdministeredObjectDefinition");
            desc.setName("java:module/env/HelloStatefulEJB_Annotation_AdminObject");
            desc.setClassName("connector.MyAdminObject");
            desc.setResourceAdapterName("aod-ra");
            desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
            expectedAODDs.put(desc.getName(), desc);

            desc = new AdministeredObjectDefinitionDescriptor();
            desc.setDescription("component-scope resource defined by @AdministeredObjectDefinition");
            desc.setName("java:comp/env/HelloStatefulEJB_Annotation_AdminObject");
            desc.setClassName("connector.MyAdminObject");
            desc.setResourceAdapterName("aod-ra");
            desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
            expectedAODDs.put(desc.getName(), desc);
        }

        // administered-object in annotation for stateless EJB
        {
            desc = new AdministeredObjectDefinitionDescriptor();
            desc.setDescription("global-scope resource defined by @AdministeredObjectDefinition");
            desc.setName("java:global/env/HelloEJB_Annotation_AdminObject");
            desc.setClassName("connector.MyAdminObject");
            desc.setResourceAdapterName("aod-ra");
            desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
            expectedAODDs.put(desc.getName(), desc);
            
            desc = new AdministeredObjectDefinitionDescriptor();
            desc.setDescription("application-scope resource defined by @AdministeredObjectDefinition");
            desc.setName("java:app/env/HelloEJB_Annotation_AdminObject");
            desc.setClassName("connector.MyAdminObject");
            desc.setResourceAdapterName("aod-ra");
            desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
            expectedAODDs.put(desc.getName(), desc);
            
            desc = new AdministeredObjectDefinitionDescriptor();
            desc.setDescription("module-scope resource defined by @AdministeredObjectDefinition");
            desc.setName("java:module/env/HelloEJB_Annotation_AdminObject");
            desc.setClassName("connector.MyAdminObject");
            desc.setResourceAdapterName("aod-ra");
            desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
            expectedAODDs.put(desc.getName(), desc);
            
            desc = new AdministeredObjectDefinitionDescriptor();
            desc.setDescription("component-scope resource defined by @AdministeredObjectDefinition");
            desc.setName("java:comp/env/HelloEJB_Annotation_AdminObject");
            desc.setClassName("connector.MyAdminObject");
            desc.setResourceAdapterName("aod-ra");
            desc.addProperty("org.glassfish.admin-object.resType", "connector.MyAdminObject");
            expectedAODDs.put(desc.getName(), desc);
        }
        
        TestUtil.compareAODD(expectedAODDs, acturalAODDs);
    }
    
}
