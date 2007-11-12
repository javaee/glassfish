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
 * $Header: /cvs/glassfish/jmx-remote/rjmx-impl/src/java/com/sun/enterprise/admin/jmx/remote/http/HttpUrlConnector.java,v 1.4 2005/12/25 04:26:32 tcfujii Exp $
 * $Revision: 1.4 $
 * $Date: 2005/12/25 04:26:32 $
 */

package com.sun.enterprise.admin.jmx.remote.http;

import java.util.logging.Logger;
import java.util.Map;
import javax.management.remote.JMXServiceURL;

import com.sun.enterprise.admin.jmx.remote.DefaultConfiguration;
import com.sun.enterprise.admin.jmx.remote.UrlConnector;

/** A Concrete implementation of UrlConnector that uses {@link java.net.URLConnection.openConnection} and
 * HttpUrlConnection to communicate with the server. 
 * @author Kedar Mhaswade
 * @since S1AS8.0
 * @version 1.0
 */

public class HttpUrlConnector extends UrlConnector {
    
    private static final Logger logger = Logger.getLogger(
        DefaultConfiguration.JMXCONNECTOR_LOGGER);/*, 
        DefaultConfiguration.LOGGER_RESOURCE_BUNDLE_NAME );*/

    /** Creates a new instance of HttpUrlConnector */
    
    public HttpUrlConnector(JMXServiceURL serviceUrl, Map environment) {
        super(serviceUrl, environment);
    }
    
    protected void validateJmxServiceUrl() throws RuntimeException {
        //additional validation
    }
    
    protected void validateEnvironment() throws RuntimeException {
        super.validateEnvironment();
    }
}
