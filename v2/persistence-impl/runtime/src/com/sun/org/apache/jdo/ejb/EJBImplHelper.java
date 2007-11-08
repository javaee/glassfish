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
 * EJBImplHelper.java
 *
 * Created on December 15, 2000, 10:15 AM
 */
package com.sun.org.apache.jdo.ejb;

import javax.transaction.*;

import com.sun.org.apache.jdo.util.ApplicationLifeCycleEventListener;
import com.sun.persistence.support.JDODataStoreException;
import com.sun.persistence.support.PersistenceManagerFactory;

/** Provides an isolation layer for the implementation to get information 
 * from the server and transaction manager without knowing specifically 
 * which server or transaction manager is running.
 * Calls corresponding methods on the registered class which implements 
 * EJBHelper interface.
 * 
 * @author Marina Vatkina
 */
public class EJBImplHelper {
    
   /** Reference to a class that implements EJBHelper interface for this
    * particular application server
    */
    static EJBHelper myHelper = DefaultEJBHelperImpl.getInstance();
 
   /** Register class that implements EJBHelper interface
    * Should be called by a static method at class initialization time.
    * If null is passed, sets the reference to the DefaultEJBHelperImpl.
    *
    * @param h application server specific implemetation of the
    * EJBHelper interface.
    */
    public static void registerEJBHelper (EJBHelper h) {
        myHelper = h;
        if (myHelper == null) {
            myHelper = DefaultEJBHelperImpl.getInstance();
        }
    }

   /** Returns Transaction instance that can be used to register
    * synchronizations. In a non-managed environment or if there is no
    * transaction associated with the current thread, this method
    * returns null.
    *
    * @see EJBHelper#getTransaction()
    * @return the Transaction instance for the calling thread
    */
    public static Transaction getTransaction() {
        return myHelper.getTransaction();
    }

   /** Returns the UserTransaction associated with the calling thread.
    * In a non-managed environment or if there is no transaction
    * currently in progress, this method returns null.
    *
    * @see EJBHelper#getUserTransaction()
    * @return the UserTransaction instance for the calling thread
    */
    public static UserTransaction getUserTransaction() {
        return myHelper.getUserTransaction();
    }

    /** Called in a managed environment to access a TransactionManager
     * for managing local transaction boundaries and synchronization
     * for local transaction completion.
     *
     * @return javax.transaction.TransactionManager
     */
    public static TransactionManager getLocalTransactionManager() {
        return myHelper.getLocalTransactionManager();
    }

    /** Identifies the managed environment behavior.
     * @return true if there is a helper class registered. 
     */
    public static boolean isManaged() {
        return myHelper.isManaged();
    }

   /** Translates local representation of the Transaction Status to 
    * javax.transaction.Status value. In a non-managed environment
    * returns the value passed to it as an argument.
    *
    * @see EJBHelper#translateStatus(int st)
    * @param 	st 	Status value
    * @return 	the javax.transaction.Status value of the status 
    */ 
    public static int translateStatus(int st) {
        return myHelper.translateStatus(st);
    }

   /** Returns the hashed instance of internal PersistenceManagerFactory 
    * that compares equal to the newly created instance or the instance 
    * itself if it is not found. In a non-managed environment returns
    * the value passed to it as an argument.
    *
    * @see EJBHelper#replacePersistenceManagerFactory(
    * 	PersistenceManagerFactory pmf)
    * @param 	pmf 	PersistenceManagerFactory instance to be replaced
    * @return 	the PersistenceManagerFactory known to the runtime
    */
    public static PersistenceManagerFactory replacePersistenceManagerFactory(
        PersistenceManagerFactory pmf) {
        return myHelper.replacePersistenceManagerFactory(pmf);
    }

   /** Called at the beginning of the Transaction.beforeCompletion()
    * to register the component with the app server if necessary. In a
    * non-managed environment or if the delistBeforeCompletion method
    * does not use the value, this method returns null. 
    *
    * @see EJBHelper#enlistBeforeCompletion(Object component)
    * @param 	component 	an array of Objects
    * @return implementation-specific Object
    */
    public static Object enlistBeforeCompletion(Object component) {
        return myHelper.enlistBeforeCompletion(component);
    }

   /** Called a non-managed environment at the end of the
    * Transaction.beforeCompletion() to de-register the component with
    * the app server if necessary.
    *
    * @see EJBHelper#delistBeforeCompletion(Object im)
    * @param im implementation-specific Object
    */
    public static void delistBeforeCompletion(Object im) {
        myHelper.delistBeforeCompletion(im);
    }

    /**
     * Called in a managed environment to get a Connection from the application
     * server specific resource. In a non-managed environment throws an
     * Exception as it should not be called.
     * @param resource the application server specific resource.
     * @param username the resource username. If null, Connection is requested
     * without username and password validation.
     * @param password the password for the resource username.
     * @return a Connection as a Object.
     * @throws JDODataStoreException
     */
    public static Object getConnection(Object resource,
            String username, String password) {
        return myHelper.getConnection(resource, username, password);
    }
 
