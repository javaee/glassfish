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

import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
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

    private final MBeanServer mMBeanServer;

    private volatile JMXConnectorServer mJMXMP = null;

    private volatile JMXServiceURL mJMXMPServiceURL = null;

    private volatile ObjectName mJMXMPObjectName = null;

    public static final int JMXMP_PORT = 8888;

    public JMXServiceURL getJMXServiceURL()
    {
        return mJMXMPServiceURL;
    }

    private static volatile boolean STARTED = false;

    JMXMPConnectorStarter(final MBeanServer mbs)
    {
        mMBeanServer = mbs;
    }

    public synchronized JMXConnectorServer start()
    {
        if (STARTED)
        {
            return mJMXMP;
        }

        final int TRY_COUNT = 100;

        int port = JMXMP_PORT;
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
    }

    private JMXConnectorServer startJMXMPConnectorServer(int port)
            throws MalformedURLException, IOException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException
    {
        if (mJMXMPObjectName == null)
        {
            final Map<String, Object> env = new HashMap<String, Object>();
            env.put("jmx.remote.protocol.provider.pkgs", "com.sun.jmx.remote.protocol");
            env.put("jmx.remote.protocol.provider.class.loader", this.getClass().getClassLoader());

            final JMXServiceURL serviceURL = new JMXServiceURL("service:jmx:jmxmp://localhost:" + port);
            final JMXConnectorServer jmxmp = JMXConnectorServerFactory.newJMXConnectorServer(serviceURL, env, mMBeanServer);

            ObjectName objectName = AMXUtil.newObjectName("jmxremote:type=jmx-connector,name=jmxmp,port=" + port);
            objectName = mMBeanServer.registerMBean(jmxmp, objectName).getObjectName();

            boolean startedOK = false;
            try
            {
                // start it only if we can register it successsfully
                jmxmp.start();
                startedOK = true;
            }
            finally
            {
                // we do it this way so that the original exeption will be thrown out
                if (!startedOK)
                {
                    try
                    {
                        jmxmp.stop();
                    }
                    catch (Exception e)
                    {
                        ignore(e);
                    }
                    try
                    {
                        mMBeanServer.unregisterMBean(objectName);
                        objectName = null;
                    }
                    catch (Exception e)
                    {
                        ignore(e);
                    }
                }
            }

            mJMXMPServiceURL = serviceURL;
            mJMXMP = jmxmp;
            mJMXMPObjectName = objectName;
            System.out.println("JMXMP connector server URL = " + mJMXMPServiceURL);

            // test
            // JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
            // MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
        }
        return mJMXMP;
    }

}




