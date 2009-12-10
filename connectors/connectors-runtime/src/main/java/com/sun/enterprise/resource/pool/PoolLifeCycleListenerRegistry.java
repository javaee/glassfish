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
package com.sun.enterprise.resource.pool;

import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.resource.listener.PoolLifeCycleListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of PoolLifeCycleListener to listen to events related to a 
 * connection pool. The registry allows multiple listeners (ex: pool monitoring)
 * to listen to the pool's lifecyle. Maintains a list of listeners for this pool
 * identified by poolName.
 * 
 * @author shalini
 */
public class PoolLifeCycleListenerRegistry implements PoolLifeCycleListener {

    //List of listeners 
    protected List<PoolLifeCycleListener> poolListenersList;
    
    //name of the pool for which the registry is maintained
    private String poolName;

    public PoolLifeCycleListenerRegistry(String poolName) {
        this.poolName = poolName;
        poolListenersList = new ArrayList<PoolLifeCycleListener>();
    }

    /**
     * Add a listener to the list of pool life cycle listeners maintained by 
     * this registry.
     * @param listener
     */
    public void registerPoolLifeCycleListener(PoolLifeCycleListener listener) {
        poolListenersList.add(listener);
        
        //Check if poolLifeCycleListener has already been set to this. There
        //could be multiple listeners.
        if(!(poolListenersList.size() > 1)) {
            //If the pool is already created, set this registry object to the pool.
            PoolManager poolMgr = ConnectorRuntime.getRuntime().getPoolManager();
            ResourcePool pool = poolMgr.getPool(poolName);
            pool.setPoolLifeCycleListener(this);
        }
    }

    /**
     * Clear the list of pool lifecycle listeners maintained by the registry.
     * This happens when a pool is destroyed so the information about its 
     * listeners need not be stored.
     * @param poolName
     */
    public void unRegisterPoolLifeCycleListener(String poolName) {
        //To make sure the registry is for the given pool name
        if (this.poolName.equals(poolName)) {
            if (poolListenersList != null && !poolListenersList.isEmpty()) {
                //Remove all listeners from this list
                poolListenersList.clear();
            }
        }
        //Its not needed to remove pool life cycle listener from the pool since
        //the pool will already be destroyed.
    }

    public void toString(StringBuffer stackTrace) {
        for (PoolLifeCycleListener listener : poolListenersList) {
            listener.toString(stackTrace);
        }
    }

    public void connectionAcquired() {
        for (PoolLifeCycleListener listener : poolListenersList) {
            listener.connectionAcquired();
        }
    }

    public void connectionRequestServed(long timeTakenInMillis) {
        for (PoolLifeCycleListener listener : poolListenersList) {
            listener.connectionRequestServed(timeTakenInMillis);
        }
    }

    public void connectionTimedOut() {
        for (PoolLifeCycleListener listener : poolListenersList) {
            listener.connectionTimedOut();
        }
    }

    public void connectionNotMatched() {
        for (PoolLifeCycleListener listener : poolListenersList) {
            listener.connectionNotMatched();
        }
    }

    public void connectionMatched() {
        for (PoolLifeCycleListener listener : poolListenersList) {
            listener.connectionMatched();
        }
    }

    public void connectionUsed() {
        for (PoolLifeCycleListener listener : poolListenersList) {
            listener.connectionUsed();
        }
    }

    public void connectionDestroyed() {
        for (PoolLifeCycleListener listener : poolListenersList) {
            listener.connectionDestroyed();
        }
    }

    public void connectionReleased() {
        for (PoolLifeCycleListener listener : poolListenersList) {
            listener.connectionReleased();
        }
    }

    public void connectionCreated() {
        for (PoolLifeCycleListener listener : poolListenersList) {
            listener.connectionCreated();
        }
    }

    public void foundPotentialConnectionLeak() {
        for (PoolLifeCycleListener listener : poolListenersList) {
            listener.foundPotentialConnectionLeak();
        }
    }

    public void connectionValidationFailed(int count) {
        for (PoolLifeCycleListener listener : poolListenersList) {
            listener.connectionValidationFailed(count);
        }
    }

    public void connectionsFreed(int count) {
        for (PoolLifeCycleListener listener : poolListenersList) {
            listener.connectionsFreed(count);
        }
    }

    public void decrementConnectionUsed() {
        for (PoolLifeCycleListener listener : poolListenersList) {
            listener.decrementConnectionUsed();
        }
    }

    public void decrementNumConnFree() {
        for (PoolLifeCycleListener listener : poolListenersList) {
            listener.decrementNumConnFree();
        }
    }
    
    public void incrementNumConnFree(boolean beingDestroyed, int steadyPoolSize) {
        for (PoolLifeCycleListener listener : poolListenersList) {
            listener.incrementNumConnFree(beingDestroyed, steadyPoolSize);
        }
    }

    public void connectionRequestQueued() {
        for (PoolLifeCycleListener listener : poolListenersList) {
            listener.connectionRequestQueued();
        }        
    }

    public void connectionRequestDequeued() {
        for (PoolLifeCycleListener listener : poolListenersList) {
            listener.connectionRequestDequeued();
        }
    }
}