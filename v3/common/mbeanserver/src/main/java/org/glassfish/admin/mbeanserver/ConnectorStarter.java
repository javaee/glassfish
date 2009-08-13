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

import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;

import java.io.IOException;

import org.glassfish.internal.api.AdminAccessController;

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
    protected final AdminAccessController mAuthenticator;
    protected final BootAMXListener mBootListener;
    protected volatile JMXServiceURL mJMXServiceURL = null;
    protected volatile JMXConnectorServer mConnectorServer = null;


    public JMXServiceURL getJMXServiceURL()
    {
        return mJMXServiceURL;
    }


    ConnectorStarter(
        final MBeanServer mbeanServer,
        final String address,
        final int port,
        final String authRealmName,
        final boolean securityEnabled,
        final AdminAccessController authenticator,
        final BootAMXListener bootListener)
    {
        mMBeanServer = mbeanServer;
        mAddress = address;
        mPort = port;
        mAuthRealmName = authRealmName;
        mSecurityEnabled = securityEnabled;
        mAuthenticator = authenticator;
        mBootListener = bootListener;


        if (securityEnabled)
        {
            throw new IllegalArgumentException("JMXConnectorServer not yet supporting security");
        }

    }


    abstract JMXConnectorServer start() throws Exception;


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







