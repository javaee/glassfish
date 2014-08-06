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
package org.glassfish.hk2.configuration.tests.hierarchical;

import java.util.HashSet;

import junit.framework.Assert;

import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.configuration.api.ChildIterable;
import org.glassfish.hk2.configuration.api.ConfigurationUtilities;
import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.configuration.hub.api.ManagerUtilities;
import org.glassfish.hk2.configuration.hub.api.WriteableBeanDatabase;
import org.glassfish.hk2.configuration.hub.api.WriteableType;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.Test;
import org.jvnet.hk2.external.generator.ServiceLocatorGeneratorImpl;

/**
 * @author jwells
 *
 */
public class HierarchicalTest {
    private final static String ALICE = "alice";
    private final static String BOB = "bob";
    private final static String CAROL = "carol";
    private final static String DAVE = "dave";
    private final static String ED = "ed";
    private final static String FRANK = "frank";
    private final static String GIANNA = "gianna";
    private final static String HELEN = "helen";
    private final static String IAN = "ian";
    
    public final static String BBEAN_XPATH = "/a-bean/b-beans/b-bean";
    public final static String CBEAN_XPATH = "/a-bean/b-beans/b-bean/c-beans/c-bean";
    public final static String DBEAN_XPATH = "/a-bean/b-beans/b-bean/d-bean";
    
