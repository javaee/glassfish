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
 * ProcessTest.java
 *
 * Created on October 23, 2003, 2:07 PM
 */

package com.sun.enterprise.ee.nodeagent;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import com.sun.enterprise.admin.servermgmt.RuntimeStatus;
import com.sun.enterprise.admin.common.Status;

import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManager;

/**
 *
 * @author  basler
 */
public final class ProcessManager implements Observer {
    
    protected static final String PROCESS_COMMANDS="process";
    protected static final String PROCESS_NAME="name";
    protected static final String PROCESS_COMMAND_LINE="command-line";
    protected static final String PROCESS_START_COMMAND="start-command";
    protected static final String PROCESS_STOP_COMMAND="stop-command";
    protected static final String PROCESS_ENVIRONMENT_VARIABLE="environment-variable";
    protected static final String PROCESS_ENVIRONMENT_ARG="arg";
    protected static final String PROCESS_WORKING_DIR="working-dir";
    protected static final String PROCESS_WORKING_DIR_FILE="directory";
    
    // 'volatile' because of get/set methods
    private static volatile Hashtable _htProcesses=new Hashtable();
    
    private static final ProcessManager _processManager=new ProcessManager();
    private final ProcessManagerMonitor _processManagerMonitor;
    
    protected static final StringManager _strMgr=StringManager.getManager(ProcessManager.class);

    private static final boolean bDebug=System.getProperty("Debug") != null;
    
    /** Creates a new instance of ProcessManager */
    private ProcessManager() {
        _processManagerMonitor=new ProcessManagerMonitor();
        _processManagerMonitor.start();
    }

    public static ProcessManager getInstance() {
        return _processManager;
    }

    
    public void initializeConfiguration(final String configFile) throws ProcessManagerException {
        final ProcessManagerConfig config = new ProcessManagerConfig(configFile);
       setProcesses(config.initializeConfig());
    }

    
    public void startProcess(String name) throws ProcessManagerException, IOException {
        
        // get appropriate process by name to start
        ProcessInstance pInstance=(ProcessInstance)getProcesses().get(name);
        
        // start process via Runtime.exec, use this method so exceptions in start can be comminicated
        pInstance.startInstance();
        InstanceDirs.resetSysProp();
        // add the ProcessManager as an observer of 
        // the instance
        pInstance.addObserver(this);
        if(bDebug) System.err.println("\tObserver Registered for :" + name);
    }
    
    
    public void startAll() throws ProcessManagerException, IOException {
        // start each process on it's own thread
        // which will wait on the exit code
        String sxLine=null, key=null;
        Iterator it=getProcesses().keySet().iterator();
        while(it.hasNext()) {
            key=(String)it.next();
            // executing process
            startProcess(key);
        }
    }

    public void stopProcess(String name) throws ProcessManagerException, IOException {
        ProcessInstance pInstance=(ProcessInstance)getProcesses().get(name);
        if (pInstance != null) {
            //  instance could have been started by the startserv command
            // directly, or could be a nodeagent that has recovered from a failure
            pInstance.stopInstance();
            if(bDebug) System.err.println("\tProcess Stopped for :" + name);
        } else {
            // not started by nodeagent
            throw new ProcessManagerException("Instance was not started by ProcessManager");
        }
    }

    public void stopProcess(String name, int timeout) 
    throws ProcessManagerException, IOException {
        ProcessInstance pInstance=(ProcessInstance)getProcesses().get(name);
        if (pInstance != null) {    
            //  instance could have been started by the startserv command
            // directly, or could be a nodeagent that has recovered from a failure
            pInstance.stopInstance(timeout);
            if(bDebug) System.err.println("\tProcess Stopped for :" + name);
        } else {
            // not started by nodeagent
            throw new ProcessManagerException("Instance was not started by ProcessManager");
        }
    }    
    
