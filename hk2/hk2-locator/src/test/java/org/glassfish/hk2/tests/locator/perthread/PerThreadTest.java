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
package org.glassfish.hk2.tests.locator.perthread;

import junit.framework.Assert;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.locator.perlookup.PerLookupModule;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class PerThreadTest {
    private final static String TEST_NAME = "PerThradTest";
    private final static ServiceLocator locator = LocatorHelper.create(TEST_NAME, new PerThreadModule());
    
    private final Object lock = new Object();
    private int numFinished = 0;
    
    @Before
    public void before() {
        ServiceLocatorUtilities.enablePerThreadScope(locator);
        
        // Doing this twice ensures the idempotence of this call
        ServiceLocatorUtilities.enablePerThreadScope(locator);
    }
    
    @Test
    public void testPerThread() throws InterruptedException {
        StoreRunner runner1 = new StoreRunner();
        StoreRunner runner2 = new StoreRunner();
        StoreRunner runner3 = new StoreRunner();
        
        Thread thread1 = new Thread(runner1);
        Thread thread2 = new Thread(runner2);
        Thread thread3 = new Thread(runner3);
        
        thread1.start();
        thread2.start();
        thread3.start();
        
        synchronized (lock) {
            while (numFinished < 3) {
                lock.wait();
            }
        }
        
        ClothingStore store1 = runner1.store;
        ClothingStore store2 = runner2.store;
        ClothingStore store3 = runner3.store;
        
        Pants pants1 = store1.check();
        Pants pants2 = store2.check();
        Pants pants3 = store3.check();
        
        Assert.assertNotSame(pants1, pants2);
        Assert.assertNotSame(pants1, pants3);
        Assert.assertNotSame(pants2, pants3);
    }
    
    
    public class StoreRunner implements Runnable {
        private ClothingStore store;

        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            store = locator.getService(ClothingStore.class);
            
            synchronized (lock) {
                numFinished++;
                lock.notify();
            }
        }
        
    }

}
