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

/*
 * RuntimeStatus.java
 *
 * Created on February 27, 2004, 12:26 PM
 */

package com.sun.enterprise.admin.servermgmt;

import com.sun.enterprise.admin.common.Status;

import com.sun.enterprise.server.logging.FileandSyslogHandler;
import com.sun.enterprise.admin.servermgmt.InstancesManager;
import com.sun.enterprise.admin.servermgmt.InstanceException;

import java.io.Serializable;
import java.util.Vector;
import com.sun.enterprise.util.LocalStrings;
import com.sun.enterprise.util.LocalStringsImpl;

/**
 *
 * @author  kebbs
 */
public class RuntimeStatus implements Serializable {
       
    private boolean _restartNeeded;
    private Status _status;
    private Vector _recentErrorMessages;                
    private String _name;
	
	/*
	 * true if this RuntimeStatus object was created as a result of a 
	 * stop-cluster command.  We want toString() to return something
	 * special.  I.e. RuntimeStatus.toString() is too generic.
	 * stop-cluster is called from GenericCommand which runs generic
	 * return value processing code.  Also the GUI will be expecting a RuntimeStatus
	 * object so I decided on this kludge rather than subclassing.
	 * bnevins Feb 2007
	 */
	private boolean stopCluster = false; 
	private boolean startCluster = false; 
	private RuntimeStatus beforeRTStatus = null;
    
    public static RuntimeStatus getRuntimeStatus(
        String name,
        InstancesManager manager) throws InstanceException 
    {                
        int status = manager.getInstanceStatus();          
        return new RuntimeStatus(name, manager.isRestartNeeded(),
            new Status(status, Status.getStatusString(status)),
            FileandSyslogHandler.getRecentErrorMessages());        
    }       

    public static void clearRuntimeStatus() 
    {
        FileandSyslogHandler.clearRecentErrorMessages();
    }
    
    public RuntimeStatus() {         
        this ("", true, new Status(Status.kInstanceNotRunningCode, 
            Status.kInstanceNotRunningMsg), new Vector());
    }
    
    public RuntimeStatus(String name) {         
        this();
        _name = name;
    }
    
    public RuntimeStatus(String name, boolean restartNeeded, 
        Status status, Vector recentErrors)
    {
        _name = name;
        _restartNeeded = restartNeeded;
        _status = status;
        _recentErrorMessages = recentErrors;
    }

	public void setStopClusterFlag(RuntimeStatus before)
	{
		stopCluster = true;
        beforeRTStatus = before;
	}
	public void setStartClusterFlag(RuntimeStatus before)
	{
		startCluster = true;
        beforeRTStatus = before;
	}

	public boolean isRestartNeeded()
    {
        return _restartNeeded;
    }
    
    public String getName()
    {
        return _name;
    }
    
    public void setStatus(Status status) 
    {
        _status = status;
    }
    
    public Status getStatus() 
    {
        return _status;
    }
    
    public Vector getRecentErrorMessages()
    {
        return _recentErrorMessages;
    }
    
    
    public boolean isRunning()
    {
        return getStatus().getStatusCode() == Status.kInstanceRunningCode ? true : false;        
    }
    
    public boolean isStopped()
    {
        return getStatus().getStatusCode() == Status.kInstanceNotRunningCode ? true : false;
    }
    
    public String toShortString()
    {
        String result = getStatus().getStatusString();
        if (isRunning()) {
            if (isRestartNeeded()) {
                result = LocalStrings.get("requiresRestartYes");
            } 
        } 
        return result;
    }
    
    public String toString()
    {
		if(stopCluster)
			return toStringStopCluster();

        if(startCluster)
			return toStringStartCluster();
		
		return toStringRegular();
	}

