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
package com.sun.appserv.management.util.misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


//import com.sun.appserv.management.helper.AMXDebugHelper;

/**
    Central registry of timing values. 
 */
public final class Timings
{
    static private final Timings INSTANCE = new Timings( "default" );
    static private final Map<String,Timings>  sInstances  = new HashMap<String,Timings>();
    
    private final List<String>    mCheckpointNames;
    private final List<Long>      mCheckpointTimes;
    
    private final String              mName;
    
    static private final String NEWLINE = System.getProperty( "line.separator" );
    
    /**
        Get the default Timings object. No synchronization happens or is needed; fast.
     */
        public static Timings
    getInstance()
    {
        return INSTANCE;
    }
    
    /**
        Get a named Timings object. Synchronization happens in this call, to access a Map.
     */
       public static Timings
    getInstance( final String name )
    {
        Timings   timings   = null;
        synchronized( sInstances )
        {
            timings = sInstances.get( name );
            if ( timings == null )
            {
                timings = newInstance( name );
                sInstances.put( name, timings );
            }
        }
        return timings;
    }
    
     /**
        Create a  Timings object. It is not retained in  the list of Timings instances.
     */
       public static Timings
    newInstance( final String name )
    {
        return new Timings( name );
    }
    
        public static void
    removeInstance( final String name )
    {   
        synchronized( sInstances )
        {
            sInstances.remove( name );
        }
    }
    
        public
    Timings( final String name )
    {
        mName   = name;
        mCheckpointNames    = new ArrayList<String>();
        mCheckpointTimes    = new ArrayList<Long>();
    }
    
        public String
    getName()
    {
        return mName;
    }
    
    /**
        @param checkpoint the name of the checkpoint
        @param nanoseconds the nanoseconds ascribed to this checkpoint
     */
        public void
    add( final String checkpointName, final long nanoseconds )
    {
        synchronized( mCheckpointNames )
        {
            mCheckpointNames.add( checkpointName );
            mCheckpointTimes.add( nanoseconds );
        }
    }
    
    /**
        Convenience--same as <code>add( checkpoint, delta.elapsed() )</code>;
        @param checkpoint the name of the checkpoint
        @param delta a TimingDelta
     */
        public void
    add( final String checkpointName, final TimingDelta delta )
    {
        add( checkpointName, delta.elapsedMillis() );
    }
    
    /**
        Remove all timings.
     */

        public void
    clear()
    {
        synchronized( mCheckpointNames )
        {
            mCheckpointNames.clear();
        }
    }
    
    
        public String
    toString()
    {
        final StringBuffer buf  = new StringBuffer();
        
        buf.append( "Timing Log " );
        buf.append( StringUtil.quote( getName() ) );
        buf.append( NEWLINE );

        final List<String>    names = new ArrayList<String>();
        final List<Long>      times = new ArrayList<Long>();
        
        synchronized( mCheckpointNames )
        {
            names.addAll( mCheckpointNames );
            times.addAll( mCheckpointTimes );
        }
        
        for( int i = 0; i < names.size(); ++i )
        {
            final String name = names.get(i);
            final long   nanos = times.get(i);
            
            buf.append( name );
            buf.append( " = " );
            buf.append( StringUtil.getMillisString( nanos ) );
            buf.append( NEWLINE );
        }

        return buf.toString();
    }
}

































