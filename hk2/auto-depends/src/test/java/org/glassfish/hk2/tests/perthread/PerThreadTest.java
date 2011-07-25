/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.tests.perthread;

import com.sun.hk2.component.Holder;
import org.glassfish.hk2.BinderFactory;
import org.glassfish.hk2.HK2;
import org.glassfish.hk2.Module;
import org.glassfish.hk2.Services;
import org.glassfish.hk2.scopes.PerThread;
import org.glassfish.hk2.scopes.Singleton;
import org.junit.Assert;
import org.junit.Test;
import org.jvnet.hk2.component.PerLookup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * JUnit test for {@link PerThread} scope
 * @author Jerome Dochez
 */
public class PerThreadTest {

    @Test
    public void perThreadTest() throws Exception {

        final Services services = HK2.get().create(null, new Module() {
            @Override
            public void configure(BinderFactory binderFactory) {
                binderFactory.bind().to(PerThread.class).in(Singleton.class);
                binderFactory.bind(SomeContract.class).to(PerThreadService.class).in(PerThread.class);
                binderFactory.bind().to(PerThreadInjectionTarget.class).in(PerLookup.class);
            }
        });

        ExecutorService service = Executors.newFixedThreadPool(5);
        List<Future> futures = new ArrayList<Future>();
        for (int i=0;i<5;i++) {
            futures.add(service.submit(new Callable<Void>() {
                @Override
                public Void call() {
                    PerThreadInjectionTarget target = services.byType(PerThreadInjectionTarget.class).get();
                    return null;
                }
            }));
        }
        for (Future future : futures) future.get();
        service.shutdown();
    }

    @Test
    public void removalTest() throws Exception {
        final Services services = HK2.get().create(null, new Module() {
            @Override
            public void configure(BinderFactory binderFactory) {
                binderFactory.bind().to(PerThread.class).in(Singleton.class);
                binderFactory.bind(SomeContract.class).to(PerThreadService.class).in(PerThread.class);
                binderFactory.bind().to(PerThreadInjectionTarget.class).in(PerLookup.class);
                binderFactory.bind().to(AnotherPerThreadService.class).in(PerThread.class);
            }
        });

        ExecutorService service = Executors.newFixedThreadPool(5);
        Future<Void> future = service.submit(new Callable<Void>() {
            @Override
            public Void call() {

                PerThreadInjectionTarget target = services.byType(PerThreadInjectionTarget.class).get();
                int originalAllocationNumber = target.service.allocationNumber();
                PerThread perThread = services.byType(PerThread.class).get();
                perThread.release();
                // ensure preDestroy was called.
                Assert.assertTrue("preDestroy was not called when scope was released", target.service.preDestroyed);
                target = services.byType(PerThreadInjectionTarget.class).get();
                Assert.assertTrue(target.service.allocationNumber()>originalAllocationNumber);
                return null;
            }
        });
        future.get();
        service.shutdown();
    }

    @Test
    public void severalTest() throws Exception {
        final Services services = HK2.get().create(null, new Module() {
            @Override
            public void configure(BinderFactory binderFactory) {
                binderFactory.bind().to(PerThread.class).in(Singleton.class);
                binderFactory.bind(SomeContract.class).to(PerThreadService.class).in(PerThread.class);
                binderFactory.bind().to(PerThreadInjectionTarget.class).in(PerLookup.class);
                binderFactory.bind().to(AnotherPerThreadService.class).in(PerThread.class);
            }
        });

        ExecutorService service = Executors.newFixedThreadPool(5);
        List<Future> futures = new ArrayList<Future>();
        for (int i=0;i<5;i++) {
            futures.add(service.submit(new Callable<Void>() {
                @Override
                public Void call() {
                    PerThreadInjectionTarget target = services.byType(PerThreadInjectionTarget.class).get();
                    AnotherPerThreadService target2 = services.byType(AnotherPerThreadService.class).get();
                    org.junit.Assert.assertEquals(target.service.perThread(), target2.service.perThread());
                    Assert.assertTrue(target.service.allocationNumber()==target2.service.allocationNumber());
                    return null;
                }
            }));
        }
        for (Future future : futures) future.get();
        service.shutdown();
    }
}
