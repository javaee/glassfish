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

import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.logging.Level;

import java.lang.Process;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import com.sun.enterprise.util.ProcessExecutor;
import com.sun.logging.ee.EELogDomains;

/**
 *
 * @author  basler
 */
public abstract class AbstractProcessInstance extends Observable implements ProcessInstance {
    
    private String _name=null;
    private Vector _startCommand=new Vector();
    private Vector _stopCommand=new Vector();
    private Vector _env=new Vector();
    private String _workingDir=null;
    private Process _process=null;
    private Thread _thread=null;
    private int _exitCode=0;
    private Logger _logger=null;
    private boolean _restartProcess=true;
    
    protected static boolean bDebug=false;

    private boolean _isStopping=false;

    // ***********************************************************
    // Runnable Interface's method(s) (ancestor of ProcessInstance)
    // ***********************************************************
    
    public abstract void startInstance() throws ProcessManagerException;    
    public abstract void stopInstance() throws ProcessManagerException;    
    
    public void setExitCode(int exitCode) {
        setChanged();
        _exitCode=exitCode;
    }
    public int getExitCode() {
        return _exitCode;
    }
    
    public void setProcess(Process process) {
        if (bDebug) System.err.println("Changing Process for " + getName() + " to " + process);
        _process=process;
    }
    public Process getProcess() {
        return _process;
    }

    public void setRestartProcess(boolean restartProcess) {
        _restartProcess=restartProcess;
    }
    public boolean getRestartProcess() {
        return _restartProcess;
    }
    public boolean restartProcess() {
        return _restartProcess;
    }
    
    public void setName(String name) {
        _name=name;
    }
    public String getName() {
        return _name;
    }

    
    public void addCommandLine(String type, String cmd) {
        if(type.equals(START_COMMAND)) {
            addStartCommandLine(cmd);
        } else if(type.equals(STOP_COMMAND)) {
            addStopCommandLine(cmd);
        }
    }
    
    
    public void addStartCommandLine(String cmd) {
        _startCommand.add(cmd);
    }
    public String[] getStartCommandAsArray() {
        String[] array=new String[_startCommand.size()];
        return (String[])_startCommand.toArray(array);
    }
    public String getStartCommand() {
        StringBuffer sbRet=new StringBuffer();
        Iterator it=_startCommand.iterator();
        while(it.hasNext()) {
            sbRet.append((String)it.next()+ " ");
        }
       return sbRet.toString();
    }

    public void addStopCommandLine(String cmd) {
        _stopCommand.add(cmd);
    }
    public String[] getStopCommandAsArray() {
        String[] array=new String[_stopCommand.size()];
        return (String[])_stopCommand.toArray(array);
    }
    public String getStopCommand() {
        StringBuffer sbRet=new StringBuffer();
        Iterator it=_stopCommand.iterator();
        while(it.hasNext()) {
            sbRet.append((String)it.next()+ " ");
        }
       return sbRet.toString();
    }
    
    
    public void setEnvironmentVariable(String name_value_pair) {
        _env.add(name_value_pair);
    }
    public void removeEnvironmentVariable(String name_value_pair) {
        _env.remove(name_value_pair);
    }

    public String[] getEnvironmentAsArray() {
        String[] array=new String[_env.size()];
        return (String[])_env.toArray(array);
    }
        
    public String getEnvironmentAsString() {
        String[] array=getEnvironmentAsArray();
        StringBuffer sbRet=new StringBuffer();
        for(int ii=0;ii < array.length; ii++) {
            sbRet.append(array[ii] + " ");
        }
        return sbRet.toString();
    }
    
    
    public void setWorkingDirectory(String dir) {
        _workingDir=dir;
    }
    public String getWorkingDirectory() {
        return _workingDir;
    }

   /**
    * Method getLogger
    *
    * @return Logger - logger for the NodeAgent
    */
    protected Logger getLogger() {
        if (_logger == null) {
            _logger = Logger.getLogger(EELogDomains.NODE_AGENT_LOGGER, "com.sun.logging.ee.enterprise.system.nodeagent.LogStrings");
        }
        return _logger;
    }
    
    public String toString() {
        StringBuffer sbRet=new StringBuffer("\n**Process Name:" + getName());
        sbRet.append("\n\tStartCommandLine:" + getStartCommand());
        sbRet.append("\n\tStopCommandLine:" + getStopCommand());
        sbRet.append("\n\tEnvironment Variables:" + getEnvironmentAsString());
        sbRet.append("\n\tWorking Directory:" + getWorkingDirectory());
        return sbRet.toString();
    }

   public boolean isStopping() {
	return _isStopping;
   }       

   public void setStopping(boolean value) {
	_isStopping = value;
   }
}
