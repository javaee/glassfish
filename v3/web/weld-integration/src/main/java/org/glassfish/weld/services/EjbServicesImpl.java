/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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


package org.glassfish.weld.services;

import com.sun.enterprise.deployment.*;

import java.util.HashSet;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.glassfish.ejb.api.EjbContainerServices;
import org.glassfish.weld.ejb.EjbDescriptorImpl;
import org.glassfish.weld.ejb.SessionObjectReferenceImpl;

import org.jboss.weld.ejb.api.SessionObjectReference;
import org.jboss.weld.ejb.spi.EjbServices;
import org.jboss.weld.ejb.spi.EjbDescriptor;
import org.jboss.weld.ejb.spi.InterceptorBindings;

import org.jvnet.hk2.component.Habitat;

/**
 */
public class EjbServicesImpl implements EjbServices
{

    private Habitat habitat;

    public EjbServicesImpl(Habitat h) {
        habitat = h;
    }

   
   /**
    * Request a reference to an EJB session object from the container. If the
    * EJB being resolved is a stateful session bean, the container should ensure
    * the session bean is created before this method returns.
    * 
    * @param ejbDescriptor the ejb to resolve
    * @return a reference to the session object
    */
    public SessionObjectReference resolveEjb(EjbDescriptor<?> ejbDescriptor) {

        SessionObjectReference sessionObj = null;

        // All we need to do is create a reference based on one of the beans'
        // client views, so just choose one and get its corresponding portable
        // JNDI name.

        String globalJndiName = getDefaultGlobalJndiName(ejbDescriptor);
        if( globalJndiName != null ) {
            try {

                InitialContext ic = new InitialContext();

                Object ejbRef = ic.lookup(globalJndiName);

                EjbContainerServices containerServices = habitat.getByContract(EjbContainerServices.class);

                sessionObj = new SessionObjectReferenceImpl(containerServices, ejbRef);

            } catch(NamingException ne) {
               throw new IllegalStateException("Error resolving session object reference for ejb name " +
                       ejbDescriptor.getBeanClass() + " and jndi name " + globalJndiName, ne);
            }
        }  else {
           throw new IllegalArgumentException("Not enough type information to resolve ejb for " +
               " ejb name " + ejbDescriptor.getBeanClass());
        }

	    return sessionObj;

    }
   

    private String getDefaultGlobalJndiName(EjbDescriptor ejbDesc) {

        EjbSessionDescriptor sessionDesc = (EjbSessionDescriptor)
                ((EjbDescriptorImpl) ejbDesc).getEjbDescriptor();

        String clientView = null;

        if( sessionDesc.isLocalBean() ) {
            clientView = sessionDesc.getEjbClassName();
        } else if( sessionDesc.getLocalBusinessClassNames().size() == 1) {
            clientView = sessionDesc.getLocalBusinessClassNames().iterator().next();
        } else if( sessionDesc.getRemoteBusinessClassNames().size() == 1) {
            clientView = sessionDesc.getRemoteBusinessClassNames().iterator().next();
        }

        return (clientView != null) ? sessionDesc.getPortableJndiName(clientView) : null;

    }

    // TODO  ****  Implement
    public void registerInterceptors(EjbDescriptor<?> ejbDescriptor, InterceptorBindings interceptorBindings) {

    }

    public void cleanup() {

    }

}
