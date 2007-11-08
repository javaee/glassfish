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
package com.sun.enterprise.distributedtx;

import java.util.*;
import java.rmi.RemoteException;
import javax.transaction.*;
import javax.transaction.xa.*;

import com.sun.enterprise.*;
import com.sun.ejb.*;
import com.sun.enterprise.resource.*;
import com.sun.enterprise.log.Log;

import javax.servlet.Servlet;
import javax.servlet.Filter;
import javax.servlet.SingleThreadModel;
import javax.ejb.EnterpriseBean;

import javax.resource.spi.XATerminator;
import javax.resource.spi.work.WorkException;


// START IASRI 4662745
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.TransactionService;
import com.sun.enterprise.config.serverbeans.ElementProperty;
import com.sun.enterprise.transaction.TransactionAdminBean;
import com.sun.enterprise.transaction.JTSConfigChangeEventListener;
import com.sun.enterprise.admin.event.tx.JTSEvent;
import com.sun.enterprise.util.i18n.StringManager;
// END IASRI 4662745
//
import com.sun.enterprise.util.InvocationManagerImpl;

import com.sun.jts.jta.TransactionManagerImpl;

//START OF IASRI 4664284
import java.util.logging.*;
import com.sun.logging.*;
//END OF IASRI 4664284

// START IASRI 4705808 TTT001 -- use BaseCache instead of Hashtable
import com.sun.appserv.util.cache.Cache;
import com.sun.appserv.util.cache.BaseCache;
// END IASRI 4705808 TTT001


import com.sun.ejb.ComponentContext;


/**
 * This class implements the Transaction Manager for the J2EE RI.
 * It provides implementations of the JTA TransactionManager interface,
 * which delegates actual transaction work to JTS.
 * It also implements the association between transactions, resources and
 * components at various stages of the component lifecycle.
 *
 * @author Tony Ng
 */
public class J2EETransactionManagerImpl implements J2EETransactionManager {

    // START OF IASRI 4664284
    static Logger _logger=LogDomains.getLogger(LogDomains.JTA_LOGGER);
    // END OF IASRI 4664284

    private static final String TX_TIMEOUT = "transaction.timeout";
    private static final String TX_OPT = "transaction.nonXA.optimization";

    // an implementation of the JTA TransactionManager provided by JTS.
    private TransactionManager tm;

    // If multipleEnlistDelists is set to true, with in the transaction, for the same
    //  - connection multiple enlistments and delistments might happen
    // - By setting the System property ALLOW_MULTIPLE_ENLISTS_DELISTS to true
    // - multipleEnlistDelists can be enabled
    private boolean multipleEnlistDelists = false;
	// Sting Manager for Localization
	private static StringManager sm = StringManager.getManager(J2EETransactionManagerImpl.class);
    protected InvocationManager invMgr;

    protected PoolManager poolmgr;

    //  START OF IASRI 4629815
    //
    //protected  ResourcePoolManager resourcePoolMgr;     //Added by Miriam - ECU
    //  START OF IASRI 4629815
    //

    protected int transactionTimeout;
    protected ThreadLocal<Integer> txnTmout = new ThreadLocal();
    protected boolean useLAO = true;

    // START IASRI 4662745
    // admin and monitoring related parameters
    protected Hashtable statusMap;
    protected Vector activeTransactions;
    protected boolean monitoringEnabled = false;

    static protected int JTAStatus[] =
    {
        javax.transaction.Status.STATUS_ACTIVE,
        javax.transaction.Status.STATUS_MARKED_ROLLBACK,
        javax.transaction.Status.STATUS_PREPARED,
        javax.transaction.Status.STATUS_COMMITTED,
        javax.transaction.Status.STATUS_ROLLEDBACK,
        javax.transaction.Status.STATUS_UNKNOWN,
        javax.transaction.Status.STATUS_NO_TRANSACTION,
        javax.transaction.Status.STATUS_PREPARING,
        javax.transaction.Status.STATUS_COMMITTING,
        javax.transaction.Status.STATUS_ROLLING_BACK
    };

    static protected String STATUS[] =
    {
        "Active",
        "MarkedRollback",
        "Prepared",
        "Committed",
        "RolledBack",
        "UnKnown",
        "NoTransaction",
        "Preparing",
        "Committing",
        "RollingBack"
    };
    protected int m_transCommitted = 0;
    protected int m_transRolledback = 0;
    protected int m_transInFlight = 0;
    // END IASRI 4662745

