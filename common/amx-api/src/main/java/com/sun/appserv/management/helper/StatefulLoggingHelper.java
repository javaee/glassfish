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
import java.util.Collections;

import java.io.Serializable;


import javax.management.Attribute;

import com.sun.appserv.management.ext.logging.Logging;
import static com.sun.appserv.management.ext.logging.Logging.*;
import com.sun.appserv.management.ext.logging.LogQuery;
import com.sun.appserv.management.ext.logging.LogModuleNames;
import com.sun.appserv.management.ext.logging.LogQueryResult;
import com.sun.appserv.management.ext.logging.LogQueryEntry;
import com.sun.appserv.management.ext.logging.LogQueryResultImpl;

import com.sun.appserv.management.util.misc.GSetUtil;




/**
	Helper class for simplifying querying the log files.
	Unlike {@link LoggingHelper}, state is maintained from
	call-to-call, which may be helpful in performing
	repeated queries.
	<p>
	Some combinations of parameters may not be useful; it is
	up to the caller to ensure reasonable parameters.
	<p>
	A typical use for this helper would be periodic queries
	for new log records and/or retrieving log records in
	batches.
	
	@since AppServer 9.0
	@see LoggingHelper
 */
public final class StatefulLoggingHelper extends Helper
{
    private Logging mLogging;
    
    private String      mLogFile;
    private long        mStartIndex;
    private Set<String> mModules;
    private boolean     mSearchForward;
    private int         mMaxRecords;
    private Long        mStartTime;
    private Long        mStopTime;
    private String      mLogLevel;
    private List<Attribute> mAttrs;
    
    /**
        Create with default parameters.
     */
		public
	StatefulLoggingHelper( final Logging logging  )
	{
		super( logging.getDomainRoot() );
		mLogging    = logging;
		
		mLogFile     = MOST_RECENT_NAME;
		mStartIndex  = FIRST_RECORD;
		mSearchForward  = true;
		mLogLevel       = LOWEST_SUPPORTED_QUERY_LEVEL;
		mModules     = LogModuleNames.ALL_NAMES;
		mMaxRecords  = ALL_RECORDS;
		mStartTime   = null;
		mStopTime    = null;
		mAttrs       = new ArrayList<Attribute>();
	}
	
	    public Logging
	getLogging()
	{
	    return mLogging;
	}
	
	public String   getLogFile()        { return mLogFile; }
	public long      getStartIndex()     { return mStartIndex; }
	public Set<String>      getModules()   { return mModules; }
	public List<Attribute>  getAttrs()  { return mAttrs; }
	public boolean  getSearchForward()  { return mSearchForward; }
	public long    getStartTime()         { return mStartTime; }
	public long    getStopTime()          { return mStopTime; }
	public String  getLogLevel()          { return mLogLevel; }
	public int     getMaxRecords()        { return mMaxRecords; }
	
	
	/**
	    If the specified log file is different, the startIndex
	    is reset appropriately.
	 */
	    public void
	setLogFile( final String name )
	{
	    if ( ! mLogFile.equals( name ) )
	    {
	        mLogFile    = name;
	        setStartIndex( getSearchForward() ? FIRST_RECORD : LAST_RECORD );
	    }

	}
	
	    public void
	setStartIndex( final int startIndex )
	{
	    mStartIndex    = startIndex;
	}
	
	    public void
	setLogLevel( final String logLevel )
	{
	    mLogLevel  = logLevel;
	}
	
	    public void
	setMaxRecords( final int maxRecords )
	{
	    mMaxRecords = maxRecords;
	}
	
	
	    public void
	setModules( final Set<String> modules )
	{
	    mModules    = modules;
	}
	
	    public void
	setModule( final String module )
	{
	    mModules    = GSetUtil.newSet( module );
	}
	
	    public void
	setAttrs( final List<Attribute> attrs )
	{
	    mAttrs.clear();
	    mAttrs.addAll( attrs );
	}
	
	    public void
	setSearchForward( final boolean searchForward )
	{
	    mSearchForward  = true;
	}
	
	    public void
	setStartTime( final Long startTime )
	{
	    mStartTime  = startTime;
	}
	
	    public void
	setStopTime( final Long stopTime )
	{
	    mStopTime  = stopTime;
	}
	
	/**
	    Query for LogRecords based upon the current settings.
	    The startIndex is updated appropriately following the query,
	    depending on the search direction.  A subsequent
	    query will begin at the next available index.
	 */
	    public LogQueryResult
	query()
	{
	    final Logging   logging = getLogging();
	    assert( logging != null );
	    
        final LogQueryResult result = logging.queryServerLog(
            mLogFile,
            mStartIndex,
            mSearchForward,
            mMaxRecords,
            mStartTime,
            mStopTime,
            mLogLevel,
            mModules,
            mAttrs );
       
       final LogQueryEntry[]    entries = result.getEntries();
       if ( entries.length != 0 )
       {
            // update start index
            if ( mSearchForward )
            {
                mStartIndex    = entries[ entries.length - 1 ].getRecordNumber() + 1;
            }
            else
            {
                mStartIndex = entries[ 0 ].getRecordNumber();
            }
       }
       
       return result;
	}
}





























