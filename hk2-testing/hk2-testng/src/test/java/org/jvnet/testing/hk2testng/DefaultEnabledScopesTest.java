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
package org.jvnet.testing.hk2testng;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import static org.assertj.core.api.Assertions.assertThat;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.HK2Loader;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.jvnet.testing.hk2testng.service.GenericInterface;
import org.jvnet.testing.hk2testng.service.InheritableThreadService;
import org.jvnet.testing.hk2testng.service.PerThreadService;
import org.jvnet.testing.hk2testng.service.impl.ImmediateServiceImpl;
import org.jvnet.testing.hk2testng.service.impl.SimpleService;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author jwells
 *
 */
@HK2
public class DefaultEnabledScopesTest {
    @Inject
    private ServiceLocator locator;

    /**
     * Tests that immediate scope is working by default
     *
     * @throws InterruptedException
     */
    @Test
    public void assertImmediateScopeWorks() throws InterruptedException {
        ServiceLocatorUtilities.addClasses(locator, ImmediateServiceImpl.class);

        assertThat(ImmediateServiceImpl.waitForStart(20 * 1000)).isTrue();
    }

    /**
     * Tests that per thread scope is working by default
     *
     * @throws InterruptedException
     */
    @Test
    public void assertPerThreadScopeWorks() throws InterruptedException {
        ConcurrentHashMap<Long, PerThreadService> results = new ConcurrentHashMap<Long, PerThreadService>();

        for (int lcv = 0; lcv < 3; lcv++) {
            Thread t = new Thread(new LookupThread(locator, results));
            t.start();
        }

        while (results.size() < 3) {
            Thread.sleep(5);
        }


        for (Map.Entry<Long, PerThreadService> entry : results.entrySet()) {
            assertThat(entry.getKey()).isEqualTo(entry.getValue().getId());
        }
    }

    /**
     * Tests that inheritable thread scope is working by default
     *
     * @throws InterruptedException
     */
    @Test
    public void assertInheritableThreadScopeWorks() throws InterruptedException {
        ConcurrentHashMap<Long, InheritableThreadService> results = new ConcurrentHashMap<Long, InheritableThreadService>();

        for (int lcv = 0; lcv < 3; lcv++) {
            Thread t = new Thread(new LookupInheritableThread(locator, results));
            t.start();
        }

        while (results.size() < 3) {
            Thread.sleep(5);
        }

        for (Map.Entry<Long, InheritableThreadService> entry : results.entrySet()) {
            assertThat(entry.getKey()).isEqualTo(entry.getValue().getId());
        }
    }

    /**
     * Tests that reification errors are rethrown by default
     */
    @Test
    public void assertReifyExceptionsAreThrown() {
        Descriptor addMe = BuilderHelper.link(SimpleService.class.getName()).
                to(GenericInterface.class.getName()).
                andLoadWith(new HK2Loader() {

                    @Override
                    public Class<?> loadClass(String className)
                            throws MultiException {
                        throw new MultiException(new ClassNotFoundException("Could not find " + className));
                    }

                }).build();

        ServiceLocatorUtilities.addOneDescriptor(locator, addMe);

        try {
            locator.getService(GenericInterface.class);
            Assert.fail("Should have failed because reification failures are rethrown by default");
        }
        catch (MultiException me) {
            assertThat(me.toString()).contains(SimpleService.class.getName());
        }
    }

    private final static class LookupThread implements Runnable {
        private final ServiceLocator locator;
        private final ConcurrentHashMap<Long, PerThreadService> addResult;

        private LookupThread(ServiceLocator locator, ConcurrentHashMap<Long, PerThreadService> addResult) {
            this.locator = locator;
            this.addResult = addResult;
        }

        @Override
        public void run() {
            long threadId = Thread.currentThread().getId();

            PerThreadService pts = locator.getService(PerThreadService.class);

            addResult.put(threadId, pts);
        }

    }

    private final static class LookupInheritableThread implements Runnable {

        private final ServiceLocator locator;
        private final ConcurrentHashMap<Long, InheritableThreadService> addResult;

        private LookupInheritableThread(ServiceLocator locator, ConcurrentHashMap<Long, InheritableThreadService> addResult) {
            this.locator = locator;
            this.addResult = addResult;
        }

        @Override
        public void run() {
            long threadId = Thread.currentThread().getId();

            InheritableThreadService pts = locator.getService(InheritableThreadService.class);

            addResult.put(threadId, pts);
        }

    }

}
