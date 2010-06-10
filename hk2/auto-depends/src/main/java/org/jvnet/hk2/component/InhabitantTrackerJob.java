package org.jvnet.hk2.component;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jvnet.hk2.component.InhabitantTracker.Callback;

/**
 * @see Habitat#trackFuture(InhabitantTrackerContext)
 * 
 * @author Jeff Trent
 * 
 * @since 3.1
 */
/*public*/class InhabitantTrackerJob implements Future<InhabitantTracker>,
    Callable<InhabitantTracker>, Callback {
  private final Habitat h;
  volatile InhabitantTrackerImpl it;
  private final InhabitantTrackerContext itc;
  private Long timeout;

  /*public*/ InhabitantTrackerJob(Habitat h, InhabitantTrackerContext itc) {
    this.h = h;
    this.itc = itc;
  }

  @Override
  public boolean cancel(boolean arg) {
    if (null != it) {
      synchronized (it) {
        timeout = 0L;
        it.notify();
      }
      it.release();
    }
    return true;
  }

  @Override
  public InhabitantTracker get() throws InterruptedException,
      ExecutionException {
    if (!isDone()) {
      timeout = null;
      h.exec.submit(this).get();
    }
    return isDone() ? it : null;
  }

  @Override
  public InhabitantTracker get(long timeout, TimeUnit units)
      throws InterruptedException, ExecutionException, TimeoutException {
    if (!isDone()) {
      this.timeout = units.convert(timeout, TimeUnit.MILLISECONDS);
      if (0 == timeout) {
        return null;
      }
      h.exec.submit(this).get();
    }
    return isDone() ? it : null;
  }

  @Override
  public boolean isCancelled() {
    return true;
  }

  @Override
  public boolean isDone() {
    if (null == it) {
      it = new InhabitantTrackerImpl(h, itc, this);
    }
    return it.isDone();
  }

  @Override
  public void updated(InhabitantTracker t, Habitat h, boolean initial) {
    if (null != it) {
      synchronized (it) {
        it.notify();
      }
    }
  }

  @Override
  public InhabitantTracker call() throws Exception {
    if (null != it) {
      synchronized (it) {
        if (!isDone()) {
          if (null == timeout) {
            it.wait();
          } else {
            it.wait(timeout);
          }
        }
      }
    }
    return it;
  }
}
