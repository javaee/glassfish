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

import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigModel;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.DomDocument;

import javax.xml.stream.XMLStreamReader;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedList;
import java.util.List;

/**
 * Subclass of our standard DOM element handler with more muscle :
 *  - can fire property change events when attributes or elements are changed
 *  - supports Transaction access to attributes and elements.
 *
 * @author Jerome Dochez
 */
public class ConfigBean extends Dom implements ConstrainedBean, Cloneable, Transactor {
                                

    private Transaction currentTx=null;
    protected VetoableChangeSupport support=new TypedVetoableChangeSupport(this);

    public ConfigBean(Habitat habitat, DomDocument domDocument, Dom dom, ConfigModel configModel, XMLStreamReader xmlStreamReader) {
        super(habitat, domDocument, dom, configModel, xmlStreamReader);   
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
        } 
    }

	/**
	 * Aborts this Transaction, reverting the state

	 * @param t the aborting transaction
	 */    
    public synchronized void abort(Transaction t) {
        currentTx=null;
    }

    /**
     * Allocate a new ConfigBean object as part of the Transaction
     * associated with this configuration object. This will eventually
     * be moved to a factory.
     *
     * @param type the request configuration object type
     * @return the propertly constructed configuration object
     */

    public <T extends ConfigBeanProxy> T allocate(Class<T> type) {
        if (currentTx==null) {
            throw new RuntimeException("Not part of a transaction");
        }
        ConfigBean instance;
        instance = new ConfigBean(habitat, document, this, document.getModel(type), null);
        
        instance.join(currentTx);
        return instance.createProxy(type);
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
            return o;
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }

    /**
     * {@link java.lang.reflect.InvocationHandler} implementation that allows strongly-typed access
     * to the configuration.
     *
     * <p>
     * TODO: it might be a great performance improvement to have APT generate
     * code that does this during the development time by looking at the interface.
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // serve java.lang.Object methods by ourselves
        if(method.getDeclaringClass()==Object.class) {
            try {
                return method.invoke(this,args);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }

        ConfigModel.Property p = toProperty(method);
        if(p==null)
            throw new IllegalArgumentException("No corresponding property found for method: "+method);

        if(args==null || args.length==0) {
            // getter
            return getter(p, method.getGenericReturnType());
        } else {
            // setter
            Object oldValue = getter(p, method.getGenericParameterTypes()[0]);
            PropertyChangeEvent evt = new PropertyChangeEvent(proxy,p.xmlName(), oldValue, args[0]);
            if (currentTx==null) {
                throw new PropertyVetoException("No transaction associated with " + this, evt);
            }
            // check that if we are setting a new value object, that this value is also part of
            // the same transaction
            try {
                Proxy valueProxy = Proxy.class.cast(evt.getNewValue());
                if (valueProxy!=null) {
                    ConfigBean isIt = (ConfigBean) Proxy.getInvocationHandler(valueProxy);
                    if (!isIt.canCommit(currentTx)) {
                        throw new PropertyVetoException("New value of type " + evt.getNewValue().getClass() + " is not part " +
                            "of this ConfigBean transaction, use allocate(Class<T>) instead of new() to allocate new config objects", evt);
                    }
                }
            } catch(ClassCastException e) {
                // ignore
            }
            // this will generate a PropertyVetoException if any of the listener disagree
            support.fireVetoableChange(p.xmlName(), oldValue, args[0]);

            // so far so good, nobody objects to the new value being set on the object
            setter(p, args[0]);
            return null;
        }
    }
}
