package org.jvnet.hk2.config;

import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * A WriteableView is a view of a ConfigBean object that allow access to the
 * setters of the ConfigBean.
 *
 * @author Jerome Dochez
 */
public class WriteableView implements InvocationHandler, Transactor, ConfigView {

    private final ConfigBean bean;
    private final Map<String, PropertyChangeEvent> changedAttributes;
    Transaction currentTx;

    public WriteableView(ConfigBean b) {
        this.bean = b;
        changedAttributes = new HashMap<String, PropertyChangeEvent>();
    }

    /**
     * Processes a method invocation on a proxy instance and returns
     * the result.  This method will be invoked on an invocation handler
     * when a method is invoked on a proxy instance that it is
     * associated with.
     *
     * @param    proxy the proxy instance that the method was invoked on
     * @param    method the <code>Method</code> instance corresponding to
     * the interface method invoked on the proxy instance.  The declaring
     * class of the <code>Method</code> object will be the interface that
     * the method was declared in, which may be a superinterface of the
     * proxy interface that the proxy class inherits the method through.
     * @param    args an array of objects containing the values of the
     * arguments passed in the method invocation on the proxy instance,
     * or <code>null</code> if interface method takes no arguments.
     * Arguments of primitive types are wrapped in instances of the
     * appropriate primitive wrapper class, such as
     * <code>java.lang.Integer</code> or <code>java.lang.Boolean</code>.
     * @return the value to return from the method invocation on the
     * proxy instance.  If the declared return type of the interface
     * method is a primitive type, then the value returned by
     * this method must be an instance of the corresponding primitive
     * wrapper class; otherwise, it must be a type assignable to the
     * declared return type.  If the value returned by this method is
     * <code>null</code> and the interface method's return type is
     * primitive, then a <code>NullPointerException</code> will be
     * thrown by the method invocation on the proxy instance.  If the
     * value returned by this method is otherwise not compatible with
     * the interface method's declared return type as described above,
     * a <code>ClassCastException</code> will be thrown by the method
     * invocation on the proxy instance.
     * @throws Throwable the exception to throw from the method
     * invocation on the proxy instance.  The exception's type must be
     * assignable either to any of the exception types declared in the
     * <code>throws</code> clause of the interface method or to the
     * unchecked exception types <code>java.lang.RuntimeException</code>
     * or <code>java.lang.Error</code>.  If a checked exception is
     * thrown by this method that is not assignable to any of the
     * exception types declared in the <code>throws</code> clause of
     * the interface method, then an
     * {@link java.lang.reflect.UndeclaredThrowableException} containing the
     * exception that was thrown by this method will be thrown by the
     * method invocation on the proxy instance.
     * @see    java.lang.reflect.UndeclaredThrowableException
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        // are we still in a transaction
        if (currentTx==null) {
            throw new IllegalStateException("Not part of a transation");
        }
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
                return bean.invoke(proxy, method, args);
            }
        } else {
            // setter
            Object oldValue = bean.getter(property, method.getGenericParameterTypes()[0]);
            Object newValue = args[0];
            if (args[0] instanceof ConfigBeanProxy) {
                ConfigView bean = (ConfigView) Proxy.getInvocationHandler((ConfigBeanProxy) args[0]);
                newValue = bean.getMasterView();
            }
            PropertyChangeEvent evt = new PropertyChangeEvent(proxy,property.xmlName(), oldValue, newValue);
            changedAttributes.put(property.xmlName(), evt);
            return null;
        }
    }

    public static <T  extends ConfigBeanProxy> T getRawView(T s) {

        Transformer rawTransformer = new Transformer() {
            public <T  extends ConfigBeanProxy> T transform(T source) {
                 return source;
            }
        };

        return getView(rawTransformer, s);
    }

    public static WriteableView getWriteableView(ConfigBean sourceBean) {
        WriteableView f = new WriteableView(sourceBean);
        if (sourceBean.getLock().tryLock()) {
            return f;
        }
        return null;
    }

    public static <T extends ConfigBeanProxy> T getWriteableView(final T source) {

        Transformer writeableTransformer = new Transformer() {

            @SuppressWarnings("unchecked")
            public <T extends ConfigBeanProxy> T transform(T s) {
                try {
                    ConfigBean sourceBean = (ConfigBean) Proxy.getInvocationHandler(s);
                    Class[] interfacesClasses = { sourceBean.getClass().getClassLoader().loadClass(sourceBean.model.targetTypeName) };
                    WriteableView f = getWriteableView(sourceBean);
                    if (f!=null) {
                        return (T) Proxy.newProxyInstance(sourceBean.getClass().getClassLoader(), interfacesClasses, f);
                    }
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        };
        return getView(writeableTransformer, source);
    }

    public static <T extends ConfigBeanProxy> T getView(Transformer t, T source) {
        return t.transform(source);
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
        List<PropertyChangeEvent> appliedChanges = new ArrayList<PropertyChangeEvent>();
        for (PropertyChangeEvent event : changedAttributes.values()) {
            ConfigModel.Property property = bean.model.findIgnoreCase(event.getPropertyName());
            property.set(bean, event.getNewValue());
            appliedChanges.add(event);
        }
        bean.getLock().unlock();
        changedAttributes.clear();
        return appliedChanges;

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
        WriteableView writeableView = getWriteableView(newBean);        
        writeableView.join(currentTx);

        return writeableView.getProxy(type);
   }

    public ConfigBean getMasterView() {
        return bean;
    }

    @SuppressWarnings("unchecked")    
    public <T extends ConfigBeanProxy> T getProxy(Class<T> type) {
        final ConfigBean sourceBean = getMasterView();
        Class[] interfacesClasses = { type };
        return (T) Proxy.newProxyInstance(sourceBean.getClass().getClassLoader(), interfacesClasses, this);
    }
}