/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Sun Microsystems, Inc. All rights reserved.
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
package org.jvnet.hk2.config.provider.internal;

import java.beans.PropertyChangeEvent;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.MultiMap;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.provider.ConfigTransaction;
import org.jvnet.hk2.config.provider.ConfigTransactionException;
import org.jvnet.hk2.config.provider.ConfigTransactionRejectedException;

import com.sun.hk2.component.InhabitantStore;
import com.sun.hk2.component.InjectionResolverQuery;

/**
 * Implementation for ConfigTransaction.
 * 
 * @author Jeff Trent
 */
@Service
public class ConfigTransactionImpl implements ConfigTransaction, InjectionResolverQuery {

  private final Logger logger = Logger.getLogger(ConfigTransactionImpl.class.getName());

  // the one responsible for interacting with the outside (habitat)
  private ConfigTransactionCoordinator coordinator;
  
  // map of config bean to configEntry objects describing changes impending for that config bean
  private Map<Object, ConfigEntry> txnConfig2Entry = new HashMap<Object, ConfigEntry>();

  // are we in the prepared state?
  private boolean prepared;

  
  ConfigTransactionImpl(ConfigTransactionCoordinator coordinator) {
    this.coordinator = coordinator;
  }

  @Override
  public synchronized void prepare() throws ConfigTransactionException {
    if (prepared) {
      return;
    }
    
    verifyActiveTransaction(true);

    try {
      for (Entry<Object, ConfigEntry> entry : txnConfig2Entry.entrySet()) {
        prepareEntry(entry.getKey(), entry.getValue());
      }
      prepared = true;
    } catch (ConfigTransactionRejectedException e) {
      // force rollback
      rollback();
    }
  }

  private void prepareEntry(Object configBean, ConfigEntry value) {
    if (value.prepared) {
      return;
    }
    
    if (Change.CREATE == value.change) {
      // get the corresponding inhabitants configured by this type of configuration bean
      value.setConfiguredByMeta(coordinator.getConfiguredByInhabitants(configBean.getClass()));
      
      // iterate over each meta inhabitant, actualizing the corresponding managed inhabitant instance
      // and send out prepare notifications to applicable interested instances
      for (ConfigByMetaInhabitant cbi : value.configuredByMeta) {
        cbi.managePrepare(configBean, (InhabitantStore)value, this);
        // TODO: send out a prepare event
      }
    } else if (Change.UPDATE == value.change) {
      // TODO:
      throw new UnsupportedOperationException();
    } else if (Change.DELETE == value.change) {
      // nop
    } else {
      // should never be here
      assert(false);
    }
    
    value.prepared = true;
  }

  @Override
  public synchronized void commit() throws ConfigTransactionException {
    if (!prepared) {
      prepare();
    } else {
      verifyActiveTransaction(false);
    }
  
    ComponentException ce = null;
    for (Entry<Object, ConfigEntry> entry : txnConfig2Entry.entrySet()) {
      try {
        commitEntry(entry.getKey(), entry.getValue());
      } catch (ComponentException e) {
        logger.log(Level.FINE, "error during commit", e);
        if (null == ce) {
          ce = e;
        }
      }
    }
    
    coordinator.finishedTransaction(this);
    coordinator = null;
    prepared = false;
    
    if (null != ce) {
      throw new ConfigTransactionException("error during commit", ce);
    }
  }

  @Override
  public synchronized void commit(Map<Object, Object> beanReplacements) throws ConfigTransactionException {
    // TODO:
    throw new UnsupportedOperationException();
  }
  
  
  private void commitEntry(Object configBean, ConfigEntry value) {
    assert(value.prepared);
    
    if (Change.CREATE == value.change) {
      if (null == value.suggestedName) {
        value.suggestedName = ReflectionHelper.nameOf(configBean);
      }
      
      Set<Class<?>> beanContracts = new HashSet<Class<?>>();
      ReflectionHelper.annotatedWith(beanContracts, configBean, Contract.class);
      
      coordinator.manage(configBean, beanContracts, value.suggestedName, value.configuredBy);
    } else if (Change.UPDATE == value.change) {
      
      // TODO:
      throw new UnsupportedOperationException();
      
    } else if (Change.DELETE == value.change) {
      String name = ReflectionHelper.nameOf(configBean);
      
      Set<Class<?>> beanContracts = new HashSet<Class<?>>();
      ReflectionHelper.annotatedWith(beanContracts, configBean, Contract.class);
      
      coordinator.unmanage(configBean, beanContracts, name);
    } else {
      // should never be here
      assert(false);
    }
    
    value.prepared = true;
  }
  
  @Override
  public synchronized void rollback() {
    verifyActiveTransaction(false);

    for (Entry<Object, ConfigEntry> entry : txnConfig2Entry.entrySet()) {
      rollback(entry.getKey(), entry.getValue());
    }
    
    coordinator.finishedTransaction(this);
    coordinator = null;
    prepared = false;
  }

  private void rollback(Object key, ConfigEntry value) {
    if (value.prepared) {
      if (Change.CREATE == value.change) {
        
        
      } else if (Change.UPDATE == value.change) {
        
        
      } else if (Change.DELETE == value.change) {
        
        
      } else {
        assert(false);
      }
      
      value.prepared = false;
    }
  }

