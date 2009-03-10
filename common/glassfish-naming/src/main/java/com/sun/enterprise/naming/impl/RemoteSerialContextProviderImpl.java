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
package com.sun.enterprise.naming.impl;

import javax.rmi.PortableRemoteObject;
import java.rmi.*;

import javax.naming.*;

import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.CORBA.Policy;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.glassfish.internal.api.Globals;

import javax.rmi.CORBA.Tie;

import java.util.logging.*;
import java.util.Hashtable;


/**
 * This class is the implementation of the Remote SerialContextProvider
 *
 * @author Sheetal Vartak
 */

public class RemoteSerialContextProviderImpl 
    extends SerialContextProviderImpl {

    static public final String SERIAL_CONTEXT_PROVIDER_NAME =
        "SerialContextProvider";

    private ORB orb;

    private RemoteSerialContextProviderImpl(ORB orb, TransientContext rootContext)
	    throws RemoteException {

	    super(rootContext);

        this.orb = orb;

	    PortableRemoteObject.exportObject(this);
    }

   /**
     * Create the remote object and publish it in the CosNaming name service.
     */
    static public void initSerialContextProvider(ORB orb, TransientContext rootContext)
	    throws RemoteException {

        try {
	        SerialContextProviderImpl impl =
		        new RemoteSerialContextProviderImpl(orb, rootContext);

            Tie servantsTie = javax.rmi.CORBA.Util.getTie(impl);
            
            //servantsTie.orb(ORBManager.getORB());
            //org.omg.CORBA.Object provider = servantsTie.thisObject());

	        // Create a CORBA objref for SerialContextProviderImpl using a POA
	        POA rootPOA = (POA) orb.resolve_initial_references("RootPOA");
	    
	        Policy[] policy = new Policy[2];
	        policy[0] = rootPOA.create_implicit_activation_policy(
			    ImplicitActivationPolicyValue.IMPLICIT_ACTIVATION);
	        policy[1] = rootPOA.create_lifespan_policy(
		    LifespanPolicyValue.PERSISTENT);

	        POA poa = rootPOA.create_POA("SerialContextProviderPOA", null,
					 policy);
	        poa.the_POAManager().activate();
	        org.omg.CORBA.Object provider = poa.servant_to_reference(
							(Servant)servantsTie);
            
            // put object in NameService
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContext ncRef = NamingContextHelper.narrow(objRef);
            NameComponent nc = 
                new NameComponent(SERIAL_CONTEXT_PROVIDER_NAME, "");
            NameComponent path[] = {nc};
            ncRef.rebind(path, provider);

        } catch (Exception ex) {

            _logger.log(Level.SEVERE,
                 "enterprise_naming.excep_in_insertserialcontextprovider",ex);
            
            RemoteException re = 
                new RemoteException("initSerialCtxProvider error");
            re.initCause(ex);
            throw re;
        }
    }
        
    public Object lookup(String name) throws NamingException, RemoteException {

        Object obj = super.lookup(name);

        // If CORBA object, resolve here in server to prevent a
	    // another round-trip to CosNaming.
	    try {
	        if( obj instanceof Reference ) {
		        Reference ref = (Reference) obj;

		        if( ref.getFactoryClassName().equals(GlassfishNamingManagerImpl.IIOPOBJECT_FACTORY) ) {

                    Hashtable env = new Hashtable();
                    env.put("java.naming.corba.orb", orb);

                    obj = javax.naming.spi.NamingManager.getObjectInstance
                            (obj, new CompositeName(name), null, env);
                }

		    }
	    } catch(Exception e) {
	        RemoteException re = new RemoteException("", e);
            throw re;
        }

        return obj;
   }
}
