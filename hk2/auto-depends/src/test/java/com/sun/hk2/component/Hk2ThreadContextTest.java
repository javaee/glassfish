package com.sun.hk2.component;

import static org.junit.Assert.*;

import java.security.AccessControlContext;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;
import org.jvnet.hk2.component.InjectionPoint;
import org.jvnet.hk2.component.concurrent.Hk2Executor;
import org.jvnet.hk2.component.concurrent.WorkManager;

/**
 * Unit testing for Hk2ThreadContext
 * 
 * @author Jeff Trent
 */
public class Hk2ThreadContextTest {

  @Test
  public void baselineACC() throws Exception {
    AccessControlContext acc = Hk2ThreadContext.getCallerACC();
    assertNull(acc);
    
    Hk2ThreadContext.captureACCandRun(new Runnable() {
      @Override
      public void run() {
        AccessControlContext acc = Hk2ThreadContext.getCallerACC();
        assertNotNull(acc);
        assertSame(acc, Hk2ThreadContext.getCallerACC());
      }
    });

    acc = Hk2ThreadContext.getCallerACC();
    assertNull(acc);
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void baselineIP() throws Exception {
    InjectionPoint ip = Hk2ThreadContext.getCallerIP();
    assertNull(ip);

    Object res = Hk2ThreadContext.captureIPandRun(new InjectionPointImpl(null, null, null, null, null), new Callable() {
      @Override
      public Object call() throws Exception {
        InjectionPoint ip = Hk2ThreadContext.getCallerIP();
        assertNotNull(ip);
        assertSame(ip, Hk2ThreadContext.getCallerIP());
        return 1;
      }
    });
    assertEquals(new Integer(1), res);
    
    ip = Hk2ThreadContext.getCallerIP();
    assertNull(ip);
  }
  
  @Test
  public void propagationSupportedInWorkManager() throws Exception {
    Hk2ThreadContext.captureACCandRun(new Runnable() {
      @Override
      public void run() {
        final AccessControlContext acc = Hk2ThreadContext.getCallerACC();
        assertNotNull(acc);
        
        Callable<AccessControlContext> c1 = new Callable<AccessControlContext>() {
          @Override
          public AccessControlContext call() throws Exception {
            return Hk2ThreadContext.getCallerACC();
          }
        };

        Callable<InjectionPoint> c2 = new Callable<InjectionPoint>() {
          @Override
          public InjectionPoint call() throws Exception {
            return Hk2ThreadContext.getCallerIP();
          }
        };
        
        Runnable r1 = new Runnable() {
          @Override
          public void run() {
            assertNotNull(Hk2ThreadContext.getCallerIP());
          }
        };
        
        Hk2Executor es1 = new Hk2Executor(2, 2);
        ExecutorService es2 = Executors.newFixedThreadPool(2);
        
        WorkManager wm1 = new WorkManager(es1);
        WorkManager wm2 = new WorkManager(es2, 2);
        
        try {
          Future<AccessControlContext> res1a = wm1.submit(c1);
          Future<InjectionPoint> res1b = wm1.submit(c2);
          wm1.execute(r1);

          Future<AccessControlContext> res2a = wm2.submit(c1);
          Future<InjectionPoint> res2b = wm2.submit(c2);
          wm2.execute(r1);
          
          wm1.awaitCompletion();
          wm2.awaitCompletion();
          
//          assertNotSame("propagated acc context", acc, res1a.get());
          assertEquals("propagated acc context", acc, res1a.get());

          assertNull(res1b.get());

//          assertNotSame("propagated acc context", acc, res2a.get());
          assertEquals("propagated acc context", acc, res2a.get());

          assertNotSame(res1a.get(), res2a.get());
          
          assertNull(res2b.get());
        } catch (Exception e) {
          throw new RuntimeException(e);
        } finally {
          es1.shutdown();
          es2.shutdown();
        }
      }
    });
    
    AccessControlContext acc = Hk2ThreadContext.getCallerACC();
    assertNull(acc);
    
    InjectionPoint ip = Hk2ThreadContext.getCallerIP();
    assertNull(ip);
  }

  @Test
  public void noInjectionPointsOnStack() {
    Hk2ThreadContext.captureACCandRun(new Runnable() {
      @Override
      public void run() {
        assertEquals("active injection point", null, Hk2ThreadContext.getCallerIP());
      }
    });
  }

}
