package org.jvnet.hk2.component;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;

/**
 * A Test ExecutorService bridging to a more simple Executor.
 * 
 * @author Jeff Trent
 */
@Ignore
class TestExecutorService extends AbstractExecutorService {

  private final Executor exec;

  public TestExecutorService(Executor exec) {
    this.exec = exec;
  }

  @Override
  public boolean awaitTermination(long timeout, TimeUnit unit)
      throws InterruptedException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isShutdown() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isTerminated() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void shutdown() {
    // TODO Auto-generated method stub
  }

  @Override
  public List<Runnable> shutdownNow() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void execute(Runnable runnable) {
    exec.execute(runnable);
  }

}
