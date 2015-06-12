/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2015 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.utilities.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.glassfish.hk2.utilities.reflection.internal.ClassReflectionHelperImpl;
import org.glassfish.hk2.utilities.reflection.types.AbstractServiceOne;
import org.glassfish.hk2.utilities.reflection.types.AbstractServiceTwo;
import org.glassfish.hk2.utilities.reflection.types.ConcreteServiceOne;
import org.glassfish.hk2.utilities.reflection.types.ConcreteServiceTwo;
import org.glassfish.hk2.utilities.reflection.types.InterfaceFive;
import org.glassfish.hk2.utilities.reflection.types.InterfaceFour;
import org.glassfish.hk2.utilities.reflection.types.InterfaceOne;
import org.glassfish.hk2.utilities.reflection.types.InterfaceThree;
import org.glassfish.hk2.utilities.reflection.types.InterfaceTwo;
import org.glassfish.hk2.utilities.reflection.types.ParameterizedClassOne;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class ReflectionHelperTest {
    private final static String KEY1 = "key1";
    private final static String KEY2 = "key2";
    private final static String VALUE1 = "VALUE1";
    private final static String VALUE2 = "VALUE2";
    private final static String MULTI_KEY = "multiKey";
    private final static String MULTI_VALUE = "\"A,B,C=,D\"";
    private final static String NAKED_MULTI_VALUE = "A,B,C=,D";
    
    private final static String GOOD_METADATA = KEY1 + "=" + VALUE1 + "," +
        MULTI_KEY + "=" + MULTI_VALUE + "," +
        KEY2 + "=" + VALUE2 + "," +
        KEY1 + "=" + VALUE2;
    
    private final static String GOOD_METADATA_2 = "key1=value1,key2=value2";
    
    private final static String BADLY_FORMED_METADATA = KEY1 + "=" + VALUE1 + "," +
            KEY2 + "=\"No trailing quote";
    
    /**
     * Tests the most basic of metadata
     */
    @Test
    public void testBasicServiceMetadata() {
        HashMap<String, List<String>> metadata = new HashMap<String, List<String>>();
        
        ReflectionHelper.parseServiceMetadataString(GOOD_METADATA, metadata);
        
        Assert.assertEquals(metadata.get(KEY1).get(0), VALUE1);
        Assert.assertEquals(metadata.get(KEY1).get(1), VALUE2);
        
        Assert.assertEquals(metadata.get(KEY2).get(0), VALUE2);
        
        Assert.assertEquals(metadata.get(MULTI_KEY).get(0), NAKED_MULTI_VALUE);   
    }
    
    /**
     * Tests another metadata scenario
     */
    @Test
    public void testBasic2ServiceMetadata() {
        HashMap<String, List<String>> metadata = new HashMap<String, List<String>>();
        
        ReflectionHelper.parseServiceMetadataString(GOOD_METADATA_2, metadata);
        
        Assert.assertEquals(metadata.get("key1").get(0), "value1");
        Assert.assertEquals(metadata.get("key2").get(0), "value2");
    }
    
    /**
     * Tests some metadata with an empty string
     */
    @Test
    public void testEmptyStringMetadata() {
        HashMap<String, List<String>> metadata = new HashMap<String, List<String>>();
        
        ReflectionHelper.parseServiceMetadataString("", metadata);
        
        Assert.assertTrue(metadata.isEmpty());
    }
    
    /**
     * Negative test for some bad metadata
     */
    @Test
    public void testBadlyFormedMetadata() {
        HashMap<String, List<String>> metadata = new HashMap<String, List<String>>();
        
        try {
            ReflectionHelper.parseServiceMetadataString(BADLY_FORMED_METADATA, metadata);
            Assert.fail("Should have failed due to parse");
        }
        catch (IllegalStateException ise) {
            Assert.assertTrue(ise.getMessage().contains("Badly formed metadata"));
        }
    }
    
    /**
     * Tests a complex type, makes sure we get everything properly filled in
     */
    @Test // @org.junit.Ignore
    public void testComplexType() {
        Set<Type> allTypes = ReflectionHelper.getAllTypes(ConcreteServiceOne.class);
        Assert.assertEquals(8, allTypes.size());
        
        int lcv = 0;
        for (Type type : allTypes) {
            switch(lcv) {
            case 0:  // top class ConcreteServiceOne
            {
                Assert.assertEquals(ConcreteServiceOne.class, type);
                break;
            }
            case 1:  // AbstractServiceTwo
            {
                Assert.assertTrue(type instanceof ParameterizedType);
                ParameterizedType pt = (ParameterizedType) type;
                
                Assert.assertEquals(AbstractServiceTwo.class, pt.getRawType());
                
                Assert.assertEquals(5, pt.getActualTypeArguments().length);
                Assert.assertEquals(Integer.class, pt.getActualTypeArguments()[0]);
                Assert.assertEquals(Long.class, pt.getActualTypeArguments()[1]);
                Assert.assertEquals(Float.class, pt.getActualTypeArguments()[2]);
                Assert.assertEquals(Double.class, pt.getActualTypeArguments()[3]);
                Assert.assertEquals(Character.class, pt.getActualTypeArguments()[4]);
                break;
            }
            case 2:  // AbstractServiceOne
            {
                Assert.assertTrue(type instanceof ParameterizedType);
                ParameterizedType pt = (ParameterizedType) type;
                
                Assert.assertEquals(AbstractServiceOne.class, pt.getRawType());
                
                Assert.assertEquals(4, pt.getActualTypeArguments().length);
                Assert.assertEquals(Float.class, pt.getActualTypeArguments()[0]);
                Assert.assertEquals(String.class, pt.getActualTypeArguments()[1]);
                Assert.assertEquals(Integer.class, pt.getActualTypeArguments()[2]);
                Assert.assertEquals(Character.class, pt.getActualTypeArguments()[3]);
                break;
            }
            case 3:  // Object
            {
                Assert.assertEquals(Object.class, type);
                break;
            }
            case 4:  // InterfaceFour
            {
                Assert.assertTrue(type instanceof ParameterizedType);
                ParameterizedType pt = (ParameterizedType) type;
                
                Assert.assertEquals(InterfaceFour.class, pt.getRawType());
                
                Assert.assertEquals(2, pt.getActualTypeArguments().length);
                Assert.assertEquals(Double.class, pt.getActualTypeArguments()[0]);
                Assert.assertEquals(Long.class, pt.getActualTypeArguments()[1]);
                break;
            }
            case 5:  // InterfaceThree
            {
                Assert.assertTrue(type instanceof ParameterizedType);
                ParameterizedType pt = (ParameterizedType) type;
                
                Assert.assertEquals(InterfaceThree.class, pt.getRawType());
                
                Assert.assertEquals(1, pt.getActualTypeArguments().length);
                Assert.assertEquals(Double.class, pt.getActualTypeArguments()[0]);
                break;
            }
            case 6:  // InterfaceOne
            {
                Assert.assertTrue(type instanceof ParameterizedType);
                ParameterizedType pt = (ParameterizedType) type;
                
                Assert.assertEquals(InterfaceOne.class, pt.getRawType());
                
                Assert.assertEquals(2, pt.getActualTypeArguments().length);
                Assert.assertEquals(Float.class, pt.getActualTypeArguments()[0]);
                Assert.assertEquals(Integer.class, pt.getActualTypeArguments()[1]);
                break;
            }
            case 7:  // InterfaceTwo
            {
                Assert.assertTrue(type instanceof ParameterizedType);
                ParameterizedType pt = (ParameterizedType) type;
                
                Assert.assertEquals(InterfaceTwo.class, pt.getRawType());
                
                Assert.assertEquals(2, pt.getActualTypeArguments().length);
                Assert.assertEquals(String.class, pt.getActualTypeArguments()[0]);
                Assert.assertEquals(String.class, pt.getActualTypeArguments()[1]);
                break;
            }
            default:
                Assert.fail("Should never get here");
            }
            
            lcv++;
            
        }
    }
    
    /**
     * Tests a complex parameterized type, makes sure we get everything properly filled in
     */
    @Test // @org.junit.Ignore
    public void testComplexParameterizedType() {
        Set<Type> allTypes = ReflectionHelper.getAllTypes(ConcreteServiceTwo.class);
        Assert.assertEquals(4, allTypes.size());
        
        int lcv = 0;
        for (Type type : allTypes) {
            switch(lcv) {
            case 0:  // top class ConcreteServiceOne
            {
                Assert.assertEquals(ConcreteServiceTwo.class, type);
                break;
            }
            case 1:  // AbstractServiceTwo
            {
                Assert.assertTrue(type instanceof ParameterizedType);
                ParameterizedType pt = (ParameterizedType) type;
                
                Assert.assertEquals(ParameterizedClassOne.class, pt.getRawType());
                
                Assert.assertEquals(1, pt.getActualTypeArguments().length);
                Assert.assertEquals(String.class, pt.getActualTypeArguments()[0]);
                break;
            }
            case 2:  // Object
            {
                Assert.assertEquals(Object.class, type);
                break;
            }
            case 3:  // InterfaceFour
            {
                Assert.assertTrue(type instanceof ParameterizedType);
                ParameterizedType pt = (ParameterizedType) type;
                
                Assert.assertEquals(InterfaceFive.class, pt.getRawType());
                
                Assert.assertEquals(1, pt.getActualTypeArguments().length);
                Type iFaceTypePT = pt.getActualTypeArguments()[0];
                Assert.assertTrue(iFaceTypePT instanceof ParameterizedType);
                
                ParameterizedType iFacePT = (ParameterizedType) iFaceTypePT;
                Assert.assertEquals(List.class, iFacePT.getRawType());
                
                Assert.assertEquals(1, iFacePT.getActualTypeArguments().length);
                Assert.assertEquals(String.class, iFacePT.getActualTypeArguments()[0]);
                break;
            }
            default:
                Assert.fail("Should never get here");
            }
            
            lcv++;
        }
        
    }
    
    /**
     * Tests that a field that is a generic type is returned as the hard type
     * that it is when subclassed
     */
    @Test
    public void testFieldWithGenericTypeIsSpecifiedWhenSubclassed() {
        ClassReflectionHelper helper = new ClassReflectionHelperImpl();
        
        Set<Field> fields = helper.getAllFields(FieldAsInteger.class);
        Assert.assertEquals(1, fields.size());
        
        Field field = null;
        for (Field f : fields) {
            field = f;
        }
        
        Type fType = ReflectionHelper.resolveField(FieldAsInteger.class, field);
        Assert.assertEquals(Integer.class, fType);
    }
    
    /**
     * Tests that a field that is a generic type is returned as the hard ParameterizedType
     * that it is when subclassed
     */
    @Test
    public void testFieldWithGenericTypeIsSpecifiedWhenSubclassedPT() {
        ClassReflectionHelper helper = new ClassReflectionHelperImpl();
        
        Set<Field> fields = helper.getAllFields(FieldAsListOfLong.class);
        Assert.assertEquals(1, fields.size());
        
        Field field = null;
        for (Field f : fields) {
            field = f;
        }
        
        Type fType = ReflectionHelper.resolveField(FieldAsListOfLong.class, field);
        Assert.assertTrue(fType instanceof ParameterizedType);
        
        ParameterizedType pt = (ParameterizedType) fType;
        Assert.assertEquals(List.class, pt.getRawType());
        Assert.assertEquals(Long.class, pt.getActualTypeArguments()[0]);
    }
    
    /**
     * Tests that a field that is a generic type can have intermediate types
     * (one intermediate class)
     */
    @Test
    public void testFieldWithIntermediateTypedSubclass() {
        ClassReflectionHelper helper = new ClassReflectionHelperImpl();
        
        Set<Field> fields = helper.getAllFields(FieldAsDouble.class);
        Assert.assertEquals(1, fields.size());
        
        Field field = null;
        for (Field f : fields) {
            field = f;
        }
        
        Type fType = ReflectionHelper.resolveField(FieldAsDouble.class, field);
        Assert.assertEquals(Double.class, fType);
    }
    
    /**
     * Tests that a field that is a generic type can have intermediate types
     * (two intermediate classes)
     */
    @Test
    public void testFieldWithIntermediate2TypedSubclass() {
        ClassReflectionHelper helper = new ClassReflectionHelperImpl();
        
        Set<Field> fields = helper.getAllFields(FieldAsFloat.class);
        Assert.assertEquals(1, fields.size());
        
        Field field = null;
        for (Field f : fields) {
            field = f;
        }
        
        Type fType = ReflectionHelper.resolveField(FieldAsFloat.class, field);
        Assert.assertEquals(Float.class, fType);
    }
    
    /**
     * Tests that a parameterized type field (Map<A,B>) can be
     * filled in
     */
    @Test @org.junit.Ignore
    public void testParameterizedField() {
        ClassReflectionHelper helper = new ClassReflectionHelperImpl();
        
        Set<Field> fields = helper.getAllFields(MapStringString.class);
        Assert.assertEquals(1, fields.size());
        
        Field field = null;
        for (Field f : fields) {
            field = f;
        }
        
        Type fType = ReflectionHelper.resolveField(MapStringString.class, field);
        Assert.assertTrue(fType instanceof ParameterizedType);
        
        ParameterizedType pType = (ParameterizedType) fType;
        Assert.assertEquals(Map.class, pType.getRawType());
        Assert.assertEquals(String.class, pType.getActualTypeArguments()[0]);
        Assert.assertEquals(String.class, pType.getActualTypeArguments()[1]);
    }

}
