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
 * PersistenceManagerFactory.java
 *
 * Created on February 25, 2000
 */
 
package com.sun.persistence.support;

import java.util.Properties;
import java.util.Collection;

/** The <code>PersistenceManagerFactory</code> is the interface to use to obtain
 * <code>PersistenceManager</code> instances.  All <code>PersistenceManager</code> instances obtained
 * from the same <code>PersistenceManagerFactory</code> will have the same default
 * properties.
 *
 * <P><code>PersistenceManagerFactory</code> instances may be configured and
 * serialized for later use.  They may be stored via JNDI and looked up
 * and used later.  Any properties configured will be saved and restored.
 *
 * <P>Once the first <code>PersistenceManager</code> is obtained from the 
 * <code>PersistenceManagerFactory</code>, the factory can no longer be configured.
 * <P>If the <code>ConnectionFactory</code> property is set (non-<code>null</code>) then 
 * all other Connection properties including <code>ConnectionFactoryName</code> are ignored;
 * otherwise, if <code>ConnectionFactoryName</code> is set (non-<code>null</code>) then
 * all other Connection properties are ignored.
 * Similarly, if the <code>ConnectionFactory2</code> property is set (non-<code>null</code>) then 
 * <code>ConnectionFactory2Name</code> is ignored.
 * <P>Operational state (<code>PersistenceManager</code> pooling, connection pooling,
 * operational parameters) must not be serialized.
 *
 * @author Craig Russell
 * @version 1.0.1
 */

public interface PersistenceManagerFactory extends java.io.Serializable
{
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
   * @since 1.0.1
   */
  void close();
    
  /** Tests if this <code>PersistenceManagerFactory</code> has been closed.
   *
   * @return <code>true</code> if this <code>PersistenceManagerFactory</code> has been closed
   */
  boolean isClosed();

  /** Get an instance of <code>PersistenceManager</code> from this factory.  The instance has
   * default values for options.
   *
   * <P>After the first use of <code>getPersistenceManager</code>, no "set" methods will
   * succeed.
   *
   * @return a <code>PersistenceManager</code> instance with default options.
   */
  PersistenceManager getPersistenceManager();

    /** Get an instance of <code>PersistenceManager</code> from this factory.  The instance has
     * default values for options.  The parameters <code>userid</code> and <code>password</code> are used
     * when obtaining datastore connections from the connection pool.
     *
     * <P>After the first use of <code>getPersistenceManager</code>, no "set" methods will
     * succeed.
     *
     * @return a <code>PersistenceManager</code> instance with default options.
     * @param userid the userid for the connection
     * @param password the password for the connection
     */
  PersistenceManager getPersistenceManager(String userid, String password);

  /** Set the user name for the data store connection.
   * @param userName the user name for the data store connection.
   */
  void setConnectionUserName(String userName);

  /** Get the user name for the data store connection.
   * @return the user name for the data store connection.
   */
  String getConnectionUserName ();
  
  /** Set the password for the data store connection.
   * @param password the password for the data store connection.
   */
  void setConnectionPassword (String password);
  
  /** Set the URL for the data store connection.
   * @param URL the URL for the data store connection.
   */
  void setConnectionURL (String URL);

  /** Get the URL for the data store connection.
   * @return the URL for the data store connection.
   */
  String getConnectionURL ();
  
  /** Set the driver name for the data store connection.
   * @param driverName the driver name for the data store connection.
   */
  void setConnectionDriverName  (String driverName);

  /** Get the driver name for the data store connection.
   * @return the driver name for the data store connection.
   */
  String getConnectionDriverName ();
    
  /** Set the name for the data store connection factory.
   * @param connectionFactoryName the name of the data store connection factory.
   */
  void setConnectionFactoryName (String connectionFactoryName);

  /** Get the name for the data store connection factory.
   * @return the name of the data store connection factory.
   */
  String getConnectionFactoryName ();
  
  /** Set the data store connection factory.  JDO implementations
   * will support specific connection factories.  The connection
   * factory interfaces are not part of the JDO specification.
   * @param connectionFactory the data store connection factory.
   */
  void setConnectionFactory (Object connectionFactory);
  
  /** Get the data store connection factory.
   * @return the data store connection factory.
   */
  Object getConnectionFactory ();
  
  /** Set the name for the second data store connection factory.  This is
   * needed for managed environments to get nontransactional connections for
   * optimistic transactions.
   * @param connectionFactoryName the name of the data store connection factory.
   */
  void setConnectionFactory2Name (String connectionFactoryName);

  /** Get the name for the second data store connection factory.  This is
   * needed for managed environments to get nontransactional connections for
   * optimistic transactions.
   * @return the name of the data store connection factory.
   */
  String getConnectionFactory2Name ();
  
  /** Set the second data store connection factory.  This is
   * needed for managed environments to get nontransactional connections for
   * optimistic transactions.  JDO implementations
   * will support specific connection factories.  The connection
   * factory interfaces are not part of the JDO specification.
   * @param connectionFactory the data store connection factory.
   */
  void setConnectionFactory2 (Object connectionFactory);
  
  /** Get the second data store connection factory.  This is
   * needed for managed environments to get nontransactional connections for
   * optimistic transactions.
   * @return the data store connection factory.
   */
  Object getConnectionFactory2 ();
  
  /** Set the default Multithreaded setting for all <code>PersistenceManager</code> instances
   * obtained from this factory.
   *
   * @param flag the default Multithreaded setting.
   */
  void setMultithreaded (boolean flag);
  
