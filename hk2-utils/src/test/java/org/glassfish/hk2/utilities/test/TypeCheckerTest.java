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
package org.glassfish.hk2.utilities.test;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.glassfish.hk2.utilities.reflection.TypeChecker;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the TypeChecker utility.
 * <p>
 * This test case illustrates some of the differences between
 * the java rules for assigning and the CDI rules for safe
 * injection.  In general, the CDI rules are more strict, so
 * you will see in the tests places where what Java allows
 * assignment but CDI does not.  These places are marked in
 * the comments of the code
 * 
 * @author jwells
 *
 */
@SuppressWarnings("unused")
public class TypeCheckerTest {
    @SuppressWarnings("rawtypes")
    private Dao dao;
    
    private Dao<Order> order;
    private Dao<User> user;
    private Dao<?> wildCard;
    private Dao<? extends Persistent> wPersistent;
    private Dao<? extends User> wUser;
    
    private Type daoType;
    private Type orderType;
    private Type userType;
    private Type wildCardType;
    private Type wPersistentType;
    private Type wUserType;
    
    @Before
    public void before() {
        try {
            getTypes();
        }
        catch (NoSuchFieldException nsfe) {
            throw new RuntimeException(nsfe);
        }
    }
    
    private void getTypes() throws NoSuchFieldException {
        Class<?> clazz = getClass();
        Field field;
        
        field = clazz.getDeclaredField("dao");
        daoType = field.getGenericType();
        
        field = clazz.getDeclaredField("order");
        orderType = field.getGenericType();
        
        field = clazz.getDeclaredField("user");
        userType = field.getGenericType();
        
        field = clazz.getDeclaredField("wPersistent");
        wPersistentType = field.getGenericType();
        
        field = clazz.getDeclaredField("wildCard");
        wildCardType = field.getGenericType();
        
        field = clazz.getDeclaredField("wUser");
        wUserType = field.getGenericType();
    }
    
    /**
     * Class to itself
     */
    @Test
    public void testClassToClass() {
        Assert.assertTrue(TypeChecker.isRawTypeSafe(daoType, daoType));
    }
    
    /**
     * Check raw class into unbounded type variables
     */
    @Test // @org.junit.Ignore
    public void testClassToPT() {
        Assert.assertTrue(TypeChecker.isRawTypeSafe(daoType, userType));
        Assert.assertTrue(TypeUtils.isAssignable(daoType, userType));
        
        Assert.assertTrue(TypeChecker.isRawTypeSafe(daoType, orderType));
        Assert.assertTrue(TypeUtils.isAssignable(daoType, orderType));
        
        // Java less restrictive than CDI rule
        Assert.assertFalse(TypeChecker.isRawTypeSafe(userType, daoType));
        Assert.assertTrue(TypeUtils.isAssignable(userType, daoType));
        
        // Java less restrictive than CDI rule
        Assert.assertFalse(TypeChecker.isRawTypeSafe(orderType, daoType));
        Assert.assertTrue(TypeUtils.isAssignable(orderType, daoType));
    }
    
    /**
     * Check wildcard to wildcard
     */
    @Test // @org.junit.Ignore
    public void testWildcardToWildcard() {
        // Java less restrictive than CDI rule
        Assert.assertFalse(TypeChecker.isRawTypeSafe(wildCardType, wildCardType));
        Assert.assertTrue(TypeUtils.isAssignable(wildCardType, wildCardType));
        
    }
    
    /**
     * Check raw class into bounded wildcards
     */
    @Test // @org.junit.Ignore
    public void testClassToBoundedWildcards() {
        Assert.assertTrue(TypeChecker.isRawTypeSafe(daoType, wPersistentType));
        Assert.assertTrue(TypeUtils.isAssignable(daoType, wPersistentType));
        
        Assert.assertTrue(TypeChecker.isRawTypeSafe(wPersistentType, daoType));
        Assert.assertTrue(TypeUtils.isAssignable(wPersistentType, daoType));
        
        Assert.assertTrue(TypeChecker.isRawTypeSafe(daoType, wUserType));
        Assert.assertTrue(TypeUtils.isAssignable(daoType, wUserType));
        
        // Java less restrictive than CDI rule
        Assert.assertFalse(TypeChecker.isRawTypeSafe(wUserType, daoType));
        Assert.assertTrue(TypeUtils.isAssignable(wUserType, daoType));
    }
    
    private static class Dao<T extends Persistent> {}
    private static interface Order extends Persistent {}
    private static class User implements Persistent {}
    private static interface Persistent {}

}
