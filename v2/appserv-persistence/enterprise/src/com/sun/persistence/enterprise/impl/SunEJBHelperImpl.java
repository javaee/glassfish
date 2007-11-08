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
 * SunEJBHelperImpl.java
 *
 * Created on June 07, 2005.
 */


package com.sun.persistence.enterprise.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.transaction.*;
import javax.naming.InitialContext;

import javax.ejb.EJBObject;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBContext;
import javax.ejb.EntityContext;

import com.sun.jts.jta.*;

import com.sun.appserv.jdbc.DataSource;

import com.sun.persistence.support.JDOFatalInternalException;
import com.sun.persistence.support.PersistenceManagerFactory;
import com.sun.persistence.runtime.transaction.impl.AbstractEJBHelperImpl;

import com.sun.org.apache.jdo.ejb.EJBImplHelper;
import com.sun.org.apache.jdo.util.I18NHelper;
import com.sun.org.apache.jdo.util.ApplicationLifeCycleEventListener;

import com.sun.enterprise.server.event.ApplicationEvent;
import com.sun.enterprise.server.event.ApplicationLoaderEventListener;
import com.sun.enterprise.server.event.ApplicationLoaderEventNotifier;
import com.sun.enterprise.server.event.EjbContainerEvent;

/**
 * Sun specific implementation for EJBHelper interface.
 */
public class SunEJBHelperImpl extends AbstractEJBHelperImpl
        implements ApplicationLoaderEventListener {

    /**
     * I18N message handler
     */
    private final static I18NHelper msg = I18NHelper.getInstance(
            "com.sun.persistence.enterprise.impl.Bundle"); // NOI18N

    static private List pmf_list;

    /**
     * Array of registered ApplicationLifeCycleEventListener
     */
    private List applicationLifeCycleEventListeners = new ArrayList();

    /** Garantees singleton.
     * Registers itself during initial load
     */
    static {
        SunEJBHelperImpl helper = new SunEJBHelperImpl();
        EJBImplHelper.registerEJBHelper(helper);
        // Register with ApplicationLoaderEventNotifier to receive Sun
        // Application Server specific lifecycle events.
        ApplicationLoaderEventNotifier.getInstance().addListener(helper);
        pmf_list = new ArrayList();
    }

    /**
     * Default constructor should not be public
     */
    SunEJBHelperImpl() {
    }

    // helper class for looking up the TransactionManager instances.
    static private class TransactionManagerFinder {

        // JNDI name of the TransactionManager used for transaction synchronization.
        static private final String PM_TM_NAME = "java:pm/TransactionManager"; //NOI18N

        // JNDI name of the TransactionManager used for managing local transactions.
        static private final String AS_TM_NAME = "java:appserver/TransactionManager"; //NOI18N

        // TransactionManager instance used for transaction synchronization.
        static TransactionManager tm = null;

        // TransactionManager instance used for managing local transactions.
        static TransactionManager appserverTM = null;

        static {
            try {
                tm =
                        (TransactionManager) (new InitialContext()).lookup(
                                PM_TM_NAME);
                appserverTM =
                        (TransactionManager) (new InitialContext()).lookup(
                                AS_TM_NAME);
            } catch (Exception e) {
                throw new JDOFatalInternalException(e.getMessage());
            }
        }
    }

    /**
     * SunEJBHelperImpl specific code
     */
    public Transaction getTransaction() {
        try {
            return TransactionManagerFinder.tm.getTransaction();
        } catch (Exception e) {
            throw new JDOFatalInternalException(e.getMessage());
        } catch (ExceptionInInitializerError err) {
            throw new JDOFatalInternalException(err.getMessage());
        }
    }

    /**
     * SunEJBHelperImpl specific code
     */
    public UserTransaction getUserTransaction() {
        try {
            InitialContext ctx = (InitialContext) Class.forName(
                    "javax.naming.InitialContext")
                    .newInstance(); //NOI18N

            return (UserTransaction) ctx.lookup("java:comp/UserTransaction"); //NOI18N
        } catch (Exception e) {
            throw new JDOFatalInternalException(e.getMessage());
        }
    }

    /**
     * SunEJBHelperImpl specific code
     */
    public synchronized PersistenceManagerFactory replaceInternalPersistenceManagerFactory(
            PersistenceManagerFactory pmf) {

        int i = pmf_list.indexOf(pmf);
        if (i == -1) {
            // New PersistenceManagerFactory. Remember it.
            pmf_list.add(pmf);
            return pmf;
        }

        return (PersistenceManagerFactory) pmf_list.get(i);
    }

    /**
     * Called in a managed environment to get a Connection from the application
     * server specific resource. In a non-managed environment returns null as it
     * should not be called. SunEJBHelperImpl specific code uses
     * com.sun.appserv.jdbc.DataSource to get a Connection.
     * @param resource the application server specific resource.
     * @param username the resource username. If null, Connection is requested
     * without username and password validation.
     * @param password the password for the resource username.
     * @return a Connection as an Object.
     * @throws JDOFatalInternalException if resource is not of the expected
     * type.
     * @throws JDODataStoreException.
     */
    public Object getNonTransactionalConnection(Object resource,
            String username, String password) {

        Object rc = null;
        // resource is expected to be com.sun.appserv.jdbc.DataSource
        if (resource instanceof DataSource) {
            try {
                DataSource ds = (DataSource) resource;
                if (username == null) {
                    rc = ds.getNonTxConnection();
                } else {
                    rc = ds.getNonTxConnection(username, password);
                }
            } catch (java.sql.SQLException e) {
                handleSQLException(e);
            }
        } else {
            throw new JDOFatalInternalException(
                    msg.msg(
                            "sunejbhelperimpl.wrongdatasourcetype", //NOI18N
                            resource.getClass().getName()));
        }
        return rc;
    }

    /**
     * SunEJBHelperImpl specific code
     */
    public TransactionManager getLocalTransactionManager() {
        try {
            return TransactionManagerFinder.appserverTM;
        } catch (ExceptionInInitializerError err) {
            throw new JDOFatalInternalException(err.getMessage());
        }
    }

    /**
     * SunEJBHelperImpl specific code
     */
    public void registerApplicationLifeCycleEventListener(
            ApplicationLifeCycleEventListener listener) {
        synchronized (applicationLifeCycleEventListeners) {
            applicationLifeCycleEventListeners.add(listener);
        }
    }
    
    //-------------------ApplicationLifeCycleEventListener Methods --------------//

    /**
     * SunEJBHelperImpl specific code
     */
    public void handleApplicationEvent(ApplicationEvent event) {
        // Change to switch-case if handling more than one events.
        if (ApplicationEvent.AFTER_APPLICATION_UNLOAD == event.getEventType()) {
            ClassLoader classLoader = event.getClassLoader();
            for (Iterator iterator = applicationLifeCycleEventListeners.iterator();
                 iterator.hasNext();) {
                ApplicationLifeCycleEventListener applicationLifeCycleEventListener = (ApplicationLifeCycleEventListener) iterator.next();
                applicationLifeCycleEventListener.notifyApplicationUnloaded(
                        classLoader);
            }
        }
    }

    /**
     * SunEJBHelperImpl specific code
     */
    public void handleEjbContainerEvent(EjbContainerEvent event) {
        //Ignore EjbContainerEvents
    }

}
