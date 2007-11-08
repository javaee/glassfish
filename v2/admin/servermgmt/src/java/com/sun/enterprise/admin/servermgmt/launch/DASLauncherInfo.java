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
 * DASLauncherInfo.java
 *
 * Created on October 9, 2006, 12:24 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.enterprise.admin.servermgmt.launch;

import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.java.util.jar.pack.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author bnevins
 */
public class DASLauncherInfo
{
    public DASLauncherInfo()
    {
    }

    ///////////////////////////////////////////////////////////////////////////
    
    public DASLauncherInfo(String... Args)
    {
        args = Args;
        processArgs();
    }

    ///////////////////////////////////////////////////////////////////////////
    
    public void validate() throws ASLauncherException
    {
        String message = "";
        
        installDir  = safeGetCanonicalFile(installDir);
        domainsDir = safeGetCanonicalFile(domainsDir);       
        domainDir = safeGetCanonicalFile(domainDir);       
        configDir = safeGetCanonicalFile(configDir);       

        if(!safeIsDir(installDir))
            message += "Bad install dir: " + installDir  + "\n";
        
        if(!safeIsDir(domainsDir))
            message += "Bad domains dir: " + domainsDir + "\n";
        
        if(!safeIsDir(configDir) && safeIsDir(installDir))
        {
            // guess
            configDir = safeGetCanonicalFile(new File(installDir, "config"));
        }

        if(!safeIsDir(configDir))
            message += "Bad config dir: " + configDir + "\n";
        
        if(!safeIsDir(domainDir) && safeIsDir(domainsDir) && ok(domainName))
        {
            // calculate it...
            domainDir = safeGetCanonicalFile(new File(domainsDir, domainName));
        }

        if(!safeIsDir(domainDir))
            message += "Bad domain dir: " + domainDir + "\n";
        
        if(!ok(domainName))
            message += "Missing domain name\n";

        if(!ok(instanceName))
            message += "No instance name\n";
        
        if(!safeIsDir(domainDir))
            message += "Bad domain dir: " + domainDir + "\n";
        
        if(!ok(xmlProcessName))
            message += "Bad process name: " + xmlProcessName + "\n";
        
        if(message.length() > 0)
            throw new ASLauncherException(message);
        
        valid = true;
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("verbose=" + verbose + '\n');
        sb.append("debug=" + debug + '\n');
        sb.append("domainsDir=" + domainsDir + '\n');
        sb.append("domainName=" + domainName + '\n');
        sb.append("installDir=" + installDir + '\n');
        sb.append("domainDir=" + domainDir + '\n');
        sb.append("configDir=" + configDir + '\n');
        sb.append("instanceName=" + instanceName + '\n');
        sb.append("xmlProcessName=" + xmlProcessName + '\n');
        return sb.toString();
    }

    ///////////////////////////////////////////////////////////////////////////

    void setSystemProps() throws ASLauncherException
    {
        if(!valid)
            throw new ASLauncherException("Internal state is invalid");
        
        System.setProperty(SystemPropertyConstants.CONFIG_ROOT_PROPERTY,     configDir.getPath());
        System.setProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY,    installDir.getPath());
        System.setProperty(SystemPropertyConstants.SERVER_NAME,              instanceName);
        System.setProperty(SystemPropertyConstants.INSTANCE_ROOT_PROPERTY,   domainDir.getPath());
        System.setProperty(LaunchConstants.PROCESS_NAME_PROP,                xmlProcessName);
        System.setProperty("domain.name",                                    domainName);
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private void processArgs()
    {
        parseScriptPath();
        parseDomainDir();
        parseInstallDir();
        parseOtherStuff();
    }

    ///////////////////////////////////////////////////////////////////////////
    
    private void parseScriptPath()
    {
        // example path: c:/ee/domains/domain1/bin/startdas.bat
        // we need to pick out:
        // (1) c:/ee/domains
        // (2) domain1

        for(String s : args)
        {
            if(!s.startsWith(SCRIPT_PATH))
                continue;
            
            String path = s.substring(SCRIPT_PATH.length());
            
            if(!ok(path))
                break;
            
            File script = new File(path);
            
            if(!ok(script))
                break;
            
            File bindir = script.getParentFile();
            
            if(!safeIsDir(bindir))
                break;
            
            domainDir = safeGetCanonicalFile(bindir.getParentFile());
            extractInfoFromDomainDir();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    
    private void parseDomainDir()
    {
        // example path: c:/ee/domains/domain1
        // we need to pick out:
        // (1) c:/ee/domains
        // (2) domain1

        for(String s : args)
        {
            if(!s.startsWith(DOMAIN_DIR))
                continue;
            
            String path = s.substring(DOMAIN_DIR.length());
            domainDir = safeGetCanonicalFile(new File(path));
            extractInfoFromDomainDir();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    
    private void extractInfoFromDomainDir()
    {
        if(!safeIsDir(domainDir))
            return;
        
        domainsDir = safeGetCanonicalFile(domainDir.getParentFile());

        if(!safeIsDir(domainsDir))
            return;

        domainName = domainDir.getName();
        domainsDirParent = safeGetCanonicalFile(domainsDir.getParentFile());
    }

    ///////////////////////////////////////////////////////////////////////////
    
    private void parseInstallDir()
    {
        // first look for an explicit argument...
        
        for(String s : args)
        {
            if(!s.startsWith(INSTALL_DIR))
                continue;

            String path = s.substring(INSTALL_DIR.length());
            
            if(!ok(path))
                break;
            
            installDir = safeGetCanonicalFile(new File(path));
            
            if(!safeIsDir(installDir))
                break;
        }
        
        if(installDir != null)
            return;
        
        // In desperation, figure it out from the script path
        if(domainsDirParent != null)
            installDir = domainsDirParent;
    }

    ///////////////////////////////////////////////////////////////////////////
    
    private void parseOtherStuff()
    {
        for(String s : args)
        {
            String arg = s.toLowerCase();
            
            if(arg.equals("verbose"))
                verbose = true;
            
            if(arg.equals("debug"))
                debug = true;
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private static File safeGetCanonicalFile(File f)
    {
        if(f == null || !f.exists())
            return null;
        
        try
        {
            return f.getCanonicalFile();
        }
        catch(IOException ioe)
        {
            return f.getAbsoluteFile();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    
    private static boolean safeIsDir(File f)
    {
        return f != null && f.exists() && f.isDirectory();
    }

    ///////////////////////////////////////////////////////////////////////////
    
    private static boolean ok(String s)
    {
        return s != null && s.length() > 0;
    }

    ///////////////////////////////////////////////////////////////////////////
    
    private static boolean ok(File f)
    {
        return f != null && f.exists();
    }

    ///////////////////////////////////////////////////////////////////////////
    
    public boolean  verbose     = false;
    public boolean  debug       = false;
    public File     domainsDir;
    public File     domainDir;
    public String   domainName;
    public File     installDir;
    public File     configDir;
    public String   instanceName    = "server";
    public String   xmlProcessName  = "as9-server";

    ///////////////////////////////////////////////////////////////////////////
    
    private                 String[]    args;
    private                 File        domainsDirParent;
    private                 boolean     valid = false;
    public final static    String      SCRIPT_PATH = "SCRIPT_PATH=";
    public final static    String      INSTALL_DIR = "INSTALL_DIR=";
    public final static    String      DOMAIN_DIR  = "DOMAIN_DIR=";
    ///////////////////////////////////////////////////////////////////////////
    
    public static void main(String[] Args)
    {
        new DASLauncherInfo(Args);
    }

}
