/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2008 Sun Microsystems, Inc. All rights reserved.
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
import java.lang.reflect.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.*;
import org.jvnet.tiger_types.Types;

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
                t.rollback();
                throw e;
            }

        } catch(TransactionFailure e) {
            t.rollback();
            throw e;
        } catch (Exception e) {
            t.rollback();
            throw new TransactionFailure(e.getMessage(), e);
        }

    }

    static <T extends ConfigBeanProxy> WriteableView getWriteableView(T s, ConfigBean sourceBean)
        throws TransactionFailure {

        WriteableView f = new WriteableView(s);
        if (sourceBean.getLock().tryLock()) {
            return f;
        }
        throw new TransactionFailure("Config bean already locked " + sourceBean, null);
    }

    /**
     * Returns a writeable view of a configuration object
     * @param source the configured interface implementation
     * @return the new interface implementation providing write access
     */
    public static <T extends ConfigBeanProxy> T getWriteableView(final T source)
        throws TransactionFailure {

        ConfigView sourceBean = (ConfigView) Proxy.getInvocationHandler(source);
        WriteableView writeableView = getWriteableView(source, (ConfigBean) sourceBean.getMasterView());
        return (T) writeableView.getProxy(sourceBean.getProxyType());
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
    public static UnprocessedChangeEvents sortAndDispatch(PropertyChangeEvent[] events, Changed target, Logger logger) {
        List<UnprocessedChangeEvent> unprocessed = new ArrayList<UnprocessedChangeEvent>();
        List<Dom> added = new ArrayList<Dom>();
        List<Dom> changed = new ArrayList<Dom>();

        for (PropertyChangeEvent event : events) {

            if (event.getOldValue()==null && event.getNewValue() instanceof ConfigBeanProxy) {
                // something was added
                try {
                    final ConfigBeanProxy proxy =  ConfigBeanProxy.class.cast(event.getNewValue());
                    added.add(Dom.unwrap(proxy));
                    final NotProcessed nc = target.changed(Changed.TYPE.ADD, proxyType(proxy), proxy);
                    if ( nc != null ) {
                        unprocessed.add( new UnprocessedChangeEvent(event, nc.getReason() ) );
                    }
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
                    final NotProcessed nc = target.changed(Changed.TYPE.REMOVE, proxyType(proxy), proxy );
                    if ( nc != null ) {
                        unprocessed.add( new UnprocessedChangeEvent(event, nc.getReason() ) );
                    }
                } else {
                    if (!changed.contains(eventSource)) {
                        final ConfigBeanProxy proxy =  ConfigBeanProxy.class.cast(event.getSource());
                        changed.add(eventSource);
                        final NotProcessed nc = target.changed(Changed.TYPE.CHANGE, proxyType(proxy), proxy);
                        if ( nc != null ) {
                            unprocessed.add( new UnprocessedChangeEvent(event, nc.getReason() ) );
                        }
                    }
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Exception while processing config bean changes : ", e);
            }
        }
        
        return new UnprocessedChangeEvents( unprocessed );
    }

    // kind of insane, just to get the proper return type for my properties.
    static private List<String> defaultPropertyValue() {
        return null;    
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
                if (prop.isCollection()) {
                    try {
                        List<String> values = (List<String>) writeable.getter(prop,
                                ConfigSupport.class.getDeclaredMethod("defaultPropertyValue", null).getGenericReturnType());
                        values.add(change.getValue());                        
                    } catch (NoSuchMethodException e) {
                        throw new TransactionFailure("Unknown property name " + xmlName + " on " + source.getProxyType(), null);                        
                    }
                } else {
                    writeable.setter(prop, change.getValue(), String.class);
                }
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

    /**
     * Returns the list of sub-elements supported by a ConfigBean
     * @return array of classes reprensenting the sub elements of a particular
     * @throws ClassNotFoundException for severe errors with the model associated
     * with the passed config bean.
     */
    public static Class<?>[] getSubElementsTypes(ConfigBean bean)
        throws ClassNotFoundException {

        List<Class<?>> subTypes = new ArrayList<Class<?>>();
        for (ConfigModel.Property element : bean.model.elements.values()) {
            if (!element.isLeaf()) {
                ConfigModel elementModel =  ((ConfigModel.Node) element).model;
                Class<?> subType = elementModel.classLoaderHolder.get().loadClass(elementModel.targetTypeName);
                subTypes.add(subType);
            } else {
                if (element.isCollection()) {
                    subTypes.add(List.class);
                }
            }
        }
        return subTypes.toArray(new Class[subTypes.size()]);
    }

    /**
     * Returns the list of attributes names by the passed ConfigBean
     * @return array of String for all the attributes names
     */
    public static String[] getAttributesNames(ConfigBean bean) {
        return xmlNames(bean.model.attributes.values());
    }


    /**
     * Returns the list of elements names by the passed ConfigBean
     * @return array of String for all the elements names
     */
    public static String[] getElementsNames(ConfigBean bean) {
        return xmlNames(bean.model.elements.values());
    }

    private static String[] xmlNames(Collection<? extends ConfigModel.Property> properties) {

        List<String> names = new ArrayList<String>();
        for (ConfigModel.Property attribute : properties) {
            names.add(attribute.xmlName());
        }
        return names.toArray(new String[names.size()]);

    }


    /**
     * Creates a new child of the passed child and add it to the parent's live
     * list of elements. The child is also initialized with the attributes passed
     * where each key represent the xml property name for the attribute and the value
     * represent the attribute's value.
     *
     * This code will be executed within a Transaction and can therefore throw
     * a TransactionFailure when the creation or settings of attributes failed.
     *
     * Example creating a new http-listener element under http-service
     *      ConfigBean httpService = ... // got it from somwhere.
     *      Map<String, String> attributes = new HashMap<String, String>();
     *      attributes.put("id", "jerome-listener");
     *      attributes.put("enabled", "true");
     *      ConfigSupport.createAndSet(httpService, HttpListener.class, attributes);
     *
     * @param parent parent config bean to which the child will be added.
     * @param childType child type
     * @param attributes map of key value pair to set on the newly created child
     * @throws TransactionFailure if the creation or attribute settings failed
     */
    public static ConfigBean createAndSet(
                final ConfigBean parent,
                final Class<? extends ConfigBeanProxy> childType,
                final Map<String, String> attributes,
                final TransactionCallBack<WriteableView> runnable)
        throws TransactionFailure {


        return createAndSet(parent, childType, AttributeChanges.from(attributes), runnable);
        
    }
    /**
     * Creates a new child of the passed child and add it to the parent's live
     * list of elements. The child is also initialized with the attributes passed
     * where each key represent the xml property name for the attribute and the value
     * represent the attribute's value.
     *
     * This code will be executed within a Transaction and can therefore throw
     * a TransactionFailure when the creation or settings of attributes failed.
     *
     * Example creating a new http-listener element under http-service
     *      ConfigBean httpService = ... // got it from somwhere.
     *      Map<String, String> attributes = new HashMap<String, String>();
     *      attributes.put("id", "jerome-listener");
     *      attributes.put("enabled", "true");
     *      ConfigSupport.createAndSet(httpService, HttpListener.class, attributes);
     *
     * @param parent parent config bean to which the child will be added.
     * @param childType child type
     * @param attributes list of attribute changes to apply to the newly created child
     * @param runnable code that will be invoked as part of the transaction to add
     * more attributes or elements to the newly create type
     * @throws TransactionFailure if the creation or attribute settings failed
     */
    public static ConfigBean createAndSet(
                final ConfigBean parent,
                final Class<? extends ConfigBeanProxy> childType,
                final List<AttributeChanges> attributes,
                final TransactionCallBack<WriteableView> runnable)
        throws TransactionFailure {

        ConfigBeanProxy readableView = parent.getProxy(parent.getProxyType());
        ConfigBeanProxy readableChild = (ConfigBeanProxy)
                ConfigSupport.apply(new SingleConfigCode<ConfigBeanProxy>() {
            /**
             * Runs the following command passing the configration object. The code will be run
             * within a transaction, returning true will commit the transaction, false will abort
             * it.
             *
             * @param param is the configuration object protected by the transaction
             * @return any object that should be returned from within the transaction code
             * @throws java.beans.PropertyVetoException
             *          if the changes cannot be applied
             *          to the configuration
             */
            public Object run(ConfigBeanProxy param) throws PropertyVetoException, TransactionFailure {

                // create the child
                ConfigBeanProxy child = ConfigSupport.createChildOf(param, childType);

                // add the child to the parent.
                WriteableView writeableParent = (WriteableView) Proxy.getInvocationHandler(param);
                Class parentProxyType = parent.getProxyType();

                Class<?> targetClass = null;
                // first we need to find the element associated with this type
                ConfigModel.Property element = null;
                for (ConfigModel.Property e : parent.model.elements.values()) {
                    if (e.isLeaf()) {
                        continue;
                    }
                    ConfigModel elementModel =  ((ConfigModel.Node) e).model;

                    if (Logger.getAnonymousLogger().isLoggable(Level.FINE)) {
                        Logger.getAnonymousLogger().fine( "elementModel.targetTypeName = " + elementModel.targetTypeName +
                            ", collection: " + e.isCollection() + ", childType.getName() = " + childType.getName() );
                    }
                            
                    if (elementModel.targetTypeName.equals(childType.getName())) {
                        element = e;
                        break;
                    }
                    else if ( e.isCollection() ) {
                        try {
                            final Class<?> tempClass = childType.getClassLoader().loadClass( elementModel.targetTypeName);
                            if ( tempClass.isAssignableFrom( childType ) ) {
                                element = e;
                                targetClass = tempClass;
                                break;
                            }
                        } catch (Exception ex ) { 
                            throw new TransactionFailure("EXCEPTION getting class for " + elementModel.targetTypeName, ex);
                        }
                    }
                }
                
                // now depending whether this is a collection or a single leaf,
                // we need to process this setting differently
                if (element != null) {
                    if (element.isCollection()) {
                        // this is kind of nasty, I have to find the method that returns the collection
                        // object because trying to do a element.get without having the parametized List
                        // type will not work.
                        for (Method m : parentProxyType.getMethods()) {
                            final Class returnType = m.getReturnType();
                            if (Collection.class.isAssignableFrom(returnType)) {
                                // this could be it...
                                if (!(m.getGenericReturnType() instanceof ParameterizedType))
                                    throw new IllegalArgumentException("List needs to be parameterized");
                                final Class itemType = Types.erasure(Types.getTypeArgument(m.getGenericReturnType(), 0));
                                if (itemType.isAssignableFrom(childType)) {
                                    List list = null;
                                    try {
                                        list = (List) m.invoke(param, null);
                                    } catch (IllegalAccessException e) {
                                        throw new TransactionFailure("Exception while adding to the parent", e);
                                    } catch (InvocationTargetException e) {
                                        throw new TransactionFailure("Exception while adding to the parent", e);
                                    }
                                    if (list != null) {
                                        list.add(child);
                                        break;
                                    }
                                }
                            }
                        }
                    } else {
                        // much simpler, I can use the setter directly.
                        writeableParent.setter(element, child, childType);
                    }
                } else {
                    throw new TransactionFailure("Parent " + parent.getProxyType() + " does not have a child of type " + childType);
                }

                WriteableView writeableChild = (WriteableView) Proxy.getInvocationHandler(child);
                applyProperties(writeableChild, attributes);
                
                if (runnable!=null) {
                    runnable.performOn(writeableChild);
                }
                return child;
            }
        }, readableView);
        return (ConfigBean) Dom.unwrap(readableChild);
    }

    private static void applyProperties(WriteableView target, List<? extends AttributeChanges> changes)
            throws TransactionFailure {

        if (changes != null) {
            for (AttributeChanges change : changes) {

                ConfigModel.Property prop = target.getProperty(change.name);
                if (prop == null) {
                    throw new TransactionFailure("Unknown property name " + change.name + " on " + target.getProxyType());
                }
                if (prop.isCollection()) {
                    // we need access to the List
                    try {
                        List list = (List) target.getter(prop, ConfigSupport.class.getDeclaredMethod("defaultPropertyValue", null).getGenericReturnType());
                        for (String value : change.values()) {
                            list.add(value);
                        }
                    } catch (NoSuchMethodException e) {
                        throw new TransactionFailure(e.getMessage(), e);
                    }
                } else {
                    target.setter(prop, change.values()[0], String.class);
                }
            }
        }

    }


    /**
     * Creates a new child of the passed child and add it to the parent's live
     * list of elements. The child is also initialized with the attributes passed
     * where each key represent the xml property name for the attribute and the value
     * represent the attribute's value.
     *
     * This code will be executed within a Transaction and can therefore throw
     * a TransactionFailure when the creation or settings of attributes failed.
     *
     * Example creating a new http-listener element under http-service
     *      ConfigBean httpService = ... // got it from somwhere.
     *      Map<String, String> attributes = new HashMap<String, String>();
     *      attributes.put("id", "jerome-listener");
     *      attributes.put("enabled", "true");
     *      ConfigSupport.createAndSet(httpService, HttpListener.class, attributes);
     *
     * @param parent parent config bean to which the child will be added.
     * @param childType child type
     * @param attributes list of attributes changes to apply to the new created child
     * @throws TransactionFailure if the creation or attribute settings failed
     */
    public static ConfigBean createAndSet(
                final ConfigBean parent,
                final Class<? extends ConfigBeanProxy> childType,
                final Map<String, String> attributes)
            throws TransactionFailure {

        return createAndSet(parent, childType, attributes, null);        
    }

    public static ConfigBean createAndSet(
                final ConfigBean parent,
                final Class<? extends ConfigBeanProxy> childType,
                final List<AttributeChanges> attributes)
            throws TransactionFailure {

        return createAndSet(parent, childType, attributes, null);
    }
    
    public static void deleteChild(
                final ConfigBean parent,
                final ConfigBean child)
        throws TransactionFailure {


        ConfigBeanProxy readableView = parent.getProxy(parent.getProxyType());
        final Class<? extends ConfigBeanProxy> childType = child.getProxyType();

        ConfigSupport.apply(new SingleConfigCode<ConfigBeanProxy>() {

            /**
             * Runs the following command passing the configration object. The code will be run
             * within a transaction, returning true will commit the transaction, false will abort
             * it.
             *
             * @param param is the configuration object protected by the transaction
             * @return any object that should be returned from within the transaction code
             * @throws java.beans.PropertyVetoException
             *          if the changes cannot be applied
             *          to the configuration
             */
            public Object run(ConfigBeanProxy param) throws PropertyVetoException, TransactionFailure {

                // get the child
                ConfigBeanProxy childProxy = child.getProxy(childType);

                // remove the child from the parent.
                WriteableView writeableParent = (WriteableView) Proxy.getInvocationHandler(param);
                Class parentProxyType = parent.getProxyType();

                // first we need to find the element associated with this type
                ConfigModel.Property element = null;
                for (ConfigModel.Property e : parent.model.elements.values()) {
                    ConfigModel elementModel = ((ConfigModel.Node) e).model;
                    try {
                        final Class<?> targetClass = parent.model.classLoaderHolder.get().loadClass(elementModel.targetTypeName);
                        if (targetClass.isAssignableFrom(childType)) {
                            element = e;
                            break;
                        }
                    } catch(Exception ex) {
                        // ok.
                    }
                }
                // now depending whether this is a collection or a single leaf,
                // we need to process this setting differently
                if (element != null) {
                    if (element.isCollection()) {
                        // this is kind of nasty, I have to find the method that returns the collection
                        // object because trying to do a element.get without having the parametized List
                        // type will not work.
                        for (Method m : parentProxyType.getMethods()) {
                            final Class returnType = m.getReturnType();
                            if (Collection.class.isAssignableFrom(returnType)) {
                                // this could be it...
                                if (!(m.getGenericReturnType() instanceof ParameterizedType))
                                    throw new IllegalArgumentException("List needs to be parameterized");
                                final Class itemType = Types.erasure(Types.getTypeArgument(m.getGenericReturnType(), 0));
                                if (itemType.isAssignableFrom(childType)) {
                                    List list = null;
                                    try {
                                        list = (List) m.invoke(param, null);
                                    } catch (IllegalAccessException e) {
                                        throw new TransactionFailure("Exception while adding to the parent", e);
                                    } catch (InvocationTargetException e) {
                                        throw new TransactionFailure("Exception while adding to the parent", e);
                                    }
                                    if (list != null) {
                                        list.remove(childProxy);
                                        break;
                                    }
                                }
                            }
                        }
                    } else {
                        // much simpler, I can use the setter directly.
                        writeableParent.setter(element, child, childType);
                    }
                } else {
                    throw new TransactionFailure("Parent " + parent.getProxyType() + " does not have a child of type " + childType);
                }

                return child;
            }
        }, readableView);
    }
    
    public interface TransactionCallBack<T> {
        public void performOn(T param) throws TransactionFailure;
    }

    public static abstract class AttributeChanges {
        final String name;

        AttributeChanges(String name) {
            this.name = name;
        }
        abstract String[] values();

        static List<AttributeChanges> from(Map<String, String> values) {
            if (values==null) {
                return null;
            }
            List<AttributeChanges> changes = new ArrayList<AttributeChanges>();
            for(Map.Entry<String, String> entry : values.entrySet()) {
                changes.add(new SingleAttributeChange(entry.getKey(), entry.getValue()));
            }
            return changes;
        }
        
    }

    public static class SingleAttributeChange extends AttributeChanges {
        final String[] values = new String[1];
        
        public SingleAttributeChange(String name, String value) {
            super(name);
            values[0] = value;
        }

        String[] values() {
            return values;
        }
    }

    public static class MultipleAttributeChanges extends AttributeChanges {
        final String[] values;

        public MultipleAttributeChanges(String name, String[] values) {
            super(name);
            this.values = values;
        }

        String[] values() {
            return values;
        }
    }
 }
