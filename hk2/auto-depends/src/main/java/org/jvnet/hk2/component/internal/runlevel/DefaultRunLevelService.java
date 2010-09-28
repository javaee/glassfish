/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2010 Sun Microsystems, Inc. All rights reserved.
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
package org.jvnet.hk2.component.internal.runlevel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import org.jvnet.hk2.annotations.RunLevel;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.HabitatListener;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.InhabitantListener;
import org.jvnet.hk2.component.RunLevelListener;
import org.jvnet.hk2.component.RunLevelService;
import org.jvnet.hk2.component.RunLevelState;
import org.jvnet.hk2.component.ServiceContext;

import com.sun.hk2.component.AbstractInhabitantImpl;
import com.sun.hk2.component.LazyInhabitant;
import com.sun.hk2.component.RunLevelInhabitant;

/**
 * The default RunLevelService implementation for Hk2.
 * 
 * Here is a brief example of the behavior of this service:<br>
 * Assume ServiceA, ServiceB, and ServiceC are all in the same RunLevel X and the
 * dependencies are:
 * <p>
 * ServiceA -> ServiceB -> ServiceC
 * <p>
 * When the DefaultRunLevelService is asked to proceedTo(X), the
 * expected start order is : ServiceC, ServiceB, ServiceA, and thee xpected shutdown /
 * PreDestroy order: ServiceA, ServiceB, ServiceC
 * <p>
 * RunLevel-annotated services correspond to {@link RunLevelInhabitant}'s and they  
 * hook into the PostConstruct activation sequence to record the activation order
 * of inhabitants within each RunLevel.
 * <p>
 * Note that no model of dependencies between services are kept in the habitat to
 * make the implementation work.  Any inhabitant in RunLevel X is arbitrarily picked
 * to start with upon activation, and {@link Inhabitant#get()}() is issued.
 * <p>
 * Consider the cases of activation ordering:
 * <p>
 * Case 1: A, B, then C by RLS. get ServiceA (called by RLS) Start ServiceA: get
 * ServiceB Start ServiceB: get ServiceC Start ServiceC wire ServiceC
 * PostConstruct ServiceC wire ServiceB PostConstruct ServiceB wire ServiceA
 * PostConstruct ServiceA get ServiceB (called by RLS) get ServiceC (called by
 * RLS)
 * <p>
 * Case 2: B, C, then A by RLS. get ServiceB (called by RLS) Start ServiceB: get
 * ServiceC Start ServiceC wire ServiceC PostConstruct ServiceC wire ServiceB
 * PostConstruct ServiceB get ServiceC (called by RLS) get ServiceA (called by
 * RLS) Start ServiceA: get ServiceB wire ServiceA PostConstruct ServiceA
 * <p>
 * Case 3: B, A, then C by RLS. get ServiceB (called by RLS) Start ServiceB: get
 * ServiceC Start ServiceC wire ServiceC PostConstruct ServiceC wire ServiceB
 * PostConstruct ServiceB get ServiceA (called by RLS) Start ServiceA: get
 * ServiceB wire ServiceA PostConstruct ServiceA get ServiceC (called by RLS)
 * <p>
 * Case 4: C, B, then A by RLS. get ServiceC (called by RLS) Start ServiceC:
 * wire ServiceC PostConstruct ServiceC get ServiceB (called by RLS) Start
 * ServiceB: get ServiceC wire ServiceB PostConstruct ServiceB get ServiceA
 * (called by RLS) Start ServiceA: get ServiceB wire ServiceA PostConstruct
 * ServiceA get ServiceA (called by RLS)
 * <p>
 * You can see that the order is always correct.
 * <p>
 * ~~~
 * <p>
 * Note that the implementation performs some level of constraint checking
 * during injection. For example,
 * <p>
 * - It is an error to have a RunLevel-annotated service at RunLevel X to depend
 * on (i.e., be injected with) a RunLevel-annotated service at RunLevel Y when Y
 * > X.
 * <p>
 * - It is an error to have a non-RunLevel-annotated service to depend on a
 * RunLevel-annotated service at any RunLevel.
 * <p>
 * Note that the implementation does not handle Holder and Collection injection
 * constraint validations.
 * <p>
 * ~~~
 * <p>
 * The implementation will automatically proceedTo(-1) after the habitat has
 * been initialized.
 * <p>
 * Note that all RunLevel values less than -1 will be ignored.
 * <p>
 * ~~~
 * <p>
 * The implementation is written to support two modes of operation, asynchronous
 * / threaded, and synchronous / single threaded.  The DefaultRunLevelService
 * implementation mode is pre-configured to be synchronous.  The
 * DefaultRunLevelService is thread safe.
 * <p>
 * In the synchronous mode, calls can be made to proceedTo() to "interrupt"
 * processing of any currently executing proceedTo() operation.  This might occur:
 * in another thread, in the {@link RunLevelListener} handlers, or in a
 * {@link RunLevel} annotated service's {@link PostConstruct} method call.
 * <p>
 * Note, however, that even in synchronous mode the proceedTo() operation may exhibit
 * asynchronous behavior.  This is the case when the caller has two threads calling
 * proceedTo(), where the second thread is canceling the operation of the first (perhaps
 * due to timeout of a service's PostConstruct, etc.).  In this case, an interrupt will
 * be sent to the first running thread to cancel the previous operation, and proceedTo
 * the run level from the second thread's request.  This presumes that the first thread
 * is capable of being interrupted.  In such a situation, the second proceedTo() call
 * returns immediately and the first proceedTo() is interrupted to continue to the 
 * new runLevel requested from the second thread's interrupt.
 * <p>
 * proceedTo invocations from a {@link PostConstruct} callback are discouraged.  Consider
 * using {@link RunLevelListener} instead.
 * <p>
 * <b>Important Note:</b><br>
 * The proceedTo() method will throw unchecked exceptions of type 
 * {@link DefaultRunLevelService$Interrupt} if it detects that it is being called
 * reentrantly in synchronous mode.  Callers should be careful NOT to swallow exceptions
 * of this type as shown in the following example:
 * <p>
 * try {<br>
 * &nbsp;rls.proceedTo(x);<br>
 * } catch (Exception e) {<br>
 * &nbsp;// swallow exception<br>
 * }
 * <p>
 * ~~~
 * <p>
 * All calls to the {@Link RunLevelListener} happens synchronously on the
 * same thread that caused the Inhabitant to be activated.  Therefore, implementors
 * of this interface should be careful and avoid calling long operations.
 * 
 * @author Jeff Trent
 * 
 * @since 3.1
 */
