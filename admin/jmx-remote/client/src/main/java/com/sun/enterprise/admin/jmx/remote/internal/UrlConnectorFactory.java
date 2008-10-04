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

/*
 * $Header: /cvs/glassfish/jmx-remote/rjmx-impl/src/java/com/sun/enterprise/admin/jmx/remote/internal/UrlConnectorFactory.java,v 1.3 2005/12/25 04:26:34 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 04:26:34 $
 */

package com.sun.enterprise.admin.jmx.remote.internal;

import java.util.Map;
import javax.management.remote.JMXServiceURL;
import com.sun.enterprise.admin.jmx.remote.http.HttpUrlConnector;
import com.sun.enterprise.admin.jmx.remote.https.HttpsUrlConnector;
import com.sun.enterprise.admin.jmx.remote.UrlConnector;

/** A factory to create instances of various UrlConnectors depending upon the
 * JMXServiceUrl and environment provided.
 * @author Kedar Mhaswade
 * @since S1AS8.0
 * @version 1.0
*/

public class UrlConnectorFactory {
    
    private UrlConnectorFactory() {
    }
    /** Returns the UrlConnector instance appropriately.
	 * @param url			instance of JMXServiceURL
	 * @param env			map specifying name value pairs
	 */
    public static UrlConnector getHttpConnector(JMXServiceURL url, Map env) {
        return new HttpUrlConnector(url, env);
    }
	
	public static UrlConnector getHttpsConnector(JMXServiceURL url, Map env) {
		return new HttpsUrlConnector(url, env);
	}
}