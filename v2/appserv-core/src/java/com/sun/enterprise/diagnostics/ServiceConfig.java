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
package com.sun.enterprise.diagnostics;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.diagnostics.Constants;

/**
 * Represents diagnostic service related config information from domain.xml
 * @author Manisha Umbarje
 */
public class ServiceConfig {

    private static Logger logger = 
            LogDomains.getLogger(LogDomains.ADMIN_LOGGER);
    private String logFile;
    private int minLogLevel;
    private int maxNoOfEntries ;
    private String repositoryDir;
    private String instanceName;
    private boolean local;
    private boolean captureSystemInfoEnabled;
    private boolean captureChecksumEnabled = true;
    private boolean captureAppDDEnabled = true;
    private boolean captureInstallLogEnabled = true;
    private boolean verifyDomainEnabled = true;
    private boolean captureHadbInfoEnabled ;
    private ServiceConfigHelper configHelper;

    public ServiceConfig(boolean localFlag, String repositoryDir, 
            String instanceName) throws DiagnosticException {
	local = localFlag;
	this.repositoryDir = repositoryDir;
	this.instanceName = instanceName;
	setValues();
    }
    
    public ServiceConfig(boolean captureChecksumEnabled, 
            boolean captureAppDDEnabled, boolean captureInstallLogEnabled,
            boolean verifyDomainEnabled, boolean captureHadbInfoEnabled,
            boolean captureSystemInfoEnabled, int minLogLevel, 
            int maxNoOfEntries, String logFile, String repositoryDir, 
            String instanceName
            ) {
        this.captureChecksumEnabled = captureChecksumEnabled;
        this.captureAppDDEnabled = captureAppDDEnabled;
        this.captureInstallLogEnabled = captureInstallLogEnabled;
        this.verifyDomainEnabled = verifyDomainEnabled;
        this.captureHadbInfoEnabled = captureHadbInfoEnabled;
        this.captureSystemInfoEnabled = captureSystemInfoEnabled;
        this.minLogLevel = minLogLevel;
        this.maxNoOfEntries = maxNoOfEntries;
        this.logFile = logFile;
        this.instanceName = instanceName;
        this.repositoryDir = repositoryDir;
    }

    /**
     * Returns value of max-log-entries of diagnostic-service
     */
    public int getMaxNoOfEntries() {
	return maxNoOfEntries;
    }

    /**
     * Returns value of min-log-level of diagnostic-service
     */
    public int getMinLogLevel() {
	return minLogLevel;
    }

    /**
     * Returns value of file of log-service
     */
    public String getLogFile() {
	return logFile;
    }//getLogFile

     /**
     * Returns central or cache repository location
     */
    public String getRepositoryDir() {
	return repositoryDir;
    }

 
    /**
     * Returns true capturing app DD is enabled
     */
    public boolean isCaptureAppDDEnabled() {
	return captureAppDDEnabled;
    }
    
    /**
     * Returns true capturing install log is enabled
     */
    public boolean isCaptureInstallLogEnabled() {
	return captureInstallLogEnabled;
    }
    
    /**
     * Returns true if output of verify-domain is captured
     */
    public boolean isVerifyDomainEnabled() {
	return verifyDomainEnabled;
    }

    /**
     * Returns true if check sum is  captured
     */
    public boolean isCaptureChecksumEnabled() {
	return captureChecksumEnabled;
    }
    /**
     * Returns true capturing app DD is enabled
     */
    public boolean isCaptureHadbInfoEnabled() {
	return captureHadbInfoEnabled;
    }
    
    public String getInstanceName() {
        return instanceName;
    }

    /**
     * Returns true if output of verify-domain is captured
     */
    public boolean isCaptureSystemInfoEnabled() {
	return captureSystemInfoEnabled;
    }

    /**
     * Returns null, if ServiceConfig is created with default values.
     */
    public String getConfigName() {
        if (configHelper != null)
            return configHelper.getConfigName();
        return null;
            
    }
      
    public void debug() {
        logger.log(Level.FINEST, "diagnostic-service.capture_app_dd ", 
               new Object[] {captureAppDDEnabled});
        logger.log(Level.FINEST, "diagnostic-service.capture_hadb_info",
                new Object[]{captureHadbInfoEnabled});
        logger.log(Level.FINEST, "diagnostic-service.capture_checksum",
                new Object[]{captureChecksumEnabled});
        logger.log(Level.FINEST, "diagnostic-service.capture_install_log",
                new Object[]{captureInstallLogEnabled});
        logger.log(Level.FINEST, "diagnostic-service.min_log_level", 
                new Object[]{minLogLevel});
        logger.log(Level.FINEST, "diagnostic-service.max_log_entries", 
                new Object[]{maxNoOfEntries});
    }
    
    public String toString() {
        return getInstanceName() + "," + getConfigName() + 
                "," + getMaxNoOfEntries() +"," + getMinLogLevel() + 
                "," + getLogFile() +
                "," + getRepositoryDir() + "," + isCaptureAppDDEnabled() + 
                "," + isCaptureChecksumEnabled() + 
                "," + isCaptureHadbInfoEnabled() + 
                "," + isCaptureInstallLogEnabled() +
                "," + isVerifyDomainEnabled() ;
    }
    /**
     * Initializes config values
     */
    private void setValues() throws DiagnosticException {
        //setRepositoryDir();
	configHelper = new ServiceConfigHelper(repositoryDir, instanceName, local);
        minLogLevel = Level.parse
		    (getAttribute(ServerTags.MIN_LOG_LEVEL)).intValue();
	logFile = getAttribute(ServerTags.FILE);
	maxNoOfEntries = Integer.parseInt
		    (getAttribute(ServerTags.MAX_LOG_ENTRIES));
        captureAppDDEnabled = getBooleanAttribute(ServerTags.CAPTURE_APP_DD);
	captureInstallLogEnabled =getBooleanAttribute(ServerTags.CAPTURE_INSTALL_LOG);
        verifyDomainEnabled = getBooleanAttribute(ServerTags.VERIFY_CONFIG);
	captureHadbInfoEnabled = getBooleanAttribute(ServerTags.CAPTURE_HADB_INFO);
        captureChecksumEnabled = getBooleanAttribute(ServerTags.COMPUTE_CHECKSUM);
        captureSystemInfoEnabled = getBooleanAttribute(ServerTags.CAPTURE_SYSTEM_INFO);
    }

    /**
     * Retrieve value of the supplied attribute
     */
    private String getAttribute(String attribute) throws DiagnosticException {
	return configHelper.getAttribute(attribute);
    }

    private boolean getBooleanAttribute(String attrName) 
        throws DiagnosticException{
        return Boolean.valueOf(getAttribute(attrName)).booleanValue();
    }
}
