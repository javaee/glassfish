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
 
/*
 * $Header: /cvs/glassfish/admin/mbeans/tests/com/sun/enterprise/admin/dottedname/DottedNameGetSetMBeanImplTest.java,v 1.3 2005/12/25 03:43:05 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:43:05 $
 */
 

package com.sun.enterprise.admin.dottedname;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanServerFactory;
import javax.management.JMException;
import javax.management.Attribute;
import javax.management.AttributeList;

import com.sun.enterprise.admin.mbeans.DottedNameGetSetMBeanImpl;
import com.sun.enterprise.admin.dottedname.DottedNamePropertySupport;
import com.sun.enterprise.admin.util.ArrayConversion;
	


public final class DottedNameGetSetMBeanImplTest extends junit.framework.TestCase
{
	MBeanServer				mServer;
	
	DottedNameRegistry		mRegistry;
	DottedNameRegistry		mMonitoringRegistry;
	DottedNameGetSetMBean	mGetSetMBean;
	
		public
	DottedNameGetSetMBeanImplTest(  )
	{
	}
	
	
	//-----------------------------------------------------------------------------------------
	
	private static final String	DOMAIN = DottedNameAliasSupport.DOMAIN_SCOPE;
	private static final String	SERVER_NAME_BASE		= "server";
	private static final String	CONFIG_SUFFIX			= "-config";
	private static final String	TESTEE_OBJECTNAME_BASE	= "Test:name=testee";
	private static final int	NUM_SERVERS				= 2;
	
	static private String []	DOMAIN_SUBPARTS	= (String [])ArrayConversion.setToArray(
			DottedNameAliasSupport.DOMAIN_PARTS, new String [ DottedNameAliasSupport.DOMAIN_PARTS.size() ] );
			
	private static final int	NUM_DOMAIN_SUBPARTS		= DOMAIN_SUBPARTS.length;
	
	private static final String	SIMPLE		= "simple";
	private static final String	SIMPLE_NAME	= SIMPLE;
	
	
	private static final char	BACKSLASH	= '\\';
	
	// name will be "funky\.test" (literal '\' and literal '.' )
	//  must be registered as "funky\\\.test"
	private static final String	FUNKY_DOTTED_NAME	= "test" + BACKSLASH + BACKSLASH +
														BACKSLASH + ".";
	private static final String FUNKY_ID			= "funky";
	
	/*
		Implement a ServerInfo to satisfy our testee, DottedNameGetSetMBeanImpl.
	*/
	static private class StubbedServerInfo implements DottedNameServerInfo 
	{
			public
		StubbedServerInfo()
		{
		}
		
		private static final Set	SERVER_NAME_SET	= initServerNames( NUM_SERVERS );
		private static final Set	CONFIG_NAME_SET	= initConfigNames( NUM_SERVERS );
			
			private static Set
		initServerNames( int numNames )
		{
			final HashSet	s	= new HashSet();
			
			// generate server names server0, server1, etc
			for( int i = 0; i < numNames; ++i )
			{
				s.add( getServerDottedName( "" + i ) );
			}
			
			return( Collections.unmodifiableSet( s ) );
		}
		
			private static Set
		initConfigNames( int numNames )
		{
			final HashSet	s	= new HashSet();
			
			// generate config names server0-config, server1-config, etc
			for( int i = 0; i < numNames; ++i )
			{
				s.add( getConfigDottedName( "" + i ) );
			}
			
			return( Collections.unmodifiableSet( s ) );
		}
			
			public Set
		getServerNames()
		{
			return( SERVER_NAME_SET );
		}
		
			public Set
		getConfigNames()
		{
			return( CONFIG_NAME_SET );
		}
		
			public String
		getConfigNameForServer( String serverName )
			throws DottedNameServerInfo.UnavailableException
		{
			if ( SERVER_NAME_SET.contains( serverName ) )
			{
				return( serverName + CONFIG_SUFFIX );
			}
			
			throw new DottedNameServerInfo.UnavailableException( "" );
		}
		
