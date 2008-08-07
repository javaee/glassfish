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

import com.sun.appserv.management.helper.AMXDebugHelper;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;


/**
    <b>INTERNAL USE ONLY -- DO NOT USE</b>
    Base class (can be used directly) for running small, short lived tasks.  An ExecutorService
    is used for efficiency, and excess threads are discarded quickly.
    <p>
    Includes convenience routines for submitting tasks, determining result status.
    <p>
    <b>Example (inline usage)</b>
    <pre>
    final RunnableBase myTask = new RunnableBase( "Compute PI" ) {
        public void run() {
            final double pi = 3.141926386; // cheater method
        }
    };
    myTask.submit();
    ...
    // wait for compute task
    myTask.waitDoneThrow(); // or waitDone() followed by myTask.getThrowable()
    </pre>
    <p>
    <b>NOTE: </b>An ExecutorService is used with a thread pool.  Inheritable thread
    local variables will <b>not</b> be inherited.
 */
public abstract class  RunnableBase<T> implements Runnable
{
    /** a Throwable if anything was thrown from the run loop */
    private volatile Throwable      mThrowable;
    
    /** means to block client threads until done */
    private volatile CountDownLatch mLatch;
    
    /** optional name of the task */
    private final String            mName;
    
    /** debugging: whether to sleep a random amount.  See {@link #setUseRandomSleep} */
    private volatile boolean        mUseRandomSleep;
    
    // optional data for use by the task
    private final T                 mData;
    
    private volatile long           mSubmitNanos;
    private volatile long           mRunStartNanos;
    private volatile long           mRunDoneNanos;
    
    private final AMXDebugHelper    mDebug;

    /** must set separate thread's context class loader to the submitter's */
    private volatile ClassLoader             mClassLoader;
    
    /**
        RUN_IN_CURRENT_THREAD:  execute the task synchronously (in calling thread), return when done <br />
        RUN_IN_SEPARATE_THREAD:  execute the task asynchronously (separate thread), return immediately <br />
     */
    public enum HowToRun {
        RUN_INVALID,
        RUN_IN_CURRENT_THREAD,
        RUN_IN_SEPARATE_THREAD,
    };
    
    private static final AtomicInteger mThreadsRunning = new AtomicInteger(0);
    
        private void
    debug( final Object... args)
    {
        if ( mDebug.getDebug() )
        {
            mDebug.println( args );
        }
    }
        private static ExecutorService
    createExecutorService()
    {
        // Testing at startup shows that a thread pool equal in size to the number
        // of processors offers the best performance.  However, this can 'hang'
        // services that expect their threads to run once submitted.
        return Executors.newCachedThreadPool();
    }
    
    private static final ExecutorService   _DefaultExecutorService = createExecutorService();
    
        public static ExecutorService
    getDefaultExecutorService()
    {
        return _DefaultExecutorService;
    }
    
    /**
        Run in a separate thread.  Calls getExecutorService().submit(r).
        Subclasses may override if desired.
     */
        protected void
    runInSeparateThread( final Runnable r )
    {
        getExecutorService().submit( r );
    }
    
    /**
        Subclasses may override the choice of ExecutorService
     */
        protected ExecutorService
    getExecutorService()
    {
        return getDefaultExecutorService();
    }
    
        private void
    _submit( final HowToRun howToRun )
    {
        // Save submitter's class loader so it can be set, if needed, in a 
        // different thread when the task is run.
        mClassLoader = Thread.currentThread().getContextClassLoader();

        if ( howToRun != HowToRun.RUN_IN_CURRENT_THREAD && howToRun != HowToRun.RUN_IN_SEPARATE_THREAD )
        {
            throw new IllegalArgumentException();
        }
        
        if ( mLatch != null )
        {
            // already in progress
            throw new IllegalStateException();
        }
        
        mSubmitNanos    = System.nanoTime();
        
        if ( howToRun == HowToRun.RUN_IN_CURRENT_THREAD )
        {
            // No need to change the current context class loader to itself.
            runSync();
        }
        else
        {
            mLatch  = new CountDownLatch(1);
            
            runInSeparateThread( this );
        }
    }
    
