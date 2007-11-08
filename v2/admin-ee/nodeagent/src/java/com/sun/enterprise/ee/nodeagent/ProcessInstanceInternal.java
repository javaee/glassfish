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
 * ProcessInstance.java
 *
 * Created on October 23, 2003, 2:25 PM
 */

package com.sun.enterprise.ee.nodeagent;

import com.sun.enterprise.util.SystemPropertyConstants;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Iterator;
import java.lang.Process;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import com.sun.enterprise.util.ProcessExecutor;
import com.sun.enterprise.ee.admin.servermgmt.InstanceConfig;
import com.sun.enterprise.ee.admin.servermgmt.EEInstancesManager;

/**
 *
 * @author  basler
 */
public class ProcessInstanceInternal extends AbstractProcessInstance
{
    public ProcessInstanceInternal(String name)
    {
        
        setName(name);
    }
    
    /**
     * startInstance - start sequence specific to intances that are internal to the appserver
     */
    
    public void startInstance() throws ProcessManagerException
    {
        try
        {
            if(bDebug) System.err.println("\n" + this.getClass().getName() + ": ProcessInstanceInternal:startInstance for :" + getName());
            InstanceConfig instanceConfig=new InstanceConfig(InstanceDirs.getRepositoryName(), InstanceDirs.getRepositoryRoot(), getName());
            EEInstancesManager eeInstancesManager=new EEInstancesManager(instanceConfig);
            // reset exitcode to make sure on restart it doesn't incur a race condition in the
            // monitor where the exitCode is non-zero and it has a process so another reset is attempted
            setExitCode(0);
            // start instance, this will not sync since sync call was moved to nodeagent, and it is not supposed to sync
            setProcess(eeInstancesManager.startInstance());
            if(bDebug) System.out.println("\nCurrently Running:" + ProcessManager.getInstance().toString());
            
        }
        catch (Exception e)
        {
            throw new ProcessManagerException(e);
        }
    }
    
    
    /**
     * stop sequence specific to intances that are internal to the appserver
     */
    public void stopInstance() throws ProcessManagerException
    {
        stopInstance(false, -1);
    }
    
    /**
     * stop sequence specific to intances that are internal to the appserver
     * @param timeout - maxtime within which the process has to be stopped
     */
    public void stopInstance(int timeout) throws ProcessManagerException
    {
        stopInstance(true, timeout);
    }
    
    private void stopInstance(boolean isTimeBound, int timeout)
    throws ProcessManagerException
    {
        try
        {
            setStopping(true);
            if (bDebug)
                System.err.println("\n" + this.getClass().getName() +
                        ": ProcessInstanceInternal:stopInstance for :" + getName());
            
            InstanceConfig instanceConfig =
                    new InstanceConfig(InstanceDirs.getRepositoryName(),
                    InstanceDirs.getRepositoryRoot(), getName());
            //instanceConfig.setInstanceName(getName());
            EEInstancesManager eeInstancesManager =
                    new EEInstancesManager(instanceConfig);
            
            if (isTimeBound)
            {
                boolean stopped = false;
                
                if(timeout > 0)
                    stopped = eeInstancesManager.stopInstanceWithinTime(timeout);
                
                if (!stopped) eeInstancesManager.killRelatedProcesses();
            }
            else
            {
                eeInstancesManager.stopInstance();
            }
            
            if (bDebug) System.out.println("\nCurrently Running:" +
                    ProcessManager.getInstance().toString());
        }
        catch (Exception e)
        {
            throw new ProcessManagerException(e);
        }
    }
}
