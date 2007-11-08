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

import com.sun.org.apache.jdo.ejb.EJBImplHelper;
import com.sun.org.apache.jdo.impl.pm.PersistenceManagerFactoryImpl;
import com.sun.org.apache.jdo.pm.PersistenceManagerFactoryInternal;
import com.sun.org.apache.jdo.pm.PersistenceManagerInternal;
import com.sun.org.apache.jdo.util.I18NHelper;
import com.sun.persistence.runtime.em.EntityManagerFactoryInternal;
import com.sun.persistence.runtime.em.EntityManagerInternal;
import com.sun.persistence.runtime.sqlstore.impl.SQLPersistenceManagerFactory;

import javax.naming.Reference;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContextType;


/** 
 * Used to create <code>EntityManager</code> instances.
 *
 * @see javax.persistence.EntityManagerFactory
 * @author Martin Zaun
 */
public class EntityManagerFactoryImpl implements EntityManagerFactoryInternal {

    /**
     * I18N message handler
     */
    static protected final I18NHelper msg
        = I18NHelper.getInstance("com.sun.persistence.runtime.Bundle"); //NOI18N

    /** The PersistenceManagerFactory delegate. */
    protected final PersistenceManagerFactoryInternal pmf;
    
    /**
     * Creates a new <code>EntityManagerFactoryImpl</code>.
     */
    public EntityManagerFactoryImpl() {
        pmf = new SQLPersistenceManagerFactory();
        setPMFDefaults();
    }
    
    /**
     * Creates a new <code>EntityManagerFactoryImpl</code>.
     * @param url         URL for the data store connection
     * @param userName    user name for the data store connection 
     * @param password    password for the data store connection
     * @param driverName  driver name for the data store connection
     */
    public EntityManagerFactoryImpl(String url,
                                    String userName,
                                    String password,
                                    String driverName) {
        pmf = new SQLPersistenceManagerFactory(url, userName, password,
                                               driverName);        
        setPMFDefaults();
    }
  
    // ----------------------------------------------------------------------

    /**
     * Create a new EntityManager of PersistenceContextType.TRANSACTION
     * @see javax.persistence.EntityManagerFactory#createEntityManager
     */
    public EntityManager createEntityManager() {
        return createEntityManager(PersistenceContextType.TRANSACTION);
    }
    
    /**
     * Create a new EntityManager of the specified PersistenceContextType.
     * @see javax.persistence.EntityManagerFactory#createEntityManager
     */
    public EntityManager createEntityManager(PersistenceContextType type) {
        assert (type != null);
        assert (type == PersistenceContextType.TRANSACTION
                || type == PersistenceContextType.EXTENDED);
        ensureIsOpen();

        // XXX : remove comments when finally implemented
        // The isOpen method will return true on the returned instance.
        // This method returns a new EntityManager instance (with a new
        // persistence context) every time it is invoked.

        if (EJBImplHelper.isManaged() || 
            (!EJBImplHelper.isManaged() && type == PersistenceContextType.TRANSACTION)) {
            throw new UnsupportedOperationException("not implemented yet");
        }
    
        final PersistenceManagerInternal pm
            = (PersistenceManagerInternal)pmf.getPersistenceManager();
        return new EntityManagerImpl(this, pm);
    }
    
    /**
     * Get the EntityManager bound to the current JTA transaction.
     * @see javax.persistence.EntityManagerFactory#getEntityManager
     */
    public EntityManager getEntityManager() {
        // XXX : remove comments when finally implemented
        // If there is no persistence context bound to the current
        // JTA transaction, a new persistence context is created and
        // associated with the transaction.
        // If there is an existing persistence context bound to
        // the current JTA transaction, it is returned.
        // If no JTA transaction is in progress, an EntityManager
        // instance is created that will be bound to subsequent
        // JTA transactions.
        ensureIsOpen();
        ensureJtaAware();
    
        final PersistenceManagerInternal pm
            = (PersistenceManagerInternal)pmf.getPersistenceManager();
        return new EntityManagerImpl(this, pm);
    }
    
    /**
     * Close this factory, releasing any resources that might be
     * held by this factory.
     * @see javax.persistence.EntityManagerFactory#close
     */
    public void close() {
        // XXX : remove comments when finally implemented
        // After invoking this method, all methods
        // on the EntityManagerFactory instance will throw an
        // IllegalStateException, except for isOpen, which will return
        // false.
        ensureIsOpen();
        pmf.close();
    }
    
    /**
     * Indicates whether or not this factory is open.
     * @see javax.persistence.EntityManagerFactory#isOpen
     */
    public boolean isOpen() {
        return !pmf.isClosed();
    }

    /**
     * Retrieves the Reference of this object.
     * @see javax.naming.Referenceable#getReference
     */
    public Reference getReference() {
        // XXX: verify that this is all that is needed.
        return new Reference(EntityManagerFactory.class.getName());
    }
    
