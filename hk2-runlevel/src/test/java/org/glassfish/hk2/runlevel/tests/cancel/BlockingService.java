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
package org.glassfish.hk2.runlevel.tests.cancel;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.glassfish.hk2.runlevel.RunLevel;

/**
 * The postConstruct method blocks until the test tells it to go
 * (which may be never)
 * @author jwells
 *
 */
@RunLevel(5)
public class BlockingService {
    private final static Object lock = new Object();
    private static boolean go = false;
    
    private final static Object postConstructLock = new Object();
    private static boolean postConstructCalled = false;
    
    private static boolean preDestroyCalled = false;
    
    /**
     * Will block until test tells it to go
     */
    @PostConstruct
    public void postConstruct() {
        synchronized (postConstructLock) {
            postConstructCalled = true;
            postConstructLock.notifyAll();
        }
        
        synchronized (lock) {
            while (!go) {
                try {
                   lock.wait();
                }
                catch (InterruptedException ie) {
                    throw new RuntimeException(ie);
                }
            }
        }
        
    }
    
    /**
     * Ensures that if this came up, it also went down
     */
    @PreDestroy
    public void preDestroy() {
        preDestroyCalled = true;
    }
    
    public static boolean getPreDestroyCalled() {
        return preDestroyCalled;
    }
    
    /**
     * Done so the test can be sure the postConstruct has been called
     * 
     * @throws InterruptedException
     */
    public static void waitForPostConstruct() throws InterruptedException {
        synchronized (postConstructLock) {
            while (!postConstructCalled) {
                postConstructLock.wait();
            }
        }
    }
    
    public static boolean getPostConstructCalled() {
        synchronized (postConstructLock) {
            return postConstructCalled;
        }
    }
    
    /**
     * Tells service to go ahead
     */
    public static void go() {
        synchronized (lock) {
            go = true;
            lock.notifyAll();
        }
    }
    
    /**
     * Tells service to stop in the postConstruct
     */
    public static void clear() {
        synchronized (lock) {
            go = false;
        }
        
        synchronized (postConstructLock) {
            postConstructCalled = false;
        }
        
        preDestroyCalled = false;
    }
}
