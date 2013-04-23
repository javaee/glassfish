/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package connector;

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
import javax.naming.InitialContext;

/**
 * This is a sample resource adapter
 *
 * @author Qingqing Ouyang
 */
public class SimpleResourceAdapterImpl
        implements ResourceAdapter, java.io.Serializable {

    private BootstrapContext ctx;
    private WorkManager wm;
    private String testName;

    private boolean debug = true;
    private Work work;

    public SimpleResourceAdapterImpl() {
        debug("constructor...");
    }

    public void
    start(BootstrapContext ctx) throws ResourceAdapterInternalException {

        this.ctx = ctx;
        this.wm = ctx.getWorkManager();
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
    endpointActivation(MessageEndpointFactory factory, ActivationSpec spec)
            throws NotSupportedException {
        try {
            debug("B.000. Create and schedule Dispatcher");
            spec.validate();
            work = new WorkDispatcher("DISPATCHER", ctx, factory, spec);
            wm.scheduleWork(work, 30 * 1000, null, null);
            
            //Test if a resource defined in the comp's namespace is available
            Object o = (new InitialContext()).lookup("java:comp/env/MyDB");
            System.out.println("**** lookedup in RA endpointActivation:" + o);
            
            debug("B.001. Scheduled Dispatcher");
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public void endpointDeactivation(
            MessageEndpointFactory endpointFactory,
            ActivationSpec spec) {
        debug("endpointDeactivation called...");
        //Test if a resource defined in the comp's namespace is available
//        try{
//            Object o = (new InitialContext()).lookup("java:comp/env/MyDB");
//            System.out.println("lookedup in RA endpointDeactivation:" + o);
//        } catch (Exception ex){
//            System.out.println("**** Error while looking up in component context " +
//            		"in endpointDeactivation");
//            ex.printStackTrace();
//            throw new RuntimeException(ex);
//        }
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
    debug(String message) {
        if (debug)
            System.out.println("[SimpleResourceAdapterImpl] ==> " + message);
    }

    public XAResource[] getXAResources(ActivationSpec[] specs)
            throws ResourceException {
        throw new UnsupportedOperationException();
    }
}
