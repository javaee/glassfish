/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

/*
 * TestJSR88Concurrency.java
 *
 * Created on September 19, 2006, 3:52 PM
 *
 */

package devtests.deployment.jsr88.apitests;

import com.sun.enterprise.deployapi.SunDeploymentFactory;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;

/**
 *Tests concurrent use of the same deployment connection to the back-end.
 *<p>
 *You can use this program to create multiple threads in the same JVM (all
 *running the same task) or to run multiple copies of the program in different
 *JVMs but synchronized.
 *<p>
 *arguments:
 * --host <host> 
 * --port <port> 
 * --username <username> 
 * --password <password> 
 * --operation <operation>[*<n>] 
 * --startTime <testStartTime> (as H:mm:ss)
 * --delay <delayTime) (in ms)
 * --secure <true or false>
 *<p>
 *where <operation> can be getTargets, getApps, loopGetTargets");
 *  n (default=1) is the number of concurrent threads to share the connection at once
 *  testStartTime is a time in the current day, specified as [H]H:mm:ss  default is to start immediately
 *<p>
 *The getTargets and getApps tasks invoke the corresponding back-end method only
 *once in each thread.  The loopGetTargets is more likely to induce problems if
 *there are race conditions somewhere in the code path, since it invokes the 
 *getTargets method multiple times from each thread.  
 *<p>
 *The system property sleep.time, if set, is used as the delay between when 
 *multiple threads are started.  This is normally not needed but could be used
 *to stagger the requests from the threads that are started to see if doing
 *so avoids any race conditon that is revealed by starting the threads
 *without delay.
 *
 *The classpath must include:
 *  javaee.jar
 *  appserv-deployment-client.jar
 *  appserv-admin.jar
 *
 * @author tjquinn
 */
public class TestJSR88Concurrency {
    
    private DeploymentFactory factory;
    
    private DeploymentManager manager;
    
    private static final String HOST = "--host";
    private static final String PORT = "--port";
    private static final String USERNAME = "--username";
    private static final String PASSWORD = "--password";
    private static final String OPERATION = "--operation";
    private static final String START_TIME = "--startTime";
    private static final String DELAY = "--delay";
    private static final String SECURE = "--secure";
    private static final String TIMEOUT = "--timeout";
    
    /** initialization info for the command-line option names and defaults, if any */
    private static final String[][] optionsAndDefaults = new String[][] 
        { {HOST, "localhost"},
          {PORT, "4848"},
          {USERNAME, "admin"},
          {PASSWORD, "adminadmin"},
          {OPERATION, null}, // no default value
          {START_TIME, null}, // no default value
          {DELAY, "0"},
          {SECURE, "false"},
          {TIMEOUT, "10000"} // 10 seconds
    };
    
    /* command-line option names and values */
    private HashMap<String,String> options;
    
    /* used to control how many iterations of the loopGetTargets will run */
    private static final int LOOP_MAX = 100;
    
