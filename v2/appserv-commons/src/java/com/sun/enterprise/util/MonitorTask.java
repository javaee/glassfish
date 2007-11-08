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

/**
 * This task is put in a Timer thread and is executed every N seconds. This
 * task prints in the output the monitorabled of the ORB.
 * @author Darpan Dinker, $Author: tcfujii $
 * @version $Revision: 1.3 $ on $Date: 2005/12/25 04:12:02 $
 * @since jdk1.4
 * Note: None of the methods in this class are synchronized, it is assumed
 * that code which is using this class would take care of synchronization
 * if there is a need. Most cases where initialization of these monitorables
 * is done during server startup should not require any kind of
 * synchronization
 */

// START IASRI 4682740 - support standalone monitoring 

package com.sun.enterprise.util;

import java.util.logging.Level;
import com.sun.enterprise.util.logging.Debug;
import com.sun.enterprise.corba.ee.internal.util.LogWrap;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.Iterator;

public class MonitorTask extends java.util.TimerTask {

    private static boolean initialized = false;
    private static boolean needORBMonitoring = false;
    private static boolean needEJBMonitoring = false;
    private static boolean needJDBCMonitoring = false;
    private static ArrayList monitorableList = null;
    private static long schedPeriod = 180000; // 3 minutes default
    private static java.util.Timer timer;

    /**
     * This method will read the System properties and look for
     * "MONITOR_ORB".
     * "MONITOR_EJB".
     * "MONITOR_JDBC".
     * If the string exists, the
     * monitoring task shall be created at every
     * MONITOR_TIME_PERIOD_SECONDS
     * if provided too, otherwise defaults will be taken for scheduling period.
     */
    private synchronized static boolean isMonitoring () {
        if (!initialized) {
            try {
                String str1=System.getProperties().getProperty("MONITOR_ORB");
                String str2=System.getProperties().getProperty("MONITOR_EJB");
                String str3=System.getProperties().getProperty("MONITOR_JDBC");
                String strm=System.getProperties().getProperty("MONITOR_TIME_PERIOD_SECONDS");
                if( null!=str1 ) {
                    if ( str1.startsWith("true") || str1.startsWith("TRUE") ) {
                        needORBMonitoring = true;
                    }
                }
                if ( null!=str2 ) {
                    if ( str2.startsWith("true") || str2.startsWith("TRUE") ) {
                        needEJBMonitoring = true;
                    }
                }
                if ( null!=str3 ) {
                    if ( str3.startsWith("true") || str3.startsWith("TRUE") ) {
                        needJDBCMonitoring = true;
                    }
                }
                if (needORBMonitoring || needEJBMonitoring || needJDBCMonitoring) {
                    if(null!=strm) {
                        schedPeriod = 1000 * Long.parseLong(strm);
                    }
                }
            } catch(Exception e) {
                LogWrap.logger.log(Level.FINE,
                                   "MINOR: Unable to start a performance monitoring task > " + e);
            }
            if (needORBMonitoring || needEJBMonitoring || needJDBCMonitoring) {
                monitorableList = new ArrayList();
                timer = new java.util.Timer();
                timer.schedule(new MonitorTask(), schedPeriod, schedPeriod);
                LogWrap.logger.log(Level.SEVERE,
                                   "Starting the MonitorTask every "+schedPeriod+" milliseconds.");
            }
            initialized = true;
        }
        return (needORBMonitoring || needEJBMonitoring || needJDBCMonitoring);
    }

    /**
     * Other subsystems in the ORB can use this method to add monitorable
     * entities which are then dumped at the preset intervals.
     * The only requirement for adding an object to the list is that it should
     * implement the toString interface so that details about the object are
     * dumped
     */
    public static void addORBMonitorable(Object monitorable) {
        if (isMonitoring() && needORBMonitoring) {
            monitorableList.add(monitorable);
        }
    }

    public static void addEJBMonitorable(Object monitorable) {
        if (isMonitoring() && needEJBMonitoring) {
            monitorableList.add(monitorable);
        }
    }

    public static void addJDBCMonitorable(Object monitorable) {
        if (isMonitoring() && needJDBCMonitoring) {
            monitorableList.add(monitorable);
        }
    }

    public static ArrayList getMonitorableList() {
        return monitorableList;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see     java.lang.Thread#run()
     */
    public void run() {
        StringBuffer sb = new StringBuffer ();
        try {
            boolean first = true;

            sb.append("MONITORING : ");
            Iterator iter = MonitorTask.getMonitorableList().iterator();
            while (iter.hasNext()) {
                if (first == false) {
                    sb.append(", ");
                } else {
                    first = false;
                }
                sb.append(iter.next().toString());
            }            
            LogWrap.logger.log(Level.SEVERE, sb.toString() );
        } catch(Exception e) {
            LogWrap.logger.log(Level.FINE, "MonitorTask received an exception > " + e);
        }
    }

    protected java.util.Timer getTimer() {
        return timer;
    }

}

// End IASRI 4682740 - ORB to support standalone monitoring 
