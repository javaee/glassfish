/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.runlevel.tests.blocking2;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.glassfish.hk2.api.ServiceLocator;
import org.junit.Assert;

/**
 * This service has no explicit dependencies, but DOES call
 * getService from the postConstruct, and so it has a
 * hidden dependency on a service that is blocking...
 * 
 * @author jwells
 *
 */
@Singleton
public class SingletonWithSneakyDependency {
    @Inject
    private ServiceLocator locator;
    
    private final static Object lock = new Object();
    private static boolean initialized = false;
    
    @SuppressWarnings("unused")
    @PostConstruct
    private void postConstruct() {
        // This sleep makes sure the BlockingService
        // gets going on the other thread prior to
        // getting invoked
        try {
            Thread.sleep(250L);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        try {
            locator.getService(BlockingService.class);
        }
        catch (Throwable th) {
            th.printStackTrace();
            Assert.fail("Should not have reached here, not rethrowing original exception");
        }
        
        synchronized (lock) {
            initialized = true;
            lock.notifyAll();
        }
        
    }
    
    public static boolean isInitialized(long totalWait) throws InterruptedException {
        synchronized (lock) {
            while (totalWait > 0 && !initialized) {
                long elapsedTime = System.currentTimeMillis();
                lock.wait(totalWait);
                elapsedTime = System.currentTimeMillis() - elapsedTime;
                
                totalWait -= elapsedTime;
            }
            
            return initialized;
        }
    }

}
