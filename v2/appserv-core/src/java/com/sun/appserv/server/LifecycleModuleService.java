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

package com.sun.appserv.server;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.Application;
import com.sun.web.security.RealmAdapter;

import org.apache.catalina.Context;
import org.apache.catalina.core.StandardContext;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;

import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.LifecycleModule;
import com.sun.enterprise.config.serverbeans.ElementProperty;

import com.sun.enterprise.server.ServerContext;

import com.sun.appserv.management.util.misc.TimingDelta;
import com.sun.appserv.management.util.misc.RunnableBase;
import static com.sun.appserv.management.util.misc.RunnableBase.HowToRun;
import static com.sun.appserv.management.util.misc.RunnableBase.HowToRun.*;
import com.sun.appserv.management.helper.AMXDebugHelper;

/**
 * Support class to assist in firing LifecycleEvent notifications to
 * registered LifecycleListeners.
 */
public final class LifecycleModuleService implements ServerLifecycle {

    private OneTimeIniter  mOneTimeIniter;
    private OneTimeStartup mOneTimeStartup;
    
    private final AMXDebugHelper    mDebug  = new AMXDebugHelper( "LifecycleModuleService" );
    private void debug( final Object...args )   { mDebug.println( args ); }
    
    // whether to force synchronous (non-threaded) execution either "sync" or "async"
    private static final String     SUBMIT_TYPE_SPROP    = "LifecycleModuleService.submitType";
    private static final String SUBMIT_TYPE = System.getProperty( SUBMIT_TYPE_SPROP );
    
        private static boolean
    isMultiCore()
    {
        // the javadoc advises checking this periodically
        return Runtime.getRuntime().availableProcessors() >= 2;
    }
    
        private HowToRun
    getSubmitType() {
        HowToRun submitType = RUN_INVALID;
        
        if ( "async".equals( SUBMIT_TYPE ) ) {
            submitType = RUN_IN_SEPARATE_THREAD;
        }
        else if ( "sync".equals( SUBMIT_TYPE ) ) {
            submitType = RUN_IN_CURRENT_THREAD;
        }
        else {
            // default behavior
            // use multiple threads only if we have more than one CPU core
            submitType = isMultiCore() ? RUN_IN_SEPARATE_THREAD : RUN_IN_CURRENT_THREAD;
        }
        
        debug( "SUBMIT_TYPE: ", SUBMIT_TYPE, " => ", submitType );
        
        return submitType;
    }
    
            
    /**
     * Not accessed concurrently, so no need to use a synchronized List.
     */
    private final List<ServerLifecycleModule> mLifecycleModules = new ArrayList<ServerLifecycleModule>();

    /**
        Initialization code, to be run on its own thread.
        @see #onStartup
     */
    private final class OneTimeIniter extends RunnableBase {
        private final ServerContext mServerContext;
        
        public OneTimeIniter( final ServerContext serverContext ) {
            mServerContext  = serverContext;
        }
        
        protected void doRun() throws ConfigException, ServerLifecycleException {
            //ROB: config changes
            //Applications apps = 
                //ServerBeansFactory.getServerBean(mServerContext.getConfigContext()).getApplications();
            final Applications apps = ServerBeansFactory.getApplicationsBean(mServerContext.getConfigContext());
            if (apps == null) return;

            final LifecycleModule[] lcms = apps.getLifecycleModule();
            if(lcms == null) return;
        

            final Set<ServerLifecycleModule> listenerSet = new HashSet<ServerLifecycleModule>();
            for(int i=0;i<lcms.length;i++) {
                final LifecycleModule next = lcms[i];
                    
                if ( isEnabled(next, mServerContext.getConfigContext()) ) {
                    int order = Integer.MAX_VALUE;
                    final String strOrder = next.getLoadOrder();
                    if (strOrder != null) {
                        try {
                            order = Integer.parseInt(strOrder);
                        } catch(NumberFormatException nfe) {
                            nfe.printStackTrace();
                        }
                    }
                    final ServerLifecycleModule slcm =
                        new ServerLifecycleModule(mServerContext, next.getName(), next.getClassName());
                    slcm.setLoadOrder(order);
                    slcm.setClasspath(next.getClasspath());
                    slcm.setIsFatal(next.isIsFailureFatal());
                        
                    final ElementProperty[] s = next.getElementProperty();
                    if(s != null) {
                        for(int j=0;j< s.length;j++) {
                            final ElementProperty next1 = s[j];
                            slcm.setProperty(next1.getName(), next1.getValue());
                        }
                    }

                    final LifecycleListener listener = slcm.loadServerLifecycle();
                    listenerSet.add(slcm);
                }
            }
            sortModules(listenerSet);
            
            initialize(mServerContext);
        }
    };
    
