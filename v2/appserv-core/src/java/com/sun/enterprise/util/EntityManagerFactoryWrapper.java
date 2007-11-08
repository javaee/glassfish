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
package com.sun.enterprise.util;

import java.io.Serializable;
import java.util.logging.*;
import java.util.Set;
import java.util.Map;

import javax.naming.Reference;

import javax.persistence.*;

import com.sun.enterprise.deployment.types.EntityManagerFactoryReference;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.Application;

import com.sun.enterprise.InvocationManager;
import com.sun.enterprise.Switch;
import com.sun.enterprise.ComponentInvocation;

import com.sun.logging.*;

/**
 * Wrapper for application references to entity manager factories.
 * A new instance of this class will be created for each injected
 * EntityManagerFactory reference or each lookup of an EntityManagerFactory
 * reference within the component jndi environment.    
 *
 * @author Kenneth Saks
 */
public class EntityManagerFactoryWrapper implements EntityManagerFactory,
    Serializable {

    static Logger _logger=LogDomains.getLogger(LogDomains.UTIL_LOGGER);

    static private LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(EntityManagerFactoryWrapper.class);

    private String unitName;
    transient private EntityManagerFactory entityManagerFactory;

    public EntityManagerFactoryWrapper(EntityManagerFactoryReference
                                       referenceDescriptor) {

        unitName = referenceDescriptor.getUnitName();

    }

    private EntityManagerFactory getDelegate() {

        if( entityManagerFactory == null ) {
        
            entityManagerFactory = lookupEntityManagerFactory(unitName);
            
            if( entityManagerFactory == null ) {
                throw new IllegalStateException
                    ("Unable to retrieve EntityManagerFactory for unitName "
                     + unitName);
            }

        }

        return entityManagerFactory;
    }

    public EntityManager createEntityManager() {
        return getDelegate().createEntityManager();
    }

    public EntityManager createEntityManager(Map map) {
        return getDelegate().createEntityManager(map);
    }

    public void close() {
        getDelegate().close();
    }

    public boolean isOpen() {
        return getDelegate().isOpen();
    }


    /**
     * Lookup physical EntityManagerFactory based on current component
     * invocation.  
     * @param emfUnitName unit name of entity manager factory or null if not
     *                    specified.
     * @return EntityManagerFactory or null if no matching factory could be
     *         found.
     **/
    static EntityManagerFactory lookupEntityManagerFactory(String emfUnitName)
    {

        InvocationManager invMgr = Switch.getSwitch().getInvocationManager();
        ComponentInvocation inv  = invMgr.getCurrentInvocation();

        EntityManagerFactory emf = null;

        if( inv != null ) {

            Object descriptor = 
                Switch.getSwitch().getDescriptorFor(inv.getContainerContext());

            emf = lookupEntityManagerFactory(inv.getInvocationType(),
                    emfUnitName, descriptor);
        }
        
        return emf;
    }
    
    public static EntityManagerFactory lookupEntityManagerFactory(int invType,
            String emfUnitName, Object descriptor) {

        Application app = null;
        BundleDescriptor module = null;

        EntityManagerFactory emf = null;

        switch (invType) {

        case ComponentInvocation.EJB_INVOCATION:

            EjbDescriptor ejbDesc = (EjbDescriptor) descriptor;
            module = ejbDesc.getEjbBundleDescriptor();
            app = module.getApplication();

            break;

        case ComponentInvocation.SERVLET_INVOCATION:

            module = (WebBundleDescriptor) descriptor;
            app = module.getApplication();

            break;

        case ComponentInvocation.APP_CLIENT_INVOCATION:

            module = (ApplicationClientDescriptor) descriptor;
            app = module.getApplication();

            break;

        default:

            break;
        }

        // First check module-level for a match.
        if (module != null) {
            if (emfUnitName != null) {
                emf = module.getEntityManagerFactory(emfUnitName);
            } else {
                Set<EntityManagerFactory> emFactories = module
                        .getEntityManagerFactories();
                if (emFactories.size() == 1) {
                    emf = emFactories.iterator().next();
                }
            }
        }

        // If we're in an .ear and no module-level persistence unit was
        // found, check for an application-level match.
        if ((app != null) && (emf == null)) {
            if (emfUnitName != null) {

                emf = app.getEntityManagerFactory(emfUnitName, module);

            } else {
                Set<EntityManagerFactory> emFactories = app
                        .getEntityManagerFactories();
                if (emFactories.size() == 1) {
                    emf = emFactories.iterator().next();
                }
            }
        }

        return emf;
    }

}
