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

import com.sun.logging.LogDomains;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.logging.*;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;

/**
 * This is a generic utility class that uses the native code to fetch 
 * process id of the current JVM process. One usage of this PId is in 
 * the --kill CLI option for stop-* commands such as asadmin stop-domain, 
 * asadmin stop-instance, asadmin stop-node-agent and asadmin stop-cluster.
 * 
 */

public class JvmInfoUtil { 

    public native int getPid();

    boolean debug = false;

    static boolean pidLoggingEnabled = false;

    static Logger _logger = LogDomains.getLogger(LogDomains.UTIL_LOGGER);

    private static LocalStringManagerImpl localStrings = 
        new LocalStringManagerImpl(JvmInfoUtil.class);

    final static public int getPIDfromFileAndDelete(File pidFile)
    {
        // In the only current use case, we are getting the pid in order to kill
        // the process.  In which case this pid file will contain garbage so it
        // should be deleted.
        
        int pid = getPIDfromFile(pidFile);
        try
        {
            pidFile.delete();
        }
        catch(Exception e)
        {
            // ignore
        }
        
        return pid;
    }
    final static public int getPIDfromFile(File pidFile)
    {
        BufferedReader reader = null;
        int pid = -1;
        
        try {
            reader = new BufferedReader(new FileReader(pidFile));
            try {
                pid = Integer.parseInt(reader.readLine());
            }
            catch(Exception e) {
                pid = -1;
            }
        } catch (Exception ex) {
            pid = -1;
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (Exception ex) {
                // too bad this stream closure failed... ignore
            }
        }
        return pid;
    }
    public synchronized void logPID(String pidFileName) {
	try {
            System.loadLibrary("jvminfoutil");
            pidLoggingEnabled = true;
	} catch (Throwable t) {
	    // Log the fact that PID logging remains disabled 
            // which means --kill commands will not work.
            if (debug && !pidLoggingEnabled) {
                _logger.log(Level.WARNING, localStrings.getLocalString(
                "utility.no.pid.logging", "Process ID of this Java Virtual" +
                "Machine will not be logged. Consequently the --kill option " +
                "in Asadmin CLI commands such as stop-domain, stop-instance," +
                " stop-node-agent and stop-cluster will be disabled."));                        
            } 
        }
	PrintStream ps = null;
        File pidFile = new File(pidFileName);
        try {
	    if (debug) System.out.println("PID = " + getPid());
	    if (debug) System.out.println("PID FILENAME = " + pidFileName);
            
            ps = new PrintStream(pidFile);
            if (pidLoggingEnabled) 
                ps.println("" + getPid());
	    else ps.println("-1"); //dos.writeInt(-1); // clients of the pid file interprete -1 
                                   // as "NO_PROCESS should be killed".
            ps.flush();
        } catch (Throwable ex) {
            // Could not update the pid file with latest info.
            // ignore. Because the initiator VMs for stop-xxx commands
            // will check for the timestamp of this file and the admsn
        } finally {
	    try {
	        if (ps != null) ps.close();
	    } catch (Exception ex) {
	        //ignore
            }
            // Bugfix suggested by Abhijit Kumar
            // this will guarantee that once this process is dead - there won't
            // be a file with the old invalid pid lying around.
            pidFile.deleteOnExit();
        }
    }
    
    public static void main(String[] args) {
        int pid = new JvmInfoUtil().getPid(); 
        System.out.println("This JVM Process's Process ID is =" + pid);
   }
}