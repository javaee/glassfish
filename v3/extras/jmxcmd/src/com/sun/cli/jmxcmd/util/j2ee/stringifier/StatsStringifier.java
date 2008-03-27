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
package com.sun.cli.jmxcmd.util.j2ee.stringifier;

import java.util.Arrays;

import javax.management.j2ee.statistics.Stats;
import javax.management.j2ee.statistics.Statistic;

import com.sun.cli.jcmd.util.stringifier.Stringifier;
import com.sun.cli.jcmd.util.stringifier.SmartStringifier;
import com.sun.cli.jcmd.util.stringifier.ArrayStringifier;

/**
	Stringifier for javax.management.j2ee.statistics 
 */
public class StatsStringifier implements Stringifier
{
	public static final StatsStringifier	DEFAULT	= new StatsStringifier();
	
		public
	StatsStringifier( )
	{
	}
	
		public String
	stringify( Object o )
	{
		final Stats			stats	= (Stats)o;
		final String []		names	= stats.getStatisticNames();
		
		Arrays.sort( names );
		
		final StringBuffer	buf	= new StringBuffer();
		
		buf.append( "Stats: " + ArrayStringifier.stringify( names, ", " ) + "\n" );
		
		for( int i = 0; i < names.length; ++i )
		{
			final Object	statistic	= stats.getStatistic( names[ i ] );
			
			buf.append( SmartStringifier.toString( statistic ) );
			buf.append( "---\n" );
		}
		buf.append( "\n" );
		
		return( buf.toString() );
	}
}



















