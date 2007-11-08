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

/**
 * PROPRIETARY/CONFIDENTIAL.  Use of this product is subject to license terms.
 *
 * Copyright 2001-2002 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 */
package com.sun.enterprise.admin.monitor;

import java.util.Map;
import java.util.Set;

/**
 * IMonitorable interface defines behaviour exhibited by any component
 * (service) that wants to expose some of its internal state to the
 * monitoring clients (HTML gui, command-line administration or other
 * third party tools built over command-line admin utility).
 */
public interface IMonitorable {

    /**
     * Start monitoring on this component. This will be called when monitoring
     * is enabled on this component (or the group containing this component)
     * through user interface.
     * @see stopMonitoring
     */
    public void startMonitoring();

    /**
     * Stop monitoring on this component. Called when monitoring is disabled on
     * user interface.
     */
    public void stopMonitoring();

    /**
     * Get value of specified monitored attribute.
     * @param monitorAttributeName name of the monitored attribute
     * @return value of the specified monitored attribute
     */
    public Object getMonitoredAttributeValue(String monitorAttributeName);

    /**
     * Get values of specified monitored attributes. This method returns a
     * map of monitored attribute names and their corresponding values.
     *
     * @param monitorAttributeNameSet set of monitored attribute names
     *
     * @return map of attribute names and their values
     */
    public Map getMonitoredAttributeValues(Set monitorAttributeNameSet);

    /**
     * Get a map of monitored attribute names and their types. The keys in
     * the map are names of the attribute and the values are their types. The
     * type value are instances of class
     * com.iplanet.ias.monitor.type.MonitoredAttributeType (or its sub-classes)
     *
     * @return map of names and types of all monitored attributes
     */
    public Map getMonitoringMetaData();
}
