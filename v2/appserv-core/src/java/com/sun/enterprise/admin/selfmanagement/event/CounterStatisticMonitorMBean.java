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
 * CounterStatisticMonitorMBean.java
 * 
 * Created on July 11, 2005 3:00 PM
 */

package com.sun.enterprise.admin.selfmanagement.event; 

// jmx imports
//
import javax.management.ObjectName;

/**
 * Exposes the remote management interface of the counter statistic 
 * monitor MBean. Used for JDK version greater than 1.5.
 *
 * @author      Sun Microsystems, Inc
 */
public interface CounterStatisticMonitorMBean extends StatisticMonitorMBean { 
        
    // GETTERS AND SETTERS
    //--------------------    
    
    /**
     * Gets the derived gauge.
     *
     * @return The derived gauge.
     * @deprecated As of JMX 1.2, replaced by {@link #getDerivedGauge(ObjectName)}
     */
    @Deprecated
    public Number getDerivedGauge();
    
    /**
     * Gets the derived gauge timestamp.
     *
     * @return The derived gauge timestamp.
     * @deprecated As of JMX 1.2, replaced by {@link #getDerivedGaugeTimeStamp(ObjectName)}
     */
    @Deprecated
    public long getDerivedGaugeTimeStamp();
    
    /**
     * Gets the threshold value.
     *
     * @return The threshold value.
     *
     * @see #setThreshold(Number)
     *
     * @deprecated As of JMX 1.2, replaced by {@link #getThreshold(ObjectName)}
     */
    @Deprecated
    public Number getThreshold(); 

    /**
     * Sets the threshold value.
     *
     * @see #getThreshold()
     *
     * @param value The threshold value.
     * @exception java.lang.IllegalArgumentException The specified threshold is null or the threshold value is less than zero.
     * @deprecated As of JMX 1.2, replaced by {@link #setInitThreshold}
     */
    @Deprecated
    public void setThreshold(Number value) throws java.lang.IllegalArgumentException; 

    /**
     * Gets the derived gauge for the specified MBean.
     *
     * @param object the MBean for which the derived gauge is to be returned
     * @return The derived gauge for the specified MBean if this MBean is in the
     *         set of observed MBeans, or <code>null</code> otherwise.
     *
     * @since.unbundled JMX 1.2
     */
    public Number getDerivedGauge(ObjectName object);
    
    /**
     * Gets the derived gauge timestamp for the specified MBean.
     *
     * @param object the MBean for which the derived gauge timestamp is to be returned
     * @return The derived gauge timestamp for the specified MBean if this MBean
     *         is in the set of observed MBeans, or <code>null</code> otherwise.
     *
     * @since.unbundled JMX 1.2
     */
    public long getDerivedGaugeTimeStamp(ObjectName object);
    
    /**
     * Gets the threshold value for the specified MBean.
     *
     * @param object the MBean for which the threshold value is to be returned
     * @return The threshold value for the specified MBean if this MBean
     *         is in the set of observed MBeans, or <code>null</code> otherwise.
     *
     * @see #setThreshold
     *
     * @since.unbundled JMX 1.2
     */
    public Number getThreshold(ObjectName object); 

    /**
     * Gets the initial threshold value common to all observed objects.
     *
     * @return The initial threshold value.
     *
     * @see #setInitThreshold
     *
     * @since.unbundled JMX 1.2
     */
    public Number getInitThreshold();
    
    /**
     * Sets the initial threshold value common to all observed MBeans.
     *
     * @param value The initial threshold value.
     * @exception java.lang.IllegalArgumentException The specified
     * threshold is null or the threshold value is less than zero.
     *
     * @see #getInitThreshold
     *
     * @since.unbundled JMX 1.2
     */
    public void setInitThreshold(Number value) throws java.lang.IllegalArgumentException;

    /**
     * Gets the offset value.
     *
     * @see #setOffset(Number)
     *
     * @return The offset value.
     */
    public Number getOffset(); 

    /**
     * Sets the offset value.
     *
     * @param value The offset value.
     * @exception java.lang.IllegalArgumentException The specified
     * offset is null or the offset value is less than zero.
     *
     * @see #getOffset()
     */
    public void setOffset(Number value) throws java.lang.IllegalArgumentException; 

    /**
     * Gets the modulus value.
     *
     * @return The modulus value.
     *
     * @see #setModulus
     */
    public Number getModulus(); 

    /**
     * Sets the modulus value.
     *
     * @param value The modulus value.
     * @exception java.lang.IllegalArgumentException The specified
     * modulus is null or the modulus value is less than zero.
     *
     * @see #getModulus
     */
    public void setModulus(Number value) throws java.lang.IllegalArgumentException; 
    
    /**
     * Gets the notification's on/off switch value.
     *
     * @return <CODE>true</CODE> if the counter monitor notifies when
     * exceeding the threshold, <CODE>false</CODE> otherwise.
     *
     * @see #setNotify
     */
    public boolean getNotify(); 

    /**
     * Sets the notification's on/off switch value.
     *
     * @param value The notification's on/off switch value.
     *
     * @see #getNotify
     */
    public void setNotify(boolean value); 

    /**
     * Gets the difference mode flag value.
     *
     * @return <CODE>true</CODE> if the difference mode is used,
     * <CODE>false</CODE> otherwise.
     *
     * @see #setDifferenceMode
     */
    public boolean getDifferenceMode(); 

    /**
     * Sets the difference mode flag value.
     *
     * @param value The difference mode flag value.
     *
     * @see #getDifferenceMode
     */
    public void setDifferenceMode(boolean value); 
}
