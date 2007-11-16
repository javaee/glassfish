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
package org.glassfish.api.admin;

import org.jvnet.hk2.component.PostConstruct;
import java.beans.*;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.sun.enterprise.config.serverbeans.ConstrainedBean;

/**
 * Parent class for our config-api beans. Config-api beans do not
 *
 * @author Jerome Dochez
 */
public class ConfigBean implements Serializable, Cloneable, ConstrainedBean, PostConstruct, Transactor {


    transient Transaction currentTx=null;
    transient BeanTransactionListener listener=null;

    protected VetoableChangeSupport support=new TypedVetoableChangeSupport(this);

    /**
     * HK2 component lifecyle
     * Adds the necessary VetoableChangeListener so changes cannot be made on this
     * instance without first associating it with a transaction.
     */
    public void postConstruct() {

        // add a safeguard to check that any property change is done under a transaction scheme
        support.addVetoableChangeListener(new VetoableChangeListener() {

            public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
                if (currentTx==null) {
                    throw new PropertyVetoException("Change not part of a transaction", evt);
                }
                // check that if we are setting a new value object, that this value is also part of
                // the same transaction
                try {
                    ConfigBean isIt = ConfigBean.class.cast(evt.getNewValue());
                    if (!isIt.canCommit(currentTx)) {
                        throw new PropertyVetoException("New value of type " + evt.getNewValue().getClass() + " is not part " +
                                "of this ConfigBean transaction, use allocate(Class<T>) instead of new() to allocate new config objects", evt);
                    }
                } catch(ClassCastException e) {
                    // ignore
                }
            }
        });
    }

    /**
     * Returns the vetoable change support
     * @return vetoable change support
     */
    public VetoableChangeSupport getVetoableChangeSupport() {
        return support;
    }

    public void addVetoableChangeListener(VetoableChangeListener listener) {
        support.addVetoableChangeListener(listener);
    }

    public void addVetoableChangeListener(String propertyName, VetoableChangeListener listener) {
        support.addVetoableChangeListener(propertyName, listener);
    }

    public void removeVetoableChangeListener(String propertyName, VetoableChangeListener listener) {
        support.removeVetoableChangeListener(propertyName, listener);
    }

    public void removeVetoableChangeListener(VetoableChangeListener listener) {
        support.removeVetoableChangeListener(listener);
    }

	/**
	 * Enter a new Transaction, this method should return false if this object
	 * is already enlisted in another transaction, or cannot be enlisted with
	 * the passed transaction. If the object returns true, the object
	 * is enlisted in the passed transaction and cannot be enlisted in another
	 * transaction until either commit or abort has been issued.
	 *
	 * @param t the transaction to enlist with
	 * @return true if the enlisting with the passed transaction was accepted,
	 * false otherwise
	 */    
    public synchronized boolean join(Transaction t) {
        
        if (currentTx==null) {
            // set up our transaction and add ourself to the transaction
            currentTx = t;
            t.addParticipant(this);
            // start recording our transaction change events
            listener = new BeanTransactionListener();
            support.addVetoableChangeListener(listener);
            return true;
        }
        return false;
    }

	/**
	 * Returns true of this Transaction can be committed on this object
	 *
	 * @param t is the transaction to commit, should be the same as the
	 * one passed during the join(Transaction t) call.
	 *
	 * @return true if the trsaction commiting would be successful
	 */    
    public synchronized boolean canCommit(Transaction t) {
        // so far, it's pretty simple
        return currentTx==t;
    }

	/**
	 * Commit this Transaction.
	 *
	 * @param t the transaction commiting.
	 * @throws TransactionFailure if the transaction commit failed
	 */    
    public synchronized void commit(Transaction t) throws TransactionFailure {
        if (currentTx==t) {
            currentTx=null;
            support.removeVetoableChangeListener(listener);
            listener = null;
        } 
    }

    public List<PropertyChangeEvent> getTransactionEvents() {
        return listener.getEvents();
    }

	/**
	 * Aborts this Transaction, reverting the state

	 * @param t the aborting transaction
	 */    
    public synchronized void abort(Transaction t) {

        support.removeVetoableChangeListener(listener);
        
        // housekeeping first
        currentTx=null;
        listener=null;

    }

    /**
     * Allocate a new ConfigBean object as part of the Transaction
     * associated with this configuration object. This will eventually
     * be moved to a factory.
     *
     * @param type the request configuration object type
     * @return the propertly constructed configuration object
     */

    public <T extends ConfigBean> T allocate(Class<T> type) {
        if (currentTx==null) {
            throw new RuntimeException("Not part of a transaction");
        }
        T instance;
        try {
            instance = type.newInstance();
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
        instance.join(currentTx);
        instance.postConstruct();
        return instance;
   }

    /**
     * Implementation of clone to support working on copies.
     * This will eventually be removed and moved to the DOLElement
     *
     * @return a shallow copy of itself
     * @throws CloneNotSupportedException
     */
   public Object clone() throws CloneNotSupportedException {
        try {
            ConfigBean o = (ConfigBean) super.clone();
            o.support=new TypedVetoableChangeSupport(o);
            o.postConstruct();
            return o;
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }

    /**
     * Utility class to register change events during a transaction
     */
    private class BeanTransactionListener implements VetoableChangeListener {

        final LinkedList<PropertyChangeEvent> events = new LinkedList<PropertyChangeEvent>();
        
        /**
         * This method gets called when a constrained property is changed.
         *
         * @param evt a <code>PropertyChangeEvent</code> object describing the
         *            event source and the property that has changed.
         * @throws java.beans.PropertyVetoException
         *          if the recipient wishes the property
         *          change to be rolled back.
         */
        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            events.addLast(evt);
        }

        private List<PropertyChangeEvent> getEvents() {
            return events;
        }
    }

}