			public String []
		getServerNamesForConfig( String configName )
			throws DottedNameServerInfo.UnavailableException
		{
			final java.util.Iterator iter	= getServerNames().iterator();
			final java.util.ArrayList	namesOut	= new java.util.ArrayList();
			
			while ( iter.hasNext() )
			{
				final String	serverName	= (String)iter.next();
				
				if ( configName.equals( getConfigNameForServer( serverName ) ) )
				{
					namesOut.add( serverName );
				}
			}
			
			final String []	namesOutArray	= new String [ namesOut.size() ];
			namesOut.toArray( namesOutArray );
			
			return( namesOutArray );
		}
	}
	
	//-----------------------------------------------------------------------------------------
	
	/*
		Extend our testee, DottedNameGetSetMBeanImpl, so that we can supply a stubbed ServerInfo
		to it.
	*/
	static private class HookedDottedNameGetSetMBeanImpl
		extends DottedNameGetSetMBeanImpl
		implements DottedNameGetSetMBean
	{
			public
		HookedDottedNameGetSetMBeanImpl(
			final MBeanServerConnection conn,
			final DottedNameRegistry registry,
			final DottedNameRegistry monitoringRegistry )
			throws Exception
		{
			super( conn, registry, monitoringRegistry );
			
			// we don't want to see log messages during unit testing
			DottedNameLogger.getInstance().setLevel( java.util.logging.Level.SEVERE );
		}
		
	
			protected DottedNameServerInfo
		createServerInfo( MBeanServerConnection conn )
		{
			return( new StubbedServerInfo( ) );
		}
		
	}
	
	//-----------------------------------------------------------------------------------------

	
	//-----------------------------------------------------------------------------------------
	
		static private TesteeMBean
	createTestee()
	{
		final TesteeMBean testee	= new Testee();
		
		return( testee );
	}
	
		private static String
	getServerDottedName( String id )
	{
		return( SERVER_NAME_BASE + id );
	}
	
		private static String
	getMonitoringDottedName( String id )
	{
		return( getServerDottedName( id ) );
	}
	
		private static String
	getConfigDottedName( String id )
	{
		return( getServerDottedName( id ) + CONFIG_SUFFIX );
	}
	
	
		ObjectName
	registerAndAddTestee(
		final Object		mbean,
		final String		dottedName,
		final String		id,
		final String		category,
		DottedNameRegistry	registry )
		throws JMException
	{
		final ObjectName	objectName	= new ObjectName( TESTEE_OBJECTNAME_BASE +
								",id=" + id +
								",category=" + category );
		
		mServer.registerMBean( mbean, objectName );

		registry.add(  dottedName, objectName );
		assert( registry.dottedNameToObjectName( dottedName ) == objectName );
		
		return( objectName );
	}
	
		ObjectName
	registerAndAddTestee(
		final String		dottedName,
		final String		id,
		final String		category,
		DottedNameRegistry	registry )
		throws JMException
	{
		final ObjectName	objectName	=
			registerAndAddTestee( createTestee(), dottedName, id, category, registry );
		
		return( objectName );
	}
	
	final String	CATEGORY_CONFIG		= "config";
	final String	CATEGORY_MONITORING	= "monitoring";
	final String	CATEGORY_SPECIAL	= "special";
	
