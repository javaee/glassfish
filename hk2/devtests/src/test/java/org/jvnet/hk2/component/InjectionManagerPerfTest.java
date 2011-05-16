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

import static org.junit.Assert.*;

import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.concurrent.SameThreadExecutor;
import org.jvnet.hk2.junit.Hk2Runner;
import org.jvnet.hk2.junit.Hk2Test;
import org.jvnet.hk2.test.impl.IOorCpuBoundService;
import org.jvnet.hk2.test.impl.PerLookupService;
import org.jvnet.hk2.test.impl.PerLookupServiceNested3;

import com.sun.hk2.component.InjectInjectionResolver;
import com.sun.hk2.component.InjectionResolver;

/**
 * InjectionManager performance tests.
 * 
 * @author Jeff Trent
 */
// TODO: implement propagation of callers ACC to workers
//@Ignore
public class InjectionManagerPerfTest extends Hk2Test implements PostConstruct {
  static final int PRIMING_THREAD_COUNT = 8;
  static final int INJECTIONS = 100;
  
  private static String previousConcurrencyControls;
  
  // we don't use this habitat for the test --- only for priming 
  @Inject
  Habitat h;
  
  @Inject(name=Constants.EXECUTOR_INHABITANT_INJECTION_MANAGER, optional=true)
  ExecutorService es;
  
  InjectionManager im = new InjectionManager();

  @Override
  public void postConstruct() {
//    if (null == es) {
//      Logger.getAnonymousLogger().info("CREATING TEST EXECUTOR SERVICE");
//      es = Executors.newCachedThreadPool(new ThreadFactory() {
//        @Override
//        public Thread newThread(Runnable runnable) {
//          Thread t = new Thread(runnable);
//          Logger.getAnonymousLogger().log(Level.FINE, "newThread for: {0}: " + t, runnable);
//          t.setDaemon(true);
//          t.setPriority(Thread.MAX_PRIORITY);
//          return t;
//        }
//      });
//    }

    if (null == es) {
      Logger.getAnonymousLogger().severe("no habitat level executor service - creating one here");
      es = Habitat.exec;
      assertNotNull(es);
    }
    
    Logger.getAnonymousLogger().info("priming executor");
    for (int i = 0; i < PRIMING_THREAD_COUNT; i++) {
      es.execute(new Runnable(){
        @Override
        public void run() {
        }});
    }
  }
  
  @BeforeClass
  static void enableConcurrencyControls() {
    previousConcurrencyControls = System.getProperty(Habitat.HK2_CONCURRENCY_CONTROLS);
    System.setProperty(Habitat.HK2_CONCURRENCY_CONTROLS, "true");
  }
  
  @AfterClass
  static void disableConcurrencyControls() {
    if (null == previousConcurrencyControls) {
      System.getProperties().remove(Habitat.HK2_CONCURRENCY_CONTROLS);
    } else {
      System.setProperty(Habitat.HK2_CONCURRENCY_CONTROLS, previousConcurrencyControls);
    }
  }
  
  @Before
  public void reset() throws Exception {
    runOnce(PerLookupService.class, null, h);
    
    PerLookupService.constructs = 0;
    PerLookupServiceNested3.constructs = 0;

    System.gc();
    try {
      Thread.sleep(250);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
  
  @Test
  public void nonThreadedInjection_trivialComponent() throws Exception { 
    run(PerLookupService.class, null, "trivial non-threaded");
  }

  @Test
  public void threadedInjection_trivialComponent() throws Exception { 
    run(PerLookupService.class, es, "trivial threaded");
  }
  
  @Test
  public void sameThreadedInjection_trivialComponent() throws Exception { 
    run(PerLookupService.class, SameThreadExecutor.instance, "trivial SameThreaded ES");
  }
  
  @Test
  public void nonThreadedInjection_complexComponent() throws Exception { 
    run(IOorCpuBoundService.class, null, "complex non-threaded");
  }

  @Test
  public void threadedInjection_complexComponent() throws Exception { 
    run(IOorCpuBoundService.class, es, "complex threaded");
  }

  @Test
  public void sameThreadedInjection_complexComponent() throws Exception { 
    run(IOorCpuBoundService.class, SameThreadExecutor.instance, "complex SameThreaded ES");
  }

  private void run(Class<?> clazz, ExecutorService es, String description) throws Exception {
    long total = 0;
    for (int i = 0; i < INJECTIONS; i++) {
      Habitat h = Hk2Runner.createHabitat();
      
      long start = System.currentTimeMillis();
      runOnce(clazz, es, h);
      long end = System.currentTimeMillis();
      total += end-start;
    }

    Logger.getAnonymousLogger().log(Level.SEVERE, description + " injection took {0} ms for {1} iterations", new Object[] {total, INJECTIONS});
    assertEquals(INJECTIONS, PerLookupServiceNested3.constructs);
  }

  private void runOnce(Class<?> clazz, ExecutorService es, Habitat h)
      throws InstantiationException, IllegalAccessException {
    Object component = clazz.newInstance();
    im.inject(component, null, es, new InjectionResolver[] {new InjectInjectionResolver(h)});
    PostConstruct.class.cast(component).postConstruct();
  }

}
