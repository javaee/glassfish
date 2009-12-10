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
package org.glassfish.admin.mbeanserver;

import javax.management.MBeanServer;

import javax.management.remote.*;
import javax.security.auth.*;

import java.io.IOException;

import org.glassfish.internal.api.AdminAccessController;
import org.jvnet.hk2.component.*;

/**
Start and stop JMX connectors, base class.
 */
abstract class ConnectorStarter
{
    protected static void debug(final String s)
    {
        System.out.println(s);
    }
    protected final MBeanServer mMBeanServer;
    protected final String mAddress;
    protected final int mPort;
    protected final String mAuthRealmName;
    protected final boolean mSecurityEnabled;
    private   final Habitat mHabitat;
    protected final BootAMXListener mBootListener;
    protected volatile JMXServiceURL mJMXServiceURL = null;
    protected volatile JMXConnectorServer mConnectorServer = null;


    public JMXServiceURL getJMXServiceURL()
    {
        return mJMXServiceURL;
    }
    
    public String hostname()
    {
        if ( mAddress.equals("") || mAddress.equals("0.0.0.0") )
        {
            return Util.localhost();
        }
        
        return mAddress;
    }


    ConnectorStarter(
        final MBeanServer mbeanServer,
        final String address,
        final int port,
        final String authRealmName,
        final boolean securityEnabled,
        final Habitat habitat,
        final BootAMXListener bootListener)
    {
        mMBeanServer = mbeanServer;
        mAddress = address;
        mPort = port;
        mAuthRealmName = authRealmName;
        mSecurityEnabled = securityEnabled;
        mHabitat = habitat;
        mBootListener = bootListener;


        if (securityEnabled)
        {
            throw new IllegalArgumentException("JMXConnectorServer not yet supporting security");
        }

    }


    abstract JMXConnectorServer start() throws Exception;

    public JMXAuthenticator getAccessController() {

        // we return a proxy to avoid instantiating the jmx authenticator until it is actually
        // needed by the system.
        return new JMXAuthenticator() {

            /**
             * We actually wait for the first authentication request to delegate/
             * @param credentials
             * @return
             */
            public Subject authenticate(Object credentials) {
                // lazy init...
                // todo : lloyd, if this becomes a performance bottleneck, we should cache
                // on first access.
                JMXAuthenticator controller = mHabitat.getByContract(JMXAuthenticator.class);
                return controller.authenticate(credentials);
            }
        };
    }



    public synchronized void stop()
    {
        try
        {
            mConnectorServer.stop();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    static protected void ignore(Throwable t)
    {
        // ignore
    }
}







