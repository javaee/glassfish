/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
package amxtest;

import java.util.Properties;
import java.util.Set;
import java.util.HashSet;

import org.testng.Reporter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Configuration;
import org.testng.annotations.ExpectedExceptions;
import org.testng.annotations.Test;
import org.testng.annotations.*;
import org.testng.Assert;


import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import java.net.MalformedURLException;
import java.io.IOException;
import org.glassfish.admin.amx.base.DomainRoot;
import org.glassfish.admin.amx.base.Query;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.admin.amx.core.proxy.AMXBooter;
import org.glassfish.admin.amx.core.proxy.ProxyFactory;
import org.glassfish.admin.amx.util.TimingDelta;

/** The base class for AMX tests
 */
public class AMXTestBase
{
    String mAdminUser;

    String mAdminPassword;

    String mHost;

    int mPort;

    boolean mDebug;

    private volatile MBeanServerConnection mMBeanServerConnection;

    private volatile DomainRoot mDomainRoot;

    private volatile Query mQueryMgr;

    private volatile Set<AMXProxy> mAllAMX;

    protected static void debug(final String s)
    {
        System.out.println("" + s);
    }

    AMXTestBase()
    {
        //debug("################################ AMXTestBase");
    }

    // might need these later: "admin.user", "admin.password"
    @BeforeClass(description = "get setup and connect to the MBeanServer")
    @Parameters(
    {
        "amx.debug", "amx.rmiport"
    })
    void setUpEnvironment(
            final boolean debug,
            final int port)
    {
        // defined in top-level build.xml
        mHost = System.getProperty("http.host");

        mDebug = debug;
        mPort = port;

        try
        {
            setup();
        }
        catch (Exception ex)
        {
            debug("AMXTestBase: Exception in setting up env. = " + ex);
            ex.printStackTrace();
        }
    }

    /**
    Subclasses may override if desired.  AMX will have been started
    and initialized already.
     */
    protected void setup()
    {
        //debug("################################ AMXTestBase.setup");

        final TimingDelta timing = new TimingDelta();
        final TimingDelta overall = new TimingDelta();

        try
        {
            mMBeanServerConnection = _getMBeanServerConnection();
            //debug( "AMXTestBase.setup(): millis to connect: " + timing.elapsedMillis() );
            mDomainRoot = _getDomainRoot(mMBeanServerConnection);
            //debug( "AMXTestBase.setup(): millis to boot AMX: " + timing.elapsedMillis() );
            mQueryMgr = getDomainRootProxy().getQueryMgr();
            //debug( "AMXTestBase.setup(): millis to get QueryMgr: " + timing.elapsedMillis() );

            mAllAMX = getQueryMgr().queryAll();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        //debug( "AMXTestBase.setup(): total setup millis: " + overall.elapsedMillis() );
    }

    protected Query getQueryMgr()
    {
        return mQueryMgr;
    }

    /** get all AMX MBeans that were found when the test started
    Caller should use the QueryMgr if a fresh set is needed */
    protected Set<AMXProxy> getAllAMX()
    {
        return mAllAMX;
    }

    protected <T> Set<T> getAll(final Class<T> intf)
    {
        return getAll(getAllAMX(), intf);
    }

    protected <T> Set<T> getAll(final Set<AMXProxy> all, final Class<T> intf)
    {
        final Set<T> result = new HashSet<T>();
        for (final AMXProxy amx : all)
        {
            if (intf.isAssignableFrom(amx.getClass()))
            {
                result.add(intf.cast(amx));
            }
        }
        return result;
    }

    protected DomainRoot getDomainRootProxy()
    {
        return mDomainRoot;
    }

    protected DomainRoot _getDomainRoot(final MBeanServerConnection conn)
            throws MalformedURLException, IOException, java.net.MalformedURLException
    {
        final ObjectName domainRootObjectName = AMXBooter.bootAMX(conn);
        final DomainRoot domainRoot = ProxyFactory.getInstance(conn).getDomainRootProxy();
        return domainRoot;
    }

    private MBeanServerConnection _getMBeanServerConnection()
            throws MalformedURLException, IOException
    {
        // service:jmx:rmi:///jndi/rmi://192.168.1.8:8686/jmxrmi
        // service:jmx:jmxmp://localhost:8888
        // CHANGE to RMI once it's working
        //
        // final String urlStr = "service:jmx:jmxmp://" + mHost + ":" + mPort;
        final String urlStr = "service:jmx:rmi:///jndi/rmi://" + mHost + ":" + mPort + "/jmxrmi";

        final JMXServiceURL url = new JMXServiceURL(urlStr);

        final JMXConnector jmxConn = JMXConnectorFactory.connect(url);
        //debug( "BaseAMXTest: connecting to: " + url );
        final MBeanServerConnection conn = jmxConn.getMBeanServerConnection();
        conn.getDomains();	// sanity check
        return conn;
    }

    protected static final String NL = System.getProperty("line.separator");

    protected static String getEnvString()
    {
        final Properties props = System.getProperties();
        final StringBuilder buf = new StringBuilder();
        buf.append("SYSTEM PROPERTIES:" + NL);
        for (final Object key : props.keySet())
        {
            buf.append(key);
            buf.append(" = ");
            buf.append("" + props.get(key) + NL);
        }
        final String result = buf.toString();
        return result;
    }

}









