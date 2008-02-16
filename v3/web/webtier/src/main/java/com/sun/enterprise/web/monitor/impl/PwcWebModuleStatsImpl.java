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

/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package com.sun.enterprise.web.monitor.impl;

import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import javax.management.ObjectName;
import javax.management.MBeanServerFactory;
import javax.management.MBeanServer;
import javax.management.Attribute;
import com.sun.enterprise.web.monitor.PwcWebModuleStats;

/**
 * Class responsible for gathering web module statistics.
 */
public class PwcWebModuleStatsImpl implements PwcWebModuleStats {
    
    private static final String WEB_MODULE_PREFIX = "//";
    private static final String LIST_SESSION_IDS = "listSessionIds";
    private static final String GET_SESSION = "getSession";
    private static final String[] STRING_PARAM = new String[] {
                                                        "java.lang.String" };
    private static final Integer ZERO_INTEGER = Integer.valueOf(0);

    private ObjectName jspMonitorObjName;
    private ObjectName sessionManagerObjName;
    private ObjectName ctxObjName;
    private transient MBeanServer server;


    /**
     * Constructor.
     *
     * @param ctxObjNameStr Context object name
     * @param ctxPath Context path
     * @param domain Domain in which the Servlet MBean is registered
     * @param vsId The id of the virtual-server on which the web module has
     *        been deployed
     * @param appName Name of the J2EE application to which the web module 
     *        belongs, or null if the web module is standalone
     * @param serverName The server instance name
     */
    public PwcWebModuleStatsImpl(String ctxObjNameStr,
                                 String ctxPath,
                                 String domain,
                                 String vsId,
                                 String appName,
                                 String serverName) {

        // Get an instance of the MBeanServer
        ArrayList servers = MBeanServerFactory.findMBeanServer(null);
        if(servers != null && !servers.isEmpty()) {
            server = (MBeanServer)servers.get(0);
        } else {
            server = MBeanServerFactory.createMBeanServer();
        }

        if ("".equals(ctxPath)) {
            ctxPath = "/";
        }

        /*
         * j2eeType=WebModule
         */
        try {
            ctxObjName = new ObjectName(ctxObjNameStr);
        } catch (Throwable t) {
            MonitorUtil.log(Level.SEVERE,
                            "pwc.monitoring.objectNameCreationError",
                            new Object[] { ctxObjNameStr },
                            t);
        }

        /*
         * type=JspMonitor
         */
        String objNameStr = domain
                + ":type=JspMonitor"
                + ",WebModule=" + WEB_MODULE_PREFIX + vsId + ctxPath
                + ",J2EEApplication=" + appName
                + ",J2EEServer=" + serverName
                + ",*";
        try {
            jspMonitorObjName = new ObjectName(objNameStr);
        } catch (Throwable t) {
            MonitorUtil.log(Level.SEVERE,
                            "pwc.monitoring.objectNameCreationError",
                            new Object[] { objNameStr },
                            t);
        }

        /*
         * type=Manager
         */
        objNameStr = domain + ":type=Manager,path=" + ctxPath
                + ",host=" + vsId;
        try {
            sessionManagerObjName = new ObjectName(objNameStr);
        } catch (Throwable t) {
            MonitorUtil.log(Level.SEVERE,
                            "pwc.monitoring.objectNameCreationError",
                            new Object[] { objNameStr },
                            t);
        }

    }
    
    
    /**
     * Gets the number of JSPs that have been loaded in the web module.
     *
     * Web modules may have more than one JspServlet and associated
     * JspMonitoring MBean: There is one JspServlet declared in and
     * inherited from default-web.xml, plus one JspServlet for each
     * servlet mapped to a jsp-file in web.xml.
     *
     * Therefore, the ObjectName that is queried for JSP related statistics
     * ("jspMonitorObjName") is constructed as a wildcard name, and the
     * queryWildcardStatistic() method will enumerate all matching
     * ObjectNames, retrieve their individual values for the requested
     * monitoring attribute, and return an aggregate value.
     *.
     * @return Number of JSPs that have been loaded
     */
    public int getJspCount() {
        return queryWildcardStatistic(jspMonitorObjName, "jspCount");
    }
    
    
    /**
     * Gets the number of JSPs that have been reloaded in the web module.
     *.
     * @return Number of JSPs that have been reloaded
     */
    public int getJspReloadCount() {
        return queryWildcardStatistic(jspMonitorObjName, "jspReloadCount");
    }


