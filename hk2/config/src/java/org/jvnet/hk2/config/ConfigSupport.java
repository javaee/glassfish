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

import org.jvnet.hk2.annotations.Service;

import java.beans.PropertyVetoException;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.Proxy;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
  * <p>
  * Helper class to execute some code on configuration objects while taking
  * care of the transaction boiler plate code.
  * </p>
  * <p>
  * Programmers that wish to apply some changes to configuration objects
  * can use these convenience methods to reduce the complexity of handling
  * transactions.
  * </p>
  * <p>
  * For instance, say a programmer need to change the HttpListener port from
  * 8080 to 8989, it just needs to do :
  * </p>
  * <pre>
  *     ... in his code somewhere ...
  *     HttpListener httpListener = domain.get...
  *
  *     // If the programmer tries to modify the httpListener directly
  *     // it will get an exception
  *     httpListener.setPort("8989"); // will generate a PropertyVetoException
  *
  *     // instead he needs to use a transaction and can use the helper services
  *     ConfigSupport.apply(new SingleConfigCode<HttpListener>() {
  *         public Object run(HttpListener okToChange) throws PropertyException {
  *             okToChange.setPort("8989"); // good...
  *             httpListener.setPort("7878"); // not good, exceptions still raised...
  *             return null;
  *         });
  *
  *     // Note that after this code
  *     System.out.println("Port is " + httpListener.getPort());
  *     // will display 8989
  * }
  * </pre>
  * @author Jerome Dochez
  */
@Service
public class ConfigSupport {
 
    /**
     * Execute§ some logic on one config bean of type T protected by a transaction
     *
     * @param code code to execute
     * @param param config object participating in the transaction
     * @return list of events that represents the modified config elements.
     * @throws TransactionFailure when code did not run successfully
     */
    public static <T extends ConfigBeanProxy> Object apply(final SingleConfigCode<T> code, T param)
        throws TransactionFailure {
        
        ConfigBeanProxy[] objects = { param };
        return apply((new ConfigCode() {
            @SuppressWarnings("unchecked")
            public Object run(ConfigBeanProxy... objects) throws PropertyVetoException, TransactionFailure {
                return code.run((T) objects[0]);
            }
        }), objects);
    }

    /**
     * Creates a new child of a parent configured object
     *
     * @param parent the parent configured object
     * @param type type of the child
     * @return new child instance of the provided type
     * @throws TransactionFailure if the child cannot be created
     */
     public static <T extends ConfigBeanProxy> T createChildOf(Object parent, Class<T> type) throws TransactionFailure {
         if (parent==null) {
             throw new IllegalArgumentException("parent cannot be null");
         }
         try {
             WriteableView bean = WriteableView.class.cast(Proxy.getInvocationHandler(Proxy.class.cast(parent)));
             return bean.allocateProxy(type);
         } catch (ClassCastException e) {
             throw new TransactionFailure("Must use a locked parent config object for instantiating new config object", e);
         }


     }
    
    /**
     * Executes some logic on some config beans protected by a transaction.
     *
     * @param code code to execute
     * @param objects config beans participating to the transaction
     * @return list of property change events
     * @throws TransactionFailure when the code did run successfully due to a
     * transaction exception
     */
    public static Object apply(ConfigCode code, ConfigBeanProxy... objects)
            throws TransactionFailure {
        
        // the fools think they operate on the "real" object while I am
        // feeding them with writeable view. Only if the transaction succeed
        // will I apply the "changes" to the real ones.
        WriteableView[] views = new WriteableView[objects.length];

        ConfigBeanProxy[] proxies = new ConfigBeanProxy[objects.length];

        // create writeable views.
        for (int i=0;i<objects.length;i++) {
            proxies[i] = getWriteableView(objects[i]);
            views[i] = (WriteableView) Proxy.getInvocationHandler(proxies[i]);
        }

        // Of course I am not locking the live objects but the writable views.
        // if the user try to massage the real objects, he will get
        // a well deserved nasty exception
        Transaction t = new Transaction();
        for (WriteableView view : views) {
            if (!view.join(t)) {
                t.rollback();
                throw new TransactionFailure("Cannot enlist " + view.getMasterView().getProxyType()
                    + " in transaction", null);
            }
        }
        
        try {
            final Object toReturn = code.run(proxies);
            try {
                t.commit();
                if (toReturn instanceof WriteableView) {
                    return ((WriteableView) toReturn).getMasterView();
                } else {
                    return toReturn;
                }
            } catch (RetryableException e) {
                System.out.println("Retryable...");
                // TODO : do something meaninful here
                t.rollback();
                return null;
            } catch (TransactionFailure e) {
                System.out.println("failure, not retryable...");
                t.rollback();
                throw e;
            }

        } catch (PropertyVetoException e) {
            t.rollback();
            throw new TransactionFailure(e.getMessage(), e);
        } catch(TransactionFailure e) {
            t.rollback();
            throw e;
        }

    }

    static <T extends ConfigBeanProxy> WriteableView getWriteableView(T s, ConfigBean sourceBean) {

        WriteableView f = new WriteableView(s);
        if (sourceBean.getLock().tryLock()) {
            return f;
        }
        return null;
    }

