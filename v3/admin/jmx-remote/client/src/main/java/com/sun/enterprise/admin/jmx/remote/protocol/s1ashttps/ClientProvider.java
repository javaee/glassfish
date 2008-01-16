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
 * $Header: /cvs/glassfish/jmx-remote/rjmx-impl/src/java/com/sun/enterprise/admin/jmx/remote/protocol/s1ashttps/ClientProvider.java,v 1.3 2005/12/25 04:26:36 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 04:26:36 $
 */

package com.sun.enterprise.admin.jmx.remote.protocol.s1ashttps;

import javax.management.remote.JMXConnectorProvider;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;

import com.sun.enterprise.admin.jmx.remote.internal.UrlConnectorFactory;
import java.util.Map;
import java.io.IOException;

/** S1AS provides its own JSR 160 client provider to instantiate the supported instance of {@link JMXConnector}. 
 * It is arranged as per the algorithm
 * provided in {@link JMXConnectorFactory}. This instance of provider will
 * always use HTTPS as the transport.
 * @author Kedar Mhaswade
 * @since S1AS8.0
 * @version 1.0
*/

public class ClientProvider implements JMXConnectorProvider {
    
    /** Creates a new instance of ClientProvider */
    public ClientProvider() {
    }
    
    public JMXConnector newJMXConnector(JMXServiceURL serviceURL, Map environment)
    throws IOException {
		return ( UrlConnectorFactory.getHttpsConnector(serviceURL, environment) );
    }
}