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
 * PersistenceManagerFactoryImpl.java
 *
 * Created on December 1, 2000
 */
 
package com.sun.org.apache.jdo.impl.pm;

import java.util.*;
import java.security.AccessController;
import java.security.PrivilegedAction;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.org.apache.jdo.ejb.EJBImplHelper;
import com.sun.org.apache.jdo.impl.model.java.runtime.RuntimeJavaModelFactory;
import com.sun.org.apache.jdo.model.jdo.JDOClass;
import com.sun.org.apache.jdo.pm.Accessor;
import com.sun.org.apache.jdo.pm.PersistenceManagerFactoryInternal;
import com.sun.org.apache.jdo.util.I18NHelper;
import com.sun.org.apache.jdo.util.JDORIVersion;
import com.sun.persistence.support.*;
import com.sun.persistence.support.spi.JDOPermission;

/** 
 * This is an abstract PersistenceManagerFactoryImpl class that provides the 
 * StoreManager independent implementation of com.sun.persistence.support.PersistenceManager
 * interface. 
 * <p>
 * Subclasses must override the following methods declared abstract:
 * <ul>
 * <li> {@link #getOptionArray()}
 * <li> {@link #createPersistenceManager(String userid, String password)}
 * <li> {@link #setPMFClassProperty (Properties props)}
 * <li> {@link #encrypt(String s)}
 * <li> {@link #decrypt(String s)}
 * <li> {@link #setCFProperties(Properties p)}
 * <li> {@link #getCFFromProperties(Properties p)}
 * <li> {@link #isConnectionFactoryConfigured()}
 * <li> and all methods from com.sun.org.apache.jdo.pm.PersistenceManagerFactoryInternal.
 * </ul> 
 * <p>
 * Please note, this class is changed wrt. the JDO apache version:
 * The query tree factory method has been removed.
 * 
 * @author  Marina Vatkina
 * @version 0.1
 */

