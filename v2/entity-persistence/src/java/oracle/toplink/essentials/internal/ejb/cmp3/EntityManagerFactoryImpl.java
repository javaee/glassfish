/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
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
package oracle.toplink.essentials.internal.ejb.cmp3;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContextType;

import oracle.toplink.essentials.threetier.ServerSession;
import oracle.toplink.essentials.jndi.JNDIConnector;
import oracle.toplink.essentials.internal.localization.ExceptionLocalization;

/**
* <p>
* <b>Purpose</b>: Provides the implementation for the EntityManager Factory.
* <p>
* <b>Description</b>: This class will store a reference to the active ServerSession.  When a request
* is made for an EntityManager an new EntityManager is created with the ServerSession and returned.
* The primary consumer of these EntityManager is assumed to be either the Container.    There is
* one EntityManagerFactory per deployment.
* @see javax.persistence.EntityManager
* @see oracle.toplink.essentials.ejb.cmp3.EntityManager
* @see oracle.toplink.essentials.ejb.cmp3.EntityManagerFactory
*/

/*  @author  gyorke
 *  @since   TopLink 10.1.3 EJB 3.0 Preview
 */
public class EntityManagerFactoryImpl 
    extends oracle.toplink.essentials.internal.ejb.cmp3.base.EntityManagerFactoryImpl
    implements EntityManagerFactory 
{

    /**
     * Will return an instance of the Factory.  Should only be called by TopLink.
     * @param serverSession
     */
    public EntityManagerFactoryImpl(ServerSession serverSession) {
        super(serverSession);
    }

    /**
     * Will return an instance of the Factory.  Should only be called by TopLink.
     * @param serverSession
     */
    public EntityManagerFactoryImpl(EntityManagerSetupImpl setupImpl, Map properties) {
        super(setupImpl, properties);
    }

    /**
     * PUBLIC:
     * Returns an EntityManager for this deployment
     */
    public EntityManager createEntityManager() {
        return (EntityManager) createEntityManagerImpl(false);
    }
    
    /**
     * PUBLIC:
     * Returns an EntityManager for this deployment
     */
    public EntityManager createEntityManager(Map properties) {
        return (EntityManager) createEntityManagerImpl(properties, false);
    }

    //TODO change the way create works to deal with how the specification works with persistence contexts
    protected oracle.toplink.essentials.internal.ejb.cmp3.base.EntityManagerImpl createEntityManagerImplInternal(Map properties, boolean extended) {
        return new EntityManagerImpl(this, properties, false, extended);
    }
}
