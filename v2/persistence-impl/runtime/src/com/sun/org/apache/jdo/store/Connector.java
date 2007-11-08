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

/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.sun.org.apache.jdo.store;

/**
* A Connector is the store-independent means of representing a connection.
* Each different kind of store should implement it's own connector, which
* delegates the operations to its particular kind of connection.
*
* @author Dave Bristor
*/
public interface Connector {

    /**
    * Informs the Connector that a transaction is beginning.
    * @param optimistic If true, then an optimistic transaction is
    * beginning.
    * @throws JDODataStoreException is [@link setRollbackOnly} has been
    * invoked on this Connector.
    */
    public void begin(boolean optimistic);
    
    /**
    * Informs the Connector that the transaction has reached it's
    * beforeCompletion phase.
    * @throws JDODataStoreException is [@link setRollbackOnly} has been
    * invoked on this Connector.
    */
    public void beforeCompletion();
    
    /**
    * Requests that the Connector send all pending database operations to the
    * store.
    * @throws JDODataStoreException is [@link setRollbackOnly} has been
    * invoked on this Connector.
    */
    public void flush();
    
    /**
    * Requests that the Connector make all changes made since the previous
    * commit/rollback permanent and releases any database locks held by the
    * Connector.
    * @throws JDODataStoreException is [@link setRollbackOnly} has been
    * invoked on this Connector.
    */
    public void commit();

    /**
    * Requests that the Connector drop all changes made since the previous
    * commit/rollback and releases any database locks currently held by this
    * Connector.
    */
    public void rollback();

    /**
     * Requests that the Connector put itself in a state such that the only
     * allowable operations is {@link
     * com.sun.org.apache.jdo.store.Connector#getRollbackOnly}. Once set, attempts to 
     * do any other operations will result in a JDODataStoreException.
     */
    public void setRollbackOnly();
    
    /**
     * Indicates whether or not the connector can do operations other than
     * rollback.
     * @return <code>false</code> if the connector can do operations other than
     * rollback.
     */
    public boolean getRollbackOnly();
    
}
