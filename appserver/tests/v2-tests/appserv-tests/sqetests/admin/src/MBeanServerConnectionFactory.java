/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

import com.sun.appserv.management.client.AppserverConnectionSource;
import com.sun.appserv.management.client.TLSParams;
import com.sun.appserv.management.client.TrustAnyTrustManager;
import javax.management.MBeanServerConnection;
import javax.net.ssl.X509TrustManager;

/** A class to create an @{link MBeanServerConnection} to appserver.
 */
public class MBeanServerConnectionFactory {
    
    /** Creates a new instance of MBeanServerConnectionFactory */
    private MBeanServerConnectionFactory() {
    }
    
    public static final MBeanServerConnection getMBeanServerConnectionHTTPOrHTTPS(final String adminUser, final String 
            adminPassword, final String adminHost, final String adminPort, final String isSecure) throws RuntimeException {
        
        MBeanServerConnection mbsc = null;
        try {
            final String protocol = AppserverConnectionSource.PROTOCOL_HTTP;
            final int port = Integer.parseInt(adminPort);
            final boolean sec = Boolean.parseBoolean(isSecure);
            final TLSParams tlsp = sec ? getDummyTLSParams() : null;
            final AppserverConnectionSource acs = new AppserverConnectionSource(protocol, adminHost, port, adminUser, adminPassword, tlsp, null);
            mbsc = acs.getMBeanServerConnection(true);
            return ( mbsc );
        } catch(final Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private static TLSParams getDummyTLSParams() {
        final X509TrustManager[] tms = TrustAnyTrustManager.getInstanceArray();
        return ( new TLSParams(tms, null) );
    }
}
