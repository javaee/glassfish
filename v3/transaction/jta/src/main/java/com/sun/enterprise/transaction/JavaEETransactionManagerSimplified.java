/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.enterprise.transaction;

import java.util.*;
import java.util.logging.*;
import java.rmi.RemoteException;
import javax.transaction.*;
import javax.transaction.xa.*;
import javax.resource.spi.XATerminator;
import javax.resource.spi.work.WorkException;

import com.sun.appserv.util.cache.Cache;
import com.sun.appserv.util.cache.BaseCache;

import com.sun.enterprise.transaction.api.JavaEETransaction;
import com.sun.enterprise.transaction.api.JavaEETransactionManager;
import com.sun.enterprise.transaction.spi.JavaEETransactionManagerDelegate;
import com.sun.enterprise.transaction.spi.TransactionalResource;

import com.sun.logging.LogDomains;
import com.sun.enterprise.util.i18n.StringManager;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.ContractProvided;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PostConstruct;

import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.invocation.InvocationException;
import org.glassfish.api.invocation.ResourceHandler;

/**
 * Implementation of javax.transaction.TransactionManager interface.
 * This class provides non-XA local transaction support and delegates 
 * to implementation of the JavaEETransactionManagerDelegate for XA 
 * or LAO optimization, and complete JTS implementation.
 *
 * @author Tony Ng
 * @author Marina Vatkina
 */