public class DefaultRunLevelService implements RunLevelService<Void>,
    RunLevelState<Void>, InhabitantListener, HabitatListener {
  // the initial run level
  public static final int INITIAL_RUNLEVEL = -2;

  // the default mode - sync or async.
  static final boolean ASYNC_ENABLED = false;
  
  private static final Logger logger = Logger.getLogger(DefaultRunLevelService.class.getName());
  
  private final Object lock = new Object();

  // the async mode for this instance.
  // if enabled, then all work is performed using a private
  // executor.  If disabled, then almost all of the work
  // occurs on the calling thread to proceedTo().  Almost all
  // because in the event a thread calls proceedTo() while
  // another thread is already executing a proceedTo() operation,
  // the the interrupting thread will return immediately and
  // the pre-existing executing thread is interrupted to go to
  // the new run level.
  private final boolean asyncMode;

  // the private executor service if this instance is using
  // async mode.
  private final ExecutorService exec;

  // the target environment, defaulting to Void.class
  private final Class<?> targetEnv;

  // the habitat for this instance of the RunLevelService
  private final Habitat habitat;

  // used primarily for testing purposes - can ignore
  private RunLevelState<Void> delegate;

  // the current run level (the last one successfully achieved)
  private Integer current;

  // the set of recorders, one per runlevel (used as necessary, cleared when shutdown)
  private final HashMap<Integer, Recorder> recorders;

  // the "active" proceedTo worker
  private Worker worker;
  
  // used for eventing an {@link RunLevelListener}s
  private enum ListenerEvent {
    PROGRESS, CANCEL, ERROR,
  }

  public DefaultRunLevelService(Habitat habitat) {
    this(habitat, ASYNC_ENABLED, Void.class, new LinkedHashMap<Integer, Recorder>());
  }

  DefaultRunLevelService(Habitat habitat, boolean async, Class<?> targetEnv,
      HashMap<Integer, Recorder> recorders) {
    this.habitat = habitat;
    assert (null != habitat);
    this.asyncMode = async;
    if (asyncMode) {
      // we can't use a singleThreadExecutor because a thread could become "stuck"
      exec = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable runnable) {
          Thread activeThread = new RunLevelServiceThread(runnable);
          synchronized (lock) {
            logger.log(Level.FINE, "new thread: {0}", activeThread);
          }
          return activeThread;
        }
      });
    } else {
      this.exec = null;
    }
    this.targetEnv = targetEnv;
    this.recorders = recorders;

    // subscribe to events in the habitat since we cannot rely on PostConstruct.
    // This is because the complete habitat may not be initialized at the time of
    // our initialization.
    habitat.addHabitatListener(this);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "-" + System.identityHashCode(this)
        + "(" + getDescription(false) + ", del: " + delegate + ")";
  }
  
  public String getDescription(boolean extended) {
    StringBuilder b = new StringBuilder();
    b.append("curr=").append(getCurrentRunLevel()).append(", ");
    b.append("act=").append(getActivatingRunLevel()).append(", ");
    b.append("plan=").append(getPlannedRunLevel()).append(", ");
    b.append("env=").append(getEnvironment()).append(", ");
    if (extended) {
      b.append("thrd=").append(Thread.currentThread());
    }
    return b.toString();
  }

  @SuppressWarnings("unused")
  private void setDelegate(RunLevelState<Void> stateProvider) {
    assert (this != stateProvider);
    assert (getEnvironment() == stateProvider.getEnvironment());
    this.delegate = stateProvider;
  }

  @Override
  public RunLevelState<Void> getState() {
    return (null == delegate) ? this : delegate;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class getEnvironment() {
    return (null == delegate) ? targetEnv : delegate.getEnvironment();
  }

  @Override
  public Integer getCurrentRunLevel() {
    return (null == delegate) ? current : delegate.getCurrentRunLevel();
  }

  @Override
  public Integer getPlannedRunLevel() {
    if (null == delegate) {
      synchronized (lock) {
        return (null == worker) ? null : worker.getPlannedRunLevel();
      }
    } else {
      return delegate.getPlannedRunLevel();
    }
  }

  public Integer getActivatingRunLevel() {
    synchronized (lock) {
      return (null == worker) ? null : worker.getActivatingRunLevel();
    }
  }
  
  /**
   * Returns true if the RunLevel for the given inhabitant in question
   * should be processed by this RunLevelService instance.
   * 
   * @param i the inhabitant
   * @param rl the inhabitan'ts runLevel
   * @param activeRunLevel the current runLevel
   * 
   * @return
   */
  protected boolean accept(Inhabitant<?> i, RunLevel rl, int activeRunLevel) {
    return (rl.value() == activeRunLevel && rl.environment() == targetEnv);
  }
  
  /**
   * This is needed in the scenario where the habitat initially didn't
   * have an instance of this RunLevelService (or derivative) and then
   * later on in time, after all {@link RunLevelInhabitant}'s became defined,
   * this instance was introduced. In the event that the RunLevelInhabitant's
   * have not yet been bound, this will latently bind them now.
   */ 
  private void checkBinding(RunLevelInhabitant<?,?> rli) {
    RunLevelState<?> state = rli.getState();
    if (state != this && RunLevelServiceStub.class.isInstance(state)) {
      RunLevelService<?> delegate = ((RunLevelServiceStub)state).getDelegate();
      if (null != delegate) {
        assert(this == delegate);
      } else {
        ((RunLevelServiceStub)state).activate(this);
      }
    }
  }
  
  private boolean isCancelled(Worker worker) {
    synchronized (lock) {
      return (this.worker != worker);
    }
  }

  /**
   * Called after the proceedTo work is finished.  This is
   * akin to a suicide act.
   * 
   * @param worker the worker that was performing the work
   */
  private void finished(Worker worker) {
    synchronized (lock) {
      // ensure that the worker is the "active" one and not some zombie back to life
      if (!isCancelled(worker)) {
        // it was the "trusted" worker
        this.worker = null;
      }
    }
    
    synchronized (this) {
      notifyAll();
    }
  }
  
  private void setCurrent(Worker worker, Integer current) {
    synchronized (lock) {
      // ensure that the worker is the "active" one and not some zombie back to life
      if (isCancelled(worker)) {
        return;
      } else {
        this.current = current;
      }
    }

    // notify listeners that we progressed
    event(worker, ListenerEvent.PROGRESS, null, null);
  }
  
  protected List<Integer> getRecordersToRelease(
      HashMap<Integer, Recorder> list, int runLevel) {
    List<Integer> qualifying = new ArrayList<Integer>();
    synchronized (lock) {
      for (Entry<Integer, Recorder> entry : recorders.entrySet()) {
        int entryKey = entry.getKey();
        if (entryKey >= runLevel) {
          qualifying.add(entry.getKey());
        }
      }
    }

    // return in order of highest to lowest
    Collections.sort(qualifying);
    Collections.reverse(qualifying);

    return qualifying;
  }
  
  protected void event(Worker worker, ListenerEvent event, ServiceContext context, Throwable error) {
    logger.log(Level.FINE, "event {0} - " + getDescription(true), event);
    
    if (isCancelled(worker)) {
      logger.log(Level.FINE, "Ignoring this notification!");
    } else {
      Interrupt lastInterrupt = null;
      Collection<RunLevelListener> activeListeners = 
          habitat.getAllByContract(RunLevelListener.class);
      for (RunLevelListener listener : activeListeners) {
        try {
          if (ListenerEvent.PROGRESS == event) {
            listener.onProgress(this);
          } else if (ListenerEvent.CANCEL == event) {
            listener.onCancelled(this, current);
          } else {
            listener.onError(this, context, error, true);
          }
        } catch (Interrupt interrupt) {
          lastInterrupt = interrupt;
        } catch (Exception e) {
          // don't percolate the exception since it may negatively impact processing
          logger.log(Level.WARNING, "swallowing exception - " + getDescription(true),
              new ComponentException(e));
        }
      }
      
      if (null != lastInterrupt) {
        throw lastInterrupt;
      } else {
        if (null != error) {
          logger.log(Level.FINE, "swallowing exception - " + context, new ComponentException(error));
        }
      }
    }
  }
  
  @Override
  public boolean inhabitantChanged(InhabitantListener.EventType eventType,
      Inhabitant<?> inhabitant) {
    if (InhabitantListener.class.isInstance(delegate)) {
      return InhabitantListener.class.cast(delegate).inhabitantChanged(
          eventType, inhabitant);
    }

    AbstractInhabitantImpl<?> ai = AbstractInhabitantImpl.class.cast(inhabitant);
    RunLevel rl = ai.getAnnotation(RunLevel.class);
    Integer activeRunLevel = (null == rl) ? null : rl.value();
    
    if (null != activeRunLevel) {
      // forward to the active recorder?
      if (InhabitantListener.EventType.INHABITANT_ACTIVATED == eventType) {
        Recorder activeRecorder;
        synchronized (lock) {
          activeRecorder = recorders.get(activeRunLevel);
          if (null == activeRecorder) {
            activeRecorder = new Recorder(activeRunLevel, getEnvironment());
            recorders.put(activeRunLevel, activeRecorder);
          }
        }

        if (null != activeRecorder) {
          activeRecorder.inhabitantChanged(eventType, inhabitant);
        }
      }
    }
    
    // we always want to maintain our subscription
    return true;
  }

  @Override
  public boolean inhabitantChanged(HabitatListener.EventType eventType,
      Habitat habitat, Inhabitant<?> inhabitant) {
    if (org.jvnet.hk2.component.HabitatListener.EventType.HABITAT_INITIALIZED == eventType) {
      proceedTo(-1);
    }
    return !habitat.isInitialized();
  }

  @Override
  public boolean inhabitantIndexChanged(HabitatListener.EventType eventType,
      Habitat habitat, Inhabitant<?> inhabitant, String index, String name,
      Object service) {
    return true;
  }
  
  @Override
  public void proceedTo(final int runLevel) {
    if (runLevel < -1) {
      throw new IllegalArgumentException();
    }

    // see if we can interrupt first
    Worker worker = this.worker;
    if (null != worker) {
      if (worker.interrupt(runLevel)) {
        return;
      }
    }

    // if we are here, then we couldn't interrupt and we must create a new worker
    synchronized (lock) {
      this.worker = worker = (asyncMode) ?
            new AsyncProceedToOp(runLevel) :
            new SyncProceedToOp(runLevel);
    }
    
    worker.proceedTo(runLevel);
  }
  
  
  private abstract class Worker implements Runnable {
    // the target runLevel we want to achieve
    protected volatile Integer planned;
    
    // the active run level attempting to be activated
    private Integer activeRunLevel;

    // tracks the direction of any active proceedTo worker
    protected Boolean upSide;
    
    protected Worker(int runLevel) {
      this.planned = runLevel;
    }

    public Integer getPlannedRunLevel() {
      return planned;
    }
    
    public Integer getActivatingRunLevel() {
      return activeRunLevel;
    }

    /**
     * Checks to see if this worker has been interrupted, and will abort
     * if it finds it has been.
     * 
     * @param e any error encountered during the nested proceedTo operation; may be null
     * @param i the inhabitant that was being activated / released during the operation; may be null
     */
    public void checkInterrupt(Exception e, Inhabitant<?> i) {
      if (null != e) {
        event(this, ListenerEvent.ERROR, serviceContext(e, i), e);
      }
    }
    
    /**
     * Attempts to interrupt processing to go to a new runLevel.
     * 
     * @param runLevel the revised runLevel to proceedTo
     * 
     * @return true, if its possible to go to the new runLevel; note that
     *    implementation may handle the interrupt by other means (i.e.,
     *    throwing an InterruptException for the synchronous case)
     */
    public abstract boolean interrupt(int runLevel);

    /**
     * Called after initialization to run the proceedTo operation
     * 
     * @param runLevel the runLevel to proceedTo
     */
    public abstract void proceedTo(int runLevel);
    
    /**
     * Core control logic.
     */
    @Override
    public void run() {
      logger.log(Level.FINE, "proceedTo({0}) - " + getDescription(true), planned);
      
      upSide = null;
      
      if (null != planned) {
        int current = (null == getCurrentRunLevel()) ? INITIAL_RUNLEVEL : getCurrentRunLevel();
        if (planned > current) {
          upSide = true;
          int rl = current + 1;
          while (rl <= planned) {
            upActiveRecorder(rl);
            rl++;
          }
        } else if (planned < current) {
          upSide = false;

          // start things off we a notification of the current runLevel
          setCurrent(this, current);

          int rl = current;
          while (rl > planned) {
            downActiveRecorder(rl);
            rl--;
          }
        } else { // planned == current
          // force closure of any orphaned higher RunLevel services
          downActiveRecorder(current+1);
        }
      }

      finished(this);
    }
    
    private void upActiveRecorder(int runLevel) {
      activeRunLevel = runLevel;
  
      // create demand for RunLevel (runLevel) components
      activateRunLevel();
  
      // don't set current until we've actually reached it
      setCurrent(this, runLevel);
    }
    
    private void activateRunLevel() {
      // TODO: we could cache this in top-level proceedTo()
      Collection<Inhabitant<?>> runLevelInhabitants = 
        habitat.getAllInhabitantsByContract(RunLevel.class.getName());
      for (Inhabitant<?> i : runLevelInhabitants) {
        AbstractInhabitantImpl<?> ai = AbstractInhabitantImpl.class.cast(i);
        RunLevel rl = ai.getAnnotation(RunLevel.class);
    
        if (accept(ai, rl, activeRunLevel)) {
          RunLevelInhabitant<?,?> rli = RunLevelInhabitant.class.cast(ai);
          checkBinding(rli);
          
          if (logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, "activating {0} - " + getDescription(true), rli);
          }
          
          try {
            rli.get();
            assert(rli.isInstantiated());
            checkInterrupt(null, rli);
          } catch (Exception e) {
            checkInterrupt(e, rli);
          }
        }
      }
    }
    
    protected void downActiveRecorder(int runLevel) {
      activeRunLevel = runLevel;

      // release stuff
      deactiveRunLevel(runLevel);
      
      // don't set current until we've actually reached it
      setCurrent(this, activeRunLevel = runLevel-1);
    }

    private void deactiveRunLevel(int runLevel) {
      List<Integer> downRecorders = getRecordersToRelease(recorders, runLevel);
      for (int current : downRecorders) {
        Recorder downRecorder;
        synchronized (lock) {
          downRecorder = recorders.get(current);
        }
        
        if (null != downRecorder) {
          // Causes release of the entire activationSet.  Release occurs in the inverse
          // order of the recordings.  So A->B->C will have startUp ordering be (C,B,A)
          // because of dependencies.  The shutdown ordering will b (A,B,C).
          Inhabitant<?> i;
          while (null != (i = downRecorder.pop())) {
            if (logger.isLoggable(Level.FINER)) {
              logger.log(Level.FINER, "releasing {0} - " + getDescription(true), i);
            }
            
            try{
              i.release();
              assert(!i.isInstantiated());
              checkInterrupt(null, i);
            } catch (Exception e) {
              checkInterrupt(e, i);
            }
          }
        }
      }
    }
  }
  
  
  /**
   * Sync worker 
   */
  private class SyncProceedToOp extends Worker {
    // record the thread performing the operation
    private final Thread activeThread = Thread.currentThread();
    
    // the next planned runLevel (after interrupt)
    protected Integer nextPlannedAfterInterrupt;
    
    // records where a cancel was issued
    private boolean cancelIssued;
    
    private SyncProceedToOp(int runLevel) {
      super(runLevel);
    }

    /**
     * Interrupts are always handles in the synchronous case.
     * 
     * Either by popping the stack for the reentrant call on
     * same thread, or by sending a cancel event to the
     * active thread doing the proceedTo() call.
     */
    @Override
    public boolean interrupt(int runLevel) {
      Thread ourThread = Thread.currentThread();
      
      synchronized (lock) {
        Integer planned = getPlannedRunLevel();
        if (null != planned && planned == runLevel) {
          return true;  // short circuit
        }
        
        nextPlannedAfterInterrupt = runLevel;

        if (ourThread == activeThread) {
          checkInterrupt(null, null);
        } else {
          // must interrupt another thread to do the new proceedTo().
          // Note how this thread exhibits async behavior in this case.
          // The cancel notification will happen on the other thread
          logger.log(Level.FINE, "Interrupting thread {0} - " + getDescription(true), activeThread);
          
          activeThread.interrupt();
        }
      }
        
      return true;
    }
    
    @Override
    public void proceedTo(int runLevel) {
      synchronized (lock) {
        planned = runLevel;
        nextPlannedAfterInterrupt = null;
        cancelIssued = false;
      }

      try {
        run();
      } catch (Exception e) {
        handleInterruptException(e);
      }
    }
    
    @Override
    public void checkInterrupt(Exception e, Inhabitant<?> i) {
      synchronized (lock) {
        boolean cancelled = isCancelled(this);
        if (cancelled || null != nextPlannedAfterInterrupt) {
          if (!cancelled && canUpdateProceedTo(nextPlannedAfterInterrupt)) {
            planned = nextPlannedAfterInterrupt;
            nextPlannedAfterInterrupt = null;
          } else {
            // send cancel event, but only one time
            if (!cancelIssued) {
              cancelIssued = true;
              event(this, ListenerEvent.CANCEL, null, null);
            }
            
            // pop stack to last proceedTo()
            throw new Interrupt();
          }
        }
      }
      
      super.checkInterrupt(e, i);
    }
    
    private boolean canUpdateProceedTo(Integer proposed) {
      if (null != upSide) {
        Integer planned = getPlannedRunLevel();
        Integer active = getActivatingRunLevel();
        if (null != planned && null != active && null != proposed) {
          if (upSide && proposed > active) {
            return true;
          } else if (!upSide && proposed < active) {
            return true;
          }
        }
      }
      
      return false;
    }
    
    private void handleInterruptException(Exception e) {
      logger.log(Level.FINE, "Interrupt caught - " + getDescription(true), e);

      Thread currentThread = Thread.currentThread();

      // we want to handle the new proceedTo if interrupted by another thread,
      // otherwise we fall out since we are not the owning thread.
      Integer next = null;
      if (activeThread == currentThread) {
        next = nextPlannedAfterInterrupt;
      }
      
      if (null != next) {
        proceedTo(next);
      } else {
        // RLS must continue / fall out
        logger.log(Level.FINE, "swallowing exception - " + getDescription(true), 
            new ComponentException(e));
      }
    }

  }

  
  /**
   * Async worker 
   */
  private class AsyncProceedToOp extends Worker implements Runnable {
    // record the future for the operation
    private Future<?> activeFuture;
  
    private AsyncProceedToOp(int runLevel) {
      super(runLevel);
    }

    /**
     * Interrupts are never handled in the asynchronous case.
     * 
     * Here, we just kill the worker, and expect a new one to form.
     */
    @Override
    public boolean interrupt(int runLevel) {
      boolean haveFuture;
      synchronized (lock) {
        haveFuture = (null != activeFuture);
        if (haveFuture) {
          // cancel previous, but down hit thread with interrupt
          activeFuture.cancel(false);
          activeFuture = null;
        }
      }
      
      if (haveFuture) {
        event(this, ListenerEvent.CANCEL, null, null);
      }
      
      return false;
    }
    
    @Override
    public void run() {
      super.run();
      synchronized (lock) {
        activeFuture = null;
      }
    }

    @Override
    public void proceedTo(int runLevel) {
      assert(null == activeFuture);
      activeFuture = exec.submit(this);
    }

    @Override
    public void checkInterrupt(Exception e, Inhabitant<?> i) {
      if (isCancelled(this)) {
        throw new Interrupt();
      }
      super.checkInterrupt(e, i);
    }
  }


  protected ServiceContext serviceContext(Exception e, final Inhabitant<?> i) {
    ServiceContext ctx = null;

    if (e instanceof ComponentException) {
      ctx = ((ComponentException) e).getFailureContext();
    }

    if (null == ctx) {
      ctx = new ServiceContext() {
        @Override
        public ClassLoader getClassLoader() {
          ClassLoader cl;
          if (LazyInhabitant.class.isInstance(i)) {
            cl = ((LazyInhabitant<?>) i).getClassLoader();
          } else {
            cl = i.getClass().getClassLoader();
          }
          return cl;
        }

        @Override
        public Inhabitant<?> getInhabitant() {
          return i;
        }

        @Override
        public String getType() {
          return i.typeName();
        }

        @Override
        public String toString() {
          return i.toString();
        }
      };
    }

    return ctx;
  }


  private static class RunLevelServiceThread extends Thread {
    private RunLevelServiceThread(Runnable r) {
      super(r);
      setDaemon(true);
      setName(getClass().getSimpleName() + "-" + System.currentTimeMillis());
    }
  }
  
  
  @SuppressWarnings("serial")
  public static class Interrupt extends RuntimeException {
    private Interrupt() {}
  }

}
