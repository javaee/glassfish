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
package com.sun.appserv.management.config;

import java.util.Map;

import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.Container;
import com.sun.appserv.management.base.XTypes;

/**
 * Configuration for the &lt;virtual-server&gt; element.
 */
@AMXCreateInfo(paramNames = {"id", "hosts", "optional"})
public interface VirtualServerConfig
    extends NamedConfigElement, PropertiesAccess, Container, DefaultValues {
    /**
     * The j2eeType as returned by {@link AMX#getJ2EEType}.
     */
    String J2EE_TYPE = XTypes.VIRTUAL_SERVER_CONFIG;

    String getSsoEnabled();

    void setSsoEnabled(String value);

    String getDefaultWebModule();

    void setDefaultWebModule(String value);

    String getHosts();

    void setHosts(String value);

    String getHTTPListeners();

    void setHTTPListeners(String value);

    //** default: "${com.sun.aas.instanceRoot}/logs/server.log" */
    String getLogFile();

    void setLogFile(String value);

    String getState();

    void setState(String value);

    String getDocRoot();

    void setDocRoot(String value);

    /**
     * Removes http-access-log element.
     */
    void removeHTTPAccessLogConfig();

    /**
     * Get the HTTPAccessLogConfig MBean.
     */
    HTTPAccessLogConfig getHTTPAccessLogConfig();

    /**
     * Creates new http-access-log element.
     *
     * @return A proxy to the HTTPAccessLogConfig MBean.
     *
     * @param    ipOnly
     * @param    logDirectory
     * @param    reserved
     */
    HTTPAccessLogConfig createHTTPAccessLogConfig(@ResolveTo(Boolean.class) String ipOnly,
        String logDirectory, Map<String, String> reserved );
}