    // ----------------------------------------------------------------------

    /** 
     * Set the user name for the data store connection.
     * @param userName the user name for the data store connection.
     */
    public void setConnectionUserName(String userName) {
        ensureIsOpen();
        pmf.setConnectionUserName(userName);
    }
  
    /**
     * Get the user name for the data store connection.
     * @return    the user name for the data store connection.
     */
    public String getConnectionUserName() {
        ensureIsOpen();
        return pmf.getConnectionUserName();
    }

    /**
     * Set the password for the data store connection.
     * @param password the password for the data store connection.
     */
    public void setConnectionPassword(String password) {
        ensureIsOpen();
        pmf.setConnectionPassword(password);
    }
  
    /**
     * Get the password for the data store connection.  Protected so 
     * not just anybody can get the password.
     * @return password the password for the data store connection.
     */
/*
    protected String getConnectionPassword() {
        ensureIsOpen();
        return pmf.getConnectionPassword();
    }
*/
  
    /**
     * Set the URL for the data store connection.
     * @param URL the URL for the data store connection.
     */
    public void setConnectionURL(String URL) {
        ensureIsOpen();
        pmf.setConnectionURL(URL);
    }
  
    /**
     * Get the URL for the data store connection.
     * @return the URL for the data store connection.
     */
    public String getConnectionURL() {
        ensureIsOpen();
        return pmf.getConnectionURL();
    }

    /**
     * Set the driver name for the data store connection.
     * @param driverName the driver name for the data store connection.
     */
    public void setConnectionDriverName(String driverName) {
        ensureIsOpen();
        pmf.setConnectionDriverName(driverName);
    }

    /**
     * Get the connection factory for the data store connection.
     * @return the connection factory for the data store connection.
     */
    public Object getConnectionFactory() {
        ensureIsOpen();
        return pmf.getConnectionFactory();
    }

    /**
     * Set the connection factory for the data store connection.
     * @param connectionFactory the connection factory instance to use.
     */
    public void setConnectionFactory(Object connectionFactory) {
        ensureIsOpen();
        pmf.setConnectionFactory(connectionFactory);
    }

    /**
     * Get the connection factory name for the data store connection.
     * @return the connection factory name for the data store connection.
     */
    public String getConnectionFactoryName() {
        ensureIsOpen();
        return pmf.getConnectionFactoryName();
    }

    /**
     * Set the connection factory name for the data store connection.
     * @param name the connection factory name for the ata store connection.
     */
    public void setConnectionFactoryName(String name) {
        ensureIsOpen();
        pmf.setConnectionFactoryName(name);
    }

   /**
     * Get the driver name for the data store connection.
     * @return the driver name for the data store connection.
     */
    public String getConnectionDriverName() {
        ensureIsOpen();
        return pmf.getConnectionDriverName();
    }

    /**
     * Returns the internally used <code>PersistenceManagerFactory</code>
     * associated with this factory
     * @see EntityManagerInternal#getPersistenceManagerFactoryInternal
     */
    public PersistenceManagerFactoryInternal getPersistenceManagerFactory() {
        ensureIsOpen();
        return pmf;
    }

    /** 
     * Configures this factory to return JTA or resource-local entity managers.
     */
    public void setJtaAware(boolean isJtaAware) {
        ensureIsOpen();
        ((SQLPersistenceManagerFactory)pmf).setJtaAware(isJtaAware);
    }  

    /**
     * Tests if this factory is configured for providing JTA entity managers.
     * @see com.sun.persistence.runtime.em.EntityManagerFactoryInternal#isJtaAware
     */
    public boolean isJtaAware() {
        ensureIsOpen();
        return ((SQLPersistenceManagerFactory)pmf).isJtaAware();
    }

    public synchronized boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof EntityManagerFactoryImpl))
            return false;

        return pmf.equals(o);
    }

    public synchronized int hashCode() {
        return pmf.hashCode();
    }


    // ----------------------------------------------------------------------

    protected final void ensureIsOpen() {
        if (!isOpen()) {
            throw new IllegalStateException(
                msg.msg("runtime.entitymanagerfactoryimpl.isclosed"));//NOI18N
        }
    }

    protected final void ensureJtaAware() {
        if (!((SQLPersistenceManagerFactory)pmf).isJtaAware()) {
            throw new IllegalStateException(
                msg.msg("runtime.entitymanagerfactoryimpl.notjtaaware"));//NOI18N
        }
    }

    /**
     * Sets PersistenceManagerFactory defaults for the expected
     * entity lifecycle behavior.
     */
    protected void setPMFDefaults() {
        pmf.setNontransactionalRead(true);
        pmf.setNontransactionalWrite(true);
        pmf.setRetainValues(true);
        // rollback behavior is undefined
        pmf.setRestoreValues(false);
        // can be enabled later if needed
        pmf.setOptimistic(false);
    }
}
