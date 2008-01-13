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
package com.sun.enterprise.util;

import java.util.HashMap;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;

import com.sun.logging.LogDomains;

import java.util.logging.Logger;
import java.util.logging.Level;

import com.sun.enterprise.util.net.NetUtils;
import java.net.UnknownHostException;

/**
 * Class ASenvPropertyReader
 * 
 * This class converts the envrionment variables stored in asenv.conf (UNIX)
 * or asenv.bat (WINDOWS) into their equivalent system properties. 
 * This means that a number of system properties with fixed values do 
 * not have to be passed on the java command line using -D.
 */
public class ASenvPropertyReader
{
    private static Logger _logger = null;
        
    private HashMap _propertyMap = null;
    private String _configDirectory = null;
    private boolean useLogger = true;

    /**
     * Constructor ASenvPropertyReader
     *
     *
     * @param configDirectory The configuration directory where asenv.conf
     * or asenv.bat resides.
     *
     */
    public ASenvPropertyReader(String configDirectory) {

        _configDirectory = configDirectory;
        _propertyMap = new HashMap();

        //The _propertyMap keeps the mapping between environment variable
        //name and system property name.
        _propertyMap.put("AS_ANT", 
            SystemPropertyConstants.ANT_ROOT_PROPERTY);
        _propertyMap.put("AS_ANT_LIB", 
            SystemPropertyConstants.ANT_LIB_PROPERTY);
        _propertyMap.put("AS_DERBY_INSTALL", 
            SystemPropertyConstants.DERBY_ROOT_PROPERTY);
	_propertyMap.put("AS_WEBCONSOLE_LIB",
            SystemPropertyConstants.WEBCONSOLE_LIB_PROPERTY);
        _propertyMap.put("AS_WEBCONSOLE_APP",
            SystemPropertyConstants.WEBCONSOLE_APP_PROPERTY);
        _propertyMap.put("AS_JATO_LIB",
            SystemPropertyConstants.JATO_ROOT_PROPERTY);
        _propertyMap.put("AS_WEBSERVICES_LIB", 
            SystemPropertyConstants.WEB_SERVICES_LIB_PROPERTY);
        _propertyMap.put("AS_PERL", 
             SystemPropertyConstants.PERL_ROOT_PROPERTY);
        _propertyMap.put("AS_NSS", 
             SystemPropertyConstants.NSS_ROOT_PROPERTY);
        _propertyMap.put("AS_NSS_BIN", 
             SystemPropertyConstants.NSS_BIN_PROPERTY);
        _propertyMap.put("AS_IMQ_LIB", 
             SystemPropertyConstants.IMQ_LIB_PROPERTY);
        _propertyMap.put("AS_IMQ_BIN", 
             SystemPropertyConstants.IMQ_BIN_PROPERTY);
        _propertyMap.put("AS_CONFIG", 
             SystemPropertyConstants.CONFIG_ROOT_PROPERTY);
        _propertyMap.put("AS_INSTALL", 
             SystemPropertyConstants.INSTALL_ROOT_PROPERTY);
        _propertyMap.put("AS_JAVA", 
             SystemPropertyConstants.JAVA_ROOT_PROPERTY);
        _propertyMap.put("AS_ACC_CONFIG", null);
        _propertyMap.put("AS_JHELP", 
             SystemPropertyConstants.JHELP_ROOT_PROPERTY);
        _propertyMap.put("AS_ICU_LIB", 
             SystemPropertyConstants.ICU_LIB_PROPERTY);
        _propertyMap.put("AS_LOCALE", 
             SystemPropertyConstants.DEFAULT_LOCALE_PROPERTY);
        _propertyMap.put("AS_DEF_DOMAINS_PATH", 
             SystemPropertyConstants.DOMAINS_ROOT_PROPERTY);
        _propertyMap.put("AS_HADB",
            SystemPropertyConstants.HADB_ROOT_PROPERTY);
        _propertyMap.put("AS_NATIVE_LAUNCHER",
            SystemPropertyConstants.NATIVE_LAUNCHER);
        _propertyMap.put("AS_NATIVE_LAUNCHER_LIB_PREFIX",
            SystemPropertyConstants.NATIVE_LAUNCHER_LIB_PREFIX);        
        _propertyMap.put("AS_JDMK_HOME",
            SystemPropertyConstants.JDMK_HOME_PROPERTY);
        _propertyMap.put("AS_MFWK_HOME",
            SystemPropertyConstants.MFWK_HOME_PROPERTY);
    }

