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
package com.sun.enterprise.resource;

import java.util.*;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnection;
import javax.transaction.*;
import java.util.logging.*;
import com.sun.logging.*;

import javax.naming.Context;
import javax.naming.NamingException;
import com.sun.enterprise.Switch;
import com.sun.enterprise.connectors.*;
import com.sun.enterprise.distributedtx.*;
import com.sun.enterprise.util.i18n.StringManager;

/**
 * this resource pool does not allow sharing
 * A resource is only given out if it is not used by
 * any enterprise bean and it does not have any pending transaction
 *
 * @author Aditya Gore
 * @since 9.0
 */
public abstract class AbstractResourcePool implements MonitorableResourcePool {
    
    protected final static StringManager localStrings =
            StringManager.getManager(AbstractResourcePool.class);
    
    protected ArrayList<ResourceHandle> resources;
    
    /**
     * list of ResourceHandles that are free and unenlisted
     */
    protected ArrayList<ResourceHandle> free;
    
    // time (in ms) before destroying a free resource
    protected long idletime;
    
    protected String name;
    
    // adding resourceSpec and allocator
    protected ResourceSpec resourceSpec;
    // Note: This resource allocator may not be the same as the allocator
    //          passed in to getResource()
    protected ResourceAllocator allocator;
    
    //config properties
    
    protected int maxPoolSize;           // Max size of the pool
    
    protected int steadyPoolSize;        // Steady size of the pool
    
    protected int resizeQuantity;        // used by resizer to downsize the pool
    
    protected int maxWaitTime;           // The total time a thread is willing to wait
    //  for a resource object.
    
    protected boolean failAllConnections = false;
    
    protected boolean matchConnections = false;
    
    protected boolean poolInitialized = false;
    
    protected Timer timer;
    
    // hold on to the resizer task so we can cancel/reschedule it.
    protected TimerTask resizerTask;
    
    protected boolean monitoringEnabled; //Indicates if monitoring is on
    
    protected PoolCounters poolCounters = null;
    
    protected boolean validation = false;
    /**
     * given a Transaction, get a Set of resources enlisted in that
     * transaction (Transaction -> Set of ResourceHandle)
     */
    
    protected final LinkedList waitQueue = new LinkedList();

      //Commented from 9.1 as it is not used
/*
    protected boolean lazyConnectionAssoc_;
    protected boolean lazyConnectionEnlist_;
    protected boolean associateWithThread_;
*/
    protected boolean connectionLeakTracing_;
    
    //introduced in 9.1
    protected long connectionLeakTimeoutInMilliSeconds_;
    protected boolean connectionLeakReclaim_;
    protected boolean connectionCreationRetry_;
    protected int connectionCreationRetryAttempts_;
    protected long conCreationRetryInterval_ ;
    protected long validateAtmostPeriodInMilliSeconds_;
    protected int maxConnectionUsage_ ;
    
    //Lock and HashMap to trace connection leaks
    private final Object connectionLeakLock;
    private HashMap<ResourceHandle, StackTraceElement[]> connectionLeakThreadStackHashMap;
    private HashMap<ResourceHandle, ConnectionLeakTask> connectionLeakTimerTaskHashMap;

    //Commented from 9.1 as it is not used
    /*//System property to be used in case connection pooling is switched off in ACC
    private static final String Switch_Off_ACC_Connection_Pooling =
        "com.sun.enterprise.Connectors.SwitchoffACCConnectionPooling";
    private static String switchoffACCConnectionPooling = System.getProperty(Switch_Off_ACC_Connection_Pooling);*/

    private boolean selfManaged_;
    
    //To validate a Sun RA Pool Connection if it hasnot been validated in the past x sec. (x=idle-timeout)
    //The property will be set from system property - com.sun.enterprise.connectors.ValidateAtmostEveryIdleSecs=true
    private boolean validateAtmostEveryIdleSecs = false;
    

      //Commented from 9.1 as it is not used
   /* private boolean inTx() {
        com.sun.enterprise.ComponentInvocation inv =
                Switch.getSwitch().getInvocationManager().getCurrentInvocation();
        if (inv == null) {
            throw new com.sun.enterprise.InvocationException();
        }
        Transaction tran = inv.getTransaction();
        return (tran != null);
    }*/

    public AbstractResourcePool(String poolName) throws PoolingException{
        this.name = poolName;
        setPoolConfiguration();
        monitoringEnabled = false;
        resources = new ArrayList<ResourceHandle>(maxPoolSize);
        free = new ArrayList<ResourceHandle>(maxPoolSize);
        connectionLeakThreadStackHashMap = new HashMap<ResourceHandle, StackTraceElement[]>();
        connectionLeakTimerTaskHashMap = new HashMap<ResourceHandle, ConnectionLeakTask>();
        connectionLeakLock = new Object();
    }
    
    protected final static Logger _logger = LogDomains.getLogger(LogDomains.RSR_LOGGER);

    private void setPoolConfiguration() throws PoolingException {
        Context ic = Switch.getSwitch().getNamingManager().getInitialContext();
        ConnectorConnectionPool poolResource;
        try {
            String jndiNameOfPool = ConnectorAdminServiceUtils.
                    getReservePrefixedJNDINameForPool(name);
            poolResource = (ConnectorConnectionPool) ic.lookup(jndiNameOfPool);
        } catch (NamingException ex) {
            throw new PoolingException(ex);
        }
        idletime = Integer.parseInt(poolResource.getIdleTimeoutInSeconds()) * 1000;
        maxPoolSize = Integer.parseInt(poolResource.getMaxPoolSize());
        steadyPoolSize = Integer.parseInt(poolResource.getSteadyPoolSize());

        if (maxPoolSize < steadyPoolSize) {
            maxPoolSize = steadyPoolSize;
        }
        resizeQuantity = Integer.parseInt(poolResource.getPoolResizeQuantity());

        maxWaitTime = Integer.parseInt(poolResource.getMaxWaitTimeInMillis());
        //Make sure it's not negative.
        if (maxWaitTime < 0) {
            maxWaitTime = 0;
        }

        failAllConnections = poolResource.isFailAllConnections();

        validation = poolResource.isIsConnectionValidationRequired();

        validateAtmostEveryIdleSecs = poolResource.isValidateAtmostEveryIdleSecs();

        setAdvancedPoolConfiguration(poolResource);
    }

    // This method does not need to be synchronized since all caller methods are,
    //  but it does not hurt. Just to be safe.
    protected synchronized void initPool(ResourceSpec resourceSpec,
            ResourceAllocator allocator)
            throws PoolingException {
        
        if (poolInitialized) {
            return;
        }
        
        this.resourceSpec = resourceSpec;
        this.allocator = allocator;
        
        createResources(this.allocator, steadyPoolSize);
        
        // if the idle time out is 0, then don't schedule the resizer task
        if (idletime > 0) {
            scheduleResizerTask();
        }
        poolInitialized = true;
    }
    
    /**
     * Schedules the resizer timer task. If a task is currently scheduled,
     * it would be canceled and a new one is scheduled.
     */
    private void scheduleResizerTask() {
        if (resizerTask != null) {
            //cancel the current task
            resizerTask.cancel();
            resizerTask = null;
        }
        
        resizerTask = new Resizer();
        
        if (timer == null){
            timer = Switch.getSwitch().getTimer();
        }
        
        timer.scheduleAtFixedRate(resizerTask, idletime, idletime);
        if (_logger.isLoggable( Level.FINEST ) ) {
            _logger.finest("schduled resizer task");
        }
    }


    /**
     * add a resource with status busy and not enlisted
     */
    synchronized public void addResource(ResourceSpec spec,
                                         ResourceHandle h) {
        if (_logger.isLoggable( Level.FINE ) ) {
            _logger.log(Level.FINE,"Pool: resource added: " + spec + "," + h);
        }
        // all resources in this pool have the same resource spec
        ResourceState state = new ResourceState();
        resources.add(h);
        h.setResourceState(state);
        state.setEnlisted(false);
        setResourceStateToBusy(h);
    }
    
    /**
     * marks resource as free. This method should be used instead of directly calling 
     * resoureHandle.getResourceState().setBusy(false)
     * OR
     * getResourceState(resourceHandle).setBusy(false)
     * as this method handles stopping of connection leak tracing
     * If connection leak tracing is enabled, takes care of stopping
     * connection leak tracing
     */
    protected void setResourceStateToFree(ResourceHandle resourceHandle){
        getResourceState(resourceHandle).setBusy(false);
        if(connectionLeakTracing_)
            stopConnectionLeakTracing(resourceHandle);
    }
    
    /**
     * marks resource as busy. This method should be used instead of directly calling 
     * resoureHandle.getResourceState().setBusy(true)
     * OR
     * getResourceState(resourceHandle).setBusy(true)
     * as this method handles starting of connection leak tracing
     * If connection leak tracing is enabled, takes care of starting
     * connection leak tracing
     */
    protected void setResourceStateToBusy(ResourceHandle resourceHandle){
        getResourceState(resourceHandle).setBusy(true);
        if(connectionLeakTracing_)
            startConnectionLeakTracing(resourceHandle);
    }
    
