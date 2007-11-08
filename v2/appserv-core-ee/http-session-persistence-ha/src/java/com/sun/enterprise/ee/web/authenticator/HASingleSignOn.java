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
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */

/*
 * HASingleSignOn.java
 *
 * Created on January 9, 2003, 10:25 AM
 */

package com.sun.enterprise.ee.web.authenticator;

/**
 *
 * @author  Sridhar Satuloori
 */
import java.sql.*;
import java.util.*;
import java.io.IOException;
import java.security.Principal;
import java.sql.Connection;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.authenticator.*;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.HttpResponse;
import org.apache.catalina.Realm;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.Session;
import org.apache.catalina.ValveContext;
import org.apache.catalina.session.StandardSession;
import com.sun.enterprise.security.web.SingleSignOn;
import com.sun.enterprise.security.web.SingleSignOnEntry;
import com.sun.web.security.RealmAdapter;
import com.sun.enterprise.web.ShutdownCleanupCapable;

import com.sun.enterprise.ee.web.sessmgmt.*;

public class HASingleSignOn extends SingleSignOn
        implements ShutdownCleanupCapable, HASSO {

    /**
     * The logger to use for logging ALL web container related messages.
     */
    private static Logger _logger = null;
    
    /**
     * The store pool.
     */    
    protected StorePool _pool = null;
    
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
     * The background thread.
     */
    private Thread thread = null;


    /**
     * The background thread completion semaphore.
     */
    private boolean threadDone = false;

    /**
     * The map containing connections used for cleanup during normal shutdown
     */          
    private Map _connectionsMap = Collections.synchronizedMap(new WeakHashMap(50));
    
    /** Creates a new instance of HASingleSignOn */
    public HASingleSignOn() {
        if (_logger == null) {
            _logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);
        }         
    }

    // store pool was set during initialization of this valve
    public void setSSOStorePool(StorePool pool) {
        _pool = pool;
    }

    public StorePool getSSOStorePool() {
        return _pool;
    }

    
    /*
     * Connection cleanup methods
     */

    public void doCloseCachedConnection() {
        //for now no implementation
    } 
    
    public int doShutdownCleanup() {
        return this.closeAllConnections();
    } 
    
    public int closeAllConnections() {

        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("HASingleSignOn.closeAllConnections()");
        }        

        int count = 0;
        Set connections = _connectionsMap.keySet();
        Iterator it = connections.iterator();
        while(it.hasNext()) {
            Connection nextConn = (Connection) it.next();
            if(nextConn != null) {
                try {
                    nextConn.close();
                    count++;
                } catch (SQLException ex) {}
            }
        }
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("HASingleSignOn closed " + count
                           + " connections during shutdown");
        }         
        return count;
    }      
    
    public synchronized void putConnection(Connection conn) {
        _connectionsMap.put(conn, null);
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
            _logger.finest("HASingleSignOn.invoke()");
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
            _logger.finest("HASingleSignOn.lookupEntry(): ssoId=" + ssoId);
        }

        SingleSignOnEntry ssoEntry=null;

        long startTime = System.currentTimeMillis();
        SSOStorePoolElement store = null;
        try {
            store = (SSOStorePoolElement) _pool.take();
            store.setContainer(this.getContainer());
            ssoEntry = super.lookupEntry(ssoId);
            if (ssoEntry != null) {
                return ssoEntry; //return if the sso is in cache
            }
            try{
                ssoEntry = store.loadSSO(ssoId);
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

        long startTime = System.currentTimeMillis();
        SSOStorePoolElement store = null;

        try {
            store = (SSOStorePoolElement) _pool.take();
            store.setContainer(this.getContainer());

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
                    _logger.finest("HASingleSignOn.register(): "
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
        SSOStorePoolElement store = null;
        try {
            store = (SSOStorePoolElement) _pool.take();
                    store.setContainer(this.getContainer());

            synchronized (cache) {
                sso = (SingleSignOnEntry) cache.remove(ssoId);
            }
            try {
                /* replace with line below
                if(!bgCall)
                    store.remove(ssoId);//remove from ssotable
                else
                    store.remove(ssoId,null);
                 */
                store.remove(ssoId);//remove from ssotable                
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
                /* replace this with line below
                if(!bgCall)
                    store.removeInActiveSessions(ssoId); 
                else
                    store.removeInActiveSessions(ssoId,null);
                 */
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
            _logger.finest("HASingleSignOn.associate(): "
                           + "Associate sso id " + ssoId
                           + " with session " + session);
        }

        long startTime = System.currentTimeMillis();
        SSOStorePoolElement store = null;
        try {
            store = (SSOStorePoolElement) _pool.take();
            store.setContainer(this.getContainer());

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
                            "Exception in HASingleSignOn.associate()",
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
        SSOStorePoolElement store = null;
        lifecycle.fireLifecycleEvent(STOP_EVENT, null);
        started = false;

        try {
            store = (SSOStorePoolElement) _pool.take();
            store.setContainer(this.getContainer());
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
                        "Exception in HASingleSignOn.processExpires()",
                        e);
        }
    }


   /**
     * Update all SSO enytries whose LAT has changed
     */
    private void processUpdateLat() {
        long startTime = System.currentTimeMillis();
        SSOStorePoolElement store = null;
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
            	store = (SSOStorePoolElement) _pool.take();
            	store.setContainer(this.getContainer());

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
                        "Exception in HASingleSignOn.processUpdateLat()",
                        e);
        }
    }	

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
        String threadName = "HASingleSignOnExpiration";
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


