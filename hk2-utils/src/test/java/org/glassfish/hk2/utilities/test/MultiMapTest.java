/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2015 Oracle and/or its affiliates. All rights reserved.
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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.jvnet.hk2.component.MultiMap;

/**
 * @author jwells
 *
 */
public class MultiMapTest {
    private final static String NOKEY = "NoKey";
    
    private final static String KEY1 = "Key1";
    private final static String VALUE1_1 = "Value1_1";
    private final static String VALUE1_2 = "Value1_2";
    
    private final static String KEY2 = "Key2";
    private final static String VALUE2_1 = "Value2_1";
    private final static String VALUE2_2 = "Value2_2";
    private final static String VALUE2_3 = "Value2_3";
    private final static String VALUE2_4 = "Value2_4";
    
    private final static String KEY3 = "Key3";
    private final static String VALUE3_1 = "Value3_1";
    private final static String VALUE3_2 = "Value3_2";
    
    /**
     * Tests the basic add function of MultiMap
     */
    @Test
    public void testBasicAdd() {
        MultiMap<String, String> mm = new MultiMap<String,String>();
        
        mm.add(KEY1, VALUE1_1);
        mm.add(KEY1, VALUE1_2);
        
        mm.add(KEY2, VALUE2_1);
        mm.add(KEY2, VALUE2_2);
        
        Assert.assertEquals(2, mm.size());
        
        {
            List<String> values1 = mm.get(KEY1);
        
            Assert.assertEquals(2, values1.size());
        
            Assert.assertEquals(VALUE1_1, values1.get(0));
            Assert.assertEquals(VALUE1_2, values1.get(1));
        }
        
        {
            List<String> values2 = mm.get(KEY2);
        
            Assert.assertEquals(2, values2.size());
        
            Assert.assertEquals(VALUE2_1, values2.get(0));
            Assert.assertEquals(VALUE2_2, values2.get(1));
        }
        
        Assert.assertTrue(mm.get(NOKEY).isEmpty());
    }
    
    /**
     * Tests the keySet functionality of MultiMap
     */
    @Test
    public void testKeySet() {
        MultiMap<String, String> mm = new MultiMap<String,String>();
        
        Assert.assertTrue(mm.keySet().isEmpty());
        
        mm.add(KEY1, VALUE1_1);
        mm.add(KEY1, VALUE1_2);
        
        mm.add(KEY2, VALUE2_1);
        mm.add(KEY2, VALUE2_2);
        
        Set<String> keySet = mm.keySet();
        Assert.assertEquals(2, keySet.size());
        
        Assert.assertTrue(keySet.contains(KEY1));
        Assert.assertTrue(keySet.contains(KEY2));
        
        Assert.assertNotNull(mm.remove(KEY1));
        
        keySet = mm.keySet();
        
        Assert.assertEquals(1, keySet.size());
        
        Assert.assertTrue(keySet.contains(KEY2));
    }
    
    /**
     * Tests the set functionality of MultiMap
     */
    @Test
    public void testSet() {
        MultiMap<String, String> mm = new MultiMap<String,String>();
        
        mm.add(KEY1, VALUE1_1);
        mm.add(KEY1, VALUE1_2);
        
        LinkedList<String> values2 = new LinkedList<String>();
        
        values2.add(VALUE2_1);
        values2.add(VALUE2_2);
        
        // Set over a pre-existing key
        mm.set(KEY1, values2);
        
        // Post the set call adding a value to to list
        values2.add(VALUE2_3);
        {
            List<String> values1 = mm.get(KEY1);
        
            Assert.assertEquals(2, values1.size());
        
            Assert.assertEquals(VALUE2_1, values1.get(0));
            Assert.assertEquals(VALUE2_2, values1.get(1));
        }
        
        // Set of a key that did not previously exist
        mm.set(KEY2, values2);
        
        {
            List<String> values3 = mm.get(KEY2);
        
            Assert.assertEquals(3, values3.size());
        
            Assert.assertEquals(VALUE2_1, values3.get(0));
            Assert.assertEquals(VALUE2_2, values3.get(1));
            Assert.assertEquals(VALUE2_3, values3.get(2));
        }
    }
    