  private void verifyActiveTransaction(boolean mustBeOpen) {
    if (null == coordinator) {
      throw new ConfigTransactionException("no active transaction");
    }
    
    if (mustBeOpen && prepared) {
      throw new ConfigTransactionException("in prepared state");
    }
  }

  @Override
  public synchronized void created(Object createdConfigBean, String name, MultiMap<String, String> metadata) {
    // verify state & arguments
    verifyActiveTransaction(true);
    
    if (null == createdConfigBean) {
      throw new IllegalArgumentException();
    }
    
    if (txnConfig2Entry.containsKey(createdConfigBean) || coordinator.has(createdConfigBean)) {
      throw new ConfigTransactionException(createdConfigBean + " is already in txn");
    }
    
    Configured configured = ReflectionHelper.annotation(createdConfigBean, Configured.class);
    if (null == configured) {
      throw new IllegalArgumentException();
    }
    
    // track the configured inhabitant
    txnConfig2Entry.put(createdConfigBean, new ConfigEntry(Change.CREATE, name, null));
  }

  @Override
  public void deleted(Collection<?> deletedConfigBeans) {
    for (Object bean : deletedConfigBeans) {
      deleted(bean);
    }
  }

  @Override
  public synchronized void deleted(Object deleted) {
    track(deleted, Change.DELETE);
  }

  @Override
  public void updated(Collection<?> updatedConfigBeans) {
    for (Object bean : updatedConfigBeans) {
      updated(bean, null);
    }
  }

  @Override
  // TODO: do something with the PropertyChangeEvent
  public synchronized void updated(Object updated, PropertyChangeEvent event) {
    track(updated, Change.UPDATE);
  }

  private void track(Object bean, Change change) {
    // verify state & arguments
    verifyActiveTransaction(true);
    
    if (null == bean) {
      throw new IllegalArgumentException();
    }

    if (txnConfig2Entry.containsKey(bean)) {
      throw new ConfigTransactionException(bean + " is already in txn");
    }

    MultiMap<String, ConfigByInhabitant> allConfiguredByBean = coordinator.get(bean);
    if (null == allConfiguredByBean) {
      throw new ConfigTransactionException(bean + " is not being tracked");
    }
    
    // track the configured inhabitant
    txnConfig2Entry.put(bean, new ConfigEntry(change, null, allConfiguredByBean));
  }

  /**
   * override of {@link InjectionResolverQuery} to provide view of changes
   * scoped to this txn context.
   */
  // TODO: this could be made much more sophisticated
  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue(Object component, 
      Inhabitant<?> onBehalfOf,
      AnnotatedElement annotated, 
      Type genericType,
      Class<V> type)
        throws ComponentException {
    String name = annotated.getAnnotation(Inject.class).name();
    name = (null == name || name.isEmpty()) ? null : name;
    
    if (null != type.getAnnotation(Configured.class)) {
      // looking for a @Configured bean
      for (Object bean : txnConfig2Entry.keySet()) {
        Class<?> clazz = bean.getClass();
        if (type.isAssignableFrom(clazz)) {
          String name2 = ReflectionHelper.nameOf(bean);
          if (eq(name, name2)) {
            return (V) bean;
          }
        }
      }
    }

    // look for normal services by contract [and name]
    for (ConfigEntry ce : txnConfig2Entry.values()) {
      if (null != ce.configuredBy) {
        for (Entry<String, List<ConfigByInhabitant>> entry : ce.configuredBy.entrySet()) {
          if (null == entry.getKey()) {
            // TODO:
          } else {
            // TODO:
          }
        }
      }
    }

    logger.log(Level.FINE, "couldn''t resolve: {0}", type);
    
    return null;
  }

  private static boolean eq(String a, String b) {
    if(a==null && b==null)  return true;
    if(a==null || b==null)  return false;
    return a.equals(b);
  }
  
  private enum Change {
    CREATE,
    UPDATE,
    DELETE
  }
  
  private static class ConfigEntry implements InhabitantStore {
    private final Change change;
    private String suggestedName;
    private Collection<ConfigByMetaInhabitant> configuredByMeta;
    private MultiMap<String, ConfigByInhabitant> configuredBy;  // contract:name -> ConfigByInhabitant
    private boolean prepared;
    
    private ConfigEntry(Change change, String name, MultiMap<String, ConfigByInhabitant> ci) {
      this.change = change;
      this.suggestedName = name;
      this.configuredBy = ci;
    }
    
    public void setConfiguredByMeta(Collection<ConfigByMetaInhabitant> configuredByInhabitants) {
      configuredByMeta = configuredByInhabitants;
    }

    private void initConfiguredByCollection() {
      if (null == configuredBy) {
        configuredBy = new MultiMap<String, ConfigByInhabitant>();
      }
    }

    @Override
    public void add(Inhabitant<?> managed) {
      // TODO: we should ignore these because we don't want any unnamed inhabitants in the habitat
      initConfiguredByCollection();
      configuredBy.add(null, (ConfigByInhabitant) managed);
    }

    @Override
    public void addIndex(Inhabitant<?> managed, String typeName, String name) {
      initConfiguredByCollection();
      configuredBy.add(typeName + (null == name ? "" : (":" + name)), (ConfigByInhabitant) managed);
    }

    @Override
    public boolean remove(Inhabitant<?> managed) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeIndex(String index, String name) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeIndex(String index, Object serviceOrInhabitant) {
      throw new UnsupportedOperationException();
    }
  }

}
