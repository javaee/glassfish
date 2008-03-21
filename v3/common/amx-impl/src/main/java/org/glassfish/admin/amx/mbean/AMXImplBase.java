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
package org.glassfish.admin.amx.mbean;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Collections;
import java.util.Properties;
import java.util.HashSet;

import java.util.logging.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.io.IOException;

import javax.management.*;

import com.sun.appserv.management.util.stringifier.SmartStringifier;
import com.sun.appserv.management.util.stringifier.ArrayStringifier;
import com.sun.appserv.management.util.jmx.stringifier.AttributeChangeNotificationStringifier;


import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.AMXDebug;
import com.sun.appserv.management.base.AMXAttributes;
import com.sun.appserv.management.base.Extra;
import com.sun.appserv.management.base.Container;
import com.sun.appserv.management.base.Singleton;
import com.sun.appserv.management.base.Utility;

import com.sun.appserv.management.base.QueryMgr;
import com.sun.appserv.management.base.XTypes;
/*
import org.glassfish.admin.amx.types.XTypesMapper;
import org.glassfish.admin.amx.types.AllTypesMapper;
import org.glassfish.admin.amx.types.TypeInfos;
import org.glassfish.admin.amx.types.TypeInfo;
*/

import com.sun.appserv.management.base.AMXLoggerBase;

import com.sun.appserv.management.config.NamedConfigElement;

import com.sun.appserv.management.base.QueryMgr;
import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.base.Util;

import com.sun.appserv.management.client.ConnectionSource;
import com.sun.appserv.management.util.jmx.MBeanServerConnectionSource;
import com.sun.appserv.management.util.jmx.JMXUtil;

import com.sun.appserv.management.util.misc.ClassUtil;
import com.sun.appserv.management.util.misc.ExceptionUtil;
import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.misc.ThrowableMapper;
import com.sun.appserv.management.util.misc.StringUtil;
import com.sun.appserv.management.util.misc.CollectionUtil;

import org.glassfish.admin.amx.util.ObjectNames;
import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.Container;

import com.sun.appserv.management.client.ProxyFactory;

import com.sun.appserv.management.util.jmx.NotificationBuilder;
import com.sun.appserv.management.util.jmx.AttributeChangeNotificationBuilder;
import com.sun.appserv.management.util.jmx.stringifier.MBeanInfoStringifier;

import com.sun.appserv.management.util.misc.MapUtil;
import com.sun.appserv.management.config.AMXConfig;
import com.sun.appserv.management.config.PropertiesAccess;

import com.sun.appserv.management.j2ee.J2EETypes;

import org.glassfish.admin.amx.util.Issues;


/**
	Base class from which all AMX MBeans should derive (but not "must").
	<p>
	Note that even though this base class implements a number of interfaces,
	the actual MBean interface supplied by the subclass construction-time
	determines which of these is actually exposed in the MBeanInfo.
	<p>
	A subclass should generally <b>not</b> implement get/setAttribute(s) as these
	calls are processed in this base class--
	<p>
	If a subclass implements a getter or setter Method it will be invoked automatically.
	If there is no getter or setter Method, then the getAttributeManually() or
	setAttributeManually() methods will be invoked; the subclass should implement
	these methods instead.
	<p>
	Method invocation is also handled automatically. If a Method cannot be found,
	the invokeManually() method is called; the subclass should implement this method.
	<p>
	Note that various optimizations are possible, but not implemented. These include
	caching Methods for each Attribute and for operations as well.  Careful testing
	should be done before complicating the code with such optimizations.
 */
