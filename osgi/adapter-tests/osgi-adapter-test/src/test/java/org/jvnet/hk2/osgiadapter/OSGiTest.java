/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
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
package org.jvnet.hk2.osgiadapter;

import static org.ops4j.pax.exam.CoreOptions.cleanCaches;
import static org.ops4j.pax.exam.CoreOptions.frameworkProperty;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.provision;
import static org.ops4j.pax.exam.CoreOptions.systemPackage;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.workingDirectory;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.ProxyCtl;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.HK2LoaderImpl;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.options.SystemPropertyOption;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.oracle.sdp.management.InstallSDPService;
import com.oracle.test.bar.Bar;
import com.oracle.test.bar.BarContract;
import com.oracle.test.contracts.FooContract;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.bootstrap.Main;
import com.sun.enterprise.module.bootstrap.StartupContext;

/**
 * @author jwells
 *
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class OSGiTest {
    @Inject
    private BundleContext bundleContext;
    
    @Configuration
    public Option[] configuration() {
        String projectVersion = System.getProperty("project.version");
        return options(
                workingDirectory(System.getProperty("basedir") + "/target/wd"),
                systemProperty("java.io.tmpdir").value(System.getProperty("basedir") + "/target"),
                frameworkProperty("org.osgi.framework.storage").value(System.getProperty("basedir") + "/target/felix"),
                systemPackage("sun.misc"),
                systemPackage("javax.net.ssl"),
                systemPackage("javax.xml.bind"),
                systemPackage("javax.xml.bind.annotation"),
                systemPackage("javax.xml.bind.annotation.adapters"),
                systemPackage("javax.xml.namespace"),
                systemPackage("javax.xml.parsers"),
                systemPackage("javax.xml.stream"),
                systemPackage("javax.xml.stream.events"),
                systemPackage("javax.xml.transform"),
                systemPackage("javax.xml.transform.stream"),
                systemPackage("javax.xml.validation"),
                systemPackage("javax.annotation"),
                systemPackage("javax.script"),
                systemPackage("javax.management"),
                systemPackage("org.w3c.dom"),
                systemPackage("org.xml.sax"),
                junitBundles(),
                provision(mavenBundle().groupId(ServiceLocatorHk2MainTest.GROUP_ID).artifactId("hk2-utils").version(projectVersion).startLevel(4)),
                provision(mavenBundle().groupId(ServiceLocatorHk2MainTest.GROUP_ID).artifactId("hk2-api").version(projectVersion).startLevel(4)),
                provision(mavenBundle().groupId(ServiceLocatorHk2MainTest.GROUP_ID).artifactId("hk2-runlevel").version(projectVersion).startLevel(4)),
                provision(mavenBundle().groupId(ServiceLocatorHk2MainTest.GROUP_ID).artifactId("core").version(projectVersion).startLevel(4)),
                provision(mavenBundle().groupId(ServiceLocatorHk2MainTest.GROUP_ID).artifactId("hk2-config").version(projectVersion).startLevel(4)),
                provision(mavenBundle().groupId(ServiceLocatorHk2MainTest.GROUP_ID).artifactId(
                        "hk2-locator").version(projectVersion).startLevel(4)),
                provision(mavenBundle().groupId(ServiceLocatorHk2MainTest.EXT_GROUP_ID).artifactId(
                        "javax.inject").version(projectVersion).startLevel(4)),
                provision(mavenBundle().groupId(ServiceLocatorHk2MainTest.EXT_GROUP_ID).artifactId(
                                                "bean-validator").version(projectVersion).startLevel(4)),
                provision(mavenBundle().groupId("org.javassist").artifactId(
                        "javassist").version("3.18.1-GA").startLevel(4)),
                provision(mavenBundle().groupId(ServiceLocatorHk2MainTest.EXT_GROUP_ID).artifactId(
                        "asm-all-repackaged").version(projectVersion).startLevel(4)),
                provision(mavenBundle().groupId(ServiceLocatorHk2MainTest.EXT_GROUP_ID).artifactId(
                        "aopalliance-repackaged").version(projectVersion).startLevel(4)),
                provision(mavenBundle().groupId(ServiceLocatorHk2MainTest.GROUP_ID)
                        .artifactId("osgi-resource-locator").version("1.0.1").startLevel(4)),
                provision(mavenBundle().groupId(ServiceLocatorHk2MainTest.GROUP_ID).artifactId(
                        "class-model").version(projectVersion).startLevel(4)),
                provision(mavenBundle().groupId(ServiceLocatorHk2MainTest.GROUP_ID).artifactId(
                        "osgi-adapter").version(projectVersion).startLevel(1)),
                provision(mavenBundle().groupId(ServiceLocatorHk2MainTest.GROUP_ID).artifactId(
                        "test-module-startup").version(projectVersion).startLevel(4)),
                provision(mavenBundle().groupId(ServiceLocatorHk2MainTest.GROUP_ID).artifactId(
                                "contract-bundle").version(projectVersion).startLevel(4)),
                provision(mavenBundle().groupId(ServiceLocatorHk2MainTest.GROUP_ID).artifactId(
                                "no-hk2-bundle").version(projectVersion).startLevel(4)),
                provision(mavenBundle().groupId(ServiceLocatorHk2MainTest.GROUP_ID).artifactId(
                                "sdp-management-bundle").version(projectVersion).startLevel(4)),
                provision(mavenBundle().groupId("javax.el").artifactId("javax.el-api").version("2.2.5")),
                // systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level")
                //      .value("DEBUG"),
                cleanCaches()
        // systemProperty("com.sun.enterprise.hk2.repositories").value(cacheDir.toURI().toString()),
        // vmOption(
        // "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005" )
        );
    }
    
    /**
     * Tests that late installation properly removes
     * services
     * 
     * @throws Throwable
     */
    @Test
    public void testLateBundleInstallation() throws Throwable {
        ServiceLocator serviceLocator = getMainServiceLocator();
        
        ServiceTracker hk2Tracker = new ServiceTracker(
                this.bundleContext,
                InstallSDPService.class.getName(),
                null);
        hk2Tracker.open();
        InstallSDPService installationService = (InstallSDPService)
                hk2Tracker.waitForService(0);
        
        hk2Tracker.close();
        
        FooContract fooC = serviceLocator.getService(FooContract.class);
        Assert.assertNull(fooC);
        
        /**
         * First install and uninstall
         */
    
        installationService.install();
        
        List<ActiveDescriptor<?>> descriptors =
                serviceLocator.getDescriptors(
                        BuilderHelper.createContractFilter(
                                FooContract.class.getName()));
        Assert.assertEquals(1, descriptors.size());
        
        Assert.assertTrue(installationService.uninstall());
        
        descriptors =
                serviceLocator.getDescriptors(
                        BuilderHelper.createContractFilter(
                                FooContract.class.getName()));
        
        Assert.assertEquals(0, descriptors.size());
        
        fooC = serviceLocator.getService(FooContract.class);
        Assert.assertNull(fooC);
        
        /**
         * then install again
         */
    
        installationService.install();
        
        descriptors =
                serviceLocator.getDescriptors(
                        BuilderHelper.createContractFilter(
                                FooContract.class.getName()));
        
        Assert.assertEquals(1, descriptors.size());
        
        fooC = serviceLocator.getService(FooContract.class);
        Assert.assertNotNull(fooC);
    }
    
    
    /**
     * See https://java.net/jira/browse/HK2-163
     * 
     * The problem was that the interface had no access to hk2 at
     * all, which caused classloading problems
     * 
     * @throws Throwable
     */
    @Test
    public void testProxyInterfaceWithNoAccessToHK2() throws Throwable {
        ServiceLocator serviceLocator = getMainServiceLocator();
        
        Descriptor addMe = BuilderHelper.link(Bar.class.getName()).
                to(BarContract.class.getName()).
                in(Singleton.class.getName()).
                proxy().
                andLoadWith(new HK2LoaderImpl(Bar.class.getClassLoader())).
                build();
        
        ActiveDescriptor<?> added = ServiceLocatorUtilities.addOneDescriptor(serviceLocator, addMe);
        try {
            BarContract contract = serviceLocator.getService(BarContract.class);
            
            Assert.assertNotNull(contract);
            Assert.assertTrue(contract instanceof ProxyCtl);
        }
        finally {
            ServiceLocatorUtilities.removeOneDescriptor(serviceLocator, added);
        }
    }
    
    /**
     * See https://java.net/jira/browse/HK2-163
     * 
     * The problem was that the interface had no access to hk2 at
     * all, which caused classloading problems
     * 
     * @throws Throwable
     */
    @Test
    public void testProxyClassWithNoAccessToHK2() throws Throwable {
        ServiceLocator serviceLocator = getMainServiceLocator();
        
        // This time the interface is NOT in the set of contracts
        Descriptor addMe = BuilderHelper.link(Bar.class.getName()).
                in(Singleton.class.getName()).
                proxy().
                andLoadWith(new HK2LoaderImpl(Bar.class.getClassLoader())).
                build();
        
        ActiveDescriptor<?> added = ServiceLocatorUtilities.addOneDescriptor(serviceLocator, addMe);
        try {
            Bar bar = serviceLocator.getService(Bar.class);
            
            Assert.assertNotNull(bar);
            Assert.assertTrue(bar instanceof ProxyCtl);
        }
        finally {
            ServiceLocatorUtilities.removeOneDescriptor(serviceLocator, added);
        }
    }
    
    private ServiceLocator getMainServiceLocator() throws Throwable {
        StartupContext startupContext = new StartupContext();
        ServiceTracker hk2Tracker = new ServiceTracker(
                this.bundleContext, Main.class.getName(), null);
        hk2Tracker.open();
        Main main = (Main) hk2Tracker.waitForService(0);
        
        hk2Tracker.close();
        
        ModulesRegistry mr = (ModulesRegistry) bundleContext
                .getService(bundleContext
                        .getServiceReference(ModulesRegistry.class
                                .getName()));

        ServiceLocator serviceLocator = main.createServiceLocator(
                mr, startupContext,null,null);
        
        ServiceLocatorUtilities.enableLookupExceptions(serviceLocator);
        
        return serviceLocator;
        
    }

}
