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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.inject.Named;
import javax.inject.Singleton;

import junit.framework.Assert;

import org.glassfish.hk2.api.DescriptorType;
import org.glassfish.hk2.api.DescriptorVisibility;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.utilities.DescriptorImpl;
import org.glassfish.hk2.utilities.general.GeneralUtilities;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hk2.config.GenerateServiceFromMethod;
import org.jvnet.hk2.generator.HabitatGenerator;
import org.jvnet.hk2.generator.InFlightGenerator;

/**
 * Tests for the inhabitant generator
 * 
 * @author jwells
 */
public class InhabitantsGeneratorTest {
    private final static String CLASS_PATH_PROP = "java.class.path";
    private final static String CLASSPATH = GeneralUtilities.getSystemProperty(CLASS_PATH_PROP, null);
    
    private final static String FILE_ARGUMENT = "--file";
    private final static String OUTJAR_FILE_ARGUMENT = "--outjar";
    private final static String VERBOSE_ARGUMENT = "--verbose";
    private final static String NOSWAP_ARGUMENT = "--noswap";
    private final static String LOCATOR_ARGUMENT = "--locator";
    private final static String CLASS_DIRECTORY = "gendir";
    private final static String NEGATIVE_CLASS_DIRECTORY = "negative";
    private final static String JAR_FILE = "gendir.jar";
    private final static File OUTJAR_FILE = new File("outgendir.jar");
    private final static String COPIED_INPUT_JAR_NAME = "gendirCopy.jar";
    
    private final static String META_INF_NAME = "META-INF";
    private final static String INHABITANTS = "hk2-locator";
    private final static String DEFAULT = "default";
    private final static String OTHER = "other";
    
    private final static String ZIP_FILE_INHABITANT_NAME = "META-INF/hk2-locator/default";
    private final static String NON_DEFAULT_INHABITANT_NAME = "META-INF/hk2-locator/non-default-name";
    
    private final static String MAVEN_CLASSES_DIR = "test-classes";
    
    public final static String GENERATE_METHOD_CREATE_IMPL = "com.acme.service.GenerateMethodImpl";
    public final static String GENERATE_METHOD_CREATE_CONTRACT = "com.acme.api.GenerateMethod";
    public final static String GENERATE_METHOD_CREATE_NAME1 = "name1";
    public final static String GENERATE_METHOD_CREATE_NAME2 = "name2";
    public final static String GENERATE_METHOD_CREATE_NAME3 = "name3";
    public final static String GENERATE_METHOD_CREATE_NAME4 = "name4";
    public final static String GENERATE_METHOD_CREATE_NAME5 = "name5";
    
    public final static String GENERATE_METHOD_DELETE_IMPL = "com.acme.service.DeleteImpl";
    public final static String GENERATE_METHOD_DELETE_CONTRACT = "com.acme.api.GenerateMethod";
    public final static String GENERATE_METHOD_DELETE_SCOPE = "javax.inject.Singleton";
    
    // metadata constants
    public final static String KEY1 = "key1";
    public final static String VALUE1 = "value1";
    public final static String KEY2 = "key2";
    public final static String VALUE2 = "value2";
    public final static String KEY3 = "key3";
    public final static String VALUE3 = "3";
    public final static String KEY4 = "key4";
    public final static String VALUE4 = InhabitantsGeneratorTest.class.getName();
    public final static String KEY5 = "key5";
    public final static String VALUE5_1 = "5_1";
    public final static String VALUE5_2 = "5_2";
    public final static String VALUE5_3 = "5_3";
    public final static String KEY6 = "key6";
    public final static long VALUE6_1 = 6001L;
    public final static long VALUE6_2 = 6002L;
    public final static long VALUE6_3 = 6003L;
    
    /** The name for non-defaulted things */
    public final static String NON_DEFAULT_NAME = "non-default-name";
    
    /** The rank to use when testing for rank */
    public final static int RANK = 13;
    
    /** The rank to use when testing for rank on factory method */
    public final static int FACTORY_METHOD_RANK = -1;
    
    /** A custom analyzer for a descriptor */
    public final static String CUSTOM_ANALYZER = "CustomAnalyzer";
    
    private final static Map<DescriptorImpl, Integer> EXPECTED_DESCRIPTORS = new HashMap<DescriptorImpl, Integer>();
    