    /**
     * xml would be like:
     *   <b-beans>
     *     <b-bean name="alice">
     *       <c-beans>
     *         <c-bean name="bob" />
     *         <c-bean name="carol" />
     *       </c-beans>
     *       <d-bean name="dave" />
     *       <d-bean name="ed" />
     *       <d-bean name="frank" />
     *     </b-bean>
     *     <b-bean name="gianna">
     *       <c-beans>
     *         <c-bean name="helen" />
     *         <c-bean name="ian" />
     *         <c-bean name="frank" />
     *       </c-beans>
     *       <d-bean name="dave" />
     *       <d-bean name="frank" />
     *     </b-bean>
     *   </b-beans>
     */
    @Test // @org.junit.Ignore
    public void testBasicHierarchy() {
        BBeans bbeans = new BBeans();
        
        BBean alice = bbeans.addBBean(ALICE);
        BBean gianna = bbeans.addBBean(GIANNA);
        
        CBeans alice_cbeans = alice.getCBeans();
        CBean bob = alice_cbeans.addCBean(BOB);
        CBean carol = alice_cbeans.addCBean(CAROL);
        
        DBean dave = alice.addDBean(DAVE);
        DBean ed = alice.addDBean(ED);
        DBean frank = alice.addDBean(FRANK);
        
        CBeans gianna_cbeans = gianna.getCBeans();
        CBean helen = gianna_cbeans.addCBean(HELEN);
        CBean ian = gianna_cbeans.addCBean(IAN);
        CBean frank2 = gianna_cbeans.addCBean(FRANK);
        
        DBean dave2 = gianna.addDBean(DAVE);
        DBean frank3 = gianna.addDBean(FRANK);
        
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create(null, null, new ServiceLocatorGeneratorImpl());
        
        ConfigurationUtilities.enableConfigurationSystem(locator);
        ServiceLocatorUtilities.addClasses(locator, BService.class, CService.class, DService.class);
        
        Hub hub = locator.getService(Hub.class);
        
        WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        
        WriteableType bbean_type = wbd.addType(BBEAN_XPATH);
        bbean_type.addInstance(getBName(ALICE), alice);
        bbean_type.addInstance(getBName(GIANNA), gianna);
        
        WriteableType cbean_type = wbd.addType(CBEAN_XPATH);
        
        // alices c-beans
        cbean_type.addInstance(getCName(alice, BOB), bob);
        cbean_type.addInstance(getCName(alice, CAROL), carol);
        
        // giannas c-beans
        cbean_type.addInstance(getCName(gianna, HELEN), helen);
        cbean_type.addInstance(getCName(gianna, IAN), ian);
        cbean_type.addInstance(getCName(gianna, FRANK), frank2);
        
        WriteableType dbean_type = wbd.addType(DBEAN_XPATH);
        
        // alices d-beans
        dbean_type.addInstance(getDName(alice, DAVE), dave);
        dbean_type.addInstance(getDName(alice, ED), ed);
        dbean_type.addInstance(getDName(alice, FRANK), frank);
        
        // giannas d-beans
        dbean_type.addInstance(getDName(gianna, DAVE), dave2);
        dbean_type.addInstance(getDName(gianna, FRANK), frank3);
        
        wbd.commit();
        
        // All that setup!  now for the real test
        {
            BService aliceService = locator.getService(BService.class, getBName(ALICE));
            
            {
                ChildIterable<CService> aliceCServices = aliceService.getCServices();
        
                HashSet<String> aliceCServicesNames = new HashSet<String>();
                for (CService cservice : aliceCServices) {
                    aliceCServicesNames.add(cservice.getName());
                }
        
                Assert.assertTrue(aliceCServicesNames.contains(BOB));
                Assert.assertTrue(aliceCServicesNames.contains(CAROL));
                Assert.assertEquals(2, aliceCServicesNames.size());
            }
        
            ChildIterable<DService> aliceDServices = aliceService.getDServices();
            
            {
                HashSet<String> aliceDServicesNames = new HashSet<String>();
                for (DService dservice : aliceDServices) {
                    aliceDServicesNames.add(dservice.getName());
                }
        
                Assert.assertTrue(aliceDServicesNames.contains(DAVE));
                Assert.assertTrue(aliceDServicesNames.contains(ED));
                Assert.assertTrue(aliceDServicesNames.contains(FRANK));
                Assert.assertEquals(3, aliceDServicesNames.size());
            }
            
            Assert.assertNotNull(aliceService.getDave());
            
            ChildIterable<DService> aliceDServicesAsHandle = aliceService.getDServicesAsHandles();
            
            {
                
                HashSet<String> handleNames = new HashSet<String>();
                HashSet<String> aliceDServicesNames = new HashSet<String>();
                
                for (ServiceHandle<DService> dservice : aliceDServicesAsHandle.handleIterator()) {
                    aliceDServicesNames.add(dservice.getService().getName());
                    handleNames.add(dservice.getActiveDescriptor().getName());
                }
        
                Assert.assertTrue(aliceDServicesNames.contains(DAVE));
                Assert.assertTrue(aliceDServicesNames.contains(ED));
                Assert.assertTrue(aliceDServicesNames.contains(FRANK));
                Assert.assertEquals(3, aliceDServicesNames.size());
                
                Assert.assertTrue(handleNames.contains(getDName(alice, DAVE)));
                Assert.assertTrue(handleNames.contains(getDName(alice, ED)));
                Assert.assertTrue(handleNames.contains(getDName(alice, FRANK)));
            }
        }
        
        {
            BService giannaService = locator.getService(BService.class, getBName(GIANNA));
            
            {
                ChildIterable<CService> giannaCServices = giannaService.getCServices();
        
                HashSet<String> giannaCServicesNames = new HashSet<String>();
                for (CService cservice : giannaCServices) {
                    giannaCServicesNames.add(cservice.getName());
                }
        
                Assert.assertTrue(giannaCServicesNames.contains(HELEN));
                Assert.assertTrue(giannaCServicesNames.contains(IAN));
                Assert.assertTrue(giannaCServicesNames.contains(FRANK));
                Assert.assertEquals(3, giannaCServicesNames.size());
            }
        
            ChildIterable<DService> giannaDServices = giannaService.getDServices();
            
            {   
                HashSet<String> giannaDServicesNames = new HashSet<String>();
                for (DService dservice : giannaDServices) {
                    giannaDServicesNames.add(dservice.getName());
                }
        
                Assert.assertTrue(giannaDServicesNames.contains(DAVE));
                Assert.assertTrue(giannaDServicesNames.contains(FRANK));
                Assert.assertEquals(2, giannaDServicesNames.size());
            }
            
            Assert.assertNotNull(giannaService.getDave());
            
            ChildIterable<DService> giannaDServicesAsHandle = giannaService.getDServicesAsHandles();
            
            {
                HashSet<String> handleNames = new HashSet<String>();
                HashSet<String> giannaDServicesNames = new HashSet<String>();
                for (ServiceHandle<DService> dservice : giannaDServicesAsHandle.handleIterator()) {
                    giannaDServicesNames.add(dservice.getService().getName());
                    handleNames.add(dservice.getActiveDescriptor().getName());
                }
        
                Assert.assertTrue(giannaDServicesNames.contains(DAVE));
                Assert.assertTrue(giannaDServicesNames.contains(FRANK));
                Assert.assertEquals(2, giannaDServicesNames.size());
                
                Assert.assertTrue(handleNames.contains(getDName(gianna, DAVE)));
                Assert.assertTrue(handleNames.contains(getDName(gianna, FRANK)));
            }
        }
    }
    
