/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

/* CVS information
 * $Header: /cvs/glassfish/jmx-remote/rjmx-impl/src/java/com/sun/enterprise/admin/jmx/remote/SunOneHttpJmxConnectorFactory.java,v 1.4 2005/12/25 04:26:30 tcfujii Exp $
 * $Revision: 1.4 $
 * $Date: 2005/12/25 04:26:30 $
*/

package com.sun.enterprise.admin.jmx.remote;

import java.util.Map;
import java.util.HashMap;
import java.io.IOException;

import javax.management.remote.JMXServiceURL;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import com.sun.enterprise.admin.jmx.remote.DefaultConfiguration;

/** A convenience class that knows how to setup the client side to get the reference of JMXConnector. 
 * This class is specific to Sun ONE Application Server 8.0. Any
 * client can use the following to initialize the S1AS 8.0 JSR 160 client.
 * This class lets the clients to do this under the hood and provide a {@link JMXConnectorFactory} like
 * API.
 * @author Kedar Mhaswade
 * @since S1AS8.0
 * @version 1.0
*/

public class SunOneHttpJmxConnectorFactory {

    /** Creates a new instance of SunOneHttpJmxConnectorFactory */
    private SunOneHttpJmxConnectorFactory() {
    }
    
    private static Map initEnvironment() {
        final Map env = new HashMap();
        final String PKGS = "com.sun.enterprise.admin.jmx.remote.protocol";
        env.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, PKGS);
        env.put(DefaultConfiguration.HTTP_AUTH_PROPERTY_NAME, DefaultConfiguration.DEFAULT_HTTP_AUTH_SCHEME);
        
        return ( env );
    }

    public static JMXConnector connect(JMXServiceURL url, String user, String password) 
    throws IOException {
        return connect(url, user, password, null);
    }

    public static JMXConnector connect(JMXServiceURL url, String user, String password, Map extraEnv) 
    throws IOException {
        final Map env = initEnvironment();
        if (user != null) 
            env.put(DefaultConfiguration.ADMIN_USER_ENV_PROPERTY_NAME, user);
        if (password != null)
            env.put(DefaultConfiguration.ADMIN_PASSWORD_ENV_PROPERTY_NAME, password);
        if (extraEnv != null) env.putAll(extraEnv);
        
        return ( JMXConnectorFactory.connect(url, env) );
    }
}
