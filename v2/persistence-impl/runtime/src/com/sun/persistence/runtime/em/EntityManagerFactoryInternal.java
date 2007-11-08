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

package com.sun.persistence.runtime.em;

import javax.persistence.EntityManagerFactory;
import com.sun.org.apache.jdo.pm.PersistenceManagerFactoryInternal;

/**
 * Internal interface to the factory providing access to entity managers.
 * 
 * @author Martin Zaun
 */
public interface EntityManagerFactoryInternal extends EntityManagerFactory {

    /**
     * Returns the internally used <code>PersistenceManagerFactory</code>
     * associated with this factory.
     * @return the current <code>PersistenceManagerFactory</code>
     */
    PersistenceManagerFactoryInternal getPersistenceManagerFactory();

    /**
     * Tests if this factory is configured for providing JTA entity managers.
     * @return <code>true<code> if this factory returns JTA entity managers
     */
    boolean isJtaAware();
}
