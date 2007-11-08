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
 * StatisticMonitorNotification.java
 * 
 * Created on July 11, 2005
 */

package com.sun.appserv.management.event; 


// jmx imports
//
import javax.management.ObjectName;

/**
 * Provides definitions of the notifications sent by statistic monitor 
 * MBeans. Used for JDK version greater than 1.5.
 * <P>
 * The notification source and a set of parameters concerning the statistic monitor MBean's state
 * need to be specified when creating a new object of this class.
 *
 * The list of notifications fired by the statistic monitor MBeans is the following:
 *
 * <UL>
 * <LI>Common to all kind of monitors:
 *     <UL>
 *     <LI>The observed object is not registered in the MBean server.
 *     <LI>The observed attribute is not contained in the observed object.
 *     <LI>The type of the observed attribute is not correct.
 *     <LI>Any exception (except the cases described above) occurs when trying to get the value of the observed attribute.
 *     </UL>
 * <LI>Common to the counter and the gauge statistic monitors:
 *     <UL>
 *     <LI>The threshold high or threshold low are not of the same type as the gauge (gauge monitors).
 *     <LI>The threshold or the offset or the modulus are not of the same type as the counter (counter statistic monitors).
 *     </UL>
 * <LI>CounterStatistic monitors only:
 *     <UL>
 *     <LI>The observed attribute has reached the threshold value.
 *     </UL>
 * <LI>GaugeStatistic monitors only:
 *     <UL>
 *     <LI>The observed attribute has exceeded the threshold high value.
 *     <LI>The observed attribute has exceeded the threshold low value.
 *     </UL>
 * <LI>StringStatistic monitors only:
 *     <UL>
 *     <LI>The observed attribute has matched the "string to compare" value.
 *     <LI>The observed attribute has differed from the "string to compare" value.
 *     </UL>
 * </UL>
 *
 * @author      Sun Microsystems, Inc
 */
public class StatisticMonitorNotification extends javax.management.Notification { 


    /*
     * ------------------------------------------
     *  PUBLIC VARIABLES
     * ------------------------------------------
     */    
    
    /**
     * Notification type denoting that the observed object is not registered in the MBean server.
     * This notification is fired by all kinds of statistic monitors.
     * <BR>The value of this notification type is <CODE>jmx.monitor.error.mbean</CODE>.
     */
    public static final String OBSERVED_OBJECT_ERROR = "jmx.monitor.error.mbean";

    /**
     * Notification type denoting that the observed attribute is not contained in the observed object.
     * This notification is fired by all kinds of statistic monitors.
     * <BR>The value of this notification type is <CODE>jmx.monitor.error.attribute</CODE>.
     */
    public static final String OBSERVED_ATTRIBUTE_ERROR = "jmx.monitor.error.attribute";

    /**
     * Notification type denoting that the type of the observed attribute is not correct.
     * This notification is fired by all kinds of statistic monitors.
     * <BR>The value of this notification type is <CODE>jmx.monitor.error.type</CODE>.
     */
    public static final String OBSERVED_ATTRIBUTE_TYPE_ERROR = "jmx.monitor.error.type";

    /**
     * Notification type denoting that the type of the thresholds, offset or modulus is not correct.
     * This notification is fired by counter and gauge statistic monitors.
     * <BR>The value of this notification type is <CODE>jmx.monitor.error.threshold</CODE>.
     */
    public static final String THRESHOLD_ERROR = "jmx.monitor.error.threshold";
    
    /**
     * Notification type denoting that a non-predefined error type has occurred when trying to get the value of the observed attribute.
     * This notification is fired by all kinds of statistic monitors.
     * <BR>The value of this notification type is <CODE>jmx.monitor.error.runtime</CODE>.
     */
    public static final String RUNTIME_ERROR = "jmx.monitor.error.runtime";
    
    /**
     * Notification type denoting that the observed attribute has reached the threshold value.
     * This notification is only fired by counter statistic monitors.
     * <BR>The value of this notification type is <CODE>jmx.monitor.counter.threshold</CODE>.
     */
    public static final String THRESHOLD_VALUE_EXCEEDED = "jmx.monitor.counter.threshold";

