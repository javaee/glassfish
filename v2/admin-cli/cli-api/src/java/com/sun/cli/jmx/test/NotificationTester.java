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
 * $Header: /cvs/glassfish/admin-cli/cli-api/src/java/com/sun/cli/jmx/test/NotificationTester.java,v 1.4 2006/11/10 21:14:45 dpatil Exp $
 * $Revision: 1.4 $
 * $Date: 2006/11/10 21:14:45 $
 */
 
package com.sun.cli.jmx.test;


// java imports
//
import java.util.*;
import java.io.*;
import java.net.*;

// RI imports
//
import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnector;
import javax.management.MBeanServerConnection;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.Notification;
import javax.management.Attribute;

import com.sun.cli.jmx.support.CLISupportStrings;


/*
	Trivial application that displays a string
*/

public class NotificationTester extends Thread
	implements NotificationListener
{
	MBeanServerConnection	mServer;
	JMXConnector			mConnection;
	String					mHost;
	int						mPort;
	ThroughputCallback		mThroughputCallback;

	public static interface ThroughputCallback
	{
		public void	throughputReport( long milliseconds, long numCalls );
	}


		private static void
	p( Object arg )
	{
		System.out.println( arg.toString() );
	}


		public 
	NotificationTester( String host, int port ) throws Exception
	{
		mHost	= host;
		mPort	= port;
		
		mConnection	= Connect( host, port );
		mServer		= mConnection.getMBeanServerConnection( );
	}
	
	
		private JMXConnector
	Connect( String host, int port ) throws Exception
	{
		final JMXServiceURL	url	= new JMXServiceURL( "service:jmx:jmxmp://" + host + ":" + port );
			
		JMXConnector conn	= JMXConnectorFactory.connect( url );
		return( conn );
	}


		public void
	RunSinglePerConnection( int numConnections ) throws Exception
	{
		final long	startTime	= System.currentTimeMillis();
		
		final ObjectName	name	= new ObjectName( CLISupportStrings.CLI_SUPPORT_TARGET );
		for( int i = 0; i < numConnections; ++i )
		{
			JMXConnector			conn	= Connect( mHost, mPort );
			MBeanServerConnection	server	= conn.getMBeanServerConnection( );
			
			Object result	= server.getAttribute( name, "NbChanges" );
			conn.close();
		}
		
		final long	endTime	= System.currentTimeMillis();
		final long	elapsed	= endTime - startTime;
		
		p( "elapsed = " + elapsed );
		p( "connections per second = " + numConnections / (elapsed / 1000.0) );
	}
	
		public void
	RunMultiplePerConnection( int outerCount, int numIterations ) throws Exception
	{
		final ObjectName	name	= new ObjectName( "foo" );
		//final ObjectName	name	= new ObjectName( TestShared.kSimpleDynamicName );
			
		for( int outer = 0; outer < outerCount; ++outer )
		{
			final long	startTime	= System.currentTimeMillis();
			
			for( int i = 0; i < numIterations; ++i )
			{
				Object result	= mServer.getAttribute( name, "NotifsEmitted" );
			}
			
			final long	endTime	= System.currentTimeMillis();
			final long	elapsed	= endTime - startTime;
			
			//p( mName + ": " + numIterations / (elapsed / 1000.0) + " iterations/sec" );
			mThroughputCallback.throughputReport( elapsed, numIterations );
		}
	}
	
	class MyFilter implements NotificationFilter, Serializable
	{
		// Object	mHost;	// TestClient
		
		public MyFilter( NotificationTester host )
		{
			// mHost	= host;
		}

	    public boolean isNotificationEnabled(Notification notification)
	    {
	    	return( true );
	    } 
	}
	
	long	mNotifCount	= 0;
	
		public void
    handleNotification(Notification notification, Object handback)
    {
    	++mNotifCount;
    }
    
		public void
	RunNotifTest( long sleepMillis ) throws Exception
	{
             final ObjectName emitterName = new ObjectName(CLISupportStrings.CLI_SIMPLE_TESTEE_TARGET);

             mNotifCount = 0;
             boolean success = false;

             try {
                 p("adding listener");
                 mServer.addNotificationListener(emitterName, this, null, null);
                 p("setting attribute");
                 mServer.setAttribute(emitterName,
                                      new Attribute("NotifMillis",
                                                    Long.valueOf(sleepMillis)));
                 p("starting");
                 mServer.invoke(emitterName, "startNotif", null, null);
                 success = true;
                 p("started");
             }
             catch (Exception e) {
                 p("caught exception: " + e);
             }
             long startTime = System.currentTimeMillis();

             while (true) {
                 Thread.sleep(500);
                 final long notifCount = mNotifCount;
                 final long elapsedMillis = System.currentTimeMillis() -
                                            startTime;
                 final double rate = (double) notifCount /
                                     (elapsedMillis / 1000.0);

                 System.out.println("total notifications: " + notifCount + " = " +
                                    (rate * 10.0) / 10.0 + "/sec");
             }
         }
	
	String	mName;
	int		mOuterLoop;
	int		mNumIterations;
	
		public void
	run()
	{
		try
		{
			RunMultiplePerConnection( mOuterLoop, mNumIterations );
		}
		catch( Exception e )
		{
			p( e );
		}
	}
	
		public void
	RunMultiplePerConnectionThreaded(
		String					name,
		int						outerLoop,
		int						numIterations,
		ThroughputCallback		throughputCallback) throws Exception
	{
		mName				= name;
		mOuterLoop			= outerLoop;
		mNumIterations		= numIterations;
		mThroughputCallback	= throughputCallback;
		
		this.start();
	}


};


