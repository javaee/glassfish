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
package com.sun.cli.jcmd.util.misc;

/**
 */
public class DebugOutImpl implements DebugOut
{
    private final String    mID;
    private boolean         mDebug;
    private DebugSink       mSink;
    
        public
    DebugOutImpl(
        final String  id,
        final boolean debug,
        final DebugSink sink)
    {
        mID     = id;
        mDebug  = debug;
        
        mSink   = sink == null ? new DebugSinkImpl( System.out ) : sink ;
    }
    
        public
    DebugOutImpl(
        final String  id,
        final boolean debug )
    {
        this( id, debug, null );
    }
    
        public String
    getID()
    {
        return mID;
    }
    
	    public boolean
	getDebug()
	{
	    return mDebug;
	}
	
	    
	    public void
	print( final Object o  )
	{
	    mSink.print( "" + o );
	}
	
	    public void
	println( Object o )
	{
	    mSink.println( "" + o );
	}
	
	    public String
	toString( final Object... args )
	{
	    return StringUtil.toString( ", ", args );
	}
    
	    public void
	setDebug( final boolean debug)
	{
	    mDebug  = debug;
	}
	
	    public void
	debug( final Object... args )
	{
	    if ( getDebug() )
	    {
	        mSink.println( toString( args ) );
	    }
	}
	
		public void
	debugMethod(
	    final String    methodName,
	    final Object... args )
	{
	    if ( getDebug() )
	    {
	        debug( methodString( methodName, args ) );
	    }
	}
	
		public void
	debugMethod(
	    final String msg,
	    final String methodName,
	    final Object... args )
	{
	    if ( getDebug() )
	    {
	        debug( methodString( methodName, args ) + ": " + msg );
	    }
	}
	
  
        public static String
    methodString(
        final String name,
        final Object... args )
    {
        String  result  = null;
        
        if ( args == null || args.length == 0 )
        {
            result  = name + "()";
        }
        else
        {
            final String    argsString  = StringUtil.toString( ", ", args );
            result  = StringUtil.toString( "", name, "(", argsString, ")" );
        }
        
        return result;
    }
}




























