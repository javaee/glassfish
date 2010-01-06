/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2010 Sun Microsystems, Inc. All rights reserved.
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
 * $Header: /m/jws/jmxcmd/tests/com/sun/cli/jmxcmd/util/jmx/ObjectNameQueryImplTest.java,v 1.2 2003/11/21 22:15:45 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2003/11/21 22:15:45 $
 */
package org.glassfish.admin.amx.util.jmx;

import java.util.Set;
import java.util.HashSet;

import javax.management.ObjectName;

public class ObjectNameQueryImplTest extends junit.framework.TestCase
{
		public void
	testCreation()
	{
		new ObjectNameQueryImpl();
	}
	
	static final Set<ObjectName>		EmptySet	= java.util.Collections.emptySet();
	static final String []	EmptyStrings	= new String [0];
	
		static Set<ObjectName>
	createSet( ObjectName name )
	{
		final HashSet<ObjectName>	s	= new HashSet<ObjectName>();
		
		s.add( name );
		
		return( s );
	}
	
		static Set<ObjectName>
	createSet( ObjectName [] names )
	{
		final HashSet<ObjectName>	s	= new HashSet<ObjectName>();
		
		for( int i = 0; i < names.length; ++i )
		{
			s.add( names[ i ] );
		}
		
		return( s );
	}
	
	
		static ObjectName
	createName( String nameString )
	{
		ObjectName	name	= null;
		
		try
		{
			name	= new ObjectName( nameString );
		}
		catch( Exception e )
		{
			assert( false );
		}
		return( name );
	}
	
		public void
	testEmptySet()
	{
		final ObjectNameQuery	q	= new ObjectNameQueryImpl();
		
		assertEquals( 0, q.matchAny( EmptySet, null, null ).size() );
		assertEquals( 0, q.matchAny( EmptySet, EmptyStrings, EmptyStrings ).size() );
		assertEquals( 0, q.matchAny( EmptySet, null, EmptyStrings ).size() );
		assertEquals( 0, q.matchAny( EmptySet, EmptyStrings, null ).size() );
		
		assertEquals( 0, q.matchAll( EmptySet, null, null ).size() );
		assertEquals( 0, q.matchAll( EmptySet, EmptyStrings, EmptyStrings ).size() );
		assertEquals( 0, q.matchAll( EmptySet, null, EmptyStrings ).size() );
		assertEquals( 0, q.matchAll( EmptySet, EmptyStrings, null ).size() );
	}
	
		public void
	testSingleItem()
	{
		final ObjectName	name1	= createName( ":name=test,type=test" );
		final Set<ObjectName>			testSet	= createSet( name1 );
		
		final ObjectNameQuery	q	= new ObjectNameQueryImpl();
		
		assertEquals( 1, q.matchAny( testSet,
			null,
			null ).size() );
		
		assertEquals( 1, q.matchAny( testSet, 
			new String [] { "type" },
			new String [] { "test" } ).size() );
			
		assertEquals( 1, q.matchAny( testSet, 
			new String [] { "type" },
			null ).size() );
			
		assertEquals( 1, q.matchAny( testSet, 
			null,
			new String [] { "test" } ).size() );
			
			
		assertEquals( 1, q.matchAny( testSet, 
			new String [] { "t.*" },
			new String [] { ".*e.*" } ).size() );
			
			
		assertEquals( 1, q.matchAny( testSet, 
			null,
			new String [] { ".*e.*" } ).size() );
		
		
		assertEquals( 0, q.matchAny( testSet, EmptyStrings, EmptyStrings ).size() );
		assertEquals( 0, q.matchAny( testSet, null, EmptyStrings ).size() );
		assertEquals( 0, q.matchAny( testSet, EmptyStrings, null ).size() );
	}
}






