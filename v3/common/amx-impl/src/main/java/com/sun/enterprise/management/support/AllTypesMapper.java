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

import com.sun.appserv.management.base.AMX;

/**
	Map all types (XTypes and J2EETypes) to their respective interfaces.
	
	@see XTypesMapper
	@see com.sun.appserv.management.j2ee.J2EETypesMapper
 */
public final class AllTypesMapper extends TypesMapper
{
	private static AllTypesMapper	INSTANCE	= null;
	
	
	/**
		Get the singleton instance.
	 */
		public static synchronized AllTypesMapper
	getInstance()
	{
		if ( INSTANCE == null )
		{
			INSTANCE	= new AllTypesMapper();
		}
		
		return( INSTANCE );
	}

		private
	AllTypesMapper()
	{
		super( new Class[ 0 ] );
	}
	
	
	/**
		@param type the j2eeType
		@return the interface class associated with a given j2eeType.
	 */
		public Class<?>
	getInterfaceForType( final String type )
	{
		Class<?> theClass	= XTypesMapper.getInstance().getInterfaceForType( type );
		 
		if ( theClass == null )
		{
			theClass	= J2EETypesMapper.getInstance().getInterfaceForType( type );
		}

		if ( theClass == null )
		{
			throw new IllegalArgumentException( "Can't find interface for: " + type );
		}
		
		assert( AMX.class.isAssignableFrom( theClass ) ):
			"WARNING: mbean does not implement AMX: " + theClass.getName();

		return( theClass );
	}
	
	
}