    /**
     * Gets the number of errors that were triggered by JSP invocations.
     *.
     * @return Number of errors triggered by JSP invocations
     */
    public int getJspErrorCount() {
        return queryWildcardStatistic(jspMonitorObjName, "jspErrorCount");
    }


    /**
     * Gets the total number of sessions that have been created for the web
     * module.
     *.
     * @return Total number of sessions created
     */
    public int getSessionsTotal() {
        return getIntValue(queryStatistic(sessionManagerObjName,
                                          "sessionCount"));
    }


    /**
     * Gets the number of currently active sessions for the web module.
     *.
     * @return Number of currently active sessions
     */
    public int getActiveSessionsCurrent() {
        return getIntValue(queryStatistic(sessionManagerObjName,
                                          "activeSessions"));
    }


    /**
     * Gets the maximum number of concurrently active sessions for the web
     * module.
     *
     * @return Maximum number of concurrently active sessions
     */
    public int getActiveSessionsHigh() {
        return getIntValue(queryStatistic(sessionManagerObjName,
                                          "maxActive"));
    }


    /**
     * Gets the total number of rejected sessions for the web module.
     *
     * <p>This is the number of sessions that were not created because the
     * maximum allowed number of sessions were active.
     *
     * @return Total number of rejected sessions
     */
    public int getRejectedSessionsTotal() {
        return getIntValue(queryStatistic(sessionManagerObjName,
                                          "rejectedSessions"));
    }


    /**
     * Gets the total number of expired sessions for the web module.
     *.
     * @return Total number of expired sessions
     */
    public int getExpiredSessionsTotal() {
        return getIntValue(queryStatistic(sessionManagerObjName,
                                          "expiredSessions"));
    }


    /**
     * Gets the longest time (in seconds) that an expired session had been
     * alive.
     *
     * @return Longest time (in seconds) that an expired session had been
     * alive.
     */
    public int getSessionMaxAliveTimeSeconds() {
        return getIntValue(queryStatistic(sessionManagerObjName,
                                          "sessionMaxAliveTimeSeconds"));
    }


    /**
     * Gets the average time (in seconds) that expired sessions had been
     * alive.
     *
     * @return Average time (in seconds) that expired sessions had been
     * alive.
     */
    public int getSessionAverageAliveTimeSeconds() {
        return getIntValue(queryStatistic(sessionManagerObjName,
                                          "sessionAverageAliveTimeSeconds"));
    }


    /**
     * Gets the time when the web module was started.
     *
     * @return Time (in milliseconds since January 1, 1970, 00:00:00) when the
     * web module was started 
     */
    public long getStartTimeMillis() {
        return getLongValue(queryStatistic(ctxObjName, "startTimeMillis"));
    }


    /**
     * Gets the cumulative processing times of all servlets in the web module
     * associated with this PwcWebModuleStatsImpl.
     *
     * @return Cumulative processing times of all servlets in the web module
     * associated with this PwcWebModuleStatsImpl
     */
    public long getServletProcessingTimesMillis() {
        return getLongValue(queryStatistic(ctxObjName, "processingTimeMillis"));
    }


    /**
     * Returns the session ids of all currently active sessions.
     *
     * @return Session ids of all currently active sessions
     */
    public String getSessionIds() {

        Object resultObj = null;

        try {
            resultObj = server.invoke(sessionManagerObjName, 
                                      LIST_SESSION_IDS,
                                      null,
                                      null);
        } catch (Throwable t) {
            MonitorUtil.log(Level.SEVERE,
                            "pwc.monitoring.actionInvocationError", 
                            new Object[] { LIST_SESSION_IDS,
                                           sessionManagerObjName },
                            t);
        }

        return (String) resultObj;
    }


