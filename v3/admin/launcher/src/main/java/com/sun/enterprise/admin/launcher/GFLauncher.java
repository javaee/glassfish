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
import com.sun.enterprise.universal.xml.MiniXmlParserException;
import java.io.*;
import java.util.*;
import com.sun.enterprise.universal.glassfish.ASenvPropertyReader;
import com.sun.enterprise.universal.xml.MiniXmlParser;
import java.util.logging.Level;
import static com.sun.enterprise.universal.glassfish.SystemPropertyConstants.*;

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
    public final void launch() throws GFLauncherException {
        try {
            startTime = System.currentTimeMillis();
            setup();
            internalLaunch();
        }
        catch (GFLauncherException gfe) {
            throw gfe;
        }
        catch (Throwable t) {
            // hk2 might throw a java.lang.Error
            throw new GFLauncherException("unknownError", t);
        }
        finally {
            GFLauncherLogger.removeLogFileHandler();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    //////     ALL private and package-private below   ////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    abstract void internalLaunch() throws GFLauncherException;

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
    private void setup() throws GFLauncherException, MiniXmlParserException {
        
        ASenvPropertyReader pr;
        if(isFakeLaunch()) {
            pr = new ASenvPropertyReader(info.getInstallDir());
        }
        else {
            pr = new ASenvPropertyReader();
        }
        
        asenvProps = pr.getProps();
        info.setup();
        MiniXmlParser parser = new MiniXmlParser(getInfo().getConfigFile(), getInfo().getInstanceName());
        String domainName = parser.getDomainName();
        if(GFLauncherUtils.ok(domainName)) {
            info.setDomainName(parser.getDomainName());
        }
        javaConfig = new JavaConfig(parser.getJavaConfig());
        setupProfilerAndJvmOptions(parser);
        sysPropsFromXml = parser.getSystemProperties();
        asenvProps.put(INSTANCE_ROOT_PROPERTY, getInfo().getInstanceRootDir().getPath());
        debugOptions = getDebug();
        logFilename = parser.getLogFilename();
        resolveAllTokens();
        GFLauncherLogger.addLogFileHandler(logFilename);
        setJavaExecutable();
        setClasspath();
        setCommandLine();
        logCommandLine();
    }
    
    private void setCommandLine() throws GFLauncherException {
        // todo handle stuff in javaConfig like debug...
        commandLine = new ArrayList<String>();
        commandLine.add(javaExe);
        commandLine.add("-cp");
        commandLine.add(classpath);
        commandLine.addAll(debugOptions);
        commandLine.addAll(jvmOptions.toStringArray());
        commandLine.addAll(getNativePathCommandLine());
        commandLine.add(getMainClass());
        commandLine.addAll(getInfo().getArgsAsList());
    }

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
        all.putAll(envProps);
        all.putAll(sysProps);
        all.putAll(asenvProps);
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

    private void setClasspath() throws GFLauncherException {
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
        classpath = GFLauncherUtils.fileListToPathString(all);
    }

    private boolean setJavaExecutableIfValid(String filename) {
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
            javaExe = GFLauncherUtils.absolutize(f).getPath();
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

    private void setupProfilerAndJvmOptions(MiniXmlParser parser) throws MiniXmlParserException {
        // add JVM options from Profiler *last* so they override config's
        // JVM options
        
        profiler  = new Profiler(
                parser.getProfilerConfig(), 
                parser.getProfilerJvmOptions(), 
                parser.getProfilerSystemProperties());

        List<String> rawJvmOptions = parser.getJvmOptions();
        
        if(profiler.isEnabled()) {
            rawJvmOptions.addAll(profiler.getJvmOptions());
        }
        jvmOptions = new JvmOptions(rawJvmOptions);
    }

    private List<String> getNativePathCommandLine() {
        // do nothing unless we have something to add.
        // in that case, concatenate rather than replace.
        List<String> list = new ArrayList<String>();
        
        // if not enabled -- fagetaboutit
        if(!profiler.isEnabled())
            return list;
        
        List<File> profilerNativeFiles = profiler.getNativePath();
        
        // if no native path configured -- color me GONE!
        if(profilerNativeFiles.size() <= 0)
            return list;
        
        // OK -- we have at least one file in the path.  Append it/them...
        List<File> nativeFiles = GFLauncherUtils.stringToFiles(
                System.getProperty(JAVA_NATIVE_SYSPROP_NAME));

        // put the existing files first, then append the profiler paths
        nativeFiles.addAll(profilerNativeFiles);
        String nativeCommand = "-D" + JAVA_NATIVE_SYSPROP_NAME + "=";
        nativeCommand += GFLauncherUtils.fileListToPathString(nativeFiles);
        list.add(nativeCommand);
        return list;
    }

    private void logCommandLine() {
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


    private GFLauncherInfo info;
    private Map<String, String> asenvProps;
    private JavaConfig javaConfig;
    private JvmOptions jvmOptions;
    private Profiler profiler;
    private Map<String, String> sysPropsFromXml;
    private String javaExe;
    private String classpath;
    private List<String> debugOptions;
    private List<String> commandLine;
    private long startTime;
    private String logFilename;
    private LaunchType mode = LaunchType.normal;
    private final static String JAVA_NATIVE_SYSPROP_NAME = "java.library.path";
    private static final String NEWLINE = System.getProperty("line.separator");

}


