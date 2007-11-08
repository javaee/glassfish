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
 * GaugeStatisticMonitor.java	
 *
 * Created on July 11, 2005 3:00 PM
 */

package com.sun.enterprise.admin.selfmanagement.event;

import javax.management.MBeanNotificationInfo;
import javax.management.ObjectName;
import static com.sun.enterprise.admin.selfmanagement.event.StatisticMonitor.NumericalType.*;
import com.sun.appserv.management.event.StatisticMonitorNotification;
import static com.sun.appserv.management.event.StatisticMonitorNotification.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;

/**
 * Defines a monitor MBean designed to observe the values 
 * of a gauge attribute. Used for JDK version greater than 1.5.
 *
 * <P> A gauge statistic monitor observes an attribute that is continuously
 * variable with time. A gauge statistic monitor sends notifications as
 * follows:
 *
 * <UL>
 *
 * <LI> if the attribute value is increasing and becomes equal to or
 * greater than the high threshold value, a {@link
 * StatisticMonitorNotification#THRESHOLD_HIGH_VALUE_EXCEEDED threshold high
 * notification} is sent. The notify high flag must be set to
 * <CODE>true</CODE>.
 *
 * <BR>Subsequent crossings of the high threshold value do not cause
 * further notifications unless the attribute value becomes equal to
 * or less than the low threshold value.</LI>
 *
 * <LI> if the attribute value is decreasing and becomes equal to or
 * less than the low threshold value, a {@link
 * StatisticMonitorNotification#THRESHOLD_LOW_VALUE_EXCEEDED threshold low
 * notification} is sent. The notify low flag must be set to
 * <CODE>true</CODE>.
 *
 * <BR>Subsequent crossings of the low threshold value do not cause
 * further notifications unless the attribute value becomes equal to
 * or greater than the high threshold value.</LI>
 *
 * </UL>
 *
 * This provides a hysteresis mechanism to avoid repeated triggering
 * of notifications when the attribute value makes small oscillations
 * around the high or low threshold value.
 *
 * <P> If the gauge difference mode is used, the value of the derived
 * gauge is calculated as the difference between the observed gauge
 * values for two successive observations.
 *
 * <BR>The derived gauge value (V[t]) is calculated using the following method:
 * <UL>
 * <LI>V[t] = gauge[t] - gauge[t-GP]</LI>
 * </UL>
 *
 * This implementation of the gauge statistic monitor requires the observed
 * attribute to be of the type integer or floating-point
 * (<CODE>Byte</CODE>, <CODE>Integer</CODE>, <CODE>Short</CODE>,
 * <CODE>Long</CODE>, <CODE>Float</CODE>, <CODE>Double</CODE>).
 *
 * @author      Sun Microsystems, Inc
 */
public class GaugeStatisticMonitor extends StatisticMonitor implements GaugeStatisticMonitorMBean {

    /*
     * ------------------------------------------
     *  PRIVATE VARIABLES
     * ------------------------------------------
     */

    /**
     * Gauge high threshold.
     *
     * <BR>The default value is a null Integer object.
     */
    private Number highThreshold = INTEGER_ZERO;

    /**
     * Gauge low threshold.
     *
     * <BR>The default value is a null Integer object.
     */
    private Number lowThreshold = INTEGER_ZERO;

    /**
     * Flag indicating if the gauge statistic monitor notifies when exceeding
     * the high threshold.
     *
     * <BR>The default value is <CODE>false</CODE>.
     */
    private boolean notifyHigh = false;

    /**
     * Flag indicating if the gauge statistic monitor notifies when exceeding
     * the low threshold.
     *
     * <BR>The default value is <CODE>false</CODE>.
     */
    private boolean notifyLow = false;

    /**
     * Flag indicating if the gauge difference mode is used.  If the
     * gauge difference mode is used, the derived gauge is the
     * difference between two consecutive observed values.  Otherwise,
     * the derived gauge is directly the value of the observed
     * attribute.
     *
     * <BR>The default value is set to <CODE>false</CODE>.
     */
    private boolean differenceMode = false;

    /**
     * Scan gauge values captured by the previous observation.
     * <BR>Each element in this array corresponds to an observed
     * object in the list.
     */
    private Number previousScanGauge[] = new Number[capacityIncrement];

    /**
     * This attribute is used to handle the hysteresis mechanism.
     * <BR>Each element in this array corresponds to an observed
     * object in the list.
     */
    private int status[] = new int[capacityIncrement];

