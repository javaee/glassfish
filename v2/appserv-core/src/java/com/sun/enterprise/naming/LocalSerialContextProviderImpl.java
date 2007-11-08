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

import java.rmi.*;
import java.util.logging.*;
import java.util.*;
import java.io.*;
import javax.naming.*;
import com.sun.logging.*;

import com.sun.enterprise.util.*;

/**
 * This class is the implementation of the local SerialContextProvider
 * 
 * @author Sheetal Vartak
 */

public class LocalSerialContextProviderImpl 
    extends SerialContextProviderImpl {

    private static LocalStringManagerImpl localStrings =
	new LocalStringManagerImpl(LocalSerialContextProviderImpl.class);

    static Logger _logger=LogDomains.getLogger(LogDomains.JNDI_LOGGER);

    LocalSerialContextProviderImpl(TransientContext rootContext) throws RemoteException {
	super(rootContext);
    }

    static LocalSerialContextProviderImpl getProvider(
				       TransientContext rootContext) {
	try {
	    return new LocalSerialContextProviderImpl(rootContext);
	} catch (RemoteException re) {
	    _logger.log(Level.SEVERE,
			localStrings.getLocalString("local.provider.null",
						    "Exception occurred. Returning null provider : {0}" , 
						    new Object[] {re.getMessage()}));
	    return null;
	}
    }  

    /**
     * overriding the super.bind() since we need to make a copy of the object
     * before it gets put into the rootContext
     * Remote Provider already does that since when a method is called 
     * on a remote object (in our case the remote provider), 
     * the copies of the method arguments get passed and not the real objects.
     */

    public void bind(String name, Object obj) 
	throws NamingException, RemoteException {

	Object copyOfObj = NamingUtils.makeCopyOfObject(obj);
        super.bind(name, copyOfObj);
    }
    
    
    /**
     * overriding the super.rebind() since we need to make a copy of the object
     * before it gets put into the rootContext. 
     * Remote Provider already does that since when a method is called 
     * on a remote object (in our case the remote provider), 
     * the copies of the method arguments get passed and not the real objects.
     */
    
    public void rebind(String name, Object obj) 
	throws NamingException, RemoteException {

	Object copyOfObj = NamingUtils.makeCopyOfObject(obj);
        super.rebind(name, copyOfObj);
    }

    public Object lookup(String name) 
        throws NamingException, RemoteException {
	Object obj = super.lookup(name);
	// If CORBA object, resolve here in server to prevent a 
	// another round-trip to CosNaming.
	try {
	    if( obj instanceof Reference ) {
		Reference ref = (Reference) obj;
		if( ref.getFactoryClassName().equals
		    (NamingManagerImpl.IIOPOBJECT_FACTORY)) {
		    
		    Hashtable env = new Hashtable();
		    org.omg.CORBA.ORB orb = ORBManager.getORB();
		    env.put("java.naming.corba.orb", orb);
		    obj = javax.naming.spi.NamingManager.getObjectInstance
			(obj, new CompositeName(name), null, env);		 
		    return obj;
		}
	    } 
	    return NamingUtils.makeCopyOfObject(obj);
	} catch(RemoteException re) {
	    throw re;
	} catch(Exception e) {
	    RemoteException re = new RemoteException("", e);
            throw re;

        }
    }
}
