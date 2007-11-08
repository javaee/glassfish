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
 * $Header: /cvs/glassfish/admin/mbeans/tests/com/sun/enterprise/admin/dottedname/Testee.java,v 1.3 2005/12/25 03:43:09 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:43:09 $
 */
 

package com.sun.enterprise.admin.dottedname;

import java.util.Iterator;
import java.util.HashMap;

import javax.management.Attribute;
import javax.management.AttributeList;

	
public final class Testee implements TesteeMBean
{
	private char		mchar	= 'c';
	private byte		mbyte	= 0;
	private short		mshort	= 0;
	private int			mint	= 0;
	private long		mlong	= 0;
	private float		mfloat	= (float)0.0;
	private double		mdouble	= 0.0;
	
	private Character	mCharacter	= new Character( 'c' );
	private Byte		mByte		= new Byte( (byte)0 );
	private Short		mShort		= new Short( (short)0 );
	private Integer		mInteger	= new Integer( 0 );
	private Long		mLong		= new Long( 0 );
	private Float		mFloat		= new Float( 0.0 );
	private Double		mDouble		= new Double( 0.0 );
	
	private String		mString	= "";
	private String []	mStringArray	= new String[ 0 ];
	private Integer []	mIntegerArray	= new Integer[ 0 ];
	
	private HashMap	mProperties;
	
	public final static String	PROPERTY_NAME	= "prop1";
	public final static String	PROPERTY_VALUE	= "prop1-value";
	
		public
	Testee()
	{
		mProperties	= new HashMap();
		
		mProperties.put( PROPERTY_NAME, PROPERTY_VALUE );
	}
	
	public char		getchar()	{ return( mchar ); }
	public byte		getbyte()	{ return( mbyte ); }
	public short	getshort()	{ return( mshort ); }
	public int		getint()	{ return( mint ); }
	public long		getlong()	{ return( mlong ); }
	public float	getfloat()	{ return( mfloat ); }
	public double	getdouble()	{ return( mdouble ); }
	public String	getString()	{ return( mString ); }
	
	
	public void		setchar( char value )		{ mchar		= value; }
	public void		setbyte( byte value )		{ mbyte		= value; }
	public void		setshort( short value )		{ mshort	= value; }
	public void		setint( int value )			{ mint		= value; }
	public void		setlong( long value )		{ mlong		= value; }
	public void		setfloat( float value )		{ mfloat	= value; }
	public void		setdouble( double value )	{ mdouble	= value; }
	public void		setString( String value )	{ mString	= value; }
	
	
	public Character	getCharacter()	{ return( mCharacter ); }
	public Byte			getByte()	{ return( mByte ); }
	public Short		getShort()	{ return( mShort ); }
	public Integer		getInteger(){ return( mInteger ); }
	public Long			getLong()	{ return( mLong ); }
	public Float		getFloat()	{ return( mFloat ); }
	public Double		getDouble()	{ return( mDouble ); }
	public String[]		getStringArray()	{ return( mStringArray ); }
	public Integer[]	getIntegerArray()	{ return( mIntegerArray ); }
	
	
	public void		setCharacter( Character value )	{ mCharacter		= value; }
	public void		setByte( Byte value )		{ mByte		= value; }
	public void		setShort( Short value )		{ mShort	= value; }
	public void		setInteger( Integer value )	{ mInteger	= value; }
	public void		setLong( Long value )		{ mLong		= value; }
	public void		setFloat( Float value )		{ mFloat	= value; }
	public void		setDouble( Double value )	{ mDouble	= value; }
	public void		setStringArray( String[] value )	{ mStringArray	= value; }
	public void		setIntegerArray( Integer[] value )	{ mIntegerArray	= value; }
	
	
	
		synchronized public Object
	getPropertyValue( String propertyName )
	{
		return( (String)mProperties.get( propertyName ) );
	}
	
		synchronized public AttributeList
	getProperties()
	{
		final Iterator		iter	= mProperties.keySet().iterator();
		final AttributeList	attrs	= new AttributeList();
		
		while ( iter.hasNext() )
		{
			final String	name	= (String)iter.next();
			final Object	value	= mProperties.get( name );
			
			attrs.add(  new Attribute( name, value ) );
		}
		
		return( attrs );
	}
	
		synchronized public void
	setProperty( Attribute property )
	{
		mProperties.put( property.getName(), property.getValue() );
	}
}









