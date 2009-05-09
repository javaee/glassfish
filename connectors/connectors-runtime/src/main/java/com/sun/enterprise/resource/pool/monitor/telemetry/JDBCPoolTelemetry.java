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
package com.sun.enterprise.resource.pool.monitor.telemetry;

import com.sun.enterprise.resource.pool.PoolLifeCycleListenerRegistry;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.flashlight.client.ProbeClientMediator;
import org.glassfish.flashlight.client.ProbeClientMethodHandle;
import org.glassfish.flashlight.client.ProbeListener;
import org.glassfish.flashlight.datatree.TreeNode;
import org.glassfish.flashlight.datatree.factory.TreeNodeFactory;
import org.glassfish.flashlight.provider.annotations.ProbeParam;
import org.glassfish.flashlight.statistics.Average;
import org.glassfish.flashlight.statistics.Counter;
import org.glassfish.flashlight.statistics.factory.AverageFactory;
import org.glassfish.flashlight.statistics.factory.CounterFactory;
import org.jvnet.hk2.component.PostConstruct;


/**
 * Telemetry object for Jdbc pool monitoring. 
 * 
 * Implements various events related to jdbc pool monitoring and provides 
 * objects to the calling modules that retrieve monitoring information.
 * 
 * @author shalini
 */
public class JDBCPoolTelemetry implements PostConstruct {
    //Flag to indicate if monitoring is enabled.
    private boolean isEnabled = true;
    private String monitoringLevel;
    
    //A telemetry object is identified by its pool name
    private String jdbcPoolName;
    private Collection<ProbeClientMethodHandle> handles;
    private Logger logger;
    private TreeNode jdbcPoolNode = null;
    private JDBCPoolTelemetryBootstrap jdbcPoolTMBootstrap = 
            new JDBCPoolTelemetryBootstrap();
    private ProbeClientMediator pcm;    
    
    //A telemetry object is associated with a registry that stores all listeners
    //to this object,
    private PoolLifeCycleListenerRegistry poolRegistry;
    
    
    //Objects that are exposed by this telemetry
    private Counter numConnFailedValidation = CounterFactory.createCount();
    private Counter numConnTimedOut = CounterFactory.createCount();
    private Counter numConnFree = CounterFactory.createCount();
    private Counter numConnUsed = CounterFactory.createCount();
    private Average connRequestWaitTime = AverageFactory.createAverage();
    private Counter numConnDestroyed = CounterFactory.createCount();
    private Counter numConnAcquired = CounterFactory.createCount();
    private Counter numConnReleased = CounterFactory.createCount();
    private Counter numConnCreated = CounterFactory.createCount();
    private Counter numPotentialConnLeak = CounterFactory.createCount();
    
    public void postConstruct() {
    }

    public JDBCPoolTelemetry(TreeNode parent, String jdbcPoolName, Logger logger, ProbeClientMediator pcm) {    
        this.jdbcPoolNode = parent;
        this.jdbcPoolName = jdbcPoolName;
        this.logger = logger;
        this.pcm = pcm;
        addSubNodes(parent);
    }

    public JDBCPoolTelemetry(TreeNode parent, String jdbcPoolName, Logger logger) {    
        this.jdbcPoolNode = parent;
        this.jdbcPoolName = jdbcPoolName;
        this.logger = logger;
        addSubNodes(parent);
    }

    public TreeNode getJdbcPoolNode() {
        return this.jdbcPoolNode;
    }
    
