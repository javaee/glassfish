

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
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
package org.apache.tomcat.util.handler;

import java.io.*;
import java.util.*;
import java.security.*;


/**
 * The lowest level component of Jk ( and hopefully Coyote ). 
 *
 * Try to keep it minimal and flexible - add only if you _have_ to add.
 *
 * It is similar in concept and can implement/wrap tomcat3.3 Interceptor, tomcat4.0 Valve,
 * axis Handler, tomcat3.3 Handler, apache2 Hooks etc.
 *
 * Both iterative (Interceptor, Hook ) and recursive ( Valve ) behavior are supported.
 * Named TcHandler because Handler name is too overloaded.
 *
 * The interface allows both stateless and statefull implementations ( like Servlet ).
 *
 * @author Costin Manolache
 */
public abstract class TcHandler {
    public static final int OK=0;
    public static final int LAST=1;
    public static final int ERROR=2;

    protected Hashtable attributes=new Hashtable();
    protected TcHandler next;
    protected String name;
    protected int id;

    // -------------------- Configuration --------------------
    
    /** Set the name of the handler. Will allways be called by
     *  worker env after creating the worker.
     */
    public void setName(String s ) {
        name=s;
    }

    public String getName() {
        return name;
    }

    /** Set the id of the worker. It can be used for faster dispatch.
     *  Must be unique, managed by whoever creates the handlers.
     */
    public void setId( int id ) {
        this.id=id;
    }

    public int getId() {
        return id;
    }
    
    /** Catalina-style "recursive" invocation. A handler is required to call
     *  the next handler if set.
     */
    public void setNext( TcHandler h ) {
        next=h;
    }


    /** Base implementation will just save all attributes. 
     *  It is higly desirable to override this and allow runtime reconfiguration.
     *  XXX Should I make it abstract and force everyone to override ?
     */
    public void setAttribute( String name, Object value ) {
        attributes.put( name, value );
    }

    /** Get an attribute. Override to allow runtime query ( attribute can be
     *  anything, including statistics, etc )
     */
    public Object getAttribute( String name ) {
        return attributes.get(name) ;
    }

    //-------------------- Lifecycle --------------------
    
    /** Should register the request types it can handle,
     *   same style as apache2.
     */
    public void init() throws IOException {
    }

    /** Clean up and stop the handler. Override if needed.
     */
    public void destroy() throws IOException {
    }

    public void start() throws IOException {
    }

    public void stop() throws IOException {
    }

    // -------------------- Action --------------------
    
    /** The 'hook' method. If a 'next' was set, invoke should call it ( recursive behavior,
     *  similar with valve ).
     *
     * The application using the handler can also iterate, using the same semantics with
     * Interceptor or APR hooks.
     *
     * @returns OK, LAST, ERROR Status of the execution, semantic similar with apache
     */
    public abstract int invoke(TcHandlerCtx tcCtx)  throws IOException;



}
