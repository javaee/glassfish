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

package org.glassfish.hk2.tests.locator.optional;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.glassfish.hk2.api.IterableProvider;
import org.junit.Assert;
import org.jvnet.hk2.annotations.Optional;

/**
 * @author jwells
 *
 */
@Singleton
public class InjectedManyTimes {
    @Inject @Optional
    private SimpleService simpleByField;
    
    @Inject @Optional
    private OptionalService optionalByField;
    
    @Inject @Optional
    private Provider<OptionalService> optionalByProvider;
    
    @Inject @Optional
    private Iterable<OptionalService> optionalByIterable;
    
    @Inject @Optional
    private IterableProvider<OptionalService> optionalByIterableProvider;
    
    private final SimpleService simpleByConstructor;
    private final OptionalService optionalByConstructor;
    
    private SimpleService simpleByMethod;
    private OptionalService optionalByMethod;
    
    private boolean isValid = false;
    
    @Inject
    private InjectedManyTimes(@Optional SimpleService simpleByConstructor,
            @Optional OptionalService optionalByConstructor) {
        this.simpleByConstructor = simpleByConstructor;
        this.optionalByConstructor = optionalByConstructor;
    }
    
    @Inject
    private void viaMethod(@Optional OptionalService optionalByMethod,
            @Optional SimpleService simpleByMethod) {
        this.simpleByMethod = simpleByMethod;
        this.optionalByMethod = optionalByMethod;
        
    }
    
    @SuppressWarnings("unused")
    private void postConstruct() {
        Assert.assertNotNull(simpleByField);
        Assert.assertNull(optionalByField);
        
        Assert.assertNotNull(simpleByConstructor);
        Assert.assertNull(optionalByConstructor);
        
        Assert.assertNotNull(simpleByMethod);
        Assert.assertNull(optionalByMethod);
        
        Assert.assertNotNull(optionalByIterable);
        int lcv = 0;
        for (OptionalService os : optionalByIterable) {
            lcv++;
        }
        Assert.assertEquals(0, lcv);
        
        Assert.assertNotNull(optionalByIterableProvider);
        Assert.assertNull(optionalByIterableProvider.getHandle());
        Assert.assertNull(optionalByIterableProvider.get());
        
        lcv = 0;
        for (OptionalService os : optionalByIterableProvider) {
            lcv++;
        }
        Assert.assertEquals(0, lcv);
        
        Assert.assertNotNull(optionalByProvider);
        Assert.assertNull(optionalByProvider.get());
        
        isValid = true;
    }
    
    public boolean isValid() {
        return isValid;
    }

}
