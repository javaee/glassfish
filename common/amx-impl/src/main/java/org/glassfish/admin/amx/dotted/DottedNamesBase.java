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
package org.glassfish.admin.amx.dotted;

import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.helper.AMXDebugHelper;
import org.glassfish.admin.amx.mbean.AMXNonConfigImplBase;

import javax.management.AttributeNotFoundException;
import javax.management.ObjectName;


/**
	Base class from which the DottedNames (config and monitoring) MBeans
	inherit.
	<p>
 */
public abstract class DottedNamesBase extends AMXNonConfigImplBase
{
    protected final AMXDebugHelper    mDebugHelper  = new AMXDebugHelper( "DottedNamesBase" );
    protected void debugMsg( final Object...args )   { mDebugHelper.println( args ); }

		public
	DottedNamesBase(
        final String j2eeType,
        final String fullType,
        final ObjectName parentObjectName,
		final Class<? extends AMX> theInterface )
	{
        super( j2eeType, fullType, parentObjectName, theInterface, null );
        debugMsg( "DottedNamesBase: " + this.getClass().getName() );
        mDebugHelper.setEchoToStdOut( true );
	}
	
	
	public abstract Object[]	dottedNameGet( String[] names );
	public abstract Object		dottedNameGet( String name );
	public abstract Object[]	dottedNameList( String[] names );
	public abstract Object[]	dottedNameSet( String[] nameValuePairs );
	
	protected abstract boolean		isWriteableDottedName( String name );
	
    /*
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
		*
			Convert each attribute to a name/value pair.
			Omit any attributes that don't have a legal attribute name
		 *
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
    
    */
            		
		protected final String[]
	getDottedNamesArray()
	{
        /*
		final Map<String,Attribute>	map	= getAttributes();
		
		final Set<String>	keySet	= map.keySet();
		
		return( (String[])keySet.toArray( new String[ keySet.size() ] ) );
        */
        return null;
	}
	
	
		private final void
	checkLegalName( String name )
		throws AttributeNotFoundException
	{
        /*
        checkRefresh();
        
		if ( ! isLegalAttributeName( name ) )
		{	    
			throw new AttributeNotFoundException( "illegal attribute name: " + name );
		}
        */
	}
	

	private static final String	ALL_DOTTED_NAMES	= "*";
    
    /*
    protected volatile boolean mRefreshNeeded   = false;
    protected final ReentrantLock mRefreshLock = new ReentrantLock();]
    
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
    */
}








