/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.configuration.hub.api.Instance;
import org.glassfish.hk2.configuration.hub.api.Type;
import org.glassfish.hk2.utilities.general.GeneralUtilities;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.junit.Assert;

/**
 * @author jwells
 *
 */
public class OverlayUtilities {
    public static final String OROOT_A = "overlay-root-A";
    public static final String A_LIST_CHILD = "unkeyed-leaf-list";
    public static final String A_ARRAY_CHILD = "unkeyed-leaf-array";
    public static final String NAME_TAG = "name";
    public static final String LEAF_LIST = "leaf-list";
    public static final String LEAF_ARRAY = "leaf-array";
    
    public static final String OROOT_TYPE = "/" + OROOT_A;
    public static final String LIST_TYPE = "/" + OROOT_A + "/" + A_LIST_CHILD;
    public static final String ARRAY_TYPE = "/" + OROOT_A + "/" + A_ARRAY_CHILD;
    
    private static final String LEFT_PAREN = "(";
    private static final String RIGHT_PAREN = ")";
    
    public static void generateOverlayRootABean(XmlRootHandle<OverlayRootABean> handle, String singleLetterLeafNames) {
        generateOverlayRootABean(handle, singleLetterNames(singleLetterLeafNames));
    }
    
    private static void generateOverlayRootABean(XmlRootHandle<OverlayRootABean> handle, String... leafNames) {
        OverlayRootABean root = handle.getRoot();
        Assert.assertNull(root);
        
        handle.addRoot();
        
        root = handle.getRoot();
        
        createOverlay(root, null, leafNames);
    }
    
    private static int createOverlay(OverlayRootABean root, UnkeyedLeafBean parent, String leafNames[]) {
        UnkeyedLeafBean addedListChild = null;
        UnkeyedLeafBean addedArrayChild = null;
        
        for (int lcv = 0; lcv < leafNames.length; lcv++) {
            String leafName = leafNames[lcv];
            
            if (LEFT_PAREN.equals(leafName)) {
                String subList[] = new String[leafNames.length - 1];
                System.arraycopy(leafNames, 1, subList, 0, subList.length);
                
                int addMe = createOverlay(root, addedListChild, subList);
                Assert.assertEquals(addMe, createOverlay(root, addedArrayChild, subList));
                
                lcv += addMe;
            }
            else if (RIGHT_PAREN.equals(leafName)) {
                return lcv;
            }
            else {
                if (parent == null) {
                    addedArrayChild = root.addUnkeyedLeafArray();
                    addedListChild = root.addUnkeyedLeafList();
                    
                    addedArrayChild.setName(leafName);
                    addedListChild.setName(leafName);
                }
                else {
                    addedArrayChild = parent.addArrayLeaf();
                    addedListChild = parent.addListLeaf();
                    
                    addedArrayChild.setName(leafName);
                    addedListChild.setName(leafName);
                }
            }
        }
        
        return leafNames.length;
    }
    
    private static String[] singleLetterNames(String parseMe) {
        if (parseMe == null) parseMe = "";
        
        String retVal[] = new String[parseMe.length()];
        for (int lcv = 0; lcv < parseMe.length(); lcv++) {
            retVal[lcv] = parseMe.substring(lcv, lcv + 1);
        }
        
        return retVal;
    }
    
    public static void checkSingleLetterOveralyRootA(XmlRootHandle<OverlayRootABean> handle, Hub hub, String names) {
        checkSingleLetterOveralyRootA(handle.getRoot(), hub, singleLetterNames(names));
    }
    
    private static void checkSingleLetterOveralyRootA(OverlayRootABean root, Hub hub, String names[]) {
        String typeName = OROOT_TYPE;
        Type rootType = hub.getCurrentDatabase().getType(typeName);
        
        Map<String, Instance> rootInstance = rootType.getInstances();
        Assert.assertEquals(1, rootInstance.size());
        
        int childCount = 0;
        for (int lcv = 0; lcv < names.length; lcv++) {
            String name = names[lcv];
            
            if (LEFT_PAREN.equals(name)) {
                String childNames[] = getChildNames(names, lcv);
                
                lcv += childNames.length;
                
                for (UnkeyedLeafBean child : root.getUnkeyedLeafList()) {
                    checkSingleLetterLeaf(child, hub, typeName, childNames);
                }
                
                for (UnkeyedLeafBean child : root.getUnkeyedLeafArray()) {
                    checkSingleLetterLeaf(child, hub, typeName, childNames);
                }
            }
            else if (RIGHT_PAREN.equals(name)) {
                // Ignore it
            }
            else {
                UnkeyedLeafBean listBean = root.getUnkeyedLeafList().get(childCount);
                checkLeafInHub(hub, LIST_TYPE, listBean, name);
                
                UnkeyedLeafBean arrayBean = root.getUnkeyedLeafArray()[childCount];
                checkLeafInHub(hub, ARRAY_TYPE, arrayBean, name);
                
                childCount++;
            }
        }
        
        // Now check hub sizes of children, make sure there are no extras
        {
            Type listType = hub.getCurrentDatabase().getType(LIST_TYPE);
            if (listType == null) {
                Assert.assertEquals(0, childCount);
            }
            else {
                Map<String, Instance> listInstances = listType.getInstances();
            
                Assert.assertEquals(childCount, listInstances.size());
            }
        }
        
        {
            Type arrayType = hub.getCurrentDatabase().getType(ARRAY_TYPE);
            if (arrayType == null) {
                Assert.assertEquals(0, childCount);
            }
            else {
                Map<String, Instance> arrayInstances = arrayType.getInstances();
            
                Assert.assertEquals(childCount, arrayInstances.size());
            }
        }
    }
    
