
package com.sun.enterprise.glassfish.bootstrap.launcher;

import java.io.*;
import java.util.*;
/**
 * @author bnevins
 */
class GFLauncherInfo 
{
    boolean                     verbose         = false;
    boolean                     debug           = false;
    boolean                     valid           = false;
    File                        installDir;
    File                        domainsDir;
    File                        domainDir;
    File                        instanceDir;
    File                        configDir;
    String                      domainName;
    String                      instanceName;
    private Map<String,String>  props           = new HashMap<String,String>();
    ArrayList<String>           cmdArgs = new ArrayList<String>();
    
    void addArgs(String... args)
    {
        for(String s : args)
            cmdArgs.add(s);
    }

    void addProps(Properties p)
    {
        Map<String,String> map = CollectionsUtils.propsToMap(p);
        props.putAll(map);
    }

    /*
     * cmdArgs take precedence over Props.
     * Simply add the cmdArgs to props and they will automatically override
     * the same keys
     */
    void setup()
    {
        Map<String,String> map = CollectionsUtils.stringsToMap(cmdArgs);
        
        File f = null;
        String s = null;
        Boolean b = null;
        
        // pick out 0-5 file props -- remove them from props after creating a 
        // File object.
        
        if((f = getFile("installDir")) != null)
            installDir = f;
        
        if((f = getFile("domainsDir")) != null)
            domainsDir = f;
        
        if((f = getFile("domainDir")) != null)
            domainDir = f;
        
        if((f = getFile("instanceDir")) != null)
            instanceDir = f;
        
        if((f = getFile("configDir")) != null)
            configDir = f;

        // Now do the same thing with known Strings
        if((s = getString("domainName")) != null)
            domainName = s;
        if((s = getString("instanceName")) != null)
            instanceName = s;

        // finally, do the booleans
        
        if((b = getBoolean("debug")) != null)
            debug = b;

        if((b = getBoolean("verbose")) != null)
            verbose = b;
    }

    private Boolean getBoolean(String key) 
    {
        // 3 return values -- true, false, null
        if(props.containsKey(key))
        {
            String s = props.get(key);
            props.remove(key);
            return Boolean.valueOf(s);
        }
        return null;
    }

    private File getFile(String key)
    {
        if(props.containsKey(key))
        {
            File f = new File(props.get(key));
            props.remove(key);
            return f;
        }
        return null;
    }

    private String getString(String key) 
    {
        if(props.containsKey(key))
        {
            String s = props.get(key);
            props.remove(key);
            return s;
        }
        return null;
    }
}

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
