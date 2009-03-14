/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package connector;

import java.lang.reflect.Method;
import java.util.Iterator;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.XATerminator;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.UnavailableException;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkManager;
import javax.resource.spi.work.ExecutionContext;
import javax.transaction.xa.Xid;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

/**
 *
 * @author	Qingqing Ouyang
 */
public class WorkDispatcher implements Work {

    protected boolean stop = false;
    protected String id;
    protected MessageEndpointFactory factory;
    protected BootstrapContext ctx;
    protected ActivationSpec spec;
    protected WorkManager wm;
    protected XATerminator xa;
    
    public WorkDispatcher(
            String id,
            BootstrapContext ctx,
            MessageEndpointFactory factory, 
            ActivationSpec spec) {
        this.id      = id;
        this.ctx     = ctx;
        this.factory = factory;
        this.spec    = spec;
        this.wm      = ctx.getWorkManager();
        this.xa      = ctx.getXATerminator();
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

        //try 3 times to create endpoint (in case of failure)
        for (int i = 0; i < 1; i++) {

            try {

                Method onMessage = getOnMessageMethod();
                System.out.println("isDeliveryTransacted = " + 
                        factory.isDeliveryTransacted(onMessage));

                if (!factory.isDeliveryTransacted(onMessage)) {
                    //MessageEndpoint ep = factory.createEndpoint(null);
                    //DeliveryWork d = new DeliveryWork("NO_TX", ep); 
                    //wm.doWork(d, 0, null, null);
                } else {
                    
                    //MessageEndpoint ep = factory.createEndpoint(null);
                    MessageEndpoint ep = factory.createEndpoint(new FakeXAResource());
                    int numOfMessages = 5;
                    
                    //importing transaction

                    //write/commit
                    ExecutionContext ec = startTx();
                    debug("Start TX - " + ec.getXid());

                    DeliveryWork w = 
                        new DeliveryWork(ep, numOfMessages, "WRITE");
                    wm.doWork(w, 0, ec, null);
                    xa.commit(ec.getXid(), true);

                    debug("DONE WRITE TO DB");
                    Controls.expectedResults = numOfMessages;
                    notifyAndWait();
                    
                    //delete/rollback
                    ec = startTx();
                    debug("Start TX - " + ec.getXid());

                    w = new DeliveryWork(ep, numOfMessages, "DELETE");
                    wm.doWork(w, 0, ec, null);
                    xa.rollback(ec.getXid());

                    debug("DONE ROLLBACK FROM DB");
                    Controls.expectedResults = numOfMessages;
                    notifyAndWait();

                    //delete/commit
                    ec = startTx();
                    debug("Start TX - " + ec.getXid());
                    
                    w = new DeliveryWork(ep, numOfMessages, "DELETE");
                    wm.doWork(w, 0, ec, null);
                    xa.commit(ec.getXid(), true);

                    debug("DONE DELETE FROM DB");
                    Controls.expectedResults = 0;
                    notifyAndWait();

                    //write/commit
                    ec = startTx();
                    debug("Start TX - " + ec.getXid());

                    w = new DeliveryWork(ep, numOfMessages, "WRITE");
                    wm.doWork(w, 0, ec, null);
                    xa.commit(ec.getXid(), true);

                    debug("DONE WRITE TO DB");
                    Controls.expectedResults = numOfMessages;
                    notifyAndWait();

                    //delete/commit
                    ec = startTx();
                    debug("Start TX - " + ec.getXid());
                    
                    w = new DeliveryWork(ep, numOfMessages, "DELETE");
                    wm.doWork(w, 0, ec, null);
                    xa.commit(ec.getXid(), true);

                    debug("DONE DELETE FROM DB");
                    Controls.expectedResults = 0;
                    notifyAndWait();

                    //write multiple times using doWork/commit
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

                    //write multiple times using doWork/rollback
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

                    //write multiple times using doWork/rollback
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
                    
                    //delete all.
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
                //ex.printStackTrace();
                System.out.println("WorkDispatcher["+id+"] Endpoint Unavailable");
                try {
                    Thread.currentThread().sleep(3*1000); //3 seconds
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (XAException ex) {
                ex.printStackTrace();
                System.out.println("ERROR CODE = " + ex.errorCode);
                done();
                break;
            }  catch (Exception ex) {
                System.out.println("Exception while executing work");
                ex.printStackTrace();
                done();
                break;
            }finally{
                done();
            }
        }
        
        debug("LEAVE...");
    }

    public void release() {}

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
            onMessageMethod = 
                msgListenerClass.getMethod("onMessage", paramTypes);
            
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
        return onMessageMethod;
    }

    protected ExecutionContext startTx() {
        ExecutionContext ec = new ExecutionContext();
        try {
            Xid xid = new XID();
            ec.setXid(xid);
            ec.setTransactionTimeout(5*1000); //5 seconds
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ec;
    }

    protected void notifyAndWait() {
        try {
            //Sleep for 5 seconds
            //Thread.currentThread().sleep(5*1000);
            
            synchronized(Controls.readyLock) {
                //Notify the client to check the results
                Controls.readyLock.notifyAll(); 

                //Wait until results are verified by the client
                Controls.readyLock.wait(); 

                if (stop) {
                    return;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void done() {
        try {

            synchronized(Controls.readyLock) {
                Controls.done = true;
                //Notify the client to check the results
                Controls.readyLock.notifyAll();

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void debug(String mesg) {
        System.out.println("Dispatcher[" + id + "] --> " + mesg);
    }
}
