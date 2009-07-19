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

import com.sun.enterprise.admin.launcher.GFLauncherFactory.ServerType;
import com.sun.enterprise.universal.glassfish.GFLauncherUtils;
import com.sun.enterprise.universal.io.SmartFile;
import java.io.*;
import java.util.*;

/**
 * @author bnevins
 */
public class GFLauncherInfo {

    /**
     * Add the string arguments in the order given.
     * @param args The string arguments
     */
    public void addArgs(String... args) {
        for (String s : args) {
            argsRaw.add(s);
        }
    }

    /**
     * Set the (optional) domain name.  This can also be sent in as a String arg
     * like so: "-domainname" "theName"
     * @param domainName
     */
    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    /**
     * Set the (optional) domain parent directory.  
     * This can also be sent in as a String arg
     * like so: "-domaindir" "parentDirPath"
     * @param domainParentName The parent directory of the domain
     */
    public void setDomainParentDir(String domainParentName) {
        this.domainParentDir = new File(domainParentName);
    }

    /**
     * Starts the server in verbose mode
     * @param b 
     */
    public void setVerbose(boolean b) {
        verbose = b;
    }

    /**
     * Starts the server in debug mode
     * @param b 
     */
    public void setDebug(boolean b) {
        debug = b;
    }

     /**
     * Starts the server in upgrade mode
     * @param b
     */
    public void setUpgrade(boolean b) {
        upgrade = b;
    }

    public void setDomainRootDir(File f) {
        domainRootDir = f;
    }

    public boolean isDomain() {
        return type == ServerType.domain;
    }

    /**
     * 
     * @return true if verbose mode is on.
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * 
     * @return true if debug mode is on.
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     *
     * @return true if upgrade mode is on.
     */
    public boolean isUpgrade() {
        return upgrade;
    }

    /**
     * 
     * @return The domain name
     */
    public String getDomainName() {
        return domainName;
    }

    public File getConfigFile() {
        return configFile;
    }

