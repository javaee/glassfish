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

package com.sun.enterprise.connectors.work;

import com.sun.logging.LogDomains;

import javax.resource.spi.work.Work;
import java.util.logging.Logger;
import com.sun.enterprise.connectors.work.context.WorkContextHandler;

/**
 * Represents one piece of work that will be submitted to the workqueue.
 *
 * @author Binod P.G
 */
public final class OneWork implements com.sun.corba.ee.spi.orbutil.threadpool.Work {

    private final Work work;
    private final WorkCoordinator coordinator;
    private long nqTime;
    private WorkContextHandler contextHandler;
    private static final Logger logger =
            LogDomains.getLogger(OneWork.class, LogDomains.RSR_LOGGER);
    private String name = "Resource adapter work";

    /**
     * Creates a work object that can be submitted to a workqueue.
     *
     * @param work Actual work submitted by Resource adapter.
     * @param coordinator <code>WorkCoordinator</code> object.
     */
    OneWork (Work work, WorkCoordinator coordinator, WorkContextHandler contextHandler) {
        this.work = work;
        this.coordinator = coordinator;
        this.contextHandler = contextHandler;
    }

    /**
     * This method is executed by thread pool as the basic work operation.
     */
    public void doWork() {
        coordinator.preInvoke(); // pre-invoke will set work state to "started",
        boolean timedOut = coordinator.isTimedOut();

        // validation of work context should be after this
        //so as to throw WorkCompletedException in case of error.
        if (coordinator.proceed()) {
            try {
                coordinator.setupContext(this);
            } catch (Throwable e) {
                coordinator.setException(e);
            }
        }

        //there may be failure in context setup
        if(coordinator.proceed()){
            try {
                work.run();
            } catch (Throwable t) {
                coordinator.setException(t);
            }
        }

        if(!timedOut){
            coordinator.postInvoke();
        }
    }

    /**
     * Time at which this work is enqueued.
     *
     * @param tme Time in milliseconds.
     */
    public void setEnqueueTime(long tme) {
        this.nqTime = tme;
    }

    /**
     * Retrieves the time at which this work is enqueued
     *
     * @return Time in milliseconds.
     */
    public long getEnqueueTime() {
        return nqTime;
    }

    /**
     * Retrieves the name of the work.
     *
     * @return Name of the work.
     */
    public String getName() {
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    /**
     * Retrieves the string representation of work.
     *
     * @return String representation of work.
     */
    public String toString() {
        return (work.toString());
    }
}