    private static void checkSingleLetterLeaf(UnkeyedLeafBean root, Hub hub, String parentType, String names[]) {
        String listChildType = parentType + "/" + LEAF_LIST;
        String arrayChildType = parentType + "/" + LEAF_ARRAY;
        
        int childCount = 0;
        for (int lcv = 0; lcv < names.length; lcv++) {
            String name = names[lcv];
            
            if (LEFT_PAREN.equals(name)) {
                String childNames[] = getChildNames(names, lcv);
                
                lcv += childNames.length;
                
                for (UnkeyedLeafBean child : root.getListLeaf()) {
                    checkSingleLetterLeaf(child, hub, listChildType, childNames);
                }
                
                for (UnkeyedLeafBean child : root.getArrayLeaf()) {
                    checkSingleLetterLeaf(child, hub, arrayChildType, childNames);
                }
            }
            else if (RIGHT_PAREN.equals(name)) {
                // Ignore it
            }
            else {
                UnkeyedLeafBean listBean = root.getListLeaf().get(childCount);
                checkLeafInHub(hub, LIST_TYPE, listBean, name);
                
                UnkeyedLeafBean arrayBean = root.getArrayLeaf()[childCount];
                checkLeafInHub(hub, ARRAY_TYPE, arrayBean, name);
                
                childCount++;
            }
        }
        
        // Now check hub sizes of children, make sure there are no extras
        {
            Type listType = hub.getCurrentDatabase().getType(listChildType);
            if (listType == null) {
                Assert.assertEquals(0, childCount);
            }
            else {
                Map<String, Instance> listInstances = listType.getInstances();
            
                Assert.assertEquals(childCount, listInstances.size());
            }
        }
        
        {
            Type arrayType = hub.getCurrentDatabase().getType(arrayChildType);
            if (arrayType == null) {
                Assert.assertEquals(0, childCount);
            }
            else {
                Map<String, Instance> arrayInstances = arrayType.getInstances();
            
                Assert.assertEquals(childCount, arrayInstances.size());
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private static void checkLeafInHub(Hub hub, String typeName, UnkeyedLeafBean bean, String expectedName) {
        Assert.assertEquals(expectedName, bean.getName());
        
        Type type = hub.getCurrentDatabase().getType(typeName);
        
        Map<String, Instance> instances = type.getInstances();
        boolean found = false;
        for (Instance instance : instances.values()) {
            Map<String, Object> blm = (Map<String, Object>) instance.getBean();
            
            String instanceName = (String) blm.get(NAME_TAG);
            
            if (instanceName != null && instanceName.equals(expectedName)) {
                found = true;
                break;
            }
        }
        
        Assert.assertTrue("Did not find expectedName " + expectedName + " in hub type " + typeName + " in bean " + bean, found);
    }
    
    private final static String[] getChildNames(String names[], int leftParenIndex) {
        Assert.assertEquals(LEFT_PAREN, names[leftParenIndex]);
        
        Assert.assertTrue(names.length > leftParenIndex);
        
        int leftParenCount = 0;
        for (int dot = leftParenIndex + 1; dot < names.length; dot++) {
            String current = names[dot];
            
            if (current.equals(LEFT_PAREN)) {
                leftParenCount++;
            }
            else if (current.equals(RIGHT_PAREN)) {
                if (leftParenCount <= 0) {
                    // This is the terminal right paren, we can now get the substring
                    int retLen = dot - leftParenIndex - 2;
                    if (retLen <= 0) {
                        return new String[0];
                    }
                    
                    String retVal[] = new String[retLen];
                    System.arraycopy(names, leftParenIndex + 1, retVal, 0, retLen);
                    
                    return retVal;
                }
                
                leftParenCount--;
            }
        }
        
        throw new AssertionError("There was a left paren without a matching right paren in " + names);
    }
}
