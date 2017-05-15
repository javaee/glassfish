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


package connector;

import java.lang.reflect.Method;

import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.UnavailableException;
import javax.resource.spi.XATerminator;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.ExecutionContext;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkManager;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * 
 * @author Qingqing Ouyang
 */
public class WorkDispatcher implements Work {

    private boolean stop = false;
    private String id;
    private MessageEndpointFactory factory;
    private BootstrapContext ctx;
    private ActivationSpec spec;
    private WorkManager wm;
    private XATerminator xa;

    public WorkDispatcher(String id, BootstrapContext ctx,
            MessageEndpointFactory factory, ActivationSpec spec) {
        this.id = id;
        this.ctx = ctx;
        this.factory = factory;
        this.spec = spec;
        this.wm = ctx.getWorkManager();
        this.xa = ctx.getXATerminator();
    }

    public void run() {

        debug("ENTER...");

        try {
            synchronized (Controls.readyLock) {
                debug("WAIT...");
                Controls.readyLock.wait();

                if (stop) {
                    return;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        debug("Running...");

        // try 3 times to create endpoint (in case of failure)
        for (int i = 0; i < 3; i++) {

            try {

                Method onMessage = getOnMessageMethod();
                System.out.println("isDeliveryTransacted = "
                        + factory.isDeliveryTransacted(onMessage));

                if (!factory.isDeliveryTransacted(onMessage)) {
                    // MessageEndpoint ep = factory.createEndpoint(null);
                    // DeliveryWork d = new DeliveryWork("NO_TX", ep);
                    // wm.doWork(d, 0, null, null);
                } else {

                    // MessageEndpoint ep = factory.createEndpoint(null);
                    MessageEndpoint ep = factory
                            .createEndpoint(new FakeXAResource());
                    int numOfMessages = 5;

                    // importing transaction

                    // write/commit
                    ExecutionContext ec = startTx();
                    debug("Start TX - " + ec.getXid());

                    DeliveryWork w = new DeliveryWork(ep, numOfMessages,
                            "WRITE");
                    wm.doWork(w, 0, ec, null);
                    xa.commit(ec.getXid(), true);

                    debug("DONE WRITE TO DB");
                    Controls.expectedResults = numOfMessages;
                    notifyAndWait();

                    // delete/rollback
                    ec = startTx();
                    debug("Start TX - " + ec.getXid());

                    w = new DeliveryWork(ep, numOfMessages, "DELETE");
                    wm.doWork(w, 0, ec, null);
                    xa.rollback(ec.getXid());

                    debug("DONE ROLLBACK FROM DB");
                    Controls.expectedResults = numOfMessages;
                    notifyAndWait();

                    // delete/commit
                    ec = startTx();
                    debug("Start TX - " + ec.getXid());

                    w = new DeliveryWork(ep, numOfMessages, "DELETE");
                    wm.doWork(w, 0, ec, null);
                    xa.commit(ec.getXid(), true);

                    debug("DONE DELETE FROM DB");
                    Controls.expectedResults = 0;
                    notifyAndWait();

                    // write/commit
                    ec = startTx();
                    debug("Start TX - " + ec.getXid());

                    w = new DeliveryWork(ep, numOfMessages, "WRITE");
                    wm.doWork(w, 0, ec, null);
                    xa.commit(ec.getXid(), true);

                    debug("DONE WRITE TO DB");
                    Controls.expectedResults = numOfMessages;
                    notifyAndWait();

                    // delete/commit
                    ec = startTx();
                    debug("Start TX - " + ec.getXid());

                    w = new DeliveryWork(ep, numOfMessages, "DELETE");
                    wm.doWork(w, 0, ec, null);
                    xa.commit(ec.getXid(), true);

                    debug("DONE DELETE FROM DB");
                    Controls.expectedResults = 0;
                    notifyAndWait();

                    // write multiple times using doWork/commit
                    ec = startTx();
                    debug("Start TX - " + ec.getXid());

                    w = new DeliveryWork(ep, 1, "WRITE", true);
                    wm.doWork(w, 0, ec, null);
                    wm.doWork(w, 0, ec, null);
                    wm.doWork(w, 0, ec, null);
                    xa.commit(ec.getXid(), true);

                    debug("DONE WRITE TO DB");
                    Controls.expectedResults = 3;
                    notifyAndWait();

                    // write multiple times using doWork/rollback
                    ec = startTx();
                    debug("Start TX - " + ec.getXid());

                    w = new DeliveryWork(ep, 1, "WRITE", true);
                    wm.doWork(w, 0, ec, null);
                    wm.doWork(w, 0, ec, null);
                    wm.doWork(w, 0, ec, null);
                    xa.rollback(ec.getXid());

                    debug("DONE WRITE TO DB");
                    Controls.expectedResults = 3;
                    notifyAndWait();

                    ec = startTx();
                    debug("Start TX - " + ec.getXid());

                    // write multiple times using doWork/rollback
                    w = new DeliveryWork(ep, 2, "WRITE", true);
                    wm.doWork(w, 0, ec, null);
                    wm.doWork(w, 0, ec, null);
                    wm.doWork(w, 0, ec, null);

                    if (XAResource.XA_OK == xa.prepare(ec.getXid())) {
                        xa.commit(ec.getXid(), false);
                        debug("XA PREPARE/COMMIT. DONE WRITE TO DB ");
                        Controls.expectedResults = 9;
                        notifyAndWait();
                    } else {
                        xa.rollback(ec.getXid());
                        debug("XA PREPARE UNSUCCESSFUL. DONE ROLLBACK");
                        Controls.expectedResults = 3;
                        notifyAndWait();
                    }

                    // delete all.
                    ec = startTx();
                    debug("Start TX - " + ec.getXid());

                    w = new DeliveryWork(ep, 1, "DELETE_ALL");
                    wm.doWork(w, 0, ec, null);
                    xa.commit(ec.getXid(), true);

                    debug("DONE DELETE ALL FROM DB");
                    Controls.expectedResults = 0;
                    notifyAndWait();

                    done();
                }

                break;
            } catch (UnavailableException ex) {
                // ex.printStackTrace();
                System.out.println("WorkDispatcher[" + id
                        + "] Endpoint Unavailable");
                try {
                    Thread.currentThread().sleep(3 * 1000); // 3 seconds
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (XAException ex) {
                ex.printStackTrace();
                System.out.println("ERROR CODE = " + ex.errorCode);
                done();
                break;
            } catch (Exception ex) {
                ex.printStackTrace();
                done();
                break;
            }
        }

        debug("LEAVE...");
    }

    public void release() {
    }

    public void stop() {
        this.stop = true;
    }

    public String toString() {
        return id;
    }

    public Method getOnMessageMethod() {

        Method onMessageMethod = null;
        try {
            Class msgListenerClass = connector.MyMessageListener.class;
            Class[] paramTypes = { java.lang.String.class };
            onMessageMethod = msgListenerClass.getMethod("onMessage",
                    paramTypes);

        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
        return onMessageMethod;
    }

    private ExecutionContext startTx() {
        ExecutionContext ec = new ExecutionContext();
        try {
            Xid xid = new XID();
            ec.setXid(xid);
            ec.setTransactionTimeout(5 * 1000); // 5 seconds
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ec;
    }

    private void notifyAndWait() {
        try {
            // Sleep for 5 seconds
            // Thread.currentThread().sleep(5*1000);

            synchronized (Controls.readyLock) {
                // Notify the client to check the results
                Controls.readyLock.notify();

                // Wait until results are verified by the client
                Controls.readyLock.wait();

                if (stop) {
                    return;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void done() {
        try {
            Controls.done = true;
            synchronized (Controls.readyLock) {
                // Notify the client to check the results
                Controls.readyLock.notify();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void debug(String mesg) {
        System.out.println("Dispatcher[" + id + "] --> " + mesg);
    }
}
