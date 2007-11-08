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

/*
 * TransactionSynchronization.java
 *
 * Create on August 1, 20001
 */

package com.sun.org.apache.jdo.impl.pm;

import javax.transaction.*;


/**   
 * This is a Synchronization instance associated with the corresponding instance
 * of the TransactionImpl. Is used for synchronization callbacks in a managed
 * environment. This is a separate object to avoid a requirement for a non-managed
 * application have JTA classes in its classpath.
 *  
 * @author Marina Vatkina  
 */ 
class TransactionSynchronization implements Synchronization {
    /** Reference to TransactionImpl instance associated
     * with this instance of TransactionSynchronization
     */
    private TransactionImpl tx = null;

    TransactionSynchronization(TransactionImpl newtx) {
        tx = newtx;
    }

    /**
     * @see javax.transaction.Synchronization#beforeCompletion()
     */
    public void beforeCompletion() {
        tx.beforeCompletion();
    }

    /** 
     * @see javax.transaction.Synchronization#afterCompletion(int status) 
     */ 
    public void afterCompletion(int status) { 
        tx.afterCompletion(status); 
    } 

}
