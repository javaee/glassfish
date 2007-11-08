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
 * Copyright 2005-2006 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
package com.sun.mfwk.agent.appserv.delegate;

/**
 * Provides the default mapping for attributes.
 */
public class DefaultDelegate extends AbstractDelegate {

    /** 
     * Creates a new instance of default delegate. 
     */
    public DefaultDelegate() {
        super();
    } 

    /**
     * Initializes the settings for CMM_Capabilities mbean. This is done so 
     * that only enabled settings can be turned ON in the cmm mbean mapping 
     * xml file. 
     *
     * This method is called by the mbean modeler when this delegate is 
     * surving a capabilities mbean.
     */
    public void initCapabilitiesMappings() {

        // default capabilities
        addDefaultMappingEntry("EventManageable", Boolean.FALSE);
        addDefaultMappingEntry("EventsEnabled", Boolean.FALSE);
        addDefaultMappingEntry("EventsProvider", Boolean.FALSE);
        addDefaultMappingEntry("LogManageable", Boolean.FALSE);
        addDefaultMappingEntry("LogsEnabled", Boolean.FALSE); 
        addDefaultMappingEntry("LogsProvider", Boolean.FALSE);
        addDefaultMappingEntry("MonitoringEnabled", Boolean.FALSE);
        addDefaultMappingEntry("ServiceTimeEnabled", Boolean.FALSE);
        addDefaultMappingEntry("ServiceTimeSupported", Boolean.FALSE);
        addDefaultMappingEntry("SettingManageable", Boolean.FALSE);
        addDefaultMappingEntry("SettingsEnabled", Boolean.FALSE); 
        addDefaultMappingEntry("SettingsProvider", Boolean.FALSE); 
        addDefaultMappingEntry("StateManageable", Boolean.FALSE); 
        addDefaultMappingEntry("StatesEnabled", Boolean.FALSE); 
        addDefaultMappingEntry("StatesProvider", Boolean.FALSE); 
        addDefaultMappingEntry("StatisticManageable", Boolean.FALSE); 
        addDefaultMappingEntry("StatisticsEnabled", Boolean.FALSE);
        addDefaultMappingEntry("StatisticsProvider", Boolean.FALSE);
    }

    /**
     * Sets the name of the application server instance.
     *
     * @param  server  server instance name
     */
    public void setServerName(String server) {
        serverName = server;
    }

    /**
     * Sets the name of the application server domain.
     *
     * @param  domain  domain name
     */
    public void setDomainName(String domain) {
        domainName = domain;
    }
}