    /**
     * Tests the set functionality of MultiMap
     */
    @Test
    public void testEmptyListSet() {
        MultiMap<String, String> mm = new MultiMap<String,String>();
        
        mm.add(KEY1, VALUE1_1);
        mm.add(KEY1, VALUE1_2);
        
        Assert.assertEquals(1, mm.size());
        Assert.assertTrue(mm.containsKey(KEY1));
        
        // Set over a pre-existing key
        mm.set(KEY1, new LinkedList<String>());
        
        Assert.assertEquals(0, mm.size());
        Assert.assertFalse(mm.containsKey(KEY1));
        
        // Set over a key not in already
        mm.set(KEY2, new LinkedList<String>());
        
        Assert.assertEquals(0, mm.size());
        Assert.assertFalse(mm.containsKey(KEY2));
    }
    
    /**
     * Tests the set
     */
    @Test
    public void testSetSpecific() {
        MultiMap<String, String> mm = new MultiMap<String,String>();
        
        mm.add(KEY1, VALUE1_1);
        mm.add(KEY1, VALUE1_2);
        
        // Set over an existing key
        mm.set(KEY1, VALUE2_1);
        
        {
            List<String> values1 = mm.get(KEY1);
        
            Assert.assertEquals(1, values1.size());
        
            Assert.assertEquals(VALUE2_1, values1.get(0));
        }
        
        // Set over a key that was not in the map
        mm.set(KEY2, VALUE2_2);
        
        {
            List<String> values2 = mm.get(KEY2);
        
            Assert.assertEquals(1, values2.size());
        
            Assert.assertEquals(VALUE2_2, values2.get(0));
        }
        
    }
    
    /**
     * Tests the containsKey functionality
     */
    @Test
    public void testContainsKey() {
        MultiMap<String, String> mm = new MultiMap<String,String>();
        
        mm.add(KEY1, VALUE1_1);
        mm.add(KEY1, VALUE1_2);
        
        mm.add(KEY2, VALUE2_1);
        
        Assert.assertTrue(mm.containsKey(KEY1));
        Assert.assertTrue(mm.containsKey(KEY2));
        Assert.assertFalse(mm.containsKey(NOKEY));
        
        Assert.assertNotNull(mm.remove(KEY1));
        
        Assert.assertFalse(mm.containsKey(KEY1));
        
    }
    
    /**
     * Tests the contains functionality
     */
    @Test
    public void testContains() {
        MultiMap<String, String> mm = new MultiMap<String,String>();
        
        mm.add(KEY1, VALUE1_1);
        mm.add(KEY1, VALUE1_2);
        
        mm.add(KEY2, VALUE2_1);
        
        Assert.assertTrue(mm.contains(KEY1, VALUE1_1));
        Assert.assertTrue(mm.contains(KEY1, VALUE1_2));
        Assert.assertFalse(mm.contains(KEY1, VALUE2_1));
        Assert.assertTrue(mm.contains(KEY2, VALUE2_1));
        Assert.assertFalse(mm.contains(KEY2, VALUE1_1));
        Assert.assertFalse(mm.contains(KEY2, VALUE2_2));
        Assert.assertFalse(mm.contains(NOKEY, VALUE1_1));
        Assert.assertFalse(mm.contains(NOKEY, VALUE2_3));
        
        Assert.assertTrue(mm.remove(KEY1, VALUE1_2));
        
        Assert.assertTrue(mm.contains(KEY1, VALUE1_1));
        Assert.assertFalse(mm.contains(KEY1, VALUE1_2));
        Assert.assertFalse(mm.contains(KEY1, VALUE2_1));
        
    }
    
