/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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
package org.jvnet.hk2.generator.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.inject.Named;
import javax.inject.Singleton;

import junit.framework.Assert;

import org.glassfish.hk2.api.DescriptorType;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.utilities.DescriptorImpl;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hk2.generator.HabitatGenerator;

/**
 * Tests for the inhabitant generator
 * 
 * @author jwells
 */
public class InhabitantsGeneratorTest {
    private final static String FILE_ARGUMENT = "--file";
    private final static String OUTJAR_FILE_ARGUMENT = "--outjar";
    private final static String CLASS_DIRECTORY = "gendir";
    private final static String JAR_FILE = "gendir.jar";
    private final static File OUTJAR_FILE = new File("outgendir.jar");
    
    private final static String META_INF_NAME = "META-INF";
    private final static String INHABITANTS = "hk2-locator";
    private final static String DEFAULT = "default";
    
    private final static String ZIP_FILE_INHABITANT_NAME = "META-INF/hk2-locator/default";
    
    private final static String MAVEN_CLASSES_DIR = "test-classes";
    
    // metadata constants
    private final static String KEY1 = "key1";
    private final static String VALUE1 = "value1";
    private final static String KEY2 = "key2";
    private final static String VALUE2 = "value2";
    
    /** The name for non-defaulted things */
    public final static String NON_DEFAULT_NAME = "non-default-name";
    
    private final static Set<DescriptorImpl> EXPECTED_DESCRIPTORS = new HashSet<DescriptorImpl>();
    
