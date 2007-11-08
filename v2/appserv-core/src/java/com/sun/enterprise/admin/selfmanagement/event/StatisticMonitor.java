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
 * StatisticMonitor.java
 *
 * Created on July 11, 2005 3:00 PM
 */

package com.sun.enterprise.admin.selfmanagement.event;

import com.sun.jmx.mbeanserver.GetPropertyAction;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import com.sun.appserv.management.event.StatisticMonitorNotification;
import static com.sun.appserv.management.event.StatisticMonitorNotification.*;
import javax.management.openmbean.CompositeData;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;

/**
 * Defines the part common to all monitor MBeans.
 * A statistic monitor MBean monitors values of an attribute common 
 * to a set of observed MBeans. The observed attribute is monitored 
 * at intervals specified by the
 * granularity period. A gauge value (derived gauge) is derived from the values
 * of the observed attribute.
 * Used for JDK version greater than 1.5
 *
 * @author      Sun Microsystems, Inc
 */
public abstract class StatisticMonitor
    extends NotificationBroadcasterSupport
    implements StatisticMonitorMBean, MBeanRegistration {

    /*
     * ------------------------------------------
     *  PRIVATE VARIABLES
     * ------------------------------------------
     */

    /**
     * List of MBeans to which the attribute to observe belongs.
     */
    private List<ObjectName> observedObjects = new ArrayList<ObjectName>();

    /**
     * Attribute to observe.
     */
    private String observedAttribute;

    /**
     * Monitor granularity period (in milliseconds).
     * The default value is set to 10 seconds.
     */
    private long granularityPeriod = 10000;

    /**
     * Monitor state.
     * The default value is set to <CODE>false</CODE>.
     */
    private boolean isActive = false;

    /**
     * Monitor sequence number.
     * The default value is set to 0.
     */
    private long sequenceNumber = 0;

    /**
     * Complex type attribute flag.
     * The default value is set to <CODE>false</CODE>.
     */
    private boolean isComplexTypeAttribute = false;

    /**
     * First attribute name extracted from complex type attribute name.
     */
    private String firstAttribute;

    /**
     * Remaining attribute names extracted from complex type attribute name.
     */
    private List<String> remainingAttributes = new ArrayList<String>();

    /**
     * AccessControlContext of the Monitor.start() caller.
     */
    private AccessControlContext acc;

    /**
     * Scheduler Service.
     */
    private static final ScheduledExecutorService scheduler =
        Executors.newSingleThreadScheduledExecutor(
            new DaemonThreadFactory("Scheduler"));

    /**
     * Maximum Pool Size
     */
    private static final int maximumPoolSize;

    /**
     * Executor Service.
     */
    private static final ExecutorService executor;

    // LOGGER
    //---------------
    protected static final Logger _logger = LogDomains.getLogger(LogDomains.SELF_MANAGEMENT_LOGGER);

    static {
        final String maximumPoolSizeSysProp = "jmx.x.monitor.maximum.pool.size";
        final String maximumPoolSizeStr = (String) AccessController.doPrivileged(
            new GetPropertyAction(maximumPoolSizeSysProp));
        if (maximumPoolSizeStr == null ||
            maximumPoolSizeStr.trim().length() == 0) {
            maximumPoolSize = 10;
        } else {
            int maximumPoolSizeTmp = 10;
            try {
                maximumPoolSizeTmp = Integer.parseInt(maximumPoolSizeStr);
            } catch (NumberFormatException e) {            
                if ( _logger.isLoggable(Level.WARNING) ) {
                    _logger.log(Level.WARNING,"Wrong value for " + maximumPoolSizeSysProp + " system property: " + e);
                    _logger.log(Level.WARNING,maximumPoolSizeSysProp + " defaults to 10.");
                }
                    maximumPoolSizeTmp = 10;
            }
            if (maximumPoolSizeTmp < 1) {
                maximumPoolSize = 1;
            } else {
                maximumPoolSize = maximumPoolSizeTmp;
            }
        }
        executor = new ThreadPoolExecutor(
                maximumPoolSize,
                maximumPoolSize,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new DaemonThreadFactory("Executor"));
//        See bug 6255405: JMX monitors should remove idle threads
//        ((ThreadPoolExecutor)executor).allowCoreThreadTimeOut(true);
    }

    /**
     * Monitor task to be executed by the Executor Service.
     */
    private MonitorTask monitorTask = new MonitorTask();

    /**
     * Scheduler task to be executed by the Scheduler Service.
     */
    private Runnable schedulerTask = new SchedulerTask(monitorTask);

    /**
     * ScheduledFuture associated to the current scheduler task.
     */
    private ScheduledFuture<?> schedulerFuture;

    /*
     * ------------------------------------------
     *  PROTECTED VARIABLES
     * ------------------------------------------
     */

    /**
     * The amount by which the capacity of the monitor arrays are
     * automatically incremented when their size becomes greater than
     * their capacity.
     */
    protected final static int capacityIncrement = 16;

    /**
     * The number of valid components in the vector of observed objects.
     *
     * @since.unbundled JMX 1.2
     */
    protected int elementCount = 0;

    /**
     * Monitor errors that have already been notified.
     * @deprecated equivalent to {@link #alreadyNotifieds}[0].
     */
    @Deprecated
    protected int alreadyNotified = 0;

    /**
     * <p>Selected monitor errors that have already been notified.</p>
     *
     * <p>Each element in this array corresponds to an observed object
     * in the vector.  It contains a bit mask of the flags {@link
     * #OBSERVED_OBJECT_ERROR_NOTIFIED} etc, indicating whether the
     * corresponding notification has already been sent for the MBean
     * being monitored.</p>
     *
     * @since.unbundled JMX 1.2
     */
    protected int alreadyNotifieds[] = new int[capacityIncrement];

    /**
     * Reference to the MBean server.  This reference is null when the
     * monitor MBean is not registered in an MBean server.  This
     * reference is initialized before the monitor MBean is registered
     * in the MBean server.
     * @see #preRegister(MBeanServer server, ObjectName name)
     */
    protected MBeanServer server;

    // Flags defining possible monitor errors.
    //

    /**
     * This flag is used to reset the {@link #alreadyNotifieds
     * alreadyNotifieds} monitor attribute.
     */
    protected static final int RESET_FLAGS_ALREADY_NOTIFIED             = 0;

    /**
     * Flag denoting that a notification has occurred after changing
     * the observed object.  This flag is used to check that the new
     * observed object is registered in the MBean server at the time
     * of the first notification.
     */
    protected static final int OBSERVED_OBJECT_ERROR_NOTIFIED           = 1;

    /**
     * Flag denoting that a notification has occurred after changing
     * the observed attribute.  This flag is used to check that the
     * new observed attribute belongs to the observed object at the
     * time of the first notification.
     */
    protected static final int OBSERVED_ATTRIBUTE_ERROR_NOTIFIED        = 2;

    /**
     * Flag denoting that a notification has occurred after changing
     * the observed object or the observed attribute.  This flag is
     * used to check that the observed attribute type is correct
     * (depending on the monitor in use) at the time of the first
     * notification.
     */
    protected static final int OBSERVED_ATTRIBUTE_TYPE_ERROR_NOTIFIED   = 4;

    /**
     * Flag denoting that a notification has occurred after changing
     * the observed object or the observed attribute.  This flag is
     * used to notify any exception (except the cases described above)
     * when trying to get the value of the observed attribute at the
     * time of the first notification.
     */
    protected static final int RUNTIME_ERROR_NOTIFIED                   = 8;

    /*
     * ------------------------------------------
     *  PACKAGE VARIABLES
     * ------------------------------------------
     */

    /**
     * Flag denoting that a notification has occured after changing
     * the threshold. This flag is used to notify any exception
     * related to invalid thresholds settings.
     */
    static final int THRESHOLD_ERROR_NOTIFIED                           = 16;

    /**
     * Derived gauges.
     *
     * <BR>Each element in this array corresponds to an observed
     * object in the list.
     */
    Object derivedGauge[] = new Object[capacityIncrement];

    /**
     * Derived gauges' timestamps.
     *
     * <BR>Each element in this array corresponds to an observed
     * object in the list.
     */
    long derivedGaugeTimestamp[] = new long[capacityIncrement];

    /**
     * Enumeration used to keep trace of the derived gauge type
     * in counter and gauge monitors.
     */
    enum NumericalType { BYTE, SHORT, INTEGER, LONG, FLOAT, DOUBLE };

    /**
     * Constant used to initialize all the numeric values.
     */
    static final Integer INTEGER_ZERO = new Integer(0);

    /*
     * ------------------------------------------
     *  PUBLIC METHODS
     * ------------------------------------------
     */

    /**
     * Allows the statistic monitor MBean to perform any operations it needs
     * before being registered in the MBean server.
     * <P>
     * Initializes the reference to the MBean server.
     *
     * @param server The MBean server in which the statistic monitor MBean will
     * be registered.
     * @param name The object name of the statistic monitor MBean.
     *
     * @return The name of the statistic monitor MBean registered.
     *
     * @exception Exception
     */
    public ObjectName preRegister(MBeanServer server, ObjectName name)
        throws Exception {
        if ( _logger.isLoggable(Level.FINER) )
             _logger.log(Level.FINER,"Initialize reference on the MBean Server");
        this.server = server;
        return name;
    }

    /**
     * Allows the statistic monitor MBean to perform any operations needed after
     * having been registered in the MBean server or after the
     * registration has failed.
     * <P>
     * Not used in this context.
     */
    public void postRegister (Boolean registrationDone) {
    }

    /**
     * Allows the statistic monitor MBean to perform any operations it needs
     * before being unregistered by the MBean server.
     * <P>
     * Stops the statistic monitor.
     *
     * @exception Exception
     */
    public void preDeregister() throws Exception {
        if ( _logger.isLoggable(Level.FINER) )
             _logger.log(Level.FINER,"Stop the statistic monitor");
        // Stop the StatisticMonitor.
        //
        stop();
    }

    /**
     * Allows the statistic monitor MBean to perform any operations needed after
     * having been unregistered by the MBean server.
     * <P>
     * Not used in this context.
     */
    public void postDeregister() {
    }

    /**
     * Starts the statistic monitor.
     */
    public abstract void start();

    /**
     * Stops the statistic monitor.
     */
    public abstract void stop();

    // GETTERS AND SETTERS
    //--------------------

    /**
     * Returns the object name of the first object in the set of observed
     * MBeans, or <code>null</code> if there is no such object.
     *
     * @return The object being observed.
     *
     * @see #setObservedObject(ObjectName)
     *
     * @deprecated As of JMX 1.2, replaced by {@link #getObservedObjects}
     */
    @Deprecated
    public synchronized ObjectName getObservedObject() {
        if (observedObjects.isEmpty()) {
            return null;
        } else {
            return observedObjects.get(0);
        }
    }

    /**
     * Removes all objects from the set of observed objects, and then adds the
     * specified object.
     *
     * @param object The object to observe.
     * @exception IllegalArgumentException The specified
     * object is null.
     *
     * @see #getObservedObject()
     *
     * @deprecated As of JMX 1.2, replaced by {@link #addObservedObject}
     */
    @Deprecated
    public synchronized void setObservedObject(ObjectName object)
        throws IllegalArgumentException {
        while (!observedObjects.isEmpty()) {
            removeObservedObject(observedObjects.get(0));
        }
        addObservedObject(object);
    }

    /**
     * Adds the specified object in the set of observed MBeans, if this object
     * is not already present.
     *
     * @param object The object to observe.
     * @exception IllegalArgumentException The specified object is null.
     *
     * @since.unbundled JMX 1.2
     */
    public synchronized void addObservedObject(ObjectName object)
        throws IllegalArgumentException {

        if (object == null) {
            throw new IllegalArgumentException("Null observed object");
        }

        // Check that the specified object is not already contained.
        //
        if (observedObjects.contains(object)) {
            return;
        }

        // Add the specified object in the list.
        //
        observedObjects.add(object);

        // Update arrays.
        //
        if (elementCount >= alreadyNotifieds.length) {
            alreadyNotifieds = expandArray(alreadyNotifieds);
            derivedGauge = expandArray(derivedGauge);
            derivedGaugeTimestamp = expandArray(derivedGaugeTimestamp);
        }
        alreadyNotifieds[elementCount] = RESET_FLAGS_ALREADY_NOTIFIED;
        updateDeprecatedAlreadyNotified();
        derivedGauge[elementCount] = null;
        derivedGaugeTimestamp[elementCount] = System.currentTimeMillis();

        // Update other specific arrays.
        //
        insertSpecificElementAt(elementCount);

        // Update elementCount.
        //
        elementCount++;
    }

    /**
     * Removes the specified object from the set of observed MBeans.
     *
     * @param object The object to remove.
     *
     * @since.unbundled JMX 1.2
     */
    public synchronized void removeObservedObject(ObjectName object) {
        // Check for null object.
        //
        if (object == null)
            return;

        int index = observedObjects.indexOf(object);
        if (index >= 0) {
            observedObjects.remove(index);
            
            // Update arrays.
            //
            removeElementAt(alreadyNotifieds, index);
            updateDeprecatedAlreadyNotified();
            removeElementAt(derivedGauge, index);
            removeElementAt(derivedGaugeTimestamp, index);
            
            // Update other specific arrays.
            //
            removeSpecificElementAt(index);
            
            // Update elementCount.
            //
            elementCount--;
        }
    }

    /**
     * Tests whether the specified object is in the set of observed MBeans.
     *
     * @param object The object to check.
     * @return <CODE>true</CODE> if the specified object is present,
     * <CODE>false</CODE> otherwise.
     *
     * @since.unbundled JMX 1.2
     */
    public synchronized boolean containsObservedObject(ObjectName object) {
        return observedObjects.contains(object);
    }

    /**
     * Returns an array containing the objects being observed.
     *
     * @return The objects being observed.
     *
     * @since.unbundled JMX 1.2
     */
    public synchronized ObjectName[] getObservedObjects() {
        return observedObjects.toArray(new ObjectName[0]);
    }

    /**
     * Gets the attribute being observed.
     * <BR>The observed attribute is not initialized by default (set to null).
     *
     * @return The attribute being observed.
     *
     * @see #setObservedAttribute
     */
    public String getObservedAttribute() {
        return observedAttribute;
    }

    /**
     * Sets the attribute to observe.
     * <BR>The observed attribute is not initialized by default (set to null).
     *
     * @param attribute The attribute to observe.
     * @exception IllegalArgumentException The specified
     * attribute is null.
     *
     * @see #getObservedAttribute
     */
    public void setObservedAttribute(String attribute)
        throws IllegalArgumentException {

        if (attribute == null) {
            throw new IllegalArgumentException("Null observed attribute");
        }

        // Update alreadyNotified array.
        //
        synchronized(this) {
            observedAttribute = attribute;

            // Reset the complex type attribute information
            // such that it is recalculated again.
            //
            firstAttribute = null;
            remainingAttributes.clear();
            isComplexTypeAttribute = false;

            for (int i = 0; i < elementCount; i++) {
                resetAlreadyNotified(i,
                                     OBSERVED_ATTRIBUTE_ERROR_NOTIFIED |
                                     OBSERVED_ATTRIBUTE_TYPE_ERROR_NOTIFIED);
            }
        }
    }

    /**
     * Gets the granularity period (in milliseconds).
     * <BR>The default value of the granularity period is 10 seconds.
     *
     * @return The granularity period value.
     *
     * @see #setGranularityPeriod
     */
    public synchronized long getGranularityPeriod() {
        return granularityPeriod;
    }

    /**
     * Sets the granularity period (in milliseconds).
     * <BR>The default value of the granularity period is 10 seconds.
     *
     * @param period The granularity period value.
     * @exception IllegalArgumentException The granularity
     * period is less than or equal to zero.
     *
     * @see #getGranularityPeriod
     */
    public synchronized void setGranularityPeriod(long period)
            throws IllegalArgumentException {

        if (period <= 0) {
            throw new IllegalArgumentException("Nonpositive granularity " +
                                               "period");
        }
        granularityPeriod = period;
        
        // Reschedule the scheduler task if the statistic monitor is active.
        //
        if (isActive()) {
            schedulerFuture.cancel(false);
            schedulerFuture = scheduler.schedule(schedulerTask,
                                                 period,
                                                 TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Tests whether the statistic monitor MBean is active.  A statistic monitor MBean is
     * marked active when the {@link #start start} method is called.
     * It becomes inactive when the {@link #stop stop} method is
     * called.
     *
     * @return <CODE>true</CODE> if the statistic monitor MBean is active,
     * <CODE>false</CODE> otherwise.
     */
    /* This method must be synchronized so that the monitoring thread will
       correctly see modifications to the isActive variable. See the MonitorTask
       action executed by the Scheduled Executor Service. */
    public synchronized boolean isActive() {
        return isActive;
    }

    /*
     * ------------------------------------------
     *  PACKAGE METHODS
     * ------------------------------------------
     */

    /**
     * Starts the statistic monitor.
     */
    void doStart() {
        if ( _logger.isLoggable(Level.FINER) )
             _logger.log(Level.FINER,"Start the statistic monitor");

        synchronized(this) {
            if (isActive()) {
                if ( _logger.isLoggable(Level.WARNING) )
                    _logger.log(Level.WARNING,"The StatisticMonitor is already active");
                return;
            }

            isActive = true;

            // Reset the complex type attribute information
            // such that it is recalculated again.
            //
            firstAttribute = null;
            remainingAttributes.clear();
            isComplexTypeAttribute = false;

            // Cache the AccessControlContext of the Monitor.start() caller.
            // The monitor tasks will be executed within this context.
            //
            acc = AccessController.getContext();

            // Start the scheduler.
            //
            schedulerFuture = scheduler.schedule(schedulerTask,
                                                 getGranularityPeriod(),
                                                 TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Stops the statistic monitor.
     */
    void doStop() {
        if ( _logger.isLoggable(Level.FINER) )
             _logger.log(Level.FINER,"Stop the StatisticMonitor");

        synchronized(this) {
            if (!isActive()) {
                if ( _logger.isLoggable(Level.WARNING) )
                    _logger.log(Level.WARNING,"StatisticMonitor is not active");
                return;
            }

            isActive = false;

            // Cancel the scheduler task associated with the scheduler.
            //
            schedulerFuture.cancel(false);

            // Reset the AccessControlContext.
            //
            acc = null;

            // Reset the complex type attribute information
            // such that it is recalculated again.
            //
            firstAttribute = null;
            remainingAttributes.clear();
            isComplexTypeAttribute = false;
        }
    }

    /**
     * Gets the derived gauge of the specified object, if this object is
     * contained in the set of observed MBeans, or <code>null</code> otherwise.
     *
     * @param object the name of the object whose derived gauge is to
     * be returned.
     *
     * @return The derived gauge of the specified object.
     *
     * @since 6.0
     */
    synchronized Object getDerivedGauge(ObjectName object) {
        int index = indexOf(object);
        if (index != -1) {
            return derivedGauge[index];
        }
        else
            return null;
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
    synchronized long getDerivedGaugeTimeStamp(ObjectName object) {
        int index = indexOf(object);
        if (index != -1)
            return derivedGaugeTimestamp[index];
        else
            return 0;
    }

    Object getAttribute(MBeanServerConnection mbsc,
                        ObjectName object,
                        String attribute)
        throws AttributeNotFoundException,
               InstanceNotFoundException,
               MBeanException,
               ReflectionException,
               IOException {
        // Check for complex type attribute
        //
        if (firstAttribute == null) {
            if (attribute.indexOf('.') != -1) {
                MBeanInfo mbi;
                try {
                    mbi = mbsc.getMBeanInfo(object);
                } catch (IntrospectionException e) {
                    throw new IllegalArgumentException(e);
                }
                MBeanAttributeInfo mbaiArray[] = mbi.getAttributes();
                for (MBeanAttributeInfo mbai : mbaiArray) {
                    if (attribute.equals(mbai.getName())) {
                        firstAttribute = attribute;
                        break;
                    }
                }
                if (firstAttribute == null) {
                    String tokens[] = attribute.split("\\.", -1);
                    firstAttribute = tokens[0];
                    for (int i = 1; i < tokens.length; i++)
                        remainingAttributes.add(tokens[i]);
                    isComplexTypeAttribute = true;
                }
            } else {
                firstAttribute = attribute;
            }
        }
        return mbsc.getAttribute(object, firstAttribute);
    }

    Comparable<?> getComparableFromAttribute(ObjectName object,
                                             String attribute,
                                             Object value)
        throws AttributeNotFoundException {
        if (isComplexTypeAttribute) {
            Object v = value;
            for (String attr : remainingAttributes)
                v = introspect(object, attr, v);
            return (Comparable<?>) v;
        } else {
            return (Comparable<?>) value;
        }
    }

    Object introspect(ObjectName object,
                      String attribute,
                      Object value)
        throws AttributeNotFoundException {
        try {
            if (value.getClass().isArray() && attribute.equals("length")) {
                return Array.getLength(value);
            } else if (value instanceof CompositeData) {
                return ((CompositeData) value).get(attribute);
            } else {
                // Java Beans introspection
                //
                BeanInfo bi = Introspector.getBeanInfo(value.getClass());
                PropertyDescriptor[] pds = bi.getPropertyDescriptors();
                for (PropertyDescriptor pd : pds)
                    if (pd.getName().equals(attribute)) {
                        return pd.getReadMethod().invoke(value);
                    }
                throw new AttributeNotFoundException(
                    "Could not find the getter method for the property " +
                    attribute + " using the Java Beans introspector");
            }
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        } catch (AttributeNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw (AttributeNotFoundException)
                new AttributeNotFoundException(e.getMessage()).initCause(e);
        }
    }

    boolean isComparableTypeValid(ObjectName object,
                                  String attribute,
                                  Comparable<?> value) {
        return true;
    }

    String buildErrorNotification(ObjectName object,
                                  String attribute,
                                  Comparable<?> value) {
        return null;
    }

    void onErrorNotification(StatisticMonitorNotification notification) {
    }

    Comparable<?> getDerivedGaugeFromComparable(ObjectName object,
                                                String attribute,
                                                Comparable<?> value) {
        return (Comparable<?>) value;
    }

    StatisticMonitorNotification buildAlarmNotification(ObjectName object,
                                               String attribute,
                                               Comparable<?> value){
        return null;
    }

    boolean isThresholdTypeValid(ObjectName object,
                                 String attribute,
                                 Comparable<?> value) {
        return true;
    }

    static Class<? extends Number> classForType(NumericalType type) {
        switch (type) {
            case BYTE:
                return Byte.class;
            case SHORT:
                return Short.class;
            case INTEGER:
                return Integer.class;
            case LONG:
                return Long.class;
            case FLOAT:
                return Float.class;
            case DOUBLE:
                return Double.class;
            default:
                throw new IllegalArgumentException(
                    "Unsupported numerical type");
        }
    }

    static boolean isValidForType(Object value, Class<? extends Number> c) {
        return ((value == INTEGER_ZERO) || c.isInstance(value));
    }

    /**
     * Gets the {@link ObjectName} of the object at the specified
     * index in the list of observed MBeans.
     * @return The observed object at the specified index.
     * @exception ArrayIndexOutOfBoundsException If the index is invalid.
     */
    synchronized ObjectName getObservedObject(int index)
        throws ArrayIndexOutOfBoundsException {
        return observedObjects.get(index);
    }

    /**
     * Update the deprecated {@link #alreadyNotified} field.
     */
    synchronized void updateDeprecatedAlreadyNotified() {
        if (elementCount > 0)
            alreadyNotified = alreadyNotifieds[0];
        else
            alreadyNotified = 0;
    }

    /**
     * Set the given bits in the given element of {@link #alreadyNotifieds}.
     * Ensure the deprecated {@link #alreadyNotified} field is updated
     * if appropriate.
     */
    synchronized void setAlreadyNotified(int index, int mask) {
        alreadyNotifieds[index] |= mask;
        if (index == 0)
            updateDeprecatedAlreadyNotified();
    }

    /**
     * Reset the given bits in the given element of {@link #alreadyNotifieds}.
     * Ensure the deprecated {@link #alreadyNotified} field is updated
     * if appropriate.
     */
    synchronized void resetAlreadyNotified(int index, int mask) {
        alreadyNotifieds[index] &= ~mask;
        if (index == 0)
            updateDeprecatedAlreadyNotified();
    }

    synchronized boolean alreadyNotified(int index, int mask) {
        return ((alreadyNotifieds[index] & mask) != 0);
    }

    /**
     * Reset all bits in the given element of {@link #alreadyNotifieds}.
     * Ensure the deprecated {@link #alreadyNotified} field is updated
     * if appropriate.
     */
    synchronized void resetAllAlreadyNotified(int index) {
        alreadyNotifieds[index] = RESET_FLAGS_ALREADY_NOTIFIED;
        if (index == 0)
            updateDeprecatedAlreadyNotified();
    }

    /**
     * Expands the specified int array.
     */
    int[] expandArray(int[] array) {
        int[] newArray = new int[array.length + capacityIncrement];
        System.arraycopy(array, 0, newArray, 0, array.length);
        return newArray;
    }

    /**
     * Expands the specified long array.
     */
    long[] expandArray(long[] array) {
        long[] newArray = new long[array.length + capacityIncrement];
        System.arraycopy(array, 0, newArray, 0, array.length);
        return newArray;
    }

    /**
     * Expands the specified boolean array.
     */
    boolean[] expandArray(boolean[] array) {
        boolean[] newArray = new boolean[array.length + capacityIncrement];
        System.arraycopy(array, 0, newArray, 0, array.length);
        return newArray;
    }

    /**
     * Expands the specified Number array.
     */
    Number[] expandArray(Number[] array) {
        Number[] newArray = new Number[array.length + capacityIncrement];
        System.arraycopy(array, 0, newArray, 0, array.length);
        return newArray;
    }

    /**
     * Expands the specified String array.
     */
    String[] expandArray(String[] array) {
        String[] newArray = new String[array.length + capacityIncrement];
        System.arraycopy(array, 0, newArray, 0, array.length);
        return newArray;
    }

    /**
     * Expands the specified NumericalType array.
     */
    NumericalType[] expandArray(NumericalType[] array) {
        NumericalType[] newArray =
            new NumericalType[array.length + capacityIncrement];
        System.arraycopy(array, 0, newArray, 0, array.length);
        return newArray;
    }

    /**
     * Expands the specified Object array.
     */
    Object[] expandArray(Object[] array) {
        Object[] newArray = new Object[array.length + capacityIncrement];
        System.arraycopy(array, 0, newArray, 0, array.length);
        return newArray;
    }

    /**
     * Removes the component at the specified index from the specified
     * int array.
     */
    synchronized void removeElementAt(int[] array, int index) {
        if (index < 0 || index >= elementCount)
            return;
        int j = elementCount - index - 1;
        if (j > 0) {
            System.arraycopy(array, index + 1, array, index, j);
        }
    }

    /**
     * Removes the component at the specified index from the specified
     * long array.
     */
    synchronized void removeElementAt(long[] array, int index) {
        if (index < 0 || index >= elementCount)
            return;
        int j = elementCount - index - 1;
        if (j > 0) {
            System.arraycopy(array, index + 1, array, index, j);
        }
    }

    /**
     * Removes the component at the specified index from the specified
     * boolean array.
     */
    synchronized void removeElementAt(boolean[] array, int index) {
        if (index < 0 || index >= elementCount)
            return;
        int j = elementCount - index - 1;
        if (j > 0) {
            System.arraycopy(array, index + 1, array, index, j);
        }
    }

    /**
     * Removes the component at the specified index from the specified
     * Object array.
     */
    synchronized void removeElementAt(Object[] array, int index) {
        if (index < 0 || index >= elementCount)
            return;
        int j = elementCount - index - 1;
        if (j > 0) {
            System.arraycopy(array, index + 1, array, index, j);
        }
        array[elementCount - 1] = null;
    }

    /**
     * Searches for the first occurence of the given argument, testing
     * for equality using the equals method.
     */
    synchronized int indexOf(ObjectName object) {
        return observedObjects.indexOf(object);
    }

    /**
     * This method is overridden by the specific monitor classes
     * (Counter, Gauge and String).  It updates all the specific
     * arrays after adding a new observed object in the list.
     *
     * The method is not abstract so that this class can be subclassed
     * by classes outside this package.
     */
    void insertSpecificElementAt(int index) {}

    /**
     * This method is overridden by the specific monitor classes
     * (Counter, Gauge and String).  It updates all the specific
     * arrays after removing an observed object from the vector.
     *
     * The method is not abstract so that this class can be subclassed
     * by classes outside this package.
     */
    void removeSpecificElementAt(int index) {}

    /*
     * ------------------------------------------
     *  PRIVATE METHODS
     * ------------------------------------------
     */

    /**
     * This method is used by the monitor MBean to create and send a
     * monitor notification to all the listeners registered for this
     * kind of notification.
     *
     * @param type The notification type.
     * @param timeStamp The notification emission date.
     * @param msg The notification message.
     * @param derGauge The derived gauge.
     * @param trigger The threshold/string (depending on the monitor
     * type) that triggered off the notification.
     * @param object The ObjectName of the observed object that triggered
     * off the notification.
     * @param onError Flag indicating if this monitor notification is
     * an error notification or an alarm notification.
     */
    private void sendNotification(String type, long timeStamp, String msg,
                                  Object derGauge, Object trigger,
                                  ObjectName object, boolean onError) {
        if ( _logger.isLoggable(Level.FINER) )
             _logger.log(Level.FINER,"send notification: " +
                  "\n\tNotification observed object = " + object +
                  "\n\tNotification observed attribute = " + observedAttribute +
                  "\n\tNotification derived gauge = " + derGauge);

        long seqno;
        synchronized (this) {
            seqno = sequenceNumber++;
        }

        StatisticMonitorNotification mn =
            new StatisticMonitorNotification(type,
                                    this,
                                    seqno,
                                    timeStamp,
                                    msg,
                                    object,
                                    observedAttribute,
                                    derGauge,
                                    trigger);
        if (onError)
            onErrorNotification(mn);
        sendNotification(mn);
    }

    /**
     * This method is called by the monitor each time
     * the granularity period has been exceeded.
     * @param index The index of the observed object.
     */
    private void monitor(int index) {

        String notifType = null;
        String msg = null;
        Object derGauge = null;
        Object trigger = null;
        ObjectName object = null;
        Comparable<?> value = null;
        StatisticMonitorNotification alarm = null;

        synchronized(this) {
            if (!isActive())
                return;

            // Check that neither the observed object nor the
            // observed attribute are null.  If the observed
            // object or observed attribute is null, this means
            // that the monitor started before a complete
            // initialization and nothing is done.
            //
            object = getObservedObject(index);
            String attribute = getObservedAttribute();
            if (object == null || attribute == null) {
                return;
            }

            // Check that the observed object is registered in the
            // MBean server and that the observed attribute
            // belongs to the observed object.
            //
            Object attributeValue = null;
            try {
                attributeValue = getAttribute(server, object, attribute);
                if (attributeValue == null)
                    if (alreadyNotified(index,
                                        OBSERVED_ATTRIBUTE_TYPE_ERROR_NOTIFIED))
                        return;
                    else {
                        notifType = OBSERVED_ATTRIBUTE_TYPE_ERROR;
                        setAlreadyNotified(
                                  index,
                                  OBSERVED_ATTRIBUTE_TYPE_ERROR_NOTIFIED);
                        msg = "The observed attribute value is null.";
                        if ( _logger.isLoggable(Level.WARNING) )
                            _logger.log(Level.WARNING,msg);
                    }
            } catch (NullPointerException np_ex) {
                if (alreadyNotified(index, RUNTIME_ERROR_NOTIFIED))
                    return;
                else {
                    notifType = RUNTIME_ERROR;
                    setAlreadyNotified(index, RUNTIME_ERROR_NOTIFIED);
                    msg =
                        "The monitor must be registered in the MBean " +
                        "server or an MBeanServerConnection must be " +
                        "explicitly supplied.";
                    if ( _logger.isLoggable(Level.WARNING) ) {
                        _logger.log(Level.WARNING,msg);
                        _logger.log(Level.WARNING, np_ex.toString());
                    }
                }
            } catch (InstanceNotFoundException inf_ex) {
                if (alreadyNotified(index, OBSERVED_OBJECT_ERROR_NOTIFIED))
                    return;
                else {
                    notifType = OBSERVED_OBJECT_ERROR;
                    setAlreadyNotified(index, OBSERVED_OBJECT_ERROR_NOTIFIED);
                    msg =
                        "The observed object must be accessible in " +
                        "the MBeanServerConnection.";
                    if ( _logger.isLoggable(Level.WARNING) ) {
                        _logger.log(Level.WARNING,msg);
                        _logger.log(Level.WARNING, inf_ex.toString());
                    }
                }
            } catch (AttributeNotFoundException anf_ex) {
                if (alreadyNotified(index, OBSERVED_ATTRIBUTE_ERROR_NOTIFIED))
                    return;
                else {
                    notifType = OBSERVED_ATTRIBUTE_ERROR;
                    setAlreadyNotified(index,
                                       OBSERVED_ATTRIBUTE_ERROR_NOTIFIED);
                    msg =
                        "The observed attribute must be accessible in " +
                        "the observed object.";
                    if ( _logger.isLoggable(Level.WARNING) ) {
                        _logger.log(Level.WARNING,msg);
                        _logger.log(Level.WARNING, anf_ex.toString());
                    }

                }
            } catch (MBeanException mb_ex) {
                if (alreadyNotified(index, RUNTIME_ERROR_NOTIFIED))
                    return;
                else {
                    notifType = RUNTIME_ERROR;
                    setAlreadyNotified(index, RUNTIME_ERROR_NOTIFIED);
                    msg = mb_ex.getMessage();
                    if ( _logger.isLoggable(Level.WARNING) ) {
                        _logger.log(Level.WARNING,msg);
                        _logger.log(Level.WARNING, mb_ex.toString());
                    }
                }
            } catch (ReflectionException ref_ex) {
                if (alreadyNotified(index, RUNTIME_ERROR_NOTIFIED)) {
                    return;
                } else {
                    notifType = RUNTIME_ERROR;
                    setAlreadyNotified(index,
                                       RUNTIME_ERROR_NOTIFIED);
                    msg = ref_ex.getMessage();
                    if ( _logger.isLoggable(Level.WARNING) ) {
                        _logger.log(Level.WARNING,msg);
                        _logger.log(Level.WARNING, ref_ex.toString());
                    }
                }
            } catch (IOException io_ex) {
                if (alreadyNotified(index, RUNTIME_ERROR_NOTIFIED))
                    return;
                else {
                    notifType = RUNTIME_ERROR;
                    setAlreadyNotified(index, RUNTIME_ERROR_NOTIFIED);
                    msg = io_ex.getMessage();
                    if ( _logger.isLoggable(Level.WARNING) ) {
                        _logger.log(Level.WARNING,msg);
                        _logger.log(Level.WARNING, io_ex.toString());
                    }
                }
            } catch (RuntimeException rt_ex) {
                if (alreadyNotified(index, RUNTIME_ERROR_NOTIFIED))
                    return;
                else {
                    notifType = RUNTIME_ERROR;
                    setAlreadyNotified(index, RUNTIME_ERROR_NOTIFIED);
                    msg = rt_ex.getMessage();
                    if ( _logger.isLoggable(Level.WARNING) ) {
                        _logger.log(Level.WARNING,msg);
                        _logger.log(Level.WARNING, rt_ex.toString());
                    }
                }
            }

            // Derive a Comparable object from the ObservedAttribute value
            // if the type of the ObservedAttribute value is a complex type.
            //
            if (msg == null) {
                try {
                    value = getComparableFromAttribute(object,
                                                       attribute,
                                                       attributeValue);
                } catch (AttributeNotFoundException e) {
                    if (alreadyNotified(index,
                                        OBSERVED_ATTRIBUTE_ERROR_NOTIFIED))
                        return;
                    else {
                        notifType = OBSERVED_ATTRIBUTE_ERROR;
                        setAlreadyNotified(index,
                                           OBSERVED_ATTRIBUTE_ERROR_NOTIFIED);
                        msg =
                            "The observed attribute must be accessible in " +
                            "the observed object.";
                        if ( _logger.isLoggable(Level.WARNING) ) {
                            _logger.log(Level.WARNING,msg);
                            _logger.log(Level.WARNING, e.toString());
                        }

                    }
                } catch (RuntimeException e) {
                    if (alreadyNotified(index, RUNTIME_ERROR_NOTIFIED))
                        return;
                    else {
                        notifType = RUNTIME_ERROR;
                        setAlreadyNotified(index, RUNTIME_ERROR_NOTIFIED);
                        msg = e.getMessage();
                        if ( _logger.isLoggable(Level.WARNING) ) {
                            _logger.log(Level.WARNING,msg);
                            _logger.log(Level.WARNING, e.toString());
                        }
                    }
                }
            }

            // Check that the observed attribute type is supported by this
            // monitor.
            //
            if (msg == null) {
                if (!isComparableTypeValid(object, attribute, value)) {
                    if (alreadyNotified(index,
                                        OBSERVED_ATTRIBUTE_TYPE_ERROR_NOTIFIED))
                        return;
                    else {
                        notifType = OBSERVED_ATTRIBUTE_TYPE_ERROR;
                        setAlreadyNotified(
                                  index,
                                  OBSERVED_ATTRIBUTE_TYPE_ERROR_NOTIFIED);
                        msg = "The observed attribute type is not valid.";
                        if ( _logger.isLoggable(Level.WARNING) ) {
                            _logger.log(Level.WARNING,msg);
                        }
                    }
                }
            }

            // Check that threshold type is supported by this monitor.
            //
            if (msg == null) {
                if (!isThresholdTypeValid(object, attribute, value)) {
                    if (alreadyNotified(index, THRESHOLD_ERROR_NOTIFIED))
                        return;
                    else {
                        notifType = THRESHOLD_ERROR;
                        setAlreadyNotified(index, THRESHOLD_ERROR_NOTIFIED);
                        msg = "The threshold type is not valid.";
                        if ( _logger.isLoggable(Level.WARNING) ) {
                            _logger.log(Level.WARNING,msg);
                        }
                    }
                }
            }

            // Let someone subclassing the monitor to perform additional
            // monitor consistency checks and report errors if necessary.
            //
            if (msg == null) {
                msg = buildErrorNotification(object, attribute, value);
                if (msg != null) {
                    if (alreadyNotified(index, RUNTIME_ERROR_NOTIFIED))
                        return;
                    else {
                        notifType = RUNTIME_ERROR;
                        setAlreadyNotified(index, RUNTIME_ERROR_NOTIFIED);
                        if ( _logger.isLoggable(Level.WARNING) ) {
                            _logger.log(Level.WARNING,msg);
                        }
                    }
                }
            }

            // If no errors were found then clear all error flags and
            // let the monitor decide if a notification must be sent.
            //
            if (msg == null) {
                // Clear all already notified flags.
                //
                resetAllAlreadyNotified(index);

                // Get derived gauge from comparable value.
                //
                derGauge = getDerivedGaugeFromComparable(object,
                                                         attribute,
                                                         value);
                derivedGauge[index] = derGauge;
                derivedGaugeTimestamp[index] = System.currentTimeMillis();

                // Check if an alarm must be fired.
                //
                alarm = buildAlarmNotification(object,
                                               attribute,
                                               (Comparable<?>) derGauge);
            }
        }

        // Notify monitor errors
        //
        if (msg != null)
            sendNotification(notifType,
                             System.currentTimeMillis(),
                             msg,
                             derGauge,
                             trigger,
                             object,
                             true);

        // Notify monitor alarms
        //
        if (alarm != null && alarm.getType() != null) {
            sendNotification(alarm.getType(),
                             System.currentTimeMillis(),
                             alarm.getMessage(),
                             derGauge,
                             alarm.getTrigger(),
                             object,
                             false);
        } 
    }

    /**
     * SchedulerTask nested class: This class implements the Runnable interface.
     *
     * The SchedulerTask is executed periodically with a given fixed delay by
     * the Scheduled Executor Service.
     */
    private static class SchedulerTask implements Runnable {

        private Runnable task = null;

        /*
         * ------------------------------------------
         *  CONSTRUCTORS
         * ------------------------------------------
         */

        public SchedulerTask(Runnable task) {
            this.task = task;
        }

        /*
         * ------------------------------------------
         *  PUBLIC METHODS
         * ------------------------------------------
         */

        public void run() {
            executor.submit(task);
        }
    }

    /**
     * MonitorTask nested class: This class implements the Runnable interface.
     *
     * The MonitorTask is executed periodically with a given fixed delay by the
     * Scheduled Executor Service.
     */
    private class MonitorTask implements Runnable {

        /*
         * ------------------------------------------
         *  CONSTRUCTORS
         * ------------------------------------------
         */

        public MonitorTask() {
        }

        /*
         * ------------------------------------------
         *  PUBLIC METHODS
         * ------------------------------------------
         */

        public void run() {
            synchronized(StatisticMonitor.this) {
                AccessController.doPrivileged(new PrivilegedAction() {
                    public Object run() {
                        if (StatisticMonitor.this.isActive())
                            for (int i = 0; i < StatisticMonitor.this.elementCount; i++)
                                StatisticMonitor.this.monitor(i);
                        return null;
                    }
                }, StatisticMonitor.this.acc);
                StatisticMonitor.this.schedulerFuture =
                    scheduler.schedule(StatisticMonitor.this.schedulerTask,
                                       StatisticMonitor.this.getGranularityPeriod(),
                                       TimeUnit.MILLISECONDS);
            }
        }
    }

    /**
     * Daemon thread factory used by the monitor executors.
     * <P>
     * This factory creates all new threads used by an Executor in
     * the same ThreadGroup. If there is a SecurityManager, it uses
     * the group of System.getSecurityManager(), else the group of
     * the thread instantiating this DaemonThreadFactory. Each new
     * thread is created as a daemon thread with priority
     * Thread.NORM_PRIORITY. New threads have names accessible via
     * Thread.getName() of "JMX Monitor <pool-name> Pool [Thread-M]",
     * where M is the sequence number of the thread created by this
     * factory.
     */
    private static class DaemonThreadFactory implements ThreadFactory {
        final ThreadGroup group;
        final AtomicInteger threadNumber = new AtomicInteger(1);
        final String namePrefix;
        final String nameSuffix = "]";

        public DaemonThreadFactory(String poolName) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                                  Thread.currentThread().getThreadGroup();
            namePrefix = "JMX Monitor " + poolName + " Pool [Thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group,
                                  r,
                                  namePrefix +
                                  threadNumber.getAndIncrement() +
                                  nameSuffix,
                                  0);
            t.setDaemon(true);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
