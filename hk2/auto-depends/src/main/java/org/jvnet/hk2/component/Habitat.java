/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
package org.jvnet.hk2.component;

import com.sun.hk2.component.*;

import static com.sun.hk2.component.CompanionSeed.Registerer.createCompanion;

import static com.sun.hk2.component.InhabitantsFile.CAGE_BUILDER_KEY;

import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.ContractProvided;
import org.jvnet.hk2.annotations.FactoryFor;
import org.jvnet.hk2.component.HabitatListener.EventType;
import org.jvnet.hk2.component.InhabitantTracker.Callback;
import org.jvnet.hk2.component.concurrent.Hk2Executor;
import org.jvnet.hk2.component.concurrent.SameThreadExecutor;
import org.jvnet.hk2.component.internal.runlevel.DefaultRunLevelService;

import java.lang.annotation.Annotation;
import java.lang.annotation.AnnotationTypeMismatchException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.InvalidParameterException;
import java.util.Map.Entry;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A set of templates that constitute a world of objects.
 *
 * @author Kohsuke Kawaguchi
 * @author Jerome Dochez
 */
@SuppressWarnings("unchecked")
public class Habitat implements Injector {

    private static final Logger logger = Logger.getLogger(Habitat.class.getName());
  
    /**
     * Name to use to programmatically store default instances of a particular service.
     */
    public final String DEFAULT_NAME = "_HABITAT_DEFAULT";

    /**
     * System property tag for concurrency controls (i.e., true for multi threaded injection, inhabitant activation, etc.)
     */
    public static final String HK2_CONCURRENCY_CONTROLS = "hk2.concurrency.controls";

    /**
     * Contract type FQCN to their corresponding inhabitants.
     *
     * Can't use {@link Class} as the key so that we can create index without
     * loading contract types. Once populated upfront, it works as a read-only map.
     */
    private final MultiMap<String,NamedInhabitant> byContract; 

    /**
     * Index by {@link Inhabitant#type()}.
     */
    private final MultiMap<String,Inhabitant> byType; 

    public final ScopeInstance singletonScope;

    // TODO: toggle to use concurrency controls as the default (or after habitat is initially built)
    static final boolean CONCURRENCY_CONTROLS_DEFAULT = 
      Boolean.valueOf(System.getProperty(HK2_CONCURRENCY_CONTROLS, "false"));
    final boolean concurrencyControls;

    static ExecutorService exec = new Hk2Executor(0, 24, new ThreadFactory() {
      @Override
      public Thread newThread(Runnable runnable) {
        Thread t = new Thread(runnable);
        t.setName("hk2-thread");
        t.setDaemon(true);
        t.setPriority(Thread.MAX_PRIORITY);
        return t;
      }
    });

    // see InjectInjectionResolver
    // this manages whether PerLookup scoped inhabitants (and other applicable scopes) get released when their parents get released
    public static final boolean MANAGED_INJECTION_POINTS_ENABLED = true;

    private static final String NULL_STR_ARR[] = new String[] { null };

    /**
     * Here solely for performance optimization(s) - albeit slight
     */
    private static boolean contextualFactoriesPresentAnywhere;
    private boolean contextualFactoriesPresentHere;

    private boolean initialized;

    
    public Habitat() {
      this(null);
    }

    Habitat(Boolean concurrency_controls) {
        this.concurrencyControls = 
            (null == concurrency_controls) 
                ? CONCURRENCY_CONTROLS_DEFAULT : concurrency_controls;
        this.byContract = new MultiMap<String,NamedInhabitant>(this.concurrencyControls);
        this.byType = new MultiMap<String,Inhabitant>(this.concurrencyControls);
        this.singletonScope = new ScopeInstance("singleton", new HashMap());

        // add the set of ExecutorServices by name
        if (concurrencyControls) {
          logger.fine("concurrency controls enabled");
          // TODO: remove me (eventually)
//          System.out.println("CONCURRENCY CONTROLS ENABLED");
          addIndex(new ExistingSingletonInhabitant<ExecutorService>(ExecutorService.class,
              exec),
              ExecutorService.class.getName(), Constants.EXECUTOR_INHABITANT_INJECTION_MANAGER);
          addIndex(new ExistingSingletonInhabitant<ExecutorService>(ExecutorService.class,
              exec),
              ExecutorService.class.getName(), Constants.EXECUTOR_INHABITANT_ACTIVATOR);
        }
        addIndex(new ExistingSingletonInhabitant<ExecutorService>(ExecutorService.class,
            SameThreadExecutor.instance),
            ExecutorService.class.getName(), Constants.EXECUTOR_HABITAT_LISTENERS_AND_TRACKERS);
        
        // make the listeners available very early in lifecycle
        addHabitatListener(new SelfListener());

        // add the set of injection resolvers
        InjectInjectionResolver injectresolver = new InjectInjectionResolver(this);
        add(new ExistingSingletonInhabitant<InjectionResolver>(InjectionResolver.class,
                injectresolver));
        addIndex(new ExistingSingletonInhabitant<InjectionResolver>(InjectionResolver.class,
                injectresolver), InjectionResolver.class.getName(), null);
        add(new ExistingSingletonInhabitant<InjectionResolver>(InjectionResolver.class,
                new LeadInjectionResolver(this)));
        
        // make the habitat itself available
        Inhabitant<Habitat> habitatInh = new ExistingSingletonInhabitant<Habitat>(Habitat.class,this);
        add(habitatInh);
        addIndex(habitatInh, Injector.class.getName(), null);


        add(new ExistingSingletonInhabitant<CompanionSeed.Registerer>(CompanionSeed.Registerer.class,
                new CompanionSeed.Registerer(this)));
        add(new ExistingSingletonInhabitant<CageBuilder.Registerer>(CageBuilder.Registerer.class,
                new CageBuilder.Registerer(this)));

        InhabitantProviderInterceptor interceptor = new RunLevelInhabitantProvider(this);
        addIndex(new ExistingSingletonInhabitant<InhabitantProviderInterceptor>(
            InhabitantProviderInterceptor.class, interceptor), 
            InhabitantProviderInterceptor.class.getName(), null);
        
        // the default RunLevelService
        DefaultRunLevelService rls = new DefaultRunLevelService(this);
        ExistingSingletonInhabitant<RunLevelService> rlsI = 
          new ExistingSingletonInhabitant<RunLevelService>(RunLevelService.class, rls);
        add(rlsI);
        addIndex(rlsI, RunLevelService.class.getName(), DefaultRunLevelService.NAME);
    }
    