    private void addSubNodes(TreeNode parent) {    
        try {
            //Creating the jdbc-connection-pool name node and adding to parent
            TreeNode poolNode = TreeNodeFactory.createTreeNode(jdbcPoolName, null, "jdbc-connection-pool");
            parent.addChild(poolNode);

            //Potential Connection Leak
            Method m1 = this.getClass().getMethod("getNumPotentialConnLeakCount");
            TreeNode connLeakNode = TreeNodeFactory.createMethodInvoker("numPotentialConnLeak", this, "jdbc-connection-pool", m1);
            poolNode.addChild(connLeakNode);
            
            //Connection Validation Failed
            Method m2 = this.getClass().getMethod("getNumConnFailedValidation");
            TreeNode connFailValNode = TreeNodeFactory.createMethodInvoker("numConnFailedValidation", this, "jdbc-connection-pool", m2);
            poolNode.addChild(connFailValNode);
            
            //Num Connection Timed Out
            Method m3 = this.getClass().getMethod("getNumConnTimedOut");
            TreeNode numConnTimedOutNode = TreeNodeFactory.createMethodInvoker("numConnTimedOut", this, "jdbc-connection-pool", m3);
            poolNode.addChild(numConnTimedOutNode);
            
            //Num Conn Free
            Method m4 = this.getClass().getMethod("getNumConnFree");
            TreeNode numConnFreeNode = TreeNodeFactory.createMethodInvoker("numConnFree", this, "jdbc-connection-pool", m4);
            poolNode.addChild(numConnFreeNode);
            
            //Num Conn Used
            Method m5 = this.getClass().getMethod("getNumConnUsed");
            TreeNode numConnUsedNode = TreeNodeFactory.createMethodInvoker("numConnUsed", this, "jdbc-connection-pool", m5);
            poolNode.addChild(numConnUsedNode);
            
            //Connection Request Wait Time
            Method m6 = this.getClass().getMethod("getConnRequestWaitTime");
            TreeNode connReqWaitTimeNode = TreeNodeFactory.createMethodInvoker("connRequestWaitTime", this, "jdbc-connection-pool", m6);
            poolNode.addChild(connReqWaitTimeNode);
            
            //Num Conn Destroyed
            Method m7 = this.getClass().getMethod("getNumConnDestroyed");
            TreeNode numConnDestroyedNode = TreeNodeFactory.createMethodInvoker("numConnDestroyed", this, "jdbc-connection-pool", m7);
            poolNode.addChild(numConnDestroyedNode);            
           
            //Num Conn Acquired
            Method m8 = this.getClass().getMethod("getNumConnAcquired");
            TreeNode numConnAcquiredNode = TreeNodeFactory.createMethodInvoker("numConnAcquired", this, "jdbc-connection-pool", m8);
            poolNode.addChild(numConnAcquiredNode);            

            //Num Conn Created
            Method m9 = this.getClass().getMethod("getNumConnCreated");
            TreeNode numConnCreatedNode = TreeNodeFactory.createMethodInvoker("numConnCreated", this, "jdbc-connection-pool", m9);
            poolNode.addChild(numConnCreatedNode);            

            //Num Conn Released
            Method m10 = this.getClass().getMethod("getNumConnReleased");
            TreeNode numConnReleasedNode = TreeNodeFactory.createMethodInvoker("numConnReleased", this, "jdbc-connection-pool", m10);
            poolNode.addChild(numConnReleasedNode);            
            
            parent.addChild(poolNode);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(JDBCPoolTelemetry.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(JDBCPoolTelemetry.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(JDBCPoolTelemetry.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Get the jdbc pool name of this telemetry object
     * @return jdbcPoolName
     */
    public String getJdbcPoolName() {
        return jdbcPoolName;
    }

    /**
     * Get the pool registry associated with this telemetry object. 
     * It contains all listeners for this pool's lifecycle.
     * @return poolRegistry
     */
    public PoolLifeCycleListenerRegistry getPoolRegistry() {
        return poolRegistry;
    }

    /**
     * Set registry for this telemetry object.
     * The registry stores all listeners of the associated pool's lifecycle.
     * @param poolRegistry
     */
    public void setPoolRegistry(PoolLifeCycleListenerRegistry poolRegistry) {
        this.poolRegistry = poolRegistry;
    }
    
    public void enableMonitoring(boolean flag) {
        //loop through the handles for this node and enable/disable the listeners
        //delegate the request to the child nodes
        if (isEnabled != flag) {
            for (ProbeClientMethodHandle handle : handles) {
                if (flag == true) 
                    handle.enable();
                else
                    handle.disable();
            }
            isEnabled = flag;
        }
    }

    public void setProbeListenerHandles(Collection<ProbeClientMethodHandle> handles) {
        this.handles = handles;
    }

    public void removeProbeListenerHandles() {
        this.handles = null;
    }
    
    /**
     * Find if monitoring is enabled or disabled.
     * @return isEnabled
     */
    public boolean isEnabled() {
        return isEnabled;
    }
    
    /**
     * Whenever connection leak happens, increment numPotentialConnLeak
     * @param pool JdbcConnectionPool that got a connLeakEvent
     */
    @ProbeListener("jdbc-connection-pool:jdbc-connection-pool::potentialConnLeakEvent")
    public void potentialConnLeakEvent(@ProbeParam("poolName") String poolName) {
	// handle the conn leak probe event
        if((poolName != null) && (poolName.equals(this.jdbcPoolName))) {
            logger.finest("Connection Leak event received - poolName = " + 
                             poolName);
            //TODO V3: Checking if this is a valid event
            //Increment counter
            numPotentialConnLeak.increment();
        }
    }

    /**
     * Whenever connection timed-out event occurs, increment numConnTimedOut
     * @param pool JdbcConnectionPool that got a connTimedOutEvent
     */
    @ProbeListener("jdbc-connection-pool:jdbc-connection-pool::connectionTimedOutEvent")
    public void connectionTimedOutEvent(@ProbeParam("poolName") String poolName) {
	// handle the conn timed out probe event
        if((poolName != null) && (poolName.equals(this.jdbcPoolName))) {
            logger.finest("Connection Timed-out event received - poolName = " + 
                             poolName);
            //Increment counter
            numConnTimedOut.increment();
        }        
    }
    
    /**
     * Decrement numconnfree event
     * @param poolName
     * @param steadyPoolSize
     */
    @ProbeListener("jdbc-connection-pool:jdbc-connection-pool::decrementFreeConnectionsSizeEvent")
    public void decrementFreeConnectionsSizeEvent(
            @ProbeParam("poolName") String poolName, 
            @ProbeParam("steadyPoolSize") int steadyPoolSize) {
	// handle the num conn free decrement event
        if((poolName != null) && (poolName.equals(this.jdbcPoolName))) {
            logger.finest("Decrement Num Connections Free event received - poolName = " + 
                             poolName);
            //Decrement counter
            if(numConnFree.getCount() + numConnUsed.getCount() > steadyPoolSize) {
                logger.finest("Free + Used greater than steady pool size." +
                        " Decrementing numConnFree");                
                numConnFree.decrement();
            }
        }
    }
    
    /**
     * Decrement numConnUsed event
     * @param poolName
     * @param beingDestroyed if the connection is destroyed due to error
     * @param steadyPoolSize
     */
    @ProbeListener("jdbc-connection-pool:jdbc-connection-pool::decrementConnectionUsedEvent")
    public void decrementConnectionUsedEvent(
            @ProbeParam("poolName") String poolName, 
            @ProbeParam("beingDestroyed") boolean beingDestroyed,
            @ProbeParam("steadyPoolSize") int steadyPoolSize) {
	// handle the num conn used decrement event
        if((poolName != null) && (poolName.equals(this.jdbcPoolName))) {
            logger.finest("Decrement Num Connections Used event received - poolName = " + 
                             poolName);
            //Decrement numConnUsed counter
            numConnUsed.decrement();//.addDataPoint(-1);
            //TODO V3 : increment numConnFree accordingly needed?
            if(beingDestroyed) {
                //if pruned by resizer thread
                if(numConnFree.getCount() + numConnUsed.getCount() < steadyPoolSize) {
                    numConnFree.increment();
                }                    
            } else {
                numConnFree.increment();
            }
        }
    }
    
    /**
     * Connections freed event
     * @param poolName 
     * @param count number of connections freed to the pool
     */
    @ProbeListener("jdbc-connection-pool:jdbc-connection-pool::connectionsFreedEvent")
    public void connectionsFreedEvent(
            @ProbeParam("poolName") String poolName, 
            @ProbeParam("count") int count) {
	// handle the connections freed event
        if((poolName != null) && (poolName.equals(this.jdbcPoolName))) {
            logger.finest("Connections Freed event received - poolName = " + 
                             poolName);
            logger.finest("numConnUsed =" + numConnUsed.getCount() + 
                    " numConnFree=" + numConnFree.getCount() + 
                    " Number of connections freed =" + count);
            //set numConnFree to the count value
            numConnFree.setCount(count);
        }
    }

    /**
     * Connection used event
     * @param poolName
     */
    @ProbeListener("jdbc-connection-pool:jdbc-connection-pool::connectionUsedEvent")
    public void connectionUsedEvent(
            @ProbeParam("poolName") String poolName) {
	// handle the connection used event
        if((poolName != null) && (poolName.equals(this.jdbcPoolName))) {
            logger.finest("Connection Used event received - poolName = " + 
                             poolName);
            //increment numConnUsed
            numConnUsed.increment();
            //decrement numConnFree
            numConnFree.decrement();
            numConnFree.setCount(numConnFree.getCount()<0 ? 0 : numConnFree.getCount());
        }
    }

    /**
     * Whenever connection leak happens, increment numConnFailedValidation
     * @param pool JdbcConnectionPool that got a failed validation event
     */
    @ProbeListener("jdbc-connection-pool:jdbc-connection-pool::connectionValidationFailedEvent")
    public void connectionValidationFailedEvent(
            @ProbeParam("poolName") String poolName, @ProbeParam("increment") int increment) {
        if((poolName != null) && (poolName.equals(this.jdbcPoolName))) {
            logger.finest("Connection Validation Failed event received - " +
                    "poolName = " + poolName);
            //TODO V3 : add support in CounterImpl for addAndGet(increment)
            numConnFailedValidation.increment(increment);
            /*for(int i=0; i<increment; i++) {
                numConnFailedValidation.increment();
            }*/
        }
        
    }
    
    /**
     * Event that a connection request is served in timeTakenInMillis.
     * 
     * @param poolName
     * @param timeTakenInMillis
     */
    @ProbeListener("jdbc-connection-pool:jdbc-connection-pool::connectionRequestServedEvent")
    public void connectionRequestServedEvent(
            @ProbeParam("poolName") String poolName, 
            @ProbeParam("timeTakenInMillis") long timeTakenInMillis) {
        if((poolName != null) && (poolName.equals(this.jdbcPoolName))) {
            logger.finest("Connection request served event received - " +
                    "poolName = " + poolName);
            connRequestWaitTime.addDataPoint(timeTakenInMillis);
        }        
    }  
    
    /**
     * When connection destroyed event is got increment numConnDestroyed.
     */
    @ProbeListener("jdbc-connection-pool:jdbc-connection-pool::connectionDestroyedEvent")
    public void connectionDestroyedEvent(
            @ProbeParam("poolName") String poolName) {
        if((poolName != null) && (poolName.equals(this.jdbcPoolName))) {
            logger.finest("Connection destroyed event received - " +
                    "poolName = " + poolName);
            numConnDestroyed.increment();
        }                
    }
    
    /**
     * When a connection is acquired increment counter
     */
    @ProbeListener("jdbc-connection-pool:jdbc-connection-pool::connectionAcquiredEvent")
    public void connectionAcquiredEvent(
            @ProbeParam("poolName") String poolName) {
        if((poolName != null) && (poolName.equals(this.jdbcPoolName))) {
            logger.finest("Connection acquired event received - " +
                    "poolName = " + poolName);
            numConnAcquired.increment();
        }                        
    }

    /**
     * When a connection is released increment counter
     */
    @ProbeListener("jdbc-connection-pool:jdbc-connection-pool::connectionReleasedEvent")
    public void connectionReleasedEvent(
            @ProbeParam("poolName") String poolName) {
        if((poolName != null) && (poolName.equals(this.jdbcPoolName))) {
            logger.finest("Connection released event received - " +
                    "poolName = " + poolName);
            numConnReleased.increment();
        }                                
    }

    /**
     * When a connection is created increment counter
     */
    @ProbeListener("jdbc-connection-pool:jdbc-connection-pool::connectionCreatedEvent")
    public void connectionCreatedEvent(
            @ProbeParam("poolName") String poolName) {
        if((poolName != null) && (poolName.equals(this.jdbcPoolName))) {
            logger.finest("Connection created event received - " +
                    "poolName = " + poolName);
            numConnCreated.increment();
        }                                        
    }
    
    /**
     * When a connection leak is observed, the monitoring statistics are displayed
     * to the server.log. This method helps in segregating the statistics based
     * on LOW/HIGH monitoring levels and displaying them.
     * 
     * @param poolName
     * @param stackTrace
     */
    @ProbeListener("jdbc-connection-pool:jdbc-connection-pool::toString")
    public void toString(@ProbeParam("poolName") String poolName,
            @ProbeParam("stackTrace") StringBuffer stackTrace) {
        logger.finest("toString(poolName) event received. " +
                "Monitoring level observed : " + monitoringLevel);
        if((poolName != null) && (poolName.equals(this.jdbcPoolName))) {
            //If level is not OFF then print the stack trace.
            if(jdbcPoolTMBootstrap.getEnabledValue(monitoringLevel)) {
                if("LOW".equals(monitoringLevel)) {
                    lowLevelLog(stackTrace);
                } else if("HIGH".equals(monitoringLevel)) {
                    highLevelLog(stackTrace);                    
                }
            }
        }    
    }
    
    private void lowLevelLog(StringBuffer stackTrace) {
        stackTrace.append("\n curNumConnUsed = " + numConnUsed.getCount());
        stackTrace.append("\n curNumConnFree = " + numConnFree.getCount());
        stackTrace.append("\n numConnCreated = " + numConnCreated.getCount());
        stackTrace.append("\n numConnDestroyed = " + numConnDestroyed.getCount());        
    }
    
    private void highLevelLog(StringBuffer stackTrace) {
        lowLevelLog(stackTrace);
        stackTrace.append("\n numConnFailedValidation = " + numConnFailedValidation.getCount());
        stackTrace.append("\n numConnTimedOut = " + numConnTimedOut.getCount());

        stackTrace.append("\n numConnAcquired = " + numConnAcquired.getCount());
        stackTrace.append("\n numConnReleased = " + numConnReleased.getCount());

        //TODO V3 : enabling other counters.
        /*stackTrace.append("\n currConnectionRequestWait = " + currConnectionRequestWait);
        stackTrace.append("\n minConnectionRequestWait = " + minConnectionRequestWait);
        stackTrace.append("\n maxConnectionRequestWait = " + maxConnectionRequestWait);
        stackTrace.append("\n totalConnectionRequestWait = " + totalConnectionRequestWait);

        stackTrace.append("\n numConnSuccessfullyMatched = " + this.numConnSuccessfullyMatched);
        stackTrace.append("\n numConnNotSuccessfullyMatched = " + numConnNotSuccessfullyMatched);*/
        stackTrace.append("\n numPotentialConnLeak = " + numPotentialConnLeak.getCount());
    }
    
    public void setMonitoringLevel(String newLevel) {
        this.monitoringLevel = newLevel;
    }
    
    public long getNumPotentialConnLeakCount() {
        return (numPotentialConnLeak.getCount());
    }
    
    public long getNumConnFailedValidation() {
        return (numConnFailedValidation.getCount());
    }
    
    public long getNumConnTimedOut() {
        return (numConnTimedOut.getCount());
    }
    
    public long getNumConnUsed() {
        return (numConnUsed.getCount());
    }
    
    public long getNumConnFree() {
        return (numConnFree.getCount());
    }
    
    public long getConnRequestWaitTime() {
        return (connRequestWaitTime.getCurrent());
    }
    
    public long getNumConnDestroyed() {
        return (numConnDestroyed.getCount());
    }
    
    public long getNumConnAcquired() {
        return (numConnAcquired.getCount());
    }

    public long getNumConnCreated() {
        return (numConnCreated.getCount());
    }

    public long getNumConnReleased() {
        return (numConnReleased.getCount());
    }
}
