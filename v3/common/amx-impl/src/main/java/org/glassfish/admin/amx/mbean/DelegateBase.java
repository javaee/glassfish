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

import com.sun.appserv.management.util.misc.Output;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanParameterInfo;
import java.util.Map;

/**
	Delegate base class which most Delegates will want to extend.
 */
public abstract class DelegateBase implements Delegate
{
	//private Set<String>	mAttributeNames;
	private volatile DelegateOwner		mOwner;
	private volatile Output              mDebug;
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
	DelegateBase(
        final String id )
	{
	    mID = id;
		//mAttributeNames	= null;
		mOwner	= null;
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
		Default behavior is to loop over each Attribute; subclass
		may wish to maintain atomicity by implementing directly.
	 */
	public abstract AttributeList setAttributes( final AttributeList attrs, final Map<String,Object> oldValues);
	

		public boolean
	supportsAttribute( String attrName )
	{
        return false;
	}

		public boolean
	supportsOperation(
		String 		operationName,
		Object[]	args,
		String[]	types )
	{
        return false;
	}

	/**
	 */
		public final Object
	invoke(
		String 		operationName,
		Object[]	args,
		String[]	types )
	{
        throw new RuntimeException( "invoke() not yet implemented" );
	}
}








