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
 * CounterStatisticMonitor.java
 *
 * Created on July 11, 2005 3:00 PM 
 */

package com.sun.enterprise.admin.selfmanagement.event;

import javax.management.ObjectName;
import javax.management.MBeanNotificationInfo;
import static com.sun.enterprise.admin.selfmanagement.event.StatisticMonitor.NumericalType.*;
import com.sun.appserv.management.event.StatisticMonitorNotification;
import static com.sun.appserv.management.event.StatisticMonitorNotification.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;

/**
 * Defines a monitor MBean designed to observe the values of a counter
 * attribute, for JDK greater than 1.5.
 *
 * <P> A counter monitor sends a {@link
 * StatisticMonitorNotification#THRESHOLD_VALUE_EXCEEDED threshold
 * notification} when the value of the counter reaches or exceeds a
 * threshold known as the comparison level.  The notify flag must be
 * set to <CODE>true</CODE>.
 *
 * <P> In addition, an offset mechanism enables particular counting
 * intervals to be detected.  If the offset value is not zero,
 * whenever the threshold is triggered by the counter value reaching a
 * comparison level, that comparison level is incremented by the
 * offset value.  This is regarded as taking place instantaneously,
 * that is, before the count is incremented.  Thus, for each level,
 * the threshold triggers an event notification every time the count
 * increases by an interval equal to the offset value.
 *
 * <P> If the counter can wrap around its maximum value, the modulus
 * needs to be specified.  The modulus is the value at which the
 * counter is reset to zero.
 *
 * <P> If the counter difference mode is used, the value of the
 * derived gauge is calculated as the difference between the observed
 * counter values for two successive observations.  If this difference
 * is negative, the value of the derived gauge is incremented by the
 * value of the modulus.  The derived gauge value (V[t]) is calculated
 * using the following method:
 *
 * <UL>
 * <LI>if (counter[t] - counter[t-GP]) is positive then
 * V[t] = counter[t] - counter[t-GP]
 * <LI>if (counter[t] - counter[t-GP]) is negative then
 * V[t] = counter[t] - counter[t-GP] + MODULUS
 * </UL>
 *
 * This implementation of the counter monitor requires the observed
 * attribute to be of the type integer (<CODE>Byte</CODE>,
 * <CODE>Integer</CODE>, <CODE>Short</CODE>, <CODE>Long</CODE>).
 *
 * @author      Sun Microsystems, Inc
 */
public class CounterStatisticMonitor extends StatisticMonitor implements CounterStatisticMonitorMBean {

    /*
     * ------------------------------------------
     *  PRIVATE VARIABLES
     * ------------------------------------------
     */

    /**
     * Counter thresholds.
     * <BR>Each element in this array corresponds to an observed
     * object in the list.
     */
    private Number threshold[] = new Number[capacityIncrement];

    /**
     * Counter modulus.
     * <BR>The default value is a null Integer object.
     */
    private Number modulus = INTEGER_ZERO;

    /**
     * Counter offset.
     * <BR>The default value is a null Integer object.
     */
    private Number offset = INTEGER_ZERO;

    /**
     * Flag indicating if the counter monitor notifies when exceeding
     * the threshold.  The default value is set to
     * <CODE>false</CODE>.
     */
    private boolean notify = false;

    /**
     * Flag indicating if the counter difference mode is used.  If the
     * counter difference mode is used, the derived gauge is the
     * difference between two consecutive observed values.  Otherwise,
     * the derived gauge is directly the value of the observed
     * attribute.  The default value is set to <CODE>false</CODE>.
     */
    private boolean differenceMode = false;

    /**
     * Initial counter threshold.  This value is used to initialize
     * the threshold when a new object is added to the list and reset
     * the threshold to its initial value each time the counter
     * resets.
     */
    private Number initThreshold = INTEGER_ZERO;

    /**
     * Scan counter value captured by the previous observation.
     * <BR>Each element in this array corresponds to an observed
     * object in the list.
     */
    private Number previousScanCounter[] = new Number[capacityIncrement];

    /**
     * Flag indicating if the modulus has been exceeded by the
     * threshold.  This flag is used to reset the threshold once we
     * are sure that the counter has been resetted.
     * <BR>Each element in this array corresponds to an observed
     * object in the list.
     */
    private boolean modulusExceeded[] = new boolean[capacityIncrement];