    /**
     * xml will start like this:
     *   <b-beans>
     *     <b-bean name="alice"/>
     *   </b-beans>
     *   
     * and change to this:
     * 
     *   <b-beans>
     *     <b-bean name="alice"/>
     *       <c-beans>
     *         <c-bean name="bob" />
     *       </c-beans>
     *       <d-bean name="dave" />
     *     </b-bean>
     *   </b-beans>
     *   
     * and then back to the original
     */
    @Test
    public void testDynamicAdditionAndRemovals() {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create(null, null, new ServiceLocatorGeneratorImpl());
        
        ConfigurationUtilities.enableConfigurationSystem(locator);
        ServiceLocatorUtilities.addClasses(locator, BService.class, CService.class, DService.class);
        
        Hub hub = locator.getService(Hub.class);
        
        {
            BBeans bbeans = new BBeans();
        
            BBean alice = bbeans.addBBean(ALICE);
        
            WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        
            WriteableType bbean_type = wbd.addType(BBEAN_XPATH);
            bbean_type.addInstance(getBName(ALICE), alice);
        
            wbd.commit();
        }
        
        // An empty BBean is in there, lets get the service
        BService aliceService = locator.getService(BService.class, getBName(ALICE));
        
        ChildIterable<CService> cServices = aliceService.getCServices();
        ChildIterable<DService> dServices = aliceService.getDServices();
        
        for (CService cService : cServices) {
            Assert.fail("There should be no cServices: " + cService);
        }
        
        for (DService dService : dServices) {
            Assert.fail("There should be no dServices: " + dService);
        }
        
        {
            // Now modify the beans
            BBeans bbeans2 = new BBeans();
            
            BBean alice2 = bbeans2.addBBean(ALICE);
            
            CBeans alice2_cbeans = alice2.getCBeans();
            CBean bob2 = alice2_cbeans.addCBean(BOB);
            
            DBean dave2 = alice2.addDBean(DAVE);
            
            WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
            
            WriteableType bbean2_type = wbd.getWriteableType(BBEAN_XPATH);
            bbean2_type.modifyInstance(getBName(ALICE), alice2);
            
            WriteableType cbean_type = wbd.addType(CBEAN_XPATH);
            
            // alice2s c-beans
            cbean_type.addInstance(getCName(alice2, BOB), bob2);
            
            WriteableType dbean_type = wbd.addType(DBEAN_XPATH);
            
            // alice2s d-beans
            dbean_type.addInstance(getDName(alice2, DAVE), dave2);
            
            wbd.commit();
        }
        
        // Alice should NOT have changed
        BService aliceService2 = locator.getService(BService.class, getBName(ALICE));
        Assert.assertEquals(aliceService, aliceService2);
        
        for (CService cService : cServices) {
            Assert.assertEquals(cService.getName(), BOB);
        }
        
        for (DService dService : dServices) {
            Assert.assertEquals(dService.getName(), DAVE);
        }
        
        {
            BBeans bbeans = new BBeans();
        
            BBean alice = bbeans.addBBean(ALICE);
        
            WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        
            WriteableType bbean_type = wbd.getWriteableType(BBEAN_XPATH);
            bbean_type.modifyInstance(getBName(ALICE), alice);
            
            WriteableType cbean_type = wbd.getWriteableType(CBEAN_XPATH);
            
            cbean_type.removeInstance(getCName(alice, BOB));
            
            WriteableType dbean_type = wbd.getWriteableType(DBEAN_XPATH);
            
            dbean_type.removeInstance(getDName(alice, DAVE));
        
            wbd.commit();
        }
        
        // Alice should NOT have changed
        BService aliceService3 = locator.getService(BService.class, getBName(ALICE));
        Assert.assertEquals(aliceService, aliceService3);
        
        // And these should be back to empty
        for (CService cService : cServices) {
            Assert.fail("There should be no cServices: " + cService);
        }
        
        for (DService dService : dServices) {
            Assert.fail("There should be no dServices: " + dService);
        }
    }
    