    public void stopAll() throws ProcessManagerException, IOException {
        // stop each process on it's own thread
        // which will wait on the exit code
        String sxLine=null, key=null;
        Iterator it=getProcesses().keySet().iterator();
        while(it.hasNext()) {
            key=(String)it.next();
            // executing process
            stopProcess(key);
        }
    }
    
    /**
     * The setMonitorSleepInterval method allows the sleep time interval
     * to be set for the ProcessManagerMonitor
     */
    public void setMonitorSleepInterval(int sleepTime) {
        if (getProcessManagerMonitor() != null) {
            getProcessManagerMonitor().setSleepInterval(sleepTime);
        }
    }
    
    /**
     * The getMonitorSleepInterval method allows the sleep time interval
     * to be retrieved from the ProcessManagerMonitor
     */
    public int getMonitorSleepInterval() {
        int iRet=0;
        if (getProcessManagerMonitor() != null) {
            iRet=getProcessManagerMonitor().getSleepInterval();
        }
        return iRet;
    }
    
    private ProcessManagerMonitor getProcessManagerMonitor() {
        return _processManagerMonitor;
    }
        
    public void addProcessInstance(String key, ProcessInstance pi) 
    {                
        getProcesses().put(key, pi);
    }

    public void removeProcessInstance(String key) {          
        getProcesses().remove(key);
    }
    
    private Hashtable getProcesses() {
        return _htProcesses;
    }
    
    public boolean processExists(String key) {
        boolean bRet=false;
        Object obj=getProcess(key);
        if (obj != null) {
            bRet=true;
        }
        return bRet;
    }
    
    public ProcessInstance getProcess(String key) {
        return (ProcessInstance)_htProcesses.get(key);
    }
    
    private void setProcesses(Hashtable ht) {
        _htProcesses=ht;
    }

