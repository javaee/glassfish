/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import java.io.File;

import org.glassfish.admin.amx.util.FileOutput;
import org.glassfish.admin.amx.util.Output;
import org.glassfish.admin.amx.util.OutputIgnore;
import org.glassfish.admin.amx.util.SetUtil;
import org.glassfish.admin.amx.util.StringUtil;
import org.glassfish.admin.amx.util.DebugOutImpl;


/**
	Internal debug facility.  For development use only.
	
	<p>
	<b>Usage notes</b>
	Debug associates a file with each identifier (typically a classname).
	These files are located within the {@link #DEBUG_SUBDIR}
	subdirectory within the directory specified by
	System.getProperty( "user.home" ) unless the system property
	{@link #DEBUG_DIR_SPROP} is specified.
	All resulting Debug output files
	end in the suffix {@link #DEBUG_SUFFIX}.
	<p>
	This fine-grained approach makes it possible to "tail" just the
	output from just the classes of interest,
	something that is difficult or impossible otherwise.
	<p>
	Debug is designed as a singleton. However, arbitrary identifiers
	may be used to associate debugging output with a particular
	output file.  This allows fine-grained and selective debugging
	of just the items of interest.
	<p>
	When debugging is off, overhead is minimal, because all debugging
	calls are routed to "dev/null".  The caller can also wrap
	such calls such that they don't make it to Debug at all.
	<p>
	The debug flag may be set via the system property
	{@link #DEBUG_ENABLED_SPROP}.  Debugging will be enabled if that
	property has value "true".  Otherwise, it is disabled.
	Debugging may also be programmatically enabled, on a per-ID
	basis.
	<p>
	The expected usage is per-class and the classname can generally
	be used as the identifier.  However, usage include other
	patterns; anything that the emitting code can agree on, regardless
	of whether it is in the same class, or spread across many.  One
	possibility would be to place the Output into the thread context.
	There are other possibilities.
	<p>
	Output may be marked using the {@link #mark} and {@link #markAll} routines.
	This aids in visually organizing the output.
	<p>
	<b>For more information, see the javadoc on individual routines.</b>
	
 */
public final class JCMDDebug
{
    private final Map<String,WrapOutput>    mOutputs;
    
    private static final JCMDDebug   INSTANCE    = new JCMDDebug();
    
    private final File  mDir;
    private boolean     mMadeDebugDir;
    private boolean     mDefaultDebug;
    private final boolean     mAppend;
    
    /** the key for the system property to enable AMX debug facility */
    public static final String  DEBUG_ENABLED_SPROP   = "JCMD-DEBUG.enabled";
    
    /** the key for the system property to append to debug files.
        Otherwise they are overwritten each time
      */
    public static final String  DEBUG_APPEND_SPROP   = "JCMD-DEBUG.append";
    
    /**
        The key for the system property to specify a different DEBUG_DIR.
        This value is uninterpreted--the result from
        new File( System.getProperty( {@link #DEBUG_SUBDIR} ) is used directly.
        <p>
        If the sytem property {@link #DEBUG_SUBDIR} is not specified,
        then Debug looks for the system property
        "com.sun.aas.instanceRoot". If that system property
        is not found, then "user.home" is used.  The result of this is
        the "parent dir".  The resulting output
        directory is then <parent-dir>/{@link #DEBUG_SUBDIR}.
      */
    public static final String  DEBUG_DIR_SPROP   = "JCMD-DEBUG.dir";
    
    /**
        The name of the default subdirectory which contains
        the ".debug" files created by Debug.  This is the directory
        used if {@link #DEBUG_DIR_SPROP} is not specified.
      */
    public static final String  DEBUG_SUBDIR   = "JCMD-DEBUG";
    
    /**
        Suffix used on all Output files.
     */
    public static final String  DEBUG_SUFFIX   = ".debug";
    
    // Output for Debug itself
    private final WrapOutput    mDebug;
    
    private final String NEWLINE;
    private final Set<Character>    ILLEGAL_CHARS;
    
    private final char[]    ILLEGAL_CHARS_ARRAY    =
    {
        '\u0000',
        '?', '*', '|', '\'', '|', '\\', '/', ':',
    };
    
        private
    JCMDDebug()
    {
        ILLEGAL_CHARS   = new HashSet<Character>();
        for( final char c : ILLEGAL_CHARS_ARRAY )
        {
            ILLEGAL_CHARS.add( c );
        }
        
        NEWLINE =  System.getProperty( "line.separator" );
        assert( NEWLINE != null );
        
	    String value   = System.getProperty( DEBUG_ENABLED_SPROP );
	    if ( value == null )
	    {
	        // not the right one, but a common mistake.
	        value   = System.getProperty( "AMX-DEBUG" );
	        if ( value != null && value.equals( "" ) )
	        {
	            value   = "true";
	        }
	    }
	    mDefaultDebug  = (value != null) && Boolean.parseBoolean( value );
	    
	    value   = System.getProperty( DEBUG_APPEND_SPROP );
	    mAppend  = (value != null) && Boolean.parseBoolean( value );
	    
        mOutputs    = new HashMap<String,WrapOutput>();
        
        mDir    = getDir();
        mMadeDebugDir   = false;
        
        if ( mDefaultDebug )
        {
            makeDebugDir();
        }
        
        mDebug = _getOutput( this.getClass().getName() );
        mark( mDebug, getStdMarker( "Debug started " ) );
        mDebug.println( "*** System Properties ***" );
        dumpSystemProps( mDebug );
        
        mark( mDebug, getStdMarker( "Debug initialization done" ) );
    }
    
