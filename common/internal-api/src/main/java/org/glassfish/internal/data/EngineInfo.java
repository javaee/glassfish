/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package org.glassfish.internal.data;

import org.glassfish.api.container.Sniffer;
import org.glassfish.api.container.Container;
import org.glassfish.api.deployment.Deployer;
import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.DeploymentContext;
import org.jvnet.hk2.component.Inhabitant;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;


import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * This class holds information about a particular container such as a reference
 * to the sniffer, the container itself and the list of applications deployed in
 * that container.
 *
 * @author Jerome Dochez 
 */
public class EngineInfo<T extends Container, U extends ApplicationContainer> {

    final Inhabitant<T> container;
    final Sniffer sniffer;
    ContainerRegistry registry = null;
    Map<WeakReference<Thread>, Set<Integer>> addedThreadLocals = new HashMap();
    Deployer deployer;
    final ClassLoader mainClassLoader;

    /**
     * Creates a new ContractProvider info with references to the container, the sniffer
     * and the connector module implementing the ContractProvider/Deployer interfaces.
     *
     * @param container instance of the container
     * @param sniffer sniffer associated with that container
     */
    public EngineInfo(Inhabitant<T> container, Sniffer sniffer, ClassLoader cloader) {
        this.container = container;
        this.sniffer = sniffer;
        this.mainClassLoader = cloader;
    }

    /**
     * Returns the container instance
     * @return the container instance
     */
    public T getContainer() {
        return container.get();
    }

    /**
     * Returns the sniffer associated with this container
     * @return the sniffer instance
     */
    public Sniffer getSniffer() {
        return sniffer;
    }

    /**
     * Returns the deployer instance for this container
     *
     * @return Deployer instance
     */
    public Deployer<T, U> getDeployer() {
        return deployer;
    }

    /**
     * Sets the deployer associated with this container
     * 
     * @param deployer
     */
    public void setDeployer(Deployer<T, U> deployer) {
        this.deployer = deployer;
    }

    public void load(ExtendedDeploymentContext context) {
    }

    public void unload(ExtendedDeploymentContext context) throws Exception {
    }

    public void clean(ExtendedDeploymentContext context) throws Exception {
        getDeployer().clean(context);
    }

    /*
     * Sets the registry this container belongs to
     * @param the registry owning me
     */
    public void setRegistry(ContainerRegistry registry) {
        this.registry = registry;
    }

    public ClassLoader getClassLoader() {
        return mainClassLoader;
    }

    /**
     * Adds number of threadlocal hashcodes
     */
    public synchronized void addThreadLocal(Thread t, Set<Integer> newEntries) {

        Set<Integer> existingEntries = null;
        for (Map.Entry<WeakReference<Thread>, Set<Integer>> entry : addedThreadLocals.entrySet()) {
            if (entry.getKey().get()!=null) {
                if (entry.getKey().get().equals(t)) {
                    // found our guy
                    existingEntries = entry.getValue();
                    break;
                }
            } else {
                // thread is dead, cleanup since we are at it.
                addedThreadLocals.remove(entry.getKey());
            }
        }
        if (existingEntries==null) {
            existingEntries = new HashSet();
            addedThreadLocals.put(new WeakReference(t), existingEntries);
        }
        
        existingEntries.addAll(newEntries);
    }

    private void cleanup() {
        for (Map.Entry<WeakReference<Thread>, Set<Integer>> entry : addedThreadLocals.entrySet()) {
            if (entry.getKey().get()!=null) {
                try {
                    Field threadLocalsField = Thread.class.getDeclaredField("threadLocals");
                    threadLocalsField.setAccessible(true);
                    Class c = Class.forName("java.lang.ThreadLocal$ThreadLocalMap");
                    Field tableField = c.getDeclaredField("table");
                    Method remove = c.getDeclaredMethod("remove", ThreadLocal.class);
                    tableField.setAccessible(true);
                    c = Class.forName("java.lang.ThreadLocal$ThreadLocalMap$Entry");
                    Field value = c.getDeclaredField("value");
                    value.setAccessible(true);
                    Thread t = entry.getKey().get();                                
                    Object threadLocals = threadLocalsField.get(t);
                    WeakReference<ThreadLocal>[] table = (WeakReference<ThreadLocal>[]) tableField.get(threadLocals);
                    int len = table.length;
                    for (int j = 0; j < len; j++) {
                        WeakReference<ThreadLocal> e = table[j];
                        if (e != null) {
                            remove.invoke(threadLocalsField, e.get());
                            System.out.println("Removed " + value.get(e));
                        }
                    }
                } catch(NoSuchFieldException e) {
                    e.printStackTrace();
                } catch(ClassNotFoundException e) {
                    e.printStackTrace();
                } catch(IllegalAccessException e) {
                    e.printStackTrace();
                } catch(NoSuchMethodException e) {
                    e.printStackTrace();
                } catch(InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Todo : take care of Deployer when unloading...
    public void stop(Logger logger)
    {
        if (getDeployer()!=null) {
            Inhabitant i = registry.habitat.getInhabitantByType(getDeployer().getClass());
            if (i!=null) {
                i.release();
            }
        }
        if (getContainer()!=null) {
            Inhabitant i = registry.habitat.getInhabitantByType(getContainer().getClass());
            if (i!=null) {
                i.release();
            }
        }
        registry.removeContainer(this);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Container " + getContainer().getName() + " stopped");
        }
    }    
}