    /**
     * Add a habitat listener with no contract-level filtering.
     * This API is primarily intended for internal cases within
     * Hk2.
     * 
     * The listener with no contract-level filtering will be 
     * called for all change events within the habitat pertaining
     * to inhabitants.
     *
     * @see {@link #addHabitatListener(HabitatListener, String...)}
     *      is recommended for most cases
     * 
     * @param listener
     *      The habitat Listener to be added
     * 
     * @since 3.1
     */
    public void addHabitatListener(HabitatListener listener) {
        addHabitatListener(listener, DEFAULT_NAME);
    }
    
    /**
     * Add a habitat listener with contract-level filtering.
     * 
     * The listener will be called based on the set of contract
     * filters provided.
     *
     * @param listener
     *      The habitat Listener to be added
     * @param typeNames
     *      The contracts to filter on; this should be non-null
     * 
     * @since 3.1
     */
    public void addHabitatListener(HabitatListener listener, String... typeNames) {
      if (null == typeNames || 0 == typeNames.length) throw new IllegalArgumentException();
      addHabitatListener(listener, new HashSet<String>(Arrays.asList(typeNames)));
    }
    
    protected void addHabitatListener(HabitatListener listener, Set<String> typeNames) {
        ExistingSingletonInhabitant<HabitatListener> inhabitant = 
          new ExistingSingletonInhabitant<HabitatListener>(HabitatListener.class,
              listener, metaData(typeNames));
        add(inhabitant);
  
        // track listeners by type - support of filtered notification
        for (String contract : typeNames) {
          ListenersByTypeInhabitant sameListeners; 
          synchronized (byContract) {
              sameListeners = (ListenersByTypeInhabitant)
                      getInhabitantByContract(ListenersByTypeInhabitant.class.getName(),
                      contract);
              if (null == sameListeners) {
                  sameListeners = new ListenersByTypeInhabitant(contract);
                  addIndex(sameListeners, ListenersByTypeInhabitant.class.getName(),
                      contract,
                      false); // don't send notifications for this type (no particular reason really)
              }
          }
          sameListeners.add(listener);
        }
    }

    /**
     * Remove a habitat listener.
     * 
     * @param listener
     *      The habitat Listener to be removed
     * @param typeNames
     *      The contracts to filter on
     * 
     * @return true; if the listener was indeed removed
     * 
     * @since 3.1
     */
    public boolean removeHabitatListener(HabitatListener listener) {
      List<Inhabitant> list = byType._get(HabitatListener.class.getName());
      List<Inhabitant> releaseList = new ArrayList<Inhabitant>();
      Iterator<Inhabitant> iter = list.iterator();
      while (iter.hasNext()) {
          Inhabitant existing = iter.next();
          if (existing.get() == listener) {
              releaseList.add(existing);
              if (concurrencyControls) {
                  list.remove(existing);
              } else {
                  iter.remove();
              }
          }
      }

      // need to release the per-listener-contract entries too
      for (Inhabitant released : releaseList) {
          MultiMap<String, String> metadata = released.metadata();
          if (null != metadata) {
              List<String> filters = metadata.get(Constants.OBJECTCLASS);
              for (String contract : filters) {
                  ListenersByTypeInhabitant sameListeners; 
                  synchronized (byContract) {
                      sameListeners = (ListenersByTypeInhabitant)
                              getInhabitantByContract(ListenersByTypeInhabitant.class.getName(),
                              contract);
                  }
                  sameListeners.remove(listener);
              }                    
          }
          
          released.release();
          notify(released, EventType.INHABITANT_REMOVED, null, released);
      }
      
      return !releaseList.isEmpty();
    }

