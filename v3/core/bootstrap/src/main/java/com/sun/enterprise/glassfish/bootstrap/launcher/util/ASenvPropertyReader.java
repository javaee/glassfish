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
package com.sun.enterprise.glassfish.bootstrap.launcher.util;

import com.sun.enterprise.glassfish.bootstrap.launcher.GFLauncherLogger;
import java.util.HashMap;

import java.io.*;
import java.util.logging.*;
import java.util.*;

/**
 * Class ASenvPropertyReader
 * 
 * This class converts the envrionment variables stored in asenv.conf (UNIX)
 * or asenv.bat (WINDOWS) into their equivalent system properties. 
 * This means that a number of system properties with fixed values do 
 * not have to be passed on the java command line using -D.
 */
public class ASenvPropertyReader {

    public ASenvPropertyReader(File installDir) {
        this.configDir = new File(installDir, "config");
        setEnvToPropMap();

        //props.putAll(System.getProperties());
        props.put(SystemPropertyConstants.INSTALL_ROOT_PROPERTY, installDir.getPath());
        setProperties();
        postProcess();
    }

    public Map<String, String> getProperties() {
        return props;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Set<String> keys = props.keySet();

        for (String key : keys) {
            sb.append(key).append("=").append(props.get(key)).append('\n');
        }
        return sb.toString();
    }

    /**
     * Method setSystemProperties
     * Iterate through the lines of asenv.conf or asenv.bat and convert to 
     * system properties. 
     */
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

    /* 
     * 2 things to do
     * 1) change reltive paths to absolute
     * 2) change env. variables to the actual values in the environment
     */
    private void postProcess() {
        final Map<String, String> env = System.getenv();
        Map<String, String> all = new HashMap<String, String>(props);
        all.putAll(env);
        TokenResolver tr = new TokenResolver(all);
        tr.resolve(props);

        // props have all tokens replaced now (if they exist)
        // now make the paths absolute.

        Set<String> keys = props.keySet();

        for (String key : keys) {
            String value = props.get(key);
            if (GFLauncherUtils.isRelativePath(value)) {
                // we have to handle both of these:
                // /x/y/../z
                // ../x/y/../z

                File f;
                if (value.startsWith(".")) {
                    f = GFLauncherUtils.absolutize(new File(configDir, value));
                }
                else {
                    f = GFLauncherUtils.absolutize(new File(value));
                }

                props.put(key, f.getPath());
            }
        }
    }

    private void setEnvToPropMap() {
        //The envToPropMap keeps the mapping between environment variable
        //name and system property name.
        envToPropMap.put("AS_ANT",
                SystemPropertyConstants.ANT_ROOT_PROPERTY);
        envToPropMap.put("AS_ANT_LIB",
                SystemPropertyConstants.ANT_LIB_PROPERTY);
        envToPropMap.put("AS_DERBY_INSTALL",
                SystemPropertyConstants.DERBY_ROOT_PROPERTY);
        envToPropMap.put("AS_WEBCONSOLE_LIB",
                SystemPropertyConstants.WEBCONSOLE_LIB_PROPERTY);
        envToPropMap.put("AS_WEBCONSOLE_APP",
                SystemPropertyConstants.WEBCONSOLE_APP_PROPERTY);
        envToPropMap.put("AS_JATO_LIB",
                SystemPropertyConstants.JATO_ROOT_PROPERTY);
        envToPropMap.put("AS_WEBSERVICES_LIB",
                SystemPropertyConstants.WEB_SERVICES_LIB_PROPERTY);
        envToPropMap.put("AS_PERL",
                SystemPropertyConstants.PERL_ROOT_PROPERTY);
        envToPropMap.put("AS_NSS",
                SystemPropertyConstants.NSS_ROOT_PROPERTY);
        envToPropMap.put("AS_NSS_BIN",
                SystemPropertyConstants.NSS_BIN_PROPERTY);
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
        envToPropMap.put("AS_ACC_CONFIG", null);
        envToPropMap.put("AS_JHELP",
                SystemPropertyConstants.JHELP_ROOT_PROPERTY);
        envToPropMap.put("AS_ICU_LIB",
                SystemPropertyConstants.ICU_LIB_PROPERTY);
        envToPropMap.put("AS_LOCALE",
                SystemPropertyConstants.DEFAULT_LOCALE_PROPERTY);
        envToPropMap.put("AS_DEF_DOMAINS_PATH",
                SystemPropertyConstants.DOMAINS_ROOT_PROPERTY);
        envToPropMap.put("AS_HADB",
                SystemPropertyConstants.HADB_ROOT_PROPERTY);
        envToPropMap.put("AS_NATIVE_LAUNCHER",
                SystemPropertyConstants.NATIVE_LAUNCHER);
        envToPropMap.put("AS_NATIVE_LAUNCHER_LIB_PREFIX",
                SystemPropertyConstants.NATIVE_LAUNCHER_LIB_PREFIX);
        envToPropMap.put("AS_JDMK_HOME",
                SystemPropertyConstants.JDMK_HOME_PROPERTY);
        envToPropMap.put("AS_MFWK_HOME",
                SystemPropertyConstants.MFWK_HOME_PROPERTY);
    }
    private Map<String, String> envToPropMap = new HashMap<String, String>();
    private Map<String, String> props = new HashMap<String, String>();
    private File configDir;
}
