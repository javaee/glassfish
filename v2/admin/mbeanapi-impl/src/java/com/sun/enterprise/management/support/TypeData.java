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
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
 
/*
 */

package com.sun.enterprise.management.support;

import java.util.Set;
import java.util.Collections;

import com.sun.appserv.management.util.misc.ObjectUtil;


/**
	Basic data from which we derive all our TypeInfo. Any j2eeType must either
	have 1 or more legal parent types ( in which case it is a subType),
	or must have a contained-by j2eeType (possibly null), in which case it is a
	top-level type, possibly contained by something else.
	<p>
	Separating these two ideas enables reasonably short types that don't have to
	artifically include the singletons they are logically contained in.  For example,
	it enables the type "X-ConfigConfig" instead of 
	"X-DomainRoot.X-DomainConfig.X-ConfigConfig", which is considerably longer
	than (unnecessarily so).
 */
class TypeData
{
	private final String	    mJ2EEType;
	private final Set<String>	mLegalParentsTypes;
	private final String	    mContainedByJ2EEType;
	
 	
 	    public int
 	hashCode()
 	{
 	    return ObjectUtil.hashCode(
 	            mJ2EEType, mLegalParentsTypes, mContainedByJ2EEType);
 	}
 	
 	    public boolean
 	equals( final Object rhs )
 	{
 	    boolean equals  = false;
 	    
 	    if ( this == rhs )
 	    {
 	        equals  = true;
 	    }
 	    else if ( ! (rhs instanceof TypeData) )
 	    {
 	        equals  = false;
 	    }
 	    else
 	    {
 	        final TypeData rhsTypeData  = (TypeData)rhs;
 	        
 	        equals  =  ObjectUtil.equals( mJ2EEType, rhsTypeData.mJ2EEType ) &&
 	                    mLegalParentsTypes.equals( rhsTypeData.mLegalParentsTypes ) &&
 	                    ObjectUtil.equals( mContainedByJ2EEType, rhsTypeData.mContainedByJ2EEType );
 	    }
 	    
 	    return equals;
 	}
 	
	/**
		Same as TypeData( j2eeType, newSet( parentJ2EEType ) )
	 */
	protected TypeData(
		final String	j2eeType,
		final String	parentJ2EEType )
	{
		this( j2eeType, Collections.singleton( parentJ2EEType ) );
	}
	
	/**
		@param j2eeType	the j2eeType
		@param legalParentJ2EETypes	the possible j2eeTypes of the parent (0 or more)
	 */
	protected TypeData(
		final String	j2eeType,
		final Set<String>		legalParentJ2EETypes )
	{
		this( j2eeType, legalParentJ2EETypes, null );
	}
	
	/**
		@param j2eeType	the j2eeType
		@param legalParentJ2EETypes	the possible j2eeTypes of the parent (0 or more)
		@param containingJ2EEType if non-null, the containing type, legalParentJ2EETypes must be null
	 */
	protected TypeData(
		final String	j2eeType,
		final Set<String>		legalParentJ2EETypes,
		final String	containedByJ2EEType )
	{
		if ( containedByJ2EEType != null && legalParentJ2EETypes != null )
		{
			throw new IllegalArgumentException( "can't have both parents and contained type" );
		}
		
		mJ2EEType				= j2eeType;
		if ( containedByJ2EEType != null )
		{
			mContainedByJ2EEType	= containedByJ2EEType;
			mLegalParentsTypes		= null;
		}
		else
		{
			mContainedByJ2EEType	= null;
			mLegalParentsTypes		= legalParentJ2EETypes == null ?
					null : Collections.unmodifiableSet( legalParentJ2EETypes );
		}
	}
	
		public final Set<String>
	getLegalParentJ2EETypes()
	{
		return( mLegalParentsTypes );
	}
	
		public final String
	getJ2EEType()
	{
		return( mJ2EEType );
	}
	
		public boolean
	isSubType()
	{
		return( mLegalParentsTypes != null ); 
	}
	
		public final String
	getContaineeByJ2EEType()
	{
		return( mContainedByJ2EEType );
	}
}