    /**
        Return a List of Set<ServerLifecycleModule> where each item in the same Set has the same
        load order.  The modules are assumed to be already sorted.
     */
        private List<Set<ServerLifecycleModule>>
    groupByLoadOrder( final List<ServerLifecycleModule> lifecycleModules ) {
        final List<Set<ServerLifecycleModule>>  sets    = new ArrayList<Set<ServerLifecycleModule>>();
        int curOrder = Integer.MIN_VALUE;
        
        Set<ServerLifecycleModule> curSet = null;
        for( final ServerLifecycleModule next : lifecycleModules ) {
            final int order = next.getLoadOrder();
            // debug( "Load order for " + next.getName() + ": " + order );
            if ( order < curOrder ) {
                throw new IllegalStateException();
            }
            
            if ( curSet == null || curOrder != order )
            {
                curSet  =  new HashSet<ServerLifecycleModule>();
                sets.add( curSet );
            }
            curSet.add( next );
            curOrder = order;
        }
        
        return sets;
    }

    
    private void sortModules( final Set<ServerLifecycleModule> listenerSet) {
        // FIXME: use a better sorting algorithm, this one is O( N^2 )
        for( final ServerLifecycleModule next : listenerSet ) {
            final int order = next.getLoadOrder();
            
            int i = 0;
            for( ; i < this.mLifecycleModules.size(); i++) {
                if( mLifecycleModules.get(i).getLoadOrder() > order) {
                    break;
                }
            }
            
            mLifecycleModules.add(i,next);
        }
    }
    
      
    /**
        Base Runnable to call a LifecycleModule
    */
    private abstract class LifecycleModuleCaller extends RunnableBase {
        protected final ServerLifecycleModule mLifecycleModule;
        protected final ServerContext         mServerContext;

        public LifecycleModuleCaller(
            final String                methodName,
            final ServerContext         serverContext,
            final ServerLifecycleModule lifecycleModule ) {
            super( lifecycleModule.getName() + "." + methodName + "()" );
            mServerContext    = serverContext;
            mLifecycleModule  = lifecycleModule;
        }

        protected abstract void doRun() throws ServerLifecycleException;
    };
    
    
    /**
        Call a LifecycleModule's onInitialization() method.
    */
    private final class onInitializationCaller extends LifecycleModuleCaller {
        public onInitializationCaller(
            final ServerContext         serverContext,
            final ServerLifecycleModule lifecycleModule ) {
            super( "onInitialization", serverContext, lifecycleModule );
        }
        protected void doRun() throws ServerLifecycleException {
            mLifecycleModule.onInitialization( mServerContext );
        }
    };
    
    
    /**
        Call a LifecycleModule's onStartup() method.
    */
    private final class onStartupCaller extends LifecycleModuleCaller {
        public onStartupCaller(
            final ServerContext         serverContext,
            final ServerLifecycleModule lifecycleModule ) {
            super( "onStartup",  serverContext, lifecycleModule );
        }
        protected void doRun() throws ServerLifecycleException {
            // the SERVLET invocation context so J2EE invocations from 
            // lifecycle modules get a legal ComponentInvocation.

            // create an invocation context that is of the type 
            // SERVLET_INVOCATION
            final Context               invocationContext = new StandardContext();
            final WebBundleDescriptor   wbd               = new WebBundleDescriptor();
            Application.createApplication( mLifecycleModule.getName(),  wbd.getModuleDescriptor());
            invocationContext.setRealm( new RealmAdapter(wbd, false) );
            mLifecycleModule.onStartup( mServerContext, invocationContext);
        }
    };
    
    /**
        Call a LifecycleModule's onReady() method.
    */
    private final class onReadyCaller extends LifecycleModuleCaller {
        public onReadyCaller(
            final ServerContext         serverContext,
            final ServerLifecycleModule lifecycleModule ) {
            super( "onReady", serverContext, lifecycleModule );
        }
        protected void doRun() throws ServerLifecycleException {
            mLifecycleModule.onReady( mServerContext );
        }
    };
    
    
    
        private LifecycleModuleCaller
    createCaller(
        final ServerContext         serverContext,
        final ServerLifecycleModule lifecycleModule,
        final String                methodName)    {
        
        LifecycleModuleCaller caller = null;
        
        if ( methodName.equals( "onInitialization" ) ) {
            caller = new onInitializationCaller( serverContext, lifecycleModule );
        }
        else if ( methodName.equals( "onStartup" ) ) {
            caller = new onStartupCaller( serverContext, lifecycleModule );
        }
        else if ( methodName.equals( "onReady" ) ) {
            caller = new onReadyCaller( serverContext, lifecycleModule );
        }
        else {
            throw new IllegalArgumentException( methodName );
        }
            
        return caller;
    }
    
