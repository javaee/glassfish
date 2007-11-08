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

import java.util.Iterator;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.org.apache.jdo.pm.PersistenceManagerInternal;
import com.sun.org.apache.jdo.state.StateManagerInternal;
import com.sun.org.apache.jdo.util.I18NHelper;
import com.sun.persistence.support.JDOFatalInternalException;

/**
* StoreManagerImpl provides a base for fully implementing the StoreManager
* interface. 
* <p>
* Please note, this class is changed wrt. the JDO apache version:
* Method newQueryResult is in comments.
*
* @author Dave Bristor
*/
public abstract class StoreManagerImpl implements StoreManager {

    /** Indicates whether current transaction is optimistic or not.  See
     * {@link #flush}. */
    protected boolean optimistic;

    /** Logger instance */
    private static final Log logger = LogFactory.getFactory().getInstance(
        "com.sun.org.apache.jdo.store"); // NOI18N

    /** I18N support. */
    private static final I18NHelper msg =
        I18NHelper.getInstance(StoreManagerImpl.class);

    /** Default constructor. */
    protected StoreManagerImpl() { }

    
    /**
     * Perform any actions required before StateManager's are flushed.
     */
    abstract protected  void preFlush();

    /**
    * Flushes all StateManagerInternals in the given Iterator.  This will in
    * turn cause invocations of insert, delete, and update on this
    * StoreManager. Update, in particular, must be done after this flush
    * method is invoked, so that the value of <code>optimistic</code> is set
    * correctly.
    * @see com.sun.org.apache.jdo.store.StoreManager#flush */
    public void flush(Iterator it, PersistenceManagerInternal pm) {
        this.optimistic = pm.currentTransaction().getOptimistic();

        if (debugging()) {
            logger.debug(
                "SRMImpl.flush: begin, optimistic=" + optimistic); // NOI18N
        }

        preFlush();
        
        if (debugging()) {
            logger.debug(
                "SRMImpl.flush: completed flush commit"); // NOI18N
        }

        boolean err = false;

        while (it.hasNext()) {
            StateManagerInternal sm = (StateManagerInternal)it.next();
            sm.preStore();

            sm.replaceSCOFields();
            sm.flush(this);
            if ( ! sm.isFlushed()) {
                err = true;
                break;
            }
        }

        if (debugging()) {
            logger.debug("SRMImpl.flush: end, err=" + err); // NOI18N
        }

        if (err) {
            throw new JDOFatalInternalException(
                msg.msg("EXC_UnableToFlushAll")); // NOI18N
        }
    }

    /**
     * Returns a QueryResult instance which is then returned as the result of 
     * Query.execute(...). This method allows support for datastore specific 
     * query execution strategies, since each StoreManager can have its own
     * implementation of the QueryResult interface. 
     * @param qrh the helper providing the query tree, the candidates 
     * and the actual parameters.
     * @return a datastore specific query result instance
     */
    // MBO: method newQueryResult is in comments, because QueryResult
    // and QueryResultHelper are defined in the jdoql package.
    // public QueryResult newQueryResult(QueryResultHelper qrh) {
    //    return new BasicQueryResult(qrh);
    // }

    private boolean debugging() {
        return logger.isDebugEnabled();
    }
}
