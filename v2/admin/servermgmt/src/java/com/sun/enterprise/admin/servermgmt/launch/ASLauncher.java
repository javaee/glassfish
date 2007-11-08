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

/**
 * THIS CODE IS THREAD UNSAFE
 */

package com.sun.enterprise.admin.servermgmt.launch;

import com.sun.enterprise.util.io.FileUtils;
import java.io.FileWriter;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.BufferedWriter;
import java.io.PushbackInputStream;
import java.lang.InterruptedException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.SimpleFormatter;
import java.util.List;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;

import com.sun.logging.LogDomains;
import com.sun.enterprise.util.OS;
import com.sun.enterprise.util.RelativePathResolver;
import com.sun.enterprise.util.ASenvPropertyReader;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.ProcessExecutor;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.NodeAgent;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.JavaConfig;
import com.sun.enterprise.config.serverbeans.LogService;
import com.sun.enterprise.config.serverbeans.Profiler;
import com.sun.enterprise.config.serverbeans.ElementProperty;
import com.sun.enterprise.config.serverbeans.SystemProperty;
import com.sun.enterprise.config.serverbeans.ConfigAPIHelper;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.NodeAgentHelper;
import com.sun.enterprise.config.serverbeans.ClusterHelper;
import com.sun.enterprise.util.natives.NativeUtils;
import com.sun.enterprise.util.system.GFSystem;
import com.sun.enterprise.security.store.IdentityManager;
import com.sun.enterprise.admin.util.JvmOptionsHelper;

/**
 * This is the main class for the new ProcessLauncher which is responcible for creating and
 * executing a java command.  There are two general types of commands, internal and external.
 * An internal command is basically one that is mostly defined by domain.xml.  The command's
 * java-config (classpath, jvm option, system properties, debug info, ...) is extracted from
 * domain.xml using the config-api who's information is coupled with more general information
 * from the processLauncher.xml file.  How the final command is executed is based on the
 * LaunchConstants.LAUNCHER_RETURN_PROP system property:
 *
 * LaunchConstants.LAUNCHER_RETURN_RETURN_VALUE - denotes that the command is executed via the Runtime .exec and immediately returns.
 * If the "verbose" option is present as an argurment then the processes' stdout and stderr is
 * attached to and sent to the stderr of the calling process.
 *
 * "hold" - denotes that the command is written to a temporary script in the calling scripts
 * directory, then once the ProcessLauncher's java command returns the script is executed as the
 * next command in the script and doesn't return until the temportary script finishes execution.
 */

public class ASLauncher
{
    
    protected static final String RELATIVE_LOCATION_DOMAIN_XML="/config/domain.xml";
    protected static final String CLASSPATH_ENV_NAME="CLASSPATH";
    protected static final String LAUNCHER_SCRIPT_LOCATION="bin";
    protected static final String LAUNCHER_START_ACTION="start";
    protected static final String LAUNCHER_STOP_ACTION="stop";
    protected static final String INTERNAL_SERVER_PROFILE="s1as8-server";
    protected static final String AS9_INTERNAL_SERVER_PROFILE="as9-server";
    protected static final String INTERNAL_NODE_AGENT_PROFILE="s1as8-nodeagent";
    protected static final int SLEEP_TIME_FOR_PROCESS_START=2000;
    protected static final String COMMAND_LINE_ARG_VERBOSE="verbose";
    protected static final String COMMAND_LINE_ARG_DEBUG="debug";
    protected static final String COMMAND_LINE_ARG_DISPLAY="display";
    protected static final String COMMAND_LINE_ARG_NATIVE="native";
    protected static final char COMMAND_DELIMITER_LIST[] = {'|','+','^','@','!','?','*','%','$','#','(',')','~','`','{','}','[',']','<','>', 127};
    public static final String DEBUG_OPTIONS = "com.sun.aas.jdwpOptions";
    public static final String VERBOSE_SYSTEM_PROPERTY = "com.sun.aas.verboseMode";
    public static final String LOGFILE_SYSTEM_PROPERTY = "com.sun.aas.defaultLogFile";
    public static final String PROPMPT_FOR_IDENTITY_SYSTEM_PROPERTY = "com.sun.aas.promptForIdentity";
    public static final String SPARC = "sparc";
    public static final String SPARCV9 = "sparcv9";
    public static final String X86 = "x86";
    public static final String AMD64 = "amd64";
    public static final String JVM_OPTION_FOR_64BIT = "-d64";
    
    private static final String CLASSPATH_PREFIX_PROPERTY = "com.sun.aas.ClassPathPrefix";
    private static final String CLASSPATH_SUFFIX_PROPERTY = "com.sun.aas.ClassPathSuffix";
    private static final String SERVER_CLASSPATH_PROPERTY = "com.sun.aas.ServerClassPath";
    private static final String UNDEFINED_ACTION = "undefined_action";
    private static final String ERROR_FLUSHER    = "Error Stream Flusher";
    private static final String OUT_FLUSHER    = "Output Stream Flusher";
    private static final String DOMAIN_NAME_XML_PROPERTY = "administrative.domain.name";
    private static final String DOMAIN_NAME_SYSTEM_PROPERTY = "domain.name";
    private static final String SCRIPT_PATH_SYSTEM_PROPERTY = "com.sun.aas.scriptpath";
    
    int    _returnValue = 1;
    String[]    securityInfo;   // username & passwords if neccessary...
    
    // WBN the level is here as a constant so that it can be changed to INFO or better
    // when actively debugging in order to see the messages easily.  Always return to FINE
    // when finished.
    private static final Level  FINE_LEVEL = Level.FINE;
    private Logger _logger = null;
    private String[] _args=null;
    private static boolean bDebug=false;
    
    /* Refresh the config context from the disk, by default */
    private boolean refreshConfigContext = true;
    
    // WBN March 2007
    // The server's logfile is added to the *local* logger.  But it is never
    // removed.  The files are kept open by the logger.  One really bad result
    // is that Windows will not be able to delete that server after stopping
    // it.
    // Solution keep track of what was added and then remove it when finished.
    FileHandler externalLogfileHandler;
    

    // WBN May 18, 2007 appserv-launch.jar removed from config's system-classpath'
    private static final String APPSERV_LAUNCH_JAR = "appserv-launch.jar";
    
    public static void main(String[] args)
    {
        int returnValue = 1;
        
        ASLauncher pl=null;
        try
        {
            pl=new ASLauncher();
            pl.preProcess(args);    // figure out relocatable stuff
            pl.getSecurityFromUser(args);
            Process process = pl.process(args, pl.securityInfo);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if ( pl != null )
            {
                returnValue = pl.getReturnValue();
            }
        }
        
        System.exit(returnValue);
    }
    
    int getReturnValue()
    {
        return _returnValue;
    }
    
    /**
     * This method is meant to be called from the PLBootstrap.class to finish the setup
     * of the ProcessLauncher to execute normally
     *
     * arg[0] - should be the name of the profile to use from processLauncher.xml
     * arg[1] - should be the action to run (default is "start")
     */
    public static void bootstrap(String[] args)
    {
        ASLauncher pl=new ASLauncher();
        
        // set to nodeagent, by default
        String profile="s1as-deploytool";
        if (args != null && args.length >= 1)
        {
            // set to argument profile
            profile=args[0];
            
            // shift args
            String[] newArgs=new String[args.length - 1];
            for (int ii=0; ii < newArgs.length; ii++)
            {
                newArgs[ii]=args[ii + 1];
            }
            
            // set back to args
            args=newArgs;
        }
        
        // put profile in properties for later use
        GFSystem.setProperty(LaunchConstants.PROCESS_NAME_PROP, profile);
        
        if (bDebug) System.out.println("bootstrapping profile - " + profile);
        
        // start processing
        pl.process(args);
    }
    /**
     * WBN Oct 2006 -- The startserv script has been made relocatable.  But we have to be backward
     * compatible.  So look for the new args and preprocess them...
     * Adding this allows us to remove lots of unchanging stuff from the script.
     */
    public void preProcess(String[] args, Properties extraProps) throws ASLauncherException
    {
        boolean nativeLauncher = false;
        boolean verbose = false;
        
        readASEnv();
        
        for(String arg : args)
        {
            if("display".equalsIgnoreCase(arg))
                nativeLauncher = true;
            
            if("verbose".equalsIgnoreCase(arg))
                verbose = true;
        }
        
        // Set system props needed by launcher
        // first add all the ones that were passed in from a caller...
        
        Enumeration keys = extraProps.propertyNames();
        
        while(keys.hasMoreElements())
        {
            String key = (String)keys.nextElement();
            String value = extraProps.getProperty(key);
            GFSystem.setProperty(key, value);
        }
        
        
        // set props but only if NOT in extraProps already!
        setInstanceRoot(extraProps);
        
        if(extraProps.getProperty(LaunchConstants.JAVA_HOME_PROP) == null)
            GFSystem.setProperty(LaunchConstants.JAVA_HOME_PROP, System.getProperty(SystemPropertyConstants.JAVA_ROOT_PROPERTY));
        if(extraProps.getProperty(LaunchConstants.LOG_MGR_PROP) == null)
            GFSystem.setProperty(LaunchConstants.LOG_MGR_PROP, LaunchConstants.LOG_MGR_VALUE);
        if(extraProps.getProperty(LaunchConstants.ENV_FACTORY_PROP) == null)
            GFSystem.setProperty(LaunchConstants.ENV_FACTORY_PROP, LaunchConstants.ENV_FACTORY_VALUE);
        if(extraProps.getProperty(LaunchConstants.PROCESS_NAME_PROP) == null)
            GFSystem.setProperty(LaunchConstants.PROCESS_NAME_PROP, AS9_INTERNAL_SERVER_PROFILE);
        if( extraProps.getProperty(LaunchConstants.LAUNCHER_RETURN_PROP) == null)
        {
            if(verbose && nativeLauncher)
                GFSystem.setProperty(LaunchConstants.LAUNCHER_RETURN_PROP, LaunchConstants.LAUNCHER_RETURN_HOLD_VALUE);
            else
                GFSystem.setProperty(LaunchConstants.LAUNCHER_RETURN_PROP, LaunchConstants.LAUNCHER_RETURN_RETURN_VALUE);
        }
        
    }
    
    public void setRefreshConfigContext(final boolean refresh) {
        this.refreshConfigContext = refresh;
    }
    
    public boolean getRefreshConfigContext() {
        return ( refreshConfigContext );
    }
    
    void preProcess(String[] args) throws ASLauncherException
    {
        // We are being called from a script or native code
        // i.e. we are in our own JVM and we need to look at System Properties
        // and repackage them for the public preprocess method
        // These are the ONLY properties needed to start any kind of server.
        // this allows us to change the script to pass these in as args instead
        // of System Properties later -- by changing this code and the scripts at
        // the same time.
        
        // these Strings ugly-up the code so let's make nice neat abbreviations here!
        String irpTag       = SystemPropertyConstants.INSTANCE_ROOT_PROPERTY;
        String procNameTag  = LaunchConstants.PROCESS_NAME_PROP;
        String inTag        = SystemPropertyConstants.SERVER_NAME;
        String rootTag      = SystemPropertyConstants.INSTALL_ROOT_PROPERTY;
        String retTag       = LaunchConstants.LAUNCHER_RETURN_PROP;
        
        Properties props = new Properties();
        
        copySystemProp(props, irpTag);
        copySystemProp(props, procNameTag);
        copySystemProp(props, inTag);
        copySystemProp(props, rootTag);
        copySystemProp(props, retTag);
        
        preProcess(args, props);
    }
    
