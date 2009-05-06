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

import com.sun.enterprise.naming.util.LogFacade;

import javax.naming.Context;
import javax.naming.NamingException;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.logging.Logger;

public class SerialContextProviderImpl implements SerialContextProvider {

    static Logger _logger = LogFacade.getLogger();

    private TransientContext rootContext;


    protected SerialContextProviderImpl(TransientContext rootContext)
            throws RemoteException {
        this.rootContext = rootContext;
    }

    /**
     * Lookup the specified name.
     *
     * @return the object orK context bound to the name.
     * @throws NamingException if there is a naming exception.
     * @throws if              there is an RMI exception.
     */

    public Object lookup(String name)
            throws NamingException, RemoteException {
        try {
            _logger.fine(" SerialContextProviderImpl :: lookup " + name);

            return rootContext.lookup(name);
        } catch (NamingException ne) {
            throw ne;
        } catch (Exception e) {
            _logger.severe("Exception occurred : " + e.getMessage());
            RemoteException re = new RemoteException("", e);
            throw re;

        }
    }

    /**
     * Bind the object to the specified name.
     *
     * @throws NamingException if there is a naming exception.
     * @throws if              there is an RMI exception.
     */

    public void bind(String name, Object obj)
            throws NamingException, RemoteException {

        rootContext.bind(name, obj);
    }

    /**
     * Rebind the object to the specified name.
     *
     * @throws NamingException if there is a naming exception.
     * @throws if              there is an RMI exception.
     */

    public void rebind(String name, Object obj)
            throws NamingException, RemoteException {

        rootContext.rebind(name, obj);
    }

    /**
     * Unbind the specified object.
     *
     * @throws NamingException if there is a naming exception.
     * @throws if              there is an RMI exception.
     */

    public void unbind(String name)
            throws NamingException, RemoteException {

        rootContext.unbind(name);
    }

    /**
     * Rename the bound object.
     *
     * @throws NamingException if there is a naming exception.
     * @throws if              there is an RMI exception.
     */

    public void rename(String oldname, String newname)
            throws NamingException, RemoteException {

        rootContext.rename(oldname, newname);
    }

    public Hashtable list() throws RemoteException {

        return rootContext.list();
    }

    /**
     * List the contents of the specified context.
     *
     * @throws NamingException if there is a naming exception.
     * @throws if              there is an RMI exception.
     */

    public Hashtable list(String name) throws NamingException, RemoteException {
        Hashtable ne = rootContext.listContext(name);
        return ne;
    }

    /**
     * Create a subcontext with the specified name.
     *
     * @return the created subcontext.
     * @throws NamingException if there is a naming exception.
     * @throws if              there is an RMI exception.
     */

    public Context createSubcontext(String name)
            throws NamingException, RemoteException {

        Context ctx = rootContext.createSubcontext(name);
        return ctx;
    }

    /**
     * Destroy the subcontext with the specified name.
     *
     * @throws NamingException if there is a naming exception.
     * @throws if              there is an RMI exception.
     */

    public void destroySubcontext(String name)
            throws NamingException, RemoteException {

        rootContext.destroySubcontext(name);
    }

}