    /**
     * Convert a set of type names into OBJECTCLASS keys within metadata
     */
    private MultiMap<String, String> metaData(Set<String> typeNames) {
        if (null == typeNames) {
          return null;
        }
        
        MultiMap<String, String> metadata = new MultiMap<String, String>();
        for (String typeName : typeNames) {
          metadata.add(Constants.OBJECTCLASS, typeName);
        }
        
        return metadata;
    }
    
    /**
     * Registers a dependency on the inhabitant with the given tracker context.
     * <p>
     * Once the criteria is met, any callback provided is called.  This callback may
     * occur asynchronously from the thread initiating the event.
     * 
     * @param itc
     *          The tracking criteria.
     * @param callback
     *          Optionally the callback.
     * @return
     *          The tracker
     * @throws ComponentException
     * 
     * @since 3.1
     */
    public InhabitantTracker track(InhabitantTrackerContext itc,
            Callback callback) throws ComponentException {
        if (null == itc) throw new IllegalArgumentException();
        return new InhabitantTrackerImpl(this, itc, callback);
    }
  
    /**
     * Returns a future that can be checked asynchronously, and multiple times.
     * <p>
     * <b>Implementation Note:</b> The Future that is returned does not behave in
     * the traditional sense in that it is NOT directly submitted to an
     * ExecutorService.  Each call to get() or get(timeout) may result in a 
     * [re]submission to an internally held executor.  This means that a
     * call to get(...) may return a tracker, and a subsequent call to get(...) may
     * return null, or vice versa.  This is true until the underlying tracker is
     * released at which point a tracker is no longer usable.
     * 
     * @param itc
     *          The tracking criteria.
     * @return
     *          The tracker
     * @throws ComponentException
     * 
     * @since 3.1
     */
    public Future<InhabitantTracker> trackFuture(InhabitantTrackerContext itc) throws ComponentException {
        if (null == itc) throw new IllegalArgumentException();
        return new InhabitantTrackerJob(this, itc);
    }
  
    /*
        Why initialize/processInhabitantDecorations didn't work:

        when a new CageBuilder comes into the habitat, it needs to build cages for all existing components.
        when a caged component comes into the habitat, it checks existing cage builders in the habitat.

        Now, when  a cage builder and a component are both initialized first and then processInhabitantDecorations
        runs for both of them later, then you end up creating a cage twice, because the builder think it got into
        habitat after than the component, and the component think it got into habitat after the builder.
     */

    /**
     * Removes all inhabitants for a particular type
     *
     * @param type of the component
     * 
     * @return true if any inhabitants were removed
     */
    public boolean removeAllByType(Class<?> type) {
        boolean removed = false;
        String name = type.getName();

        // remove all existing inhabitants by type
        List<Inhabitant> list = new ArrayList<Inhabitant>(byType.get(name));
        for (Inhabitant existing : list) {
            removed |= remove(existing);
        }
        
        return removed;
    }

    /**
     * Adds a new inhabitant.
     *
     * <p>
     * See {@link Inhabitants} for typical ways to create {@link Inhabitant}s.
     */
    public void add(final Inhabitant<?> i) {
        String name = i.typeName();
        byType.add(name,i);

        // for each companion, create an inhabitant that goes with the lead and hook them up
        List<Inhabitant> companions=null;
        for(Inhabitant<?> c : getInhabitantsByAnnotation(CompanionSeed.class,name)) {
            if (companions==null) {
                companions = new ArrayList<Inhabitant>();
            }
            companions.add(createCompanion(this,i,c));
        }
        i.setCompanions(companions);

        String cageBuilderName = i.metadata().getOne(CAGE_BUILDER_KEY);
        if(cageBuilderName!=null) {
            Inhabitant cageBuilder = byType.getOne(cageBuilderName);
            if (cageBuilder!=null) {
                ((CageBuilder)cageBuilder.get()).onEntered(i);
            }
            // if cageBuilder==null, we can't cage this component now, but
            // we'll do that when cageBuilder comes into the habitat.
            // that happens because every CageBuilder implementations are caged by
            // CageBuilder.Registerer.
        }
        
        notify(i, EventType.INHABITANT_ADDED, null, null);
    }

    /**
     * Adds a new index to look up the given inhabitant.
     *
     * @param index
     *      Primary index name, such as contract FQCN.
     * @param name
     *      Name that identifies the inhabitant among other inhabitants
     *      in the same index. Can be null for unnamed inhabitants.
     */
    public void addIndex(Inhabitant<?> i, String index, String name) {
      addIndex(i, index, name, true);
    }

    protected void addIndex(Inhabitant<?> i, String index, String name, boolean notify) {
      byContract.add(index,new NamedInhabitant(name,i));
      
      if (notify) {
        notify(i, EventType.INHABITANT_INDEX_ADDED, index, name, null, null);
      }
    }

