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

import java.util.Map;
import java.util.Set;

/**
	All MBeans which <i>logically contain</i> other MBeans implement this interface;
	such an MBean is considered a Container, and the contained MBean is considered
	a Containee.  The interface indicates the <i>potential</i> to contain other
	MBeans; at any given time MBeans may or may not be contained.
 */
public interface Container extends AMX
{
	/**
		Attribute returned by getContaineeJ2EETypes().
        @see #getContaineeJ2EETypes
	 */
	public static final String	ATTR_CONTAINEE_J2EE_TYPES	= "ContaineeJ2EETypes";
    
	/**
		Attribute returned by getContaineeSet().
	 */
	public static final String	ATTR_CONTAINEE_OBJECT_NAME_SET	= "ContaineeObjectNameSet";
	
	/**
		@return Set of String of all <i>possible</i> j2eeTypes contained within this item
		@see com.sun.appserv.management.base.Util#getNamesSet
	 */
	public Set<String>		getContaineeJ2EETypes();
	
	/**
		Return a Map keyed by j2eeType. The <i>value</i> corresponding to each
		key (j2eeType) is another Map, as returned from {@link #getContaineeMap}.
		<p>
		If the passed Set is null, then all types are obtained.  Pass
		the set returned from {@link #getContaineeJ2EETypes} to get all currently
		present containees.
		
		@param j2eeTypes	the j2eeTypes to look for, or null for all
		@return Map (possibly empty) of <i>j2eeType</i>=&lt;Map of <i>name</i>=<i>AMX</i>&gt;
	 */
	public <T extends AMX> Map<String,Map<String,T>>		getMultiContaineeMap( final Set<String> j2eeTypes );
	
	/**
		Each key in the resulting Map is a String which is the value of the 
		AMX.NAME_KEY for that {@link AMX}, which is the value.
		
		@param j2eeType	the j2eeType to look for
		@return Map <i>name</i>=<i>AMX</i>
	 */
	public <T extends AMX> Map<String,T>		getContaineeMap( final String j2eeType );
	
	/**
		Obtain the singleton MBean having the specified type.
		
		@param j2eeType
		@return {@link AMX} of specified j2eeType or null if not present
		@throws IllegalArgumentException if there is more than one item of this type
		@see Util#getNamesSet
	 */
	public <T extends AMX> T	getContainee( final String j2eeType );
	
	/**
		@return all containees having the specified j2eeType.
		@see Util#getNamesSet
	 */
	public <T extends AMX> Set<T> 	getContaineeSet( final String j2eeType );
	
	/**
		Same as getContaineeSet( getContaineeJ2EETypes() )
		@return all containees of any j2eeType
		@see Util#getNamesSet
		@see #getContaineeSet(java.util.Set)
	 */
	public <T extends AMX> Set<T> 	getContaineeSet( );
	
	/**
		@return all containees having the specified j2eeType(s).
		@see Util#getNamesSet
		@see #getMultiContaineeMap
	 */
	public <T extends AMX> Set<T> 	getContaineeSet( final Set<String> j2eeTypes );
	
	
	/**
		@return all containees having one of the specified types
		and the specified name.  If all types are desired, pass the result of
        getContaineeJ2EETypes() for 'j2eeTypes'.
		@see com.sun.appserv.management.base.Util#getNamesSet
	 */
	public <T extends AMX> Set<T> 	getByNameContaineeSet(
	                                final Set<String> j2eeTypes, final String name );
	
	/**
		Get a singleton containee having the specified j2eeType and name. 
        <p>
        If 'j2eeType' is null, uniqueness is assumed and either null or a
        Containee with the specified name will be returned.
        An IllegalArgumentException will be thrown if there is more than one Containee with the
        specified name.
		@param j2eeType	the j2eeType of the contained 
		@param name		the name of the contained  (as found in "name" property)
		@return AMX or null if not found
		@see Util#getNamesSet
	*/
	public <T extends AMX> T	getContainee( final String j2eeType, final String name );
}