    /**
     * instance -> resource list mapping
     * // START IASRI 4705808 TTT002 -- use List instead of Vector
     * For EJB, a mapping of EnterpriseBean -> ArrayList
     * For Servlets/JSPs, a mapping of <Context+Thread> -> ArrayList
     * // END IASRI 4705808 TTT002
     */
    // START IASRI 4705808 TTT001 -- use BaseCache instead of Hashtable
    private Cache resourceTable;
    // END IASRI 4705808 TTT001



    public J2EETransactionManagerImpl() {
		if (_logger.isLoggable(Level.FINE))
        	_logger.log(Level.FINE,"TM: Initializing distributed TM...");
        //tm = TransactionManagerImpl.getTransactionManagerImpl();
        if( Switch.getSwitch().getPoolManager() == null ) {
	    poolmgr = new PoolManagerImpl();
	    Switch.getSwitch().setPoolManager( poolmgr );
	} else {
            poolmgr = Switch.getSwitch().getPoolManager();
	}
        Switch.getSwitch().setPoolManager(poolmgr);

        ResourceInstaller installer = new ResourceInstaller();
        Switch.getSwitch().setResourceInstaller(installer);

	// START IASRI 4705808 TTT001 -- use BaseCache instead of Hashtable
        //int maxEntries = 16000; // FIXME: this maxEntry should be a config
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

        invMgr = Switch.getSwitch().getInvocationManager();
	if (invMgr == null) { // Stand Alone Clients
		invMgr = new InvocationManagerImpl();
		// Switch.getSwitch().setInvocationManager(invMgr);
		// createJTSTransactionManager();
	}
        // START IASRI 4662745

        ServerContext sCtx = ApplicationServer.getServerContext();
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
                            useLAO = false;
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
        // ENF OF BUG 4665539
		if (_logger.isLoggable(Level.FINE))
        	_logger.log(Level.FINE,"TM: Tx Timeout = " + transactionTimeout);

        // monitor and administration
	activeTransactions = new Vector();
        statusMap = new Hashtable();
        for (int i=0; i<JTAStatus.length; i++) {
            statusMap.put(new Integer(JTAStatus[i]),STATUS[i]);
        }
        // END IASRI 4662745

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
	// END IASRI 4705808 TTT004
    }

    public static J2EETransactionManager createTransactionManager() {
        ServerConfiguration sc = ServerConfiguration.getConfiguration();
	String txOpt = sc.getProperty(TX_OPT);

		if (_logger.isLoggable(Level.FINE))
	    	_logger.log(Level.FINE,"TM: Tx Opt = " + txOpt);

	J2EETransactionManager j2eeTM;
	if ( txOpt != null && txOpt.equals("false") )
	    j2eeTM = new J2EETransactionManagerImpl();
	else {
	    j2eeTM = new J2EETransactionManagerOpt();
            J2EETransaction.j2eeTM = (J2EETransactionManagerOpt)j2eeTM;
        }

	return j2eeTM;
    }

    public static void createJTSTransactionManager() {
        // It is assumed that transaction manager is already created
        // and is available in switch.
        J2EETransactionManagerImpl impl = (J2EETransactionManagerImpl) 
        Switch.getSwitch().getTransactionManager();
	if (impl.tm != null)
		return;

        impl.tm = TransactionManagerImpl.getTransactionManagerImpl();
       
    }

    private static void print(String s) {
        /** IASRI 4664284
        System.err.println(s);
        **/
        // START OF IASRI 4664284
        _logger.log(Level.FINE,s);
        // END OF IASRI 4664284

    }


/****************************************************************************/
/** Implementations of J2EETransactionManager APIs **************************/
/****************************************************************************/

    /**
     * Return true if a "null transaction context" was received
     * from the client. See EJB2.0 spec section 19.6.2.1.
     * A null tx context has no Coordinator objref. It indicates
     * that the client had an active
     * tx but the client container did not support tx interop.
     */
    public boolean isNullTransaction()
    {
	try {
	    return com.sun.jts.pi.InterceptorImpl.isTxCtxtNull();
	} catch ( Exception ex ) {
	    // sometimes JTS throws an EmptyStackException if isTxCtxtNull
	    // is called outside of any CORBA invocation.
	    return false;
	}
    }

    public void recover(XAResource[] resourceList) {
        int size = resourceList.length;
        Vector v = new Vector();
        for (int i=0; i<size; i++) {
            v.addElement(resourceList[i]);
        }
        ((TransactionManagerImpl)tm).recover(v.elements());
    }

