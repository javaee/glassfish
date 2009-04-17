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
package org.glassfish.admin.amx.impl.path;

import static org.glassfish.admin.amx.impl.path.DottedNameSpecialChars.*;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

/*
 */
final class ParsedV3Pathname
{
    private static void cdebug( final String s ) { System.out.println(s); }
    
    private final boolean           mIsFullPath;
    
    /** */
	private final List<PathPart>	mParts;
    
    /**  */
    private final AttrPart          mAttr;
	
    public ParsedV3Pathname( final boolean fullPath, final List<PathPart> parts)
	{
        this( fullPath, parts, null );
    }
    
		public
	ParsedV3Pathname(
        final boolean       fullPath,
        final List<PathPart> parts,
        final AttrPart       attrPart )
	{
		for( final PathPart part : parts )
		{
    cdebug( "checkLegalNamePart: " + part.getType() );
			checkLegalNamePart( part.getType() );
            if ( part.getName() != null )
            {
    cdebug( "checkLegalNamePart: " + part.getName() );
                checkLegalNamePart( part.getName() );
            }
		}        
		
		mParts		  = parts;
        mAttr         = attrPart;
        mIsFullPath   = fullPath;
	}
    
    /**
        This is *not* a parse, it's a crude proof of concept.
    */
    	static ParsedV3Pathname
	parse( final String sourceString )
	{
        int idx = sourceString.indexOf(ATTRIBUTE_CHAR);
        if ( idx == 0 ) { 
            throw new IllegalArgumentException(sourceString);
        }
        String attr = null;
        String path = null;
        if ( idx < 0 ) {
            path = sourceString;
        }
        else {
            path = sourceString.substring(0,idx);
            attr = sourceString.substring( idx + 1, sourceString.length() );
        }
        if ( path.length() == 0 ) {
            throw new IllegalArgumentException(sourceString);
        }
        
        final boolean isFullPath = path.indexOf(SEPARATOR_CHAR) == 0;
        if ( isFullPath ) {
            path = path.substring(1, path.length());
        }
cdebug( "fullpath = " + isFullPath + ", path = " + path + ", attr = " + attr );
        
        final List<PathPart> parts = new ArrayList<PathPart>();
        final String[] splits = path.split( "/" );
        for( final String s : splits ) {
            parts.add( new PathPart(s) );
            cdebug( "part: /" + s );
        }
        
        final AttrPart attrPart = attr == null ? null : new AttrPart(attr);
cdebug( "attrPart = " + attrPart );
        final ParsedV3Pathname  parsed = new ParsedV3Pathname( isFullPath, parts, attrPart );
        
cdebug( "ParsedV3Pathname = " + parsed );
        return parsed;
	}
	
    public List<PathPart> getParts() { return mParts; }
    public AttrPart  getAttrPart()      { return mAttr; }
    public boolean  isFullPath()      { return mIsFullPath; }
    
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
        hashcode = 37 * hashcode + mParts.hashCode();
        if ( mAttr != null ) {
            hashcode ^= mAttr.hashCode();
        }
        
        return hashcode;
        
    }
        
		public boolean
	equals( Object other )
	{
		boolean	equals	= false;
		
		if ( ! (other instanceof ParsedV3Pathname) )
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
        final StringBuffer buf = new StringBuffer();
        
        // part names should be escaped!
        
        for( final PathPart part : mParts ) {
            buf.append(SEPARATOR_CHAR);
            buf.append( part.toString() );
        }
        
        if ( mAttr != null ) {
            buf.append(ATTRIBUTE_CHAR);
            buf.append(mAttr.toString());
        }
        
		return buf.toString();
	}
}






