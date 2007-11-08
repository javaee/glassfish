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
 * $Header: /cvs/glassfish/admin/mbeans/tests/com/sun/enterprise/admin/dottedname/DottedNameTest.java,v 1.3 2005/12/25 03:43:07 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:43:07 $
 */
 

package com.sun.enterprise.admin.dottedname;

/*
	This MBean must be modified to store its aliases within domain.xml.  For now, it uses
	an internal implementation.
 */
public final class DottedNameTest extends junit.framework.TestCase
{
		public
	DottedNameTest(  )
	{
	}
	
		public void
	attemptInvalidName( String name )
	{
		try
		{
			final DottedName dottedName	= new DottedName( name );
			fail( "expected dotted name to fail: \"" + name + "\"");
		}
		catch( Exception e )
		{
			// good, we expected to get here
		}
	}
	
	public final static String	LEGAL_CHARS	=  DottedName.LEGAL_CHARS;
			
	public final static String	ILLEGAL_CHARS	= 
			"!#%^&+=" +
			"~`:\"\',?" +
			"\n\r\t\"\'";

	
	private static final char	BACKSLASH	= '\\';
		public void
	testEscapedEscapeChar()
	{
		try
		{
			DottedName	dn	= new DottedName( "test" + BACKSLASH + "Name" );
			assert( false );
		}
		catch( IllegalArgumentException e )
		{
			// good
		}
		
		final DottedName	dn	= new DottedName( "test" + BACKSLASH + BACKSLASH + "Name" );
		assertEquals( "test" + BACKSLASH + BACKSLASH + "Name", dn.toString() );
	}
	
		public void
	testEscapedDot()
	{
		final String		name	= "test" + BACKSLASH + ".1.part" + BACKSLASH + ".1";
		final DottedName	dn	= new DottedName( name );
		assertEquals( "test.1", dn.getScope() );
		assertEquals( "part.1", dn.getParts().get( 0 ) );
		
		
	}
	
		public void
	testEmptyName()
		throws Exception
	{
		attemptInvalidName( "" );
	}
	
		public void
	testScopeOnly()
		throws Exception
	{
		new DottedName( "domain" );
	}
	
		public void
	testMissingValue()
		throws Exception
	{
		attemptInvalidName( "domain." );
	}
	
		public void
	testBadSyntax()
		throws Exception
	{
		attemptInvalidName( "." );
		attemptInvalidName( ".." );
		attemptInvalidName( "..." );
		attemptInvalidName( ".x.." );
		attemptInvalidName( "x.x.x." );
	}
	
		public void
	testWithDomain()
		throws Exception
	{
		new DottedName( "mydomain:domain.locale" );
		new DottedName( "mydomain:domain.a.b.c.foo" );
	}
	
		public void
	testDomainOnly()
		throws Exception
	{
		attemptInvalidName( "mydomain:" );
	}
	
		public void
	testDomainAndScopeOnly()
		throws Exception
	{
		new DottedName( "mydomain:domain" );
	}
	
		public void
	testLongName()
		throws Exception
	{
		final DottedName dn	= new DottedName( "mydomain:scope.b.c.d.e.f.g.h.i.j.k.l.m.n.o.p" );
		
		assertEquals( dn.getDomain(), "mydomain" );
		assertEquals( dn.getScope(), "scope" );
		assertEquals( dn.getParts().size(), 15  );
		assertEquals( dn.getParts().get( 14 ), "p"  );
	}
	
		public void
	testEscapedName()
		throws Exception
	{
		final DottedName dn	= new DottedName( "domain.server\\.1.port" );
		
		assertEquals( dn.getScope(), "domain" );
		assertEquals( dn.getParts().get( 0 ), "server.1" );
		assertEquals( dn.getParts().get( 1 ), "port" );
	}
	
		public void
	testThatToStringMatchesOrig()
		throws Exception
	{
		final String	TEST	= "domain.server\\.1.port";
		final DottedName dn	= new DottedName( TEST );
		
		assertEquals( dn.toString(), TEST );
	}
	
		public void
	testIllegalChars()
	{
		for( int i = 0; i < ILLEGAL_CHARS.length(); ++i )
		{
			final char	theChar	= ILLEGAL_CHARS.charAt( i );
			
			attemptInvalidName( "" + theChar );
			attemptInvalidName( "domain." + theChar + "y" );
		}
	}
	
		public void
	testNameWithPartOfDot()
		throws Exception
	{
		new DottedName( "domain." + DottedName.escapePart( "" + '.' ) );
	}
	
	
		public void
	testLegalChars()
		throws Exception
	{
		for( int i = 0; i < LEGAL_CHARS.length(); ++i )
		{
			final char	theChar	= LEGAL_CHARS.charAt( i );
			
			final String	escapedChar	= DottedName.escapePart( "" + theChar );
			new DottedName( "domain." + escapedChar );
		}
	}
}








