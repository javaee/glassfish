/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2003-2017 Oracle and/or its affiliates. All rights reserved.
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

package devtests.deployment.jsr88.apitests;

import java.io.*;
import java.util.*;
import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.shared.ModuleType;
import devtests.deployment.util.JSR88Deployer;

public class TestClient {

        /**
         *system property (integer) specifying the sleep delay to introduce into ProgressobjectImpl event notification
         *to make sure a possible race condition does not occur.  The property also has a default value.
         */
        private final String SLEEP_TIME_PROPERTY_NAME = "devtests.sleepDurationForProgressObjectImplRaceTest";
        private final Integer SLEEP_TIME_DEFAULT = new Integer(4000);

        JSR88Deployer depl = null;

    private void initDeployer(String[] args) throws Exception {
        log("Getting access to JSR88 deployer: using URI = " + args[1]);
        depl = new JSR88Deployer(args[1], args[2], args[3]);
    }

    private boolean checkIfAppsArePresent(TargetModuleID[] reportedApps, HashMap expectedApps) {
        for(int i=0; i<reportedApps.length; i++) {
            String modId = reportedApps[i].getModuleID();
            log("Reported application  name = " + modId);
            if(expectedApps.containsKey(modId))
                expectedApps.remove(modId);
        }
        if(expectedApps.size() != 0) {
            log("Not all expected applications have been reported");
            return(false);
        }
        log("All expected applications have been reported");
        return(true);
    }
    
    private boolean checkReportedApps(String[] args, Boolean state)
                                                    throws Exception {
        log("Checking if application names reported are valid");

        HashMap set = new HashMap();
        set.put(args[4], "app");
        set.put(args[5], "app");
        set.put(args[6], "app");

        TargetModuleID[] apps = depl.getAllApplications(state);
        log("Total count = " + apps.length);
        return(checkIfAppsArePresent(apps, set));
    }

    private int testProgressObjectImplRace(String[] args) throws Exception {
        /*
         *This test works like testStartStop except that it uses only one application and sets the system
         *property to introduce a sleep into listener notification to test race conditions.  See ProgressObjectImpl
         *for more details on the possible problem and how the test probes for it.
         */
        String appOfInterest = args[4];
        boolean appIsRunning = true;

        try {
            /*
             *The system property to control the test code in ProgressObjectImpl could have been set on the
             *command line that ran this test.  If not, use the default value.
             */
            Integer sleepTime = Integer.getInteger(SLEEP_TIME_PROPERTY_NAME, SLEEP_TIME_DEFAULT);
            log("ProgressObjectImpl race test is using " + SLEEP_TIME_PROPERTY_NAME + " = " + sleepTime);

            /*
             *Stop the first application specified on the command line.
             */
            log("Stopping application " + appOfInterest);
            if (depl.stop(appOfInterest) == null) {
                log("Failed to stop " + appOfInterest);
                return -1;
            }
            log("Stop request completed");

            /*
             *Wait a while for the stop operation to complete and the events to be delivered.
             */
            try {
                Thread.currentThread().sleep(sleepTime.intValue());
            } catch (InterruptedException ie) {
                log("ProgressObjectImpl race test was interrupted during a sleep waiting for the app to stop.");
                return -1;
            }

            appIsRunning = false;

            /*
             *Now that the app is stopped, start it and pay attention to the events.
             */
            log("Starting application " + appOfInterest);
            depl.clearReceivedEvents();
            if (depl.start(appOfInterest, sleepTime.intValue() / 2 ) == null) {
                log("Failed to start " + appOfInterest);
                return -1;
            }

            appIsRunning = true;

            /*
             *Wait before retrieving the events to give the start operation time to complete.
             */

            try {
                Thread.currentThread().sleep(sleepTime.intValue());
            } catch (InterruptedException ie) {
                throw new RuntimeException("TestClient was interrupted waiting for start operation events to accumulate before retrieving them.", ie);
            }

            log("About to retrieve events");
            ProgressEvent receivedEvents[] = depl.getReceivedEvents();

            /*
             *We expect two events, one corresponding to the start operation entering the "running" state and
             *one for the operation completing.  If the race condition has occurred, then we won't see both of 
             *the events.  We're going to look at the first one, expecting it to be the "running" event.
             */
            if (receivedEvents.length < 2) {
                log("ProgressObjectImpl race test expected two events (running and completed), but found " + receivedEvents.length + ".");
                if (receivedEvents.length == 1) {
                    log("...and the event that was recorded was " + receivedEvents[0].getDeploymentStatus().getMessage());
                }
                return -1;
            }

            ProgressEvent event = receivedEvents[0];
            DeploymentStatus dStatus = event.getDeploymentStatus();

            if ( ! dStatus.isRunning()) {
                log("ProgressObjectImpl race test's first recorded event was not what was expected; expected running but found " + dStatus.getMessage());
                return -1;
            } else {
                log("ProgressObjectImpl race test completed normally.");
                return 0;
            }

        } finally {
            /*
             *Restart the application if it isn't running.
             */
            if ( ! appIsRunning) {
                log("Restarting application " + appOfInterest);
                if (depl.start(appOfInterest) == null) {
                    log("Failed to restart application " + appOfInterest);
                    return -1;
                }
            }
        }
    }
        