		public void
	setUp()
		throws Exception
	{
		
		mServer	= MBeanServerFactory.newMBeanServer( "" + System.currentTimeMillis() );
		
		mRegistry			= new DottedNameRegistry1To1Impl();
		mMonitoringRegistry	= new DottedNameRegistry1To1Impl();
		
		mGetSetMBean = new HookedDottedNameGetSetMBeanImpl( mServer, mRegistry, mMonitoringRegistry );
		mServer.registerMBean( mGetSetMBean, new ObjectName( ":name=get-set-testee" ) );
		
		
		/*
			Set up configuration testees
		 */
		for( int i = 0; i < NUM_SERVERS; ++i )
		{
			final String	id	= "" + i;
			final String	dottedName	= getConfigDottedName( id );
			
			registerAndAddTestee( dottedName, id, CATEGORY_CONFIG, mRegistry );
		}
		// simulate the domain itself with some children
		final ObjectName	on	= registerAndAddTestee( DOMAIN, DOMAIN, CATEGORY_CONFIG, mRegistry );
		for( int i = 0; i < DOMAIN_SUBPARTS.length; ++i )
		{
			final String part	= DOMAIN_SUBPARTS[ i ];
			
			registerAndAddTestee( DOMAIN + "." + part, part, CATEGORY_CONFIG, mRegistry );
		}
		
		
		//dm( com.sun.cli.util.stringifier.MBeanInfoStringifier.DEFAULT.stringify( mServer.getMBeanInfo( on ) ) );
		
		/*
			Set up monitoring testees
		 */
		for( int i = 0; i < NUM_SERVERS; ++i )
		{
			final String	id	= "" + i;
			final String	dottedName	= getMonitoringDottedName( id );
			
			registerAndAddTestee( dottedName, id, CATEGORY_MONITORING, mMonitoringRegistry);
		}
		// simulate the domain itself
		registerAndAddTestee( DOMAIN,
			DOMAIN, CATEGORY_MONITORING, mMonitoringRegistry);
			
		// Register an MBean with no properties
		SIMPLE_OBJECT_NAME	= registerAndAddTestee( new MonitoringTestee(), SIMPLE_NAME,
			SIMPLE, CATEGORY_MONITORING, mMonitoringRegistry);
	}

ObjectName SIMPLE_OBJECT_NAME	= null;

		static void
	dm( Object o )
	{
		System.out.println( o.toString() );
	}
	
		public void
	tearDown()
	{
		mServer	= null;
	}
		
		private static String
	valueName( String prefix, String valueName )
	{
		return( prefix + "." + valueName );
	}
	
		Attribute
	getAttribute( String name )
	{
		final Object  result	= mGetSetMBean.dottedNameGet( name );
		
		if ( result instanceof Exception )
		{
			((Exception)result).printStackTrace();
		}

		assert( result instanceof Attribute ): "expecting Attribute, got " + result.getClass().getName();
		return( (Attribute)result );
	}
	
	private static final String	PRIMARY_TESTEE	= getServerDottedName( "0" );
		static String
	testeeValueName( String name )
	{
		return( PRIMARY_TESTEE + "." + name );
	}
	
	
	
		Attribute
	getMonitoringAttribute( String name )
	{
		final Object result	= mGetSetMBean.dottedNameMonitoringGet( name );
		
		assert( result instanceof Attribute ) :
			"expecting single Attribute, got " + result.getClass().getName();
		return( (Attribute)result );
	}
	
		public void
	testGetchar() 
	{
		final String	dn	= testeeValueName( "char" );
		assertEquals( getAttribute( dn ), new Attribute( dn, new Character( 'c' ) ) );
	}
	
	
		public void
	testGetbyte()
	{
		final String	dn	= testeeValueName( "byte" );
		assertEquals( getAttribute( dn ), new Attribute( dn, new Byte( (byte)0 ) ) );
	}
	
		public void
	testGetshort()
	{
		final String	dn	= testeeValueName( "short" );
		assertEquals( getAttribute( dn ), new Attribute( dn, new Short( (short)0 ) ) );
	}
	
		public void
	testGetint() throws Exception
	{
		final String dn	= testeeValueName( "int" );
		assertEquals( getAttribute( dn ), new Attribute( dn, new Integer( 0 ) ) );
	}
	
		public void
	testGetlong() throws Exception
	{
		final String dn	= testeeValueName( "long" );
		assertEquals( getAttribute( dn ), new Attribute( dn, new Long( 0 ) ) );
	}
	
		public void
	testGetfloat() throws Exception
	{
		final String dn	= testeeValueName( "float" );
		assertEquals( getAttribute( dn ), new Attribute( dn, new Float( 0.0 ) ) );
	}
	
		public void
	testGetdouble() throws Exception
	{
		final String dn	= testeeValueName( "double" );
		assertEquals( getAttribute( dn ), new Attribute( dn, new Double( 0.0 ) ) );
	}
	
	
		public void
	testGetString()throws Exception
	{
		final String dn	= testeeValueName( "String" );
		assertEquals( getAttribute( dn ), new Attribute( dn, "" ) );
	}
	
