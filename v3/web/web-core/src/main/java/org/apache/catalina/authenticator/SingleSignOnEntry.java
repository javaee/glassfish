/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
 *
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
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

import org.apache.catalina.Session;
import org.apache.catalina.session.StandardSession;

import java.security.Principal;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A private class representing entries in the cache of authenticated users.
 */
public class SingleSignOnEntry {

    private static final Logger log = Logger.getLogger(
        SingleSignOnEntry.class.getName());

    protected String id = null;

    protected String authType = null;

    protected char[] password = null;

    protected Principal principal = null;

    protected Session sessions[] = new Session[0];

    protected String username = null;

    protected String realmName = null;

    protected long lastAccessTime;

    public SingleSignOnEntry(String id, Principal principal, String authType,
                             String username, char[] password,
                             String realmName) {
        super();
        this.id = id;
        this.principal = principal;
        this.authType = authType;
        this.username = username;
        this.password = ((password != null) ? ((char[])password.clone()) : null);
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
        for (int i = 0; i < sessions.length; i++) {
            if (session == sessions[i])
                return false;
        }
        Session results[] = new Session[sessions.length + 1];
        System.arraycopy(sessions, 0, results, 0, sessions.length);
        results[sessions.length] = session;
        sessions = results;
        session.addSessionListener(sso);

        return true;
    }

    public synchronized void removeSession(Session session) {
        Session[] nsessions = new Session[sessions.length - 1];
        for (int i = 0, j = 0; i < sessions.length; i++) {
            if (session == sessions[i])
                continue;
            nsessions[j++] = sessions[i];
        }
        sessions = nsessions;
    }


    /**
     * Returns true if this SingleSignOnEntry does not have any sessions
     * associated with it, and false otherwise.
     *
     * @return true if this SingleSignOnEntry does not have any sessions
     * associated with it, and false otherwise
     */
    public synchronized boolean isEmpty() {
        return (sessions.length == 0);
    }


    /**
     * Expires all sessions associated with this SingleSignOnEntry
     *
     * @param reverse the reverse map from which to remove the sessions as
     * they are being expired
     */
    public synchronized void expireSessions(Map<Session, String> reverse) {
        for (int i = 0; i < sessions.length; i++) {
            if (log.isLoggable(Level.FINE)) {
                log.fine(" Invalidating session " + sessions[i]);
            }

            // Remove from reverse cache first to avoid recursion
            synchronized (reverse) {
                reverse.remove(sessions[i]);
            }
        
            //6406580 START
            /*
            // Invalidate this session
            sessions[i].expire();
             */
            // Invalidate this session
            // if it is not already invalid(ated)
            if( (sessions[i]).getIsValid() ) {
                sessions[i].expire();
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
     * Gets the name of the authentication type originally used to authenticate
     * the user associated with the SSO.
     *
     * @return "BASIC", "CLIENT_CERT", "DIGEST", "FORM" or "NONE"
     */
    public String getAuthType() {
        return authType;
    }

    /**
     * Gets the password credential (if any) associated with the SSO.
     *
     * @return  the password credential associated with the SSO, or
     *          <code>null</code> if the original authentication type
     *          does not involve a password.
     */
    public char[] getPassword() {
        return password;
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
}
