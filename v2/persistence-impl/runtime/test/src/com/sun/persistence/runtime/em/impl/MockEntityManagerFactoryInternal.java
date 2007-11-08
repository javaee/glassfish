/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

package com.sun.persistence.runtime.em.impl;

import javax.naming.Reference;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContextType;
import com.sun.org.apache.jdo.pm.PersistenceManagerFactoryInternal;
import com.sun.persistence.runtime.em.EntityManagerFactoryInternal;

/**
 * Internal interface to the factory providing access to entity managers.
 * 
 * @author Dave Bristor
 */
public class MockEntityManagerFactoryInternal implements EntityManagerFactoryInternal {

    public MockEntityManagerFactoryInternal() {
    }
    
    /* Implement EntityManagerFactory */

    public EntityManager createEntityManager() {
        return null;
    }

    public EntityManager createEntityManager(PersistenceContextType type) {
        return null;
    }

    public EntityManager getEntityManager() {
        return null;
    }

    public void close() {
    }


    public boolean isOpen() {
        return true;
    }

    /* Implement EntityManagerFactoryInternal */

    public PersistenceManagerFactoryInternal getPersistenceManagerFactory() {
        return null;
    }

    /**
     * Tests if this factory is configured for providing JTA entity managers.
     * @return <code>true<code> if this factory returns JTA entity managers
     */
    public boolean isJtaAware() {
        return true;
    }
    
    /* Implement Referenceable */
    public Reference getReference() {
        return null;
    }
}
