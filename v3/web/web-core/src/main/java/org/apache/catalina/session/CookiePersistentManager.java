/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package org.apache.catalina.session;

import org.apache.catalina.Session;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.util.HexUtils;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import javax.servlet.http.*;

/**
 * Session manager for cookie-based persistence, where cookies carry session
 * state.
 */

public class CookiePersistentManager extends StandardManager {

    private final Set<String> sessionIds = new HashSet<String>();

    // The name of the cookies that carry session state
    private String cookieName;

    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
    }

    @Override
    public void add(Session session) {
        synchronized (sessionIds) {
            if (!sessionIds.add(session.getIdInternal())) {
                throw new IllegalArgumentException("Session with id " + session.getIdInternal() +
                        " already present");
            }
            int size = sessionIds.size();
            if (size > maxActive) {
                maxActive = size;
            }
        }
    }

    @Override
    public Session findSession(String id, HttpServletRequest request) throws IOException {
        synchronized (sessionIds) {
            if (!sessionIds.contains(id)) {
                // Session was never created
                return null;
            }
        }
        if (cookieName == null) {
            return null;
        }
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        String value = null;
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return parseSession(cookie.getValue());
            }
        }
        return null;
    }

    @Override
    public void clearSessions() {
        synchronized (sessionIds) {
            sessionIds.clear();
        }
    }

    @Override
    public Session[] findSessions() {
        return null;
    }

    @Override
    public void remove(Session session) {
        synchronized (sessionIds) {
            sessionIds.remove(session.getIdInternal());
        }
    }

    @Override
    public Cookie toCookie(Session session) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        if (getContainer() != null) {
            oos = ((StandardContext) getContainer()).createObjectOutputStream(
                    new BufferedOutputStream(baos));
        } else {
            oos = new ObjectOutputStream(new BufferedOutputStream(baos));
        }
        oos.writeObject(session);
        oos.close();
        return new Cookie(cookieName, HexUtils.convert(baos.toByteArray()));
    }

    /*
     * Parses the given string into a session, and returns it.
     * *
     * The given string is supposed to contain the serialized representation of a session in Base64-encoded form.
     */
    private Session parseSession(String value) throws IOException {
        ObjectInputStream ois;
        BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(HexUtils.convert(value)));
        if (container != null) {
            ois = ((StandardContext)container).createObjectInputStream(bis);
        } else {
            ois = new ObjectInputStream(bis);
        }
        try {
            return (Session) ois.readObject();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