    public ASenvPropertyReader(String configDirectory, boolean useLogger) {
        this(configDirectory);
        this.useLogger = useLogger;
    }

    /**
     * Method setSystemProperty
     * Parses a single line of asenv.conf or asenv.bat and attempt to 
     * set the corresponding system property. Note that if the system
     * property is already set (e.g. via -D on the command line), then
     * we will not clobber its existing value.
     *
     * @param line 
     *
     */
    private void setSystemProperty(String line) {

        int pos = line.indexOf("=");

        if (pos > 0) {
            String lhs = (line.substring(0, pos)).trim();
            String rhs = (line.substring(pos + 1)).trim();

            if (OS.isWindows()) {    //trim off the "set "
                lhs = (lhs.substring(3)).trim();
            }

            if (OS.isUNIX()) {      // take the quotes out
               pos = rhs.indexOf("\"");
               if(pos != -1) {
                    rhs = (rhs.substring(pos+1)).trim();
                    pos = rhs.indexOf("\"");
                    if(pos != -1)
                        rhs = (rhs.substring(0, pos)).trim();
               }
            }

            String systemPropertyName = (String)_propertyMap.get(lhs);
            
            if (systemPropertyName != null) {
                if (System.getProperty(systemPropertyName) == null) {
                    if(_logger!=null)
                    _logger.log(Level.FINE, "System.setProperty " +
                            systemPropertyName + "=" + rhs);                    
                    System.setProperty(systemPropertyName, rhs);
                }
            }
        }
    }

    /**
     * Method setSystemProperties
     * Iterate through the lines of asenv.conf or asenv.bat and convert to 
     * system properties. 
     */
    public void setSystemProperties() {
        if(useLogger) {
            _logger = LogDomains.getLogger(LogDomains.UTIL_LOGGER);
        }
        
        //Set static properties. Currently this includes com.sun.aas.hostName. This
        //property is used to avoid placing hardcoded host names into domain.xml 
        //making it non-relocatable.
        if (System.getProperty(SystemPropertyConstants.HOST_NAME_PROPERTY) == null) {
            String hostname = "localhost";
            try {                
                // canonical name checks to make sure host is proper
                hostname = NetUtils.getCanonicalHostName();                       
            } catch (Exception ex) {
               if(_logger!=null)
                _logger.log(Level.SEVERE, "property_reader.unknownHost", ex);
            }           
            if(_logger!=null)
            _logger.log(Level.FINE, "System.setProperty " +
                SystemPropertyConstants.HOST_NAME_PROPERTY + "=" + hostname);    
            System.setProperty(SystemPropertyConstants.HOST_NAME_PROPERTY, hostname);
        }
        
        //Read in asenv.conf/bat and set system properties accordingly
        String fileName = _configDirectory + File.separatorChar;
        
        if (OS.isUNIX()) {
            fileName +=  SystemPropertyConstants.UNIX_ASENV_FILENAME;
        } else if (OS.isWindows()) {
            fileName +=  SystemPropertyConstants.WINDOWS_ASENV_FILENAME;
        } else {
            assert false;
        }

        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(fileName));

            String line = null;

            while (true) {
                line = reader.readLine();

                if (line == null) {
                    break;
                } else {
                    setSystemProperty(line);
                }
            }
        } catch (Exception ex) {
            if(_logger!=null)
            _logger.log(Level.SEVERE, "property_reader.asenvReadError", ex);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception ex) {
                if(_logger!=null)
                _logger.log(Level.WARNING, "property_reader.asenvCloseError", ex);
            }
        }
    }   
}
