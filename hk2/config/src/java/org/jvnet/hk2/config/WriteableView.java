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
package org.jvnet.hk2.config;

import org.jvnet.tiger_types.Types;

import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.beans.PropertyChangeEvent;
import java.beans.VetoableChangeSupport;
import java.beans.PropertyVetoException;
import java.util.*;

/**
 * A WriteableView is a view of a ConfigBean object that allow access to the
 * setters of the ConfigBean.
 *
 * @author Jerome Dochez
 */
public class WriteableView implements InvocationHandler, Transactor, ConfigView {

    private final ConfigBean bean;
    private final ConfigBeanProxy readView;
    private final Map<String, PropertyChangeEvent> changedAttributes;
    private final Map<String, ProtectedList> changedCollections;
    Transaction currentTx;

    public WriteableView(ConfigBeanProxy readView) {
        this.readView = readView;
        this.bean = (ConfigBean) ((ConfigView) Proxy.getInvocationHandler(readView)).getMasterView();
        changedAttributes = new HashMap<String, PropertyChangeEvent>();
        changedCollections = new HashMap<String, ProtectedList>();
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        ConfigModel.Property property = bean.toProperty(method);
        if(property==null)
            throw new IllegalArgumentException("No corresponding property found for method: "+method);

        if(args==null || args.length==0) {
            // getter
            if (changedAttributes.containsKey(property.xmlName())) {
                // serve masked changes.
                return changedAttributes.get(property.xmlName()).getNewValue();
            } else {
                // pass through.
                Object value = bean.invoke(proxy, method, args);
                if (value instanceof List) {
                    if (!changedCollections.containsKey(property.xmlName())) {
                        // wrap collections so we can record events on that collection mutation.
                        try {
                            if(!(method.getGenericReturnType() instanceof ParameterizedType))
                                throw new IllegalArgumentException("List needs to be parameterized");
                            changedCollections.put(property.xmlName(), new ProtectedList((List) value, this, property.xmlName()));
                        } catch(Exception e) {
                            e.printStackTrace();
                            throw e;
                        }
                    }
                    return changedCollections.get(property.xmlName());
                }
                return value;
            }
        } else {
            // are we still in a transaction
            if (currentTx==null) {
                throw new IllegalStateException("Not part of a transation");
            }

            // setter
            Object oldValue = bean.getter(property, method.getGenericParameterTypes()[0]);
            Object newValue = args[0];
            if (args[0] instanceof ConfigBeanProxy) {
                ConfigView bean = (ConfigView) Proxy.getInvocationHandler((ConfigBeanProxy) args[0]);
                newValue = bean.getMasterView();
            }
            PropertyChangeEvent evt = new PropertyChangeEvent(readView,property.xmlName(), oldValue, newValue);
            changedAttributes.put(property.xmlName(), evt);
            return null;
        }
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
     *         false otherwise
     */
    public synchronized boolean join(Transaction t) {
        if (currentTx==null) {
            currentTx = t;
            t.addParticipant(this);
            return true;
        }
        return false;
    }

    /**
     * Returns true of this Transaction can be committed on this object
     *
     * @param t is the transaction to commit, should be the same as the
     *          one passed during the join(Transaction t) call.
     * @return true if the trsaction commiting would be successful
     */
    public synchronized boolean canCommit(Transaction t) {
        return currentTx==t;
    }

    /**
     * Commit this Transaction.
     *
     * @param t the transaction commiting.
     * @throws TransactionFailure
     *          if the transaction commit failed
     */
    public synchronized List<PropertyChangeEvent> commit(Transaction t) throws TransactionFailure {
        if (currentTx==t) {
            currentTx=null;
        }
        try {
            List<PropertyChangeEvent> appliedChanges = new ArrayList<PropertyChangeEvent>();
            for (PropertyChangeEvent event : changedAttributes.values()) {
                //TODO : dochez : remove the test below once attribute leaf can remove values
                if (event.getNewValue()==null) {
                    continue;
                }
                ConfigModel.Property property = bean.model.findIgnoreCase(event.getPropertyName());
                property.set(bean, event.getNewValue());
                appliedChanges.add(event);
            }
            for (ProtectedList entry :  changedCollections.values())  {
                List originalList = entry.readOnly;
                for (PropertyChangeEvent event : (List<PropertyChangeEvent>) entry.changeEvents) {
                    if (event.getOldValue()==null) {
                        originalList.add(event.getNewValue());
                    } else {
                        final Dom toBeRemoved = Dom.unwrap((ConfigBeanProxy) event.getOldValue());
                        for (int index=0;index<originalList.size();index++) {
                            Object element = originalList.get(index);
                            Dom dom = Dom.unwrap((ConfigBeanProxy) element);
                            if (dom==toBeRemoved) {
                                originalList.remove(index);
                            }
                        }
                    }
                }
            }
            changedAttributes.clear();
            return appliedChanges;
        } finally {
            bean.getLock().unlock();
        }

    }

    /**
     * Aborts this Transaction, reverting the state
     *
     * @param t the aborting transaction
     */
    public synchronized void abort(Transaction t) {
        currentTx=null;
        bean.getLock().unlock();
        changedAttributes.clear();

    }

    /**
     * Allocate a new ConfigBean object as part of the Transaction
     * associated with this configuration object. This will eventually
     * be moved to a factory.
     *
     * @param type the request configuration object type
     * @return the propertly constructed configuration object
     * @throws TransactionFailure if the allocation failed 
     */

    public <T extends ConfigBeanProxy> T allocateProxy(Class<T> type) throws TransactionFailure {
        if (currentTx==null) {
            throw new TransactionFailure("Not part of a transaction", null);
        }
        ConfigBean newBean = bean.allocate(type);
        WriteableView writeableView = ConfigSupport.getWriteableView(newBean.getProxy(type), newBean);
        writeableView.join(currentTx);

        return writeableView.getProxy(type);
   }

    public ConfigBean getMasterView() {
        return bean;
    }

    public void setMasterView(ConfigView view) {

    }

    public <T extends ConfigBeanProxy> Class<T> getProxyType() {
        return bean.getProxyType();
    }

    @SuppressWarnings("unchecked")    
    public <T extends ConfigBeanProxy> T getProxy(Class<T> type) {
        final ConfigBean sourceBean = getMasterView();
        if (!(type.getName().equals(sourceBean.model.targetTypeName))) {
            throw new IllegalArgumentException("This config bean interface is " + sourceBean.model.targetTypeName
                    + " not "  + type.getName());
        }
        Class[] interfacesClasses = { type };
        return (T) Proxy.newProxyInstance(type.getClassLoader(), interfacesClasses, this);
    }

/**
 * A Protected List is a @Link java.util.List implementation which mutable
 * operations are constrained by the owner of the list.
 *
 * @author Jerome Dochez
 */
private class ProtectedList<T extends ConfigBeanProxy> extends ArrayList<T> {

    final WriteableView parent;
    final List readOnly;
    final String id;
    final List<PropertyChangeEvent> changeEvents = new ArrayList<PropertyChangeEvent>();


    ProtectedList(List readOnly, WriteableView parent, String id) {
        super(readOnly);
        this.parent = parent;
        this.readOnly = readOnly;
        this.id = id;
    }

    @Override
    public boolean add(T object) {
        changeEvents.add(new PropertyChangeEvent(parent.bean, id, null, object));
        return super.add(object);
    }

    @Override
    public boolean remove(Object object) {
        changeEvents.add(new PropertyChangeEvent(parent.bean, id, object, null));
        return super.remove(object);
    }

}

}