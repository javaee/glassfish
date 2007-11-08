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
package com.sun.enterprise.admin.event;

import com.sun.enterprise.admin.event.AdminEvent;

/**
 * Monitoring Event. A monitoring event should trigger start, stop or
 * collection of monitoring data for specified component.
 */
public class MonitoringEvent extends AdminEvent {

    /**
     * Constant denoting action of monitoring start
     */
    public static final String START_MONITORING = "start_monitoring";

    /**
     * Constant denoting action of monitoring stop
     */
    public static final String STOP_MONITORING = "stop_monitoring";

    /**
     * Constant denoting action of get monitoring data (collection of data)
     */
    public static final String GET_MONITOR_DATA = "get_monitoring";

    /**
     * Constant denoting action of list monitorable components
     */
    public static final String LIST_MONITORABLE = "list_monitorable";

    /**
     * Constant denoting action of set (for TxnMgr admin currently)
     */
    public static final String SET_MONITOR_DATA = "set_monitoring";

    /**
     * Event type
     */
    static final String eventType = MonitoringEvent.class.getName();

    private String componentName;
    private String actionCode;
    private Object command;

    /**
     * Create a new MonitoringEvent.
     * @param instance name of the server instance to which this event applies
     * @param component name of the server component to which this event applies
     * @param action monitoring action, one of MonitoringEvent.START_MONITORING,
     *        MonitoringEvent.STOP_MONITORING, MonitoringEvent.GET_MONITOR_DATA,
     *        MonitoringEvent.LIST_MONITORABLE
     */
    public MonitoringEvent(String instance, String component, String action) {
        this(instance, component, action, null);
    }

    /**
     * Create a new MonitoringEvent.
     * @param instance name of the server instance to which this event applies
     * @param component name of the server component to which this event applies
     * @param action monitoring action, one of MonitoringEvent.START_MONITORING,
     *        MonitoringEvent.STOP_MONITORING, MonitoringEvent.GET_MONITOR_DATA,
     *        MonitoringEvent.LIST_MONITORABLE
     * @param command a monitoring command. AdminCommand is extra information that
     *        is used in monitoring event listeners. This is typically an
     *        instance of com.sun.enterprise.admin.monitor.MonitorCommand (or its
     *        sub-class) and may be null.
     */
    public MonitoringEvent(String instance, String component, String action,
            Object command) {
        super(eventType, instance);
        componentName = component;
        actionCode = action;
        this.command = command;
    }

    /**
     * Get name of component to which this event applies.
     */
    public String getComponentName() {
        return componentName;
    }

    /**
     * Get event action code - one of MonitoringEvent.START_MONITORING,
     * MonitoringEvent.STOP_MONITORING, MonitoringEvent.GET_MONITOR_DATA,
     * MonitoringEvent.LIST_MONITORABLE
     */
    public String getActionCode() {
        return actionCode;
    }

    /**
     * Get monitoring command information. This is used to access an
     * instance of com.sun.enterprise.admin.monitor.MonitorCommand (or its sub
     * classes) from the event. This may be null.
     * @return Monitoring command
     */
    public Object getCommand() {
        return command;
    }

    /**
     * Get a string representation.
     */
    public String toString() {
        return "MonitoringEvent -- " + componentName + " -- " + actionCode;
    }
}
