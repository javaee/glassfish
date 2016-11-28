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
package org.glassfish.hk2.tests.locator.types;

import java.lang.reflect.Type;

import javax.inject.Inject;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class BinderTypesTest {
    private static class A<T, X, Y, Z> {

        private final T t;
        
        @Inject
        private X x;
        
        private Y y;
        private Z z;
        
        @Inject
        public A(T t) {
            this.t = t;
        }
        
        @Inject
        private void init(Z z, Y y) {
            this.z = z;
            this.y = y;
        }

    }

    private static class B {

    }
    
    private static class C {

    }
    
    private static class D {

    }
    
    private static class E {

    }

    /**
     * Tests that we can create a solidified typed class if we tell the descriptor
     * about the solidified type
     */
    @Test
    // @org.junit.Ignore
    public void testBindAsContract() {
        ServiceLocator serviceLocator = ServiceLocatorFactory.getInstance().create(null);
        ServiceLocatorUtilities.bind(serviceLocator, new AbstractBinder() {
            @Override
            protected void configure() {
                bind(B.class).to(B.class);
                bind(C.class).to(C.class);
                bind(D.class).to(D.class);
                bind(E.class).to(E.class);
                
                bindAsContract(new TypeLiteral<A<B, C, D, E>>() {}); // <--- ???
            }
        });

        Type type = new TypeLiteral<A<B, C, D, E>>() {}.getType();
        A<B, C, D, E> ab = serviceLocator.getService(type);
        Assert.assertNotNull(ab);
        
        Assert.assertNotNull(ab.t);
        Assert.assertEquals(B.class, ab.t.getClass());
        
        Assert.assertNotNull(ab.x);
        Assert.assertEquals(C.class, ab.x.getClass());
        
        Assert.assertNotNull(ab.y);
        Assert.assertEquals(D.class, ab.y.getClass());
        
        Assert.assertNotNull(ab.z);
        Assert.assertEquals(E.class, ab.z.getClass());
    }

}
