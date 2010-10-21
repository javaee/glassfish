package rls.test.infra;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import junit.framework.Assert;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.InhabitantActivator;

@Service
public class MultiThreadedInhabitantActivator implements InhabitantActivator {

  public static boolean called;
  
  private final ExecutorService es = Executors.newFixedThreadPool(2, new ThreadFactory() {
    @Override
    public Thread newThread(Runnable run) {
      Thread t = new Thread(run);
      t.setDaemon(true);
      t.setName(MultiThreadedInhabitantActivator.class.getSimpleName());
//      System.out.println("HERE: " + t);
      return t;
    }
  });
  
  @Override
  public void activate(final Inhabitant<?> inhabitant) {
    called = true;
    
    es.submit(new Runnable() {
      @Override
      public void run() {
//        System.out.println("Activating: " + inhabitant);
        try {
          Object o = inhabitant.get();
          Assert.assertNotNull(o);
        } catch (Exception e) {
          e.printStackTrace();
          Assert.fail(e.getMessage());
        }
//        System.out.println("Activated: " + inhabitant);
      }
    });
  }

  @Override
  public void deactivate(Inhabitant<?> inhabitant) {
    inhabitant.release();
  }
  
}