    /**
     * Returns information about the session with the given id.
     *
     * <p>The session information is organized as a HashMap, mapping 
     * session attribute names to the String representation of their values.
     *
     * @param id Session id
     *
     * @return HashMap mapping session attribute names to the String
     * representation of their values, or null if no session with the
     * specified id exists, or if the session does not have any attributes
     */
    public HashMap getSession(String id) {

        Object resultObj = null;

        try {
            resultObj = server.invoke(sessionManagerObjName,
                                      GET_SESSION,
                                      new Object[] { id },
                                      STRING_PARAM);
        } catch (Throwable t) {
            // Ignore if session has been invalidated
            if (!(t instanceof IllegalStateException)) {
                MonitorUtil.log(Level.SEVERE,
                                "pwc.monitoring.actionInvocationError", 
                                new Object[] { GET_SESSION,
                                               sessionManagerObjName },
                                t);
            }
        }

        return (HashMap) resultObj;
    }


    /**
     * Resets this PwcWebModuleStats.
     */
    public void reset() {

        // Reset JSP stats
        resetWildcardStatistic(jspMonitorObjName, "jspReloadCount");
    }


    /**
     * Queries the MBeanServer for the given monitoring attribute for all
     * MBeans that match the given wildcard ObjectName.
     *
     * @param wildcardON The wildcard ObjectName
     * @param attrName The name of the attribute whose value is to be
     *        returned
     *
     * @return The value corresponding to the given attribute name,
     * aggregated over all MBeans that match the given wildcard ObjectName
     */
    private int queryWildcardStatistic(ObjectName wildcardON,
                                       String attrName) {

	int result = 0;

        Set monitorONs = server.queryNames(wildcardON, null);
        Iterator iter = monitorONs.iterator();
        while (iter.hasNext()) {

            ObjectName monitorON = (ObjectName) iter.next();
            Object obj = queryStatistic(monitorON, attrName);
            if (obj != null) {
                result += getIntValue(obj);
            }
        }

        return result;
    }

    
    /*
     * Queries the MBeanServer for an attribute.
     *
     * @param on The ObjectName of the MBean being queried
     * @param attrName The name of the attribute whose value is to be
     *        returned
     *
     * @return The value corresponding to the given attribute name
     */
    private Object queryStatistic(ObjectName on, String attrName) {

        Object resultObj = null;
        try {
            resultObj = server.getAttribute(on, attrName);
        } catch (Throwable t) {
            MonitorUtil.log(Level.SEVERE,
                            "pwc.monitoring.queryError",
                            new Object[] { attrName, on },
                            t);
        }

        return resultObj;
    }


    /**
     * Resets the value of the given attribute on all MBeans whose
     * ObjectName matches the given wildcard ObjectName.
     *
     * @param wildcardON The wildcard ObjectName
     * @param attrName Name of the attribute to be reset
     */
    private void resetWildcardStatistic(ObjectName wildcardON,
                                        String attrName) {

        Set monitorONs = server.queryNames(wildcardON, null);
        Iterator<ObjectName> iter = monitorONs.iterator();
        while (iter.hasNext()) {
            resetStatistic(iter.next(), attrName);
        }
    }


    /**
     * Resets the value of the given attribute on the MBeans with
     * the given ObjectName.
     *
     * @param on The MBean's ObjectName
     * @param attrName Name of the attribute to be reset
     */
    private void resetStatistic(ObjectName on, String attrName) {

        Attribute attr = new Attribute(attrName, ZERO_INTEGER);
        try {
            server.setAttribute(on, attr);
        } catch (Throwable t) {
            MonitorUtil.log(Level.SEVERE,
                            "pwc.monitoring.resetError",
                            new Object[] { attrName, on },
                            t);
        }
    }


    /*
     * Restores this object's state from a stream.
     *
     * @param ois Stream from which to restore this object's state
     */
    private void readObject(ObjectInputStream ois)
            throws java.io.IOException, ClassNotFoundException {

        ois.defaultReadObject();

        // Get an instance of the MBeanServer
        ArrayList servers = MBeanServerFactory.findMBeanServer(null);
        if (servers != null && !servers.isEmpty()) {
            server = (MBeanServer)servers.get(0);
        } else {
            server = MBeanServerFactory.createMBeanServer();
        }
    }        


    private long getLongValue(Object resultObj) {

        long result = 0;

        if (resultObj != null) {
            Long countObj = (Long)resultObj;
            result = countObj.longValue();
        }

        return result;
    }


    private int getIntValue(Object resultObj) {

        int result = 0;

        if (resultObj != null) {
            Integer countObj = (Integer)resultObj;
            result = countObj.intValue();
        }

        return result;
    }

}
