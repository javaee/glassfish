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


package com.sun.persistence.runtime.sqlstore.sql.select.impl;

import com.sun.org.apache.jdo.pm.PersistenceManagerInternal;
import com.sun.persistence.support.JDODataStoreException;

import java.sql.ResultSet;

public interface ResultElement {
    /**
     * Gets the result corresponding to this element from the given ResultSet
     * @param pm The given <code>PersistenceManager</code>
     * @param rs The given <code>ResultSet</code>
     * @return Result corresponding to this element from the given ResultSet
     * @throws JDODataStoreException that wraps underlying SQLException
     */
    Object getResult(PersistenceManagerInternal pm, ResultSet rs)
            throws JDODataStoreException;

    /**
     * Get sql text for this element
     * @return The sql text for this result Element
     */
    String getSQLText();
}
