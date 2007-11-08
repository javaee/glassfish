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
package com.sun.enterprise.naming;

import java.util.*;
import java.io.*;
import java.rmi.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;


import com.sun.enterprise.connectors.*;

import java.util.logging.*;
import com.sun.logging.*;

public class SerialContextProviderImpl implements SerialContextProvider {

    static Logger _logger = LogDomains.getLogger(LogDomains.JNDI_LOGGER);   

    private TransientContext rootContext;

 
    protected SerialContextProviderImpl(TransientContext rootContext) 
	throws RemoteException {
	this.rootContext = rootContext;
    }

    /**
     * Lookup the specified name.
     * @return the object or context bound to the name.
     * @exception NamingException if there is a naming exception.
     * @exception if there is an RMI exception.
     */

    public Object lookup(String name) 
        throws NamingException, RemoteException {
        try { 
            _logger.fine(" SerialContextProviderImpl :: lookup " + name);
	
            Object obj = rootContext.lookup(name);         
            return obj;
        } catch(NamingException ne) {
            boolean isLoaded = checkAndLoadResource(name);
            _logger.fine("CheckAndLoad Resource of " + name + " was "  + isLoaded );
            if ( isLoaded ) {
                Object i = rootContext.lookup(name);
                return i;
            }
            throw ne;	    
        } catch(Exception e) {
	    _logger.severe("Exception occurred : " + e.getMessage());
            RemoteException re = new RemoteException("", e);
            throw re;

        }
    }

    private boolean checkAndLoadResource(String name) {
        boolean res = false;
        ConnectorRuntime connectorRuntime = ConnectorRuntime.getRuntime();
        if(connectorRuntime.isServer()) {
            res = connectorRuntime.checkAndLoadResource(name);
        }
        return res;
    }


    /**
     * Bind the object to the specified name.
     * @exception NamingException if there is a naming exception.
     * @exception if there is an RMI exception.
     */

    public void bind(String name, Object obj) 
        throws NamingException, RemoteException {

        rootContext.bind(name, obj);
    }

    /**
     * Rebind the object to the specified name.
     * @exception NamingException if there is a naming exception.
     * @exception if there is an RMI exception.
     */

    public void rebind(String name, Object obj) 
        throws NamingException, RemoteException {

        rootContext.rebind(name, obj);
    }

    /**
     * Unbind the specified object.
     * @exception NamingException if there is a naming exception.
     * @exception if there is an RMI exception.
     */

    public void unbind(String name) 
        throws NamingException, RemoteException {

        rootContext.unbind(name);
    }

    /**
     * Rename the bound object.
     * @exception NamingException if there is a naming exception.
     * @exception if there is an RMI exception.
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
     * @exception NamingException if there is a naming exception.
     * @exception if there is an RMI exception.
     */

    public Hashtable list(String name) throws NamingException, RemoteException {
	Hashtable ne = rootContext.listContext(name);
	return ne;
    }

    /**
     * Create a subcontext with the specified name.
     * @return the created subcontext.
     * @exception NamingException if there is a naming exception.
     * @exception if there is an RMI exception.
     */

    public Context createSubcontext(String name)
	throws NamingException, RemoteException {

	Context ctx = rootContext.createSubcontext(name);
	return ctx;
    }

    /**
     * Destroy the subcontext with the specified name.
     * @exception NamingException if there is a naming exception.
     * @exception if there is an RMI exception.
     */

    public void destroySubcontext(String name)
	throws NamingException, RemoteException {

	rootContext.destroySubcontext(name);
    }

}