    /**
     * This attribute is used to keep the derived gauge type.
     * <BR>Each element in this array corresponds to an observed
     * object in the list.
     */
    private NumericalType type[] = new NumericalType[capacityIncrement];

    /**
     * Derived gauge valid.
     * <BR>Each element in this array corresponds to an observed
     * object in the list.
     */
    private boolean derivedGaugeValid[] = new boolean[capacityIncrement];

    private static final String[] types = {
        RUNTIME_ERROR,
        OBSERVED_OBJECT_ERROR,
        OBSERVED_ATTRIBUTE_ERROR,
        OBSERVED_ATTRIBUTE_TYPE_ERROR,
        THRESHOLD_ERROR,
        THRESHOLD_HIGH_VALUE_EXCEEDED,
        THRESHOLD_LOW_VALUE_EXCEEDED
    };

    private static final MBeanNotificationInfo[] notifsInfo = {
        new MBeanNotificationInfo(
            types,
            "com.sun.appserv.management.event.StatisticMonitorNotification",
            "Notifications sent by the GaugeStatisticMonitor MBean")
    };

    // Flags needed to implement the hysteresis mechanism.
    //
    private static final int RISING             = 0;
    private static final int FALLING            = 1;
    private static final int RISING_OR_FALLING  = 2;

    // LOGGER
    //---------------
    protected static final Logger _logger = LogDomains.getLogger(LogDomains.SELF_MANAGEMENT_LOGGER);


    /*
     * ------------------------------------------
     *  CONSTRUCTORS
     * ------------------------------------------
     */

    /**
     * Default constructor.
     */
    public GaugeStatisticMonitor() {
    }

    /*
     * ------------------------------------------
     *  PUBLIC METHODS
     * ------------------------------------------
     */

    /**
     * Starts the gauge statistic monitor.
     */
    public synchronized void start() {
        // Reset values.
        //
        for (int i = 0; i < elementCount; i++) {
            status[i] = RISING_OR_FALLING;
            previousScanGauge[i] = null;
        }
        doStart();
    }

