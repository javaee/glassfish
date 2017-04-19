/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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
package eetimer;

import java.util.Map;
import java.util.logging.Level;

public class MigrateTimersTest extends TimerTestBase {

    public static void main(String[] args) {
        (new MigrateTimersTest()).runTests();
    }

    @Override
    protected String getTestDescription() {
        return "devtests for migrate-timers";
    }

    public void runTests() {
        try {
            deployEjbCreateTimers(cluster_name);
            migrateTimers();
            migrateTimersWithTarget();
            migrateTimersOutsideCluster();
        } finally {
            //all associated timers will be removed upon undeploy, even if some
            //instances are offline. 
            undeployEjb(cluster_name);
        }
        stat.printSummary();
    }

    //--target not specified, default to "server", should pick a running instance
    //from the same cluster
    public void migrateTimers() {
        String testName = "migrateTimers";
        
        //no automatic migration when stopping a instance since gms has been disabled
        asadmin("stop-instance", instance_name_1);
        AsadminReturn output = asadminWithOutput("migrate-timers", instance_name_1);
        logger.log(Level.INFO, "Finished migrate-timer: {0}", new Object[]{output.outAndErr});

        output = asadminWithOutput("list-timers", cluster_name);
        Map<String, Integer> timerCounts = countInstanceTimers(output.out);
        report(testName + instance_name_1 + "-0", timerCounts.get(instance_name_1) == 0);
        report(testName + instance_name_2 + "-2", timerCounts.get(instance_name_2) == 2);
    }

    public void migrateTimersWithTarget() {
        String testName = "migrateTimersWithTarget";

        //no automatic migration when stopping a instance since gms has been disabled
        asadmin("stop-instance", instance_name_2);
        asadmin("start-instance", instance_name_1);
        AsadminReturn output = asadminWithOutput("migrate-timers", "--target", instance_name_1 ,instance_name_2);
        logger.log(Level.INFO, "Finished migrate-timer: {0}", new Object[]{output.outAndErr});

        output = asadminWithOutput("list-timers", cluster_name);
        Map<String, Integer> timerCounts = countInstanceTimers(output.out);

        //3 timers in instance_1: 2 migrated from instance_2, 1 created after restart
        report(testName + instance_name_1 + "-3", timerCounts.get(instance_name_1) == 3);
        report(testName + instance_name_2 + "-0", timerCounts.get(instance_name_2) == 0);
    }

    public void migrateTimersOutsideCluster() {
        String testName = "migrateTimersOutsideCluster";
        
        //no automatic migration when stopping a instance since gms has been disabled
        asadmin("stop-instance", instance_name_1);
        asadmin("start-instance", instance_name_3);
        AsadminReturn output = asadminWithOutput("migrate-timers", "--target", instance_name_3, instance_name_1);
        logger.log(Level.INFO, "Finished migrate-timer: {0}", new Object[]{output.outAndErr});
        report(testName, output.returnValue == false);
    }
}