    public File getDomainRootDir() {
        return domainRootDir;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public Set<Integer> getAdminPorts() {
        return adminPorts;
    }
    public GFLauncherFactory.ServerType getType() {
        return type;
    }

    public File getConfigDir() {
        return SmartFile.sanitize(configDir);
    }

    void setConfigDir(File f) {
        configDir = SmartFile.sanitize(f);
    }
    
    public File getInstanceRootDir() throws GFLauncherException {
        if (!valid) {
            throw new GFLauncherException("internalError", "Call to getInstanceRootDir() on an invalid GFLauncherInfo object.");
        }
        if(instanceRootDir != null) {
            return instanceRootDir;
        }
        else if(isDomain()) {
            return domainRootDir;
        }
        else {
            throw new GFLauncherException("internalError", "Call to getInstanceRootDir() on an invalid GFLauncherInfo object.");
        }
    }

    File getDomainParentDir() {
        return domainParentDir;
    }


    /**
     *  TEMPORARY.  The guts of HK2 and V3 bootstrapping wants String[]
     * -- this will be changed soon, but it is messy to change it right now.
     * so temporarily we will humor HK2 by sending in String[]
     * @return an array of String arguments
     * @throws com.sun.enterprise.admin.launcher.GFLauncherException 
     */
    public String[] getArgsAsStringArray() throws GFLauncherException {
        List<String> list = getArgsAsList();
        String[] ss = new String[list.size()];
        return list.toArray(ss);
    }

    public List<String> getArgsAsList() throws GFLauncherException {
        Map<String, String> map = getArgs();
        Set<String> keys = map.keySet();
        List<String> argList = new ArrayList<String>();
        
        int i = 0;

        for (String key : keys) {
            argList.add(key);
            argList.add(map.get(key));
        }
        return argList;
    }

    /**
     * 
     * @return a Map<String,String> of processed and packaged args
     * @throws com.sun.enterprise.admin.launcher.GFLauncherException 
     */
    public Map<String, String> getArgs() throws GFLauncherException {
        // args processed and packaged for AppServer

        if (!valid) {
            throw new GFLauncherException("internalError", "Call to getArgs() on an invalid GFLauncherInfo object.");
        }

        Map<String, String> map = new HashMap<String, String>();
        map.put("-domaindir", SmartFile.sanitize(domainRootDir.getPath()));
        map.put("-verbose", Boolean.toString(verbose));
        map.put("-debug", Boolean.toString(debug));
        map.put("-domainname", domainName);
        map.put("-instancename", instanceName);
        map.put("-upgrade", Boolean.toString(upgrade));
        map.put("-read-stdin", "true"); //always make the server read the stdin for master password, at least.

        if(respawnInfo != null) {
            respawnInfo.put(map);
        }
        return map;
    }

    public void setRespawnInfo(String classname, String classpath, String[] args) {
        respawnInfo = new RespawnInfo(classname, classpath, args);
    }

    /** Adds the given name value pair as a security token. This is what will be put on the
     *  launched process's stdin to securely pass it on. The value is accepted as a String and it may be insecure.
     *  A string formed by concatenating name, '=' and value is written to the stdin as a single
     *  line delimited by newline character. To get
     *  the value of the token, the server should parse the line knowing this. None of the parameters may be null.
     *
     * @param name  String representing name of the token
     * @param value String representing the value (should we call it a password?)
     * @throws NullPointerException if any of the parameters are null
     */
    public void addSecurityToken(String name, String value) {
        if (name == null || value == null)
            throw new NullPointerException();
        securityTokens.add(name + "=" + value);
    }
    
    GFLauncherInfo(GFLauncherFactory.ServerType type) {
        this.type = type;
    }


    void setAdminPorts(Set<Integer> adminPorts) {
        this.adminPorts = adminPorts;
    }
    void setup() throws GFLauncherException {
        setupFromArgs();
        finalSetup();
    }

    /**
     * IMPORTANT:  These 2 methods are designed for use only by Unit Tests so we are
     * not dependent on an installation.  Normally we figure out installDir from
     * wher we are running from. 
     */
    void setInstallDir(File f) {
        installDir = f;
    }

    File getInstallDir() {
        return installDir;
    }
            
    private void setupFromArgs() {
        argsMap = ArgumentManager.argsToMap(argsRaw);

        File f = null;
        String s = null;
        Boolean b = null;

        // pick out file props
        // annoying -- cli uses "domaindir" to represent the parent of the 
        // domain root dir.  I'm sticking with the same syntax for now...
        if ((f = getFile("domaindir")) != null) {
            domainParentDir = f;
        }

        if ((f = getFile("instanceRootDir")) != null) {
            instanceRootDir = f;
        }

        if ((f = getFile("domainroot")) != null) {
            domainRootDir = f;
        }

        // Now do the same thing with known Strings
        if ((s = getString("domain")) != null) {
            domainName = s;
        }

        // the Arg processor may have set the name "default" to the domain name
        // just like in asadmin
        if (!GFLauncherUtils.ok(domainName) && (s = getString("default")) != null) {
            domainName = s;
        }

        if ((s = getString("instancename")) != null) {
            instanceName = s;
        }

        // finally, do the booleans
        if ((b = getBoolean("debug")) != null) {
            debug = b;
        }

        if ((b = getBoolean("verbose")) != null) {
            verbose = b;
        }

        if ((b = getBoolean("upgrade")) != null) {
            upgrade = b;
        }
    }

    private void finalSetup() throws GFLauncherException {
        if(installDir == null)
            installDir = GFLauncherUtils.getInstallDir();

        if (!GFLauncherUtils.safeIsDirectory(installDir)) {
            throw new GFLauncherException("noInstallDir", installDir);
        }

        // check user-supplied args
        if (domainParentDir != null) {
            // if the arg was given -- then it MUST point to a real dir
            if (!GFLauncherUtils.safeIsDirectory(domainParentDir)) {
                throw new GFLauncherException("noDomainParentDir", domainParentDir);
            }
        }

        setupDomainRootDir();

        if (!GFLauncherUtils.safeIsDirectory(domainRootDir)) {
            throw new GFLauncherException("noDomainRootDir", domainRootDir);
        }

        configDir = new File(domainRootDir, CONFIG_DIR);

        if (!GFLauncherUtils.safeIsDirectory(configDir)) {
            throw new GFLauncherException("noConfigDir", configDir);
        }

        configFile = new File(configDir, CONFIG_FILENAME);

        if (!GFLauncherUtils.safeExists(configFile)) {
            throw new GFLauncherException("noConfigFile", configFile);
        }

        if (instanceName == null) {
            instanceName = "server";
        }

        // if we made it here -- we're in pretty good shape!
        valid = true;
    }

    private void setupDomainRootDir() throws GFLauncherException {
        // if they set domainrootdir -- it takes precedence
        if (domainRootDir != null) {
            domainParentDir = domainRootDir.getParentFile();
            domainName = domainRootDir.getName();
            return;
        }

        // if they set domainParentDir -- use it.  o/w use the default dir
        if (domainParentDir == null) {
            domainParentDir = new File(installDir, DEFAULT_DOMAIN_PARENT_DIR);
        }

        // if they specified domain name -- use it.  o/w use the one and only dir
        // in the domain parent dir

        if (domainName == null) {
            domainName = getTheOneAndOnlyDomain();
        }

        domainRootDir = new File(domainParentDir, domainName);
    }

    private String getTheOneAndOnlyDomain() throws GFLauncherException {
        // look for subdirs in the parent dir -- there must be one and only one

        File[] files = domainParentDir.listFiles(new FileFilter() {

            public boolean accept(File f) {
                return GFLauncherUtils.safeIsDirectory(f);
            }
        });

        if (files == null || files.length == 0) {
            throw new GFLauncherException("noDomainDirs", domainParentDir);
        }

        if (files.length > 1) {
            throw new GFLauncherException("tooManyDomainDirs", domainParentDir);
        }

        return files[0].getName();
    }

    private Boolean getBoolean(String key) {
        // 3 return values -- true, false, null
        String s = getValueIgnoreCommandDelimiter(key);

        if (s != null)
            return Boolean.valueOf(s);
        else
            return null;
    }

    private File getFile(String key) {
        String s = getString(key);

        if (s == null)
            return null;
        else
            return new File(s);
    }

    private String getString(String key) {
        return getValueIgnoreCommandDelimiter(key);
    }

    private String getValueIgnoreCommandDelimiter(String key) {
        // it can be confusing trying to remember -- is it "--option"?
        // or "-option" or "option".  So look for any such match.

        if (argsMap.containsKey(key)) {
            return argsMap.get(key);
        }
        key = "-" + key;
        if (argsMap.containsKey(key)) {
            return argsMap.get(key);
        }
        key = "-" + key;
        if (argsMap.containsKey(key)) {
            return argsMap.get(key);
        }
        return null;
    }

    private ServerType type;
    private boolean verbose = false;
    private boolean debug = false;
    private boolean upgrade = false;
    File installDir;
    private File domainParentDir;
    private File domainRootDir;
    private File instanceRootDir;
    private File configDir;
    private File configFile; // domain.xml
    private String domainName;
    private String instanceName;
    private boolean valid = false;
    private Map<String, String> argsMap;
    private ArrayList<String> argsRaw = new ArrayList<String>();
    private Set<Integer> adminPorts;
    private RespawnInfo respawnInfo;
    // BUG TODO get the def. domains dir from asenv 3/14/2008
    private final static String DEFAULT_DOMAIN_PARENT_DIR = "domains";
    private final static String CONFIG_DIR = "config";
    private final static String CONFIG_FILENAME = "domain.xml";
    //password tokens -- could be multiple -- launcher should *just* write them onto stdin of server
    final List<String> securityTokens = new ArrayList<String>(); // note: it's package private
}

