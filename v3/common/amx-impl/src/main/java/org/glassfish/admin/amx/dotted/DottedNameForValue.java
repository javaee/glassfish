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

import org.glassfish.admin.amx.dotted.valueaccessor.PrefixedValueSupport;

import java.util.List;

/* 
	A class which interprets a DottedName as containing a value name.
 */
public final class DottedNameForValue
{
	protected final DottedName	mDottedName;
	protected DottedName		mPrefix;
	protected String			mValueName;
	
		public
	DottedNameForValue( final DottedName	dottedName )
	{
		mDottedName	= dottedName;
		mValueName	= null;
		mPrefix		= init();
	}
	
	/**
		Examine the name and extract the name of the value, which could be either
		a plain name or a prefixed name.
	 */
		private DottedName
	init()
	{
		final List	parts		= mDottedName.getParts();
		final int	numParts	= parts.size();
		
		// the scope is first, then
		// there must be at least 1 part for there to be a value
		// example:  "domain.locale"
		if ( numParts == 0 )
		{
			final String	msg	= DottedNameStrings.getString(
					DottedNameStrings.NO_VALUE_NAME_SPECIFIED_KEY );
			throw new IllegalArgumentException( msg + " = " + mDottedName );
		}
		
		// the last part is the value name
		final String	lastPart	= (String)parts.get( numParts - 1 );
		int				numPrefixParts	= 0;
		
		// If the part preceeding the value name is prefix
		// then 
        PrefixedValueSupport prop_support = new PrefixedValueSupport(null);
        final String dottedName = DottedName.toString(mDottedName.getDomain(),
                                                mDottedName.getScope(),
                                                mDottedName.getParts(), false);
        final boolean  isPrefixed = prop_support.isPrefixedValueDottedName(dottedName);
		if ( isPrefixed )
		{
			mValueName		= prop_support.getPrefixedValueName(dottedName, true);
			numPrefixParts	= numParts - 2;
		}
		else
		{
			// it's a regular value name; does not include prefix
			mValueName		= lastPart;
			numPrefixParts	= numParts - 1;
		}
		
		return( DottedNameFactory.getInstance().get( DottedName.toString( mDottedName, numPrefixParts ) ) );
	}
		
		public DottedName
	getPrefix()
	{
		return( mPrefix );
	}
	
                public String
	getValueName()
	{
		assert( mValueName != null );
		return( mValueName );
	}
                
    public String toString() {
        return mDottedName.toString();
    }
}


