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


import javax.management.ObjectName;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
	While a dotted name can refer to only 1 ObjectName, an ObjectName could
	be aliased to multiple dotted names.
	
	This implementation supports a 1 to 1 mapping each way; it cannot be used
	to map multiple dotted names to the same ObjectName.
 */
public final class DottedNameRegistry1To1Impl implements DottedNameRegistry
{
	final Map<String,ObjectName>		mDottedNameStringsToObjectNames;
	final Map<ObjectName,String>		mObjectNamesToDottedNameStrings;
	
	private final static int	INITIAL_CAPACITY	= 300;
	
		public
	DottedNameRegistry1To1Impl( )
	{
		/*
		 	Keep mappings both ways to allow efficient removal
		 	based on either.
		*/
		mDottedNameStringsToObjectNames		= new HashMap<String,ObjectName>( INITIAL_CAPACITY );
		mObjectNamesToDottedNameStrings		= new HashMap<ObjectName,String>( INITIAL_CAPACITY );
	}
	
		public synchronized ObjectName
	dottedNameToObjectName( String dottedName )
	{
		return( mDottedNameStringsToObjectNames.get( dottedName ) );
	}
	
		public synchronized String
	objectNameToDottedName( ObjectName objectName )
	{
		return( mObjectNamesToDottedNameStrings.get( objectName ) );
	}
	
		private <T> Set<T>
	copySet( Set<T> input )
	{
		final Set<T>	newSet	= new HashSet<T>();
		
		newSet.addAll( input );
		
		return( newSet );
	}
	
		public synchronized Set<String>
	allDottedNameStrings(  )
	{
		return( copySet( mDottedNameStringsToObjectNames.keySet() ) );
	}
	
		public synchronized Set
	allObjectNames(  )
	{
		return( copySet( mObjectNamesToDottedNameStrings.keySet() ) );
	}

		public synchronized void
	add( final String dottedName, final ObjectName objectName ) 
	{
        new DottedName( dottedName );   // validate
        
		/*
			Don't allow more than one dotted name mapping for an ObjectName.
			
			The check here must be via the ObjectName; checking the dottedName
			will do no good as this could be a new dottedName for the same
			ObjectName.
		 */
		if ( objectNameToDottedName( objectName ) != null )
		{
			remove( objectName );
		}

		mDottedNameStringsToObjectNames.put( dottedName, objectName );
		mObjectNamesToDottedNameStrings.put( objectName, dottedName );
	}
	
		synchronized void
	remove( final String dottedName, final ObjectName objectName )
	{
		if ( dottedName != null && objectName != null )
		{
			mDottedNameStringsToObjectNames.remove( dottedName );
			mObjectNamesToDottedNameStrings.remove( objectName );
		}
	}
	
		public void
	remove( final String dottedName )
	{
		remove( dottedName, dottedNameToObjectName( dottedName ) );
	}
	
		public void
	remove( final ObjectName objectName )
	{
		remove( objectNameToDottedName( objectName ), objectName );
	}
}



