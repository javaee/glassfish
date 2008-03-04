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

package com.sun.enterprise.admin.servermgmt.pe;

import com.sun.enterprise.admin.event.AdminEventResult;
import com.sun.enterprise.admin.event.ShutdownEvent;
import com.sun.enterprise.admin.servermgmt.launch.ASLauncherException;
import com.sun.enterprise.admin.servermgmt.launch.ASNativeLauncher;
import com.sun.enterprise.admin.servermgmt.launch.LaunchConstants;
import com.sun.enterprise.util.system.GFSystem;
import java.io.*;
import java.util.*;
import java.lang.reflect.Method;

import com.sun.enterprise.util.JvmInfoUtil;
import com.sun.enterprise.util.ProcessExecutor;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.admin.servermgmt.InstancesManager;
import com.sun.enterprise.admin.servermgmt.InstanceException;
import com.sun.enterprise.admin.servermgmt.pe.InstanceTimer;
import com.sun.enterprise.admin.servermgmt.pe.TimerCallback;

import com.sun.enterprise.admin.servermgmt.RepositoryConfig;
import com.sun.enterprise.admin.servermgmt.RepositoryManager;
import com.sun.enterprise.admin.servermgmt.DomainConfig;
import com.sun.enterprise.admin.common.Status;
import com.sun.enterprise.admin.server.core.channel.RMIClient;
import com.sun.enterprise.admin.servermgmt.launch.ASLauncher;

/**
 */
public class PEInstancesManager extends RepositoryManager implements InstancesManager
{
    protected enum InstanceType { domain, nodeAgent };
    
    protected static final int NO_PROCESS = -1;
    /**
     * i18n strings manager object
     */
    private static final StringManager _strMgr =
        StringManager.getManager(PEInstancesManager.class);

    private final           RepositoryConfig    _config;
    private                 Properties          systemPropsCopy;
    private                 String[]            securityInfo;
    private                 boolean             verbose         = false;
    private                 boolean             debug           = true;
    private                 boolean             nativeLauncher  = false;
    private                 Properties          envProps;
    
    public PEInstancesManager(RepositoryConfig config)
    {
        super();
        _config = config;
    }

    public String getNativeName()
    {
        return "appservDAS";
    }

    protected RepositoryConfig getConfig()
    {
        return _config;
    }

    public void createInstance()
        throws InstanceException
    {
        throw new UnsupportedOperationException(
            _strMgr.getString("notSupported"));
    }

    public void deleteInstance() throws InstanceException
    {
        throw new UnsupportedOperationException(
            _strMgr.getString("notSupported"));
    }

    /**
     * startInstance method to use if there are security strings (user]pw/master-pw),
     * args, and the env props to set.
     *
     * @param SecurityInfo username and passwords
     * @param commandLine arguments to be appended to the executing process' commandline
     * @param EnvProps properties that need to be set in System so that ASLauncher knows what's going on
     * @return The Process that is being excecuted
     * @throws InstanceException
     */

    public Process startInstance(String[] SecurityInfo, String[] commandLineArgs, Properties EnvProps)
        throws InstanceException
    {
        Process process = null;
        
        securityInfo = SecurityInfo;
        envProps = EnvProps;
        
        if(envProps == null)
            envProps = new Properties();    // I don't want to check for null a zillion times!
        
        nativeLauncher = Boolean.getBoolean(SystemPropertyConstants.NATIVE_LAUNCHER);
        
        // WBN TURN OFF OLD CODE BY DEFAULT
        String s = System.getenv("OLD_LAUNCHER");

        if(s != null && s.equals("true"))
            return startInstanceUsingScript(commandLineArgs);

		// synchronize across ALL instances of this class
        synchronized(getClass())
        {
            // EVERYTHING between saving & restoring system properties must be 
            // protected from other threads.
            
            try
            {
                saveSystemProps();

                /*
                if(nativeLauncher)
                    return startInstanceUsingScript(commandLineArgs);
                else
                 */
                 process = startInstanceAllJava(commandLineArgs);
            }
            finally
            {
                restoreSystemProps();
            }
        }
        
        if(!verbose || nativeLauncher)
        {
            waitUntilStarting(null);
            waitUntilStarted();
            postStart();
        }
        
        return process;
    }

    public Process startInstance(String[] interativeOptions, String[] commandLineArgs)
        throws InstanceException
    {
        return startInstance(interativeOptions, commandLineArgs, null);
    }
    

