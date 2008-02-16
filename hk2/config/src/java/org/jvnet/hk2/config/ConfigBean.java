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

import org.jvnet.hk2.component.Habitat;

import javax.xml.stream.XMLStreamReader;
import java.beans.*;
import java.lang.reflect.Type;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.TimeUnit;

import javax.management.ObjectName;


/**
 * ConfigBean is the core implementation of the config beans. It has features like locking
 * view creation and optional features attachement.
 *
 * @author Jerome Dochez
 */
public class ConfigBean extends Dom implements ConfigView {


    private volatile boolean writeLock = false;
    private final Map<Class , ConfigBeanInterceptor> optionalFeatures =
            new HashMap<Class, ConfigBeanInterceptor>();
    
    /**
        ObjectName will be null until when/if an MBean is registered.
     */
    private volatile ObjectName objectName = null;
    
    public ObjectName getObjectName() { return objectName; }
    
    public void setObjectName( final ObjectName objectNameIn )
    {
        if ( objectName != null ) throw new IllegalStateException();
        objectName = objectNameIn;
    }

    public ConfigBean(Habitat habitat, DomDocument document, Dom parent, ConfigModel model, XMLStreamReader in) {

        super(habitat, document, parent, model, in);
        // by default all ConfigBean support the ConstrainedBeanListener interface
        // allowing clients to register interest in attributes changing.
        addInterceptor(new ConfigBeanInterceptor<ConstrainedBeanListener>() {

            List<VetoableChangeListener> listeners = new ArrayList<VetoableChangeListener>();

            public ConstrainedBeanListener getConfiguration() {
                return new ConstrainedBeanListener() {

                    public void removeVetoableChangeListener(VetoableChangeListener listener) {
                        listeners.remove(listener);
                    }

                    public void addVetoableChangeListener(VetoableChangeListener listener) {
                        listeners.add(listener);
                    }
                };
            }

            public void beforeChange(PropertyChangeEvent evt) throws PropertyVetoException {
                for (VetoableChangeListener listener : listeners) {
                    listener.vetoableChange(evt);
                }
            }
            public void afterChange(PropertyChangeEvent evt, long timestamp) {
            }

            public void readValue(ConfigBean source, String xmlName, Object Value) {
            }
        });
    }

    /**
     * Returns an optional feature of the ConfigBean. Optional features are implemented
     * by other objects and attached to this instance. Attached features can be queried
     * using the getOptionalFeature method giving the type of the requestion optional
     * feature.
     * 
     * @param featureType type of the optional feature requested.
     * @return optional feature implementation is one is attached to this instance
     */
    @SuppressWarnings("unchecked")    
    public <T> T getOptionalFeature(Class<T> featureType) {
        if (optionalFeatures.containsKey(featureType)) {
            return (T) optionalFeatures.get(featureType);
        }
        return null;
    }

    protected void setter(ConfigModel.Property target, Object value) throws Exception  {
        if (!writeLock) {
            throw new PropertyVetoException("Not part of a transaction !", null);
        }
        Object oldValue = super.getter(target, value.getClass());
        PropertyChangeEvent evt = new PropertyChangeEvent(this, target.xmlName(), oldValue, value);
        for (ConfigBeanInterceptor interceptor : optionalFeatures.values()) {
            interceptor.beforeChange(evt);
        }
        super.setter(target, value);
        for (ConfigBeanInterceptor interceptor : optionalFeatures.values()) {
            interceptor.afterChange(evt, System.currentTimeMillis());
        }
    }

    protected Object getter(ConfigModel.Property target, Type t) {
        Object value = super.getter(target, t);
        for (ConfigBeanInterceptor interceptor : optionalFeatures.values()) {
            interceptor.readValue(this, target.xmlName(), value);
        }
        return value;
    }

    /**
     * Add a new ConfigBeanInterceptor to this ConfigBean instance. The inteceptor will
     * be called each time a attribute of this bean is accessed.
     *
     * @param interceptor the new interceptor
     */
    public void addInterceptor(ConfigBeanInterceptor interceptor) {
        optionalFeatures.put(interceptor.getConfiguration().getClass(), interceptor);
    }

    /**
     * We are the master view.
     *
     * @return the master view
     */
    public ConfigBean getMasterView() {
        return this;
    }

    public void setMasterView(ConfigView view) {        
    }

    /**
     * Creates a proxy for this view.
     *
     * @param proxyType requested proxy type
     * @return Java SE proxy
     */
    public <T extends ConfigBeanProxy> T getProxy(Class<T> proxyType) {
        return proxyType.cast(Proxy.newProxyInstance(proxyType.getClassLoader(), new Class[] { proxyType} , this));
    }

    /**
     * Allocate a new ConfigBean object as part of the Transaction
     * associated with this configuration object. This will eventually
     * be moved to a factory.
     *
     * @param type the request configuration object type
     * @return the propertly constructed configuration object
     */

    ConfigBean allocate(Class<?> type) {
        return new ConfigBean(habitat, document, this, document.buildModel(type), null);
   }

    /**
     * Allocate a new ConfigBean object as part of the Transaction
     * associated with this configuration object. This will eventually
     * be moved to a factory.
     *
     * @param type the request configuration object type
     * @return the propertly constructed configuration object
     */

    <T extends ConfigBeanProxy> T allocateProxy(Class<T> type) {
        return allocate(type).createProxy(type);
    }

    /**
     * Returns the lock on this object, only one external view (usually the writeable view) can
     * acquire the lock ensuring that the objects cannot be concurrentely modified
     *
     * @return lock instance
     */
    public Lock getLock() {
        return lock;

    }

    /**
     * simplistic non reentrant lock implementation, needs rework
     */
    final private Lock lock = new Lock() {
        
        public void lock() {
            throw new UnsupportedOperationException();
        }

        public void lockInterruptibly() throws InterruptedException {
            throw new UnsupportedOperationException();
        }

        public synchronized boolean tryLock() {
            if (!writeLock) {
                writeLock=true;
                return true;
            }
            return false;
        }

        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            throw new UnsupportedOperationException();
        }

        public synchronized void unlock() {
            writeLock = false;
        }

        public Condition newCondition() {
            throw new UnsupportedOperationException();
        }
    };
}
