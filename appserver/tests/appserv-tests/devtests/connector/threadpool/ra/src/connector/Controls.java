/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002-2017 Oracle and/or its affiliates. All rights reserved.
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

import javax.resource.spi.*;
import javax.resource.spi.work.*;

/**
 *
 * @author
 */
public class Controls {
    public Object readyLock = new Object();

    private static Controls control=null;

    private WorkManager wm = null;

    private int setupWorks = 10;
    private int actualWorks = 2;

    public static int completedCount = 0;
    public static int rejectedCount = 0;

    public long TIME_OUT = 5000;
    public long BUFFER = 2000;

    public boolean error = false;

    private Work[] sWorks = new Work[setupWorks];
   
    private Controls(WorkManager wm) {
        this.wm = wm;
    }

    public static void instantiate(BootstrapContext ctx) {
        control = new Controls(ctx.getWorkManager());
    }

    public static Controls getControls() {
        return control;  
    }

    public void setupInitialWorks() {
        for (int i =0; i < setupWorks; i ++) {
            Work w = new SetupWork(i);
            try {
                wm.startWork(w, 500, null, new MyWorkListener());
                sWorks[i] = w;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void startTestWorks() {
        for (int i =0; i < actualWorks; i ++) {
            Work w = new ActualWork(i);
            try {
                wm.scheduleWork(w, TIME_OUT, null, new MyWorkListener());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void trigger() {
        if (completedCount > 0 || rejectedCount > 0 || error) {
            releaseAll(); 
            debug("Completed count => " + completedCount + " Rejected count:"+ rejectedCount + "error: " + error);
            throw new RuntimeException("Error while triggering the actual tests");
        }
        for (int i = 0; i < actualWorks-1; i ++ ) {
            release(i);
        }
    }

    public void checkResult() {
        try {Thread.sleep(TIME_OUT+BUFFER);} catch (Exception e) {}
        releaseAll();
        try {Thread.sleep(BUFFER);} catch (Exception e) {}
        if ((completedCount + rejectedCount == setupWorks + actualWorks) &&
            (rejectedCount == 1 && error == false)) {
            debug("Completed count => " + completedCount + " Rejected count:"+ rejectedCount + "error: " + error);
            debug("SetupWorks => " + setupWorks + " Actual works:"+ actualWorks);
            debug("TEST IS SUCCESSFUL");
        } else {
            debug("Completed count => " + completedCount + " Rejected count:"+ rejectedCount + "error: " + error);
            debug("SetupWorks => " + setupWorks + " Actual works:"+ actualWorks);
            releaseAll();
            throw new RuntimeException("Test Failed");
        }
    }

    public void setNumberOfSetupWorks(int count) {
        System.out.println("Number of Setup works is set to : " + count);
        this.setupWorks = count;
        sWorks = new Work[setupWorks];
    }

    public int getNumberOfSetupWorks() {
        return setupWorks;
    }

    public void setNumberOfActualWorks(int num) {
        System.out.println("Number of Actual works is set to : " + num);
        this.actualWorks = num;
    }

    public int getNumberOfActualWorks() {
        return actualWorks;
    }

    void debug(String s) {
        System.out.println(s);
    }

    void release(int i) {
        sWorks[i].release();
    }

    void releaseAll() {
        for (int i=0; i < setupWorks; i ++) {
            release(i);
        }
    }

    class MyWorkListener extends WorkAdapter {
        public void workCompleted(WorkEvent event) {
            synchronized(MyWorkListener.class){
               completedCount++;
               System.out.println("Worklistener Completed " + completedCount + event.getWork());
            }
        }
        public void workAccepted(WorkEvent event) {
                System.out.println("WorkListener Accepted " + event.getWork());
        }
        public void workStarted (WorkEvent event) {
                System.out.println("WorkListener Started " + event.getWork());
        }

        public void workRejected(WorkEvent event) {
            rejectedCount++;
            System.out.println("Worklistener Rejected " + rejectedCount +  event.getWork());
            Work w = event.getWork();
            if (w instanceof ActualWork) {
                event.getException().printStackTrace();
            } else {
                error = true;
            } 
        }
    }

    class SetupWork implements Work {

        boolean released = false;
        int id;
        public SetupWork(int id) {
          this.id = id;
        }
        public void release() {
            released = true;
        }
        public String toString(){
              return "Setup Work :: " + id;
        }

        public void run() {
            debug("Setup WORK "+id+"is RUNNING");
            try {
                while (true) {
                    if (released) {
                        break;
                    } else {
                        synchronized (this) {
                            wait(500);
                        }
                        //Thread.sleep(500);
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
            debug("Setup WORK " + id + " is COMPLETED");
        }
    }

    class ActualWork implements Work {
        int id = 0;
        public ActualWork(int id) {
            this.id = id;
        }
        public String toString(){
              return "Actual Work :: " + id;
        }
        public void release() {
        }
        public void run() {
           synchronized (this) {
               try { 
                   wait(TIME_OUT+BUFFER);
               } catch (Exception e) {
                   e.printStackTrace();
               }
           }
           debug("Actual TEST " + id + "is COMPLETED");
        }

        public int getId() {
            return id;
        }
    }


}