    private int testStartStop(String[] args) {
        try {
            log("Stopping all Applications");
            for(int i=4; i<7; i++) {
                if(depl.stop(args[i]) == null) {
                    log("Failed to stop " + args[i]);
                    return(-1);
                }
            }
            boolean ret = checkReportedApps(args, Boolean.FALSE);
            if(!ret)
                return(-1);
            log("Starting all Applications");
            for(int j=4; j<7; j++) {
                if (depl.start(args[j]) == null) {;
                    log("Failed to start " + args[j]);
                    return(-1);
                }
            }
            ret = checkReportedApps(args, Boolean.TRUE);
            if(!ret)
                return(-1);
        } catch(Exception e) {
            log("testStartStop recd exception : " + e.getMessage());
            return(-1);
        }
        return(0);
    }

    private int getModulesByType(String args[], ModuleType type, Boolean state) {
        log("Getting list of Applications with type = " + type.toString());
        log("Required running state = " + state);
        try {
            TargetModuleID[] apps = depl.getApplications(type, state);
            log("Total count = " + apps.length);

            HashMap set = new HashMap();
            set.put(args[4], "app");
            
            boolean ret = checkIfAppsArePresent(apps, set);
            if(!ret)
                return(-1);
        } catch(Exception e) {
            log("getModulesByType recd exception : " + e.getMessage());
            return(-1);
        }
        return(0);
    }

    private int testEmptyModule() {
        try {
            TargetModuleID[] apps = depl.getApplications(ModuleType.WAR, null);
            if(apps.length != 0) {
                log("# of WARs expected = 0");
                log("# of WARs reported = " + apps.length);
                return(-1);
            }
            apps = depl.getApplications(ModuleType.EJB, null);
            if(apps.length != 0) {
                log("# of EJB-JARs expected = 0");
                log("# of EJB-JARs reported = " + apps.length);
                return(-1);
            }
            apps = depl.getApplications(ModuleType.EAR, null);
            if(apps.length != 0) {
                log("# of EARs expected = 0");
                log("# of EARs reported = " + apps.length);
                return(-1);
            }
        } catch(Exception e) {
            log("testEmptyModule recd exception : " + e.getMessage());
            return(-1);
        }
        return(0);
    }

    public static void main(String[] args) {
        int exitVal = 0;
        TestClient client = new TestClient();
        try {
            int testCase = (new Integer(args[0])).intValue();
            client.initDeployer(args);
            switch(testCase) {
                case 1 :
                    exitVal = client.getModulesByType(args,ModuleType.WAR,null);
                    break;
                case 2 :
                    exitVal = client.getModulesByType(args,ModuleType.EJB,null);
                    break;
                case 3 :
                    exitVal = client.getModulesByType(args,ModuleType.EAR,null);
                    break;
                case 4 :
                    exitVal = client.testStartStop(args);
                    break;
                case 5 :
                    exitVal = client.testEmptyModule();
                    break;
                case 6 :
                        exitVal = client.testProgressObjectImplRace(args);
                        break;
                default:
                    log("Wrong test number given !!!");
                    exitVal = -1;
                    break;
            }
        } catch (Exception e) {
            log("Caught Exception = " + e.getMessage());
            System.exit(-1);
        }
        System.exit(exitVal);
    }

    private static void log(String message) {
        System.out.println("[JSR88APITest]:: " + message);
    }
}