    /**
     * Called in a managed environment to get a non-transactional Connection
     * from the application server specific resource. In a non-managed
     * environment throws an Exception as it should not be called.
     * @param resource the application server specific resource.
     * @param username the resource username. If null, Connection is requested
     * without username and password validation.
     * @param password the password for the resource username.
     * @return a Connection as a Object.
     * @throws JDODataStoreException
     */
    public static Object getNonTransactionalConnection(
            Object resource, String username, String password) {
        return myHelper.getNonTransactionalConnection(
                resource, username, password);
    }

    /**
     * Called to register a ApplicationLifeCycleEventListener. If
     * ApplicationLifeCycle management is active (typically in managed
     * environment), the registered listener will receive a call back for
     * lifecycle events.
     * @param listener An instance of ApplicationLifeCycleEventListener.
     */
    public static void registerApplicationLifeCycleEventListener(
            ApplicationLifeCycleEventListener listener) {
        myHelper.registerApplicationLifeCycleEventListener(listener);
    }
 
    /**
     * This is the default implementation of the EJBHelper interface for
     * a non-mananged environment execution. In the managed environment the
     * application server specific implementation registers itself with the
     * EJBHelper to override this behavior.
     */
    protected static class DefaultEJBHelperImpl implements EJBHelper {

        private static final DefaultEJBHelperImpl instance = 
                new DefaultEJBHelperImpl();

        /**
         * Returns instance of this class.
         */
        public static DefaultEJBHelperImpl getInstance() {
            return instance;
        }

        /**
         * Identifies the non-managed environment behavior.
         * @return false.
         */
        public boolean isManaged() {
            return false;
        }

        /**
         * In a non-managed environment there is no transaction associated with
         * the current thread, this method returns null.
         * @return null;
         * @see EJBHelper#getTransaction()
         */
        public Transaction getTransaction() {
            return null;
        }

        /**
         * In a non-managed environment there is no transaction currently in
         * progress, this method returns null.
         * @return the null.
         * @see EJBHelper#getUserTransaction()
         */
        public UserTransaction getUserTransaction() {
            return null;
        }

        /**
         * In a non-managed environment there is no TransactionManager
         * available, this method returns null.
         * @return null
         */
        public TransactionManager getLocalTransactionManager() {
            return null;
        }

        /**
         * In a non-managed environment returns the value passed to it as an
         * argument.
         * @param local Status value
         * @return the status value
         * @see EJBHelper#translateStatus(int st)
         */
        public int translateStatus(int st) {
            return st;
        }

        /**
         * In a non-managed environment returns the value passed to it as an
         * argument.
         * @param pmf PersistenceManagerFactory instance to be replaced
         * @return the pmf value.
         * @see EJBHelper#replacePersistenceManagerFactory(
         *      PersistenceManagerFactory pmf)
         */
        public PersistenceManagerFactory replacePersistenceManagerFactory(
                PersistenceManagerFactory pmf) {
            return pmf;
        }

        /**
         * Set environment specific default values for the given
         * PersistenceManagerFactory. In a non-managed this is a no-op.
         * @param pmf the PersistenceManagerFactory.
         */
        public void setPersistenceManagerFactoryDefaults(
                PersistenceManagerFactory pmf) {
        }

        /**
         * Called at the beginning of the Transaction.beforeCompletion() to
         * register the component with the app server if necessary. The
         * component argument is an array of Objects. The first element is
         * com.sun.persistence.support.Transaction object responsible for transaction
         * completion. The second element is com.sun.persistence.support.PersistenceManager
         * object that has been associated with the Transaction context for the
         * calling thread. The third element is javax.transaction.Transaction
         * object that has been associated with the given instance of
         * PersistenceManager. The return value is passed unchanged to the
         * delistBeforeCompletion method.
         * @param component an array of Objects
         * @return implementation-specific Object
         */
        public Object enlistBeforeCompletion(Object component) {
            return null;
        }

        /**
         * Called at the end of the Transaction.beforeCompletion() to
         * de-register the component with the app server if necessary. The
         * parameter is the return value from enlistBeforeCompletion, and can be any Object.
         * @param im implementation-specific Object
         */
        public void delistBeforeCompletion(Object im) {
        }
        /**
         * Called in a managed environment to get a Connection from the
         * application server specific resource. In a non-managed environment
         * returns null
         * @param resource the application server specific resource.
         * @param username the resource username. If null, Connection is
         * requested without username and password validation.
         * @param password the password for the resource username.
         * @return null
         */
        public Object getConnection(Object resource,
                String username, String password) {
            return null;
        }
 
        /**
         * Called in a managed environment to get a non-transactional Connection
         * from the application server specific resource. In a non-managed
         * environment returns null.
         * @param resource the application server specific resource.
         * @param username the resource username. If null, Connection is
         * requested without username and password validation.
         * @param password the password for the resource username.
         * @return null
         */
        public Object getNonTransactionalConnection(
                Object resource, String username, String password) {
            return null;
        }

        /**
         * Called to register a ApplicationLifeCycleEventListener. If
         * ApplicationLifeCycle management is active (typically in managed
         * environment), the registered listener will receive a call back for
         * lifecycle events.
         * @param listener An instance of ApplicationLifeCycleEventListener.
         */
        public void registerApplicationLifeCycleEventListener(
                ApplicationLifeCycleEventListener listener) {
            // The default implementation is no-op
        }
    }
}