    /**
     * Notification type denoting that the observed attribute has exceeded the threshold high value.
     * This notification is only fired by gauge statistic monitors.
     * <BR>The value of this notification type is <CODE>jmx.monitor.gauge.high</CODE>.
     */
    public static final String THRESHOLD_HIGH_VALUE_EXCEEDED = "jmx.monitor.gauge.high";

    /**
     * Notification type denoting that the observed attribute has exceeded the threshold low value.
     * This notification is only fired by gauge statistic monitors.
     * <BR>The value of this notification type is <CODE>jmx.monitor.gauge.low</CODE>.
     */
    public static final String THRESHOLD_LOW_VALUE_EXCEEDED = "jmx.monitor.gauge.low";
    
    /**
     * Notification type denoting that the observed attribute has matched the "string to compare" value.
     * This notification is only fired by string statistic monitors.
     * <BR>The value of this notification type is <CODE>jmx.monitor.string.matches</CODE>.
     */
    public static final String STRING_TO_COMPARE_VALUE_MATCHED = "jmx.monitor.string.matches";

    /**
     * Notification type denoting that the observed attribute has differed from the "string to compare" value.
     * This notification is only fired by string statistic monitors.
     * <BR>The value of this notification type is <CODE>jmx.monitor.string.differs</CODE>.
     */
    public static final String STRING_TO_COMPARE_VALUE_DIFFERED = "jmx.monitor.string.differs";
    
    
    /*
     * ------------------------------------------
     *  PRIVATE VARIABLES
     * ------------------------------------------
     */
    
    /* Serial version */
    private static final long serialVersionUID = -4608189663661929204L;

    /**
     * @serial Monitor notification observed object.
     */
    private ObjectName observedObject = null;

    /**
     * @serial Monitor notification observed attribute.
     */
    private String observedAttribute = null;

    /**
     * @serial Monitor notification derived gauge.
     */
    private Object derivedGauge = null;
    
    /**
     * @serial Monitor notification release mechanism.
     *         This value is used to keep the threshold/string (depending on the
     *         monitor type) that triggered off this notification.
     */
    private Object trigger = null;


    /*
     * ------------------------------------------
     *  CONSTRUCTORS
     * ------------------------------------------
     */
    
    /**
     * Creates a statistic monitor notification object.
     *
     * @param type The notification type.
     * @param source The notification producer.
     * @param sequenceNumber The notification sequence number within the source object.
     * @param timeStamp The notification emission date.
     * @param msg The notification message.
     * @param obsObj The object observed by the producer of this notification.
     * @param obsAtt The attribute observed by the producer of this notification.
     * @param derGauge The derived gauge.
     * @param trigger The threshold/string (depending on the monitor type) that triggered the notification.
     */
    public StatisticMonitorNotification(String type, Object source, long sequenceNumber, long timeStamp, String msg,
                               ObjectName obsObj, String obsAtt, Object derGauge, Object trigger) {
        
        super(type, source, sequenceNumber, timeStamp, msg);
        this.observedObject = obsObj;
        this.observedAttribute = obsAtt;
        this.derivedGauge = derGauge;
        this.trigger = trigger;
    }
    
    /*
     * ------------------------------------------
     *  PUBLIC METHODS
     * ------------------------------------------
     */
    
    // GETTERS AND SETTERS
    //--------------------    
    
    /**
     * Gets the observed object of this  statistic monitor notification.
     *
     * @return The observed object.
     */
    public ObjectName getObservedObject() { 
        return observedObject;
    } 

    /**
     * Gets the observed attribute of this statistic monitor notification.
     *
     * @return The observed attribute.
     */
    public String getObservedAttribute() { 
        return observedAttribute;
    } 

    /**
     * Gets the derived gauge of this statistic monitor notification.
     *
     * @return The derived gauge.
     */
    public Object getDerivedGauge() { 
        return derivedGauge;
    }
    
    /**
     * Gets the threshold/string (depending on the monitor type) that triggered off this statistic monitor notification.
     *
     * @return The trigger.
     */
    public Object getTrigger() { 
        return trigger;
    }
    
}