    protected static Long getServiceRanking(Inhabitant<?> i, boolean wantNonNull) {
      MultiMap<String, String> meta = i.metadata();
      String sr = meta.getOne(Constants.SERVICE_RANKING);
      if (null == sr) {
        return (wantNonNull) ? 0L : null;
      }
      return Long.valueOf(sr);
    }
    
    /**
     * Removes an inhabitant
     *
     * @param inhabitant
     *      inhabitant to be removed
     */
    public boolean remove(Inhabitant<?> inhabitant) {
        String name = inhabitant.typeName();
        if (byType.remove(name, inhabitant)) {
            notify(inhabitant, EventType.INHABITANT_REMOVED, null, null);
            inhabitant.release();
            return true;
        }
        return false;
    }

    /**
     * Removes a NamedInhabitant for a specific contract
     *
     * @param index contract name
     * @param name instance name
     * @return true if the removal was successful
     */
    public boolean removeIndex(String index, String name) {
        boolean removed = false;
        final List<NamedInhabitant> contracted = byContract._get(index);
        if (!contracted.isEmpty()) {
            Iterator<NamedInhabitant> iter = contracted.iterator();
            while (iter.hasNext()) {
                NamedInhabitant i = iter.next();
                
                if ((i.name == null && name == null) ||
                        (i.name != null && i.name.equals(name))) {
                    if (concurrencyControls) {
                        removed = contracted.remove(i);
                        assert(removed);
                    } else {
                        iter.remove();
                    }
                    removed = true;
                    notify(i.inhabitant, EventType.INHABITANT_INDEX_REMOVED,
                        index, name, null, null);

                    // remember to remove the components stored under its type
                    remove(i.inhabitant);
                }
            }
        }

        return removed;
    }

    /**
     *  Removes a Contracted service
     *
     * @param index the contract name
     * @param serviceOrInhabitant the service instance, or an Inhabitant instance
     */
    public boolean removeIndex(String index, Object serviceOrInhabitant) {
        boolean removed = false;
        if (byContract.containsKey(index)) {
             List<NamedInhabitant> contracted = byContract._get(index);
             Iterator<NamedInhabitant> iter = contracted.iterator();
             while (iter.hasNext()) {
                 NamedInhabitant i = iter.next();
                 if (matches(i.inhabitant, serviceOrInhabitant)) {
                     if (concurrencyControls) {
                         removed = contracted.remove(i);
                         assert(removed);
                     } else {
                         iter.remove();
                     }
                     removed = true;
                     notify(i.inhabitant, EventType.INHABITANT_INDEX_REMOVED,
                         index, null, service(serviceOrInhabitant), null);

                     // remember to remove the components stored under its type
                     remove(i.inhabitant);
                 }
             }
         }
        
         return removed;
    }

    protected boolean matches(Inhabitant<?> inhabitant, Object serviceOrInhabitant) {
      boolean matches;
      if (serviceOrInhabitant instanceof Inhabitant) {
        matches = (serviceOrInhabitant == inhabitant);
      } else {
        // call to isInstantiated is necessary to avoid loading of services that are not yet loaded.
        matches = inhabitant.isInstantiated() && inhabitant.get()==serviceOrInhabitant;
      }
      return matches;
    }
    
    protected Object service(Object serviceOrInhabitant) {
      return (serviceOrInhabitant instanceof Inhabitant && ((Inhabitant)serviceOrInhabitant).isInstantiated()) 
        ? ((Inhabitant)serviceOrInhabitant).get() : serviceOrInhabitant;
    }
    
    protected static interface NotifyCall {
      boolean inhabitantChanged(HabitatListener listener);
    }
    
    /**
     * Trigger a notification that an inhabitant has changed.
     * 
     * @param inhabitant the inhabitant that has changed
     * @param contracts the contracts associated with the inhabitant
     */
    public void notifyInhabitantChanged(Inhabitant<?> inhabitant,
          String... contracts) {
      if (null == contracts || 0 == contracts.length) {
        // generic inhabitant modification (only general listeners will get notification)
        notify(inhabitant, EventType.INHABITANT_MODIFIED, null, null);
      } else {
        // all scoped-listeners will get modification
        for (String contract : contracts) {
          notify(inhabitant, EventType.INHABITANT_MODIFIED, contract, null);
        }
      }
    }
    
    /**
     * FOR INTERNAL USE ONLY
     */
    public synchronized void initialized() {
      if (initialized) throw new RuntimeException("already initialized");
      initialized = true;
      
      contextualFactoriesPresentHere = (null != getInhabitantByContract(ContextualFactory.class.getName()));
      if (contextualFactoriesPresentHere) {
        contextualFactoriesPresentAnywhere = true;
      }
      
      notify(null, EventType.HABITAT_INITIALIZED, null, null);
    }
    
    public boolean isInitialized() {
      return initialized;
    }
    
    /**
     * FOR INTERNAL USE
     */
    public static boolean isContextualFactoriesPresentAnywhere() {
      return contextualFactoriesPresentAnywhere;
    }
    
