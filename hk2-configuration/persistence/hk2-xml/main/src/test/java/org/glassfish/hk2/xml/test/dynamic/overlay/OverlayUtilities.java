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
    
    private static final String LIST_TYPE = "/" + OROOT_A + "/" + A_LIST_CHILD;
    private static final String ARRAY_TYPE = "/" + OROOT_A + "/" + A_ARRAY_CHILD;
    
    public static void generateOverlayRootABean(XmlRootHandle<OverlayRootABean> handle, String... leafNames) {
        OverlayRootABean root = handle.getRoot();
        Assert.assertNull(root);
        
        handle.addRoot();
        
        root = handle.getRoot();
        
        for (String leafName : leafNames) {
            {
                UnkeyedLeafBean arrayLeaf = root.addUnkeyedLeafArray();
                arrayLeaf.setName(leafName);
            }
            
            {
                UnkeyedLeafBean listLeaf = root.addUnkeyedLeafList();
                listLeaf.setName(leafName);
            }
        }
    }
    
    public static String[] singleLetterNames(String parseMe) {
        if (parseMe == null) parseMe = "";
        
        String retVal[] = new String[parseMe.length()];
        for (int lcv = 0; lcv < parseMe.length(); lcv++) {
            retVal[lcv] = parseMe.substring(lcv, lcv);
        }
        
        return retVal;
    }
    
    @SuppressWarnings("unchecked")
    public static void checkHubSingleLetterOveralyRootA(Hub hub, String names) {
        Type listType = hub.getCurrentDatabase().getType(LIST_TYPE);
        Type arrayType = hub.getCurrentDatabase().getType(ARRAY_TYPE);
        
        Assert.assertNotNull(listType);
        Assert.assertNotNull(arrayType);
        
        boolean foundInList = false;
        boolean foundInArray = false;
        String childNames[] = singleLetterNames(names);
        for (String childName : childNames) {
            {
                Collection<Instance> listInstances = listType.getInstances().values();
                Assert.assertEquals(childNames.length, listInstances.size());
            
                for (Instance instance : listInstances) {
                    Map<String, Object> beanLike = (Map<String, Object>) instance.getBean();
                
                    Object nameValue = beanLike.get(NAME_TAG);
                    if (GeneralUtilities.safeEquals(nameValue, childName)) {
                        foundInList = true;
                        break;
                    }
                }
            }
            
            {
                Collection<Instance> arrayInstances = arrayType.getInstances().values();
                Assert.assertEquals(childNames.length, arrayInstances.size());
            
                for (Instance instance : arrayInstances) {
                    Map<String, Object> beanLike = (Map<String, Object>) instance.getBean();
                
                    Object nameValue = beanLike.get(NAME_TAG);
                    if (GeneralUtilities.safeEquals(nameValue, childName)) {
                        foundInArray = true;
                        break;
                    }
                }
            }
            
        }
        
        Assert.assertTrue(foundInList);
        Assert.assertTrue(foundInArray);
    }
}
