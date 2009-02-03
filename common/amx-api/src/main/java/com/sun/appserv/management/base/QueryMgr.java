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
package com.sun.appserv.management.base;

import javax.management.ObjectName;
import java.util.Set;

/**
	Supports various types of queries to find s based on various Attributes.
 */
public interface QueryMgr extends AMX, Utility, Singleton
{
/** The j2eeType as returned by {@link com.sun.appserv.management.base.AMX#getJ2EEType}. */
	public static final String	J2EE_TYPE			= XTypes.QUERY_MGR;


	/**
		Calls getJ2EETypeSet( j2eeTypeValue ) and extracts the single result.
		Throws an exception if more than one object is found.
		
		@param j2eeTypeValue  the value for the j2eeType property
		@return ObjectName or null if not found
		@throws exception if not found
	 */
	public <T extends AMX> T	querySingletonJ2EEType( String j2eeTypeValue );
	
	
	
	
	/**
		Return all {@link AMX} whose j2eeType is equal to any specified in
		'j2eeTypes'. 
		
		@param j2eeTypes Set of String (j2eeType values).
		@return Set of AMX or empty Set if none
	 */
	public <T extends AMX> Set<T>	queryJ2EETypesSet( Set<String> j2eeTypes );
	
	/**
		@see #queryJ2EETypesSet
	 */
	public Set<ObjectName>	queryJ2EETypesObjectNameSet( Set<String> j2eeTypes );
	
	
	/**
		Return all {@link AMX} whose j2eeType is equal to 'j2eeTypeValue.
		Legal values include those defined in
		{@link com.sun.appserv.management.j2ee.J2EETypes} and those
		defined in  {@link com.sun.appserv.management.base.XTypes}
		
		@param j2eeTypeValue  the value for the j2eeType property
		@return Set of AMX or empty Set if none
	 */
	public <T extends AMX> Set<T>	queryJ2EETypeSet( String j2eeTypeValue );
	
	/**
		@return Set of ObjectName
	 */
	public Set<ObjectName>	queryJ2EETypeObjectNameSet( String j2eeTypeValue );
	

	/**
		Return all {@link AMX} whose name is equal to 'nameValue'.
		
		@param nameValue  the value for the j2eeType property
		@return Set of AMX or empty Set if none
	 */
	public <T extends AMX> Set<T>	queryJ2EENameSet( String nameValue );
	
	/**
		@return Set of ObjectName
	 */
	public Set<ObjectName>	queryJ2EENameObjectNameSet( String nameValue );
	
	/**
		Calls queryJ2EETypeSet( j2eeTypeValue ), then creates an array consisting
		of the names of each of the resulting objects.  Note that some names could
		be identical.
		
		@param j2eeType  the value for the j2eeType property
		@return array of names
	 */
	public String[]	queryJ2EETypeNames( String j2eeType );
	
	
	/**
	    @return Set<AMX> containing all items that have the matching j2eeType and name
	 */
	public <T extends AMX> Set<T>	queryJ2EETypeNameSet( String j2eeType, String name );
	
	/**
	    @return Set<ObjectName> containing all items that have the matching j2eeType and name
	 */
	public Set<ObjectName>	queryJ2EETypeNameObjectNameSet( String j2eeType, String name );
	

	/**
		Return all AMX whose ObjectName matches the supplied
		ObjectName pattern, as defined by the JMX specification.
		<p>
		This can be a relatively expensive operation if care is not taken to use
		a suitably constrained pattern. For example, querying for "*:*" will return
		every  available AMX.
		
		@param pattern  an ObjectName containing a pattern as defined by JMX
		@return Set of AMX or empty Set if none
	 */
	public <T extends AMX> Set<T>	queryPatternSet( ObjectName	pattern );
	
	/**
		@return Set of ObjectName
	 */
	public Set<ObjectName>	queryPatternObjectNameSet( ObjectName	pattern );
	
	
	/**
		Makes an ObjectName pattern, then calls queryPatternSet( pat )
		
		@param domain  the domain or "*" for all
		@param props a comma-separated Properties string
		@return Set of AMX or empty Set if none
	 */
	public <T extends AMX> Set<T>	queryPatternSet( String domain, String props );
	
	/**
		@return Set of ObjectName
	 */
	public Set<ObjectName>	queryPatternObjectNameSet( String domain, String props );
	
	/**
		Return all objects that match the specified properties in the JMX domain
		governed by this QueryMgr.
		
		@param props a String containing one or more name/value properties
	 */
	public <T extends AMX> Set<T>	queryPropsSet( String props );
	
	/**
		@return Set of ObjectName
	 */
	public Set<ObjectName>	queryPropsObjectNameSet( String props );
	
	
	/**
		Return all {@link AMX} whose whose ObjectName matches all property
		expressions.  Each property expression consists of a key expression, and a value
		expression; an expression which is null is considered a "*" (match all).
		<p>
		Both key and value expressions may be wildcarded with the
		"*" character, which matches 0 or more characters. 
		<p>
		Each property expression is matched in turn against the ObjectName. If a match
		fails, the ObjectName is not included in the result.  If all matches succeed, then
		the ObjectName is included.
		<p>
		Caution should be used in choosing the element type of the returned set. Unless
		certain that a uniform type will be produced, Set&lt;AMX> is usually the most
		appropriate type eg:
		<code>Set&lt;AMX>  result  = queryWildSet(...);</code>
		
		@param wildKeys	one or more name expressions, null means all
		@param wildValues	one or more value expressions, null means all
		@return Set of AMX or empty Set if none
	 */
	public <T extends AMX> Set<T>	queryWildSet(  String[]	wildKeys, String[] wildValues );
	
	/**
		@return Set of ObjectName
	 */
	public Set<ObjectName>	queryWildObjectNameSet(  String[]	wildKeys, String[] wildValues );
	
	
	/**
		Return all {@link AMX} that implement the specified interface,
		which may be any interface. This is the same as querying for all s
		for their  interfaces, then returning the set for which the 
		interface extends the specified interface.
		
		@param interfaceName  classname of the desired interface
		@param candidateObjectNames	optional (may be null) Set of ObjectName to which the search is limited
		@return Set of AMX or empty Set if none
	 */
	public <T extends AMX> Set<T>
	queryInterfaceSet( String interfaceName, Set<ObjectName> candidateObjectNames);
					
	/**
		@return Set of ObjectName
	 */
	public Set<ObjectName>
	    queryInterfaceObjectNameSet( String	interfaceName, 
	        Set<ObjectName> candidateObjectNames);
	
	/**
		@return Set of all AMX
		@see #queryAllObjectNameSet
	 */
	public Set<AMX> queryAllSet( );
	
	/**
		@return Set of ObjectName of all AMX
		@see #queryAllSet
	 */
	public Set<ObjectName> queryAllObjectNameSet( );
}




