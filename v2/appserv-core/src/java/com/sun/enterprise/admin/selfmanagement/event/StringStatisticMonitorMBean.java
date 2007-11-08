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
 * StringStatisticMonitorMBean.java
 * 
 * Created on July 11, 2005 3:00 PM
 */

package com.sun.enterprise.admin.selfmanagement.event; 

// jmx imports
//
import javax.management.ObjectName;

/**
 * Exposes the remote management interface of the string statistic 
 * monitor MBean. Used for JDK version greater than 1.5.
 *
 * @author      Sun Microsystems, Inc
 */
public interface StringStatisticMonitorMBean extends StatisticMonitorMBean { 
    
    // GETTERS AND SETTERS
    //--------------------    
        
    /**
     * Gets the derived gauge.
     *
     * @return The derived gauge.
     * @deprecated As of JMX 1.2, replaced by {@link #getDerivedGauge(ObjectName)}
     */
    @Deprecated
    public String getDerivedGauge();
    
    /**
     * Gets the derived gauge timestamp.
     *
     * @return The derived gauge timestamp.
     * @deprecated As of JMX 1.2, replaced by {@link #getDerivedGaugeTimeStamp(ObjectName)}
     */
    @Deprecated
    public long getDerivedGaugeTimeStamp();  
    
    /**
     * Gets the derived gauge for the specified MBean.
     *
     * @param object the MBean for which the derived gauge is to be returned
     * @return The derived gauge for the specified MBean if this MBean is in the
     *         set of observed MBeans, or <code>null</code> otherwise.
     *
     * @since.unbundled JMX 1.2
     */
    public String getDerivedGauge(ObjectName object);
    
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
     * Gets the string to compare with the observed attribute.
     *
     * @return The string value.
     *
     * @see #setStringToCompare
     */
    public String getStringToCompare(); 

    /**
     * Sets the string to compare with the observed attribute.
     *
     * @param value The string value.
     * @exception java.lang.IllegalArgumentException The specified
     * string to compare is null.
     *
     * @see #getStringToCompare
     */
    public void setStringToCompare(String value) throws java.lang.IllegalArgumentException; 
    
    /**
     * Gets the matching notification's on/off switch value.
     *
     * @return <CODE>true</CODE> if the string monitor notifies when
     * matching, <CODE>false</CODE> otherwise.
     *
     * @see #setNotifyMatch
     */
    public boolean getNotifyMatch(); 

    /**
     * Sets the matching notification's on/off switch value.
     *
     * @param value The matching notification's on/off switch value.
     *
     * @see #getNotifyMatch
     */
    public void setNotifyMatch(boolean value); 

    /**
     * Gets the differing notification's on/off switch value.
     *
     * @return <CODE>true</CODE> if the string monitor notifies when
     * differing, <CODE>false</CODE> otherwise.
     *
     * @see #setNotifyDiffer
     */
    public boolean getNotifyDiffer(); 

    /**
     * Sets the differing notification's on/off switch value.
     *
     * @param value The differing notification's on/off switch value.
     *
     * @see #getNotifyDiffer
     */
    public void setNotifyDiffer(boolean value); 
}
