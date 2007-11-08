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
 * StringStatisticMonitor.java
 *
 * Created on July 11, 2005 3:00 PM
 */

package com.sun.enterprise.admin.selfmanagement.event;

import javax.management.ObjectName;
import javax.management.MBeanNotificationInfo;
import com.sun.appserv.management.event.StatisticMonitorNotification;
import static com.sun.appserv.management.event.StatisticMonitorNotification.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;

/**
 * Defines a statistic monitor MBean designed to observe the values of a string
 * attribute.
 * <P>
 * A string statistic monitor sends notifications as follows:
 * <UL>
 * <LI> if the attribute value matches the string to compare value,
 *      a {@link StatisticMonitorNotification#STRING_TO_COMPARE_VALUE_MATCHED
 *      match notification} is sent.
 *      The notify match flag must be set to <CODE>true</CODE>.
 *      <BR>Subsequent matchings of the string to compare values do not
 *      cause further notifications unless
 *      the attribute value differs from the string to compare value.
 * <LI> if the attribute value differs from the string to compare value,
 *      a {@link StatisticMonitorNotification#STRING_TO_COMPARE_VALUE_DIFFERED
 *      differ notification} is sent.
 *      The notify differ flag must be set to <CODE>true</CODE>.
 *      <BR>Subsequent differences from the string to compare value do
 *      not cause further notifications unless
 *      the attribute value matches the string to compare value.
 * </UL>
 * Used for JDK version greater than 1.5
 * @author      Sun Microsystems, Inc
 */
public class StringStatisticMonitor extends StatisticMonitor implements StringStatisticMonitorMBean {

    //LOGGER 
    //---------------
    protected static final Logger _logger = LogDomains.getLogger(LogDomains.SELF_MANAGEMENT_LOGGER);

    /*
     * ------------------------------------------
     *  PRIVATE VARIABLES
     * ------------------------------------------
     */

    /**
     * String to compare with the observed attribute.
     * <BR>The default value is an empty character sequence.
     */
    private String stringToCompare = "";

    /**
     * Flag indicating if the string statistic monitor notifies when matching
     * the string to compare.
     * <BR>The default value is set to <CODE>false</CODE>.
     */
    private boolean notifyMatch = false;

    /**
     * Flag indicating if the string statistic monitor notifies when differing
     * from the string to compare.
     * <BR>The default value is set to <CODE>false</CODE>.
     */
    private boolean notifyDiffer = false;

    /**
     * This attribute is used to handle the matching/differing mechanism.
     * <BR>Each element in this array corresponds to an observed object
     * in the list.
     */
    private int status[] = new int[capacityIncrement];

    private static final String[] types = {
        RUNTIME_ERROR,
        OBSERVED_OBJECT_ERROR,
        OBSERVED_ATTRIBUTE_ERROR,
        OBSERVED_ATTRIBUTE_TYPE_ERROR,
        STRING_TO_COMPARE_VALUE_MATCHED,
        STRING_TO_COMPARE_VALUE_DIFFERED
    };

    private static final MBeanNotificationInfo[] notifsInfo = {
        new MBeanNotificationInfo(
            types,
            "com.sun.appserv.management.event.StatisticMonitorNotification",
            "Notifications sent by the StringStatisticMonitor MBean")
    };

    // Flags needed to implement the matching/differing mechanism.
    //
    private static final int MATCHING                   = 0;
    private static final int DIFFERING                  = 1;
    private static final int MATCHING_OR_DIFFERING      = 2;

    /*
     * ------------------------------------------
     *  CONSTRUCTORS
     * ------------------------------------------
     */

    /**
     * Default constructor.
     */
    public StringStatisticMonitor() {
    }

    /*
     * ------------------------------------------
     *  PUBLIC METHODS
     * ------------------------------------------
     */

    /**
     * Starts the string monitor.
     */
    public synchronized void start() {
        // Reset values.
        //
        for (int i = 0; i < elementCount; i++) {
            status[i] = MATCHING_OR_DIFFERING;
        }
        doStart();
    }

    /**
     * Stops the string monitor.
     */
    public synchronized void stop() {
        doStop();
    }

    // GETTERS AND SETTERS
    //--------------------

    /**
     * Gets the derived gauge of the specified object, if this object is
     * contained in the set of observed MBeans, or <code>null</code> otherwise.
     *
     * @param object the name of the MBean whose derived gauge is required.
     *
     * @return The derived gauge of the specified object.
     *
     * @since.unbundled JMX 1.2
     */
    public synchronized String getDerivedGauge(ObjectName object) {
        return (String) super.getDerivedGauge(object);
    }

    /**
     * Gets the derived gauge timestamp of the specified object, if
     * this object is contained in the set of observed MBeans, or
     * <code>null</code> otherwise.
     *
     * @param object the name of the object whose derived gauge
     * timestamp is to be returned.
     *
     * @return The derived gauge timestamp of the specified object.
     *
     * @since.unbundled JMX 1.2
     */
    public synchronized long getDerivedGaugeTimeStamp(ObjectName object) {
        return super.getDerivedGaugeTimeStamp(object);
    }

    /**
     * Returns the derived gauge of the first object in the set of
     * observed MBeans.
     *
     * @return The derived gauge.
     *
     * @deprecated As of JMX 1.2, replaced by
     * {@link #getDerivedGauge(ObjectName)}
     */
    @Deprecated
    public synchronized String getDerivedGauge() {
        return (String) derivedGauge[0];
    }

    /**
     * Gets the derived gauge timestamp of the first object in the set
     * of observed MBeans.
     *
     * @return The derived gauge timestamp.
     *
     * @deprecated As of JMX 1.2, replaced by
     * {@link #getDerivedGaugeTimeStamp(ObjectName)}
     */
    @Deprecated
    public synchronized long getDerivedGaugeTimeStamp() {
        return derivedGaugeTimestamp[0];
    }

