/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.universal.glassfish;

import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.util.SystemPropertyConstants;
import java.util.HashMap;

import java.io.*;
import java.util.logging.*;
import java.util.*;

/**
 * Class ASenvPropertyReader
 * 
 * This class converts the variables stored in asenv.conf (UNIX)
 * or asenv.bat (WINDOWS) into their equivalent system properties.
 * <p>This class <strong>guarantees</strong> that no Exception will get thrown back.
 * You may however, have a bad javaRoot set even though we tried everything to find
 * one
 */
public class ASenvPropertyReader {
    /**
     * Read and process the information in asenv
     * There are no arguments because the installation directory is calculated
     * relative to the jar file you are calling from.
     * Unlike V2 this class will not set any System Properties.  Instead it will
     * give you a Map<String,String> containing the properties.
     * <p>To use the class, create an instance and then call getProps().
     */
    public ASenvPropertyReader() {
        this(GFLauncherUtils.getInstallDir());
    }   
    
    /**
     * Read and process the information in asenv.[bat|conf]
     * This constructor should normally not be called.  It is designed for
     * unit test classes that are not running from an official installation.
     * @param installDir The Glassfish installation directory
     */
    public ASenvPropertyReader(File installDir)
    {
        try {
            this.installDir = SmartFile.sanitize(installDir);
            configDir = SmartFile.sanitize(new File(installDir, "config"));
            getBusy();
        }
        catch(Exception e)
        {
            // ignore -- this is universal utility code there isn't much we can
            // do.
        }
    }

    /**
     * Returns the properties that were processed.  This includes going to a bit of
     * trouble setting up the hostname and java root.
     * @return A Map<String,String> with all the properties
     */
    public Map<String, String> getProps()
    {
        return props;
    }

    /**
     * Returns a string representation of the properties in the Map<String,String>.
     * Format:  name=value\nname2=value2\n etc.
     * @return the string representation.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Set<String> keys = props.keySet();

        for (String key : keys) {
            sb.append(key).append("=").append(props.get(key)).append('\n');
        }
        return sb.toString();
    }

    private void getBusy() {
        setEnvToPropMap();
        //props.putAll(System.getProperties());
        props.put(SystemPropertyConstants.INSTALL_ROOT_PROPERTY, installDir.getPath());
        props.put(SystemPropertyConstants.CONFIG_ROOT_PROPERTY, configDir.getPath());
        setProperties();
        postProcess();
        
    }

    /* 
     * 2 things to do
     * 1) change relative paths to absolute
     * 2) change env. variables to either the actual values in the environment
     *  or to another prop in asenv
     */
    private void postProcess() {
        final Map<String, String> env = System.getenv();
        //put env props in first
        Map<String, String> all = new HashMap<String, String>(env);
        // now override with our props
        all.putAll(props);
        TokenResolver tr = new TokenResolver(all);
        tr.resolve(props);

        // props have all tokens replaced now (if they exist)
        // now make the paths absolute.
        absolutize();
        setJavaRoot();
    }
    
    private void absolutize() {
        Set<String> keys = props.keySet();

        for (String key : keys) {
            String value = props.get(key);
            if (GFLauncherUtils.isRelativePath(value)) {
                // we have to handle both of these:
                // /x/y/../z
                // ../x/y/../z

                File f;
                if (value.startsWith(".")) {
                    f = SmartFile.sanitize(new File(configDir, value));
                }
                else {
                    f = SmartFile.sanitize(new File(value));
                }

                props.put(key, f.getPath());
            }
        }
    }
    
    private void setJavaRoot() {
        // make sure we have a folder with java in it!
        // note that we are not in a position to set it from domain.xml yet
        
        // first choice -- whatever is in asenv
        String javaRootName = props.get(SystemPropertyConstants.JAVA_ROOT_PROPERTY);
        
        if(isValidJavaRoot(javaRootName))
            return; // we are already done!
            
        // try JAVA_HOME
        javaRootName = System.getenv("JAVA_HOME");

        if(isValidJavaRoot(javaRootName))
        {
            javaRootName = SmartFile.sanitize(new File(javaRootName)).getPath();
            props.put(SystemPropertyConstants.JAVA_ROOT_PROPERTY, javaRootName);
            return;
        }
        // try java.home with ../
        // usually java.home is pointing at jre and ".." goes to the jdk
        javaRootName = System.getProperty("java.home") + "/..";

        if(isValidJavaRoot(javaRootName))
        {
            javaRootName = SmartFile.sanitize(new File(javaRootName)).getPath();
            props.put(SystemPropertyConstants.JAVA_ROOT_PROPERTY, javaRootName);
            return;
        }

        // try java.home as-is
        javaRootName = System.getProperty("java.home");

        if(isValidJavaRoot(javaRootName))
        {
            javaRootName = SmartFile.sanitize(new File(javaRootName)).getPath();
            props.put(SystemPropertyConstants.JAVA_ROOT_PROPERTY, javaRootName);
            return;
        }

        // TODO - should this be an Exception?  A log message?
        props.put(SystemPropertyConstants.JAVA_ROOT_PROPERTY, null);
    }