    /**
     * starts connection leak tracing
     */
    private void startConnectionLeakTracing(ResourceHandle resourceHandle) {
        synchronized(connectionLeakLock){
            if(!connectionLeakThreadStackHashMap.containsKey(resourceHandle)){
                connectionLeakThreadStackHashMap.put(resourceHandle, Thread.currentThread().getStackTrace());
                ConnectionLeakTask connectionLeakTask = new ConnectionLeakTask(resourceHandle);
                connectionLeakTimerTaskHashMap.put(resourceHandle, connectionLeakTask);
                if(timer == null)
                    timer = Switch.getSwitch().getTimer();
                timer.schedule(connectionLeakTask, connectionLeakTimeoutInMilliSeconds_);
            }
        }
    }
    
    /**
     * stops connection leak tracing
     */
    private void stopConnectionLeakTracing(ResourceHandle resourceHandle) {
        synchronized(connectionLeakLock){
            if(connectionLeakThreadStackHashMap.containsKey(resourceHandle)){
                connectionLeakThreadStackHashMap.remove(resourceHandle);
                ConnectionLeakTask connectionLeakTask = connectionLeakTimerTaskHashMap.remove(resourceHandle);
                connectionLeakTask.cancel();
                timer.purge();
            }
        }
    }
    
    /**
     * Logs the potential connection leaks
     */
    private void potentialConnectionLeakFound(ResourceHandle resourceHandle) {
        synchronized(connectionLeakLock){
            if(connectionLeakThreadStackHashMap.containsKey(resourceHandle)){
                StackTraceElement[] threadStack = connectionLeakThreadStackHashMap.remove(resourceHandle);
                printConnectionLeakTrace(threadStack);
                connectionLeakTimerTaskHashMap.remove(resourceHandle);
                if(connectionLeakReclaim_)
                    freeResource(resourceHandle);
                if(monitoringEnabled)
                    poolCounters.incrementNumPotentialConnLeak();
            }
        }
    }
    
    /**
     * Prints the stack trace of thread leaking connection to server logs
     */
    private void printConnectionLeakTrace(StackTraceElement[] threadStackTrace){
        StringBuffer stackTrace = new StringBuffer();
        String msg = localStrings.getStringWithDefault(
                "potential.connection.leak.msg",
                "A potential connection leak detected for connection pool " + name +
                ". The stack trace of the thread is provided below : ",
                new Object[]{name});
        stackTrace.append(msg + "\n");
        for(int i=2; i < threadStackTrace.length; i++)
            stackTrace.append(threadStackTrace[i].toString() + "\n");
        if(monitoringEnabled){
            msg = localStrings.getStringWithDefault("monitoring.statistics", "Monitoring Statistics :");
            stackTrace.append("\n" + msg + "\n");
            stackTrace.append(poolCounters.toString());
        }
        _logger.log(Level.WARNING, stackTrace.toString(), "ConnectionPoolName=" + name);
    }
    
    /**
     * Clear all connection leak tracing tasks in case of connection leak
     * tracing being turned off
     */
    private void clearAllConnectionLeakTasks(){
        synchronized(connectionLeakLock){
            Iterator<ResourceHandle> iter = connectionLeakTimerTaskHashMap.keySet().iterator();
            while(iter.hasNext()){
                ResourceHandle resourceHandle = iter.next();
                ConnectionLeakTask connectionLeakTask = connectionLeakTimerTaskHashMap.get(resourceHandle);
                connectionLeakTask.cancel();
            }
            if(timer != null)
                timer.purge();
            connectionLeakThreadStackHashMap.clear();
            connectionLeakTimerTaskHashMap.clear();
        }
    }
    
    /**
     * returns resource from the pool.
     *
     * @return a free pooled resource object matching the ResourceSpec
     *
     * @throws PoolingException
     *          - if any error occurrs
     *          - or the pool has reached its max size and the
     *                  max-connection-wait-time-in-millis has expired.
     */
    public ResourceHandle getResource(ResourceSpec spec,
            ResourceAllocator alloc,
            Transaction tran) throws PoolingException {
        //Note: this method should not be synchronized or the
        //      startTime would be incorrect for threads waiting to enter
        
        /*
         * Here are all the comments for the method put togethar for
         * easy reference.
         *  1.
            // - Try to get a free resource. Note: internalGetResource()
            // will create a new resource if none is free and the max has
            // not been reached.
            // - If can't get one, get on the wait queue.
            // - Repeat this until maxWaitTime expires.
            // - If maxWaitTime == 0, repeat indefinitely.
         
            2.
            //the doFailAllConnectionsProcessing method would already
            //have been invoked by now.
            //We simply go ahead and create a new resource here
            //from the allocator that we have and adjust the resources
            //list accordingly so as to not exceed the maxPoolSize ever
            //(i.e if steadyPoolSize == maxPoolSize )
            ///Also since we are creating the resource out of the allocator
            //that we came into this method with, we need not worry about
            //matching
         */
        ResourceHandle result = null;
        
        long startTime = 0;
        long elapsedWaitTime = 0;
        long remainingWaitTime = 0;
        
        Object waitMonitor = new Object();
        
        if (maxWaitTime > 0) {
            startTime = System.currentTimeMillis();
        }
        
        while (true) {
            //See comment #1 above
            result = internalGetResource(spec, alloc, tran);
            
            if (result != null) {
                // got one, return it
                if (monitoringEnabled) {
                    poolCounters.incrementNumConnAcquired();
                    elapsedWaitTime = System.currentTimeMillis() - startTime;
                    poolCounters.setWaitTime(elapsedWaitTime);
                }
                
                
                //got one - seems we are not doing validation or matching
                //return it
                break;
            } else {
                // did not get a resource.
                if (maxWaitTime > 0) {
                    elapsedWaitTime = System.currentTimeMillis() - startTime;
                    if (elapsedWaitTime < maxWaitTime) {
                        // time has not expired, determine remaining wait time.
                        remainingWaitTime = maxWaitTime - elapsedWaitTime;
                    } else {
                        // wait time has expired
                        if (monitoringEnabled) {
                            poolCounters.incrementNumConnTimedOut();
                        }
                        String msg = localStrings.getStringWithDefault(
                                "poolmgr.no.available.resource",
                                "No available resource. Wait-time expired.");
                        throw new PoolingException(msg);
                    }
                }
                
                synchronized (waitMonitor) {
                    synchronized (waitQueue) {
                        waitQueue.addLast(waitMonitor);
                    }
                    try {
                        if (_logger.isLoggable( Level.FINE) ) {
                            _logger.log(Level.FINE,"Resource Pool: getting on wait queue");
                        }
                        //numConnWaited++;
                        waitMonitor.wait(remainingWaitTime);
                        
                    } catch (InterruptedException ex) {
                        //Could be system shutdown.
                        break;
                    }
                    
                    //try to remove in case that the monitor has timed
                    // out.  We dont expect the queue to grow to great numbers
                    // so the overhead for removing inexistant objects is low.
                    synchronized( waitQueue ) {
                        waitQueue.remove(waitMonitor);
                    }
                }
            }
        }
        
        alloc.fillInResourceObjects(result);
        return result;
    }
    
    //Overridden in AssocWithThreadResourcePool to fetch the resource
    //cached in the ThreadLocal
    //In SJSASResourcePool this simply returns null
    protected abstract ResourceHandle prefetch( ResourceSpec spec,
            ResourceAllocator alloc, Transaction tran );
    
    protected ResourceHandle internalGetResource(ResourceSpec spec,
            ResourceAllocator alloc,
            Transaction tran) throws PoolingException {
        if (!poolInitialized) {
            initPool(spec, alloc);
        }
        ResourceHandle result = null;
        
        result = prefetch( spec, alloc, tran );
        if ( result != null ) {
            return result;
        }
        
        try {
            //comment-1: sharing is possible only if caller is marked
            //shareable, so abort right here if that's not the case
            if (tran != null && alloc.shareableWithinComponent() ) {
                J2EETransaction j2eetran = (J2EETransaction) tran;
                // case 1. look for free and enlisted in same tx
                Set set =  j2eetran.getResources(name);
                if (set != null) {
                    Iterator iter = set.iterator();
                    while (iter.hasNext()) {
                        ResourceHandle h = (ResourceHandle) iter.next();
                        if (h.hasConnectionErrorOccurred()) {
                            iter.remove();
                            continue;
                        }
                        
                        ResourceState state = h.getResourceState();
                        /*
                         * One can share a resource only for the following conditions:
                         * 1. The caller resource is shareable (look at the outermost
                         *    if marked comment-1
                         * 2. The resource enlisted inside the transaction is shareable
                         * 3. We are dealing with XA resources OR
                         *    We are deling with a non-XA resource that's not in use
                         *    Note that sharing a non-xa resource that's in use involves
                         *    associating physical connections.
                         * 4. The credentials of the resources match
                         */
                        if (h.getResourceAllocator().shareableWithinComponent()) {
                            if (spec.isXA() || isNonXAResourceAndFree(j2eetran, h)) {
                                if (matchConnections) {
                                    if (!alloc.matchConnection(h)) {
                                        if (monitoringEnabled) {
                                            poolCounters.incrementNumConnNotSuccessfullyMatched();
                                        }
                                        continue;
                                    }
                                    if (h.hasConnectionErrorOccurred()) {
                                        if (failAllConnections) {
                                            //if failAllConnections has happened, we flushed the
                                            //pool, so we don't have to do iter.remove else we
                                            //will get a ConncurrentModificationException
                                            result = null;
                                            break;
                                        }
                                        iter.remove();
                                        continue;
                                    }
                                    if (monitoringEnabled) {
                                        poolCounters.incrementNumConnSuccessfullyMatched();
                                    }
                                }
                                if (state.isFree())
                                    setResourceStateToBusy(h);
                                result = h;
                                break;
                            }
                        }
                    }
                }
            }
        } catch(ClassCastException e) {
            _logger.log(Level.FINE, "Pool: getResource : " +
                    "transaction is not J2EETransaction but a " + tran.getClass().getName() , e);
        }
        
        
        // We didnt get a connections that is already enlisted in a transaction.
        if (result == null) {
            result = getUnenlistedResource(spec, alloc, tran);
            //Only getting of an unenlisted resource results in
            //an increment of the connection used stat
            if (result != null){

                if(maxConnectionUsage_ > 0){
	                result.incrementUsageCount();
                }
                if(monitoringEnabled) {
                    poolCounters.incrementNumConnUsed();
                }
            }
        }
        return result;
        
    }