    static {
        {
            // This is the Factory that should be generated
            DescriptorImpl envFactory = new DescriptorImpl();
            envFactory.setImplementation("org.glassfish.examples.ctm.EnvironmentFactory");
            envFactory.addAdvertisedContract("org.glassfish.examples.ctm.EnvironmentFactory");
            envFactory.addAdvertisedContract("org.glassfish.hk2.api.Factory");
            envFactory.setScope(Singleton.class.getName());
        
            EXPECTED_DESCRIPTORS.put(envFactory, 0);
        }
        
        {
            // This is the class that the Factory produces
            DescriptorImpl envItself = new DescriptorImpl();
            envItself.setImplementation("org.glassfish.examples.ctm.EnvironmentFactory");
            envItself.addAdvertisedContract("org.glassfish.examples.ctm.Environment");
            envItself.setScope("org.glassfish.examples.ctm.TenantScoped");
            envItself.setDescriptorType(DescriptorType.PROVIDE_METHOD);
        
            EXPECTED_DESCRIPTORS.put(envItself, 0);
        }
        
        {
            // This is the class that implements the Context
            DescriptorImpl di = new DescriptorImpl();
            di.setImplementation("org.glassfish.examples.ctm.TenantScopedContext");
            di.addAdvertisedContract("org.glassfish.examples.ctm.TenantScopedContext");
            di.addAdvertisedContract("org.glassfish.hk2.api.Context");
            di.setScope(Singleton.class.getName());
        
            EXPECTED_DESCRIPTORS.put(di, 0);
        }
        
        {
            // This is the service provider engine
            DescriptorImpl di = new DescriptorImpl();
            di.setImplementation("org.glassfish.examples.ctm.ServiceProviderEngine");
            di.addAdvertisedContract("org.glassfish.examples.ctm.ServiceProviderEngine");
            di.setScope(Singleton.class.getName());
        
            EXPECTED_DESCRIPTORS.put(di, 0);
        }
        
        {
            // This is the tenant manager
            DescriptorImpl di = new DescriptorImpl();
            di.setImplementation("org.glassfish.examples.ctm.TenantManager");
            di.addAdvertisedContract("org.glassfish.examples.ctm.TenantManager");
            di.setScope(Singleton.class.getName());
        
            EXPECTED_DESCRIPTORS.put(di, 0);
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
        
            EXPECTED_DESCRIPTORS.put(di, 0);
        }
        
        {
            // This is a descriptor with a non-defaulted Name from @Named
            DescriptorImpl di = new DescriptorImpl();
            di.setImplementation("org.jvnet.hk2.generator.tests.GivenNameFromQualifier");
            di.addAdvertisedContract("org.jvnet.hk2.generator.tests.GivenNameFromQualifier");
            di.setName(NON_DEFAULT_NAME);
            di.addQualifier(Named.class.getName());
            di.setScope(Singleton.class.getName());
        
            EXPECTED_DESCRIPTORS.put(di, 0);
        }
        
        {
            // This is a descriptor with a non-defaulted Name from @Service
            DescriptorImpl di = new DescriptorImpl();
            di.setImplementation("org.jvnet.hk2.generator.tests.ServiceWithName");
            di.addAdvertisedContract("org.jvnet.hk2.generator.tests.ServiceWithName");
            di.setName(NON_DEFAULT_NAME);
            di.setScope(Singleton.class.getName());
        
            EXPECTED_DESCRIPTORS.put(di, 0);
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
        
            EXPECTED_DESCRIPTORS.put(di, 0);
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
            di.setDescriptorType(DescriptorType.PROVIDE_METHOD);
            di.addQualifier(Blue.class.getName());
            di.addQualifier(Named.class.getName());
        
            EXPECTED_DESCRIPTORS.put(di, 0);
        }
        
        {
            // Another complex hierarchy
            DescriptorImpl di = new DescriptorImpl();
            di.setImplementation(TwoContractImpl.class.getName());
            di.addAdvertisedContract(TwoContractImpl.class.getName());
            di.addAdvertisedContract(ComplexG.class.getName());
            di.addAdvertisedContract(ComplexF.class.getName());
            di.addAdvertisedContract(ComplexA.class.getName());
            di.setScope(Singleton.class.getName());
        
            EXPECTED_DESCRIPTORS.put(di, 0);
        }
        
        {
            // This is a descriptor with a non-defaulted Name from @Service
            DescriptorImpl di = new DescriptorImpl();
            di.setImplementation("org.jvnet.hk2.generator.tests.ContractsProvidedService");
            di.addAdvertisedContract("org.jvnet.hk2.generator.tests.SimpleInterface");
            di.setScope(Singleton.class.getName());
        
            EXPECTED_DESCRIPTORS.put(di, 0);
        }
        
        {
            // This is a service with a rank
            DescriptorImpl di = new DescriptorImpl();
            di.setImplementation(ServiceWithRank.class.getName());
            di.addAdvertisedContract(ServiceWithRank.class.getName());
            di.setScope(Singleton.class.getName());
            di.setRanking(RANK);
        
            EXPECTED_DESCRIPTORS.put(di, RANK);
        }
        
        {
            // This is the Factory that should be generated
            DescriptorImpl envFactory = new DescriptorImpl();
            envFactory.setImplementation(FactoryWithRanks.class.getName());
            envFactory.addAdvertisedContract(FactoryWithRanks.class.getName());
            envFactory.addAdvertisedContract(Factory.class.getName());
            envFactory.setScope(Singleton.class.getName());
            envFactory.setRanking(RANK);
        
            EXPECTED_DESCRIPTORS.put(envFactory, RANK);
        }
        
        {
            // This is a factory with a rank
            DescriptorImpl envItself = new DescriptorImpl();
            envItself.setImplementation(FactoryWithRanks.class.getName());
            envItself.addAdvertisedContract(SimpleInterface.class.getName());
            envItself.setRanking(FACTORY_METHOD_RANK);
            envItself.setDescriptorType(DescriptorType.PROVIDE_METHOD);
        
            EXPECTED_DESCRIPTORS.put(envItself, FACTORY_METHOD_RANK);
        }
        
        {
            // This is a service with automatic metadata
            DescriptorImpl envItself = new DescriptorImpl();
            envItself.setImplementation(ServiceWithMetadata.class.getName());
            envItself.addAdvertisedContract(ServiceWithMetadata.class.getName());
            envItself.setScope(ScopeWithMetadata.class.getName());
            envItself.addQualifier(QualifierWithMetadata.class.getName());
            envItself.addMetadata(KEY1, VALUE1);
            envItself.addMetadata(KEY2, VALUE2);
            envItself.addMetadata(KEY3, VALUE3);
            envItself.addMetadata(KEY4, VALUE4);
            envItself.addMetadata(KEY5, VALUE5_1);
            envItself.addMetadata(KEY5, VALUE5_2);
            envItself.addMetadata(KEY5, VALUE5_3);
            envItself.addMetadata(KEY6, new Long(VALUE6_1).toString());
            envItself.addMetadata(KEY6, new Long(VALUE6_2).toString());
            envItself.addMetadata(KEY6, new Long(VALUE6_3).toString());
        
            EXPECTED_DESCRIPTORS.put(envItself, 0);
        }
        
        // All the following descriptors were generated from the GenerateServiceFromMethod
        // annotations
        {
            // From the @CreateMe on getStreetAddress on AddressBean
            DescriptorImpl envItself = new DescriptorImpl();
            envItself.setImplementation(GENERATE_METHOD_CREATE_IMPL);
            envItself.addAdvertisedContract(GENERATE_METHOD_CREATE_IMPL);
            envItself.addAdvertisedContract(GENERATE_METHOD_CREATE_CONTRACT);
            envItself.setScope(PerLookup.class.getName());
            envItself.setName(GENERATE_METHOD_CREATE_NAME1);
            envItself.addMetadata(GenerateServiceFromMethod.METHOD_ACTUAL, "org.jvnet.hk2.generator.tests.StreetAddress");
            envItself.addMetadata(GenerateServiceFromMethod.METHOD_NAME, "getStreetAddress");
            envItself.addMetadata(GenerateServiceFromMethod.PARENT_CONFIGURED, AddressBean.class.getName());
            
            EXPECTED_DESCRIPTORS.put(envItself, 0);
        }
        
        {
            // From the @CreateMe on getSecondaryStreetAddress on AddressBean
            DescriptorImpl envItself = new DescriptorImpl();
            envItself.setImplementation(GENERATE_METHOD_CREATE_IMPL);
            envItself.addAdvertisedContract(GENERATE_METHOD_CREATE_IMPL);
            envItself.addAdvertisedContract(GENERATE_METHOD_CREATE_CONTRACT);
            envItself.setScope(PerLookup.class.getName());
            envItself.setName(GENERATE_METHOD_CREATE_NAME2);
            envItself.addMetadata(GenerateServiceFromMethod.METHOD_ACTUAL, "org.jvnet.hk2.generator.tests.StreetAddress");
            envItself.addMetadata(GenerateServiceFromMethod.METHOD_NAME, "getSecondaryStreetAddress");
            envItself.addMetadata(GenerateServiceFromMethod.PARENT_CONFIGURED, AddressBean.class.getName());
            
            EXPECTED_DESCRIPTORS.put(envItself, 0);
        }
        
        {
            // From the @DeleteMe on getStreetAddress on AddressBean
            DescriptorImpl envItself = new DescriptorImpl();
            envItself.setImplementation(GENERATE_METHOD_DELETE_IMPL);
            envItself.addAdvertisedContract(GENERATE_METHOD_DELETE_CONTRACT);
            envItself.setScope(Singleton.class.getName());
            envItself.addMetadata(GenerateServiceFromMethod.METHOD_ACTUAL, "org.jvnet.hk2.generator.tests.StreetAddress");
            envItself.addMetadata(GenerateServiceFromMethod.METHOD_NAME, "getStreetAddress");
            envItself.addMetadata(GenerateServiceFromMethod.PARENT_CONFIGURED, AddressBean.class.getName());
            
            EXPECTED_DESCRIPTORS.put(envItself, 0);
        }
        
        {
            // From the @CreateMe on the DecoratedTown class (using @Decorate)
            DescriptorImpl envItself = new DescriptorImpl();
            envItself.setImplementation(GENERATE_METHOD_CREATE_IMPL);
            envItself.addAdvertisedContract(GENERATE_METHOD_CREATE_IMPL);
            envItself.addAdvertisedContract(GENERATE_METHOD_CREATE_CONTRACT);
            envItself.setScope(PerLookup.class.getName());
            envItself.setName(GENERATE_METHOD_CREATE_NAME3);
            envItself.addMetadata(GenerateServiceFromMethod.METHOD_ACTUAL, "org.jvnet.hk2.generator.tests.DecoratedTown");
            envItself.addMetadata(GenerateServiceFromMethod.METHOD_NAME, "getTown");
            envItself.addMetadata(GenerateServiceFromMethod.PARENT_CONFIGURED, AddressBean.class.getName());
            
            EXPECTED_DESCRIPTORS.put(envItself, 0);
        }
        
        {
            // From the @CreateMe on the getZipCode method on the DecoratedTown class
            DescriptorImpl envItself = new DescriptorImpl();
            envItself.setImplementation(GENERATE_METHOD_CREATE_IMPL);
            envItself.addAdvertisedContract(GENERATE_METHOD_CREATE_IMPL);
            envItself.addAdvertisedContract(GENERATE_METHOD_CREATE_CONTRACT);
            envItself.setScope(PerLookup.class.getName());
            envItself.setName(GENERATE_METHOD_CREATE_NAME4);
            envItself.addMetadata(GenerateServiceFromMethod.METHOD_ACTUAL, "org.jvnet.hk2.generator.tests.ZipCode");
            envItself.addMetadata(GenerateServiceFromMethod.METHOD_NAME, "getZipCodes");
            envItself.addMetadata(GenerateServiceFromMethod.PARENT_CONFIGURED, DecoratedTown.class.getName());
            
            EXPECTED_DESCRIPTORS.put(envItself, 0);
        }
        
        {
            // From the @CreateMe on the getZipCode method on the DecoratedTown class
            DescriptorImpl envItself = new DescriptorImpl();
            envItself.setImplementation(GENERATE_METHOD_CREATE_IMPL);
            envItself.addAdvertisedContract(GENERATE_METHOD_CREATE_IMPL);
            envItself.addAdvertisedContract(GENERATE_METHOD_CREATE_CONTRACT);
            envItself.setScope(PerLookup.class.getName());
            envItself.setName(GENERATE_METHOD_CREATE_NAME5);
            envItself.addMetadata(GenerateServiceFromMethod.METHOD_ACTUAL, "org.jvnet.hk2.generator.tests.StreetAddress");
            envItself.addMetadata(GenerateServiceFromMethod.METHOD_NAME, "setMyAddress");
            envItself.addMetadata(GenerateServiceFromMethod.PARENT_CONFIGURED, AddressBean.class.getName());
            
            EXPECTED_DESCRIPTORS.put(envItself, 0);
        }
        
        {
            // From a service with @UseProxy explicitly set to true
            DescriptorImpl envItself = new DescriptorImpl();
            envItself.setImplementation(ServiceWithTrueProxy.class.getName());
            envItself.addAdvertisedContract(ServiceWithTrueProxy.class.getName());
            envItself.setScope(Singleton.class.getName());
            envItself.setProxiable(Boolean.TRUE);
            
            EXPECTED_DESCRIPTORS.put(envItself, 0);
        }
        
        {
            // From a service with @UseProxy explicitly set to false
            DescriptorImpl envItself = new DescriptorImpl();
            envItself.setImplementation(ServiceWithFalseProxy.class.getName());
            envItself.addAdvertisedContract(ServiceWithFalseProxy.class.getName());
            envItself.setScope(PerLookup.class.getName());
            envItself.setProxiable(Boolean.FALSE);
            
            EXPECTED_DESCRIPTORS.put(envItself, 0);
        }
        
        {
            // From a service with @UseProxy using default value (should be true)
            DescriptorImpl envItself = new DescriptorImpl();
            envItself.setImplementation(ServiceWithDefaultProxy.class.getName());
            envItself.addAdvertisedContract(ServiceWithDefaultProxy.class.getName());
            envItself.setScope(Singleton.class.getName());
            envItself.setProxiable(Boolean.TRUE);
            
            EXPECTED_DESCRIPTORS.put(envItself, 0);
        }
        
        {
            // From a factory with default @UseProxy on the provide method.  Service descriptor
            DescriptorImpl envItself = new DescriptorImpl();
            envItself.setImplementation(FactoryWithDefaultProxy.class.getName());
            envItself.addAdvertisedContract(FactoryWithDefaultProxy.class.getName());
            envItself.addAdvertisedContract(Factory.class.getName());
            envItself.setScope(Singleton.class.getName());
            
            EXPECTED_DESCRIPTORS.put(envItself, 0);
        }
        
        {
            // From a factory with default @UseProxy on the provide method.  Method descriptor
            DescriptorImpl envItself = new DescriptorImpl();
            envItself.setImplementation(FactoryWithDefaultProxy.class.getName());
            envItself.addAdvertisedContract(Object.class.getName());
            envItself.setScope(Singleton.class.getName());
            envItself.setProxiable(Boolean.TRUE);
            envItself.setDescriptorType(DescriptorType.PROVIDE_METHOD);
            
            EXPECTED_DESCRIPTORS.put(envItself, 0);
        }
        
        {
            // From a factory with false @UseProxy on the provide method.  Service descriptor
            DescriptorImpl envItself = new DescriptorImpl();
            envItself.setImplementation(FactoryWithFalseProxy.class.getName());
            envItself.addAdvertisedContract(FactoryWithFalseProxy.class.getName());
            envItself.addAdvertisedContract(Factory.class.getName());
            envItself.setScope(Singleton.class.getName());
            
            EXPECTED_DESCRIPTORS.put(envItself, 0);
        }
        
        {
            // From a factory with false @UseProxy on the provide method.  Method descriptor
            DescriptorImpl envItself = new DescriptorImpl();
            envItself.setImplementation(FactoryWithFalseProxy.class.getName());
            envItself.addAdvertisedContract(Object.class.getName());
            envItself.setScope(Singleton.class.getName());
            envItself.setProxiable(Boolean.FALSE);
            envItself.setDescriptorType(DescriptorType.PROVIDE_METHOD);
            
            EXPECTED_DESCRIPTORS.put(envItself, 0);
        }
        
        {
            // From a service with LOCAL visibility
            DescriptorImpl envItself = new DescriptorImpl();
            envItself.setImplementation(LocalService.class.getName());
            envItself.addAdvertisedContract(LocalService.class.getName());
            envItself.setScope(Singleton.class.getName());
            envItself.setDescriptorVisibility(DescriptorVisibility.LOCAL);
            
            EXPECTED_DESCRIPTORS.put(envItself, 0);
        }
        
        {
            // From a service with NORMAL visiblity
            DescriptorImpl envItself = new DescriptorImpl();
            envItself.setImplementation(NormalService.class.getName());
            envItself.addAdvertisedContract(NormalService.class.getName());
            envItself.setScope(Singleton.class.getName());
            
            EXPECTED_DESCRIPTORS.put(envItself, 0);
        }
        
        {
            // From a factory with LOCAL visiblity (service descriptor)
            DescriptorImpl envItself = new DescriptorImpl();
            envItself.setImplementation(FactoryWithVisibility.class.getName());
            envItself.addAdvertisedContract(FactoryWithVisibility.class.getName());
            envItself.addAdvertisedContract(Factory.class.getName());
            envItself.setDescriptorVisibility(DescriptorVisibility.LOCAL);
            
            EXPECTED_DESCRIPTORS.put(envItself, 0);
        }
        
        {
            // From a factory with LOCAL visibility (method descriptor)
            DescriptorImpl envItself = new DescriptorImpl();
            envItself.setImplementation(FactoryWithVisibility.class.getName());
            envItself.addAdvertisedContract(String.class.getName());
            envItself.setDescriptorVisibility(DescriptorVisibility.LOCAL);
            envItself.setDescriptorType(DescriptorType.PROVIDE_METHOD);
            
            EXPECTED_DESCRIPTORS.put(envItself, 0);
        }
        
        {
            // From a factory with LOCAL visibility (method descriptor)
            DescriptorImpl envItself = new DescriptorImpl();
            envItself.setImplementation(CustomAnalysisService.class.getName());
            envItself.addAdvertisedContract(CustomAnalysisService.class.getName());
            envItself.setClassAnalysisName(CUSTOM_ANALYZER);
            envItself.setScope(Singleton.class.getName());
            
            EXPECTED_DESCRIPTORS.put(envItself, 0);
        }
    }
    
