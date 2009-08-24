/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package connector;

import java.lang.reflect.Method;
import java.util.Iterator;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.UnavailableException;
import javax.resource.spi.work.ExecutionContext;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkManager;
import javax.resource.spi.work.WorkException;

/**
 *
 * @author	Qingqing Ouyang
 */
public class TestWMWork implements Work {

    private boolean stop = false;
    private int id;
    private boolean isRogue;
    private boolean doNest;
    private WorkManager wm;
    private ExecutionContext ctx;

    public TestWMWork(int id, boolean isRogue) {
        this(id, isRogue, false, null);
    }
    
    public TestWMWork(int id, boolean isRogue, 
            boolean doNest, ExecutionContext ctx) {
        this.id = id;
        this.isRogue = isRogue;
        this.doNest = doNest;
        this.ctx = ctx;
    }

    public void setWorkManager (WorkManager wm) {
        this.wm = wm;
    }

    public void run() {

        System.out.println("TestWMWork[" + id + "].start running");
        if (!isRogue) {
            try {
                Thread.currentThread().sleep(1000);
            } catch (Exception ex) {
                System.out.println("TestWMWork[" + id + "].interrupted = ");
                ex.printStackTrace();
            }
        } else {
            System.out.println("TestWMWork: Simulating rogue RA's Work: Expected Arithmetic Exception - divide by Zero");
            int j = 100/0;
        }

        if (doNest && (wm != null)) {
            Work nestedWork = new TestWMWork(8888, false);
            try {
                wm.doWork(nestedWork, 1*1000, ctx, null);
            } catch (WorkException ex) {
                if (ex.getErrorCode().equals(
                            WorkException.TX_CONCURRENT_WORK_DISALLOWED)) {
                    System.out.println("TestWMWork[" + id + "] " + 
                            "PASS: CAUGHT EXPECTED = " + ex.getErrorCode());
                } else {
                    System.out.println("TestWMWork[" + id + "] " + 
                            "FAIL: CAUGHT UNEXPECTED = " + ex.getErrorCode());
                }
            }

            nestedWork = new TestWMWork(9999, false);
            try {
                ExecutionContext ec = new ExecutionContext();
                ec.setXid(new XID());
                ec.setTransactionTimeout(5*1000); //5 seconds
                wm.doWork(nestedWork, 1*1000, ec, null);
            } catch (Exception ex) {
                System.out.println("TestWMWork[" + id + "] " + 
                        "FAIL: CAUGHT UNEXPECTED = " + ex.getMessage());
            }
        }

        System.out.println("TestWMWork[" + id + "].stop running");
    }

    public void release() {}

    public void stop() {
        this.stop = true;
    }

    public String toString() {
       return String.valueOf(id);
    }
}