    /**
     * startInstance method to use if there aren't any interactiveOptions or
     * args to append to the executing process
     *
     * @return The Process that is being excecuted
     * @throws InstanceException
     */
    public Process startInstance()  throws InstanceException {
        return startInstance(null, null);
    }


    /**
     * startInstance method to use if there are interactiveOption, but no
     * args to append to the executing process
     *
     * @param interativeOptions that are passed into the executing process' standard input
     * @return The Process that is being excecuted
     * @throws InstanceException
     */
    public Process startInstance(String[] interativeOptions)  throws InstanceException {
        String[] commandLineArgs=null;
        return startInstance(interativeOptions, commandLineArgs);
    }


    /**
     * startInstance method to use if there are interactiveOption and
     * args to append to the executing process
     *
     * @param commandLine arguments to be appended to the executing process' commandline
     * @return The Process that is being excecuted
     * @throws InstanceException
     */
    private Process startInstanceAllJava(String[] commandLineArgs)
        throws InstanceException
    {
        Process process = null;
        try
        {
            // Invoke launcher directly without running startserv script.
            // This saves a JVM
            
            verbose         = false;
            debug           = false;

            preStart();
            
            if(isTrue((Boolean)getConfig().get(DomainConfig.K_VERBOSE)))
                verbose = true;;

            if(isTrue((Boolean)getConfig().get(DomainConfig.K_DEBUG)))
                debug = true;

            setSecurity();
            ArrayList<String> args = new ArrayList<String>();
            args.add("start");

            if(nativeLauncher)
                args.add("display");

            if ( debug ) 
                args.add("debug");
            
            if ( verbose ) 
                args.add("verbose");
            
            if (commandLineArgs != null) 
            {
                for (String arg : commandLineArgs)
                    if(!args.contains(arg)) // no duplicates
                        args.add(arg);
            }
            
            String[] launcherArgs = args.toArray(new String[args.size()]);
            
            GFSystem.setProperty(SystemPropertyConstants.INSTANCE_ROOT_PROPERTY,
                getConfig().getRepositoryRoot() + File.separator +
                getConfig().getRepositoryName());
        
            if(nativeLauncher)
            {
                ASNativeLauncher launcher = new ASNativeLauncher(this);
                launcher.preProcess(launcherArgs, envProps);
                try
                {
                    process = launcher.launch(launcherArgs, securityInfo);
                }
                catch(ASLauncherException e)
                {
                    throw new InstanceException(e);
                }
            }
            else
            {
                ASLauncher launcher = new ASLauncher();
                launcher.setRefreshConfigContext(_config.getRefreshConfigContext());
                launcher.preProcess(launcherArgs, envProps);
                process = launcher.process(launcherArgs, securityInfo);
            }
        }
        catch ( Exception ex ) 
        {
            throw new InstanceException(
                getMessages().getInstanceStartupExceptionMessage(
                getConfig().getDisplayName()), ex);
        }

        return process;
    }