       /**
        * Check whether the resource is non-xa
        * @param resource Resource to be verified
        * @return boolean indicating whether the resource is non-xa
        */
        private boolean isNonXAResource(ResourceHandle resource){
            return !resource.getResourceSpec().isXA() ;
        }

        /**
        * Check whether the non-xa resource is enlisted in transaction.
        * @param tran Transaction
        * @param resource Resource to be verified
        * @return boolean indicating whether thegiven non-xa  resource is in transaction
        */
        private boolean isNonXAResourceInTransaction(J2EETransaction tran, ResourceHandle resource){

           return  resource.equals(tran.getNonXAResource());
        }

        /**
        * Check whether the resource is non-xa, free and is enlisted in transaction.
        * @param tran Transaction
        * @param resource Resource to be verified
        * @return boolean indicating whether the resource is free, non-xa and is enlisted in transaction
        */
        private boolean isNonXAResourceAndFree(J2EETransaction tran, ResourceHandle resource){
          return resource.getResourceState().isFree() &&  isNonXAResource(resource) && isNonXAResourceInTransaction(tran, resource);
        }

    /**
     * To provide an unenlisted,  valid, matched resource from pool.
     * @param spec ResourceSpec
     * @param alloc ResourceAllocator
     * @param tran Transaction
     * @return ResourceHandle resource from pool
     * @throws PoolingException  Exception while getting resource from pool
     */
    protected ResourceHandle getUnenlistedResource(ResourceSpec spec,ResourceAllocator alloc,
                                            Transaction tran) throws PoolingException{
        ResourceHandle resource ;
        while((resource=getResourceFromPool(spec, alloc, tran))!=null){
        boolean isValid = isConnectionValid(resource, alloc);

            if (resource.hasConnectionErrorOccurred() || !isValid) {
                synchronized (this) {
                    if (failAllConnections) {
                        resource = createSingleResourceAndAdjustPool(alloc, spec);
                        //no need to match since the resource is created with the allocator of caller.
                        break;
                    } else {
                        resources.remove(resource);
                        destroyResource(resource);
                        //resource is invalid, continue iteration.
                        continue;
                    }
                }
            }
            // got a matched, valid resource
            break;
        }
        return resource;
    }

     /**
     * Check whether the connection is valid
     * @param h     Resource to be validated
     * @param alloc Allocator to validate the resource
     * @return boolean representing validation result
     */
    private boolean isConnectionValid(ResourceHandle h, ResourceAllocator alloc) {
         boolean connectionValid = true;

         if (validation || validateAtmostEveryIdleSecs) {
             long validationPeriod;
             //validation period is idle timeout if validateAtmostEveryIdleSecs is set to true
             //else it is validateAtmostPeriodInMilliSeconds_
             if (validation)
                 validationPeriod = validateAtmostPeriodInMilliSeconds_;
             else
                 validationPeriod = idletime;
             boolean validationRequired = true;
             long currentTime = h.getLastValidated();
             if (validationPeriod > 0) {
                 currentTime = System.currentTimeMillis();
                 long timeSinceValidation = currentTime - h.getLastValidated();
                 if (timeSinceValidation < validationPeriod)
                     validationRequired = false;
             }
             if (validationRequired) {
                 if (!alloc.isConnectionValid(h)) {
                     connectionValid = false;
                     incrementNumConnFailedValidation();
                 } else {
                     h.setLastValidated(currentTime);
                 }
             }
         }
         return connectionValid;
     }

    /**
     * check whether the connection retrieved from the pool matches with the request.
     *
     * @param resource Resource to be matched
     * @param alloc    ResourceAllocator used to match the connection
     * @return boolean representing the match status of the connection
     */
    protected boolean matchConnection(ResourceHandle resource, ResourceAllocator alloc) {
        boolean matched = true;
        if (matchConnections) {
            matched = alloc.matchConnection(resource);
            if (monitoringEnabled) {
                if (matched) {
                    poolCounters.incrementNumConnSuccessfullyMatched();
                } else {
                    poolCounters.incrementNumConnNotSuccessfullyMatched();
                }
            }
        }
        return matched;
    }

    /**
     * return resource in free list. If none is found, try to scale up the pool/purge pool and <br>
     * return a new resource. returns null if the pool new resources cannot be created. <br>
     * @param spec ResourceSpec
     * @param alloc ResourceAllocator
     * @param tran Transaction
     * @return ResourceHandle resource from pool
     * @throws PoolingException if unable to create a new resource
     */
    synchronized protected ResourceHandle getResourceFromPool(ResourceSpec spec,
            ResourceAllocator alloc,
            Transaction tran) throws PoolingException {
        
        // the order of serving a resource request
        // 1. free and enlisted in the same transaction
        // 2. free and unenlisted
        // Do NOT give out a connection that is
        // free and enlisted in a different transaction
        ResourceHandle result = null;
        
        Iterator iter = free.iterator();
        while (iter.hasNext()) {
            ResourceHandle h = (ResourceHandle) iter.next();
            if (h.hasConnectionErrorOccurred()) {
                iter.remove();
                continue;
            }
            // This check does not seem to be needed. 
            // TODO: Evaluate and remove this check
            //h.isAssociated can only be true for
            //AssocWithThreadResourcePool
            if ( h.isAssociated() ) {
                //Don't touch it then
                continue;
            }

            if (matchConnection(h, alloc)) {
                result = h;
                break;
            }
        }
        
        if (result != null) {
            // set correct state
            setResourceStateToBusy(result);
            free.remove(result);
        } else {
            result = resizePoolAndGetNewResource(alloc);
        }
        return result;
    }

    /**
     * Scale-up the pool to serve the new request. <br>
     * If pool is at max-pool-size and free resources are found, purge unmatched<br>
     * resources, create new connections and serve the request.<br>
     *
     * @param alloc ResourceAllocator used to create new resources
     * @return ResourceHandle newly created resource
     * @throws PoolingException when not able to create resources
     */
    private ResourceHandle resizePoolAndGetNewResource(ResourceAllocator alloc) throws PoolingException {
        //Must be called from the thread holding the lock to this pool.
        ResourceHandle result = null ;
        int numOfConnsToCreate = 0;
        if (resources.size() < steadyPoolSize) {
            // May be all invalid resources are destroyed as
            // a result no free resource found and no. of resources is less than steady-pool-size
            numOfConnsToCreate = steadyPoolSize - resources.size();
        } else if (resources.size() + resizeQuantity <= maxPoolSize) {
            //Create and add resources of quantity "resizeQuantity"
            numOfConnsToCreate = resizeQuantity;
        } else if (resources.size() < maxPoolSize) {
            // This else if "test condition" is not needed. Just to be safe.
            // still few more connections (less than "resizeQuantity" and to reach the count of maxPoolSize)
            // can be added
            numOfConnsToCreate = maxPoolSize - resources.size();
        }
        if (numOfConnsToCreate > 0) {
            createResources(alloc, numOfConnsToCreate);

            int newResourcesIndex = free.size() - numOfConnsToCreate;
            result =  free.remove(newResourcesIndex);
            setResourceStateToBusy(result);
        }else if(free.size() > 0){
            //pool cannot create more connections as it is at max-pool-size.
            //If there are free resources at max-pool-size, then none of the free resources
            //has matched this allocator's request (credential). Hence purge free resources
            //of size <=resizeQuantity
            purgeResources(resizeQuantity);
            result = resizePoolAndGetNewResource(alloc);
        }
        return result;
}

    /**
     * Try to purge resources by size <=  quantity <br>
     * @param quantity maximum no. of resources to remove. <br>
     * @return resourceCount No. of resources actually removed. <br>
     */
    private int purgeResources(int quantity){
        //Must be called from the thread holding the lock to this pool.
        int freeResourcesCount = free.size();
        int resourcesCount = (freeResourcesCount>=quantity) ?
                quantity : freeResourcesCount;
        if(_logger.isLoggable(Level.FINE)){
            _logger.log(Level.FINE, "Purging resources of size : " + resourcesCount);
        }
        for(int i=resourcesCount-1; i>=0;i--){
            ResourceHandle resource = free.remove(i);
            resources.remove(resource);
            destroyResource(resource);
        }
        return resourcesCount;
    }

    /**
     * This method will be called from the getUnenlistedResource method if
     * we detect a failAllConnection flag.
     * Here we simply create a new resource and replace a free resource in
     * the pool by this resource and then give it out.
     * This replacement is required since the steadypoolsize might equal
     * maxpoolsize and in that case if we were not to remove a resource
     * from the pool, our resource would be above maxPoolSize
     */
    protected ResourceHandle createSingleResourceAndAdjustPool(
            ResourceAllocator alloc, ResourceSpec spec)
            throws PoolingException {
        if ( free.size() != 0 ) {
            //resources.size() could be 0 if we were to run into
            //trouble while createResources(0 was called in
            //doFailAllConnectionsProcessing
            ResourceHandle rHandle =  free.get(0);
            resources.remove( rHandle );
            free.remove( rHandle );
        }
        ResourceHandle result = createSingleResource(alloc);
        addResource( spec, result );
        alloc.fillInResourceObjects( result );
        if ( monitoringEnabled ) {
            poolCounters.incrementNumConnCreated();
        }
        
        return result;
        
    }
    
