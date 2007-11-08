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

package com.sun.persistence.runtime.sqlstore.impl;

import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.sql.DataSource;

import com.sun.persistence.support.JDOException;
import com.sun.persistence.support.JDOUserException;
import com.sun.persistence.support.PersistenceManager;
import com.sun.persistence.support.JDOFatalException;

import com.sun.org.apache.jdo.ejb.EJBImplHelper;
import com.sun.org.apache.jdo.store.StoreManager;
import com.sun.org.apache.jdo.pm.PersistenceManagerInternal;
import com.sun.org.apache.jdo.pm.PersistenceManagerFactoryInternal;
import com.sun.org.apache.jdo.impl.pm.PersistenceManagerFactoryImpl;
import com.sun.org.apache.jdo.impl.pm.PersistenceManagerImpl;
import com.sun.org.apache.jdo.impl.model.java.runtime.RuntimeJavaModelFactory;
import com.sun.org.apache.jdo.impl.model.jdo.caching.JDOModelFactoryImplCaching;
import com.sun.org.apache.jdo.model.java.JavaModel;
import com.sun.org.apache.jdo.model.jdo.JDOModel;
import com.sun.org.apache.jdo.store.TranscriberFactory;
import com.sun.org.apache.jdo.util.I18NHelper;
import com.sun.org.apache.jdo.model.java.JavaModelFactory;
import com.sun.org.apache.jdo.model.jdo.JDOModelFactory;

import com.sun.persistence.runtime.connection.ConnectionFactory;
import com.sun.persistence.runtime.connection.impl.ConnectionFactoryImpl;
import com.sun.persistence.runtime.model.mapping.RuntimeMappingModelFactory;
import com.sun.persistence.runtime.model.mapping.RuntimeMappingModel;
import com.sun.persistence.runtime.model.mapping.impl.RuntimeMappingModelFactoryImpl;
import com.sun.persistence.runtime.sqlstore.database.DBVendorType;
import com.sun.persistence.deployment.impl.reflection.StandaloneDeployer;


/**
 * A factory for PersistenceManagers mapping objects to SQL datastores.
 * @author Martin Zaun
 */
