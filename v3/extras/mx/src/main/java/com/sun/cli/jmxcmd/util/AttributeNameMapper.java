/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */
package com.sun.cli.jmxcmd.util;


import java.util.Set;

/**
	Maps names to a derived name.  An example use for this might be to map a JMX
	Attribute "foo-bar" to a suiteable "FooBar" form so that it can be used
	as a Java identifier.
 */
public interface AttributeNameMapper
{
	/**
		Determine if the attribute name requires mapping.
		
		@param originalName	the original name from which a derived name is made
	 */
	//public boolean		requiresMapping( String originalName );
	
	/**
		@param originalName the original/source/real name of the Attribute
		@param derivedName the name by which it should be known
	 */
	public void			addMapping( final String originalName, final String derivedName );
	
	/**
		Don't perform any mapping on this name.  Equivalent to calling
		addMapping( originalName, originalName )
		
		@param originalName the "real" name of the Attribute
	 */
	public void			dontMap( final String originalName );
	
	/**
		Setup mapping for all specified Attribute names.  These add to (or replace)
		any existing mappings.
		
		@param originalNames	all names from which should be derived names
	 */
	public void			deriveAll( final String[] originalNames );
	
	/**
	    Attempt to match the derived name to one of the candidates.
	    This facility is used when different runtime conditions
	    present different original names which must be mapped to the
	    same derived name.
	    <p>
	    If a name is matched it is added as a mapping and the
	    original name is returned.
	 */
	    public String
	matchName(
	    final String   derivedName,
	    final String[] candidates );
	    
	/**
		Maps Attribute names to another name.
		<p>
		A common use is to construct legal Java identifiers, so that they can
		be exposed in an MBean proxy with get/set routines.
		<p>
		For example "classpath-prefix" is not legal in a Java API; it could not
		generate the methods getclasspath-prefix() and setclasspath-prefix().
		<p>
		Any legal mapping is OK.  Suggested  possible mappings include:
		ClasspathPrefix, classpathPrefix, classpath_prefix, etc. These would
		result in the method names: getClasspathPrefix(), getclasspathPrefix(),
		getclasspath_prefix(), etc.
		
		@param originalName	 original name
	 */
	public String		originalToDerived( String originalName );
	
	/**
		Reverse the effect of originalToDerived()
		
		@param derivedName	name derived from the original one
	 */
	public String		derivedToOriginal( String derivedName );
	
	
	/**
		Get the entire set of Attribute names, consisting of the names
		that were derived, and the names that do not require mapping. 
	 */
	public Set<String>		getAttributeNames( );
}
