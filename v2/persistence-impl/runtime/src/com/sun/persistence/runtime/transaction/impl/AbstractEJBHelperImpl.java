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
 * AbstractEJBHelperImpl.java
 *
 * Created on June 7, 2005
 */


package com.sun.persistence.runtime.transaction.impl;

import javax.transaction.*;

import com.sun.org.apache.jdo.ejb.EJBHelper;
import com.sun.org.apache.jdo.util.I18NHelper;
import com.sun.org.apache.jdo.util.ApplicationLifeCycleEventListener;
import com.sun.persistence.support.JDODataStoreException;
import com.sun.persistence.support.PersistenceManagerFactory;

/**
 * This is an abstract class which is a generic implementation of the
 * EJBHelper interface. Each concrete implementation that extends this
 * class is used for information about the distributed transaction environment.
 *
 * The class that extends this class must implement <code>getTransaction</code>
 * and <code>getUserTransaction</code> methods and replace any other method
 * implementation if it is necessary.
 *
 * Such class must register itself by a static method at class initialization
 * time. For example,
 * <blockquote><pre>
 * class blackHerringEJBHelperImpl extends AbstractEJBHelperImpl {
 *    static EJBImplHelper.register(new blackHerringEJBHelperImpl());
 *    ...
 * }
 * </pre></blockquote>
 */
abstract public class AbstractEJBHelperImpl implements EJBHelper {

    /**
     * I18N message handler
     */  
    private final static I18NHelper msg = I18NHelper.getInstance(
            "com.sun.persistence.runtime.Bundle"); // NOI18N

    /**
     * Identifies the managed environment behavior.
     * @return true as this implementation represents the managed environment.
     */
    public boolean isManaged() {
        return true;
    }

    /**
     * Identify the Transaction context for the calling thread, and return a
     * Transaction instance that can be used to register synchronizations, and
     * used as the key for HashMaps. The returned Transaction must implement
     * <code>equals()</code> and <code>hashCode()</code> based on the global
     * transaction id. <P>All Transaction instances returned by this method
     * called in the same Transaction context must compare equal and return the
     * same hashCode. The Transaction instance returned will be held as the key
     * to an internal HashMap until the Transaction completes. If there is no
     * transaction associated with the current thread, this method returns
     * null.
     * @return the Transaction instance for the calling thread
     */
    abstract public Transaction getTransaction();

    /**
     * Returns the UserTransaction associated with the calling thread.  If there
     * is no transaction currently in progress, this method returns null.
     * @return the UserTransaction instance for the calling thread
     */
    abstract public UserTransaction getUserTransaction();

    /**
     * Translate local representation of the Transaction Status to
     * javax.transaction.Status value if necessary. Otherwise this method should
     * return the value passed to it as an argument. <P>This method is used
     * during afterCompletion callbacks to translate the parameter value passed
     * by the application server to the afterCompletion method.  The return
     * value must be one of: <code>javax.transaction.Status.STATUS_COMMITTED</code>
     * or <code>javax.transaction.Status.STATUS_ROLLED_BACK</code>.
     * @param st local Status value
     * @return the javax.transaction.Status value of the status
     */
    public int translateStatus(int st) {
        return st;
    }

    /**
     * Replace newly created instance of PersistenceManagerFactory with the
     * hashed one if it exists. The replacement is necessary only if the JNDI
     * lookup always returns a new instance. Otherwise this method returns the
     * object passed to it as an argument.
     *
     * PersistenceManagerFactory is uniquely identified by
     * ConnectionFactory.hashCode() if ConnectionFactory is not null; otherwise
     * by ConnectionFactoryName.hashCode() if ConnectionFactoryName is not null;
     * otherwise by the combination of URL.hashCode() + userName.hashCode() +
     * password.hashCode() + driverName.hashCode();
     * @param pmf PersistenceManagerFactory instance to be replaced
     * @return the PersistenceManagerFactory known to the runtime
     */
    public PersistenceManagerFactory replacePersistenceManagerFactory(
            PersistenceManagerFactory pmf) {

        return pmf;
    }

