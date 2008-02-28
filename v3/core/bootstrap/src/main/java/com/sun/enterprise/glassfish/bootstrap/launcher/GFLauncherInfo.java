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
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.glassfish.bootstrap.launcher;

import com.sun.enterprise.module.bootstrap.ArgumentManager;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * @author bnevins
 */
public class GFLauncherInfo 
{
    // TEMP TEMP TEMP TEMP TEMP
    // TEMP TEMP TEMP TEMP TEMP
    // TEMP TEMP TEMP TEMP TEMP
    // TEMP TEMP TEMP TEMP TEMP
    // TEMP TEMP TEMP TEMP TEMP
    public static void main(String[] args)
    {
        try
        {
            LocalStringsImpl lsi = new LocalStringsImpl(GFLauncherInfo.class);
            System.out.println("FOO= " + lsi.get("foo"));
            System.out.println("FOO2= " + lsi.get("foo2", "xxxxx", "yyyy"));
            GFLauncherInfo gfli = new GFLauncherInfo();
            gfli.finalSetup();
        }
        catch (GFLauncherException ex)
        {
            Logger.getLogger(GFLauncherInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    // TEMP TEMP TEMP TEMP TEMP
    // TEMP TEMP TEMP TEMP TEMP
    // TEMP TEMP TEMP TEMP TEMP
    // TEMP TEMP TEMP TEMP TEMP
    // TEMP TEMP TEMP TEMP TEMP
    // TEMP TEMP TEMP TEMP TEMP

    
    
    
    
    public void addArgs(String... args)
    {
        for(String s : args)
            argsRaw.add(s);
    }

    void setup() throws GFLauncherException
    {
        setupFromArgs();
        finalSetup();
    }
    
    private void setupFromArgs()
    {
        argsMap = ArgumentManager.argsToMap(argsRaw);
        
        File f = null;
        String s = null;
        Boolean b = null;
        
        // pick out file props
        // annoying -- cli uses "domaindir" to represent the parent of the 
        // domain root dir.  I'm sticking with the same syntax for now...
        if((f = getFile("domaindir")) != null)
            domainParentDir = f;
        
        if((f = getFile("instancedir")) != null)
            instanceDir = f;
        
        if((f = getFile("domainrootdir")) != null)
            domainRootDir = f;
        
        // Now do the same thing with known Strings
        if((s = getString("domain")) != null)
            domainName = s;
        
        // the Arg processor may have set the name "default" to the domain name
        // just like in asadmin
        if(!GFLauncherUtils.ok(domainName) && (s = getString("default")) != null)
            domainName = s;
        
        if((s = getString("instancename")) != null)
            instanceName = s;

        // finally, do the booleans
        if((b = getBoolean("debug")) != null)
            debug = b;

        if((b = getBoolean("verbose")) != null)
            verbose = b;

        if((b = getBoolean("embedded")) != null)
            embedded = b;
    }
    
    private void finalSetup() throws GFLauncherException
    {
        installDir = GFLauncherUtils.getInstallDir();

        if(!GFLauncherUtils.safeIsDirectory(installDir))
            throw new GFLauncherException("noInstallDir", installDir);
        
        // check user-supplied args
        if(domainParentDir != null)
        {
            // if the arg was given -- then it MUST point to a real dir
            if(!GFLauncherUtils.safeIsDirectory(domainParentDir))
                throw new GFLauncherException("noDomainParentDir", domainParentDir);
        }

        setupDomainRootDir();
        
        if(!GFLauncherUtils.safeIsDirectory(domainRootDir))
            throw new GFLauncherException("noDomainRootDir", domainRootDir);
        
        configDir = new File(domainRootDir, CONFIG_DIR);
        
        // if we made it here -- we're in pretty good shape!
    }

    private void setupDomainRootDir() throws GFLauncherException
    {
        // if they set domainrootdir -- it takes precedence
        if(domainRootDir != null)
        {
            domainParentDir = domainRootDir.getParentFile();
            domainName = domainRootDir.getName();
            return;
        }
        
        // if they set domainParentDir -- use it.  o/w use the default dir
        if(domainParentDir == null)
        {
            domainParentDir = new File(installDir, DEFAULT_DOMAIN_PARENT_DIR);
        }
        
        // if they specified domain name -- use it.  o/w use the one and only dir
        // in the domain parent dir
        
        if(domainName == null)
        {
            domainName = getTheOneAndOnlyDomain();
        }
        
        domainRootDir = new File(domainParentDir, domainName);
    }

    private String getTheOneAndOnlyDomain() throws GFLauncherException
    {
        // look for subdirs in the parent dir -- there must be one and only one
        
        File[] files = domainParentDir.listFiles(new FileFilter()
        {
            public boolean accept(File f) 
            { 
                return GFLauncherUtils.safeIsDirectory(f); 
            }
        });
        
        if(files == null || files.length == 0)
            throw new GFLauncherException("noDomainDirs", domainParentDir);
        
        if(files.length > 1)
            throw new GFLauncherException("tooManyDomainDirs", domainParentDir);
        
        return files[0].getName();
    }
    
    private Boolean getBoolean(String key) 
    {
        // 3 return values -- true, false, null
        if(argsMap.containsKey(key))
        {
            String s = argsMap.get(key);
            //argsMap.remove(key);
            return Boolean.valueOf(s);
        }
        return null;
    }

    private File getFile(String key)
    {
        if(argsMap.containsKey(key))
        {
            File f = new File(argsMap.get(key));
            //argsMap.remove(key);
            return f;
        }
        return null;
    }
    
    private String getString(String key) 
    {
        if(argsMap.containsKey(key))
        {
            String s = argsMap.get(key);
            //argsMap.remove(key);
            return s;
        }
        return null;
    }

    // yes these variables are all accessible from any class in the package...
    boolean                     verbose         = false;
    boolean                     debug           = false;
    boolean                     embedded        = false;
    File                        installDir;
    File                        domainParentDir;
    File                        domainRootDir;
    File                        instanceDir;
    File                        configDir;
    String                      domainName;
    String                      instanceName;

    private boolean             valid           = false;
    private Map<String,String>  argsMap;
    private ArrayList<String>   argsRaw = new ArrayList<String>();
    private final static String DEFAULT_DOMAIN_PARENT_DIR = "domains";
    private final static String CONFIG_DIR                = "config";
}

