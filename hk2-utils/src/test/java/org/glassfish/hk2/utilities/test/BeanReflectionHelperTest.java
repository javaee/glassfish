/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014-2016 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.utilities.test;

import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Map;

import org.glassfish.hk2.utilities.reflection.BeanReflectionHelper;
import org.glassfish.hk2.utilities.reflection.ClassReflectionHelper;
import org.glassfish.hk2.utilities.reflection.internal.ClassReflectionHelperImpl;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class BeanReflectionHelperTest {
    private final static String VALUE = "vAlUe";
    private final static int ANOTHER_VALUE = 13;
    private final static long THIRD_VALUE = Long.MIN_VALUE;
    private final static String BEAN2_KEY = "bean2";
    private final static String BEAN3_KEY = "bean3";
    
    private final static ClassReflectionHelper classHelper = new ClassReflectionHelperImpl();
    
    private static GenericJavaBean createStandardBean() {
        GenericJavaBean gjb = new GenericJavaBean();
        
        gjb.setValue(VALUE);
        gjb.setAnotherValue(ANOTHER_VALUE);
        gjb.setThirdValue(THIRD_VALUE);
        gjb.setBean3(new Generic3JavaBean());
        
        return gjb;
    }
    
    /**
     * Converts a javabean to a map
     */
    @Test
    public void testConvertingBeanToMap() {
        GenericJavaBean gjb = createStandardBean();
        
        Map<String, Object> map = BeanReflectionHelper.convertJavaBeanToBeanLikeMap(classHelper, gjb);
        
        Assert.assertEquals(5, map.size());
        
        Assert.assertEquals(VALUE, map.get("value"));
        int mapAnotherValue = (Integer) map.get("anotherValue");
        long mapThirdValue = (Long) map.get("thirdValue");
        Generic2JavaBean bean2 = (Generic2JavaBean) map.get(BEAN2_KEY);
        Generic3JavaBean bean3 = (Generic3JavaBean) map.get(BEAN3_KEY);
        
        Assert.assertEquals(ANOTHER_VALUE, mapAnotherValue);
        Assert.assertEquals(THIRD_VALUE, mapThirdValue);
        Assert.assertNull(bean2);
        Assert.assertNotNull(bean3);
    }
    
    
    
    /**
     * Converts a javabean to a map
     */
    @Test
    public void testGetMapChangeEvents() {
        GenericJavaBean gjb = createStandardBean();
        
        Map<String, Object> oldMap = BeanReflectionHelper.convertJavaBeanToBeanLikeMap(classHelper, gjb);
        oldMap.remove(BEAN2_KEY);
        
        Map<String, Object> newMap = new HashMap<String, Object>(oldMap);
        
        newMap.put(BEAN2_KEY, new Generic2JavaBean());
        newMap.remove(BEAN3_KEY);
        newMap.put("anotherValue", new Integer(ANOTHER_VALUE + 1));
        
        PropertyChangeEvent events[] = BeanReflectionHelper.getChangeEvents(classHelper, oldMap, newMap);
        
        Assert.assertEquals(3, events.length);
        
        boolean modify = false;
        boolean add = false;
        boolean remove = false;
        for (int lcv = 0; lcv < events.length; lcv++) {
            PropertyChangeEvent event = events[lcv];
            
            if ("anotherValue".equals(event.getPropertyName())) {
                Assert.assertEquals(new Integer(13), event.getOldValue());
                Assert.assertEquals(new Integer(14), event.getNewValue());
                modify = true;
            }
            else if (BEAN2_KEY.equals(event.getPropertyName())) {
                Assert.assertNotNull(event.getNewValue());
                Assert.assertNull(event.getOldValue());
                add = true;
            }
            else if(BEAN3_KEY.equals(event.getPropertyName())) {
                Assert.assertNull(event.getNewValue());
                Assert.assertNotNull(event.getOldValue());
                remove = true;
            }
        }
        
        Assert.assertTrue(modify);
        Assert.assertTrue(add);
        Assert.assertTrue(remove);
    }

}
