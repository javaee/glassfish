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

import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class holds a list of runtime status objects which corresponds to a list of server
 * instances (e.g. in a cluster) or a list of node agents.
 * @author  kebbs
 */
public class RuntimeStatusList extends ArrayList implements Serializable {           
       
    public RuntimeStatusList() {         
        super();
    }
        
    public RuntimeStatusList(int capacity) 
    {
        super(capacity);
    }
    
    /**
     * Return the RuntimeStatus at the specified index
     * @param index
     * @throws IndexOutOfBoundsException
     * @return
     */    
    public RuntimeStatus getStatus(int index) throws IndexOutOfBoundsException
    {
        return (RuntimeStatus)super.get(index);
    }
    
    
    /**
     * 
     * @return true if at least one of the servers / node agents in the list is in a running state.
     */    
    public boolean anyRunning()
    {
        return numRunning() > 0 ? true : false;
    }
    
    /**
     *
     * @return true if all of the server or node agents in the list is in a running state.
     * Note that there must be at least one server or node agent in the list.
     */    
    public boolean allRunning()
    {   
        if (isEmpty()) {
            return false;
        } else {
            return numRunning() == size() ? true : false;
        }
    }
       
    /**
     *
     * @return the number of running instances / node agents.
     */    
    public int numRunning()
    {
        int count = 0;
        for (int i = 0; i < size(); i++) {
            if (((RuntimeStatus)get(i)).isRunning()) {              
                count++;
            }
        }
        return count;
    }
    
    /**
     *
     * @return the number of instances / node agents needing a restart. Note that 
     * stopped instances (i.e. not running) will not be counted.
     */    
    public int numNeedingRestart()
    {
        int count = 0;
        for (int i = 0; i < size(); i++) {
            //Only running instances are considered for restart required
            if (((RuntimeStatus)get(i)).isRunning() &&
                ((RuntimeStatus)get(i)).isRestartNeeded())
            {              
                count++;
            }
        }
        return count;
    }
        
    /**
     *
     * @return the number of stopped (not running) server instances
     */    
    public int numStopped() 
    {
        return size() - numRunning();
    }
    
    /**
     *
     * @return true if there are no instances / agents in the list
     */    
    public boolean isEmpty()
    {
        return size() == 0 ? true : false;
    }
    
    
    /**
     *
     * @return a string version of the list which can be: "running" if allRunning(), 
     * "partially running" if anyRunning(), or "stopped" everywhere else.
     */    
    public String toString()          
    {
       if (allRunning()) {
           return Status.getStatusString(Status.kInstanceRunningCode);
       } else if (anyRunning()) {
           return Status.getStatusString(Status.kClusterPartiallyRunningCode);
       } else {
           return Status.getStatusString(Status.kInstanceNotRunningCode);
       }
    }
}
    
