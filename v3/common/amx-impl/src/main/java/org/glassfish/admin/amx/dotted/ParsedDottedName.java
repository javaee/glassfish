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

import java.util.Collections;
import java.util.List;

import static org.glassfish.admin.amx.dotted.DottedNameSpecialChars.*;
/*
 */
final class ParsedDottedName implements java.io.Serializable
{
	public final String         mDomain;
	public final String         mScope;
	public final List<String>	mParts;
	
		public
	ParsedDottedName(
        final String domain,
        final String scope,
        final List<String> parts )
	{
		if ( domain.length() != 0 )
		{
			checkLegalNamePart( domain );
		}

		checkLegalNamePart( scope );
	
		for( int i = 0; i < parts.size(); ++i )
		{
			checkLegalNamePart( (String)parts.get( i ) );
		}
		
		mDomain		= domain;
		mScope		= scope;
		mParts		= Collections.unmodifiableList( parts );
	}
	
		boolean
	isLegalChar( final char theChar )
	{
		return( LEGAL_CHARS.indexOf( theChar ) >= 0 );
	}
	
		void
	checkLegalChar( final char theChar )
	{
		if ( ! isLegalChar( theChar ) )
		{
			final String	msg	= DottedNameStrings.getString(
					DottedNameStrings.ILLEGAL_CHARACTER_KEY,
					"'" + theChar + "'" );
					
			throw new IllegalArgumentException( msg );
		}
	}
	
		void
	checkLegalNamePart( String part )
	{
		final int length	= part.length();
		
		if ( length == 0 )
		{
			final String	msg	= DottedNameStrings.getString(
					DottedNameStrings.MISSING_EXPECTED_NAME_PART_KEY );
					
			throw new IllegalArgumentException( msg );
		}
	
		for( int i = 0; i < length; ++i )
		{
			final char	theChar	= part.charAt( i );
			
			checkLegalChar( theChar );
		}
	}
	
		public int
	hashCode()
	{
        // trying to come up with a good hash code
        // see Effective Java, pp 36-41

        int hashcode = 17;
        hashcode = 37 * hashcode + mDomain.hashCode();
        hashcode = 37 * hashcode + mScope.hashCode();
        hashcode = 37 * hashcode + mParts.hashCode();
        
        return hashcode;
        
    }
        
		public boolean
	equals( Object other )
	{
		boolean	equals	= false;
		
		if ( ! (other instanceof ParsedDottedName) )
		{
			equals	= false;
		}
		else if ( other == this )
		{
			equals	= true;
		}
		else
		{
			equals	= this.toString().equals( other.toString() );
		}
		
		return( equals );
	}
	
		public String
	toString()
	{
		return( DottedName.toString( mDomain, mScope, mParts ) );
	}
}






