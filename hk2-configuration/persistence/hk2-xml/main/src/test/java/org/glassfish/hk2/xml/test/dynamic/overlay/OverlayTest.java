/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016-2017 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.xml.test.dynamic.overlay;

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.configuration.hub.api.Change;
import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.configuration.hub.api.Instance;
import org.glassfish.hk2.configuration.hub.api.Change.ChangeCategory;
import org.glassfish.hk2.configuration.hub.api.Type;
import org.glassfish.hk2.xml.api.XmlHk2ConfigurationBean;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.api.XmlService;
import org.glassfish.hk2.xml.test.basic.beans.Commons;
import org.glassfish.hk2.xml.test.basic.beans.Museum;
import org.glassfish.hk2.xml.test.dynamic.rawsets.RawSetsTest;
import org.glassfish.hk2.xml.test.dynamic.rawsets.UpdateListener;
import org.glassfish.hk2.xml.test.utilities.Utilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class OverlayTest {
    /**
     * Overlays original file with new file
     * 
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test
    // @org.junit.Ignore
    public void testRootBeanOnlyOverlay() throws Exception {
        ServiceLocator locator = Utilities.createLocator(UpdateListener.class);
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        UpdateListener listener = locator.getService(UpdateListener.class);
        
        URL url = getClass().getClassLoader().getResource(Commons.MUSEUM1_FILE);
        
        XmlRootHandle<Museum> rootHandle = xmlService.unmarshal(url.toURI(), Museum.class);
        
        RawSetsTest.verifyPreState(rootHandle, hub);
        
        URL url2 = getClass().getClassLoader().getResource(RawSetsTest.MUSEUM2_FILE);
        
        XmlRootHandle<Museum> rootHandle2 = xmlService.unmarshal(url2.toURI(), Museum.class, false, false);
        
        // This just checks to make sure the original tree was not modified when creating the second handle
        RawSetsTest.verifyPreState(rootHandle, hub);
        
        Museum museum = rootHandle.getRoot();
        XmlHk2ConfigurationBean museumAsBean = (XmlHk2ConfigurationBean) museum;
        
        rootHandle.overlay(rootHandle2);
        
        // Now make sure new values show up
        Assert.assertEquals(RawSetsTest.ONE_OH_ONE_INT, museum.getId());
        Assert.assertEquals(Commons.BEN_FRANKLIN, museum.getName());
        Assert.assertEquals(RawSetsTest.ONE_OH_ONE_INT, museum.getAge());
        
        Instance instance = hub.getCurrentDatabase().getInstance(RawSetsTest.MUSEUM_TYPE, RawSetsTest.MUSEUM_INSTANCE);
        Map<String, Object> beanLikeMap = (Map<String, Object>) instance.getBean();
        
        Assert.assertEquals(Commons.BEN_FRANKLIN, beanLikeMap.get(Commons.NAME_TAG));
        Assert.assertEquals(RawSetsTest.ONE_OH_ONE_INT, beanLikeMap.get(Commons.ID_TAG));
        Assert.assertEquals(RawSetsTest.ONE_OH_ONE_INT, beanLikeMap.get(RawSetsTest.AGE_TAG));  // The test
        
        List<Change> changes = listener.getChanges();
        Assert.assertNotNull(changes);
        
        Assert.assertEquals(1, changes.size());
        
        Change oneChange = null;
        for (Change change : changes) {
            Assert.assertEquals(ChangeCategory.MODIFY_INSTANCE, change.getChangeCategory());
            oneChange = change;
        }
    }
    
    private List<Change> doTestA(String original, String overlay) {
        return doTestA(original, overlay, true, true);
    }
    
    private List<Change> doTestA(String original, String overlay, boolean generateLists, boolean generateArrays) {
        ServiceLocator locator = Utilities.createLocator(UpdateListener.class);
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        UpdateListener listener = locator.getService(UpdateListener.class);
        
        XmlRootHandle<OverlayRootABean> originalHandle = xmlService.createEmptyHandle(OverlayRootABean.class, true, true);
        OverlayUtilities.generateOverlayRootABean(originalHandle, generateLists, generateArrays, original);
        
        String originalFromList = OverlayUtilities.getStringVersionOfTree(originalHandle.getRoot(), true);
        String originalFromArray = OverlayUtilities.getStringVersionOfTree(originalHandle.getRoot(), false);
        
        if (generateLists) {
            Assert.assertEquals(original, originalFromList);
        }
        if (generateArrays) {
            Assert.assertEquals(original, originalFromArray);
        }
        
        OverlayUtilities.checkSingleLetterOveralyRootA(originalHandle, hub, generateLists, generateArrays, original);
        
        XmlRootHandle<OverlayRootABean> overlayHandle = xmlService.createEmptyHandle(OverlayRootABean.class, false, false);
        OverlayUtilities.generateOverlayRootABean(overlayHandle, generateLists, generateArrays, overlay);
        
        String overlayFromList = OverlayUtilities.getStringVersionOfTree(overlayHandle.getRoot(), true);
        String overlayFromArray = OverlayUtilities.getStringVersionOfTree(overlayHandle.getRoot(), false);
        
        if (generateLists) {
            Assert.assertEquals(overlay, overlayFromList);
        }
        if (generateArrays) {
            Assert.assertEquals(overlay, overlayFromArray);
        }
        
        OverlayUtilities.checkSingleLetterOveralyRootA(originalHandle, hub, generateLists, generateArrays, original);
        
        originalHandle.overlay(overlayHandle);
        
        String overlayedFromList = OverlayUtilities.getStringVersionOfTree(originalHandle.getRoot(), true);
        String overlayedFromArray = OverlayUtilities.getStringVersionOfTree(originalHandle.getRoot(), false);
        
        if (generateLists) {
            Assert.assertEquals(overlay, overlayedFromList);
        }
        if (generateArrays) {
            Assert.assertEquals(overlay, overlayedFromArray);
        }
        
        OverlayUtilities.checkSingleLetterOveralyRootA(originalHandle, hub, generateLists, generateArrays, overlay);
        
        return listener.getChanges();
    }
    
    /**
     * Tests overlay going from ABC -> BC
     * 
     * @throws Exception
     */
    @Test
    // @org.junit.Ignore
    public void testABCxBC() throws Exception {
        List<Change> changes = doTestA("ABC", "BC");
        
        OverlayUtilities.checkChanges(changes,
                new ChangeDescriptor(ChangeCategory.REMOVE_INSTANCE,
                        OverlayUtilities.LIST_TYPE,     // type name
                        OverlayUtilities.OROOT_A + ".*", "A") // instance name
                , new ChangeDescriptor(ChangeCategory.REMOVE_INSTANCE,
                        OverlayUtilities.ARRAY_TYPE,    // type name
                        OverlayUtilities.OROOT_A + ".*", "A")
                , new ChangeDescriptor(ChangeCategory.MODIFY_INSTANCE,
                        OverlayUtilities.OROOT_TYPE,    // type name
                        OverlayUtilities.OROOT_A,       // instance name
                        null,
                        OverlayUtilities.A_LIST_CHILD,  // prop changed
                        OverlayUtilities.A_ARRAY_CHILD) // prop changed
        );
        
    }
    
    /**
     * Tests overlay going from ABC -> AB
     * 
     * @throws Exception
     */
    @Test
    // @org.junit.Ignore
    public void testABCxAB() throws Exception {
        List<Change> changes = doTestA("ABC", "AB");
        
        OverlayUtilities.checkChanges(changes,
            new ChangeDescriptor(ChangeCategory.MODIFY_INSTANCE,
                OverlayUtilities.OROOT_TYPE,    // type name
                OverlayUtilities.OROOT_A,       // instance name
                null,
                OverlayUtilities.A_LIST_CHILD,  // prop changed
                OverlayUtilities.A_ARRAY_CHILD) // prop changed
            , new ChangeDescriptor(ChangeCategory.REMOVE_INSTANCE,
                OverlayUtilities.ARRAY_TYPE,    // type name
                OverlayUtilities.OROOT_A + ".*", "C")       // instance name
            , new ChangeDescriptor(ChangeCategory.REMOVE_INSTANCE,
                OverlayUtilities.LIST_TYPE,     // type name
                OverlayUtilities.OROOT_A + ".*", "C")       // instance name                    
         );
    }
    
    /**
     * Tests overlay going from ABC -> CBA
     * 
     * @throws Exception
     */
    @Test
    // @org.junit.Ignore
    public void testABCxCBA() throws Exception {
        List<Change> changes = doTestA("ABC", "CBA");
        
        OverlayUtilities.checkChanges(changes,
                new ChangeDescriptor(ChangeCategory.MODIFY_INSTANCE,
                        OverlayUtilities.OROOT_TYPE,    // type name
                        OverlayUtilities.OROOT_A,       // instance name
                        null,
                        OverlayUtilities.A_LIST_CHILD,  // prop changed
                        OverlayUtilities.A_ARRAY_CHILD) // prop changed
        );
    }
    
    /**
     * Tests overlay going from ABC -> BCA
     * 
     * @throws Exception
     */
    @Test
    // @org.junit.Ignore
    public void testABCxBCA() throws Exception {
        List<Change> changes = doTestA("ABC", "BCA");
        
        OverlayUtilities.checkChanges(changes,
                new ChangeDescriptor(ChangeCategory.MODIFY_INSTANCE,
                        OverlayUtilities.OROOT_TYPE,    // type name
                        OverlayUtilities.OROOT_A,       // instance name
                        null,
                        OverlayUtilities.A_LIST_CHILD,  // prop changed
                        OverlayUtilities.A_ARRAY_CHILD) // prop changed
        );
    }
    
    /**
     * Tests overlay going from ABC -> ABCD
     * 
     * @throws Exception
     */
    @Test
    // @org.junit.Ignore
    public void testABCxABCD() throws Exception {
        List<Change> changes = doTestA("ABC", "ABCD");
        
        OverlayUtilities.checkChanges(changes,
                new ChangeDescriptor(ChangeCategory.MODIFY_INSTANCE,
                        OverlayUtilities.OROOT_TYPE,    // type name
                        OverlayUtilities.OROOT_A,       // instance name
                        null,
                        OverlayUtilities.A_LIST_CHILD,  // prop changed
                        OverlayUtilities.A_ARRAY_CHILD) // prop changed
                , new ChangeDescriptor(ChangeCategory.ADD_INSTANCE,
                        OverlayUtilities.ARRAY_TYPE,    // type name
                        OverlayUtilities.OROOT_A + ".*", "D")       // instance name
                , new ChangeDescriptor(ChangeCategory.ADD_INSTANCE,
                        OverlayUtilities.LIST_TYPE,     // type name
                        OverlayUtilities.OROOT_A + ".*", "D")       // instance name
        );
    }
    
    /**
     * Tests overlay going from ABC -> DABC
     * 
     * @throws Exception
     */
    @Test
    // @org.junit.Ignore
    public void testABCxCABC() throws Exception {
        List<Change> changes = doTestA("ABC", "CABC");
        
        OverlayUtilities.checkChanges(changes,
                new ChangeDescriptor(ChangeCategory.MODIFY_INSTANCE,
                        OverlayUtilities.OROOT_TYPE,    // type name
                        OverlayUtilities.OROOT_A,       // instance name
                        null,
                        OverlayUtilities.A_LIST_CHILD,  // prop changed
                        OverlayUtilities.A_ARRAY_CHILD) // prop changed
                , new ChangeDescriptor(ChangeCategory.ADD_INSTANCE,
                        OverlayUtilities.ARRAY_TYPE,    // type name
                        OverlayUtilities.OROOT_A + ".*", "C")       // instance name
                , new ChangeDescriptor(ChangeCategory.ADD_INSTANCE,
                        OverlayUtilities.LIST_TYPE,     // type name
                        OverlayUtilities.OROOT_A + ".*", "C")       // instance name
        );
    }
    
    /**
     * Tests overlay going from ABC -> ABDC
     * 
     * @throws Exception
     */
    @Test
    // @org.junit.Ignore
    public void testABCxABDC() throws Exception {
        List<Change> changes = doTestA("ABC", "ABDC");
        
        OverlayUtilities.checkChanges(changes,
                new ChangeDescriptor(ChangeCategory.MODIFY_INSTANCE,
                        OverlayUtilities.OROOT_TYPE,    // type name
                        OverlayUtilities.OROOT_A,       // instance name
                        null,
                        OverlayUtilities.A_LIST_CHILD,  // prop changed
                        OverlayUtilities.A_ARRAY_CHILD) // prop changed
                , new ChangeDescriptor(ChangeCategory.ADD_INSTANCE,
                        OverlayUtilities.ARRAY_TYPE,    // type name
                        OverlayUtilities.OROOT_A + ".*", "D")       // instance name
                , new ChangeDescriptor(ChangeCategory.ADD_INSTANCE,
                        OverlayUtilities.LIST_TYPE,     // type name
                        OverlayUtilities.OROOT_A + ".*", "D")       // instance name
        );
    }
    
    /**
     * Tests overlay going from ABC -> ABC (no changes)
     * 
     * @throws Exception
     */
    @Test
    // @org.junit.Ignore
    public void testABCxABC() throws Exception {
        List<Change> changes = doTestA("ABC", "ABC");
        
        OverlayUtilities.checkChanges(changes);
    }
    
    /**
     * Tests overlay going from ABC -> ABD (no changes)
     * 
     * @throws Exception
     */
    @Test
    // @org.junit.Ignore
    public void testABCxABD() throws Exception {
        List<Change> changes = doTestA("ABC", "ABD");
        
        OverlayUtilities.checkChanges(changes,
                new ChangeDescriptor(ChangeCategory.MODIFY_INSTANCE,
                        OverlayUtilities.LIST_TYPE,    // type name
                        OverlayUtilities.OROOT_A + ".*",       // instance name
                        "C",
                        OverlayUtilities.NAME_TAG) // prop changed
                , new ChangeDescriptor(ChangeCategory.MODIFY_INSTANCE,
                        OverlayUtilities.ARRAY_TYPE,    // type name
                        OverlayUtilities.OROOT_A + ".*",       // instance name
                        "C",
                        OverlayUtilities.NAME_TAG) // prop changed
        );
    }
    
    /**
     * Tests overlay going from A(B)A(C)A(D) -> A(B)A(C)A(D)
     * 
     * @throws Exception
     */
    @Test
    // @org.junit.Ignore
    public void testA_B_A_C_A_D_xA_B_A_C_A_D_() throws Exception {
        List<Change> changes = doTestA("A(B)A(C)A(D)", "A(B)A(C)A(D)");
        
        OverlayUtilities.checkChanges(changes);
    }
    
    /**
     * Tests overlay going from A(B)A(C)A(D) -> A(B)A(C)
     * 
     * @throws Exception
     */
    @Test
    // @org.junit.Ignore
    public void testA_B_A_C_A_D_xA_B_A_C_() throws Exception {
        List<Change> changes = doTestA("A(B)A(C)A(D)", "A(B)A(C)");
        
        OverlayUtilities.checkChanges(changes,
                new ChangeDescriptor(ChangeCategory.MODIFY_INSTANCE,
                    OverlayUtilities.OROOT_TYPE,    // type name
                    OverlayUtilities.OROOT_A,       // instance name
                    null,
                    OverlayUtilities.A_LIST_CHILD,  // prop changed
                    OverlayUtilities.A_ARRAY_CHILD) // prop changed
                , new ChangeDescriptor(ChangeCategory.REMOVE_INSTANCE,
                    OverlayUtilities.LIST_TYPE + "/" + OverlayUtilities.LEAF_LIST,    // type name
                    OverlayUtilities.OROOT_A + ".*", "D")       // instance name
                , new ChangeDescriptor(ChangeCategory.REMOVE_INSTANCE,
                    OverlayUtilities.LIST_TYPE + "/" + OverlayUtilities.LEAF_ARRAY,    // type name
                    OverlayUtilities.OROOT_A + ".*", "D")       // instance name
                , new ChangeDescriptor(ChangeCategory.REMOVE_INSTANCE,
                    OverlayUtilities.LIST_TYPE,     // type name
                    OverlayUtilities.OROOT_A, "A")       // instance name  
                , new ChangeDescriptor(ChangeCategory.REMOVE_INSTANCE,
                    OverlayUtilities.ARRAY_TYPE  + "/" + OverlayUtilities.LEAF_LIST,    // type name
                    OverlayUtilities.OROOT_A, "A")       // instance name
                , new ChangeDescriptor(ChangeCategory.REMOVE_INSTANCE,
                    OverlayUtilities.ARRAY_TYPE + "/" + OverlayUtilities.LEAF_ARRAY,    // type name
                    OverlayUtilities.OROOT_A + ".*", "D")       // instance name
                , new ChangeDescriptor(ChangeCategory.REMOVE_INSTANCE,
                    OverlayUtilities.ARRAY_TYPE + "/" + OverlayUtilities.LEAF_ARRAY,    // type name
                    OverlayUtilities.OROOT_A, "D")       // instance name
                                  
             );
    }
    
    /**
     * Tests overlay going from A(B)A(C)A(D) -> A(B)A(C)
     * 
     * @throws Exception
     */
    @Test
    // @org.junit.Ignore
    public void testA_B_A_CxA_C_A_B_() throws Exception {
        List<Change> changes = doTestA("A(B)A(C)", "A(C)A(B)");
        
        OverlayUtilities.checkChanges(changes,
                new ChangeDescriptor(ChangeCategory.MODIFY_INSTANCE,
                    OverlayUtilities.OROOT_TYPE,    // type name
                    OverlayUtilities.OROOT_A,       // instance name
                    null,
                    OverlayUtilities.A_LIST_CHILD,  // prop changed
                    OverlayUtilities.A_ARRAY_CHILD) // prop changed
             );
    }
    
    /**
     * Tests overlay going from A(B)A(C)A(D) -> A(B)A(C)
     * 
     * @throws Exception
     */
    @Test
    // @org.junit.Ignore
    public void testA_B_A_C_A_D_xA_C_A_D_() throws Exception {
        List<Change> changes = doTestA("A(B)A(C)A(D)", "A(C)A(D)");
        
        OverlayUtilities.checkChanges(changes,
                new ChangeDescriptor(ChangeCategory.MODIFY_INSTANCE,
                    OverlayUtilities.OROOT_TYPE,    // type name
                    OverlayUtilities.OROOT_A,       // instance name
                    OverlayUtilities.A_LIST_CHILD,  // prop changed
                    OverlayUtilities.A_ARRAY_CHILD) // prop changed
                , new ChangeDescriptor(ChangeCategory.REMOVE_INSTANCE,
                    OverlayUtilities.LIST_TYPE + "/" + OverlayUtilities.LEAF_LIST,    // type name
                    OverlayUtilities.OROOT_A + ".*", "B")       // instance name
                , new ChangeDescriptor(ChangeCategory.REMOVE_INSTANCE,
                    OverlayUtilities.LIST_TYPE + "/" + OverlayUtilities.LEAF_ARRAY,    // type name
                    OverlayUtilities.OROOT_A + ".*", "B")       // instance name
                , new ChangeDescriptor(ChangeCategory.REMOVE_INSTANCE,
                    OverlayUtilities.LIST_TYPE,     // type name
                    OverlayUtilities.OROOT_A, "A")       // instance name  
                , new ChangeDescriptor(ChangeCategory.REMOVE_INSTANCE,
                    OverlayUtilities.ARRAY_TYPE  + "/" + OverlayUtilities.LEAF_LIST,    // type name
                    OverlayUtilities.OROOT_A, "A")       // instance name
                , new ChangeDescriptor(ChangeCategory.REMOVE_INSTANCE,
                    OverlayUtilities.ARRAY_TYPE + "/" + OverlayUtilities.LEAF_ARRAY,    // type name
                    OverlayUtilities.OROOT_A + ".*", "B")       // instance name
                , new ChangeDescriptor(ChangeCategory.REMOVE_INSTANCE,
                    OverlayUtilities.ARRAY_TYPE + "/" + OverlayUtilities.LEAF_ARRAY,    // type name
                    OverlayUtilities.OROOT_A, "B")       // instance name
                                  
             );
    }
    
    /**
     * Tests overlay going from A(B(C)D(EF))G(HI(JKL)) -> A(B(C)D(EF))G(HI(JKL))
     * 
     * @throws Exception
     */
    @Test
    // @org.junit.Ignore
    public void testA_B_C_D_EF__G_HI_JKL__xA_B_C_D_EF__G_HI_JKL__() throws Exception {
        List<Change> changes = doTestA("A(B(C)D(EF))G(HI(JKL))", "A(B(C)D(EF))G(HI(JKL))");
        
        OverlayUtilities.checkChanges(changes);
    }
    
    /**
     * Tests overlay going from A(B(C)D(EF))G(HI(JKL) -> G(HI(JKL))A(B(C)D(EF))
     * 
     * @throws Exception
     */
    @Test
    // @org.junit.Ignore
    public void testA_B_C_D_EF__G_HI_JKL__xG_HI_JKL__A_B_C_D_EF__() throws Exception {
        List<Change> changes = doTestA("A(B(C)D(EF))G(HI(JKL))", "G(HI(JKL))A(B(C)D(EF))");
        
        OverlayUtilities.checkChanges(changes,
                new ChangeDescriptor(ChangeCategory.MODIFY_INSTANCE,
                    OverlayUtilities.OROOT_TYPE,    // type name
                    OverlayUtilities.OROOT_A,       // instance name
                    OverlayUtilities.A_LIST_CHILD,  // prop changed
                    OverlayUtilities.A_ARRAY_CHILD) // prop changed
             );
    }
    
    /**
     * Tests overlay going from A(B(C)) -> A(B(CD(E))) list only
     * 
     * @throws Exception
     */
    @Test
    // @org.junit.Ignore
    public void testListA_B_C__xA_B_CD_E___() throws Exception {
        List<Change> changes = doTestA("A(B(C))", "A(B(CD(E)))", true, false);
        
        OverlayUtilities.checkChanges(changes,
                new ChangeDescriptor(ChangeCategory.ADD_INSTANCE,
                        "/overlay-root-A/unkeyed-leaf-list/leaf-list/leaf-list",    // type name
                        "overlay-root-A.*.*.*",      // instance name
                        null
               
                 )
                 , new ChangeDescriptor(ChangeCategory.ADD_TYPE,
                         "/overlay-root-A/unkeyed-leaf-list/leaf-list/leaf-list/leaf-list",    // type name
                         null,      // instance name
                         null
       
                 )
                 , new ChangeDescriptor(ChangeCategory.ADD_INSTANCE,
                         "/overlay-root-A/unkeyed-leaf-list/leaf-list/leaf-list/leaf-list",    // type name
                         "overlay-root-A.*.*.*.*",      // instance name
                         null
                
                  )
                  , new ChangeDescriptor(ChangeCategory.MODIFY_INSTANCE,
                         "/overlay-root-A/unkeyed-leaf-array/leaf-list/leaf-lst",    // type name
                         "overlay-root-A.*.*",      // instance name
                         "leaf-list"
       
                  )        
         );
    }
    
    /**
     * Tests overlay going from A(B(C)) -> A(B(CD(E))) array only
     * 
     * @throws Exception
     */
    @Test
    // @org.junit.Ignore
    public void testArrayA_B_C__xA_B_CD_E___() throws Exception {
        List<Change> changes = doTestA("A(B(C))", "A(B(CD(E)))", false, true);
        
        OverlayUtilities.checkChanges(changes,
                new ChangeDescriptor(ChangeCategory.ADD_INSTANCE,
                        "/overlay-root-A/unkeyed-leaf-array/leaf-array/leaf-array",    // type name
                        "overlay-root-A.*.*.*",      // instance name
                        null
               
                 )
                 , new ChangeDescriptor(ChangeCategory.ADD_TYPE,
                         "/overlay-root-A/unkeyed-leaf-array/leaf-array/leaf-array/leaf-array",    // type name
                         null,      // instance name
                         null
       
                 )
                 , new ChangeDescriptor(ChangeCategory.ADD_INSTANCE,
                         "/overlay-root-A/unkeyed-leaf-array/leaf-array/leaf-array/leaf-array",    // type name
                         "overlay-root-A.*.*.*.*",      // instance name
                         null
                
                  )
                  , new ChangeDescriptor(ChangeCategory.MODIFY_INSTANCE,
                         "/overlay-root-A/unkeyed-leaf-array/leaf-array",    // type name
                         "overlay-root-A.*.*",      // instance name
                         "leaf-array"
       
                  )        
         );
    }
}
