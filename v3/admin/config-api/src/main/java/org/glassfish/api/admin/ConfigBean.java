package org.glassfish.api.admin;

import org.jvnet.hk2.component.PostConstruct;
import org.glassfish.api.admin.Transaction;
import org.glassfish.api.admin.Transactor;
import org.glassfish.api.admin.TypedVetoableChangeSupport;

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

    public synchronized boolean canCommit(Transaction t) {
        // so far, it's pretty simple
        return currentTx==t;
    }

    public synchronized void commit(Transaction t) {
        if (currentTx==t) {
            currentTx=null;
            support.removeVetoableChangeListener(listener);
            listener = null;
        } 
    }

    public List<PropertyChangeEvent> getTransactionEvents() {
        return listener.getEvents();
    }

    public synchronized void abort(Transaction t) {

        support.removeVetoableChangeListener(listener);
        
        // housekeeping first
        currentTx=null;
        listener=null;

    }

    public <T extends ConfigBean> T allocate(Class<T> type) {
        if (currentTx==null) {
            throw new RuntimeException("Not part of a transaction");
        }
        T instance = null;
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

    public Object clone() {
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