    static {
        {
            // This is the Factory that should be generated
            DescriptorImpl envFactory = new DescriptorImpl();
            envFactory.setImplementation("org.glassfish.examples.ctm.EnvironmentFactory");
            envFactory.addAdvertisedContract("org.glassfish.examples.ctm.EnvironmentFactory");
            envFactory.addAdvertisedContract("org.glassfish.hk2.api.Factory");
            envFactory.setScope(Singleton.class.getName());
        
            EXPECTED_DESCRIPTORS.add(envFactory);
        }
        
        {
            // This is the class that the Factory produces
            DescriptorImpl envItself = new DescriptorImpl();
            envItself.setImplementation("org.glassfish.examples.ctm.EnvironmentFactory");
            envItself.addAdvertisedContract("org.glassfish.examples.ctm.Environment");
            envItself.setScope("org.glassfish.examples.ctm.TenantScoped");
            envItself.setDescriptorType(DescriptorType.FACTORY);
        
            EXPECTED_DESCRIPTORS.add(envItself);
        }
        
        {
            // This is the class that implements the Context
            DescriptorImpl di = new DescriptorImpl();
            di.setImplementation("org.glassfish.examples.ctm.TenantScopedContext");
            di.addAdvertisedContract("org.glassfish.examples.ctm.TenantScopedContext");
            di.addAdvertisedContract("org.glassfish.hk2.api.Context");
            di.setScope(Singleton.class.getName());
        
            EXPECTED_DESCRIPTORS.add(di);
        }
        
        {
            // This is the service provider engine
            DescriptorImpl di = new DescriptorImpl();
            di.setImplementation("org.glassfish.examples.ctm.ServiceProviderEngine");
            di.addAdvertisedContract("org.glassfish.examples.ctm.ServiceProviderEngine");
            di.setScope(Singleton.class.getName());
        
            EXPECTED_DESCRIPTORS.add(di);
        }
        
        {
            // This is the tenant manager
            DescriptorImpl di = new DescriptorImpl();
            di.setImplementation("org.glassfish.examples.ctm.TenantManager");
            di.addAdvertisedContract("org.glassfish.examples.ctm.TenantManager");
            di.setScope(Singleton.class.getName());
        
            EXPECTED_DESCRIPTORS.add(di);
        }
        
        {
            // This is a descriptor with a defaulted Name and a qualifier and metadata
            DescriptorImpl di = new DescriptorImpl();
            di.setImplementation("org.jvnet.hk2.generator.tests.ServiceWithDefaultName");
            di.addAdvertisedContract("org.jvnet.hk2.generator.tests.ServiceWithDefaultName");
            di.setName("ServiceWithDefaultName");
            di.addQualifier(Named.class.getName());
            di.addQualifier(Blue.class.getName());
            di.addMetadata(KEY1, VALUE1);
            di.addMetadata(KEY2, VALUE2);
        
            EXPECTED_DESCRIPTORS.add(di);
        }
        
        {
            // This is a descriptor with a non-defaulted Name from @Named
            DescriptorImpl di = new DescriptorImpl();
            di.setImplementation("org.jvnet.hk2.generator.tests.GivenNameFromQualifier");
            di.addAdvertisedContract("org.jvnet.hk2.generator.tests.GivenNameFromQualifier");
            di.setName(NON_DEFAULT_NAME);
            di.addQualifier(Named.class.getName());
            di.setScope(Singleton.class.getName());
        
            EXPECTED_DESCRIPTORS.add(di);
        }
        
        {
            // This is a descriptor with a non-defaulted Name from @Service
            DescriptorImpl di = new DescriptorImpl();
            di.setImplementation("org.jvnet.hk2.generator.tests.ServiceWithName");
            di.addAdvertisedContract("org.jvnet.hk2.generator.tests.ServiceWithName");
            di.setName(NON_DEFAULT_NAME);
            di.setScope(Singleton.class.getName());
        
            EXPECTED_DESCRIPTORS.add(di);
        }
        
        {
            // ComplexFactory as a service
            DescriptorImpl di = new DescriptorImpl();
            di.setImplementation("org.jvnet.hk2.generator.tests.ComplexFactory");
            di.addAdvertisedContract("org.jvnet.hk2.generator.tests.ComplexFactory");
            di.addAdvertisedContract(Factory.class.getName());
            di.setName("ComplexFactory");
            di.setScope(Singleton.class.getName());
            di.addQualifier(Named.class.getName());
        
            EXPECTED_DESCRIPTORS.add(di);
        }
        
        {
            // ComplexFactory as a factory
            DescriptorImpl di = new DescriptorImpl();
            di.setImplementation("org.jvnet.hk2.generator.tests.ComplexFactory");
            di.addAdvertisedContract("org.jvnet.hk2.generator.tests.ComplexImpl");
            di.addAdvertisedContract("org.jvnet.hk2.generator.tests.ComplexDImpl");
            di.addAdvertisedContract("org.jvnet.hk2.generator.tests.ComplexA");
            di.addAdvertisedContract("org.jvnet.hk2.generator.tests.ComplexC");
            di.addAdvertisedContract("org.jvnet.hk2.generator.tests.ComplexE");
            di.setName(NON_DEFAULT_NAME);
            di.setScope(PerLookup.class.getName());
            di.setDescriptorType(DescriptorType.FACTORY);
            di.addQualifier(Blue.class.getName());
            di.addQualifier(Named.class.getName());
        
            EXPECTED_DESCRIPTORS.add(di);
        }
        
        {
            // This is a descriptor with a non-defaulted Name from @Service
            DescriptorImpl di = new DescriptorImpl();
            di.setImplementation("org.jvnet.hk2.generator.tests.ContractsProvidedService");
            di.addAdvertisedContract("org.jvnet.hk2.generator.tests.SimpleInterface");
            di.setScope(Singleton.class.getName());
        
            EXPECTED_DESCRIPTORS.add(di);
        }
    }
    
    private File gendirDirectory;
    private File gendirJar;
    private File inhabitantsDirectory;
    