        private void
    dumpSystemProps( final Output output )
    {
        final java.util.Properties    props   = System.getProperties();
        
        final String[]  keys    = (String[])props.keySet().toArray( new String[0] );
        java.util.Arrays.sort( keys );
        for( final String key : keys )
        {
            debug( key + "=" + props.getProperty( key ) );
        }
        
    }
    
        private void
    makeDebugDir()
    {
       if ( ! mMadeDebugDir )
       {
            mDir.mkdirs();
            mMadeDebugDir   = true;
       }
    }

        private void
    debug( final String s )
    {
        // we don't use debug() because we don't want/need the "DEBUG:" prefix
        
        if ( mDefaultDebug && mDebug != null )
        {
            mDebug.println( "" + s );
        }
    }
    
        private static String
    parens( final String s )
    {
        return "(" + s + ")";
    }
    
        private File
    getDir()
    {
	    final String value   = System.getProperty( DEBUG_DIR_SPROP );
	    
	    File debugDir  = null;
	    
	    
	    if ( value == null )
	    {
	        final String  instanceRoot  = System.getProperty( "com.sun.aas.instanceRoot" );
	        
	        File parentDir  = null;
	        if ( instanceRoot != null )
	        {
	            parentDir   = new File( instanceRoot );
	        }
	        else
	        {
    	        parentDir  = new File( System.getProperty( "user.home" ) );
	        }
    	    debugDir   = new File( parentDir, DEBUG_SUBDIR );
	    }
	    else
	    {
	        debugDir   = new File( value );
	    }
        
        return debugDir;
    }
    
        public String[]
    getOutputIDs()
    {
        return SetUtil.toStringArray( mOutputs.keySet() );
    }
    
    /**
        Get the current default debug state used when any
        new Outputs are created.
     */
        public boolean
    getDefaultDebug()
    {
        return mDefaultDebug;
    }
    
    /**
        Set the current default debug state.  Existing outputs
        are not affected.
        @see #setAll
     */
        public void
    setDefaultDebug( final boolean debug )
    {
        mDefaultDebug   = debug;
        mDebug.setDebug( debug );
        debug( "setDefaultDebug" + parens( "" + debug ) );
    }
    
    /**
        Get the debug state of a particular Output for the
        specified ID.
     */
        public boolean
    getDebug( final String id )
    {
        return _getOutput( id ).getDebug();
    }
    
    /**
        Set the debug state of a particular Output for the
        specified ID.  If the Output currently maintains
        an open file, and debug is false, the file is closed.
     */
        public void
    setDebug( final String id, final boolean debug)
    {
        if ( debug )
        {
            makeDebugDir();
        }
        _getOutput( id ).setDebug( debug );
        debug( "setDebug" + parens( id + ", " + debug ) );
    }
    
    /**
        Set the debug state of all Outputs.
        @see #setDebug
     */
        public void
    setAll( final boolean debug )
    {
        debug( "setAll" + parens( "" + debug ) );
        
        setDefaultDebug( debug );
        for( final WrapOutput w : mOutputs.values() )
        {
            w.setDebug( debug );
        }
    }
    
    /**
        Turn off all debugging and close all files.
     */
        public void
    cleanup()
    {
        debug( "cleanup()" );
        
        setDefaultDebug( false );
        setAll( false );
    }
    
    /**
        Turn off debugging any close the associated file (if any)
        for the Output specified by 'id'.
     */
        public void
    reset( final String id )
    {
        debug( "reset" + parens(id) );
        _getOutput(id).reset();
    }
    
    
    private static final String DASHES = "----------";
    
    /**
        The standard marker line emitted by {@link #mark}.
     */
        public String
    getStdMarker()
    {
        return getStdMarker( "" );
    }
    
    /**
        The standard marker line emitted by {@link #mark} with a message
        inserted.
     */
        public String
    getStdMarker( final String msg)
    {
        return( NEWLINE + NEWLINE +
            DASHES + " " + new java.util.Date() + " " + msg + DASHES + NEWLINE );
    }
    
    /**
        Output a marker into the Output. If 'marker' is null, then
        the std marker is emitted.
     */
        public void
    mark( final Output output, final String marker )
    {
        output.println( marker == null ? getStdMarker() : marker );
    }
    
    /**
        Output a marker into the Output associated with 'id'.
     */
        public void
    mark( final String id, final String marker )
    {
        mark( getOutput(id), marker );
    }
    