    private void copySystemProp(Properties to, String name)
    {
        String val = System.getProperty(name);
        
        if(val != null)
            to.setProperty(name, val);
    }
    
    /**
     * process - This is the method that performs the work of creating the java command line to execute.
     * This is where the BootStrap launcher and general launcher execute the same track
     * WBN Oct 2006 -- I'd like to change the signature to return int -- but it is public so I'll
     * klunk up a instance variable instead.
     */
    public void process(String[] args)
    {
        process(args, null);
    }
    
    public Process process(String[] args, String[] SecurityInfo)
    {
        Process process = null;
        try
        {
            _returnValue=1;
            securityInfo = SecurityInfo;
            
            if(securityInfo != null)
                GFSystem.setProperty(PROPMPT_FOR_IDENTITY_SYSTEM_PROPERTY, "true");
            // store args for later use (e.g. debug, verbose)
            setArgs(args);
            
            // look for Debug system property
            if (System.getProperty("Debug") != null)
            {
                // turn on debug, this option was added to help developers
                // debug the their code what adding/modifying tasks that are executed via
                // the ProcessLauncher
                bDebug=true;
            }
            
            // Set verboseMode early so logger will also show verbose output.
            // Each command type will take care of adding it again, but that is okay.
            // Only set it not being displayed for native launcher
            if (isVerboseEnabled() && !isDisplayEnabled())
            {
                // add to System.properties for config conditional adds
                GFSystem.setProperty(VERBOSE_SYSTEM_PROPERTY, "true");
            }
            
            String passArg=null;
            if (args != null && args.length >= 1)
            {
                passArg=args[0];
            }
            else
            {
                passArg=LAUNCHER_START_ACTION;
            }
            
            // Build command and pass in action
            if (bDebug) System.out.println("ASLauncher Building command ..");
            Command command=buildCommand(passArg);
            
            // execute command that was build and pass in action
            if (bDebug) System.out.println("ASLauncher Executing command ..");
            
            // if no exception, should have executed properly or logged errors
            _returnValue=0;
            
            if(externalLogfileHandler != null)
            {
                getLogger().removeHandler(externalLogfileHandler);
                externalLogfileHandler.close();
                externalLogfileHandler = null;
            }            
            
            process = executeCommand(command, passArg);
            
        }
        catch (ConfigException ce)
        {
            // try to log but the log main not be set up properly if domain.xml had a problem
            getLogger().log(Level.SEVERE, "launcher.config_exception", ce);
            ce.printStackTrace();
        }
        catch (Exception e)
        {
            // show in server log
            getLogger().log(Level.SEVERE, "enterprise.launcher_exception_startup",e);
            e.printStackTrace();
        }
        return process;
    }
    
    
    /**
     * executeCommand
     * @param cmd - command to execute
     * @param action - action to take, should be start or stop.  defaults to "start"
     */
    public Process executeCommand(Command command, String action) throws IOException
    {
        
        String[] cmd=null;
        
        // For native launcher and service, must send command to stdout in know format for
        // parsing by native code.  The purpose is to keep as much of the launcher functionality in
        // java as possible, keeping maintanance low and staying within a skillset that we have in abundance
        // It also is a easy way to show the command that is generated
        if (isDisplayEnabled() == false)
        {
            return executeBackgroundCommand(command, isVerboseEnabled(), action);
        }
        
        // First remove display argument that is used to trigger this action
        // This is the only arg that should be removed, all the rest should be passed through
        command.removeArg(COMMAND_LINE_ARG_DISPLAY);
        command.removeArg(COMMAND_LINE_ARG_NATIVE);
        
        // add launcher type for display to native exe, so the exe can desice whether to hold the child's
        // process or not.
        String launcherRet=System.getProperty(LaunchConstants.LAUNCHER_RETURN_PROP);
        if (launcherRet != null )
        {
            if (bDebug) System.out.println("-D" + LaunchConstants.LAUNCHER_RETURN_PROP + "=" + launcherRet);
            command.addSystemVariable(LaunchConstants.LAUNCHER_RETURN_PROP, launcherRet);
        }
    
        // display java command to stdout in the following format.
        // NOTE: if this becomes externally available, use xml, for now its an internal structure.
        // Class Name (path is seperated by "/" |
        // Java Args |
        // everything else (should start with a "-") |
        // classpath should be prepended with "-Djava.class.path="
        cmd=command.getCommandInJNIFormatAsArray();
        
        // Find delimiter that doesn't exist in command, just to be safe
        boolean found=false;
        String sxDelim="|";
        for(int ii=0; ii < COMMAND_DELIMITER_LIST.length; ii++)
        {
            // reset found to false for new delimiter
            found=false;
            // first character should delimiter to used to parse command
            for(int jj=0; jj < cmd.length; jj++)
            {
                if (bDebug) System.out.println(COMMAND_DELIMITER_LIST[ii] + " - " + cmd[jj]);
                if(cmd[jj].indexOf(COMMAND_DELIMITER_LIST[ii]) >= 0)
                {
                    // delimiter is found in string
                    found=true;
                    break;
                }
            }
            if (bDebug) System.out.println("\n***Delimiter = " + found + "\n");
            
            // see if char not found in any string
            if (!found)
            {
                // didn't find delimiter, so set and exit
                sxDelim=String.valueOf(COMMAND_DELIMITER_LIST[ii]);
                break;
            }
        }
        
        if(found)
        {
            // serious delimiter error, since all the delimits in the
            // COMMAND_DELIMITER_LIST array are in the java command
            // to startup the appserver then the native launcher can't
            // parse the command correctly and a jvm.dll error will arise.
            getLogger().log(Level.SEVERE, "launcher.native_launcher_delimiter_error");
        }
        
        // INCREDIBLY IMPORTANT for native processing.
        // The only data the native process expects is the commandline
        // any other data sent to stdout will throw off the command parsing
        // Needed to make the native side have a synch string, because anything could go wrong
        // with either the script that initiates this class or the preBuildProcessing.
        System.out.print("STARTOFCOMMAND" + sxDelim);
        for(int ii=0; ii < cmd.length; ii++)
        {
            System.out.print(cmd[ii] + sxDelim);
        }
        System.out.print("ENDOFCOMMAND" + sxDelim);
        // we can't return a Process because we are being CALLED by the external process.
        // only ASNativeLauncher will have a handle to the native process that called us
        return null;
    }
    
    
    protected void preBuildProcessing()
    {
        // this is a place holder so ee can put in its
        // sychronization code
    }
    
    
    public Process executeBackgroundCommand(Command command, boolean verbose, String action) throws IOException
    {
        
        // execute the command as a runtime.exec and immediately return,
        // return will not pertain to the the executed process
        getLogger().log(FINE_LEVEL, "ASLauncher: executing Runtime execute...");
        
        // run command
        
        String[] cmd=command.getCommandAsArray();
        //getLogger().log(Level.INFO, "\n" + Arrays.toString(cmd));
        ProcessBuilder pb = new ProcessBuilder(cmd);
        
        File configDir = new File(System.getProperty(SystemPropertyConstants.INSTANCE_ROOT_PROPERTY) + "/config");
        
        if(FileUtils.safeIsDirectory(configDir))
            pb.directory(configDir);
        
        Process process = pb.start();
        
        // See is there is input that needs to be sent to the process
        // WBN Sept 2006 -- it used to check for the EXISTENCE of the System Property -
        // now it checks on whether it exists AND is set to true.
        // note we do NOT do this if the password/user name info is sent in as a parameter to process()!!  In that
        // case we already have the info and just need to write it to the AS process...
        // this is really overly-complex because of supporting native startup (sigh)
        
        if (Boolean.getBoolean(PROPMPT_FOR_IDENTITY_SYSTEM_PROPERTY) && action.equals(LAUNCHER_START_ACTION) )
        {
            if(securityInfo == null)
                sendInputToProcessInput(System.in, process);    // ancient native launcher uses startserv script
            else
                writeSecurityInfoToProcess(process);
        }
        // start stream flusher to push output to parent streams and log if they exist
        StreamFlusher sfErr=new StreamFlusher(process.getErrorStream(), System.err, command.getLogFile());
        Thread sfErrThread = new Thread(sfErr, ERROR_FLUSHER);
        sfErrThread.setDaemon(true);
        sfErrThread.start();
        
        if (verbose || isWaitEnabled())
        {
            // need to keep client around for start
            // this should only be invoked for start-domain command
            
            // set flusher on stdout also
            StreamFlusher sfOut=new StreamFlusher(process.getInputStream(), System.out, command.getLogFile());
            Thread sfOutThread = new Thread(sfOut, OUT_FLUSHER);
            sfOutThread.setDaemon(true);
            sfOutThread.start();
            
            try
            {
                process.waitFor();
                sfOutThread.join();
                sfErrThread.join();
            }
            catch (InterruptedException ie)
            {
                // just let fall through, but log at finest level
                System.out.println("While waiting in verbose mode, an InterruptedException was thrown ");
            }
        }
        else
        {
            
            // set flusher on stdout also, if not could stop with too much output
            StreamFlusher sfOut=new StreamFlusher(process.getInputStream(), System.out);
            Thread sfOutThread = new Thread(sfOut, OUT_FLUSHER);
            sfOutThread.setDaemon(true);
            sfOutThread.start();
            
            // if executing in the background and a log exists, print log location
            String logFile=command.getLogFile();
            
            // TODO extra redirect message?
            //./admin-cli/commands/src/java/com/sun/enterprise/cli/commands/LocalStrings.properties:LogRedirectedTo=Log redirected to {0}.
            if (logFile != null)
            {
                System.out.println(StringManager.getManager(ASLauncher.class).getString("launcher.redirecting.output",
                        logFile));
            }
            // must sleep for a couple of seconds, so if there is a jvm startup error, the parent process
            // is around to catch and report it when the process in executed in verbose mode.
            // WBN  ?????? It doesn't appear that we need this ????  Verbose mode was handled in the "if"
            // above...
            //try {
            //Thread.currentThread().sleep(SLEEP_TIME_FOR_PROCESS_START);
            //} catch (InterruptedException ie) {}
        }
        return process;
    }
    
    private void sendInputToProcessInput(InputStream in, Process subProcess)
    {
        // return if no input
        if (in == null || subProcess == null) return;
        
        PrintWriter out=null;
        BufferedReader br=null;
        try
        {
            // open the output stream on the process which excepts the input
            out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(subProcess.getOutputStream())));
            
            // WBN October 2006
            // See if there is input available first.  If not use standard strings...
            
            
            // read in each line and resend it to sub process
            br=new BufferedReader(new InputStreamReader(System.in));
            