    /**
     * Ancient code that uses startserv script
     */
    private Process startInstanceUsingScript(String[] commandLineArgs)
        throws InstanceException
    {
        preStart();

	// check if --debug or --verbose options were given to asadmin
	Boolean v = (Boolean)getConfig().get(DomainConfig.K_VERBOSE);
	verbose = false;
	if ( v != null ) {
	    verbose = v.booleanValue();
	}

	Boolean d = (Boolean)getConfig().get(DomainConfig.K_DEBUG);
        debug = false;
	if ( d != null ) {
	    debug = d.booleanValue();
	}

        String[] command=null;
        File script = getFileLayout(getConfig()).getStartServ();
        Process process=null;
        ArrayList alCmd=new ArrayList();
        // append required arguments to start command
        alCmd.add(script.getAbsolutePath());

        // WBN - we need to distinguish CLI calls from commandline calls
        alCmd.add("cli");

        //
        // add temporary switch for new ProcessLauncher. Will optimize this section
        // better once the commons-launcher is removed ???

        // This will be removed once PE using the new invocation classes
	if ( System.getProperty("com.sun.aas.processLauncher") == null && verbose ) {
	    try {
		// Invoke launcher directly without running startserv script.
		// This saves a JVM, and will allow CTRL-C and CTRL-\ on asadmin
		// to reach the server JVM.

		// Set system props needed by launcher
		GFSystem.setProperty(SystemPropertyConstants.INSTANCE_ROOT_PROPERTY,
                    getConfig().getRepositoryRoot() + File.separator +
                    getConfig().getRepositoryName());
		ArrayList args = new ArrayList();
		args.add("s1as-server");
                //FIXTHIS: The com.sun.aas.instanceName probably needs to be dynamically set, but for
                //now this is not important as it is not being used.
		args.add("-Dcom.sun.aas.instanceName=server");
		args.add("start");
		if ( debug ) {
		    args.add("debug");
		}
		if ( verbose ) {
		    args.add("verbose");
		}

                // addin command line args
                if (commandLineArgs != null) {
                    for(int ii=0; ii < commandLineArgs.length; ii++) {
                        args.add(commandLineArgs[ii]);
                    }
                }

                // extract full command
		String[] argStrings = (String[])args.toArray(
                    new String[args.size()]);

		/* This doesnt work with the JDK1.4.2 javac
		LauncherBootstrap.main(argStrings);
		*/
		Class launcherClass = Class.forName("LauncherBootstrap");
		Class[] paramClasses = new Class[] { String[].class };
		Object[] argsArray = new Object[] { argStrings };
		Method mainMethod = launcherClass.getMethod("main", paramClasses);

		// If verbose, LauncherBootstrap.main() returns only after
		// the appserver JVM exits.
		mainMethod.invoke(null, argsArray);

	    } catch ( Exception ex ) {
		throw new InstanceException(
		    getMessages().getInstanceStartupExceptionMessage(
			getConfig().getDisplayName()), ex);
	    }

    } else if (System.getProperty("com.sun.aas.processLauncher") != null && verbose) {
        // add arguments to main command, native must come first
        if (nativeLauncher) {
            // use native launcher, add in argument
            alCmd.add("native");
        }
        if (debug) alCmd.add("debug");
        if (verbose) alCmd.add("verbose");
        // addin command line args
        if (commandLineArgs != null) {
            for(int ii=0; ii < commandLineArgs.length; ii++) {
                alCmd.add(commandLineArgs[ii]);
            }
        }

        // extract full command
        command=new String[alCmd.size()];
        command=(String[])alCmd.toArray(command);

        try {
            // exec process directly to exercise needed control
            ProcessExecutor exec = new ProcessExecutor(command, securityInfo);
            // set verbose flag so process error stream get redirected to stderr
            exec.setVerbose(verbose);
            // call execute so it will not be timed out
            exec.execute(false, false);
            process=exec.getSubProcess();
            // this will force process to wait for executing process
            int exitValue=process.waitFor();
            System.exit(exitValue);
        } catch (Exception e) {
            throw new InstanceException(_strMgr.getString("procExecError"), e);
        }

    } else {
        // add arguments to main command, native must come first
        if (nativeLauncher) {
            // use native launcher, add in argument
            alCmd.add("native");
        }

        // check to see if debug is enabled
        if (debug) {
            alCmd.add("debug");
        }

        // addin command line args
        if (commandLineArgs != null) {
            for(int ii=0; ii < commandLineArgs.length; ii++) {
                alCmd.add(commandLineArgs[ii]);
            }
        }

        // extract full command
        command=new String[alCmd.size()];
        command=(String[])alCmd.toArray(command);

        // call method for executing so can be overriden in descendants
        // also, keep executor for any error information
        ProcessExecutor processExec=startInstanceExecute(command, securityInfo);
        process=processExec.getSubProcess();
	    waitUntilStarting(processExec);
	    waitUntilStarted();
        postStart();
	}

        return process;
    }

    /**
    * This method is called internally from the startInstance method
    * and was needed so SE could execute a process that doesn't return
    */
    
    protected ProcessExecutor startInstanceExecute(String[] command, String[] SecurityInfo) throws InstanceException {
        return execute(command, SecurityInfo);
    }


    public void stopInstance() throws InstanceException
    {
        if(!isStoppable())
            return;

        String instanceName = getConfig().getInstanceName();
        
        // domains set the instance name to "server" -- but not in the RepositoryConfig
        if(!ok(instanceName))
            instanceName = "server";
        
	    ShutdownEvent shutdownEvent = new ShutdownEvent(instanceName);
	    AdminEventResult result = getRMIClient().sendNotification(shutdownEvent);
        waitUntilStopped(60);
        postStop();
    }

