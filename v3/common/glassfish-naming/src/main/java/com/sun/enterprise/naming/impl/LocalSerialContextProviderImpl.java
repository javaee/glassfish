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
import com.sun.enterprise.naming.util.NamingUtilsImpl;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.CompositeName;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.Hashtable;
import org.omg.CORBA.ORB;
import org.glassfish.internal.api.Globals;
import javax.naming.Context;


/**
 * This class is the implementation of the local SerialContextProvider
 *
 * @author Sheetal Vartak
 */

public class LocalSerialContextProviderImpl
        extends SerialContextProviderImpl {


    static Logger _logger = LogFacade.getLogger();

    private NamingUtilsImpl namingUtils = new NamingUtilsImpl();

    private LocalSerialContextProviderImpl(TransientContext rootContext) throws RemoteException {
        super(rootContext);
    }

    static LocalSerialContextProviderImpl initProvider(TransientContext rootContext) {
        try {
            return new LocalSerialContextProviderImpl(rootContext);
        } catch (RemoteException re) {
            _logger.log(Level.SEVERE,
                    "local.provider.null.  Exception occurred. Returning null provider : {0}",
                    new Object[]{re.getMessage()});
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
        Object copyOfObj = namingUtils.makeCopyOfObject(obj);
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
        Object copyOfObj = namingUtils.makeCopyOfObject(obj);
        super.rebind(name, copyOfObj);
    }

    public Object lookup(String name)
            throws NamingException, RemoteException {
        Object obj = super.lookup(name);
        
        try {
            if (obj instanceof Reference) {
                Reference ref = (Reference) obj;

                if (ref.getFactoryClassName().equals
                        (GlassfishNamingManagerImpl.IIOPOBJECT_FACTORY)) {

                    ORB orb = ProviderManager.getProviderManager().getORB();

                    Hashtable env = new Hashtable();
                    if( orb != null ) {

                        env.put("java.naming.corba.orb", orb);

                    }


                    obj = javax.naming.spi.NamingManager.getObjectInstance
                            (obj, new CompositeName(name), null, env);
                    // NOTE : No copy object performed in this case
                    return obj;
                }

            }
       
        } catch (Exception e) {
            RemoteException re = new RemoteException("", e);
            throw re;

        }

        return namingUtils.makeCopyOfObject(obj);
    }
}
