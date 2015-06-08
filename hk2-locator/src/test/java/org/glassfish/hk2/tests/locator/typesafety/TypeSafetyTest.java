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

package org.glassfish.hk2.tests.locator.typesafety;

import junit.framework.Assert;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class TypeSafetyTest {
    private final static String TEST_NAME = "TypeSafetyTest";
    private final static ServiceLocator locator = LocatorHelper.create(TEST_NAME, new TypeSafetyModule());
    
    /** Returned by the String version of the parameterized type */
    public static final String CHECK_STRING = "Go Eagles!";
    /** Returned by the Integer version of the parameterized type */
    public static final int CHECK_INTEGER = 13;
    /** Returned by the Double version of the parameterized type */
    public static final double CHECK_DOUBLE = 0.131313;

    /**
     * RequiredType: Class
     * DescriptorType: Parameterized
     */
    @Test
    public void testRequiredClassDescriptorParameterized() {
        RawPSInjectee rpi = locator.getService(RawPSInjectee.class);
        Assert.assertNotNull(rpi);
        
        rpi.validate();
        
    }
    
    /**
     * RequiredType: Parameterized with raw wildcard
     * DescriptorType: Parameterized
     */
    @Test
    public void testRequiredRawWildcardDescriptorParameterized() {
        WildcardPSInjectee rpi = locator.getService(WildcardPSInjectee.class);
        Assert.assertNotNull(rpi);
        
        rpi.validate();
        
    }
    
    /**
     * RequiredType: Parameterized with upper bound wildcard
     * DescriptorType: Parameterized
     */
    @Test
    public void testRequiredUpperWildcardDescriptorParameterized() {
        WildcardUpperBoundPSInjectee rpi = locator.getService(WildcardUpperBoundPSInjectee.class);
        Assert.assertNotNull(rpi);
        
        rpi.validate();
        
    }
    
    /**
     * RequiredType: Parameterized with lower bound wildcard
     * DescriptorType: Parameterized
     */
    @Test
    public void testRequiredLowerWildcardDescriptorParameterized() {
        WildcardLowerBoundPSInjectee rpi = locator.getService(WildcardLowerBoundPSInjectee.class);
        Assert.assertNotNull(rpi);
        
        rpi.validate();
    }
    
    /**
     * RequiredType: Parameterized with type variable
     * DescriptorType: Parameterized
     */
    @Test
    public void testRequiredTypeVariableDescriptorParameterized() {
        @SuppressWarnings("rawtypes")
        WildcardTVSInjectee rpi = locator.getService(WildcardTVSInjectee.class);
        Assert.assertNotNull(rpi);
        
        rpi.validate();
    }
    
    /**
     * RequiredType: Parameterized with parameterized type with actual type
     * DescriptorType: Parameterized
     */
    @Test
    public void testRequiredPTWithActualDescriptorParameterized() {
        ActualTypeTVSInjectee rpi = locator.getService(ActualTypeTVSInjectee.class);
        Assert.assertNotNull(rpi);
        
        rpi.validate();
    }
    
    /**
     * RequiredType: Parameterized with parameterized type with type variable
     * DescriptorType: Parameterized
     */
    @Test
    public void testRequiredPTWithTVDescriptorParameterized() {
        @SuppressWarnings("rawtypes")
        TypeVariableTVSInjectee rpi = locator.getService(TypeVariableTVSInjectee.class);
        Assert.assertNotNull(rpi);
        
        rpi.validate();
    }

}
