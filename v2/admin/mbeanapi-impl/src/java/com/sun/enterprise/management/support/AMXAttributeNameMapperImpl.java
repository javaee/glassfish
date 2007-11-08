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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Collections;

import com.sun.appserv.management.util.misc.Output;
import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.misc.StringUtil;
import com.sun.appserv.management.util.misc.MapUtil;
import com.sun.appserv.management.util.misc.CollectionUtil;

/**
	Maps Attribute names to legal Java identifiers, so that they can
	be exposed in a proxy with get/set routines
 */
public class AMXAttributeNameMapperImpl implements AMXAttributeNameMapper
{
	final Map<String,String>	mDerivedToOriginal;
	final Map<String,String>	mOriginalToDerived;
	final Set<String>			mDerivedNames;
	
	final Map<String,String>    mUnmappedOriginals;
	
	private Output                mDebug;
	
	/**
		Create a new instance which will map nothing by default.
	 */
		public
	AMXAttributeNameMapperImpl()
	{
		mDebug  = null;
		mDerivedToOriginal	= new HashMap<String,String>();
		mOriginalToDerived	= new HashMap<String,String>();
		mDerivedNames		= new HashSet<String>();
		
		mUnmappedOriginals  = new HashMap<String,String>();
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
	
	private static final char DASH_CHAR    = '-';
	private static final String DASH    = "" + DASH_CHAR;
	
	    private String
	stripDashes( final String name )
	{
	    String  result  = name;
	    
	    if ( name.indexOf( DASH ) >= 0 )
	    {
    	    final StringBuilder builder = new StringBuilder();
    	    final int length    = name.length();
    	    
    	    boolean upperNext   = false;
    	    for( int i = 0; i < length; ++i )
    	    {
    	        final char c    = name.charAt( i );
    	        
    	        if ( c == DASH_CHAR )
    	        {
    	            upperNext   = true;
    	        }
    	        else
    	        {
    	            if ( upperNext )
    	            {
    	                builder.append( ("" + c ).toUpperCase() );
    	                upperNext   = false;
    	            }
    	            else
    	            {
    	                builder.append( c );
    	            }
    	        }
    	    }
    	    
    	    result  = builder.toString();
	    }
	    
	    return result;
	}
	
	    private String
	normalize( final String s )
	{
	    return stripDashes( s ).toLowerCase();
	}
	
	
	    private Map<String,String>
	normalize( final Set<String> s )
	{    	    
	    final Map<String,String>   result  = new HashMap<String,String>();
	    
	    for( final String name : s )
	    {
	        result.put( normalize( name ), name );
	    }

	    return result;
	}
	
	
	/**
	    Attempt to match the derived name to one of the candidates.
	    This facility is used when different runtime conditions
	    present different original names which must be mapped to the
	    same derived name.
	    <p>
	    If a name is matched it is added as a mapping and the
	    original name is returned, otherwise an exception is thrown.
	 */
	    public String
	matchName(
	    final String   derivedName,
	    final String[] candidates )
	{
	    String  result  = null;
	    
	    final String    existingOriginal    = derivedToOriginal( derivedName );
	    if ( existingOriginal != null )
	    {
	        throw new IllegalArgumentException( "Already mapped: " +
	            derivedName + " => " + existingOriginal );
	    }
	    if ( mUnmappedOriginals.keySet().size() == 0 )
	    {
	        throw new IllegalArgumentException(
	            "There are no unmapped originals, existing mappings: " +
	            MapUtil.toString( mOriginalToDerived, ", " ) );
	    }
	    
	    for( final String candidate : candidates )
	    {
	        final String candidateNormalized    = normalize( candidate );
	        
	        if ( mUnmappedOriginals.keySet().contains( candidateNormalized ) )
	        {
	            result  = mUnmappedOriginals.get( candidateNormalized );
	            mUnmappedOriginals.remove( candidateNormalized );
	            addMapping( result, derivedName );
	            break;
	        }
	    }
	    
	    if ( result == null )
	    {
	        final String msg = derivedName + " can't be matched to " + 
	            StringUtil.toString( ", " , (Object[])candidates ) +
	            ", unmapped originals: " +
	                CollectionUtil.toString( mUnmappedOriginals.values(), ", ") +
                    ",  mapped originals: {" + MapUtil.toString( mDerivedToOriginal, ", ") + "}";
	        throw new IllegalArgumentException( msg );
	    }
	    
	    return result;
	}
	
	    public String
	matchName(
	    final String   derivedName,
	    final String   candidate1,
	    final String   candidate2 )
	{
	    return matchName( derivedName, new String[] { candidate1, candidate2 } );
	}
	
	    public String
	matchName(
	    final String   derivedName,
	    final String   candidate1 )
	{
	    return matchName( derivedName, new String[] { candidate1 } );
	}
	
	
	/**
	    Attempt to match an original name with a derived one by performing
	    a case-insensitive comparison.  It is OK if there are more derived
	    names than originals, or if there are more original names than
	    derived names.
	    @param derivedNames
	    @param originalNames
	 */
		public void
	matchNames(
	    final String[]  derivedNames,
	    final String[]  originalNames )
	{
	    final Set<String>  originals  = GSetUtil.newSet( originalNames );
	    final Set<String>  deriveds   = GSetUtil.newSet( derivedNames );
	    
	    // first, eliminate identical names
	    for( final String original : originalNames )
	    {
	        if ( deriveds.contains( original ) )
	        {
    	        //System.out.println( "IDENTITY: " + original );
	            addMapping( original, original );
	            originals.remove( original );
	            deriveds.remove( original );
	        }
	    }
	    
	    // now we have remaining only those names that differ.
	    if ( originals.size() != 0 )
	    {
    	    final Map<String,String>   originalsMap = normalize( originals );
    	    final Map<String,String>   derivedsMap   = normalize( deriveds );
    	    
    	    for( final String originalLowerCase : originalsMap.keySet() )
    	    {
    	        final String    original    = originalsMap.get( originalLowerCase );
    	        final String    derived     = derivedsMap.get( originalLowerCase );
    	        if ( derived != null )
    	        {
    	            //System.out.println( "MATCHED: " + original + " => " + derived );
    	            addMapping( original, derived );
    	        }
    	        else
    	        {
    	           // System.out.println( "NO MATCH: " + original  );
    	            mUnmappedOriginals.put( normalize( original ), original );
    	        }
    	    }
	    }
	}
	
	    public Set<String>
	getUnmappedOriginals()
	{
	    Set<String>   unmapped  = Collections.emptySet();
	    
	    if ( mUnmappedOriginals.keySet().size() != 0 )
	    {
    	    unmapped    = new HashSet<String>( mUnmappedOriginals.values());
	    }
	    
	    return unmapped;
	}
	
		public void
	addMapping(
		final String originalName,
		final String derivedName )
	{
		mDerivedToOriginal.put( derivedName, originalName );
		mOriginalToDerived.put( originalName, derivedName );
		
		if ( mUnmappedOriginals.keySet().contains( originalName ) )
		{
		    mUnmappedOriginals.remove( originalName );
		}
		
		mDerivedNames.add( derivedName );
	}
	
		public void
	dontMap( final String originalName )
	{
		addMapping( originalName, originalName );
	}
	
		public String
	originalToDerived( final String originalName )
	{
		final String	derivedName	= mOriginalToDerived.get( originalName );
		return( derivedName );
	}
	
		public String
	derivedToOriginal( final String derivedName )
	{
		final String	original = mDerivedToOriginal.get( derivedName );
		return( original );
	}
	
		public Set<String>
	getDerivedNames( )
	{
		return( mDerivedNames );
	}
	
		public String
	toString()
	{
		final StringBuilder	builder	= new StringBuilder();
		
		final String LINE_SEPARATOR = System.getProperty( "line.separator" );
		for( final String name : getDerivedNames() )
		{
			builder.append( name );
			builder.append( " => " );
			builder.append( derivedToOriginal( name )  );
			builder.append( LINE_SEPARATOR );
		}
		
		return( builder.toString() );
	}
}











