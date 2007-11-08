/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package com.sun.ejb.base.container;

import java.util.Timer;
import java.util.logging.*;

import com.sun.logging.*;

import com.sun.corba.ee.spi.orbutil.threadpool.Work;
import com.sun.corba.ee.spi.orbutil.threadpool.ThreadPoolManager;
import com.sun.corba.ee.spi.orbutil.threadpool.ThreadPool;
import com.sun.enterprise.util.S1ASThreadPoolManager;
import com.sun.enterprise.util.ORBManager;

import com.sun.ejb.spi.io.J2EEObjectStreamFactory;

public class ContainerServiceImpl
    implements com.sun.ejb.spi.container.ContainerService
{

    private static String J2EE_OBJECT_FACTORY_NAME =
        "com.sun.ejb.base.io.J2EEObjectStreamFactoryImpl";

    private static final Logger _ejbLogger =
        LogDomains.getLogger(LogDomains.EJB_LOGGER);

    private Timer                   timer;
    private J2EEObjectStreamFactory j2eeObjectStreamFactory;

    public ContainerServiceImpl() {
    }
    
    public void initializeService() {
        timer = new Timer(true);

        try {
            Class clazz = Class.forName(J2EE_OBJECT_FACTORY_NAME);
            j2eeObjectStreamFactory = (J2EEObjectStreamFactory) clazz.newInstance();
            _ejbLogger.log(Level.FINE, "Instantiated J2EEObjectStreamFactory");
        } catch (Exception ex) {
            _ejbLogger.log(Level.WARNING, "Couldn't instantiate "
                    + "J2EEObjectstreamFactory", ex);
        }
    }

    public Timer getTimer() {
        return timer;
    }

    public J2EEObjectStreamFactory getJ2EEObjectStreamFactory() {
        return j2eeObjectStreamFactory;
    }

    public void scheduleWork(ClassLoader classLoader, Runnable target) {
        ThreadPoolWork work = new ThreadPoolWork(classLoader, target);

        try {
            ThreadPoolManager threadpoolMgr =
                S1ASThreadPoolManager.getThreadPoolManager();
            ThreadPool threadpool = threadpoolMgr.getDefaultThreadPool();
            threadpool.getAnyWorkQueue().addWork(work);
        } catch (Throwable th) {
            String errMsg =  "Error while adding work to orb threadpool. "
                + "Hence doing it in current thread";
            _ejbLogger.log(Level.WARNING, errMsg, th);
            work.doWork();
        }
    }

    private static class ThreadPoolWork
        implements com.sun.corba.ee.spi.orbutil.threadpool.Work
    {
        private final ClassLoader classLoader;
        private final Runnable    target;
        private long              enqueTime;

        public ThreadPoolWork(ClassLoader classLoader, Runnable target) {
            this.classLoader = classLoader;
            this.target = target;
        }

        public void setEnqueueTime(long timeInMillis) {
            enqueTime = timeInMillis;
        }

        public long getEnqueueTime() {
            return enqueTime;
        }
	
        public void doWork() {
            final ClassLoader prevClassLoader = 
                Thread.currentThread().getContextClassLoader();

            try {
                if(System.getSecurityManager() == null) {
                    Thread.currentThread().setContextClassLoader(classLoader);
                } else {
                    java.security.AccessController.doPrivileged(
                            new java.security.PrivilegedAction() {
                        public java.lang.Object run() {
                            Thread.currentThread().setContextClassLoader(
                                    classLoader);
                            return null;
                        }
                    }
                    );
                }

                target.run();

            } catch (Throwable throwable) {
                _ejbLogger.log(Level.FINE, "Error during execution", throwable);
            } finally {
                if(System.getSecurityManager() == null) {
                    Thread.currentThread().setContextClassLoader(prevClassLoader);
                } else {
                    java.security.AccessController.doPrivileged(
                            new java.security.PrivilegedAction() {
                        public java.lang.Object run() {
                            Thread.currentThread().setContextClassLoader(
                                    prevClassLoader);
                            return null;
                        }
                    }
                    );
                }
            }
        }
        
        public String getName() {
            return "ThreadPoolWork";
        }

    } // end ThreadPoolWork


    public void shutdown() {
        timer = null;
    }

}