    /**
     * Derived gauge captured when the modulus has been exceeded by
     * the threshold.  This value is used to check if the counter has
     * been resetted (in order to reset the threshold).
     * <BR>Each element in this array corresponds to an observed
     * object in the list.
     */
    private Number derivedGaugeExceeded[] = new Number[capacityIncrement];

    /**
     * Derived gauge valid.
     * <BR>Each element in this array corresponds to an observed
     * object in the list.
     */
    private boolean derivedGaugeValid[] = new boolean[capacityIncrement];

    /**
     * This flag is used to notify only once between two granularity
     * periods for a given comparison level.
     * <BR>Each element in this array corresponds to an observed
     * object in the list.
     */
    private boolean eventAlreadyNotified[] = new boolean[capacityIncrement];

    /**
     * This attribute is used to keep the derived gauge type.
     * <BR>Each element in this array corresponds to an observed
     * object in the list.
     */
    private NumericalType type[] = new NumericalType[capacityIncrement];

    private static final String[] types = {
        RUNTIME_ERROR,
        OBSERVED_OBJECT_ERROR,
        OBSERVED_ATTRIBUTE_ERROR,
        OBSERVED_ATTRIBUTE_TYPE_ERROR,
        THRESHOLD_ERROR,
        THRESHOLD_VALUE_EXCEEDED
    };

    private static final MBeanNotificationInfo[] notifsInfo = {
        new MBeanNotificationInfo(
            types,
            "com.sun.appserv.management.event.StatisticMonitorNotification",
            "Notifications sent by the CounterStatisticMonitor MBean")
    };


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
    public CounterStatisticMonitor() {
    }

    /*
     * ------------------------------------------
     *  PUBLIC METHODS
     * ------------------------------------------
     */

    /**
     * Allows the counter statistic monitor MBean to perform any operations it
     * needs before being unregistered by the MBean server.
     *
     * <P>Resets the threshold values.
     *
     * @exception Exception
     */
    public void preDeregister() throws Exception {

        // Stop the CounterStatisticMonitor.
        //
        super.preDeregister();
        if ( _logger.isLoggable(Level.FINER) )
             _logger.log(Level.FINER,"Reset the threshold values");


        // Reset values for serialization.
        //
        synchronized (this) {
            for (int i = 0; i < elementCount; i++) {
                threshold[i] = initThreshold;
            }
        }
    }

    /**
     * Starts the counter statistic monitor.
     */
    public synchronized void start() {
        // Reset values.
        //
        for (int i = 0; i < elementCount; i++) {
            threshold[i] = initThreshold;
            modulusExceeded[i] = false;
            eventAlreadyNotified[i] = false;
            previousScanCounter[i] = null;
        }
        doStart();
    }

    /**
     * Stops the counter statistic monitor.
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
     * @param object the name of the object whose derived gauge is to
     * be returned.
     *
     * @return The derived gauge of the specified object.
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
     */
    public synchronized long getDerivedGaugeTimeStamp(ObjectName object) {
        return super.getDerivedGaugeTimeStamp(object);
    }

    /**
     * Gets the current threshold value of the specified object, if
     * this object is contained in the set of observed MBeans, or
     * <code>null</code> otherwise.
     *
     * @param object the name of the object whose threshold is to be
     * returned.
     *
     * @return The threshold value of the specified object.
     */
    public synchronized Number getThreshold(ObjectName object) {
        int index = indexOf(object);
        if (index != -1)
            return threshold[index];
        else
            return null;
    }

    /**
     * Gets the initial threshold value common to all observed objects.
     *
     * @return The initial threshold.
     *
     * @see #setInitThreshold
     */
    public synchronized Number getInitThreshold() {
        return initThreshold;
    }