    public static void main(String[] args) {
        try {
            if(args.length > 0) {
                ProcessManager.getInstance().initializeConfiguration(args[0]);
            }

            // start processManager process creation and monitoring
            ProcessManager.getInstance().startAll();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method formats the instances that are manaaged for display
     * @return Formated String
     */
    public String toString() {
        StringBuffer sb=new StringBuffer("ProcessManager instances:");
        Hashtable htTemp=getProcesses();
        Iterator it=htTemp.keySet().iterator();
        String key=null, status=null, type=null;
        Process process=null;
        ProcessInstance pInstance=null;
        while(it.hasNext()) {
            key=(String)it.next();
            pInstance=(ProcessInstance)htTemp.get(key);
            process=pInstance.getProcess();
            if(process != null) {
                status="Has Process";
            } else {
                status="No Process";
            }
            if(pInstance instanceof ProcessInstanceInternal) {
                type="Internal";
            } else {
                type="External";
            }            
            sb.append("\nName:" + key + "  - Process:" + status + "  - Type:" + type + " - ExitCode:" + pInstance.getExitCode());
        }
        return sb.toString();
    }

    /**
    * Method getLogger
    *
    * @return Logger - logger for the NodeAgent
    */
    protected Logger getLogger() {
        return Logger.getLogger(EELogDomains.NODE_AGENT_LOGGER,
            "com.sun.logging.ee.enterprise.system.nodeagent.LogStrings");
    }
    

    
    // **********************************************************************************************
    // method(s) of Observable interface 
    // **********************************************************************************************
    public void update(Observable observable, Object arg) {
        
        if(observable instanceof ProcessInstance) {
            ProcessInstance pi=(ProcessInstance)observable;
            String instanceName = pi.getName();
            
            if (bDebug) System.err.println("ProcessManager: ***** CHANGE-NOTIFICATION for:" + instanceName + " EXIT CODE=" + pi.getExitCode());
            
            //Note: We do not examine the exit code of the process. If the process is in a running
            //state and exits, then it is restarted. The only way a process in a running state
            //can exit is if it crashes, since when a process is explicitly stopped through the
            //node agent, it will go into a stopping state followed by a notRunning state. One 
            //issue encountered was the exitValue() would return non-zero for a process that was
            //in the middle of gracefully stopping on Unix systems.
            //if (pi.restartProcess() && pi.getExitCode() != 0) {
            if (pi.restartProcess()) {
                //We can only restart a process that is in the running state...Currently we have
                //a problem in that on Unix platforms instance that are stopped gracefully 
                //exit with a non-zero return code if pinged by the ProcessManagerMonitor
                //before being completely stopped. This can be reproduced by reducing the sleep
                //time in the montior and stopping an instance gracefully -- it will be restarted.
                //At this point our status should be running since it is checked upstream
                //before notifying, but its best to be on the safe side.                
                boolean wasRunning = InstanceStatus.getInstance().updateStatusIfStopped(instanceName, 
                    Status.kInstanceStartingCode);
                if (wasRunning) {
                    getLogger().log(Level.INFO, "processManager.restartingProcessInstance", instanceName);
                    try {                                        
                        // restart instance
                        pi.startInstance();
                        //Upon restart failure, we mark the instance as not running.
                        InstanceStatus.getInstance().updateStatusFromAdminChannel(instanceName, 
                            Status.kInstanceRunningCode);
                    } catch(ProcessManagerException e) {
                        //??? should log
                        e.printStackTrace();
                        //Upon restart failure, we mark the instance as not running. This will 
                        //allow it to be manually started again.
                        InstanceStatus.getInstance().updateStatusFromAdminChannel(instanceName, 
                            Status.kInstanceNotRunningCode);
                    }
                } else {
                    getLogger().log(Level.FINE, "ProcessManager: did not restart" + instanceName + 
                        " since it is " + 
                        InstanceStatus.getInstance().getStatus(instanceName).getStatus().getStatusString() + 
                        " and not running.");
                }
            }
        }
        
    }

    // **********************************************************************************************
    // **********************************************************************************************
    // protected INNER CLASS ProcessManagerMonitor
    // **********************************************************************************************
    // **********************************************************************************************
    /**
     * This class is responsible for monitoring the process that have been started by the ProcessManager.
     * Is the process is returned with a non-zero return code the process will restart.
     * This method of polling removes the Process.waitFor problem of killing every instance that is associated with
     * the ProcessManager is abruptly terminated.
     */
    class ProcessManagerMonitor extends Thread {
        //This controls how frequently we monitor crashed processes for restart.
        private int _sleepTime=5000;

        public void setSleepInterval(int sleepTime) {
            _sleepTime=sleepTime;
        }
        public int getSleepInterval() {
            return _sleepTime;
        }
        
        public void run() {
            getLogger().log(Level.FINE, "Start monitoring Processes in ProcessManager...");
            while(true) {
                try {
                    // loop continueously for monitoring of process
                    Iterator it=ProcessManager.getInstance().getProcesses().values().iterator();
                    Process process=null;
                    ProcessInstance pInstance=null;
                    int iExit=0;
                    while(it.hasNext()) {
                        pInstance=(ProcessInstance)it.next();
                        process=pInstance.getProcess();
                        if (process != null && !pInstance.isStopping()) {
                            // check to if process still going
                            
                            getLogger().log(Level.FINEST, "ProcessManagerMonitor checking server - " + pInstance.getName());                            
                            try {
                                iExit=process.exitValue();
                                getLogger().log(Level.FINEST, "exitValue returned - " + iExit);                                
                                //Note: We do not examine the exit code of the process. If the process is in a running
                                //state and exits, then it is restarted. The only way a process in a running state
                                //can exit is if it crashes, since when a process is explicitly stopped through the
                                //node agent, it will go into a stopping state followed by a notRunning state. One 
                                //issue encountered was the exitValue() would return non-zero for a process that was
                                //in the middle of gracefully stopping on Unix systems.
                                //if (iExit != 0) {
                                getLogger().log(Level.FINE, "Should Restart Process - " + pInstance.getName());                                    
                                //We can only restart a process that is in the running state...Currently we have
                                //a problem in that on Unix platforms instance that are stopped gracefully 
                                //exit with a non-zero return code if pinged by the ProcessManagerMonitor
                                //before being completely stopped. This can be reproduced by reducing the sleep
                                //time in the montior and stopping an instance gracefully -- it will be restarted.
                                RuntimeStatus status = InstanceStatus.getInstance().getStatus(pInstance.getName());
                                if (status.isStopped()) {
                                    // set for observer to check status and to setChanged flag so notification will be sent
                                    pInstance.setExitCode(iExit);
                                    // set process to null so ProcessManagerMonitor will
                                    // set notify the ProcessManager again
                                    // exitCode is already stored in ProcessInstance for restart check
                                    pInstance.setProcess(null);
                                    // notify the observers that something has changed after setting process to null
                                    // our jvm uses the same thread to perform notification which is not guarented sematics
                                    // but it was messing me up when it returned
                                    // TODO: Currently the notification & restart will be done on the polling thread, need to put on another 
                                    // thread so in case of catastrophic failures, servers can be restarted faster ???
                                    pInstance.notifyObservers();
                                } else {
                                    getLogger().log(Level.FINE, "ProcessManager: did not restart" + pInstance.getName() + 
                                        " since it is " + status.getStatus().getStatusString() + " and not running.");
                                }                                
                            } catch (IllegalThreadStateException itse) {
                                // ignore, because it will be the normal case that the process has
                                // not returned.
                                getLogger().log(Level.FINEST, "exitValue exception returned - " + itse.toString());                                
                            }
                        }
                    }

                    try {
                        sleep(_sleepTime);
                    } catch (InterruptedException ie) {
                        // ignore, doesn't matter whether is true time
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    
    
    // **********************************************************************************************
    // **********************************************************************************************
    // protected INNER CLASS ProcessManagerConfig
    // **********************************************************************************************
    // **********************************************************************************************
    
    /**
     * This class is responsible for loading class information for use when the ProcessManager is used as
     * a standalone component.
     * The configuration is in the format of :
     * 
     XML configuration for PM to read in process to start is in the following format
    <processes>
        <process name="NodeAgent">
            <!-- MAKING SUBSTITUTION AND REDIRECTION WORK for commands -->
            <!-- for windows all commands must start with a "cmd /c command_to_execute -->
            <!-- for unix all commands must start with a ""sh -c command_to_execute" -->
            <start-command>
                <command-line>/AppServer8/nodeagents/nodeagent1/agent/bin/startserv</command-line>
            </start-command>
            <stop-command>
                <command-line>/AppServer8/nodeagents/nodeagentw1/agent/bin/stopserv.bat</command-line>
            </stop-command>
            <environment-variable arg="AS_IMQ_LIB=C:\AppServer8\imq\lib"/>
            <environment-variable arg="AS_IMQ_BIN=C:\AppServer8\imq\bin"/>
            <environment-variable arg="AS_CONFIG=C:\AppServer8\config"/>
            <environment-variable arg="AS_INSTALL=C:\AppServer8"/>
            <environment-variable arg="AS_JAVA=C:\AppServer8\jdk"/>
            <environment-variable arg="AS_ACC_CONFIG=C:\AppServer8\domains\domain1\config\sun-acc.xml"/>
            <environment-variable arg="AS_LOCALE=en_US"/>
            <environment-variable arg="AS_DEF_DOMAINS_PATH=C:\AppServer8\domains"/>
            <working-dir directory="/AppServer8/nodeagent/nodeagentw1/agent"/>
        </process>
    </processes>    
    */
    protected class ProcessManagerConfig {
        private String _file=null;
        
        protected ProcessManagerConfig(String file) {
            _file=file;
        }
        
        protected String getFile() {
            return _file;
        }
        
        protected Hashtable initializeConfig() throws ProcessManagerException {
        
            Hashtable htRet=new Hashtable();
            
            try {
                // read in config
                Document doc=readDOM(getFile());

                // get processes
                String commandLine=null, key=null;
                ProcessInstanceExternal pInstance=null;
                Node childNode=null, grandNode=null;
                Element processElement=null, childElement=null;
                NodeList nl=doc.getElementsByTagName(PROCESS_COMMANDS);
                NodeList nlx=null;
                
                for(int ii=0;ii < nl.getLength(); ii++) {
                    
                    // create processInstance and put in vector of managed processes
                    pInstance=new ProcessInstanceExternal();
                    
                    // get process name
                    processElement=(Element)nl.item(ii);
                    key=processElement.getAttribute(PROCESS_NAME);
                    pInstance.setName(key);
                    if(bDebug) System.err.println(this.getClass().getName() + ": Creating process:" + pInstance.getName());

                    // loop through children of process
                    childNode=processElement.getFirstChild();
                    while (childNode != null) {
                    
                        if (childNode instanceof Element) {
                            childElement=(Element)childNode;
                            
                            //
                            // digest command into its parts and populate ProcessInstance
                            if(childElement.getTagName().equals(PROCESS_START_COMMAND)) {
                                addCommandLine(pInstance, childElement, ProcessInstance.START_COMMAND);
                            
                            } else if(childElement.getTagName().equals(PROCESS_STOP_COMMAND)) {
                                // get actual stop command
                                addCommandLine(pInstance, childElement, ProcessInstance.STOP_COMMAND);
                            
                            } else if(childElement.getTagName().equals(PROCESS_ENVIRONMENT_VARIABLE)) {
                                // get actual enviroment variable and add it to the command
                                pInstance.setEnvironmentVariable(childElement.getAttribute(PROCESS_ENVIRONMENT_ARG));
                                
                            } else if(childElement.getTagName().equals(PROCESS_WORKING_DIR)) {
                                // get actual working directory
                                pInstance.setWorkingDirectory(childElement.getAttribute(PROCESS_WORKING_DIR_FILE));
                            }
                        }
                        childNode=childNode.getNextSibling();
                    }
                    
                    if(bDebug) System.err.println(this.getClass().getName() + ": Process Created: " + pInstance);

                    // add object to return vector
                    htRet.put(key, pInstance);
                }
                

            } catch (Exception e) {
                throw new ProcessManagerException(e);
            }

            // return objects to ProcessManager
            return htRet;
        }
        
        
        /**
        * addCommandLine - adds command to processInstance
        *
        * @param pInstance - ProcessInstance to add line to
        * @param element - Element that has the children to load
        * @param type - Type of command to add
        */
        private void addCommandLine(ProcessInstanceExternal pInstance, Element element, String type) {
            // get actual command
            Node grandNode=null;
            String commandLine=null;
            NodeList nlx=element.getElementsByTagName(PROCESS_COMMAND_LINE);
            for(int jj=0;jj < nlx.getLength(); jj++) {

                // get childrem of command-line and look for text node
                grandNode=nlx.item(jj).getFirstChild();
                while(grandNode != null) {
                    if(grandNode instanceof Text) {
                        // should be command
                        commandLine=grandNode.getNodeValue();
                        //if(bDebug) System.err.println("command line added:" + commandLine);
                        pInstance.addCommandLine(type, commandLine);
                    }
                    grandNode=grandNode.getNextSibling();
                }
            }
        }
        
        
        /**
        * readDOM - This method reads in XML into a DOM
        *
        * @param file - A qualified file where to read the XML
        * @return Document - The read in DOM
        * @exception - Any thrown exception that may occur during the read process
        */
        protected Document readDOM(String file) throws SAXException, ParserConfigurationException, IOException {
            DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
            DocumentBuilder db=dbf.newDocumentBuilder();
            return db.parse(file);
        }
        
    }
}