    /**
     * Method to be used to create resource, instead of calling ResourceAllocator.createResource().
     * This method handles the connection creation retrial in case of failure
     */
    protected ResourceHandle createSingleResource(ResourceAllocator resourceAllocator) throws PoolingException{
        ResourceHandle resourceHandle = null;
        int count = 0;
        while(true){
            try{
                count++;
                resourceHandle = resourceAllocator.createResource();
                if(validation || validateAtmostEveryIdleSecs)
                    resourceHandle.setLastValidated(System.currentTimeMillis());
                break;
            }catch(Exception ex){
                _logger.log(Level.FINE, "Connection creation failed for " + count + " time. It will be retried, "
                        + "if connection creation retrial is enabled.", ex);
                if(!connectionCreationRetry_ || count >= connectionCreationRetryAttempts_)
                    throw new PoolingException(ex);
                try {
                    Thread.sleep(conCreationRetryInterval_);
                } catch (InterruptedException ie) {
                    //ignore this exception
                }
            }
        }
        return resourceHandle;
    }
    
    /*
     * Create resources upto steadyPoolSize
     */
    
    //synchronized method, eliminated block sync
    private synchronized void createResources(ResourceAllocator alloc, int size) throws PoolingException {
        for (int i = 0; i < size; i++) {
            createResourceAndAddToPool(alloc);
        }
    }
    
    
    protected void destroyResource(ResourceHandle resourceHandle) {
        try {
            resourceHandle.getResourceAllocator().destroyResource(resourceHandle);
        } catch (Exception ex) {
            _logger.log(Level.WARNING,"poolmgr.destroy_resource_failed");
            if (_logger.isLoggable( Level.FINE ) ) {
                _logger.log(Level.FINE,"poolmgr.destroy_resource_failed",ex);
            }
        } finally {
            //if connection leak tracing is running on connection being
            //destroyed due to error, then stop it
            if(resourceHandle.getResourceState().isBusy())
                stopConnectionLeakTracing(resourceHandle);
            if (monitoringEnabled) {
                poolCounters.incrementNumConnDestroyed();
                if (resourceHandle.getResourceState().isBusy()){
                    //Destroying a Connection due to error
                    poolCounters.decrementNumConnUsed(true);
                } else {
                    //Destroying a free Connection
                    poolCounters.decrementNumConnFree();
                }
            }
        }
    }
    
    /**
     * this method is called to indicate that the resource is
     * not used by a bean anymore
     */
    public void resourceClosed(ResourceHandle h)
    throws IllegalStateException {
        if (_logger.isLoggable( Level.FINE ) ) {
            _logger.log(Level.FINE,"Pool: resourceClosed: " + h);
        }

        ResourceState state = getResourceState(h);
        if(state == null){
            throw new IllegalStateException("State is null");
        }

        if(!state.isBusy()){
            throw new IllegalStateException("state.isBusy() : false");
        }

        setResourceStateToFree(h);  // mark as not busy
        state.touchTimestamp();


        if (state.isUnenlisted() || (isNonXAResource(h) && isLocalResourceEligibleForReuse(h))) {
            freeUnenlistedResource(h);
        }
        
        if (monitoringEnabled){
            poolCounters.incrementNumConnReleased();
        }
        
        _logger.log(Level.FINE,"Pool: resourceFreed: " + h);
    }

    /** Check whether the local resource can be put back to pool
     *  If true, unenlist the resource
     * @param h ResourceHandle to be verified
     * @return boolean 
     */
    private boolean isLocalResourceEligibleForReuse(ResourceHandle h){
        boolean result = false;
        if((!isLocalResourceInTransaction(h))){
            try{
                enforceDelistment(h);
            }catch(SystemException se){
                _logger.log(Level.FINE,"Exception while delisting the local resource forcibily from transaction",se);
                return result;
            }
            h.getResourceState().setEnlisted(false);
            result = true;
        }
        return result;
    }

    /** Remove the resource from book-keeping
     * @param h ResourceHandle to be delisted
     */
    private synchronized void enforceDelistment(ResourceHandle h) throws SystemException{
            J2EETransaction  txn = (J2EETransaction)Switch.getSwitch().getTransactionManager().getTransaction();
            if(txn != null){
                Set set = txn.getResources(name);
                if(set != null)
                    set.remove(h);
            }
    }

    /**
     * Check whether the local resource in question is the one participating in transaction.
     * @param h ResourceHandle
     * @return true if the resource is  participating in the transaction
     */
    private boolean isLocalResourceInTransaction(ResourceHandle h) {
        boolean result = true;
               try{
                       J2EETransaction  txn = (J2EETransaction)Switch.getSwitch().getTransactionManager().getTransaction();
                       if(txn != null)
                          result = isNonXAResourceInTransaction(txn,h);
               }catch(SystemException e){
                _logger.log(Level.FINE,"Exception while checking whether the resource is nonxa " +
                        "and is enlisted in transaction : ",e); 
               }
        return result;
    }

     /**
     * If the resource is used for <i>maxConnectionUsage</i> times, destroy and create one
     * @param handle
     */
    protected void performMaxConnectionUsageOperation(ResourceHandle handle) {
        if (handle.getUsageCount() >= maxConnectionUsage_) {
            resources.remove(handle);
            free.remove(handle);
            destroyResource(handle);
            _logger.log(Level.INFO, "resource_pool.remove_max_used_conn", handle.getUsageCount());

            //compensate with a new resource only when the pool-size is less than steady-pool-size
            if(resources.size() < steadyPoolSize){
                try {
                    createResourceAndAddToPool(handle.getResourceAllocator());
                } catch (Exception e) {
                    _logger.log(Level.WARNING, "resource_pool.failed_creating_resource", e);
                }
            }
        }
    }

    protected synchronized void freeUnenlistedResource(ResourceHandle h) {
        freeResource(h);
    }
    
    protected synchronized void freeResource(ResourceHandle resourceHandle){
        // Put it back to the free collection.
        free.add(resourceHandle);
        //update the monitoring data
        if(monitoringEnabled){
            poolCounters.decrementNumConnUsed(false);
        }

        if (maxConnectionUsage_ > 0) {
            performMaxConnectionUsageOperation(resourceHandle);
        }

        //for both the cases of free.add and maxConUsageOperation, a free resource is added.
        // Hence notify waiting threads
        notifyWaitingThreads();
    }
    
    synchronized
            public void resourceErrorOccurred(ResourceHandle h)
            throws IllegalStateException {

        if(_logger.isLoggable(Level.FINE)){
            _logger.fine("Pool: resourceErrorOccurred: " + h);
        }
        
        if ( failAllConnections ) {
            doFailAllConnectionsProcessing();
            return;
        }
        
        ResourceState state = getResourceState(h);
        //GJCINT - commenting out below
        /**
         * The reason is that normally connection error is expected
         * to occur only when the connection is in use by the application.
         * When there is connection validation involved, the connection
         * can be checked for validity "before" it is passed to the
         * application i.e. when the resource is still free. Since,
         * the connection error can occur when the resource
         * is free, the following is being commented out.
         */
        /*
        if (state == null ||
        state.isBusy() == false) {
            throw new IllegalStateException();
        }
         */
        if (state == null) {
            throw new IllegalStateException();
        }
        
        // changed order of commands
        
        //Commenting resources.remove() out since we will call an iter.remove()
        //in the getUnenlistedResource method in the if check after
        //matchManagedConnections or in the internalGetResource method
        //If we were to call remove directly here there is always the danger
        //of a ConcurrentModificationExceptionbeing thrown when we return
        //
        //In case of this method being called asynchronously, since
        //the resource has been marked as "errorOccured", it will get
        //removed in the next iteration of getUnenlistedResource
        //or internalGetResource
        resources.remove(h);
        destroyResource(h);
    }
    
    private void doFailAllConnectionsProcessing() {
        logFine("doFailAllConnectionsProcessing entered");
        cancelResizerTask();
        if ( monitoringEnabled ) {
            poolCounters.incrementNumConnFailedValidation(resources.size());
        }
        
        emptyPool();
	try {
	    createResources(allocator, steadyPoolSize);
	} catch(PoolingException pe) {
	    //Ignore and hope the resizer does its stuff
	    logFine( "in doFailAllConnectionsProcessing couldn't create steady resources");
	}
	scheduleResizerTask();
	logFine("doFailAllConnectionsProcessing done - created new resources");

    }
    
    /**
     * this method is called when a resource is enlisted in
     * transation tran
     */
    public void resourceEnlisted(Transaction tran, ResourceHandle resource)
    throws IllegalStateException {
        try {
            J2EETransaction j2eetran = (J2EETransaction) tran;
            Set set = j2eetran.getResources(name);
            if (set == null) {
                set = new HashSet();
                j2eetran.setResources(set, name);
            }
            set.add(resource);
        } catch(ClassCastException e) {
            _logger.log(Level.FINE, "Pool: resourceEnlisted:" +
                    "transaction is not J2EETransaction but a " + tran.getClass().getName() , e);
        }
        ResourceState state = getResourceState(resource);
        state.setEnlisted(true);
        if (_logger.isLoggable( Level.FINE ) ) {
            _logger.log(Level.FINE,"Pool: resourceEnlisted: " + resource);
        }
    }

