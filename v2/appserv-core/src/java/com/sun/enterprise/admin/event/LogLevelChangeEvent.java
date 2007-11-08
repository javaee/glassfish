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
 * LogLevelChangeEvent is used to represent change in the log level for
 * a module. Log levels and modules are configured in the configuration
 * file element module-log-levels.
 */
public class LogLevelChangeEvent extends AdminEvent {

    /**
     * Event type
     */
    static final String eventType = LogLevelChangeEvent.class.getName();

    /**
     * Logger module name
     */
    private String moduleName;
 
    /**
     * Old log level
     */
    private String oldLogLevel;

    /**
     * New log level
     */
    private String newLogLevel;

    /** 
     * true when this event represents property changes in the 
     * module-log-level element
     */
    private boolean propertyChanged = false;

    /**
     * name of the modified property
     */
    private String modifiedPropertyName = null;

    /**
     * Creates a new instance of LogLevelChangeEvent
     * @param instanceName the server instance affected by the change
     */
    public LogLevelChangeEvent(String instanceName) {
        this(eventType, instanceName);
    }

    /**
     * Creates a new instance of LogLevelChangeEvent
     * @eventType type of the event
     * @param instanceName the server instance affected by the change
     */
    protected LogLevelChangeEvent(String eventType, String instanceName) {
        super(eventType, instanceName);
    }

    /**
     * Get name of the module for which log level has changed. The name
     * of the module is defined in the dtd for configuration.
     */
    public String getModuleName() {
        return moduleName;
    }

    /**
     * Set module name.
     * @param moduleName name of the module for which the event is being created
     */
    void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    /**
     * Get old log level for the module returned by method getModuleName().
     * The log levels are defined in dtd for configuration
     */
    public String getOldLogLevel() {
        return oldLogLevel;
    }

    /**
     * Set old log level
     */
    void setOldLogLevel(String oldLevel) {
        oldLogLevel = oldLevel;
    }

    /**
     * Get new log level for the module returned by method getModuleName().
     * The log levels are defined in the dtd for configuration
     */
    public String getNewLogLevel() {
        return newLogLevel;
    }

    /**
     * Set new log level
     */
    void setNewLogLevel(String newLevel) {
        this.newLogLevel = newLevel;
    }
    
    /**
     * Returns true when propery changed for module-log-levels.
     * 
     * @return  true when property changed for module-log-levels
     */
    public boolean isPropertyChanged() {
        return this.propertyChanged;
    }

    /**
     * Sets the property change flag for module-log-levels.
     * 
     * @param  tf   val for property change flag
     */
    void setPropertyChanged(boolean tf) {
        this.propertyChanged = tf;
    }

    /**
     * Returns the name of the modified property when isPropertyChanged is true.
     * @return  name of the modified property or null
     */
    public String getPropertyName() {
        return this.modifiedPropertyName;
    }

    /**
     * Sets the name of the modified property.
     * @param  name  name of the modified property
     */
    void setPropertyName(String name) {
        this.modifiedPropertyName = name;
    }
}
