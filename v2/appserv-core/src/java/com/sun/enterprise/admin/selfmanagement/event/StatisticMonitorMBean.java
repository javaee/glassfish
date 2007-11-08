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
 * StatisticMonitorMBean.java
 * 
 * Created on July 11, 2005
 */

package com.sun.enterprise.admin.selfmanagement.event; 

// jmx imports
//
import javax.management.ObjectName;

/**
 * Exposes the remote management interface of statistic monitor MBeans.
 * Used for JDK version greater than 1.5.
 *
 * @author      Sun Microsystems, Inc
 */
public interface StatisticMonitorMBean { 
    
    /**
     * Starts the statistic monitor.
     */
    public void start();
    
    /**
     * Stops the statistic monitor.
     */
    public void stop();
    
    // GETTERS AND SETTERS
    //-------------------- 
    
    /**
     * Adds the specified object in the set of observed MBeans.
     *
     * @param object The object to observe.
     * @exception java.lang.IllegalArgumentException the specified object is null.
     *
     * @since.unbundled JMX 1.2
     */
    public void addObservedObject(ObjectName object) throws java.lang.IllegalArgumentException;
    
    /**
     * Removes the specified object from the set of observed MBeans.
     *
     * @param object The object to remove.
     *
     * @since.unbundled JMX 1.2
     */
    public void removeObservedObject(ObjectName object);
    
    /**
     * Tests whether the specified object is in the set of observed MBeans.
     *
     * @param object The object to check.
     * @return <CODE>true</CODE> if the specified object is in the set, <CODE>false</CODE> otherwise.
     *
     * @since.unbundled JMX 1.2
     */
    public boolean containsObservedObject(ObjectName object);
    
    /**
     * Returns an array containing the objects being observed.
     *
     * @return The objects being observed.
     *
     * @since.unbundled JMX 1.2
     */
    public ObjectName[] getObservedObjects();   
    
    /**
     * Gets the object name of the object being observed.
     *
     * @return The object being observed.
     *
     * @see #setObservedObject
     *
     * @deprecated As of JMX 1.2, replaced by {@link #getObservedObjects}
     */
    @Deprecated
    public ObjectName getObservedObject();
    
    /**
     * Sets the object to observe identified by its object name.
     *
     * @param object The object to observe.
     *
     * @see #getObservedObject
     *
     * @deprecated As of JMX 1.2, replaced by {@link #addObservedObject}
     */
    @Deprecated
    public void setObservedObject(ObjectName object);
    
    /**
     * Gets the attribute being observed.
     *
     * @return The attribute being observed.
     *
     * @see #setObservedAttribute
     */
    public String getObservedAttribute();
    
    /**
     * Sets the attribute to observe.
     *
     * @param attribute The attribute to observe.
     *
     * @see #getObservedAttribute
     */
    public void setObservedAttribute(String attribute);
        
    /**
     * Gets the granularity period (in milliseconds).
     *
     * @return The granularity period.
     *
     * @see #setGranularityPeriod
     */
    public long getGranularityPeriod();
    
    /**
     * Sets the granularity period (in milliseconds).
     *
     * @param period The granularity period.
     * @exception java.lang.IllegalArgumentException The granularity
     * period is less than or equal to zero.
     *
     * @see #getGranularityPeriod
     */
    public void setGranularityPeriod(long period) throws java.lang.IllegalArgumentException;
    
    /**
     * Tests if the statistic monitor MBean is active.
     * A statistic monitor MBean is marked active when the {@link #start start} method is called.
     * It becomes inactive when the {@link #stop stop} method is called.
     *
     * @return <CODE>true</CODE> if the statistic monitor MBean is active, <CODE>false</CODE> otherwise.
     */
    public boolean isActive();
}
