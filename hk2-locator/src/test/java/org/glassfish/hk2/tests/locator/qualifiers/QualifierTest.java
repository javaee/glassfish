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

package org.glassfish.hk2.tests.locator.qualifiers;

import java.lang.annotation.Annotation;
import java.util.List;

import junit.framework.Assert;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class QualifierTest {
    private final static String TEST_NAME = "QualifierTest";
    private final static ServiceLocator locator = LocatorHelper.create(TEST_NAME, new QualifierModule());
    
    /** 49ers */
    public final static String RED = "Red";
    /** Packers */
    public final static String YELLOW = "Yellow";
    /** Giants */
    public final static String BLUE = "Blue";
    /** Broncos */
    public final static String ORANGE = "Orange";
    /** Vikings */
    public final static String PURPLE = "Purple";
    /** Eagles */
    public final static String GREEN = "Green";
    /** Raiders */
    public final static String BLACK = "Black";

    /**
     * Checks the qualifiers
     */
    @Test
    public void testAllColors() {
        ColorWheel wheel = locator.getService(ColorWheel.class);
        Assert.assertNotNull("ColorWheel is null", wheel);
        
        Assert.assertEquals(RED, wheel.getRed().getColorName());
        Assert.assertEquals(GREEN, wheel.getGreen().getColorName());
        Assert.assertEquals(BLUE, wheel.getBlue().getColorName());
        Assert.assertEquals(YELLOW, wheel.getYellow().getColorName());
        Assert.assertEquals(ORANGE, wheel.getOrange().getColorName());
        Assert.assertEquals(PURPLE, wheel.getPurple().getColorName());
        
    }

    @Test
    public void testUnqualifiedClass() {
        BlackInjectee injectee = locator.getService(BlackInjectee.class);
        Assert.assertNotNull("Injectee is null", injectee);

        Assert.assertEquals(BLACK, injectee.getBlack().getColorName());
    }

    /**
     * Tests getting something via a qualifier only
     */
    @Test
    public void testGetByQualifierOnly() {
        List<SpecifiedImplementation> specs =
                locator.getAllServices(new ImplementationQualifierImpl(SpecifiedImplementation.class.getName()));
        
        Assert.assertNotNull(specs);
        Assert.assertEquals(1, specs.size());
        Assert.assertTrue(specs.get(0) instanceof SpecifiedImplementation);
    }
    
    /**
     * Tests getting something via a qualifier only
     */
    @Test
    public void testGetByQualifierOnlyHandles() {
        List<ServiceHandle<?>> specs =
                locator.getAllServiceHandles(new ImplementationQualifierImpl(SpecifiedImplementation.class.getName()));
        
        Assert.assertNotNull(specs);
        Assert.assertEquals(1, specs.size());
        ServiceHandle<?> handle = specs.get(0);
        
        SpecifiedImplementation si = (SpecifiedImplementation) handle.getService();
        Assert.assertNotNull(si);
    }
    
    /**
     * Tests getting something via a qualifier only
     */
    @Test
    public void testFailToGetByQualifierOnly() {
        List<SpecifiedImplementation> specs =
                locator.getAllServices(new ImplementationQualifierImpl(SpecifiedImplementation.class.getName()),
                        new BlueAnnotationImpl());
        
        Assert.assertNotNull(specs);
        Assert.assertEquals(0, specs.size());
    }
    
    /**
     * Tests getting something via a qualifier only
     */
    @Test
    public void testFailToGetByQualifierOnlyHandles() {
        List<ServiceHandle<?>> specs =
                locator.getAllServiceHandles(new ImplementationQualifierImpl(SpecifiedImplementation.class.getName()),
                        new BlueAnnotationImpl());
        
        Assert.assertNotNull(specs);
        Assert.assertEquals(0, specs.size());
    }
    
    /**
     * Tests getting something via a qualifier only
     */
    @Test(expected=IllegalArgumentException.class)
    public void testNullQualifier() {
        locator.getAllServiceHandles((Annotation) null);
    }
    
    /**
     * Tests getting something via a qualifier only
     */
    @Test(expected=IllegalArgumentException.class)
    public void testDoubleQualifier() {
        locator.getAllServiceHandles(new ImplementationQualifierImpl(SpecifiedImplementation.class.getName()),
                new ImplementationQualifierImpl(SpecifiedImplementation.class.getName() + "_another"));
    }
    
    @Test
    public void testLazyReificationWhenLookedUpByQualifierWithGetAllServiceHandles() {
        List<ServiceHandle<Mauve>> handles = locator.getAllServiceHandles(Mauve.class);
        Assert.assertNotNull(handles);
        Assert.assertEquals(1, handles.size());
        
        ServiceHandle<Mauve> mauveHandle = handles.get(0);
        ActiveDescriptor<Mauve> mauveDescriptor = mauveHandle.getActiveDescriptor();
        
        // The true test
        Assert.assertFalse(mauveDescriptor.isReified());
    }
    
    @Test
    public void testLazyReificationWhenLookedUpByQualifierWithGetServiceHandle() {
        ServiceHandle<Mauve> handle = locator.getServiceHandle(Mauve.class);
        Assert.assertNotNull(handle);
        
        ActiveDescriptor<Mauve> mauveDescriptor = handle.getActiveDescriptor();
        
        // The true test
        Assert.assertFalse(mauveDescriptor.isReified());
    }
    
    @Test
    public void testLookupViaQualifierWithGetService() {
        Object maroonQualified = locator.getService(Maroon.class);
        Assert.assertNotNull(maroonQualified);
        
        Assert.assertTrue(maroonQualified instanceof MaroonQualified);
    }
}
