package org.jvnet.hk2.component;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Provides a same-thread executor service for use by Hk2 internals.
 * 
 * @author Jeff Trent
 */
/*public*/ class SameThreadExecutor extends AbstractExecutorService {

  @Override
  public boolean awaitTermination(long arg0, TimeUnit arg1)
      throws InterruptedException {
    return false;
  }

  @Override
  public boolean isShutdown() {
    return false;
  }

  @Override
  public boolean isTerminated() {
    return false;
  }

  @Override
  public void shutdown() {
  }

  @Override
  public List<Runnable> shutdownNow() {
    return null;
  }

  @Override
  public void execute(Runnable runnable) {
    runnable.run();
  }

}
