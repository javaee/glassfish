/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 */
package com.sun.enterprise.admin.launcher;

import com.sun.enterprise.universal.collections.CollectionUtils;
import com.sun.enterprise.universal.glassfish.GFLauncherUtils;
import com.sun.enterprise.universal.glassfish.TokenResolver;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.universal.process.ProcessStreamDrainer;
import com.sun.enterprise.universal.xml.MiniXmlParserException;
import java.io.*;
import java.util.*;
import com.sun.enterprise.universal.glassfish.ASenvPropertyReader;
import com.sun.enterprise.universal.xml.MiniXmlParser;
import java.util.logging.Level;
import static com.sun.enterprise.util.SystemPropertyConstants.*;
import static com.sun.enterprise.admin.launcher.GFLauncherConstants.*;
import java.util.logging.Logger;
//import com.sun.enterprise.security.store.PasswordAdapter;
//import com.sun.enterprise.security.store.IdentityManager;

/**
 * This is the main Launcher class designed for external and internal usage.
 * Each of the 3 kinds of server -- domain, node-agent and instance -- need
 * to sublass this class.  
 * @author bnevins
 */
public abstract class GFLauncher {
    ///////////////////////////////////////////////////////////////////////////
    //////     PUBLIC api area starts here             ////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    /**
     * 
     * @return The info object that contains startup info
     */
    public final GFLauncherInfo getInfo() {
        return info;
    }

    /**
     * Launches the server.  Any fatal error results in a GFLauncherException
     * No unchecked Throwables of any kind will be thrown.
     * 
     * @throws com.sun.enterprise.admin.launcher.GFLauncherException 
     */
    public final synchronized void launch() throws GFLauncherException {
        try {
            startTime = System.currentTimeMillis();
            if (!setupCalledByClients)
                setup();
            internalLaunch();
        }
        catch (GFLauncherException gfe) {
            throw gfe;
        }
        catch (Throwable t) {
            // hk2 might throw a java.lang.Error
            throw new GFLauncherException(strings.get("unknownError", t.getMessage()) ,t);
        }
        finally {
            GFLauncherLogger.removeLogFileHandler();
        }
    }
    /**
     * Launches the server - but forces the setup() to go through again.
     * @throws com.sun.enterprise.admin.launcher.GFLauncherException
     */
    public final synchronized void relaunch() throws GFLauncherException {
        setupCalledByClients = false;
        launch();
    }

    public final synchronized void launchJVM(List<String> cmdsIn) throws GFLauncherException {
        try {
            setup();    // we only use one thing -- the java executable
            List<String> commands = new LinkedList<String>();
            commands.add(javaExe);

            for(String cmd : cmdsIn) {
                commands.add(cmd);
            }

            ProcessBuilder pb = new ProcessBuilder(commands);
            Process p = pb.start();
            ProcessStreamDrainer.drain("launchJVM", p); // just to be safe
        }
        catch (GFLauncherException gfe) {
            throw gfe;
        }
        catch (Throwable t) {
            // hk2 might throw a java.lang.Error
            throw new GFLauncherException(strings.get("unknownError", t.getMessage()) ,t);
        }
        finally {
            GFLauncherLogger.removeLogFileHandler();
        }
    }

