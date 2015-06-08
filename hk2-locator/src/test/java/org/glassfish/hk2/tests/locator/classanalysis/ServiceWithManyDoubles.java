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

package org.glassfish.hk2.tests.locator.classanalysis;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import junit.framework.Assert;

import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;

/**
 * @author jwells
 *
 */
@PerLookup
public class ServiceWithManyDoubles {
    public Double d1;
    
    @Inject
    public Float f1;
    
    @Inject
    public String s1;
    
    @Inject
    public ServiceLocator locator;
    
    @Inject
    public Double d2;
    
    public boolean pickedCorrectConstructor = false;
    
    public boolean incorrectInitializerCalled = false;
    public boolean incorrectPostConstructCalled = false;
    public boolean incorrectPreDestroyCalled = false;
    
    public boolean setD1Called = false;
    public boolean setD2Called = false;
    
    public boolean correctPostConstructCalled = false;
    public boolean correctPreDestroyCalled = false;
    
    public ServiceWithManyDoubles() {
    }
    
    @Inject
    public ServiceWithManyDoubles(String s) {
        s1 = s;
    }

    public ServiceWithManyDoubles(Double d) {
        pickedCorrectConstructor = true;
    }
    
    public ServiceWithManyDoubles(Float f1, Float f2) {
    }
    
    @Inject
    public void setServiceLocator(ServiceLocator sl) {
        incorrectInitializerCalled = true;
    }
    
    public void setD1(Double d1, ServiceLocator sl) {
        if (sl == null) throw new AssertionError("ServiceLocator is null in setD1");
        if (d1 == null) throw new AssertionError("d1 is null in setD1");
        setD1Called = true;
    }
    
    @Inject
    public void setD2(Double d2) {
        if (d2 == null) throw new AssertionError("d2 is null in setD2");
        setD2Called = true;
    }
    
    @PostConstruct
    public void postConstruct() {
        incorrectPostConstructCalled = true;
        
    }
    
    @PreDestroy
    public void preDestory() {
        incorrectPreDestroyCalled = true;
        
    }
    
    public void doublePostConstruct() {
        correctPostConstructCalled = true;
        
    }
    
    public void doublePreDestroy() {
        correctPreDestroyCalled = true; 
    }
    
    public void checkCalls() {
        Assert.assertTrue(pickedCorrectConstructor);
        
        Assert.assertFalse(incorrectInitializerCalled);
        Assert.assertFalse(incorrectPostConstructCalled);
        Assert.assertFalse(incorrectPreDestroyCalled);
 
        Assert.assertTrue(setD1Called);
        Assert.assertTrue(setD2Called);
        
        Assert.assertTrue(correctPostConstructCalled);
        Assert.assertTrue(correctPreDestroyCalled);
        
        Assert.assertEquals(DoubleFactory.DOUBLE, d1);
        Assert.assertEquals(DoubleFactory.DOUBLE, d2);
        
        Assert.assertNull(f1);
        Assert.assertNull(s1);
        Assert.assertNull(locator);
    }
    
    public void checkAfterConstructor() {
        Assert.assertTrue(pickedCorrectConstructor);
        
        Assert.assertFalse(incorrectInitializerCalled);
        Assert.assertFalse(incorrectPostConstructCalled);
        Assert.assertFalse(incorrectPreDestroyCalled);
 
        Assert.assertFalse(setD1Called);
        Assert.assertFalse(setD2Called);
        
        Assert.assertFalse(correctPostConstructCalled);
        Assert.assertFalse(correctPreDestroyCalled);
        
        Assert.assertNull(d1);
        Assert.assertNull(d2);
        
        Assert.assertNull(f1);
        Assert.assertNull(s1);
        Assert.assertNull(locator);
    }
    
    public void checkAfterInitializeBeforePostConstruct() {
        Assert.assertTrue(pickedCorrectConstructor);
        
        Assert.assertFalse(incorrectInitializerCalled);
        Assert.assertFalse(incorrectPostConstructCalled);
        Assert.assertFalse(incorrectPreDestroyCalled);
 
        Assert.assertTrue(setD1Called);
        Assert.assertTrue(setD2Called);
        
        Assert.assertFalse(correctPostConstructCalled);
        Assert.assertFalse(correctPreDestroyCalled);
        
        Assert.assertEquals(DoubleFactory.DOUBLE, d1);
        Assert.assertEquals(DoubleFactory.DOUBLE, d2);
        
        Assert.assertNull(f1);
        Assert.assertNull(s1);
        Assert.assertNull(locator);
    }
    
    public void checkAfterPostConstructWithNoInitialization() {
        Assert.assertTrue(pickedCorrectConstructor);
        
        Assert.assertFalse(incorrectInitializerCalled);
        Assert.assertFalse(incorrectPostConstructCalled);
        Assert.assertFalse(incorrectPreDestroyCalled);
 
        Assert.assertFalse(setD1Called);
        Assert.assertFalse(setD2Called);
        
        Assert.assertTrue(correctPostConstructCalled);
        Assert.assertFalse(correctPreDestroyCalled);
        
        Assert.assertNull(d1);
        Assert.assertNull(d2);
        
        Assert.assertNull(f1);
        Assert.assertNull(s1);
        Assert.assertNull(locator);
    }
    
    public void checkFullCreateWithoutDestroy() {
        Assert.assertTrue(pickedCorrectConstructor);
        
        Assert.assertFalse(incorrectInitializerCalled);
        Assert.assertFalse(incorrectPostConstructCalled);
        Assert.assertFalse(incorrectPreDestroyCalled);
 
        Assert.assertTrue(setD1Called);
        Assert.assertTrue(setD2Called);
        
        Assert.assertTrue(correctPostConstructCalled);
        Assert.assertFalse(correctPreDestroyCalled);
        
        Assert.assertEquals(DoubleFactory.DOUBLE, d1);
        Assert.assertEquals(DoubleFactory.DOUBLE, d2);
        
        Assert.assertNull(f1);
        Assert.assertNull(s1);
        Assert.assertNull(locator);
    }
}