    /**
     * FOR INTERNAL USE
     */
    public boolean isContextualFactoriesPresent() {
      return contextualFactoriesPresentHere;
    }
    
    protected void notify(final Inhabitant<?> inhabitant,
        final EventType event,
        final String index,
        final Inhabitant<HabitatListener> extraListenerToBeNotified) {
      NotifyCall innerCall = new NotifyCall() {
        public boolean inhabitantChanged(HabitatListener listener) {
          return listener.inhabitantChanged(event, Habitat.this, inhabitant);
        }
      };
      notify(innerCall, inhabitant, event, index, extraListenerToBeNotified);
    }

    protected void notify(final Inhabitant<?> inhabitant,
        final EventType event,
        final String index,
        final String name,
        final Object service,
        final Inhabitant<HabitatListener> extraListenerToBeNotified) {
      NotifyCall innerCall = new NotifyCall() {
        public boolean inhabitantChanged(HabitatListener listener) {
          return listener.inhabitantIndexChanged(event, Habitat.this, inhabitant,
              index, name, service);
        }
      };
      notify(innerCall, inhabitant, event, index, extraListenerToBeNotified);
    }

    protected void notify(final NotifyCall innerCall,
        final Inhabitant<?> inhabitant,
        final EventType event,
        final String index,
        final Inhabitant<HabitatListener> extraListenerToBeNotified) {
      // do scoped listeners first
      if (null != index) {
        doNotify(innerCall, inhabitant, event, index, null);
      }
      
      // do general listeners next
      doNotify(innerCall, inhabitant, event, DEFAULT_NAME, extraListenerToBeNotified);
    }
      
    private void doNotify(final NotifyCall innerCall,
        final Inhabitant<?> inhabitant,
        final EventType event,
        final String index,
        final Inhabitant<HabitatListener> extraListenerToBeNotified) {
      final ListenersByTypeInhabitant sameListeners = 
          (ListenersByTypeInhabitant)getInhabitantByContract(
              ListenersByTypeInhabitant.class.getName(),
              index);
      if (null != sameListeners && !sameListeners.listeners.isEmpty()) {
        getComponent(ExecutorService.class, Constants.EXECUTOR_HABITAT_LISTENERS_AND_TRACKERS)
            .execute(new Runnable() {
          public void run() {
            Iterator<HabitatListener> iter = sameListeners.listeners.iterator();
            while (iter.hasNext()) {
              Object entry = iter.next();
              HabitatListener listener = 
                (entry instanceof HabitatListener) ? 
                    (HabitatListener)entry :
                    (HabitatListener)((Inhabitant)entry).get();
              try {
                boolean keepMe = innerCall.inhabitantChanged(listener);
                if (!keepMe) {
                  removeHabitatListener(listener);
                }
              } catch (Exception e){
                // don't percolate the exception since it may negatively impact processing
                logger.log(Level.WARNING, "exception caught from listener: ", e);
              }
            }
            
            // we take the extraListenerToBeNotified because
            //  (a) we want to have all notifications in the executor, and
            //  (b) it might trigger an exception that we want to trap
            if (null != extraListenerToBeNotified) {
              extraListenerToBeNotified.get().inhabitantChanged(event, Habitat.this, extraListenerToBeNotified);
            }
          }
        });
      }
    }

    
    /**
     * Checks if the given type is a contract interface that has some implementations in this {@link Habitat}.
     *
     * <p>
     * There are two ways for a type to be marked as a contract.
     * Either it has {@link Contract}, or it's marked by {@link ContractProvided} from the implementation.
     *
     * <p>
     * Note that just having {@link Contract} is not enough to make this method return true.
     * It can still return false if the contract has no implementation in this habitat.
     *
     * <p>
     * This method is useful during the injection to determine what lookup to perform,
     * and it handles the case correctly when the type is marked as a contract by {@link ContractProvided}.
     */
    public boolean isContract(Class<?> type) {
        return byContract.containsKey(type.getName());
    }

    public boolean isContract(String fullyQualifiedClassName) {
        return byContract.containsKey(fullyQualifiedClassName);
    }

    /**
     * Gets all the inhabitants registered under the given {@link Contract}.
     * This is an example of heterogeneous type-safe container.
     * 
     * @return
     *      can be empty but never null.
     */
    public <T> Collection<T> getAllByContract(Class<T> contractType) {
      return getAllByContract(contractType.getName());
    }
    
    public <T> Collection<T> getAllByContract(String contractType) {
        final List<NamedInhabitant> l = byContract.get(contractType);
        return new AbstractList<T>() {
            public T get(int index) {
                return (T)l.get(index).inhabitant.get();
            }

            public int size() {
                return l.size();
            }
        };
    }

    public Collection<Inhabitant<?>> getAllInhabitantsByContract(String contractType) {
      final List<NamedInhabitant> l = byContract.get(contractType);
      return new AbstractList<Inhabitant<?>>() {
          public Inhabitant<?> get(int index) {
              return l.get(index).inhabitant;
          }

          public int size() {
              return l.size();
          }
      };
    }

