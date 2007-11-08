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
 
/*
 * $Header: /cvs/glassfish/admin/mbeans/tests/com/sun/enterprise/admin/dottedname/DottedNameRegistryTest.java,v 1.3 2005/12/25 03:43:06 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:43:06 $
 */
 
package com.sun.enterprise.admin.dottedname;
 
import java.util.Set;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;

public class DottedNameRegistryTest extends junit.framework.TestCase
{
		DottedNameRegistry1To1Impl
	createNew()
	{
		return( new DottedNameRegistry1To1Impl() );
	}
	
		public void
	testCreation()
	{
		createNew();
	}
	
		public void
	testNewIsEmpty()
	{
		assertEquals( 0, createNew().allDottedNameStrings().size()  );
	}
	
		public void
	testAdd()
		throws MalformedObjectNameException
	{
		final DottedNameRegistry1To1Impl	registry	= createNew();
		final String				dottedName	= "a.b.c";
		final ObjectName			objectName	= new ObjectName( ":name=test" );
		
		registry.add( dottedName, objectName );
		
		assertEquals( objectName, registry.dottedNameToObjectName( dottedName ) );
		assertEquals( dottedName, registry.objectNameToDottedName( objectName ) );
		
		assertEquals( 1, registry.allDottedNameStrings().size() );
		assertEquals( 1, registry.allObjectNames().size() );
		assertEquals( dottedName, (String)registry.allDottedNameStrings().iterator().next() );
	}
	
		public void
	testRemove()
		throws MalformedObjectNameException
	{
		final DottedNameRegistry1To1Impl	registry	= createNew();
		final String				dottedName	= "a.b.c";
		final ObjectName			objectName	= new ObjectName( ":name=test" );
		
		registry.add( dottedName, objectName );
		registry.remove( dottedName, objectName );
		
		assertEquals( 0, registry.allDottedNameStrings().size() );
		assertEquals( 0, registry.allObjectNames().size() );
	}
	
		public void
	testReplaceWorks()
		throws MalformedObjectNameException
	{
		final DottedNameRegistry1To1Impl	registry	= createNew();
		final String				dottedName1	= "a.b.c";
		final String				dottedName2	= "a.b.c.d";
		final ObjectName			objectName	= new ObjectName( ":name=test" );
		
		registry.add( dottedName1, objectName );
		registry.add( dottedName2, objectName );
		assertEquals( dottedName2, registry.objectNameToDottedName( objectName ) );
	}

	
		public void
	test1To1Mapping()
		throws MalformedObjectNameException
	{
		final DottedNameRegistry1To1Impl	registry	= createNew();
		final String				dottedName1	= "a.b.c";
		final String				dottedName2	= "a.b.c.d";
		final ObjectName			objectName	= new ObjectName( ":name=test" );
		
		registry.add( dottedName1, objectName );
		registry.add( dottedName2, objectName );
		
		assertEquals( 1, registry.allDottedNameStrings().size() );
		assertEquals( dottedName2, (String)registry.allDottedNameStrings().iterator().next() );
	}
	
	/*
		public void
	testSpeed()
		throws MalformedObjectNameException
	{
		final long	start	= System.currentTimeMillis();
		
		final DottedNameRegistry1To1Impl	registry	= createNew();
		final String				dottedName1	= "a.b.c";
		final String				dottedName2	= "a.b.c.d";
		final ObjectName			objectName	= new ObjectName( ":name=test" );
		
		final int	ITERATIONS	= 50 * 1024;
		for( int i = 0; i < ITERATIONS; ++i )
		{
			final String	dottedName	= "test." + i;
			final ObjectName	newObjectName	= new ObjectName( ":" + "number=" + i );
			
			registry.add( dottedName, newObjectName );
			registry.remove( dottedName, newObjectName );
		}
		
		final long	elapsed	= System.currentTimeMillis() - start;
		System.out.println( "elapsed = " + elapsed + " = " +
			((float)ITERATIONS / (float)elapsed) + " per milli" );
	}
	*/
}