abstract public class PersistenceManagerFactoryImpl implements 
    PersistenceManagerFactoryInternal {

    //
    // PersistenceManagerFactory properties
    //
    private String URL = null;
    private String userName = null;
    protected String password = null;
    private String driverName = null;

    private Object connectionFactory = null;
    private String connectionFactoryName = null;

    private Object connectionFactory2 = null;
    private String connectionFactory2Name = null;

    private boolean multithreaded = false;

    private boolean optimistic = true;
    private boolean retainValues = true;
    private boolean restoreValues = true;
    private boolean nontransactionalRead = true;
    private boolean nontransactionalWrite = false;
    private boolean ignoreCache = true;
    
    private int queryTimeout = 0;
    private int updateTimeout = 0;

    private int minPool = 1;
    private int maxPool = 1;
    private int msWait = 0;

    /** Cached hashCode for this PMF.  Changes every time a property of this
    * PMF is changed to a non-default value.  Fixed after setConfigured()
    * (mostly).
    * @see #setConfigured()
    * @see #setNonconfigured()
    */
    private int myHashCode;

    //
    // Once false, attempts to change properties above will fail (see
    // assertConfigurable).
    //
    private boolean configurable = true;
    

    //
    // The PMF is serialized in one of 3 forms, depending on how it is
    // configured.
    //
    private static final int PERSIST_CF = 1;
    private static final int PERSIST_CF_NAME = 2;
    private static final int PERSIST_PROPS = 3;

    /** These are used for implementing close().
     */
    protected boolean closed = false;
    
    /** The closeLock protects the close flag and pmSet.
     */
    protected Object closeLock = new Object();
    
    /** The set of all PersistenceManagers that are not closed.  In order
     * for this to work, it is important that PersistenceManager implement
     * equals to be equivalent to Object.equals.
     */
    protected Set pmSet = new HashSet();
    
    /**
     * Logger instance
     */
    private static final Log logger = LogFactory.getFactory().getInstance(
        "com.sun.org.apache.jdo.impl.pm"); // NOI18N

    /**
     * I18N message handler
     */
    private final static I18NHelper msg = 
        I18NHelper.getInstance("com.sun.org.apache.jdo.impl.pm.Bundle"); // NOI18N

    /**
     * Transactional cache of PersistenceManager instances
     */
    private Hashtable pmCache = new Hashtable();

    /** RuntimeJavaModelFactory. */
    private static final RuntimeJavaModelFactory javaModelFactory =
        (RuntimeJavaModelFactory) AccessController.doPrivileged(
            new PrivilegedAction () {
                public Object run () {
                    return RuntimeJavaModelFactory.getInstance();
                }
            }
        );
 
    /** Collection of registered pmf instances. */
    private static Collection registeredPMFs = new HashSet();
    
    /** Adds a JVM shutdown hook to close pmf instances left open by the
     * user. 
     */
    static {
        AccessController.doPrivileged(new PrivilegedAction () {
            public Object run () {
                try {
                    Runtime.getRuntime().addShutdownHook(new ShutdownHook());
                    return null;
                }
                catch (SecurityException ex) {
                    throw new JDOFatalUserException(msg.msg(
                        "EXC_CannotAddShutdownHook"), ex); // NOI18N
                }
            }});
    }

    /**
     * Creates new <code>PersistenceManagerFactoryImpl</code> without
     * any user info.
     */
    public PersistenceManagerFactoryImpl() { }

    /**
     * Creates new <code>PersistenceManagerFactoryImpl</code> with user info
     * @param URL        URL for the data store connection
     * @param userName    user name for the data store connection 
     * @param password    password for the data store connection
     * @param driverName    driver name for the data store connection
     */
    public PersistenceManagerFactoryImpl(
            String URL, 
            String userName, 
            String password, 
            String driverName) {
        this.URL = URL;
        this.userName = userName;
        this.password = password;
        this.driverName = driverName;
        
    }
  
    /** 
     * Set the user name for the data store connection.
     * @param userName the user name for the data store connection.
     */
    public void setConnectionUserName (String userName) {
        assertConfigurable();
        this.userName = userName;
    }
  
    /**
     * Get the user name for the data store connection.
     * @return    the user name for the data store connection.
     */
    public String getConnectionUserName() {
        return userName;
    }
  
    /**
     * Set the password for the data store connection.
     * @param password the password for the data store connection.
     */
    public void setConnectionPassword (String password) {
        assertConfigurable();
        this.password = password;
    }
  
    /**
     * Get the password for the data store connection.  Protected so 
     * not just anybody can get the password.
     * @return password the password for the data store connection.
     */
    protected String getConnectionPassword () {
        return this.password;
    }
  
    /**
     * Set the URL for the data store connection.
     * @param URL the URL for the data store connection.
     */
    public void setConnectionURL (String URL) {
        assertConfigurable();
        this.URL = URL;
    }
  
    /**
     * Get the URL for the data store connection.
     * @return the URL for the data store connection.
     */
    public String getConnectionURL() {
        return URL;
    }

    /**
     * Set the driver name for the data store connection.
     * @param driverName the driver name for the data store connection.
     */
    public void setConnectionDriverName (String driverName) {
        assertConfigurable();
        this.driverName = driverName;
    }
  
    /**
     * Get the driver name for the data store connection.
     * @return the driver name for the data store connection.
     */
    public String getConnectionDriverName() {
        return driverName;
    }

    /**
     * Set the name for the data store connection factory.
     * @param connectionFactoryName the name of the data store
     * connection factory.
     */
    public void setConnectionFactoryName (String connectionFactoryName) {
        assertConfigurable();
        this.connectionFactoryName = connectionFactoryName;
    }
  
    /**
     * Get the name for the data store connection factory.
     * @return the name of the data store connection factory.
     */
    public String getConnectionFactoryName () {
        return connectionFactoryName;
    }

    /**
     * Set the data store connection factory.  JDO implementations
     * will support specific connection factories.  The connection
     * factory interfaces are not part of the JDO specification.
     * @param connectionFactory the data store connection factory.
     */
    public void setConnectionFactory (Object connectionFactory) {
        assertConfigurable();
        this.connectionFactory = connectionFactory;
    }
  
    /**
     * Get the data store connection factory.
     * @return the data store connection factory.
     */
    public Object getConnectionFactory() {
        return connectionFactory;
    }
    
    /** Set the name of the connection factory for non-transactional connections.
     * @see com.sun.persistence.support.PersistenceManagerFactory#setConnectionFactory2Name
     * @param connectionFactoryName the name of the connection factory
     * for non-transactional connections.
     */
    public void setConnectionFactory2Name(String connectionFactoryName)     {
        assertConfigurable();
        this.connectionFactory2Name = connectionFactory2Name;
    }
    
    /** Get the name of the connection factory for non-transactional connections.
     * @see com.sun.persistence.support.PersistenceManagerFactory#getConnectionFactory2Name
     * @return the name of the connection factory for 
     * non-transactional connections.
     */
    public String getConnectionFactory2Name() {
        return connectionFactory2Name;
    }

    /** Set the non-transactional connection factory
     * for optimistic transactions.
     * @see com.sun.persistence.support.PersistenceManagerFactory#setConnectionFactory2
     * @param connectionFactory the non-transactional connection factory.
     */
    public void setConnectionFactory2(Object connectionFactory) {
        assertConfigurable();
        this.connectionFactory2 = connectionFactory2;
    }
  
    /** Return the non-transactional connection factory
     * for optimistic transactions.
     * @see com.sun.persistence.support.PersistenceManagerFactory#getConnectionFactory2
     * @return the non-transactional connection factory for optimistic
     * transactions
     */
    public Object getConnectionFactory2() {
        return connectionFactory2;
    }
  
    /** Set the default Multithreaded setting for all
     * PersistenceManager instances obtained from this factory.
     *
     * @param flag the default Multithreaded setting.
     */
    public void setMultithreaded (boolean flag) {
         assertConfigurable();
         multithreaded = flag;
    }

    /** Get the default Multithreaded setting for all
     * PersistenceManager instances obtained from this factory.  
     *
     * @return the default Multithreaded setting.
     */
    public boolean getMultithreaded() {
        return multithreaded;
    }

    /**
     * Set the default Optimistic setting for all PersistenceManager instances
     * obtained from this factory.  Setting Optimistic to true also sets
     * NontransactionalRead to true.
     * @param flag the default Optimistic setting.
     */
    public void setOptimistic (boolean flag) {
        assertConfigurable();
        optimistic = flag;
    }

    /**
     * Get the default Optimistic setting for all PersistenceManager instances
     * obtained from this factory.
     * @return the default Optimistic setting.
     */
    public boolean getOptimistic () {
        return optimistic;
    }


    /**
     * Set the default RetainValues setting for all PersistenceManager instances
     * obtained from this factory.  Setting RetainValues to true also sets
     * NontransactionalRead to true.
     * @param flag the default RetainValues setting.
     */
    public void setRetainValues (boolean flag) {
        assertConfigurable();
        retainValues = flag;    
    }

    /**
     * Get the default RetainValues setting for all PersistenceManager instances
     * obtained from this factory.
     * @return the default RetainValues setting.
     */
     public boolean getRetainValues () {
         return retainValues;
     }

    /**
     * Set the default RestoreValues setting for all PersistenceManager instances
     * obtained from this factory.  Setting RestoreValues to true also sets
     * NontransactionalRead to true.
     * @param flag the default RestoreValues setting.
     */
    public void setRestoreValues (boolean flag) {
        assertConfigurable();
        restoreValues = flag;    
    }

    /**
     * Get the default RestoreValues setting for all PersistenceManager instances
     * obtained from this factory.
     * @return the default RestoreValues setting.
     */
     public boolean getRestoreValues () {
         return restoreValues;
     }


     /**
      * Set the default NontransactionalRead setting for all
      * PersistenceManager instances obtained from this factory.
      * @param flag the default NontransactionalRead setting.
      */   
     public void setNontransactionalRead (boolean flag) {
         assertConfigurable();
         nontransactionalRead = flag; 
     }

     /**
      * Get the default NontransactionalRead setting for all
      * PersistenceManager instances obtained from this factory.
      * @return the default NontransactionalRead setting.
      */   
     public boolean getNontransactionalRead () {
         return nontransactionalRead;
     }

     /**
      * Set the default NontransactionalWrite setting for all
      * PersistenceManager instances obtained from this factory.
      * @param flag the default NontransactionalWrite setting.
      */   
     public void setNontransactionalWrite (boolean flag) {
         assertConfigurable();
         nontransactionalWrite = flag; 
     }

    /**
     * Get the default NontransactionalWrite setting for all
     * PersistenceManager instances obtained from this factory.
     * @return the default NontransactionalWrite setting.
     */   
    public boolean getNontransactionalWrite () {
        return nontransactionalWrite;
    }


    /**
     * Set the default IgnoreCache setting for all PersistenceManager instances
     * obtained from this factory.
     * @param flag the default IgnoreCache setting.
     */
    public void setIgnoreCache (boolean flag) {
        assertConfigurable();
        ignoreCache = flag;
    }

    /**
     * Get the default IgnoreCache setting for all PersistenceManager instances
     * obtained from this factory.
     * @return the default IngoreCache setting.
     */
    public boolean getIgnoreCache () {
        return ignoreCache;
    }

    /** Set the default MsWait setting for all PersistenceManager instances
     * obtained from this factory.
     * @param msWait the default MsWait setting.
     */
    public void setMsWait(int msWait) {
        assertConfigurable();
        this.msWait = msWait;
    }
    
    /** Get the default MsWait setting for all PersistenceManager instances
     * obtained from this factory.
     * @return the default MsWait setting.
     */
    public int getMsWait() {
        return msWait;
    }
    
    /** Set the default MinPool setting for all PersistenceManager instances
     * obtained from this factory.
     * @param minPool the default MinPool setting.
     */
    public void setMinPool(int minPool) {
        assertConfigurable();
        this.minPool = minPool;
    }
    
    /** Get the default MinPool setting for all PersistenceManager instances
     * obtained from this factory.
     * @return the default MinPool setting.
     */
    public int getMinPool() {
        return minPool;
    }
    
    /** Set the default MaxPool setting for all PersistenceManager instances
     * obtained from this factory.
     * @param maxPool the default MaxPool setting.
     */
    public void setMaxPool(int maxPool) {
        assertConfigurable();
        this.maxPool = maxPool;
    }
    
    /** Get the default MaxPool setting for all PersistenceManager instances
     * obtained from this factory.
     * @return the default MaxPool setting.
     */
    public int getMaxPool() {
        return maxPool;
    }
    
    /** Set the default QueryTimeout setting for all PersistenceManager instances
     * obtained from this factory.
     * @param queryTimeout the default QueryTimeout setting.
     */
    public void setQueryTimeout(int queryTimeout) {
        assertConfigurable();
        this.queryTimeout = queryTimeout;
    }
    
    /** Get the default QueryTimeout setting for all PersistenceManager instances
     * obtained from this factory.
     * @return the default QueryTimeout setting.
     */
    public int getQueryTimeout() {
        return queryTimeout;
    }
    
    /** Set the default UpdateTimeout setting for all
     * PersistenceManager instances obtained from this factory.
     * @param updateTimeout the default UpdateTimeout setting.
     */
    public void setUpdateTimeout(int updateTimeout) {
        assertConfigurable();
        this.updateTimeout = updateTimeout;
    }
    
    /** Get the default UpdateTimeout setting for all PersistenceManager instances
     * obtained from this factory.
     * @return the default UpdateTimeout setting.
     */
    public int getUpdateTimeout() {
        return updateTimeout;
    }
    
    
    /**
     * Return "static" properties of this PersistenceManagerFactory.
     * Properties with keys VendorName and VersionNumber are required.  Other
     * keys are optional.
     * @return the non-operational properties of this PersistenceManagerFactory.
     */
    public Properties getProperties () {
        return JDORIVersion.getVendorProperties();
    }

    /** The application can determine from the results of this
     * method which optional features are supported by the
     * JDO implementation.
     * <P>Each supported JDO optional feature is represented by a
     * String with one of the following values:
     *
     * <P>com.sun.persistence.support.option.TransientTransactional
     * <P>com.sun.persistence.support.option.NontransactionalRead
     * <P>com.sun.persistence.support.option.NontransactionalWrite
     * <P>com.sun.persistence.support.option.RetainValues
     * <P>com.sun.persistence.support.option.Optimistic
     * <P>com.sun.persistence.support.option.ApplicationIdentity
     * <P>com.sun.persistence.support.option.DatastoreIdentity
     * <P>com.sun.persistence.support.option.NonDatastoreIdentity
     * <P>com.sun.persistence.support.option.ArrayList
     * <P>com.sun.persistence.support.option.HashMap
     * <P>com.sun.persistence.support.option.Hashtable
     * <P>com.sun.persistence.support.option.LinkedList
     * <P>com.sun.persistence.support.option.TreeMap
     * <P>com.sun.persistence.support.option.TreeSet
     * <P>com.sun.persistence.support.option.Vector
     * <P>com.sun.persistence.support.option.Map
     * <P>com.sun.persistence.support.option.List
     * <P>com.sun.persistence.support.option.Array     
     * <P>com.sun.persistence.support.option.NullCollection  
     *
     *<P>The standard JDO query language is represented by a String:
     *<P>com.sun.persistence.support.query.JDOQL   
     * @return the Set of String representing the supported Options
     */    
    public Collection supportedOptions() {
        return Collections.unmodifiableList(Arrays.asList(getOptionArray()));
    }

    /**
     * Returns an array of Strings indicating which options are supported by
     * this PersistenceManagerFactory.
     * @return the option array.
     */
    abstract protected String[] getOptionArray();

    /** Creates a new instance of PersistenceManager from this factory.
     * Called by getPersistenceManager(String userid, String password))
     * if there is no pooled instance that satisfies the request.
     *
     * @return a PersistenceManager instance with default options.
     * @param userid The user id of the connection factory.
     * @param password The password of the connection factory.
     */
    protected abstract PersistenceManager createPersistenceManager(
        String userid, String password);

    /** Get an instance of PersistenceManager from this factory.  The
     * instance has default values for options.
     *
     * <P>If pooling of PersistenceManager instances is supported by
     * this factory, the instance might have been returned to the pool
     * and is being reused. 
     *
     * <P>After the first use of getPersistenceManager, no "set" methods will
     * succeed.
     *
     * @return a PersistenceManager instance with default options.
     */
    public PersistenceManager getPersistenceManager() {
        return getPersistenceManager(null, null);
    }
        
    /** Get an instance of PersistenceManager from this factory.  The
     * instance has default values for options.  The parameters userid
     * and password are used when obtaining datastore connections from
     * the connection pool.
     *
     * <P>If pooling of PersistenceManager instances is supported by
     * this factory, the instance might have been returned to the pool
     * and is being reused.
     *
     * <P>After the first use of getPersistenceManager, no "set"
     * methods will succeed.
     *
     * @return a PersistenceManager instance with default options.
     * @param userid The user id of the connection factory.
     * @param password The password of the connection factory.
     */
    public PersistenceManager getPersistenceManager(
        String userid, String password){

        if (debugging())
            debug("getPersistenceManager"); // NOI18N

        if (configurable) {
            verifyConfiguration();
        }

        // Remember if it was configurable. We will need to restore it if
        // it was and createPersistenceManager failed.
        boolean wasConfigurable = configurable;

        try {
            if (wasConfigurable) {
                
                // if successful, the state of this PMF becomes configured
                setConfigured();

                // Replace this PersistenceManagerFactory with the one
                // known to the appserver, if it is the first request
                // to getPersistenceManager in this instance of the
                // PersistenceManagerFactory. 
                // This is a no-op in a non-managed environment, and
                // if an appserver does not need any extra code here.
                PersistenceManagerFactoryImpl pmf = 
                    (PersistenceManagerFactoryImpl)EJBImplHelper.
                        replacePersistenceManagerFactory(this);

                if (pmf != this) {
                    // Was replaced. Mark this PersistenceManagerFactory as 
                    // configurable.
                    setNonconfigured();
                }
                else {
                    // register this PMF
                    registeredPMFs.add(pmf);
                }
                
                return pmf.getPersistenceManagerInternal(userid, password);
            } 
            // This PersistenceManagerFactory has been already configured.
            return getPersistenceManagerInternal(userid, password);

        } catch (com.sun.persistence.support.JDOException e) {
            if (wasConfigurable) {
                setNonconfigured();
            }
            throw e;
        }
    }

    /**
     * Returns PersistenceManager instance with default options.
     * @see #getPersistenceManager(String userid, String password)
     */
    private PersistenceManager getPersistenceManagerInternal(
        String userid, String password){

        if (debugging())
            debug("getPersistenceManagerInternal"); // NOI18N

        // Check if we are in managed environment and
        // PersistenceManager is cached
        PersistenceManagerImpl pm = null;
        javax.transaction.Transaction t = EJBImplHelper.getTransaction();
 
        if (t != null) {
            pm = (PersistenceManagerImpl)pmCache.get(t);
            if (pm == null) {
                // Not found
                synchronized(pmCache) {
                    pm = (PersistenceManagerImpl)pmCache.get(t);
                    if (pm == null) {
                        pm = getFromPool(userid, password);
                        pmCache.put(t, pm);
                        pm.setJTATransaction(t);
                    }

                    // We know we are in the managed environment and
                    // JTA transaction is  active. We need to start
                    // JDO Transaction internally if it is not active.
    
                    Transaction tx = pm.currentTransaction();
                    if (!tx.isActive()) {
                        ((TransactionImpl)tx).begin(t);
                    }
                }
            }      
            if (!(pm.verify(userid, password))) {
                throw new JDOUserException(msg.msg(
                    "EXC_WrongUsernamePassword")); //NOI18N
            }
        } else {
            // We don't know if we are in the managed environment or not
            // If Yes, it is BMT with JDO Transaction and it will register
            // itself when user calls begin().
            pm = getFromPool(userid, password);
        }

        // Always return a wrapper
        return new PersistenceManagerWrapper(pm);
    }


    /**
     * Registers PersistenceManager in the transactional cache in
     * managed environment in case of BMT with JDO Transaction.
     * There is no javax.transaction.Transaction
     * available before the user starts the transaction.
     * @param pm the PersistenceManager
     * @param t the Transaction used as the hashmap key
     */
    protected void registerPersistenceManager(
        PersistenceManagerImpl pm,
        Object t) {

        if (debugging())
            debug("registerPersistenceManager"); // NOI18N

        PersistenceManagerImpl pm1 = (PersistenceManagerImpl)pmCache.get(t);
        if (pm1 == null) {
            synchronized (pmCache) {
                pm1 = (PersistenceManagerImpl)pmCache.get(t);
                if (pm1 == null) {
                    pmCache.put(t, pm);
                    pm.setJTATransaction(t);
                    return;
                }
            }
        }

        if (pm1 != pm){
            throw new JDOFatalInternalException(msg.msg(
                "EXC_WrongJTATransaction")); //NOI18N

        } else {
            // do nothing ???
        }
    }

    /** Deregisters PersistenceManager that is not associated with
     * a JTA transaction any more.
     * @param pm the PersistenceManager
     * @param t the Transaction used as the hashmap key
     */
    protected void deregisterPersistenceManager(PersistenceManagerImpl pm,
        Object t) {
        if (debugging())
            debug("deregisterPersistenceManager"); // NOI18N

        if (t != null) {        // Managed environment
            // Deregister 
            PersistenceManagerImpl pm1 = (PersistenceManagerImpl)pmCache.get(t);
            if (pm1 == null || pm1 != pm) {
                throw new JDOFatalInternalException(msg.msg(
                    "EXC_WrongJTATransaction")); //NOI18N
            } else {
                pmCache.remove(t);
            }
        }
    }

    /** Releases closed PersistenceManager that is not in use
     * @param pm the PersistenceManager
     * @param t the Transaction used as the hashmap key
     */
    protected void releasePersistenceManager(PersistenceManagerImpl pm,
        Object t) {
        if (debugging())
            debug("releasePersistenceManager"); // NOI18N

        deregisterPersistenceManager(pm, t);
        releaseStoreManager(pm);
        returnToPool(pm);
    }

    //
    // Internal methods
    //
    
    /**
     * Finds PersistenceManager for this combination of userid and password
     * in the free pool, or creates new one if not found.
     */
    private synchronized PersistenceManagerImpl getFromPool(
        String userid, String password) {

        if (debugging())
            debug("getFromPool"); // NOI18N

        // We do not have pooling yet...

        // create new PersistenceManager object and set its atributes
        PersistenceManagerImpl pm = 
        (PersistenceManagerImpl)createPersistenceManager(userid, password);
        synchronized(closeLock) {
            if (closed) {
                throw new JDOUserException(
                    msg.msg("EXC_PersistenceManagerFactoryClosed")); // NOI18N
            }
            pmSet.add(pm);
        }

        return pm;
    }

    /**
     * Returns unused PersistenceManager to the free pool
     */
    private void returnToPool(PersistenceManagerImpl pm) {
        if (debugging())
            debug("returnToPool"); // NOI18N

        // do nothing for now except remove from set of PersistenceManagers.
        synchronized(closeLock) {
            pmSet.remove(pm);
        }
    }

    /**
     * Asserts that change to the property is allowed
     */
    private void assertConfigurable() {
        synchronized(closeLock) {
            if (!configurable) {
                throw new JDOUserException (msg.msg("EXC_NotConfigurable")); // NOI18N
            }
        }
    }

    /**
     * Tracing method
     * @param msg String to display
     */
    private void debug(String msg) {
        logger.debug("In PersistenceManagerFactoryImpl " + msg); //NOI18N
    }

    /**
     * Verifies if debugging is enabled.
     * @return true if debugging is enabled.
     */
    private boolean debugging() {
        return logger.isDebugEnabled();
    }


    //
    // Explicit {read, write}Object support for java.io.Serializable so that
    // we can en/de-crypt the password
    //

    // The PMF is serialized in one of 3 forms, depending on how it is
    // configured.
    private int getSerializedForm() {
        int rc = 0;
        if (null != connectionFactory) {
            rc = PERSIST_CF;
        } else if (null != connectionFactoryName) {
            rc = PERSIST_CF_NAME;
        } else {
            rc = PERSIST_PROPS;
        }
        return rc;
    }

    /**The PMF is serialized in one of 3 forms, depending on how it is
     * configured.  This method examines a properties instance to determine
     * which form it is.
     */
    private int getSerializedForm(Properties props) {
        int rc = 0;
        if (null == props.get("com.sun.persistence.support.option.ConnectionURL")) { // NOI18N
            rc = PERSIST_CF;
        } else if (null != props.get("com.sun.persistence.support.option.ConnectionFactoryName")) { // NOI18N
            rc = PERSIST_CF_NAME;
        } else {
            rc = PERSIST_PROPS;
        }
        return rc;
    }

    /**
     * Write this object to a stream.  This method is provided so it
     * can be called from outside the class (explicitly by a subclass).
     * @param oos the ObjectOutputStream
     * @throws IOException on errors writing to the stream
     */    
    protected void doWriteObject(java.io.ObjectOutputStream oos)
        throws java.io.IOException {

        writeObject(oos);
    }
    
    private void writeObject(java.io.ObjectOutputStream oos)
        throws java.io.IOException {
        int kind = getSerializedForm();
        oos.writeInt(kind);

        switch(kind) {
            case PERSIST_CF:
                oos.writeObject(connectionFactory);
                break;

            case PERSIST_CF_NAME:
                oos.writeUTF(connectionFactoryName);
                oos.writeUTF(connectionFactory2Name);
                break;

            case PERSIST_PROPS:
                oos.writeObject(URL);
                oos.writeObject(userName);
                oos.writeObject(encrypt(password));
                oos.writeObject(driverName);
                break;
        }                                        
        oos.writeBoolean(multithreaded);
        oos.writeBoolean(optimistic);
        oos.writeBoolean(retainValues);
        oos.writeBoolean(restoreValues);
        oos.writeBoolean(nontransactionalRead);
        oos.writeBoolean(nontransactionalWrite);
        oos.writeBoolean(ignoreCache);
        
        oos.writeInt(queryTimeout);
        oos.writeInt(updateTimeout);
    }

    /**
     * Read this object from a stream.  This method is provided so it
     * can be called from outside the class (explicitly by a subclass).
     * @param ois the ObjectInputStream
     * @throws IOException on errors reading from the stream
     * @throws ClassNotFoundException if a referenced class cannot be loaded
     */    
    protected void doReadObject(java.io.ObjectInputStream ois)
        throws java.io.IOException, ClassNotFoundException {

        readObject(ois);
    }

    private void readObject(java.io.ObjectInputStream ois)
        throws java.io.IOException, ClassNotFoundException {

        int kind = ois.readInt();
        switch (kind) {
          case PERSIST_CF:
              connectionFactory = ois.readObject();
              break;
              
          case PERSIST_CF_NAME:
              connectionFactoryName = ois.readUTF();
              connectionFactory2Name = ois.readUTF();
              break;
              
          case PERSIST_PROPS:
              URL = (String)ois.readObject();
              userName = (String)ois.readObject();
              password = decrypt((String)ois.readObject());
              driverName = (String)ois.readObject();
              break;
        }
        multithreaded = ois.readBoolean();
        optimistic = ois.readBoolean();
        retainValues = ois.readBoolean();
        restoreValues = ois.readBoolean();
        nontransactionalRead = ois.readBoolean();
        nontransactionalWrite = ois.readBoolean();
        ignoreCache = ois.readBoolean();
        
        queryTimeout = ois.readInt();
        updateTimeout = ois.readInt();
    }

    /**
     * The preferred way of getting & restoring a PMF in JNDI is to do so via
     * a Properties object.
     *
     * Accessor instances allow copying values to/from a PMF and a
     * Properties.  They do the proper type translation too.
     * The PMFAccessor extends the Accessor interface which provides only
     * the getDefault method which is type-independent.  The PMFAccessor
     * provides type-specific accessor properties.
     */
    public interface PMFAccessor extends Accessor {
        
        /** Returns a value from a PMF, turned into a String.
         * @param pmf the PersistenceManagerFactory to get the property from
         * @return  the property value associated with the Accessor key
         */
        public String get(PersistenceManagerFactoryImpl pmf);
        
        /** Returns a value from a PMF, turned into a String, only if the
         * current value is not the default.
         * @param pmf the PersistenceManagerFactory to get the property from
         * @return  the non-default property value associated with the
         * Accessor key
         */
        public String getNonDefault(PersistenceManagerFactoryImpl pmf);
        
        /** Sets a value in a PMF, translating from String to the PMF's
         * representation.
         * @param pmf the PersistenceManagerFactory to set the property into
         * @param s the property value associated with the Accessor key 
         */
        public void set(PersistenceManagerFactoryImpl pmf, String s);
    }
    
    /**
     * Tables which map from names to PMFAccessors.  The names are the same as
     * the PMF's property names.
     *
     * These PMFAccessors are particular to the case when the connection
     * properties are configured as PersistenceManagerFactory properties;
     * neither a connection
     * factory nor connection factory name has been configured.
     */
    protected static HashMap pmfAccessors = new HashMap(4);
    
    /**
     *These PMFAccessors are for configuring non-connection properties.
     */
    protected static HashMap propsAccessors = new HashMap(10);
    
    /** Get JDO implementation-specific properties
     * (not specified by JDO specification).
     * @return a hashmap of accessors
     */    
    protected HashMap getLocalAccessors() {
        return new HashMap();
    }

    /** Initialize the Accessor hashmaps for
    * connection and non-connection properties.
    * <br>
    * XXX: Jikes bug
    * <br>
    * If this is protected, FOStorePMF.initPropsAccessors cannot invoke it,
    * due to a bug in jikes
    * (http://www-124.ibm.com/developerworks/bugs/?func=detailbug&bug_id=213&group_id=10)
     */
    //protected static void initPropsAccessors() {
    public static void initPropsAccessors() {
        if (pmfAccessors.size() != 0)
            return;
        synchronized (pmfAccessors) {
            if (pmfAccessors.size() != 0)
                return;
            //
            // PMF accessors
            //

            pmfAccessors.put(
                "com.sun.persistence.support.option.ConnectionURL", // NOI18N
                new PMFAccessor() {
                public String get(PersistenceManagerFactoryImpl pmf) { return pmf.getConnectionURL(); }
                public String getNonDefault(PersistenceManagerFactoryImpl pmf) { return pmf.getConnectionURL(); }
                public String getDefault() {return null;}
                public void set(PersistenceManagerFactoryImpl pmf, String s) { pmf.setConnectionURL(s); }
            });
            pmfAccessors.put(
                "com.sun.persistence.support.option.ConnectionUserName", // NOI18N
                new PMFAccessor() {
                public String get(PersistenceManagerFactoryImpl pmf) { return pmf.getConnectionUserName(); }
                public String getNonDefault(PersistenceManagerFactoryImpl pmf) { return pmf.getConnectionUserName(); }
                public String getDefault() {return null;}
                public void set(PersistenceManagerFactoryImpl pmf, String s) { pmf.setConnectionUserName(s); }
            });
            pmfAccessors.put(
                "com.sun.persistence.support.option.ConnectionPassword", // NOI18N
                new PMFAccessor() {
                public String get(PersistenceManagerFactoryImpl pmf) { return pmf.encrypt(pmf.getConnectionPassword()); }
                public String getNonDefault(PersistenceManagerFactoryImpl pmf) { return pmf.encrypt(pmf.getConnectionPassword()); }
                public String getDefault() {return null;}
                public void set(PersistenceManagerFactoryImpl pmf, String s) { pmf.setConnectionPassword(pmf.decrypt(s)); }
            });
            pmfAccessors.put(
                "com.sun.persistence.support.option.ConnectionDriverName", // NOI18N
                new PMFAccessor() {
                public String get(PersistenceManagerFactoryImpl pmf) { return pmf.getConnectionDriverName(); }
                public String getNonDefault(PersistenceManagerFactoryImpl pmf) { return pmf.getConnectionDriverName(); }
                public String getDefault() {return null;}
                public void set(PersistenceManagerFactoryImpl pmf, String s) { pmf.setConnectionDriverName(s); }
            });

            //
            // Props accessors
            //

            propsAccessors.put(
                "com.sun.persistence.support.option.Multithreaded", // NOI18N
                new PMFAccessor() {
                public String get(PersistenceManagerFactoryImpl pmf) { return new Boolean(pmf.getMultithreaded()).toString(); }
                public String getNonDefault(PersistenceManagerFactoryImpl pmf) { return (!pmf.getMultithreaded())?null:"true"; } // NOI18N
                public String getDefault() { return "false"; } // NOI18N
                public void set(PersistenceManagerFactoryImpl pmf, String s) { pmf.setMultithreaded(Boolean.valueOf(s).booleanValue()); }
            });
            propsAccessors.put(
                "com.sun.persistence.support.option.Optimistic", // NOI18N
                new PMFAccessor() {
                public String get(PersistenceManagerFactoryImpl pmf) { return new Boolean(pmf.getOptimistic()).toString(); }
                public String getNonDefault(PersistenceManagerFactoryImpl pmf) { return (pmf.getOptimistic())?null:"false"; } // NOI18N
                public String getDefault() { return "true"; } // NOI18N
                public void set(PersistenceManagerFactoryImpl pmf, String s) { pmf.setOptimistic(Boolean.valueOf(s).booleanValue()); }
            });
            propsAccessors.put(
                "com.sun.persistence.support.option.RetainValues", // NOI18N
                new PMFAccessor() {
                public String get(PersistenceManagerFactoryImpl pmf) { return new Boolean(pmf.getRetainValues()).toString(); }
                public String getNonDefault(PersistenceManagerFactoryImpl pmf) { return (pmf.getRetainValues())?null:"false"; } // NOI18N
                public String getDefault() { return "true"; } // NOI18N
                public void set(PersistenceManagerFactoryImpl pmf, String s) { pmf.setRetainValues(Boolean.valueOf(s).booleanValue()); }
            });
            propsAccessors.put(
                "com.sun.persistence.support.option.RestoreValues", // NOI18N
                new PMFAccessor() {
                public String get(PersistenceManagerFactoryImpl pmf) { return new Boolean(pmf.getRestoreValues()).toString(); }
                public String getNonDefault(PersistenceManagerFactoryImpl pmf) { return (pmf.getRestoreValues())?null:"false"; } // NOI18N
                public String getDefault() { return "true"; } // NOI18N
                public void set(PersistenceManagerFactoryImpl pmf, String s) { pmf.setRestoreValues(Boolean.valueOf(s).booleanValue()); }
            });
            propsAccessors.put(
                "com.sun.persistence.support.option.NontransactionalRead", // NOI18N
                new PMFAccessor() {
                public String get(PersistenceManagerFactoryImpl pmf) { return new Boolean(pmf.getNontransactionalRead()).toString(); }
                public String getNonDefault(PersistenceManagerFactoryImpl pmf) { return (pmf.getNontransactionalRead())?null:"false"; } // NOI18N
                public String getDefault() { return "true"; } // NOI18N
                public void set(PersistenceManagerFactoryImpl pmf, String s) { pmf.setNontransactionalRead(Boolean.valueOf(s).booleanValue()); }
            });
            propsAccessors.put(
                "com.sun.persistence.support.option.NontransactionalWrite", // NOI18N
                new PMFAccessor() {
                public String get(PersistenceManagerFactoryImpl pmf) { return new Boolean(pmf.getNontransactionalWrite()).toString(); }
                public String getNonDefault(PersistenceManagerFactoryImpl pmf) { return (!pmf.getNontransactionalWrite())?null:"true"; } // NOI18N
                public String getDefault() { return "false"; } // NOI18N
                public void set(PersistenceManagerFactoryImpl pmf, String s) { pmf.setNontransactionalWrite(Boolean.valueOf(s).booleanValue()); }
            });
            propsAccessors.put(
                "com.sun.persistence.support.option.IgnoreCache", // NOI18N
                new PMFAccessor() {
                public String get(PersistenceManagerFactoryImpl pmf) { return new Boolean(pmf.getIgnoreCache()).toString(); }
                public String getNonDefault(PersistenceManagerFactoryImpl pmf) { return (pmf.getIgnoreCache())?null:"false"; } // NOI18N
                public String getDefault() { return "true"; } // NOI18N
                public void set(PersistenceManagerFactoryImpl pmf, String s) { pmf.setIgnoreCache(Boolean.valueOf(s).booleanValue()); }
            });
            propsAccessors.put(
                "com.sun.persistence.support.option.ConnectionFactoryName", // NOI18N
                new PMFAccessor() {
                public String get(PersistenceManagerFactoryImpl pmf) { return pmf.getConnectionFactoryName(); }
                public String getNonDefault(PersistenceManagerFactoryImpl pmf) { return (pmf.getConnectionFactoryName()==null)?null:pmf.getConnectionFactoryName(); }
                public String getDefault() { return null; }
                public void set(PersistenceManagerFactoryImpl pmf, String s) { pmf.setConnectionFactoryName(s); }
            });
            propsAccessors.put(
                "com.sun.persistence.support.option.ConnectionFactory2Name", // NOI18N
                new PMFAccessor() {
                public String get(PersistenceManagerFactoryImpl pmf) { return pmf.getConnectionFactory2Name(); }
                public String getNonDefault(PersistenceManagerFactoryImpl pmf) { return (pmf.getConnectionFactory2Name()==null)?null:pmf.getConnectionFactory2Name(); }
                public String getDefault() { return null; }
                public void set(PersistenceManagerFactoryImpl pmf, String s) { pmf.setConnectionFactory2Name(s); }
            });
        }
    }

    /**
     * It should *never* be the case that our translation process encounters
     * a NumberFormatException.  If so, tell the user in the JDO-approved
     * manner.
     * @param s the input String
     * @return the int representation of the String
     */ 
    protected static int toInt(String s) {
        int rc = 0;
        try {
            rc = Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            throw new JDOFatalInternalException(msg.msg(
                    "EXC_IntegerInInvalidFormat")); // NOI18N
        }
        return rc;
    }

    /**
     * Returns a Properties representation of this PMF.
     * Only allow Properties representation if the caller configured
     * this PersistenceManagerFactory.  Otherwise, this is a security
     * exposure.
     * @return the Properties representing the non-default properties
     */
    public Properties getAsProperties() {
        assertConfigurable();
        return getAsPropertiesInternal();
    }

    /** 
     * Does not do assertConfigurable validation
     * @see #getAsProperties()
     */
    protected Properties getAsPropertiesInternal() {
        initPropsAccessors();
        Properties p = new Properties();

        int kind = getSerializedForm();

        switch (kind) {
          case PERSIST_CF:
              // XXX need to handle the case of ConnectionFactory2
              setCFProperties(p);
              break;

          case PERSIST_CF_NAME:
              p.setProperty ("com.sun.persistence.support.option.ConnectionFactoryName",
                             connectionFactoryName); // NOI18N
              if (connectionFactory2Name != null) {
                  p.setProperty ("com.sun.persistence.support.option.ConnectionFactory2Name",
                                 connectionFactory2Name); // NOI18N
              }
              break;

          case PERSIST_PROPS:
              setProps(p, pmfAccessors);
              break;
        }
        setProps(p, propsAccessors);
        setPMFClassProperty(p);
        // add the properties from the implementation class
        setProps(p, getLocalAccessors());
        return p;
    }
    
    /** Set the PMF class property for this PMF.
     * @param props the Properties to which to add the PMF class property
     */
    abstract protected void setPMFClassProperty (Properties props);

    /**
     * For each PMFAccessor in the given HashMap, gets the corresponding value
     * from the PMF and puts it in the given Properties object.
     */
    void setProps(Properties p, HashMap accessors) {
        Set s = accessors.entrySet();
        for (Iterator i = s.iterator(); i.hasNext();) {
            Map.Entry e = (Map.Entry)i.next();
            String key = (String)e.getKey();
            PMFAccessor a = (PMFAccessor)e.getValue();
            String value = (String)a.getNonDefault(this);
            if (null != value) {
                p.setProperty (key, value);
            }
        }
    }

    /**
     * Configures a PMF from the given Properties.
     * @param p the Properties used to configure this PMF
     */
    public void setFromProperties(Properties p) {
        initPropsAccessors();
        assertConfigurable();
        int kind = getSerializedForm (p);

        switch (kind) {
          case PERSIST_CF:
              getCFFromProperties(p);
              break;

          case PERSIST_CF_NAME:
              connectionFactoryName = p.getProperty(
                  "com.sun.persistence.support.option.ConnectionFactoryName"); // NOI18N
              connectionFactory2Name = p.getProperty(
                  "com.sun.persistence.support.option.ConnectionFactory2Name"); // NOI18N
              break;

          case PERSIST_PROPS:
              getProps(p, pmfAccessors);
              break;
        }
        getProps(p, propsAccessors);
        getProps(p, getLocalAccessors());
    }

    /**
     * For each PMFAccessor in the given HashMap, gets the corresponding value
     * from the Properties and sets that value in the PMF.
     */
    private void getProps(Properties p, HashMap accessors) {
        Set s = accessors.entrySet();
        for (Iterator i = s.iterator(); i.hasNext();) {
            Map.Entry e = (Map.Entry)i.next();
            String key = (String)e.getKey();
            String value = p.getProperty(key);
            if (null != value) {
//                System.out.println("PersistenceManagerFactoryImpl setting property: " + key + " to: " + value); // NOI18N
                PMFAccessor a = (PMFAccessor)e.getValue();
                a.set(this, value);
            }
        }
    }

    /**
     * Provides an encrypted version of the given string.
     * <b>NOTE:</b>
     * Be very sure that you implement this method using the kind of
     * security that is appropriate for your JDO implementation!!!
     * Note that this method is not static, because it must be overridden
     * by the specialized subclass.  But it should be written as if it were
     * static.  That is, it should not use any state in the
     * PersistenceManagerFactoryImpl instance.
     * @param s the String to be encrypted
     * @return the encrypted String
     */
    abstract protected String encrypt(String s);

    /**
     * Provides a decrypted version of the given (encrypted) string.
     * <b>NOTE:</b>
     * Be very sure that you implement this method using the kind of
     * security that is appropriate for your JDO implementation!!!
     * @param s the String to be decrypted
     * @return the decrypted String
     */
    abstract protected String decrypt(String s);

    /**
     * Set the PMF-specific ConnectionFactory's properties.
     * @param p Properties object in which the PMF's ConnectioFactory's
     * properties are to be set.
     */
    abstract protected void setCFProperties(Properties p);

    /**
     * Create a ConnectionFactory for this PMF.  The method's implementation
     * should set the PMF's connection factory with the newly created object.
     * @param p Properties from which the ConnectionFactory is to be created.
     */
        // XXX The method name contains "get" but this does not "get"
        // anything.  It should be changed to "setup" or ???
    abstract protected void getCFFromProperties(Properties p);

    /**
     * Returns if a connection factory is configured for this
     * PersistenceManagerFactory. This is used to determine whether
     * this PersistenceManagerFactory has been configured with a
     * ConnectionFactory, a ConnectionFactoryName, or a ConnectionURL.
     * @return if a connection factory is configured
     */
    abstract protected boolean isConnectionFactoryConfigured();
    
    /** The String representation of this PMF.
     * @return the String representation of this PMF
     */    
    public String toString() {
        return "" + // NOI18N
            "URL: " + URL + "\n" + // NOI18N
            "userName: " + userName + "\n" + // NOI18N
            "password: " + password + "\n" + // NOI18N
            "driverName: " + driverName + "\n" + // NOI18N

            "connectionFactory: " + connectionFactory + "\n" + // NOI18N
            "connectionFactoryName: " + connectionFactoryName + "\n" + // NOI18N

            "connectionFactory2: " + connectionFactory2 + "\n" + // NOI18N
            "connectionFactory2Name: " + connectionFactory2Name + "\n" + // NOI18N

            "multithreaded: " + multithreaded + "\n" + // NOI18N
            "optimistic: " + optimistic + "\n" + // NOI18N
            "retainValues: " + retainValues + "\n" + // NOI18N
            "restoreValues: " + restoreValues + "\n" + // NOI18N
            "nontransactionalRead: " + nontransactionalRead + "\n" + // NOI18N
            "nontransactionalWrite: " + nontransactionalWrite + "\n" + // NOI18N
            "ignoreCache: " + ignoreCache + "\n" + // NOI18N
            "queryTimeout: " + queryTimeout + "\n" + // NOI18N
            "updateTimeout: " + updateTimeout + "\n"; // NOI18N
    }
    
    /** Verify that the connection URL has been configured.
     * This might be done by the PMF property ConnectionURL,
     * or by the connection factory property URL, or
     * by configuring a connection factory name.
     */    
    protected void verifyConfiguration() {
        if ((!isConnectionFactoryConfigured()) &&
            (connectionFactoryName == null) &&
            (URL == null)) {
            throw new JDOFatalUserException(msg.msg(
                "EXC_IncompleteConfiguration")); // NOI18N
        }
    }
        
    /**
     * Set the configurable flag false so this
     * PersistenceManagerFactory can no longer be configured.  No value is
     * provided, because a PersistenceManagerFactory can never become
     * re-configurable.  Once invoked, the hashCode() of this PMF will never
     * change, except if setNonconfigured is called.
     * @see #hashCode()
     * @see #setNonconfigured()
     */        
    protected void setConfigured() {
        configurable = false;
        myHashCode = hashCode();
    }

    /**
     * Set the configurable flag true so this
     * PersistenceManagerFactory can be again configured.  Called only
     * if the action caused change to be non-configurable failed.
     */        
    protected void setNonconfigured() {
        configurable = true;
        myHashCode = 0;
    }

    /** Given an input Properties instance, add to the output Properties instance
     * only the non-default entries of the input Properties, based on the
     * Accessor map provided.  The output instance can be used as the key
     * for the PersistenceManagerFactory hashMap.
     *
     * <P>A properties instance will typically be filtered a number of times:
     * once for the JDO standard PersistenceManagerFactory properties, another
     * for the JDO implementation properties, and another for the implementation
     * ConnectionFactory properties.
     *
     * <P>A properties accessor map is passed as an argument.  The map
     * contains the PMFAccessors, keyed by property name.
     * @param props the input Properties
     * @param filtered the output properties
     * @param accessors the hashmap of accessors to filter for
     */
    public static void filterProperties (Properties props, Properties filtered,
                                         Map accessors) {
        Set s = accessors.entrySet();
        for (Iterator i = s.iterator(); i.hasNext();) {
            // for each accessor defined
            Map.Entry e = (Map.Entry)i.next();
            String key = (String)e.getKey();
            // if the key in the accessor matches a property in the properties
            String value = props.getProperty(key);
            // and if the property is not null
            if (null != value) {
                Accessor a = (Accessor)e.getValue();
                // and the value is not the default value for the accessor
                if (a.getDefault() != value) {
                    // set the property in the filtered properties
                    filtered.setProperty (key, value);
                }
            }
        }
        return;
    }

    public synchronized boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof PersistenceManagerFactoryImpl))
            return false;

	return (this.getAsPropertiesInternal().equals(
            ((PersistenceManagerFactoryImpl)o).getAsPropertiesInternal()));
    }

    /** The returned value can change before this PMF is configured.  Once
    * configured it will never change (well...)
    * @see #setConfigured()
    * @see #setNonconfigured()
    */
    public synchronized int hashCode() {
        if (0 == myHashCode) {
            return this.getAsPropertiesInternal().hashCode();
        } else {
            return myHashCode;
        }
    }
    
    /** A <code>PersistenceManagerFactory</code> instance can be used until it is closed.
     * @return <code>true</code> if this <code>PersistenceManagerFactory</code> has been closed.
     * @see com.sun.persistence.support.PersistenceManagerFactory#isClosed()
     */
    public boolean isClosed() {
        synchronized(closeLock) {
            return closed;
        }
    }
    
    /** Close this PersistenceManagerFactory. Check for
     * JDOPermission("closePersistenceManagerFactory") and if not authorized,
     * throw SecurityException.
     * <P>If the authorization check succeeds, check to see that all
     * PersistenceManager instances obtained from this PersistenceManagerFactory
     * have no active transactions. If any PersistenceManager instances have
     * an active transaction, throw a JDOUserException, with one nested
     * JDOUserException for each PersistenceManager with an active Transaction.
     * <P>If there are no active transactions, then close all PersistenceManager
     * instances obtained from this PersistenceManagerFactory, mark this
     * PersistenceManagerFactory as closed, disallow getPersistenceManager
     * methods, and allow all other get methods. If a set method or
     * getPersistenceManager method is called after close, then
     * JDOUserException is thrown.
     */
    public void close() {
        synchronized(closeLock) {
            if (closed) {
                return;
            }
            SecurityManager secmgr = System.getSecurityManager();
            if (secmgr != null) {
                // checkPermission will throw SecurityException if not authorized
                secmgr.checkPermission(JDOPermission.CLOSE_PERSISTENCE_MANAGER_FACTORY);
            }
            List activePersistenceManagers = getActivePersistenceManagers();
            int size = activePersistenceManagers.size();
            if (size != 0) {
                Throwable[] thrown = new Throwable[size];
                for (int i = 0; i < size; ++i) {
                    PersistenceManagerImpl pm = 
                        (PersistenceManagerImpl)activePersistenceManagers.get(i);
                    thrown[i] = new JDOUserException(
                        msg.msg("EXC_ActivePersistenceManager"), // NOI18N
                        pm.getCurrentWrapper());
                    }
                throw new JDOUserException(
                    msg.msg("EXC_ActivePersistenceManager"), thrown); // NOI18N
            }
            closeOpenPersistenceManagers();
            // pmf is closed => remove it from collection of registered pmfs
            registeredPMFs.remove(this);
            setConfigured();
            closed = true;
        }
    }
    
    /** Assert that this PersistenceManagerFactory is not closed.  This
     * assertion precedes all getPersistenceManager calls.  "set" methods
     * are already protected by the configured flag.
     * This method is synchronized so if another thread is calling
     * close at the same time, this thread will wait for the close to complete.
     */
    protected void assertNotClosed() {
        synchronized(closeLock) {
            if (closed) {
                throw new JDOUserException(
                    msg.msg("EXC_PersistenceManagerFactoryClosed")); // NOI18N
            }
        }
    }
    
    /** Get all active PersistenceManagers.  This is all 
     * PersistenceManagers that have active transactions.
     */
    protected List getActivePersistenceManagers() {
        List pms = new ArrayList();
        for (Iterator it=pmSet.iterator(); it.hasNext();) {
            PersistenceManager pm = (PersistenceManager)it.next();
            if (pm.currentTransaction().isActive()) {
                pms.add(pm);
            }
        }
        return pms;
    }
    
    /** Close all open PersistenceManagers.  Only the PersistenceManagers
     * in the non-transactional set are considered; there cannot be any
     * inactive PersistenceManagers in the transactional cache.
     * We do forceClose because we don't care if there are active wrappers.
     */
    protected void closeOpenPersistenceManagers() {
        // copy to avoid concurrent modification; forceClose changes pmSet.
        List toClose = Arrays.asList(pmSet.toArray()); 
        for (Iterator it=toClose.iterator(); it.hasNext();) {
            PersistenceManagerImpl pm = (PersistenceManagerImpl)it.next();
            pm.forceClose();
        }
    }
    

    /** Method called by the shudown hook to close pmf instances left open 
     * when the JVM exits.
     */
    protected void shutdown() {
        closeOpenPersistenceManagers();
    }

    /** Shutdown hook to close pmf instances left open when the JVM
     * exits. 
     */ 
    static class ShutdownHook extends Thread {
        public void run() {
            for (Iterator i = registeredPMFs.iterator(); i.hasNext();) {
                try {
                    ((PersistenceManagerFactoryImpl)i.next()).shutdown();
                }
                catch (JDOException ex) {
                    // ignore
                }
            }
        }
    }
}