    /**
     * Called in a managed environment to get a Connection from the application
     * server specific resource. In a non-managed environment returns null as it
     * should not be called. This is a generic implementation for the case of
     * javax.sql.DataSource as the resource type.
     * @param resource the application server specific resource.
     * @param username the resource username. If null, Connection is requested
     * without username and password validation.
     * @param password the password for the resource username.
     * @return a Connection as an Object.
     * @throws JDODataStoreException
     */
    public Object getConnection(Object resource, String username,
            String password) {
        Object rc = null;
        if (resource instanceof javax.sql.DataSource) {
            try {
                javax.sql.DataSource ds = (javax.sql.DataSource) resource;
                if (username == null) {
                    rc = ds.getConnection();
                } else {
                    rc = ds.getConnection(username, password);
                }
            } catch (java.sql.SQLException e) {
                handleSQLException(e);
            }
        }
        return rc;
    }

    /**
     * Called in a managed environment to get a non-transactional Connection
     * from the application server specific resource.
     * @param resource the application server specific resource.
     * @param username the resource username. If null, Connection is requested
     * without username and password validation.
     * @param password the password for the resource username.
     * @return a Connection as an Object.
     * @throws JDODataStoreException
     */
    abstract public Object getNonTransactionalConnection(
            Object resource, String username, String password);

    /**
     * Called in a managed environment to access a TransactionManager for
     * managing local transaction boundaries and registering synchronization for
     * call backs during completion of a local transaction.
     * @return javax.transaction.TransactionManager
     */
    abstract public TransactionManager getLocalTransactionManager();

    /**
     * Set environment specific default values for the given
     * PersistenceManagerFactory. In most app servers optimistic and
     * retainValues flags should be false. For any other settings this method
     * should be overritten.
     * @param pmf the PersistenceManagerFactory.
     */
    public void setPersistenceManagerFactoryDefaults(
            PersistenceManagerFactory pmf) {
        pmf.setOptimistic(false);
        //pmf.setRetainValues(false);
    }

    /** Called at the beginning of the Transaction.beforeCompletion() to
     * register the component with the app server only if necessary.
     * The component argument is an array of Objects.
     * The first element is com.sun.persistence.support.Transaction
     * object responsible for transaction completion.
     * The second element is com.sun.persistence.support.PersistenceManager
     * object that has been associated with the Transaction context for the
     * calling thread.
     * The third element is javax.transaction.Transaction object that has been
     * associated with the given instance of PersistenceManager.
     * The return value is passed unchanged to the delistBeforeCompletion method.
     *
     * @param   component       an array of Objects
     * @return  implementation-specific Object
     */
    public Object enlistBeforeCompletion(Object component) {
        return null;
    }

    /** Called at the end of the Transaction.beforeCompletion() to
     * de-register the component with the app server if necessary.
     * The parameter is the return value from enlistBeforeCompletion, 
     * and can be any Object.
     *
     * @param   im      implementation-specific Object
     */
    public void delistBeforeCompletion(Object im) {
    }


    /**
     * @inheritDoc
     */
    public void registerApplicationLifeCycleEventListener(
            ApplicationLifeCycleEventListener listener) {
    }

    /** Convert java.sql.SQLException into a JDODataStoreException
     * @param e the java.sql.SQLException
     * @throws JDODataStoreException
     */
    public void handleSQLException(java.sql.SQLException e) {
        String sqlState = e.getSQLState();
        int errorCode = e.getErrorCode();
 
        if (sqlState == null) {
            throw new JDODataStoreException(
                    msg.msg(
                    "connection.connectionfactoryimpl.sqlexception", // NOI18N
                    "null", "" + errorCode), e); // NOI18N
        } else {
            throw new JDODataStoreException(
                    msg.msg(
                    "connection.connectionfactoryimpl.sqlexception", // NOI18N
                    sqlState, "" + errorCode), e); // NOI18N
        }
    }
}
