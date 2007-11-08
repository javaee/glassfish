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
package com.sun.enterprise.management.ext.offline;

import java.util.Set;
import java.util.HashSet;

import javax.management.ObjectName;

import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.util.misc.GSetUtil;

import com.sun.enterprise.management.AMXTestBase;
import com.sun.enterprise.management.offline.OfflineDottedNamesRegistry;


/**
 */
public final class OfflineDottedNamesRegistryTest extends junit.framework.TestCase
{
        public
    OfflineDottedNamesRegistryTest()
    {
    }

        private OfflineDottedNamesRegistry
    create()
    {
        return new OfflineDottedNamesRegistry();
    }
    
    
        private ObjectName
    createObjectName( final String j2eeType, final String name)
    {
        return Util.newObjectName( "amx", Util.makeRequiredProps( j2eeType, name ) );
    }
    
		public void
	testCreate()
	{
	    create();
	}
	
	    private Set<String>
	dummySet()
	{
	    return GSetUtil.newStringSet( "hello", "there" );
	}
	
	
		public void
	testIllegalAdd()
	{
	    final OfflineDottedNamesRegistry    reg = create();
	    
	    try
	    {
	        reg.addMapping( null, "foo", dummySet() );
	        assert false : "Expected exception adding null ObjectName";
	    }
	    catch ( Exception e )
	    {
	    }
	    
	    try
	    {
	        reg.addMapping( createObjectName( "XXX", "ZZZ" ), null, dummySet() );
	        assert false : "Expected exception adding null prefix";
	    }
	    catch ( Exception e )
	    {
	    }
	    
	    try
	    {
	        reg.addMapping( createObjectName( "XXX", "ZZZ" ), "foo", null );
	        assert false : "Expected exception adding null attributes";
	    }
	    catch ( Exception e )
	    {
	    }
	    
	    try
	    {
	        reg.addMapping( createObjectName( "XXX", "ZZZ" ), "foo", dummySet() );
	        // try to add again
	        reg.addMapping( createObjectName( "XXX", "ZZZ" ), "foo", dummySet() );
	        assert false : "Expected exception adding same ObjectName";
	    }
	    catch ( Exception e )
	    {
	    }
	}
	
	
		public void
	testAddRemove()
	{
	    final OfflineDottedNamesRegistry    reg = create();
	    
	    final int ITER  = 1000;
	    
	    final Set<String>   attrs   = dummySet();
	    final ObjectName[]  objectNames = new ObjectName[ ITER ];
	    final String[]      prefixes = new String[ ITER ];
	    
	    for( int i = 0; i < ITER; ++i )
	    {
	        final String prefix = "foo" + i;
	        final ObjectName objectName = createObjectName( "test" + i, "" + i );

	        objectNames[ i ]    = objectName;
	        prefixes[ i ]       = prefix;
	        
	        reg.addMapping( objectName, prefix, attrs );
    	    assert( reg.getPrefix( objectName ) ==  prefix );
    	    assert( reg.getObjectName( prefix ) == objectName );
    	    assert( reg.getLegalAttributes( objectName ).equals( attrs ) );
	    }
	    
	    for( int i = 0; i < ITER; ++i )
	    {
	        final ObjectName    objectName  = objectNames[ i ];
	        final String        prefix  = prefixes[ i ];
	        
    	    assert( reg.getPrefix( objectName ) ==  prefix );
    	    assert( reg.getObjectName( prefix ) == objectName );
    	    assert( reg.getLegalAttributes( objectName ).equals( attrs ) );
	        
    	    reg.removeMapping( objectName );
    	    
    	    assert( reg.getPrefix( objectName) == null );
    	    assert( reg.getObjectName( prefixes[ i ] ) == null );
    	    assert( reg.getLegalAttributes( objectName ) == null );
	    }
	}
}






