    /**
        Calls submit( RUN_IN_SEPARATE_THREAD ).
     */
        public void
    submit( )
    {
        _submit( getRecommendedSubmitType() );
    }
    
       
    /**
        Submit the task for execution with {@link #submit()}.  If 'waitTillDone'
        is true, then this method won't return until the task has finished.  This
        method is useful as a transition method in the course of converting from
        serialized execution to threaded execution, allowing a simple boolean switch
        to make the change in behavior.<p>
        The task is still executed in its own thread, so as to produce the same
        runtime environment that would be used for asynchronous execution (eg thread-local
        variables).
        @param waitTillDone if true, the method executes synchronously
     */
        public void
    submit( final HowToRun howToRun )
    {
        _submit( howToRun );
    }
        /**
        Create a new task.
        @param name use-readable name of the task
        @param data optional arbitrary data (see {@link #getData})
     */
        protected
    RunnableBase( final String name, final T data )
    {
        mDebug  = new AMXDebugHelper( "RunnableBase-" + name );
        mDebug.setEchoToStdOut( true );
        
        mName       = name == null ? (this.getClass().getName() + ".<no_name>") : name ;
        mData       = data;
        mThrowable  = null;
        mSubmitNanos    = 0;
        mRunStartNanos    = 0;
        mRunDoneNanos    = 0;
        
        
        mUseRandomSleep = false;    // good for debugging
        
        mLatch   = null;
    }
    
        protected
    RunnableBase( final String name )
    {
        this( name, null );
    }
    
    public final T    getData()   { return mData; }
    
        protected
    RunnableBase(  )
    {
        this( null );
    }
    
    /* subclass must implement doRun() */
    protected abstract void doRun() throws Exception;
    
        public String
    getName()
    {
        return (mName == null || mName.length() == 0) ? this.getClass().getName() : mName;
    }
    
        protected  static void
    sleepMillis( final long millis )
    {
        try
        {
            Thread.sleep( millis );
        }
        catch( InterruptedException e )
        {
        }
    } 
    
    private static final long MAX_RANDOM_SLEEP_MILLIS   = 500;
    
    /**
        Good for debugging timing issues; a task will insert an artificial delay
        by a random amount.
     */
        public void 
    setUseRandomSleep( final boolean useRandom )
    {
        mUseRandomSleep = useRandom;
    }
    
    // this way, we don't have to execute a getTimings() call, which is synchronized
    private static final Timings TIMINGS    = Timings.getInstance( "RunnableBase" );
        public static Timings
    getTimings()
    {
        return TIMINGS;
    }
    
        private final void
    runSync()
    {
        mRunStartNanos    = System.nanoTime();
        
        if ( mUseRandomSleep )
        {
            final long sleepMillis = (System.currentTimeMillis() >> 4)  % MAX_RANDOM_SLEEP_MILLIS;
            debug( "Random sleep for: " + sleepMillis + "ms" );
            sleepMillis( sleepMillis );
        }
        try
        {
            doRun();
        }
        catch( Throwable t )
        {
            mThrowable  = t;
        }
        finally
        {
            mRunDoneNanos    = System.nanoTime();
            //debug( toString() );
            if ( mLatch != null )   // could be null if RUN_IN_CURRENT_THREAD
            {
                mLatch.countDown();
                mLatch  = null; // it only counts down to 1, so forget about it
            }
            // do this after we release the latch
            final String msg = "RunnableBase-" + StringUtil.quote(getName());
            final long runTime    = getNanosFromSubmit();
            getTimings().add( msg, runTime);
            //debug( "TIME TO ADD TIMING: " + (System.nanoTime() - start ) );
        }
    }
    
    /**
        May be called synchronously or via another thread {@link #submit}.
        See {@link #waitDone} and {@link #waitDoneThrow}.
     */
        public final void
    run()
    {
        final int numRunning = mThreadsRunning.incrementAndGet();
       // debug("Submitted ", getName(), ", #of threads = ", numRunning  );
            
        // Before running this task on this thread set the thread's class loader 
        // to the class loader saved from this task's submitter.
        Thread.currentThread().setContextClassLoader(mClassLoader);
        try {
            runSync();
        }
        finally {
            mThreadsRunning.decrementAndGet();
        }
    }
    
