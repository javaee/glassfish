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
package amxtest;

import java.util.Properties;
import java.util.Set;
import java.util.HashSet;

import org.testng.Reporter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import java.net.MalformedURLException;
import java.io.IOException;

import com.sun.appserv.management.client.AMXBooter;
import com.sun.appserv.management.client.ProxyFactory;
import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.base.QueryMgr;
import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.util.misc.TimingDelta;


/** The base class for AMX tests
 */
public class AMXTestBase {
    String mAdminUser;
    String mAdminPassword;
    String mHost;
    int	   mPort;
    boolean mDebug;
    EffortLevel mEffortLevel;
    
    private volatile MBeanServerConnection mMBeanServerConnection;
    private volatile DomainRoot mDomainRoot;
    private volatile QueryMgr   mQueryMgr;
    private volatile Set<AMX> 	mAllAMX;
    
    protected static void debug( final String s )
    {
    	System.out.println( "" + s);
    }
    
    AMXTestBase()
    {
    }
    
    // might need these later: "admin.user", "admin.password"
    @BeforeClass(description="get setup and connect to the MBeanServer")
    @Parameters({"amx.debug", "amx.port", "amx.effortLevel"})
    void setUpEnvironment(
    	final boolean debug,
    	final int     port,
    	final String  effortLevel)
    {
        // defined in top-level build.xml
        mHost = System.getProperty( "http.host" );
        
        mDebug = debug;
        mPort = port;
        mEffortLevel = EffortLevel.valueOf(effortLevel);
        debug( "AMXTestBase: EffortLevel = " + mEffortLevel);
        
        setup();
    }
    
    /**
    	Subclasses may override if desired.  AMX will have been started
    	and initialized already.
     */
    protected void setup()
    {
    	final TimingDelta timing = new TimingDelta();
    	final TimingDelta overall = new TimingDelta();
    	
    	try
    	{
			mMBeanServerConnection = _getMBeanServerConnection();
    		debug( "AMXTestBase.setup(): millis to connect: " + timing.elapsedMillis() );
			mDomainRoot = _getDomainRoot(mMBeanServerConnection);
    		debug( "AMXTestBase.setup(): millis to boot AMX: " + timing.elapsedMillis() );
			mQueryMgr   = getDomainRoot().getQueryMgr();
    		debug( "AMXTestBase.setup(): millis to get QueryMgr: " + timing.elapsedMillis() );
    		
    		mAllAMX = getQueryMgr().queryAllSet();
    	}
    	catch( Exception e )
    	{
    		throw new RuntimeException(e);
    	}
    	debug( "AMXTestBase.setup(): total setup millis: " + overall.elapsedMillis() );
    }
    
    protected EffortLevel getEffortLevel() { return mEffortLevel; }
    
    protected QueryMgr getQueryMgr()
    {
    	return mQueryMgr;
    }
    
    /** get all AMX MBeans that were found when the test started
    	Caller should use the QueryMgr if a fresh set is needed */
    protected Set<AMX> getAllAMX()
    {
    	return mAllAMX;
    }
    
    protected <T> Set<T> getAll(final Class<T> intf )
    {
    	return getAll( getAllAMX(), intf );
    }
    
    protected <T> Set<T> getAll( final Set<AMX> all, final Class<T> intf )
    {
    	final Set<T> result = new HashSet<T>();
    	for( final AMX amx : all )
    	{
    		if ( intf.isAssignableFrom( amx.getClass() ) )
    		{
    			result.add( intf.cast(amx) );
    		}
    	}
    	return result;
    }
    
    protected DomainRoot getDomainRoot()
    {
    	return mDomainRoot;
    }
    
    protected DomainRoot _getDomainRoot( final MBeanServerConnection conn)
    	throws MalformedURLException, IOException, java.net.MalformedURLException
    {
		final ObjectName domainRootObjectName = AMXBooter.bootAMX(conn);
		final DomainRoot domainRoot = ProxyFactory.getInstance(conn).getDomainRoot();
		return domainRoot;
    }
    
    private MBeanServerConnection _getMBeanServerConnection()
    	throws MalformedURLException, IOException
    {
    	// service:jmx:rmi:///jndi/rmi://192.168.1.8:8686/jmxrmi
    	// service:jmx:jmxmp://localhost:8888
    	// CHANGE to RMI once it's working
    	//
    	final String urlStr = "service:jmx:jmxmp://" + mHost + ":" + mPort;
    	
    	final JMXServiceURL url = new JMXServiceURL(urlStr);
    	
        final JMXConnector jmxConn = JMXConnectorFactory.connect(url);
        debug( "BaseAMXTest: connecting to: " + url );
    	final MBeanServerConnection conn = jmxConn.getMBeanServerConnection();
    	conn.getDomains();	// sanity check
    	return conn;
    }
    
    protected static final String NL = System.getProperty("line.separator");
    
    protected static String getEnvString()
    {
        final Properties props = System.getProperties();
    	final StringBuilder buf = new StringBuilder();
    	buf.append( "SYSTEM PROPERTIES:" + NL );
        for( final Object key : props.keySet() )
        {
            buf.append(key);
            buf.append(" = ");
            buf.append( "" + props.get(key) + NL );
        }
        final String result = buf.toString();
        return result;
    }
}









