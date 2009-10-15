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
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.NotCompliantMBeanException;

import javax.management.remote.*;
import javax.management.remote.jmxmp.JMXMPConnectorServer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.jvnet.hk2.component.*;

/**
Start and stop JMX connectors.
 */
final class JMXMPConnectorStarter extends ConnectorStarter
{
    JMXMPConnectorStarter(
        final MBeanServer mbeanServer,
        final String address,
        final int port,
        final String authRealmName,
        final boolean securityEnabled,
        final Habitat habitat,
        final BootAMXListener bootListener)
    {
        super(mbeanServer, address, port, authRealmName, securityEnabled, habitat, bootListener);
    }


    public synchronized JMXConnectorServer start()
    {
        if (mConnectorServer != null)
        {
            return mConnectorServer;
        }

        final boolean tryOtherPorts = false;
        final int TRY_COUNT = tryOtherPorts ? 100 : 1;

        int port = mPort;
        int tryCount = 0;
        while (tryCount < TRY_COUNT)
        {
            try
            {
                mConnectorServer = startJMXMPConnectorServer(port);
                break;
            }
            catch (final java.net.BindException e)
            {
            }
            catch (final Exception e)
            {
                throw new RuntimeException(e);
            }

            if (port < 1000)
            {
                port += 1000;   // in case it's a permissions thing
            }
            else
            {
                port = port + 1;
            }
        }
        return mConnectorServer;
    }

    public static final String JMXMP = "jmxmp";

    private JMXConnectorServer startJMXMPConnectorServer(final int port)
        throws MalformedURLException, IOException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException
    {
        final Map<String, Object> env = new HashMap<String, Object>();
        env.put("jmx.remote.protocol.provider.pkgs", "com.sun.jmx.remote.protocol");
        env.put("jmx.remote.protocol.provider.class.loader", this.getClass().getClassLoader());
        JMXAuthenticator authenticator = getAccessController();
        if (authenticator != null)
        {
            env.put("jmx.remote.authenticator", authenticator);
        }

        final JMXServiceURL serviceURL = new JMXServiceURL("service:jmx:" + JMXMP + "://" +hostname() + ":" + port);
        JMXConnectorServer jmxmp = null;

        boolean startedOK = false;
        try
        {
            jmxmp = new JMXMPConnectorServer(serviceURL, env, mMBeanServer);
            if ( mBootListener != null )
            {
                jmxmp.addNotificationListener(mBootListener, null, serviceURL.toString() );
            }

            jmxmp.start();
            startedOK = true;
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            // we do it this way so that the original exeption will be thrown out
            if (!startedOK)
            {
                try
                {
                    if (jmxmp != null)
                    {
                        jmxmp.stop();
                    }
                }
                catch (Exception e)
                {
                    ignore(e);
                }
            }
        }

        mJMXServiceURL  = serviceURL;
        mConnectorServer = jmxmp;

        // verify
        //final JMXConnector jmxc = JMXConnectorFactory.connect(serviceURL, null);
        //jmxc.getMBeanServerConnection().getMBeanCount();
        //jmxc.close();

        return mConnectorServer;
    }
}