    /*
     * Remove the recent error messages.
     * 
     */
    public void resetRecentErrorMessages()
    {
        // used by start-cluster when the instances are all running already.
        // we don't need to show these old messages to them now.
        _recentErrorMessages = new Vector();
    }
    public String toStringStartCluster()
    {
        /* 
         * WBN May 2007
         * These are impossible states:
         * (1) ANY instances are still running in the cluster
         * (2) ALL instances are already stopped (beforeRTStatus)
         * (3) The before-status==stopped && after-status==!stopped
         */

        if(beforeRTStatus == null)
        {
            // this is a programming error!
            return toStringRegular();
        }
        
        // by my definition, "started" == kInstanceRunningCode
        //  "not-started" == !kInstanceRunningCode
        final int afterCode = getStatus().getStatusCode();
        final int beforeCode = beforeRTStatus.getStatus().getStatusCode();
        final boolean beforeStarted =  (beforeCode == Status.kInstanceRunningCode);
        final boolean afterStarted  =  (afterCode == Status.kInstanceRunningCode);
        LocalStringsImpl stringy = new LocalStringsImpl();
        final String name = getName();
        final String beforeString = beforeRTStatus.getStatus().getStatusString();
        final String afterString = getStatus().getStatusString();
        String result = "";

        if(beforeStarted && afterStarted)
        {
            // normal -- instance was already running
            result = stringy.get("runtimeStatusToStringStartCluster.alreadyStarted", name);
        }
        
        else if(!beforeStarted && afterStarted)
        {
            // normal -- instance was stopped and is now running
            result = stringy.get("runtimeStatusToStringStartCluster.success", name);
        }
        else if(beforeStarted && !afterStarted)
        {
            // this can't happen...
            result = stringy.get("runtimeStatusToStringStartCluster.startedToStopped", 
                    name, afterString);
            
            // there is no logger available in this file
            System.err.println(result);
        }
        else if(!beforeStarted && !afterStarted)
        {
            // a "normal" error -- the instance could not be Started.
            
            result = stringy.get("runtimeStatusToStringStartCluster.error", name, 
                    beforeString, afterString);
        }
        
		Vector messages = getRecentErrorMessages();
        for (int i = 0; i < messages.size(); i++) {
            result += "\n" + stringy.get("error") + " " + i + " " + (String)messages.get(i);
        }

        return result;
    }
    public String toStringStopCluster()
    {
        /* 
         * WBN Feb 2007
         * These are impossible states:
         * (1) ANY instances are still running in the cluster
         * (2) ALL instances are already stopped (beforeRTStatus)
         * (3) The before-status==stopped && after-status==!stopped
         */

        if(beforeRTStatus == null)
        {
            // this is a programming error!
            return toStringRegular();
        }
        
        // by my definition, "stopped" == kInstanceNotRunningCode
        //  "not-stopped" == !kInstanceNotRunningCode
        final int afterCode = getStatus().getStatusCode();
        final int beforeCode = beforeRTStatus.getStatus().getStatusCode();
        final boolean beforeStopped =  (beforeCode == Status.kInstanceNotRunningCode);
        final boolean afterStopped  =  (afterCode == Status.kInstanceNotRunningCode);
        LocalStringsImpl stringy = new LocalStringsImpl();
        final String name = getName();
        final String beforeString = beforeRTStatus.getStatus().getStatusString();
        final String afterString = getStatus().getStatusString();
        String result = "";

        if(beforeStopped && afterStopped)
        {
            // normal -- instance was already stopped
            result = stringy.get("runtimeStatusToStringStopCluster.alreadyStopped", name);
        }
        
        else if(!beforeStopped && afterStopped)
        {
            // normal -- instance was running and was stopped.
            result = stringy.get("runtimeStatusToStringStopCluster.success", name);
        }
        else if(beforeStopped && !afterStopped)
        {
            // this can't happen...
            result = stringy.get("runtimeStatusToStringStopCluster.stoppedToRunning", 
                    name, afterString);
            
            // there is no logger available in this file
            System.err.println(result);
        }
        else if(!beforeStopped && !afterStopped)
        {
            // a "normal" error -- the instance could not be stopped.
            
            result = stringy.get("runtimeStatusToStringStopCluster.error", name, 
                    beforeString, afterString);
        }
        
		Vector messages = getRecentErrorMessages();
        for (int i = 0; i < messages.size(); i++) {
            result += "\n" + stringy.get("error") + " " + i + " " + (String)messages.get(i);
        }

        return result;
    }

    private String toStringRegular()
    {
        LocalStringsImpl stringy = new LocalStringsImpl();
        String result = stringy.get("runtimeStatusToString", getName(), getStatus().getStatusString());
        if (isRestartNeeded()) {
            result += ", " + stringy.get("requiresRestartYes");
        } else {
            result += ", " + stringy.get("requiresRestartNo");
        }
        
        Vector messages = getRecentErrorMessages();
        for (int i = 0; i < messages.size(); i++) {
            result += "\n" + stringy.get("error") + " " + i + " " + (String)messages.get(i);
        }

        return result;
    }
    
}
    