		public void
	testGetStringArray()throws Exception
	{
		final String dn	= testeeValueName( "StringArray" );
		
		final Attribute	attr	= getAttribute( dn );
		assertEquals( 0, ((String [])attr.getValue()).length );
	}
	
		public void
	testGetWild()throws Exception
	{
		final Attribute []	attrs	= (Attribute [])mGetSetMBean.dottedNameGet( "*.StringArray" );
		
		// will only look in "server.*" namespace
		assertEquals( NUM_SERVERS + NUM_SERVERS * NUM_DOMAIN_SUBPARTS, attrs.length );
	}
	
		public void
	testGetWildProperty()throws Exception
	{
		final Attribute []	attrs	= (Attribute [])mGetSetMBean.dottedNameGet( PRIMARY_TESTEE + ".property.*" );
		assertEquals( 1, attrs.length );
	}
		static String
	attributeToString( final Attribute attr )throws Exception
	{
		return( attr.getName() + " = " + attr.getValue().toString() );
	}
		static String
	attributeListToString( final AttributeList attrs )throws Exception
	{
		final StringBuffer	buf	= new StringBuffer();
		
		for( int i = 0; i < attrs.size(); ++i )
		{
			buf.append( attributeToString( (Attribute)attrs.get( i ) ) );
			buf.append( "\n" );
		}
		return( buf.toString() );
	}
	
		String
	arrayToString( Object [] a )throws Exception
	{
		final StringBuffer	buf	= new StringBuffer();
		
		buf.append( "{" );
		for( int i = 0; i < a.length; ++i )
		{
			buf.append( a[ i ].toString() );
			buf.append( ", " );
		}
		buf.append( "}" );
		
		return( buf.toString() );
	}
	
		public void
	testServerAliasIntoDomain()throws Exception
	{
		// eg "server0.applications"
		final String	target	= getServerDottedName( "0" ) + "." + DOMAIN_SUBPARTS[ 0 ];
		
		getAttribute( target + ".int" );
	}
	
		public synchronized void
	testWildcardGetAll()
	{
		final Attribute[]	list1	= (Attribute[])mGetSetMBean.dottedNameGet( "*" );
		final Attribute[]	list2	= (Attribute[])mGetSetMBean.dottedNameGet( "*.*" );
		final Attribute[]	list3	= (Attribute[])mGetSetMBean.dottedNameGet( "***.***" );
		final Attribute[]	list4	= (Attribute[])mGetSetMBean.dottedNameGet( "**********.*" );
		
		final int	expectedSize	= list1.length;
		assert( expectedSize != 0 );
		assertEquals( expectedSize, list2.length );
		assertEquals( expectedSize, list3.length );
		assertEquals( expectedSize, list4.length );
	}
	
	
		public synchronized void
	testWildcardMonitoringGetAll()
	{
		final Attribute[]	list1	= (Attribute[])mGetSetMBean.dottedNameMonitoringGet( "*" );
		final Attribute[]	list2	= (Attribute[])mGetSetMBean.dottedNameMonitoringGet( "*.*" );
		final Attribute[]	list3	= (Attribute[])mGetSetMBean.dottedNameMonitoringGet( "***.***" );
		final Attribute[]	list4	= (Attribute[])mGetSetMBean.dottedNameMonitoringGet( "**********.*" );
		
		final int	expectedSize	= list1.length;
		assert( expectedSize != 0 );
		assertEquals( expectedSize, list2.length );
		assertEquals( expectedSize, list3.length );
		assertEquals( expectedSize, list4.length );
	}
	
	
		public synchronized void
	testWildcardGetAllMonitor()
	{
		final Attribute[]	list	= (Attribute[])mGetSetMBean.dottedNameMonitoringGet( "*" );
		assert( list.length != 0 );
	}
	
