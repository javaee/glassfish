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
package com.sun.enterprise.management.support;

import java.util.Map;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.ObjectName;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeChangeNotification;
import javax.management.InstanceNotFoundException;
import javax.management.ReflectionException;
import javax.management.MBeanException;
import javax.management.IntrospectionException;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;

import javax.management.MBeanServerInvocationHandler;



import com.sun.appserv.management.util.misc.ClassUtil;
import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.jmx.JMXUtil;

import com.sun.appserv.management.config.PropertiesAccess;
import com.sun.appserv.management.base.XTypesMapper;
import com.sun.appserv.management.base.Util;

import com.sun.appserv.management.base.DottedNames;
import com.sun.appserv.management.base.AMX;

import com.sun.enterprise.management.support.ObjectNames;

import com.sun.appserv.management.helper.AMXDebugHelper;
import com.sun.appserv.management.util.misc.RunnableBase;

import com.sun.enterprise.util.FeatureAvailability;
import static com.sun.enterprise.util.FeatureAvailability.SERVER_STARTED_FEATURE;


/**
	Base class from which the DottedNames (config and monitoring) MBeans
	inherit.
	<p>
 */
public abstract class DottedNamesBase extends AMXImplBase
	implements DottedNames
{
	private static final String	DOTTED_NAMES	=
		"com.sun.appserv:name=dotted-name-get-set,type=dotted-name-support";
	
    // must be volatile; see getOldDottedNames
	protected volatile OldDottedNamesProxy	mOldDottedNamesProxy = null;
	
	private MBeanInfo	mMBeanInfo;
	private Map<String,Attribute>			mAttributes;
	
	private Set<String>			mParentAttributeNames;
	
    protected final AMXDebugHelper    mDebugHelper  = new AMXDebugHelper( "DottedNamesBase" );
    protected void debugMsg( final Object...args )   { mDebugHelper.println( args ); }

		public
	DottedNamesBase()
	{
		mMBeanInfo	= null;
		
		mParentAttributeNames	= null;
        
        debugMsg( "DottedNamesBase: " + this.getClass().getName() );
        mDebugHelper.setEchoToStdOut( true );
	}
	
	
	public abstract Object[]	dottedNameGet( String[] names );
	public abstract Object		dottedNameGet( String name );
	public abstract Object[]	dottedNameList( String[] names );
	public abstract Object[]	dottedNameSet( String[] nameValuePairs );
	
	protected abstract boolean		isWriteableDottedName( String name );

	
	/**
		MBeanInfo Attributes change whenever the dotted-namespace changes.
	 */
		public boolean
	getMBeanInfoIsInvariant()
	{
		return( false );
	}
	
	/**
		Note the during registration, we won't have setup our delegate, and our
		MBeanInfo will therefore not contain any Attributes.
	 */
		public MBeanInfo
	getMBeanInfo()
	{
		MBeanInfo	mbeanInfo	= null;
        
        checkRefresh();
        
		if ( getOldDottedNames() != null )
		{
			ensureMBeanInfo();
			mbeanInfo	= mMBeanInfo;
		}
		else
		{
			mbeanInfo	= super.getMBeanInfo();
		}
		
		return( mbeanInfo );
	}
	
    	public void
	postRegisterHook( final Boolean registrationSucceeded )
	{
        super.postRegisterHook( registrationSucceeded );
        
	    if ( registrationSucceeded.booleanValue() )
		{
            // start a thread upon completion of server startup that will mark things as needing
            // a refresh, providing an accurate view of all available attributes without requiring
            // the client to make an explicit call to refresh().  Failure to do this means
            // that an empty or minimal state will be improperly returned.
            final RunnableBase refresher = new RunnableBase() {
                protected void doRun() {
                    FeatureAvailability.getInstance().waitForFeature(
                            SERVER_STARTED_FEATURE, "DottedNamesBase");
                    
                    setupOldDottedNamesProxy();
                    mRefreshNeeded   = true; // refresh lazily
                }
            };
            refresher.submit( RunnableBase.HowToRun.RUN_IN_SEPARATE_THREAD );
		}
	}
	
	/**
		Proxy representing the "old" DottedNames
 	*/
	protected interface OldDottedNamesProxy
	{
		public Object[]	dottedNameGet( String[] names );
		public Object	dottedNameGet( String name );
		public Object[]	dottedNameMonitoringGet( String[] names );
		public Object	dottedNameMonitoringGet( String names );
		public String[]	dottedNameList( String[] names );
		public String[]	dottedNameMonitoringList( String[] names );
		public Object[]	dottedNameSet( String[] names );
	}

		protected void
	setupOldDottedNamesProxy( )
	{
		setupOldDottedNamesProxy( Util.newObjectName( DOTTED_NAMES ) );
	}

		protected void
	setupOldDottedNamesProxy( final ObjectName target )
	{
		final MBeanServer	server	= getMBeanServer();
        
        // server can be null early at startup
		if ( server != null && server.isRegistered( target ) )
		{
			mOldDottedNamesProxy	= (OldDottedNamesProxy)
                MBeanServerInvocationHandler.newProxyInstance(
                    server, target, OldDottedNamesProxy.class, false );
		}
	}
	
		protected OldDottedNamesProxy
	getOldDottedNames()
	{
        // synchronization not needed; making more than one proxy is not an issue
        if ( mOldDottedNamesProxy == null ) {
            setupOldDottedNamesProxy();
        }
        
		return( mOldDottedNamesProxy );
	}
	
		protected final boolean
	isDottedName( final String name )
	{
		return( getAttributes().keySet().contains( name ) );
	}
	
		protected final boolean
	isParentAttributeName( final String name )
	{
		return( mParentAttributeNames.contains( name ) );
	}
	
		protected final void
	filterNames(
		final String[]	in,
		Set<String>		dotted,
		Set<String>		parent )
	{
		for( int i = 0; i < in.length; ++i )
		{
			final String	name	= in[ i ];
			
			if ( isDottedName( name ) )
			{
				dotted.add( name );
			}
			else if ( isParentAttributeName( name ) )
			{
				parent.add( name );
			}
		}
	}
    
            
		public AttributeList
	getAttributes( final String[]	names )
	{
        checkRefresh();
        
    	mCoverage.attributesWereRead( names );
	    
		final Set<String>	dotted	= new HashSet<String>();
		final Set<String>	parent	= new HashSet<String>();
		filterNames( names, dotted, parent);

		final Object[]	dottedResults	=
			dottedNameGet( (String[])dotted.toArray( new String[ dotted.size() ] ) );
		
		final String[]		namesForParent	= new String[ parent.size() ];
		final AttributeList	parentResults	=
			super.getAttributes( (String[])parent.toArray( namesForParent ) );
		
		final AttributeList	successList	= new AttributeList();
		successList.addAll( parentResults );
		
		// add all the dotted name results
		for( int i = 0; i < dottedResults.length; ++i )
		{
			if ( dottedResults[ i ] instanceof Attribute )
			{
				successList.add( (Attribute)dottedResults[ i ] );
			}
			else
			{
				assert( dottedResults[ i ] instanceof Exception );
			}
		}
		
		
		return( successList );
	}
	
		public Object
	getAttribute( final String	name )
		throws AttributeNotFoundException
	{
		checkLegalName( name );
	    mCoverage.attributeWasRead( name );
		
		Object	result	= null;
		
		if ( isDottedName( name ) )
		{
			result	= dottedNameGet( name );
			assert( !(result instanceof Attribute) );
		}
		else if ( isParentAttributeName( name ) )
		{
			result	= super.getAttribute( name );
		}
		else
		{
			throw new AttributeNotFoundException( name );
		}
		
		return( result );
	}
	
	
		public void
	setAttribute( final Attribute attr )
		throws AttributeNotFoundException, InvalidAttributeValueException
	{
	    final String    name    = attr.getName();
	    
	    if ( isParentAttributeName( name ) )
	    {
	        super.setAttribute( attr );
	    }
	    else
	    {
    		checkLegalName( name );
    	    mCoverage.attributeWasWritten( name );
    		
    		final AttributeList		inList	= new AttributeList();
    		inList.add( attr );
    		final AttributeList	result	= setAttributes( inList );
    		if ( result.size() != 1)
    		{
    			throw new InvalidAttributeValueException( attr.getName() );
    		}
		}
	}
	
		public AttributeList
	setAttributes( AttributeList attributes )
	{
		/*
			Convert each attribute to a name/value pair.
			Omit any attributes that don't have a legal attribute name
		 */
		final int	numAttrsIn	= attributes.size();
		final List<String>	legalPairs	= new ArrayList<String>();
		for( int i = 0; i < numAttrsIn; ++i )
		{
			final Attribute	attr	= (Attribute)attributes.get( i );
			
			final String    name    = attr.getName();
			mCoverage.attributeWasWritten( name );
			
			if ( isLegalAttributeName( name ) )
			{
				legalPairs.add( attributeToNamePair( attr ) );
			}
		}
		
		final String[]	pairs	= (String[])legalPairs.toArray( new String[ legalPairs.size() ] );

		final Object[] results	= dottedNameSet( pairs );
		
		final AttributeList	attributeList	= new AttributeList();
		for( int i = 0; i < results.length; ++i )
		{
			if ( results[ i ] instanceof Attribute )
			{
				attributeList.add( (Attribute)results[ i ] );
			}
			else
			{
				assert( results[ i ] instanceof Exception );
				// it's an exception
			}
		}
		
		return( attributeList );
	}
	
	
		public Object
	invokeManually(
		String		operationName,
		Object[]	params,
		String[]	types)
		throws ReflectionException, MBeanException, NoSuchMethodException,
		AttributeNotFoundException
	{
		final boolean	noParams	= params == null || params.length == 0;
		Object			result	= null;
		
		if ( operationName.equals( "refresh" ) && noParams )
		{
			refresh();
			result	= null;
		}
		else
		{
			result	= super.invokeManually( operationName, params, types );
		}
		return( result );
	}
	
		private synchronized void
	ensureMBeanInfo()
	{
		if ( mMBeanInfo == null )
		{
			refresh();
			assert( mMBeanInfo != null );
		}
	}
	
	
		protected MBeanAttributeInfo[]
	buildAttributeInfos( final MBeanAttributeInfo[] parentAttributeInfos )
	{
		final String[]	parentAttributeNames	=
			JMXUtil.getAttributeNames( parentAttributeInfos );
		mParentAttributeNames	=
		    GSetUtil.newUnmodifiableStringSet( parentAttributeNames );
		
		final Map<String,Attribute>		attributes	=	getAttributes();
		
		final MBeanAttributeInfo[]	infos =
			new MBeanAttributeInfo[ attributes.size()  + parentAttributeInfos.length ];
		
		// make info for every Attribute
		
		int i	= 0;
		for( final String name : attributes.keySet() )
		{
			final Attribute	attr	= attributes.get( name );
		
			final Object	value	= attr.getValue();
			final Class	theClass	= value == null ? String.class : attr.getValue().getClass();
			
			infos[ i ]	= new MBeanAttributeInfo( name, theClass.getName(), "",
				true, isWriteableDottedName( name ), false );
			++i;
		}
		
		System.arraycopy( parentAttributeInfos, 0,
			infos, attributes.size(), parentAttributeInfos.length );

		return( infos );
	}
	
		protected MBeanOperationInfo[]
	buildOperationInfos( final MBeanOperationInfo[] existing )
	{
		final MBeanOperationInfo	refreshInfo	= new MBeanOperationInfo( "refresh",
			"update MBeanInfo to reflect all available dotted names",
			null,
			Void.class.getName(),
			MBeanOperationInfo.ACTION );
		
		final MBeanOperationInfo[]	infos	= new MBeanOperationInfo[ existing.length + 1 ];
		System.arraycopy( existing, 0, infos, 0, existing.length );
		infos[ infos.length -1 ]	= refreshInfo;
		
		return( infos );
	}
	
		protected MBeanInfo
	buildMBeanInfo()
	{
		final MBeanInfo	superMBeanInfo	= super.getMBeanInfo();
		
		final MBeanAttributeInfo[]		attributeInfos		=
				buildAttributeInfos( superMBeanInfo.getAttributes() );
		final MBeanOperationInfo[]		operationInfos		=
				buildOperationInfos( superMBeanInfo.getOperations() );
		
		final MBeanInfo	info	= new MBeanInfo( this.getClass().getName(),
									"exposes dotted-names as Attributes",
									attributeInfos,
									superMBeanInfo.getConstructors(),
									operationInfos,
									superMBeanInfo.getNotifications() );
		
		return( info );
	}
	
	
	
	
		private static Attribute
	namePairToAttribute( String pair )
	{
		final int			delimIndex	= pair.indexOf( "=" );
		assert( delimIndex >= 1 );
		final String		name	= pair.substring( 0, delimIndex );
		final String		value	= pair.substring( delimIndex + 1, pair.length() );
		
		return( new Attribute( name, value ) );
	}
	
		private static String
	attributeToNamePair( Attribute attr )
	{
		return( attr.getName() + "=" + attr.getValue() );
	}
	
		protected final boolean
	isLegalAttributeName( final String name )
	{
		ensureAttributes();
		return( getAttributes().keySet().contains( name ) ||
				mParentAttributeNames.contains( name ) );
	}
	
		protected final void
	ensureAttributes()
	{
		if ( mAttributes == null )
		{
			refreshAttributes();
		}
	}

		protected final Map<String,Attribute>
	getAttributes()
	{
		ensureAttributes();
		
		return( mAttributes );
	}

		protected final String[]
	getDottedNamesArray()
	{
		final Map<String,Attribute>	map	= getAttributes();
		
		final Set<String>	keySet	= map.keySet();
		
		return( (String[])keySet.toArray( new String[ keySet.size() ] ) );
	}
	
	
		private final void
	checkLegalName( String name )
		throws AttributeNotFoundException
	{
        checkRefresh();
        
		if ( ! isLegalAttributeName( name ) )
		{	    
			throw new AttributeNotFoundException( "illegal attribute name: " + name );
		}
	}
	

	private static final String	ALL_DOTTED_NAMES	= "*";
    
    protected volatile boolean mRefreshNeeded   = false;
    protected final ReentrantLock mRefreshLock = new ReentrantLock();
	/**
		Refresh the MBeanInfo to reflect the currently available attributes.
	 */
		final void
	refreshAttributes()
	{
		Object	result	= null;
		
        // if the lock cannot be acquired, that's just fine--another thread is already
        // doing the work.
        if ( mRefreshLock.tryLock() )
        {
            try
            {
                //trace( "##### DottedNamesBase.refreshAttributeNames" );
                result	= dottedNameGet( ALL_DOTTED_NAMES );
                
                // results is an array of length 1.  It should contain an Object[] containing
                // everything obtained from ALL_DOTTED_NAMES
                final Attribute[]	values	= (Attribute[])result;
                
                // extract the name of each attribute
                final Map<String,Attribute>	map	= new HashMap<String,Attribute>();
                for( final Attribute attr : values )
                {
                    map.put( attr.getName(), attr );
                }
                
                mAttributes	= map;
                
                mMBeanInfo	= buildMBeanInfo();
                
                mRefreshNeeded = false;
            }
            finally
            {
                mRefreshLock.unlock();
            }
        }
        else
        {
            // wait till the other thread has finished, in order to allow
            // the refresh to finish.
            mRefreshLock.lock();
            
            // release it
            mRefreshLock.unlock();
        }
	}
    
        private void
    checkRefresh()
    {
        if ( mRefreshNeeded )
        {
            refreshAttributes();
        }
    }
    
		public final void
	refresh()
	{
        refreshAttributes();
	}
}








