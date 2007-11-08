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
package com.sun.enterprise.management.util.jmx;

import java.util.Set;
import java.util.HashSet;

import java.io.IOException;

import javax.management.*;

import com.sun.appserv.management.util.jmx.NotificationListenerBase;
import com.sun.appserv.management.util.jmx.MBeanRegistrationListener;
import com.sun.appserv.management.util.jmx.JMXUtil;


public class NotificationListenerBaseTest extends JMXTestBase
{
    private MBeanServer   mServer;
    
    private static final String TEST    = "t";
    
        public 
    NotificationListenerBaseTest()
    {
        mServer = null;
    }
    
		protected ObjectName
	registerMBean( final Object mbean, final ObjectName objectName )
		throws InstanceAlreadyExistsException,
		NotCompliantMBeanException, MBeanRegistrationException
	{
		return mServer.registerMBean( mbean, objectName ).getObjectName();
	}
	
		public void
	setUp() throws Exception
	{
	    mServer  = MBeanServerFactory.newMBeanServer( "test" );
	}
	
		public void
	tearDown()
		throws Exception
	{
	    mServer = null;
	}
	
	private static final class MyListener extends NotificationListenerBase
	{
    	    public
    	MyListener(
    	    final MBeanServerConnection conn,
    	    final ObjectName            pattern )
    		throws InstanceNotFoundException, IOException
    	{
    	    super( "MyListener", conn, pattern, null );
    	}
    	
    		public
    	MyListener(
    	    final MBeanServerConnection conn,
    	    final ObjectName            pattern,
    	    final NotificationFilter    filter )
    		throws InstanceNotFoundException, IOException
    	{
    	    super( "MyListener", conn, pattern, filter );
    	}
    	
    	    public void
	    handleNotification( final Notification notif, final Object handback)
	    {
	    }
    }
	
	
	    private MyListener
	create( final String pattern )
	    throws Exception
	{
	    final MyListener  listener   = 
	        new MyListener( mServer, JMXUtil.newObjectName( pattern ), null );
	    
        listener.startListening();
	    return listener;
	}
	
	    public void
	testCreate()
	    throws Exception
	{
	    final MyListener  b   = create( "*:*" );
	    assert( b.getListenees().size() != 0 );
	    b.getMBeanServerConnection();
	    b.cleanup();
	    assert( b.getListenees().size() == 0 );
	}
	
	public interface DummyMBean extends NotificationEmitter
	{
	    int     getDummy();
	    void    setDummy( int value );
	}
	
	public static final class Dummy 
	    extends NotificationBroadcasterSupport
	    implements DummyMBean
	{
	    private int mDummy;
	    
	    public int     getDummy()  { return mDummy; }
	    public void    setDummy( int value )   { mDummy = value; }
	}
	
	static private final String DUMMY_OBJECT_NAME_PREFIX    = TEST + ":type=dummy";
	
    
        private ObjectName
    createObjectName(
        final String    domain,
        final int       id,
        final String    name )
    {
       String s   = domain + ":" +
            "name=" + name +
            ",id=" + id +
            (",category=" + ((id % 2) == 0 ? "even":"odd"));
	        
	   return JMXUtil.newObjectName( s );
    }
    
        Set<ObjectName>
	registerDummies(
	    final int    count,
	    final String name )
	    throws Exception
	{
	    return registerDummies( TEST, count, name );
	}
	
	    Set<ObjectName>
	registerDummies(
	    final String    domain,
	    final int    count,
	    final String name )
	    throws Exception
	{
	    final Set<ObjectName>   s   = new HashSet<ObjectName>();
	    
	    for( int i = 0; i < count; ++i )
	    {
	        final Dummy d   = new Dummy();
	        d.setDummy( i );
	        
	        final ObjectName    on  = createObjectName( domain, i, name );
	        final ObjectName    objectName  = registerMBean( d, on );
	        
	        s.add( objectName );
	    }
	    
	    return s;
	}
	    
