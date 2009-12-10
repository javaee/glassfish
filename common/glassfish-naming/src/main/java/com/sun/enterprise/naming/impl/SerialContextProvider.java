/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */
package com.sun.enterprise.naming.impl;

import java.util.*;
import javax.naming.*;
import java.rmi.*;

public interface SerialContextProvider extends Remote {

    /**
     * Lookup the specified name.
     *
     * @return the object or context bound to the name.
     * @throws NamingException if there is a naming exception.
     * @throws if              there is an RMI exception.
     */
    public Object lookup(String name)
            throws NamingException, RemoteException;

    /**
     * Bind the object to the specified name.
     *
     * @throws NamingException if there is a naming exception.
     * @throws if              there is an RMI exception.
     */
    public void bind(String name, Object obj)
            throws NamingException, RemoteException;

    /**
     * Rebind the object to the specified name.
     *
     * @throws NamingException if there is a naming exception.
     * @throws if              there is an RMI exception.
     */
    public void rebind(String name, Object obj)
            throws NamingException, RemoteException;

    /**
     * Unbind the specified object.
     *
     * @throws NamingException if there is a naming exception.
     * @throws if              there is an RMI exception.
     */
    public void unbind(String name)
            throws NamingException, RemoteException;

    /**
     * Rename the bound object.
     *
     * @throws NamingException if there is a naming exception.
     * @throws if              there is an RMI exception.
     */
    public void rename(String oldname, String newname)
            throws NamingException, RemoteException;

    public Hashtable list() throws RemoteException;

    /**
     * List the contents of the specified context.
     *
     * @throws NamingException if there is a naming exception.
     * @throws if              there is an RMI exception.
     */
    public Hashtable list(String name) throws NamingException, RemoteException;

    /**
     * Create a subcontext with the specified name.
     *
     * @return the created subcontext.
     * @throws NamingException if there is a naming exception.
     * @throws if              there is an RMI exception.
     */
    public Context createSubcontext(String name)
            throws NamingException, RemoteException;

    /**
     * Destroy the subcontext with the specified name.
     *
     * @throws NamingException if there is a naming exception.
     * @throws if              there is an RMI exception.
     */
    public void destroySubcontext(String name)
            throws NamingException, RemoteException;
}