		public synchronized void
	testNoProperties() throws Exception
	{
		mServer.getMBeanInfo(SIMPLE_OBJECT_NAME );
		// the MonitoringTestee has no properties
		final Object	result	= mGetSetMBean.dottedNameMonitoringGet( SIMPLE + ".property.foo" );
		
		assert( result instanceof AttributeNotFoundException ) :
			"expected AttributeNotFoundException, got " + result.getClass().getName() + 
			" at ";
	}
	
		public void
	testWildcardListEmpty()
	{
		assert( mGetSetMBean.dottedNameList( new String [ 0 ] ).length != 0 );
	}
	
		public synchronized void
	testWildcardListAll()
	{
		final int	length1	= dottedNameList( "*" ).length;
		
		assert( length1 != 0 );
	}
	
		public synchronized void
	testWildcardMonitoringListAll()
	{
		assert( dottedNameMonitoringList( "*" ).length != 0 );
	}
	
		public void
	testWildcardAllGetsOnlyServers()
	{
		// '*' has special meaning of only getting the server.xxx names, *not* domain.xxx or config.xxx
		final String []	names	= dottedNameList( "*" );
		
		final Set	serverNames	= new StubbedServerInfo().getServerNames();
		for( int i = 0; i < names.length; ++i )
		{
			final DottedName	dn	= DottedNameFactory.getInstance().get( names[ i ] );
			
			assert( serverNames.contains( dn.getScope() ) );
		}
	}
	
		public void
	testDomainGetsOnlyDomain()
	{
		// '*' has special meaning of only getting the server.xxx names, *not* domain.xxx or config.xxx
		final String []	names	= dottedNameList( "domain*" );
		
		for( int i = 0; i < names.length; ++i )
		{
			final DottedName	dn	= DottedNameFactory.getInstance().get( names[ i ] );
			
			assertEquals( dn.getScope(), DottedNameAliasSupport.DOMAIN_SCOPE );
		}
	}
	
		public void
	testConfigGetsOnlyConfig()
	{
		// '*' has special meaning of only getting the server.xxx names, *not* domain.xxx or config.xxx
		final String []	names	= dottedNameList( getConfigDottedName( "0" ) + "*" );
		
		for( int i = 0; i < names.length; ++i )
		{
			final DottedName	dn	= DottedNameFactory.getInstance().get( names[ i ] );
			
			assertEquals( dn.getScope(), getConfigDottedName( "0" ) );
		}
	}
	
	
		public void
	testGetWildAttr()
	{
		Attribute[]	attrs	= (Attribute[])mGetSetMBean.dottedNameGet( "*.StringArray*" );
		// we're using '*' which will select only server.xxx names.  There should be one
		// name for each server itself, and NUM_DOMAIN_SUBPARTS additional names per server.
	
		assertEquals( NUM_SERVERS + NUM_SERVERS * NUM_DOMAIN_SUBPARTS, attrs.length );
		
		attrs	= (Attribute[])mGetSetMBean.dottedNameGet("*.*int*" );
		assertEquals( NUM_SERVERS + NUM_SERVERS * NUM_DOMAIN_SUBPARTS, attrs.length );
	}
	
		public void
	testGetWildAttrSingleMBean()
	{
		// this should get all 17 attributes and 1 property and 1 attribute called "Properties"
		final Attribute[]	attrs	=
			(Attribute[])mGetSetMBean.dottedNameGet( PRIMARY_TESTEE + ".*" );
			
		assertEquals( 17 + 1 + 1, attrs.length );
	}
	
		public void
	testMultiGet()
	{
		final String []	params	= new String []
		{
			testeeValueName( "char" ),
			testeeValueName( "int" ),
			testeeValueName( "float" ),
			testeeValueName( "Double" ),
		};
		
		final Attribute[]	attrs	= (Attribute[])mGetSetMBean.dottedNameGet( params );
			
		assertEquals( params.length, attrs.length );
		for( int i = 0; i < attrs.length; ++i )
		{
			assert( attrs[ i ] instanceof Attribute );
		}
	}
	
