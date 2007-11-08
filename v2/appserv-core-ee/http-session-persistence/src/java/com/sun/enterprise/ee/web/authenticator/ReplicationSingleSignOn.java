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
/*
 * ReplicationSingleSignOn.java
 *
 * Created on June 19, 2006, 10:12 AM
 *
 */

package com.sun.enterprise.ee.web.authenticator;

import java.lang.reflect.InvocationTargetException;

import java.util.*;
import java.io.IOException;
import java.security.Principal;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.*;
import org.apache.catalina.authenticator.*;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.HttpResponse;
import org.apache.catalina.Realm;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.Session;

import com.sun.enterprise.security.web.SingleSignOn;

import com.sun.appserv.ha.spi.*;
import com.sun.enterprise.ee.web.sessmgmt.*;

import com.sun.enterprise.security.web.SingleSignOn;
import com.sun.enterprise.security.web.SingleSignOnEntry;
import com.sun.enterprise.web.ServerConfigLookup;
import org.apache.catalina.session.StandardSession;

import com.sun.web.security.RealmAdapter;

import com.sun.appserv.util.cache.BaseCache;

/**
 *
 * @author Larry White
 */
public class ReplicationSingleSignOn extends SingleSignOn 
        implements HASSO, ReplicationManager {
    
    /**
     * The logger to use for logging ALL web container related messages.
     */
    private static Logger _logger = null;
    
    final static String MODE_SSO = ReplicationState.MODE_SSO;
    
    final static String DUPLICATE_IDS_SEMANTICS_PROPERTY 
        = ReplicationState.DUPLICATE_IDS_SEMANTICS_PROPERTY;    
    
    protected static int _maxBaseCacheSize = 4096;
    protected static float _loadFactor = 0.75f;
    
    static
	{
            checkSessionCacheProperties();
	}     

    protected static boolean checkSessionCacheProperties() {
        boolean result = false;
	try
        {
            Properties props = System.getProperties();
            String cacheSize=props.getProperty("HTTP_SESSION_CACHE_MAX_BASE_CACHE_SIZE");
            if(null!=cacheSize) {
                _maxBaseCacheSize = (new Integer (cacheSize).intValue());
            }  
            String loadFactor=props.getProperty("HTTP_SESSION_CACHE_MAX_BASE_LOAD_FACTOR");
            if(null!=loadFactor) {
                _loadFactor = (new Float (loadFactor).floatValue());
            }
            /*
            System.out.println("_maxBaseCacheSize=" + _maxBaseCacheSize);
            System.out.println("_loadFactor=" + _loadFactor);
             */
        } catch(Exception e)
        {
            //do nothing accept defaults
        }
        return result;
    }    
    
    /**
     * The store pool.
     */    
    protected StorePool _pool = null;
    
    /**
     * The background thread.
     */
    private Thread thread = null;


    /**
     * The background thread completion semaphore.
     */
    private boolean threadDone = false; 
    
    /**
     * The virtual server name
     */
    private String virtualServerName = null;
    
    public String getPassedInPersistenceType() {
        return _passedInPersistenceType;
    }    
    
    public void setPassedInPersistenceType(String persistenceType) {
        _passedInPersistenceType = persistenceType;
    }    

    /**
    * the passed in persistence type may be replicated or extension type
    */    
    protected String _passedInPersistenceType = null;    
    
    /**
    * Our Replicator instance 
    */
    protected BackingStore backingStore = null;
    
    /**
    * get the backingStore
    */ 
    public BackingStore getBackingStore() {
        if(backingStore == null) {
            this.createBackingStore();
        }
        return backingStore;
    }
    
    /**
    * set the backing store
    * @param aBackingStore
    */ 
    public void setBackingStore(BackingStore aBackingStore) {
        backingStore = aBackingStore;
    } 
    
    void createBackingStore() {
        //BackingStoreFactory storeFactory = new JxtaBackingStoreFactory();
        BackingStoreFactory storeFactory = getBackingStoreFactory();
        //System.out.println("storeFactory: " + storeFactory);
        BackingStoreRegistry backingStoreRegistry 
            = BackingStoreRegistry.getInstance();
        Properties env 
            = backingStoreRegistry.getFactoryClassEnv(getPassedInPersistenceType());
        //this is always true for sso
        env.put(DUPLICATE_IDS_SEMANTICS_PROPERTY, true);        
        BackingStore backingStore = null;
        try {
            backingStore = storeFactory.createBackingStore(
                        SimpleMetadata.class,     //type
                        this.getApplicationId(), //appid
                        env);
        } catch (BackingStoreException ex) {
            //deliberate no-op
        }
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("backingStore: " + backingStore);
        }         
        if(backingStore != null) {
            if(backingStore instanceof JxtaBackingStoreImpl) {
                ((JxtaBackingStoreImpl)backingStore).setMode(MODE_SSO);
            }                
            this.setBackingStore(backingStore);
        }
    }
    
    protected BackingStoreFactory getBackingStoreFactory() {
        BackingStoreFactory backingStoreFactory = new JxtaBackingStoreFactory();
        BackingStoreRegistry backingStoreRegistry
            = BackingStoreRegistry.getInstance();
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationSingleSignOn>>getBackingStoreFactory:passedInPersistenceType=" + getPassedInPersistenceType());
        }         
        //System.out.println("ReplicationSingleSignOn>>getBackingStoreFactory:passedInPersistenceType=" + getPassedInPersistenceType());
        if(getPassedInPersistenceType() == null) {
            return backingStoreFactory;
        }
        String factoryClassName 
            = backingStoreRegistry.getFactoryClassName(this.getPassedInPersistenceType());
        return getBackingStoreFactoryFromName(factoryClassName);
    }
    
    private BackingStoreFactory getBackingStoreFactoryFromName(String className) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationSingleSignOn:className: " + className);
        }           
        BackingStoreFactory backingStoreFactory = new JxtaBackingStoreFactory();
        try {
            backingStoreFactory = 
                (BackingStoreFactory) (Class.forName(className)).newInstance();
        } catch (Exception ex) {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("unable to create backing store factory");
            }            
        } 
        return backingStoreFactory;
    }          
    
    /** Creates a new instance of ReplicationSingleSignOn */
    public ReplicationSingleSignOn(String theVirtualServerName) {
        if (_logger == null) {
            _logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);
        }
        virtualServerName = theVirtualServerName;
        //initialize replicated sso entries cache
        replicatedSSOEntries = new BaseCache();
        replicatedSSOEntries.init(_maxBaseCacheSize, _loadFactor, null);        
    }
    
    /**
    * Our cache of replicated HASingleSignOnEntry objects
    * keyed by ssoId
    */
    protected BaseCache replicatedSSOEntries = new BaseCache();

    /**
    * get the replicated ssoEntries cache
    */ 
    public BaseCache getReplicatedSSOEntries() {
        return replicatedSSOEntries;
    }
  
    /**
    * set the replicated ssoEntries cache
    * @param ssoEntryTable
    */ 
    public void setReplicatedSSOEntries(BaseCache ssoEntryTable) {
        replicatedSSOEntries = ssoEntryTable;
    }
    
    protected void putInReplicationCache(ReplicationState state) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationSingleSignOn>>putInReplicationCache id: " + state.getId());
        }         
        if(replicatedSSOEntries == null) {
            return;
        }
        replicatedSSOEntries.put(state.getId(), state);
    }
    
    protected ReplicationState getFromReplicationCache(String id) {    
        return (ReplicationState)replicatedSSOEntries.get(id);
    }
    
    protected void removeFromReplicationCache(ReplicationState state) { 
        if(replicatedSSOEntries == null  || state == null) {
            return;
        }
        replicatedSSOEntries.remove(state.getId());
    }
    
    protected synchronized ReplicationState transferFromReplicationCache(String id) { 
        ReplicationState result = this.getFromReplicationCache(id);
        removeFromReplicationCache(result);
        return result;
    }    
    
    // store pool was set during initialization of this valve
    public void setSSOStorePool(StorePool pool) {
        _pool = pool;
    }

    public StorePool getSSOStorePool() {
        return _pool;
    }
    
    public String getInstanceName() {
        ServerConfigLookup lookup = new ServerConfigLookup();
        return lookup.getServerName();
    } 
    
    public String getVirtualServerName() {
        return virtualServerName;
    }
    
    public void setVirtualServerName(String value) {
        virtualServerName = value;
    }
            
    /**
    * The application id
    */  
    protected String applicationId = null;        
    
    public String getApplicationId() {
        if(applicationId != null)
            return applicationId;
        Container container = this.getContainer();
        StringBuffer sb = new StringBuffer(50);
        //sb.append(this.getClusterId());
        ArrayList list = new ArrayList();
        while (container != null) {
            if(container.getName() != null) {
                list.add(":" + container.getName());
            }
            container = container.getParent();
        }
        sb.append("SSO");
        for(int i=(list.size() -1); i>-1; i--) {
            String nextString = (String) list.get(i);
            sb.append(nextString);
        }
        sb.append(":" + this.getVirtualServerName());
        applicationId = sb.toString();
        return applicationId;

    }    
    
   /**
     * Perform single-sign-on support processing for this request.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param context The valve context used to invoke the next valve
     *  in the current processing pipeline
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    public int invoke(Request request, Response response)
            throws IOException, ServletException {

        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("ReplicationSingleSignOn.invoke()");
        }

        // If this is not an HTTP request and response, just pass them on
        if (!(request instanceof HttpRequest) ||
            !(response instanceof HttpResponse)) {
            // START OF IASRI 4665318
            // context.invokeNext(request, response);
            // return;
            return INVOKE_NEXT;
            //return 0;
            // END OF IASRI 4665318
        }

        HttpServletRequest hreq = (HttpServletRequest) request.getRequest();
        HttpServletResponse hres = (HttpServletResponse) response.getResponse();
        request.removeNote(Constants.REQ_SSOID_NOTE);

        // Has a valid user already been authenticated?
        if (hreq.getUserPrincipal() != null) {
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.finest("Principal '"
                               + hreq.getUserPrincipal().getName()
                               + "' has already been authenticated");
            }
            return INVOKE_NEXT;
            //return 0;
        }

        if (_logger.isLoggable(Level.FINEST)) {
	    _logger.finest("Checking for SSO cookie");
        }
        Cookie cookie = null;
        Cookie cookies[] = hreq.getCookies();
        if (cookies == null)
            cookies = new Cookie[0];
        for (int i = 0; i < cookies.length; i++) {
            if (Constants.SINGLE_SIGN_ON_COOKIE.equals(cookies[i].getName())) {
                cookie = cookies[i];
                break;
            }
        }
        if (cookie == null) {
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.finest("SSO cookie not present");
            }
            return INVOKE_NEXT;
        }

        Realm realm = request.getContext().getRealm();
        if (realm == null) {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.fine(" No realm configured for this application, SSO "
                             + "does not apply.");
            }
            return INVOKE_NEXT;
        }

        String realmName = realm.getRealmName();
        if (realmName == null) {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.fine(" No realm configured for this application, SSO "
                             + "does not apply.");
            }
            return INVOKE_NEXT;
        }

        // Look up the cached Principal associated with this cookie value
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("Checking for cached principal for "
                           + cookie.getValue());
        }

        SingleSignOnEntry entry = lookupEntry(cookie.getValue());
        if (entry != null) {

            if (_logger.isLoggable(Level.FINEST)) {
                _logger.finest("Cached SingleSignOnEntry: " + entry);
            }

            // only use this SSO identity if it was set in the same realm
            if (!realmName.equals(entry.realmName)) {
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.fine(" Ignoring SSO entry which does not match "
                                 + "application realm '" + realmName + "'");
                }
                return INVOKE_NEXT;
            }

            if (_logger.isLoggable(Level.FINEST)) {
                if (entry.principal != null) {
                    _logger.finest("Found cached principal '"
                                   + entry.principal.getName()
                                   + "' with auth type '" + entry.authType
                                   + "'");
                } else {
                    _logger.finest("No cached principal found");
                }
            }

            if ((entry.principal == null)
                    && (entry.username != null)) {
                entry.principal = ((RealmAdapter)request.getContext().getRealm()).createFailOveredPrincipal(entry.username);
            }
            System.out.println("entry.principal=" + entry.principal);
            System.out.println("entry.username=" + entry.username);

            request.setNote(Constants.REQ_SSOID_NOTE, cookie.getValue());
            ((HttpRequest) request).setAuthType(entry.authType);
            ((HttpRequest) request).setUserPrincipal(entry.principal);
            // Touch the SSO entry access time
            entry.lastAccessTime = System.currentTimeMillis();
            ((HASingleSignOnEntry)entry).dirty = true;

        } else {
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.finest("No cached principal found, erasing SSO cookie");
            }
            cookie.setMaxAge(0);
	    //Bug : 4833387	
	    cookie.setPath("/");
            hres.addCookie(cookie);
        }

        // Invoke the next Valve in our pipeline
        return INVOKE_NEXT;
        //return 0;
    }    
    
    /** Look up and return the cached SingleSignOn entry associated with this
     * sso id value, if there is one; otherwise return <code>null</code>.
     *
     * @param ssoId Single sign on identifier to look up
     * @return SingleSignOnEntry 
     */
    public SingleSignOnEntry lookupEntry(String ssoId) {

        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("ReplicationSingleSignOn.lookupEntry(): ssoId=" + ssoId);
        }

        SingleSignOnEntry ssoEntry=null;

        long startTime = System.currentTimeMillis();
        ReplicationSSOStorePoolElement store = null;
        try {
            store = (ReplicationSSOStorePoolElement) _pool.take();
            store.setContainer(this.getContainer());
            store.setParent(this);
            store.setApplicationId(this.getApplicationId());
            ssoEntry = super.lookupEntry(ssoId);
            if (ssoEntry != null) {
                return ssoEntry; //return if the sso is in cache
            }
            try{
                ssoEntry = store.loadSSO(ssoId, this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(ssoEntry != null) {
                if (_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("lookup before :- ssoID="+ssoId+"   "
                                   +ssoEntry);
                    _logger.finest("container= "+container+" realm= "
                                   +container.getRealm());
                    _logger.finest("lookup after if :- ssoID="+ssoId+"   "
                                   +ssoEntry);
                }
                registerInMemory(ssoId, ssoEntry);
            }
        } catch (InterruptedException iex){
            iex.printStackTrace();
        } finally {
            if(store != null) {	
                try {
                    _pool.put(store);
                    if (_logger.isLoggable(Level.FINEST)) {
                        long endTime = System.currentTimeMillis();
                        _logger.finest("lookup_TIME MILLIS = "
                                       + (endTime - startTime));
                    }
                } catch (InterruptedException ex1) {ex1.printStackTrace();}
            }
        }
        return ssoEntry;
    }
    
     /**
     * Register the specified SingleSignOnEntry as being associated with the specified
     * value for the single sign on identifier.
     *
     * @param ssoId Single sign on identifier to register
     * @param sso Single sign on entry
     */
     void registerInMemory(String ssoId, SingleSignOnEntry sso) {
         
     	synchronized (cache) {
            cache.put(ssoId, sso);
        }
     }
     
     /**
     * Register the specified Principal as being associated with the specified
     * value for the single sign on identifier.
     *
     * @param ssoId Single sign on identifier to register
     * @param principal Associated user principal that is identified
     * @param authType Authentication type used to authenticate this
     *  user principal
     * @param username Username used to authenticate this user
     * @param password Password used to authenticate this user
     */
     protected void register(String ssoId,
                             Principal principal,
                             String authType,
                             String username,
                             String password,
                             String realmName) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationSingleSignOn>>register: ssoId=" + ssoId
            + " authType=" + authType + " username=" + username
            + " password=" + password + " realmName=" + realmName);
        }         
        long startTime = System.currentTimeMillis();
        ReplicationSSOStorePoolElement store = null;

        try {
            store = (ReplicationSSOStorePoolElement) _pool.take();
            store.setContainer(this.getContainer());
            store.setParent(this);
            store.setApplicationId(this.getApplicationId());

            SingleSignOnEntry ssoEntry = new HASingleSignOnEntry(principal,
                                                                 authType,
                                                                 username,
                                                                 password,
                                                                 realmName);
            registerInMemory(ssoId, ssoEntry);

            if (!authType.equals(org.apache.catalina.authenticator.Constants.FORM_METHOD)
                    && !authType.equals(org.apache.catalina.authenticator.Constants.BASIC_METHOD)) {
                return;
            }

            try {
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("ReplicationSingleSignOn.register(): "
                                   + "About to save: ssoId=" + ssoId
                                   + " ssoEntry=" + ssoEntry);
                }
                store.save(ssoId, ssoEntry);
            } catch (Exception e){
                e.printStackTrace();
            }
        } catch (InterruptedException iex){
           iex.printStackTrace();
        } finally {
            if(store != null){
                try {
                    _pool.put(store);
                    if (_logger.isLoggable(Level.FINEST)) {
                        long endTime = System.currentTimeMillis();
                        _logger.finest("register_TIME MILLIS = "
                                       + (endTime - startTime));
                    }
                } catch (InterruptedException ex1) {
                    ex1.printStackTrace();
                }
            }
        }
    } 
     
   /**
     * Deregister the specified single sign on identifier, and invalidate
     * any associated sessions.
     *
     * @param ssoId Single sign on identifier to deregister
     */
    protected void deregister(String ssoId) {
        deregister(ssoId, false);
    }

    protected void deregister(String ssoId, boolean bgCall) {

        if (_logger.isLoggable(Level.FINEST)) {
            if (bgCall) {
                _logger.finest("BackGround : Deregistering ssoId '"
                               + ssoId + "'");
            } else {
                _logger.finest("ForeGround : Deregistering ssoId '"
                               + ssoId + "'");
            }
        }

        // Look up and remove the corresponding SingleSignOnEntry
        SingleSignOnEntry sso = null;

        long startTime = System.currentTimeMillis();
        ReplicationSSOStorePoolElement store = null;
        try {
            store = (ReplicationSSOStorePoolElement) _pool.take();
            store.setContainer(this.getContainer());
            store.setParent(this);
            store.setApplicationId(this.getApplicationId());

            synchronized (cache) {
                sso = (SingleSignOnEntry) cache.remove(ssoId);
            }
            try {
                if(!bgCall)
                    store.remove(ssoId);//remove from ssotable
                else
                    //FIXME remove after test
                    //store.remove(ssoId,null);
                    store.remove(ssoId);
            } catch (Exception e){
                e.printStackTrace();
            }

            if (sso == null)
                return;

            // Expire any associated sessions
            Session sessions[] = sso.findSessions();
            for (int i = 0; i < sessions.length; i++) {
                if (_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("Invalidating session " + sessions[i]);
                }
                // Remove from reverse cache first to avoid recursion
                synchronized (reverse) {
                    reverse.remove(sessions[i]);
                }
                // Invalidate this session
                sessions[i].expire();
            }
            try {
                if(!bgCall)
                    store.removeInActiveSessions(ssoId); 
                else
                    //FIXME remove after test
                    //store.removeInActiveSessions(ssoId,null);
                    store.removeInActiveSessions(ssoId);               

            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (InterruptedException iex){
            iex.printStackTrace();
        } finally {
            if(store != null) {
                try {
                    _pool.put(store);
                    if (_logger.isLoggable(Level.FINEST)) {
                        long endTime = System.currentTimeMillis();
                        _logger.finest("deregister_TIME MILLIS = "
                                       + (endTime - startTime));
                    }
                } catch (InterruptedException ex1) {ex1.printStackTrace();}
            }
        }
    
        // NOTE:  Clients may still possess the old single sign on cookie,
        // but it will be removed on the next request since it is no longer
        // in the cache
    }
    
    /**
     * Associate the specified single sign on identifier with the
     * specified Session.
     *
     * @param ssoId Single sign on identifier
     * @param session Session to be associated
     */
    public void associate(String ssoId, Session session) {

        if (!started) {
            return;
        }

        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("ReplicationSingleSignOn.associate(): "
                           + "Associate sso id " + ssoId
                           + " with session " + session);
        }

        long startTime = System.currentTimeMillis();
        ReplicationSSOStorePoolElement store = null;
        try {
            store = (ReplicationSSOStorePoolElement) _pool.take();
            store.setContainer(this.getContainer());
            store.setParent(this);
            store.setApplicationId(this.getApplicationId());

            SingleSignOnEntry sso = lookupEntry(ssoId);
            if (sso != null)
                sso.addSession(this, session);
            synchronized (reverse) {
                reverse.put(session, ssoId);
            }
            try {
                if((session != null)&&(session instanceof HASession)){
                    ((HASession)session).setSsoId(ssoId);
                    store.associate((StandardSession)session,ssoId);
                }
            } catch (Exception e) {
                _logger.log(Level.WARNING,
                            "Exception in ReplicationSingleSignOn.associate()",
                            e);
            }
        } catch (InterruptedException iex){
            iex.printStackTrace();
        } finally {
            if(store != null) {
                try {
                    _pool.put(store);
                    if (_logger.isLoggable(Level.FINEST)) {
                        long endTime = System.currentTimeMillis();
                        _logger.finest("associate_TIME MILLIS = "
                                       + (endTime - startTime));
                    }
                } catch (InterruptedException ex1) {
                    ex1.printStackTrace();
                }
            }
        }

    }
    
   /**
     * Prepare for the beginning of active use of the public methods of this
     * component.  This method should be called after <code>configure()</code>,
     * and before any of the public methods of the component are utilized.
     *
     * @exception IllegalStateException if this component has already been
     *  started
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    public void start() throws LifecycleException {

        // Validate and update our current component state
        if (started) {
            throw new LifecycleException
                (sm.getString("authenticator.alreadyStarted"));
        }

        lifecycle.fireLifecycleEvent(START_EVENT, null);
        started = true;

        // BEGIN IASRI 4705699
        // Start the background reaper thread
        threadStart();
        // END IASRI 4705699
    }


    /**
     * Gracefully terminate the active use of the public methods of this
     * component.  This method should be the last one called on a given
     * instance of this component.
     *
     * @exception IllegalStateException if this component has not been started
     * @exception LifecycleException if this component detects a fatal error
     *  that needs to be reported
     */
    public void stop() throws LifecycleException {

        // Validate and update our current component state
        if (!started) {
            throw new LifecycleException
                (sm.getString("authenticator.notStarted"));
        }

        long startTime = System.currentTimeMillis();
        ReplicationSSOStorePoolElement store = null;
        lifecycle.fireLifecycleEvent(STOP_EVENT, null);
        started = false;

        try {
            store = (ReplicationSSOStorePoolElement) _pool.take();
            store.setContainer(this.getContainer());
            store.setParent(this);
            store.setApplicationId(this.getApplicationId());
            synchronized (cache) {

                Iterator it = cache.keySet().iterator();
                while (it.hasNext()) {
                    String key = (String) it.next();
                    SingleSignOnEntry sso = (SingleSignOnEntry) cache.get(key);
                    if (((HASingleSignOnEntry)sso).dirty) { 
                        if(_logger.isLoggable(Level.FINEST)) {
                            _logger.finest("Stop: updating the SSO session "
                                           +key);
                        }                         
                    	store.save(key,sso);
                        ((HASingleSignOnEntry)sso).dirty = false;
                    }
                }
            }
        } catch (InterruptedException iex){
           iex.printStackTrace();
        } catch (Exception ex){
           ex.printStackTrace();
        } finally {
            if(store != null){
                try {
                    _pool.put(store);
                    if(_logger.isLoggable(Level.FINEST)) {                    
                        long endTime = System.currentTimeMillis();
                        _logger.finest("stop_TIME MILLIS = "
                                       + (endTime - startTime));
                    }
                } catch (InterruptedException ex1) {
                    ex1.printStackTrace();
                }
            }
        }

        threadStop();
    }
    
    public void repair(long repairStartTime) {
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("ReplicationSingleSignOn>>repair");
        }        
        /*
        if (!started)
            return;
         */

        if(ReplicationHealthChecker.isStopping()) {
            return;
        }

        ReplicationState ssoEntries[] = getReplicatedSsoEntriesArray();
        for (int i = 0; i < ssoEntries.length; i++) {
            ReplicationState ssoEntry = (ReplicationState) ssoEntries[i];
            try {
                repairSave(ssoEntry); 
            } catch(Exception ex) {
                //FIXME log this
                ex.printStackTrace();
            }
        }
    }
    
    public void repair(long repairStartTime, boolean checkForStopping) {
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("ReplicationSingleSignOn>>repair");
        }        
        /*
        if (!started)
            return;
         */

        if(checkForStopping && ReplicationHealthChecker.isStopping()) {
            return;
        }

        ReplicationState ssoEntries[] = getReplicatedSsoEntriesArray();
        for (int i = 0; i < ssoEntries.length; i++) {
            ReplicationState ssoEntry = (ReplicationState) ssoEntries[i];
            try {
                repairSave(ssoEntry); 
            } catch(Exception ex) {
                //FIXME log this
                ex.printStackTrace();
            } catch(Throwable t) {
                System.out.println("Throwable during force flush");
                //t.printStackTrace();
                break;
            }
        }
    }    
    
    /** Saves the state
     * @param beanState ReplicationState
     * 
     */
    public void repairSave(ReplicationState state) {
        /*
        if( !EEHADBHealthChecker.isOkToProceed() ) {
            return;
        }
         */        
        if(_logger.isLoggable(Level.FINER)) {
            _logger.entering("ReplicationSingleSignOn", "repairSave",
                new Object[] {state});
        }                
        ReplicationSSOStorePoolElement store = null;
        try {
            store = (ReplicationSSOStorePoolElement)_pool.take();
            store.setContainer(this.getContainer());
            store.setParent(this);
            store.setApplicationId(this.getApplicationId());
            try {
                ((ReplicationSSOStore)store).saveForRepair(state);
            } catch (Exception e) {
                _logger.log(Level.WARNING,
                    "Exception in ReplicationSingleSignOn.repairSave()",
                    e);
            }            
        } catch (InterruptedException iex){
            iex.printStackTrace();
        } finally {
            if(store != null) {
                try {
                    _pool.put(store);
                } catch (InterruptedException ex1) {
                    ex1.printStackTrace();
                }
            }
        }                        
        if(_logger.isLoggable(Level.FINER)) {
            _logger.exiting("ReplicationSingleSignOn", "repairSave");
        }
    }    

    public ReplicationState[] getReplicatedSsoEntriesArray() {
        
        ReplicationState[] ssoEntries = null;
        int numberOfIds = replicatedSSOEntries.getEntryCount();
        ArrayList valuesList = new ArrayList(numberOfIds);
        Iterator valuesIter = replicatedSSOEntries.values();
        while(valuesIter.hasNext()) {
            valuesList.add((ReplicationState)valuesIter.next());
        }
        ReplicationState[] template = new ReplicationState[valuesList.size()];
        ssoEntries = (ReplicationState[])valuesList.toArray(template);
        return ssoEntries;

    }     
    