    /**
     * Tests remove more fomally
     */
    @Test
    public void testRemove() {
        MultiMap<String, String> mm = new MultiMap<String,String>();
        
        mm.add(KEY1, VALUE1_1);
        mm.add(KEY1, VALUE1_2);
        
        mm.add(KEY2, VALUE2_1);
        
        Assert.assertNull(mm.remove(NOKEY));
        
        {
            List<String> values1 = mm.remove(KEY1);
            
            Assert.assertEquals(2, values1.size());
            
            Assert.assertEquals(VALUE1_1, values1.get(0));
            Assert.assertEquals(VALUE1_2, values1.get(1));
        }
        
        {
            List<String> values2 = mm.remove(KEY2);
            
            Assert.assertEquals(1, values2.size());
            
            Assert.assertEquals(VALUE2_1, values2.get(0));
        }
        
        Assert.assertEquals(0, mm.size());
    }
    
    /**
     * Tests specificremove more fomally
     */
    @Test
    public void testSpecificRemove() {
        MultiMap<String, String> mm = new MultiMap<String,String>();
        
        mm.add(KEY1, VALUE1_1);
        mm.add(KEY1, VALUE1_2);
        
        mm.add(KEY2, VALUE2_1);
        
        Assert.assertFalse(mm.remove(NOKEY, VALUE1_1));
        Assert.assertFalse(mm.remove(KEY2, VALUE2_2));
        Assert.assertTrue(mm.remove(KEY1, VALUE1_1));
        
        Assert.assertEquals(2, mm.size());
        
        Assert.assertTrue(mm.remove(KEY1, VALUE1_2));
        
        Assert.assertEquals(1, mm.size());
        
        Assert.assertTrue(mm.get(KEY1).isEmpty());
        Assert.assertFalse(mm.containsKey(KEY1));
    }
    
    /**
     * Tests getOne
     */
    @Test
    public void testGetOne() {
        MultiMap<String, String> mm = new MultiMap<String,String>();
        
        mm.add(KEY1, VALUE1_1);
        mm.add(KEY1, VALUE1_2);
        
        mm.add(KEY2, VALUE2_1);
        
        Assert.assertEquals(VALUE1_1, mm.getOne(KEY1));
        Assert.assertEquals(VALUE2_1, mm.getOne(KEY2));
        Assert.assertNull(mm.getOne(NOKEY));
        
        mm.add(KEY2, VALUE2_2);
        
        Assert.assertEquals(VALUE2_1, mm.getOne(KEY2));
        
        Assert.assertNotNull(mm.remove(KEY2));
        
        mm.add(KEY2, null);  // Weird, but allowed
        
        Assert.assertNull(mm.getOne(KEY2));
        
    }
    
    /**
     * Tests entrySet
     */
    @Test
    public void testEntrySet() {
        MultiMap<String, String> mm = new MultiMap<String,String>();
        
        mm.add(KEY1, VALUE1_1);
        mm.add(KEY1, VALUE1_2);
        
        mm.add(KEY2, VALUE2_1);
        
        Set<Map.Entry<String, List<String>>> entrySet = mm.entrySet();
        Assert.assertEquals(2, entrySet.size());
        
        int lcv = 0;
        for (Map.Entry<String, List<String>> entry : entrySet) {
            if (lcv++ == 0) {
                Assert.assertEquals(KEY1, entry.getKey());
                List<String> values = entry.getValue();
                
                Assert.assertEquals(2, values.size());
                
                Assert.assertEquals(VALUE1_1, values.get(0));
                Assert.assertEquals(VALUE1_2, values.get(1));
            }
            else {
                Assert.assertEquals(KEY2, entry.getKey());
                List<String> values = entry.getValue();
                
                Assert.assertEquals(1, values.size());
                
                Assert.assertEquals(VALUE2_1, values.get(0));
            }
            
        }
        
    }
    
    private final static String EXPECTED_STRING = "Key1=Value1_1,Key1=Value1_2,Key2=Value2_1,Key2=null";
    