    private File gendirDirectory;
    private File negativeDirectory;
    private File gendirJar;
    private File gendirCopyJar;
    private File inhabitantsDirectory;
    
    private static void copyFile(File to, File from) throws IOException {
        FileInputStream fis = new FileInputStream(from);
        FileOutputStream fos = new FileOutputStream(to);
        
        byte buffer[] = new byte[2000];
        int read;
        while ((read = fis.read(buffer)) >= 0) {
            if (read > 0) {
                fos.write(buffer, 0, read);
            }
        }
        
        fos.close();
        fis.close();
    }
    
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
            negativeDirectory = new File(mavenClassesDir, NEGATIVE_CLASS_DIRECTORY);
            gendirJar = new File(mavenClassesDir, JAR_FILE);
            gendirCopyJar = new File(mavenClassesDir, COPIED_INPUT_JAR_NAME);
        }
        else {
            gendirDirectory = new File(CLASS_DIRECTORY);
            negativeDirectory = new File(NEGATIVE_CLASS_DIRECTORY);
            gendirJar = new File(JAR_FILE);
            gendirCopyJar = new File(COPIED_INPUT_JAR_NAME);
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
            Assert.assertTrue("Did not find " + di + " in the expected descriptors <<<" +
              EXPECTED_DESCRIPTORS + ">>>", EXPECTED_DESCRIPTORS.containsKey(di));
            
            // The rank is not part of the calculated equals or hash code (since it can change
            // over the course of the lifeycle of the object) and hence must be checked
            // separately from the containsKey above
            int expectedRank = EXPECTED_DESCRIPTORS.get(di);
            Assert.assertEquals(expectedRank, di.getRanking());
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
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    @Test
    public void testNonDefaultDirectoryGeneration() throws IOException {
        String argv[] = new String[6];
        
        argv[0] = FILE_ARGUMENT;
        argv[1] = gendirDirectory.getAbsolutePath();
        argv[2] = VERBOSE_ARGUMENT;
        argv[3] = NOSWAP_ARGUMENT;
        argv[4] = LOCATOR_ARGUMENT;
        argv[5] = OTHER;
        
        File defaultOutput = new File(inhabitantsDirectory, OTHER);
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
     * Tests generating into a jar file
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
        
        JarFile jar = null;
        try {
            int result = HabitatGenerator.embeddedMain(argv);
            Assert.assertEquals("Got error code: " + result, 0, result);
            
            Assert.assertTrue("did not generate JAR " + OUTJAR_FILE.getAbsolutePath(),
                    OUTJAR_FILE.exists());
            
            jar = new JarFile(OUTJAR_FILE);
            ZipEntry entry = jar.getEntry(ZIP_FILE_INHABITANT_NAME);
            Assert.assertNotNull(entry);
            
            InputStream is = jar.getInputStream(entry);
            
            Set<DescriptorImpl> generatedImpls = getAllDescriptorsFromInputStream(is);
            
            checkDescriptors(generatedImpls);
        }
        finally {
            if (jar != null) {
                jar.close();
            }
            
            // The test should be clean
            OUTJAR_FILE.delete();
        }
    }
    
    /**
     * Tests generating into a jar file
     * @throws IOException On failure
     * @throws InterruptedException 
     */
    @Test
    public void testNoSwapNonDefaultJarGeneration() throws IOException, InterruptedException {
        if (gendirCopyJar.exists()) {
            // Start with a clean plate
            Assert.assertTrue(gendirCopyJar.delete());
        }
        
        copyFile(gendirCopyJar, gendirJar);
        
        String argv[] = new String[5];
        
        argv[0] = FILE_ARGUMENT;
        argv[1] = gendirCopyJar.getAbsolutePath();
        
        argv[2] = NOSWAP_ARGUMENT;
        
        argv[3] = LOCATOR_ARGUMENT;
        argv[4] = NON_DEFAULT_NAME;
        
        Assert.assertTrue("Could not find file " + gendirJar.getAbsolutePath(),
                gendirJar.exists());
        
        int result = HabitatGenerator.embeddedMain(argv);
        Assert.assertEquals("Got error code: " + result, 0, result);
            
        Assert.assertTrue("did not generate JAR " + gendirCopyJar.getAbsolutePath(),
                gendirCopyJar.exists());
            
        URI jarURI = URI.create("jar:" + gendirCopyJar.toURI());
            
        InputStream is = null;
        FileSystem fileSystem = FileSystems.newFileSystem(jarURI, new HashMap<String, Object>());
        try {
            Path path = fileSystem.getPath("/" + META_INF_NAME, INHABITANTS, NON_DEFAULT_NAME);
                
            Assert.assertTrue(Files.isReadable(path));
                
            is = Files.newInputStream(path, StandardOpenOption.READ);
                
            Set<DescriptorImpl> generatedImpls = getAllDescriptorsFromInputStream(is);
                
            checkDescriptors(generatedImpls);
                
        }
        finally {
            if (is != null) is.close();
                
            fileSystem.close();
        }
        
    }
    
    /**
     * Tests that a service with two scopes will cause a failure
     * 
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    @Test // @org.junit.Ignore
    public void testServiceWithTwoScopes() throws IOException {
        String argv[] = new String[2];
        
        argv[0] = FILE_ARGUMENT;
        argv[1] = negativeDirectory.getAbsolutePath();
        
        File defaultOutput = new File(inhabitantsDirectory, DEFAULT);
        if (defaultOutput.exists()) {
            // Start with a clean plate
            Assert.assertTrue(defaultOutput.delete());
        }
        
        try {
            int result = HabitatGenerator.embeddedMain(argv);
            Assert.assertNotSame("Got error code: " + result, 0, result);
            
            Assert.assertFalse("generated output in negative case " + defaultOutput.getAbsolutePath(),
                    defaultOutput.exists());
        }
        finally {
            // The test should be clean
            defaultOutput.delete();
        }
    }
    
    private static List<File> convertClasspathToFiles() {
        StringTokenizer tokenizer = new StringTokenizer(CLASSPATH, File.pathSeparator);
        
        LinkedList<File> retVal = new LinkedList<File>();
        while (tokenizer.hasMoreTokens()) {
            String file = tokenizer.nextToken();
            
            retVal.add(new File(file));
        }
        
        return retVal;
    }
    
    /**
     * Tests the in-flight generator
     */
    @Test // @org.junit.Ignore
    public void testInFlightGenerator() throws IOException {
        ServiceLoader<InFlightGenerator> loader = ServiceLoader.load(InFlightGenerator.class);
        
        InFlightGenerator generator = null;
        for (InFlightGenerator candidate : loader) {
            generator = candidate;
            break;
        }
        
        Assert.assertNotNull(generator);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        generator.generateFromMultipleDirectories(Collections.singletonList(gendirDirectory),
                convertClasspathToFiles(),
                false,
                baos);
        
        baos.close();
        
        byte[] data = baos.toByteArray();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        Set<DescriptorImpl> generatedImpls = getAllDescriptorsFromInputStream(bais);
        
        bais.close();
        
        checkDescriptors(generatedImpls);
    }
}