    /**
     * Gets the object of the given type.
     *
     * @return
     *      can be empty but never null.
     */
    public <T> Collection<T> getAllByType(Class<T> implType) {
        final List<Inhabitant> l = byType.get(implType.getName());
        return new AbstractList<T>() {
            public T get(int index) {
                return (T)l.get(index).get();
            }

            public int size() {
                return l.size();
            }
        };
    }

    /**
     * Gets all matching inhabitants given the type.
     *
     * @return
     *      can be empty but never null.
     */
    public <T> Collection<Inhabitant> getAllInhabitantsByType(Class<T> implType) {
      final List<Inhabitant> l = byType.get(implType.getName());
      return l;
    }

    /**
     * Add an already instantiated component to this manager. The component has
     * been instantiated by external code, however dependency injection, PostConstruct
     * invocation and dependency extraction will be performed on this instance before
     * it is store in the relevant scope's resource manager.
     *
     * @param name name of the component, could be default name
     * @param component component instance
     * @throws ComponentException if the passed object is not an HK2 component or
     * injection/extraction failed.
     */
    // TODO: mutating Habitat after it's created poses synchronization issue
    public <T> void addComponent(String name, T component) throws ComponentException {
        add(new ExistingSingletonInhabitant<T>(component));
    }

    /**
     * Obtains a reference to the component inside the manager.
     *
     * <p>
     * This is the "new Foo()" equivalent in the IoC world.
     *
     * <p>
     * Depending on the {@link Scope} of the component, a new instance
     * might be created, or an existing instance might be returned.
     *
     * @return
     *      non-null.
     * @throws ComponentException
     *      If failed to obtain a requested instance.
     *      In practice, failure only happens when we try to create a
     *      new instance of the component.
     */
    public <T> T getComponent(Class<T> clazz) throws ComponentException {
        if(isContract(clazz))
            return getByContract(clazz);
        else
            return getByType(clazz);
    }

    /**
     * Loads a component that implements the given contract and has the given name.
     *
     * @param name
     *      can be null, in which case it'll only match to the unnamed component.
     * @return
     *      null if no such servce exists.
     */
    public <T> T getComponent(Class<T> contract, String name) throws ComponentException {
        if (name!=null && name.length()==0)
            name=null;
        Inhabitant i = getInhabitant(contract, name);
        if(i!=null)
            try {
                return contract.cast(i.get());
            } catch (ClassCastException e) {
                logger.severe("ClassCastException between contract " + contract + " and service " + i.get());
                logger.severe("Contract class loader " + contract.getClassLoader());
                logger.severe("Service class loader " + i.get().getClass().getClassLoader());
                throw e;
            }
        else
            return null;
    }

    public Object getComponent(String fullQualifiedName, String name) {
        if (name!=null && name.length()==0) {
            name=null;
        }
        Inhabitant i = isContract(fullQualifiedName)?
                getInhabitantByContract(fullQualifiedName, name):getInhabitantByType(fullQualifiedName);
        return i==null?null:i.get();
    }

    /**
     * Gets a lazy reference to the component.
     *
     * <p>
     * This method defers the actual instantiation of the component
     * until {@link Inhabitant#get()} is invoked.
     *
     * @return null
     *      if no such component is found.
     */
    public <T> Inhabitant<T> getInhabitant(Class<T> contract, String name) throws ComponentException {
        if (name!=null && name.length()==0) {
          name=null;
        }
        return getInhabitantByContract(contract.getName(), name);
    }

    /**
     * Gets a lazy reference to the component.
     * 
     * <p>
     * This method defers the actual instantiation of the component
     * until {@link Inhabitant#get()} is invoked.
     *
     * @return null
     *      if no such component is found.
     */
    public <T> Inhabitant<T> getInhabitantByType(Class<T> implType) {
        return (Inhabitant<T>)getInhabitantByType(implType.getName());
    }

    public Inhabitant<?> getInhabitantByType(String fullyQualifiedClassName) {
        List<Inhabitant> list = byType.get(fullyQualifiedClassName);
        if(list.isEmpty())  return null;
        return list.get(0);
    }

    /**
     * Gets the inhabitant that has the given contract annotation and the given name.
     *
     * <p>
     * This method defers the actual instantiation of the component
     * until {@link Inhabitant#get()} is invoked.
     *
     * @return null
     *      if no such component is found.
     */
    public Inhabitant<?> getInhabitantByAnnotation(Class<? extends Annotation> contract, String name) throws ComponentException {
        return getInhabitantByContract(contract.getName(), name);
    }

    /**
     * Gets all the inhabitants that has the given contract.
     */
    public <T> Collection<Inhabitant<? extends T>> getInhabitants(Class<T> contract) throws ComponentException {
        final List<NamedInhabitant> l = byContract.get(contract.getName());
        return new AbstractList<Inhabitant<? extends T>>() {
            public Inhabitant<? extends T> get(int index) {
                return l.get(index).inhabitant;
            }

            public int size() {
                return l.size();
            }
        };
    }

