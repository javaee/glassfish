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

package com.sun.enterprise.deployapi;

import com.sun.appserv.management.client.ConnectionSource;
import com.sun.enterprise.admin.util.HostAndPort;
import com.sun.enterprise.deployapi.SunDeploymentManager;
import com.sun.enterprise.deployapi.SunTarget;
import com.sun.enterprise.deployapi.SunTargetModuleID;
import com.sun.enterprise.deployment.client.DeploymentClientUtils;
import com.sun.enterprise.deployment.client.JESProgressObject;
import com.sun.enterprise.deployment.client.RollBackAction;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.Print;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Vector;

import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

/**
 * Implementation of the Progress Object 
 * @author  dochez
 */
public abstract class ProgressObjectImpl implements Runnable, JESProgressObject {
    
    protected CommandType commandType;
    protected Object[] args;
    private Vector listeners = new Vector(); // <-- needs to be synchronized
    protected SunTarget target;
    protected SunTarget[] targetsList;
    protected String moduleID;
    protected ModuleType moduleType;
    protected DeploymentStatusImpl deploymentStatus =null;
    protected TargetModuleID[] targetModuleIDs = null;
    protected Vector deliveredEvents = new Vector();
    protected com.sun.enterprise.deployment.backend.DeploymentStatus finalDeploymentStatus = null;
    protected boolean deployActionCompleted;
    protected String warningMessages;
    private static StringManager localStrings = StringManager.getManager(ProgressObjectImpl.class);
    
    private final static String MODULE_ID = 
        com.sun.enterprise.deployment.backend.DeploymentStatus.MODULE_ID;
    private final static String MODULE_TYPE = 
        com.sun.enterprise.deployment.backend.DeploymentStatus.MODULE_TYPE;
    private final static String KEY_SEPARATOR = 
        com.sun.enterprise.deployment.backend.DeploymentStatus.KEY_SEPARATOR;
    private final static String SUBMODULE_COUNT = 
        com.sun.enterprise.deployment.backend.DeploymentStatus.SUBMODULE_COUNT;
    private final static String CONTEXT_ROOT = 
        com.sun.enterprise.deployment.backend.DeploymentStatus.CONTEXT_ROOT;
    private final static String WARNING_PREFIX = "WARNING: ";

    /** Creates a new instance of ProgressObjectImpl */
    public ProgressObjectImpl(SunTarget target) {
        this.target = target;
        deploymentStatus = new DeploymentStatusImpl(this);
        deploymentStatus.setState(StateType.RELEASED);
        finalDeploymentStatus = new com.sun.enterprise.deployment.backend.DeploymentStatus();
        deployActionCompleted = false;
    }

    public ProgressObjectImpl(SunTarget[] targets) {
        this.targetsList = targets;
        deploymentStatus = new DeploymentStatusImpl(this);
        deploymentStatus.setState(StateType.RELEASED);
        finalDeploymentStatus = new com.sun.enterprise.deployment.backend.DeploymentStatus();
        deployActionCompleted = false;
    }
    
    /** Add a listener to receive Progress events on deployment
     * actions.
     *
     * @param The listener to receive events
     * @see ProgressEvent
     */
    public void addProgressListener(ProgressListener pol) {
	synchronized (listeners) {
            listeners.add(pol);
	    if (deliveredEvents.size() > 0) {
		Print.dprintln("Delivering undelivered messages...");
	        for (Iterator i = deliveredEvents.iterator(); i.hasNext();) {
		    pol.handleProgressEvent((ProgressEvent)i.next());
	        }
	    }
	}
    }
    
