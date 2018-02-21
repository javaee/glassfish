/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2003-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package samples.amx;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import javax.management.Attribute;
import javax.management.Notification;
import javax.management.MBeanServerNotification;
import javax.management.AttributeChangeNotification;


/**
	Utility methods for the samples.
 */
public final class SampleUtil
{
	private	SampleUtil()	{}
	
		public static void
	println( Object o )
	{
		System.out.println( toString( o ) );
	}
	
	/**
		Display a Map to System.out.
	 */
		public static void
	displayMap(
		final String	msg,
		final Map 		m)
	{
		println( msg + ": " + toString( m.keySet() ) );
	}
	
	
	private static final String	QUOTE_CHAR	= "\"";
		public static String
	quote( final Object o )
	{
		if ( o == null )
		{
			return quote( "null" );
		}
		
		return( QUOTE_CHAR + o.toString() + QUOTE_CHAR );
	}
	
		public static String
	toString( final Object o )
	{
		String	result	= null;
		
		if ( o == null )
		{
			result	= "null";
		}
		else if ( o instanceof Object[] )
		{
			result	= "[" + arrayToString( (Object[])o, null, ", " ) + "]";
		}
		else if ( o instanceof Notification )
		{
			result	= toString( (Notification)o );
		}
		else if ( o instanceof Attribute )
		{
			result	= toString( (Attribute)o );
		}
		else if ( o instanceof Map )
		{
			result	= toString( (Map)o );
		}
		else
		{
			result	= o.toString();
		}
		return( result );
	}
	
	
		public static String
	toString( final Attribute a )
	{
		return a.getName() + "=" + toString( a.getValue() );
	}
	
		private static void
	append( final StringBuffer b, final Object o)
	{
		if ( b != null && b.length() != 0 )
		{
			b.append( ", " );
		}
		
		b.append( o.toString() );
	}
	
		public static String
	toString( final Notification notif )
	{
		final StringBuffer	b	= new StringBuffer();
		
		append( b, "#" + notif.getSequenceNumber() );
		append( b, new java.util.Date( notif.getTimeStamp() ) );
		append( b, SampleUtil.quote( notif.getSource() ) );
		append( b, notif.getType() );
		
		append( b, "UserData = " + toString( notif.getUserData() ) );
		
		if ( notif instanceof MBeanServerNotification )
		{
			// this should really be done in a MBeanServerNotificationStringifier!
			final MBeanServerNotification	n	= (MBeanServerNotification)notif;
			
			append( b, SampleUtil.quote( n.getMBeanName() ) );
		}
		else if ( notif instanceof AttributeChangeNotification )
		{
			final AttributeChangeNotification	a	= (AttributeChangeNotification)notif;
			append( b, quote( a.getAttributeName() ) +
				", OldValue = " + quote( toString( a.getOldValue() )) +
				", NewValue = " + quote( toString( a.getNewValue() )) );
		}
		
		return( b.toString() );
	}
	
	

		public static String
	arrayToString(
		final Object[] a,
		final String prefix,
		final String suffix )
	{
		final StringBuffer	buf	= new StringBuffer();
		
		for( int i = 0; i < a.length; ++i )
		{
			if ( prefix != null )
			{
				buf.append( prefix );
			}
			
			buf.append( toString( a[ i ] ) );
			
			if ( suffix != null && i < a.length )
			{
				buf.append( suffix );
			}
		}
		
		return( buf.toString() );
	}
	
	
		public static String
	toString( final Map m )
	{
		return( mapToString( m, "\n" ) );
	}
	
		public static String
	mapToString( final Map m, final String separator )
	{
		if ( m == null )
		{
			return( "null" );
		}
		
		final StringBuffer	buf	= new StringBuffer();
		
		final Iterator	iter	= m.keySet().iterator();
		while ( iter.hasNext() )
		{
			final Object	key		= iter.next();
			final Object	value	= m.get( key );
			
			buf.append( key + "=" + SampleUtil.toString( value ) + separator );
		}
		if ( buf.length() != 0 )
		{
			// strip trailing separator
			buf.setLength( buf.length() - separator.length() );
		}
		
		return( buf.toString() );
	}
	
	
		/**
		Get the chain of exceptions via getCause(). The first element is the
		Exception passed.
		
		@param start	the Exception to traverse
		@return		a Throwable[] or an Exception[] as appropriate
	 */
		public static Throwable[]
	getCauses( final Throwable start )
	{
		final List	list	= new ArrayList();
		
		boolean	haveNonException	= false;
		
		Throwable t	= start;
		while ( t != null )
		{
			list.add( t );
			
			if ( ! ( t instanceof Exception ) )
			{
				haveNonException	= true;
			}
			
			final Throwable temp	= t.getCause();
			if ( temp == null )
				break;
			t	= temp;
		}
		
		final Throwable[]	results	= haveNonException ?
			new Throwable[ list.size() ] : new Exception[ list.size() ];
		
		list.toArray( results );
		
		return( results );
	}
	
	
	/**
		Get the original troublemaker.
		
		@param e	the Exception to dig into
		@return		the original Throwable that started the problem
	 */	public static Throwable
	getRootCause( final Throwable e )
	{
		final Throwable[]	causes	= getCauses( e );
		
		return( causes[ causes.length - 1 ] );
	}
	
		/**
		Create a new Set containing all array elements.
	 */
		public static Set
	newSet( final Object []  objects )
	{
		return( newSet( objects, 0, objects.length ) );
	}


	/**
		Create a new Set containing all array elements.
	 */
		public static Set
	newSet(
		final Object []  objects,
		final int		startIndex,
		final int		numItems )
	{
		final Set	set	= new HashSet();
		
		for( int i = 0; i < numItems; ++i )
		{
			set.add( objects[ startIndex + i ] );
		}

		return( set );
	}
}









