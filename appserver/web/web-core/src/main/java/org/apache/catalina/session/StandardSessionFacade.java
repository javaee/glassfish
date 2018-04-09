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

package org.apache.catalina.session;


import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.Enumeration;


/**
 * Facade for the StandardSession object.
 *
 * @author Remy Maucherat
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:28:00 $
 */

public class StandardSessionFacade
    implements HttpSession {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new session facade.
     */
    public StandardSessionFacade(StandardSession session) {
        super();
        this.session = session;
    }


    /**
     * Construct a new session facade.
     */
    public StandardSessionFacade(HttpSession session) {
        super();
        this.session = session;
    }


    // ----------------------------------------------------- Instance Variables


    /**
     * Wrapped session object.
     */
    private HttpSession session = null;


    // ---------------------------------------------------- HttpSession Methods


    public long getCreationTime() {
        return session.getCreationTime();
    }


    public String getId() {
        return session.getId();
    }


    public long getLastAccessedTime() {
        return session.getLastAccessedTime();
    }


    public ServletContext getServletContext() {
        // FIXME : Facade this object ?
        return session.getServletContext();
    }


    public void setMaxInactiveInterval(int interval) {
        session.setMaxInactiveInterval(interval);
    }


    public int getMaxInactiveInterval() {
        return session.getMaxInactiveInterval();
    }


    /**
     * @deprecated
     */
    public HttpSessionContext getSessionContext() {
        return session.getSessionContext();
    }


    public Object getAttribute(String name) {
        return session.getAttribute(name);
    }


    /**
     * @deprecated
     */
    public Object getValue(String name) {
        return session.getAttribute(name);
    }


    /**
     * @deprecated
     */
    public Enumeration<String> getAttributeNames() {
        return session.getAttributeNames();
    }


    public String[] getValueNames() {
        return session.getValueNames();
    }


    public void setAttribute(String name, Object value) {
        session.setAttribute(name, value);
    }


    /**
     * @deprecated
     */
    public void putValue(String name, Object value) {
        session.setAttribute(name, value);
    }


    public void removeAttribute(String name) {
        session.removeAttribute(name);
    }


    /**
     * @deprecated
     */
    public void removeValue(String name) {
        session.removeAttribute(name);
    }


    public void invalidate() {
        session.invalidate();
    }


    public boolean isNew() {
        return session.isNew();
    }
}