    public boolean stopInstanceWithinTime(int timeout) throws InstanceException
    {
        if(!isStoppable())
            return true;

        String instanceName = getConfig().getInstanceName();
        
        // domains set the instance name to "server" -- but not in the RepositoryConfig
        if(!ok(instanceName))
            instanceName = "server";
        
	ShutdownEvent shutdownEvent = new ShutdownEvent(instanceName);
	AdminEventResult result = getRMIClient().sendNotification(shutdownEvent);
        waitUntilStopped(timeout);
        if (isInstanceNotRunning()) {
            postStop();
            return true;
        } else return false;
    }
    
    public void stopInstanceScript() throws InstanceException
    {
        if(!isStoppable())
            return;

        execute(getFileLayout(getConfig()).getStopServ());        
        waitUntilStopped(60);
        postStop();
    }

    public String[] listInstances() throws InstanceException
    {
        throw new UnsupportedOperationException(
            _strMgr.getString("notSupported"));
    }

    public boolean isInstanceStarting() throws InstanceException
    {
        return (getInstanceStatus() ==
             Status.kInstanceStartingCode);
    }

    public boolean isInstanceRunning() throws InstanceException
    {
        return (Status.kInstanceRunningCode ==
                getInstanceStatus());
    }

    /**
     * Returns whether the server is in the startup failed state or
     * not.
     */
    public boolean isInstanceFailed() throws InstanceException
    {
        return (Status.kInstanceFailedCode ==
                getInstanceStatus());
    }

    public boolean isInstanceNotRunning() throws InstanceException
    {
        return (Status.kInstanceNotRunningCode ==
                getInstanceStatus());
    }

    public boolean isRestartNeeded() throws InstanceException
    {
        boolean isRestartNeeded = false;
        try
        {
            isRestartNeeded = getRMIClient().isRestartNeeded();
        }
        catch (Exception e)
        {
            throw new InstanceException(e.getMessage(), e);
        }
        return isRestartNeeded;
    }

    /**
        To start an instance the instance must be in not running state.
     */
    protected void preStart() throws InstanceException
    {
        final int state = getInstanceStatus();
        if (Status.kInstanceNotRunningCode != state)
        {
            throw new InstanceException(
                getMessages().getCannotStartInstanceInvalidStateMessage(
                    getConfig().getDisplayName(),
                    Status.getStatusString(state)));
        }
    }

    void postStart() throws InstanceException
    {
        if (isInstanceFailed()) {
            int port = getConflictedPort();
            abortServer();
            throw new InstanceException(
                getMessages().getStartupFailedMessage(
                    getConfig().getDisplayName(), port ));
        }
        if (!isInstanceRunning() &&
            !isInstanceNotRunning())
        {
            /*
                Instance could not be started in TIME_OUT_SECONDS.
                Trying to stop.
             */
            try {
                stopInstance();
            } catch (Exception e) {
                throw new InstanceException(
                    getMessages().getStartInstanceTimeOutMessage(
                        getConfig().getDisplayName()), e);
            }
            throw new InstanceException(
               getMessages().getStartInstanceTimeOutMessage(
                   getConfig().getDisplayName()));
        }
        if (isInstanceNotRunning()) {
            throw new InstanceException(
                getMessages().getStartupFailedMessage(
                    getConfig().getDisplayName()));
        }
        setRMIClient(null);
    }

    /**
        To stop an instance the instance must be in (running | stopping |
        starting) state.
     */
    private boolean isStoppable() throws InstanceException
    {
        final int state = getInstanceStatus();
        
        return 
            state == Status.kInstanceRunningCode || 
            state == Status.kInstanceStartingCode;
    }

    void postStop() throws InstanceException
    {
        if (!isInstanceNotRunning())
        {
            throw new InstanceException(
                getMessages().getCannotStopInstanceMessage(
                    getConfig().getDisplayName()));
        }
        setRMIClient(null);
    }


    // method to over ride timeout time for server to go to starting state
    // This value should be configurable by end user ???
    protected void waitUntilStarting(ProcessExecutor processExec) throws InstanceException
    {
        waitUntilStarting(processExec, 180);
    }

    protected void waitUntilStarting(ProcessExecutor processExec, int timeoutSeconds) throws InstanceException
    {
        InstanceTimer timer = new InstanceTimer(timeoutSeconds, 0,
            new TimerCallback()
            {
                public boolean check() throws Exception
                {
                    return isInstanceStarting() ||
                           isInstanceRunning()  ||
                           isInstanceFailed();
                }
            }
        );
        timer.run(); //synchronous


        if (getInstanceStatus() == Status.kInstanceNotRunningCode)
        {
            throw new InstanceException(
                getMessages().getTimeoutStartingMessage(
                    getConfig().getDisplayName()));
        }
    }