    /**
     * Gets the string to compare with the observed attribute common
     * to all observed MBeans.
     *
     * @return The string value.
     *
     * @see #setStringToCompare
     */
    public synchronized String getStringToCompare() {
        return stringToCompare;
    }

    /**
     * Sets the string to compare with the observed attribute common
     * to all observed MBeans.
     *
     * @param value The string value.
     *
     * @exception IllegalArgumentException The specified
     * string to compare is null.
     *
     * @see #getStringToCompare
     */
    public synchronized void setStringToCompare(String value)
            throws IllegalArgumentException {

        if (value == null) {
            throw new IllegalArgumentException("Null string to compare");
        }

        stringToCompare = value;

        // Reset values.
        //
        for (int i = 0; i < elementCount; i++) {
          status[i] = MATCHING_OR_DIFFERING;
        }
    }

    /**
     * Gets the matching notification's on/off switch value common to
     * all observed MBeans.
     *
     * @return <CODE>true</CODE> if the string statistic monitor notifies when
     * matching the string to compare, <CODE>false</CODE> otherwise.
     *
     * @see #setNotifyMatch
     */
    public synchronized boolean getNotifyMatch() {
        return notifyMatch;
    }

    /**
     * Sets the matching notification's on/off switch value common to
     * all observed MBeans.
     *
     * @param value The matching notification's on/off switch value.
     *
     * @see #getNotifyMatch
     */
    public synchronized void setNotifyMatch(boolean value) {
        notifyMatch = value;
    }

    /**
     * Gets the differing notification's on/off switch value common to
     * all observed MBeans.
     *
     * @return <CODE>true</CODE> if the string statistic monitor notifies when
     * differing from the string to compare, <CODE>false</CODE> otherwise.
     *
     * @see #setNotifyDiffer
     */
    public synchronized boolean getNotifyDiffer() {
        return notifyDiffer;
    }

    /**
     * Sets the differing notification's on/off switch value common to
     * all observed MBeans.
     *
     * @param value The differing notification's on/off switch value.
     *
     * @see #getNotifyDiffer
     */
    public synchronized void setNotifyDiffer(boolean value) {
        notifyDiffer = value;
    }

    /**
     * Returns a <CODE>NotificationInfo</CODE> object containing the name of
     * the Java class of the notification and the notification types sent by
     * the string statistic monitor.
     */
    public MBeanNotificationInfo[] getNotificationInfo() {
        return notifsInfo.clone();
    }

    /*
     * ------------------------------------------
     *  PACKAGE METHODS
     * ------------------------------------------
     */

    /**
     * Check that the type of the supplied observed attribute
     * value is one of the value types supported by this monitor.
     */
    boolean isComparableTypeValid(ObjectName object,
                                  String attribute,
                                  Comparable<?> value) {
        // Check that the observed attribute is of type "String".
        //
        if (value instanceof String) {
            return true;
        }
        return false;
    }

    void onErrorNotification(StatisticMonitorNotification notification) {
        int index = indexOf(notification.getObservedObject());
        synchronized(this) {
            // Reset values.
            //
            status[index] = MATCHING_OR_DIFFERING;
        }
    }

    StatisticMonitorNotification buildAlarmNotification(ObjectName object,
                                               String attribute,
                                               Comparable<?> value) {
        String type = null;
        String msg = null;
        Object trigger = null;

        int index = indexOf(object);

        synchronized(this) {

            // Send matching notification if notifyMatch is true.
            // Send differing notification if notifyDiffer is true.
            //
            if (status[index] == MATCHING_OR_DIFFERING) {
                if (derivedGauge[index].equals(stringToCompare)) {
                    if (notifyMatch) {
                        type = STRING_TO_COMPARE_VALUE_MATCHED;
                        msg = "";
                        trigger = stringToCompare;
                    }
                    status[index] = DIFFERING;
                } else {
                    if (notifyDiffer) {
                        type = STRING_TO_COMPARE_VALUE_DIFFERED;
                        msg = "";
                        trigger = stringToCompare;
                    }
                    status[index] = MATCHING;
                }
            } else {
                if (status[index] == MATCHING) {
                    if (derivedGauge[index].equals(stringToCompare)) {
                        if (notifyMatch) {
                            type = STRING_TO_COMPARE_VALUE_MATCHED;
                            msg = "";
                            trigger = stringToCompare;
                        }
                        status[index] = DIFFERING;
                    }
                } else if (status[index] == DIFFERING) {
                    if (!derivedGauge[index].equals(stringToCompare)) {
                        if (notifyDiffer) {
                            type = STRING_TO_COMPARE_VALUE_DIFFERED;
                            msg = "";
                            trigger = stringToCompare;
                        }
                        status[index] = MATCHING;
                    }
                }
            }
        }

        return new StatisticMonitorNotification(type,
                                       this,
                                       0,
                                       0,
                                       msg,
                                       null,
                                       null,
                                       null,
                                       trigger);
    }

    /**
     * This method is called when adding a new observed object in the vector.
     * It updates all the string specific arrays.
     * @param index The index of the observed object.
     */
    synchronized void insertSpecificElementAt(int index) {
        // Update status array.
        //
        if (elementCount >= status.length) {
            status = expandArray(status);
        }
        status[index] = MATCHING_OR_DIFFERING;
    }

    /**
     * This method is called when removing an observed object from the vector.
     * It updates all the string specific arrays.
     * @param index The index of the observed object.
     */
    synchronized void removeSpecificElementAt(int index) {
        // Update status array.
        //
        removeElementAt(status, index);
    }
}
