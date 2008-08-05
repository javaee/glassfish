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

/**
 * @author Mahesh Kannan
 */
public class SingletonContainer
        extends StatelessSessionContainer {

    private SessionContextFactory factory;

    private transient ComponentContext singletonCtx;

    private AtomicInteger invCount = new AtomicInteger(0);

    private boolean bmcMode = true;

    public SingletonContainer(EjbDescriptor desc, ClassLoader cl)
            throws Exception {
        super(ContainerType.SINGLETON, desc, cl);

        System.out.println("****** [SINGLETON CONTAINER CREATED] for: " + desc.getEjbClassName());
    }

    public ComponentContext instantiateSingletonInstance() {
        if (singletonCtx == null) {
            factory = new SessionContextFactory();
            singletonCtx = (ComponentContext) factory.create(null);
        }

        return singletonCtx;
    }

    protected ComponentContext _getContext(EjbInvocation inv) {
        if (singletonCtx == null) {
            synchronized (this) {
                if (singletonCtx == null) {
                    instantiateSingletonInstance();
                }
            }
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
            factory.destroy(singletonCtx);
            super.undeploy();

            System.out.println("****** [SINGLETON CONTAINER UNDEPLOYED] for: " + ejbDescriptor.getEjbClassName());
        } catch (Throwable th) {
            _logger.log(Level.INFO, "Error during SingletonContainer undeploy", th);
        }
    }
}
