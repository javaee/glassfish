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
package com.sun.enterprise.admin.server.core;


import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.instance.ServerManager;
import com.sun.enterprise.instance.InstanceEnvironment;

/** 
 * This class provides a manager for tracking manual changes.
 * A thread runs every so often and collects info about a server
 * and updates its static variable.
 *
 * Whenever there is a request from user, we check only the variable
 * to determine if there has been manual changes.
 *
 * @author Sridatta Viswanath
 */

public final class ManualChangeTracker 
{
    /**
     * Logger for admin service
     */
    static Logger logger = Logger.getLogger(AdminConstants.kLoggerName);
       
    /*
     * Default sleep period for the thread is 2 minutes
     */
    private static int sleepPeriod = 120000; //2 minutes
    
    //TODO: initialize from server.xml property
    /*
    static {
        try {
            //initialize sleep period from server.xml
            // Get ConfigContext 
            // get admin service
            // get property element called manual-changes-sleep-period in admin-service
            // initialize sleepPeriod to this value
        } catch(Throwable t) {
            //ignore errors
        } 
    }
     */
    
    /*
     * pointer to the thread for housekeeping
     */
    private static TrackerThread trackerThread = null;

    /**
     * called from admin service during initialization
     * package specific
     */
    static void start() {
        logger.log(Level.FINE, "core.tracker_thread_starting");
        trackerThread = new TrackerThread();
        trackerThread.start();
    }
    
    /**
     * called from AdminService during shutdown.
     * thread.destroy() is bad
     * thread.stop() is deprecated.
     * making trakerThread null is an elegant and preferred way of cleaning up the thread
     * Note that the while loop in the thread checks for null!
     */
    static void stop() {
        trackerThread = null;
        logger.log(Level.FINE, "core.tracker_thread_stopping");
    }
   
    private static class TrackerThread extends Thread
    {
	private static final int SLEEPTIME = sleepPeriod;

	public void run()
	{
	   Thread t = Thread.currentThread();
           
           while (t == trackerThread) {
		try {
                    sleep(SLEEPTIME);
		} catch ( InterruptedException ex ) {}

		try {
                    
                    String[] instanceIds = ServerManager.instance().getInstanceNames(false);
                    for (int i = 0 ; i < instanceIds.length ; i ++) {
                        try {
                            String instanceId = instanceIds[i];
                            InstanceEnvironment ie = new InstanceEnvironment(instanceId);
                            ManualChangeStatus mcs = new ManualChangeStatus();

                            /* TOMCAT_BEGIN Ramakanth */
                            mcs.setServerXmlFileChanged(
                                ie.hasHotXmlChanged());
                            mcs.setRealmsKeyFileChanged(
                                ie.hasHotRealmsKeyChanged());
                            /* TOMCAT_END Ramakanth */
                            logger.log(Level.FINE, "Got Manual Change status for "+  instanceId);
                            logger.log(Level.FINEST,"-------------------------------------");
                            logger.log(Level.FINEST,mcs.toString());
                            logger.log(Level.FINEST,"------------------------------------");
                            ManualChangeManager.addManualChangeStatus(instanceId, mcs); // not synchronized.
                            
                            // If there are multiple threads, this thread should not take up the cpu all the time.
                            // just a prevention. note, if it is a multi-processor machine, jdk1.4 allows this thread
                            // to run on another cpu. 
                            sleep(2000); //rest for 2 seconds. 
                        }
                        catch (Exception e) {
                            //Log the exception for this instance.
                            // what can we do if there is an exception?
                            // just log and continue.
                            logger.log(Level.WARNING, "core.error_getting_manual_changes", e);
                        }
                    }
                    
		} catch ( Exception ex ) {}
	    }
	}
    }
}
