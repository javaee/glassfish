/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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

/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package connector;

import javax.resource.spi.work.ExecutionContext;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkManager;

/**
 * 
 * @author Qingqing Ouyang
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

    public TestWMWork(int id, boolean isRogue, boolean doNest,
            ExecutionContext ctx) {
        this.id = id;
        this.isRogue = isRogue;
        this.doNest = doNest;
        this.ctx = ctx;
    }

    public void setWorkManager(WorkManager wm) {
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
            System.out
                    .println("TestWMWork: Simulating rogue RA's Work: Expected Arithmetic Exception - divide by Zero");
            int j = 100 / 0;
        }

        if (doNest && (wm != null)) {
            Work nestedWork = new TestWMWork(8888, false);
            try {
                wm.doWork(nestedWork, 1 * 1000, ctx, null);
            } catch (WorkException ex) {
                if (ex.getErrorCode().equals(
                        WorkException.TX_CONCURRENT_WORK_DISALLOWED)) {
                    System.out.println("TestWMWork[" + id + "] "
                            + "PASS: CAUGHT EXPECTED = " + ex.getErrorCode());
                } else {
                    System.out.println("TestWMWork[" + id + "] "
                            + "FAIL: CAUGHT UNEXPECTED = "
                            + ex.getErrorCode());
                }
            }

            nestedWork = new TestWMWork(9999, false);
            try {
                ExecutionContext ec = new ExecutionContext();
                ec.setXid(new XID());
                ec.setTransactionTimeout(5 * 1000); // 5 seconds
                wm.doWork(nestedWork, 1 * 1000, ec, null);
            } catch (Exception ex) {
                System.out.println("TestWMWork[" + id + "] "
                        + "FAIL: CAUGHT UNEXPECTED = " + ex.getMessage());
            }
        }

        System.out.println("TestWMWork[" + id + "].stop running");
    }

    public void release() {
    }

    public void stop() {
        this.stop = true;
    }

    public String toString() {
        return String.valueOf(id);
    }
}
