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

package com.sun.ejb.containers;

import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.ejb.ComponentContext;
import com.sun.ejb.EjbInvocation;

import javax.transaction.Transaction;
import java.util.logging.Level;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;

import org.glassfish.ejb.startup.SingletonLifeCycleManager;

/**
 * @author Mahesh Kannan
 */
public class SingletonContainer
        extends StatelessSessionContainer {

    private SessionContextFactory factory;

    private AtomicBoolean singletonInitialized = new AtomicBoolean(false);

    private volatile ComponentContext singletonCtx;

    private AtomicInteger invCount = new AtomicInteger(0);

    private AtomicBoolean onHold = new AtomicBoolean(true);

    private boolean bmcMode = true;

    private SingletonLifeCycleManager lcm;

    public SingletonContainer(EjbDescriptor desc, ClassLoader cl)
            throws Exception {
        super(ContainerType.SINGLETON, desc, cl);

        System.out.println("****** [SINGLETON CONTAINER CREATED] for: " + desc.getEjbClassName());
    }

    public void setSingletonLifeCycleManager(SingletonLifeCycleManager lcm) {
        this.lcm = lcm;
    }

    //Called from SingletonLifeCycleManager
    public ComponentContext instantiateSingletonInstance() {
        if (! singletonInitialized.get()) {
            synchronized (this) {
                if (! singletonInitialized.get()) {
                    factory = new SessionContextFactory();

                    //The following may throw exception
                    singletonCtx = (ComponentContext) factory.create(null);
                    singletonInitialized.set(true);
                }
            }
        }

        return singletonCtx;
    }

    @Override
    protected void createBeanPool() {
        //No-op
    }

    @Override
    public void doAfterApplicationDeploy() {
        super.doAfterApplicationDeploy();
        synchronized (onHold) {
            onHold.set(false);
            onHold.notifyAll();
        }

        //Now _getContext can proceed
    }

    protected ComponentContext _getContext(EjbInvocation inv) {
        //Concurrent access possible here and that too
        //  even before the Singleton (and its dependencies)
        //  are initialized

        /*
        if (onHold.get()) {
            synchronized (onHold) {
                if (onHold.get()) {
                    try {
                        onHold.wait();
                    } catch (InterruptedException inEx) {
                        //Ignore
                    }
                }
            }
        }
        */

        if (! singletonInitialized.get()) {
            //Note: NEVER call instantiateSingletonInstance() directly from here
            // The following starts all dependent beans as well
            //
            //Also, it is OK to call the following by concurrent threads
            lcm.initializeSingleton(this);
        }

        if (bmcMode) {
            synchronized (invCount) {
                invCount.incrementAndGet();
                ((SessionContextImpl) singletonCtx).setState(EJBContextImpl.BeanState.INVOKING);
            }
        }
        
        //For now return this as we support only BMC
        return singletonCtx;
    }

    public void releaseContext(EjbInvocation inv) {
        if (bmcMode) {
            synchronized (invCount) {
                int val = invCount.decrementAndGet();
                if (val == 0) {
                    ((SessionContextImpl) singletonCtx).setState(EJBContextImpl.BeanState.READY);
                }
            }
        }
    }

    protected void checkUnfinishedTx(Transaction prevTx, EjbInvocation inv) {

    }

    protected void forceDestroyBean(EJBContextImpl sc) {
        //Should not destroy the instance
    }

    public void undeploy() {
        try {
            String beanName = ejbDescriptor.getEjbClassName();
            if (factory != null) {
                factory.destroy(singletonCtx);
            }
            super.undeploy();

            System.out.println("****** [SINGLETON CONTAINER UNDEPLOYED] for: " + beanName);
        } catch (Throwable th) {
            _logger.log(Level.INFO, "Error during SingletonContainer undeploy", th);
        }
    }

}
