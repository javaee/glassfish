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
 
package com.sun.enterprise.management.ext.logging;

import java.io.IOException;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.Date;
import java.util.logging.Level;
import java.lang.reflect.Method;

import java.io.Serializable;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.MBeanNotificationInfo;
import javax.management.ListenerNotFoundException;
import javax.management.JMException;
import javax.management.ObjectName;
import javax.management.MBeanInfo;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanAttributeInfo;


import com.sun.appserv.management.base.Util;

import com.sun.appserv.management.ext.logging.LogRecordFields;
import com.sun.appserv.management.ext.logging.LogQueryResult;
import com.sun.appserv.management.ext.logging.LogQueryResultImpl;
import com.sun.appserv.management.ext.logging.LogQueryEntry;
import com.sun.appserv.management.ext.logging.LogQueryEntryImpl;

import com.sun.appserv.management.util.stringifier.ArrayStringifier;
import com.sun.appserv.management.util.jmx.stringifier.AttributeStringifier;
import com.sun.appserv.management.util.jmx.stringifier.AttributeListStringifier;
import com.sun.appserv.management.util.misc.TypeCast;


import com.sun.enterprise.management.AMXTestBase;
import com.sun.enterprise.management.Capabilities;

import com.sun.enterprise.management.ext.logging.LogMBeanIntf;

/**
    Test the LogMBean. It is not part of AMX, but we rely on it closely,
    so ensure that it works.
 */
public final class LogMBeanTest extends AMXTestBase
{
    private LogMBeanIntf    mLogMBean;
    final ObjectName    mTarget;
    
		public
	LogMBeanTest( )
	{
	    mTarget  =
            Util.newObjectName( "com.sun.appserv:name=logmanager,category=runtime,server=server" );

	    mLogMBean   = initLogMBean();
	}
	
	    public static Capabilities
	getCapabilities()
	{
	    return getOfflineCapableCapabilities( false );
	}
	
	    private void
	addListener( final LogMBeanIntf logMBean )
	{
	    final MyLogMBeanListener listener = new MyLogMBeanListener();
	    final NotificationFilter filter = null;
	    logMBean.addNotificationListener( listener, filter, null );
	}
	
	    public LogMBeanIntf
	initLogMBean()
	{
	    final LogMBeanIntf  logMBean	= (LogMBeanIntf)
		    MBeanServerInvocationHandler.newProxyInstance(
			    getConnection(), mTarget, LogMBeanIntf.class, true );
	    
	    return logMBean;
	}
	
	    public LogMBeanIntf
	getLogMBean()
	{
	    return mLogMBean;
	}
	
	
	    public void
	testGetArchivedLogFiles()
	{
	    final String[]  names   = getLogMBean().getArchivedLogfiles();
	    //trace( "getArchivedLogFiles: " + ArrayStringifier.stringify( names, "\n") );
	}
	
	
	    public void
	testGetLogFilesDirectory()
	{
	    final String  name   = getLogMBean().getLogFilesDirectory();
	    //trace( "getLogFilesDirectory: " + name );
	}
	
	    public void
	testGetLoggerNames()
	{
	    final List<String> names   = TypeCast.asList( getLogMBean().getLoggerNames() );
	    TypeCast.checkList( names, String.class );
	    //System.out.println( "getLogFilesDirectory: " + names );
	    
	    for( final String name : names )
	    {
	        final String level  = getLogMBean().getLogLevel( name );
	        Level.parse( level );
	        
	        final List<String>  unders = TypeCast.asList( getLogMBean().getLoggerNamesUnder( name ) );
	        TypeCast.checkList( unders, String.class );
	    }
	}
	
	    private void
	displayAttribute( final Attribute attr )
	{
	    trace( "Attribute: " + attr.getName() );
	    final Object    value   = attr.getValue();
	    trace( "    Value: " + (value == null ? "null" : value.getClass().getName()) );
	}
	
	     private LogQueryResult
	convertQueryResult( final AttributeList queryResult )
	{
	    // extract field descriptions into a String[]
	    final AttributeList   fieldAttrs    = (AttributeList)((Attribute)queryResult.get( 0 )).getValue();
	    final String[]  fieldHeaders  = new String[ fieldAttrs.size() ];
	    assert( fieldHeaders.length == LogRecordFields.NUM_FIELDS );
	    for( int i = 0; i < fieldHeaders.length; ++i )
	    {
	        final Attribute attr    = (Attribute)fieldAttrs.get( i );
	        fieldHeaders[ i ] = (String)attr.getValue();
	        //System.out.println( fieldHeaders[ i ] );
	    }
	    
	    // extract every record
	    final List<List<Serializable>> records    =
	        TypeCast.asList( ((Attribute)queryResult.get( 1 )).getValue() );
	    final LogQueryEntry[]  entries = new LogQueryEntry[ records.size() ];
	    for( int recordIdx = 0; recordIdx < records.size(); ++recordIdx )
	    {
	        final List<Serializable> record    = records.get( recordIdx );
	        TypeCast.checkList( record, Serializable.class );
	        
	        assert( record.size() == fieldHeaders.length );
	        final Serializable[]  fieldValues = new Serializable[ fieldHeaders.length ];
	        for( int fieldIdx = 0; fieldIdx < fieldValues.length; ++fieldIdx )
	        {
	            fieldValues[ fieldIdx ] = record.get( fieldIdx );
	        }
	        
	        entries[ recordIdx ]    = new LogQueryEntryImpl( fieldValues );
	    }
	    
	    return new LogQueryResultImpl( fieldHeaders, entries );
	}
	
	    public void
	testQuery()
	{
	    final String    filename    = "server.log";
	    final Boolean   searchForward   = Boolean.TRUE;
	    final Boolean   sortAscending   = Boolean.TRUE;
	    final int       startRecord = 0;
	    final int       requestedCount  = 1000 * 1000;
	    final Date      fromDate    = null;
	    final Date      toDate    = null;
	    final List<Object>      listOfModules   = null;
	    final Boolean   levelOnly   = Boolean.FALSE;
	    final Properties    props   = null;
	    
	    final AttributeList attrs   = getLogMBean().getLogRecordsUsingQuery(
	        filename,
	        new Long( startRecord ),
	        searchForward,
	        sortAscending,
	        new Integer( requestedCount ),
	        fromDate,
	        toDate,
	        Level.WARNING.toString(),
	        levelOnly,
	        listOfModules,
	        props );
	   
	   final LogQueryResult result  = convertQueryResult( attrs );
	   
	   final LogQueryEntry[]    entries = result.getEntries();
	   assert( entries.length != 0 );
	   
	   for( final LogQueryEntry entry : entries )
	   {
	        final String    messageID   = entry.getMessageID();
	        
	        final ArrayList  causes  = getLogMBean().getDiagnosticCausesForMessageId( messageID );
	        //trace( causes );
	        final ArrayList  checks  = getLogMBean().getDiagnosticChecksForMessageId( messageID );
	        //trace( checks );
	        final String    uri  = getLogMBean().getDiagnosticURIForMessageId( messageID );
	        //trace( uri );
	   }
	   
	  // trace( result );
	}
	
	
	private final class MyLogMBeanListener implements NotificationListener
	{
		public MyLogMBeanListener()	{}
		
			public void
		handleNotification(
			final Notification notif,
			final Object	   handback )
		{
			trace( "LoggingImpl.java: received Notification: " + notif );
		}
	}
	
}





















