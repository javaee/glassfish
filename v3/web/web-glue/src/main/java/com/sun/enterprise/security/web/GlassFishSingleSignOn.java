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

package com.sun.enterprise.security.web;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Container;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.HttpResponse;
/** CR 6411114 (Lifecycle implementation moved to ValveBase)
import org.apache.catalina.Lifecycle;
*/
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.Session;
import org.apache.catalina.SessionEvent;
import org.apache.catalina.SessionListener;
import org.apache.catalina.authenticator.Constants;
import org.apache.catalina.authenticator.SingleSignOn;
import org.apache.catalina.authenticator.SingleSignOnEntry;
import org.apache.catalina.Realm;
//6406580 START
import org.apache.catalina.session.StandardSession;
//6406580 END
import com.sun.logging.LogDomains;

/**
 * A <strong>Valve</strong> that supports a "single sign on" user experience,
 * where the security identity of a user who successfully authenticates to one
 * web application is propogated to other web applications in the same
 * security domain. 
 *
 * @author Jyri Virkki (first implementation) 
 * @author Jean-Francois Arcand
 */
public class GlassFishSingleSignOn
    extends SingleSignOn
    /** CR 6411114 (Lifecycle implementation moved to ValveBase)
    implements Lifecycle, SessionListener, Runnable, SingleSignOnMBean {
    */
    // START CR 6411114
    implements SessionListener, Runnable, SingleSignOnMBean {
    // END CR 6411114

    // ----------------------------------------------------- Instance Variables

    /**
     * The log used by this class.
     */
    private static final Logger logger = LogDomains.getLogger(
        GlassFishSingleSignOn.class, LogDomains.WEB_LOGGER);

    /**
     * The background thread.
     */
    private Thread thread = null;

    /**
     * The background thread completion semaphore.
     */
    private boolean threadDone = false;
        
    /**
     * The interval (in seconds) between checks for expired sessions.
     */
    private int ssoReapInterval = 60;

    /**
     * Max idle time (in seconds) for SSO entries before being elegible
     * for purging.
     * A value less than zero indicates that SSO entries are supposed
     * to never expire.
     */
    private int ssoMaxInactive = 300;

    //-------------------------------------------------------------- Monitoring
    
    /**
     * Number of cache hits
     */
    private AtomicInteger hitCount = new AtomicInteger(0);
    
    /**
     * Number of cache misses
     */
    private AtomicInteger missCount = new AtomicInteger(0);
    
    // ------------------------------------------------------------- Properties


    /**
     * Return expire thread interval (seconds)
     */
    public int getReapInterval() {
        
        return this.ssoReapInterval;
        
    }

        
    /**
     * Set expire thread interval (seconds)
     */
    public void setReapInterval(int t) {

        this.ssoReapInterval = t;

    }


    /**
     * Return max idle time for SSO entries (seconds)
     */
    public int getMaxInactive() {
        
        return this.ssoMaxInactive;

    }

    /**
     * Set max idle time for SSO entries (seconds)
     */
    public void setMaxInactive(int t) {

        this.ssoMaxInactive = t;

    }


    // ------------------------------------------------------ Lifecycle Methods


    /**
     * Prepare for the beginning of active use of the public methods of this
     * component.  This method should be called after <code>configure()</code>,
     * and before any of the public methods of the component are utilized.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    public void start() throws LifecycleException {

        // START CR 6411114
        if (started)            // Ignore multiple starts
            return;
        // END CR 6411114

        super.start();
        // Start the background reaper thread
        threadStart();

    }


    /**
     * Gracefully terminate the active use of the public methods of this
     * component.  This method should be the last one called on a given
     * instance of this component.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that needs to be reported
     */
    public void stop() throws LifecycleException {

        /** CR 6411114
        super.stop();
        */
        // START CR 6411114
        if (!started)       // Ignore stop if not started
            return;
        // END CR 6411114

        // Stop the background reaper thread
        threadStop();
        // START CR 6411114
        super.stop();
        // END CR 6411114
    }


    // ------------------------------------------------ SessionListener Methods


    /**
     * Acknowledge the occurrence of the specified event.
     *
     * @param event SessionEvent that has occurred
     */
    public void sessionEvent(SessionEvent event) {

        // We only care about session destroyed events
        if (!Session.SESSION_DESTROYED_EVENT.equals(event.getType()))
            return;

        // Look up the single session id associated with this session (if any)
        Session session = event.getSession();
        //S1AS8 6155481 START
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Process session destroyed on " + session);
        }
        //S1AS8 6155481 END
        String ssoId = null;
        synchronized (reverse) {
            ssoId = (String) reverse.get(session);
        }
        if (ssoId == null) {
            return;
        }
  
        // Was the session destroyed as the result of a timeout?
        // If so, we'll just remove the expired session from the
        // SSO.  If the session was logged out, we'll log out
        // of all session associated with the SSO.
        if (session.hasExpired()) {
            removeSession(ssoId, session);
        } else {
            // The session was logged out.
            // Deregister this single session id, invalidating 
            // associated sessions
            deregister(ssoId);
        }
    }


    // ---------------------------------------------------------- Valve Methods


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
    /** IASRI 4665318
    public void invoke(Request request, Response response,
                       ValveContext context)
        throws IOException, ServletException {
     */
    // START OF IASRI 4665318
    public int invoke(Request request, Response response)
        throws IOException, ServletException {
    // END OF IASRI 4665318

        // If this is not an HTTP request and response, just pass them on
        /* GlassFish 6386229
        if (!(request instanceof HttpRequest) ||
                !(response instanceof HttpResponse)) {
            // START OF IASRI 4665318
            // context.invokeNext(request, response);
            // return;
            return INVOKE_NEXT;
            // END OF IASRI 4665318
        }
        */
        HttpServletRequest hreq = (HttpServletRequest) request.getRequest();
        HttpServletResponse hres =
                        (HttpServletResponse) response.getResponse();
        request.removeNote(Constants.REQ_SSOID_NOTE);

        // Has a valid user already been authenticated?
        //S1AS8 6155481 START
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Process request for '" + hreq.getRequestURI() + "'");
        }
        if (hreq.getUserPrincipal() != null) {
            //S1AS8 6155481 START            
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(" Principal '" + hreq.getUserPrincipal().getName()
                            + "' has already been authenticated");
            }
            // START OF IASRI 4665318
            // context.invokeNext(request, response);
            // return;
            return INVOKE_NEXT;
            // END OF IASRI 4665318
        }

        // Check for the single sign on cookie
        //S1AS8 6155481 START
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(" Checking for SSO cookie");
        }
        Cookie cookies[] = hreq.getCookies();
        if (cookies == null) {
            return INVOKE_NEXT;
        }
        Cookie cookie = null;
        for (int i = 0; i < cookies.length; i++) {
            if (Constants.SINGLE_SIGN_ON_COOKIE.equals(cookies[i].getName())) {
                cookie = cookies[i];
                break;
            }
        }
        if (cookie == null) {
            //S1AS8 6155481 START    
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(" SSO cookie is not present");
            }
            //S1AS8 6155481 END
            // START OF IASRI 4665318
            // context.invokeNext(request, response);
            // return;
            return INVOKE_NEXT;
            // END OF IASRI 4665318
        }

        // Get the realm associated with the app of this request.
        // If there is no realm available, do not process SSO.
        Realm realm = request.getContext().getRealm();
        if (realm == null) {
            //S1AS8 6155481 START             
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(" No realm configured for this application, SSO "
                            + "does not apply.");
            }
            //S1AS8 6155481 END            
            // START OF IASRI 4665318
            // context.invokeNext(request, response);
            // return;
            return INVOKE_NEXT;
            // END OF IASRI 4665318
        }
         
        String realmName = realm.getRealmName();
        if (realmName == null) {
            //S1AS8 6155481 START             
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(" No realm configured for this application, SSO "
                            + "does not apply.");
            }
            //S1AS8 6155481 END            
            // START OF IASRI 4665318
            // context.invokeNext(request, response);
            // return;
            return INVOKE_NEXT;
            // END OF IASRI 4665318
        }
         
        if (debug >= 1) {
            //S1AS8 6155481 START             
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("This application uses realm '" + realmName + "'");
            }
         }
        //S1AS8 6155481 END         

        // Look up the cached Principal associated with this cookie value
        //S1AS8 6155481 START         
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(" Checking for cached principal for "
                        + cookie.getValue());
        }
        SingleSignOnEntry entry = lookupEntry(cookie.getValue());
        if (entry != null) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(" Found cached principal '"
                            + entry.principal.getName()
                            + "' with auth type '" + entry.authType
                            + "' in realm '" + entry.realmName + "'");
            }
            //S1AS8 6155481 END            

            // only use this SSO identity if it was set in the same realm
            if (entry.realmName.equals(realmName)) {
                request.setNote(Constants.REQ_SSOID_NOTE, cookie.getValue());
                ((HttpRequest) request).setAuthType(entry.authType);
                ((HttpRequest) request).setUserPrincipal(entry.principal);
                // Touch the SSO entry access time
                entry.lastAccessTime = System.currentTimeMillis();
                // update hit atomic counter
                hitCount.incrementAndGet();
            } else {
                //S1AS8 6155481 START                 
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(" Ignoring SSO entry which does not match "
                                + "application realm '" + realmName + "'");
                }
                // consider this a cache miss, update atomic counter
                missCount.incrementAndGet();
            }
        } else {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(" No cached principal found, erasing SSO cookie");
            }
            cookie.setMaxAge(0);
            hres.addCookie(cookie);
            //update miss atomic counter
            missCount.incrementAndGet();
        }
        //S1AS8 6155481 END
        // Invoke the next Valve in our pipeline
        // START OF IASRI 4665318
        // context.invokeNext(request, response);
        // return;
        return INVOKE_NEXT;
        // END OF IASRI 4665318

    }


    // -------------------------------------------------------- Package Methods


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

        //S1AS8 6155481 START
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Associate sso id " + ssoId + " with session "
                        + session);
        }
        //S1AS8 6155481 END
        SingleSignOnEntry sso = lookupEntry(ssoId);
        if (sso != null) {
            boolean wasAdded = sso.addSession(this, session);
            if (wasAdded) {
                synchronized (reverse) {
                    reverse.put(session, ssoId);
                }
            }
        }
    }

    /**
     * Deregister the specified session.  If it is the last session,
     * then also get rid of the single sign on identifier
     *
     * @param ssoId Single sign on identifier
     * @param session Session to be deregistered
     */
    protected void deregister(String ssoId, Session session) {

        synchronized (reverse) {
            reverse.remove(session);
        }

        SingleSignOnEntry sso = lookupEntry(ssoId);
        if ( sso == null )
            return;

        sso.removeSession( session );

        // see if we are the last session, if so blow away ssoId
        if (sso.isEmpty()) {
            synchronized (cache) {
                sso = (SingleSignOnEntry) cache.remove(ssoId);
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

        //S1AS8 6155481 START        
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Deregistering sso id '" + ssoId + "'");
        }
        //S1AS8 6155481 END 
        // Look up and remove the corresponding SingleSignOnEntry
        SingleSignOnEntry sso = null;
        synchronized (cache) {
            sso = (SingleSignOnEntry) cache.remove(ssoId);
        }

        if (sso == null)
            return;

        // Expire any associated sessions
        sso.expireSessions(reverse);

        // NOTE:  Clients may still possess the old single sign on cookie,
        // but it will be removed on the next request since it is no longer
        // in the cache
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
    protected void register(String ssoId, Principal principal, String authType,
                            String username, String password,
                            String realmName) {

        //S1AS8 6155481 START               
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Registering sso id '" + ssoId + "' for user '"
                        + principal.getName() + " in realm " + realmName
                        + "' with auth type '" + authType + "'");
        }
        //S1AS8 6155481 END
        synchronized (cache) {
            cache.put(ssoId, new SingleSignOnEntry(ssoId,
                                                   principal, 
                                                   authType,
                                                   username, 
                                                   password, 
                                                   realmName));
        }
    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Look up and return the cached SingleSignOn entry associated with this
     * sso id value, if there is one; otherwise return <code>null</code>.
     *
     * @param ssoId Single sign on identifier to look up
     */
    protected SingleSignOnEntry lookupEntry(String ssoId) {

        synchronized (cache) {
            return ((SingleSignOnEntry) cache.get(ssoId));
        }

    }


    /**
     * Invalidate all SSO cache entries that have expired.
     */
    private void processExpires() {

        if (ssoMaxInactive < 0) {
            // SSO entries are supposed to never expire
            return;
        }

        long tooOld = System.currentTimeMillis() - ssoMaxInactive * 1000L;
        //S1AS8 6155481 START
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("SSO expiration started. Current entries: "
                        + cache.size());
        }
        //S1AS8 6155481 END 
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
                    if (sso.sessions.length == 0 &&
                        sso.lastAccessTime < tooOld) {
                        removals.add(key);
                    }
                }
            }

            int removalCount = removals.size();
            //S1AS8 6155481 START            
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("SSO cache will expire " + removalCount
                            + " entries.");
            }
            //S1AS8 6155481 END
            // deregister any elegible sso entries
            for (int i=0; i < removalCount; i++) {
                //S1AS8 6155481 START                
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("SSO expiration removing entry: "
                                + removals.get(i));
                }
                deregister((String)removals.get(i));
            }
            //S1AS8 6155481 END
        } catch (Throwable e) { // don't let thread die
            logger.warning("Caught exception during SingleSignOn expiration: "
                           + e);
        }
    }


    /**
     * Sleep for the duration specified by the <code>ssoReapInterval</code>
     * property.
     */
    private void threadSleep() {

        try {
            Thread.sleep(ssoReapInterval * 1000L);
        } catch (InterruptedException e) {
            ;
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
        String threadName = "SingleSignOnExpiration";
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
            ;
        }

        thread = null;

    }


    // ------------------------------------------------------ Background Thread


    /**
     * The background thread that checks for SSO timeouts and shutdown.
     */
    public void run() {

        // Loop until the termination semaphore is set
        while (!threadDone) {
            threadSleep();
            processExpires();
        }

    }
        
    /**
     * Remove a single Session from a SingleSignOn.  Called when
     * a session is timed out and no longer active.
     *
     * @param ssoId Single sign on identifier from which to remove the session.
     * @param session the session to be removed.
     */
    protected void removeSession(String ssoId, Session session) {

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Removing session " + session.toString() 
                        + " from sso id " + ssoId );
        }

        // Get a reference to the SingleSignOn
        SingleSignOnEntry entry = lookupEntry(ssoId);
        if (entry == null)
            return;

        // Remove the inactive session from SingleSignOnEntry
        entry.removeSession(session);

        // Remove the inactive session from the 'reverse' Map.
        synchronized(reverse) {
            reverse.remove(session);
        }

        // If there are not sessions left in the SingleSignOnEntry,
        // deregister the entry.
        if (entry.isEmpty()) {
            deregister(ssoId);
        }
    }
    
    //-------------------------------------------------- Monitoring Support
    
    /**
     * Gets the number of sessions participating in SSO
     *
     * @return Number of sessions participating in SSO
     */
    public int getActiveSessionCount() {
        return cache.size();
    }

    
    /**
     * Gets the number of SSO cache hits
     *
     * @return Number of SSO cache hits
     */
    public int getHitCount() {
        return hitCount.intValue();
    }

    
    /**
     * Gets the number of SSO cache misses
     *
     * @return Number of SSO cache misses
     */
    public int getMissCount() {
        return missCount.intValue();
    }

}


