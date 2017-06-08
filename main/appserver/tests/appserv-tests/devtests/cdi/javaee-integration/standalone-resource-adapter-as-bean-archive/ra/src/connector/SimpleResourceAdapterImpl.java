/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package connector;

import java.util.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.XATerminator;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.UnavailableException;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.work.ExecutionContext;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkManager;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * This is a sample resource adapter
 *
 * @author	Qingqing Ouyang
 */
public class SimpleResourceAdapterImpl 
implements ResourceAdapter, java.io.Serializable {

    private BootstrapContext ctx;
    private WorkManager wm;
    private String testName;

    private boolean debug = true;
    private Work work;

    public SimpleResourceAdapterImpl () {
        debug ("constructor...");
    }

    public void
    start(BootstrapContext ctx) throws ResourceAdapterInternalException{
        
        debug("001. Simple RA start...");

        this.ctx = ctx;
        debug("002. Simple RA start...");
        this.wm  = ctx.getWorkManager();
        debug("003. Simple RA start...");

        //testing creat timer
        Timer timer = null;
	  try{
	      timer = ctx.createTimer();
	  } catch(UnavailableException ue) {
	      System.out.println("Error");
	      throw new ResourceAdapterInternalException("Error form bootstrap");
	  }
        debug("004. Simple RA start...");

          try {
          
          XATerminator xa = ctx.getXATerminator();
          
          Xid xid1 = new XID();
          System.out.println(" XID1 = " + xid1);
          
          ExecutionContext ec = new ExecutionContext();
          ec.setXid(xid1);
          ec.setTransactionTimeout(5*1000); //5 seconds
          TestWMWork outw = new TestWMWork(1000, false, true, ec);
          outw.setWorkManager(wm);
          try {
          wm.doWork(outw, 1*1000, ec, null);
          xa.commit(xid1, true);
          } catch (Exception ex) {
          
          System.out.println(" ex = " + ex.getMessage());
          xa.rollback(xid1);
          }
          
          Xid xid2 = new XID();
          System.out.println(" XID2 = " + xid2);
          
          ec = new ExecutionContext();
          ec.setXid(xid2);
          ec.setTransactionTimeout(5*1000); //5 seconds
          TestWMWork anotherw = new TestWMWork(3000, false);
          anotherw.setWorkManager(wm);
          try {
          wm.doWork(anotherw, 1*1000, ec, null);
          xa.commit(xid2, true);
          } catch (Exception ex) {
          xa.rollback(xid2);
          }

          } catch (Exception ex) {
          ex.printStackTrace();
          }

        
          for (int i = 0 ; i < 3; i++) {
          TestWMWork w = new TestWMWork(i, false);
          try {
          wm.doWork(w, 1, null, null);
          } catch (Exception ex) {
          System.out.println("FAIL: CAUGHT exception : i = " + i);
          ex.printStackTrace();
          } 
          }

          for (int i = 3 ; i < 6; i++) {
          TestWMWork w = new TestWMWork(i, true);
          boolean pass = false;
          try {
          wm.doWork(w, 1, null, null);
          } catch (Exception ex) {
          pass = true;
          System.out.println("PASS: CAUGHT EXPECTED exception : i = " + i);
          ex.printStackTrace();
          } finally {
          if (!pass) {
          System.out.println("FAIL: DID NOT GET EXPECTED exception :");
          }
          }
          }

          for (int i = 6 ; i < 9; i++) {
          TestWMWork w = new TestWMWork(i, false);
          try {
          wm.doWork(w, 1, null, null);
          } catch (Exception ex) {
          System.out.println("FAIL: CAUGHT exception : i = " + i);
          ex.printStackTrace();
          }
          }

          for (int i = 9 ; i < 12; i++) {
          TestWMWork w = new TestWMWork(i, false);
          try {
          wm.startWork(w);
          } catch (Exception ex) {
          System.out.println("FAIL: CAUGHT exception : i = " + i);
          ex.printStackTrace();
          }
          }

          for (int i = 12 ; i < 15; i++) {
          TestWMWork w = new TestWMWork(i, false);
          try {
          wm.scheduleWork(w);
          } catch (Exception ex) {
          System.out.println("FAIL: CAUGHT exception : i = " + i);
          ex.printStackTrace();
          }
          }
        
        debug("005. Simple RA start...");
    }

    public void
    stop() {
        debug("999. Simple RA stop...");
        if (work != null) {
            ((WorkDispatcher) work).stop();

            synchronized (Controls.readyLock) {
                Controls.readyLock.notify();
            }

        }
    }

    public void
    endpointActivation ( MessageEndpointFactory factory, ActivationSpec spec)
        throws NotSupportedException {
        try {
            debug("B.000. Create and schedule Dispatcher");
            spec.validate();
            work = new WorkDispatcher("DISPATCHER", ctx, factory, spec);
            wm.scheduleWork(work, 4*1000, null, null);
            debug("B.001. Scheduled Dispatcher");
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public void
    endpointDeactivation (
            MessageEndpointFactory endpointFactory, 
            ActivationSpec spec) {
        debug ("endpointDeactivation called...");
        
        ((WorkDispatcher) work).stop();
    }
  
    public String getTestName() {
        return testName;
    }

    public void setTestName(String name) {
        debug("setTestName called... name = " + name);
        testName = name;
    }

    public void
    debug (String message) {
        if (debug)
            System.out.println("[SimpleResourceAdapterImpl] ==> " + message);
    }

    public XAResource[] getXAResources(ActivationSpec[] specs) 
        throws ResourceException {
        throw new UnsupportedOperationException();
    }
}
