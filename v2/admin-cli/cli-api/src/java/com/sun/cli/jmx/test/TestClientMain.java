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
 * $Header: /cvs/glassfish/admin-cli/cli-api/src/java/com/sun/cli/jmx/test/TestClientMain.java,v 1.3 2005/12/25 03:45:53 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:45:53 $
 */
 
package com.sun.cli.jmx.test;


import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.ListIterator;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;

import com.sun.cli.jmx.cmd.CmdReader;
import com.sun.cli.jmx.support.StringifierRegistryIniter;
import com.sun.cli.jmx.support.StandardAliasesIniter;
import com.sun.cli.jmx.cmd.ArgHelper;
import com.sun.cli.jmx.cmd.ArgHelperImpl;
import com.sun.cli.jmx.cmd.ArgHelperOptionsInfo;

import com.sun.cli.jmx.support.CLISupportMBeanProxy;
import com.sun.cli.jmx.support.AliasMgrMBean;
import com.sun.cli.jmx.support.AliasMgrHashMapImpl;
import com.sun.cli.jmx.support.AliasMgr;
import com.sun.cli.jmx.support.CLISupport;
import com.sun.cli.jmx.support.CLISupportMBean;

import com.sun.cli.jmx.cmd.ArgHelperImpl;
import com.sun.cli.jmx.cmd.ArgHelperOptionsInfo;

/*
	Trivial application that displays a string
*/

public class TestClientMain implements NotificationTester.ThroughputCallback
{
	long	mMilliseconds	= 0;
	long	mNumCalls		= 0;
	long	mStartTime		= 0;
	long	mTotalNumCalls	= 0;
	short	mNumThreads		= 1;
	short	mNumAveraged	= 0;
	Object	mCritical	= new Object();

	
	TestClientMain(  )
	{
	}
	
		long
	now()
	{
		return( System.currentTimeMillis() );
	}

		private static void
	p( Object arg )
	{
		System.out.println( arg.toString() );
	}
	
		public void
	throughputReport( long milliseconds, long numCalls )
	{
		String	progString	= "";
		
		synchronized ( mCritical )
		{
			mTotalNumCalls		+= numCalls;
			
			++mNumAveraged;
			if ( mNumAveraged > mNumThreads * 8 )
			{
				// reset periodically so we don't get hurt by blips in poor performance
				mMilliseconds	= 0;
				mNumCalls		= 0;
				mNumAveraged	= 0;
			}
			
			mMilliseconds	+= milliseconds;
			mNumCalls		+= numCalls;
			
			final double	rollingCallsPerSec	= mNumCalls / ((mMilliseconds / 1000.0) / (double)mNumThreads);
			final double	totalCallsPerSec	= mTotalNumCalls / ((now() - mStartTime) / 1000.0);
			
			progString	= mNumThreads + " threads: " +
							(long)rollingCallsPerSec + " (rolling) " +
							(long)totalCallsPerSec + " (total)";
		}
		
		// keep p() outside synchronized section
		p( progString );
	}
	
		private void
	testNotifications( String host, int port ) throws Exception
	{
		System.out.println( "Testing:  " + host + ":" + port + ", threads = " + mNumThreads );
		
		final int	kNumInvokes	= 1 * 1024;
		
		mStartTime	= System.currentTimeMillis();
		for( int i = 0; i < mNumThreads; ++i )
		{
			NotificationTester	test	= new NotificationTester( host, port);
			
			// test.RunMultiplePerConnectionThreaded( "" + i, 1000, kNumInvokes, this);
			test.RunNotifTest( 0 );
		}
	}
	

		private static JMXConnector
	establishConnection( String host, int port )
		throws Exception
	{
		final JMXServiceURL	url	= new JMXServiceURL( "service:jmx:jmxmp://" + host + ":" + port );
			
		JMXConnector conn	= JMXConnectorFactory.connect( url );
		
		return( conn );
	}
	
		private CLISupportMBeanProxy
	createProxy(MBeanServerConnection managedServer, boolean runLocally ) throws Exception
	{
		CLISupportMBeanProxy proxy	= null;
		
		AliasMgrMBean	aliasMgr	= null;
		CLISupportMBean	cliSupport	= null;
		
		if ( runLocally )
		{
			final AliasMgrHashMapImpl		aliasMgrImpl	= new AliasMgrHashMapImpl();
			aliasMgrImpl.load( AliasMgrHashMapImpl.DEFAULT_FILENAME );
			aliasMgr	= new AliasMgr( aliasMgrImpl );
			StandardAliasesIniter.init( aliasMgr );
			
			cliSupport	= new CLISupport( managedServer, aliasMgr );
			
		}
		else
		{
			aliasMgr	= CLISupportMBeanProxy.createAliasMgrProxy( managedServer );
			cliSupport	= CLISupportMBeanProxy.createCLISupportProxy( managedServer );
		}
		
		proxy	= new CLISupportMBeanProxy( aliasMgr, cliSupport);
		
		return( proxy );
	}
	
		private void
	testCLISupport( MBeanServerConnection conn, boolean runLocally) throws Exception
	{
		final CLISupportMBeanProxy	proxy	= createProxy( conn, runLocally );
		
		final CLISupportTester	tester	= new CLISupportTester( conn, proxy );
		tester.Run();
	}

		
	private final static String OPTIONS = "host=1 port=2 interactive localsupport";
	
		public static void
	main(String args[])
	{
		try
		{
			final ArgHelperOptionsInfo		optionInfo	= new ArgHelperOptionsInfo( OPTIONS );
			
			final ListIterator	iter	= Arrays.asList( args ).listIterator();
			final ArgHelper	argHelper	= new ArgHelperImpl( iter, optionInfo);
			
			final Integer	port		= argHelper.getInteger( "--port" );
			final String	host		= argHelper.getString( "--host", "localhost");
			final boolean	interactive	= argHelper.getBoolean( "--interactive", Boolean.FALSE).booleanValue();
			final boolean	runLocally	= argHelper.getBoolean( "--localsupport", Boolean.FALSE ).booleanValue();
			
			if ( port == null )
			{
				System.out.println( "USAGE: TestClient --port=<port-number>" );
				System.exit( 1 );
			}
			
			final TestClientMain	testMain	= new TestClientMain( );
			
			new StringifierRegistryIniter();
			
			final JMXConnector	jmxConnector	= establishConnection( host, port.intValue() );
			final MBeanServerConnection	conn	= jmxConnector.getMBeanServerConnection();
			
			p( "Connected to: " + host + ":" + port );
			
			p( ( runLocally ? "Running with local CLI support.":"Running with remote CLI support") );
			
			//testMain.testNotifications( host, port.intValue() );
			testMain.testCLISupport( conn, runLocally );
		}
		catch( Exception e )
		{
			p( e );
		}
	}
};


