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

import com.sun.enterprise.module.bootstrap.BootException;
import com.sun.enterprise.universal.process.ProcessStreamDrainer;
import com.sun.enterprise.universal.xml.MiniXmlParserException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import com.sun.enterprise.glassfish.bootstrap.Main;
import com.sun.enterprise.universal.glassfish.ASenvPropertyReader;
import com.sun.enterprise.universal.glassfish.GFLauncherUtils;
import com.sun.enterprise.universal.glassfish.SystemPropertyConstants;
import com.sun.enterprise.universal.glassfish.TokenResolver;
import static com.sun.enterprise.universal.glassfish.SystemPropertyConstants.*;
import com.sun.enterprise.universal.xml.MiniXmlParser;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * GFDomainLauncher
 * This class is a package-private subclass of GFLauncher designed for
 * domain launching
 * @author bnevins
 */
class GFDomainLauncher extends GFLauncher {
    void internalLaunch() throws GFLauncherException {
        try {
            if (info.isEmbedded()) {
                launchEmbedded();
            }
            else {
                launchExternal();
            }
        }
        catch (GFLauncherException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new GFLauncherException(ex);
        }
    }

    private void launchEmbedded() throws GFLauncherException, BootException {
        Main main = new Main();
        main.start(info.getArgsAsStringArray());
        GFLauncherLogger.info("FinishedEmbedded", info.getDomainName());
    }

    private void launchExternal() throws GFLauncherException, MiniXmlParserException{
        // 1. get props from asenv
        ASenvPropertyReader pr = new ASenvPropertyReader();
        envProps = pr.getProps();


        MiniXmlParser parser = new MiniXmlParser(info.getConfigFile(), info.getInstanceName());
        javaConfig = parser.getJavaConfig();
        jvmOptions = new JvmOptions(parser.getJvmOptions());
        systemPropertiesDomainXml = parser.getSystemProperties();
        envProps.put(INSTANCE_ROOT_PROPERTY, info.getDomainRootDir().getPath());

        // 3. resolve tokens
        fixDomainXmlInfo();

        //4. check on the java executable.  If it isn't null -- then it should be OK
        setJavaExecutable();
        
        //5. combine all the args for the new JVM process
        List<String> cmds = packageCommandArgs();

            // 6. create the process
        ProcessBuilder pb = new ProcessBuilder(cmds);

        // 6.1 Temporary
        System.out.println("** Important Messages: \nThe TP2 Launcher does not yet support debugging");
        System.out.println("** The commandline below is here for your info.  When logging is setup " +
                "it will go to the log file.");
        System.out.println("*******   jvm command line  *********");
        for (String s : cmds) {
            System.out.println(s);
        }
        // 7. run the process and attach Stream Drainers
        Process p;
        try {
            p = pb.start();
            if (getInfo().isVerbose())
                ProcessStreamDrainer.redirect(getInfo().getDomainName(), p);
            else
                ProcessStreamDrainer.drain(getInfo().getDomainName(), p);
        }
        catch (IOException e) {
            throw new GFLauncherException("jvmfailure", e, e);
        }
        // 8. if verbose, hang round until the domain stops
        try {
            if (getInfo().isVerbose())
                p.waitFor();
        }
        catch (InterruptedException ex) {
            throw new GFLauncherException("verboseInterruption", ex, ex);
        }
     }
    
    private void fixDomainXmlInfo()
    {
        // resolve domain.xml stuff with:
        // 1. itself
        // 2. <system-property>'s from domain.xml
        // 3. envProps
        // 4. system properties -- essential there is, e.g. "${path.separator}" in domain.xml
        // i.e. add in reverse order to get the precedence right
        
        Map<String,String> all = new HashMap<String,String>();
        // Properties are annoying!!!
        Properties sp = System.getProperties();
        Set<Object> spNames = sp.keySet();
        for(Object o : spNames) {
            all.put((String)o, (String)sp.get(o));
        }
        all.putAll(envProps);
        all.putAll(systemPropertiesDomainXml);
        all.putAll(jvmOptions.getCombinedMap());
        TokenResolver resolver = new TokenResolver(all);
        resolver.resolve(jvmOptions.xProps);
        resolver.resolve(jvmOptions.xxProps);
        resolver.resolve(jvmOptions.plainProps);
        resolver.resolve(jvmOptions.sysProps);
    }

    private List<String> packageCommandArgs() throws GFLauncherException {
        // todo handle stuff in javaConfig like debug...
        List<String> cmds = new ArrayList<String>();
        cmds.add(javaExe);
        cmds.addAll(jvmOptions.toStringArray());
        cmds.add("-jar");
        cmds.add(getBootstrapJar());
        String[] args = getInfo().getArgsAsStringArray();
        
        for(String s : args) {
            cmds.add(s);
        }
        
        return cmds;
    }

    private void setJavaExecutable() throws GFLauncherException
    {
        String s = envProps.get(SystemPropertyConstants.JAVA_ROOT_PROPERTY);
        
        if(s == null)
            throw new GFLauncherException("nojvm");

        File f = new File(s);
        
        if(GFLauncherUtils.isWindows())
            f = new File(f, "bin/java.exe");
        else
            f = new File(f, "bin/java");
        
        javaExe = f.getPath();
    }

    private String getBootstrapJar() throws GFLauncherException {
        File f = new File(envProps.get(SystemPropertyConstants.INSTALL_ROOT_PROPERTY));
        f = new File(f, "modules/" + BOOTSTRAP_JAR_NAME);
        
        if(!f.exists())
            throw new GFLauncherException("nobootjar", f.getPath());
        
        return f.getPath();
    }
            
    private Map<String, String> envProps;
    private Map<String, String> javaConfig;
    private JvmOptions jvmOptions;
    private Map<String, String> systemPropertiesDomainXml;
    private String  javaExe;
    
    private static final String BOOTSTRAP_JAR_NAME = "glassfish-10.0-SNAPSHOT.jar";
}

