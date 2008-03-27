/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
