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
package com.sun.enterprise.resource.pool.monitor;

/**
 * An abstract class that houses the common implementations of various probe
 * providers. All probe providers extend this implementation.
 * 
 * @author Shalini M
 */
public abstract class ConnectionPoolProbeProvider {

    /**
     * Emits probe event/notification that the given  connection pool 
     * <code>poolName</code>has got a connection validation failed event.
     * 
     * @param poolName for which connection validation has failed
     * @param increment number of times the validation failed
     */
    public void connectionValidationFailedEvent(String poolName, int increment) {
    }

    /**
     * Emits probe event/notification that a  connection pool with the given
     * name <code>poolName</code> has got a connection timed out event.
     * 
     * @param poolName that has got a connection timed-out event
     */
    public void connectionTimedOutEvent(String poolName) {
    }

    /**
     * Emits probe event/notification that the pool with the given name 
     * <code>poolName</code> is having a potentialConnLeak event.
     * 
     * @param poolName
     */
    public void potentialConnLeakEvent(String poolName) {
    }

    /**
     * Emits probe event/notification that the given  connection pool 
     * <code>poolName</code>has got a decrement free connections size event.
     * 
     * @param poolName for which decrement numConnFree is got
     */
    public void decrementNumConnFreeEvent(String poolName) {
    }

    /**
     * Emits probe event/notification that the given  connection pool 
     * <code>poolName</code>has got a decrement free connections size event.
     * 
     * @param poolName for which decrement numConnFree is got
     * @param beingDestroyed if the connection is destroyed due to error 
     * @param steadyPoolSize 
     */
    public void incrementNumConnFreeEvent(String poolName, boolean beingDestroyed,
            int steadyPoolSize) {
    }

    /**
     * Emits probe event/notification that the given  connection pool 
     * <code>poolName</code>has got a decrement connections used event.
     * 
     * @param poolName for which decrement numConnUsed is got
     */
    public void decrementConnectionUsedEvent(String poolName) {
    }

    /**
     * Emits probe event/notification that the given  connection pool 
     * <code>poolName</code>has got a increment connections used event.
     * 
     * @param poolName for which increment numConnUsed is got
     */
    public void connectionUsedEvent(String poolName) {
    }

    /**
     * Emits probe event/notification that the given  connection pool 
     * <code>poolName</code>has got a increment connections free event.
     * 
     * @param poolName for which increment numConnFree is got
     * @param count number of connections freed to pool
     */
    public void connectionsFreedEvent(String poolName, int count) {
    }

    /**
     * Emits probe event/notification that a connection request is served in the
     * time <code>timeTakenInMillis</code> for the given  connection pool
     * <code> poolName</code> 
     * 
     * @param poolName 
     * @param timeTakenInMillis time taken to serve a connection
     */
    public void connectionRequestServedEvent(String poolName, long timeTakenInMillis) {
    }

    /**
     * Emits probe event/notification that a connection is destroyed for the 
     * given  connection pool <code>poolName</code>
     * 
     * @param poolName 
     */
    public void connectionDestroyedEvent(String poolName) {
    }

    /**
     * Emits probe event/notification that a connection is acquired by application
     * for the given  connection pool <code>poolName</code>
     * 
     * @param poolName
     */
    public void connectionAcquiredEvent(String poolName) {
    }

    /**
     * Emits probe event/notification that a connection is released for the given
     *  connection pool <code>poolName</code>
     * 
     * @param poolName
     */
    public void connectionReleasedEvent(String poolName) {
    }

    /**
     * Emits probe event/notification that a new connection is created for the
     * given  connection pool <code>poolName</code>
     * 
     * @param poolName
     */
    public void connectionCreatedEvent(String poolName) {
    }

    public void toString(String poolName, StringBuffer stackTrace) {
    }

    /**
     * Emits probe event/notification that a connection under test matches the 
     * current request for the given  connection pool <code>poolName</code>
     * 
     * @param poolName
     */
    public void connectionMatchedEvent(String poolName) {
    }

    /**
     * Emits probe event/notification that a connection under test does not 
     * match the current request for the given  connection pool 
     * <code>poolName</code>
     * 
     * @param poolName
     */
    public void connectionNotMatchedEvent(String poolName) {
    }

    /**
     * Emits probe event/notification that the wait queue length has increased 
     * for the given  connection pool <code>poolName</code>
     * 
     * @param poolName
     */
    public void connectionRequestQueuedEvent(String poolName) {
    }

    /**
     * Emits probe event/notification that the wait queue length has decreased 
     * for the given  connection pool <code>poolName</code>
     * 
     * @param poolName
     */
    public void connectionRequestDequeuedEvent(String poolName) {
    }
}
