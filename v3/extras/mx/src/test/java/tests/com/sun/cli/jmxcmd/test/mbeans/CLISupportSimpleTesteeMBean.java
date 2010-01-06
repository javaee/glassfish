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
 * $Header: /m/jws/jmxcmd/tests/com/sun/cli/jmxcmd/test/mbeans/CLISupportSimpleTesteeMBean.java,v 1.6 2004/04/24 02:10:19 llc Exp $
 * $Revision: 1.6 $
 * $Date: 2004/04/24 02:10:19 $
 */
 
package  com.sun.cli.jmxcmd.test.mbeans;

import java.util.Properties;

import java.net.URL;
import java.net.URI;

public interface CLISupportSimpleTesteeMBean
{

	class NonSerializable
	{
		int	x;
			public
		NonSerializable()	{x = 0;}
		public String	toString()	{ return( "" + x ); }
			public boolean
		equals( Object rhs )
		{
			return( (rhs instanceof NonSerializable) &&
				x == ((NonSerializable)rhs).x );
		}
	}
	public NonSerializable	getNonSerializable();

	
    public long		getCurrentMillis();
    public long		getCurrentSeconds();
    
    public long		getNotifMillis();
    public void 	setNotifMillis( long millis );
    
    public long		getNotifsEmitted();
    
    public void		startNotif();
    public void		stopNotif();
    
    public void		test11ObjectArgs( String a1, Boolean a2, Character a3, Byte a4, Short a5,
    					Integer a6, Long a7, Float a8, Double a9,
    						java.math.BigInteger a10, java.math.BigDecimal a11);
    
    public void		test11MixedArgs( String a1, boolean a2, char a3, byte a4, short a5,
    					int a6, long a7, float a8, double a9,
    						java.math.BigInteger a10, java.math.BigDecimal a11);
    
	
	public Boolean	testBoolean( Boolean b );
	public boolean	testboolean( boolean b );
	public char		testchar( char c );
	public short	testshort( short x );
	public int		testint( int x );
	public long		testlong( long x );
	
	
	public void		testbooleanVoid( boolean b );
	public void		testcharVoid( char c );
	public void		testshortVoid( short x );
	public void		testintVoid( int x );
	public void		testlongVoid( long x );

    public String		testString( String s );
    public Object		testObject( Object obj );
    public Integer		testInteger( Integer i );
    public int			test_int( int i );
    public URL			testURL( URL u );
    public URI			testURI( URI u );
    public Object []	testObjectArray( Object [] objects );
    
    public String		testcasesensitivity1();
    public String		testCASESENSITIVITY1();
    public String		testCaseSensitivity1();
    
    public String		testCaseSensitivity2();
    
    public Object		testUnknownType();
    //public Object		getUnknownType();
}