    public synchronized void setup() throws GFLauncherException, MiniXmlParserException {
        ASenvPropertyReader pr;
        if(isFakeLaunch()) {
            pr = new ASenvPropertyReader(info.getInstallDir());
        }
        else {
            pr = new ASenvPropertyReader();
        }
        
        asenvProps = pr.getProps();
        info.setup();
        setupLogLevels();
        MiniXmlParser parser = new MiniXmlParser(getInfo().getConfigFile(), getInfo().getInstanceName());
        String domainName = parser.getDomainName();
        if(GFLauncherUtils.ok(domainName)) {
            info.setDomainName(domainName);
        }
        info.setAdminPorts(parser.getAdminPorts());
        javaConfig = new JavaConfig(parser.getJavaConfig());
        setupProfilerAndJvmOptions(parser);
        setupMonitoring(parser);
        sysPropsFromXml = parser.getSystemProperties();
        asenvProps.put(INSTANCE_ROOT_PROPERTY, getInfo().getInstanceRootDir().getPath());
        debugOptions = getDebug();
        parser.setupConfigDir(getInfo().getConfigDir(), getInfo().getInstallDir());
        setLogFilename(parser);
        resolveAllTokens();
        fixLogFilename();
        GFLauncherLogger.addLogFileHandler(logFilename, info);
        setJavaExecutable();
        setClasspath();
        setCommandLine();
        logCommandLine();
        // if no <network-config> element, we need to upgrade this domain
        needsUpgrade = !parser.hasNetworkConfig();
        setupCalledByClients = true;
    }

    /**
     * Returns the exit value of the process.  This only makes sense when we ran
     * in verbose mode and waited for the process to exit in the wait() method.
     * Caveat Emptor!
     * @return the process' exit value if it completed and we waited.  Otherwise
     * it returns -1
     */
    public final int getExitValue() {
        return exitValue;
    }
    
    /**
     * You don't want to call this before calling launch because it would not 
     * make sense.
     * @return The Process object of the launched Server process. you will either get
     * a valid Process object or an Exceptio will be thrown.  You are guaranteed not to get a null.
     * @throws GFLauncherException if the Process has not been created yet - call launch()
     * before calling this method.
     */
    public final Process getProcess() throws GFLauncherException {
        if(process == null)
                throw new GFLauncherException("invalid_process");

        return process;
    }

    /**
     * A ProcessStreamDrainer is always attached to every Process created here.
     * It is handy for getting the stdin and stdout as a nice String.
     *
     * @return A valid ProcessStreamDrainer.  You are guaranteed to never get a null.
     * @throws GFLauncherException if the process has not launched yet
     * @see com.sun.enterprise.universal.process.ProcessStreamDrainer
     */
    public final ProcessStreamDrainer getProcessStreamDrainer() throws GFLauncherException {
        if(psd == null)
            throw new GFLauncherException("invalid_psd");

        return psd;
    }

    /**
     * Get the location of the server logfile
     * @return The full path of the logfile
     * @throws GFLauncherException if you call this method too early
     */
    public String getLogFilename() throws GFLauncherException {
        if(!logFilenameWasFixed)
            throw new GFLauncherException(strings.get("internalError") + " call to getLogFilename() before it has been initialized");

        return logFilename;
    }