    /**
     * Stops the gauge statistic monitor.
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
     * @param object the name of the MBean.
     *
     * @return The derived gauge of the specified object.
     *
     * @since.unbundled JMX 1.2
     */
    public synchronized Number getDerivedGauge(ObjectName object) {
        return (Number) super.getDerivedGauge(object);
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
    public synchronized Number getDerivedGauge() {
        return (Number) derivedGauge[0];
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
     * Gets the high threshold value common to all observed MBeans.
     *
     * @return The high threshold value.
     *
     * @see #setThresholds
     */
    public synchronized Number getHighThreshold() {
        return highThreshold;
    }

    /**
     * Gets the low threshold value common to all observed MBeans.
     *
     * @return The low threshold value.
     *
     * @see #setThresholds
     */
    public synchronized Number getLowThreshold() {
        return lowThreshold;
    }

    /**
     * Sets the high and the low threshold values common to all
     * observed MBeans.
     *
     * @param highValue The high threshold value.
     * @param lowValue The low threshold value.
     *
     * @exception IllegalArgumentException The specified high/low
     * threshold is null or the low threshold is greater than the high
     * threshold or the high threshold and the low threshold are not
     * of the same type.
     *
     * @see #getHighThreshold
     * @see #getLowThreshold
     */
    public synchronized void setThresholds(Number highValue, Number lowValue)
            throws IllegalArgumentException {

        if ((highValue == null) || (lowValue == null)) {
            throw new IllegalArgumentException("Null threshold value");
        }

        if (highValue.getClass() != lowValue.getClass()) {
            throw new IllegalArgumentException("Different type " +
                                               "threshold values");
        }

        if (isFirstStrictlyGreaterThanLast(lowValue, highValue,
                                           highValue.getClass().getName())) {
            throw new IllegalArgumentException("High threshold less than " +
                                               "low threshold");
        }

        highThreshold = highValue;
        lowThreshold = lowValue;

        for (int i = 0; i < elementCount; i++) {
            resetAlreadyNotified(i, THRESHOLD_ERROR_NOTIFIED);

            // Reset values.
            //
            status[i] = RISING_OR_FALLING;
        }
    }

    /**
     * Gets the high notification's on/off switch value common to all
     * observed MBeans.
     *
     * @return <CODE>true</CODE> if the gauge monitor notifies when
     * exceeding the high threshold, <CODE>false</CODE> otherwise.
     *
     * @see #setNotifyHigh
     */
    public synchronized boolean getNotifyHigh() {
        return notifyHigh;
    }

    /**
     * Sets the high notification's on/off switch value common to all
     * observed MBeans.
     *
     * @param value The high notification's on/off switch value.
     *
     * @see #getNotifyHigh
     */
    public synchronized void setNotifyHigh(boolean value) {
        notifyHigh = value;
    }

    /**
     * Gets the low notification's on/off switch value common to all
     * observed MBeans.
     *
     * @return <CODE>true</CODE> if the gauge monitor notifies when
     * exceeding the low threshold, <CODE>false</CODE> otherwise.
     *
     * @see #setNotifyLow
     */
    public synchronized boolean getNotifyLow() {
        return notifyLow;
    }

    /**
     * Sets the low notification's on/off switch value common to all
     * observed MBeans.
     *
     * @param value The low notification's on/off switch value.
     *
     * @see #getNotifyLow
     */
    public synchronized void setNotifyLow(boolean value) {
        notifyLow = value;
    }

    /**
     * Gets the difference mode flag value common to all observed MBeans.
     *
     * @return <CODE>true</CODE> if the difference mode is used,
     * <CODE>false</CODE> otherwise.
     *
     * @see #setDifferenceMode
     */
    public synchronized boolean getDifferenceMode() {
        return differenceMode;
    }

    /**
     * Sets the difference mode flag value common to all observed MBeans.
     *
     * @param value The difference mode flag value.
     *
     * @see #getDifferenceMode
     */
    public synchronized void setDifferenceMode(boolean value) {
        differenceMode = value;

        // Reset values.
        //
        for (int i = 0; i < elementCount; i++) {
            status[i] = RISING_OR_FALLING;
            previousScanGauge[i] = null;
        }
    }

   /**
     * Returns a <CODE>NotificationInfo</CODE> object containing the
     * name of the Java class of the notification and the notification
     * types sent by the gauge monitor.
     */
    public MBeanNotificationInfo[] getNotificationInfo() {
        return notifsInfo.clone();
    }

    /*
     * ------------------------------------------
     *  PRIVATE METHODS
     * ------------------------------------------
     */

    /**
     * Updates the derived gauge attribute of the observed object at
     * the specified index.
     *
     * @param scanGauge The value of the observed attribute.
     * @param index The index of the observed object.
     * @return <CODE>true</CODE> if the derived gauge value is valid,
     * <CODE>false</CODE> otherwise.  The derived gauge value is
     * invalid when the differenceMode flag is set to
     * <CODE>true</CODE> and it is the first notification (so we
     * haven't 2 consecutive values to update the derived gauge).
     */
    private synchronized boolean updateDerivedGauge(Object scanGauge,
                                                    int index) {

        boolean is_derived_gauge_valid;

        // The gauge difference mode is used.
        //
        if (differenceMode) {

            // The previous scan gauge has been initialized.
            //
            if (previousScanGauge[index] != null) {
                setDerivedGaugeWithDifference((Number)scanGauge, index);
                is_derived_gauge_valid = true;
            }
            // The previous scan gauge has not been initialized.
            // We cannot update the derived gauge...
            //
            else {
                is_derived_gauge_valid = false;
            }
            previousScanGauge[index] = (Number)scanGauge;
        }
        // The gauge difference mode is not used.
        //
        else {
            derivedGauge[index] = (Number)scanGauge;
            is_derived_gauge_valid = true;
        }

        return is_derived_gauge_valid;
    }

    /**
     * Updates the notification attribute of the observed object at the
     * specified index and notifies the listeners only once if the notify flag
     * is set to <CODE>true</CODE>.
     * @param index The index of the observed object.
     */
    private StatisticMonitorNotification updateNotifications(int index) {

        StatisticMonitorNotification n = null;

        // Send high notification if notifyHigh is true.
        // Send low notification if notifyLow is true.
        //
        synchronized(this) {
            if (status[index] == RISING_OR_FALLING) {
                if (isFirstGreaterThanLast((Number)derivedGauge[index],
                                           highThreshold,
                                           type[index])) {
                    if (notifyHigh) {
                        n = new StatisticMonitorNotification(
                                THRESHOLD_HIGH_VALUE_EXCEEDED,
                                this,
                                0,
                                0,
                                "",
                                null,
                                null,
                                null,
                                highThreshold);
                    }
                    status[index] = FALLING;
                } else if (isFirstGreaterThanLast(lowThreshold,
                                                  (Number)derivedGauge[index],
                                                  type[index])) {
                    if (notifyLow) {
                        n = new StatisticMonitorNotification(
                                THRESHOLD_LOW_VALUE_EXCEEDED,
                                this,
                                0,
                                0,
                                "",
                                null,
                                null,
                                null,
                                lowThreshold);
                    }
                    status[index] = RISING;
                }
            } else {
                if (status[index] == RISING) {
                    if (isFirstGreaterThanLast((Number)derivedGauge[index],
                                               highThreshold,
                                               type[index])) {
                        if (notifyHigh) {
                            n = new StatisticMonitorNotification(
                                    THRESHOLD_HIGH_VALUE_EXCEEDED,
                                    this,
                                    0,
                                    0,
                                    "",
                                    null,
                                    null,
                                    null,
                                    highThreshold);
                        }
                        status[index] = FALLING;
                    }
                } else if (status[index] == FALLING) {
                    if (isFirstGreaterThanLast(lowThreshold,
                                               (Number)derivedGauge[index],
                                               type[index])) {
                        if (notifyLow) {
                            n = new StatisticMonitorNotification(
                                    THRESHOLD_LOW_VALUE_EXCEEDED,
                                    this,
                                    0,
                                    0,
                                    "",
                                    null,
                                    null,
                                    null,
                                    lowThreshold);
                        }
                        status[index] = RISING;
                    }
                }
            }
        }    

        return n;
    }

    /**
     * Sets the derived gauge when the differenceMode flag is set to
     * <CODE>true</CODE>.  Both integer and floating-point types are
     * allowed.
     *
     * @param scanGauge The value of the observed attribute.
     * @param index The index of the observed object.
     */
    private synchronized void setDerivedGaugeWithDifference(Number scanGauge,
                                                            int index) {
        Number prev = previousScanGauge[index];
        Number der;
        switch (type[index]) {
        case INTEGER:
            der = new Integer(((Integer)scanGauge).intValue() -
                              ((Integer)prev).intValue());
            break;
        case BYTE:
            der = new Byte((byte)(((Byte)scanGauge).byteValue() -
                                  ((Byte)prev).byteValue()));
            break;
        case SHORT:
            der = new Short((short)(((Short)scanGauge).shortValue() -
                                    ((Short)prev).shortValue()));
            break;
        case LONG:
            der = new Long(((Long)scanGauge).longValue() -
                           ((Long)prev).longValue());
            break;
        case FLOAT:
            der = new Float(((Float)scanGauge).floatValue() -
                            ((Float)prev).floatValue());
            break;
        case DOUBLE:
            der = new Double(((Double)scanGauge).doubleValue() -
                             ((Double)prev).doubleValue());
            break;
        default:
            // Should never occur...
            if ( _logger.isLoggable(Level.WARNING) )
                _logger.log(Level.WARNING,"The threshold type is invalid");
            return;
        }
        derivedGauge[index] = der;
    }

    /**
     * Tests if the first specified Number is greater than or equal to
     * the last.  Both integer and floating-point types are allowed.
     *
     * @param greater The first Number to compare with the second.
     * @param less The second Number to compare with the first.
     * @param type The number type.
     * @return <CODE>true</CODE> if the first specified Number is
     * greater than or equal to the last, <CODE>false</CODE>
     * otherwise.
     */
    private boolean isFirstGreaterThanLast(Number greater,
                                           Number less,
                                           NumericalType type) {

        switch(type) {
        case INTEGER:
        case BYTE:
        case SHORT:
        case LONG:
            return (greater.longValue() >= less.longValue());
        case FLOAT:
        case DOUBLE:
            return (greater.doubleValue() >= less.doubleValue());
        default:
            // Should never occur...
            if ( _logger.isLoggable(Level.WARNING) )
                _logger.log(Level.WARNING,"The threshold type is invalid");
            return false;
        }
    }

    /**
     * Tests if the first specified Number is strictly greater than the last.
     * Both integer and floating-point types are allowed.
     *
     * @param greater The first Number to compare with the second.
     * @param less The second Number to compare with the first.
     * @param className The number class name.
     * @return <CODE>true</CODE> if the first specified Number is
     * strictly greater than the last, <CODE>false</CODE> otherwise.
     */
    private boolean isFirstStrictlyGreaterThanLast(Number greater,
                                                   Number less,
                                                   String className) {

        if (className.equals("java.lang.Integer") ||
            className.equals("java.lang.Byte") ||
            className.equals("java.lang.Short") ||
            className.equals("java.lang.Long")) {

            return (greater.longValue() > less.longValue());
        }
        else if (className.equals("java.lang.Float") ||
                 className.equals("java.lang.Double")) {

            return (greater.doubleValue() > less.doubleValue());
        }
        else {
            // Should never occur...
            if ( _logger.isLoggable(Level.WARNING) )
                _logger.log(Level.WARNING,"The threshold type is invalid");
            return false;
        }
    }

    /*
     * ------------------------------------------
     *  PACKAGE METHODS
     * ------------------------------------------
     */

    /**
     * This method globally sets the derived gauge type for the given
     * "object" and "attribute" after checking that the type of the
     * supplied observed attribute value is one of the value types
     * supported by this monitor.
     */
    boolean isComparableTypeValid(ObjectName object,
                                  String attribute,
                                  Comparable<?> value) {
        int index = indexOf(object);

        // Check that the observed attribute is either of type
        // "Integer" or "Float".
        //
        if (value instanceof Integer) {
            type[index] = INTEGER;
        } else if (value instanceof Byte) {
            type[index] = BYTE;
        } else if (value instanceof Short) {
            type[index] = SHORT;
        } else if (value instanceof Long) {
            type[index] = LONG;
        } else if (value instanceof Float) {
            type[index] = FLOAT;
        } else if (value instanceof Double) {
            type[index] = DOUBLE;
        } else {
            return false;
        }
        return true;
    }

    Comparable<?> getDerivedGaugeFromComparable(ObjectName object,
                                                String attribute,
                                                Comparable<?> value) {
        int index = indexOf(object);

        // Update the derived gauge attributes and check the
        // validity of the new value. The derived gauge value
        // is invalid when the differenceMode flag is set to
        // true and it is the first notification, i.e. we
        // haven't got 2 consecutive values to update the
        // derived gauge.
        //
        derivedGaugeValid[index] = updateDerivedGauge(value, index);

        return (Comparable<?>) derivedGauge[index];
    }

    void onErrorNotification(StatisticMonitorNotification notification) {
        int index = indexOf(notification.getObservedObject());
        synchronized(this) {
            // Reset values.
            //
            status[index] = RISING_OR_FALLING;
            previousScanGauge[index] = null;
        }
    }

    StatisticMonitorNotification buildAlarmNotification(ObjectName object,
                                               String attribute,
                                               Comparable<?> value) {
        int index = indexOf(object);
        
        // Notify the listeners if the updated derived
        // gauge value is valid.
        //
        StatisticMonitorNotification alarm = null;
        if (derivedGaugeValid[index])
            alarm = updateNotifications(index);
        return alarm;
    }

    /**
     * Tests if the threshold high and threshold low are both of the
     * same type as the gauge.  Both integer and floating-point types
     * are allowed.
     *
     * Note:
     *   If the optional lowThreshold or highThreshold have not been
     *   initialized, their default value is an Integer object with
     *   a value equal to zero.
     *
     * @param object The observed object.
     * @param attribute The observed attribute.
     * @param value The sample value.
     * @return <CODE>true</CODE> if type is the same,
     * <CODE>false</CODE> otherwise.
     */
    synchronized boolean isThresholdTypeValid(ObjectName object,
                                              String attribute,
                                              Comparable<?> value) {
        int index = indexOf(object);
        Class<? extends Number> c = classForType(type[index]);
        return (isValidForType(highThreshold, c) &&
                isValidForType(lowThreshold, c));
    }

    /**
     * This method is called when adding a new observed object in the vector.
     * It updates all the gauge specific arrays.
     * @param index The index of the observed object.
     */
    synchronized void insertSpecificElementAt(int index) {
        // Update previousScanGauge, status, type and derivedGaugeValid arrays.
        //
        if (elementCount >= previousScanGauge.length) {
            previousScanGauge = expandArray(previousScanGauge);
            status = expandArray(status);
            type = expandArray(type);
            derivedGaugeValid = expandArray(derivedGaugeValid);
        }
        previousScanGauge[index] = null;
        status[index] = RISING_OR_FALLING;
        type[index] = INTEGER;
        derivedGaugeValid[index] = false;
    }

    /**
     * This method is called when removing an observed object from the vector.
     * It updates all the gauge specific arrays.
     * @param index The index of the observed object.
     */
    synchronized void removeSpecificElementAt(int index) {
        // Update previousScanGauge, status, type and derivedGaugeValid arrays.
        //
        removeElementAt(previousScanGauge, index);
        removeElementAt(status, index);
        removeElementAt(type, index);
        removeElementAt(derivedGaugeValid, index);
    }
}
