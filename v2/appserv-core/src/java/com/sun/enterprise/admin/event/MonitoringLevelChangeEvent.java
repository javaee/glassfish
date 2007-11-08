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
package com.sun.enterprise.admin.event;

/**
 * MonitoringLevelChangeEvent is used to represent change in the Monitoring
 * level for a component. Monitoring levels and components are configured in
 * the configuration file element monitoring-service.
 */
public class MonitoringLevelChangeEvent extends AdminEvent {

    /**
     * Event type
     */
    static final String eventType = MonitoringLevelChangeEvent.class.getName();

    /**
     * Monitored component name
     */
    private String componentName;
 
    /**
     * Old monitoring level
     */
    private String oldMonitoringLevel;

    /**
     * New monitoring level
     */
    private String newMonitoringLevel;

    /**
     * Creates a new instance of MonitoringLevelChangeEvent
     * @param instanceName the server instance affected by the change
     */
    public MonitoringLevelChangeEvent(String instanceName) {
        this(eventType, instanceName);
    }

    /**
     * Creates a new instance of MonitoringLevelChangeEvent
     * @eventType type of the event
     * @param instanceName the server instance affected by the change
     */
    protected MonitoringLevelChangeEvent(String eventType, String instanceName) {
        super(eventType, instanceName);
    }

    /**
     * Get name of the component for which Monitoring level has changed. The name
     * of the component is defined in the dtd for configuration.
     */
    public String getComponentName() {
        return componentName;
    }

    /**
     * Set component name.
     * @param componentName name of the component for which the event is being created
     */
    void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    /**
     * Get old monitoring level for the component returned by method
     * getComponentName(). The monitoring levels are defined in dtd for
     * configuration
     */
    public String getOldMonitoringLevel() {
        return oldMonitoringLevel;
    }

    /**
     * Set old Monitoring level
     */
    void setOldMonitoringLevel(String oldLevel) {
        oldMonitoringLevel = oldLevel;
    }

    /**
     * Get new monitoring level for the component returned by method
     * getComponentName(). The monitoring levels are defined in the dtd for
     * configuration.
     */
    public String getNewMonitoringLevel() {
        return newMonitoringLevel;
    }

    /**
     * Set new monitoring level
     */
    void setNewMonitoringLevel(String newLevel) {
        this.newMonitoringLevel = newLevel;
    }
    
}
