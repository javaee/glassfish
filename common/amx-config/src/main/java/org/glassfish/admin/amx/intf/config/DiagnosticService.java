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
package org.glassfish.admin.amx.intf.config;

import org.glassfish.admin.amx.base.Singleton;


/**
 * Configuration for the &lt;diagnostic-service&gt; for a &lt;config&gt; in
 * a domain. This element controls the output of the diagnostic information
 * collected when requested by the management clients. The actual information
 * collected is documented in product documentation.
 * @since Appserver 9.0
*/
public interface DiagnosticService
    extends PropertiesAccess, ConfigElement, Singleton
{
    /**
        @return true if the diagnostic service is computing the checksum for the data
     */
    
    public String getComputeChecksum();
    
    /**
        See {@link #getComputeChecksum}.
     */
    public void setComputeChecksum(final String value);
    
    /**
     @return true if installation log is collected for diagnosis, false otherwise
     */
    
    public String getCaptureInstallLog();
    
    /**
        See {@link #getCaptureInstallLog}.
     */
    public void setCaptureInstallLog(final String value);
    
    /**
     @return true if operating system information is being collected, false otherwise
     */
    public String getCaptureSystemInfo();
    
    /** Set capturing system information to specified value.
     */
    public void setCaptureSystemInfo(final String value);
    
    /**
        @return true if HA data-base information is being collected, false otherwise
     */
    
    public String getCaptureHADBInfo();
    
    /**
        See {@link #getCaptureHADBInfo}.
     */
    public void setCaptureHADBInfo(final String value);

    /**
        @return true if app deployment descriptor data is being collected, false otherwise
     */
    
    public String getCaptureAppDD();
    
    /**
        See {@link #getCaptureAppDD}.
     */
    public void setCaptureAppDD(final String value);
    
    /**
        The level at which the messages for diagnostic services will
        be retrieved. All the messages at a level equal to or more than this
        level will be captured.
        @return the log level of the messages for diagnostic service
        @see java.util.logging.Level
     */
    public String getMinLogLevel();
    
    /**
        See {@link #getMinLogLevel}.
        The specified value must be a valid {@link java.util.logging.Level}.
     */
    public void setMinLogLevel(final String level);
    
    /**
        @return number of log entries to be read from the log
        file. Defaults to 500.
     */
    public String getMaxLogEntries();
    
    /**
        See {@link #getMaxLogEntries}.
     */
    
    public void setMaxLogEntries(final String entries);
    
    /**                    
        Indicates whether output of verify-config asadmin command is
        included in the diagnostic report.                                               
     */
    
    public String  getVerifyConfig();
    
    /**
        See {@link #getVerifyConfig}
     */
    public void     setVerifyConfig( String verify );
}