    // method to over ride timeout time for server to started state
    // Also this value should be configurable by end user ???
    protected void waitUntilStarted() throws InstanceException
    {
        // PE set to 20 minutes
        waitUntilStarted(1200);
    }


    protected void waitUntilStarted(int timeoutSeconds) throws InstanceException
    {
        InstanceTimer timer = new InstanceTimer(timeoutSeconds, 0,
            new TimerCallback()
            {
                public boolean check() throws Exception
                {
                    return (isInstanceRunning() ||
                            isInstanceFailed()  ||
                            isInstanceNotRunning());
                }
            }
        );
        timer.run(); //synchronous
    }

    void waitUntilStopped(int timeout) throws InstanceException
    {
        InstanceTimer timer = new InstanceTimer(timeout, 0,
            new TimerCallback()
            {
                public boolean check() throws Exception
                {
                    return isInstanceNotRunning();
                }
            }
        );
        timer.run(); //synchronous
    }

    void execute(File script) throws InstanceException
    {
        try
        {
            ProcessExecutor exec = new ProcessExecutor(
                                   new String[] {script.getAbsolutePath()});
            exec.execute();
        }
        catch (Exception e)
        {
            throw new InstanceException(_strMgr.getString("procExecError"), e);
        }
    }

    ProcessExecutor execute(String[] command) throws InstanceException {
        return execute(command, null);
    }

    ProcessExecutor execute(String[] command, String[] interativeOptions) throws InstanceException
    {
        try
        {
            ProcessExecutor exec = new ProcessExecutor(command, interativeOptions);
            if (nativeLauncher) {
                // native processes don't return, so don't wait
                // this follows the methodology for se, but should be revisted to make
                // sure timeouts for state transitions are reasonable
                exec.execute(false, false);
            } else {
                // expect the process to return
                exec.execute();
            }


            // this signature for execute will terminiate the process
            // if it goes on too long, reason for return signature is for SE ProcessManager watchdog
            return exec;
        }
        catch (Exception e)
        {
            throw new InstanceException(_strMgr.getString("procExecError"), e);
        }
    }

    protected PEFileLayout getFileLayout()
    {
        return super.getFileLayout(getConfig());
    }

    /**
     * Get the port on which conflict occured.
     */
    int getConflictedPort() {
        int port = 0;
        try {
            port = getRMIClient().getConflictedPort();
        }
        catch (Exception ex) {
            // Not interested!!
        }
        return port;
    }

    /**
     * AS PE will wait before exiting, until client executes this method.
     */
    void abortServer() {
        try {
            getRMIClient().triggerServerExit();
        }
        catch (Throwable t) {
            //Even if there is an error, that should not affect client performance. 
            //Ignore the error.
        }
    }

    public int getInstanceStatus() throws InstanceException
    {
        int status = Integer.MIN_VALUE;
        try {
            status = getRMIClient().getInstanceStatusCode();
        }
        catch (Exception ex) {
            /**
             * There is a timimg issue here while reading the admsn file.
             * If the client attempts to rmi the server before the admsn
             * is created an exception will be thrown. Supressing the exception
             * so that the client will be able to rmi the server in the next
             * attempt once the admsn was created.
             */
            //throw new InstanceException(ex);
            //log this??
        }
        return status;
    }

    public void killRelatedProcesses() throws InstanceException 
    {
        List<String> pids = getPIDsToBeKilled();
        // kill the hanging process
        if (!pids.isEmpty())  {
            int exitValue = executeKillServ(pids);
	    if (debug) System.out.println("Exit value is = " + exitValue);
            if (exitValue != 0) 
                throw new InstanceException(
                    getMessages().getCannotStopInstanceMessage(
                        getConfig().getDisplayName()));            
        } else {
            throw new InstanceException(_strMgr.getString("noPidsToKill"));
        }
    }
    
    /**
     * @return array of integer PIDs - For DAS and each Server instance this 
     * this will contain a single PID of the VM of the server - DAS or instance
     * But for Node Agent the list will contain pids of all managed instances
     * @see AgentManager.getPIDsToBeKilled
     */
    protected List<String> getPIDsToBeKilled() throws InstanceException 
    {
        List<String> pids = new ArrayList<String>();
        int pid = NO_PROCESS;
        
        File pidFileDir = getFileLayout().getConfigRoot();
        File pidFile = new File(pidFileDir, SystemPropertyConstants.PID_FILE);

        if (! isPIDFileValid(pidFileDir, pidFile)) 
            return pids;
        
        pid = JvmInfoUtil.getPIDfromFileAndDelete(pidFile);
        // 2. check if file has latest pid - TODO
        if (pid != NO_PROCESS)  pids.add(""+pid);
        return pids;
    }
    
