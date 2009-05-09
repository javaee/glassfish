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
package com.sun.enterprise.web.stats;

import java.util.Iterator;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.text.MessageFormat;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import com.sun.logging.LogDomains;

/**
 * Utility class for retrieving and manipulating stats.
 */
public final class StatsUtil {

    private static final Logger _logger =
        LogDomains.getLogger(StatsUtil.class, LogDomains.WEB_LOGGER);


    /**
     * Queries the MBean with the given object name for the value of the
     * attribute with the given name.
     *
     * @param server MBean server
     * @param on MBean object name
     * @param attrName Attribute name
     *
     * @return Attribute value
     */
    static Object getStatistic(MBeanServer server, ObjectName on,
                                 String attrName) {

        Object resultObj = null;

        try {
            resultObj = server.getAttribute(on, attrName);
        } catch (Throwable t) {
            String msg = _logger.getResourceBundle().getString(
                                            "webcontainer.mbeanQueryError");
            msg = MessageFormat.format(msg, new Object[] { attrName, on });
            _logger.log(Level.WARNING, msg, t);
        }

        return resultObj;
    }

  
    /**
     * Queries the first MBeans corresponding to the given (wildcard)
     * object name for the value of the attribute with the given name, and
     * returns it.
     *
     * This method assumes that the given attribute name has the same value
     * for all MBeans corresponding to the given wildcard object name.
     *
     * @param server MBean server
     * @param on MBean object name
     * @param attrName Attribute name
     *
     * @return Attribute values
     */
    static int getConstant(MBeanServer server, ObjectName on,
                           String attrName) {

	int result = 0;

        Iterator iter = server.queryNames(on, null).iterator();
        if (iter.hasNext()) {
            Object obj = StatsUtil.getStatistic(server,
                                                (ObjectName) iter.next(),
                                                attrName);
            result = getIntValue(obj);
        }

        return result;
    }


    /**
     * Queries the MBeans corresponding to the given (wildcard) object name
     * for the value of the attribute with the given name, and returns the
     * aggregated attribute values.
     *
     * @param server MBean server
     * @param on MBean object name
     * @param attrName Attribute name
     *
     * @return Aggregated attribute values
     */
    static int getAggregateStatistic(MBeanServer server, ObjectName on,
                                     String attrName) {

	int result = 0;

        Iterator iter = server.queryNames(on, null).iterator();
        while (iter.hasNext()) {
            Object obj = StatsUtil.getStatistic(server,
                                                (ObjectName) iter.next(),
                                                attrName);
            if (obj != null) {
                result += getIntValue(obj);
            }
        }

        return result;
    }
    
    
    /**
     * Queries the MBeans corresponding to the given (wildcard) object name
     * for the value of the attribute with the given name, and returns the
     * aggregated attribute values.
     *
     * @param server MBean server
     * @param on MBean object name
     * @param attrName Attribute name
     *
     * @return Aggregated attribute values
     */
    static long getAggregateLongStatistic(MBeanServer server, ObjectName on,
                                          String attrName) {

	long result = 0;

        Iterator iter = server.queryNames(on, null).iterator();
        while (iter.hasNext()) {
            Object obj = StatsUtil.getStatistic(server,
                                                (ObjectName) iter.next(),
                                                attrName);
            if (obj != null) {
                result += getLongValue(obj);
            }
        }

        return result;
    }   


    /**
     * Queries the MBeans corresponding to the given (wildcard) object name
     * for the value of the attribute with the given name, and returns the
     * largest attribute value.
     *
     * @param server MBean server
     * @param on MBean object name
     * @param attrName Attribute name
     *
     * @return Largest attribute value
     */
    static int getMaxStatistic(MBeanServer server, ObjectName on,
                               String attrName) {

	int max = 0;

        Iterator iter = server.queryNames(on, null).iterator();
        while (iter.hasNext()) {
            Object obj = StatsUtil.getStatistic(server,
                                                (ObjectName) iter.next(),
                                                attrName);
            int result = getIntValue(obj);
            if (result > max) {
                max = result;
            }
        }

        return max;
    }


    /**
     * Queries the MBeans corresponding to the given (wildcard) object name
     * for the value of the attribute with the given name, and returns the
     * average attribute value.
     *
     * @param server MBean server
     * @param on MBean object name
     * @param attrName Attribute name
     *
     * @return Average attribute value
     */
    static int getAverageStatistic(MBeanServer server, ObjectName on,
                                   String attrName) {

        int total = 0;
        int num = 0;

        Iterator iter = server.queryNames(on, null).iterator();
        while (iter.hasNext()) {
            Object obj = StatsUtil.getStatistic(server,
                                                (ObjectName) iter.next(),
                                                attrName);
            if (obj != null) {
                total += getIntValue(obj);
                num++;
            }
        }

        return (num > 0 ? total/num : 0);
    }


    static int getIntValue(Object resultObj) {

        int result = 0;

        if (resultObj instanceof Integer) {
            Integer countObj = (Integer)resultObj;
            result = countObj.intValue();
        }

        return result;
    }


    static long getLongValue(Object resultObj) {

        long result = 0;

        if (resultObj instanceof Long) {
            result = ((Long)resultObj).longValue();
        } else if (resultObj instanceof Integer) {
            result = ((Integer)resultObj).intValue();
        }

        return result;
    }
    
    
    /**
     * Queries the MBeans corresponding to the given (wildcard) object name
     * for the value of the attribute with the given name, and returns the
     * largest attribute value.
     *
     * @param server MBean server
     * @param on MBean object name
     * @param attrName Attribute name
     *
     * @return Largest attribute value
     */
    static long getMaxLongStatistic(MBeanServer server, ObjectName on,
                                    String attrName) {

        long max = 0;

        Iterator iter = server.queryNames(on, null).iterator();
        while (iter.hasNext()) {
            Object obj = StatsUtil.getStatistic(server,
                                                (ObjectName) iter.next(),
                                                attrName);
            long result = getLongValue(obj);
            if (result > max) {
                max = result;
            }
        }

        return max;
    }
}