public class SQLPersistenceManagerFactory
    extends PersistenceManagerFactoryImpl {
    //implements Externalizable, Referenceable

    /**
     * Supported list of options for this implementation.
     */
    private final String[] optionArray = new String[] {
        "com.sun.persistence.support.option.TransientTransactional", // NOI18N
        "com.sun.persistence.support.option.NontransactionalRead", // NOI18N
        "com.sun.persistence.support.option.NontransactionalWrite", // NOI18N
        "com.sun.persistence.support.option.RetainValues", // NOI18N
        "com.sun.persistence.support.option.RestoreValues", // NOI18N
        "com.sun.persistence.support.option.Optimistic", // NOI18N
        "com.sun.persistence.support.option.ApplicationIdentity", // NOI18N
        //"com.sun.persistence.support.option.DatastoreIdentity", // NOI18N
        //"com.sun.persistence.support.option.NonDatastoreIdentity", // NOI18N
        //"com.sun.persistence.support.option.ArrayList", // NOI18N
        //"com.sun.persistence.support.option.HashMap", // NOI18N
        //"com.sun.persistence.support.option.Hashtable", // NOI18N
        //"com.sun.persistence.support.option.LinkedList", // NOI18N
        //"com.sun.persistence.support.option.TreeMap", // NOI18N
        //"com.sun.persistence.support.option.TreeSet", // NOI18N
        //"com.sun.persistence.support.option.Vector", // NOI18N
        //"com.sun.persistence.support.option.Map", // NOI18N
        //"com.sun.persistence.support.option.List", // NOI18N
        //"com.sun.persistence.support.option.Array", // NOI18N
        //"com.sun.persistence.support.option.NullCollection", // NOI18N
        //"com.sun.persistence.support.query.JDOQL" // NOI18N
    };

    /** The DataSource or ConnectionFactory for this PMF. */
    private transient Object cf;

    /** If this factory is for JTA or resource-local persistence managers. */
    protected boolean isJtaAware = EJBImplHelper.isManaged();

    /** I18N support. */
    private static final I18NHelper msg =
        I18NHelper.getInstance("com.sun.persistence.runtime.Bundle"); // NOI18N

    /**
     * Instance of RuntimeMappingModel for this pmf
     */
    private RuntimeMappingModel model;

    /** Instance of DBVendorType for this pmf */
    private DBVendorType dbVendor;

    /** Set to true when initialized */
    private boolean initialized;
    
    /**
     * Creates a new <code>SQLPersistenceManagerFactory</code>.
     */
    public SQLPersistenceManagerFactory() {
    }

    /**
     * Creates a new <code>SQLPersistenceManagerFactory</code>.
     * @param url         URL for the data store connection
     * @param userName    user name for the data store connection
     * @param password    password for the data store connection
     * @param driverName  driver name for the data store connection
     */
    public SQLPersistenceManagerFactory(String url,
                                        String userName,
                                        String password,
                                        String driverName) {
        super(url, userName, password, driverName);
    }

    // ----------------------------------------------------------------------
    // PersistenceManagerFactoryInternal Methods
    // ----------------------------------------------------------------------

    /**
     * @see PersistenceManagerFactoryInternal#getTranscriberFactory
     * @return The PersistenceManagerFactory's transcriber factory.
     */
    public TranscriberFactory getTranscriberFactory() {
        // XXX : implement when needed
        throw new UnsupportedOperationException();
    }

    /**
    * If parameter is non-null and implements PersistenceCapable, returns
    * OID.class.
    * @see PersistenceManagerFactoryInternal#getObjectIdClass
    */
    public Class getObjectIdClass(Class cls) {
        // XXX : implement when needed
        throw new UnsupportedOperationException();
    }

    /**
    * @see PersistenceManagerFactoryInternal#getStoreManager
    */
    public StoreManager getStoreManager(PersistenceManager pm) {
        return ((PersistenceManagerInternal)pm).getStoreManager();
    }

    /**
    * @see PersistenceManagerFactoryInternal#releaseStoreManager
    */
    public void releaseStoreManager(PersistenceManager pm) {
        ((PersistenceManagerInternal)pm).setStoreManager(null);
    }

    /**
    * @see PersistenceManagerFactoryInternal#getTrackedClass
    */
    public Class getTrackedClass(Class type) {
        // FIXME XXX : support for mutable SCOs not implemented yet
        // don't throw new UnsupportedOperationException();
        return null;
    }

    /**
     * Configures this factory to return JTA or resource-local entity managers.
     * XXX @see PersistenceManagerFactoryInternal#setJtaAware
     */
    public void setJtaAware(boolean isJtaAware) {
        this.isJtaAware = isJtaAware;
    }

    /**
     * Tests if this factory is configured for providing JTA entity managers.
     * XXX @see PersistenceManagerFactoryInternal#isJtaAware
     */
    public boolean isJtaAware() {
        return isJtaAware;
    }

    // ----------------------------------------------------------------------
    // PersistenceManagerFactoryImpl Methods
    // ----------------------------------------------------------------------

    /** Create a new instance of PersistenceManager with
     * the specific user name and password.
     * @see PersistenceManagerFactoryImpl#createPersistenceManager
     */
    protected PersistenceManager createPersistenceManager(
        String userid, String password) {
        final PersistenceManagerInternal pm;
        try {
            pm = new PersistenceManagerImpl(this, userid, password);
            // XXX remove synchronized when PMF.getPM is fixed
            synchronized (this) {
                if (!initialized) {
                    final Object cf = getConnectionFactory();
                    //Create a dummy store manager to obtain dbvendorType
                    SQLStoreManager srmDBVendorType =
                            new SQLStoreManager(pm, cf, userid, password);
                    dbVendor = srmDBVendorType.getDBVendorType();
                    //Initialize the model
                    getRuntimeMappingModel();
                    initialized = true;
                }
            }
            SQLStoreManager srm = new SQLStoreManager(pm, cf, userid, password);
            pm.setStoreManager(srm);
            setConfigured();
        } catch (JDOException ex) {
            // XXX : improve exception handling
            throw ex;
        } catch (RuntimeException ex) {
            // XXX : improve exception handling
            throw ex;
        }
        return pm;
    }

    /** Override PersistenceManagerFactoryImpl's method so we can use our
     * own cf variable.
     * @see PersistenceManagerFactoryImpl#setConnectionFactory
     */
    public void setConnectionFactory(Object cf) {
        assert ((cf == null) || (cf instanceof ConnectionFactory) ||
                (cf instanceof DataSource));
        super.setConnectionFactory(cf);
        this.cf = cf;
    }

    /**
     * Override PersistenceManagerFactoryImpl's method so that we can get a
     * handle on the connection factory to close the database at close() time.
     * @see PersistenceManagerFactoryImpl#getConnectionFactory
     */
    public synchronized Object getConnectionFactory() {
        // already known
        if (cf != null) {
            return cf;
        }

        // set by the user
        if ((cf = super.getConnectionFactory()) != null) {
            return cf;
        }

        // lookup connection factory by name
        final String cfName = getConnectionFactoryName();
        if (cfName != null) {
            try {
                InitialContext ctx = new InitialContext();
                cf = ctx.lookup(cfName);
            } catch (NamingException e) {
                throw new JDOUserException(
                        msg.msg(
                        "sqlpmf.lookupfailed", cfName), e); //NOI18N
            }
        }

        // Not set explicitly or by name - create our connection factory
        if (cf == null) {
            final String url = getConnectionURL();
            final String user = getConnectionUserName();
            final String password = getConnectionPassword();
            final String driver = getConnectionDriverName ();
            cf = new ConnectionFactoryImpl(url, user, password, driver);

/* FIXME: CreateSQLPMF interface?
            final ConnectionFactory cf0 = (ConnectionFactory)cf;
            cf0.setMinPool(...);
            cf0.setMaxPool(...);
            cf0.setMsWait(...);
            cf0.setMsInterval(...);
            cf0.setLogWriter(...);
            cf0.setLoginTimeout(...);
            if (txIsolation > 0)
               cf0.setTransactionIsolation(txIsolation);
*/

        }

        assert (cf != null);
        // XXX FIXME : throws a JDOUserException at assertConfigurable()
        // ("This in stance of PersistenceManagerFactory is not configurable")
        // Do not need to set ConnectionFactory in the super class as the
        // getConnectionFactory() call is overridden here and will return
        // the correct value.
        // super.setConnectionFactory(cf);       
        return cf;
    }

    /** Verifies that the associated connection factory
     * is configured (at least the URL is specified).
     * @return if the connection factory is properly configured
     */
    protected boolean isConnectionFactoryConfigured() {
        // XXX FIXME : need to have cf.isConfigured()
        //return (cf == null);
        return (cf == null ? false : true);
        //return (cf == null ? false : cf.isConfigured());
    }

    /**
     * @see PersistenceManagerFactoryImpl#getOptionArray
     */
    protected String[] getOptionArray() {
        return optionArray;
    }

    /**
     * @see PersistenceManagerFactoryImpl#setCFProperties
     */
    protected void setCFProperties(Properties p) {
        // XXX : implement when needed
        throw new UnsupportedOperationException();
        //if (cf != null) {
        //    cf.setProperties(p);
        //}
    }

    /**
     * @see PersistenceManagerFactoryImpl#getCFFromProperties
     */
    protected void getCFFromProperties(Properties p) {
        // XXX : implement when needed
        throw new UnsupportedOperationException();
        //cf = new ConnectionFactoryImpl();
        //cf.setFromProperties(p);
    }

    /**
     * @see PersistenceManagerFactoryImpl#setPMFClassProperty
     */
    protected void setPMFClassProperty(Properties props) {
        props.setProperty ("com.sun.persistence.support.PersistenceManagerFactoryClass", "com.sun.persistence.runtime.sqlstore.impl.SQLPersistenceManagerFactory"); // NOI18N
    }

    /**
      * Uses rot13 algorithm.
      * @see PersistenceManagerFactoryImpl#encrypt
      */
    protected String encrypt(String s) {
        return doEncrypt(s);
    }

    /**
      * Uses rot13 algorithm.
      * @see PersistenceManagerFactoryImpl#decrypt
      */
    protected String decrypt(String s) {
        return doEncrypt(s);
    }

    /**
     * Use same encryption for others in this package.
     */
    static String doEncrypt(String s) {
        return doDecrypt(s);
    }

    /**
     * Use same encryption for others in this package.
     */
    static String doDecrypt(String s) {
        String rc = null;
        if (null != s) {
            rc = rot13(s);
        }
        return rc;
    }

    // Standard Rot13 stuff.  Translated to Java from a C implementation found
    // on the net.
    static private String rot13(String s) {
        String rc = null;
        int length = s.length();
        StringBuffer sb = new StringBuffer(length);
        for (int i = 0; i < length; i++) {
            int c = s.charAt(i);
            int cap = c & 32;
            c &= ~cap;
            c = ((c >= 'A') && (c <= 'Z') ? ((c - 'A' + 13) % 26 + 'A') : c) | cap;
            sb.append((char)c);
        }
        rc =  sb.toString();
        return rc;
    }

    /**
     * Get instance of RuntimeMappingModel for the persistence unit corresponding
     * to this pmf
     * @return Instance of RuntimeMappingModel for the persistence unit
     * corresponding to this pmf
     */
    public synchronized RuntimeMappingModel getRuntimeMappingModel() {
        //TODO: Following code assumes that the cardinality between persistence
        // unit and instance of SQLPersistenceManagerFactory is 1:1.
        // This might not be true.
        if (model == null) {
            JavaModelFactory javaModelFactory = 
                (JavaModelFactory) AccessController.doPrivileged(
                    new PrivilegedAction () {
                        public Object run () {
                            return RuntimeJavaModelFactory.getInstance();
                        }
                    }
                    );
            JDOModelFactory jdoModelFactory = JDOModelFactoryImplCaching.getInstance();
            RuntimeMappingModelFactory mappingModelFactory =
                    RuntimeMappingModelFactoryImpl.getInstance();
            //USe the classloader that loaded this application
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (EJBImplHelper.isManaged()) {
                // Get the JavaModel for the context class loader
                JavaModel javaModel = javaModelFactory.getJavaModel(cl);
                // Get the JDOModel for the JavaModel; the JDOModel is already
                // populated, so pass loadXMLMetadataDefault=false to prevent
                // looking for metadata files.
                JDOModel jdoModel = jdoModelFactory.getJDOModel(javaModel, false);
                // Today we cache only by jdoModel, so the second argument is
                // null. This should be replaced by pmf or emf later.
                model =  mappingModelFactory.getMappingModel(jdoModel, null);
            } else {
                // non-managed environment => 
                // call StandaloneDeployer to do model bootstrap 
                try {
                    model = (RuntimeMappingModel)
                        StandaloneDeployer.mapMappingModel(cl, javaModelFactory,
                            jdoModelFactory, mappingModelFactory);
                } catch (Exception ex) {
                    //TODO: Put the message in bundle
                    throw new JDOFatalException
                        ("Problems during mapping model initialization.", ex);
                }
            }
        }
        return model;
    }

    public DBVendorType getDBVendorType(){
        return dbVendor;
    }
}