    /**
     * Tests toCommaSeparated
     */
    @Test
    public void testToCommaSeparated() {
        MultiMap<String, String> mm = new MultiMap<String,String>();
        
        mm.add(KEY1, VALUE1_1);
        mm.add(KEY1, VALUE1_2);
        
        mm.add(KEY2, VALUE2_1);
        mm.add(KEY2, null);
        
        Assert.assertEquals(EXPECTED_STRING, mm.toCommaSeparatedString());
    }
    
    /**
     * Tests toString
     */
    @Test
    public void testToString() {
        MultiMap<String, String> mm = new MultiMap<String,String>();
        
        mm.add(KEY1, VALUE1_1);
        mm.add(KEY1, VALUE1_2);
        
        mm.add(KEY2, VALUE2_1);
        
        String ts = mm.toString();
        
        Assert.assertTrue(ts.contains(KEY1));
        Assert.assertTrue(ts.contains(VALUE1_1));
        Assert.assertTrue(ts.contains(VALUE1_2));
        
        Assert.assertTrue(ts.contains(KEY2));
        Assert.assertTrue(ts.contains(VALUE2_1));
        Assert.assertFalse(ts.contains(VALUE2_2));
    }
    
    /**
     * Tests equals
     */
    @Test
    public void testHashCodeEquals() {
        MultiMap<String, String> mm1 = new MultiMap<String,String>();
        
        mm1.add(KEY1, VALUE1_1);
        mm1.add(KEY1, VALUE1_2);
        
        mm1.add(KEY2, VALUE2_1);
        
        MultiMap<String, String> mm2 = new MultiMap<String,String>();
        
        mm2.add(KEY1, VALUE1_1);
        mm2.add(KEY1, VALUE1_2);
        
        mm2.add(KEY2, VALUE2_1);
        
        Assert.assertEquals(mm1, mm2);
        
        Assert.assertEquals(mm1.hashCode(), mm2.hashCode());
    }
    
    /**
     * Tests not equals
     */
    @Test
    public void testNotEquals() {
        MultiMap<String, String> mm1 = new MultiMap<String,String>();
        
        mm1.add(KEY1, VALUE1_1);
        mm1.add(KEY1, VALUE1_2);
        
        mm1.add(KEY2, VALUE2_1);
        
        MultiMap<String, String> mm2 = new MultiMap<String,String>();
        
        mm2.add(KEY1, VALUE1_1);
        mm2.add(KEY1, VALUE1_2);
        
        mm2.add(KEY2, VALUE2_1);
        mm2.add(KEY2, VALUE2_2);
        
        Assert.assertFalse(mm1.equals(mm2));
        
        Assert.assertFalse(mm1.equals(null));
        Assert.assertFalse(mm1.equals(NOKEY));
    }
    
    /**
     * Tests copy constructor
     */
    @Test
    public void testCopyConstructor() {
        MultiMap<String, String> mm1 = new MultiMap<String,String>();
        
        mm1.add(KEY1, VALUE1_1);
        mm1.add(KEY1, VALUE1_2);
        
        mm1.add(KEY2, VALUE2_1);
        
        MultiMap<String, String> mm2 = new MultiMap<String,String>(mm1);
        
        Assert.assertEquals(mm1, mm2);
        
        // Now do something to mm2, make sure it is NOT reflected in mm1
        mm2.add(KEY2, VALUE2_2);
        
        Assert.assertFalse(mm1.equals(mm2));
        
        {
            List<String> mm1Key2Values = mm1.get(KEY2);
        
            Assert.assertEquals(1, mm1Key2Values.size());
            
            Assert.assertEquals(VALUE2_1, mm1Key2Values.get(0));
        }
        
        {
            List<String> mm2Key2Values = mm2.get(KEY2);
        
            Assert.assertEquals(2, mm2Key2Values.size());
            
            Assert.assertEquals(VALUE2_1, mm2Key2Values.get(0));
            Assert.assertEquals(VALUE2_2, mm2Key2Values.get(1));
        }
        
        
    }
    