    private void callAllModules( final ServerContext serverContext, final String methodName )
                            throws ServerLifecycleException {
        // save current ClassLoader
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        
        final List<Set<ServerLifecycleModule>> sets = groupByLoadOrder( mLifecycleModules );
        
        for ( final Set<ServerLifecycleModule> itemsOfSameLoadOrder : sets ) {
            final int numModules = itemsOfSameLoadOrder.size();
            final ServerLifecycleModule[] modules = new ServerLifecycleModule[ numModules ];
            itemsOfSameLoadOrder.toArray( modules );
            
            final LifecycleModuleCaller[] callers = new LifecycleModuleCaller[ numModules ];
                
            for( int i = 0; i < numModules; ++ i ) {
                callers[ i ] = createCaller( serverContext, modules[ i ], methodName);
                
                // run the *last* one in *this* thread to save the overhead of a new thread
                // (common case might be just 1 or 2 items)
                final boolean isLast = (i + 1) == numModules;
                final HowToRun submitType = isLast ? RUN_IN_CURRENT_THREAD : getSubmitType();
                callers[ i ].submit( submitType );
            }
            
            // They're all submitted and running.  Wait until they're *all* done in order to maintain
            // the semantics of not loading any subsequent modules of a later load-order.
            for( int idx = 0; idx < callers.length; ++idx ) {
                callers[idx].waitDone();    // do not call waitDoneThrow(); we want to wait for all
                debug( "Millis for " + callers[idx].getName() + ": " + callers[idx].getNanosFromSubmit() / (1000*1000) );
            }
            // All modules have now finished
            
            // they're all done, for better or worse. Loop again, throw an exception if
            // any of them had one.
            for( int idx = 0; idx < callers.length; ++idx ) {
                callers[idx].waitDoneThrow();
            }
        }
        
        // restore ClassLoader, in case a module changed it
        resetClassLoader(cl);
    }


    private void initialize( final ServerContext serverContext) 
                            throws ServerLifecycleException {
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        
        callAllModules( serverContext, "onInitialization" );

        // set it back
        resetClassLoader(cl);
    }
    
    
    
    public LifecycleModuleService() {
    }
    

    
    public void onInitialization( final ServerContext context)
                                        throws ServerLifecycleException {
        mOneTimeIniter = new OneTimeIniter( context );
                                        
        mOneTimeIniter.submit( getSubmitType() );
        // leave it running; we'll sync up with it in start()
    }

    /**
     * Returns true if life cycle module is enabled in the application
     * level and in the application ref level.
     *
     * @param  lcm  life cycle module
     * @param  config  config context
     *
     * @return  true if life cycle module is enabled
     */
    private boolean isEnabled(LifecycleModule lcm, ConfigContext config) {
        try {
            // return false if arguments are null
            if (lcm == null || config == null) {
                return false;
            }

            // find the ref to the life cycle module
            final Server server = ServerBeansFactory.getServerBean(config);
            final ApplicationRef appRef=server.getApplicationRefByRef(lcm.getName());

            // true if enabled in both lifecyle module and in the ref
            return ((lcm.isEnabled()) && 
                        (appRef != null && appRef.isEnabled()));

        } catch (ConfigException e) {
            return false;
        }
    }

    private void resetClassLoader(final ClassLoader c) {
         // set the common class loader as the thread context class loader
        java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction() {
                public Object run() {
                    Thread.currentThread().setContextClassLoader(c);
                    return null;
                }
            }
        );
    }
    
    /*
        Note that both the onStartup() and onReady() phases
        are performed (in sequence) by this thread.
        @see #onStartup
        @see #onReady
    */
    private final class OneTimeStartup extends RunnableBase {
        private final ServerContext mServerContext;

        public OneTimeStartup( final ServerContext serverContext ) {
            mServerContext  = serverContext;
        }

        protected void doRun() throws ServerLifecycleException {
            final ClassLoader cl = Thread.currentThread().getContextClassLoader();
            
            callAllModules( mServerContext, "onStartup" );
            
            // restore ClassLoader, in case it changed
            resetClassLoader(cl);
        }
    };
        
        
    public void onStartup(ServerContext context) throws ServerLifecycleException {
        //final TimingDelta delta    = new TimingDelta();
        
        // wait for initialization to finish
        mOneTimeIniter.waitDoneThrow();
        //debug( "Millis for OneTimeIniter to run: ", (mOneTimeIniter.getNanosFromSubmit() / (1000*1000)), ", wait time = ", delta.elapsedMillis() );
        mOneTimeIniter  = null;
        
        mOneTimeStartup = new OneTimeStartup( context );
        mOneTimeStartup.submit( getSubmitType() );
        // we'll sync up with this thread in onReady()
    }

    public void onReady(final ServerContext serverContext ) throws ServerLifecycleException {
        mOneTimeStartup.waitDoneThrow();
        mOneTimeStartup = null;
        // onReady() not yet done, do it now
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        
        callAllModules( serverContext, "onReady" );
        
        resetClassLoader(cl);
        assert( mOneTimeStartup == null );
    }

    public void onShutdown() throws ServerLifecycleException {
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        for( final ServerLifecycleModule next : mLifecycleModules ) {
            next.onShutdown();
        }
        // set it back
        resetClassLoader(cl);
    }
    
    public void onTermination() throws ServerLifecycleException {
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        for( final ServerLifecycleModule next : mLifecycleModules ) {
            next.onTermination();
        }
        // set it back
        resetClassLoader(cl);
    }
}





