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
package com.sun.enterprise.server.logging;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import com.sun.enterprise.util.SystemPropertyConstants;


public class SystemLogHandler extends Handler {
    public native void logMessage(String fileName, String level, 
        String message);

    private static final String SYSLOG_FILE_PREFIX = "SJSAS81";

    // We set the prefix first and then build the complete file
    // name in the constructor.
    private String syslogFileName = SYSLOG_FILE_PREFIX;

    public SystemLogHandler( ) {
        try {
            System.loadLibrary( "utilforsyslog" );
            // We will build the syslogFileName which will be in the format
            // SJSAS81_<ClusterId>_<ServerId>.log
            // This will let users to configure Syslog to send out all 
            // AppServer Logs running on different instances to log to
            // one central server, similarly can do the same at the
            // cluster level as well.
            String serverName = System.getProperty( 
                SystemPropertyConstants.SERVER_NAME );
            String clusterId = System.getProperty(
                SystemPropertyConstants.CLUSTER_NAME );
            if( clusterId != null ) {
                syslogFileName = syslogFileName + "_" + clusterId;
            }
            if( serverName != null ) {
                syslogFileName = syslogFileName + "_" + serverName;
            } 
            syslogFileName = syslogFileName + ".log";
        } catch( Exception e ) {
            System.err.println( "Exception in loading Syslog Library..." + e );
            e.printStackTrace( );
        }
    }

    public synchronized void publish( LogRecord record ) {
        logMessage( syslogFileName, record.getLevel().toString(),
            getFormatter().format( record ) );
    }

    public void close( ) { }

    public void flush( ) { }
}

 
    