@Service
@ContractProvided(TransactionManager.class)
public class JavaEETransactionManagerSimplified 
        implements JavaEETransactionManager, PostConstruct {

    protected Logger _logger = LogDomains.getLogger(LogDomains.JTA_LOGGER);

    @Inject protected InvocationManager invMgr;

    // XXX @Inject private ServerContext sCtx;

    @Inject private Habitat habitat;

    private JavaEETransactionManagerDelegate delegate;

    // Sting Manager for Localization
    private static StringManager sm 
           = StringManager.getManager(JavaEETransactionManagerSimplified.class);

    // Note: this is not inheritable because we dont want transactions
    // to be inherited by child threads.
    ThreadLocal<JavaEETransactionImpl> transactions;

    private ThreadLocal localCallCounter;
    private ThreadLocal<JavaEETransactionManagerDelegate> delegates;

    // If multipleEnlistDelists is set to true, with in the transaction, for the same
    //  - connection multiple enlistments and delistments might happen
    // - By setting the System property ALLOW_MULTIPLE_ENLISTS_DELISTS to true
    // - multipleEnlistDelists can be enabled
    private boolean multipleEnlistDelists = false;

    protected int transactionTimeout;
    protected ThreadLocal<Integer> txnTmout = new ThreadLocal();

    // admin and monitoring related parameters
    protected static Hashtable statusMap = new Hashtable();
    protected Vector activeTransactions = new Vector();
    protected boolean monitoringEnabled = false;

    protected int m_transCommitted = 0;
    protected int m_transRolledback = 0;
    protected int m_transInFlight = 0;

    private Cache resourceTable;

    private  Timer _timer = new Timer("transaction-manager", true);

    private static java.util.concurrent.locks.ReentrantReadWriteLock freezeLock = new java.util.concurrent.locks.ReentrantReadWriteLock();

    static {
        statusMap.put(javax.transaction.Status.STATUS_ACTIVE, "Active");
        statusMap.put(javax.transaction.Status.STATUS_MARKED_ROLLBACK, "MarkedRollback");
        statusMap.put(javax.transaction.Status.STATUS_PREPARED, "Prepared");
        statusMap.put(javax.transaction.Status.STATUS_COMMITTED, "Committed");
        statusMap.put(javax.transaction.Status.STATUS_ROLLEDBACK, "RolledBack");
        statusMap.put(javax.transaction.Status.STATUS_UNKNOWN, "UnKnown");
        statusMap.put(javax.transaction.Status.STATUS_NO_TRANSACTION, "NoTransaction");
        statusMap.put(javax.transaction.Status.STATUS_PREPARING, "Preparing");
        statusMap.put(javax.transaction.Status.STATUS_COMMITTING, "Committing");
        statusMap.put(javax.transaction.Status.STATUS_ROLLING_BACK, "RollingBack");
    }
    public JavaEETransactionManagerSimplified() {
        transactions = new ThreadLocal<JavaEETransactionImpl>();
        localCallCounter = new ThreadLocal();
        delegates = new ThreadLocal<JavaEETransactionManagerDelegate>();
    }

    public void postConstruct() {
        init();
        initDelegate();
    }
    protected void init() {
        int maxEntries = 8192; // FIXME: this maxEntry should be a config
        float loadFactor = 0.75f; // FIXME: this loadFactor should be a config
        // for now, let's get it from system prop
        try {
            String mEnlistDelists
                = System.getProperty("ALLOW_MULTIPLE_ENLISTS_DELISTS");
            if ("true".equals(mEnlistDelists)) {
                multipleEnlistDelists = true;
                if (_logger.isLoggable(Level.FINE))
                    _logger.log(Level.FINE,"TM: multiple enlists, delists are enabled");
            }
            String maxEntriesValue
                = System.getProperty("JTA_RESOURCE_TABLE_MAX_ENTRIES");
            if (maxEntriesValue != null) {
                int temp = Integer.parseInt(maxEntriesValue);
                if (temp > 0) {
                    maxEntries = temp;
                }
            }
            String loadFactorValue
                = System.getProperty("JTA_RESOURCE_TABLE_DEFAULT_LOAD_FACTOR");
            if (loadFactorValue != null) {
                float f = Float.parseFloat(loadFactorValue);
                if (f > 0) {
                     loadFactor = f;
                }
            }
        } catch (Exception ex) {
            // ignore
        }

        Properties cacheProps = null;

        resourceTable = new BaseCache();
        ((BaseCache)resourceTable).init(maxEntries, loadFactor, cacheProps);
        // END IASRI 4705808 TTT001

/** XXX  TransactionService XXX
        // running on the server side
        if (sCtx != null) {
            ConfigContext ctx = sCtx.getConfigContext();
            TransactionService txnService = null;
            try {
                txnService = ServerBeansFactory.getTransactionServiceBean(ctx);
                transactionTimeout = Integer.parseInt(txnService.getTimeoutInSeconds());
                ElementProperty[] eprops = txnService.getElementProperty();
                for (int index = 0; index < eprops.length; index++) {
                    if ("use-last-agent-optimization".equals(eprops[index].getName())) {
                        if ("false".equals(eprops[index].getValue())) {
                           // XXX Need to set it on the reset delegate also
                            delegate.setUseLAO(false);
                            if (_logger.isLoggable(Level.FINE))
                                _logger.log(Level.FINE,"TM: LAO is disabled");
                        }
                    }
                }
            } catch(ConfigException e) {
                throw new RuntimeException(sm.getString("enterprise_distributedtx.config_excep",e));
            } catch (NumberFormatException ex) {
            }
        }
*** XXX **/
        // ENF OF BUG 4665539
                if (_logger.isLoggable(Level.FINE))
                _logger.log(Level.FINE,"TM: Tx Timeout = " + transactionTimeout);

        // START IASRI 4705808 TTT004 -- monitor resource table stats
        try {
            String doStats
                = System.getProperty("MONITOR_JTA_RESOURCE_TABLE_STATISTICS");
            if (Boolean.getBoolean(doStats)) {
                registerStatisticMonitorTask();
            }
        } catch (Exception ex) {
            // ignore
        }
    }

    public void clearThreadTx() {
        transactions.set(null);
        delegates.set(null);
    }

/****************************************************************************/
/** Implementations of JavaEETransactionManager APIs **************************/
/****************************************************************************/

    /**
     * Return true if a "null transaction context" was received
     * from the client. See EJB2.0 spec section 19.6.2.1.
     * A null tx context has no Coordinator objref. It indicates
     * that the client had an active
     * tx but the client container did not support tx interop.
     */
    public boolean isNullTransaction() {
        return true;
    }

    public void recover(XAResource[] resourceList) {
        throw new UnsupportedOperationException("recover");
    }

    public boolean enlistResource(Transaction tran, TransactionalResource h)
            throws RollbackException, IllegalStateException, SystemException {
        if ( !h.isTransactional() )
            return true;

        //If LazyEnlistment is suspended, do not enlist resource.
        if(h.isEnlistmentSuspended()){
            return false;
        }

       JavaEETransactionImpl tx = (JavaEETransactionImpl)tran;

       if(_logger.isLoggable(Level.FINE)) {
           _logger.log(Level.FINE,"\n\nIn JavaEETransactionManagerSimplified.enlistResource, h=" 
                   +h+" h.xares="+h.getXAResource()
                   /** +" h.alloc=" +h.getResourceAllocator() **/ +" tx="+tx);
       }

       JavaEETransactionManagerDelegate d = setDelegate();
       boolean useLAO = d.useLAO();

       if ( (tx.getNonXAResource()!=null) && (!useLAO || (useLAO && !h.supportsXA()))) {
           boolean isSameRM=false;
           try {
               isSameRM = h.getXAResource().isSameRM(tx.getNonXAResource().getXAResource());
           } catch ( XAException xex ) {
               throw new SystemException(sm.getString("enterprise_distributedtx.samerm_excep",xex));
           } catch ( Exception ex ) {
               throw new SystemException(sm.getString("enterprise_distributedtx.samerm_excep",ex));
           }
           if ( !isSameRM ) {
               throw new IllegalStateException(sm.getString("enterprise_distributedtx.already_has_nonxa"));
           }
       }

       if (monitoringEnabled) {
           tx.addResourceName(h.getName());
       }

       if ( h.supportsXA() ) {
           if ( tx.isLocalTx() ) {
               d.enlistLAOResource(tx, tx.getNonXAResource());

/** XXX MOVE TO DELEGATE XXX **
               startJTSTx(tx);

               //If transaction conatains a NonXA and no LAO, convert the existing
               //Non XA to LAO
               if(useLAO) {
                   if(tx.getNonXAResource()!=null && (tx.getLAOResource()==null) ) {
                       tx.setLAOResource(tx.getNonXAResource());
                       // XXX super.enlistLAOResource(tx, tx.getNonXAResource());
                   }
               }
** XXX MOVE TO XA DELEGATES XXX **/
           }
           return enlistXAResource(tx, h);
       } else { // non-XA resource
            if (tx.isImportedTransaction())
                throw new IllegalStateException(
                        sm.getString("enterprise_distributedtx.nonxa_usein_jts"));
            if (tx.getNonXAResource() == null) {
                tx.setNonXAResource(h);
            }
            if ( tx.isLocalTx() ) {
                // notify resource that it is being used for tx,
                // e.g. this allows the correct physical connection to be
                // swapped in for the logical connection.
                // The flags parameter can be 0 because the flags are not
                // used by the XAResource implementation for non-XA resources.
                try {
                    h.getXAResource().start(tx.getLocalXid(), 0);
                } catch ( XAException ex ) {
                    throw new RuntimeException(
                            sm.getString("enterprise_distributedtx.xaresource_start_excep"),ex);
                }

                h.enlistedInTransaction(tx);
                return true;
            } else {
                return d.enlistDistributedNonXAResource(tx, h);
/** XXX MOVE TO XA DELEGATES? XXX
                if(useLAO) {
                    return super.enlistResource(tx, h);
                } else {
                    throw new IllegalStateException(
                            sm.getString("enterprise_distributedtx.nonxa_usein_jts"));
                }
** XXX MOVE TO XA DELEGATES? XXX **/
            }
        }
    }

    public void unregisterComponentResource(TransactionalResource h) {
        Object instance = h.getComponentInstance();
        if (instance == null) return;
        h.setComponentInstance(null);
        ComponentInvocation inv = invMgr.getCurrentInvocation();
        List l = getExistingResourceList(instance, inv);

        if (l != null) {
            l.remove(h);
        }
    }

    void startJTSTx(JavaEETransactionImpl tx)
            throws RollbackException, IllegalStateException, SystemException {
        try {
            if (tx.isAssociatedTimeout()) {
                // calculate the timeout for the transaction, this is required as the local tx 
                // is getting converted to a global transaction
                int timeout = tx.cancelTimerTask();
                int newtimeout = (int) ((System.currentTimeMillis() - tx.getStartTime()) / 1000);
                newtimeout = (timeout -   newtimeout);
                begin(newtimeout);
            } else {
                begin();
            }
            
            // The local Transaction was promoted to global Transaction
            if (tx!=null && monitoringEnabled){
                if(activeTransactions.remove(tx)){
                    m_transInFlight--;
                }
            }
            
        } catch ( NotSupportedException ex ) {
            throw new RuntimeException(sm.getString("enterprise_distributedtx.lazy_transaction_notstarted"),ex);
        }
        Transaction jtsTx = getTransaction();
        tx.setJTSTx(jtsTx);
        jtsTx.registerSynchronization(new JTSSynchronization(jtsTx, this));
    }

    public List getResourceList(Object instance, ComponentInvocation inv) {
        if (inv == null)
            return new ArrayList(0);
        List l = null;

/** XXX EJB CONTAINER ONLY XXX -- NEED TO CHECK THE NEW CODE BELOW **
        if (inv.getInvocationType() == 
                ComponentInvocation.ComponentInvocationType.EJB_INVOCATION) {
            ComponentContext ctx = inv.context;
            if (ctx != null)
                l = ctx.getResourceList();
            else {
                l = new ArrayList(0);
            }
        }
** XXX EJB CONTAINER ONLY XXX **/

        ResourceHandler rh = inv.getResourceHandler();
        if (rh != null) {
            l = rh.getResourceList();
            if (l == null) {
                l = new ArrayList(0);
            }
        }
        else {
            Object key = getResourceTableKey(instance, inv);
            if (key == null)
                return new ArrayList(0);
            l = (List) resourceTable.get(key);
            if (l == null) {
                l = new ArrayList(); //FIXME: use an optimum size?
                resourceTable.put(key, l);
            }
        }
        return l;
    }

    public void enlistComponentResources() throws RemoteException {
        if (_logger.isLoggable(Level.FINE))
            _logger.log(Level.FINE,"TM: enlistComponentResources");

        ComponentInvocation inv = invMgr.getCurrentInvocation();
        if (inv == null)
            return;
        try {
            Transaction tran = getTransaction();
            inv.setTransaction((JavaEETransactionImpl)tran);
            enlistComponentResources(inv);
        } catch (InvocationException ex) {
            _logger.log(Level.SEVERE,"enterprise_distributedtx.excep_in_enlist" ,ex);
            throw new RemoteException(ex.getMessage(), ex.getNestedException());
        } catch (Exception ex) {
            _logger.log(Level.SEVERE,"enterprise_distributedtx.excep_in_enlist" ,ex);
            throw new RemoteException(ex.getMessage(), ex);
        }
    }

    public boolean delistResource(Transaction tran, TransactionalResource h, int flag)
            throws IllegalStateException, SystemException {
        if (!h.isTransactional()) return true;

        JavaEETransactionImpl tx = (JavaEETransactionImpl)tran;
        if ( tx.isLocalTx() ) {
            // dissociate resource from tx
            try {
                h.getXAResource().end(tx.getLocalXid(), flag);
            } catch ( XAException ex ) {
                throw new RuntimeException(sm.getString("enterprise_distributedtx.xaresource_end_excep", ex));
            }
            return true;
        }
        else
            return delistJTSResource(tran, h, flag);
    }

    public void delistComponentResources(boolean suspend)
            throws RemoteException {
        if (_logger.isLoggable(Level.FINE))
            _logger.log(Level.FINE,"TM: delistComponentResources");
        ComponentInvocation inv = invMgr.getCurrentInvocation();
        // BEGIN IASRI# 4646060
        if (inv == null) {
            return;
        }
        // END IASRI# 4646060
        try {
            delistComponentResources(inv, suspend);
        } catch (InvocationException ex) {
            _logger.log(Level.SEVERE,"enterprise_distributedtx.excep_in_delist",ex);
            throw new RemoteException("", ex.getNestedException());
        } catch (Exception ex) {
            _logger.log(Level.SEVERE,"enterprise_distributedtx.excep_in_delist",ex);
            throw new RemoteException("", ex);
        }
    }


    public void registerComponentResource(TransactionalResource h) {
        ComponentInvocation inv = invMgr.getCurrentInvocation();
        if (inv != null) {
            Object instance = inv.getInstance();
            if (instance == null) return;
            h.setComponentInstance(instance);
            List l = getResourceList(instance, inv);
            l.add(h);
        }
    }

    private List getExistingResourceList(Object instance) {
        throw new IllegalStateException("Should not be called");
/**
        if (instance == null)
            return null;
        Object key = getResourceTableKey(instance);
        if (key == null)
            return null;
        return (List) resourceTable.get(key);
**/
    }

    public List getExistingResourceList(Object instance, ComponentInvocation inv) {
       if (inv == null)
           return null;
        List l = null;

/** XXX EJB CONTAINER ONLY XXX -- NEED TO CHECK THE NEW CODE BELOW **
        if (inv.getInvocationType() == 
                ComponentInvocation.ComponentInvocationType.EJB_INVOCATION) {
            ComponentContext ctx = inv.context;
            if (ctx != null)
                l = ctx.getResourceList();
            return l;
        }
** XXX EJB CONTAINER ONLY XXX **/

        ResourceHandler rh = inv.getResourceHandler();
        if (rh != null) {
            l = rh.getResourceList();
        }
        else {
            Object key = getResourceTableKey(instance, inv);
            if (key != null)
                l =  (List) resourceTable.get(key);
        }
        return l;
    }

    public void preInvoke(ComponentInvocation prev)
            throws InvocationException {
        if ( prev != null && prev.getTransaction() != null &&
            prev.isTransactionCompleting() == false) {
            // do not worry about delisting previous invocation resources
            // if transaction is being completed
            delistComponentResources(prev, true);  // delist with TMSUSPEND
        }

    }

    public void postInvoke(ComponentInvocation curr, ComponentInvocation prev)
            throws InvocationException {

        if ( curr != null && curr.getTransaction() != null )
            delistComponentResources(curr, false);  // delist with TMSUCCESS
        if ( prev != null && prev.getTransaction() != null &&
                prev.isTransactionCompleting() == false) {
            // do not worry about re-enlisting previous invocation resources
            // if transaction is being completed
            enlistComponentResources(prev);
        }

    }

    public void componentDestroyed(Object instance) {
        componentDestroyed(instance, null);
    }

    public void componentDestroyed(Object instance, ComponentInvocation inv) {
        if (_logger.isLoggable(Level.FINE))
            _logger.log(Level.FINE,"TM: componentDestroyed" + instance);

        // Access resourceTable directly to avoid adding an empty list then removing it
        List l = (List)resourceTable.get(getResourceTableKey(instance, inv));
        if (l != null && l.size() > 0) {
            resourceTable.remove(getResourceTableKey(instance, inv));
            Iterator it = l.iterator();
            while (it.hasNext()) {
                TransactionalResource h = (TransactionalResource) it.next();
                try {
                    h.closeUserConnection();
                } catch (Exception ex) {
                    if (_logger.isLoggable(Level.FINE))
                        _logger.log(Level.WARNING,"enterprise_distributedtx.pooling_excep", ex);
                }
            }
            l.clear();
        }
    }

    public void ejbDestroyed(ComponentInvocation inv) {
        if (_logger.isLoggable(Level.FINE))
            _logger.log(Level.FINE, " ejbDestroyed: " + inv);

        List l = null;
        ResourceHandler rh = inv.getResourceHandler();
        if (rh != null) {
            l = rh.getResourceList();

            if (l != null && l.size() > 0) {
                Iterator it = l.iterator();
                while (it.hasNext()) {
                    TransactionalResource h = (TransactionalResource) it.next();
                    try {
                        h.closeUserConnection();
                    } catch (Exception ex) {
                        if (_logger.isLoggable(Level.FINE))
                            _logger.log(Level.WARNING,"enterprise_distributedtx.pooling_excep", ex);
                    }
                }
                l.clear();
            }
        }
    }

    public boolean isTimedOut() {
        JavaEETransactionImpl tx = transactions.get();
        if ( tx != null)
            return tx.isTimedout();
        else
            return false;
    }

    /**
     * Called from the CORBA Interceptors on the server-side when
     * the server is replying to the client (local + remote client).
     * Check if there is an active transaction and remove it from TLS.
     */
    public void checkTransactionImport() {
        // First check if this is a local call
        int[] count = (int[])localCallCounter.get();
        if ( count != null && count[0] > 0 ) {
            count[0]--;
            return;
        }
        else {
            // A remote call, clear TLS so that if this thread is reused
            // later, the current tx doesnt hang around.
            clearThreadTx();
        }
    }

    /**
     * Called from the CORBA Interceptors on the client-side when
     * a client makes a call to a remote object (not in the same JVM).
     * Check if there is an active, exportable transaction.
     * @exception RuntimeException if the transaction is not exportable
     */
    public void checkTransactionExport(boolean isLocal) {

        if ( isLocal ) {
            // Put a counter in TLS indicating this is a local call.
            // Use int[1] as a mutable java.lang.Integer!
            int[] count = (int[])localCallCounter.get();
            if ( count == null ) {
                count = new int[1];
                localCallCounter.set(count);
            }
            count[0]++;
            return;
        }

        JavaEETransactionImpl tx = transactions.get();
        if ( tx == null )
            return;

        if ( !tx.isLocalTx() ) // a JTS tx, can be exported
            return;

        // Check if a local tx with non-XA resource is being exported.
        // XXX what if this is a call on a non-transactional remote object ?
        if ( tx.getNonXAResource() != null )
            throw new RuntimeException(sm.getString("enterprise_distributedtx.cannot_export_transaction_having_nonxa"));

        // If we came here, it means we have a local tx with no registered
        // resources, so start a JTS tx which can be exported.
        try {
            startJTSTx(tx);
        } catch ( RollbackException rlex ) {
            throw new RuntimeException(sm.getString("enterprise_distributedtx.unable_tostart_JTSTransaction"),rlex);
        } catch ( IllegalStateException isex ) {
            throw new RuntimeException(sm.getString("enterprise_distributedtx.unable_tostart_JTSTransaction"),isex);
        } catch ( SystemException ex ) {
            throw new RuntimeException(sm.getString("enterprise_distributedtx.unable_tostart_JTSTransaction"),ex);
        } catch ( Exception excep ) {
            throw new RuntimeException(sm.getString("enterprise_distributedtx.unable_tostart_JTSTransaction"),excep);
        }
    }

    /**
     * This is used by importing transactions via the Connector contract.
     * Should not be called
     *
     * @return a <code>XATerminator</code> instance.
     * @throws UnsupportedOperationException
     */
    public XATerminator getXATerminator() {
        throw new UnsupportedOperationException("getXATerminator");
    }

    /**
     * Release a transaction. This call causes the calling thread to be
     * dissociated from the specified transaction. <p>
     * This is used by importing transactions via the Connector contract.
     *
     * @param xid the Xid object representing a transaction.
     */
    public void release(Xid xid) throws WorkException {
        throw new UnsupportedOperationException("release");
    }

    /**
     * Recreate a transaction based on the Xid. This call causes the calling
     * thread to be associated with the specified transaction. <p>
     * This is used by importing transactions via the Connector contract.
     *
     * @param xid the Xid object representing a transaction.
     */
    public void recreate(Xid xid, long timeout) throws WorkException {
        throw new UnsupportedOperationException("recreate");
    }

/****************************************************************************/
/** Implementations of JTA TransactionManager APIs **************************/
/****************************************************************************/

    public void registerSynchronization(Synchronization sync)
            throws IllegalStateException, SystemException {
        if (_logger.isLoggable(Level.FINE))
            _logger.log(Level.FINE,"TM: registerSynchronization");

        try {
            Transaction tran = getTransaction();
            if (tran != null) {
                tran.registerSynchronization(sync);
            }
        } catch (RollbackException ex) {
            _logger.log(Level.SEVERE,"enterprise_distributedtx.rollbackexcep_in_regsynch",ex);
            throw new IllegalStateException();
        }
    }

   // Implementation of begin() is moved to begin(int timeout) 
   public void begin() throws NotSupportedException, SystemException {
       begin(getEffectiveTimeout());
   }

   /**
    * This method is introduced as part of implementing the local transaction timeout 
    * capability. Implementation of begin() moved here. Previpusly there is no timeout
    * infrastructure for local txns, so when ever a timeout required for local txn, it
    * uses the globaltxn timeout infrastructure by doing an XA simulation.   
    **/
   public void begin(int timeout) throws NotSupportedException, SystemException {
       // Check if tx already exists
       if ( transactions.get() != null )
           throw new NotSupportedException(sm.getString("enterprise_distributedtx.notsupported_nested_transaction"));

       setDelegate();

       // Check if JTS tx exists, without starting JTS tx.
       // This is needed in case the JTS tx was imported from a client.
       if ( getStatus() != Status.STATUS_NO_TRANSACTION )
           throw new NotSupportedException(sm.getString("enterprise_distributedtx.notsupported_nested_transaction"));

        // START IASRI 4662745
        boolean acquiredlock = false;
        if(monitoringEnabled){
             freezeLock.readLock().lock(); // XXX acquireReadLock();
             acquiredlock = true;
        }
        try{
            JavaEETransactionImpl tx = null;
            if (timeout > 0)
                tx = new JavaEETransactionImpl(timeout);
            else
                tx = new JavaEETransactionImpl();

            // Do not need to use injection.
            tx.javaEETM = this;

            transactions.set(tx);
            if (monitoringEnabled) {
                activeTransactions.addElement(tx);
                m_transInFlight++;
                ComponentInvocation inv = invMgr.getCurrentInvocation();
                if (inv != null && inv.getInstance() != null) {
                    tx.setComponentName(inv.getInstance().getClass().getName());
                }
            }
        }finally{
            if(acquiredlock){
                freezeLock.readLock().unlock(); // XXX releaseReadLock();
            }
        }
        // START IASRI 4662745
    }

    public void commit() throws RollbackException,
            HeuristicMixedException, HeuristicRollbackException, SecurityException,
            IllegalStateException, SystemException {

        try {
            JavaEETransactionImpl tx = transactions.get();
            if ( tx != null && tx.isLocalTx()) {
                Object obj = null;
                boolean acquiredlock = false;
                if(monitoringEnabled){
                    obj = tx;
                }

                if(monitoringEnabled){
                    freezeLock.readLock().lock(); // XXX acquireReadLock();
                    acquiredlock = true;
                }

                try{
                    tx.commit(); // commit local tx
                    if (monitoringEnabled){
                        monitorTxCompleted(obj, true);
                    }
                }catch(RollbackException e){
                    if (monitoringEnabled){
                        monitorTxCompleted(obj, false);
                    }
                    throw e;
                }catch(HeuristicRollbackException e){
                    if (monitoringEnabled){
                        monitorTxCompleted(obj, false);
                    }
                    throw e;
                }catch(HeuristicMixedException e){
                    if (monitoringEnabled){
                        monitorTxCompleted(obj, true);
                    }
                    throw e;
                }finally{
                    if(acquiredlock){
                        freezeLock.readLock().unlock(); // XXX releaseReadLock();
                    }
                }
            }
            else  {
                // it might be a JTS imported global tx or an error
                getDelegate().commitDistributedTransaction(); 
            }

        } finally {
            transactions.set(null); // clear current thread's tx
            delegates.set(null);
        }
        // END IASRI 4662745
    }

    public void rollback() throws IllegalStateException, SecurityException,
                SystemException {
        boolean acquiredlock=false;
        try {
            JavaEETransactionImpl tx = transactions.get();
            if ( tx != null && tx.isLocalTx()) {
                Object obj = null;
                if(monitoringEnabled){
                    obj = tx;
                }
                if(monitoringEnabled){
                    freezeLock.readLock().lock(); // XXX acquireReadLock();
                    acquiredlock = true;
                }
                tx.rollback(); // rollback local tx
                if(monitoringEnabled){
                    monitorTxCompleted(obj, false);
                }
            }
            else  {
                getDelegate().rollbackDistributedTransaction(); // a JTS imported global tx or an error
            }

        } finally {
            transactions.set(null); // clear current thread's tx
            delegates.set(null);
            if(acquiredlock){
                freezeLock.readLock().unlock(); // XXX releaseReadLock();
            }
        }
    }


    public int getStatus() throws SystemException {
        return getDelegate().getStatus();
    }

    public Transaction getTransaction() throws SystemException {
        return getDelegate().getTransaction();

/** XXX MOVE TO THE JTS DELEGATE XXX **
            Transaction jtsTx = super.getTransaction();
            if ( jtsTx == null )
                return null;
            else {
                // check if this JTS Transaction was previously active
                // in this JVM (possible for distributed loopbacks).
                tx = (JavaEETransactionImpl)globalTransactions.get(jtsTx);
                if ( tx == null ) {
                    tx = new JavaEETransactionImpl(jtsTx);
                    tx.setImportedTransaction();
                    try {
                        jtsTx.registerSynchronization(
                                new JTSSynchronization(jtsTx, this));
                    } catch ( RollbackException rlex ) {
                        throw new SystemException(rlex.toString());
                    } catch ( IllegalStateException isex ) {
                        throw new SystemException(isex.toString());
                    } catch ( Exception ex ) {
                        throw new SystemException(ex.toString());
                    } 

                    globalTransactions.put(jtsTx, tx);
                }
                transactions.set(tx); // associate tx with thread
                return tx;
            }
** XXX MOVE TO THE JTS DELEGATE XXX **/
    }

    public void setRollbackOnly()
        throws IllegalStateException, SystemException {

        JavaEETransactionImpl tx = transactions.get();
        // START IASRI 4662745
        if ( tx != null && tx.isLocalTx()){
            boolean acquiredlock=false;
            if(monitoringEnabled){
                freezeLock.readLock().lock(); // XXX acquireReadLock();
                acquiredlock = true;
            }
            try{
                tx.setRollbackOnly();
            }finally{
                if(acquiredlock){
                    freezeLock.readLock().unlock(); // XXX releaseReadLock();
                }
            }
        }
        else
            getDelegate().setRollbackOnlyDistributedTransaction(); // probably a JTS imported tx
    }

    public Transaction suspend() throws SystemException {
        return getDelegate().suspend(transactions.get());
/** XXX MOVE TO DELEGATES XXX **
        if ( tx != null ) {
            if ( !tx.isLocalTx() )
                super.suspend();

            transactions.set(null);
            return tx;
        }
        else {
            return super.suspend(); // probably a JTS imported tx
        }
** XXX MOVE TO DELEGATES XXX **/
    }

    public void resume(Transaction tobj)
            throws InvalidTransactionException, IllegalStateException,
            SystemException {

        JavaEETransactionImpl tx = transactions.get();
        if ( tx != null )
            throw new IllegalStateException(
                    sm.getString("enterprise_distributedtx.transaction_exist_on_currentThread"));

        if ( tobj instanceof JavaEETransactionImpl ) {
            JavaEETransactionImpl javaEETx = (JavaEETransactionImpl)tobj;
            if ( !javaEETx.isLocalTx() )
                getDelegate().resume(javaEETx.getJTSTx());

            transactions.set(javaEETx);
        }
        else {
            getDelegate().resume(tobj); // probably a JTS imported tx
        }
    }

    /**
     * Modify the value of the timeout value that is associated with the
     * transactions started by the current thread with the begin method.
     *
     * <p> If an application has not called this method, the transaction
     * service uses some default value for the transaction timeout.
     *
     * @param seconds The value of the timeout in seconds. If the value
     *    is zero, the transaction service restores the default value.
     *
     * @exception SystemException Thrown if the transaction manager
     *    encounters an unexpected error condition
     *
     */
    public void setTransactionTimeout(int seconds) throws SystemException {
        if (seconds < 0) seconds = 0;
        txnTmout.set(seconds);
        // transactionTimeout = seconds;
    }

/****************************************************************************/
/*********************** Called by Admin Framework **************************/
/****************************************************************************/
   /*
    * Called by Admin Framework to freeze the transactions.
    */
    public synchronized void freeze(){
        // XXX ??? super.freeze();
        if(freezeLock.isWriteLocked()){
            //multiple freezes will hang this thread, therefore just return
            return;
        }
        freezeLock.writeLock().lock(); // XXX acquireWriteLock();
    }
    /*
     * Called by Admin Framework to freeze the transactions. These undoes the work done by the freeze.
     */
    public synchronized void unfreeze(){
        // XXX ??? super.unfreeze();
        if(freezeLock.isWriteLocked()){
            freezeLock.writeLock().unlock(); // XXX releaseWriteLock();
        }
    }

    /** XXX ???
     */
    public boolean isFrozen() {
        return freezeLock.isWriteLocked();
    }

    public void cleanTxnTimeout() {
        txnTmout.set(null);
    }

    int getEffectiveTimeout() {
        Integer tmout = txnTmout.get();
        if (tmout ==  null) {
            return transactionTimeout;
        }
        else {
            return tmout;
        }
    }

    public void setDefaultTransactionTimeout(int seconds) {
        if (seconds < 0) seconds = 0;
        transactionTimeout = seconds;
    }

    /* Returned Number of transactions Active
     *  Called by Admin Framework when transaction monitoring is enabled
     */
    public int getNumberOfActiveTransactions(){
        return m_transInFlight;
    }

   /*
    *  This method returns the details of the Currently Active Transactions
    *  Called by Admin Framework when transaction monitoring is enabled
    *  @returns ArrayList of TransactionAdminBean
    *  @see TransactionAdminBean
    */
    public ArrayList getActiveTransactions() {
        ArrayList tranBeans = new ArrayList();
        Vector active = (Vector)activeTransactions.clone(); // get the clone of the active transactions
        for(int i=0;i<active.size();i++){
            try{
                Transaction tran = (Transaction)active.elementAt(i);
                String id="unknown";
                long startTime = 0;
                long elapsedTime = 0;
                String status = "unknown";
                String componentName = "unknown";
                ArrayList<String> resourceNames = null;
                if(tran instanceof JavaEETransactionImpl){
                    JavaEETransactionImpl tran1 = (JavaEETransactionImpl)tran;
                    id=tran1.getTransactionId();
                    startTime = tran1.getStartTime();
                    componentName = tran1.getComponentName();
                    resourceNames = tran1.getResourceNames();
                }
                elapsedTime = System.currentTimeMillis()-startTime;
                status = (String)statusMap.get(new Integer(tran.getStatus()));
                TransactionAdminBean tBean = new TransactionAdminBean(tran,id,status,elapsedTime,
                                             componentName, resourceNames);
                tranBeans.add(tBean);
            }catch(Exception ex){
                //LOG !!!
            }
        }
        return tranBeans;
    }

    /* Returned Number of transactions Rolledback since the time monitoring
     * was enabeld
     * Called by Admin Framework when transaction monitoring is enabled
     */
    public int getNumberOfTransactionsRolledBack(){
        return m_transRolledback;
    }

    /* Returned Number of transactions commited since the time monitoring
     * was enabeld
     * Called by Admin Framework when transaction monitoring is enabled
     */
    public int getNumberOfTransactionsCommitted(){
        return m_transCommitted;
    }

    /*
     *  Called by Admin Framework when transaction monitoring is enabled
     */
    public void forceRollback(Transaction tran) throws IllegalStateException, SystemException{
        if (tran != null){
            tran.setRollbackOnly();
        }
    }

    public void setMonitoringEnabled(boolean enabled){
        monitoringEnabled = enabled;
        //reset the variables
        m_transCommitted = 0;
        m_transRolledback = 0;
        m_transInFlight = 0;
        activeTransactions.removeAllElements();
    }

    protected void monitorTxCompleted(Object tran, boolean committed){
        if(tran==null || !activeTransactions.remove(tran)){
            // WARN !!!
            return;
        }
        if(committed){
            m_transCommitted++;
        }else{
            m_transRolledback++;
        }
        m_transInFlight--;
    }

    // Mods: Adding method for statistic dumps using TimerTask
    private void registerStatisticMonitorTask() {
        TimerTask task = new StatisticMonitorTask();
        // for now, get monitoring interval from system prop
        int statInterval = 2 * 60 * 1000;
        try {
            String interval
                = System.getProperty("MONITOR_JTA_RESOURCE_TABLE_SECONDS");
            int temp = Integer.parseInt(interval);
            if (temp > 0) {
                statInterval = temp;
            }
        } catch (Exception ex) {
            // ignore
        }

        _timer.scheduleAtFixedRate(task, 0, statInterval);
    }

    // Mods: Adding TimerTask class for statistic dumps
    class StatisticMonitorTask extends TimerTask {
        public void run() {
            if (resourceTable != null) {
                Map stats = resourceTable.getStats();
                Iterator it = stats.keySet().iterator();
                String key;
                _logger.log(Level.INFO, 
                        "********** J2EETransactionManager resourceTable stats *****");
                while (it.hasNext()) {
                    key = (String)it.next();
                    _logger.log(Level.INFO, key + ": " + stats.get(key).toString());
                }
            }
        }
    }

/****************************************************************************/
/************************* Helper Methods ***********************************/
/****************************************************************************/
    protected static String getStatusAsString(int status) {
        return (String)statusMap.get(status);
    }

    private void delistComponentResources(ComponentInvocation inv,
                                          boolean suspend)
        throws InvocationException {

        try {
            Transaction tran = inv.getTransaction();
            if (isTransactionActive(tran)) {
                List l = getExistingResourceList(inv.getInstance(), inv);
                if (l == null || l.size() == 0)
                    return;

                int flag = (suspend)? XAResource.TMSUSPEND : XAResource.TMSUCCESS;
                Iterator it = l.iterator();
                while(it.hasNext()){
                    TransactionalResource h = (TransactionalResource)it.next();
                    try{
                        if ( h.isEnlisted() ) {
                            delistResource(tran, h, flag);
                        }
                    } catch (IllegalStateException ex) {
                        // ignore error due to tx time out
                    }catch(Exception ex){
                        it.remove();
                        handleResourceError(h, ex, tran);
                    }
                }
                //END OF IASRI 4658504
            }
        } catch (Exception ex) {
            _logger.log(Level.SEVERE,"enterprise_distributedtx.excep_in_delist",ex);
        }
    }

    protected boolean enlistXAResource(Transaction tran, TransactionalResource h)
            throws RollbackException, IllegalStateException, SystemException {

        if (_logger.isLoggable(Level.FINE))
            _logger.log(Level.FINE,"TM: enlistResource");

        if (h.isTransactional() && (!h.isEnlisted() || !h.isShareable() || multipleEnlistDelists)) {
            XAResource res = h.getXAResource();
            boolean result = tran.enlistResource(res);
            if (!h.isEnlisted())
                h.enlistedInTransaction(tran);
            return result;
        } else {
            return true;
        }
    }

    private void enlistComponentResources(ComponentInvocation inv)
    throws InvocationException {

        try {
            Transaction tran = inv.getTransaction();
            if (isTransactionActive(tran)) {
                List l = getExistingResourceList(inv.getInstance(), inv);
                if (l == null || l.size() == 0) return;
                Iterator it = l.iterator();
                // END IASRI 4705808 TTT002
                while(it.hasNext()) {
                    TransactionalResource h = (TransactionalResource) it.next();
                    try{
                        enlistResource(tran,h);
                    }catch(Exception ex){
                        it.remove();
                        handleResourceError(h,ex,tran);
                    }
                }
                //END OF IASRI 4658504
            }
        } catch (Exception ex) {
            _logger.log(Level.SEVERE,"enterprise_distributedtx.excep_in_enlist",ex);
        }
    }

    private void handleResourceError(TransactionalResource h,
                                     Exception ex, Transaction tran) {

        if (_logger.isLoggable(Level.FINE)) {
            if (h.isTransactional()) {
                _logger.log(Level.FINE,"TM: HandleResourceError " +
                                   h.getXAResource() +
                                   "," + ex);
            }
        }
        try {
            if (tran != null && h.isTransactional() && h.isEnlisted() ) {
                tran.delistResource(h.getXAResource(), XAResource.TMSUCCESS);
            }
        } catch (Exception ex2) {
            // ignore
        } 


        if (ex instanceof RollbackException) {
            // transaction marked as rollback
            return;
        } else if (ex instanceof IllegalStateException) {
            // transaction aborted by time out
            // close resource
            try {
                h.closeUserConnection();
            } catch (Exception ex2) {
                //Log.err.println(ex2);
            }
        } else {
            // destroy resource. RM Error.
            try {
                h.destroyResource();
            } catch (Exception ex2) {
                //Log.err.println(ex2);
            }
        }
    }

    private Object getResourceTableKey(Object instance, ComponentInvocation inv) {
        Object key = null;
        if ( inv != null) {
            key = inv.getResourceTableKey();
        } else {
            key = instance;
        }
        return key;
    }

    private boolean isTransactionActive(Transaction tran) {
        return (tran != null);
    }

    /**
     * JTS version of the #delistResource
     * @param suspend true if the transaction association should
     * be suspended rather than ended.
     */
    private boolean delistJTSResource(Transaction tran, TransactionalResource h,
                                  int flag)
        throws IllegalStateException, SystemException {

// ** XXX Throw an exception instead ??? XXX **
        if (_logger.isLoggable(Level.FINE))
            _logger.log(Level.FINE,"TM: delistResource");

        if (!h.isShareable() || multipleEnlistDelists) {
            if (h.isTransactional() && h.isEnlisted()) {
                return tran.delistResource(h.getXAResource(), flag);
            } else {
                return true;
            }
        }
        return true;
    }

    private void remove(Transaction tx) {
        getDelegate().removeTransaction(tx);

/** XXX MOVE TO DELEGATES XXX
        javaEETM.globalTransactions.remove(jtsTx);
** XXX MOVE TO DELEGATES XXX **/
    }

    private JavaEETransactionManagerDelegate getDelegate() {
        JavaEETransactionManagerDelegate d = delegates.get();
        return (d == null)? delegate : d;
    }

    private JavaEETransactionManagerDelegate setDelegate() {
        JavaEETransactionManagerDelegate d = delegates.get();
        if (d == null) {
            d = delegate;
            delegates.set(d);
        }

        return d;
    }

    private void initDelegate() {

        if (habitat == null)
            return; // the delegate will be set explicitly

        int order = 0;
        for (JavaEETransactionManagerDelegate d : 
                habitat.getAllByContract(JavaEETransactionManagerDelegate.class) ) {
            if (d.getOrder() > order) {
                order = d.getOrder();
                setDelegate(d);
                if (_logger.isLoggable(Level.FINE))
                    _logger.log(Level.FINE,"Replaced delegate with " 
                            + d.getClass().getName());
            }
        }
    }

    public void setDelegate(JavaEETransactionManagerDelegate d) {
        // XXX Check if it's valid to set
        delegate = d;
        delegate.setTransactionManager(this);
    }

/****************************************************************************/
/** Implementation of javax.transaction.Synchronization *********************/
/****************************************************************************/
    private class JTSSynchronization implements Synchronization {
        private Transaction jtsTx;
        private JavaEETransactionManagerSimplified javaEETM;
    
        JTSSynchronization(Transaction jtsTx, JavaEETransactionManagerSimplified javaEETM){
            this.jtsTx = jtsTx;
            this.javaEETM = javaEETM;
        }

        public void beforeCompletion() {}

        public void afterCompletion(int status) {
            javaEETM.remove(jtsTx);
        }
    }
}