    /** (optional)
     * A cancel request on an in-process operation
     * stops all further processing of the operation and returns
     * the environment to it original state before the operation
     * was executed.  An operation that has run to completion
     * cannot be cancelled.
     *
     * @throws OperationUnsupportedException this optional command
     *         is not supported by this implementation.
     */
    public void cancel() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("cancel not supported");
    }
    
    /** Return the ClientConfiguration object associated with the
     * TargetModuleID.
     *
     * @return ClientConfiguration for a given TargetModuleID or
     *         null if none exists.
     */
    public ClientConfiguration getClientConfiguration(TargetModuleID id) {
        return null;
    }
    
    /** Retrieve the status of this activity.
     *
     * @return An object containing the status
     *          information.
     */
    public DeploymentStatus getDeploymentStatus() {
        DeploymentStatusImpl result = new DeploymentStatusImpl(this);
        result.setState(deploymentStatus.getState());
        result.setMessage(deploymentStatus.getMessage());
        
        return result;
    }

    /**
     * Retrieve the final deployment status which has complete details for each stage
     */
    public com.sun.enterprise.deployment.backend.DeploymentStatus getCompletedStatus() {
        if(deployActionCompleted) {
            return finalDeploymentStatus;
        }
        return null;
    }
    
    /** Retrieve the list of TargetModuleIDs successfully
     * processed or created by the associated DeploymentManager
     * operation.
     *
     * @return a list of TargetModuleIDs.
     */
    public TargetModuleID[] getResultTargetModuleIDs() {

        /**
         * this should go once CTS has fixed their bugs...
         */
        if (targetModuleIDs==null) {
            if(target != null) {
                initializeTargetModuleIDs(moduleID);
            } else if(targetsList != null) {
                initializeTargetModuleIDForAllServers(null, null);
            }
        }
        // will return null until the operation is completed
        return targetModuleIDs;
    }
    
    /**
     * initialize the target module IDs with the passed application moduleID
     * and the descriptors
     */
    protected void initializeTargetModuleIDs(String moduleID) {
        SunTargetModuleID parentTargetModuleID = new SunTargetModuleID(moduleID, target);        
        parentTargetModuleID.setModuleType(getModuleType());
        
        targetModuleIDs = new SunTargetModuleID[1];        
        targetModuleIDs[0] = parentTargetModuleID;
    }

    /**
     * Initialize the target module IDs with the application information stored
     * in the DeploymentStatus for all the server in the target list.
     */
    protected void initializeTargetModuleIDForAllServers(
        com.sun.enterprise.deployment.backend.DeploymentStatus status, 
        MBeanServerConnection mbsc) {

        if(targetsList == null) {
            return;
        }

        targetModuleIDs = new SunTargetModuleID[targetsList.length];
        String moduleID = status == null 
                        ? this.moduleID : status.getProperty(MODULE_ID);
        String key = moduleID + KEY_SEPARATOR + MODULE_TYPE;
        ModuleType type = status == null
                        ? getModuleType()
                        : ModuleType.getModuleType((new Integer(status.getProperty(key))).intValue());

        for(int i=0; i<targetsList.length; i++) {
            SunTargetModuleID parentTargetModuleID = new SunTargetModuleID(moduleID, targetsList[i]);
            parentTargetModuleID.setModuleType(type);
            targetModuleIDs[i] = parentTargetModuleID;

            if (status != null) {
                // let's get the host name and port where the application was deployed 
                HostAndPort webHost=null;
                try {
                    Object[] params = new Object[]{ moduleID, Boolean.FALSE };
                    String[] signature = new String[]{ "java.lang.String", "boolean"};
                    ObjectName applicationsMBean = new ObjectName(APPS_CONFIGMBEAN_OBJNAME);                
                    webHost = (HostAndPort) mbsc.invoke(applicationsMBean, "getHostAndPort", params, signature);                        
                } catch(Exception e) {
                    Print.dprintStackTrace(e.getLocalizedMessage(), e);
                }

                key = moduleID + KEY_SEPARATOR + SUBMODULE_COUNT;
                if (status.getProperty(key) == null) { //standalone module
                    if (ModuleType.WAR.equals(type)) {
                        key = moduleID + KEY_SEPARATOR + CONTEXT_ROOT;
                        String contextRoot = status.getProperty(key);
                        initTargetModuleIDWebURL(parentTargetModuleID, webHost, contextRoot);
                     }
                } else {
                    int counter = (Integer.valueOf(status.getProperty(key))).intValue();
                    // now for each sub module            
                    for (int j = 0; j < counter; j++) {
                        //subModuleID
                        key = moduleID + KEY_SEPARATOR + MODULE_ID + KEY_SEPARATOR + String.valueOf(j);
                        String subModuleID = status.getProperty(key);
                        SunTargetModuleID subModule = new SunTargetModuleID(subModuleID, targetsList[i]);

                        //subModuleType 
                        key = subModuleID + KEY_SEPARATOR + MODULE_TYPE;
                        type = ModuleType.getModuleType((new Integer(status.getProperty(key))).intValue());
                        subModule.setModuleType(type);
                        if (ModuleType.WAR.equals(type) && webHost!=null) {
                            key = subModuleID + KEY_SEPARATOR + CONTEXT_ROOT;
                            String contextRoot = status.getProperty(key);
                            initTargetModuleIDWebURL(subModule, webHost, contextRoot);
                        }
                        parentTargetModuleID.addChildTargetModuleID(subModule);
                    }
                } 
            }
        }
    }

    /**
     * private method to initialize the web url for the associated deployed web
     * module
     */
    private void initTargetModuleIDWebURL(
        SunTargetModuleID tm, HostAndPort webHost, String contextRoot) {
        
        if (webHost==null)
            return;
        
        try {
            // Patchup code for fixing netbeans issue 6221411; Need to find a 
            // good solution for this and WSDL publishing
            String host;
            SunDeploymentManager sdm = new SunDeploymentManager(tm.getConnectionInfo());
            if(sdm.isPE()) {
                host = tm.getConnectionInfo().getHostName();
            } else {
                host = webHost.getHost();
            }
            
            URL webURL = new URL("http", host, webHost.getPort(), contextRoot);
            tm.setWebURL(webURL.toExternalForm());
        } catch(Exception e) {
            Print.dprintStackTrace(e.getLocalizedMessage(),e);
        }
    }
    
    /** Tests whether the vendor supports a cancel
     * opertation for deployment activities.
     *
     * @return <code>true</code> if canceling an
     *         activity is supported by this platform.
     */
    public boolean isCancelSupported() {
        return false;
    }
    
    /** Tests whether the vendor supports a stop
     * opertation for deployment activities.
     *
     * @return <code>true</code> if canceling an
     *         activity is supported by this platform.
     */
    public boolean isStopSupported() {
        return false;
    }
    
    /** Remove a ProgressObject listener.
     *
     * @param The listener being removed
     * @see ProgressEvent
     */
    public void removeProgressListener(ProgressListener pol) {
	synchronized (listeners) {
            listeners.remove(pol);
	}
    }
    
    /** (optional)
     * A stop request on an in-process operation allows the
     * operation on the current TargetModuleID to run to completion but
     * does not process any of the remaining unprocessed TargetModuleID
     * objects.  The processed TargetModuleIDs must be returned by the
     * method getResultTargetModuleIDs.
     *
     * @throws OperationUnsupportedException this optional command
     *         is not supported by this implementation.
     */
    public void stop() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("stop not supported");
    }

    
    public void setCommand(CommandType commandType, Object[] args) {
        this.commandType = commandType;
        this.args = args;
    }
    

    /**
     * implement this method to do the actual processing
     */
    public abstract void run();


    /**
     * Notifies all listeners that have registered interest for ProgressEvent notification. 
     */
    protected void fireProgressEvent(ProgressEvent progressEvent) {
        /*
         *Bug 4977764
         *Iteration failed due to concurrent modification of the vector.  Even though the add, remove, and fire 
         *methods synchronize on the listeners vector, a listener could conceivably invoke add or remove 
         *recursively, thereby triggering the concurrent modification exception.
         *
         *Fix: clone the listeners vector and iterate through the clone.  
         */
	Vector currentListeners = null;
        synchronized (listeners) {
            currentListeners = (Vector) listeners.clone();
            /*
             *The following add must remain inside the synchronized block.  Otherwise, there will be a small window
             *in which a new listener's registration could interleave with fireProgressEvent, registering itself 
             *after the listeners vector had been cloned (thus excluding the new listener from the iteration a
             *few lines below) but before the list of previously-delivered events had been updated.  
             *This would cause the new listener to miss the event that was firing.  
             *Keeping the following add inside the synchronized block ensures that updates to the listeners 
             *vector by addProgressListener and to deliveredEvents by fireProgressEvent do not interleave and therefore
             *all listeners will receive all events.
             */
            
            deliveredEvents.add(progressEvent);
        }

        for (Iterator listenersItr = currentListeners.iterator(); listenersItr.hasNext();) {
            ((ProgressListener)listenersItr.next()).handleProgressEvent(progressEvent);
        }
        currentListeners = null;
    }


    /**
     * Notifies all listeners that have registered interest for ProgressEvent notification. 
     */
    protected void fireProgressEvent(StateType state, String message) {
        fireProgressEvent(state, message, target);
    }

    /**
     * Notifies all listeners that have registered interest for ProgressEvent notification. 
     */
    protected void fireProgressEvent(StateType state, String message, SunTarget aTarget) {
        
        StateType stateToBroadcast = (state != null) ? state : deploymentStatus.getState();

        /* new copy of DeploymentStatus */
	DeploymentStatusImpl depStatus = new DeploymentStatusImpl(this);
	depStatus.setState(stateToBroadcast);
        depStatus.setMessage(message);

        /*
         *Update this progress object's status before notifying listeners.
         */
        if (state != null) {
            deploymentStatus.setMessage(message);
            deploymentStatus.setState(state); // retain current state
	}
        
        /* send notification */
	SunTargetModuleID tmi = new SunTargetModuleID(moduleID, aTarget);
        tmi.setModuleType(getModuleType());
	fireProgressEvent(new ProgressEvent(this, tmi, depStatus));
    }

    CommandType getCommandType() {
        return commandType;
    }
    
    /**
     * Sets the module type for this deployed module
     * @param the module type
     */
    public void setModuleType(ModuleType moduleType) {
        this.moduleType = moduleType;
    }
    
    /**
     * @return the module type of this deployed module
     */ 
    public ModuleType getModuleType() {
        return moduleType;
    }    

    /**
     * Since DeploymentStatus only provides the ability to pass a String to the
     * ProgressListener, the following is a convenience method for allowing the
     * stack-trace from a Throwable to be converted to a String to send to the
     * ProgressListeners.
     */
    protected String getThrowableString(Throwable t) {
	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	PrintStream ps = new PrintStream(bos);
	t.printStackTrace(ps);
	ps.close(); // may not be necessary
	return bos.toString();
    }
    
    protected static final String APPS_CONFIGMBEAN_OBJNAME =
                                  "com.sun.appserv:type=applications,category=config";    

    protected String getDeploymentStatusMessage(com.sun.enterprise.deployment.backend.DeploymentStatus status) {
        return getDeploymentStatusMessage(status, false);
    }
    /**
     * Parse the DeploymentStatus to get the status message within
     */
    protected String getDeploymentStatusMessage(com.sun.enterprise.deployment.backend.DeploymentStatus status, boolean isStartPhase) {
        if(status == null) {
            return null;
        }
        // if stage status is success, return as it is
        if (status!=null && status.getStatus() >= com.sun.enterprise.deployment.backend.DeploymentStatus.SUCCESS) {
            return null;
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(bos);
        com.sun.enterprise.deployment.backend.DeploymentStatus.parseDeploymentStatus(status, pw);
        byte[] statusBytes = bos.toByteArray();
        String statusString = new String(statusBytes);
        // if stage status is WARNING, collect the warning messages
        if(status.getStatus() == com.sun.enterprise.deployment.backend.DeploymentStatus.WARNING) {
            if(warningMessages==null) {
                warningMessages = WARNING_PREFIX + statusString;
            } else {
                warningMessages += statusString;
            }

            // add additional messages if it failed at loading.
            if (isStartPhase) { 
                warningMessages = localStrings.getString("enterprise.deployment.client.start_failed_msg") + warningMessages;
            }

            return null;
        }
        // Failed stage; return the failure message
        return statusString;
    }

    protected void setupForNormalExit(String message, SunTarget aTarget) {
        String i18nmsg;
        // If we ever got some warning during any of the stages, the the final status is warning; else status=success
        if(warningMessages == null) {
            i18nmsg = localStrings.getString("enterprise.deployment.client.action_completed", message);
            finalDeploymentStatus.setStageStatus(com.sun.enterprise.deployment.backend.DeploymentStatus.SUCCESS);
        } else {
            i18nmsg = localStrings.getString("enterprise.deployment.client.action_completed_with_warning", warningMessages);
            finalDeploymentStatus.setStageStatus(com.sun.enterprise.deployment.backend.DeploymentStatus.WARNING);            
        }
        finalDeploymentStatus.setStageStatusMessage(i18nmsg);
        deployActionCompleted = true;
        fireProgressEvent(StateType.COMPLETED, i18nmsg, aTarget);
        return;
    }
    
    protected void setupForAbnormalExit(String errorMsg, SunTarget aTarget) {
        String i18nmsg = localStrings.getString("enterprise.deployment.client.action_failed", errorMsg);
        finalDeploymentStatus.setStageStatus(com.sun.enterprise.deployment.backend.DeploymentStatus.FAILURE);
        finalDeploymentStatus.setStageStatusMessage(i18nmsg);
        deployActionCompleted = true;
        fireProgressEvent(StateType.FAILED, i18nmsg, aTarget);
        return;
    }

    protected boolean checkStatusAndAddStage(SunTarget aTarget, RollBackAction rollback, String action, ConnectionSource dasConnection, com.sun.enterprise.deployment.backend.DeploymentStatus currentStatus) {
        return checkStatusAndAddStage(aTarget, rollback, action, dasConnection, 
            currentStatus, false);
    }

    /**
     * Given a Deployment status, this checks if the status is success
     * If the status is failed, it tries roll back operations (if rollback is specified) and then sets up
     * for an abnormal exit
     */
    protected boolean checkStatusAndAddStage(SunTarget aTarget, RollBackAction rollback, String action, ConnectionSource dasConnection, com.sun.enterprise.deployment.backend.DeploymentStatus currentStatus, boolean isStartPhase) {
        String statusMsg = getDeploymentStatusMessage(currentStatus, 
            isStartPhase);
        finalDeploymentStatus.addSubStage(currentStatus);
        if(statusMsg == null) {
            fireProgressEvent(StateType.RUNNING,
                                localStrings.getString("enterprise.deployment.client.action_completed", action),
                                aTarget);
            return true;
        }
        if(rollback != null) {
            com.sun.enterprise.deployment.backend.DeploymentStatus tmp = new
                        com.sun.enterprise.deployment.backend.DeploymentStatus();
            if(!rollback.rollback(dasConnection, tmp)) {
                fireProgressEvent(StateType.RUNNING, 
                                localStrings.getString("enterprise.deployment.client.action_failed", "Rollback failed"),
                                        aTarget);
                tmp.setStageStatus(com.sun.enterprise.deployment.backend.DeploymentStatus.FAILURE);
                tmp.setStageStatusMessage(localStrings.getString("enterprise.deployment.client.action_failed", "Rollback failed"));
            } else {
                fireProgressEvent(StateType.RUNNING, 
                                localStrings.getString("enterprise.deployment.client.action_completed", "Rollback"),
                                        aTarget);
                tmp.setStageStatus(com.sun.enterprise.deployment.backend.DeploymentStatus.SUCCESS);
                tmp.setStageStatusMessage(localStrings.getString("enterprise.deployment.client.action_completed", "Rollback"));
            }
            finalDeploymentStatus.addSubStage(tmp);
        }
        setupForAbnormalExit(localStrings.getString("enterprise.deployment.client.action_failed_with_message", action, statusMsg),  aTarget);
        return false;
    }
}
