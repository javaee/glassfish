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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.configuration.hub.api.Instance;
import org.glassfish.hk2.configuration.hub.api.Type;
import org.glassfish.hk2.xml.api.XmlHk2ConfigurationBean;
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
    
    public static String getStringVersionOfTree(OverlayRootABean root, boolean followList) {
        if (followList) {
            return getStringVersionOfTreeList(root);
        }
        
        return getStringVersionOfTreeArray(root);
    }
    
    private static String getStringVersionOfTreeList(OverlayRootABean root) {
        StringBuffer sb = new StringBuffer();
        
        for(UnkeyedLeafBean ulb : root.getUnkeyedLeafList()) {
            sb.append(ulb.getName());
            
            boolean wroteParen = false;
            if (!ulb.getListLeaf().isEmpty()) {
                sb.append(LEFT_PAREN);
                wroteParen = true;
            }
            for (UnkeyedLeafBean ulbc : ulb.getListLeaf()) {
                getStringVersionOfLeafList(ulbc, sb);
            }
            if (wroteParen) {
                sb.append(RIGHT_PAREN);
            }
        }
        
        return sb.toString();
    }
    
    private static void getStringVersionOfLeafList(UnkeyedLeafBean ulb, StringBuffer sb) {
        sb.append(ulb.getName());
        
        boolean wroteParen = false;
        if (!ulb.getListLeaf().isEmpty()) {
            sb.append(LEFT_PAREN);
            wroteParen = true;
        }
        for (UnkeyedLeafBean ulbc : ulb.getListLeaf()) {
            getStringVersionOfLeafList(ulbc, sb);
        }
        if (wroteParen) {
            sb.append(RIGHT_PAREN);
        }
    }
    
    private static String getStringVersionOfTreeArray(OverlayRootABean root) {
        StringBuffer sb = new StringBuffer();
        
        for(UnkeyedLeafBean ulb : root.getUnkeyedLeafArray()) {
            sb.append(ulb.getName());
            
            boolean wroteParen = false;
            if (!ulb.getListLeaf().isEmpty()) {
                sb.append(LEFT_PAREN);
                wroteParen = true;
            }
            for (UnkeyedLeafBean ulbc : ulb.getListLeaf()) {
                getStringVersionOfLeafArray(ulbc, sb);
            }
            if (wroteParen) {
                sb.append(RIGHT_PAREN);
            }
        }
        
        return sb.toString();
    }
    
    private static void getStringVersionOfLeafArray(UnkeyedLeafBean ulb, StringBuffer sb) {
        sb.append(ulb.getName());
        
        boolean wroteParen = false;
        if (!ulb.getListLeaf().isEmpty()) {
            sb.append(LEFT_PAREN);
            wroteParen = true;
        }
        for (UnkeyedLeafBean ulbc : ulb.getListLeaf()) {
            getStringVersionOfLeafArray(ulbc, sb);
        }
        if (wroteParen) {
            sb.append(RIGHT_PAREN);
        }
    }
    
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
    
    private static void createOverlay(OverlayRootABean root, UnkeyedLeafBean parent, String leafNames[]) {
        UnkeyedLeafBean addedListChild = null;
        UnkeyedLeafBean addedArrayChild = null;
        
        for (int lcv = 0; lcv < leafNames.length; lcv++) {
            String leafName = leafNames[lcv];
            
            if (LEFT_PAREN.equals(leafName)) {
                String subList[] = getChildNames(leafNames, lcv);
                
                createOverlay(root, addedListChild, subList);
                createOverlay(root, addedArrayChild, subList);
                
                lcv += subList.length + 1; // The extra one for the right paren
            }
            else if (RIGHT_PAREN.equals(leafName)) {
                // Ignore
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
        UnkeyedLeafBean currentListBean = null;
        UnkeyedLeafBean currentArrayBean = null;
        for (int lcv = 0; lcv < names.length; lcv++) {
            String name = names[lcv];
            
            if (LEFT_PAREN.equals(name)) {
                String childNames[] = getChildNames(names, lcv);
                
                lcv += childNames.length;
               
                checkSingleLetterLeaf(currentListBean, hub, LIST_TYPE, childNames);
                
                checkSingleLetterLeaf(currentArrayBean, hub, ARRAY_TYPE, childNames);
            }
            else if (RIGHT_PAREN.equals(name)) {
                // Ignore it
            }
            else {
                currentListBean = root.getUnkeyedLeafList().get(childCount);
                checkLeafInHub(hub, LIST_TYPE, currentListBean, name, childCount);
                
                currentArrayBean = root.getUnkeyedLeafArray()[childCount];
                checkLeafInHub(hub, ARRAY_TYPE, currentArrayBean, name, childCount);
                
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
        UnkeyedLeafBean currentListBean = null;
        UnkeyedLeafBean currentArrayBean = null;
        for (int lcv = 0; lcv < names.length; lcv++) {
            String name = names[lcv];
            
            if (LEFT_PAREN.equals(name)) {
                String childNames[] = getChildNames(names, lcv);
                
                lcv += childNames.length;
                
                checkSingleLetterLeaf(currentListBean, hub, listChildType, childNames);
                
                checkSingleLetterLeaf(currentArrayBean, hub, arrayChildType, childNames);
            }
            else if (RIGHT_PAREN.equals(name)) {
                // Ignore it
            }
            else {
                currentListBean = root.getListLeaf().get(childCount);
                checkLeafInHub(hub, listChildType, currentListBean, name, childCount);
                
                currentArrayBean = root.getArrayLeaf()[childCount];
                checkLeafInHub(hub, arrayChildType, currentArrayBean, name, childCount);
                
                childCount++;
            }
        }
        
        {
            List<UnkeyedLeafBean> lBeans = root.getListLeaf();
            Assert.assertEquals("Number of entries in " + lBeans + " is wrong", childCount, lBeans.size());
        }
        
        {
            UnkeyedLeafBean aBeans[] = root.getArrayLeaf();
            Assert.assertEquals("Number of entries in " + Arrays.toString(aBeans) + " is wrong", childCount, aBeans.length);
        }
        
        String parentInstanceName = ((XmlHk2ConfigurationBean) root)._getInstanceName();
        
        // Now check hub sizes of children, make sure there are no extras
        {
            Type listType = hub.getCurrentDatabase().getType(listChildType);
            if (listType == null) {
                Assert.assertEquals(0, childCount);
            }
            else {
                Map<String, Instance> listInstances = listType.getInstances();
            
                int myChildrenCount = 0;
                for (Map.Entry<String, Instance> me : listInstances.entrySet()) {
                    String candidateInstanceName = me.getKey();
                    
                    if (isDirectChildBasedOnInstanceName(parentInstanceName, candidateInstanceName)) {
                        myChildrenCount++;
                    }
                }
                
                Assert.assertEquals("The type " + listChildType + " had the wrong number of entries",
                        childCount, myChildrenCount);
            }
        }
        
        {
            Type arrayType = hub.getCurrentDatabase().getType(arrayChildType);
            if (arrayType == null) {
                Assert.assertEquals(0, childCount);
            }
            else {
                Map<String, Instance> arrayInstances = arrayType.getInstances();
                
                int myChildrenCount = 0;
                for (Map.Entry<String, Instance> me : arrayInstances.entrySet()) {
                    String candidateInstanceName = me.getKey();
                    
                    if (isDirectChildBasedOnInstanceName(parentInstanceName, candidateInstanceName)) {
                        myChildrenCount++;
                    }
                }
            
                Assert.assertEquals("The type " + arrayChildType + " had the wrong number of entries",
                        childCount, myChildrenCount);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private static void checkLeafInHub(Hub hub, String typeName, UnkeyedLeafBean bean, String expectedName, int parentIndex) {
        Assert.assertEquals("In type " + typeName + " we got wrong name at parent index " + parentIndex + " in bean " + bean,
                expectedName, bean.getName());
        
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
                    int retLen = dot - leftParenIndex - 1;
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
    
    private final static boolean isDirectChildBasedOnInstanceName(String parentInstanceName, String myInstanceName) {
        if (!myInstanceName.startsWith(parentInstanceName)) return false;
        
        String remainder = myInstanceName.substring(parentInstanceName.length());
        if (!remainder.startsWith(".")) return false;
        
        remainder = remainder.substring(1);
        int dotIndex = remainder.indexOf('.');
        return (dotIndex < 0);
    }
}
