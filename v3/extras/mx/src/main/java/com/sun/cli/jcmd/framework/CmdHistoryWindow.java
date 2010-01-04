/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2009 Sun Microsystems, Inc. All rights reserved.
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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/CmdHistoryWindow.java,v 1.3 2004/01/09 22:17:26 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2004/01/09 22:17:26 $
 */
 

package com.sun.cli.jcmd.framework;

import java.util.Set;


/**
	Interface describing a command history.
 */
public final class CmdHistoryWindow implements CmdHistory
{
	final CmdHistory	mHistory;
	final int			mFirst;
	int					mLast;
	
		public
	CmdHistoryWindow( CmdHistory history, int first, int last)
	{
		if (	first < history.getFirstCmd().getID() ||
				last > history.getLastCmd().getID() ||
				first > last + 1 )
		{
			throw new IllegalArgumentException( "Illegal range: " + first + ", " + last );
		}
		
		mHistory	= history;
		mFirst		= first;
		mLast		= last;
		
	}
	
		public
	CmdHistoryWindow( CmdHistory history,  int last)
	{
		this( history, history.getFirstCmd().getID(), last );
		
	}
	
	public	CmdHistoryItem	getFirstCmd()	{ return( mHistory.getCmd( mFirst ) ); }
	public	CmdHistoryItem	getLastCmd()	{ return( mHistory.getCmd( mLast ) ); }
	
		public CmdHistoryItem
	getCmd( int id )
	{
		if ( id < mFirst || id > mLast )
		{
			throw new IllegalArgumentException( "Illegal cmd ID: " + id );
		}

		return( mHistory.getCmd( id ) );
	}
	
		public CmdHistoryItem[]
	getRange( int first, int last )
	{
		if (	first < getFirstCmd().getID() ||
				last > getLastCmd().getID() ||
				first > last + 1 )
		{
			throw new IllegalArgumentException( "Illegal range: " + first + ", " + last );
		}
		
		return( mHistory.getRange( first, last ) );
	}
	
		public void
	truncate( int last )
	{
		mLast	= last;
	}
	
		public void
	clear( )
	{
		mLast	= mFirst -1;
	}
	
		public CmdHistoryItem[]
	getAll()
	{
		return( mHistory.getRange( mFirst, mLast ) );
	}
	
		public int
	addCmd( String[] tokens )
	{
		throw new IllegalArgumentException( "can't add a command to a windowed history" );
	}
};
	