    protected boolean isPIDFileValid(File pidFileDir, File pidFile) {
        // Before reading it and executing scripts using the pids in that 
        // file, compare the timestamp on this file and the admsn file in 
        // the same folder. Why compare with admsn? Because I need to make 
        // sure that this pid file was indeed updated when the VM came up
        // If it was not then we should not kill some random processes with
        // pids corresponding to the stale pids in the file. And to effect 
        // that I have to use some relative timestamp - in this case it is 
        // that of admsn. Also admsn does not get deleted during VM stop
        // It was important to choose a file (for reference timestamp) that 
        // will not be deleted during VM stop. Otherwise the --kill option
        // will be useless if the VM hung after deleting the reference file
        // during the stop. We need the --kill option exactly in this 
        // scenario. Hence the choice of admsn

        File refTSFile = new File(pidFileDir, SystemPropertyConstants.REF_TS_FILE);
        if (pidFile.lastModified() < refTSFile.lastModified()) 
            return false;
        return true;
    }

    protected int executeKillServ(List<String> pidList) throws InstanceException 
    {
	if (debug) System.out.println("PID fed to killserv is = " + pidList);
        try {
            String killServScript = fetchKillServ();
            
            List<String> cmds = new ArrayList<String>();
            cmds.add(killServScript);
            cmds.addAll(pidList);
            ProcessBuilder pb = new ProcessBuilder(cmds);
            Process killer = pb.start();
            return killer.waitFor();
        } catch (Exception ex) {
            throw new InstanceException(ex);
        }
    }
    
    private String fetchKillServ() throws Exception {
        String killServScript =
            getConfig().getRepositoryRoot() + File.separator + 
            getConfig().getRepositoryName() + File.separator;
        if (getConfig().getInstanceName() != null) 
            killServScript += getConfig().getInstanceName() + File.separator;
        killServScript += "bin" + File.separator + PEFileLayout.KILL_SERV_OS;        
        
        File killServ = new File(killServScript);
        
        if (killServ.exists()) return killServScript;
        
        String  installLibKillServ = 
            getFileLayout().getInstallRootDir().getAbsolutePath() + 
            File.separator + PEFileLayout.LIB_DIR + File.separator + 
            PEFileLayout.KILL_SERV_OS;
        
        return installLibKillServ;
    }
    
    private RMIClient rmiClient = null;

    private RMIClient getRMIClient()
    {
        if (rmiClient == null)
        {
            String stubPath = getFileLayout().getStubFile().getAbsolutePath();
            String seedPath = getFileLayout().getSeedFile().getAbsolutePath();
            rmiClient = new RMIClient(false, stubPath, seedPath);
        }
        return rmiClient;
    }

    private void setRMIClient(RMIClient client)
    {
        this.rmiClient = client;
    }

    // WBN
    private void saveSystemProps()
    {
        systemPropsCopy = new Properties();
        systemPropsCopy.putAll(System.getProperties());
    }

    private void restoreSystemProps()
    {
        GFSystem.setProperties(systemPropsCopy);
    }

    private boolean isTrue(Boolean b)
    {
        return Boolean.TRUE.equals(b);
    }

    private boolean ok(String s)
    {
        return s != null && s.length() > 0;
    }
    
    private void setSecurity()
    {
        // Ref: Issue 1558
        // if the username, admin password and master password  are all empty or 
        // null strings, don't mess with writing to the stdin of
        // the AS process eventually.
        // Note that this case never happens currently. The master password will always
        // be set to something.
        
        boolean gotAtLeastOne = 
                securityInfo != null        && 
                securityInfo.length >=3     &&
                (ok(securityInfo[0]) || ok(securityInfo[1]) || ok(securityInfo[2]));

        if(gotAtLeastOne)
        {
            GFSystem.setProperty("com.sun.aas.promptForIdentity", "true");                    
        }
        else
        {
            // don't set com.sun.aas.promptForIdentity to false -- it may be seen as true anyways!!
            securityInfo = null;
        }
    }
}
