/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
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