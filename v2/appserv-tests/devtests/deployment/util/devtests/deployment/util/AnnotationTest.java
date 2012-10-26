/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package devtests.deployment.util;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.deploy.shared.ModuleType;

import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.BundleDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.io.AppClientDeploymentDescriptorFile;
import com.sun.enterprise.deployment.io.DeploymentDescriptorFile;
import org.glassfish.ejb.deployment.io.EjbDeploymentDescriptorFile;
import org.glassfish.web.deployment.descriptor.WebBundleDescriptorImpl;
import org.glassfish.web.deployment.io.WebDeploymentDescriptorFile;
import org.glassfish.webservices.io.WebServicesDeploymentDescriptorFile;
import org.glassfish.hk2.api.ServiceLocator;

import junit.extensions.TestSetup;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.glassfish.internal.api.Globals;
import org.glassfish.tests.utils.Utils;

/**
 * This is a annotation testing code using JUnit.
 * It has one time setup with a default test comparing descriptors.
 * One can subclass this by adding new method with name starting test
 * and a new main method.
 *
 * @author Shing Wai Chan
 */
public class AnnotationTest extends TestCase {
    private static final String META_INF_DIR = "META-INF/";
    private static final String WEB_INF_DIR = "WEB-INF/";
    private static final String GOLDEN_DIR = "goldenfiles/";

    // the following need to be static so that it can initialize once
    // and use in the given JVM
    private static boolean initialized = false;
    private static StandaloneProcessor processor = null;
    private static String workDir = System.getProperty("workDir", ".");
    private boolean hasGoldenFile = false;

    protected ModuleType type = ModuleType.EJB;
    protected Set<String> componentClassNames = new HashSet<String>();

    static {
        final ServiceLocator serviceLocator = Utils.getServiceLocator();
        Globals.setDefaultHabitat(serviceLocator);
    }
    
    public AnnotationTest(String name) {
        super(name);
    }

    /**
     * Per test setup.
     */
    public void setUp() {
        if (!initialized) {
            initialized = true;
            processor = new StandaloneProcessor(type);
            processor.setComponentClassNames(componentClassNames);
            String testClasspath = System.getProperty("testClasspath");
            System.out.println("testClasspath = " + testClasspath);
            try {
                processor.run(new String[] { testClasspath });
            } catch(Exception ex) {
                ex.printStackTrace();
                throw new IllegalStateException(ex);
            }
        }
    }

    /**
     * Per test cleanup.
     */
    public void tearDown() {
    }

    /**
     * This test case compare descriptor files generated from annotation
     * with the golden files if there is any.  It will return silently
     * if there is no golden files.
     */
    public void testDescriptors() {
        System.out.println("running testDescriptors");

        BundleDescriptor[] bundleDescs = null;
        if (ModuleType.EJB.equals(type)) {
            bundleDescs = processEjbDescriptors();
        } else if (ModuleType.WAR.equals(type)) {
            bundleDescs = processWebDescriptors();
        } else if (ModuleType.CAR.equals(type)) {
            bundleDescs = processAppClientDescriptors();
        } else {
            Assert.fail("ModuleType: " + type + " is not supported.");
        }
        if (!hasGoldenFile) {
            Assert.fail("Goldenfile is not setup for this test.");
        }

        DescriptorContentComparator dc = new DescriptorContentComparator();

        Assert.assertTrue("BundleDescriptors do not match.",
            dc.compareContent(bundleDescs[0], bundleDescs[1]));
    }
 
    protected BundleDescriptor[] processEjbDescriptors() {
        try {
            processor.generateEjbJarXmlFile(workDir);
            processor.generateWebServicesXmlFile(workDir);
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new IllegalStateException(ex);
        }

        BundleDescriptor[] bundleDescs = { new EjbBundleDescriptorImpl(),
                new EjbBundleDescriptorImpl() };
        bundleDescs = loadDescriptorFiles(bundleDescs[0], bundleDescs[1],
                new EjbDeploymentDescriptorFile(),
                new EjbDeploymentDescriptorFile(),
                META_INF_DIR, "ejb-jar.xml");

        bundleDescs = loadDescriptorFiles(bundleDescs[0], bundleDescs[1],
                new WebServicesDeploymentDescriptorFile(bundleDescs[0]),
                new WebServicesDeploymentDescriptorFile(bundleDescs[1]),
                META_INF_DIR, "webservices.xml");

        return bundleDescs;
    }

    protected BundleDescriptor[] processAppClientDescriptors() {
        try {
            processor.generateAppClientXmlFile(workDir);
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new IllegalStateException(ex);
        }

        BundleDescriptor[] bundleDescs = { new ApplicationClientDescriptor(),
                new ApplicationClientDescriptor() };
        bundleDescs = loadDescriptorFiles(bundleDescs[0], bundleDescs[1],
                new AppClientDeploymentDescriptorFile(),
                new AppClientDeploymentDescriptorFile(),
                META_INF_DIR, "application-client.xml");

        return bundleDescs;
    }

    protected BundleDescriptor[] processWebDescriptors() {
        try {
            processor.generateWebXmlFile(workDir);
            processor.generateWebServicesXmlFile(workDir);
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new IllegalStateException(ex);
        }

        BundleDescriptor[] bundleDescs = { new WebBundleDescriptorImpl(),
                new WebBundleDescriptorImpl() };
        bundleDescs = loadDescriptorFiles(bundleDescs[0], bundleDescs[1],
                new WebDeploymentDescriptorFile(),
                new WebDeploymentDescriptorFile(),
                WEB_INF_DIR, "web.xml");

        bundleDescs = loadDescriptorFiles(bundleDescs[0], bundleDescs[1],
                new WebServicesDeploymentDescriptorFile(bundleDescs[0]),
                new WebServicesDeploymentDescriptorFile(bundleDescs[1]),
                WEB_INF_DIR, "webservices.xml");
  
        return bundleDescs;
    }

    private BundleDescriptor[] loadDescriptorFiles(
            BundleDescriptor expBundleDescriptor,
            BundleDescriptor genBundleDescriptor, 
            DeploymentDescriptorFile expDf, DeploymentDescriptorFile genDf,
            String metaDir, String descFilename) {

        BundleDescriptor[] bundleDescs = new BundleDescriptor[] {
                expBundleDescriptor, genBundleDescriptor };
        String goldenXml = workDir + "/" + GOLDEN_DIR + descFilename;
        File goldenXmlFile = new File(goldenXml);
        if (goldenXmlFile.exists()) {
            hasGoldenFile = true;
            bundleDescs[0] = loadDescriptor(expBundleDescriptor,
                expDf, goldenXml);

            // load gen xml info into DOL for comparison due to various
            // default values
            String genXml = workDir + "/" + metaDir + descFilename;
            File genXmlFile = new File(genXml);
            if (genXmlFile.exists()) {
                bundleDescs[1] = loadDescriptor(genBundleDescriptor,
                    genDf, genXml);
            }
        }
        return bundleDescs;
    }

    private BundleDescriptor loadDescriptor(
            BundleDescriptor bundle, DeploymentDescriptorFile df,
            String loc) {
        File file = new File(loc);
        if (!file.exists()) {
            Assert.fail(loc + " does not exist.");
        }
        try {
            return (BundleDescriptor)df.read(bundle, file);
        } catch(Throwable t) {
            Assert.fail(t.toString());
            return null;
        }
    }
}