public class AMXImplBase extends MBeanImplBase
	implements DynamicMBean, MBeanRegistration,
	AMX,
	NotificationEmitter, DelegateOwner
{
	protected static final String	GET	= "get";
	protected static final String	SET	= "set";
	
	/**
		The interface this MBean implements
	*/
	private final Class			mInterface;
	
	/**
		The MBeanInfo from the supplied AMX interface.  Additional info
        might be available.
	*/
	private final MBeanInfo		mAMXMBeanInterfaceMBeanInfo;
    
	/**
		The final and complete MBeanInfo which must of course be invariant.
        If this field is non-null, then it will be returned by getMBeanInfo().
	*/
	private volatile MBeanInfo		mInvariantMBeanInfo;
	
	/**
		The Container or "parent" MBean for this MBean.
	*/
	private final ObjectName		mContainerObjectName;
	
	/**
		Flag to enable or disable whether AttributeChangeNotifications are
		emitted.
	*/
	private final boolean			mEmitAttributeChangeNotifications;
	
	
	private volatile QueryMgr	mQueryMgr;
	private volatile AMX		mSelfProxy;
	
	private ConnectionSource	mConnectionSource;
	
	/**
		An optional Delegate
	*/
	private final Delegate		mSuppliedDelegate;
	private volatile Delegate   mDelegate;
	
	//private AMXAttributeNameMapper	mAttributeNameMapper	= null;
	
	private Map<String,MBeanAttributeInfo>			mAttributeInfos;
	
	private final String    mFullType;
	private final String	mJ2EEType;
	
    /**
        Maps a j2eeType to a Map<String,Class>  which maps an Attribute name to a Class.
     */
    private static final Map<String,Map<String,Class>>  ATTRIBUTE_CLASSES =
        Collections.synchronizedMap( new HashMap<String,Map<String,Class>>() );
	    
       private synchronized MBeanInfo
    getInterfaceMBeanInfo(final Class<? extends AMX> theInterface )
    {
		final MBeanInfo info	= MBeanInfoCache.getAMXMBeanInfo( theInterface );
        if ( getAMXDebug() )
        {
            debug( "Interface " + mInterface.getName() +
                " has MBeanInfo:\n" +
                MBeanInfoStringifier.DEFAULT.stringify( info ) );
                
            //info   = addDebugMBeanInfo( info );
        }
        
        return info;
    }

 
	/**
		Construct a new implementation that implements the supplied mbeanInterface.
		
		@param j2eeTypeIn		(may be null) the j2eeType of this instance
		@param theInterface		(may be null) the public interface as seen on the client
		@param delegate			an MBean to which unknown requests are delegated
	*/
		public
	AMXImplBase(
        final String        j2eeType,
        final String        fullType,
        final ObjectName    parentObjectName,
		final Class<? extends AMX> theInterface,
		final Delegate		delegate )
	{
		super();
        
        if ( parentObjectName == null && theInterface != DomainRoot.class )
        {
            debug( "WARNING: every AMX MBean must have a parent object (Container)! Missing parent for j2eeType " + j2eeType );
            throw new IllegalArgumentException( "every AMX MBean must have a parent object (Container)" );
        }

        //System.out.println( "AMXImplBase: j2eeType = " + j2eeType + ", fullType = " + fullType );
        if ( j2eeType == null )
        {
            throw new IllegalArgumentException( "AMXImplBase: j2eeType is null for " + theInterface.getName() );
        }
        if ( fullType == null || ! fullType.endsWith(j2eeType) )
        {
            throw new IllegalArgumentException( "AMXImplBase: fullType is null or ends wrong: " + fullType  + " for " + theInterface.getName() );
        }
        //System.out.println( "AMXImplBase: j2eeType = " + j2eeType + ", fullType = " + fullType );
        
        mJ2EEType   = j2eeType; //  overrides the interface, and/or interface might not specify
        mFullType = fullType;
		mInterface	= theInterface;
		mContainerObjectName	= parentObjectName;
        
		if ( delegate != null )
		{
			delegate.setOwner( this );
		}
		
        //debug( "J2EE_TYPE: " + j2eeType + " <==> " + ClassUtil.getFieldValue( theInterface, "J2EE_TYPE" ) );
		//mJ2EEType	= (String)ClassUtil.getFieldValue( theInterface, "J2EE_TYPE" );
		
		mEmitAttributeChangeNotifications	= true;
		mQueryMgr			= null;
		mSelfProxy				= null;
		
		mSuppliedDelegate		= delegate;
		if ( mSuppliedDelegate instanceof DelegateBase )
		{
		    ((DelegateBase)mSuppliedDelegate).setDebugOutput( getDebugOutput() );
		}

        ATTRIBUTE_CLASSES.put( mJ2EEType,
            Collections.synchronizedMap( new HashMap<String,Class>() ) );
			
		// initialization of mDelegate is deferred until later; the supplied delegate
		// may not be in a position to run yet
		mDelegate				= null;
		//mAttributeNameMapper	= null;
		
		mAttributeInfos	= null;
		
		mAMXMBeanInterfaceMBeanInfo	= getInterfaceMBeanInfo( mInterface );
	}
	
		public void
	delegateFailed( final Throwable t )
	{
		// the delegate has fatally failed
	}
	
	
        protected String
    getDebugID()
    {
        return ClassUtil.stripPackageName( this.getClass().getName() );
    }
	
	/**
		Almost all MBeans don't change their MBeanInfo once it's created, making it
		possible to cache it on the client side.
	*/
		public boolean
	getMBeanInfoIsInvariant()
	{
		return( true );
	}
		
		protected MBeanInfo
	removeUnsupportedMBeanInfo( final MBeanInfo info )
	{
		return( info );
	}
	
	/**
	    Hook for subclass to modify anything in MBeanInfo.
	 */
		protected MBeanInfo
	modifyMBeanInfo( final MBeanInfo info )
	{
		return( info );
	}
    
	/**
	    Subclass may override, but should call super.addDebugMBeanInfo().
	    Add any additional debugging stuff to the MBeanInfo.
		protected MBeanInfo
	addDebugMBeanInfo( final MBeanInfo origInfo )
	{
		final MBeanInfo debugInfo	=
			MBeanInfoConverter.getInstance().convert( AMXDebugStuff.class, null );
	
	    return JMXUtil.mergeMBeanInfos( origInfo, debugInfo );
	}
	 */
	
	
	
	/**
		By default, the MBeanInfo is derived once from the MBean interface.
		Then certain items are removed, and optional debugging is added.
		This method should not cache the MBeanInfo because it may change
		dynamically.  The client-side proxies cache the MBeanInfo if
		getMBeanInfoIsInvariant() returns true.
	*/
		public MBeanInfo
	getMBeanInfo()
	{
        MBeanInfo	mbeanInfo = null;
        
        if ( mInvariantMBeanInfo != null )
        {
            mbeanInfo = mInvariantMBeanInfo;
        }
        else
        {
            mbeanInfo = mAMXMBeanInterfaceMBeanInfo;
            
            try
            {
                final MBeanNotificationInfo[]    notifs  = getNotificationInfo();
                if ( notifs != null && notifs.length != 0 )
                {
                    mbeanInfo   = JMXUtil.addNotificationInfos( mbeanInfo, notifs );
                }
            }
            catch( Exception e )
            {
                e.printStackTrace();
                throw new RuntimeException( e );
            }
            
            mbeanInfo   = removeUnsupportedMBeanInfo( mbeanInfo );
            mbeanInfo   = modifyMBeanInfo( mbeanInfo );
                
            if ( getMBeanInfoIsInvariant() )
            {
                mInvariantMBeanInfo = mbeanInfo;
            }
        }
		
		return( mbeanInfo );
	}
	
	
		protected final boolean
	shouldEmitNotifications()
	{
		return( mEmitAttributeChangeNotifications && getListenerCount() != 0 );
	}
	
	
		public Delegate
	getDelegate()
	{
		return( mDelegate );
	}
	
		protected void
	setDelegate( final Delegate delegate )
	{
		mDelegate	= delegate;
	}
	
		protected boolean
	haveDelegate()
	{
		return( getDelegate() != null );
	}
    
    /*
	
		protected Object
	getDelegateProxy( final Class theInterface )
	{
		return( DelegateInvocationHandler.newProxyInstance( getDelegate(), theInterface ) );
	}
	
	
		protected AMXAttributeNameMapper
	getAttributeNameMapper()
	{
		return( mAttributeNameMapper );
	}
	*/
 
	private static final MBeanNotificationInfo[] EMPTY_NOTIFICATIONS = new MBeanNotificationInfo[ 0 ];
	
 	/**
 	    Defined by NotificationBroadcaster.
 	    
		@return an empty array
 		Subclass may wish to override this.
 	*/
		public MBeanNotificationInfo[]
	getNotificationInfo()
	{
		return( EMPTY_NOTIFICATIONS );
	}
 
	
	
	/**
		Get the value of a property within this MBean's ObjectName.
		
		@return the value of the specified property, or null if not found.
	*/
		protected String
	getKeyProperty( final String key )
	{
		return( getObjectName().getKeyProperty( key ) );
	}
	
	
		public ProxyFactory
	getProxyFactory()
	{
		assert( mConnectionSource != null );
		return( ProxyFactory.getInstance( mConnectionSource, true ) );
	}
	
		public <T extends AMX> T
	getProxy( final ObjectName objectName, final Class<T> theClass)
	{
	    return getProxyFactory().getProxy( objectName, theClass );
	}
	

		protected boolean
	shouldOmitObjectNameForDebug()
	{
		return super.shouldOmitObjectNameForDebug() ||
		    getObjectName().getKeyProperty( "name" ).equals( AMX.NO_NAME );
	}
	
		protected static boolean
	isSingletonMBean( final Class	mbeanInterface )
	{
		return( Singleton.class.isAssignableFrom( mbeanInterface ) );
	}
	
		protected static boolean
	isUtilityMBean( final Class	mbeanInterface )
	{
		return( Utility.class.isAssignableFrom( mbeanInterface ) );
	}
	
		protected static boolean
	hasElementName( final Class	mbeanInterface )
	{
		return( NamedConfigElement.class.isAssignableFrom( mbeanInterface ) );
	}
	
	
		public Container
	getFactoryContainer()
	{
		return( Container.class.cast( getSelf() ) );
	}
	
	
		public final Container
	getContainer()
	{
		final ObjectName	objectName	= getContainerObjectName();
		
		return(  getProxyFactory().getProxy( objectName, Container.class) );
	}
	
		public ObjectName
	getContainerObjectName()
	{
        return mContainerObjectName;
	}
	
	    protected ObjectNames
    getObjectNames()
    {
    	return ObjectNames.getInstance( getJMXDomain() );
    }

	/**
		Use the ObjectName with which this MBean was registered in combination with
		its j2eeType and its parent keys to determine the ObjectName <i>pattern</i>
		that uniquely identifies it.
	*/
		public final ObjectName
	getObjectNamePattern()
	{
		final ObjectName	selfObjectName	= getObjectName();
		final Set<String>	requiredKeys	= Util.getPatternKeys( getFullType() );
		
		final String		requiredProps	= JMXUtil.getProps( selfObjectName, requiredKeys, true );
		final ObjectName	pat	= Util.newObjectNamePattern( selfObjectName.getDomain(), requiredProps );
		
		return( pat );
	}
		
		public final Class
	getInterface()
	{
		return( mInterface );
	}
	
		public final String
	getInterfaceName()
	{
		return( getInterface().getName() );
	}
		public final String[]
	getAttributeNames()
	{
		return( GSetUtil.toStringArray( getAttributeInfos().keySet() ) );
	}
	
	/**
		An operation has not been implemented. Deal with appropriately.
	*/
		protected final void
	unimplementedOperation( final String operation )
	{
	    final String msg = "UNIMPLEMENTED OPERATION: " + operation + " in " + getObjectName();
		
		logInfo( msg );
		
		throw new UnsupportedOperationException( operation );
	}
	

	/**
		An Attribute has not been implemented.
	*/
		protected final Object
	unimplementedAttribute( final String attrName )
	{
	    final String msg = "UNIMPLEMENTED ATTRIBUTE: " + attrName + " in " + getObjectName();
		logInfo( msg );
				
		return( null );
	}
	
	/**
		The impossible has happened.
	*/
		protected final void
	impossible( final Throwable t )
	{
		logSevere( "AMXImplBase.impossible: " + t.getMessage() );
		assert( false );
		throw new RuntimeException( t );
	}
	
	
		private Object
	convertToClass(
		final Object	value,
		final Class		theClass )
		throws Exception
	{
		Object	result	= value;
		
		if ( value instanceof String )
		{
			result	= ClassUtil.InstantiateFromString( theClass, (String)value );
		}
		else
		{
			getMBeanLogger().info( "convertToClass: don't know how to convert: " +
				value.getClass().getName() );
		}
		
		return( result );
	}
	
    /**
        Called every time an Attribute is obtained via delegateGetAttribute(), so 
        make sure it's reasonably fast.
     */
		private Class<?>
	getAttributeClass( final String attributeName )
		throws ClassNotFoundException
	{
        final Map<String,Class> mappings    = ATTRIBUTE_CLASSES.get( getJ2EEType() );
        
        Class theClass = mappings.get( attributeName );
        // initialize only if the class is null and there isn't a mapping for to null
        if ( theClass == null && ! mappings.containsKey( attributeName ) )
        {
            // no need to synchronize; the Map is already so.
            // And if mappings were somehow 'put' twice, that's rare and of no importance
            final MBeanAttributeInfo[]	infos	= getMBeanInfo().getAttributes();
            
            // Map each Attribute to a Class
            for( int i = 0; i < infos.length; ++i )
            {
                final String attrName   = infos[ i ].getName();
                final Class c = ClassUtil.getClassFromName( infos[ i ].getType() );
                mappings.put( attrName, c );
            }
            
            theClass    = mappings.get( attributeName );
		}
        
		return( theClass );
	}
	
	/**
	*/
		protected  Object
	delegateGetAttribute( final String name )
		throws Exception
	{
	    assert( name != null );
	    final Delegate  delegate    = getDelegate();
	    assert( delegate != null );
	    
		final Object	value	= delegate.getAttribute( name );
		
		Object result	= value;
		
		if ( value != null )
		{
			Class<?>	attrClass	= getAttributeClass( name );
			
			if ( attrClass != null )
			{
				if ( ClassUtil.IsPrimitiveClass( attrClass ) )
				{
					attrClass	= ClassUtil.PrimitiveClassToObjectClass( attrClass );
				}
					
				if ( ! attrClass.isAssignableFrom( value.getClass() ) )
				{
				    try
				    {
					    result	= convertToClass( value, attrClass );
					}
					catch( Exception e )
					{
					    // OK, there are a few exceptions
					    result  = value;
					}
				}
			}
			else
			{
				getMBeanLogger().warning( "AMXImplBase.delegateGetAttribute: " +
					"Can't find class for attribute: " + name + "=" + value +
					" in object " + getObjectName() );
                
                // add a null mapping
                ATTRIBUTE_CLASSES.get( getJ2EEType() ).put( name, null );
			}
		}
		
		return( result );
	}
	
		protected  Object
	delegateGetAttributeNoThrow( String name )
	{
		try
		{
			final Object	value	= delegateGetAttribute( name );
			
			return( value );
		}
		catch( Exception e )
		{
			throw new RuntimeException( e );
		}
	}
    
	/*
        March 14 2008: retain this code until we're sure it's not needed
        
		protected void
	delegateSetAttribute(
		final String	name,
		final Object	value  )
		throws AttributeNotFoundException, InvalidAttributeValueException
	{
		getDelegate().setAttribute( new Attribute( name, value ) );
	}
	
		protected void
	delegateSetAttributeNoThrow( String name, Object value  )
	{
		try
		{
			delegateSetAttribute( name, value );
		}
		catch( JMException e )
		{
		    debug( ExceptionUtil.toString( e ) );
			throw new RuntimeException( e );
		}
		catch( RuntimeException ee )
		{
		    debug( ExceptionUtil.toString( ee ) );
			throw ee;
		}
	}
    */
	
	
	
		protected Object
	getAttributeNoThrow( String name )
	{
		Object	result	= null;
		
		try
		{
			result	= getAttribute( name );
		}
		catch( Exception e )
		{
			throw new RuntimeException( new ThrowableMapper( e ).map() );
		}
		return( result );
	}
	
		protected synchronized Map<String,MBeanAttributeInfo>
	getAttributeInfos()
	{
		if ( mAttributeInfos == null || ! getMBeanInfoIsInvariant() )
		{
			mAttributeInfos	= JMXUtil.attributeInfosToMap( getMBeanInfo().getAttributes() );
		}
		
		return( mAttributeInfos );
	}
	
	/**
		Subclasses may need to force a refresh.
	 */
		protected void
	clearAttributeInfos()
	{
		mAttributeInfos	= null;
	}
	
		protected boolean
	isLegalAttribute( final String name )
	{
		return( getAttributeInfos().keySet().contains( name )  );
	}
	
		protected MBeanAttributeInfo
	getAttributeInfo( final String name )
	{
		return( (MBeanAttributeInfo)getAttributeInfos().get( name ) );
	}
	
		protected boolean
	isReadOnlyAttribute( final String name )
	{
		return( ! getAttributeInfo( name ).isWritable() );
	}
	
	
		public Logger
	getLogger()
	{
		return( getMBeanLogger() );
	}

	/**
		Get an Attribute value, first by looking for a getter method
		of the correct name and signature, then by looking for a delegate,
		and finally by calling getAttributeManually(), which a subclass
		is expected to override.
		
		@param name	name of the Attribute
		@return value of the Attribute
	*/
		public Object
	getAttribute( final String name )
		throws AttributeNotFoundException
	{
		Object	result	= null;
		
		if ( ! (isLegalAttribute( name ) || name.equals(OBJECT_REF_ATTR_NAME) ) )
		{
			debug( "getAttribute: unknown Attribute " + name + ", legal Attributes are: " + toString( getAttributeInfos().keySet() ) );
			throw new AttributeNotFoundException( name );
		}
		
		try
		{
			result	= getAttributeInternal( name );
		}
		catch( AttributeNotFoundException e)
		{
			throw e;
		}
		catch( Exception e )
		{
			throw new AttributeNotFoundException( name );
		}
		
		return( result );
	}
	
    
    private boolean isSpecialAMXAttr( final String attrName )
    {
        return isObjectNameMapAttribute( attrName ) || isObjectNameAttribute( attrName );
    }


		protected Object
	getAttributeInternal( final String name )
		throws AttributeNotFoundException, ReflectionException, MBeanException
	{
		Object	result	= null;
		boolean	handleManually	= false;
		
        //System.out.println( "getAttributeInternal: " + name );
        
        // see if a getter exists
        final Method m	= findGetter( name );
        if ( m != null )
        {
        //System.out.println( "getAttributeInternal: found getter method for: " + name );
            result	= getAttributeByMethod( name, m );
            debug( "getAttribute: " + name + " CALLED GETTER: " + m + " = " + result);
            handleManually	= false;
        }
        else if ( isSpecialAMXAttr( name ) )
        {
       // System.out.println( "getAttributeInternal: isSpecialAMXAttr for: " + name );
            handleManually = true;
        }
        else if ( haveDelegate() )
        {
       // System.out.println( "getAttributeInternal: haveDelegate() for: " + name );
            trace( "getAttribute: " + name + " HAVE DELEGATE " );
                
            if ( getDelegate().supportsAttribute( name ) )
            {
                trace( "getAttribute: " + name + " CALLING DELEGATE " );
                try
                {
                    result	= delegateGetAttribute( name );
                    
                    // special case handling: String to String[]
                    if ( result.getClass() == String.class &&
                            getAttributeType(name).equals( String[].class.getName() ) )
                    {
                        result = ((String)result).split( ":" );
                    }
                }
                catch( Exception e )
                {
                    trace( "getAttribute: DELEGATE claims support, but fails: " + name  );
                    handleManually	= true;
                }
            }
            else
            {
                trace( "getAttribute: " + name + " DELEGATE DOES NOT SUPPORT " );
                handleManually	= true;
            }
        }
        else
        {
            handleManually	= true;
        }
    
		if ( handleManually )
		{
			trace( "getAttribute: handle manually: " + name );
			try
			{
				result	= getAttributeManually( name );
			}
			catch( AttributeNotFoundException e )
			{
				trace( "getAttribute: " + name + " NOT FOUND " );
				throw e;
			}
		}
		
		return( result );
	}
	
	/**
		Bulk get.  Note that is is important for this implementation to
		call getAttribute() for each name so that each may be processed
		appropriately; some Attributes may be in this MBean itself, and some
		may reside in a {@link Delegate}.
		
		@param names	array of Attribute names
		@return AttributeList of Attributes successfully fetched
	*/
		public AttributeList
	getAttributes( String[] names )
	{
		trace( "AMXImplBase.getAttributes: " + SmartStringifier.toString( names ) );
		//trace( "AMXImplBase.getAttributes: delegate class = " + getDelegate().getClass().getName() );
		
		final AttributeList	attrs	= new AttributeList();
		
		for( int i = 0; i < names.length; ++i )
		{
			try
			{
				trace( "%%% calling getAttribute: " + names[ i ] + " on " + getObjectName() );
				final Object value	= getAttribute( names[ i ] );
				attrs.add( new Attribute( names[ i ], value ) );
			}
			catch( Exception e )
			{
				trace( "### AttributeNotFoundException: " + names[ i ] );
				// ignore, as per spec
			}
		}
		return( attrs );
	}
	
	
		private final void
	rethrowAttributeNotFound(
		final Throwable t,
		final String	attrName )
		throws AttributeNotFoundException
	{
		final Throwable rootCause	= ExceptionUtil.getRootCause( t );
		if ( rootCause instanceof AttributeNotFoundException )
		{
			throw (AttributeNotFoundException)rootCause;
		}
		
		final String msg = "Attribute not found: " + StringUtil.quote(attrName) + " [" + rootCause.getMessage() + "]";;
		throw new AttributeNotFoundException( msg );
	}
    
	
	/**
		Set an Attribute by invoking the supplied method.
	*/
		protected Object
	getAttributeByMethod( final String attrName, final Method m)
		throws AttributeNotFoundException
	{
		Object	result	= null;
		
		try
		{
			//trace( "getAttributeByMethod: " + attrName  );
			result	= m.invoke( this, (Object[])null );
		}
		catch( InvocationTargetException e )
		{
			trace( "InvocationTargetException: " + attrName + " by " + m );
			rethrowAttributeNotFound( e, attrName );
		}
		catch( IllegalAccessException e )
		{
			trace( "ILLEGAL ACCESS TO: " + attrName + " by " + m );
			rethrowAttributeNotFound( e, attrName );
		}
		catch( Exception e )
		{
			trace( "Exception: " + attrName + " by " + m );
			rethrowAttributeNotFound( e, attrName );
		}
		
		return( result );
	}
	
		protected void
	setAttributeByMethod( final Attribute attr, final Method m)
		throws AttributeNotFoundException, InvalidAttributeValueException
	{
		try
		{
			// trace( "setAttributeByMethod: " + m );
			m.invoke( this, new Object[] { attr.getValue() } );
		}
		catch( InvocationTargetException e )
		{
			trace( "setAttributeByMethod: InvocationTargetException: " + e );
		
			final Throwable t	= ExceptionUtil.getRootCause( e );
			if ( t instanceof InvalidAttributeValueException)
			{
				throw (InvalidAttributeValueException)t;
			}

			rethrowAttributeNotFound( e, attr.getName() );
		}
		catch( IllegalAccessException e )
		{
			trace( "setAttributeByMethod: IllegalAccessException: " + e );
			rethrowAttributeNotFound( e, attr.getName()  );
		}
		catch( Exception e )
		{
			trace( "setAttributeByMethod: Exception: " + e );
			rethrowAttributeNotFound( e, attr.getName()  );
		}
	}
	
	/**
		Subclasses should override this to handle getAttribute( attrName ). It will
		be called if no appropriate getter is found.
		<p>
		It generically handles all <Abc>ObjectName() and <Abc>ObjectNameMap
		Attributes.
	*/
		protected Object
	getAttributeManually( final String attributeName )
		throws AttributeNotFoundException
	{
	    Object  result  = null;
	    
	    AMXDebug.getInstance().getOutput( "getAttributeManually" ).println(
	        attributeName + " on " + getObjectName() );
	        
	    if ( isObjectNameAttribute( attributeName ) )
	    {
	        final String    j2eeType    = attributeNameToJ2EEType( attributeName );
	        debug( "getAttributeManually: attributeName " + attributeName + " => j2eeType " + j2eeType );
	        result  = getContainerSupport().getContaineeObjectName( j2eeType );
	    }
	    else if ( isObjectNameMapAttribute( attributeName  ) )
	    {
	        final String    j2eeType    = attributeNameToJ2EEType( attributeName );
	        debug( "invokeManually:  attributeName " + attributeName + " => j2eeType " + j2eeType );
	        result  = getContainerSupport().getContaineeObjectNameMap( j2eeType );
	    }
		else
		{
		    throw new AttributeNotFoundException( attributeName );
		}
		return result;
	}


	/**
		Subclasses should override this to handle setAttribute( attr ). It will
		be called if no appropriate setter is found.
	*/
		protected void
	setAttributeManually( final Attribute attr )
		throws AttributeNotFoundException, InvalidAttributeValueException
	{
		throw new AttributeNotFoundException( attr.getName() );
	}

		public void
	setAttribute( final Attribute attr )
		throws AttributeNotFoundException, InvalidAttributeValueException
	{
	    final String    name    = attr.getName();
	    
		if ( isReadOnlyAttribute( name ) )
		{
			throw new IllegalArgumentException( "Attribute is read-only: " + attr.getName() );
		}
		
		boolean failure = true;
		
		try
		{
			setAttributeInternal( attr );
			failure = false;
		}
		catch( AttributeNotFoundException e )
		{
			throw e;
		}
		catch( InvalidAttributeValueException e )
		{
			throw e;
		}
		catch( RuntimeException e )
		{
			throw (RuntimeException)e;
		}
		catch( Exception e )
		{
			throw new RuntimeException( e );
		}
		finally
		{
		    if ( failure )
		    {
		    }
		}
	}
	
	/**
		Set an Attribute value, first by looking for a setter method
		of the correct name and signature, then by looking for a delegate,
		and finally by calling setAttributeManually(), which a subclass
		is expected to override.
		
		@param attr	the Attribute
	*/
		protected void
	setAttributeInternal( final Attribute attr )
		throws AttributeNotFoundException, InvalidAttributeValueException,
				ReflectionException, MBeanException
	{
		trace( "setAttribute: " + attr.getName() + " = " + attr.getValue() );
		
		boolean			handleManually	= false;
		final Method	m	= findSetter( attr );
		
		boolean	shouldEmitNotifications	= shouldEmitNotifications();
		// note that this will fail if an Attribute is write-only
		final Object	oldValue	= shouldEmitNotifications ?
						getAttribute( attr.getName() ) : null;
		
		if ( m != null )
		{
			setAttributeByMethod( attr, m );
		}
		else if ( haveDelegate() )
		{
			if ( getDelegate().supportsAttribute( attr.getName() ) )
			{
                // For delegated attributes, treat singles as a list
                final AttributeList attrList = new AttributeList();
                attrList.add( attr );
                setAttributes( attrList );
                shouldEmitNotifications = false; // already handled
			}
			else
			{
				handleManually	= true;
			}
		}
		else
		{
			handleManually	= true;
		}
		
		if ( handleManually )
		{
			setAttributeManually( attr );
		}
		
		if ( shouldEmitNotifications )
		{
			final String	attrType	= getAttributeType( attr.getName() );
			
			sendAttributeChangeNotification( "", attr.getName(), attrType, System.currentTimeMillis(), oldValue, attr.getValue() );
		}
	}
	
		protected String
	getAttributeType( final String attrName )
	{
		final MBeanAttributeInfo	info	=
			JMXUtil.getMBeanAttributeInfo( getMBeanInfo(), attrName );
		
		return( info.getType() );
	}
	
		protected synchronized void
	sendAttributeChangeNotification(
		final String	msg,
        final String    name,
		final String	attrType,
        final long      when,
		final Object	oldValue,
		final Object    newValue )
	{
        //
        // do not send a Notification when nothing has changed
        //
        if ( oldValue != null  && ! oldValue.equals(newValue) )
        {
            final AttributeChangeNotificationBuilder builder	=
            (AttributeChangeNotificationBuilder)
                getNotificationBuilder( AttributeChangeNotification.ATTRIBUTE_CHANGE );
		
            final AttributeChangeNotification	n	= 
                builder.buildAttributeChange( msg, name, attrType, when, oldValue, newValue );
            
            System.out.println( "AttributeChangeNotification: " + AttributeChangeNotificationStringifier.DEFAULT.stringify(n) );
            sendNotification( n );
        }
	}
	
    /**
        Split the attributes into two lists: those supported via a delegate and those not.
        This is done so that a single transaction can be done for setAttributes().
     */
        protected void
    splitAttributes(
        final AttributeList attrsIn,
        final AttributeList delegatedAttrs,
        final AttributeList otherAttrs )
    {
        if ( haveDelegate() )
        {
            final Delegate delegate = getDelegate();
            for( final Object attrO: attrsIn )
            {
                final Attribute attr = (Attribute)attrO;
                if ( delegate.supportsAttribute( attr.getName() ) )
                {
                    delegatedAttrs.add( attr );
                }
                else
                {
                    otherAttrs.add( attr );
                }
            }
        }
        else
        {
            otherAttrs.addAll( attrsIn );
        }
    }
    
        private void
    sendAttributeChangeNotifications(
        final AttributeList      attrList,
        final Map<String,Object> oldValues )
    {
        // issue all of them using the same time-of-change
        final long when = System.currentTimeMillis();
        
        final Map<String, String> attrsMap = JMXUtil.attributeListToStringMap( attrList );
        if ( ! attrsMap.keySet().equals( oldValues.keySet() ) )
        {
            throw new IllegalArgumentException();
        }
        
        final String msg = "";
        for (final String attrName : attrsMap.keySet() )
        {
            final String attrType = getAttributeType(attrName);
            final Object oldValue = oldValues.get(attrName);
            final Object newValue = attrsMap.get(attrName);
            
			sendAttributeChangeNotification( "", attrName, attrType, when, oldValue, newValue );
        }
    }
    
	/**
		Bulk set.  Note that is is important for this implementation to
		call setAttribute() for each name so that each may be processed
		appropriately; some Attributes may be in this MBean itself, and some
		may reside in a {@link Delegate}.  However, for those in a Delegate, we pass
        them as a group so that a single transaction may be done.
		
		@param attrs	attributes to be set
		@return AttributeList containing Attributes successfully set
	*/
		public AttributeList
	setAttributes( final AttributeList attrs )
	{
		final AttributeList	successList	= new AttributeList();
        
		System.out.println( "AMXImplBase.setAttributes = " + SmartStringifier.toString( attrs ) );
		
        final AttributeList delegatedAttrs = new AttributeList();
        final AttributeList otherAttrs = new AttributeList();
        
        splitAttributes( attrs, delegatedAttrs, otherAttrs );
        
        if ( delegatedAttrs.size() != 0 )
        {
            final Map<String,Object> oldValues = new HashMap<String,Object>();
            
            final AttributeList delegateSuccess = getDelegate().setAttributes( delegatedAttrs, oldValues );
            successList.addAll( delegateSuccess );
            
            sendAttributeChangeNotifications( delegateSuccess, oldValues );
        }
        
        if ( otherAttrs.size() != 0 )
        {
            for( int i = 0; i < otherAttrs.size(); ++i )
            {
                final Attribute attr	= (Attribute)otherAttrs.get( i );
                try
                {
                    setAttribute( attr );
                    // AttributeChangeNotification should now be sent
                    
                    successList.add( attr );
                }
                catch( Exception e )
                {
                    // ignore, as per spec
                    debug( ExceptionUtil.toString(e) );
                }
            }
        }
		return( successList );
	}
	
	
	/**
		Find a method.
		
		@param methodName
		@param sig
		@return a Method or null if not found
	*/
		protected final Method
	findMethod( String methodName, final Class[] sig )
	{
		return( ClassUtil.findMethod( this.getClass(), methodName, sig ) );
	}
	
	/**
		Find a getXXX() method that matches the Attribute
		
		@param name the name to which "get" will be prepended
		@return a Method or null if not found
	*/
	static private final Class[]	GETTER_SIG	= new Class[0];
		protected final Method
	findGetter( String name )
	{
		final String	methodName	= GET + name;
		
		Method	m	= findMethod( methodName, GETTER_SIG );
		if ( m == null )
		{
			m	= findMethod( "is" + name, GETTER_SIG );
		}
		
		return( m );
	}
	
	/**
		Find a setXXX() method that matches the Attribute.
		
		@param attr	an Attribute for which a matching setter should be located
		@return a Method or null if not found
	*/
		protected final Method
	findSetter( final Attribute attr )
	{
		final Object	value		= attr.getValue();
		Class		valueClass  = null;
		if ( value == null )
		{
		    final MBeanAttributeInfo    info    = getAttributeInfos().get( attr.getName() );
		    if ( info != null )
		    {
		        try
		        {
		            valueClass  = ClassUtil.getClassFromName( info.getType() );
		        }
		        catch( Exception e )
		        {
		        }
		    }
		}
		else
		{
		    valueClass	= value.getClass();
		}
		
		if ( valueClass == null )
		{
		    return null;
		}
		
		final String	methodName	= SET + attr.getName();
		Class[]			sig			= new Class[]	{ valueClass };
		Method			setter		= findMethod( methodName, sig );
		
		final Class	primitiveClass	= ClassUtil.ObjectClassToPrimitiveClass( valueClass );
		if ( setter == null && primitiveClass != valueClass )
		{
			//trace( "findSetter: retrying for primitive class: " + primitiveClass );
			// the Attribute value is always an object.  But it may be
			// that the setter takes a primitive type.  So for example,
			// the Attribute may contain a value of type Boolean, but the setter
			// may required type boolean
			sig[ 0 ]	= primitiveClass;
			setter		= findMethod( methodName, sig );
		}
		
		return( setter );
	}

    protected static final String GET_PREFIX    = "get";
    protected static final String OBJECT_NAME_SUFFIX    = "ObjectName";
    protected static final String OBJECT_NAME_MAP_SUFFIX    = "ObjectNameMap";
    
        protected boolean
    operationNameMatches(
        final String operationName,
        final String prefix,
        final String suffix )
    {
        return operationName.startsWith( prefix ) &&
	        operationName.endsWith( suffix );
    }
    
        protected boolean
    getterNameMatches(
        final String operationName,
        final String suffix )
    {
        return operationNameMatches( operationName, GET_PREFIX, suffix );
    }
    
        protected boolean
    isObjectNameGetter(
		final String 	operationName,
		final Object[]	args,
		final String[]	types )
    {
        final int   numArgs = args == null ? 0 : args.length;
        return numArgs == 0 && isObjectNameGetter( operationName );
    }
    
       protected boolean
    isObjectNameGetter( final String operationName)
    {
        return getterNameMatches( operationName, OBJECT_NAME_SUFFIX );
    }
    
    private static final Set<String> NO_AUTO_GET = GSetUtil.newUnmodifiableStringSet(
            "ContainerObjectName",
            "MonitoringPeerObjectName",
            "ObjectName",
            "ConfigPeerObjectName",
            "ServerObjectName" );
           
       protected boolean
    isObjectNameAttribute(final String attributeName)
    {
        return attributeName.endsWith( OBJECT_NAME_SUFFIX ) &&
            ! NO_AUTO_GET.contains( attributeName );
    }
    
        protected boolean
    isObjectNameMapAttribute(final String attributeName)
    {
        return attributeName.endsWith( OBJECT_NAME_MAP_SUFFIX ) &&
            ! NO_AUTO_GET.contains( attributeName );
    }
    
	    protected String
	attributeNameToJ2EEType( final String attributeName )
    {
        String  j2eeType   = null;
        
        if ( isObjectNameAttribute( attributeName ) )
        {
            j2eeType   = StringUtil.stripSuffix( attributeName, OBJECT_NAME_SUFFIX);
        }
        else if ( isObjectNameMapAttribute( attributeName ) )
        {
            j2eeType   = StringUtil.stripSuffix( attributeName, OBJECT_NAME_MAP_SUFFIX);
        }
        
        if ( ! J2EETypes.ALL_STD.contains( j2eeType ) )
        {
            j2eeType    = XTypes.PREFIX + j2eeType;
        }
            
        return j2eeType;
    }

        protected boolean
    isObjectNameMapGetter(
		final String 		operationName,
		final Object[]	args,
		final String[]	types )
    {
        final int   numArgs = args == null ? 0 : args.length;
        return numArgs == 0 && isObjectNameMapGetter( operationName );
    }
    
        protected boolean
    isObjectNameMapGetter( final String operationName)
    {
        return getterNameMatches( operationName, OBJECT_NAME_MAP_SUFFIX );
    }
    
	    protected String
	j2eeTypeToSimpleClassname( final String j2eeType )
    {
        return StringUtil.stripPrefix( j2eeType, XTypes.PREFIX );
    }
    
    
	    protected String
	operationNameToJ2EEType( final String operationName )
    {
        String  j2eeType   = null;
        
        if ( isObjectNameGetter( operationName ) )
        {
            j2eeType   =
                StringUtil.stripPrefixAndSuffix( operationName, GET_PREFIX, OBJECT_NAME_SUFFIX);
        }
        else if ( isObjectNameMapGetter( operationName ) )
        {
            j2eeType   =
                StringUtil.stripPrefixAndSuffix( operationName, GET_PREFIX, OBJECT_NAME_MAP_SUFFIX);
        }
        
        if ( ! J2EETypes.ALL_STD.contains( j2eeType ) )
        {
            j2eeType    = XTypes.PREFIX + j2eeType;
        }
            
        return j2eeType;
    }
    
     /**
        An operation is being invoked manually, meaning that it is missing as a method.
        invokeManually() will be called only if no appropriate Method is found.
        <p>
		Subclasses may override this to handle invoke(), though usually it's just
		easier to write the appropriate method directly, which will be found and called
		if present. 
     */
    	protected Object
	invokeManually(
		String 		operationName,
		Object[]	args,
		String[]	types )
		throws MBeanException, ReflectionException, NoSuchMethodException, AttributeNotFoundException
	{
	    final int   numArgs = args == null ? 0 : args.length;
	    
	    Object  result  = null;
	    
	    boolean handled = false;
	    
	    final boolean   ALLOW_GETTERS   = true;
	    
	    if ( ALLOW_GETTERS &&
	        numArgs == 0 &&
	        operationName.startsWith( GET )  )
	    {
    	    final String    attributeName   = StringUtil.stripPrefix( operationName, GET );
    	    
	        if ( getAttributeInfos().get( attributeName ) != null )
	        {
    	        result  = getAttribute( attributeName );
    	        handled = true;
	        }
	    }
	    else if ( operationName.equals( "toString" ) && numArgs == 0 )
	    {
	        result  = toString();
	    }
	    
	    if ( ! handled )
	    {
    	    debugMethod( operationName, args );
    		throw new NoSuchMethodException( "no operation " + operationName +
    		    toString( types ) + " in " + getObjectName() );
		}
		
		return result;
	}

	
		protected void
	handleException( final Exception e )
		throws MBeanException, ReflectionException
	{
		final ThrowableMapper	mapper	= new ThrowableMapper( e );
		final Throwable			mapped	= mapper.map();
		
		if ( mapped instanceof ReflectionException )
		{
			throw (ReflectionException)mapped;
		}
		else if ( mapped instanceof MBeanException )
		{
			throw (MBeanException)mapped;
		}
		else if ( ! (mapped instanceof Exception) )
		{
			// wrap the Throwable in an Exception
			final Exception	wrapper	= new Exception( mapped );
			throw new MBeanException( wrapper );
		}
		else
		{
			throw new MBeanException( (Exception)mapped );
		}
	}
	
		protected void
	handleGetAttributeException( final Exception e )
		throws MBeanException, ReflectionException, AttributeNotFoundException
	{
		if ( e instanceof AttributeNotFoundException )
		{
			// AttributeNotFoundException can never contain anything non-standard
			throw (AttributeNotFoundException)e;
		}
		else
		{
			handleException( e );
		}
	}
	
		protected void
	handleInvokeThrowable( final Exception e )
		throws MBeanException, ReflectionException
	{
		handleException( e );
	}
	

	/**
		Generic handling of invoke(). Converts the types[] to a Class[], then attempts
		to locate a suitable Method.  If a suitable Method is found, it is invoked.
		If not found the subclass is expected to handle it in invokeManually();
	*/
		public final Object
	invoke(
		String 		operationName,
		Object[]	args,
		String[]	types )
		throws MBeanException, ReflectionException
	{
		Object	result	= null;
		boolean	unimplemented	= false;
		
		try
		{
			final Class[]	signature	= ClassUtil.signatureFromClassnames( types );
			final Method	m	= findMethod( operationName, signature );
			if ( m != null )
			{
				debugMethod( "invoking method: " + operationName, args );
				result	= m.invoke( this, args );
			}
			else if ( haveDelegate() &&
				getDelegate().supportsOperation( operationName, args, types ) )
			{
				debug( "AMXImplBase.invoke: calling delegate for ", operationName );
				result	= getDelegate().invoke( operationName, args, types );

			}
			else
			{
				result	= invokeManually( operationName, args, types );
			}
		}
		catch( Exception e )
		{
		    debug( ExceptionUtil.toString( e ) );
			handleInvokeThrowable( e );
		}
		
		return( result );
	}
	
    /*
		protected TypeInfo
	getTypeInfo( final String j2eeType )
	{
//System.out.println( "getTypeInfo: " + j2eeType + " for " + this.getClass().getName() );
		return( TypeInfos.getInstance().getInfo( j2eeType ) );
	}
	*/
    
		protected final String
	getSelfJ2EEType()
	{
		return( mJ2EEType );
	}
	
		protected String
	getSelfName()
	{
		return( Util.getName( getObjectName() ) );
	}

/*
		protected TypeInfo
	getSelfTypeInfo()
	{
//System.out.println( "getSelfJ2EEType: " + getSelfJ2EEType() );
		return( getTypeInfo( getSelfJ2EEType() ) );
	}
*/

		private boolean
	isContainer()
	{
		return( Container.class.isAssignableFrom( getInterface() ) );
	}
	
	protected final static Set<String>  EMPTY_STRING_SET    = Collections.emptySet();
	
    private ContainerSupport mContainerSupport = null;
        
  	
	/**
		Our container is the one that actually holds the MBeans we
		created. Ask it for the ObjectName.
	 */
		protected ObjectName
	getProgenyObjectName(
		final String	j2eeType,
		final String	name )
	{
		final Container	container	= getContainer();
		
		final AMX	containee	= container.getContainee( j2eeType, name );
		if ( containee == null )
		{
			throw new IllegalArgumentException( "Not containee found: " + j2eeType + "=" + name );
		}
		
		return( Util.getObjectName( containee ) );
	}
	
    /*
		protected boolean
	isOfflineCapable( final TypeInfo childInfo )
	{
	    final Class c   = childInfo.getInterface();
	    
	    return  AMXConfig.class.isAssignableFrom( c ) || 
	            Utility.class.isAssignableFrom( c ) ||
	            c == DomainRoot.class;
	}
    */
	
	    protected boolean
	getOffline()
	{
	    return false;
	}
	

		protected void
	preDeregisterHook()
	{
	}
	
		protected void
	postDeregisterHook()
	{
	}
	
		protected void
	unregisterMisc()
	{
		// nothing by default
	}
	
	
	/**
		Classes of MBeans should override this.
	*/
		public String
	getGroup()
	{
		return( GROUP_OTHER );
	}
	
		public String
	getName()
	{
		return( Util.getName( getObjectName() ) );
	}
	
		public String
	getJ2EEType()
	{
		return( Util.getJ2EEType( getObjectName() ) );
	}
	
		public final String
	getFullType( )
	{
		return( mFullType );
	}
	
	
	/**
		O the ObjectName by adding to it:
		<ul>
		<li>adding AMX.FULL_TYPE_KEY property</li>
		<li></li>
		</ul>
	*/
		protected  ObjectName
	preRegisterModifyName(
		final MBeanServer	server,
		final ObjectName	nameIn )
	{
        /*
		// now ensure that certain singleton ancestors have a name
		String	ancestorProps	= "";
		final String[]	fullTypeArray	= Util.getTypeArray( mFullType );
		for( int i = 0; i < fullTypeArray.length - 1; ++i )
		{
			final String	key	= fullTypeArray[ i ];
			
			if ( nameIn.getKeyProperty( key ) == null )
			{
				final String	name	= ObjectNames.getSingletonName( key );
				final String	prop	= Util.makeProp( key, name );
				
				ancestorProps	= Util.concatenateProps( ancestorProps, prop );
			} 
		}
		
		final String	props	=  ancestorProps;
		
		final String	newName	=
			Util.concatenateProps( nameIn.toString(), props );
		
		final ObjectName	nameOut	= Util.newObjectName( newName );

		return( nameOut );
        */
        return nameIn;
	}
	
    /*
        Note that this method is 'synchronized'--to force visibility of all fields it affects. 
        Since it's called only once (per instance) for an MBean Registration, it has no performance
        impact on later use, but guarantees visibility of all non-final instance variables.
    */
		public final synchronized ObjectName
	preRegister(
		final MBeanServer	server,
		final ObjectName	nameIn)
		throws Exception
	{
		final ObjectName	nameFromSuper	= super.preRegister( server, nameIn );

		mConnectionSource	= new MBeanServerConnectionSource( server );
		
		mDelegate	= mSuppliedDelegate;
        
		mSelfObjectName	= preRegisterModifyName( server, nameFromSuper );
		
		mSelfObjectName = preRegisterHook( mSelfObjectName );
		
		//registerSpecialContainees();
		
		preRegisterDone();
		return( mSelfObjectName );
	}
    
    /**
        This is an opportunity for a subclass to do initialization
        and optionally to modify the ObjectName one last time.
     */
        protected ObjectName
    preRegisterHook( final ObjectName selfObjectName)
	    throws Exception
    {
        // subclass may do something
        return selfObjectName;
    }
    
        protected void
    preRegisterDone()
        throws Exception
    {
		debug( "AMXImplBase.preRegister() done for: ", getObjectName() );
    }
	
	
    /*
	static private final Set<String>  AMX_NATIVE_ATTRIBUTES    =
	    Collections.unmodifiableSet( GSetUtil.newSet(
    	    new String[]
    	    {
    	        "Name",
    	        "ObjectName", "FullType", "Group", "J2EEType", "InterfaceName",
    	        "MBeanEmitLogNotifications", "MBeanInfoIsInvariant", "MBeanLoggerName",
    	        "AttributeNames", "MBeanLogLevel",
    	        "WhatsNotDone",
    	        "DomainRootObjectName", 
    	        "ContainerObjectName", "ContaineeJ2EETypes", "ContaineeObjectNameSet",
    	        "NotificationInfo",
    	        "Properties", "PropertyNames",
    	        "SystemProperties", "SystemPropertyNames",
    	        "OpenStats", "StatsInterfaceName", "StatisticNames", "Stats",
    	        "ConfigProvider",
    	    }));
	    
	
	**
		An optimization to not bother with all the names that are
		native to AMX and not mapped to a Delegate Attribute.
	 *
	    private Set<String>
    getMyAttributeMappingCandidates()
    {
		final Set<String>    candidates    = GSetUtil.newSet( getAttributeNames() );
		
		candidates.removeAll( AMX_NATIVE_ATTRIBUTES );
		
		// now remove all Attributes that end appropriately
		final Set<String>   toRemove    = new HashSet<String>();
		for( final String name : candidates )
		{
		    if ( name.endsWith( "ObjectNameMap" ) ||
		         name.endsWith( "ObjectNameSet" ) ||
		         name.endsWith( "ObjectName" ) ||
		         name.endsWith( "Stats" )
		        )
		    {
		        toRemove.add( name );
		    }
		}
		
		candidates.removeAll( toRemove );
		
		return candidates;
    }
    */

	/*
		protected void
	implCheck()
	{
		final boolean	isContainer	= isContainer();
		final String	j2eeType	= getSelfJ2EEType();
		final TypeInfo	selfInfo	= TypeInfos.getInstance().getInfo( j2eeType );
		
		final Set<String>	nonChildren	= selfInfo.getNonChildJ2EETypes();
		final Set<String>	children	= selfInfo.getChildJ2EETypes();
		
		if ( isContainer )
		{
			assert( nonChildren.size() != 0 || children.size() != 0 ) :
				"ERROR: is Container but contains no children or containees " + j2eeType;
		}
		else
		{
			assert( nonChildren.size() == 0 ) :
				"ERROR: not a Container: " + j2eeType + " but contains types: " + toString( nonChildren );
				
			assert( children.size() == 0 ) :
				"ERROR: not a Container: " + j2eeType + " but contains children: " + toString( children );
		}
		
	    checkSuperfluousMethods();
	}
    */
	
	
	private static final Set<String> NOT_SUPERFLUOUS =
	    GSetUtil.newUnmodifiableStringSet(
	        "getProxyFactory", 
            "getDomainRootObjectName",
            "getQueryMgrObjectName",
            "getServerRootMonitorObjectName"
            );
            
    
    /**
        @return any non-superflous methods that are the exception to the default assumptions
     */
		protected Set<String>
	getNotSuperfluousMethods()
	{
	    return NOT_SUPERFLUOUS;
	}
	
    /**
        @return all method names that appear superfluous
     */
		protected Set<String>
	getSuperfluousMethods()
	{
	    final Set<String>   items   = new HashSet<String>();
	    
	    final Method[]  methods = this.getClass().getMethods();
	    for( final Method m : methods )
	    {
            if ( JMXUtil.isGetter( m ) )
            {
	            final String    name    = m.getName();
	        
                final String attributeName  = StringUtil.stripPrefix( name, GET );
                if ( isObjectNameAttribute( attributeName ) ||
                    isObjectNameMapAttribute( attributeName ) )
                {
                    items.add( name );
                }
            }
	    }
	    
	    items.removeAll( NOT_SUPERFLUOUS );
	    
	    return items;
	}

		protected final void
	checkSuperfluousMethods()
	{
	    final Set<String>   items = getSuperfluousMethods();
	    
	    items.removeAll( getNotSuperfluousMethods() );
	    
	    if ( items.size() != 0 )
	    {
	        final String    LINE_SEP    = System.getProperty( "line.separator" );
	        
            final String msg    = 
            "The following methods in " + getJ2EEType() +
            " are probably superfluous:" + LINE_SEP +
            CollectionUtil.toString( items, LINE_SEP ) + LINE_SEP;
	        
	        AMXDebug.getInstance().getOutput( "AMXImplBase.checkSuperfluousMethods" ).println( msg );
	        logFine( msg );
	    }
	}
	
    /**
        Hide this dirty business: non-serializable and private class.
     */
    private static final class ObjectRefWrapper
    {
        final AMXImplBase mPayload;
        ObjectRefWrapper( final AMXImplBase payload ) { mPayload = payload; }
    }
    
    /*
        This *hack* allows us to get the actual object itself, preferable to exposing
        an operation to the "whole world".
     */
        private AMXImplBase
    getContainerObject()
    {
        AMXImplBase containerObject = null;
        
        final ObjectName containerObjectName = getContainerObjectName();
        // shouldn't have to call isRegistered(), but special MBeans at startup
        // do not have a Container (eg SystemInfo).
        if ( containerObjectName != null && getMBeanServer().isRegistered(containerObjectName) )
        {
            try
            {
                final Object value =  getMBeanServer().getAttribute( containerObjectName, OBJECT_REF_ATTR_NAME );
                containerObject = ObjectRefWrapper.class.cast( value ).mPayload;
            }
            catch( Exception e )
            {
                e.printStackTrace();
            }
        }
        return containerObject;
    }
    
    /*
        Note that this method is 'synchronized'--to force visibility of all fields it affects. 
        Since it's called only once (per instance) for an MBean Registration, it has no performance
        impact on later use, but guarantees visibility of all non-final instance variables, both
        on this class and all subclasses, since they can only modify things via postRegisterHook().
    */
		public final synchronized void
	postRegister( Boolean registrationSucceeded )
	{
		super.postRegister( registrationSucceeded );
		
        if ( registrationSucceeded.booleanValue() )
        {
            //------------------------------------------------------
            final AMXImplBase containerObject = getContainerObject();
            if ( getContainerObjectName() == null && getInterface() != DomainRoot.class ) {
                System.out.println("postRegister: containerObject null for " + getObjectName() );
            }
            if ( containerObject != null )
            {
                containerObject.getContainerSupport().containeeRegistered( getObjectName() );
            }
            //------------------------------------------------------
        }
        
		postRegisterHook( registrationSucceeded );
	}
    
    private static final String OBJECT_REF_ATTR_NAME = "__ObjectRef";
        public ObjectRefWrapper
    get__ObjectRef()
    {
        return new ObjectRefWrapper(this);
    }
	
		public void
	postRegisterHook( Boolean registrationSucceeded )
	{
	    if ( registrationSucceeded.booleanValue() )
		{
		}
	}

		public final void
	preDeregister()
		throws Exception
	{
	    super.preDeregister();
	    
	    preDeregisterHook();
	}
	
		public void
	postDeregister()
	{
	    super.postDeregister();
        
        //------------------------------------------------------
        final AMXImplBase containerObject = getContainerObject();
        if ( containerObject != null )
        {
            containerObject.getContainerSupport().containeeUnregistered( getObjectName() );
        }
        //------------------------------------------------------
	    
	    postDeregisterHook();
	}

		public final ObjectName
	getDomainRootObjectName()
	{
		return( Util.getObjectName( getDomainRoot() ) );
	}

	/**
		The QueryMgr is a special-case; all the other types rely on it.
	*/
		public ObjectName
	getQueryMgrObjectName()
	{
		ObjectName	objectName	= null;
		
		if ( mQueryMgr != null )
		{
			// do it the fast way if we already have the proxy
			objectName	= Util.getObjectName( mQueryMgr );
		}
		else
		{
			final MBeanServer	server		= getMBeanServer();
			final String		domainName	= getObjectName().getDomain();
			
			objectName	= QueryMgrImpl.querySingletonJ2EETypeObjectName( server,
							domainName, QueryMgr.J2EE_TYPE );
		}
	
		assert( objectName != null ) : "getQueryMgrObjectName failed";
		return( objectName );
	}
	
	
		protected ConnectionSource
	getMBeanServerConnectionSource()
	{
		return( mConnectionSource );
	}
	
		protected final synchronized AMX
	getSelf()
	{
		if ( mSelfProxy == null )
		{
			final ObjectName	selfObjectName	= getObjectName();
			assert( selfObjectName != null );
			
			mSelfProxy	= getProxyFactory().getProxy( selfObjectName, AMX.class );
			assert( mSelfProxy != null );
		}
		return( mSelfProxy );
	}
		protected final <T> T
	getSelf( final Class<T> theClass )
	{
        return theClass.cast( getSelf() );
    }
    
    protected Container getSelfAsContainer() { return getSelf(Container.class); }
	
	
		public final DomainRoot
	getDomainRoot()
	{
		return( getProxyFactory().getDomainRoot() );
	}
	
	
		protected final QueryMgr
	getQueryMgr()
	{
        // this relies on mQueryMgr being 'volatile'
		if ( mQueryMgr != null )
            return mQueryMgr;
        
        final ObjectName	objectName	= getQueryMgrObjectName();
        if ( objectName != null )
        {
            // it doesn't matter if two thread do this; the same proxy will be returned.
            mQueryMgr	= getProxyFactory().getProxy( objectName, QueryMgr.class);
        }
    
		return( mQueryMgr );
	}


	/**
		@param parentType
		@param subType
	 */
		private static String
	makeType( final String parentType, final String subType )
	{
		String	result	= null;
		
		if ( parentType == null || parentType.length() == 0 )
		{
			result	= subType;
		}
		else
		{
			result	= parentType + AMX.FULL_TYPE_DELIM + subType;
		}

		return( result  );
	}
	
  	
	//------------------------ Access to other MBeans --------------------------------
	
	
		protected Object
	getAttribute(
		final ObjectName	objectName,
		String				name )
		throws AttributeNotFoundException, InstanceNotFoundException,
				ReflectionException, MBeanException
	{
		return( getMBeanServer().getAttribute( objectName, name ) );
	}
	
		protected AttributeList
	getAttributes(
		final ObjectName	objectName,
		String[] 			names )
		throws AttributeNotFoundException, InstanceNotFoundException,
				ReflectionException, MBeanException
	{
		return( getMBeanServer().getAttributes( objectName, names ) );
	}
	
		protected void
	setAttribute( 
		final ObjectName	objectName,
		Attribute			attr )
		throws AttributeNotFoundException, InvalidAttributeValueException,
				InstanceNotFoundException,
				ReflectionException, MBeanException
	{
		getMBeanServer().setAttribute( objectName, attr );
	}
	
		protected AttributeList
	setAttributes(
		final ObjectName	objectName,
		AttributeList		attrs )
		throws AttributeNotFoundException, InvalidAttributeValueException,
				InstanceNotFoundException,
				ReflectionException, MBeanException
	{
		return( getMBeanServer().setAttributes( objectName, attrs ) );
	}
	
	//-------------------------------------------------------------------------------
	
		protected ObjectName
	registerMBean( Object mbean, ObjectName name )
		throws MalformedObjectNameException, InstanceAlreadyExistsException,
		NotCompliantMBeanException, MBeanRegistrationException
	{
		return getMBeanServer().registerMBean( mbean, name ).getObjectName();
	}
	
	
		protected String
	stringify( Object o )
	{
		return( SmartStringifier.toString( o ) );
	}
	
	
	    public String
	toString()
	{
	    return getImplString( false );
	}
	
	    public String
	getImplString( final boolean verbose )
	{
	    final String NEWLINE    = System.getProperty( "line.separator" );
	    
	    String s = this.getClass().getName() + NEWLINE +
	        MBeanInfoStringifier.DEFAULT.stringify( getMBeanInfo() ) + NEWLINE;
	    
	    if ( verbose )
	    {
	        final AttributeList attrs   = getAttributes( getAttributeNames() );
	        final Map<String,Object>    m   = JMXUtil.attributeListToValueMap( attrs );
	        s   = NEWLINE + s + MapUtil.toString( m, NEWLINE + NEWLINE ) + NEWLINE;
	    }
	    
	    return s;
	}
    
    // FIX
    public boolean isDAS()
    {
        Issues.getAMXIssues().notDone( "AMXImplBase: how to determine if this is the DAS?" );
        return true;
    }
    
    
	//---------------------------------- Container support ---------------------------------------------
          protected void
    addContainee( final ObjectName objectName)
    {
        getContainerSupport().addContainee( objectName );
    }
    
       protected void
    removeContainee( final ObjectName objectName)
    {
        getContainerSupport().removeContainee( objectName );
    }

        protected ContainerSupport
    getContainerSupport()
    {
		if ( ! isContainer() )
		{
    System.out.println("NOT A CONTAINER: " + getObjectName() );
			throw new UnsupportedOperationException("MBean " + StringUtil.quote(getObjectName()) + " is not an AMX 'Container'");
		}
        
        synchronized(this)
        {
            if ( mContainerSupport == null )
            {
                mContainerSupport = new ContainerSupport( getMBeanServer(), getObjectName() );
            }
        }
        return mContainerSupport;
    }	

        public Set<String>
    getContaineeJ2EETypes()
    {
        final Set<String> j2eeTypes = getContainerSupport().getContaineeJ2EETypes();
        
        return j2eeTypes;
    }
	
        public Map<String,Map<String,ObjectName>>
    getMultiContaineeMap( final Set<String> j2eeTypes )
    {
        return getContainerSupport().getMultiContaineeObjectNameMap( j2eeTypes );
    }
	
        public Map<String,ObjectName>
    getContaineeObjectNameMap( final String j2eeType )
    {
        return getContainerSupport().getContaineeObjectNameMap( j2eeType );
    }
	
        public ObjectName
    getContaineeObjectName( final String j2eeType )
    {
        return getContainerSupport().getContaineeObjectName( j2eeType );
    }
	
        public Set<ObjectName>
    getContaineeObjectNameSet( final String j2eeType )
    {
        return getContainerSupport().getContaineeObjectNameSet( j2eeType );
    }
	
        public Set<ObjectName>
    getContaineeObjectNameSet()
    {
        return getContainerSupport().getContaineeObjectNameSet();
    }
	
        public Set<ObjectName>
    getContaineeObjectNameSet( final Set<String> j2eeTypes )
    {
        return getContainerSupport().getContaineeObjectNameSet( j2eeTypes );
    }
	
	
        public Set<ObjectName> 
    getByNameContaineeObjectNameSet( final Set<String> j2eeTypes, final String name )
    {
        return getContainerSupport().getByNameContaineeObjectNameSet( j2eeTypes, name );
    }
	
	public ObjectName	getContaineeObjectName( final String j2eeType, final String name )
    {
        return getContainerSupport().getContaineeObjectName( j2eeType, name );
    }


}