    private boolean isValidJavaRoot(String javaRootName) {
        if(!GFLauncherUtils.ok(javaRootName))
            return false;
        
        // look for ${javaRootName}/bin/java[.exe]
        File f = new File(javaRootName);
        
        if(GFLauncherUtils.isWindows())
            f = new File(f, "bin/java.exe");
        else
            f = new File(f, "bin/java");
        
        return f.exists();
    }

    private void setProperties() {
        String hostname = "localhost";
        try {
            // canonical name checks to make sure host is proper
            hostname = GFLauncherUtils.getCanonicalHostName();
        }
        catch (Exception ex) {
        // ignore, go with "localhost"
        }
        props.put(SystemPropertyConstants.HOST_NAME_PROPERTY, hostname);

        //Read in asenv.conf/bat and set system properties accordingly
        File asenv;

        if (GFLauncherUtils.isWindows()) {
            asenv = new File(configDir, SystemPropertyConstants.WINDOWS_ASENV_FILENAME);
        }
        else {
            asenv = new File(configDir, SystemPropertyConstants.UNIX_ASENV_FILENAME);
        }

        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(asenv));
            String line = null;

            while (true) {
                line = reader.readLine();

                if (line == null) {
                    break;
                }
                else {
                    setProperty(line);
                }
            }
        }
        catch (Exception ex) {
        // TODO
        }
        finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            }
            catch (Exception ex) {
            }
        }
    }

    /**
     * Method setProperty
     * Parses a single line of asenv.conf or asenv.bat and attempt to 
     * set the corresponding property. Note that if the system
     * property is already set (e.g. via -D on the command line), then
     * we will not clobber its existing value.
     *
     * @param line 
     *
     */
    private void setProperty(String line) {
        int pos = line.indexOf("=");

        if (pos > 0) {
            String lhs = (line.substring(0, pos)).trim();
            String rhs = (line.substring(pos + 1)).trim();

            if (GFLauncherUtils.isWindows()) {    //trim off the "set "
                lhs = (lhs.substring(3)).trim();
            }
            else {      // take the quotes out
                pos = rhs.indexOf("\"");
                if (pos != -1) {
                    rhs = (rhs.substring(pos + 1)).trim();
                    pos = rhs.indexOf("\"");
                    if (pos != -1) {
                        rhs = (rhs.substring(0, pos)).trim();
                    }
                }
            }

            String systemPropertyName = envToPropMap.get(lhs);

            if (systemPropertyName != null) {
                props.put(systemPropertyName, rhs);
            }
        }
    }
    private void setEnvToPropMap() {
        //The envToPropMap keeps the mapping between environment variable
        //name and system property name.
        envToPropMap.put("AS_DERBY_INSTALL",
                SystemPropertyConstants.DERBY_ROOT_PROPERTY);
        envToPropMap.put("AS_IMQ_LIB",
                SystemPropertyConstants.IMQ_LIB_PROPERTY);
        envToPropMap.put("AS_IMQ_BIN",
                SystemPropertyConstants.IMQ_BIN_PROPERTY);
        envToPropMap.put("AS_CONFIG",
                SystemPropertyConstants.CONFIG_ROOT_PROPERTY);
        envToPropMap.put("AS_INSTALL",
                SystemPropertyConstants.INSTALL_ROOT_PROPERTY);
        envToPropMap.put("AS_JAVA",
                SystemPropertyConstants.JAVA_ROOT_PROPERTY);
        envToPropMap.put("AS_DEF_DOMAINS_PATH",
                SystemPropertyConstants.DOMAINS_ROOT_PROPERTY);
    }
    private Map<String, String> envToPropMap = new HashMap<String, String>();
    private Map<String, String> props = new HashMap<String, String>();
    private File configDir;
    private File installDir;
}