    /**
     * this method is called when transaction tran is completed
     */
    synchronized
            public void transactionCompleted(Transaction tran, int status)
            throws IllegalStateException {
        try {
            J2EETransaction j2eetran = (J2EETransaction) tran;
            Set set =  j2eetran.getResources(name);
            if (set == null) return;
            
            Iterator iter = set.iterator();
            while (iter.hasNext()) {
                ResourceHandle resource = (ResourceHandle) iter.next();
                ResourceState state = getResourceState(resource);
                state.setEnlisted(false);
                // Application might not have closed the connection.
                if ( isResourceUnused(resource) ){
                    freeResource(resource);
                }
                iter.remove();
                if (_logger.isLoggable( Level.FINE ) ) {
                    _logger.log(Level.FINE,"Pool: transactionCompleted: " + resource);
                }
            }
        } catch (ClassCastException e) {
            _logger.log(Level.FINE, "Pool: transactionCompleted: " +
                    "transaction is not J2EETransaction but a " + tran.getClass().getName() , e);
        }
    }
    
    protected boolean isResourceUnused(ResourceHandle h){
        return getResourceState(h).isFree();
    }
    
    protected void notifyWaitingThreads() {
        // notify the first thread in the waitqueue
        Object waitMonitor = null;
        synchronized (waitQueue) {
            if (waitQueue.size() > 0) {
                waitMonitor = waitQueue.removeFirst();
            }
        }
        if (waitMonitor != null) {
            synchronized (waitMonitor) {
                waitMonitor.notify();
            }
        }
    }
    
    // Start of methods related to Pool Monitoring
    /**
     * Return the number of threads that are waiting
     * to obtain a connection from the pool
     */
    public int getNumThreadWaiting() {
        return waitQueue.size();
    }
    
    /**
     * Return the number of connections that have failed validation
     */
    public long getNumConnFailedValidation() {
        return poolCounters.numConnFailedValidation;
    }
    
    /**
     * Return the number of threads that have time out after
     * waiting to obtain a connection from the pool.
     */
    public long getNumConnTimedOut() {
        return poolCounters.numConnTimedOut;
    }
    
    /**
     * Return the number of free connections in the pool
     */
    public synchronized long getNumConnFree() {
        return poolCounters.currNumConnFree;
    }
    
    public long getMaxNumConnFree(){
        return poolCounters.maxNumConnFree;
    }
    
    public long getMinNumConnFree(){
        if (poolCounters.minNumConnFree != Long.MAX_VALUE) {
            return poolCounters.minNumConnFree;
        } else {
            return 0;
        }
    }
    
    /**
     * Return the number of connections in use
     */
    public synchronized long getNumConnInUse() {
        return poolCounters.currNumConnUsed;
    }
    
    /**
     * Return the maximum number of connections ever used in
     * this pool
     */
    public long getMaxNumConnUsed() {
        return poolCounters.maxNumConnUsed;
    }
    
    
    //8.1 monitoring
    public long getCurrentConnRequestWaitTime(){
        return poolCounters.currConnectionRequestWait;
    }
    
    public long getMaxConnRequestWaitTime(){
        return poolCounters.maxConnectionRequestWait;
    }
    
    public long getMinConnRequestWaitTime(){
        if(poolCounters.minConnectionRequestWait != Long.MAX_VALUE) {
            return  poolCounters.minConnectionRequestWait;
        } else {
            return 0;
        }
    }
    
    public long getTotalConnectionRequestWaitTime() {
        return poolCounters.totalConnectionRequestWait;
    }
    
    public long getMinNumConnUsed(){
        if (poolCounters.minNumConnUsed != Long.MAX_VALUE) {
            return poolCounters.minNumConnUsed;
        } else {
            return 0;
        }
    }
    
    public long getNumConnCreated(){
        return poolCounters.numConnCreated;
    }
    
    public long getNumConnDestroyed(){
        return poolCounters.numConnDestroyed;
    }
    
    public long getNumConnAcquired() {
        return poolCounters.numConnAcquired;
    }
    
    public long getNumConnReleased() {
        return poolCounters.numConnReleased;
    }
    
    public long getNumConnSuccessfullyMatched(){
        return poolCounters.numConnSuccessfullyMatched;
    }
    
    public long getNumConnNotSuccessfullyMatched(){
        return poolCounters.numConnNotSuccessfullyMatched;
    }
    
    public long getNumPotentialConnLeak(){
        return poolCounters.numPotentialConnLeak;
    }

    private void incrementNumConnFailedValidation(){
        if (monitoringEnabled) {
            poolCounters.incrementNumConnFailedValidation(1);
        }
    }

    
    // end of methods related to pool monitoring
    
    // Modifications on resizePool() from RI:
    //  - Use destroyResource()
    //  - Reuse-variable optimization.
    //  - Add steady pool size check.
    
