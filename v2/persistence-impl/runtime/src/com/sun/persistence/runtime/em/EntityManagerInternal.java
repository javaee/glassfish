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

import javax.persistence.EntityManager;
import com.sun.org.apache.jdo.pm.PersistenceManagerInternal;

/**
 * Internal interface to interact with the persistence context.
 * 
 * @author Martin Zaun
 */
public interface EntityManagerInternal extends EntityManager {

    /**
     * Returns the internally used <code>PersistenceManager</code>
     * associated with this entity manager.
     * @return the current <code>PersistenceManager</code>
     */
    PersistenceManagerInternal getPersistenceManager();
}
