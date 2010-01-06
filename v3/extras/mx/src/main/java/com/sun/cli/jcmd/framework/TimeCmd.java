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

package com.sun.cli.jcmd.framework;

import java.util.Collections;
import java.util.Map;
import java.util.Date;
import java.util.HashMap;

import com.sun.cli.jcmd.util.cmd.CmdInfos;
import com.sun.cli.jcmd.util.cmd.CmdInfo;
import com.sun.cli.jcmd.util.cmd.CmdInfoImpl;
import com.sun.cli.jcmd.util.cmd.OperandsInfo;
import com.sun.cli.jcmd.util.cmd.OperandsInfoImpl;

import org.glassfish.admin.amx.util.stringifier.ArrayStringifier;

/**
 */
public class TimeCmd extends CmdBase
{
	private final static Map<String,Long> mTimeMap	=
	    Collections.synchronizedMap( new HashMap<String,Long>() );
	
		public
	TimeCmd( final CmdEnv env )
	{
		super( env );
	}
	

	static final class TimeCmdHelp extends CmdHelpImpl
	{
			public
		TimeCmdHelp()	{ super( getCmdInfos() ); }
		
		static final String	SYNOPSIS		= "get current and/or elapsed time";
		static final String	SOURCE_TEXT		=
		"time -- prints the current time\n" +
		"mark-time <name> prints the current time, and names it with 'name'\n" +
		"time-since [<name>] displays time since last time <name> was marked";

		public String	getSynopsis()	{	return( formSynopsis( SYNOPSIS ) ); }
		public String	getText()		{	return( SOURCE_TEXT ); }
	}
		public CmdHelp
	getHelp()
	{
		return( new TimeCmdHelp() );
	}

	
	
	public final static String	NAME				= "time";
	public final static String	TIME_CMD_NAME		= "time";
	public final static String	MARK_TIME_CMD_NAME	= "mark-time";
	public final static String	TIME_SINCE_CMD_NAME	= "time-since";
	
	private final static CmdInfo	TIME_CMD_INFO	=
		new CmdInfoImpl( TIME_CMD_NAME );
		
	private final static CmdInfo	MARK_TIME_CMD_INFO	=
		new CmdInfoImpl( MARK_TIME_CMD_NAME, null, new OperandsInfoImpl( "marker-name", 1, 1) );
		
	private final static CmdInfo	TIME_SINCE_CMD_INFO	=
		new CmdInfoImpl( TIME_SINCE_CMD_NAME, null, new OperandsInfoImpl( "marker-name", 0, 1) );
	
	private final static CmdInfo[]	CMD_INFOS =
		new CmdInfo[] { TIME_CMD_INFO, MARK_TIME_CMD_INFO, TIME_SINCE_CMD_INFO };
		
		public static CmdInfos
	getCmdInfos( )
	{
		return( new CmdInfos( CMD_INFOS  ) );
	}
	
	private static final String	LAST	= "last";
	
		private void
	displayTime( final long theTime )
	{
		println( new Date( theTime ).toString() + " " + (theTime % 1000) + " ms" );
	}
	
	
		private void
	displayTime( )
	{
		final long	now	= now();
		
		displayTime( now );
		putTime( LAST, now );
	}
		
		private final void
	putTime(
		final String	markerName,
		final long		theTime )
	{
		mTimeMap.put( markerName, new Long( theTime ) );
	}

		private void
	markTime( final String markerName )
	{
		final long	now	= now();
		
		putTime( markerName, now );
		putTime( LAST, now );
		
		displayTime( now );
	}
		
		private void
	timeSince( final String markerName )
		throws CmdException
	{
		final long	now	= System.currentTimeMillis();
		
		final Long	start	= (Long)mTimeMap.get( markerName );
		
		if ( start == null )
		{
			throw new CmdException( getSubCmdNameAsInvoked(),
					"marker " + quote( markerName ) + " not found" );
		}

		final long	elapsed	= now - start.longValue();
		
		println( (elapsed / 1000.0) + " seconds" );
		
		putTime( LAST, now );
	}
	
	
		protected void
	executeInternal()
		throws Exception
	{
		final String [] operands	= getOperands();
		final String	cmd	= getSubCmdNameAsInvoked();
		
		if ( cmd.equals( TIME_CMD_NAME ) )
		{
			displayTime();
		}
		else if ( cmd.equals( MARK_TIME_CMD_NAME ) )
		{
			markTime( operands[ 0 ] );
		}
		else if ( cmd.equals( TIME_SINCE_CMD_NAME ) )
		{
			timeSince( operands.length == 0 ?  LAST : operands[ 0 ] );
		}
		else
		{
		}
	}
}






