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

import javax.resource.spi.work.*;
import java.io.*;
import java.util.logging.Logger;

/**
 * Proxy for WorkManager.<br>
 * This implementation is Serializable(Externalizable) such that RAR implementation
 * can use it safely in Serialization mandated scenarios<br>
 *
 * @author Jagadish Ramu
 */
public class WorkManagerProxy implements WorkManager, Externalizable/*, MonitorableWorkManager */{

    private transient WorkManager wm;
    private String moduleName;
    //private boolean monitorableInstance;
    private static Logger _logger = LogDomains.getLogger(WorkManagerProxy.class, LogDomains.RSR_LOGGER);


    public WorkManagerProxy(WorkManager wm, String moduleName){
        this.wm = wm;
        this.moduleName = moduleName;
        //monitorableInstance = isMonitorableInstance(wm);
    }

    public WorkManagerProxy(){
    }

    /**
     * @see javax.resource.spi.work.WorkManager
     */
    public void doWork(Work work) throws WorkException {
        wm.doWork(work);
    }

    /**
     * @see javax.resource.spi.work.WorkManager
     */
    public void doWork(Work work, long startTimeout, ExecutionContext executionContext, WorkListener workListener) throws WorkException {
        wm.doWork(work, startTimeout, executionContext, workListener);
    }

    /**
     * @see javax.resource.spi.work.WorkManager
     */
    public long startWork(Work work) throws WorkException {
        return wm.startWork(work);
    }

    /**
     * @see javax.resource.spi.work.WorkManager
     */
    public long startWork(Work work, long startTimeout, ExecutionContext executionContext, WorkListener workListener) throws WorkException {
        return wm.startWork(work, startTimeout, executionContext, workListener);
    }

    /**
     * @see javax.resource.spi.work.WorkManager
     */
    public void scheduleWork(Work work) throws WorkException {
        wm.scheduleWork(work);
    }
    /**
     * @see javax.resource.spi.work.WorkManager
     */
    public void scheduleWork(Work work, long startTimeout, ExecutionContext executionContext, WorkListener workListener) throws WorkException {
        wm.scheduleWork(work, startTimeout, executionContext, workListener);
    }

    /**
     * @see java.io.Externalizable
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(moduleName);
    }

    /**
     * @see java.io.Externalizable
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        moduleName = in.readUTF();
        wm = WorkManagerFactory.retrieveWorkManager(moduleName);
        //monitorableInstance = isMonitorableInstance(wm);
        _logger = LogDomains.getLogger(WorkManagerProxy.class, LogDomains.RSR_LOGGER);
    }

    public boolean equals(Object o){
        boolean equal = false;
        if(o instanceof WorkManagerProxy){
            WorkManagerProxy wmp = (WorkManagerProxy)o;
            equal = wmp.wm.equals(wm);
        }
        return equal;
    }

    public int hashCode(){
        return wm.hashCode();
    }

/*
    private boolean isMonitorableInstance(WorkManager wm){
        boolean isMonitorable = (wm instanceof MonitorableWorkManager);
        if(!isMonitorable){
            _logger.warning("Monitoring information cannot be collected for this WorkManager " +
                    "[ " + wm.getClass().getName() + " ] as it is not of type " +
                    MonitorableWorkManager.class.getName() );
        }
        return isMonitorable;
    }

    public boolean isMonitoringEnabled() {
        if(monitorableInstance){
            return ((MonitorableWorkManager)wm).isMonitoringEnabled();
        }else{
            return false;
        }
    }//reset all counts when isEnabled = false;
    public void setMonitoringEnabled(boolean isEnabled){
        if(monitorableInstance){
            ((MonitorableWorkManager)wm).setMonitoringEnabled(isEnabled);
        }
    }

    public long getCurrentActiveWorkCount() {
        if(monitorableInstance){
            return ((MonitorableWorkManager)wm).getCurrentActiveWorkCount();
        }else{
            return -1;
        }
    }

    public long getMaxActiveWorkCount() {
        if(monitorableInstance){
            return ((MonitorableWorkManager)wm).getMaxActiveWorkCount();
        }else{
            return -1;
        }
    }

    public long getMinActiveWorkCount() {
        if(monitorableInstance){
            return ((MonitorableWorkManager)wm).getMinActiveWorkCount();
        }else{
            return -1;
        }
    }

    public long getWaitQueueLength() {
        if(monitorableInstance){
            return ((MonitorableWorkManager)wm).getWaitQueueLength();
        }else{
            return -1;
        }
    }

    public long getMaxWaitQueueLength() {
        if(monitorableInstance){
            return ((MonitorableWorkManager)wm).getMaxWaitQueueLength();
        }else{
            return -1;
        }
    }

    public long getMinWaitQueueLength() {
        if(monitorableInstance){
            return ((MonitorableWorkManager)wm).getMinWaitQueueLength();
        }else{
            return -1;
        }
    }

    public long getSubmittedWorkCount() {
        if(monitorableInstance){
            return ((MonitorableWorkManager)wm).getSubmittedWorkCount();
        }else{
            return -1;
        }
    }

    public long getRejectedWorkCount() {
        if(monitorableInstance){
            return ((MonitorableWorkManager)wm).getRejectedWorkCount();
        }else{
            return -1;
        }
    }

    public long getCompletedWorkCount() {
        if(monitorableInstance){
            return ((MonitorableWorkManager)wm).getCompletedWorkCount();
        }else{
            return -1;
        }
    }

    public long getMaxWorkRequestWaitTime() {
        if(monitorableInstance){
            return ((MonitorableWorkManager)wm).getMaxWorkRequestWaitTime();
        }else{
            return -1;
        }
    }

    public long getMinWorkRequestWaitTime() {
        if(monitorableInstance){
            return ((MonitorableWorkManager)wm).getMinWorkRequestWaitTime();
        }else{
            return -1;
        }
    }
*/
}