    /**
     * Sets the initial threshold value common to all observed objects.
     *
     * <BR>The current threshold of every object in the set of
     * observed MBeans is updated consequently.
     *
     * @param value The initial threshold value.
     *
     * @exception IllegalArgumentException The specified
     * threshold is null or the threshold value is less than zero.
     *
     * @see #getInitThreshold
     */
    public synchronized void setInitThreshold(Number value)
        throws IllegalArgumentException {

        if (value == null) {
            throw new IllegalArgumentException("Null threshold");
        }
        if (value.longValue() < 0L) {
            throw new IllegalArgumentException("Negative threshold");
        }

        initThreshold = value;

        for (int i = 0; i < elementCount; i++) {
            resetAlreadyNotified(i, THRESHOLD_ERROR_NOTIFIED);

            // Reset values.
            //
            threshold[i] = value;
            modulusExceeded[i] = false;
            eventAlreadyNotified[i] = false;
        }
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
     * Gets the threshold value of the first object in the set of
     * observed MBeans.
     *
     * @return The threshold value.
     *
     * @see #setThreshold
     *
     * @deprecated As of JMX 1.2, replaced by {@link #getThreshold(ObjectName)}
     */
    @Deprecated
    public synchronized Number getThreshold() {
        return threshold[0];
    }

    /**
     * Sets the initial threshold value.
     *
     * @param value The initial threshold value.
     *
     * @exception IllegalArgumentException The specified threshold is
     * null or the threshold value is less than zero.
     *
     * @see #getThreshold()
     *
     * @deprecated As of JMX 1.2, replaced by {@link #setInitThreshold}
     */
    @Deprecated
    public synchronized void setThreshold(Number value)
        throws IllegalArgumentException {
        setInitThreshold(value);
    }

    /**
     * Gets the offset value common to all observed MBeans.
     *
     * @return The offset value.
     *
     * @see #setOffset
     */
    public synchronized Number getOffset() {
        return offset;
    }

    /**
     * Sets the offset value common to all observed MBeans.
     *
     * @param value The offset value.
     *
     * @exception IllegalArgumentException The specified
     * offset is null or the offset value is less than zero.
     *
     * @see #getOffset
     */
    public synchronized void setOffset(Number value)
        throws IllegalArgumentException {

        if (value == null) {
            throw new IllegalArgumentException("Null offset");
        }
        if (value.longValue() < 0L) {
            throw new IllegalArgumentException("Negative offset");
        }

        offset = value;

        for (int i = 0; i < elementCount; i++) {
            resetAlreadyNotified(i, THRESHOLD_ERROR_NOTIFIED);
        }
    }

    /**
     * Gets the modulus value common to all observed MBeans.
     *
     * @see #setModulus
     *
     * @return The modulus value.
     */
    public synchronized Number getModulus() {
        return modulus;
    }

    /**
     * Sets the modulus value common to all observed MBeans.
     *
     * @param value The modulus value.
     *
     * @exception IllegalArgumentException The specified
     * modulus is null or the modulus value is less than zero.
     *
     * @see #getModulus
     */
    public synchronized void setModulus(Number value)
            throws IllegalArgumentException {

        if (value == null) {
            throw new IllegalArgumentException("Null modulus");
        }
        if (value.longValue() < 0L) {
            throw new IllegalArgumentException("Negative modulus");
        }

        modulus = value;

        for (int i = 0; i < elementCount; i++) {
            resetAlreadyNotified(i, THRESHOLD_ERROR_NOTIFIED);

            // Reset values.
            //
            modulusExceeded[i] = false;
        }
    }

    /**
     * Gets the notification's on/off switch value common to all
     * observed MBeans.
     *
     * @return <CODE>true</CODE> if the counter monitor notifies when
     * exceeding the threshold, <CODE>false</CODE> otherwise.
     *
     * @see #setNotify
     */
    public synchronized boolean getNotify() {
        return notify;
    }

    /**
     * Sets the notification's on/off switch value common to all
     * observed MBeans.
     *
     * @param value The notification's on/off switch value.
     *
     * @see #getNotify
     */
    public synchronized void setNotify(boolean value) {
        notify = value;
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

        for (int i = 0; i < elementCount; i++) {
            // Reset values.
            //
            threshold[i] = initThreshold;
            modulusExceeded[i] = false;
            eventAlreadyNotified[i] = false;
            previousScanCounter[i] = null;
        }
    }

    /**
     * Returns a <CODE>NotificationInfo</CODE> object containing the
     * name of the Java class of the notification and the notification
     * types sent by the counter monitor.
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
     * @param scanCounter The value of the observed attribute.
     * @param index The index of the observed object.
     * @return <CODE>true</CODE> if the derived gauge value is valid,
     * <CODE>false</CODE> otherwise.  The derived gauge value is
     * invalid when the differenceMode flag is set to
     * <CODE>true</CODE> and it is the first notification (so we
     * haven't 2 consecutive values to update the derived gauge).
     */
    private synchronized boolean updateDerivedGauge(Object scanCounter,
                                                    int index) {

        boolean is_derived_gauge_valid;

        // The counter difference mode is used.
        //
        if (differenceMode) {

            // The previous scan counter has been initialized.
            //
            if (previousScanCounter[index] != null) {
                setDerivedGaugeWithDifference((Number)scanCounter, null, index);

                // If derived gauge is negative it means that the
                // counter has wrapped around and the value of the
                // threshold needs to be reset to its initial value.
                //
                if (((Number)derivedGauge[index]).longValue() < 0L) {
                    if (modulus.longValue() > 0L) {
                        setDerivedGaugeWithDifference((Number)scanCounter,
                                                      (Number)modulus, index);
                    }
                    threshold[index] = initThreshold;
                    eventAlreadyNotified[index] = false;
                }
                is_derived_gauge_valid = true;
            }
            // The previous scan counter has not been initialized.
            // We cannot update the derived gauge...
            //
            else {
                is_derived_gauge_valid = false;
            }
            previousScanCounter[index] = (Number)scanCounter;
        }
        // The counter difference mode is not used.
        //
        else {
            derivedGauge[index] = (Number)scanCounter;
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

        synchronized(this) {
            // Send notification if notify is true.
            //
            if (!eventAlreadyNotified[index]) {
                if (((Number)derivedGauge[index]).longValue() >=
                    threshold[index].longValue()) {
                    if (notify) {
                        n = new StatisticMonitorNotification(THRESHOLD_VALUE_EXCEEDED,
                                                    this,
                                                    0,
                                                    0,
                                                    "",
                                                    null,
                                                    null,
                                                    null,
                                                    threshold[index]);
                    }
                    if (!differenceMode) {
                        eventAlreadyNotified[index] = true;
                    }
                }
            } else {
                if ( _logger.isLoggable(Level.INFO) ) {
                    _logger.log(Level.INFO,"The notification:" +
                          "\n\tNotification observed object = " +
                          getObservedObject(index) +
                          "\n\tNotification observed attribute = " +
                          getObservedAttribute() +
                          "\n\tNotification threshold level = " +
                          threshold[index] +
                          "\n\tNotification derived gauge = " +
                          derivedGauge[index] +
                          "\nhas already been sent");
                }
            }
        }

        return n;
    }

    /**
     * Updates the threshold attribute of the observed object at the
     * specified index.
     * @param index The index of the observed object.
     */
    private synchronized void updateThreshold(int index) {

        // Calculate the new threshold value if the threshold has been
        // exceeded and if the offset value is greater than zero.
        //
        if (((Number)derivedGauge[index]).longValue() >=
            threshold[index].longValue()) {

            if (offset.longValue() > 0L) {

                // Increment the threshold until its value is greater
                // than the one for the current derived gauge.
                //
                long threshold_value = threshold[index].longValue();
                while (((Number)derivedGauge[index]).longValue() >=
                       threshold_value) {
                    threshold_value += offset.longValue();
                }

                // Set threshold attribute.
                //
                switch(type[index]) {
                    case INTEGER:
                        threshold[index] = new Integer((int)threshold_value);
                        break;
                    case BYTE:
                        threshold[index] = new Byte((byte)threshold_value);
                        break;
                    case SHORT:
                        threshold[index] = new Short((short)threshold_value);
                        break;
                    case LONG:
                        threshold[index] = new Long((long)threshold_value);
                        break;
                    default:
                        // Should never occur...
                        if ( _logger.isLoggable(Level.WARNING) )
                            _logger.log(Level.WARNING,"Threshold Type is Invalid");
                        break;
                }

                // If the counter can wrap around when it reaches
                // its maximum and we are not dealing with counter
                // differences then we need to reset the threshold
                // to its initial value too.
                //
                if (!differenceMode) {
                    if (modulus.longValue() > 0L) {
                        if (threshold[index].longValue() >
                            modulus.longValue()) {
                            modulusExceeded[index] = true;
                            derivedGaugeExceeded[index] =
                                    (Number) derivedGauge[index];
                        }
                    }
                }

                // Threshold value has been modified so we can notify again.
                //
                eventAlreadyNotified[index] = false;
            } else {
                modulusExceeded[index] = true;
                derivedGaugeExceeded[index] = (Number) derivedGauge[index];
            }
        }
    }

    /**
     * Sets the derived gauge of the specified index when the
     * differenceMode flag is set to <CODE>true</CODE>.  Integer types
     * only are allowed.
     *
     * @param scanCounter The value of the observed attribute.
     * @param mod The counter modulus value.
     * @param index The index of the observed object.
     */
    private synchronized void setDerivedGaugeWithDifference(Number scanCounter,
                                                            Number mod,
                                                            int index) {
        /* We do the arithmetic using longs here even though the
           result may end up in a smaller type.  Since
           l == (byte)l (mod 256) for any long l,
           (byte) ((byte)l1 + (byte)l2) == (byte) (l1 + l2),
           and likewise for subtraction.  So it's the same as if
           we had done the arithmetic in the smaller type.*/

        long derived =
            scanCounter.longValue() - previousScanCounter[index].longValue();
        if (mod != null)
            derived += modulus.longValue();

        switch (type[index]) {
        case INTEGER: derivedGauge[index] = new Integer((int) derived); break;
        case BYTE: derivedGauge[index] = new Byte((byte) derived); break;
        case SHORT: derivedGauge[index] = new Short((short) derived); break;
        case LONG: derivedGauge[index] = new Long(derived); break;
        default:
            // Should never occur...
            if ( _logger.isLoggable(Level.WARNING) )
                _logger.log(Level.WARNING,"Threshold Type is Invalid");
            break;
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

        // Check that the observed attribute is of type "Integer".
        //
        if (value instanceof Integer) {
            type[index] = INTEGER;
        } else if (value instanceof Byte) {
            type[index] = BYTE;
        } else if (value instanceof Short) {
            type[index] = SHORT;
        } else if (value instanceof Long) {
            type[index] = LONG;
        } else {
            return false;
        }
        return true;
    }

    Comparable<?> getDerivedGaugeFromComparable(ObjectName object,
                                                String attribute,
                                                Comparable<?> value) {
        int index = indexOf(object);

        // Check if counter has wrapped around.
        //
        if (modulusExceeded[index]) {
            if (((Number)derivedGauge[index]).longValue() <
                derivedGaugeExceeded[index].longValue()) {
                    threshold[index] = initThreshold;
                    modulusExceeded[index] = false;
                    eventAlreadyNotified[index] = false;
            }
        }

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
            modulusExceeded[index] = false;
            eventAlreadyNotified[index] = false;
            previousScanCounter[index] = null;
        }
    }

    StatisticMonitorNotification buildAlarmNotification(ObjectName object,
                                               String attribute,
                                               Comparable<?> value) {
        int index = indexOf(object);

        // Notify the listeners and update the threshold if
        // the updated derived gauge value is valid.
        //
        StatisticMonitorNotification alarm = null;
        if (derivedGaugeValid[index]) {
            alarm = updateNotifications(index);
            updateThreshold(index);
        }
        return alarm;
    }

    /**
     * Tests if the threshold, offset and modulus of the specified index are
     * of the same type as the counter. Only integer types are allowed.
     *
     * Note:
     *   If the optional offset or modulus have not been initialized, their
     *   default value is an Integer object with a value equal to zero.
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
        return (c.isInstance(threshold[index]) &&
                isValidForType(offset, c) &&
                isValidForType(modulus, c));
    }

    /**
     * This method is called when adding a new observed object in the vector.
     * It updates all the counter specific arrays.
     * @param index The index of the observed object.
     */
    synchronized void insertSpecificElementAt(int index) {
        // Update threshold, previousScanCounter, derivedGaugeExceeded,
        // derivedGaugeValid, modulusExceeded, eventAlreadyNotified and
        // type arrays.
        //
        if (elementCount >= threshold.length) {
            threshold = expandArray(threshold);
            previousScanCounter = expandArray(previousScanCounter);
            derivedGaugeExceeded = expandArray(derivedGaugeExceeded);
            derivedGaugeValid = expandArray(derivedGaugeValid);
            modulusExceeded = expandArray(modulusExceeded);
            eventAlreadyNotified = expandArray(eventAlreadyNotified);
            type = expandArray(type);
        }
        threshold[index] = INTEGER_ZERO;
        previousScanCounter[index] = null;
        derivedGaugeExceeded[index] = null;
        derivedGaugeValid[index] = false;
        modulusExceeded[index] = false;
        eventAlreadyNotified[index] = false;
        type[index] = INTEGER;
    }

    /**
     * This method is called when removing an observed object from the vector.
     * It updates all the counter specific arrays.
     * @param index The index of the observed object.
     */
    synchronized void removeSpecificElementAt(int index) {
        // Update threshold, previousScanCounter, derivedGaugeExceeded,
        // derivedGaugeValid, modulusExceeded, eventAlreadyNotified and
        // type arrays.
        //
        removeElementAt(threshold, index);
        removeElementAt(previousScanCounter, index);
        removeElementAt(derivedGaugeExceeded, index);
        removeElementAt(derivedGaugeValid, index);
        removeElementAt(modulusExceeded, index);
        removeElementAt(eventAlreadyNotified, index);
        removeElementAt(type, index);
    }
}
