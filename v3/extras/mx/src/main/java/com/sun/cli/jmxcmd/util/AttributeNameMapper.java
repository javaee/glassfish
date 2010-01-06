/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Sun Microsystems, Inc. All rights reserved.
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