    public boolean enlistResource(Transaction tran, ResourceHandle h)
        throws RollbackException,
               IllegalStateException, SystemException {
		if (_logger.isLoggable(Level.FINE))
        	_logger.log(Level.FINE,"TM: enlistResource");
        if (h.isTransactional() && (!h.isEnlisted() || !h.isShareable() || multipleEnlistDelists)) {
            XAResource res = h.getXAResource();
            boolean result = tran.enlistResource(res);
            if (!h.isEnlisted())
                poolmgr.resourceEnlisted(tran, h);
            return result;
        } else {
            return true;
        }
    }

    public boolean enlistLAOResource(Transaction tran, ResourceHandle h)
        throws RollbackException,
               IllegalStateException, SystemException {
		if (_logger.isLoggable(Level.FINE))
        	_logger.log(Level.FINE,"TM: enlistLAOResource");
        if (h.isTransactional()) {
            XAResource res = h.getXAResource();
            boolean result = tran.enlistResource(res);
            return result;
        } else {
            return true;
        }
    }

    public void enlistComponentResources() throws RemoteException {
		if (_logger.isLoggable(Level.FINE))
        	_logger.log(Level.FINE,"TM: enlistComponentResources");
        ComponentInvocation inv = invMgr.getCurrentInvocation();
        if (inv == null)
            return;
        try {
            Transaction tran = getTransaction();
            inv.setTransaction(tran);
            enlistComponentResources(inv);
        } catch (InvocationException ex) {
            /** IASRI 4664284
            ex.printStackTrace();
            ex.printStackTrace(Log.err);
            **/
            //START OF IASRI 4664284
            _logger.log(Level.SEVERE,"enterprise_distributedtx.excep_in_enlist" ,ex);
            //END OF IASRI 4664284
            
            throw new RemoteException(ex.getMessage(), ex.getNestedException());
        } catch (Exception ex) {
            /** IASRI 4664284
            ex.printStackTrace();
            ex.printStackTrace(Log.err);
            **/
            //START OF IASRI 4664284
            _logger.log(Level.SEVERE,"enterprise_distributedtx.excep_in_enlist" ,ex);
            //END OF IASRI 4664284
            throw new RemoteException(ex.getMessage(), ex);
        }
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
            /** IASRI 4664284
            ex.printStackTrace();
            ex.printStackTrace(Log.err);
            **/
            // START OF IASRI 4664284
            _logger.log(Level.SEVERE,"enterprise_distributedtx.excep_in_delist",ex);
            // END OF IASRI 4664284

            throw new RemoteException("", ex.getNestedException());
        } catch (Exception ex) {
            /** IASRI 4664284
            ex.printStackTrace();
            ex.printStackTrace(Log.err);
            **/
            // START OF IASRI 4664284
            _logger.log(Level.SEVERE,"enterprise_distributedtx.excep_in_delist",ex);
            // END OF IASRI 4664284
            throw new RemoteException("", ex);
        }
    }

    public void registerComponentResource(ResourceHandle h) {
        ComponentInvocation inv = invMgr.getCurrentInvocation();
        if (inv != null) {
            Object instance = inv.getInstance();
            if (instance == null) return;
            h.setComponentInstance(instance);
            List l = getResourceList(instance, inv);
            l.add(h);
        }
    }

    public void unregisterComponentResource(ResourceHandle h) {
        Object instance = h.getComponentInstance();
        if (instance == null) return;
        h.setComponentInstance(null);
        ComponentInvocation inv = invMgr.getCurrentInvocation();
        List l = null;
        if (inv != null)
            l = getExistingResourceList(instance, inv);
        else
            l = getExistingResourceList(instance);
	if (l != null) {
            l.remove(h);
	}
    }

    private void handleResourceError(ResourceHandle h,
                                     Exception ex, Transaction tran,
                                     ComponentInvocation inv) {

		if (_logger.isLoggable(Level.FINE)) {
            if (h.isTransactional()) {
                _logger.log(Level.FINE,"TM: HandleResourceError " +
                                   h.getXAResource() +
                                   "," + ex);
            }
        }
		/** IASRI 4658504
        // unregister resource
        Object instance = inv.getInstance();
        if (instance == null) return;
        Vector v = getResourceList(instance);
        v.removeElement(h);

		** IASRI 4658504 **/
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
                h.getResourceAllocator().closeUserConnection(h);
            } catch (Exception ex2) {
                //Log.err.println(ex2);
            }
        } else {
            // destroy resource. RM Error.
            try {
                h.getResourceAllocator().destroyResource(h);
            } catch (Exception ex2) {
                //Log.err.println(ex2);
            }
        }
    }

    private void enlistComponentResources(ComponentInvocation inv)
    throws InvocationException {

        // Exception ex1 = null;
        try {
            Transaction tran = inv.getTransaction();
            if (isTransactionActive(tran)) {
		/** IASRI 4658504
                Vector v = getResourceList(inv.getInstance());
                Enumeration e = v.elements();
                while (e.hasMoreElements()) {
                    ResourceHandle h = (ResourceHandle) e.nextElement();
                    try {
                        enlistResource(tran, h);
                    } catch (Exception ex) {
                        handleResourceError(h, ex, tran, inv);
                    }
                }
		** IASRI 4658504    **/
		
                List l = getExistingResourceList(inv.getInstance(), inv);
                if (l == null || l.size() == 0) return;
                Iterator it = l.iterator();
		// END IASRI 4705808 TTT002
		while(it.hasNext()) {
		    ResourceHandle h = (ResourceHandle) it.next();
		    try{
			enlistResource(tran,h);
		    }catch(Exception ex){
                        // ex1 = ex;
			it.remove();
			handleResourceError(h,ex,tran,inv);
		    }
		}
		//END OF IASRI 4658504		
            }
        } catch (Exception ex) {
            /** IASRI 4664284
            ex.printStackTrace();
            ex.printStackTrace(Log.err);
            **/
            // START OF IASRI 4664284
            _logger.log(Level.SEVERE,"enterprise_distributedtx.excep_in_enlist",ex);
            // END OF IASRI 4664284
            //throw new InvocationException(ex);
        }
        /**
        if (ex1 != null) {
            InvocationException ivx = new InvocationException(ex1.getMessage());
            ivx.initCause(ex1);
            throw ivx;
        }
        **/
    }

    private void delistComponentResources(ComponentInvocation inv,
                                          boolean suspend)
        throws InvocationException {

        try {
            Transaction tran = inv.getTransaction();
            if (isTransactionActive(tran)) {
				/** IASRI 4658504
                Vector v = getResourceList(inv.getInstance());
                Enumeration e = v.elements();
                int flag = XAResource.TMSUCCESS;
                if (suspend) flag = XAResource.TMSUSPEND;
                while (e.hasMoreElements()) {
                    ResourceHandle h = (ResourceHandle) e.nextElement();
                    try {
                        delistResource(tran, h, flag);
                    } catch (IllegalStateException ex) {
                        // ignore error due to tx time out
                    } catch (Exception ex) {
                        handleResourceError(h, ex, tran, inv);
                    }
                }
                **IASRI 4658504 **/ 

                List l = getExistingResourceList(inv.getInstance(), inv);
                if (l == null || l.size() == 0)
                    return;
                Iterator it = l.iterator();
		// END IASRI 4705808 TTT002
		int flag = XAResource.TMSUCCESS;
		if(suspend)flag = XAResource.TMSUSPEND;
		while(it.hasNext()){
		    ResourceHandle h = (ResourceHandle)it.next();
		    try{
                        if ( h.isEnlisted() ) {
			    delistResource(tran,h,flag);
                        }
		    } catch (IllegalStateException ex) {
			// ignore error due to tx time out
		    }catch(Exception ex){
			it.remove();
			handleResourceError(h,ex,tran,inv);
		    }  
		}
		//END OF IASRI 4658504
            }
        } catch (Exception ex) {
            /** IASRI 4664284
            ex.printStackTrace();
            ex.printStackTrace(Log.err);
            **/
            // START OF IASRI 4664284
            _logger.log(Level.SEVERE,"enterprise_distributedtx.excep_in_delist",ex);
            // END OF IASRI 4664284
            //throw new InvocationException(ex);
        }
    }

    private boolean isTransactionActive(Transaction tran) {
        return (tran != null);
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
		if (_logger.isLoggable(Level.FINE))
        	_logger.log(Level.FINE,"TM: componentDestroyed" + instance);
	// START IASRI 4705808 TTT002 -- use List instead of Vector
        // Mod: remove the bad behavior of adding an empty list then remove it
        List l = (List)resourceTable.get(getInstanceKey(instance));
        if (l != null && l.size() > 0) {
			//START IASRI 4720840
            resourceTable.remove(getInstanceKey(instance));
			//END IASRI 4720840
            Iterator it = l.iterator();
            while (it.hasNext()) {
                ResourceHandle h = (ResourceHandle) it.next();
                try {
                    h.getResourceAllocator().closeUserConnection(h);
                } catch (PoolingException ex) {
                    /** IASRI 4664284
                    if (debug) ex.printStackTrace();
                    **/
                    // START OF IASRI 4664284
					if (_logger.isLoggable(Level.FINE))
                    	_logger.log(Level.WARNING,"enterprise_distributedtx.pooling_excep", ex);
                    // END OF IASRI 4664284
                }
            }
            l.clear();
			//START IASRI 4720840
            // resourceTable.remove(getInstanceKey(instance));
			//END IASRI 4720840
        }
	// END IASRI 4705808 TTT002
    }

    public void ejbDestroyed(ComponentContext context) {
	if (_logger.isLoggable(Level.FINE))
            _logger.log(Level.FINE, " ejbDestroyed: " + context);
        List l = (List)context.getResourceList();
        if (l != null && l.size() > 0) {
            Iterator it = l.iterator();
            while (it.hasNext()) {
                ResourceHandle h = (ResourceHandle) it.next();
                try {
                    h.getResourceAllocator().closeUserConnection(h);
                } catch (PoolingException ex) {
		    if (_logger.isLoggable(Level.FINE))
                        _logger.log(Level.WARNING,"enterprise_distributedtx.pooling_excep", ex);
                }
            }
            l.clear();
        }
    }



    private Object getInstanceKey(Object instance) {
        Object key = null;
        if (instance instanceof Servlet ||
            instance instanceof Filter) {
            // Servlet or Filter
            if (instance instanceof SingleThreadModel) {
                key = instance;
            } else {
                Vector pair = new Vector(2);
                pair.addElement(instance);
                pair.addElement(Thread.currentThread());
                key = pair;
            }
        } else {
            key = instance;
        }
        return key;
    }

	//START IASRI 4720840
    public List getExistingResourceList(Object instance) {
        if (instance == null)
            return null;
        Object key = getInstanceKey(instance);
        if (key == null)
            return null;
        return (List) resourceTable.get(key);
    }
	//END IASRI 4720840

    public List getExistingResourceList(Object instance, ComponentInvocation inv) {
       if (inv == null)
           return null;
        List l = null;
        if (inv.getInvocationType() == ComponentInvocation.EJB_INVOCATION) {
            ComponentContext ctx = inv.context;
            if (ctx != null)
                l = ctx.getResourceList();
            return l;
        }
        else {
            Object key = getInstanceKey(instance);
            if (key == null)
                return null;
            return (List) resourceTable.get(key);
       }
    }

    // START IASRI 4705808 TTT001 -- use BaseCache instead of Hashtable
    public List getResourceList(Object instance) {
        Object key = getInstanceKey(instance);
        List l = (List) resourceTable.get(key);
        if (l == null) {
            l = new ArrayList(); //FIXME: use an optimum size?
            resourceTable.put(key, l);
        }
        return l;
    }
    // END IASRI 4705808 TTT002

    public List getResourceList(Object instance, ComponentInvocation inv) {
        if (inv == null)
            return new ArrayList(0);
        List l = null;
        if (inv.getInvocationType() == ComponentInvocation.EJB_INVOCATION) {
            ComponentContext ctx = inv.context;
            if (ctx != null)
                l = ctx.getResourceList();
            else {
	        l = new ArrayList(0);
            }
        }
        else {
            Object key = getInstanceKey(instance);
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



    /**
     * @param suspend true if the transaction association should
     * be suspended rather than ended.
     */
    public boolean delistResource(Transaction tran, ResourceHandle h,
                                  int flag)
        throws IllegalStateException, SystemException {
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

    public void registerSynchronization(Synchronization sync)
        throws IllegalStateException, SystemException
    {
		if (_logger.isLoggable(Level.FINE))
        	_logger.log(Level.FINE,"TM: registerSynchronization");
        try {
            Transaction tran = getTransaction();
            if (tran != null) {
                tran.registerSynchronization(sync);
            }
        } catch (RollbackException ex) {
            /** IASRI 4664284
            ex.printStackTrace();
            ex.printStackTrace(Log.err);
            **/
            //START OF IASRI 4664284
            _logger.log(Level.SEVERE,"enterprise_distributedtx.rollbackexcep_in_regsynch",ex);
            //END OF IASRI 4664284

            throw new IllegalStateException();
        }
    }

    public void begin(int timeout)
        throws NotSupportedException, SystemException {

		//START RI PERFIMPROVEMENT
		/***
		// Just to tell that, use this timeout for the transaction, there is no need
		// to synchronized and thus serrialize the activity. Now this is avoided by
		// the introduction of two new methods begin(timeout) in TransactionManagerImpl
		// and CurrentImpl

        // ensure no other thread change the timeout
		synchronized(tm) {
            tm.setTransactionTimeout(timeout);
            tm.begin();
            tm.setTransactionTimeout(0);
            // START IASRI 4662745
            if (monitoringEnabled) {
				Transaction tran = tm.getTransaction();
                activeTransactions.addElement(tran);
                m_transInFlight++;
            }
            // END IASRI 4662745
	 	}
		**/

        ((TransactionManagerImpl)tm).begin(timeout);
        // START IASRI 4662745
        if (monitoringEnabled) {
			Transaction tran = tm.getTransaction();
            activeTransactions.addElement(tran);
            m_transInFlight++;
        }
        // END IASRI 4662745
		//END RI PERFIMPROVEMENT
    }


    public void checkTransactionExport(boolean isLocal) { }

    public void checkTransactionImport() { }

    private void validateTransactionManager() throws IllegalStateException {
        if (tm == null) {
            throw new IllegalStateException
            (sm.getString("enterprise_distributedtx.transaction_notactive"));
        }
    }



/****************************************************************************/
/** Implementations of JTA TransactionManager APIs **************************/
/****************************************************************************/


    /**
     * Create a new transaction and associate it with the current thread.
     *
     * @exception NotSupportedException Thrown if the thread is already
     *    associated with a transaction.
     *
     * @exception SystemException Thrown if the transaction manager
     *    encounters an unexpected error condition
     *
     */
    public void begin() throws NotSupportedException, SystemException {
		if (_logger.isLoggable(Level.FINE))
        	_logger.log(Level.FINE,"TM: begin");

		//START RI PERFIMPROVEMENT
		/***
		// Just to tell that, use this timeout for the transaction, there is no need
		// to synchronize and thus serrialize the activity. Now this is avoided by
		// the introduction of two new methods begin(timeout) in TransactionManagerImpl
		// and CurrentImpl

        synchronized(tm) {
            // ensure no other thread change the timeout
            tm.setTransactionTimeout(transactionTimeout);
            tm.begin();
            tm.setTransactionTimeout(0);

            // START IASRI 4662745
            if (monitoringEnabled) {
                Transaction tran = tm.getTransaction();
                activeTransactions.addElement(tran);
                m_transInFlight++;
            }
            // END IASRI 4662745
	 	}
		***/
        ((TransactionManagerImpl)tm).begin(getEffectiveTimeout());

        // START IASRI 4662745
        if (monitoringEnabled) {
        	Transaction tran = tm.getTransaction();
        	activeTransactions.addElement(tran);
        	m_transInFlight++;
       	}
       // END IASRI 4662745
       // END IASRI PERFIMPROVEMENT
    }

    /**
     * Complete the transaction associated with the current thread. When this
     * method completes, the thread becomes associated with no transaction.
     *
     * @exception RollbackException Thrown to indicate that
     *    the transaction has been rolled back rather than committed.
     *
     * @exception HeuristicMixedException Thrown to indicate that a heuristic
     *    decision was made and that some relevant updates have been committed
     *    while others have been rolled back.
     *
     * @exception HeuristicRollbackException Thrown to indicate that a
     *    heuristic decision was made and that some relevant updates have been
     *    rolled back.
     *
     * @exception SecurityException Thrown to indicate that the thread is
     *    not allowed to commit the transaction.
     *
     * @exception IllegalStateException Thrown if the current thread is
     *    not associated with a transaction.
     *
     * @exception SystemException Thrown if the transaction manager
     *    encounters an unexpected error condition
     *
     */
    public void commit() throws RollbackException,
    HeuristicMixedException, HeuristicRollbackException, SecurityException,
    IllegalStateException, SystemException {
		if (_logger.isLoggable(Level.FINE))
        	_logger.log(Level.FINE,"TM: commit");
        validateTransactionManager();
        Object obj = null;
        if(monitoringEnabled){
            obj = tm.getTransaction();
        }
        if (invMgr.isInvocationStackEmpty()) {
            try{
                tm.commit();
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
            }
        } else {
            ComponentInvocation curr = null;
            try {
                curr = invMgr.getCurrentInvocation();
                if (curr != null)
                    curr.setTransactionCompeting(true);
                tm.commit();
                if (monitoringEnabled){
                    monitorTxCompleted(obj, true);
                }
            } catch (InvocationException ex) {
                assert false;
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
            } finally {
                if (curr != null) {
                    curr.setTransactionCompeting(false);
                }
            }
        }
        
    }

    /**
     * Obtain the status of the transaction associated with the current thread.
     *
     * @return The transaction status. If no transaction is associated with
     *    the current thread, this method returns the Status.NoTransaction
     *    value.
     *
     * @exception SystemException Thrown if the transaction manager
     *    encounters an unexpected error condition
     *
     */
    public int getStatus() throws SystemException {
        if (tm != null) {
            return tm.getStatus();
        } else {
            return javax.transaction.Status.STATUS_NO_TRANSACTION;
        }
    }

    /**
     * Get the transaction object that represents the transaction
     * context of the calling thread
     *
     * @exception SystemException Thrown if the transaction manager
     *    encounters an unexpected error condition
     *
     */
    public Transaction getTransaction() throws SystemException {
        if (tm == null) {
            return null;
        } else {
            return tm.getTransaction();
        }
    }

    /**
     * Resume the transaction context association of the calling thread
     * with the transaction represented by the supplied Transaction object.
     * When this method returns, the calling thread is associated with the
     * transaction context specified.
     *
     * @exception InvalidTransactionException Thrown if the parameter
     *    transaction object contains an invalid transaction
     *
     * @exception IllegalStateException Thrown if the thread is already
     *    associated with another transaction.
     *
     * @exception SystemException Thrown if the transaction manager
     *    encounters an unexpected error condition
     */
    public void resume(Transaction tobj)
        throws InvalidTransactionException, IllegalStateException,
            SystemException {
		if (_logger.isLoggable(Level.FINE))
        	_logger.log(Level.FINE,"TM: resume");
        tm.resume(tobj);
    }


    /**
     * Roll back the transaction associated with the current thread. When this
     * method completes, the thread becomes associated with no transaction.
     *
     * @exception SecurityException Thrown to indicate that the thread is
     *    not allowed to roll back the transaction.
     *
     * @exception IllegalStateException Thrown if the current thread is
     *    not associated with a transaction.
     *
     * @exception SystemException Thrown if the transaction manager
     *    encounters an unexpected error condition
     *
     */
    public void rollback() throws IllegalStateException, SecurityException,
    SystemException {
		if (_logger.isLoggable(Level.FINE))
        	_logger.log(Level.FINE,"TM: rollback");
        validateTransactionManager();
        // START IASRI 4662745
        Object obj = null;
        if (monitoringEnabled){
            obj = tm.getTransaction();
        }
        if (invMgr.isInvocationStackEmpty()) {
            tm.rollback();
        } else {
            ComponentInvocation curr = null;
            try {
                curr = invMgr.getCurrentInvocation();
                if (curr != null)
                    curr.setTransactionCompeting(true);
                tm.rollback();
            } catch (InvocationException ex) {
                assert false;
            } finally {
                if (curr != null) {
                    curr.setTransactionCompeting(false);
                }
            }
        }
        
        if (monitoringEnabled){
            monitorTxCompleted(obj, false);
        }
        // END IASRI 4662745
        
    }
    
    /**
     * Modify the transaction associated with the current thread such that
     * the only possible outcome of the transaction is to roll back the
     * transaction.
     *
     * @exception IllegalStateException Thrown if the current thread is
     *    not associated with a transaction.
     *
     * @exception SystemException Thrown if the transaction manager
     *    encounters an unexpected error condition
     *
     */
    public void setRollbackOnly()
        throws IllegalStateException, SystemException {
		if (_logger.isLoggable(Level.FINE))
        	_logger.log(Level.FINE,"TM: setRollbackOnly");
        validateTransactionManager();
        tm.setRollbackOnly();
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

    /**
     * Suspend the transaction currently associated with the calling
     * thread and return a Transaction object that represents the
     * transaction context being suspended. If the calling thread is
     * not associated with a transaction, the method returns a null
     * object reference. When this method returns, the calling thread
     * is associated with no transaction.
     *
     * @exception SystemException Thrown if the transaction manager
     *    encounters an unexpected error condition
     *
     * @exception SystemException Thrown if the transaction manager
     *    encounters an unexpected error condition
     *
     */
    public Transaction suspend() throws SystemException {
		if (_logger.isLoggable(Level.FINE))
        	_logger.log(Level.FINE,"TM: suspend");
        validateTransactionManager();
        return tm.suspend();
    }

    // START IASRI 4662745
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
                if(tran instanceof com.sun.jts.jta.TransactionImpl){
                    id=((com.sun.jts.jta.TransactionImpl)tran).getTransactionId();
                    startTime = ((com.sun.jts.jta.TransactionImpl)tran).getStartTime();

                }else if(tran instanceof J2EETransaction){
                    J2EETransaction tran1 = (J2EETransaction)tran;
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
    /*
    *  Called by Admin Framework when transaction monitoring is enabled
    */
    public void forceRollback(Transaction tran) throws IllegalStateException, SystemException{
        if (tran != null){
            tran.setRollbackOnly();
        }
    }
    /* Returned Number of transactions commited since the time monitoring
     * was enabeld
     * Called by Admin Framework when transaction monitoring is enabled
     */

    public int getNumberOfTransactionsCommitted(){
        return m_transCommitted;
    }

    /* Returned Number of transactions Rolledback since the time monitoring
     * was enabeld
     * Called by Admin Framework when transaction monitoring is enabled
     */
    public int getNumberOfTransactionsRolledBack(){
        return m_transRolledback;
    }

    /* Returned Number of transactions Active
     *  Called by Admin Framework when transaction monitoring is enabled
     */
    public int getNumberOfActiveTransactions(){
        return m_transInFlight;
    }
    /*
     * Called by Admin Framework to freeze the transactions.
     */
    public synchronized void freeze(){
        if(com.sun.jts.CosTransactions.AdminUtil.isFrozenAll()){
            //multiple freezes will hang this thread, therefore just return
            return;
        }
        com.sun.jts.CosTransactions.AdminUtil.freezeAll();
    }
    /*
     * Called by Admin Framework to freeze the transactions. These undoes the work done by the freeze.
     */
    public synchronized void unfreeze(){
        if(com.sun.jts.CosTransactions.AdminUtil.isFrozenAll()){
            com.sun.jts.CosTransactions.AdminUtil.unfreezeAll();
        }
    }

    /*
     * Called by Admin Framework.
     */
    public synchronized boolean isFrozen(){
        return com.sun.jts.CosTransactions.AdminUtil.isFrozenAll();
    }


    /**
     * Recreate a transaction based on the Xid. This call causes the calling
     * thread to be associated with the specified transaction. <p>
     * This is used by importing transactions via the Connector contract.
     *
     * @param xid the Xid object representing a transaction.
     */
    public void recreate(Xid xid, long timeout) throws WorkException {
        ((TransactionManagerImpl) tm).recreate(xid, timeout);
    }

    /**
     * Release a transaction. This call causes the calling thread to be
     * dissociated from the specified transaction. <p>
     * This is used by importing transactions via the Connector contract.
     *
     * @param xid the Xid object representing a transaction.
     */
    public void release(Xid xid) throws WorkException {
        ((TransactionManagerImpl) tm).release(xid);
    }

    /**
     * Provides a handle to a <code>XATerminator</code> instance. The
     * <code>XATerminator</code> instance could be used by a resource adapter
     * to flow-in transaction completion and crash recovery calls from an EIS.
     * <p>
     * This is used by importing transactions via the Connector contract.
     *
     * @return a <code>XATerminator</code> instance.
     */
    public XATerminator getXATerminator() {
        return ((TransactionManagerImpl) tm).getXATerminator();
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
    public void setMonitoringEnabled(boolean enabled){
        monitoringEnabled = enabled;
	//reset the variables
    	m_transCommitted = 0;
    	m_transRolledback = 0;
    	m_transInFlight = 0;
        activeTransactions.removeAllElements();
    }
    static {
		com.sun.enterprise.admin.event.AdminEventListenerRegistry.addEventListener(JTSEvent.eventType, new JTSConfigChangeEventListener()); 
    }
    // END IASRI 4662745

    // overridden in the Opt
    public boolean isTimedOut() {
        return false;
    }

    // START IASRI 4705808 TTT004 -- monitor resource table stats
    // Mods: Adding method for statistic dumps using TimerTask
    private void registerStatisticMonitorTask() {
        TimerTask task = new StatisticMonitorTask();
        Timer timer = Switch.getSwitch().getTimer();
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

        timer.scheduleAtFixedRate(task, 0, statInterval);
    }

    // Mods: Adding TimerTask class for statistic dumps
    class StatisticMonitorTask extends TimerTask {
        public void run() {
            if (resourceTable != null) {
                Map stats = resourceTable.getStats();
                Iterator it = stats.keySet().iterator();
                String key;
		//FIXME: use logger instead of System.out
                System.out.println("********** J2EETransactionManagerImpl.resourceTable stats *****"); 
                while (it.hasNext()) {
                    key = (String)it.next();
                    System.out.println(key + ": " + stats.get(key).toString());
                }
            }
        }
    }
    // END IASRI 4705808 TTT004
}