		Attribute
	find( Object[] attrs, String name )
	{
		Attribute	attr	= null;
		
		final int size	= attrs.length;
		for( int i = 0; i < size; ++i )
		{
			final Attribute	candidate	= (Attribute)attrs[ i ];
			
			if ( candidate.getName().equals( name ) )
			{
				attr	= candidate;
				break;
			}
		}
		return( attr );
	}
	
		public synchronized void
	testMultiSet()
	{
		final String []	params	= new String []
		{
			testeeValueName( "int" ) + "=99",
			testeeValueName( "char" ) + "=x",
			testeeValueName( "String" ) + "=9.999",
			testeeValueName( "Double" ) + "=9.999",
		};
		
		final Object[]	attrs	= mGetSetMBean.dottedNameSet( params );
		assertEquals( params.length, attrs.length );

		assertEquals(	new Attribute( testeeValueName( "int" ), new Integer( 99 ) ),
						find( attrs, testeeValueName( "int" )));
						

		assertEquals(	new Attribute( testeeValueName( "String" ), "9.999" ),
						find( attrs, testeeValueName( "String" )));
						

		assertEquals(	new Attribute( testeeValueName( "char" ), new Character( 'x' ) ),
						find( attrs, testeeValueName( "char" )));
						

		assertEquals(	new Attribute( testeeValueName( "Double" ), new Double( 9.999 ) ),
						find( attrs, testeeValueName( "Double" )));
	}
	
		public void
	testSetWild()
	{
		final Object	result	= mGetSetMBean.dottedNameSet( "*.int=1" );
			
		assert( result instanceof Exception );
	}
	
		public void
	testSetchar()
	{
		final String	dn	= testeeValueName( "char" );
		mGetSetMBean.dottedNameSet( new String [] { dn + "=x" } );
		assertEquals( getAttribute( dn ), new Attribute( dn, new Character( 'x' ) ) );
	}
	
		public void
	testSetbyte()
	{
		final String	dn	= testeeValueName( "byte" );
		mGetSetMBean.dottedNameSet( new String [] { dn + "=100" } );
		assertEquals( getAttribute( dn ), new Attribute( dn, new Byte( (byte)100 ) ) );
	}
	
		public void
	testSetshort()
	{
		final String	dn	= testeeValueName( "short" );
		mGetSetMBean.dottedNameSet( new String [] { dn + "=100" } );
		assertEquals( getAttribute( dn ), new Attribute( dn, new Short( (short)100 ) ) );
	}
	
		public void
	testSetint()
	{
		final String	dn	= testeeValueName( "int" );
		mGetSetMBean.dottedNameSet( new String [] { dn + "=100" } );
		assertEquals( getAttribute( dn ), new Attribute( dn, new Integer( 100 ) ) );
	}
	
		public void
	testSetlong()
	{
		final String	dn	= testeeValueName( "long" );
		mGetSetMBean.dottedNameSet( new String [] { dn + "=100" } );
		assertEquals( getAttribute( dn ), new Attribute( dn, new Long( 100 ) ) );
	}
	
		public void
	testSetfloat()
	{
		final String	dn	= testeeValueName( "float" );
		mGetSetMBean.dottedNameSet( new String [] { dn + "=99.99" } );
		assertEquals( getAttribute( dn ), new Attribute( dn, new Float( 99.99 ) ) );
	}
	
		public void
	testSetdouble()
	{
		final String	dn	= testeeValueName( "double" );
		mGetSetMBean.dottedNameSet( new String [] { dn + "=99.99" } );
		assertEquals( getAttribute( dn ), new Attribute( dn, new Double( 99.99 ) ) );
	}
	
	
	
		public void
	testSetCharacter()
	{
		final String	dn	= testeeValueName( "Character" );
		mGetSetMBean.dottedNameSet( new String [] { dn + "=x" } );
		assertEquals( getAttribute( dn ), new Attribute( dn, new Character( 'x' ) ) );
	}
	
		public void
	testSetByte()
	{
		final String	dn	= testeeValueName( "byte" );
		mGetSetMBean.dottedNameSet( new String [] { dn + "=100" } );
		assertEquals( getAttribute( dn ), new Attribute( dn, new Byte( (byte)100 ) ) );
	}
	