    /**
     * Tests clone
     * @throws CloneNotSupportedException 
     */
    @Test
    public void testClone() throws CloneNotSupportedException {
        MultiMap<String, String> mm1 = new MultiMap<String,String>();
        
        mm1.add(KEY1, VALUE1_1);
        mm1.add(KEY1, VALUE1_2);
        
        mm1.add(KEY2, VALUE2_1);
        
        MultiMap<String, String> mm2 = mm1.clone();
        
        Assert.assertEquals(mm1, mm2);
        
        // Now do something to mm2, make sure it is NOT reflected in mm1
        mm2.add(KEY2, VALUE2_2);
        
        Assert.assertFalse(mm1.equals(mm2));
        
        {
            List<String> mm1Key2Values = mm1.get(KEY2);
        
            Assert.assertEquals(1, mm1Key2Values.size());
            
            Assert.assertEquals(VALUE2_1, mm1Key2Values.get(0));
        }
        
        {
            List<String> mm2Key2Values = mm2.get(KEY2);
        
            Assert.assertEquals(2, mm2Key2Values.size());
            
            Assert.assertEquals(VALUE2_1, mm2Key2Values.get(0));
            Assert.assertEquals(VALUE2_2, mm2Key2Values.get(1));
        }
    }
    
    /**
     * Tests merge
     */
    @Test
    public void testMerge() {
        MultiMap<String, String> mm1 = new MultiMap<String,String>();
        
        mm1.add(KEY1, VALUE1_1);
        mm1.add(KEY1, VALUE1_2);
        
        mm1.add(KEY2, VALUE2_1);
        mm1.add(KEY2, VALUE2_2);
        mm1.add(KEY2, VALUE2_3);
        
        MultiMap<String, String> mm2 = new MultiMap<String,String>();
        
        mm2.add(KEY2, VALUE2_4);
        mm2.add(KEY2, VALUE2_3);
        mm2.add(KEY2, VALUE2_2);
        
        mm2.add(KEY3, VALUE3_1);
        mm2.add(KEY3, VALUE3_2);
        
        mm1.mergeAll(mm2);
        
        {
            List<String> values = mm1.get(KEY1);
            
            Assert.assertEquals(2, values.size());
            
            Assert.assertEquals(VALUE1_1, values.get(0));
            Assert.assertEquals(VALUE1_2, values.get(1));
        }
        
        {
            List<String> values = mm1.get(KEY2);
            
            Assert.assertEquals(4, values.size());
            
            // order matters here, to show that the dup values did not get reordered
            Assert.assertEquals(VALUE2_1, values.get(0));
            Assert.assertEquals(VALUE2_2, values.get(1));
            Assert.assertEquals(VALUE2_3, values.get(2));
            Assert.assertEquals(VALUE2_4, values.get(3));
        }
        
        {
            List<String> values = mm1.get(KEY3);
            
            Assert.assertEquals(2, values.size());
            
            Assert.assertEquals(VALUE3_1, values.get(0));
            Assert.assertEquals(VALUE3_2, values.get(1));
        }
    }
    
    /**
     * Tests merge with null
     * @throws CloneNotSupportedException 
     */
    @Test
    public void testMergeNull() throws CloneNotSupportedException {
        MultiMap<String, String> mm1 = new MultiMap<String,String>();
        
        mm1.add(KEY1, VALUE1_1);
        mm1.add(KEY1, VALUE1_2);
        
        mm1.add(KEY2, VALUE2_1);
        mm1.add(KEY2, VALUE2_2);
        mm1.add(KEY2, VALUE2_3);
        
        MultiMap<String, String> mm2 = mm1.clone();
        
        mm1.mergeAll(null);
        
        Assert.assertEquals(mm1, mm2);
    }

}