            if(br.ready())
            {
                String sxLine=null;
                while ((sxLine=br.readLine()) != null)
                {
                    // get input lines from process if any
                    out.println(sxLine);
                    if (bDebug) System.out.println("Feeding in Line:" + sxLine);
                }
            }
            else
            {
                out.println("");
                out.println("");
                out.println("changeit");
            }
            out.flush();
        }
        catch (Exception e)
        {
            getLogger().log(Level.INFO,"WRITE TO INPUT ERROR", e);
        }
        finally
        {
            try
            {
                if (out != null) out.close();
            }
            catch (Throwable t)
            {}
        }
    }
    
    public Command buildCommand(String action) throws ConfigException
    {
        
        // execute any preprocessing,
        preBuildProcessing();
        String processName=System.getProperty(LaunchConstants.PROCESS_NAME_PROP, INTERNAL_SERVER_PROFILE);
        //Set system properties that correspond directly to asenv.conf/bat. This
        //keeps us from having to pass them all from -D on the command line.
        
        // check to see whether to build external or internal (domain.xml) command
        Command command=null;
        if (isServerProfile())
        {
            command=buildInternalCommand(action);
        }
        else
        {
            command=buildExternalCommand(action);
        }

        command.reorder();
        
        // log final command
        String finalCommand=command.toStringWithLines();
        getLogger().log(Level.INFO, finalCommand);
        
        return command;
    }
    
    
    /**
     * buildInternalCommand - This Method build san internal server command from domain.xml, so this
     * method is specifically used for server instances
     */
    public Command buildInternalCommand(String action) throws ConfigException
    {
        StringManager _strMgr=StringManager.getManager(ASLauncher.class);
        
        // derive domain.xml location and create config to be used by config api
        String domainXMLLocation=System.getProperty(SystemPropertyConstants.INSTANCE_ROOT_PROPERTY)
        + RELATIVE_LOCATION_DOMAIN_XML;
        //ConfigContext configCtxt=ConfigFactory.createConfigContext(domainXMLLocation);
        final ConfigContext configCtxt; //if-else guarantees definite assignment of blank finals
        if (this.getRefreshConfigContext()) //refresh, hence cache = false, new CC is returned;
            configCtxt = ConfigFactory.createConfigContext(domainXMLLocation, true, false, false);
        else //use cached config context
            configCtxt = ConfigFactory.createConfigContext(domainXMLLocation, true, false, true);
        
        /* Implementation Note: For start-domain command, control should go into else clause
         * because the start-domain-command code is creating the config context and
         * passing it to launcher. In the case of instances and node agents, the
         * cached config context is NOT used.
         * This is true with changes made to launcher during Oct 2006 - April 2007.
         */
        Domain domain=ConfigAPIHelper.getDomainConfigBean(configCtxt);
        setDomainName(domain);
        // get the server's config by name, need it as soon as possible for logging
        String serverName=System.getProperty(SystemPropertyConstants.SERVER_NAME);
        Server server=ServerHelper.getServerByName(configCtxt, serverName);
        String configRef=server.getConfigRef();
        
        // create the command to execute
        Command command=new Command();
        
        // set jvmargs for thread dump to go to child process log, workaround for bug #4957071
        //command.addJvmOption("-XX:+UnlockDiagnosticVMOptions");
        //command.addJvmOption("-XX:+LogVMOutput");
        //command.addJvmOption("-XX:LogFile=" + logFile);
        //command.addJvmOption("-XX:LogFile=/tmp/threadDump.txt");
        
        // get server's config
        Config config=ServerHelper.getConfigForServer(configCtxt, serverName);
        
        // configure log service and print redirect message
        String logFile=configureLogService(config);
        if (bDebug) System.out.println("LOGFILE = " + logFile);
        
        // make sure log is writable, if not a message will be logged to the screen if in verbose mode
        createFileStructure(logFile);
        command.setLogFile(logFile);
        
        // should NOT need to addLogFileToLogger(logFile), logManager should already do this for us
        // may need to enable if log is movable ???
        
        // TODO Logging Weirdnesses
        addLogFileToLogger(logFile);
        
        // add log to properties so PEMAIN will redirect is applicable
        command.addSystemVariable(LOGFILE_SYSTEM_PROPERTY, logFile);
        
        getLogger().log(FINE_LEVEL,"Retrieved domain.xml from " + domainXMLLocation);
        getLogger().log(FINE_LEVEL,"Start building the command the to execute.");
        
        //Set system properties that correspond directly to asenv.conf/bat. This
        //keeps us from having to pass them all from -D on the command line.
        //ASenvPropertyReader reader = new ASenvPropertyReader(System.getProperty(SystemPropertyConstants.CONFIG_ROOT_PROPERTY));
        //reader.setSystemProperties();
        
        // verbose set, flag used in ServerLogManager to send logs to stderr
        if (isVerboseEnabled())
        {
            command.addSystemVariable(VERBOSE_SYSTEM_PROPERTY, "true");
            // add to System.properties for config conditional adds (could be set about if not native launcher)
            GFSystem.setProperty(VERBOSE_SYSTEM_PROPERTY, "true");
        }
        
        // read in ASLauncherConfig
        String launcherConfigFile=System.getProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY)
        + File.separator + "lib" + File.separator + "processLauncher.xml";
        
        String processName=System.getProperty(LaunchConstants.PROCESS_NAME_PROP, INTERNAL_SERVER_PROFILE);
        getLogger().log(FINE_LEVEL,"Loading ASLauncher config from: " + launcherConfigFile
                + " - for the process named: " + processName);
        
        ASLauncherConfig plConfig=new ASLauncherConfig(launcherConfigFile, processName);
        
        // take plConfig properties as the base for the system jvm args for the new process
        Properties systemProperties=plConfig.getSystemProperties();
        
        // add domain.xml property elements to the jvm args in reverse order of precedence.
        // First add the domain properties
        addSystemProperties(domain.getSystemProperty(), systemProperties);
        
        
        
        // set config name (which is retrieved from domain.xml) into System properties to be used for path resolution
        GFSystem.setProperty(SystemPropertyConstants.CONFIG_NAME_PROPERTY, configRef);
        systemProperties.put(SystemPropertyConstants.CONFIG_NAME_PROPERTY, configRef);
        
        // get javaconfig for server that is being started
        JavaConfig javaConfig=config.getJavaConfig();
        
        // derive and add java command to Command
        String jvmCmd=javaConfig.getJavaHome() + File.separator + "bin"
                + File.separator + "java";
        
        command.setJavaCommand(jvmCmd);
        
        
        // fix for bug# 6323645
        // Do not add options which are not applicable for stop action.
        // For ex. debug options and profiler options.
        // In other words add the following options ony when the action
        // is other than stop.
        
        Profiler profiler=javaConfig.getProfiler();
        String profilerClasspath=null;
        
        if (!action.equals(LAUNCHER_STOP_ACTION))
        {
            
            // The debug options including the debug port would be added
            // to the command  when the action is start.
            // If the action is stop adding the same port to the java command
            // would stack up the ports and block until the port assigned for
            // start action is released. To avoid this we check for stop action.
            
            // If the stop action needs to be debugged then one work around is to
            // copy the java command from server.log, change the debug settings and
            // run the command.
            
            // debug options
            if ((javaConfig.isDebugEnabled() || isDebugEnabled()))
            {
                // add debug statements
                addDebugOptions(command, javaConfig.getDebugOptions());
            }
            
            // add profiler properties & jvm args
            if (profiler != null && profiler.isEnabled())
            {
                // add config properties
                addElementProperties(profiler.getElementProperty(), systemProperties);
                String [] jvmOptions=profiler.getJvmOptions();
                addJvmOptions(command, jvmOptions, action);
                profilerClasspath=profiler.getClasspath();
            }
        }
        
        // set the default locale specified in domain.xml config file
        String locale=domain.getLocale();
        if (locale == null || locale.equals(""))
        {
            // if not specified in domain try system
            locale=System.getProperty(SystemPropertyConstants.DEFAULT_LOCALE_PROPERTY);
        }
        // make sure locale is specified before setting it
        if (locale != null && !locale.equals(""))
        {
            command.addSystemVariable(SystemPropertyConstants.DEFAULT_LOCALE_PROPERTY, locale);
        }
        //
        // add jvm args, look for combined jvm options
        String[] jvmOptions=javaConfig.getJvmOptions();
        addJvmOptions(command, jvmOptions, action);
        
        //
        // add config system properties
        addSystemProperties(config.getSystemProperty(), systemProperties);
        
        //
        // add cluster system properties if the server instance is clustered
        if (ServerHelper.isServerClustered(configCtxt, server))
        {
            Cluster cluster = ClusterHelper.getClusterForInstance(configCtxt,
                    server.getName());
            addSystemProperties(cluster.getSystemProperty(), systemProperties);
        }
        
        //
        // add server system properties
        addSystemProperties(server.getSystemProperty(), systemProperties);
        
        //
        // add classpath
        // check to see if jvmCmd starts with same as processLauncher jvm.
        // if so, use the system property java-version to determine jvm version
        if(OS.isWindows())
        {
            // make sure all delimeters are the same
            jvmCmd=jvmCmd.replace('/', '\\');
        }
        
        if (jvmCmd.startsWith(System.getProperty(SystemPropertyConstants.JAVA_ROOT_PROPERTY)))
        {
            // jvm command are the same, so use processLauncher jvm version
            jvmCmd=null;
        }
        String classpath=deriveClasspath(plConfig, jvmCmd, javaConfig, profilerClasspath);
        getLogger().log(FINE_LEVEL, "Complete process classpath = " + classpath);
        command.setClasspath(classpath);
        
        //
        //add main class
        command.setMainClass(plConfig.getMainClass());
        
        //
        // native library path to java path and system properties
        deriveNativeClasspath(command, javaConfig, profiler, systemProperties);
        
        //
        // add all system properties to command as jvm args
        Iterator it=systemProperties.keySet().iterator();
        String key=null;
        String property=null, value=null;;
        while(it.hasNext())
        {
            key=(String)it.next();
            value=systemProperties.getProperty(key);
            
            // take care of vm arg that are sent in via the processlauncher.xml file
            if (key.startsWith("-"))
            {
                property = key;
                if (value != null && !value.equals(""))
                {
                    property += "=" + value;
                }
                command.addJvmOption(property);
                getLogger().log(FINE_LEVEL, "JVM Option: " + property);
            }
            else
            {
                // regular system property
                property = "-D" + key + "=" + value;
                command.addSystemVariable(key, value);
                getLogger().log(FINE_LEVEL, "System Property: " + property);
            }
        }
        
        //Add prefix and suffix for AS9Profile
        if (getProcessLauncherProfile().equals(AS9_INTERNAL_SERVER_PROFILE))
        {
            String classpathPrefix=javaConfig.getClasspathPrefix();
            String classpathSuffix=javaConfig.getClasspathSuffix();
            String serverClasspath=javaConfig.getServerClasspath();
            getLogger().log(FINE_LEVEL, " prefix :: " + classpathPrefix
                    + " suffix :: " + classpathSuffix);
            if (classpathPrefix == null) classpathPrefix = "";
            command.addSystemVariable(CLASSPATH_PREFIX_PROPERTY, classpathPrefix);
            if (classpathSuffix == null) classpathSuffix = "";
            command.addSystemVariable(CLASSPATH_SUFFIX_PROPERTY, classpathSuffix);
            if (serverClasspath == null) serverClasspath = "";
            command.addSystemVariable(SERVER_CLASSPATH_PROPERTY, serverClasspath);
        }
        
        setPromptForIdentity(command);
        
        //
        // add command args from script
        String[] args=getArgs();
        for(int ii=0; ii < args.length; ii++)
        {
            command.addArg(args[ii]);
        }
        return command;
    }
    
    
    /**
     * This Method builds an external java command to execute componets like the nodeagent
     * and the deploy tool
     */
    public Command buildExternalCommand(String action) throws ConfigException
    {
        
        // create the command to execute
        Command command=new Command();
        
        getLogger().log(FINE_LEVEL,"Start building the command the to execute.");
        
        // read in ASLauncherConfig
        String launcherConfigFile=System.getProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY)
        + File.separator + "lib" + File.separator + "processLauncher.xml";
        
        String processName=System.getProperty(LaunchConstants.PROCESS_NAME_PROP, INTERNAL_NODE_AGENT_PROFILE);
        getLogger().log(FINE_LEVEL,"Loading ProcessLauncher config from: " + launcherConfigFile +
                " - for the process named: " + processName);
        
        // see if we have a config root set
        String configRoot=System.getProperty(SystemPropertyConstants.CONFIG_ROOT_PROPERTY);
        if (configRoot == null)
        {
            // no config root try and make one from install root
            configRoot=System.getProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY) + File.separator + "config";
        }
        //Set system properties that correspond directly to asenv.conf/bat. This
        //keeps us from having to pass them all from -D on the command line.
        //ASenvPropertyReader reader = new ASenvPropertyReader(configRoot);
        //reader.setSystemProperties();
        
        // verbose set, flag used in ServerLogManager to send logs to stderr
        if (isVerboseEnabled())
        {
            command.addSystemVariable(VERBOSE_SYSTEM_PROPERTY, "true");
            // add to System.properties for config conditional adds (could be set about if not native launcher)
            GFSystem.setProperty(VERBOSE_SYSTEM_PROPERTY, "true");
        }
        
        // get configuration
        ASLauncherConfig plConfig=new ASLauncherConfig(launcherConfigFile, processName);
        
        // take plConfig properties as the base for the system jvm args for the new process
        Properties systemProperties=plConfig.getSystemProperties();
        
        // check for log file location
        String logFile=systemProperties.getProperty(LOGFILE_SYSTEM_PROPERTY);
        if(bDebug) System.out.println("Is external command nodeagent - " + isNodeAgentProfile());
        if (isNodeAgentProfile())
        {
            // need to get log info from domain.xml
            getLogger().log(FINE_LEVEL,"BuildExternalCommand for NodeAgent");
            
            try
            {
                // derive domain.xml location and create config to be used by config api
                String domainXMLLocation=System.getProperty(SystemPropertyConstants.INSTANCE_ROOT_PROPERTY)
                + RELATIVE_LOCATION_DOMAIN_XML;
                ConfigContext configCtxt=ConfigFactory.createConfigContext(domainXMLLocation);
                Domain domain=ConfigAPIHelper.getDomainConfigBean(configCtxt);
                setDomainName(domain);
                
                // get the nodeagent by name, need it as soon as possible for logging
                String nodeAgentName=System.getProperty(SystemPropertyConstants.SERVER_NAME);
                NodeAgent nodeAgent=NodeAgentHelper.getNodeAgentByName(configCtxt, nodeAgentName);
                LogService logService=nodeAgent.getLogService();
                
                if(logService != null)
                {
                    getLogger().log(FINE_LEVEL, "LogService found for nodeagent");
                    // get logservice info from config beans
                    String logFileX=logService.getFile();
                    
                    if (logFileX !=null)
                    {
                        logFile=logFileX;
                        // add log to properties so NodeAgentMain will redirect is applicable
                        systemProperties.setProperty(LOGFILE_SYSTEM_PROPERTY, logFile);
                    }
                    
                    // set log level to the level that the nodeagent is set to
                    String logLevel=logService.getModuleLogLevels().getNodeAgent();
                    getLogger().setLevel(Level.parse(logLevel));
                }
            }
            catch (ConfigException ce)
            {
                // domain.xml should not be there on first start up, just log
                getLogger().log(FINE_LEVEL,"domain.xml does not exist yet for the nodeagent");
            }
        }
        
        // set log file for logger and native launcher
        if (logFile != null)
        {
            // make sure log is writable, if not a message will be logged to the screen if in verbose mode
            // WBN March 2007 - The JVM invocation command line does NOT get written
            // anywhere if isInternalLogger() returns true.  
            // It returns true for everything -- NA, DAS, Instance
            // So why is NA getting special treatment?  I see no reason so I'm
            //removing the restriction.
            
            if(createFileStructure(logFile) /* && !isInternalLogger() */)
            {
                // add this file to the logger so at least some of the launcher info gets propagated
                addLogFileToLogger(logFile);
            }
            command.setLogFile(logFile);
        }
        
        // derive and add java command to Command
        String javaHome=System.getProperty(LaunchConstants.JAVA_HOME_PROP);
        // use javaw for windows instead of java
        String javaCall="java", javaCmd=null;
        if(OS.isWindows())
        {
            javaCall="javaw";
        }
        
        if (javaHome != null)
        {
            // use standard java home
            javaCmd=javaHome + File.separator + "bin" + File.separator + javaCall;
        }
        else
        {
            // use executed jmv location of java.home
            String javaInt=System.getProperty("java.home");
            javaCmd=javaInt + File.separator + "bin" + File.separator + javaCall;
        }
        
        command.setJavaCommand(javaCmd);
        
        //
        // add classpath
        String classpath=deriveClasspath(plConfig, null);
        command.setClasspath(classpath);
        
        //
        //add main class
        command.setMainClass(plConfig.getMainClass());
        
        //
        // add all system properties to command as system variables args
        Iterator it=systemProperties.keySet().iterator();
        String key=null;
        
        String property=null, value=null;;
        while(it.hasNext())
        {
            key=(String)it.next();
            value=systemProperties.getProperty(key);
            
            // take care of vm arg that are sent in via the processlauncher.xml file
            if (key.startsWith("-"))
            {
                property = key;
                if (value != null && !value.equals(""))
                {
                    property += "=" + value;
                }
                if ( ( key.equals("-client")) || (key.equals("-server")) )
                {
                    // If user sets server or client mode for VM
                    // then use that over default which is "client"
                    // As we want to keep this as first arg don't add
                    // to the jvmArgsList yet
                    command.setMode(key);
                }
                else
                {
                    command.addJvmOption(property);
                }
            }
            else
            {
                // regular system property
                property = "-D" + key + "=" + value;
                command.addSystemVariable(key, value);
            }
        }
        
        //
        // derive native library path for native part of launcher
        command.addSystemVariable("java.library.path", deriveNativeClasspath(command, null, null, systemProperties));
        
        //
        // add command args from script
        String[] args=getArgs();
        for(int ii=0; ii < args.length; ii++)
        {
            command.addArg(args[ii]);
        }
        return command;
    }
    
    
    /**
     * configureLogService - get log information out of domain.xml and set the processLaunchers log level
     * to that of the instance log level
     *
     * @param config This represents the config element in domain.xml for the server
     * @return logFile Log file for server
     */
    protected String configureLogService(Config config)
    {
        String logFile="";
        LogService logService=config.getLogService();
        if(logService != null)
        {
            // get logservice info from config beans
            logFile=logService.getFile();
            String logLevel=logService.getModuleLogLevels().getAdmin();
            
            // set log level to the level that the instance is set to (for internal instances only
            getLogger().setLevel(Level.parse(logLevel));
        }
        return logFile;
    }
    
    
    /**
     * This method derives the classpath by using the information from the processLauncher.xml file which gives criteria for the classpath
     * to be derived from the library directory.
     */
    protected String deriveClasspath(ASLauncherConfig plConfig, String jvmCmd)
    {
        return deriveClasspath(plConfig, jvmCmd, null, null);
    }
    
    
    /**
     * This method derives the classpath by using the information from the processLauncher.xml file which gives criteria for the classpath
     * to be derived from the library directory.  It also will include the javaConfig classpath and profilerClasspath if they are passed in
     *
     * classpath construction hierarchy:
     * (javaConfig.getClasspathPrefix)
     * (processLauncher.Classpath.prefix)
     * (processLauncher.Classpath.j2se1_4_prefix or processLauncher.Classpath.j2se1_5_or_later_prefix)
     * (derivedClasspath based on processLauncher.Classpath excludes and includes)
     * (javaConfig.getSystemClasspath)
     * (javaConfig.getClasspathSuffix)
     * (javaConfig.profilerClasspath if enabled)
     * (Environment classpath if enabled)
     *
     */
    protected String deriveClasspath(ASLauncherConfig plConfig, String jvmCmd, JavaConfig javaConfig, String profilerClasspath)
    {
        // add classpath
        String libDir=RelativePathResolver.resolvePath(plConfig.getClasspathLibDir());
        
        // WBN May 18, 2007 appserv-launch.jar removed from config's system-classpath'
        String classpath = getPELauncherJarPath();

        String libClasspath =  Classpath.getLibClasspath(libDir, plConfig.getClasspathIncludes(),
                plConfig.getClasspathExcludes());
        if (isValidString(libClasspath)) {
            classpath += File.pathSeparator + libClasspath;
        }

        getLogger().log(FINE_LEVEL, "Derived Classpath from " + libDir + " - \n" + classpath);
        
        // handle processLauncher.xml j2se prefixes always go first
        // check to see what jdk we are using
        String javaVersion=System.getProperty("java.version"); // default to 1.5 or later
        if (jvmCmd != null)
        {
            try
            {
                // execute the java command with version option
                Process process=Runtime.getRuntime().exec(jvmCmd + " -version");
                // get streams and capture output
                ByteArrayOutputStream baosOut= new ByteArrayOutputStream();
                ByteArrayOutputStream baosErr= new ByteArrayOutputStream();
                StreamFlusher sfOut=new StreamFlusher(process.getInputStream(), baosOut);
                StreamFlusher sfErr=new StreamFlusher(process.getErrorStream(), baosErr);
                new Thread(sfOut, OUT_FLUSHER).start();
                new Thread(sfErr, ERROR_FLUSHER).start();
                // wait for process to end, should be fast
                process.waitFor();
                javaVersion=baosErr.toString();
            }
            catch (Exception e)
            {
                // log at fine incase of problem
                getLogger().log(FINE_LEVEL,"Java version retrieving error, will default to 1.5 or later!", e);
            }
        }
        
        // put in jdk prefix, if exits, goes before what currently has been built
        String prefix="";
        String jvmv="";
        if(javaVersion.indexOf("1.4") >= 0)
        {
            // j2se 1.4
            jvmv="j2se 1.4";
            prefix=plConfig.getClasspathJ2se14Prefix();
        }
        else
        {
            // j2se 1.5 or later
            jvmv="j2se 1.5 or later";
            prefix=plConfig.getClasspathJ2se15OrLaterPrefix();
        }
        // log java version
        getLogger().log(FINE_LEVEL,"Java version being used is: ->" + jvmv +
                "<- based on ->" + javaVersion + "<-");
        
        // set j2se prefix, this alway goes first so the components classpath doesn't interfer with
        // the j2se's requirements
        if(prefix != null && !prefix.equals(""))
        {
            
            // resolve any tokens in the prefix
            prefix=RelativePathResolver.resolvePath(prefix);
            
            // only add prefix to path if one exists.
            if(classpath.equals(""))
            {
                // no other classpath information so only use prefix, could be know classpath
                classpath=prefix;
            }
            else
            {
                // prepend prefix to path
                classpath=prefix + File.pathSeparator + classpath;
            }
        }
        
        
        // handle processLauncher.xml prefix, if exits, goes before what currently has been built
        prefix=plConfig.getClasspathPrefix();
        // set prefix, this also can be used as a method for entering a know classpath.
        if(prefix != null && !prefix.equals(""))
        {
            
            // resolve any tokens in the prefix
            prefix=RelativePathResolver.resolvePath(prefix);
            
            // only add prefix to path if one exists.
            if(classpath.equals(""))
            {
                // no other classpath information so only use prefix, could be know classpath
                classpath=prefix;
            }
            else
            {
                // prepend prefix to path
                classpath=prefix + File.pathSeparator + classpath;
            }
        }
        
        
        // add in the javaconfig paths
        if(javaConfig != null)
        {
            String classpathPrefix=javaConfig.getClasspathPrefix();
            String classpathSystem=javaConfig.getSystemClasspath();
            String classpathSuffix=javaConfig.getClasspathSuffix();
            
            if(isValidString(classpathSystem))
            {
                classpath += File.pathSeparator + classpathSystem;
            }
            
            //Classpath prefix/suffix and server classpath gets now prefixed/suffixed to
            //the shared classloader at PELaunch.java. Instead of setting it here, set
            //system properties, so that the system properties could be used to construct
            //the classpaths in PELaunch
            if (getProcessLauncherProfile().equals(INTERNAL_SERVER_PROFILE))
            {
                if(isValidString(classpathPrefix))
                {
                    classpath=classpathPrefix + File.pathSeparator + classpath;
                }
                if(isValidString(classpathSuffix))
                {
                    classpath=classpath + File.pathSeparator +  classpathSuffix;
                }
            }
            
            // add profiler information
            if(profilerClasspath != null)
            {
                classpath += File.pathSeparator + profilerClasspath;
            }
            // See if need to add user classpath
            // WBN changed to use JDK 1.5 built-in getEnv()
            if(!javaConfig.isEnvClasspathIgnored())
            {
                // Add user classpath...
                String userCP = System.getenv(CLASSPATH_ENV_NAME);
                if(userCP != null && userCP.length() > 0)
                    classpath += File.pathSeparator + userCP;
            }
        }
        
        getLogger().log(FINE_LEVEL, "Final classpath - \n" + classpath);
        if (bDebug) System.out.println("Final classpath=" + classpath);
        return classpath;
    }

    //Checks if the given string is not null and not an empty string.
    private boolean isValidString(String s) {
        return (s != null && !s.trim().equals(""));
    }

    
    protected String deriveNativeClasspath(Command command, JavaConfig javaConfig, Profiler profiler, Properties systemProperties)
    {
        // native path works in native launcher
        String javaLibPath = System.getProperty("java.library.path");
        if (bDebug) System.out.println("Current java.library.path=" + javaLibPath + "\n");
        if (javaLibPath == null) javaLibPath="";
        String libDirFor64Bit = "";
        
        if (javaConfig != null)
        {
            String nativePrefix=javaConfig.getNativeLibraryPathPrefix();
            String nativeSuffix=javaConfig.getNativeLibraryPathSuffix();
            
            String nativeProfiler=null;
            if(profiler != null && profiler.isEnabled())
            {
                nativeProfiler=profiler.getNativeLibraryPath();
            }
            
            // put native path together in designated order
            if ( (nativePrefix != null ) && !nativePrefix.trim().equals(""))
            {
                javaLibPath=nativePrefix + (javaLibPath.equals("") ? "" : File.pathSeparator + javaLibPath);
            }
            if ( (nativeSuffix != null ) && !nativeSuffix.trim().equals(""))
            {
                javaLibPath=(javaLibPath.equals("") ? "" : javaLibPath + File.pathSeparator) + nativeSuffix ;
            }
            if (( nativeProfiler!= null ) && !nativeProfiler.trim().equals(""))
            {
                javaLibPath=(javaLibPath.equals("") ? "" : javaLibPath + File.pathSeparator) + nativeProfiler ;
            }
            
            
            String[] jvmOptions=javaConfig.getJvmOptions();
            for(String s:jvmOptions)
            {
                if(s.indexOf(JVM_OPTION_FOR_64BIT)!=-1)
                {
                    String osArch = System.getProperty("os.arch");
                    if(osArch.equals(SPARC)) libDirFor64Bit = SPARCV9;
                    else if(osArch.equals(X86))  libDirFor64Bit = AMD64;
                    
                    String nssRoot=System.getProperty(SystemPropertyConstants.NSS_ROOT_PROPERTY);
                    String installRoot=System.getProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY);
                    String imqLib=System.getProperty(SystemPropertyConstants.IMQ_LIB_PROPERTY);
                    String java64BitLibPath = "";
                    if (installRoot != null)
                    {
                        if(imqLib != null)
                        {
                            java64BitLibPath = imqLib + File.separator + libDirFor64Bit + File.pathSeparator + java64BitLibPath;
                        }
                        
                        if(installRoot != null)
                        {
                            java64BitLibPath = installRoot + File.separator + "lib" + File.separator + libDirFor64Bit + File.pathSeparator + java64BitLibPath;
                        }
                        if(nssRoot != null)
                        {
                            java64BitLibPath= nssRoot + File.separator + libDirFor64Bit + File.pathSeparator + java64BitLibPath;
                        }
                        javaLibPath = java64BitLibPath + File.pathSeparator + javaLibPath;
                    }
                }
            }
        }
        // add nss and lib directories to from of java.library.path if windows to get around jdk
        // addition of c:\windows\system32
        if (OS.isWindows())
        {
            String nssRoot=System.getProperty(SystemPropertyConstants.NSS_ROOT_PROPERTY);
            String installRoot=System.getProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY);
            
            if (installRoot != null && nssRoot != null)
            {
                javaLibPath= nssRoot + File.pathSeparator +
                        installRoot + File.separator + "lib" + File.pathSeparator + javaLibPath;
            }
        }
        
        if (isDisplayEnabled())
        {
            // need to add the path the correct jvm library so the native portion will
            // switch jvm modes, it should be set to client by default, but set it just in case
            
            // fix for bug# 6240672 and 6318497
            // This fix enables the use of java_home available in java-config.
            // If the java_home picked up from java-config is invalid then it falls back
            // to default java_home of config/asenv.conf
            
            String java_home = null;
            if ((javaConfig != null) && (javaConfig.getJavaHome() != null))
            {
                java_home = javaConfig.getJavaHome();
            }
            else
            {
                java_home = SystemPropertyConstants.JAVA_ROOT_PROPERTY;
            }
            String jvmLibPath = java_home +
                    System.getProperty(SystemPropertyConstants.NATIVE_LAUNCHER_LIB_PREFIX);
            
            // remove default jvm mode and add proper one
            if (command.getMode() != null)
            {
                jvmLibPath=jvmLibPath.substring(0, jvmLibPath.lastIndexOf(File.separator) + 1) +
                        command.getMode().substring(1);
            }
            
            if (javaLibPath != null)
            {
                javaLibPath=jvmLibPath + File.pathSeparator + javaLibPath;
            }
            else
            {
                javaLibPath=jvmLibPath;
            }
        }
        
        
        // now check for spaces in path
        if(javaLibPath.indexOf(" ") >= 0)
        {
            // there are spaces in the path so send warning message
            // Almost every Windows user has spaces in their path.
            // Bug 6342806 determined that this is "noise"
            // I changed the message to FINE and I added in the names of the items
            // in the path that have spaces...
            String items = getPathItemsWithSpaces(javaLibPath);
            getLogger().log(FINE_LEVEL,"launcher.spacesInPath", new Object[] { items });
            
            // remove all quotes, because either the java or native launchers will not consistenly
            // accept the mix in the java.library.path
            // This is a problem between the JNI invocation api and the straight java command.
            javaLibPath=javaLibPath.replaceAll("\"", "");
        }
        
        systemProperties.put("java.library.path" , javaLibPath);
        
        
        
        GFSystem.setProperty("java.library.path", javaLibPath );
        command.setNativeClasspath(javaLibPath);
        
        
        if (bDebug) System.out.println("Final java.library.path=" + javaLibPath + "\n");
        return javaLibPath;
    }
    
    protected void addSystemProperties(SystemProperty sp[], Properties systemProperties)
    {
        if(sp != null)
        {
            for(int ii=0; ii < sp.length; ii++)
            {
                systemProperties.put(sp[ii].getName(), sp[ii].getValue());
            }
        }
    }
    
    protected void addElementProperties(ElementProperty ep[], Properties systemProperties)
    {
        if(ep != null)
        {
            for(int ii=0; ii < ep.length; ii++)
            {
                systemProperties.put(ep[ii].getName(), ep[ii].getValue());
            }
        }
    }
    
    
    
    
    protected Logger getLogger()
    {
        if (_logger == null)
        {
            // check log manager to see if it is internal or external
            // if (!isInternalLogger()) {
            // external log manager, add resource bundle for i18n
            // will associate a file to the logger if it is specified in the system args
            _logger = Logger.getLogger(LogDomains.PROCESS_LAUNCHER_LOGGER, "com.sun.logging.enterprise.system.tools.launcher.LogStrings");
            
            // check to see if in verbose mode, if not remove default console handler
            if (!isVerboseEnabled())
            {
                Handler[] h=_logger.getParent().getHandlers();
                for (int ii=0; ii < h.length; ii++)
                {
                    if (h[ii].getClass().getName().equals("java.util.logging.ConsoleHandler"))
                    {
                        _logger.getParent().removeHandler(h[ii]);
                    }
                }
            }
            
            //} else {
            // use internal log manager which associates the resource bundle and
            // log file automatically
            //    _logger = Logger.getLogger(LogDomains.PROCESS_LAUNCHER_LOGGER, "com.sun.logging.enterprise.system.tools.launcher.LogStrings");
            //}
        }
        
        // set each time reguardless of what is preset in domain.xml (for internals)
        if (bDebug) _logger.setLevel(Level.FINEST);
        return _logger;
    }
    
    
    
    protected boolean isInternalLogger()
    {
        boolean bRet=false;
        // check log manager to see if it is internal or external
        String logManager=System.getProperty(LaunchConstants.LOG_MGR_PROP);
        if (logManager != null && logManager.equals(LaunchConstants.LOG_MGR_VALUE))
        {
            bRet=true;
        }
        return bRet;
    }
    
    protected void addLogFileToLogger(String logFile)
    {
        if (logFile == null) return;
        
        // Send logger output to our FileHandler.
        getLogger().log(FINE_LEVEL, "*** Adding logFileHandler - " + logFile);
        // already created directory structure for log file, so just add log
        try
        {// todo 6581651
            externalLogfileHandler = new FileHandler(logFile, true);
            externalLogfileHandler.setFormatter(new SimpleFormatter());
            externalLogfileHandler.setLevel(Level.ALL);
            getLogger().addHandler(externalLogfileHandler);
        }
        catch(IOException e)
        {
            // should be seen in verbose mode for debugging
            e.printStackTrace();
        }
    }
    
    
    /**
     * This method handles the jvm options and was pulled from the LaunchFilter
     */
    protected void addJvmOptions(Command command, String[] args, String action)
    {
        String systemProperty = null;
        String property = null;
        String value = null;
        String[] jvmOptions = null;
        
        try
        {
            jvmOptions = (new JvmOptionsHelper(args)).getJvmOptions();
        }
        catch(Exception e)
        {
        }
        
        if(jvmOptions!=null)
        {
            for(int i=0; i<jvmOptions.length; i++)
            {
                addJvmArg(command, jvmOptions[i], action);
            }
            return;
        }
        
        //here we are only in case if jvm-options helper had exception
        // then - old style of parsing, to avoid exception
        if(args != null)
        {
            // loop through args
            for(int ii=0; ii < args.length; ii++)
            {
                // remove leading and trailing spaces
                systemProperty=args[ii].trim();
                
                if (bDebug) System.out.println("addJvmOptions: IN Property " + systemProperty);
                
                // ignore white space
                if(systemProperty.trim().equals(""))
                {
                    continue;
                }
                
                int iSpace=0, iQuote1=0, iQuote2=0;
                // loop through jvm-options line and see if multple entries on one line
                while(systemProperty.length() > 0)
                {
                    
                    // Find first space and quote
                    iSpace=systemProperty.indexOf(" -");
                    iQuote1=systemProperty.indexOf("\"");
                    
                    // see if it has a space that may specify 2 args
                    if (iSpace >= 0)
                    {
                        // see if there are double quotes, which could mean the space is part of the value
                        if (iQuote1 >= 0)
                        {
                            // see where quote is in relation to space, if a space exists
                            if (iQuote1 > iSpace && iSpace >= 0 )
                            {
                                // quote is before space so break up to space, should be full arg
                                addJvmArg(command, systemProperty.substring(0, iSpace), action);
                                // set remainder string, minus space delimiter
                                systemProperty=systemProperty.substring(iSpace + 1).trim();
                                if (bDebug) System.out.println("*** left 1:" + systemProperty);
                            }
                            else
                            {
                                // quote is first, could have a space in quotes, or just quoted string at end
                                
                                // loop to find next un-escaped quote
                                int iQuoteStartPos=iQuote1 + 1;
                                while (true)
                                {
                                    iQuote2=systemProperty.indexOf("\"", iQuoteStartPos);
                                    if (iQuote2 < 0)
                                    {
                                        // error can't find last quote, so log and exit loop
                                        getLogger().log(Level.WARNING, "launcher.missMatchQuotesInArg", systemProperty);
                                        // set to "" to end multiple arg loop
                                        systemProperty="";
                                        // breakout of inner quote loop
                                        break;
                                    }
                                    
                                    // found second quote see if it is escaped,which means the
                                    // value has internal quotes
                                    if (systemProperty.charAt(iQuote2 - 1) == '\\')
                                    {
                                        // quote escaped, look for next quote
                                        iQuoteStartPos=iQuote2 + 1;
                                        continue;
                                    }
                                    else
                                    {
                                        // found end quote that is not escaped
                                        
                                        // see if there are any more spaces after second quote
                                        // this happends when directories are added with spaces in them
                                        // like java.dirs.ext system property
                                        if (systemProperty.indexOf(" -", iQuote2) < 0)
                                        {
                                            // no more space, so space was enclosed in quotes
                                            // send total line as one
                                            addJvmArg(command, systemProperty, action);
                                            
                                            // should be at the end
                                            // set to "" to end multiple arg loop
                                            systemProperty="";
                                        }
                                        else
                                        {
                                            // another space was found in the line
                                            iQuote2++; // add on to include quote in arg
                                            addJvmArg(command, systemProperty.substring(0, iQuote2), action);
                                            // set remainder string minus space delimiter
                                            if (iQuote2 < systemProperty.length())
                                            {
                                                systemProperty=systemProperty.substring(iQuote2 + 1).trim();
                                                if (bDebug) System.out.println("*** left 2:" + systemProperty);
                                            }
                                            else
                                            {
                                                // should be at the end
                                                // set to "" to end multiple arg loop
                                                systemProperty="";
                                            }
                                        }
                                        // breakout of inner quote loop
                                        break;
                                    }
                                }
                            }
                            
                        }
                        else
                        {
                            // no quotes, just break on " -" for multiple args
                            // space could be non-quoted like in java.ext.dirs
                            int iDel=systemProperty.indexOf(" -");
                            while(iDel >= 0)
                            {
                                // found token
                                addJvmArg(command, systemProperty.substring(0, iDel), action);
                                systemProperty=systemProperty.substring(iDel + 1).trim();
                                iDel=systemProperty.indexOf(" -");
                            }
                            
                            // make sure you get the last one
                            if (!systemProperty.equals(""))
                            {
                                addJvmArg(command, systemProperty, action);
                            }
                            
                            // break out of multiple arg loop
                            break;
                        }
                    }
                    else
                    {
                        // no spaces, should just be one value so add
                        addJvmArg(command, systemProperty, action);
                        // break out of multiple arg loop
                        break;
                    }
                }
            }
        }
        
    }
    
    
    protected void addJvmArg(Command command, String option, String action)
    {
        
        //
        // fix for bug# 6416997 so that the memory options
        // -Xmx and -Xms are not passed to jvm for stop command
        //
        
        // WBN
        if(option == null)
            return;
        
        if (LAUNCHER_STOP_ACTION.equals(action))
        {
            option = removeJVMStopOption(option);
            if (option.equals(""))
            {
                return;
            }
        }
        if ( option.startsWith("-D") )
        {
            // set to systemvaiables for commandline ordering
            command.addSystemVariable(option);
        }
        else
        {
            if ( ( option.equals("-client")) || (option.equals("-server")) )
            {
                //If user mentions server or client mode for VM
                // then use that over default which is "server"
                // As we want to keep this as first arg don't add
                // to the jvmArgsList yet
                command.setMode(option);
            }
            else
            {
                // just add the option to the jvm options
                command.addJvmOption(option);
            }
        }
        
        //getLogger().log(Level.INFO,"addJvmOptions: OUT Property " + option);
        if (bDebug) System.out.println("addJvmOptions: OUT Property " + option);
    }
    
    
    /**
     * This method handles the debug options and was pulled from the LaunchFilter
     */
    protected void addDebugOptions(Command command, String debug_options)
    {
        // only do for start action, not stop
        
        // If debug is enabled, then we need to pass on -Xdebug option
        command.addDebugOption("-Xdebug");
        
        // It seems that -Xdebug and other debug options shouldn't go
        // as one argument  So we will check if debug_options starts
        //  with that and give it as separate argument
        debug_options=debug_options.trim();
        if ( debug_options.startsWith("-Xdebug") )
        {
            debug_options =debug_options.substring("-Xdebug".length()).trim();
        }
        
        // Get the JPDA transport and address (port) from the
        // debug_options. If address is not specified in debug_options
        // for transport=dt_socket, we find a free port
        // and add it to -Xrunjdwp.
        //
        // If address is specified in -Xrunjdwp, then the JVM
        // does not print any debug message, so we need to print it for
        // easy viewing by the user.
        // If address is not specified in debug_options,
        // then the JVM will print a message like:
        // Listening for transport dt_socket at address: 33305
        // This is only visible with "asadmin start-domain --verbose"
        //
        // The format of debug_options is:
        // -Xrunjdwp:<name1>[=<value1>],<name2>[=<value2>]
        
        String transport = getDebugProperty(debug_options, "transport");
        String addr = getDebugProperty(debug_options, "address");
        
        if ( transport == null || transport.equals("") )
        {
            // XXX I18N this
            // throw exception
            System.out.println("Cannot start server in debug mode: no transport specified in debug-options in domain.xml.");
        }
        
        if ( transport.equals("dt_socket") )
        {
            if ( addr != null && !addr.equals("") )
            {
                // XXX Should we check if the port is free using
                // com.sun.enterprise.util.net.NetUtils.isPortFree(port)
            }
            else
            {
                // Get a free port
                int port =
                        com.sun.enterprise.util.net.NetUtils.getFreePort();
                if ( port == 0 )
                {
                    // XXX I18N this
                    // throw exception ???
                    System.out.println("Cannot start server in debug mode: unable to obtain a free port for transport dt_socket.");
                }
                addr = String.valueOf(port);
                
                debug_options = debug_options + ",address=" + addr;
            }
        }
        
        command.addDebugOption(debug_options);
        
        // Provide the actual JDWP options to the server using a
        // system property. This allow the server to make it available
        // to the debugger (e.g. S1 Studio) using an API.
        String jdwpOptions = debug_options.substring(
                debug_options.indexOf("-Xrunjdwp:") + "-Xrunjdwp:".length());
        
        command.addSystemVariable(DEBUG_OPTIONS, jdwpOptions);
        
    }
    
    protected String getDebugProperty(String debug_options, String name)
    {
        int nameIndex;
        if ( (nameIndex = debug_options.indexOf(name)) != -1 )
        {
            // format is "name=value"
            String value = debug_options.substring(nameIndex
                    + name.length() + 1);
            int commaIndex;
            if ( (commaIndex = value.indexOf(",")) != -1 )
            {
                value = value.substring(0, commaIndex);
            }
            return value;
        }
        return null;
    }
    
    protected boolean isWaitEnabled()
    {
        boolean bRet=false;
        String launcherRet=System.getProperty(LaunchConstants.LAUNCHER_RETURN_PROP);
        if(launcherRet != null && launcherRet.equals(LaunchConstants.LAUNCHER_RETURN_HOLD_VALUE))
        {
            bRet=true;
        }
        return bRet;
    }
    
    protected boolean isVerboseEnabled()
    {
        return argExists(COMMAND_LINE_ARG_VERBOSE);
    }
    
    
    protected boolean isDebugEnabled()
    {
        return argExists(COMMAND_LINE_ARG_DEBUG);
    }
    
    protected boolean isDisplayEnabled()
    {
        return argExists(COMMAND_LINE_ARG_DISPLAY);
    }
    
    protected boolean isServerProfile()
    {
        String processName=getProcessLauncherProfile();
        return processName.equals(INTERNAL_SERVER_PROFILE) ||
                processName.equals(AS9_INTERNAL_SERVER_PROFILE);
    }
    
    protected boolean isNodeAgentProfile()
    {
        String processName=getProcessLauncherProfile();
        return processName.equals(INTERNAL_NODE_AGENT_PROFILE);
    }
    
    
    protected String getFiletRelativeName(String action)
    {
        return System.getProperty(SystemPropertyConstants.INSTANCE_ROOT_PROPERTY) + File.separator
                + LAUNCHER_SCRIPT_LOCATION + File.separator + action;
    }
    
    
    protected String getProcessLauncherProfile()
    {
        return System.getProperty(LaunchConstants.PROCESS_NAME_PROP, INTERNAL_SERVER_PROFILE);
    }
    
    protected String getScriptRelativeName(String action)
    {
        String sxRet=getFiletRelativeName(action);
        if(OS.isWindows())
        {
            sxRet+="_temp.bat";
        }
        else
        {
            sxRet+="_temp.sh";
        }
        return sxRet;
    }
    
    protected void setArgs(String[] args)
    {
        ArrayList<String> processedArgs = new ArrayList<String>(args.length);
        // Process the args
        for(String arg : args)
        {
            // allow "debug" to be used as an arg in startserv script...
            if(arg.equalsIgnoreCase("debug"))
                GFSystem.setProperty("Debug", "true");
            
            processedArgs.add(arg);
        }
        _args = processedArgs.toArray(new String[processedArgs.size()]);
    }
    
    protected String[] getArgs()
    {
        return _args;
    }
    
    protected boolean argExists(String arg)
    {
        boolean bRet=false;
        String[] args=getArgs();
        if(args != null)
        {
            for(int ii=0; ii < args.length; ii++)
            {
                if(args[ii].equals(arg))
                {
                    bRet=true;
                    break;
                }
            }
        }
        return bRet;
    }
    
    
    /**
     * createFileStructure - This method validates that that the file can be written to.  It the
     * if the parent directory structure does not exist, it will be created
     *
     * @param logFile - fully qualified path of the logfile
     */
    protected boolean createFileStructure(String logFile)
    {
        boolean bRet=false;
        File outputFile=new File(logFile);
        
        try
        {
            // Verify that we can write to the output file
            File parentFile = new File(outputFile.getParent());
            // To take care of non-existent log directories
            if ( !parentFile.exists() )
            {
                // Trying to create non-existent parent directories
                parentFile.mkdirs();
            }
            // create the file if it doesn't exist
            if (!outputFile.exists())
            {
                outputFile.createNewFile();
            }
            if (outputFile.canWrite())
            {
                // everything is okay to logfile
                bRet=true;
            }
        }
        catch (IOException e)
        {
            // will only see on verbose more, so okay
            e.printStackTrace();
        }
        
        if (!bRet)
        {
            // log can't be created or isn't writtable
            getLogger().log(Level.WARNING,"launcher.logWriteFailure", logFile);
        }
        
        return bRet;
    }
    
    private String getPathItemsWithSpaces(String path)
    {
        StringTokenizer st = new StringTokenizer(path, File.pathSeparator);
        String ret = "";
        boolean firstItem = true;
        
        while (st.hasMoreTokens())
        {
            String item = st.nextToken();
            
            if(item.indexOf(' ') >= 0)
            {
                if(!firstItem)
                    ret += File.pathSeparator;
                else
                    firstItem = false;
                
                ret += item;
            }
        }
        
        return ret;
    }
    
    
    private String removeJVMStopOption(String jvmOption)
    {
        if (jvmOption.startsWith("-X"))
        {
            jvmOption = "";
        }
        if (jvmOption.startsWith("-server"))
        {
            jvmOption = "-client";
        }
        return jvmOption;
    }
    private void setPromptForIdentity(Command command)
    {
        // WBN -- the current xml files wires in the sys prop: com.sun.aas.promptForIdentity=true
        // we may not want that so we remove it if it is false in the "real" System Properties
        // note that if it is set to ANYTHING it is taken as "true", even if it is set to "false"!
        // so it has to be gone for it to be "false"
        
        Object prompt = null;
        
        // may be null!
        String actual = System.getProperty(PROPMPT_FOR_IDENTITY_SYSTEM_PROPERTY);
        String startsWith = "-D" + PROPMPT_FOR_IDENTITY_SYSTEM_PROPERTY;
        // remove it in all cases and add it back only if actual is not null...
        
        for(Object o : command._systemVariables)
        {
            String s = (String)o;
            
            if(s.startsWith(startsWith))
            {
                prompt = o;
                break;
            }
        }
        // remove it always
        if(prompt != null)
            command._systemVariables.remove(prompt);
        
        // put it back in if it is set in System Properties
        if(actual != null)
            command.addSystemVariable(PROPMPT_FOR_IDENTITY_SYSTEM_PROPERTY, actual);
    }
    
    private void readASEnv() throws ASLauncherException
    {
        // WBN October 2006
        // see if we have a config root set
        String configRoot=System.getProperty(SystemPropertyConstants.CONFIG_ROOT_PROPERTY);
        
        if (configRoot == null)
        {
            // no config root try and make one from install root
            configRoot=System.getProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY) + File.separator + "config";
            // let's go ahead and set the sys prop now...
            GFSystem.setProperty(SystemPropertyConstants.CONFIG_ROOT_PROPERTY, configRoot);
        }
        
        if(!FileUtils.safeIsDirectory(configRoot))
        {
            String message = "Can't find the AS configuration script.  This script normally lives in <install>/config and is named asenv";
            
            if(OS.isWindows())
                message += ".bat";
            message += "\nYou need to set these 2 System Properties: \n" + SystemPropertyConstants.INSTALL_ROOT_PROPERTY + "=your-install-directory\n" +
                    SystemPropertyConstants.CONFIG_ROOT_PROPERTY + "=your-config-directory\n";
            throw new ASLauncherException(message);
        }
        ASenvPropertyReader reader = new ASenvPropertyReader(configRoot);
        reader.setSystemProperties();
    }
    
    
    void writeSecurityInfoToProcess(Process p)
    {
        if(securityInfo == null) // impossible!
            return;
        
        PrintWriter out = null;
        try
        {
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(p.getOutputStream())));
            
            for(String s : securityInfo)
            {
                if(bDebug)
                {
                    System.out.println("InputLine ->" + s + "<-");
                }
                out.println(s);
            }
            out.flush();
        }
        finally
        {
            try
            {
                out.close();
            }
            catch (Throwable t)
            {
                // nothing to do
            }
        }
    }
    
    private void setDomainName(Domain domain) throws ConfigException
    {
        try
        {
            ElementProperty ep = domain.getElementPropertyByName(DOMAIN_NAME_XML_PROPERTY);
            
            if(ep == null)
                throw new ConfigException("The property, " + DOMAIN_NAME_XML_PROPERTY + ", was null");
            
            String domainName = ep.getValue();
            
            if(domainName == null || domainName.length() < 1)
                throw new ConfigException("The value of the property, " + DOMAIN_NAME_XML_PROPERTY + ", was null or empty");
            
            GFSystem.setProperty(DOMAIN_NAME_SYSTEM_PROPERTY, domainName);
        }
        catch(ConfigException ce)
        {
            throw ce;
        }
        catch(Exception e)
        {
            throw new ConfigException("Error getting " + DOMAIN_NAME_XML_PROPERTY + " from domain.xml", e);
        }
    }
    
     void getSecurityFromUser(String[] args) throws IOException
     {
        if(System.getProperty("caller", "not-cli").equals("cli"))
         return;

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        securityInfo = new String[3];
        System.out.print("admin username:");
        securityInfo[0] = reader.readLine();
        securityInfo[1] = getPassword(reader, "admin password:  ");
        securityInfo[2] = getPassword(reader, "master password: ");
        GFSystem.setProperty(PROPMPT_FOR_IDENTITY_SYSTEM_PROPERTY, "true");
     }
     
    private String getPassword(BufferedReader stdin, String prompt) throws IOException
    {
        try
        {
            NativeUtils nativeUtils = new NativeUtils();
            return nativeUtils.getPassword(prompt);
        }
        catch(Throwable t)
        {
                // fall through -- it needs to be Throwable, not Exception...
        }

        System.out.print(prompt);
       
        return stdin.readLine();    
    }
    
    private void setInstanceRoot(Properties callerProps)  throws ASLauncherException
    {
        String irpTag = SystemPropertyConstants.INSTANCE_ROOT_PROPERTY; // too ugly to type!
        String irp = callerProps.getProperty(irpTag);
        
        if(irp != null)
            GFSystem.setProperty(irpTag, irp);
        
        
        // if the user is running a script stand-alone the instance-root may be
        // something like "./startserv".  We need to convert it to a real path
        
        irp = System.getProperty(irpTag);
        
        if(irp == null || irp.length() <= 0 )
        {
            // if we are being called from stand-alone java or a script -- they need to
            // do a -DirpTag=...
            // if we are called from PEInstancesManager, it has been set.  If it isn't
            // set let's fail right now!
            throw new ASLauncherException("Missing System Property: " + irpTag);
        }
        
        File f = new File(irp);
        try
        {
            irp = f.getCanonicalPath();
        }
        catch(Exception e)
        {
            irp = f.getAbsolutePath();
        }
        
        GFSystem.setProperty(irpTag, irp);
    }
    
    private String getPELauncherJarPath()
    {
        // WBN May 18, 2007 appserv-launch.jar removed from config's system-classpath'
        return System.getProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY) 
                + File.separator
                + "lib"
                + File.separator
                + APPSERV_LAUNCH_JAR;
    }
    
    // END END END END ENDEND -- this is here for grepping to the end of this gigantic class!!
    
    //**************************************************************************
    // ***********        protected inner Command class        *****************
    //**************************************************************************
    /**
     * This class is a structure class that holds the executable command as it is compiled.  Once the
     * command is completed, an instance method returns the command in the form of a
     * string array, with or without the classpath information present.  The without
     * classpath option is triggered by having the commands LaunchConstants.LAUNCHER_RETURN_PROP
     * is flagged as "hold",  setting the "com.sun.aas.limitedCommamdExecution" and being on
     * the Windows platform.  This was used as an intermediate solution to get around the
     * command execution length limitation on Windows.  The classpath information was
     * written out to be executed as a separate "set" command.
     */
    
    // thread unsafe class!!!!
    protected class Command
    {
		// WBN 4/20/07 -- _sysProps are the properties that will be given to the JVM
		// as "-Dname=value".  _systemVariables is a list of those fully layed out
		// strings.
        private final Properties		  _sysProps			= new Properties(); 
        private final ArrayList<String> _jvmOptions		= new ArrayList<String>();
        private final ArrayList<String> _systemVariables	= new ArrayList<String>();
        private final ArrayList<String> _args				= new ArrayList<String>();
        private final ArrayList<String> _debugOptions		= new ArrayList<String>();
        private String _mainClass;
        private String _classpath;
        private String _javaCommand;
        private String _mode;
        private String _logFile;
        private String _nativeClasspath;
        
        private void resolveAll()
        {
            TokenResolver resolver = new TokenResolver(_sysProps);
            resolver.resolve(_jvmOptions);
            resolver.resolve(_systemVariables);
            resolver.resolve(_debugOptions);
            resolver.resolve(_args);
            _classpath          = resolver.resolve(_classpath);
            _javaCommand        = resolver.resolve(_javaCommand);
            _mode               = resolver.resolve(_mode);
            _logFile            = resolver.resolve(_logFile);
            _nativeClasspath    = resolver.resolve(_nativeClasspath);
            _mainClass          = resolver.resolve(_mainClass);
        }
    
        protected void addJvmOption(String jvmOptions)
        {
            // must trim these or Runtime.exec will blow with leading spaces
            _jvmOptions.add(jvmOptions.trim());
        }
        
        protected void addSystemVariable(String systemVariable)
        {
            // format "-Dxxx=yyy"
            // format "-Dx"
            if(systemVariable.length() < 3)
                return;
            
            String s = systemVariable.substring(2);
            int index = s.indexOf("=");
            
            if(index <= 0)
            {
                // handle "-DXXX"
                addSystemVariable(s, null);
            }
            else
            {
                String name = s.substring(0, index);
                String value = s.substring(index + 1);
                addSystemVariable(name, value);
            }
        }
        void addSystemVariable(String name, String value)
        {
            name = name.trim();
            
            if(value == null)
            {
                // this is something like: 
                // <jvm-option>-Dxyz</jvm-option>
                // There is no value to use for token replacement -- 
                // so don't add to _sysProps
                _systemVariables.add("-D" + name);
            }
            else
            {
                // value == "" is OK
                value = value.trim();
                _sysProps.setProperty(name, value);
                _systemVariables.add("-D" + name + "=" + value);
            }
        }
        
        protected void addArg(String arg)
        {
            _args.add(arg);
        }
        protected String[] getArgs()
        {
            return (String[])_args.toArray(new String[_args.size()]);
        }
        protected void removeArg(String arg)
        {
            _args.remove(arg);
        }
        
        protected void addDebugOption(String debugOption)
        {
            _debugOptions.add(debugOption);
        }
        protected String[] getDebugOptions()
        {
            return (String[])_debugOptions.toArray(new String[_debugOptions.size()]);
        }
        
        protected void setMode(String mode)
        {
            _mode=mode;
        }
        protected String getMode()
        {
            return _mode;
        }
        
        protected void setMainClass(String mainClass)
        {
            _mainClass=mainClass;
        }
        protected String getMainClass()
        {
            return _mainClass;
        }
        
        protected void setNativeClasspath(String classpath)
        {
            _nativeClasspath=classpath;
        }
        protected String getNativeClasspath()
        {
            return _nativeClasspath;
        }
        
        protected void setClasspath(String classpath)
        {
            _classpath=classpath;
        }
        protected String getClasspath()
        {
            return _classpath;
        }
        
        protected void setJavaCommand(String javaCommand)
        {
            _javaCommand=javaCommand;
        }
        protected String getJavaCommand()
        {
            return _javaCommand;
        }
        
        protected void setLogFile(String logFile)
        {
            if (bDebug) System.out.println("Logfile set to " + logFile);
            _logFile=logFile;
        }
        protected String getLogFile()
        {
            return _logFile;
        }
        
        /*
         * This method returns the command in standard java format
         * which can be used by Runtime.execute
         */
        protected String[] getCommandAsArray()
        {
            resolveAll();
            ArrayList<String> cmd=new ArrayList<String>();
            cmd.add(_javaCommand);
            cmd.addAll(_systemVariables);
            if(_mode != null)
            {
                cmd.add(_mode);
            }
            cmd.addAll(_debugOptions);
            cmd.addAll(_jvmOptions);
            // put cp on separate line of Runtimes execute
            // doesn't work
            cmd.add("-cp");
            cmd.add(_classpath);
            cmd.add(_mainClass);
            cmd.addAll(_args);
            return (String[])cmd.toArray(new String[cmd.size()]);
        }
        
        /**
         * This method return the executable command without the classpath attached
         * It is used when building scripts that
         * are executed in an environment with a limited command line length like
         * windows 2000 - 2071 character limit and XP - 8000 character limit
         * To get full command use getClasspath() also
         */
        protected String[] getLimitedCommandAsArray()
        {
            resolveAll();
            ArrayList<String> cmd=new ArrayList<String>();
            cmd.add(_javaCommand);
            cmd.addAll(_systemVariables);
            if(_mode != null)
            {
                cmd.add(_mode);
            }
            cmd.addAll(_debugOptions);
            cmd.addAll(_jvmOptions);
            cmd.add(_mainClass);
            cmd.addAll(_args);
            return (String[])cmd.toArray(new String[cmd.size()]);
        }
        
        
        /*
         * This method returns the command in a format that is JNI invocation api friendly and
         * can be easely digested in the native environment
         */
        protected String[] getCommandInJNIFormatAsArray()
        {
            // display java command to stdout in the following format.
            // NOTE: if this becomes externally available, use xml, for now its an internal structure.
            // Class Name (path is seperated by "/"
            // commandline Args
            // everything else (should start with a "-")
            // classpath should be prepended with "-Djava.class.path="
            
            // alter main class path for jni invocation api, do it here to keep as much out of native as possible

            resolveAll();
            String jniMainClassName=_mainClass;
            int iPos=0;
            while((iPos=jniMainClassName.indexOf(".")) >= 0)
            {
                jniMainClassName=jniMainClassName.substring(0, iPos) + "/" + jniMainClassName.substring(iPos + 1);
            }
            
            ArrayList<String> cmd=new ArrayList<String>();
            cmd.addAll(_systemVariables);
            if(_mode != null)
            {
                cmd.add(_mode);
            }
            cmd.addAll(_debugOptions);
            cmd.addAll(_jvmOptions);
            cmd.add("-Djava.class.path=" + _classpath);
            cmd.add(_mainClass);
            cmd.addAll(_args);
            return (String[])cmd.toArray(new String[cmd.size()]);
        }
        
        
        /**
         * This method returns only the system variables for the command to execute
         */
        protected String[] getSystemVariablesAsArray()
        {
            ArrayList cmd=new ArrayList();
            cmd.addAll(_systemVariables);
            return (String[])cmd.toArray(new String[cmd.size()]);
        }
        
        
        public String toString()
        {
            StringBuffer cmd= new StringBuffer();
            
            String[] ret=getCommandAsArray();
            for(int ii=0; ii < ret.length; ii++)
            {
                cmd.append(ret[ii]);
            }
            
            return cmd.toString();
        }
        
        public String toStringWithLines()
        {
            StringBuffer cmd= new StringBuffer();
            
            String[] ret=getCommandAsArray();
            for(int ii=0; ii < ret.length; ii++)
            {
                cmd.append("\n" + ret[ii]);
            }
            return cmd.toString();
        }

        ////////////////////////////////////////////////////////////
        
        void reorder()
        {
            // WBN March 2007
            reorderJVMOptions();
            reorderSystemProps();
        }
        
        private void reorderJVMOptions()
        {
            /* WBN MARCH 2007 
             * There *is* an order dependency in JVM options.  I.e. I know of one 
             * such case.  The Unlock... option must appear before the other two:
             * <jvm-options>-XX:+UnlockDiagnosticVMOptions</jvm-options>
             * <jvm-options>-XX:+LogVMOutput</jvm-options>
             * <jvm-options>-XX:LogFile=${com.sun.aas.instanceRoot}/logs/jvm.log</jvm-options>            
             * -- So reorder the list of jvm options (possibly)...
             */
            // WBN March 2007
            String opt = "-XX:+UnlockDiagnosticVMOptions";
            int index = _jvmOptions.indexOf(opt);
            
            if(index > 0) // yes ">" not ">=" -- if index is zero, we are done!
            {
                // remove it...
                _jvmOptions.remove(index);
                // add it back to the beginning...
                _jvmOptions.add(0, opt);
            }
        }

        private void reorderSystemProps()
        {
            // WBN March 2007
            // There are a LOT of these.  Why not write them out in alphabetical 
            // order?
            
            Collections.sort(_systemVariables);

            // WBN July 2007
            // move INSTANCE_ROOT_PROPERTY to the beginning so that we can see 
            // the name easily with 'jps -v' output

            final String irp = SystemPropertyConstants.INSTANCE_ROOT_PROPERTY;
            
            // first get the index...
            for(int i = 0; i < _systemVariables.size(); i++)
            {
                String s = _systemVariables.get(i);
                
                if(s != null && s.startsWith("-D" + irp + "="))
                {
                    // got it!
                    _systemVariables.remove(i);
                    _systemVariables.add(0, s);
                    break;
                }
            }
        }
    }
    
    
    //**************************************************************************
    //***********        protected inner Classpath class       *****************
    //**************************************************************************
    /**
     * A class that encaspilates the functionality required to derive the classpath from criteria in
     * attributes of the classpath element that is present in the processLauncher.xml file.
     */
    protected static class Classpath
    {
        /**
         * getLibClasspath - This method returns a string classpath which represents the items in the
         * lib directory in accordance with the regular expressions that represents the
         * include and excludes attributes of the processLaunher.xml
         */
        protected static String getLibClasspath(String libDir, String includes, String excludes)
        {
            ArrayList arIncludes=new ArrayList();
            ArrayList arExcludes=new ArrayList();
            
            // construct include and excludes for comparison
            StringTokenizer st=new StringTokenizer(includes, ",");
            while(st.hasMoreTokens())
            {
                arIncludes.add(st.nextToken().trim());
            }
            st=new StringTokenizer(excludes, ",");
            while(st.hasMoreTokens())
            {
                arExcludes.add(st.nextToken().trim());
            }
            
            String path="";
            // if lib dir exists then see if items can be included or excluded.
            if (libDir != null && !libDir.equals(""))
            {
                // get file dir
                File dir=new File(libDir);
                
                // loop through items in directory
                String[] filenames=dir.list();
                for(int ii=0; ii < filenames.length; ii++)
                {
                    
                    // see if should be included
                    if(matchStringToList(filenames[ii], arIncludes) && !matchStringToList(filenames[ii], arExcludes))
                    {
                        // see if in excluded list
                        path += libDir + File.separator + filenames[ii] + File.pathSeparator;
                    }
                }
                
                // remove last pasthSeparator
                if(path.endsWith(File.pathSeparator))
                {
                    path=path.substring(0, path.length()-1);
                }
            }
            
            return path;
        }
        
        
        /**
         * matchStringToList - This method performs a match to each item in the arraylist to the
         * filename.
         */
        protected static boolean matchStringToList(String filename, ArrayList list)
        {
            boolean bRet=false;
            
            String criteria=null, endMatch=null;
            Iterator it=list.iterator();
            while(it.hasNext())
            {
                criteria=(String)it.next();
                
                if (criteria.startsWith("*"))
                {
                    // give a work around for people who don't know regular expressions
                    // legacy functionality
                    endMatch=criteria.substring(1);
                    // see if end matches rest of criteria string
                    if (filename.endsWith(endMatch))
                    {
                        // match, so set return and break out
                        bRet=true;
                        break;
                    }
                }
                else if (isRegularExpression(criteria))
                {
                    // contains wildcard use regexpression
                    if(Pattern.matches(criteria, filename))
                    {
                        // equals, so set return and break out
                        bRet=true;
                        break;
                    }
                }
                else
                {
                    // perform straight equals
                    if(filename.equals(criteria))
                    {
                        // equals, so set return and break out
                        bRet=true;
                        break;
                    }
                }
            }
            
            return bRet;
        }
        
        /**
         * isRegularExpression - This method checks to see if the item in the arraylist could be a regular expression.
         * This method is not full proof and may need to be modified to suite the appservers needs
         */
        protected static boolean isRegularExpression(String criteria)
        {
            boolean bRet=false;
            if(criteria.indexOf("^") > -1 || criteria.indexOf("$")  > -1 || criteria.indexOf("[")  > -1
                    || criteria.indexOf("]")  > -1 || criteria.indexOf("*")  > -1)
            {
                bRet=true;
            }
            return bRet;
        }
    }
    
    
    
    //**************************************************************************
    //***********        protected inner StreamFlusher class   *****************
    //**************************************************************************
    /**
     * A class that attaches to the output streams of the executed process and sends the data
     * to the calling processes output streams
     */
    protected class StreamFlusher implements Runnable
    {
        
        private InputStream _input=null;
        private OutputStream _output=null;
        private String _logFile=null;
        
        
        public StreamFlusher(InputStream input, OutputStream output)
        {
            this(input, output, null);
        }
        
        public StreamFlusher(InputStream input, OutputStream output, String logFile)
        {
            this._input=input;
            this._output=output;
            this._logFile=logFile;
        }
        
        public void run()
        {
            
            // check for null stream
            if (_input == null) return;
            
            PrintStream printStream=null;
            
            // If applicable, write to a log file
            if (_logFile != null)
            {
                try
                {
                    if(createFileStructure(_logFile))
                    {
                        // reset streams to logfile
                        printStream = new PrintStream(new FileOutputStream(_logFile, true), true);
                    }
                    else
                    {
                        // could not write to log for some reason
                        _logFile=null;
                    }
                }
                catch (IOException ie)
                {
                    ie.printStackTrace();
                    _logFile=null;
                }
            }
            
            // transfer bytes from input to output stream
            try
            {
                int byteCnt=0;
                byte[] buffer=new byte[4096];
                while ((byteCnt=_input.read(buffer)) != -1)
                {
                    if (_output != null && byteCnt > 0)
                    {
                        _output.write(buffer, 0, byteCnt);
                        _output.flush();
                        
                        // also send to log, if it exists
                        if (_logFile != null)
                        {
                            printStream.write(buffer, 0, byteCnt);
                            printStream.flush();
                        }
                    }
                    //yield();
                }
            }
            catch (IOException e)
            {
                // just log this as an finest exception, because it really should matter
                //getLogger().log(FINE_LEVELST,"Exception thrown while reading/writing verbose error stream", e);
            }
            finally
            {
                // 6581651 WBN July 18, 2007
                // close the handle to the logfile when the process dies.
                // In Windows, if we don't do this, then NA will keep a handle
                // to the logfile open after the instance is dead and the instance
                // files can't be deleted.
                if(printStream != null)
                    printStream.close();
            }
        }
    }
}