    /**
     * xml will look like this:
     *
     *   <b-beans>
     *     <b-bean name="alice"/>
     *       <c-beans>
     *         <c-bean name="bob" />
     *       </c-beans>
     *       <d-bean name="dave" />
     *     </b-bean>
     *   </b-beans>
     *   
     * We will then test that we can get the services with the byKey method of ChildIterable
     */
    @Test // @org.junit.Ignore
    public void testByKey() {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create(null, null, new ServiceLocatorGeneratorImpl());
        
        ConfigurationUtilities.enableConfigurationSystem(locator);
        ServiceLocatorUtilities.addClasses(locator, BService.class, CService.class, DService.class);
        
        Hub hub = locator.getService(Hub.class);
        
        doBasicHubSetup(hub);
        
        // An empty BBean is in there, lets get the service
        BService aliceService = locator.getService(BService.class, getBName(ALICE));
        
        ChildIterable<CService> cServices = aliceService.getCServices();
        ChildIterable<DService> dServices = aliceService.getDServices();
        
        Assert.assertNotNull(cServices.byKey(BOB));
        Assert.assertNotNull(dServices.byKey(DAVE));
        
        Assert.assertNull(cServices.byKey(CAROL));
        Assert.assertNull(dServices.byKey(ED));
    }
    
    /**
     * This test fills the hub up with data *prior* to initializing
     * the configuration system, to ensure it still works
     * @throws InterruptedException 
     */
    @Test // @org.junit.Ignore
    public void testFillHubPriorToInitializingConfiguration() throws InterruptedException {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create(null, null, new ServiceLocatorGeneratorImpl());
        
        ManagerUtilities.enableConfigurationHub(locator);
        
        Hub hub = locator.getService(Hub.class);
        
        doBasicHubSetup(hub);
        
        // Turn on configuration later, after hub has its data already
        ConfigurationUtilities.enableConfigurationSystem(locator);
        ServiceLocatorUtilities.addClasses(locator, BService.class, CService.class, DService.class);
        
        // An empty BBean is in there, lets get the service
        BService aliceService = locator.getService(BService.class, getBName(ALICE));
        Assert.assertNotNull(aliceService);
        
        ChildIterable<CService> cServices = aliceService.getCServices();
        ChildIterable<DService> dServices = aliceService.getDServices();
        
        Assert.assertNotNull(cServices.byKey(BOB));
        Assert.assertNotNull(dServices.byKey(DAVE));
        
        Assert.assertNull(cServices.byKey(CAROL));
        Assert.assertNull(dServices.byKey(ED));
    }
    
    /**
     * This test fills the hub up with data *prior* to initializing
     * the configuration system, as well as pre-seeding the locator
     * with ConfiguredBy services, which exercises a different
     * code-path
     * 
     * @throws InterruptedException 
     */
    @Test // @org.junit.Ignore
    public void testFillHubAndServicesPriorToInitializingConfiguration() throws InterruptedException {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create(null, null, new ServiceLocatorGeneratorImpl());
        
        ManagerUtilities.enableConfigurationHub(locator);
        
        Hub hub = locator.getService(Hub.class);
        
        doBasicHubSetup(hub);
        
        // THIS IS THE TEST.  Pre-seed (prior to enabling the configuration system)
        // the ConfiguredBy progenitors.  This takes initialization down a
        // different code path
        ServiceLocatorUtilities.addClasses(locator, BService.class, CService.class, DService.class);
        
        // Turn on configuration later, after hub has its data already
        // AND the progenitor services are already in the locator
        ConfigurationUtilities.enableConfigurationSystem(locator);
         
        // An empty BBean is in there, lets get the service
        BService aliceService = locator.getService(BService.class, getBName(ALICE));
        Assert.assertNotNull(aliceService);
        
        ChildIterable<CService> cServices = aliceService.getCServices();
        ChildIterable<DService> dServices = aliceService.getDServices();
        
        Assert.assertNotNull(cServices.byKey(BOB));
        Assert.assertNotNull(dServices.byKey(DAVE));
        
        Assert.assertNull(cServices.byKey(CAROL));
        Assert.assertNull(dServices.byKey(ED));
    }
    
