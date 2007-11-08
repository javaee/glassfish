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
package com.sun.enterprise.server.logging;

import java.util.logging.LogRecord;
import java.util.logging.Level;
import java.util.logging.Formatter;

import javax.management.ObjectName;
import javax.management.MBeanServer;

import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

import com.sun.enterprise.util.FeatureAvailability;

import com.sun.appserv.management.helper.AMXDebugHelper;

/**
    Hook used for AMX logging by FileandSysLogHandler.
    <b>This class assumes that the caller is holding a lock that
    will prevent more than one thread from calling {@link #publish}.</b>
 */
public final class AMXLoggingHook
{
    private ObjectName          mLoggingObjectName;
    private MBeanServer         mMBeanServer;
    private LoggingImplHook     mLoggingImplHook;
    
    /**
        The minimum log level for which LogRecords will be passed
        to the AMX Logging MBean.  Generally speaking, all LogRecords
        should be passed along, letting the AMX Logging MBean decide
        their disposition.
     */
    private Level               mMinimumLogLevel;
    
    private final AMXDebugHelper    mDebug;
    private final String            mServerName;
    
        private void
    dumpSystemProps( final AMXDebugHelper output )
    {
        final java.util.Properties    props   = System.getProperties();
        
        final String[]  keys    = (String[])props.keySet().toArray( new String[0] );
        java.util.Arrays.sort( keys );
        for( final String key : keys )
        {
            debug( key + "=" + props.getProperty( key ) );
        }
        
    }
    
    private static final AMXLoggingHook INSTANCE = new AMXLoggingHook();
    
        public static AMXLoggingHook
    getInstance()
    {
        return INSTANCE;
    }
    
    AMXLoggingHook()
    {
        mServerName = System.getProperty( com.sun.enterprise.util.SystemPropertyConstants.SERVER_NAME);
        final String  instanceRoot  = System.getProperty(
            com.sun.enterprise.util.SystemPropertyConstants.INSTANCE_ROOT_PROPERTY );
            
        mDebug = new AMXDebugHelper( instanceRoot + "/AMXLoggingHook-" + mServerName + ".debug" );

        // debug( "\n_______________________________________________________________________________");
        // debug( "AMXLoggingHook: REV = 1");
        // debug( "AMXLoggingHook: started at " + new java.util.Date() + " for server " + mServerName);
        
        // dumpSystemProps( mDebug );

        mLoggingObjectName  = null;
        mMinimumLogLevel    = Level.FINEST;
        mLoggingImplHook    = null;
        
        //mDebug.println( com.sun.appserv.management.util.misc.ExceptionUtil.toString( new Exception("HELLO") ) );
        
        // can't initialize now; FileandSysLogHandler is called even before main()
        mMBeanServer = null;
    }
    private final void  debug( final Object o )   { mDebug.println( o ); }

     private static final String  LOGGING_IMPL_CLASSNAME =
        "com.sun.enterprise.management.ext.logging.LoggingImpl";
    
    
    /**
        Called exactly once to install the Logging MBean and turn on AMX support for logging.
        @return ObjectName of AMX Logging MBean
     */
        public static ObjectName
    enableLoggingHook()
    {
        return getInstance()._enableLoggingHook();
    }
    
        private synchronized ObjectName
    _enableLoggingHook()
    {
        debug( "_enableLoggingHook" );
        if ( mLoggingImplHook != null )
        {
            throw new IllegalStateException();
        }
        
        mMBeanServer = FeatureAvailability.getInstance().waitForMBeanServer();
        
        LoggingImplHook hook = null;
        try
        {
            final Class  loggingClass = Class.forName( LOGGING_IMPL_CLASSNAME );
            
            final Constructor constructor = loggingClass.getConstructor( String.class );
            hook = (LoggingImplHook)constructor.newInstance( mServerName );
            
            final Method   getObjectNameMethod =
                loggingClass.getMethod( "getObjectName", String.class );
            final ObjectName proposedObjectName = (ObjectName)getObjectNameMethod.invoke( hook, mServerName );
            debug( "registering Logging as: " + proposedObjectName );
            mLoggingObjectName  = mMBeanServer.registerMBean( hook, proposedObjectName ).getObjectName();
            mLoggingImplHook   = hook;
         }
         catch ( Exception e )
         {
            hook    = null;
            final String msg = "Can't load " + LOGGING_IMPL_CLASSNAME + ", caught: " + e;
            debug( msg );
            throw new Error( msg, e);
         }
         
    debug( "_enableLoggingHook DONE" );
         return mLoggingObjectName;
    }
    
        public Level
    getMinimumLogLevel( )
    {
        return mMinimumLogLevel;
    }
    
        public void
    setMinimumLogLevel( final Level level )
    {
        mMinimumLogLevel    = level;
    }
        
        // call the Logging MBean
        void
    publish( 
        final LogRecord record,
        final Formatter theFormatter )
    {
        if ( record.getLevel().intValue() < mMinimumLogLevel.intValue() )
        {
            return;
        }

        debug( "publish: " + theFormatter.format( record ) );
        if ( mLoggingImplHook != null )
        {
            try
            {
                mLoggingImplHook.privateLoggingHook( record, theFormatter );
            }
            catch( Throwable t )
            {
                mDebug.println( "AMXLoggingHook.publish: Exception calling privateLoggingHook: ", t );
                // squelch--we can't log it or we'll have a recursive call
            }
        }
    }
    

    
    /**
        Maintains an ordered list of LogRecords which
        could not be sent to the not-yet-existing AMX Logging MBean.
    private static final class StartupRecords {
        private final List<LogRecord>  mList;
        
        public StartupRecords() {
            mList   = new ArrayList<LogRecord>();
        }
        
        public void add( final LogRecord r ){mList.add( r );}
        public void clear(){    mList.clear(); }
        public List   getApplications()  { return Collections.unmodifiableList( mList ); }
    };
    
     */
    
    private interface Output
    {
        public void println( Object o );
    }

    /**
    	Directs output to a file; used internally for debugging to avoid
    	infinite recursion between logging and this hook.
     */
    private static final class FileOutput implements Output
    {
        private java.io.PrintStream mOut;
        
        // passing null means output is not emitted
    		public
    	FileOutput( final java.io.File f)
    	    throws java.io.IOException
    	{
    	    mOut    = new java.io.PrintStream( new java.io.FileOutputStream( f ) );
    	}
    	
    		public void
    	println( Object o )
    	{
    	    mOut.println( o.toString() );
    	}
    	
    		public void
    	close( )
    	{
    	    if ( mOut != null )
    	    try
    	    {
    	        mOut.close();
    	    }
    	    finally
    	    {
    	        mOut    = null;
    	    }
    	}
    };
    
    // ignores all output
    private static final class NullOutput implements Output
    {
        NullOutput()    {}
        public void println( Object o ) {}
    }
}




