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
package org.glassfish.hk2.tests.locator.servicelocatorutilities;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.glassfish.hk2.utilities.AbstractActiveDescriptor;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * This tests {@link ServiceLocatorUtility} methods that are not tested
 * in other suites
 * 
 * @author jwells
 *
 */
public class ServiceLocatorUtilitiesTest {
    private final static String TEST_NAME = "ServiceLocatorUtilitiesTest";
    private final static ServiceLocator locator = LocatorHelper.create(TEST_NAME, null);

    @Test
    public void testAddActiveDescriptor() {
        SimpleService ss = new SimpleService();
        
        ActiveDescriptor<SimpleService> active = BuilderHelper.createConstantDescriptor(ss);
        
        ServiceLocatorUtilities.addOneDescriptor(locator, active);
        
        SimpleService ss1 = locator.getService(SimpleService.class);
        Assert.assertNotNull(ss1);
        
        Assert.assertSame(ss, ss1);
    }
    
    @Test
    public void testAddDescriptor() {
        Descriptor descriptor = BuilderHelper.createDescriptorFromClass(SimpleService1.class);
        
        ServiceLocatorUtilities.addOneDescriptor(locator, descriptor);
        
        SimpleService1 ss1 = locator.getService(SimpleService1.class);
        Assert.assertNotNull(ss1);
    }
    
    @Test
    public void testAddNonReifiedActiveDescriptor() {
        SimpleService2 ss = new SimpleService2();
        
        AbstractActiveDescriptor<SimpleService2> active = BuilderHelper.createConstantDescriptor(ss);
        
        NonReifiedActiveDescriptor<SimpleService2> nonReified = new NonReifiedActiveDescriptor<SimpleService2>(active);
        
        ServiceLocatorUtilities.addOneDescriptor(locator, nonReified);
        
        SimpleService2 ss1 = locator.getService(SimpleService2.class);
        Assert.assertNotNull(ss1);
        
        // This should NOT be the same because the non-reified goes in as
        // a normal descriptor, which means the system will create the SimpleService2
        // rather than using the thing from the active descriptor
        Assert.assertNotSame(ss, ss1);
    }
    
    /**
     * Tests the createAndInitialize method
     */
    @Test
    public void testCreateAndInitialize() {
        ServiceWithPostConstruct swpc = locator.createAndInitialize(
                ServiceWithPostConstruct.class);
        Assert.assertNotNull(swpc);
        
        swpc.check();
    }
    
    public static class NonReifiedActiveDescriptor<T> extends AbstractActiveDescriptor<T> implements ActiveDescriptor<T> {
        /**
         * 
         */
        private static final long serialVersionUID = 8750311164952618038L;
        
        private final AbstractActiveDescriptor<T> delegate;
        
        private NonReifiedActiveDescriptor(AbstractActiveDescriptor<T> delegate) {
            super(delegate.getContractTypes(),
                    delegate.getScopeAnnotation(),
                    delegate.getName(),
                    delegate.getQualifierAnnotations(),
                    delegate.getDescriptorType(),
                    delegate.getRanking(),
                    delegate.getMetadata());
            
            this.delegate = delegate;
        }
        
        /**
         * This method is the point of this class.  Since this ActiveDescriptor is not reified the
         * method should treat it as a descriptor, not an active descriptor.  And hence the constant
         * should NOT be honored.
         */
        @Override
        public boolean isReified() {
            return false;
        }
        
        public String getImplementation() {
            return delegate.getImplementation();
        }

        /* (non-Javadoc)
         * @see org.glassfish.hk2.api.ActiveDescriptor#getImplementationClass()
         */
        @Override
        public Class<?> getImplementationClass() {
            return delegate.getImplementationClass();
        }

        /* (non-Javadoc)
         * @see org.glassfish.hk2.api.ActiveDescriptor#create(org.glassfish.hk2.api.ServiceHandle)
         */
        @Override
        public T create(ServiceHandle<?> root) {
            return delegate.create(root);
        }
        
    }
}