    @Override
    public <T> T injects(final T object) {
        Creator<T> c = new ConstructorCreator<T>((Class<T>) object.getClass(), this, null) {
            @Override
            public T create(Inhabitant onBehalfOf) throws ComponentException {
                    return object;
                }
        };
        return c.get();
    }

    /**
     * Creates a new value object which will be instantiated using the empty parameter
     * constructor. All declared dependencies of this value object will be injected as
     * well as normal component lifecycle.
     *
     * Instances created by this method will not be added to the habitat and will not be
     * managed or lookup up by the habitat.
     *
     * @param type  requested value object type
     * @param <T>  value object type
     * @return  value object instantiated and injected.
     */
    public <T> T newValueObject(Class<T> type) {

        Creator<T> c = new ConstructorCreator<T>(type, this, null) {
            @Override
            public T create(Inhabitant onBehalfOf) throws ComponentException {
                try {
                    return type.newInstance();
                } catch (InstantiationException e) {
                    throw new ComponentException("Failed to create "+type,e);
                } catch (IllegalAccessException e) {
                    try {
                      Constructor<T> ctor = type.getDeclaredConstructor(null);
                      ctor.setAccessible(true);
                      return ctor.newInstance(null);
                    } catch (Exception e1) {
                      // ignore
                    }
                    throw new ComponentException("Failed to create "+type,e);
                } catch (LinkageError e) {
                    throw new ComponentException("Failed to create "+type,e);
                } catch (RuntimeException e) {
                    throw new ComponentException("Failed to create "+type,e);
                }
            }
        };
        return c.get();
    }

    /**
     * Gets all the inhabitants that has the given implementation type.
     */
    public <T> Collection<Inhabitant<T>> getInhabitantsByType(Class<T> implType) throws ComponentException {
        return (Collection)byType.get(implType.getName());
    }

    /**
     * Gets all the inhabitants that has the given implementation type name.
     */
    public Collection<Inhabitant<?>> getInhabitantsByType(String fullyQualifiedClassName) {
        return (Collection)byType.get(fullyQualifiedClassName);
    }

    /**
     * Get the first inhabitant by contract
     * 
     * @param fullyQualifiedClassName
     * @return
     */
    public Inhabitant<?> getInhabitantByContract(String typeName) {
      final List<NamedInhabitant> services = byContract._get(typeName);
      return (null == services || services.isEmpty()) ? null : services.get(0).inhabitant;
    }
      
    public Collection<Inhabitant<?>> getInhabitantsByContract(String fullyQualifiedClassName) {
        final List<NamedInhabitant> services = byContract.get(fullyQualifiedClassName);
        return new AbstractList<Inhabitant<?>>() {
            public Inhabitant<?> get(int index) {
                return services.get(index).inhabitant;
            }

            public int size() {
                return services.size();
            }
        };
    }

    private class MultiMapIterator implements Iterator {
        final Iterator<Entry<String, List<NamedInhabitant>>> itr;
        MultiMapIterator(MultiMap map) {
            itr = map.entrySet().iterator();
        }

        public boolean hasNext() {
            return itr.hasNext();
        }

