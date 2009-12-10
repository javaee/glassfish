/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/misc/CircularList.java,v 1.1 2005/11/08 22:39:20 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2005/11/08 22:39:20 $
 */
 
package com.sun.cli.jcmd.util.misc;

import java.util.AbstractList;


import org.glassfish.admin.amx.util.CompareUtil;
import org.glassfish.admin.amx.util.stringifier.ArrayStringifier;


/**
	A circular list implementation
 */
public final class CircularList extends AbstractList
{
	private final Object[]		mObjects;
	private int					mNumItems;
	private int					mFirst;
	private OverflowHandler		mOverflowHandler;
	
		public
	CircularList( final int size )
	{
		if ( size == 0 )
		{
			throw new IllegalArgumentException( "list must have at least one item" );
		}

		mObjects	= new Object[ size ];
		mFirst		= 0;
		mNumItems	= 0;
		
		mOverflowHandler	= null;
	}
	
	public interface OverflowHandler
	{
		void	handleBufferOverflow( Object o );
	}
	
		public void
	setOverflowHandler( final OverflowHandler handler )
	{
		mOverflowHandler	= handler;
	}
	
		public OverflowHandler
	getOverflowHandler( )
	{
		return( mOverflowHandler );
	}
	
		public final int
	size()
	{
		return( mNumItems );
	}
	
		public final int
	capacity()
	{
		return( mObjects.length );
	}
	
	
		public final void
	clear()
	{
		for( int i = 0; i < size(); ++i )
		{
			set( i, null );
		}
		mNumItems	= 0;
		++modCount;
	}
	
		private final int
	getPhysicalIndex( final int logicalIndex )
	{
		return( (mFirst + logicalIndex) % capacity() );
	}
	
		public final Object
	get( final int i)
	{
		checkInBounds( i );
		
		return( mObjects[ getPhysicalIndex( i ) ] );
	}
	
	
		public final Object
	set(
		final int		i,
		final Object	item)
	{
		checkInBounds( i );
		
		final int	physicalIndex	= getPhysicalIndex( i );
		final Object	oldItem	= mObjects[ physicalIndex ];
		mObjects[ physicalIndex ] = item;
		
		return( oldItem );
	}
	
	
		protected void
	discardedObject( final Object o )
	{
		if ( mOverflowHandler != null )
		{
			mOverflowHandler.handleBufferOverflow( o );
		}
	}
	
		private final void
	store(
		final int			logicalIndex,
		final Object		item )
	{
		mObjects[ getPhysicalIndex( logicalIndex ) ]	= item;
	}
	
		public final boolean
	add( final Object item )
	{
		final int	capacity	= capacity();
		
		assert( mFirst < capacity );
		
		// if we're full before adding, then we'll be overwriting
		// the first item.
		if ( size() == capacity )
		{
			final Object	overwrittenObject	= get( 0 );
			
			// first item will be overwritten; next one is first (oldest)
			mFirst	= (mFirst + 1) % capacity;
			store( capacity - 1, item );
			
			discardedObject( overwrittenObject );
			
		}
		else
		{
			store( mNumItems, item );
			++mNumItems;
		}
		
		++modCount;
		return( true );
	}
	
	/**
		May be added to the end only.
	 */
		public final void
	add(
		final int		index,
		final Object	item )
	{
		if ( index == mNumItems )
		{
			add( item );
		}
		else
		{
			throw new UnsupportedOperationException( "add not at end" );
		}
	}
	
		public final Object
	remove( final int i)
	{
		Object	result	= null;
		
		if ( i == 0 )
		{
			result	= removeFirst();
		}
		else if ( i == mNumItems - 1 )
		{
			result	= removeLast();
		}
		else
		{
			throw new UnsupportedOperationException();
		}
		
		++modCount;
		return( result );
	}
	
		private final void
	checkInBounds( final int i )
	{
		if ( i < 0 || i >= mNumItems )
		{
			throw new IndexOutOfBoundsException( "" + i );
		}
	}
	
		public final Object
	removeFirst()
	{
		checkInBounds( 0 );
		
		final Object	result	= get( 0 );
		--mNumItems;
		mFirst	= (mFirst + 1) % capacity();
		
		return( result );
	}
	
	
		public final Object
	removeLast()
	{
		checkInBounds( 0 );
		
		final Object	result	= get( mNumItems - 1 );
		--mNumItems;
		
		return( result );
	}
	
	
		public boolean
	equals( final Object rhsIn )
	{
		boolean	equal	= false;
		
		if ( rhsIn == this )
		{
			equal	= true;
		}
		else if ( ! (rhsIn instanceof CircularList) )
		{
			equal	= false;
		}
		else
		{
			final CircularList	rhs	= (CircularList)rhsIn;
			
			equal =  capacity() == rhs.capacity() &&
				size() == rhs.size();
			if ( equal )
			{
				final int	size	= size();
				for( int i = 0; i < size(); ++i )
				{
					if ( ! CompareUtil.objectsEqual( get( i ), rhs.get( i ) ) )
					{
						equal	= false;
						break;
					}
				}
			}
		}
		
		return( equal );
	}

		public String
	toString()
	{
		return( ArrayStringifier.stringify( toArray( ), ", " ) );
	}
}

