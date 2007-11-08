package test;
/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * glassfish/bootstrap/legal/CDDLv1.0.txt or
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
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
    
    public static final MBeanServerConnection getMBeanServerConnectionRMI(final String adminUser, final String adminPassword,
            final String adminHost, final String rmiPort, final String isSecure) throws RuntimeException {
        MBeanServerConnection mbsc = null;
        try {
            final String protocol = AppserverConnectionSource.PROTOCOL_RMI;
            final int port = Integer.parseInt(rmiPort);
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