/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import junit.framework.Assert;

/**
 * Tests for ParameterizedTypeImpl
 * 
 * @author jwells
 *
 */
public class ParameterizedTypeTest {
    
    /**
     * Tests the equals of ParameterizedTypeImpl against a Java provided
     * ParameterizedType
     */
    @Test
    public void testGoodEqualsOfPTI() {
        Class<ExtendsBase> ebc = ExtendsBase.class;
        Type ebcGenericSuperclass= ebc.getGenericSuperclass();
        
        Assert.assertTrue(ebcGenericSuperclass instanceof ParameterizedType);
        
        ParameterizedType ebcPT = (ParameterizedType) ebcGenericSuperclass;
        
        ParameterizedTypeImpl pti = new ParameterizedTypeImpl(Base.class, String.class);
        
        Assert.assertTrue(pti.equals(ebcPT));
        
        Assert.assertTrue(ebcPT.equals(pti));
        
        Assert.assertEquals(ebcPT.hashCode(), pti.hashCode());
    }
    
    /**
     * Tests the toString code of PTI
     */
    @Test
    public void testToStringOfPTI() {
        ParameterizedTypeImpl pti = new ParameterizedTypeImpl(Base.class, String.class);
        
        Assert.assertTrue(pti.toString().contains("Base<String>"));
    }
    
    /**
     * Tests that null passed to equals returns false
     */
    @Test
    public void testNullDoesNotEqualsPTI() {
        ParameterizedTypeImpl pti = new ParameterizedTypeImpl(Base.class, String.class);
        Assert.assertFalse(pti.equals(null));
    }
    
    /**
     * Tests that a non-parameterized type passed to equals returns false
     */
    @Test
    public void testNotPTDoesNotEqualsPTI() {
        ParameterizedTypeImpl pti = new ParameterizedTypeImpl(Base.class, String.class);
        Assert.assertFalse(pti.equals(new String()));
    }
    
    /**
     * Tests that a parameterized type with different raw type are not equal
     */
    @Test
    public void testNotSameRawTypeDoesNotEqualsPTI() {
        ParameterizedTypeImpl pti = new ParameterizedTypeImpl(Base.class, String.class);
        ParameterizedTypeImpl pti1 = new ParameterizedTypeImpl(List.class, String.class);
        
        Assert.assertFalse(pti.equals(pti1));
    }
    
    /**
     * Tests that a parameterized type with different actual type are not equal
     */
    @Test
    public void testNotSameActualTypesDoesNotEqualsPTI() {
        ParameterizedTypeImpl pti = new ParameterizedTypeImpl(Base.class, String.class);
        ParameterizedTypeImpl pti1 = new ParameterizedTypeImpl(Base.class, Integer.class);
        
        Assert.assertFalse(pti.equals(pti1));
    }
    
    /**
     * Tests that a parameterized type a different number of actual arguments
     */
    @Test
    public void testNotSameNumberOfActualTypesDoesNotEqualsPTI() {
        ParameterizedTypeImpl pti = new ParameterizedTypeImpl(Map.class, String.class);
        ParameterizedTypeImpl pti1 = new ParameterizedTypeImpl(Map.class, Integer.class, String.class);
        
        Assert.assertFalse(pti.equals(pti1));
    }
    
    /**
     * Tests that a parameterized type a different number of actual arguments
     */
    @Test
    public void testNullOwnerType() {
        ParameterizedTypeImpl pti = new ParameterizedTypeImpl(Map.class, String.class);
        
        Assert.assertNull(pti.getOwnerType());
    }

}
