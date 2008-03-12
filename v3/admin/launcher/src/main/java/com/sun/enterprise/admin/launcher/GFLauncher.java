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
    }

    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    //////     ALL private and package-private below   ////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    abstract void internalLaunch() throws GFLauncherException;

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
        ASenvPropertyReader pr = new ASenvPropertyReader();
        asenvProps = pr.getProps();
        info.setup();
        MiniXmlParser parser = new MiniXmlParser(getInfo().getConfigFile(), getInfo().getInstanceName());
        javaConfig = new JavaConfig(parser.getJavaConfig());
        jvmOptions = new JvmOptions(parser.getJvmOptions());
        sysPropsFromXml = parser.getSystemProperties();
        asenvProps.put(INSTANCE_ROOT_PROPERTY, getInfo().getInstanceRootDir().getPath());
        debugOptions = getDebug();
        resolveAllTokens();
        setJavaExecutable();
        setClasspath();
        setCommandLine();
    }
    
    private void setCommandLine() throws GFLauncherException {
        // todo handle stuff in javaConfig like debug...
        commandLine = new ArrayList<String>();
        commandLine.add(javaExe);
        commandLine.add("-cp");
        commandLine.add(classpath);
        commandLine.addAll(debugOptions);
        commandLine.addAll(jvmOptions.toStringArray());
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
        TokenResolver resolver = new TokenResolver(all);
        resolver.resolve(jvmOptions.xProps);
        resolver.resolve(jvmOptions.xxProps);
        resolver.resolve(jvmOptions.plainProps);
        resolver.resolve(jvmOptions.sysProps);
        resolver.resolve(javaConfig.getMap());
        resolver.resolve(debugOptions);
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

        // create a list of all the classpath pieces in the right order
        List<File> all = new ArrayList<File>();
        all.addAll(prefixCP);
        all.addAll(mainCP);
        all.addAll(sysCP);
        all.addAll(envCP);
        all.addAll(suffixCP);

        // note: ALL files have been absolutized already.
        // let's use forward slashes for neatness...
        
        StringBuilder sb = new StringBuilder();
        boolean firstFile = true;
        
        for (File f : all) {
            if(firstFile) {
                firstFile = false;
            }
            else {
                sb.append(File.pathSeparatorChar);
            }
            sb.append(f.getPath().replace('\\', '/'));
        }
        classpath = sb.toString();
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
    
    private GFLauncherInfo info;
    private Map<String, String> asenvProps;
    private JavaConfig javaConfig;
    private JvmOptions jvmOptions;
    private Map<String, String> sysPropsFromXml;
    private String javaExe;
    private String classpath;
    private List<String> debugOptions;
    private List<String> commandLine;
    private long startTime;
}