    /**
     * Creates a new instance of TestJSR88Concurrency
     */
    public TestJSR88Concurrency() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new TestJSR88Concurrency().run(args);
        } catch (Throwable thr) {
            thr.printStackTrace();
            System.exit(1);
        }
    }
    
    private void run(String[] args) throws DeploymentManagerCreationException, TargetException, InterruptedException, ParseException, UserException {
        try {
            prepareArgs(args);
            factory = initFactory();
            manager = initManager(
                    getHost(), 
                    getPort(), 
                    getUsername(), 
                    getPassword(), 
                    options.get(SECURE).equalsIgnoreCase("true"));

            waitForTargetTime();

            processFunction(getOperation());
        } catch (UserException ue) {
            System.err.println("User error: " + ue.getMessage());
            System.exit(1);
        }
        
    }
    
    private void waitForTargetTime() throws ParseException, InterruptedException {
        if (getTargetTimeText() == null) {
            System.out.println("No start delay specified; continuing immediately");
            return;
        }
        SimpleDateFormat targetTimeFormat = new SimpleDateFormat("H:mm:ss");
        Calendar targetTime = Calendar.getInstance();
        targetTime.setTime(targetTimeFormat.parse(getTargetTimeText()));
        
        Calendar now = Calendar.getInstance();
        targetTime.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        System.out.println("Currently it is " + now.getTime().toString());
        System.out.println("Waiting until   " + targetTime.getTime().toString());
        Thread.sleep(targetTime.getTimeInMillis() - System.currentTimeMillis());
        System.out.println("Proceeding");
    }
    
    private void prepareArgs(String[] args) throws UserException {
        options = initOptionsMap();
        
        int i = 0;
        while (i < args.length) {
            if (options.containsKey(args[i])) {
                options.put(args[i], getRequiredOptionValue(args, ++i));
            } else if (args[i].startsWith("--")) {
                throw new UserException("Unrecognized option " + args[i]);
            }
            i++;
        }
        
        ensureRequiredInfoSupplied();
    }
    
    private void ensureRequiredInfoSupplied() throws UserException {
        if (getOperation() == null) {
            throw new UserException("Expected --operation <operation[*n]> but none was found");
        }
    }
    
    private HashMap<String,String> initOptionsMap() {
        HashMap<String,String> result = new HashMap<String,String>();
        for (String[] optionAndValue : optionsAndDefaults) {
            result.put(optionAndValue[0], optionAndValue[1]);
        }
        return result;
    }
    
    private String getRequiredOptionValue(String[] args, int valuePosition) throws UserException {
        if ((valuePosition >= args.length) || (args[valuePosition].startsWith("--"))) {
            throw new UserException("No value available for " + args[valuePosition - 1]);
        }
        return args[valuePosition];
    }
    
    private void usage() {
        System.out.println("devtests.deployment.jsr88.apitests.TestJSR88Concurrency --host <host> --port <port> --username <username> --password <password> --operation <operation>[*<n>] --startTime testStartTime (as H:mm:ss) --delay <delay-in-ms> --secure true/false");
        System.out.println("  where <operation> can be getTargets, getApps, loopGetTargets");
        System.out.println("  n (default=1) is the number of concurrent threads to share the connection at once");
        System.out.println("  testStartTime is a time in the current day, specified as [H]H:mm:ss  default is to start immediately");
        System.out.println("  delay is the number of milliseconds to wait between starting threads");
    }
    
    private DeploymentFactory initFactory() {
        /*
         *We know we are testing our factory, so just instantiate it.
         */
        return new SunDeploymentFactory();
    }
    
    private DeploymentManager initManager(
            String host, 
            String port, 
            String username, 
            String password,
            boolean secure) throws DeploymentManagerCreationException {
        String url = "deployer:Sun:AppServer::" + host + ":" + port + getConnectionStringSuffix(secure);
        return factory.getDeploymentManager(url, username, password);
    }

    private void processFunction(String function) throws TargetException, InterruptedException {
        /*
         *The star, if present, separates the operation name from the number of
         *threads on which to run that operation.
         */
        String[] pieces = function.split("\\*");
        long delay = getDelay();
        int parallelThreadCount = 1;
        if (pieces.length > 1) {
            parallelThreadCount = Integer.parseInt(pieces[1]);
        }
        if (parallelThreadCount == 1) {
            performFunction(pieces[0], 1);
        } else {
            Thread[] threads = new Thread[parallelThreadCount];
            AtomicBoolean[] threadOK = new AtomicBoolean[parallelThreadCount];
            for (int i = 0; i < parallelThreadCount; i++) {
                threadOK[i] = new AtomicBoolean();
                threads[i] = new Thread(new Runner(pieces[0], i, threadOK[i]));
                threads[i].start();
                /*
                 *No need to wait if we just started the last thread.
                 */
                if (delay != 0 && i < parallelThreadCount - 1) {
                    Thread.currentThread().sleep(delay);
                }
            }
            boolean allThreadsOK = true;
            for (int i = 0; i < parallelThreadCount; i++) {
                Thread t = threads[i];
                t.join(getTimeout());
                allThreadsOK &= threadOK[i].get();
            }
            
            if ( ! allThreadsOK) {
                throw new RuntimeException("At least one thread failed");
            }
        }
    }
    
    private void printTargets(int threadID) {
        
        System.out.println("[" + threadID + "]Targets:");
        for (Target t : getTargets()) {
            System.out.println("[" + threadID + "]  " + t.getName());
        }
        System.out.println();
    }
    
    private Target[] getTargets() {
        return manager.getTargets();
    }
    
    private TargetModuleID[] getApps() throws TargetException {
        Target[] targets = getTargets();
        ModuleType[] types = new ModuleType[] {ModuleType.EJB, ModuleType.EAR, ModuleType.CAR, ModuleType.RAR};
        List<TargetModuleID> result = new ArrayList<TargetModuleID>();
        for (ModuleType mt : types) {
            result.addAll(Arrays.asList(manager.getAvailableModules(mt, targets)));
        }
        return result.toArray(new TargetModuleID[result.size()]);
    }
    
    private void printApps(int threadID) throws TargetException {
        System.out.println("[" + threadID + "]Applications:");
        for (TargetModuleID id : getApps()) {
            System.out.println("[" + threadID + "]  " + id.getModuleID());
        }
        System.out.println("[" + threadID + "]");
    }
    
    private void loopGetTargets() {
        getTargets(); // to warm up the connection
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < LOOP_MAX; i++) {
            getTargets();
        }
        System.out.println((System.currentTimeMillis() - startTime));
    }
    
    private void performFunction(String function, int threadID) throws TargetException {
        if (function.equals("getTargets")) {
            printTargets(threadID);
        } else if (function.equals("getApps")) {
            printApps(threadID);
        } else if (function.equals("loopGetTargets")) {
            loopGetTargets(); 
        } else {
            System.err.println("No recognized function in thread " + threadID);
        }
    }
    
    private void multiGetApps() {
        
    }
    
    private String getStartTimeText() {
        return options.get(START_TIME);
    }

    private String getHost() {
        return options.get(HOST);
    }

    private String getPort() {
        return options.get(PORT);
    }

    private String getUsername() {
        return options.get(USERNAME);
    }

    private String getPassword() {
        return options.get(PASSWORD);
    }

    private String getOperation() {
        return options.get(OPERATION);
    }

    private String getTargetTimeText() {
        return options.get(START_TIME);
    }

    private long getDelay() {
        return Long.parseLong(options.get(DELAY));
    }
    
    private String getConnectionStringSuffix(boolean secure) {
        return secure ? ":https" : "";
    }

    private long getTimeout() {
        return Long.parseLong(options.get(TIMEOUT));
    }
    
    private class Runner implements Runnable {
        
        private String function;
        private int threadID;
        private AtomicBoolean result;
        
        public Runner(String function, int threadID, AtomicBoolean result) {
            Runner.this.function = function;
            this.threadID = threadID;
            this.result = result;
        }
        
        public void run() {
            try {
                performFunction(function, threadID);
                result.set(true);
            } catch (Throwable thr) {
                result.set(false);
                synchronized (TestJSR88Concurrency.this) {
                    System.err.println(Thread.currentThread().getName());
                    thr.printStackTrace();
                }
            }
            
        }
    }
    
    /**
     *Indicates a user error, such as a missing command-line value, that should
     *be displayed without a stack trace.
     */
    public class UserException extends Exception {
        public UserException(String msg) {
            super(msg);
        }
    }
}