    /**
     * Setup before every test
     */
    @Before
    public void before() {
        String buildDir = System.getProperty("build.dir");
        
        if (buildDir != null) {
            File buildDirFile = new File(buildDir);
            
            File mavenClassesDir = new File(buildDirFile, MAVEN_CLASSES_DIR);
            gendirDirectory = new File(mavenClassesDir, CLASS_DIRECTORY);
            gendirJar = new File(mavenClassesDir, JAR_FILE);
        }
        else {
            gendirDirectory = new File(CLASS_DIRECTORY);
            gendirJar = new File(JAR_FILE);
        }
        
        File metaInfFile = new File(gendirDirectory, META_INF_NAME);
        inhabitantsDirectory = new File(metaInfFile, INHABITANTS);
    }
    
    private Set<DescriptorImpl> getAllDescriptorsFromInputStream(InputStream is) throws IOException {
        BufferedReader pr = new BufferedReader(new InputStreamReader(is));
        
        Set<DescriptorImpl> retVal = new HashSet<DescriptorImpl>();
        while (pr.ready()) {
            DescriptorImpl di = new DescriptorImpl();
            
            if (!di.readObject(pr)) {
                continue;
            }
            
            retVal.add(di);
        }
        
        return retVal;
    }
    
    private void checkDescriptors(Set<DescriptorImpl> dis) {
        for (DescriptorImpl di : dis) {
            Assert.assertTrue("Did not find " + di + " in the expected descriptors", EXPECTED_DESCRIPTORS.contains(di));
        }
        
        
        Assert.assertEquals("Expecting " + EXPECTED_DESCRIPTORS.size() + " descriptors, but only got " + dis.size(),
                EXPECTED_DESCRIPTORS.size(), dis.size());
    }
    
    /**
     * Tests generating into a directory
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    @Test
    public void testDefaultDirectoryGeneration() throws IOException {
        String argv[] = new String[2];
        
        argv[0] = FILE_ARGUMENT;
        argv[1] = gendirDirectory.getAbsolutePath();
        
        File defaultOutput = new File(inhabitantsDirectory, DEFAULT);
        if (defaultOutput.exists()) {
            // Start with a clean plate
            Assert.assertTrue(defaultOutput.delete());
        }
        
        try {
            int result = HabitatGenerator.embeddedMain(argv);
            Assert.assertEquals("Got error code: " + result, 0, result);
            
            Assert.assertTrue("did not generate " + defaultOutput.getAbsolutePath(),
                    defaultOutput.exists());
            
            Set<DescriptorImpl> generatedImpls = getAllDescriptorsFromInputStream(
                    new FileInputStream(defaultOutput));
            
            checkDescriptors(generatedImpls);
        }
        finally {
            // The test should be clean
            defaultOutput.delete();
        }
    }
    
    /**
     * Tests generating into a directory
     * @throws IOException On failure
     */
    @Test
    public void testDefaultJarGeneration() throws IOException {
        String argv[] = new String[4];
        
        argv[0] = FILE_ARGUMENT;
        argv[1] = gendirJar.getAbsolutePath();
        
        argv[2] = OUTJAR_FILE_ARGUMENT;
        argv[3] = OUTJAR_FILE.getAbsolutePath();
        
        Assert.assertTrue("Could not find file " + gendirJar.getAbsolutePath(),
                gendirJar.exists());
        
        if (OUTJAR_FILE.exists()) {
            // Start with a clean plate
            Assert.assertTrue(OUTJAR_FILE.delete());
        }
        
        try {
            int result = HabitatGenerator.embeddedMain(argv);
            Assert.assertEquals("Got error code: " + result, 0, result);
            
            Assert.assertTrue("did not generate JAR " + OUTJAR_FILE.getAbsolutePath(),
                    OUTJAR_FILE.exists());
            
            JarFile jar = new JarFile(OUTJAR_FILE);
            ZipEntry entry = jar.getEntry(ZIP_FILE_INHABITANT_NAME);
            Assert.assertNotNull(entry);
            
            InputStream is = jar.getInputStream(entry);
            
            Set<DescriptorImpl> generatedImpls = getAllDescriptorsFromInputStream(is);
            
            checkDescriptors(generatedImpls);
        }
        finally {
            // The test should be clean
            OUTJAR_FILE.delete();
        }
    }
}