		public void
	testSetShort()
	{
		final String	dn	= testeeValueName( "Short" );
		mGetSetMBean.dottedNameSet( new String [] { dn + "=100" } );
		assertEquals( getAttribute( dn ), new Attribute( dn, new Short( (short)100 ) ) );
	}
	
		public void
	testSetInteger()
	{
		final String	dn	= testeeValueName( "Integer" );
		mGetSetMBean.dottedNameSet( new String [] { dn + "=100" } );
		assertEquals( getAttribute( dn ), new Attribute( dn, new Integer( 100 ) ) );
	}
	
		public void
	testSetLong()
	{
		final String	dn	= testeeValueName( "Long" );
		mGetSetMBean.dottedNameSet( new String [] { dn + "=100" } );
		assertEquals( getAttribute( dn ), new Attribute( dn, new Long( 100 ) ) );
	}
	
		public void
	testSetFloat()
	{
		final String	dn	= testeeValueName( "Float" );
		mGetSetMBean.dottedNameSet( new String [] { dn + "=99.99" } );
		assertEquals( getAttribute( dn ), new Attribute( dn, new Float( 99.99 ) ) );
	}
	
		public void
	testSetDouble()
	{
		final String	dn	= testeeValueName( "Double" );
		mGetSetMBean.dottedNameSet( new String [] { dn + "=99.99" } );
		assertEquals( getAttribute( dn ), new Attribute( dn, new Double( 99.99 ) ) );
	}
	
	
		public void
	testSetString()
	{
		final String	dn	= testeeValueName( "String" );
		mGetSetMBean.dottedNameSet( new String [] { dn + "=hello world" } );
		assertEquals( getAttribute( dn ), new Attribute( dn, "hello world" ) );
	}
	
		public void
	testSetStringArray()
	{
		final String	dn		= testeeValueName( "StringArray" );
		final Attribute	result	= (Attribute)mGetSetMBean.dottedNameSet( dn + "=hello,there,world" );
		
		final Attribute	attr	= getAttribute( dn );
		assertEquals( result, attr );
		final String []	values	= (String [])attr.getValue();
		assertEquals( 3, values.length );
		assertEquals( "hello", values[ 0 ] );
		assertEquals( "there", values[ 1 ] );
		assertEquals( "world", values[ 2 ] );
	}
	
		public void
	testSetIntegerArray()
	{
		final String	dn	= testeeValueName( "IntegerArray" );
		mGetSetMBean.dottedNameSet( dn + "=1,2,3" );
		
		final Attribute	attr	= getAttribute( dn );
		final Integer []	values	= (Integer [])attr.getValue();
		assertEquals( 3, values.length );
		assertEquals( new Integer( 1 ), values[ 0 ] );
		assertEquals( new Integer( 2 ), values[ 1 ] );
		assertEquals( new Integer( 3 ), values[ 2 ] );
	}
	
		public void
	testGetProperty()
	{
		final String	dottedName	= PRIMARY_TESTEE + "." +
					DottedNamePropertySupport.getPropertySuffix( Testee.PROPERTY_NAME );
					
		final Attribute attr	= getAttribute( dottedName );
		assertEquals( Testee.PROPERTY_VALUE, attr.getValue()  );
	}
	
		public synchronized void
	testSetProperty()
	{
		final String	dottedName	= PRIMARY_TESTEE + "." +
					DottedNamePropertySupport.getPropertySuffix( "testProperty" );
					
		final String	TEST_VALUE	= "hello";
		
		// set a value and re-get it to verify it was set
		final Object	value	= mGetSetMBean.dottedNameSet( dottedName + "=" + TEST_VALUE );
		if ( value instanceof Exception )
		{
			((Exception)value).printStackTrace();
		}

		final Attribute	attr	= (Attribute)value;
		assertEquals( TEST_VALUE, attr.getValue( ) );
		assertEquals( TEST_VALUE, getAttribute( dottedName ).getValue()  );
	}
	
		private String []
	dottedNameList( String name )
	{
		return( mGetSetMBean.dottedNameList( new String [] { name } ) );
	}
	