	    public void
	testGetListenees()
	    throws Exception
	{
	    final int    NUM = 10;
	    final String NAME   = "bbb";
	    
	    registerDummies( NUM, NAME );
	    
	    final MyListener  odd   = create( TEST + ":category=odd,*" );
	    assert( odd.getListenees().size() == NUM / 2 );
	    for( final ObjectName item : odd.getListenees() )
	    {
	        assert( item.getKeyProperty( "category" ).equals( "odd" ) );
	    }
	    assert( odd.isAlive() );
	    
	    final MyListener  even   = create( TEST + ":category=even,*" );
	    assert( even.getListenees().size() == NUM / 2 );
	    for( final ObjectName item : even.getListenees() )
	    {
	        assert( item.getKeyProperty( "category" ).equals( "even" ) );
	    }
	    assert( even.isAlive() );
	    
	    final MyListener  all   = create( TEST + ":name=bbb,*" );
	    assert( all.getListenees().size() == NUM );
	    for( final ObjectName item : all.getListenees() )
	    {
	        assert( item.getKeyProperty( "name" ).equals( NAME ) );
	    }
	    assert( all.isAlive() );
	}
	
	
	private final class MyRegListener extends MBeanRegistrationListener
	{
	    public int mRegCount   = 0;
	    public int mUnregCount = 0;
	    
    	    public
    	MyRegListener( final ObjectName constrain )
    		throws InstanceNotFoundException, IOException
    	{
    	    super( "MyRegListener", mServer, constrain );
    	}
    	
            protected void
        mbeanRegistered( final ObjectName objectName )
        {
            ++mRegCount;
        }
        
            protected void
        mbeanUnregistered( final ObjectName objectName )
        {
            ++mUnregCount;
        }
    }
    
        private void
	testListenForRegistration(
	    final String    domain,
	    final String    name,
	    final int       count,
	    final String    constrainStr)
	    throws Exception
	{
	    final ObjectName    constrain = (constrainStr == null) ? null :
	        JMXUtil.newObjectName( constrainStr );
	    
	    final MyRegListener listener = new MyRegListener( constrain );
        listener.startListening();
	    assert( listener.getListenees().size() == 1 );
	    
	    final Set<ObjectName>   s   = registerDummies( domain, count, name );
	    assert( listener.mRegCount == count );
	    
	    for( final ObjectName objectName : s )
	    {
	        mServer.unregisterMBean( objectName );
	    }
	    assert( listener.mUnregCount == count );
	    listener.cleanup();
	    assert( listener.getListenees().size() == 0 );
	    
	}
	
	
    static private final String REG_DOMAIN = "LFR";
    static private  final int    REG_COUNT   = 1000;
    
	    public void
	testListenForRegistration1()
	    throws Exception
	{
	    final String NAME   = "zzz";
	    testListenForRegistration( REG_DOMAIN, NAME, REG_COUNT, null );
	    testListenForRegistration( REG_DOMAIN, NAME, REG_COUNT, "*:*");
	    testListenForRegistration( REG_DOMAIN, NAME, REG_COUNT, REG_DOMAIN +":*");
	    testListenForRegistration( REG_DOMAIN, NAME, REG_COUNT, "*:" + "name=" + NAME);
	}
	
	   public void
	testListenForRegistration2()
	    throws Exception
	{
	    final String NAME   = "zzzz";
	    testListenForRegistration( REG_DOMAIN, NAME, REG_COUNT, null );
	    testListenForRegistration( REG_DOMAIN, NAME, REG_COUNT, "*:*");
	    testListenForRegistration( REG_DOMAIN, NAME, REG_COUNT, REG_DOMAIN +":*");
	    testListenForRegistration( REG_DOMAIN, NAME, REG_COUNT, "*:" + "name=" + NAME);
	}
	
	   public void
	testListenForRegistration3()
	    throws Exception
	{
	    final String NAME   = "zzzzz";
	    testListenForRegistration( REG_DOMAIN, NAME, REG_COUNT, null );
	    testListenForRegistration( REG_DOMAIN, NAME, REG_COUNT, "*:*");
	    testListenForRegistration( REG_DOMAIN, NAME, REG_COUNT, REG_DOMAIN +":*");
	    testListenForRegistration( REG_DOMAIN, NAME, REG_COUNT, "*:" + "name=" + NAME);
	}
}


























