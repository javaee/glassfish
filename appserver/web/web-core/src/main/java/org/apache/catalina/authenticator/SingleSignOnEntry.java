/*
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.catalina.authenticator;

import org.apache.catalina.LogFacade;
import org.apache.catalina.Session;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A private class representing entries in the cache of authenticated users.
 */
public class SingleSignOnEntry {

    private static final Logger log = LogFacade.getLogger();

    protected String id = null;

    protected String authType = null;

    protected Principal principal = null;

    protected Set<Session> sessions = new HashSet<Session>();

    protected String username = null;

    protected String realmName = null;

    protected long lastAccessTime;

    protected AtomicLong version = null;

    public SingleSignOnEntry(String id, long ver,
                             Principal principal, String authType,
                             String username, String realmName) {
        super();
        this.id = id;
        this.version = new AtomicLong(ver);
        this.principal = principal;
        this.authType = authType;
        this.username = username;
        this.realmName = realmName;
        this.lastAccessTime = System.currentTimeMillis();
    }

    /**
     * Adds the given session to this SingleSignOnEntry if it does not
     * already exist.
     * 
     * @return true if the session was added, false otherwise
     */
    public synchronized boolean addSession(SingleSignOn sso, Session session) {
        boolean result = sessions.add(session);
        if (result) {
            session.addSessionListener(sso);
        }

        return true;
    }

    public synchronized void removeSession(Session session) {
        sessions.remove(session);
    }


    /**
     * Returns true if this SingleSignOnEntry does not have any sessions
     * associated with it, and false otherwise.
     *
     * @return true if this SingleSignOnEntry does not have any sessions
     * associated with it, and false otherwise
     */
    public synchronized boolean isEmpty() {
        return (sessions.size() == 0);
    }


    /**
     * Expires all sessions associated with this SingleSignOnEntry
     *
     */
    public synchronized void expireSessions() {
        for (Session session: sessions) {
            if (log.isLoggable(Level.FINE)) {

                log.log(Level.FINE, " Invalidating session " + session);
            }
        
            //6406580 START
            /*
            // Invalidate this session
            session.expire();
             */
            // Invalidate this session
            // if it is not already invalid(ated)
            if( (session).getIsValid() ) {
                session.expire();
            }
            //6406580 END
        }
    }

    /**
     * Gets the id of this SSO entry.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the id version of this SSO entry
     */
    public long getVersion() {
        return version.get();
    }

    /**
     * Gets the name of the authentication type originally used to authenticate
     * the user associated with the SSO.
     *
     * @return "BASIC", "CLIENT_CERT", "DIGEST", "FORM" or "NONE"
     */
    public String getAuthType() {
        return authType;
    }

    /**
     * Gets the <code>Principal</code> that has been authenticated by
     * the SSO.
     */
    public Principal getPrincipal() {
        return principal;
    }

    /**
     * Gets the username provided by the user as part of the authentication
     * process.
     */
    public String getUsername() {
        return username;
    }

    public String getRealmName() {
        return realmName;
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(long lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    public long incrementAndGetVersion() {
        return version.incrementAndGet();
    }
}