//---------------------------Background thread methods-----------------    

   /**
     * Invalidate all SSO cache entries that have expired.
     */
    private void processExpires() {

        long tooOld = System.currentTimeMillis() - getMaxInactive() * 1000;

        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest(tooOld
                           + "SSO Expiration thread started. Current entries: "
                           + cache.size());
        }

        ArrayList removals = new ArrayList(cache.size()/2);

        // build list of removal targets

        // Note that only those SSO entries which are NOT associated with
        // any session are elegible for removal here.
        // Currently no session association ever happens so this covers all
        // SSO entries. However, this should be addressed separately.

        try {
            synchronized (cache) {

                Iterator it = cache.keySet().iterator();
                while (it.hasNext()) {
                    String key = (String) it.next();
                    SingleSignOnEntry sso = (SingleSignOnEntry) cache.get(key);
                    if(_logger.isLoggable(Level.FINEST)) {
                        _logger.finest(tooOld
                                       + "*******************  "
                                       + sso.lastAccessTime
                                       + "   SSO Expiration thread started. Current entries: "
                                       + cache.size());
                    }
                    if (sso.sessions.length == 0 &&
                        sso.lastAccessTime < tooOld) {
                        removals.add(key);
                    }
                }
            }
            int removalCount = removals.size();

            if (_logger.isLoggable(Level.FINEST)) {
                _logger.finest("SSO cache will expire " + removalCount
                               + " entries.");
            }

            // deregister any elegible sso entries
            for (int i=0; i < removalCount; i++) {
            	if(_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("SSO Expiration removing entry: "
                                   + removals.get(i));
                }
                deregister((String)removals.get(i),true);
            }

        } catch (Throwable e) { // don't let thread die
            _logger.log(Level.WARNING,
                        "Exception in ReplicationSingleSignOn.processExpires()",
                        e);
        }
    }    
    
   /**
     * Update all SSO enytries whose LAT has changed
     */
    private void processUpdateLat() {
        long startTime = System.currentTimeMillis();
        ReplicationSSOStorePoolElement store = null;
        Hashtable updatedLats = new Hashtable(cache.size()/2);

        try {
            synchronized (cache) {

            	Iterator it = cache.keySet().iterator();
                while (it.hasNext()) {

                    String key = (String) it.next();
                    SingleSignOnEntry sso = (SingleSignOnEntry) cache.get(key);
                    if(_logger.isLoggable(Level.FINEST)) {
                        _logger.finest("===="
                                       + sso.lastAccessTime
                                       + "   SSO Expiration/Updation thread started. Current cache entries: "
                                       + cache.size());
                    }
                    if (((HASingleSignOnEntry)sso).dirty) {
                        updatedLats.put(key,sso);
                    }
                }
            }

            int updatedLatsCount = updatedLats.size();

            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("SSO table will updated " + updatedLatsCount
                               + " entries.");
            }

            try {
            	store = (ReplicationSSOStorePoolElement) _pool.take();
            	store.setContainer(this.getContainer());
                store.setParent(this);
                store.setApplicationId(this.getApplicationId());

            	// update all elegible sso entries
            	Iterator it = updatedLats.keySet().iterator();
            	while (it.hasNext()) {
                    String ssoId = (String) it.next();
                    if(_logger.isLoggable(Level.FINEST)) {
                        _logger.finest("SSO LATupdation updating entry: "
                                       + ssoId);
                    }

                    store.updateLastAccessTime(
                        ssoId,
                        ((SingleSignOnEntry)updatedLats.get(ssoId)).lastAccessTime);
                    ((HASingleSignOnEntry)updatedLats.get(ssoId)).dirty = false;
            	}
            } catch (InterruptedException iex) {
                iex.printStackTrace();
            } finally {
                if (store != null) {
                    try{
                        _pool.put(store);
                        if(_logger.isLoggable(Level.FINEST)) {
                            long endTime = System.currentTimeMillis();
                            _logger.finest("processUpdateLat_TIME MILLIS = "
                                           + (endTime - startTime));
                        }
                    } catch (InterruptedException ex1) {ex1.printStackTrace();}
                }
            }

        } catch (Throwable e) { // don't let thread die
            _logger.log(Level.WARNING,
                        "Exception in ReplicationSingleSignOn.processUpdateLat()",
                        e);
        }
    }
    
    // begin message processing methods

    public void processMessage(ReplicationState message) {
        //handle broadcast methods
        if(ReplicationState.isBroadcastState(message)) {
            processBroadcastMessage(message);
            return;
        }
        
        //handle non-void methods
        ReplicationStateQueryResponse queryResult = null;
        //do process non-void message (and cannot be response either)
        if(!message.isResponseState() && !message.isVoidMethodReturnState()) {
            //do non-void processing including sending response
            queryResult = this.doProcessQueryMessage(message);
            ReplicationState qResponse = queryResult.getState();
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("ReplicationSingleSignOn:qResponse=" + qResponse);
            }             
            //System.out.println("ReplicationSingleSignOn:qResponse=" + qResponse);
            if(qResponse != null) {
                //sourceInstanceName is preserved in the response
                ReplicationState response = 
                    ReplicationState.createResponseFrom(qResponse);
                if(_logger.isLoggable(Level.FINE)) {
                    _logger.fine("ReplicationSingleSignOn:responseState=" + response);
                }                
                //System.out.println("ReplicationSingleSignOn:responseState=" + response);
                this.doSendResponse(response);
            }
            return;
        }
        //end do process non-void message
        
        /*
        ReplicationState response = 
            ReplicationState.createResponseFrom(message);
         */
        //send a response before further processing only if processed 
        //msg is not itself a response and if method is a void return type
        //FIXME this send will be removed if upstream ack send works
        /* removing this for test
        if(!message.isReturnMessage() && message.isVoidMethodReturnState()) {
            this.doSendResponse(response);
        }
        */        

        boolean isResponse = this.doProcessMessage(message);
        
        //send a response only if processed msg is not itself a response
        //and if method is not void return type (in that case ack was
        //already sent)
        /*
        if(!isResponse && !message.isVoidMethodReturnState()) {
            //ReplicationState response = 
            //    ReplicationState.createResponseFrom(message);
            this.doSendResponse(response);
        }
         */
    }    
    
    public void processMessagePrevious(ReplicationState message) {
        //FIXME complete
        if(ReplicationState.isBroadcastState(message)) {
            processBroadcastMessage(message);
            return;
        }
        
        ReplicationState response = 
            ReplicationState.createResponseFrom(message);
        //send a response before further processing only if processed 
        //msg is not itself a response and if method is a void return type
        //FIXME this send will be removed if upstream ack send works
        /* removing this for test
        if(!message.isReturnMessage() && message.isVoidMethodReturnState()) {
            this.doSendResponse(response);
        }
         */         

        boolean isResponse = this.doProcessMessage(message);
        //send a response only if processed msg is not itself a response
        //and if method is not void return type (in that case ack was
        //already sent)
        if(!isResponse && !message.isVoidMethodReturnState()) {
            /*
            ReplicationState response = 
                ReplicationState.createResponseFrom(message);
             */
            this.doSendResponse(response);
        }
    }
    
    //return true if message is processResponse
    public boolean doProcessMessage(ReplicationState message) {
        boolean result = false;
        String methodName = getProcessMethodName(message);
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("in ReplicationSingleSignOn>>doProcessMessage:methodName=" + methodName);
        }
        //System.out.println("in ReplicationSingleSignOn>>doProcessMessage:methodName=" + methodName);
        try {
            Class myClass = this.getClass();
            myClass.getMethod(
                methodName,
                    new Class[]{ message.getClass() }).invoke(
                        this, new Object[]{ message });
        } catch (IllegalAccessException ex) {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("in ReplicationSingleSignOn>>doProcessMessage:methodName=" + methodName + "illegalAccessException");              
            }            
        } catch (NoSuchMethodException ex) {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("in ReplicationSingleSignOn>>doProcessMessage:methodName=" + methodName + "noSuchMethodException");              
            }             
        } catch (InvocationTargetException ex) {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("in ReplicationSingleSignOn>>doProcessMessage:methodName=" + methodName + "invocationTargetException");
                _logger.fine("invocationException.getCause()= " + ex.getCause());
            }                      
            ex.printStackTrace();
        }              
        if(methodName.equals("processResponse")) {
            result = true;
        }
        return result;
    }
    
    private String getProcessMethodName(ReplicationState message) {
        String command = message.getCommand();
        return "process" + camelCase(command);
    }
    
    /**
     * this method strips out all non-alpha characters; camelCases the result
     *
     * @param inputString
     */     
    private String camelCase(String inputString) {
        String strippedString = stripNonAlphas(inputString);
        String firstLetter = (strippedString.substring(0, 1)).toUpperCase();
        String remainingPart = 
            (strippedString.substring(1, strippedString.length())).toLowerCase();
        return firstLetter + remainingPart;
    }
    
    /**
     * this method strips out all non-alpha characters
     *
     * @param inputString
     */     
    private String stripNonAlphas(String inputString) {
        StringBuffer sb = new StringBuffer(50);
        for(int i=0; i<inputString.length(); i++) {
            char nextChar = inputString.charAt(i);
            if(Character.isLetter(nextChar)) {
                sb.append(nextChar);
            }
        }
        return sb.toString();
    }    
    
    /**
    * send the response
    *
    * @param ssoEntryState 
    *   The replication state response
    */    
    public void doSendResponse(ReplicationState ssoEntryState) {
               
        StorePool storePool = this.getSSOStorePool();
        ReplicationSSOStorePoolElement repStore = null;

        try
        {                        
            repStore = (ReplicationSSOStorePoolElement) storePool.take();
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("GOT ReplicationSSOStore from pool");
            }
            try
            {
                repStore.setContainer(this.getContainer());
                repStore.setParent(this);
                //_logger.finest("ReplicationStore has container = " + this.getContainer());
                //_logger.finest("ENTERING repStore.sendResponse");
                ((ReplicationSSOStore)repStore).sendResponse(ssoEntryState);
                //_logger.finest("FINISHED repStore.sendResponse");
            } catch (Exception ex) {
                //FIXME handle exception from sendResponse
                //log error but allow processing to continue in spite of failure
                ex.printStackTrace();
            } finally {
                repStore.setContainer(null);
                if(repStore != null) {
                    try
                    {
                        storePool.put((ReplicationSSOStorePoolElement)repStore);
                        //_logger.finest("PUT ReplicationSSOStore into pool");
                    } catch (InterruptedException ex1) {}
                }                    
            }                
        } catch (InterruptedException ex) {
            //FIXME handle exception from pool take
        } 
    }
    
    public void processQueryMessage(ReplicationState message, String returnInstance) {
        //FIXME complete
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicatedSingleSignOn>>processQueryMessage:returnInstance= " + returnInstance);
        }        
        ReplicationStateQueryResponse response = this.doProcessQueryMessage(message);
        boolean isResponse = response.isResponse();
        ReplicationState responseState = response.getState();
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("RepSSO:processQueryMessage:after doProcessQueryMessage:response=" + isResponse);
            _logger.fine("RepSSO:processQueryMessage:after doProcessQueryMessage:responseState=" + responseState);            
        }        
        //System.out.println("RepSSO:processQueryMessage:after doProcessQueryMessage:response=" + isResponse);
        //System.out.println("RepSSO:processQueryMessage:after doProcessQueryMessage:responseState=" + responseState);
        //don't send a response to a response
        if(!isResponse && responseState != null) {
            //point-to-point response back to sender
            //System.out.println("processQueryMessage - need to send back result to " + returnInstance);
            //doSendQueryResponse(responseState, this.getInstanceName());
            doSendQueryResponse(responseState, returnInstance);
        }
    } 
    
    public void processBroadcastMessage(ReplicationState message) {
        ReplicationStateQueryResponse response = this.doProcessQueryMessage(message);
        boolean isResponse = response.isResponse();
        ReplicationState responseState = response.getState();
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("processBroadcastMessage:after doProcessQueryMessage:response=" + isResponse);
            _logger.fine("processBroadcastMessage:after doProcessQueryMessage:responseState=" + responseState);
            _logger.fine("processBroadcastMessage:after doProcessQueryMessage:responseStateTrunk=" + responseState.getTrunkState());
            _logger.fine("processBroadcastMessage:after doProcessQueryMessage:responseStateAttr=" + responseState.getState());
            _logger.fine("processBroadcastMessage:after doProcessQueryMessage:responseStateVer=" + responseState.getVersion());
        }
        //System.out.println("processBroadcastMessage:after doProcessQueryMessage:response=" + isResponse);
        //System.out.println("processBroadcastMessage:after doProcessQueryMessage:responseState=" + responseState);
        //System.out.println("processBroadcastMessage:after doProcessQueryMessage:responseStateTrunk=" + responseState.getTrunkState());
        //System.out.println("processBroadcastMessage:after doProcessQueryMessage:responseStateAttr=" + responseState.getState());
        //System.out.println("processBroadcastMessage:after doProcessQueryMessage:responseStateVer=" + responseState.getVersion());

        //don't send a response to a response
        if(!isResponse) {
            //point-to-point response back to sender
            //System.out.println("processBroadcastMessage - need to send back result");
            //doSendQueryResponse(responseState, this.getInstanceName());
            doSendQueryResponse(responseState, message.getInstanceName());
            /*
            ReplicationState response = 
                ReplicationState.createResponseFrom(message);
            this.doSendResponse(response);
             */
        }
    }    
    
    public void processBroadcastMessagePrevious(ReplicationState message) {
        //FIXME complete
        ReplicationStateQueryResponse response = this.doProcessQueryMessage(message);
        boolean isResponse = response.isResponse();
        ReplicationState responseState = response.getState();
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("processBroadcastMessage:after doProcessQueryMessage:response=" + isResponse);
            _logger.fine("processBroadcastMessage:after doProcessQueryMessage:responseState=" + responseState);                      
        }
        //don't send a response to a response
        if(!isResponse) {
            //point-to-point response back to sender
            //System.out.println("processBroadcastMessage - need to send back result");
            doSendQueryResponse(responseState, this.getInstanceName());
            /*
            ReplicationState response = 
                ReplicationState.createResponseFrom(message);
            this.doSendResponse(response);
             */
        }
    }
    
    /**
    * send the response
    *
    * @param sessionState 
    *   The replication state response
    * @param instanceName  the name of the target instance
    */    
    public void doSendQueryResponse(ReplicationState ssoEntryState, String instanceName) {
               
        StorePool storePool = this.getSSOStorePool();
        ReplicationSSOStorePoolElement repStore = null;

        try
        {                        
            repStore = (ReplicationSSOStorePoolElement) storePool.take();
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("GOT ReplicationSSOStore from pool");
            }
            try
            {
                repStore.setContainer(this.getContainer());
                repStore.setParent(this);
                //_logger.finest("ReplicationSSOStore has container = " + this.getContainer());
                //_logger.finest("ENTERING repStore.sendQueryResponse");
                ((ReplicationSSOStore)repStore).sendQueryResponse(ssoEntryState, instanceName);
                //_logger.finest("FINISHED repStore.sendQueryResponse");
            } catch (Exception ex) {
                //FIXME handle exception from sendResponse
                //log error but allow processing to continue in spite of failure
                ex.printStackTrace();
            } finally {
                repStore.setContainer(null);
                if(repStore != null) {
                    try
                    {
                        storePool.put((ReplicationSSOStorePoolElement)repStore);
                        //_logger.finest("PUT ReplicationSSOStore into pool");
                    } catch (InterruptedException ex1) {}
                }                    
            }                
        } catch (InterruptedException ex) {
            //FIXME handle exception from pool take
        } 
    }       
    
    //return true if message is processQueryResponse
    public ReplicationStateQueryResponse doProcessQueryMessage(ReplicationState message) {
        //FIXME
        ReplicationState resultState = null;
        //System.out.println("in ReplicationSingleSignOn>>doProcessQueryMessage");
        String methodName = getProcessMethodName(message);
        //System.out.println("in ReplicationSingleSignOn>>doProcessQueryMessage:methodName=" + methodName);
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("in ReplicationSingleSignOn>>doProcessQueryMessage");
            _logger.fine("in ReplicationSingleSignOn>>doProcessQueryMessage:methodName=" + methodName);
        }        
        try {
            Class myClass = this.getClass();
            resultState = (ReplicationState) myClass.getMethod(
                methodName,
                    new Class[]{ message.getClass() }).invoke(
                        this, new Object[]{ message });           
        } catch (IllegalAccessException ex) {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("in ReplicationSingleSignOn>>doProcessQueryMessage:methodName=" + methodName + "illegalAccessException");              
            }                        
        } catch (NoSuchMethodException ex) {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("in ReplicationSingleSignOn>>doProcessQueryMessage:methodName=" + methodName + "noSuchMethodException");              
            }                        
        } catch (InvocationTargetException ex) {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("in ReplicationSingleSignOn>>doProcessQueryMessage:methodName=" + methodName + "invocationTargetException");
                _logger.fine("invocationException.getCause()= " + ex.getCause());
            }             
            ex.printStackTrace();
        }            
        boolean isResponse = methodName.equals("processBroadcastresponse");
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationSingleSignOn>>doProcessQueryMessage:resultState=" + resultState);
        }
        return new ReplicationStateQueryResponse(resultState, isResponse);
    }
    
    public void processValvesave(ReplicationState message) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("in ReplicationSingleSignOn>>processValvesave");
        }        
        //System.out.println("in ReplicationSingleSignOn>>processValvesave");        
        this.putInReplicationCache(message);
    }
    
    public void processRemove(ReplicationState message) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("in ReplicationSingleSignOn>>processRemove");
        }        
        //System.out.println("in ReplicationSingleSignOn>>processRemove");        
        this.removeFromReplicationCache(message);
    }    

    public void processUpdatelastaccesstime(ReplicationState message) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("in ReplicationSingleSignOn>>processUpdatelastaccesstime");
        }        
        //System.out.println("in ReplicationSingleSignOn>>processUpdatelastaccesstime");        
        ReplicationState storedReplica 
            = this.getFromReplicationCache((String)message.getId());
        if(storedReplica != null) {
            storedReplica.setLastAccess(message.getLastAccess());
            storedReplica.setVersion(message.getVersion());
        }        
    }
    
    public ReplicationState processBroadcastfindsession(ReplicationState queryState) {
        //complete query and send back response
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("in " + this.getClass().getName() + ">>processBroadcastfindSession:instance: " + getInstanceName());
            _logger.fine("in " + this.getClass().getName() + ">>processBroadcastfindSession:id=" + queryState.getId());                        
        }        
        //System.out.println("in " + this.getClass().getName() + ">>processBroadcastfindSession:instance: " + getInstanceName());
        //System.out.println("in " + this.getClass().getName() + ">>processBroadcastfindSession:id=" + queryState.getId() + "dumping:");
        //Thread.dumpStack();
        ReplicationState replicaState 
            = findReplicatedState(queryState);
        ReplicationState returnState = null;
        if(replicaState != null) {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("processBroadcastfindsession:REPLICA_FOUND:replicaStateVersion:" + replicaState.getVersion());
                _logger.fine("processBroadcastfindsession:REPLICA_FOUND:replicaState:" + replicaState.getTrunkState());
                _logger.fine("processBroadcastfindsession:REPLICA_FOUND:replicaAttrState" + replicaState.getState());                     
            }            
            //System.out.println("processBroadcastfindsession:REPLICA_FOUND:replicaStateVersion:" + replicaState.getVersion());
            //System.out.println("processBroadcastfindsession:REPLICA_FOUND:trunkState:" + replicaState.getTrunkState());
            //System.out.println("processBroadcastfindsession:REPLICA_FOUND:state" + replicaState.getState());
            returnState = ReplicationState.createQueryResponseFrom(replicaState);
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("processBroadcastfindsession:replicaStateResponseVersion:" + returnState.getVersion());                  
            }            
            //System.out.println("processBroadcastfindsession:replicaStateResponseVersion:" + returnState.getVersion());
            //FIXME may want to wait for ack before doing this
            //FIXME waiting for Jxta fix to put this next line back in
            //replicatedSessions.remove(replicaState.getId());
            //while here check and remove from manager cache if present
            this.clearFromManagerCache((String)queryState.getId());
        }
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("in " + this.getClass().getName() + ">>processBroadcastfindSession:returnState=" + returnState);
        }          
        //System.out.println("in " + this.getClass().getName() + ">>processBroadcastfindSession:returnState=" + returnState);
        return returnState;

    } 
    
    protected ReplicationState findReplicatedState(ReplicationState queryState) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("findReplicatedState:id = " + queryState.getId());
        }              
        return this.getFromReplicationCache( (String)queryState.getId() );
    }
    
    protected void clearFromManagerCache(String id) {
        /* FIXME later
        Session sess = null;
        try {
            sess = this.findSessionFromCacheOnly(id);
        } catch (IOException ex) {}
        if(sess != null) {                               
            this.removeSessionFromManagerCache(sess);
        } 
         */
    }
    
    public void processResponse(ReplicationState message) {
        //complete processing response - not sending response to a response
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("IN" + this.getClass().getName() + ">>processResponse");            
        }        
        ReplicationResponseRepository.putEntry(message);
    }    

    public ReplicationState processBroadcastresponse(ReplicationState queryResponseState) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("in " + this.getClass().getName() + ">>processBroadcastresponse:queryResponseState=" + queryResponseState);            
        }
        //System.out.println("in " + this.getClass().getName() + ">>processBroadcastresponse:queryResponseState=" + queryResponseState);
        //ReplicationResponseRepository.putEntry(queryResponseState);
        ReplicationResponseRepository.putFederatedEntry(queryResponseState);
        return queryResponseState;
    }    
    
    //end message processing methods
    
    /**
     * Sleep for the duration specified by the <code>ssoReapInterval</code>
     * property.
     */
    private void threadSleep() {

        try {
            Thread.sleep(getReapInterval() * 1000L);
        } catch (InterruptedException e) {
        }
    }


   /**
     * Start the background thread that will periodically check for
     * SSO timeouts.
     */
    private void threadStart() {

        if (thread != null)
            return;

        threadDone = false;
        String threadName = "ReplicationSingleSignOnExpiration";
        thread = new Thread(this, threadName);
        thread.setDaemon(true);
        thread.start();
    }


   /**
     * Stop the background thread that is periodically checking for
     * SSO timeouts.
     */
    private void threadStop() {

        if (thread == null)
            return;

        threadDone = true;
        thread.interrupt();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        thread = null;

    }


    /**
     * The background thread that checks for SSO timeouts and shutdown.
     */
    public void run() {

        // Loop until the termination semaphore is set
        while (!threadDone) {
            threadSleep();
            processExpires();
            processUpdateLat();	
        }
    }    
    
}