		private String []
	dottedNameMonitoringList( String name )
	{
		return( mGetSetMBean.dottedNameMonitoringList( new String [] { name } ) );
	}
	
		public void
	testListAll()
	{
		// list all will only access server.* variants
		final int 	required	=
			NUM_SERVERS + // server itself
			NUM_SERVERS * NUM_DOMAIN_SUBPARTS + // aliased parts
			0;
		
		final String []	names	= dottedNameList( "*" );
		assertEquals( required, names.length );
	}
	
		public void
	testListEmpty()
	{
		final String []	names	= dottedNameList( "" );
		assertEquals( 0, names.length );
	}
		
		String
	setToString( final Set	s )
	{
		final Iterator	iter	= s.iterator();
		final StringBuffer	buf	= new StringBuffer();
		
		while ( iter.hasNext() )
		{
			buf.append( (String)iter.next() + "\n" );
		}
		
		return( buf.toString() );
	}
	
		public void
	testListNoMatch()
	{
		assertEquals( 0, dottedNameList( "*foo*" ).length );
	}
		public void
	testListDomain()
	{
		// domain only works if it starts with "domain"
		assertEquals( 0, dottedNameList( "*" + DOMAIN + "*" ).length );
		assertEquals( 0, dottedNameList( "*" + DOMAIN ).length );
		
		assertEquals( 1 + NUM_DOMAIN_SUBPARTS, dottedNameList( DOMAIN + "*" ).length );
	}
	
		public void
	testListServer()
	{
		final int 	required	= NUM_SERVERS + NUM_SERVERS * NUM_DOMAIN_SUBPARTS;
		assertEquals( required, dottedNameList( SERVER_NAME_BASE + "*" ).length );
		assertEquals( required, dottedNameList( "*" + SERVER_NAME_BASE + "*" ).length );
	}

	
		public void
	testListRecursive()
	{
		final int 	required	=
			NUM_SERVERS + 	// one for each testee itself
			NUM_SERVERS * NUM_DOMAIN_SUBPARTS + // each server aliases into domain
			0;
			
		assertEquals( required, dottedNameList( SERVER_NAME_BASE + "*").length );
	}
	
		public void
	testListNonRecursive()
	{
		assertEquals( NUM_DOMAIN_SUBPARTS, dottedNameList( getServerDottedName( "0" )  ).length );
		assertEquals( NUM_DOMAIN_SUBPARTS, dottedNameList( DOMAIN  ).length );
	}
	
		public void
	testListNoChildren()
	{
		assertEquals( 0, dottedNameList( getServerDottedName( "0." + DOMAIN_SUBPARTS[ 0 ] ) ).length );
		assertEquals( 0, dottedNameList( getConfigDottedName( "0" ) ).length );
	}
	
		public void
	testListWithChildren()
	{
		assertEquals( NUM_DOMAIN_SUBPARTS, dottedNameList( DOMAIN + ".*" ).length );
	}
	
		public void
	testListWildcardMonitoring()
	{
		assertEquals( NUM_SERVERS, dottedNameMonitoringList( "server*" ).length );
	}
	
		public void
	testAliasingOffForMonitoring()
	{
		final Object	result	= mGetSetMBean.dottedNameMonitoringGet( getConfigDottedName( "0" ) + ".*" );
		
		assert( result instanceof Attribute[] );
		
		assertEquals( 0, dottedNameMonitoringList( "*config*" ).length );
	}
	
		public void
	testListNonExistent()
	{
		assertEquals( 0, dottedNameList( "foo" ).length );
		assertEquals( 0, dottedNameList( "/" ).length );
	}
	
		public void
	testMonitoringGet()
	{
		final String dn	= DOMAIN + ".int";
		assertEquals( getMonitoringAttribute( dn ), new Attribute( dn, new Integer( 0 ) ) );
	}
	
		public void
	testFunkyName() throws JMException
	{
		// set up testee with strange name
		// simulate the domain itself
		registerAndAddTestee( FUNKY_DOTTED_NAME, FUNKY_ID, CATEGORY_SPECIAL, mRegistry);
	}
	
}


