    /**
        @return the number of nanoseconds to execute the task from the time it was submitted
     */
        public long
    getNanosFromSubmit()
    {
        return mRunDoneNanos - mSubmitNanos;
    }
    
    /**
        @return the number of nanoseconds to execute the task from the time it actually entered
        the {@link #run} method.
     */
        public long
    getNanosFromRunStart()
    {
        return mRunDoneNanos - mRunStartNanos;
    }
    
    /**
        @return the number of nanoseconds between task-submittal and the actual execution start
     */
        public long
    getRunLatency()
    {
        return mRunStartNanos - mSubmitNanos;
    }
    
    /**
        Block until the task has finished, and return any Throwable (hopefully null).
        @return the Throwable that was thrown (if any), otherwise null
     */
        public final Throwable
    waitDone()
    {
        // if mLatch is null, it was run synchronously, or has already finished (or never started)
        // use temp, avoid race condition between null check and usage should mLatch go null
        // after check for null
        final CountDownLatch latch  = mLatch;
        if ( latch != null )
        {
            try
            {
                latch.await();
            }
            catch( final InterruptedException intr )
            {
               throw new RuntimeException( intr );
            }
        }
        return mThrowable;
    }
    
    /**
        Block until the task has finished.  If a Throwable was thrown, then this method
        will rethrow it, or a RuntimeException.
     */
        public final void
    waitDoneThrow()
    {
        final Throwable t   = waitDone();
        if ( t != null )
        {
            if ( t instanceof RuntimeException )
            {
                throw (RuntimeException)t;
            }
            else if ( t instanceof Error )
            {
                throw (Error)t;
            }
            else
            {
                throw new RuntimeException( t );
            }
        }
    }
    
    
    /**
        A subclass may transform the thrown exception (if any) into a more appropriate or
        expected kind.
     */
        protected Throwable
    launderThrowable( final Throwable t ) {
        return t;
    }

    
    /**
        Taking into account single vs multi-core, the number of RunnableBase currently
        running, return a recommended HowToRun.
        <p>
        Callers that know significant I/O is involved should usually submit using
            HowToRun.RUN_IN_SEPARATE_THREAD, even on single processor machines.
        <p>
        Callers with long-running tasks should generally not call this method; it's best used
        with numbers of short-running tasks.
        <p>
        A subclass that knows it performs I/O might override this method to usually or always
        return HowToRun.RUN_IN_SEPARATE_THREAD.
     */
        public HowToRun
    getRecommendedSubmitType()
    {
        final int       numProcessors = Runtime.getRuntime().availableProcessors();
        final boolean   singleCore = numProcessors == 1;
        
        HowToRun    howToRun    = HowToRun.RUN_IN_CURRENT_THREAD;
        
        if ( singleCore )
        {
            // try to keep it to just one thread; there could be some I/O
            howToRun    = mThreadsRunning.intValue()  <= 1 ?
                            HowToRun.RUN_IN_SEPARATE_THREAD : HowToRun.RUN_IN_CURRENT_THREAD;
        }
        else
        {
            final int CUTOFF = numProcessors * 2;
            if ( mThreadsRunning.intValue() <= CUTOFF )
            {
                howToRun    = HowToRun.RUN_IN_SEPARATE_THREAD;
            }
            else
            {
                // all cores are busy (though this might be stale as soon as we checked).
                howToRun = HowToRun.RUN_IN_CURRENT_THREAD;
            }
        }
        return howToRun;
    }
        
        public String
    toString()
    {
        final String delim = ", ";
        
        final boolean started   = mSubmitNanos != 0;
        final boolean done      = mRunDoneNanos != 0;
        final long runTimeNanos      = started ?
            (done ? (mRunDoneNanos - mRunStartNanos) : System.nanoTime() - mRunStartNanos) : 0;
        final String throwable = mThrowable == null ? "" : mThrowable.toString();
        
        final String runTimeString = StringUtil.getTimingString( runTimeNanos );
        
        return "Runnable \"" + this.getClass().getName() + "\"" + delim + "name = " + getName() +
            delim + "started=" + started + delim + "done=" + done +
            delim + "run-time=" + runTimeString + delim + throwable;
    }
};
































