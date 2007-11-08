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
package com.sun.appserv.management.helper;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import java.util.logging.Level;

import javax.management.Attribute;

import com.sun.appserv.management.ext.logging.Logging;
import static com.sun.appserv.management.ext.logging.Logging.*;
import com.sun.appserv.management.ext.logging.LogQuery;
import com.sun.appserv.management.ext.logging.LogModuleNames;
import com.sun.appserv.management.ext.logging.LogQueryResult;
import com.sun.appserv.management.ext.logging.LogQueryResultImpl;

import com.sun.appserv.management.util.misc.GSetUtil;


/**
	Helper class for simplifying access to logging.
	@since AppServer 9.0
 */
public final class LoggingHelper extends Helper
{
    private final Logging   mLogging;
    
    /**
        Create with default parameters.
     */
		public
	LoggingHelper( final Logging logging  )
	{
		super( logging.getDomainRoot() );
		mLogging    = logging;
	}
	
	    public Logging
	getLogging()
	{
	    return mLogging;
	}
	
    
    /**
        Get all log records of the specified error level or higher level for a
        all specified modules in the most current log file.
        
        @see LogQuery
        @see LogModuleNames
     */
        public LogQueryResult
    queryServerLog(
        final String      logLevel,
        final Set<String> modules)
    {
        final String    name            = MOST_RECENT_NAME;
        final int       startIndex      = FIRST_RECORD;
        final boolean   searchForward   = true;
        final int       maxRecords      = ALL_RECORDS;
        final Long      startTime       = null;
        final Long      stopTime        = null;
        final List<Attribute>  attrs    = null;
        
        assert( getLogging() != null );
        
        final LogQueryResult result = getLogging().queryServerLog(
            name,
            startIndex,
            searchForward,
            maxRecords,
            startTime,
            stopTime,
            logLevel,
            modules,
            attrs );
        
        return result;
    }
    
    /**
        Get all log records of the specified error level or higher level for a
        particular module in the most current log file.
        
        @see LogQuery
     */
        public LogQueryResult
    queryServerLog(
        final String logLevel,
        final String moduleID)
    {
        return queryServerLog( logLevel, GSetUtil.newSet( moduleID ) );
    }
    
    /**
        Get all log records of the specified error level or higher level for
        all modules in the most current log file.
        @see LogQuery
     */
        public LogQueryResult
    queryServerLog( final String logLevel )
    {
        return queryServerLog( logLevel, LogModuleNames.ALL_NAMES );
    }
    
    
    /**
        Get all available log records for all modules in the most current log file.
        @see LogQuery
     */
        public LogQueryResult
    queryAllCurrent()
    {
        return queryAllInFile( MOST_RECENT_NAME );
    }
    
    
    
    
        private long
    now()
    {
        return System.currentTimeMillis();
    }
    
    /**
        Get all available log records for all modules in the most current log file
        which have occurred within the last number of seconds.
        
        @param seconds
        @see LogQuery
     */
        public LogQueryResult
    queryServerLogRecent( final long seconds )
    {
        return queryServerLogRecent( seconds, LogModuleNames.ALL_NAMES );
    }
    
    /**
        Get all available log records for all modules in the most current log file
        which have occurred within the last number of seconds.
        
        @param seconds
        @see LogQuery
     */
        public LogQueryResult
    queryServerLogRecent(
        final long          seconds,
        final Set<String>   modules )
    {
        final String    name            = MOST_RECENT_NAME;
        final int       startIndex      = LAST_RECORD;
        final boolean   searchForward   = false;
        final int       maxRecords      = ALL_RECORDS;
        final Long      startTime       = now();
        final Long      stopTime        = now() - (seconds * 1000);
        final List<Attribute>  attrs    = null;
        
        final LogQueryResult result = getLogging().queryServerLog(
            name,
            startIndex,
            searchForward,
            maxRecords,
            startTime,
            stopTime,
            LOWEST_SUPPORTED_QUERY_LEVEL,
            modules,
            attrs );
        return result;
    }
	
	
	/**
        Get all available log records for all modules in the specified log file.
        @see LogQuery
     */
        public LogQueryResult
    queryAllInFile( final String name )
    {
        final int       startIndex      = FIRST_RECORD;
        final boolean   searchForward   = true;
        final int       maxRecords      = ALL_RECORDS;
        final Long      startTime       = null;
        final Long      stopTime        = null;
        final List<Attribute>  attrs    = null;
        
        final LogQueryResult result = getLogging().queryServerLog(
            name,
            startIndex,
            searchForward,
            maxRecords,
            startTime,
            stopTime,
            LOWEST_SUPPORTED_QUERY_LEVEL,
            LogModuleNames.ALL_NAMES,
            attrs );
        
        return result;
    }
    
    /**
        Get all available log records in <i>all files</i> for all modules of all
        available log levels.
        
        @see LogQuery
     */
	    public LogQueryResult[]
	queryAll()
	{
	    final String[]  names    = getLogging().getLogFileNames( SERVER_KEY );
	    
	    final List<LogQueryResult>  all = new ArrayList<LogQueryResult>( names.length );
	    for( final String name : names )
	    {
	        // a log file could disappear while querying
	        try
	        {
	            final LogQueryResult result   = queryAllInFile( name );
	            all.add( result );
	        }
	        catch( Exception e )
	        {
	            // ignore and try next one.
	        }
	    }
	    
	    final LogQueryResult[]  results = new LogQueryResultImpl[ all.size() ];
	    return all.toArray( results );
	}
}





























