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
package org.glassfish.admin.amx.impl.loader;

import org.glassfish.admin.amx.util.ExceptionUtil;

import javax.management.ObjectName;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;
	
final class LoaderRegThread extends Thread
{
    private static final class QueueItem
    {
        public final ObjectName mObjectName;
        public final boolean    mRegister;
        
        QueueItem( final ObjectName item, final boolean register )
        {
            mObjectName = item;
            mRegister   = register;
        }
    }
    
	private volatile boolean               mQuit;
	private final ArrayBlockingQueue<QueueItem>	mQueue;
	private final Logger                    mLogger;
	private final LoaderRegHandler          mRegHandler;
	
		public
	LoaderRegThread(
		final LoaderRegHandler	regHandler,
		final Logger	logger )
	{
		mRegHandler	= regHandler;
		mLogger	= logger;
		mQuit	= false;

        final int maxElements = 512;
		mQueue		= new ArrayBlockingQueue<QueueItem>( maxElements );
	}
	
		private final Logger
	getLogger( )
	{
		return( mLogger );
	}
	
		private final void
	trace( final Object o )
	{
		if ( mLogger != null )
		{
			mLogger.finer( o.toString() );
		}
	}

		public void
	quit()
	{
		mQuit	= true;
		synchronized( this )
		{
			this.notify();
		}
	}

		public void
	enqueue(
		final boolean           register,
		final List<ObjectName>	candidates )
	{
        for( final ObjectName objectName : candidates )
		{
			enqueue( register, objectName );
		}
	}
	
		public void
	enqueue(
		final boolean		register,
		final ObjectName	theObject )
	{
        //debug( "ENQUEUE: " + (register? "register" : "unregister") + " : " + JMXUtil.toString(theObject) );
        try
        {
            mQueue.put( new QueueItem( theObject, register ) );
        }
        catch( InterruptedException e )
        {
            mQuit   = true;
        }
	}

		public void
	run()
	{
		mQuit	= false;
		
		while ( ! mQuit )
		{
			try
            {
                process();
            }
            catch( InterruptedException e )
            {
                mQuit   = true;
            }
		}
	}
	
    
		protected boolean
	mySleep( final long millis )
	{
		boolean	interrupted	= false;
		
		try
		{
			Thread.sleep( millis );
		}
		catch( InterruptedException e )
		{
			Thread.interrupted();
			interrupted	= true;
		}
		
		return interrupted;
	}

    /** to support waitAll()--a best effort indicator */
    private volatile QueueItem   mInProgress = null;
    
    /**
        Wait for all outstanding requests to be finished.
        A best-effort method; not a 100% guarantee, but that's also
        true if something is queued right after the call!
     */
		public void
	waitAll()
	{
        while ( mQueue.size() != 0 || mInProgress != null )
        {
			mySleep( 100 );
        }
	}

		private void
	processRegistration( final ObjectName objectName )
	{
		try
		{
			mRegHandler.handleMBeanRegistered( objectName );
			getLogger().finer( "LoaderRegThread.processRegistration: processed mbean: " + objectName );
		}
		catch( Throwable t )
		{
			getLogger().warning( "LoaderRegThread.processRegistration: " +
				"registration of MBean failed for: " + 
				objectName + " = " + t.toString() + ", " + t.getMessage() + "\n" +
				ExceptionUtil.getStackTrace( t ) );
		}
	}
	
		private void
	processUnregistration( final ObjectName objectName )
	{
		try
		{
			mRegHandler.handleMBeanUnregistered( objectName );
		}
		catch( Throwable t )
		{
			getLogger().warning( "LoaderRegThread.processUnregistration: " +
				"unregistration of MBean failed for: " + 
				objectName + " = " + t.toString() );
		}
	}
        
        public boolean
    isQueueEmpty()
    {
        return mQueue.size() == 0;
    }
	
		private void
	process()
        throws InterruptedException
	{
		while ( ! mQuit  )
		{
            mInProgress = mQueue.take();
            if ( mInProgress == null )
            {
                mQuit   = true; // a null item signals that we should quit
                break;
            }
            
            //debug( "PROCESS: " + (item.mRegister? "register" : "unregister") + " : " + JMXUtil.toString(item.mObjectName) );
            
            if ( mInProgress.mRegister )
            {
                processRegistration( mInProgress.mObjectName );
            }
            else
            {
                processUnregistration( mInProgress.mObjectName );
            }
            mInProgress = null;
		}
	}
}












