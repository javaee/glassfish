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

package com.sun.enterprise.web.tomcat;

import org.apache.catalina.LifecycleException;
import org.apache.coyote.tomcat5.CoyoteConnector;

/**
 * A CoyoteConnector subclass which "wraps around" an existing Grizzly
 * SelectorThread that is being started and stopped outside of the lifecycle
 * of this CoyoteConnector subclass (the SelectorThread is started and
 * stopped as part of the GrizzlyAdapter lifecycle).
 *
 * The only purpose of this TomcatConnector is to start and stop its associated
 * MapperListener, which populates the Catalina Mapper that is used by the
 * CoyoteAdapter which gets registered with the GrizzlyAdapter for web
 * context endpoints.
 *
 * @author jluehe
 */ 
public class TomcatConnector extends CoyoteConnector {

    public void start() throws LifecycleException {
    
        mapperListener.setPort(getPort());
        mapperListener.setDomain("com.sun.appserv");
        mapperListener.init();
    }

    public void setDefaultHost(String defaultHost) {
        mapperListener.setDefaultHost(defaultHost);
    }

    public void initialize() throws LifecycleException {
        initialized = true;
    }
}
