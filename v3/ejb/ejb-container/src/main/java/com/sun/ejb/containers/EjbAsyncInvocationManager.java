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

import com.sun.ejb.EjbInvocation;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PostConstruct;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.invocation.ComponentInvocation;

/**
 * @author Mahesh Kannan
 */
@Service
public class EjbAsyncInvocationManager
    extends ThreadPoolExecutor {

    private AtomicLong invCounter = new AtomicLong();

    private ConcurrentHashMap<Long, EjbFutureTask> taskMap =
            new ConcurrentHashMap<Long, EjbFutureTask>();

    @Inject
    InvocationManager invMgr;

    public EjbAsyncInvocationManager() {
        //TODO get the paramters from ejb-container config
        super(16, 32, 60, TimeUnit.SECONDS, new LinkedBlockingQueue());
        super.setThreadFactory(new EjbAsyncThreadFactory());
    }

    public boolean isCancelRequestedByCurrentInvocation() {
        boolean result = false;
        ComponentInvocation compInv = (ComponentInvocation) invMgr.getCurrentInvocation();
        if (compInv instanceof EjbInvocation) {
            EjbFutureTask task = taskMap.get(((EjbInvocation) compInv).getInvId());
            if (task != null) {
                result = task.isCancelled();
            }
        }
        return result;
    }

    public FutureTask createFuture(EjbInvocation inv) {
        EjbFutureTask futureTask = new EjbFutureTask(new EjbAsyncTask());
        inv.setEjbFutureTask(futureTask);

        return futureTask;
    }

    public Future submit(EjbInvocation inv) {
        long invId = invCounter.incrementAndGet();
        inv.setInvId(invId);

        //We need to clone this invocation as submitting
        //so that the inv is *NOT* shared between the
        //current thread and the executor service thread
        EjbInvocation asyncInv = inv.clone();
        inv.clearYetToSubmitStatus();
        asyncInv.clearYetToSubmitStatus();

        //TODO: FIXME => We need to propogate SecurityPrincipal
        EjbFutureTask futureTask = asyncInv.getEjbFutureTask();
        futureTask.getEjbAsyncTask().initialize(asyncInv);

        taskMap.put(invId, futureTask);
        return super.submit(futureTask.getEjbAsyncTask());
    }

    /**
     * Ensure that we give out our EjbFutureTask as opposed to JDK's FutureTask
     * @param callable
     * @return a RunnableFuture
     */
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        FutureTask<T> result = null;

        if (callable instanceof EjbAsyncTask) {
            EjbAsyncTask task = (EjbAsyncTask) callable;
            result = task.getFutureTask();
        } else {
            result = new FutureTask(callable);
        }

        return result;
    }

    private static class EjbAsyncThreadFactory
        implements ThreadFactory {

        private AtomicInteger threadId = new AtomicInteger(0);

        public Thread newThread(Runnable r) {
            Thread th = new Thread(r, "Ejb-Async-Thread-" + threadId.incrementAndGet());
            th.setDaemon(true);

            System.out.println("Created thread: " + th);
            th.setContextClassLoader(null); //Prevent any app classloader being set as CCL
            return th;
        }
    }

}