    /**
        Output a standard marker into the Output.
     */
        public void
    mark( final String id )
    {
        mark( id, null );
    }
    
    /**
        Output a standard marker into the Output.
     */
        public void
    markAll( final String marker )
    {
        for( final WrapOutput w : mOutputs.values() )
        {
            if ( w.getDebug() ) // optimization for debug=false
            {
                mark( w, marker );
            }
        }
    }
    
    /**
        Output a standard marker into the Output.
     */
        public void
    markAll()
    {
        markAll( null );
    }
    
    /**
        Get the Singletone Debug instance.
     */
        public static JCMDDebug
    getInstance()
    {
        return INSTANCE;
    }
    
    /**
        Get the File associated with 'id'.  The file may or
        may not exist, and may or may not be open, depending
        on whether debug is enabled and whether anything was
        written to the file.
     */
        public File
    getOutputFile( final String id )
    {
        final String    filename    = makeSafeForFile( id ) + DEBUG_SUFFIX;
        
        return new java.io.File( mDir, filename );
    }
    
        private WrapOutput
    _getOutput( final String id )
    {
        WrapOutput  output  = mOutputs.get( id );
        if ( output == null )
        {
            synchronized( this )
            {
                if ( mOutputs.get( id ) == null ) 
                {
                    debug( "Creating output for " + StringUtil.quote( id ) );
                    try
                    {
                        output  = new WrapOutput( getOutputFile( id ), mDefaultDebug );
                        mOutputs.put( id, output );
                    }
                    catch( Throwable t )
                    {
                        debug( "Couldn't create output for " + StringUtil.quote( id ) );
                    }
                }
            }
        }
        
        return output;
    }
    
        public Output
    getShared()
    {
        return getOutput( "Debug-Shared" );
    }
    
    /**
        ID is typically a classname, but may be
        anything which can be used for a filename.  The
        id will be used to create file <id>.debug in the
        {@link #DEBUG_SUBDIR} directory.
     */
        public Output
    getOutput( final String id )
    {
        return _getOutput( id );
    }
        
        
    /**
        Get a form of the ID that is safe to for a filename.
     */
        private String
    makeSafeForFile( final String id )
    {
        if ( id == null )
        {
            throw new IllegalArgumentException( id );
        }

        final StringBuilder  s   = new StringBuilder();
        
        final char[]    chars   = id.toCharArray();
        for( final char c : chars )
        {
            if (  ILLEGAL_CHARS.contains( c ) )
            {
                s.append( "_" );
            }
            else
            {
                s.append( c );
            }
        }
        
        return s.toString();
    }
    
    /**
        Internal class which wraps the Output so that
        debug may be dynamically enabled or disable without any
        users of the Output having to be aware of it.  
     */
    public final class WrapOutput implements Output
    {
        private Output  mWrapped;
        private File    mFile;
        private Output  mFileOutput;
        private boolean mDebug;
        
    		private
    	WrapOutput( final File file, final boolean debug )
    	{
    		mDebug      = debug;
    		mWrapped    = OutputIgnore.INSTANCE;
    	    mFile       = file;
    		mFileOutput = new FileOutput( file, mAppend);
    		checkStatus();
    	}
    	    public boolean
    	getDebug()
    	{
    	    return mDebug;
    	}
    
        /**
            Change debug status.  If debug is <i>enabled</i> any 
            subsequent debugging messages will be written to their outputs,
            creating files if necessary.
            If debug is <i>disabled</i>, all output to files ceases, and
            the files are closed.
         */
            public void
        setDebug( final boolean debug )
        {
            mDebug  = debug;
            
            checkStatus();
        }
    	
    		public void
    	print( final Object o )
    	{
    	    mWrapped.print( o );
    	}
    	
    		public void
    	println( final Object o )
    	{
    	    mWrapped.println( o );
    	}
    	
    		public void
    	printError( final Object o )
    	{
    	    mWrapped.printError( o );
    	}
    	
    		public void
    	printDebug( final Object o )
    	{
    	    mWrapped.printDebug( o );
    	}
    	
    	    public synchronized void
    	reset()
    	{
    	    mWrapped    = OutputIgnore.INSTANCE;
    	    mFileOutput.close();
    	    
    	    // the new one is lazy-opened...
    	    mFileOutput = new FileOutput( mFile );
    	    
    	    checkStatus();
    	}
    	
    		public void
    	close()
    	{
    	    reset();
    	}
    	
    	/**
    	    Switch between FileOutput and OutputIgnore
    	    if there is a mismatch.
    	 */
    	    private synchronized void
    	checkStatus()
    	{
    	    if ( getDebug() )
    	    {
    	        mWrapped    = mFileOutput;
    	    }
    	    else
    	    {
    	        mWrapped.println( "turning DEBUG OFF" );
    	        mWrapped    = OutputIgnore.INSTANCE;
    	    }
    	}
    }
    
  
        public static String
    methodString( final String name, final Object... args )
    {
        return DebugOutImpl.methodString( name, args );
    }
}