        public Object next() {
            return itr.next().getKey();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public Iterator<String> getAllContracts() {
        return new MultiMapIterator(byContract);
    }

    public Iterator<String> getAllTypes() {
        return new MultiMapIterator(byType);
    }

    public Inhabitant getInhabitantByContract(String fullyQualifiedName, String name) {
        if (null == name || name.isEmpty() || !name.contains(",")) {
            for (NamedInhabitant i : byContract.get(fullyQualifiedName)) {
                if (eq(i.name, name))
                    return i.inhabitant;
            }
        } else {
            String names[];
            if (null == name || name.equals("")) {
              names = NULL_STR_ARR;
            } else {
              names = name.split(",");
            }
            
            List<NamedInhabitant> list = byContract.get(fullyQualifiedName);
            for (String sname : names) {
                sname = (null == sname || sname.equals("*")) ? null : sname;
                for (NamedInhabitant i : list) {
                    String iname = i.name;
                    if (eq(iname, sname)) {
                        return i.inhabitant;
                    }
                }
            }
        }
        
        return null;
    }

    /**
     * Gets all the inhabitants that has the given contract and the given name
     *
     * <p>
     * This method defers the actual instantiation of the component
     * until {@link Inhabitant#get()} is invoked.
     *
     * @return
     *      Can be empty but never null.
     */
    public <T> Iterable<Inhabitant<? extends T>> getInhabitants(Class<T> contract, String name) throws ComponentException {
        if (name!=null && name.length()==0) {
          name=null;
        }
        return _getInhabitants(contract,name);
    }

    /**
     * Gets all the inhabitants that has the given contract annotation and the given name.
     *
     * <p>
     * This method defers the actual instantiation of the component
     * until {@link Inhabitant#get()} is invoked.
     *
     * @return
     *      Can be empty but never null.
     */
    public Iterable<Inhabitant<?>> getInhabitantsByAnnotation(Class<? extends Annotation> contract, String name) throws ComponentException {
        return _getInhabitants(contract, name);
    }

    // intentionally not generified so that the getInhabitants methods can choose the right signature w/o error
    private Iterable _getInhabitants(final Class contract, final String name) {
        return new Iterable<Inhabitant>() {
            private final Iterable<NamedInhabitant> base = byContract.get(contract.getName());

            public Iterator<Inhabitant> iterator() {
                return new Iterator<Inhabitant>() {
                    private Inhabitant i = null;
                    private final Iterator<NamedInhabitant> itr = base.iterator();

                    public boolean hasNext() {
                        while(i==null && itr.hasNext()) {
                            NamedInhabitant ni = itr.next();
                            if(eq(ni.name,name))
                                i = ni.inhabitant;
                        }
                        return i!=null;
                    }

                    public Inhabitant next() {
                        if(i==null)
                            throw new NoSuchElementException();
                        Inhabitant r = i;
                        i = null;
                        return r;
                    }

                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    private static boolean eq(String a, String b) {
        if(a==null && b==null)  return true;
        if(a==null || b==null)  return false;
        return a.equals(b);
    }

    /**
     * Gets the object of the given type.
     *
     * @return null if not found.
     */
    public <T> T getByType(Class<T> implType) {
        return getBy(implType, byType);
    }

    /**
     * Gets the object that has the given contract.
     *
     * <p>
     * If there are more than one of them, this method arbitrarily return
     * one of them. 
     */
    public <T> T getByContract(Class<T> contractType) {
        List<NamedInhabitant> l = byContract.get(contractType.getName());
        if(l.isEmpty())     return null;
        else                return (T)l.get(0).inhabitant.get();
    }

    private <T> T getBy(Class<T> implType, MultiMap<String, Inhabitant> index) {
        List<Inhabitant> l = index.get(implType.getName());
        if(l.isEmpty())     return null;
        else                return (T)l.get(0).get();
    }

    /**
     * Releases all the components.
     * Should be called for orderly shut-down of the system.
     * 
     * TODO: more javadoc needed
     */
    public void release() {
        // TODO: synchronization story?
        for (Entry<String, List<Inhabitant>> e : byType.entrySet()) {
            for (Inhabitant i : e.getValue()) {
                i.release();
                notify(i, EventType.INHABITANT_REMOVED, null, null);
            }
        }
    }

    private static final class NamedInhabitant {
        /**
         * Name. Can be null.
         */
        final String name;
        final Inhabitant inhabitant;

        public NamedInhabitant(String name, Inhabitant inhabitant) {
            this.name = name;
            this.inhabitant = inhabitant;
        }
    }
    
    final class ListenersByTypeInhabitant 
          extends ExistingSingletonInhabitant {
      private final String name;
      private final CopyOnWriteArrayList<HabitatListener> listeners =
          new CopyOnWriteArrayList<HabitatListener>();
      private volatile boolean released;

      protected ListenersByTypeInhabitant(String name) {
        super(new ExistingSingletonInhabitant(ListenersByTypeInhabitant.class, null));
        this.name = name;
      }

      @Override
      public String toString() {
        return getClass().getSimpleName() + "(" + name + ")";
      } 

      public void add(HabitatListener listener) {
        listeners.add(0, listener);
      }
      
      public boolean remove(HabitatListener listener) {
        return listeners.remove(listener);
      }

      public int size() {
        return listeners.size();
      }

      @Override
      public void release() {
        if (!released) {
          released = true;
          
          for (HabitatListener listener : listeners) {
            removeHabitatListener(listener);
          }
        }
        
        super.release();
      }
    }

    // TODO: this can now be performed more systematically by having an InhabitantProvider implementation
    private static final class SelfListener implements HabitatListener {
      public boolean inhabitantChanged(EventType eventType, Habitat habitat,
          Inhabitant<?> inhabitant) {
        return true;
      }

      public boolean inhabitantIndexChanged(EventType eventType, Habitat habitat,
          Inhabitant<?> i, String index, String name, Object service) {

        // for each FactoryFor component, insert inhabitant for components created by the factory
        if (index.equals(FactoryFor.class.getName())) {
            FactoryFor ff = i.type().getAnnotation(FactoryFor.class);

            try {
              Class<?> targetClasses[] = ff.value();
              if (null != targetClasses && targetClasses.length > 0) {
                for (Class<?> altClass : targetClasses) {
                  FactoryCreator target = new FactoryCreator(altClass, i, habitat, MultiMap.<String,String>emptyMap());
                  habitat.add(target);
                  habitat.addIndex(target, altClass.getName(), null);
                }
              }
            } catch (AnnotationTypeMismatchException e) {
              logger.log(Level.WARNING, "annotation error", e);
            }
        }
        
//        System.out.println("Habitat Index Changed: " + eventType + "; " + i + "; index=" + index + "; name=" + name);

        return true;
      }
    }

}