    /**
     * Returns a writeable view of a configuration object
     * @param source the configured interface implementation
     * @return the new interface implementation providing write access
     */
    public static <T extends ConfigBeanProxy> T getWriteableView(final T source) {

        Transformer writeableTransformer = new Transformer() {

            @SuppressWarnings("unchecked")
            public <T extends ConfigBeanProxy> T transform(T s) {
                ConfigView sourceBean = (ConfigView) Proxy.getInvocationHandler(s);
                WriteableView writeableView = new WriteableView(source);
                return (T) writeableView.getProxy(sourceBean.getProxyType());
            }
        };
        return getView(writeableTransformer, source);
    }

    /**
     * Generic api to create a new view based on a transformer and a source object
     * @param t transformer responsible for changing the source object into a new view.
     * @param source the source object to adapt
     * @return the adapted view of the source object
     */
    public static <T extends ConfigBeanProxy> T getView(Transformer t, T source) {
        return t.transform(source);
    }

    /**
     * Return the main implementation bean for a proxy.
     * @param source configuration interface proxy
     * @return the implementation bean
     */
    public static ConfigView getImpl(ConfigBeanProxy source) {

        Object bean = Proxy.getInvocationHandler(source);
        if (bean instanceof ConfigView) {
            return ((ConfigView) bean).getMasterView();
        } else {
            return (ConfigBean) bean;
        }
        
    }

    /**
     * Returns the type of configuration object this config proxy represents.
     * @param element is the configuration object
     * @return the configuration interface class
     */
    public static <T extends ConfigBeanProxy> Class<T> proxyType(T element) {
        ConfigView bean = getImpl(element);
        return bean.getProxyType();
    }

    /**
     * sort events and dispatch the changes. There will be only one notification of event
     * per event type, per object, meaning that if an object has had 3 attributes changes, the
     * Changed interface implementation will get notified only once.
     *
     * @param events of events that resulted of a successful configuration transaction
     * @param target the intended receiver of the changes notification
     * @param logger to log any issues.
     */
    public static void sortAndDispatch(PropertyChangeEvent[] events, Changed target, Logger logger) {


        List<PropertyChangeEvent> unprocessed = new ArrayList<PropertyChangeEvent>();
        List<Dom> added = new ArrayList<Dom>();
        List<Dom> changed = new ArrayList<Dom>();

        for (PropertyChangeEvent event : events) {

            if (event.getOldValue()==null && event.getNewValue() instanceof ConfigBeanProxy) {
                // something was added
                try {
                    final ConfigBeanProxy proxy =  ConfigBeanProxy.class.cast(event.getNewValue());
                    added.add(Dom.unwrap(proxy));
                    target.changed(Changed.TYPE.ADD, proxyType(proxy), proxy);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Exception while processing config bean changes : ", e);
                }
            }
        }

        for (PropertyChangeEvent event : events) {

            try {
                Dom eventSource = Dom.unwrap((ConfigBeanProxy) event.getSource());
                if (added.contains(eventSource)) {
                    // we don't really send the changed events for new comers.
                    continue;
                }
                if (event.getNewValue()==null) {
                    final ConfigBeanProxy proxy =  ConfigBeanProxy.class.cast(event.getOldValue());
                    target.changed(Changed.TYPE.REMOVE, proxyType(proxy), proxy );
                } else {
                    if (!changed.contains(eventSource)) {
                        final ConfigBeanProxy proxy =  ConfigBeanProxy.class.cast(event.getSource());
                        changed.add(eventSource);
                        target.changed(Changed.TYPE.CHANGE, proxyType(proxy), proxy);
                    }
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Exception while processing config bean changes : ", e);
            }
        }
    }

    public static void apply(Map<ConfigBean, Map<String, String>> mapOfChanges) throws TransactionFailure {

        Transaction t = new Transaction();
        for (Map.Entry<ConfigBean, Map<String, String>> configBeanChange : mapOfChanges.entrySet()) {

            ConfigBean source = configBeanChange.getKey();
            ConfigBeanProxy readableView = source.getProxy(source.getProxyType());
            WriteableView writeable = ConfigSupport.getWriteableView(readableView, source);
            if (!writeable.join(t)) {
                t.rollback();
                throw new TransactionFailure("Cannot enlist " + source.getProxyType() + " in transaction",null);
            }
            for (Map.Entry<String, String> change : configBeanChange.getValue().entrySet()) {
                String xmlName = change.getKey();
                ConfigModel.Property prop = writeable.getProperty(xmlName);
                if (prop==null) {
                    throw new TransactionFailure("Unknown property name " + xmlName + " on " + source.getProxyType(), null);
                }
                writeable.setter(writeable.getProperty(xmlName), change.getValue(), String.class);
            }
        }
        try {
            t.commit();
        } catch (RetryableException e) {
            System.out.println("Retryable...");
            // TODO : do something meaninful here
            t.rollback();
            throw new TransactionFailure(e.getMessage(), e);
        } catch (TransactionFailure e) {
            System.out.println("failure, not retryable...");
            t.rollback();
            throw e;
        }
    }
 }