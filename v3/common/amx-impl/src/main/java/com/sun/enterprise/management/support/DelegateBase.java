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
 * $Header: /cvs/glassfish/admin/mbeanapi-impl/src/java/com/sun/enterprise/management/support/DelegateBase.java,v 1.5 2007/05/05 05:23:41 tcfujii Exp $
 * $Revision: 1.5 $
 * $Date: 2007/05/05 05:23:41 $
 */

package com.sun.enterprise.management.support;

import java.util.Set;

import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.ReflectionException;
import javax.management.MBeanException;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanOperationInfo;

import com.sun.appserv.management.base.AMXDebug;

import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.util.misc.ArrayConversion;
import com.sun.appserv.management.util.misc.Output;

/**
	Delegate base class which most Delegates will want to extend.
 */
public abstract class DelegateBase implements Delegate
{
	private Set	mAttributeNames;
	private DelegateOwner		mOwner;
	private Output              mDebug;
	private final String        mID;
	
	/**
		An operation has not been implemented. Deal with appropriately.
	 */
		protected void
	unimplementedOperation( String operation )
	{
	    debug( "unimplemented operation: " + operation );
		throw new UnsupportedOperationException( operation );
	}
	
		public
	DelegateBase( final String id, DelegateOwner owner )
	{
	    mID = id;
		mAttributeNames	= null;
		mOwner	= owner;
		mDebug  = null;
	}
	
	    public void
	setDebugOutput( final Output debugOutput )
	{
	    mDebug  = debugOutput;
	}
	
	 	protected final void
	debug(final Object o)
	{
	    if ( mDebug != null )
	    {
	        mDebug.println( o );
	    }
	}
	
	
	public final String    getID()    { return mID; }
	
		public void
	setOwner( final DelegateOwner	owner )
	{
		mOwner	= owner;
	}
	
		public DelegateOwner
	getOwner()
	{
		return( mOwner );
	}

	/**
		Default behavior is to loop over each Attribute; subclass
		may wish to maintain atomicity by implementing directly.
	 */
		public AttributeList
	getAttributes( final String[] attrNames )
	{
		final AttributeList		attrs	= new AttributeList();
		
		for( int i = 0; i < attrNames.length; ++i )
		{
			try
			{
				final String	attrName	= attrNames[ i ];
				
				final Attribute	attr	=
					new Attribute( attrName, getAttribute( attrName ) );
				attrs.add( attr );
			}
			catch( Exception e )
			{
				// ignore
			}
		}
		
		return( attrs );
	}
	
	
	/**
		Default behavior is too loop over each Attribute; subclass
		may wish to maintain atomicity by implementing directly.
	 */
		public AttributeList
	setAttributes( final AttributeList attrs )
	{
		final int			numAttrs	= attrs.size();
		final AttributeList	successList	= new AttributeList();
		
		for( int i = 0; i < numAttrs; ++i )
		{
			final Attribute attr	= (Attribute)attrs.get( i );
			try
			{
				setAttribute( attr );
				
				successList.add( attr );
			}
			catch( AttributeNotFoundException e )
			{
				// ignore, as per spec
			}
			catch( InvalidAttributeValueException e )
			{
				// ignore, as per spec
			}
		}
		return( successList );
	}
	
	
	
	/**
		Do the classnames match the parameter infos?
	 */
		private boolean
	typesMatch(
		final String[]				types,
		final MBeanParameterInfo[]	paramInfos )
	{
		boolean	matches	= false;
		final int  numTypes  = types == null ? 0 : types.length;
		final int   numParams   = paramInfos == null ? 0 : paramInfos.length;
		
		if ( numTypes == numParams )
		{
			matches	= true;
			
			for( int i = 0; i < numTypes; ++i )
			{
				if ( ! types[ i ].equals( paramInfos[ i ].getType() ) )
				{
					matches	= false;
					break;
				}
			}
		}
		
		return( matches );
	}

		public synchronized boolean
	supportsAttribute( String 		attrName )
	{
		if ( mAttributeNames == null )
		{
			final String[]	attrNames	=
				JMXUtil.getAttributeNames( getMBeanInfo().getAttributes() );
				
			mAttributeNames	= ArrayConversion.arrayToSet( attrNames );
		}
		
		return( mAttributeNames.contains( attrName ) );
	}
    
        protected String
    _getDefaultValue( final String name )
        throws AttributeNotFoundException
    {
        return "!!! NO DEFAULT VALUE FOR: \"" + name + "\" !!!" + ", " + this.getClass().getName();
    }
    
        public final String
    getDefaultValue( final String name )
        throws AttributeNotFoundException
    {
        if ( ! supportsAttribute( name ) )
        {
            throw new AttributeNotFoundException( name );
        }
        
        return _getDefaultValue( name );
    }

		public boolean
	supportsOperation(
		String 		operationName,
		Object[]	args,
		String[]	types )
	{
		boolean	supports	= false;
		
		final MBeanOperationInfo[]	opInfos	= getMBeanInfo().getOperations();
		
		for( int i = 0; i < opInfos.length; ++i )
		{
			final MBeanOperationInfo	info	= opInfos[ i ];
			
			if ( info.getName().equals( operationName ) )
			{
				if ( typesMatch( types, info.getSignature() ) )
				{
					supports	= true;
					break;
				}
			}
		}
		
		return( supports );
	}
	
}








