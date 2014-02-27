/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.tests.locator.messaging.basic;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Self;
import org.glassfish.hk2.api.messaging.SubscribeTo;
import org.junit.Assert;
import org.jvnet.hk2.annotations.Optional;

/**
 * This subscriber has many different kinds of injection points
 * 
 * @author jwells
 */
@Singleton
public class SubscriberWithInjectionPoints {
    private boolean singletonInjectionPointCalled = false;
    private boolean perLookupInjectionPointCalled = false;
    private boolean multi1InjectionPointCalled = false;
    private boolean multi2InjectionPointCalled = false;
    private boolean multi3InjectionPointCalled = false;
    private boolean optionalInjectionPointCalled = false;
    private boolean selfInjectionPointCalled = false;
    
    @Inject @Self
    private ActiveDescriptor<?> selfie;
    
    @SuppressWarnings("unused")
    private void singletonSubscriber(@SubscribeTo Foo foo, SingletonService singletonService) {
        if (foo != null && singletonService != null) {
            singletonInjectionPointCalled = true;
            return;
        }
        
        Assert.fail("foo=" + foo + " singletonService=" + singletonService + " in singletonSubscriber");
    }
    
    protected void perLookupSubscriber(@SubscribeTo Foo foo, PerLookupService perLookupService) {
        if (foo != null && perLookupService != null) {
            perLookupInjectionPointCalled = true;
            return;
        }
        
        Assert.fail("foo=" + foo + " perLookupService=" + perLookupService + " in perLookupSubscriber");
    }
    
    /* package */ void multi1Subscriber(@SubscribeTo Foo foo, PerLookupService perLookupService, SingletonService singletonService) {
        if (foo != null && perLookupService != null && singletonService != null) {
            multi1InjectionPointCalled = true;
            return;
        }
        
        Assert.fail("foo=" + foo + " perLookupService=" + perLookupService + " singletonService=" + singletonService + " in multi1Subscriber");
    }
    
    /**
     * Different order than multi1 or multi3
     * 
     * @param perLookupService
     * @param foo
     * @param singletonService
     */
    public void multi2Subscriber(PerLookupService perLookupService, @SubscribeTo Foo foo, SingletonService singletonService) {
        if (foo != null && perLookupService != null && singletonService != null) {
            multi2InjectionPointCalled = true;
            return;
        }
        
        Assert.fail("foo=" + foo + " perLookupService=" + perLookupService + " singletonService=" + singletonService + " in multi2Subscriber");
    }
    
    /**
     * Different order than multi1 or multi2
     * 
     * @param perLookupService
     * @param singletonService
     * @param foo
     */
    @SuppressWarnings("unused")
    private void multi3Subscriber(PerLookupService perLookupService, SingletonService singletonService, @SubscribeTo Foo foo) {
        if (foo != null && perLookupService != null && singletonService != null) {
            multi3InjectionPointCalled = true;
            return;
        }
        
        Assert.fail("foo=" + foo + " perLookupService=" + perLookupService + " singletonService=" + singletonService + " in multi3Subscriber");
    }
    
    @SuppressWarnings("unused")
    private void optionalSubscriber(@SubscribeTo Foo foo, @Optional OptionalService optional) {
        if (foo != null && optional == null) {
            optionalInjectionPointCalled = true;
            return;
        }
        
        Assert.fail("foo=" + foo + " optionalService=" + optional + " in optionalSubscriber");
        
    }
    
    @SuppressWarnings("unused")
    private void selfSubscriber(@SubscribeTo Foo foo, @Self ActiveDescriptor<?> selfie) {
        if (foo != null && selfie != null && this.selfie.equals(selfie)) {
            selfInjectionPointCalled = true;
            return;
        }
        
        Assert.fail("foo=" + foo + " selfie=" + selfie + " this.selfie=" + this.selfie + " in selfSubscriber");
    }
    
    public void check() {
        Assert.assertTrue(singletonInjectionPointCalled);
        Assert.assertTrue(perLookupInjectionPointCalled);
        Assert.assertTrue(multi1InjectionPointCalled);
        Assert.assertTrue(multi2InjectionPointCalled);
        Assert.assertTrue(multi3InjectionPointCalled);
        Assert.assertTrue(optionalInjectionPointCalled);
        Assert.assertTrue(selfInjectionPointCalled);
    }

}