    synchronized public void resizePool(boolean forced) {
        
        //Following will be the resizers behavior
        // a) If the wait queue is NOT empty, don't do anything.
        // b) It should try to scale down the pool by "pool-resize" value
        // c) First, Remove all invalid and idle resources, as a result one of the following may happen
        //      i)   equivalent to "pool-reize" quantity of resources are removed
        //      ii)  less than "pool-reize" quantity of resources are removed
        //           remove more resources to match pool-resize quantity, atmost to scale down till steady-pool-size
        //      iii) more than "pool-resize" quantity of resources are removed
        //           (1) if pool-size is less than steady-pool-size, bring it back to steady-pool-size.
        //           (2) if pool-size is greater than steady-pool-size, don't do anything.
        
        synchronized (waitQueue) {
            if (waitQueue.size() > 0) {
                return;
            }
        }
        
        int poolSize = resources.size();
        //remove invalid and idle resource(s)
        removeInvalidAndIdleResources();
        
        int noOfResourcesRemoved = poolSize - resources.size();
        
        if (resizeQuantity > 0 && forced) {
            int moreResourcesToRemove = resizeQuantity - noOfResourcesRemoved;
            moreResourcesToRemove = (moreResourcesToRemove <= resources.size() - steadyPoolSize) ? moreResourcesToRemove : 0;
            
            ResourceHandle h;
            if (moreResourcesToRemove > 0) {
                Iterator iter = free.iterator();
                while (iter.hasNext() && moreResourcesToRemove > 0) {
                    h = (ResourceHandle) iter.next();
                    resources.remove(h);
                    destroyResource(h);
                    iter.remove();
                    moreResourcesToRemove--;
                }
            }
        }
        
        // Now make sure that the steady pool size is met.
        
        // eliminated block sync
        if (resources.size() < steadyPoolSize) {
            // Create resources to match the steady pool size
            // changed to use resources.size() instead of counter
            for (int i = resources.size(); i < steadyPoolSize; i++) {
                try {
                    createResourceAndAddToPool(allocator);
                } catch (PoolingException ex) {
                    _logger.log(Level.WARNING,
                            "resource_pool.resize_pool_error", ex.getMessage());
                }
            }
        }
        
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "Pool: " + name + " -- Resource held: " + resources.size());
        }
    }

    private void removeInvalidAndIdleResources() {
        
        //Find all ManagedConnections that are free/not-in-use
        Set freeManagedConnections = new HashSet();
        ResourceState state;
        int size = free.size();
        // let's cache the current time since precision is not required here.
        long currentTime = System.currentTimeMillis();
        
        for (Iterator iter = free.iterator(); iter.hasNext();) {
            ResourceHandle element = (ResourceHandle) iter.next();
            
            state = getResourceState(element);
            //remove all idle-time lapsed resources.
            if (currentTime - state.getTimestamp() > idletime) {
                resources.remove(element);
                destroyResource(element);
                iter.remove();
            } else {
                freeManagedConnections.add(element.getResource());
            }
        }
        
        removeInvalidResources(freeManagedConnections);
        
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "Pool: " + name + " -- Idle resources freed: " + (size - freeManagedConnections.size()));
            _logger.log(Level.FINE, "Pool: " + name + " -- Invalid resources removed: " + (freeManagedConnections.size() - free.size()));
        }
    }

    /**
     * Removes invalid resource handles in the pool while resizing the pool.
     * Uses the Connector 1.5 spec 6.5.3.4 optional RA feature to obtain
     * invalid ManagedConnections
     */
    private void removeInvalidResources(Set freeManagedConnections) {
        try {
            
            _logger.log(Level.FINE, "Sending to RA a set of free connections " +
                    "of size : " + freeManagedConnections.size());
            
            //get Invalid ManagedConnections from the resource-adapter
            Set invalidManagedConnections =
                    this.allocator.getInvalidConnections(freeManagedConnections);
            
            //Find the appropriate ResourceHandle for a returned invalid
            //ManagedConnection and destroy the Resourcehandle and references to
            //it in resources and free list.
            if (invalidManagedConnections != null) {
                _logger.log(Level.FINE, "Received from RA invalid connections : "+
                        invalidManagedConnections.size());
                
                for (Iterator iter = invalidManagedConnections.iterator();
                iter.hasNext();) {
                    ManagedConnection invalidManagedConnection =
                            (ManagedConnection) iter.next();
                    for (Iterator freeResourcesIter = free.iterator();
                    freeResourcesIter.hasNext();) {
                        ResourceHandle handle =
                                (ResourceHandle) freeResourcesIter.next();
                        if (invalidManagedConnection.equals
                                (handle.getResource())) {
                            resources.remove(handle);
                            destroyResource(handle);
                            freeResourcesIter.remove();
                            incrementNumConnFailedValidation();
                        }
                    }
                }
            } else {
                _logger.log(Level.FINE, "RA does not support " +
                        "ValidatingManagedConnectionFactory");
            }
        } catch (ResourceException re) {
            _logger.log(Level.FINE, "ResourceException while trying to " +
                    "get invalid connections from MCF", re);
        } catch (Exception e) {
            _logger.log(Level.FINE, "Exception while trying " +
                    "to get invalid connections from MCF", e);
        }
    }
    
    private ResourceState getResourceState(ResourceHandle h) {
        return h.getResourceState();
    }
    
    synchronized public void emptyPool() {
        logFine("EmptyPool: Name = " + name);
        
        Iterator iter = resources.iterator();
        while(iter.hasNext()) {
            ResourceHandle h = (ResourceHandle) iter.next();
            destroyResource(h);
        }
        free.clear();
        resources.clear();
    }
    
    synchronized public void emptyFreeConnectionsInPool() {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine( "Emptying free connections in pool : " + name );
        }
        for (Object aFree : free) {
            ResourceHandle h = (ResourceHandle) aFree;
            resources.remove(h);
            destroyResource(h);
        }
        free.clear();
    }
    
    class Resizer extends TimerTask {
        public void run() {
            
            if (_logger.isLoggable( Level.FINE ) ) {
                _logger.log(Level.FINE,"AbstractResourcePool: resize pool "
                        + name);
            }
            resizePool(true);
        }
    }
    
    class ConnectionLeakTask extends TimerTask {
        
        ResourceHandle resourceHandle;
        
        ConnectionLeakTask(ResourceHandle resourceHandle){
            this.resourceHandle = resourceHandle;
        }
        
        public void run(){
            potentialConnectionLeakFound(resourceHandle);
        }
        
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer("Pool [");
        sb.append(name);
        sb.append("] PoolSize=");
        sb.append(resources.size());
        sb.append("  FreeResources=");
        sb.append(free.size());
        sb.append("  QueueSize=");
        sb.append(waitQueue.size());
        sb.append(" matching=");
        sb.append( (matchConnections ? "on" : "off") );
        sb.append(" validation=");
        sb.append( (validation ? "on" : "off") );
        return sb.toString();
    }
    
    
    public boolean isMonitoringEnabled() {
        return monitoringEnabled;
    }
    public void disableMonitoring() {
        monitoringEnabled = false;
    }
    
    public void setMonitoringEnabledHigh() {
        logFine("Enabling monitoring to level : HIGH");
        int numConnFree = (this.poolInitialized) ? this.free.size() : this.steadyPoolSize;
        if ( poolCounters == null ){
            poolCounters = new HighPoolCounters(numConnFree);
        }
        poolCounters.reset(numConnFree);
        monitoringEnabled = true;
    }
    
    public void setMonitoringEnabledLow() {
        logFine("Enabling monitoring to level : LOW");
        if ( poolCounters == null ){
            poolCounters = new LowPoolCounters(0);
        }
        poolCounters.reset(0);
        monitoringEnabled = true;
    }

    /**
     * Reconfigure the Pool's properties. The reconfigConnectorConnectionPool
     * method in the ConnectorRuntime will use this method (through PoolManager)
     * if it needs to just change pool properties and not recreate the pool
     *
     * @param poolResource - the ConnectorConnectionPool javabean that holds
     *                     the new pool properties
     * @throws PoolingException if the pool resizing fails
     */
    public synchronized void reconfigPoolProperties(ConnectorConnectionPool poolResource)
            throws PoolingException {
        int _idleTime = Integer.parseInt(poolResource.getIdleTimeoutInSeconds())
                * 1000;
        if(poolInitialized){
            if (_idleTime != idletime && _idleTime != 0) {
                scheduleResizerTask();
            }
            if (_idleTime == 0) {
                //resizerTask.cancel();
                cancelResizerTask();
            }
        }
        idletime = _idleTime;

        resizeQuantity = Integer.parseInt(poolResource.getPoolResizeQuantity());

        maxWaitTime = Integer.parseInt(poolResource.getMaxWaitTimeInMillis());
        //Make sure it's not negative.
        if (maxWaitTime < 0) {
            maxWaitTime = 0;
        }

        validation = poolResource.isIsConnectionValidationRequired();
        failAllConnections = poolResource.isFailAllConnections();
        boolean oldConnectionLeakTracing_ = connectionLeakTracing_;
        setAdvancedPoolConfiguration(poolResource);

            //in case of connection leak tracing being turned off clear
            //all connection leak tracing tasks
            if (!connectionLeakTracing_ && oldConnectionLeakTracing_ && poolInitialized) {
                clearAllConnectionLeakTasks();
            }

        //Self managed quantities. These are ignored if self management
        //is on
        if (!isSelfManaged()) {
            int _maxPoolSize = Integer.parseInt(poolResource.getMaxPoolSize());

            if (_maxPoolSize < steadyPoolSize) {
                //should not happen, admin must throw exception when this condition happens.
                //as a precaution set max pool size to steady pool size
                maxPoolSize = steadyPoolSize;
            } else {
                maxPoolSize = _maxPoolSize;
            }


            int _steadyPoolSize = Integer.parseInt(poolResource.getSteadyPoolSize());
            int oldSteadyPoolSize = steadyPoolSize;

            if (_steadyPoolSize > maxPoolSize) {
                //should not happen, admin must throw exception when this condition happens.
                //as a precaution set steady pool size to max pool size
                steadyPoolSize = maxPoolSize;
            } else {
                steadyPoolSize = _steadyPoolSize;
            }

            if(poolInitialized){
                //In this case we need to kill extra connections in the pool
                //For the case where the value is increased, we need not
                //do anything
                //num resources to kill is decided by the resources in the pool.
                //if we have less than current maxPoolSize resources, we need to
                //kill less.
                int toKill = resources.size() - maxPoolSize;

                if(toKill >0)
                    killExtraResources(toKill);
            }

              if (oldSteadyPoolSize != steadyPoolSize) {
                if(poolInitialized){
                    if(oldSteadyPoolSize < steadyPoolSize)
                        increaseSteadyPoolSize(_steadyPoolSize);
                } else if(monitoringEnabled){
                        poolCounters.setNumConnFree(steadyPoolSize);
                }
            }
        }
    }

    /**
     * sets advanced pool properties<br>
     * used during pool configuration (initialization) and re-configuration<br>
     * @param poolResource Connector Connection Pool
     */
    private void setAdvancedPoolConfiguration(ConnectorConnectionPool poolResource) {
        matchConnections = poolResource.matchConnections();

      //Commented from 9.1 as it is not used
/*
        lazyConnectionAssoc_ = poolResource.isLazyConnectionAssoc();
        lazyConnectionEnlist_ = poolResource.isLazyConnectionEnlist();
        associateWithThread_ = poolResource.isAssociateWithThread();
*/
        maxConnectionUsage_ = Integer.parseInt(poolResource.getMaxConnectionUsage());
        connectionCreationRetryAttempts_ = Integer.parseInt
                (poolResource.getConCreationRetryAttempts());
        //Converting seconds to milliseconds as TimerTask will take input in milliseconds
        conCreationRetryInterval_ =
                Integer.parseInt(poolResource.getConCreationRetryInterval()) * 1000;
        connectionCreationRetry_ = connectionCreationRetryAttempts_ > 0;

        validateAtmostPeriodInMilliSeconds_ =
                Integer.parseInt(poolResource.getValidateAtmostOncePeriod()) * 1000;
        connectionLeakReclaim_ = poolResource.isConnectionReclaim();
        connectionLeakTimeoutInMilliSeconds_ = Integer.parseInt(
                poolResource.getConnectionLeakTracingTimeout()) * 1000;

        connectionLeakTracing_ = connectionLeakTimeoutInMilliSeconds_ > 0;
    }

    /*
    * Kill the extra resources at the end of the Hashtable
    * The maxPoolSize being reduced causes this method to
    * be called
    */
    private void killExtraResources(int numToKill) {
        cancelResizerTask();
        
        Iterator iter = free.iterator();
        for( int i = 0; iter.hasNext() && i < numToKill ; i++ ) {
            ResourceHandle h = (ResourceHandle) iter.next();
            resources.remove(h);
            destroyResource( h );
            iter.remove();
        }
        
        scheduleResizerTask();
    }
    
    /*
     * Increase the number of steady resources in the pool
     * if we detect that the steadyPoolSize has been increased
     */
    private void increaseSteadyPoolSize( int newSteadyPoolSize )
    throws PoolingException {
        cancelResizerTask();
        for (int i = resources.size(); i < newSteadyPoolSize; i++) {
            createResourceAndAddToPool(allocator);
        }
        scheduleResizerTask();
    }
    
    /**
     * @throws PoolingException
     */
    private void createResourceAndAddToPool(ResourceAllocator alloc) throws PoolingException {
        ResourceHandle resourceHandle = createSingleResource(alloc);
        //addResource() will also increment numResourcesInPool	
        addResource(resourceSpec, resourceHandle);
        
        // addResource() does not add the resource to the free pool!!!
        // so we need to do that.
        setResourceStateToFree(resourceHandle);
        free.add(resourceHandle);
        
        if ( monitoringEnabled ) {
            poolCounters.incrementNumConnCreated();
        }
    }
    
    /**
     * Switch on matching of connections in the pool.
     */
    public void switchOnMatching() {
        matchConnections = true;
    }
    
    /**
     * query the name of this pool. Required by monitoring
     *
     * @return the name of this pool
     */
    public String getPoolName() {
        return name;
    }
    
    public synchronized void cancelResizerTask() {
        
        if (_logger.isLoggable( Level.FINE ) ) {
            _logger.finest("Cancelling resizer");
        }
        if (resizerTask != null ) {
            resizerTask.cancel();
        }
        resizerTask = null;
        
        if (timer != null){
            timer.purge();
        }
    }


    /**
     * This method can be used for debugging purposes
     */
    public synchronized void dumpPoolStatus() {
        _logger.log(Level.INFO, "Name of pool :" + name);
        _logger.log(Level.INFO, "Free connections :" + free.size());
        _logger.log(Level.INFO, "Total connections :" + resources.size());
        _logger.log(Level.INFO, "Pool's matching is :" + matchConnections);
        _logger.log(Level.INFO, "Free Table is :" + free);
        _logger.log(Level.INFO, "Resource Table is :" + resources);
    }
    
    //Accessor to aid debugging
    public PoolCounters getPoolCounters(){
        return this.poolCounters;
    }
    
    private void logFine( String msg ) {
        if ( _logger.isLoggable( Level.FINE) ) {
            _logger.fine( msg );
        }
    }
    
    //Self management methods
    public int getMaxPoolSize() {
        return maxPoolSize;
    }
    
    public int getSteadyPoolSize() {
        return steadyPoolSize;
    }
    
    
    public void setMaxPoolSize( int size ) {
        if ( size < resources.size() ) {
            synchronized( this ) {
                int toKill =  resources.size() - size;
                if ( toKill > 0 ) {
                    try {
                        killExtraResources( toKill );
                    } catch( Exception re ) {
                        //ignore for now
                        if (_logger.isLoggable(Level.FINE) ) {
                            _logger.fine( "setMaxPoolSize:: killExtraResources " +
                                    "throws exception: " + re.getMessage() );
                        }
                    }
                }
            }
        }
        maxPoolSize = size;
    }
    
    public void setSteadyPoolSize( int size ) {
        steadyPoolSize = size;
    }
    
    public void setSelfManaged( boolean selfManaged ) {
        logFine( "Setting selfManaged to : " +selfManaged+" in pool : "+name );
        selfManaged_ = selfManaged;
    }
    
    protected boolean isSelfManaged() {
        return selfManaged_;
    }
    
    class PoolCounters {
        
        volatile long maxNumConnUsed = 0;  //The max number of connections ever used
        volatile long minNumConnUsed = Long.MAX_VALUE;
        volatile long currNumConnUsed = 0;
        
        volatile long maxNumConnFree = 0;
        volatile long minNumConnFree = Long.MAX_VALUE;
        volatile long currNumConnFree = 0;
        
        volatile long numConnCreated = 0;
        volatile long numConnDestroyed = 0;
        
        volatile long numConnFailedValidation = 0;
        volatile long numConnTimedOut = 0;
        
        volatile long numConnAcquired = 0;
        volatile long numConnReleased = 0;
        
        //8.1 new monitoring statistics
        volatile long currConnectionRequestWait = 0;
        volatile long maxConnectionRequestWait = 0;
        volatile long minConnectionRequestWait = Long.MAX_VALUE;
        volatile long totalConnectionRequestWait = 0; //to compute Avg Conn Req wait time
        
        volatile long numConnSuccessfullyMatched = 0;
        volatile long numConnNotSuccessfullyMatched = 0;
        
        volatile long numPotentialConnLeak = 0;
        
        Class poolCountersClass;
        
        PoolCounters(int freePoolSize) {
            poolCountersClass = this.getClass();
            while (!(poolCountersClass.equals(PoolCounters.class)))
                poolCountersClass = poolCountersClass.getSuperclass();
        }
        
        protected void setWaitTime(long elapsedWaitTime) {}
        protected void incrementNumConnUsed(){}
        protected void decrementNumConnUsed(boolean isConnectionDestroyed){}
        protected void decrementNumConnFree(){}
        protected void incrementNumConnCreated(){}
        protected void incrementNumConnDestroyed(){}
        protected void incrementNumConnMatched() {}
        protected void incrementNumConnNotMatched() {}
        protected void incrementNumConnAcquired() {}
        protected void incrementNumConnTimedOut() {}
        protected void incrementNumConnReleased() {}
        protected void incrementNumConnFailedValidation(int incr) {}
        protected void incrementNumConnSuccessfullyMatched() {}
        protected void incrementNumConnNotSuccessfullyMatched() {}
        protected void incrementNumPotentialConnLeak() {}
        
        protected void reset(int freePoolSize) {
        }

        protected long setNumConnFree(long numConnFree) {
            return numConnFree;
        }
        
        @Override
        public String toString(){
            return "PoolCounters: ";
        }
    }
    
    class LowPoolCounters extends PoolCounters {
        
        AtomicLongFieldUpdater maxNumConnUsedFieldUpdater = null;
        AtomicLongFieldUpdater minNumConnUsedFieldUpdater = null;
        AtomicLongFieldUpdater currNumConnUsedFieldUpdater = null;
        
        AtomicLongFieldUpdater maxNumConnFreeFieldUpdater = null;
        AtomicLongFieldUpdater minNumConnFreeFieldUpdater = null;
        AtomicLongFieldUpdater currNumConnFreeFieldUpdater = null;
        
        AtomicLongFieldUpdater numConnCreatedFieldUpdater = null;
        AtomicLongFieldUpdater numConnDestroyedFieldUpdater = null;
        
        Object lock = null;
        
        
        LowPoolCounters( int freePoolSize ) {
            super(freePoolSize);
            
            maxNumConnUsedFieldUpdater = AtomicLongFieldUpdater.newUpdater(poolCountersClass, "maxNumConnUsed");
            minNumConnUsedFieldUpdater = AtomicLongFieldUpdater.newUpdater(poolCountersClass, "minNumConnUsed");
            currNumConnUsedFieldUpdater = AtomicLongFieldUpdater.newUpdater(poolCountersClass, "currNumConnUsed");
            
            maxNumConnFreeFieldUpdater = AtomicLongFieldUpdater.newUpdater(poolCountersClass, "maxNumConnFree");
            minNumConnFreeFieldUpdater = AtomicLongFieldUpdater.newUpdater(poolCountersClass, "minNumConnFree");
            currNumConnFreeFieldUpdater = AtomicLongFieldUpdater.newUpdater(poolCountersClass, "currNumConnFree");
            
            numConnCreatedFieldUpdater = AtomicLongFieldUpdater.newUpdater(poolCountersClass, "numConnCreated");
            numConnDestroyedFieldUpdater = AtomicLongFieldUpdater.newUpdater(poolCountersClass, "numConnDestroyed");
            
            lock = new Object();
        }
        
        @Override
        protected void incrementNumConnCreated(){
            numConnCreatedFieldUpdater.incrementAndGet(this);
        }
        
        @Override
        protected void incrementNumConnDestroyed(){
            numConnDestroyedFieldUpdater.incrementAndGet(this);
        }
        
        @Override
        protected void incrementNumConnUsed(){
            long numConnUsed;
            long numConnFree;
            synchronized (lock){
                numConnUsed = currNumConnUsedFieldUpdater.incrementAndGet(this);
                numConnFree = setNumConnFree(currNumConnFreeFieldUpdater.get(this) - 1);
            }
            latchMaxAndMinNumConnFree(numConnFree);
            latchMaxAndMinNumConnUsed(numConnUsed);
        }
        
        @Override
        //Called while a connection is removed from the pool
        //or a connection is closed/transaction completed
        protected void decrementNumConnUsed(boolean isConnectionDestroyed){
            long numConnUsed;
            long numConnFree;
            synchronized(lock){
                numConnUsed = currNumConnUsedFieldUpdater.decrementAndGet(this);
                if (isConnectionDestroyed) {
                    //If pool is being pruned by resizer thread
                    //latch total number of connections in pool (conn free + conn used) to
                    //steady-pool size
                    numConnFree = currNumConnFreeFieldUpdater.get(this);
                    if((numConnFree + numConnUsed) < steadyPoolSize) {
                        numConnFree = currNumConnFreeFieldUpdater.incrementAndGet(this);
                    }
                } else {
                    //donot latch here as this is a simple connection close
                    //or tx completed and the pool size could
                    //be greater than steady pool size
                    numConnFree = currNumConnFreeFieldUpdater.incrementAndGet(this);
                }
            }
            latchMaxAndMinNumConnFree(numConnFree);
            latchMaxAndMinNumConnUsed(numConnUsed);
        }
        
        //Called while a free connection is destroyed from the
        //pool by the resizer.
        @Override
        protected void decrementNumConnFree(){
            long numConnFree = -1;
            synchronized (lock){
                //Latch total number of connections [free+used] to
                //steady pool size
                if((currNumConnFreeFieldUpdater.get(this) + currNumConnUsedFieldUpdater.get(this))
                > steadyPoolSize)
                    numConnFree = setNumConnFree(currNumConnFreeFieldUpdater.get(this) - 1);
            }
            latchMaxAndMinNumConnFree(numConnFree);
        }
        
        
        protected long setNumConnFree(long numConnFree){
            //set current - number of connections free cannot be less than zero
            long numConnFreeToSet = (numConnFree >= 0) ? numConnFree : 0;
            currNumConnFreeFieldUpdater.set(this, numConnFreeToSet);
            return numConnFreeToSet;
        }
        
        private void latchMaxAndMinNumConnUsed(long numConnInUse){
            //this method is not 100% thread safe
            //latch max
            if ( numConnInUse > maxNumConnUsedFieldUpdater.get(this) ) {
                maxNumConnUsedFieldUpdater.set(this, numConnInUse);
            }
            //latch min
            if ( numConnInUse < minNumConnUsedFieldUpdater.get(this) ) {
                if (numConnInUse <= 0) {
                    minNumConnUsedFieldUpdater.set(this, 0);
                } else {
                    minNumConnUsedFieldUpdater.set(this, numConnInUse);
                }
            }
        }
        
        private void latchMaxAndMinNumConnFree(long numConnFree){
            //this method is not 100% thread safe
            //latch max
            if ( numConnFree > maxNumConnFreeFieldUpdater.get(this) ) {
                maxNumConnFreeFieldUpdater.set(this, numConnFree);
            }
            //latch min
            if ( numConnFree < minNumConnFreeFieldUpdater.get(this) ) {
                if (numConnFree <= 0) {
                    minNumConnFreeFieldUpdater.set(this, 0);
                } else {
                    minNumConnFreeFieldUpdater.set(this, numConnFree);
                }
            }
        }
        
        @Override
        protected void reset(int freePoolSize) {
            super.reset(freePoolSize);
            
            synchronized (lock){
                maxNumConnUsedFieldUpdater.set(this, 0);
                minNumConnUsedFieldUpdater.set(this, Long.MAX_VALUE);
                currNumConnUsedFieldUpdater.set(this, 0);
                
                maxNumConnFreeFieldUpdater.set(this, 0);
                minNumConnFreeFieldUpdater.set(this, Long.MAX_VALUE);
                currNumConnFreeFieldUpdater.set(this, freePoolSize);
            }
            
            numConnCreatedFieldUpdater.set(this, 0);
            numConnDestroyedFieldUpdater.set(this, 0);
        }
        
        @Override
        public String toString(){
            StringBuffer strBuffer = new StringBuffer(super.toString());
            strBuffer.append("\n maxNumConnUsed = " + maxNumConnUsed);
            strBuffer.append("\n minNumConnUsed = " + minNumConnUsed);
            strBuffer.append("\n currNumConnUsed = " + currNumConnUsed);
            
            strBuffer.append("\n maxNumConnFree =  = " + maxNumConnFree);
            strBuffer.append("\n minNumConnFree = " + minNumConnFree);
            strBuffer.append("\n currNumConnFree = " + currNumConnFree);
            
            strBuffer.append("\n numConnCreated = " + numConnCreated);
            strBuffer.append("\n numConnDestroyed = " + numConnDestroyed);
            
            return strBuffer.toString();
        }
    }
    
    class HighPoolCounters extends LowPoolCounters {
        
        AtomicLongFieldUpdater currConnectionRequestWaitFieldUpdater = null;
        AtomicLongFieldUpdater maxConnectionRequestWaitFieldUpdater = null;
        AtomicLongFieldUpdater minConnectionRequestWaitFieldUpdater = null;
        AtomicLongFieldUpdater totalConnectionRequestWaitFieldUpdater = null;
        AtomicLongFieldUpdater numConnAcquiredFieldUpdater = null;
        AtomicLongFieldUpdater numConnTimedOutFieldUpdater = null;
        AtomicLongFieldUpdater numConnReleasedFieldUpdater = null;
        AtomicLongFieldUpdater numConnFailedValidationFieldUpdater = null;
        AtomicLongFieldUpdater numConnSuccessfullyMatchedFieldUpdater = null;
        AtomicLongFieldUpdater numConnNotSuccessfullyMatchedFieldUpdater = null;
        AtomicLongFieldUpdater numPotentialConnLeakFieldUpdater = null;
        
        HighPoolCounters( int freePoolSize ) {
            super( freePoolSize );
            
            currConnectionRequestWaitFieldUpdater =
                    AtomicLongFieldUpdater.newUpdater(poolCountersClass, "currConnectionRequestWait");
            maxConnectionRequestWaitFieldUpdater =
                    AtomicLongFieldUpdater.newUpdater(poolCountersClass, "maxConnectionRequestWait");
            minConnectionRequestWaitFieldUpdater =
                    AtomicLongFieldUpdater.newUpdater(poolCountersClass, "minConnectionRequestWait");
            totalConnectionRequestWaitFieldUpdater =
                    AtomicLongFieldUpdater.newUpdater(poolCountersClass, "totalConnectionRequestWait");
            numConnTimedOutFieldUpdater =
                    AtomicLongFieldUpdater.newUpdater(poolCountersClass, "numConnTimedOut");
            numConnAcquiredFieldUpdater =
                    AtomicLongFieldUpdater.newUpdater(poolCountersClass, "numConnAcquired");
            numConnReleasedFieldUpdater =
                    AtomicLongFieldUpdater.newUpdater(poolCountersClass, "numConnReleased");
            numConnFailedValidationFieldUpdater =
                    AtomicLongFieldUpdater.newUpdater(poolCountersClass, "numConnFailedValidation");
            numConnSuccessfullyMatchedFieldUpdater =
                    AtomicLongFieldUpdater.newUpdater(poolCountersClass, "numConnSuccessfullyMatched");
            numConnNotSuccessfullyMatchedFieldUpdater =
                    AtomicLongFieldUpdater.newUpdater(poolCountersClass, "numConnNotSuccessfullyMatched");
            numPotentialConnLeakFieldUpdater =
                    AtomicLongFieldUpdater.newUpdater(poolCountersClass, "numPotentialConnLeak");
        }
        
        @Override
        public void setWaitTime(long elapsedWaitTime) {
            //this method is not 100% thread safe
            //latch max
            currConnectionRequestWaitFieldUpdater.set(this, elapsedWaitTime);
            totalConnectionRequestWaitFieldUpdater.addAndGet(this, elapsedWaitTime);
            if (elapsedWaitTime > maxConnectionRequestWaitFieldUpdater.get(this))
                maxConnectionRequestWaitFieldUpdater.set(this, elapsedWaitTime);
            //latch min
            if (elapsedWaitTime < minConnectionRequestWaitFieldUpdater.get(this))
                minConnectionRequestWaitFieldUpdater.set(this, elapsedWaitTime);
            
        }
        
        @Override
        protected void incrementNumConnAcquired() {
            numConnAcquiredFieldUpdater.incrementAndGet(this);
        }
        
        @Override
        protected void incrementNumConnTimedOut() {
            numConnTimedOutFieldUpdater.incrementAndGet(this);
        }
        
        @Override
        protected void incrementNumConnReleased() {
            numConnReleasedFieldUpdater.incrementAndGet(this);
        }
        
        @Override
        protected void incrementNumConnFailedValidation( int incr ) {
            numConnFailedValidationFieldUpdater.addAndGet(this, incr);
        }
        
        @Override
        protected void incrementNumConnSuccessfullyMatched() {
            numConnSuccessfullyMatchedFieldUpdater.incrementAndGet(this);
        }
        
        @Override
        protected void incrementNumConnNotSuccessfullyMatched() {
            numConnNotSuccessfullyMatchedFieldUpdater.incrementAndGet(this);
        }
        
        @Override
        protected void incrementNumPotentialConnLeak() {
            numPotentialConnLeakFieldUpdater.incrementAndGet(this);
        }
        
        @Override
        protected void reset(int freePoolSize) {
            super.reset(freePoolSize);
            numConnFailedValidationFieldUpdater.set(this, 0);
            numConnTimedOutFieldUpdater.set(this, 0);
            
            numConnAcquiredFieldUpdater.set(this, 0);
            numConnReleasedFieldUpdater.set(this, 0);
            
            currConnectionRequestWaitFieldUpdater.set(this, 0);
            maxConnectionRequestWaitFieldUpdater.set(this, 0);
            minConnectionRequestWaitFieldUpdater.set(this, Long.MAX_VALUE);
            totalConnectionRequestWaitFieldUpdater.set(this, 0);
            
            numConnSuccessfullyMatchedFieldUpdater.set(this, 0);
            numConnNotSuccessfullyMatchedFieldUpdater.set(this, 0);
            
            numPotentialConnLeakFieldUpdater.set(this, 0);
        }
        
        @Override
        public String toString(){
            StringBuffer strBuffer = new StringBuffer(super.toString());
            strBuffer.append("\n numConnFailedValidation = " + numConnFailedValidation);
            strBuffer.append("\n numConnTimedOut = " + numConnTimedOut);
            
            strBuffer.append("\n numConnAcquired = " + numConnAcquired);
            strBuffer.append("\n numConnReleased = " + numConnReleased);
            
            strBuffer.append("\n currConnectionRequestWait = " + currConnectionRequestWait);
            strBuffer.append("\n minConnectionRequestWait = " + minConnectionRequestWait);
            strBuffer.append("\n maxConnectionRequestWait = " + maxConnectionRequestWait);
            strBuffer.append("\n totalConnectionRequestWait = " + totalConnectionRequestWait);
            
            strBuffer.append("\n numConnSuccessfullyMatched = " + numConnSuccessfullyMatched);
            strBuffer.append("\n numConnNotSuccessfullyMatched = " + numConnNotSuccessfullyMatched);
            strBuffer.append("\n numPotentialConnLeak = " + numPotentialConnLeak);
            return strBuffer.toString();
        }
    }
    
}
