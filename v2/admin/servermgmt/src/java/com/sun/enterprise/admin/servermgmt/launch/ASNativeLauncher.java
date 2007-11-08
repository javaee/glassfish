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
 * ASNativeLauncher.java
 *
 * Created on November 2, 2006, 11:07 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.enterprise.admin.servermgmt.launch;

import com.sun.enterprise.admin.servermgmt.pe.PEInstancesManager;
import com.sun.enterprise.util.OS;
import java.io.*;
import com.sun.enterprise.util.io.*;
import com.sun.enterprise.util.io.ProcessStreamDrainer;
import com.sun.enterprise.util.SystemPropertyConstants;
import java.util.*;
import java.util.ArrayList;

/**
 *
 * @author bnevins
 */
public class ASNativeLauncher extends ASLauncher
{
    public ASNativeLauncher(PEInstancesManager mgr)
    {
        nativeName = mgr.getNativeName();
        classname = getClass().getSuperclass().getName();
    }
    
    public Process launch(String[] Args, String[] SecurityInfo) throws ASLauncherException
    {
        securityInfo = SecurityInfo;
        setArguments(Args);
        setup();
        return launch();
    }
    
    ///////////////////////////////////////////////////////////////////////////

    private Process launch() throws ASLauncherException
    {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(currDir);
        try
        {
            Process process = pb.start();
            ProcessStreamDrainer drainer = ProcessStreamDrainer.redirect("NativeLauncher", process);
            writeSecurityInfoToProcess(process);
          
            
            if (userSetVerbose) 
            {
                // native verbose -- hang here!!
                process.waitFor();
                //drainer.waitFor();
            }

            return process;
        }
        catch(Exception e)
        {
            throw new ASLauncherException(e);
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private void setup() throws ASLauncherException
    {
        // don't change the order of these unless you REALLY know what you are
        // doing!!
        setJava();
        setInstallRoot();
        setInstanceRoot();
        setInstanceName();
        setProcessName();
        setReturn();
        setDomainName();
        setClasspath();
        setVerbose();
        setDefines();
        setNatives();
        setCommand();
        setCurrDir();
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private void setArguments(String[] Args) throws ASLauncherException
    {
        // TODO Bug: "debug" argument causes a JVM.dll not found error?!?
        args = new ArrayList<String>();
        args.add("start");
        args.add("display");
        //args.add("verbose");
        
        for(String arg : Args)
        {
            if("native".equalsIgnoreCase(arg))
                continue;

            // TODO
            if("debug".equalsIgnoreCase(arg))
                continue;

            if("verbose".equalsIgnoreCase(arg))
            {
                userSetVerbose = true;
                args.add(arg);
                continue;
            }
            
            if(args.contains(arg))  // no duplicates!
                continue;

            args.add(arg);
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private void setJava() throws ASLauncherException
    {
        String javaRoot = System.getProperty(SystemPropertyConstants.JAVA_ROOT_PROPERTY);
        String exeName = OS.isWindows() ? "java.exe" : "java";
        
        java = verifyFile(javaRoot + "/bin/" + exeName);
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private void setInstallRoot() throws ASLauncherException
    {
       installRoot = System.getProperty(installRootTag);
       installRoot = verifyFile(installRoot);
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private void setInstanceRoot() throws ASLauncherException
    {
       instanceRoot = System.getProperty(instanceRootTag);
       instanceRoot = verifyFile(instanceRoot);
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private void setInstanceName()
    {
       instanceName = System.getProperty(instanceNameTag);
       
       if(!ok(instanceName))
           instanceName = "server";
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private void setProcessName()
    {
       processName = System.getProperty(processNameTag);
       
       if(!ok(processName))
           processName = "as9-server";
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private void setReturn()
    {
        // do NOT send in a "-D" for this if it isn't already set
       returnValue = System.getProperty(returnTag);
       
       if(!ok(returnValue))
           returnValue = null;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private void setDomainName() throws ASLauncherException
    {
        // this is used for "appservDAS.exe domainName"
        domainName = System.getProperty(domainNameTag);
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private void setClasspath() throws ASLauncherException
    {
        classpath = new String();
        
        for(int i = 0; i < jars.length; i++)
        {
            if(i != 0)
                classpath += File.pathSeparator;
            
            classpath += verifyFile(installRoot + "/lib/" + jars[i]);
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private void setDefines()
    {
        defines = new ArrayList<String>();
        
        defines.add("-D" + installRootTag + "=" + installRoot);
        defines.add("-D" + instanceRootTag + "=" + instanceRoot);
        defines.add("-D" + instanceNameTag + "=" + instanceName);
        defines.add("-D" + processNameTag + "=" + processName);
        defines.add("-Dcaller=cli");
        
        if(ok(returnValue))
            defines.add("-D" + returnTag + "=" + returnValue);
        
        //defines.add("-Dcom.sun.aas.verboseMode=" + Boolean.toString(verbose));
        if(debugger)
        {
            defines.add("-Xdebug");
            defines.add("-Xnoagent");
            defines.add("-Xrunjdwp:transport=dt_socket,address=5555,suspend=y,server=y");
            defines.add("-Djava.compiler=NONE");
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private void setNatives() throws ASLauncherException
    {
        String exe = "";
        
        if(OS.isWindows())
            exe = ".exe";
        
        nativeLauncher = verifyFile(installRoot + "/lib/" + "appservLauncher" + exe);
        native1 = verifyFile(installRoot + "/lib/" + nativeName + exe);
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private void setCommand()
    {
        //"%AS_INSTALL%/lib/appservLauncher.exe" "%AS_INSTALL%/lib/appservDAS.exe %DOMAIN_NAME%"  "\"%~df0\" %CLI% display" 
        command = new ArrayList<String>();
        
        command.add(nativeLauncher);
        command.add(native1);
        command.add(java);
        command.add("-cp");
        command.add(classpath);
        
        for(String define : defines)
            command.add(define);
        
        command.add(classname);
        //command.add("start");
        //command.add("display");
        
        for(String arg : args)
        {
            command.add(arg);
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////

    private void setCurrDir() throws ASLauncherException
    {
        currDir = new File(verifyFile(instanceRoot + "/config"));
    }
    
    ///////////////////////////////////////////////////////////////////////////

    private void setVerbose() throws ASLauncherException
    {
        // this looks bizarre and kludgy so I'll try to explain.
        // the native code which one does NOT want to touch unless absolutely
        // neccessary (it's a mess) -- will hang forever if "verbose" is passed in as an arg
        // We can't just get rid of it though because PEMain will look for a verbose
        // System Property.  If it does not see this System Property -- then you
        // will never see thread dumps -- which is the whole point of starting with native.
        
        verbose = false;
        
        for(String arg : args)
        {
            if("verbose".equalsIgnoreCase(arg))
            {
                verbose = true;
                return;
            }
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private boolean ok(String s)
    {
        return s != null && s.length() > 0;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private boolean ok(File f)
    {
        return f != null && f.exists();
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private String verifyFile(String s) throws ASLauncherException
    {
        if(!ok(s))
            // TODO
            throw new ASLauncherException("Null filename");
        
        File f = new File(s);
        
        if(!ok(f))
            // TODO
            throw new ASLauncherException("Bad File: " + s);
        
        return FileUtils.safeGetCanonicalPath(f);
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private String                  java;
    private String                  classpath;
    private String                  installRoot;
    private String                  instanceName;
    private String                  instanceRoot;
    private String                  processName;
    private String                  returnValue;
    private String                  nativeLauncher;
    private String                  native1;
    private String                  domainName;
    private String                  classname;
    private File                    currDir;
    private boolean                 verbose;
    private boolean                 userSetVerbose = false;
    private ArrayList<String>       defines;
    private ArrayList<String>       command;
    private ArrayList<String>       args;
    private String                  nativeName;
    private static final String     installRootTag  =  SystemPropertyConstants.INSTALL_ROOT_PROPERTY;
    private static final String     javaRootTag     =  SystemPropertyConstants.JAVA_ROOT_PROPERTY;
    private static final String     instanceRootTag =  SystemPropertyConstants.INSTANCE_ROOT_PROPERTY;
    private static final String     instanceNameTag =  SystemPropertyConstants.SERVER_NAME;
    private static final String     domainNameTag   =  "domain.name";
    private static final String     verboseTag      =  "com.sun.aas.verboseMode";
    private static final String     processNameTag  = LaunchConstants.PROCESS_NAME_PROP;
    private static final String     returnTag       = LaunchConstants.LAUNCHER_RETURN_PROP;
    private static final String[]   jars = { "appserv-admin.jar", "appserv-rt.jar" };    
    private static final boolean    debugger = false;
}