  /** Get the default Multithreaded setting for all <code>PersistenceManager</code> instances
   * obtained from this factory.  
   *
   * @return the default Multithreaded setting.
   */
  boolean getMultithreaded();
    
  /** Set the default Optimistic setting for all <code>PersistenceManager</code> instances
   * obtained from this factory.  
   *
   * @param flag the default Optimistic setting.
   */
  void setOptimistic (boolean flag);
  
  /** Get the default Optimistic setting for all <code>PersistenceManager</code> instances
   * obtained from this factory.  
   *
   * @return the default Optimistic setting.
   */
  boolean getOptimistic();
    
  /** Set the default RetainValues setting for all <code>PersistenceManager</code> instances
   * obtained from this factory.
   *
   * @param flag the default RetainValues setting.
   */
  void setRetainValues (boolean flag);
  
  /** Get the default RetainValues setting for all <code>PersistenceManager</code> instances
   * obtained from this factory.
   *
   * @return the default RetainValues setting.
   */
  boolean getRetainValues ();
    
    /** Set the default value for the RestoreValues property.  
     * If <code>true</code>, at rollback, fields of newly persistent instances 
     * are restored to 
     * their values as of the beginning of the transaction, and the instances
     * revert to transient.  Additionally, fields of modified
     * instances of primitive types and immutable reference types
     * are restored to their values as of the beginning of the 
     * transaction.
     * <P>If <code>false</code>, at rollback, the values of fields of 
     * newly persistent instances are unchanged and the instances revert to
     * transient.  Additionally, dirty instances transition to hollow.
     * If an implementation does not support this option, a 
     * <code>JDOUnsupportedOptionException</code> is thrown.
     * @param restoreValues the value of the restoreValues property
     */
    void setRestoreValues(boolean restoreValues);
    
    /** Get the default value for the RestoreValues property.  
     * @return the value of the restoreValues property
     */
    boolean getRestoreValues();
    
  /** Set the default NontransactionalRead setting for all <code>PersistenceManager</code> instances
   * obtained from this factory.  
   *
   * @param flag the default NontransactionalRead setting.
   */
  void setNontransactionalRead (boolean flag);
  
  /** Get the default NontransactionalRead setting for all <code>PersistenceManager</code> instances
   * obtained from this factory.
   *
   * @return the default NontransactionalRead setting.
   */
  boolean getNontransactionalRead ();
    
  /** Set the default NontransactionalWrite setting for all <code>PersistenceManager</code> instances
   * obtained from this factory.  
   *
   * @param flag the default NontransactionalWrite setting.
   */
  void setNontransactionalWrite (boolean flag);
  
  /** Get the default NontransactionalWrite setting for all <code>PersistenceManager</code> instances
   * obtained from this factory.
   *
   * @return the default NontransactionalWrite setting.
   */
  boolean getNontransactionalWrite ();
    
  /** Set the default IgnoreCache setting for all <code>PersistenceManager</code> instances
   * obtained from this factory.
   *
   * @param flag the default IgnoreCache setting.
   */
  void setIgnoreCache (boolean flag);
  
  /** Get the default IgnoreCache setting for all <code>PersistenceManager</code> instances
   * obtained from this factory.
   *
   * @return the default IngoreCache setting.
   */
  boolean getIgnoreCache ();
  
   /** Return non-configurable properties of this <code>PersistenceManagerFactory</code>.
   * Properties with keys <code>VendorName</code> and <code>VersionNumber</code> are required.  Other
   * keys are optional.
   * @return the non-configurable properties of this
   * <code>PersistenceManagerFactory</code>.
   */
  Properties getProperties();
  
    /** The application can determine from the results of this
     * method which optional features, and which query languages 
     * are supported by the JDO implementation.
     * <P>Each supported JDO optional feature is represented by a
     * <code>String</code> with one of the following values:
     *
     * <P><code>com.sun.persistence.support.option.TransientTransactional
     * <BR>com.sun.persistence.support.option.NontransactionalRead
     * <BR>com.sun.persistence.support.option.NontransactionalWrite
     * <BR>com.sun.persistence.support.option.RetainValues
     * <BR>com.sun.persistence.support.option.Optimistic
     * <BR>com.sun.persistence.support.option.ApplicationIdentity
     * <BR>com.sun.persistence.support.option.DatastoreIdentity
     * <BR>com.sun.persistence.support.option.NonDatastoreIdentity
     * <BR>com.sun.persistence.support.option.ArrayList
     * <BR>com.sun.persistence.support.option.HashMap
     * <BR>com.sun.persistence.support.option.Hashtable
     * <BR>com.sun.persistence.support.option.LinkedList
     * <BR>com.sun.persistence.support.option.TreeMap
     * <BR>com.sun.persistence.support.option.TreeSet
     * <BR>com.sun.persistence.support.option.Vector
     * <BR>com.sun.persistence.support.option.Map
     * <BR>com.sun.persistence.support.option.List
     * <BR>com.sun.persistence.support.option.Array  
     * <BR>com.sun.persistence.support.option.NullCollection</code>
     *
     *<P>The standard JDO query language is represented by a <code>String</code>:
     *<P><code>com.sun.persistence.support.query.JDOQL</code>
     * @return the <code>Collection</code> of <code>String</code>s representing the supported options.
     */    
    Collection supportedOptions();
   
}