    /**
     * Return the port number of the debug port, or -1
     * if debugging is not enabled.
     *
     * @return the debug port, or -1 if not debugging
     */
    public final int getDebugPort() {
        // look for an option of this form:
        // -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=9009
        // and extract the address value
        for (String opt : debugOptions) {
            if (!opt.startsWith("-Xrunjdwp:"))
                continue;
            String[] attrs = opt.substring(10).split(",");
            for (String attr : attrs) {
                if (attr.startsWith("address=")) {
                    try {
                        return Integer.parseInt(attr.substring(8));
                    } catch (NumberFormatException ex) {
                        return -1;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Does this domain need to be upgraded before it can be started?
     *
     * @return true if the domain needs to be upgraded first
     */
    public boolean needsUpgrade() {
        return needsUpgrade;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    //////     ALL private and package-private below   ////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    abstract void internalLaunch() throws GFLauncherException;

    private void setLogFilename(MiniXmlParser parser) throws GFLauncherException {
        logFilename = parser.getLogFilename();

        if(logFilename == null)
            logFilename = DEFAULT_LOGFILE;
    }

    private void fixLogFilename() throws GFLauncherException {
        if(!GFLauncherUtils.ok(logFilename))
            logFilename = DEFAULT_LOGFILE;

        File f = new File(logFilename);

        if(!f.isAbsolute()) {
            // this is quite normal.  Logging Service will by default return
            // a relative path!
            f = new File(info.getInstanceRootDir(), logFilename);
        }

        // Get rid of garbage like "c:/gf/./././../gf"
        f = SmartFile.sanitize(f);

        // if the file doesn't exist -- make sure the parent dir exists
        // this is common in unit tests AND the first time the instance is
        // started....

        if(!f.exists()) {
            File parent = f.getParentFile();
            if(!parent.isDirectory()) {
                boolean wasCreated = parent.mkdirs();
                if(!wasCreated) {
                    f = null; // give up!!
                }
            }
        }

        if(f == null)
            logFilename = null;
        else
            logFilename = f.getPath();
                
        logFilenameWasFixed = true;
    }

    // unit tests will want 'fake' so that the process is not really started.
    enum LaunchType
    {
        normal, debug, trace, fake
    }; 
    
    void setMode(LaunchType mode) {
        this.mode = mode;
    }
    
    LaunchType getMode() {
        return mode;
    }
    
    boolean isFakeLaunch() {
        return mode == LaunchType.fake;
    }
    
    abstract List<File> getMainClasspath() throws GFLauncherException;

    abstract String getMainClass() throws GFLauncherException;

    GFLauncher(GFLauncherInfo info) {
        this.info = info;
    }

    final Map<String, String> getEnvProps() {
        return asenvProps;
    }

    final List<String> getCommandLine() {
        return commandLine;
    }
    
    final long getStartTime() {
        return startTime;
    }
    
    void launchInstance() throws GFLauncherException, MiniXmlParserException {
        if(isFakeLaunch()) {
            return;
        }
        
        List<String> cmds = getCommandLine();
        ProcessBuilder pb = new ProcessBuilder(cmds);
        
        //pb.directory(getInfo().getConfigDir());


        // change the directory if there is one specified, o/w stick with the
        // default.
        try {
            File newDir = getInfo().getConfigDir();
            pb.directory(newDir);
        }
        catch(Exception e) {
        }

        
        //run the process and attach Stream Drainers
        try {
            process = pb.start();
            if (getInfo().isVerbose()) {
                psd = ProcessStreamDrainer.redirect(getInfo().getDomainName(), process);
            }
            else {
                psd = ProcessStreamDrainer.save(getInfo().getDomainName(), process);
            }
            writeSecurityTokens(process);
        }
        catch (Exception e) {
            throw new GFLauncherException("jvmfailure", e, e);
        }

        long endTime = System.currentTimeMillis();
        GFLauncherLogger.info("launchTime", (endTime - getStartTime()));
        
        //if verbose, hang round until the domain stops
        if (getInfo().isVerbose())
            wait(process);
    }

    private void writeSecurityTokens(Process sp) throws GFLauncherException, IOException {
        handleDeadProcess();
        OutputStream os = sp.getOutputStream();
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(os));
            for(String token : info.securityTokens) {
                bw.write(token);
                bw.newLine();
                bw.flush();      //flusing once is ok too
            }
        } catch(IOException e) {
            handleDeadProcess();
            throw e;   //process is not dead, but got some other exception, rethrow it
        } finally {
            if (bw != null) {
                handleDeadProcess();
                bw.close();
            }
        }
    }
    private void handleDeadProcess() throws GFLauncherException {
        String trace = getDeadProcessTrace(process);
        if (trace != null)
            throw new GFLauncherException(trace);
    }
    
    private String getDeadProcessTrace(Process sp) throws GFLauncherException {
        //returns null in case the process is NOT dead
        try {
            int ev = sp.exitValue();
            ProcessStreamDrainer psd = getProcessStreamDrainer();
            String output = psd.getOutErrString();
            String trace = strings.get("server_process_died", ev, output);
            return trace;
        } catch(IllegalThreadStateException e) {
            //the process is still running and we are ok
            return null;
        }
    }

    void setCommandLine() throws GFLauncherException {
        List<String> cmdLine = getCommandLine();
        cmdLine.clear();
        addIgnoreNull(cmdLine, javaExe);
        addIgnoreNull(cmdLine, "-cp");
        addIgnoreNull(cmdLine, getClasspath());
        addIgnoreNull(cmdLine, debugOptions);

        String CLIStartTime = System.getProperty("WALL_CLOCK_START");

        if(CLIStartTime != null && CLIStartTime.length() > 0) {
            cmdLine.add("-DWALL_CLOCK_START=" + CLIStartTime);
        }

        if(jvmOptions != null)
            addIgnoreNull(cmdLine, jvmOptions.toStringArray());

        GFLauncherNativeHelper nativeHelper = new GFLauncherNativeHelper(info, javaConfig, jvmOptions, profiler);
        addIgnoreNull(cmdLine, nativeHelper.getCommands());
        addIgnoreNull(cmdLine, getMainClass());

        try {
            addIgnoreNull(cmdLine, getInfo().getArgsAsList());
        }
        catch(GFLauncherException gfle) {
            throw gfle;
        }
        catch(Exception e) {
            //harmless
        }
    }

    private void addIgnoreNull(List<String> list, String s) {
        if(GFLauncherUtils.ok(s))
            list.add(s);
    }
    private void addIgnoreNull(List<String> list, Collection<String> ss) {
        if(ss != null && !ss.isEmpty())
            list.addAll(ss);
    }


    private void wait(final Process p) throws GFLauncherException {
        try {
            setShutdownHook(p);
            p.waitFor();
            exitValue = p.exitValue();
        }
        catch (InterruptedException ex) {
            throw new GFLauncherException("verboseInterruption", ex, ex);
        }
    }

    private synchronized void setShutdownHook(final Process p) {
        // ON UNIX a ^C on the console will also kill DAS
        // On Windows a ^C on the console will not kill DAS
        // We want UNIX behavior on Windows
        // note that the hook thread will run in both cases:
        // 1. the server died on its own, e.g. with a stop-domain
        // 2. a ^C (or equivalent signal) was received by the console
        // note that exitValue is still set to -1

        // if we are restarting we may get many many processes.
        // Each time this method is called we reset the Process reference inside
        // the processWhacker

        if(processWhacker == null)  {
            Runtime runtime = Runtime.getRuntime();
            final String msg = strings.get("serverStopped", info.getType());
            processWhacker = new ProcessWhacker(p, msg);
            runtime.addShutdownHook(new Thread(processWhacker));
        }
        else
            processWhacker.setProcess(p);
    }
        
    ////////////////////////////////////////////////////////////////////////////
    ///////              EVERYTHING BELOW IS PRIVATE                  //////////
    ////////////////////////////////////////////////////////////////////////////

    private void resolveAllTokens() {
        // resolve jvm-options against:
        // 1. itself
        // 2. <system-property>'s from domain.xml
        // 3. system properties -- essential there is, e.g. "${path.separator}" in domain.xml
        // 4. asenvProps
        // 5. env variables
        // i.e. add in reverse order to get the precedence right

        Map<String, String> all = new HashMap<String, String>();
        Map<String, String> envProps = System.getenv();
        Map<String, String> sysProps =
                CollectionUtils.propertiesToStringMap(System.getProperties());

        // TODO: Uncomment when admin password processing & aliasing is sorted out.

        // Map<String, String> passwordAliases = new HashMap<String, String>();
        // try {
        //     String masterPassword = "changeit";
        //     if (IdentityManager.getMasterPassword() != null)
        //         masterPassword = IdentityManager.getMasterPassword();
        //     PasswordAdapter pa = new PasswordAdapter(masterPassword.toCharArray());
        //     Enumeration e = pa.getAliases();
        //     if (e.hasMoreElements()) {
        //         String alias = (String) e.nextElement();
        //         passwordAliases.put(alias, pa.getPasswordForAlias(alias));
        //     }
        // } catch (Exception e) {
        //     // TODO: ignore now. Defaults to not resolving password aliases
        // }
        // all.putAll(passwordAliases);

        all.putAll(envProps);
        all.putAll(asenvProps);
        all.putAll(sysProps);
        all.putAll(sysPropsFromXml);
        all.putAll(jvmOptions.getCombinedMap());
        all.putAll(profiler.getConfig());
        TokenResolver resolver = new TokenResolver(all);
        resolver.resolve(jvmOptions.xProps);
        resolver.resolve(jvmOptions.xxProps);
        resolver.resolve(jvmOptions.plainProps);
        resolver.resolve(jvmOptions.sysProps);
        resolver.resolve(javaConfig.getMap());
        resolver.resolve(profiler.getConfig());
        resolver.resolve(debugOptions);
        //resolver.resolve(sysPropsFromXml);
        logFilename = resolver.resolve(logFilename);
    // TODO ?? Resolve sysPropsFromXml ???
    }

    private void setJavaExecutable() throws GFLauncherException {
        // first choice is from domain.xml
        if (setJavaExecutableIfValid(javaConfig.getJavaHome()))
            return;

        // second choice is from asenv
        if (!setJavaExecutableIfValid(asenvProps.get(JAVA_ROOT_PROPERTY)))
            throw new GFLauncherException("nojvm");

    }

    void setClasspath() throws GFLauncherException {
        List<File> mainCP = getMainClasspath(); // subclass provides this
        List<File> envCP = javaConfig.getEnvClasspath();
        List<File> sysCP = javaConfig.getSystemClasspath();
        List<File> prefixCP = javaConfig.getPrefixClasspath();
        List<File> suffixCP = javaConfig.getSuffixClasspath();
        List<File> profilerCP = profiler.getClasspath();

        // create a list of all the classpath pieces in the right order
        List<File> all = new ArrayList<File>();
        all.addAll(prefixCP);
        all.addAll(profilerCP);
        all.addAll(mainCP);
        all.addAll(sysCP);
        all.addAll(envCP);
        all.addAll(suffixCP);
        setClasspath(GFLauncherUtils.fileListToPathString(all));
    }

    boolean setJavaExecutableIfValid(String filename) {
        if (!GFLauncherUtils.ok(filename)) {
            return false;
        }

        File f = new File(filename);

        if (!f.isDirectory()) {
            return false;
        }

        if (GFLauncherUtils.isWindows()) {
            f = new File(f, "bin/java.exe");
        }
        else {
            f = new File(f, "bin/java");
        }

        if (f.exists()) {
            javaExe = SmartFile.sanitize(f).getPath();
            return true;
        }
        return false;
    }

    private List<String> getDebug() {
        if(info.isDebug() || javaConfig.isDebugEnabled()) {
            return javaConfig.getDebugOptions();
        }
        return Collections.emptyList();
    }

    private void setupProfilerAndJvmOptions(MiniXmlParser parser) throws MiniXmlParserException, GFLauncherException {
        // add JVM options from Profiler *last* so they override config's
        // JVM options
        
        profiler  = new Profiler(
                parser.getProfilerConfig(), 
                parser.getProfilerJvmOptions(), 
                parser.getProfilerSystemProperties());

        List<String> rawJvmOptions = parser.getJvmOptions();
        rawJvmOptions.addAll(getSpecialSystemProperties());
        if(profiler.isEnabled()) {
            rawJvmOptions.addAll(profiler.getJvmOptions());
        }
        jvmOptions = new JvmOptions(rawJvmOptions);
    }


    private void setupMonitoring(MiniXmlParser parser) throws GFLauncherException {
        // As usual we have to be very careful.

        // If it is NOT enabled -- we are out of here!!!
        if(parser.isMonitoringEnabled() == false)
            return;

        // if the user has a hard-coded "-javaagent" jvm-option that uses OUR jar
        //then we do NOT want to add our own.
        Set<String> plainKeys = jvmOptions.plainProps.keySet();
        for(String key : plainKeys) {
            if(key.startsWith("javaagent:")) {
                // complications -- of course!!  They may have mix&match forward and back slashes
                key = key.replace('\\', '/');
                if(key.indexOf(BTRACE_PATH) > 0)
                    return; // Done!!!!
            }
        }

        // It is not already specified AND monitoring is enabled.
        try {
            jvmOptions.plainProps.put(getMonitoringJvmOptionString(), null);
        }
        catch(GFLauncherException gfe) {
            // This has been defined as a non-fatal error.
            // Silently ignore it -- but do NOT add it as an option
        }
    }

    private String getMonitoringJvmOptionString() throws GFLauncherException {
        //-javaagent:${ASINSTALL_ROOT}/lib/monitor/btrace-agent.jar=unsafe=true
        File jarFile = new File(getInfo().getInstallDir(), BTRACE_PATH);
        String jarPath = SmartFile.sanitize(jarFile).getPath().replace('\\', '/');

        // make sure it exists
        if(!jarFile.isFile())
            throw new GFLauncherException("no_btrace_jar", jarPath);
        
        return "javaagent:" + jarPath + "=unsafe=true,noServer=true";
    }
    private List<String> getSpecialSystemProperties() throws GFLauncherException {
        Map<String, String> props = new HashMap<String, String>();
        props.put(INSTALL_ROOT_PROPERTY, getInfo().getInstallDir().getAbsolutePath());
        props.put(INSTANCE_ROOT_PROPERTY, getInfo().getInstanceRootDir().getAbsolutePath());
        return ( this.propsToJvmOptions(props) );
    }

    void logCommandLine() {
        StringBuilder sb = new StringBuilder();
        for(String s : commandLine) {
            // newline before the first line...
            sb.append(NEWLINE);
            sb.append(s);
        }
        if(!isFakeLaunch()) {
            GFLauncherLogger.info("commandline", sb.toString());
        }
    }

    String getClasspath() {
        return classpath;
    }

    void setClasspath(String s) {
        classpath = s;
    }

    private List<String> propsToJvmOptions(Map<String,String> map) {
        List<String> ss = new ArrayList<String>();
        Set<String> set = map.keySet();
        
        for(String name : set) {
            String value = map.get(name);
            String jvm = "-D" + name; 
            
            if(value != null) {
                jvm += "=" + value;
            }
            
            ss.add(jvm);
        }
        
        return ss;
    }
    private void setupLogLevels() {
        if(info.isVerbose())
            GFLauncherLogger.setConsoleLevel(Level.INFO);
        else
            GFLauncherLogger.setConsoleLevel(Level.WARNING);
    }

    private List<String> commandLine = new ArrayList<String>();
    private GFLauncherInfo info;
    private Map<String, String> asenvProps;
    private JavaConfig javaConfig;
    private JvmOptions jvmOptions;
    private Profiler profiler;
    private Map<String, String> sysPropsFromXml;
    private String javaExe;
    private String classpath;
    private List<String> debugOptions;
    private long startTime;
    private String logFilename;
    private LaunchType mode = LaunchType.normal;
    private final static LocalStringsImpl strings = new LocalStringsImpl(GFLauncher.class);
    private boolean setupCalledByClients = false; //handle with care
    private int     exitValue = -1;
    private ProcessWhacker  processWhacker;
    private Process process;
    private ProcessStreamDrainer    psd;
    private boolean logFilenameWasFixed = false;
    private boolean needsUpgrade = false;

    ///////////////////////////////////////////////////////////////////////////
    
    private static class ProcessWhacker implements Runnable {
        ProcessWhacker(Process p, String msg) {
            message = msg;
            process = p;
        }

        void setProcess(Process p) {
            process = p;
        }

        public void run() {
            // we are in a shutdown hook -- most of the JVM is gone.
            // logger won't work anymore...
            System.out.println(message);
            process.destroy();
        }

        private String message;
        private Process process;
    }
}
