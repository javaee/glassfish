/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Container;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.HttpResponse;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.Session;
import org.apache.catalina.SessionEvent;
import org.apache.catalina.SessionListener;
import org.apache.catalina.authenticator.Constants;
import org.apache.catalina.Realm;
import org.apache.catalina.session.StandardSession;

/**
 * A private class representing entries in the cache of authenticated users.
 */
public class SingleSignOnEntry {

    private static final Logger log = Logger.getLogger(
        SingleSignOnEntry.class.getName());

    public String id = null;

    public String authType = null;

    public String password = null;

    public Principal principal = null;

    public Session sessions[] = new Session[0];

    public String username = null;

    public String realmName = null;

    public long lastAccessTime;

    public SingleSignOnEntry(String id, Principal principal, String authType,
                             String username, String password,
                             String realmName) {
        super();
        this.id = id;
        this.principal = principal;
        this.authType = authType;
        this.username = username;
        this.password = password;
        this.realmName = realmName;
        this.lastAccessTime = System.currentTimeMillis();
    }

    /**
     * Gets the id of this SSO entry.
     */
    public String getId() {
        return id;
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
    public synchronized void expireSessions(HashMap reverse) {
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
            if( ((StandardSession)sessions[i]).getIsValid() ) {
                sessions[i].expire();
            }
            //6406580 END
        }
    }
}
