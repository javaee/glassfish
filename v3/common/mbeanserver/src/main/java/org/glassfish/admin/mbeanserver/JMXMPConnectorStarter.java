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
import javax.management.ObjectName;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.NotCompliantMBeanException;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.jmxmp.JMXMPConnectorServer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.glassfish.api.amx.AMXUtil;


/**
Start and stop JMX connectors.
 */
final class JMXMPConnectorStarter
{
    protected static void debug(final String s)
    {
        System.out.println(s);
    }

    public static final String JMXMP = "jmxmp";
    
    private final MBeanServer mMBeanServer;

    private volatile JMXConnectorServer mJMXMP = null;

    private volatile JMXServiceURL mJMXMPServiceURL = null;

    private final String mAddress;
    private final int mPort;
    private final String mAuthRealmName;
    private final boolean mSecurityEnabled;

    public JMXServiceURL getJMXServiceURL()
    {
        return mJMXMPServiceURL;
    }

    JMXMPConnectorStarter(
        final MBeanServer mbeanServer,
        final String address,
        final int port,
        final String authRealmName,
        final boolean securityEnabled)
    {
        mMBeanServer = mbeanServer;
        mAddress = address;
        mPort = port;
        mAuthRealmName = authRealmName;
        mSecurityEnabled = securityEnabled;
    }

    public synchronized JMXConnectorServer start(final boolean tryOtherPorts)
    {
        if (mJMXMP != null)
        {
            return mJMXMP;
        }

        final int TRY_COUNT = tryOtherPorts ? 100 : 1;

        int port = mPort;
        int tryCount = 0;
        while (tryCount < TRY_COUNT)
        {
            try
            {
                mJMXMP = startJMXMPConnectorServer(port);
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
        return mJMXMP;
    }

    public synchronized void stop()
    {
        try
        {
            mJMXMP.stop();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    static void ignore(Throwable t)
    {
        // ignore
    }

    private JMXConnectorServer startJMXMPConnectorServer(final int port)
            throws MalformedURLException, IOException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException
    {
        final Map<String, Object> env = new HashMap<String, Object>();
        env.put("jmx.remote.protocol.provider.pkgs", "com.sun.jmx.remote.protocol");
        env.put("jmx.remote.protocol.provider.class.loader", this.getClass().getClassLoader());

        final JMXServiceURL serviceURL = new JMXServiceURL("service:jmx:" + JMXMP + "://" + Util.localhost() + ":" + port);
        JMXConnectorServer jmxmp = null;

        boolean startedOK = false;
        try
        {
            jmxmp = new JMXMPConnectorServer(serviceURL, env, mMBeanServer);
            
            jmxmp.start();
            startedOK = true;
        }
        catch( final Exception e )
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
                    if ( jmxmp != null ) jmxmp.stop();
                }
                catch (Exception e)
                {
                    ignore(e);
                }
            }
        }

        mJMXMPServiceURL = serviceURL;
        mJMXMP = jmxmp;

        // verify
        final JMXConnector jmxc = JMXConnectorFactory.connect(serviceURL, null);
        jmxc.getMBeanServerConnection().getMBeanCount();
        
        return mJMXMP;
    }
}







