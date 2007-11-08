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
 * EJBHelper.java
 *
 * Created on December 15, 2000, 10:06 AM
 */

package com.sun.org.apache.jdo.ejb;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import com.sun.org.apache.jdo.util.ApplicationLifeCycleEventListener;
import com.sun.persistence.support.JDODataStoreException;
import com.sun.persistence.support.PersistenceManagerFactory;

/** Provides an isolation layer for the implementation to get information 
 * from the server and transaction manager without knowing specifically 
 * which server or transaction manager is running.
 * The class that implements this interface must register itself
 * by a static method at class initialization time.  For example,
 * <blockquote><pre>
 * import com.sun.org.apache.jdo.*;
 * class blackHerringEJBImplHelper implements EJBHelper {
 *    static EJBImplHelper.register(new blackHerringEJBImplHelper());
 *    ...
 * }
 * </pre></blockquote>
 *
 * @author Marina Vatkina
 */  
public interface EJBHelper {

    /** Identifies the managed environment behavior.
     * @return true if this implementation represents the managed environment.
     */
    boolean isManaged();

    /** Returns the UserTransaction associated with the calling thread.  If there
     * is no transaction currently in progress, this method returns null.
     * @return the UserTransaction instance for the calling thread
     */  
    UserTransaction getUserTransaction();

    /** Identify the Transaction context for the calling thread, and return a
     * Transaction instance that can be used to register synchronizations,
     * and used as the key for HashMaps. The returned Transaction must implement
     * <code>equals()</code> and <code>hashCode()</code> based on the global transaction id.
     * <P>All Transaction instances returned by this method called in the same
     * Transaction context must compare equal and return the same hashCode.
     * The Transaction instance returned will be held as the key to an
     * internal HashMap until the Transaction completes. If there is no transaction 
     * associated with the current thread, this method returns null.
     * @return the Transaction instance for the calling thread
     */  
    Transaction getTransaction();

    /**
     * Called in a managed environment to access a TransactionManager for
     * managing local transaction boundaries and registering synchronization for
     * call backs during completion of a local transaction.
     * @return javax.transaction.TransactionManager
     */
    TransactionManager getLocalTransactionManager();

    /**
     * Set environment specific default values for the given
     * PersistenceManagerFactory.
     * @param pmf the PersistenceManagerFactory.
     */
    void setPersistenceManagerFactoryDefaults(PersistenceManagerFactory pmf);

    /** Translate local representation of the Transaction Status to
     * javax.transaction.Status value if necessary. Otherwise this method
     * should return the value passed to it as an argument.
     * <P>This method is used during afterCompletion callbacks to translate
     * the parameter value passed by the application server to the 
     * afterCompletion method.  The return value must be one of:
     * <code>javax.transaction.Status.STATUS_COMMITTED</code> or
     * <code>javax.transaction.Status.STATUS_ROLLED_BACK</code>.
     * @param 	st 	local Status value
     * @return the javax.transaction.Status value of the status
     */
    int translateStatus(int st);

    /** Replace newly created instance of internal PersistenceManagerFactory
     * with the hashed one if it exists. The replacement is necessary only if 
     * the JNDI lookup always returns a new instance. Otherwise this method 
     * returns the object passed to it as an argument.
     *
     * PersistenceManagerFactory is uniquely identified by 
     * ConnectionFactory.hashCode() if ConnectionFactory is 
     * not null; otherwise by ConnectionFactoryName.hashCode() if 
     * ConnectionFactoryName is not null; otherwise 
     * by the combination of URL.hashCode() + userName.hashCode() + 
     * password.hashCode() + driverName.hashCode();
     *
     * @param 	pmf 	PersistenceManagerFactory instance to be replaced
     * @return 	the PersistenceManagerFactory known to the runtime
     */
    PersistenceManagerFactory replacePersistenceManagerFactory(
        PersistenceManagerFactory pmf) ;

    /** Called at the beginning of the Transaction.beforeCompletion() to
     * register the component with the app server if necessary.
     * The component argument is an array of Objects. 
     * The first element is com.sun.persistence.support.Transaction object responsible for 
     * transaction completion.
     * The second element is com.sun.persistence.support.PersistenceManager object that has 
     * been associated with the Transaction context for the calling thread.
     * The third element is javax.transaction.Transaction object that has been 
     * associated with the given instance of PersistenceManager.
     * The return value is passed unchanged to the delistBeforeCompletion method.
     *
     * @param 	component 	an array of Objects
     * @return 	implementation-specific Object
     */
    Object enlistBeforeCompletion(Object component) ;

    /** Called at the end of the Transaction.beforeCompletion() to
     * de-register the component with the app server if necessary.
     * The parameter is the return value from enlistBeforeCompletion, 
     * and can be any Object.
     *
     * @param 	im 	implementation-specific Object
     */
    void delistBeforeCompletion(Object im) ;

    /**
     * Called in a managed environment to get a Connection from the application
     * server specific resource. In a non-managed environment throws an
     * Exception as it should not be called.
     * @param resource the application server specific resource.
     * @param username the resource username. If null, Connection is requested
     * without username and password validation.
     * @param password the password for the resource username.
     * @return a Connection as an Object.
     * @throws JDODataStoreException
     */  
    Object getConnection(Object resource, String username,
            String password);

    /**
     * Called in a managed environment to get a non-transactional Connection
     * from the application server specific resource. In a non-managed
     * environment throws an Exception as it should not be called.
     * @param resource the application server specific resource.
     * @param username the resource username. If null, Connection is requested
     * without username and password validation.
     * @param password the password for the resource username.
     * @return a Connection as an Object.
     * @throws JDODataStoreException
     */  
    Object getNonTransactionalConnection(Object resource,
            String username, String password);

    /**
     * Called to register a ApplicationLifeCycleEventListener. If
     * ApplicationLifeCycle management is active (typically in managed
     * environment), the registered listener will receive a call back for
     * lifecycle events.
     * @param listener An instance of ApplicationLifeCycleEventListener.
     */  
    void registerApplicationLifeCycleEventListener(
            ApplicationLifeCycleEventListener listener);
}