    /**
     * This test has two CServices hanging off of the BBean that have name
     * "bob" and "bobbob".  The trouble is that when doing "byKey" and asking
     * for "bob" it MAY be possible to get the "bobbob" one if it is
     * implemented improperly.  (Which it was lol)
     * 
     * @throws InterruptedException 
     */
    @Test @org.junit.Ignore
    public void testNamedBobBob() throws InterruptedException {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create(null, null, new ServiceLocatorGeneratorImpl());
        
        ConfigurationUtilities.enableConfigurationSystem(locator);
        ServiceLocatorUtilities.addClasses(locator, BService.class, CService.class, DService.class);
        
        Hub hub = locator.getService(Hub.class);
        
        {
            BBeans bbeans2 = new BBeans();
            
            BBean alice2 = bbeans2.addBBean(ALICE);
            
            CBeans alice2_cbeans = alice2.getCBeans();
            CBean bobbob = alice2_cbeans.addCBean(BOB + BOB);
            CBean bob2 = alice2_cbeans.addCBean(BOB);
            
            WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
            
            WriteableType bbean2_type = wbd.addType(BBEAN_XPATH);
            bbean2_type.addInstance(getBName(ALICE), alice2);
            
            WriteableType cbean_type = wbd.addType(CBEAN_XPATH);
            
            // alice2s c-beans
            cbean_type.addInstance(getCName(alice2, BOB + BOB), bobbob);
            cbean_type.addInstance(getCName(alice2, BOB), bob2);
            
            wbd.commit();
        }
        
         
        // An empty BBean is in there, lets get the service
        BService aliceService = locator.getService(BService.class, getBName(ALICE));
        Assert.assertNotNull(aliceService);
        
        ChildIterable<CService> cServices = aliceService.getCServices();
        
        CService found = cServices.byKey(BOB);
        Assert.assertEquals(BOB, found.getName());
    }
    
    /**
     * xml will look like this:
     *
     *   <b-beans>
     *     <b-bean name="alice"/>
     *       <c-beans>
     *         <c-bean name="bob" />
     *       </c-beans>
     *       <d-bean name="dave" />
     *     </b-bean>
     *   </b-beans>
     *   
     * @param hub The hub to populate
     */
    private static void doBasicHubSetup(Hub hub) {
     // Now modify the beans
        BBeans bbeans2 = new BBeans();
        
        BBean alice2 = bbeans2.addBBean(ALICE);
        
        CBeans alice2_cbeans = alice2.getCBeans();
        CBean bob2 = alice2_cbeans.addCBean(BOB);
        
        DBean dave2 = alice2.addDBean(DAVE);
        
        WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        
        WriteableType bbean2_type = wbd.addType(BBEAN_XPATH);
        bbean2_type.addInstance(getBName(ALICE), alice2);
        
        WriteableType cbean_type = wbd.addType(CBEAN_XPATH);
        
        // alice2s c-beans
        cbean_type.addInstance(getCName(alice2, BOB), bob2);
        
        WriteableType dbean_type = wbd.addType(DBEAN_XPATH);
        
        // alice2s d-beans
        dbean_type.addInstance(getDName(alice2, DAVE), dave2);
        
        wbd.commit();
    }
    
    private static String getBName(String name) {
        return "b-beans." + name;
    }
    
    private static String getCName(BBean bbean, String name) {
        return "b-beans." + bbean.getName() + ".c-beans." + name;
    }
    
    private static String getDName(BBean bbean, String name) {
        return "b-beans." + bbean.getName() + "." + name;
    }

}
